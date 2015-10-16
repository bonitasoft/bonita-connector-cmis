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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.bonitasoft.connectors.cmis.cmisclient.AbstractCmisClient;
import org.bonitasoft.engine.connector.ConnectorException;

public class ListDocuments extends AbstractCMISConnector {

    public static final String FOLDER_PATH = "folder_path";

    public static final String FOLDER_DOCUMENTS = "folder_documents";

    private String folderPath;

    
    @Override
    public void executeBusinessLogic() throws ConnectorException {
        final AbstractCmisClient cmisClient = getClient();
        try {
            if (cmisClient != null) {
                final Folder folder = cmisClient.getFolderByPath(folderPath);
                final ItemIterable<CmisObject> documents = folder.getChildren();
                final List<CmisObject> documentList = new ArrayList<CmisObject>();
                for (CmisObject document : documents)
                	documentList.add(document);
                setOutputParameter(FOLDER_DOCUMENTS, documentList);
            }
        } finally {
            if (!getOutputParameters().containsKey(FOLDER_DOCUMENTS)) {
                setOutputParameter(FOLDER_DOCUMENTS, null);
            }
        }
    }

    @Override
    public void setInputParameters(final Map<String, Object> parameters) {
        super.setInputParameters(parameters);
        final Logger logger = Logger.getLogger(this.getClass().toString());
        folderPath = (String) parameters.get(FOLDER_PATH);
        logger.log(Level.ALL, FOLDER_PATH);
    }
}
