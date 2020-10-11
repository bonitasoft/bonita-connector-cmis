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
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.connector.ConnectorException;

public class UploadNewDocument extends AbstractCMISConnector {

    public static final String DOCUMENT_ID_OUTPUT = "document_id";
    public static final String DOCUMENT = "document";
    public static final String FOLDER_PATH = "folder_path";
    public static final String DESTINATION_NAME = "destinationName";

    private ProcessAPI processApi;
    private String document;
    private String folder_path;
    private String destinationName;


    @Override
    public void setInputParameters(final Map<String, Object> parameters) {
        super.setInputParameters(parameters);
        document = (String) parameters.get(DOCUMENT);
        folder_path = (String) parameters.get(FOLDER_PATH);
        destinationName = (String) parameters.get(DESTINATION_NAME);
    }


    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        processApi = getAPIAccessor().getProcessAPI();
        final AbstractCmisClient cmisClient = getClient();
        if (cmisClient == null) {
            throw new ConnectorException("CMIS UploadNewDocument connector is not connected properly.");
        }
        final Document doc = getDocument(document);
        final byte[] documentContent = getDocumentContent(doc);
        final String documentId = cmisClient.uploadNewDocument(folder_path, destinationName, documentContent, doc.getContentMimeType()).getId();

        setOutputParameter(DOCUMENT_ID_OUTPUT, documentId);
    }

    private byte[] getDocumentContent(final Document doc) throws ConnectorException {
        byte[] documentContent;
        try {
            documentContent = processApi.getDocumentContent(doc.getContentStorageId());
        } catch (final DocumentNotFoundException e) {
            throw new ConnectorException("Failed to retrieve document content for " + doc.getName(), e);
        }
        return documentContent;
    }

    private Document getDocument(final String document) throws ConnectorException {
        Document doc;
        try {
            doc = processApi.getLastDocument(getExecutionContext().getProcessInstanceId(), document);
        } catch (final DocumentNotFoundException e) {
            throw new ConnectorException("Failed to retrieve document "+document, e);
        }
        return doc;
    }

}
