/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.connectors.cmis;

import java.util.Map;

import org.bonitasoft.connectors.cmis.cmisclient.AbstractCmisClient;
import org.bonitasoft.engine.connector.ConnectorException;

public class DeleteFolder extends AbstractCMISConnector {

    public static final String FOLDER_PATH = "folder_path";

    public static final String IS_FOLDER_DELETED_OUTPUT = "isFolderDeleted";

    private String folderPath;

    @Override
    public void setInputParameters(final Map<String, Object> parameters) {
        super.setInputParameters(parameters);
        folderPath = (String) parameters.get(FOLDER_PATH);
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        final AbstractCmisClient cmisClient = getClient();
        if (cmisClient == null) {
            throw new ConnectorException("CMIS DeleteFolder connector is not connected properly.");
        }
        if (!cmisClient.checkIfObjectExists(folderPath)) {
            throw new ConnectorException("Folder " + folderPath + " does not exist!");
        }
        try {
            cmisClient.deleteFolderByPath(folderPath);
        } finally {
            setOutputParameter(IS_FOLDER_DELETED_OUTPUT, !cmisClient.checkIfObjectExists(folderPath));
        }
    }

}
