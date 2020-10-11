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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.connectors.cmis.cmisclient.AbstractCmisClient;
import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.connector.ConnectorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UploadNewDocumentsTest {

    private final static String DOCUMENT_NAME = "documentName";

    @Spy
    UploadNewDocuments connector;
    @Mock
    Document mockDoc;

    @BeforeEach
    public void initConnector() throws ConnectorException {
        connector.setInputParameters(new HashMap<String, Object>());
        when(mockDoc.getContentMimeType()).thenReturn("text/plain");
        when(mockDoc.getContentFileName()).thenReturn("myDoc.txt");
        doReturn(new byte[0]).when(connector).getDocumentContent(mockDoc);
    }

    @Test
    void should_getDocumentsFromInputParameter_call_getDocumentFromName_when_document_is_a_string()
            throws ConnectorException {
        List<Document> mockDocs = new ArrayList<Document>();
        mockDocs.add(mockDoc);
        doReturn(mockDocs).when(connector).getDocumentsFromName(DOCUMENT_NAME);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(UploadNewDocuments.DOCUMENTS, DOCUMENT_NAME);
        connector.setInputParameters(parameters);
        connector.getDocumentsFromInputParameter();
        verify(connector).getDocumentsFromName(DOCUMENT_NAME);
    }

    @Test
    void should_getDocumentsFromInputParameter_call_getDocumentFromName_when_document_is_a_list_of_strings()
            throws ConnectorException {
        doReturn(new ArrayList<Document>()).when(connector).getDocumentsFromName(DOCUMENT_NAME);
        List<String> documentNames = new ArrayList<String>();
        documentNames.add(DOCUMENT_NAME);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(UploadNewDocuments.DOCUMENTS, documentNames);
        connector.setInputParameters(parameters);

        connector.getDocumentsFromInputParameter();

        verify(connector).getDocumentsFromName(DOCUMENT_NAME);
    }

    @Test
    void should_getDocumentsFromInputParameter_work_when_document_is_a_document() throws ConnectorException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(UploadNewDocuments.DOCUMENTS, mockDoc);
        connector.setInputParameters(parameters);

        List<Document> documents = connector.getDocumentsFromInputParameter();

        assertThat(documents).hasSize(1)
                .contains(mockDoc);
    }

    @Test
    void should_getDocumentsFromInputParameter_work_when_document_is_a_list_of_documents() throws ConnectorException {
        List<Document> mockDocs = new ArrayList<Document>();
        mockDocs.add(mockDoc);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(UploadNewDocuments.DOCUMENTS, mockDocs);
        connector.setInputParameters(parameters);

        List<Document> documents = connector.getDocumentsFromInputParameter();

        assertThat(documents).isEqualTo(mockDocs);
    }

    @Test
    void should_getDocumentFromName_work_with_single_document() throws ConnectorException {
        doReturn(null).when(connector).getDocumentListFromName(DOCUMENT_NAME);
        doReturn(mockDoc).when(connector).getSingleDocumentFromName(DOCUMENT_NAME);

        List<Document> documents = connector.getDocumentsFromName(DOCUMENT_NAME);

        assertThat(documents).hasSize(1).contains(mockDoc);
    }

    @Test
    void should_getDocumentFromName_work_with_document_list() throws ConnectorException {
        List<Document> mockDocs = new ArrayList<>();
        mockDocs.add(mockDoc);
        doReturn(mockDocs).when(connector).getDocumentListFromName(DOCUMENT_NAME);

        List<Document> documents = connector.getDocumentsFromName(DOCUMENT_NAME);

        assertThat(documents).isEqualTo(mockDocs);
    }

    @Test
    void should_getDocumentFromName_fail_when_document_is_not_found() throws ConnectorException {
        doReturn(null).when(connector).getDocumentListFromName(DOCUMENT_NAME);
        doReturn(null).when(connector).getSingleDocumentFromName(DOCUMENT_NAME);

        assertThrows(ConnectorException.class, () -> connector.getDocumentsFromName(DOCUMENT_NAME));
    }

    @Test
    void should_executeBusinessLogic_work() throws ConnectorException {
        List<Document> mockDocs = new ArrayList<Document>();
        mockDocs.add(mockDoc);
        mockDocs.add(mockDoc);

        Map<String, Object> params = new HashMap<>();
        params.put(UploadNewDocuments.FOLDER_PATH, "/some/path");
        connector.setInputParameters(params);
        APIAccessor apiAccessor = Mockito.mock(APIAccessor.class);
        doReturn(apiAccessor).when(connector).getAPIAccessor();

        org.apache.chemistry.opencmis.client.api.Document cmisDocument = Mockito
                .mock(org.apache.chemistry.opencmis.client.api.Document.class);
        when(cmisDocument.getId()).thenReturn("1");
        AbstractCmisClient client = Mockito.mock(AbstractCmisClient.class);
        when(client.uploadNewDocument(any(String.class), any(String.class), any(byte[].class), any(String.class)))
                .thenReturn(cmisDocument);

        doReturn(client).when(connector).getClient();
        doReturn(mockDocs).when(connector).getDocumentsFromInputParameter();

        connector.executeBusinessLogic();

        verify(client, times(2)).uploadNewDocument(any(String.class), any(String.class), any(byte[].class),
                any(String.class));
    }
}
