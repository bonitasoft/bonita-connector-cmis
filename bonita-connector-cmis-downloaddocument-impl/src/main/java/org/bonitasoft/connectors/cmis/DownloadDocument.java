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

import java.io.IOException;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.bonitasoft.connectors.cmis.cmisclient.AbstractCmisClient;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.io.IOUtil;

public class DownloadDocument extends AbstractCMISConnector {

    public static final String DOCUMENT = "documentOutput";
    public static final String REMOTE_DOCUMENT_PATH = "remote_document_path";

    private String remote_path;


    @Override
    public void setInputParameters(final Map<String, Object> parameters) {
        super.setInputParameters(parameters);
        remote_path = (String) parameters.get(REMOTE_DOCUMENT_PATH);
    }



    //Need ObjectService binding
    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        final AbstractCmisClient cmisClient = getClient();
        if (cmisClient == null) {
            throw new ConnectorException("CMIS DownloadDocument connector is not connected properly.");
        }
        if(!cmisClient.checkIfObjectExists(remote_path)){
            throw new ConnectorException("Document "+remote_path+" does not exists!");
        }
        final Document cmisDocument =(Document) cmisClient.getObjectByPath(remote_path);
        final ContentStream contentStream = cmisDocument.getContentStream();
        DocumentValue docValue;
        try {
            docValue = new DocumentValue(IOUtil.getAllContentFrom(contentStream.getStream()),contentStream.getMimeType(), contentStream.getFileName());
        } catch (final IOException e) {
            throw new ConnectorException(e);
        }
        setOutputParameter(DOCUMENT, docValue);
    }


    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        super.validateInputParameters();
        try{
            getInputParameter(REMOTE_DOCUMENT_PATH);
        }catch(final ClassCastException ex){
            throw new ConnectorValidationException("Invalid type for input "+REMOTE_DOCUMENT_PATH);
        }
    }
}
