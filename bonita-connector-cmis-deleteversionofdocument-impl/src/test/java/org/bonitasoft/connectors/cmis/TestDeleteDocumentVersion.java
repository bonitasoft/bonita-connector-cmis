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

import java.io.ByteArrayInputStream;
import java.util.Properties;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.bonitasoft.connectors.cmis.cmisclient.CmisClient;
import org.junit.Test;

/**
 * 
 * @author Romain Bioteau
 *
 */
public class TestDeleteDocumentVersion {

	public static final String URL = "url";
	public static final String BINDING = "binding_type";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String REPOSITORY = "repository";

	@Test
	public void testCreateFolderWithAtom() throws Exception {
		Properties alfrescoTestConfig = new Properties();
		alfrescoTestConfig.load(TestDeleteDocumentVersion.class.getClassLoader().getResourceAsStream("alfresco_atom.properties"));
		final CmisClient client = new CmisClient(alfrescoTestConfig.getProperty(USERNAME),
				alfrescoTestConfig.getProperty(PASSWORD),
				alfrescoTestConfig.getProperty(URL),
				alfrescoTestConfig.getProperty(BINDING),
				alfrescoTestConfig.getProperty(REPOSITORY));
		deleteDocumentVersion(client);
	}


	
	@Test
	public void testCreateFolderWithWebservice() throws Exception {
		Properties alfrescoTestConfig = new Properties();
		alfrescoTestConfig.load(TestDeleteDocumentVersion.class.getClassLoader().getResourceAsStream("alfresco_ws.properties"));
		final CmisClient client = new CmisClient(alfrescoTestConfig.getProperty(USERNAME),
				alfrescoTestConfig.getProperty(PASSWORD),
				alfrescoTestConfig.getProperty(URL),
				alfrescoTestConfig.getProperty(BINDING),
				alfrescoTestConfig.getProperty(REPOSITORY));
		deleteDocumentVersion(client);
	}
	
	protected void deleteDocumentVersion(final CmisClient client) {
		String parentFolderPath = "/User Homes/qa/automaticTests";
		String documentName = "migration_note.txt";
		Folder f = client.getFolderByPath(parentFolderPath);
		Document doc = (Document) client.getObjectByPath(f.getPath()+"/"+documentName);
		
		client.uploadNewVersionOfDocument(doc.getPaths().get(0), new String("new test version").getBytes(), "text/plain");
		client.deleteVersionOfDocumentByLabel(doc.getPaths().get(0), "1.2");
	}
}
