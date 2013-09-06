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


import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Properties;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.bonitasoft.connectors.cmis.cmisclient.CmisClient;
import org.junit.Test;

/**
 * 
 * @author Romain Bioteau
 *
 */
public class TestDownloadDocument {

	public static final String URL = "url";
	public static final String BINDING = "binding_type";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String REPOSITORY = "repository";

	
	@Test
	public void testDownloadDocumentWithWebservice() throws Exception {
		Properties alfrescoTestConfig = new Properties();
		alfrescoTestConfig.load(TestDownloadDocument.class.getClassLoader().getResourceAsStream("alfresco_ws.properties"));
		final CmisClient client = new CmisClient(alfrescoTestConfig.getProperty(USERNAME),
				alfrescoTestConfig.getProperty(PASSWORD),
				alfrescoTestConfig.getProperty(URL),
				alfrescoTestConfig.getProperty(BINDING),
				alfrescoTestConfig.getProperty(REPOSITORY));
		
		String remoteDocumentPath= "/User Homes/qa/automaticTests/migration_note.txt";
		Document f = (Document) client.getObjectByPath(remoteDocumentPath);
		ContentStream contentStream = f.getContentStream();
		assertNotNull(contentStream);
		assertEquals("Invalid mime type","text/plain",contentStream.getMimeType());
		assertEquals("Invalid mime type","migration_note.txt",contentStream.getFileName());
		InputStream stream = contentStream.getStream();
		assertNotNull(stream);
		assertTrue(stream.available() > 0);
		stream.close();
	}
	
}
