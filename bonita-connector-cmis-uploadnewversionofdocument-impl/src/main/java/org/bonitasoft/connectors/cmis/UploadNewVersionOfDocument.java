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
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.connector.ConnectorException;

public class UploadNewVersionOfDocument extends AbstractCMISConnector {

    public static final String DOCUMENT_ID_OUTPUT = "document_id";

    public static final String DOCUMENT = "document";

    public static final String REMOTE_DOCUMENT = "remote_document";

    private ProcessAPI processApi;

    private Object document;

    private String remote_document;

    @Override
    public void setInputParameters(final Map<String, Object> parameters) {
        super.setInputParameters(parameters);
        document =  parameters.get(DOCUMENT);
        remote_document = (String) parameters.get(REMOTE_DOCUMENT);
    }


    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        final AbstractCmisClient cmisClient = getClient();
        if (cmisClient == null) {
            throw new ConnectorException("CMIS UploadNewVersionOfDocument connector is not connected properly.");
        }
        if (!cmisClient.checkIfObjectExists(remote_document)) {
            throw new ConnectorException("Remote document "+remote_document+" doesn't exists !");
        }

        processApi = getAPIAccessor().getProcessAPI();
        final Document doc = getDocument(document, processApi);
        final byte[] documentContent = getDocumentContent(doc);
        final String documentId = cmisClient.uploadNewVersionOfDocument(remote_document, documentContent, doc.getContentMimeType()).getId();
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

    private Document getDocument(Object attachment, ProcessAPI processAPI) throws ConnectorException {
        try {
            if (attachment instanceof String) {
                String docName = (String) attachment;
                long processInstanceId = getExecutionContext().getProcessInstanceId();
                return processAPI.getLastDocument(processInstanceId, docName);
            } else {
                //Already checked in CMISParametersValidator
                return (Document) attachment;
            }

        }catch( DocumentNotFoundException e){
            throw new ConnectorException("Document is not found ", e);
        }
    }
}
