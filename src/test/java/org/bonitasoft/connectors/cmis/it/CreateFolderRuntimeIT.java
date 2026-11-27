/*
 * Copyright (C) 2009 - 2020 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.bonitasoft.connectors.cmis.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.bonitasoft.connectors.cmis.AbstractCMISConnector;
import org.bonitasoft.connectors.cmis.CreateFolder;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

/**
 * Runs the packaged create folder connector (the zip, as imported in the Studio) in a process deployed on a Bonita
 * runtime, against an OpenCMIS InMemory server, with both bindings. Catches classes missing at runtime (e.g. a
 * dependency expected from the runtime) that compile time and unit tests cannot see.
 */
@Testcontainers
class CreateFolderRuntimeIT {

    private static final String REPOSITORY = "Apache Chemistry OpenCMIS InMemory Repository";
    private static final String CMIS_URL = "http://cmis:8080/cmis";

    private static final Network NETWORK = Network.newNetwork();

    @Container
    private static final GenericContainer<?> CMIS_SERVER = new GenericContainer<>("tomcat:9.0-jdk8-temurin")
            .withNetwork(NETWORK)
            .withNetworkAliases("cmis")
            // copied by the maven-dependency-plugin (pre-integration-test)
            .withCopyFileToContainer(MountableFile.forHostPath("target/it/cmis.war"), "/usr/local/tomcat/webapps/cmis.war")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/cmis/atom11").withStartupTimeout(Duration.ofMinutes(2)));

    @Container
    private static final BonitaContainer BONITA = new BonitaContainer().withNetwork(NETWORK);

    @Test
    void should_create_folder_with_atompub_binding() throws Exception {
        var inputs = commonInputs(AbstractCMISConnector.ATOMPUB, "atompub");
        inputs.put(AbstractCMISConnector.URL, CMIS_URL + "/atom11");

        assertThat(createFolder(inputs)).isNotBlank();
    }

    @Test
    void should_create_folder_with_webservices_binding() throws Exception {
        var inputs = commonInputs(AbstractCMISConnector.WEBSERVICE, "webservices");
        inputs.put(AbstractCMISConnector.WEBSERVICES_OBJECT_SERVICE, CMIS_URL + "/services/ObjectService?wsdl");
        inputs.put(AbstractCMISConnector.WEBSERVICES_REPOSITORY_SERVICE, CMIS_URL + "/services/RepositoryService?wsdl");

        assertThat(createFolder(inputs)).isNotBlank();
    }

    @Test
    void impl_jar_dependencies_should_match_zip_classpath() throws Exception {
        try (var zip = new ZipFile(connectorZip().toFile())) {
            var impl = new String(zip.getInputStream(zip.getEntry("cmis-createfolder.impl")).readAllBytes());
            var declared = Pattern.compile("<jarDependency>(.*?)</jarDependency>").matcher(impl).results()
                    .map(m -> m.group(1)).collect(Collectors.toSet());

            assertThat(declared).isEqualTo(classpathJars(zip));
        }
    }

    private static Map<String, String> commonInputs(String binding, String folderName) {
        var inputs = new HashMap<String, String>();
        inputs.put(AbstractCMISConnector.USERNAME, "test");
        inputs.put(AbstractCMISConnector.PASSWORD, "test");
        inputs.put(AbstractCMISConnector.REPOSITORY, REPOSITORY);
        inputs.put(AbstractCMISConnector.BINDING, binding);
        inputs.put(CreateFolder.PARENT_FOLDER_PATH, "/");
        inputs.put(CreateFolder.FOLDER_NAME, folderName);
        return inputs;
    }

    /** Deploys and starts a process running the create folder connector, returns its folder_id output. */
    private static String createFolder(Map<String, String> inputs) throws Exception {
        var processName = "CreateFolder-" + inputs.get(AbstractCMISConnector.BINDING);
        BONITA.installProcess(buildBusinessArchive(processName, inputs));
        long caseId = BONITA.startProcess(processName);

        // the user task keeps a successful case started; a failing connector puts it in error
        await().atMost(Duration.ofMinutes(1))
                .until(() -> BONITA.getState(caseId), state -> Set.of("started", "error").contains(state));
        assertThat(BONITA.getState(caseId)).as("case state (see Bonita logs on error)").isEqualTo("started");
        return BONITA.getProcessVariableValue(caseId, CreateFolder.FOLDER_ID_OUTPUT);
    }

    private static BusinessArchive buildBusinessArchive(String processName, Map<String, String> inputs) throws Exception {
        var process = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        process.addActor("system");
        process.addData(CreateFolder.FOLDER_ID_OUTPUT, String.class.getName(), null);
        var connector = process.addConnector("create-folder", "cmis-createfolder", "2.0.1", ConnectorEvent.ON_ENTER);
        for (var input : inputs.entrySet()) {
            connector.addInput(input.getKey(), new ExpressionBuilder().createConstantStringExpression(input.getValue()));
        }
        connector.addOutput(new OperationBuilder().createSetDataOperation(CreateFolder.FOLDER_ID_OUTPUT,
                new ExpressionBuilder().createDataExpression(CreateFolder.FOLDER_ID_OUTPUT, String.class.getName())));
        process.addUserTask("wait", "system");

        var bar = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process.done());
        try (var zip = new ZipFile(connectorZip().toFile())) {
            for (var entry : zip.stream().filter(e -> !e.isDirectory()).collect(Collectors.toList())) {
                var name = Path.of(entry.getName()).getFileName().toString();
                var content = zip.getInputStream(entry).readAllBytes();
                if (name.endsWith(".impl")) {
                    bar.addConnectorImplementation(new BarResource(name, content));
                } else if (name.endsWith(".jar") && !name.endsWith("-sources.jar")) {
                    bar.addClasspathResource(new BarResource(name, content));
                }
            }
        }
        var actor = new Actor("system");
        actor.addUser(BonitaContainer.DEFAULT_USER);
        var actorMapping = new ActorMapping();
        actorMapping.addActor(actor);
        return bar.setActorMapping(actorMapping).done();
    }

    private static Set<String> classpathJars(ZipFile zip) {
        return zip.stream()
                .map(e -> e.getName())
                .filter(name -> name.startsWith("classpath/") && name.endsWith(".jar") && !name.endsWith("-sources.jar"))
                .map(name -> name.substring("classpath/".length()))
                .collect(Collectors.toSet());
    }

    private static Path connectorZip() {
        return Path.of("target", "bonita-connector-cmis-" + System.getProperty("connector.version") + "-createfolder-impl.zip");
    }

}
