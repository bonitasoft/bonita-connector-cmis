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
package org.bonitasoft.connectors.cmis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.bonitasoft.connectors.cmis.cmisclient.AbstractCmisClient;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

public class DeleteVersionOfDocument extends AbstractCMISConnector {

    public static final String DOCUMENT_PATH = "documentPath";

    public static final String VERSION_LABEL = "versionLabel";

    public static final String IS_DOCUMENT_VERSION_DELETED_OUTPUT = "isDocumentVersionDeleted";

    private String documentPath;

    private String versionLabel;


    @Override
    public void setInputParameters(final Map<String, Object> parameters) {
        super.setInputParameters(parameters);
        documentPath = (String) parameters.get(DOCUMENT_PATH);
        versionLabel = (String) parameters.get(VERSION_LABEL);
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        final AbstractCmisClient cmisClient = getClient();
        if (cmisClient == null) {
            throw new ConnectorException("CMIS DeleteVersionOfDocument connector is not connected properly.");
        }
        try {
            cmisClient.deleteVersionOfDocumentByLabel(documentPath, versionLabel);
        } finally {
            try {
                final CmisObject cmisObject = cmisClient.getObjectByPath(documentPath);
                if (cmisObject instanceof Document) {
                    boolean versionExists = false;
                    for (final Document versionedDocument : ((Document) cmisObject).getAllVersions()) {
                        if (versionLabel.equals(versionedDocument.getVersionLabel())) {
                            versionExists = true;
                        }
                    }
                    setOutputParameter(IS_DOCUMENT_VERSION_DELETED_OUTPUT, !versionExists);
                }
            } catch (final CmisObjectNotFoundException ex) {
                setOutputParameter(IS_DOCUMENT_VERSION_DELETED_OUTPUT, Boolean.TRUE);
            }

        }
    }

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        super.validateInputParameters();
        final Map<String, Object> parameters = getInputParameters();
        final String bindingType = (String) parameters.get(BINDING);
        if (AbstractCMISConnector.WEBSERVICE.equals(bindingType)) {
            final List<String> errors = new ArrayList<String>(0);
            final String wsVersioningServiceUrl = (String) parameters.get(AbstractCMISConnector.WEBSERVICES_VERSIONING_SERVICE);
            final String wsVersioningServiceEndpointUrl = (String) parameters.get(AbstractCMISConnector.WEBSERVICES_VERSIONING_SERVICE_ENDPOINT);
            if ((wsVersioningServiceUrl == null || wsVersioningServiceUrl.isEmpty())
                    && (wsVersioningServiceEndpointUrl == null || wsVersioningServiceEndpointUrl.isEmpty())) {
                errors.add("VersioningService URL or Endpoint is not set");
            }

            final String wsNavServiceUrl = (String) parameters.get(AbstractCMISConnector.WEBSERVICES_NAVIGATION_SERVICE);
            final String wsNavServicEndpointUrl = (String) parameters.get(AbstractCMISConnector.WEBSERVICES_NAVIGATION_SERVICE_ENDPOINT);
            if ((wsNavServiceUrl == null || wsNavServiceUrl.isEmpty())
                    && (wsNavServicEndpointUrl == null || wsNavServicEndpointUrl.isEmpty())) {
                errors.add("NavigationService URL or Endpoint is not set");
            }
            if (!errors.isEmpty()) {
                throw new ConnectorValidationException(this, errors);
            }
        }
    }

}
