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

import org.bonitasoft.connectors.cmis.cmisclient.AbstractCmisClient;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.connector.ConnectorException;

public class UploadNewDocuments extends AbstractCMISConnector {

    public static final String DOCUMENT_IDS_OUTPUT = "document_ids";
    public static final String DOCUMENTS = "documents";
    public static final String FOLDER_PATH = "folder_path";

    private ProcessAPI processApi;
    
    private Object rawDocuments;
    private String folder_path;
    private List<String> remoteDocumentIds;

	@Override
    public void setInputParameters(final Map<String, Object> parameters) {
        super.setInputParameters(parameters);
        rawDocuments = parameters.get(DOCUMENTS);
        folder_path = (String) parameters.get(FOLDER_PATH);
    }
    
    @Override
    protected void executeBusinessLogic() throws ConnectorException {
    	// Log in CMIS
    	processApi = getAPIAccessor().getProcessAPI();
        final AbstractCmisClient cmisClient = getClient();
        if (cmisClient == null) {
            throw new ConnectorException("CMIS UploadNewDocuments connector is not connected properly.");
        }
        // Get documents
        List<Document> documents = getDocumentsFromInputParameter();
        // Upload documents
        remoteDocumentIds = new ArrayList<String>();
        for (Document document : documents){
            final byte[] documentContent = getDocumentContent(document);
            final String remoteDocumentId = cmisClient.uploadNewDocument(folder_path, document.getContentFileName(), documentContent, document.getContentMimeType()).getId();
            remoteDocumentIds.add(remoteDocumentId);
        }
        // Set result
        setOutputParameter(DOCUMENT_IDS_OUTPUT, remoteDocumentIds);
    }

    @SuppressWarnings("unchecked")
    protected List<Document> getDocumentsFromInputParameter() throws ConnectorException {
    	List<Document> documents = new ArrayList<Document>();
    	if (rawDocuments instanceof String)
        {
        	String documentName = (String) rawDocuments;
        	documents = getDocumentsFromName(documentName);
        }
    	else if (rawDocuments instanceof Document)
    	{
    		documents.add((Document) rawDocuments);
    	}
    	else if (rawDocuments instanceof List)
    	{
    		List<?> rawDocList = (List<?>) rawDocuments;
    		if (rawDocList.size() > 0)
    		{
    			if (rawDocList.get(0) instanceof String)
    			{
    				for (String documentName : (List<String>) rawDocList)
    					documents.addAll(getDocumentsFromName(documentName));
    			}
    			else if (rawDocList.get(0) instanceof Document)
    			{
    				documents = (List<Document>) rawDocList;
    			}
    			else
    				throw new ConnectorException("Unsupported List<?> type " + rawDocList.get(0).getClass().getName() +" for parameter "+ DOCUMENTS);
    		}
    	}
    	else
    		throw new ConnectorException("Unsupported type " + rawDocuments.getClass().getName() +" for parameter "+ DOCUMENTS);
    	return documents;
    }
    
    protected List<Document> getDocumentsFromName(String documentName) throws ConnectorException {
    	
		// Try to retrieve document list
    	List<Document> documents = getDocumentListFromName(documentName);
    	if (documents != null)
    		return documents;
    	// Try to retrieve single document
    	Document document = getSingleDocumentFromName(documentName);
    	if (document != null)
    	{
    		documents = new ArrayList<Document>();
			documents.add(document);
			return documents;
    	}
		throw new ConnectorException("Failed to retrieve " + documentName +" either as a single or multiple document");
    }
    
    protected byte[] getDocumentContent(final Document doc) throws ConnectorException {
        try {
        	return processApi.getDocumentContent(doc.getContentStorageId());
        } catch (final DocumentNotFoundException e) {
            throw new ConnectorException("Failed to retrieve document content for " + doc.getName(), e);
        }
    }

    protected Document getSingleDocumentFromName(final String documentName) {
        try {
            return processApi.getLastDocument(getExecutionContext().getProcessInstanceId(), documentName);
        } catch (final DocumentNotFoundException e) {
           return null;
        }
    }

    protected List<Document> getDocumentListFromName(final String documentListName) {
    	try {
			return processApi.getDocumentList(getExecutionContext().getProcessInstanceId(), documentListName, 0, Integer.MAX_VALUE);
		} catch (DocumentNotFoundException e) {
			return null;
		}
    }
}
