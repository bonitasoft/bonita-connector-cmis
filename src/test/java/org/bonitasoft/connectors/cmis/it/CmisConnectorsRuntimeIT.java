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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.bonitasoft.connectors.cmis.AbstractCMISConnector;
import org.bonitasoft.connectors.cmis.CreateFolder;
import org.bonitasoft.connectors.cmis.DownloadDocument;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

/**
 * Runs the packaged connectors (the zips, as imported in the Studio) in processes deployed on a Bonita runtime, against
 * an OpenCMIS InMemory server, with both bindings. Catches classes missing at runtime (e.g. a dependency expected from
 * the runtime) that compile time and unit tests cannot see.
 * <p>
 * Needs Docker and the packaged connectors: run with {@code ./mvnw verify}, or from the IDE after {@code ./mvnw package}.
 */
@Testcontainers
class CmisConnectorsRuntimeIT {

    private static final String REPOSITORY = "Apache Chemistry OpenCMIS InMemory Repository";
    private static final String CMIS_URL = "http://cmis:8080/cmis";
    private static final String CONTENT = "Hello from Bonita 12 ✓";
    private static final String DOWNLOADED_CONTENT = "downloaded_content";

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

    @ParameterizedTest
    @ValueSource(strings = { AbstractCMISConnector.ATOMPUB, AbstractCMISConnector.WEBSERVICE })
    void should_create_folder(String binding) throws Exception {
        var inputs = connectionInputs(binding);
        inputs.put(CreateFolder.PARENT_FOLDER_PATH, "/");
        inputs.put(CreateFolder.FOLDER_NAME, "folder-" + binding);

        long caseId = runProcess("CreateFolder-" + binding, List.of(
                new ConnectorCall("cmis-createfolder", "2.0.1", inputs,
                        CreateFolder.FOLDER_ID_OUTPUT, output(CreateFolder.FOLDER_ID_OUTPUT, String.class))));

        assertThat(BONITA.getProcessVariableValue(caseId, CreateFolder.FOLDER_ID_OUTPUT)).isNotBlank();
    }

    /**
     * Content goes through the binding (MTOM attachment with the web services binding). The document is created from
     * the test: the upload connectors always create a major version of cmis:document, which no lightweight CMIS server
     * accepts (cmis:document is not versionable in the OpenCMIS InMemory and FileShare servers).
     */
    @ParameterizedTest
    @ValueSource(strings = { AbstractCMISConnector.ATOMPUB, AbstractCMISConnector.WEBSERVICE })
    void should_download_document(String binding) throws Exception {
        var remoteName = "document-" + binding + ".txt";
        createRemoteDocument(remoteName, CONTENT);
        var inputs = connectionInputs(binding);
        inputs.put(DownloadDocument.REMOTE_DOCUMENT_PATH, "/" + remoteName);

        long caseId = runProcess("DownloadDocument-" + binding, List.of(
                new ConnectorCall("cmis-downloaddocument", "2.0.1", inputs,
                        DOWNLOADED_CONTENT, new ExpressionBuilder().createGroovyScriptExpression("contentAsString",
                                "new String(" + DownloadDocument.DOCUMENT_OUTPUT + ".content, 'UTF-8')", String.class.getName(),
                                output(DownloadDocument.DOCUMENT_OUTPUT, DocumentValue.class)))));

        assertThat(BONITA.getProcessVariableValue(caseId, DOWNLOADED_CONTENT)).isEqualTo(CONTENT);
    }

    @Test
    void impl_jar_dependencies_should_match_zip_classpath() throws Exception {
        try (var zip = new ZipFile(packagedZip("createfolder-impl").toFile())) {
            var impl = new String(zip.getInputStream(zip.getEntry("cmis-createfolder.impl")).readAllBytes(),
                    StandardCharsets.UTF_8);
            var declared = Pattern.compile("<jarDependency>(.*?)</jarDependency>").matcher(impl).results()
                    .map(m -> m.group(1)).collect(Collectors.toSet());

            assertThat(declared).isEqualTo(classpathJars(zip));
        }
    }

    /** A connector of the process under test, its output stored in a String process variable. */
    private record ConnectorCall(String definitionId, String definitionVersion, Map<String, String> inputs,
            String outputVariable, Expression outputValue) {
    }

    private static Map<String, String> connectionInputs(String binding) {
        var inputs = new HashMap<String, String>();
        inputs.put(AbstractCMISConnector.USERNAME, "test");
        inputs.put(AbstractCMISConnector.PASSWORD, "test");
        inputs.put(AbstractCMISConnector.REPOSITORY, REPOSITORY);
        inputs.put(AbstractCMISConnector.BINDING, binding);
        if (AbstractCMISConnector.ATOMPUB.equals(binding)) {
            inputs.put(AbstractCMISConnector.URL, CMIS_URL + "/atom11");
        } else {
            inputs.put(AbstractCMISConnector.WEBSERVICES_OBJECT_SERVICE, CMIS_URL + "/services/ObjectService?wsdl");
            inputs.put(AbstractCMISConnector.WEBSERVICES_REPOSITORY_SERVICE, CMIS_URL + "/services/RepositoryService?wsdl");
        }
        return inputs;
    }

    /** Creates a non-versioned document at the repository root, through AtomPub from the test JVM. */
    private static void createRemoteDocument(String name, String content) {
        // mutable: OpenCMIS adds its defaults to the parameters
        var session = SessionFactoryImpl.newInstance().createSession(new HashMap<>(Map.of(
                SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value(),
                SessionParameter.ATOMPUB_URL, String.format("http://%s:%s/cmis/atom11", CMIS_SERVER.getHost(), CMIS_SERVER.getFirstMappedPort()),
                SessionParameter.REPOSITORY_ID, "A1",
                SessionParameter.USER, "test",
                SessionParameter.PASSWORD, "test")));
        var bytes = content.getBytes(StandardCharsets.UTF_8);
        session.getRootFolder().createDocument(
                Map.of(PropertyIds.OBJECT_TYPE_ID, "cmis:document", PropertyIds.NAME, name),
                new ContentStreamImpl(name, BigInteger.valueOf(bytes.length), "text/plain", new ByteArrayInputStream(bytes)),
                VersioningState.NONE);
    }

    /** Reference to a connector output, as the Studio maps it. */
    private static Expression output(String name, Class<?> type) throws Exception {
        return new ExpressionBuilder().createInputExpression(name, type.getName());
    }

    /** Deploys and starts a process running the connectors on enter, waits for them and returns the case id. */
    private static long runProcess(String processName, List<ConnectorCall> connectors) throws Exception {
        var process = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        process.addActor("system");
        for (var call : connectors) {
            process.addData(call.outputVariable(), String.class.getName(), null);
            var connector = process.addConnector(call.definitionId(), call.definitionId(), call.definitionVersion(),
                    ConnectorEvent.ON_ENTER);
            for (var input : call.inputs().entrySet()) {
                connector.addInput(input.getKey(), new ExpressionBuilder().createConstantStringExpression(input.getValue()));
            }
            connector.addOutput(new OperationBuilder().createSetDataOperation(call.outputVariable(), call.outputValue()));
        }
        // keeps a successful case started; a failing connector puts it in error
        process.addUserTask("wait", "system");

        var bar = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process.done());
        try (var zip = new ZipFile(packagedZip("all").toFile())) {
            var classpath = classpathJars(zip);
            for (var entry : zip.stream().toList()) {
                var name = Path.of(entry.getName()).getFileName().toString();
                if (name.endsWith(".impl")) {
                    bar.addConnectorImplementation(new BarResource(name, zip.getInputStream(entry).readAllBytes()));
                } else if (classpath.contains(name)) {
                    bar.addClasspathResource(new BarResource(name, zip.getInputStream(entry).readAllBytes()));
                }
            }
        }
        var actor = new Actor("system");
        actor.addUser(BonitaContainer.DEFAULT_USER);
        var actorMapping = new ActorMapping();
        actorMapping.addActor(actor);
        BONITA.installProcess(bar.setActorMapping(actorMapping).done());

        long caseId = BONITA.startProcess(processName);
        await().atMost(Duration.ofMinutes(1))
                .until(() -> BONITA.getState(caseId), state -> Set.of("started", "error").contains(state));
        assertThat(BONITA.getState(caseId)).as("case state (see Bonita logs on error)").isEqualTo("started");
        return caseId;
    }

    private static Set<String> classpathJars(ZipFile zip) {
        return zip.stream()
                .map(e -> e.getName())
                .filter(name -> name.startsWith("classpath/") && name.endsWith(".jar") && !name.endsWith("-sources.jar"))
                .map(name -> name.substring("classpath/".length()))
                .collect(Collectors.toSet());
    }

    /** The zip built by the assembly plugin, found by its suffix so the test needs no version (runs from the IDE). */
    private static Path packagedZip(String assemblyId) throws IOException {
        var matches = new ArrayList<Path>();
        try (DirectoryStream<Path> zips = Files.newDirectoryStream(Path.of("target"), "bonita-connector-cmis-*-" + assemblyId + ".zip")) {
            zips.forEach(matches::add);
        } catch (java.nio.file.NoSuchFileException e) {
            // no target directory: reported below
        }
        assertThat(matches).as("packaged connector (run ./mvnw package first)").hasSize(1);
        return matches.get(0);
    }

}
