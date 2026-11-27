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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.web.client.BonitaClient;
import org.bonitasoft.web.client.api.ArchivedProcessInstanceApi;
import org.bonitasoft.web.client.api.ProcessApi.SearchProcessesQueryParams;
import org.bonitasoft.web.client.api.ProcessInstanceApi;
import org.bonitasoft.web.client.api.ProcessInstanceVariableApi;
import org.bonitasoft.web.client.exception.NotFoundException;
import org.bonitasoft.web.client.model.UserCreateRequest;
import org.bonitasoft.web.client.services.policies.ProcessImportPolicy;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Bonita runtime to deploy and run processes using the connectors (same pattern as bonita-connector-email).
 */
public class BonitaContainer extends GenericContainer<BonitaContainer> {

    private static final String TECH_USER = "install";
    private static final String TECH_PASSWORD = "install";
    public static final String DEFAULT_USER = "test.user";
    // ponytail: duplicated from the pom's bonita.image property so the IT runs from the IDE; keep both in sync
    private static final String DEFAULT_IMAGE = "bonitasoft.jfrog.io/docker-snapshots/bonita-community:12.0-SNAPSHOT";

    public BonitaContainer() {
        super(System.getProperty("bonita.image", DEFAULT_IMAGE));
        withExposedPorts(8080);
        waitingFor(Wait.forHttp("/bonita").withStartupTimeout(Duration.ofMinutes(5)));
        withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(BonitaContainer.class)));
    }

    @Override
    public void start() {
        super.start();
        call(client -> client.users().createUser(new UserCreateRequest()
                .userName(DEFAULT_USER)
                .password("bpm")
                .passwordConfirm("bpm")
                .firstname("")
                .lastname("")
                .enabled(Boolean.TRUE.toString())));
    }

    public void installProcess(BusinessArchive bar) {
        call(client -> {
            try {
                File processFile = Files.createTempFile("process", ".bar").toFile();
                processFile.delete();
                BusinessArchiveFactory.writeBusinessArchiveToFile(bar, processFile);
                try {
                    client.processes().importProcess(processFile, ProcessImportPolicy.REPLACE_DUPLICATES);
                    return null;
                } finally {
                    processFile.delete();
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public long startProcess(String processName) {
        return call(client -> {
            var processes = client.processes()
                    .searchProcesses(new SearchProcessesQueryParams().p(0).c(1).f(List.of("name=" + processName)));
            if (processes.isEmpty()) {
                throw new IllegalArgumentException(String.format("Process '%s' not found.", processName));
            }
            return Long.valueOf(client.processes().startProcess(processes.get(0).getId(), Map.of()).getCaseId());
        });
    }

    /** State of the case, looked up in the archives once it is no longer active. */
    public String getState(long caseId) {
        return call(client -> {
            try {
                return client.get(ProcessInstanceApi.class).getProcessInstanceById(String.valueOf(caseId), (String) null)
                        .getState().name().toLowerCase();
            } catch (NotFoundException e) {
                return client.get(ArchivedProcessInstanceApi.class)
                        .searchArchivedProcessInstances(new ArchivedProcessInstanceApi.SearchArchivedProcessInstancesQueryParams()
                                .c(1).p(0).f(List.of("caller=any", "sourceObjectId=" + caseId)))
                        .get(0).getState().name().toLowerCase();
            }
        });
    }

    public String getProcessVariableValue(long caseId, String variableName) {
        return call(client -> client.get(ProcessInstanceVariableApi.class)
                .getVariableByProcessInstanceId(String.valueOf(caseId), variableName)
                .getValue());
    }

    private <T> T call(Function<BonitaClient, T> action) {
        var client = BonitaClient.builder(String.format("http://%s:%s/bonita", getHost(), getFirstMappedPort())).build();
        client.login(TECH_USER, TECH_PASSWORD);
        try {
            return action.apply(client);
        } finally {
            client.logout();
        }
    }

}
