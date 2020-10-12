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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.bonitasoft.connectors.cmis.cmisclient.AbstractCmisClient;
import org.bonitasoft.engine.connector.ConnectorException;

public class CreateFolder extends AbstractCMISConnector {

    public static final String PARENT_FOLDER_PATH = "folder_path";

    public static final String FOLDER_NAME = "subfolder_name";

    public static final String FOLDER_ID_OUTPUT = "folder_id";

    private String parentFolderPath;

    private String folderName;

    @Override
    public void executeBusinessLogic() throws ConnectorException {
        final AbstractCmisClient cmisClient = getClient();
        try {
            if (cmisClient != null) {
                final Folder folder = cmisClient.createSubFolder(parentFolderPath, folderName);
                if (folder != null) {
                    setOutputParameter(FOLDER_ID_OUTPUT, folder.getId());
                }
            }
        } finally {
            if (!getOutputParameters().containsKey("folder_id")) {
                setOutputParameter(FOLDER_ID_OUTPUT, null);
            }
        }
    }

    @Override
    public void setInputParameters(final Map<String, Object> parameters) {
        super.setInputParameters(parameters);
        final Logger logger = Logger.getLogger(this.getClass().toString());
        parentFolderPath = (String) parameters.get(PARENT_FOLDER_PATH);
        logger.log(Level.ALL, PARENT_FOLDER_PATH);
        folderName = (String) parameters.get(FOLDER_NAME);
        logger.log(Level.ALL, FOLDER_NAME);
    }
}
