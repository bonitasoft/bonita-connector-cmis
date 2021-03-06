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

import java.util.Map;

import org.bonitasoft.connectors.cmis.cmisclient.AbstractCmisClient;
import org.bonitasoft.engine.connector.ConnectorException;

public class DeleteDocument extends AbstractCMISConnector {

    public static final String IS_DOCUMENT_DELETED_OUTPUT = "isDocumentDeleted";

    public static final String DOCUMENT_PATH = "document_path";

    private String documentPath;


    @Override
    public void setInputParameters(final Map<String, Object> parameters) {
        super.setInputParameters(parameters);
        documentPath = (String) parameters.get(DOCUMENT_PATH);
    }


    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        final AbstractCmisClient cmisClient = getClient();
        if (cmisClient == null) {
            throw new ConnectorException("CMIS DeleteDocument connector is not connected properly.");
        }
        try {
            if (!cmisClient.checkIfObjectExists(documentPath)) {
                throw new ConnectorException("Document " + documentPath + "does not exist!");
            }
            cmisClient.deleteObjectByPath(documentPath);
        } finally {
            setOutputParameter(IS_DOCUMENT_DELETED_OUTPUT, !cmisClient.checkIfObjectExists(documentPath));
        }
    }
}
