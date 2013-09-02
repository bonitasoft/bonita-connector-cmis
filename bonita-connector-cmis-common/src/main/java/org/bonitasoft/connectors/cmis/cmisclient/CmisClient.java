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
package org.bonitasoft.connectors.cmis.cmisclient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

/**
 * This class is responsible for all business level interaction with a CMIS server
 * @author Frederic Bouquet
 * @author Romain Bioteau
 */
public class CmisClient {
    private Session session;

    public CmisClient() {

    }

    public CmisClient(String username, String password, String url, String bindingType, String repository) {
        createSessionByRepositoryName(username, password, url, bindingType, repository);
    }

    /**
     * Retrieve the repositories
     * @param username the username to access the server
     * @param password the password to access the server
     * @param url the URL of the server. It may differ depending on the binding type
     * @param bindingType the protocol to use to connect the server. Atom or Webservice are supported
     * @return The list of repositories
     */
    public List<Repository> getRepositories(final String username,
                                                 final String password, final String url, final String bindingType) {

        final SessionFactory f = SessionFactoryImpl.newInstance();
        final Map<String, String> parameter = fixParameters(username, password,
                url, bindingType);
        return f.getRepositories(parameter);
    }

    /**
     * Get a folder object from its path
     * @param path path to the folder
     * @return the folder object
     */
    public Folder getFolderByPath(final String path) {
        return (Folder) session.getObjectByPath(path);
    }

    /**
     * Get a CMIS object from a CMIS repository. It may correspond to all kind of objects supported by CMIS.
     * Please refer to the standard for more information
     * @param path path to the object
     * @return the CMIS object
     */
    public CmisObject getObjectByPath(final String path) {
        return session.getObjectByPath(path);
    }

    /**
     * Get the Root folder of a CMIS server
     * @return the root folder
     */
    public Folder getRootFolder() {
        return session.getRootFolder();
    }

    /**
     * Check if an object exists
     * @param objectPath path to the object to check
     * @return true if the object exists
     */
    public Boolean checkIfObjectExists(String objectPath) {
        try {
            session.getObjectByPath(objectPath);
            return true;
        } catch (CmisObjectNotFoundException e) {
            return false;
        }
    }

    /**
     * Create a folder
     * @param parentPath Path in which to create the folder
     * @param folderName the folder name
     */
    public void createSubFolder(String parentPath, String folderName) {
        final Folder folder = (Folder) session.getObjectByPath(parentPath);
        final HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, folderName);
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        folder.createFolder(properties);
    }

    /**
     * Delete a folder
     * @param folderPath The path to the folder to delete
     * @return the list of object ids which failed to be deleted
     */
    public List<String> deleteFolderByPath(String folderPath) {
        Folder folder = (Folder) session.getObjectByPath(folderPath);
        return folder.deleteTree(true, UnfileObject.DELETE, true);
    }

    /**
     * List the content of a folder
     * @param folderPath the folder to list
     * @return the list of contents
     */
    public List<CmisObject> listFolder(String folderPath) {
        Folder folder = (Folder) session.getObjectByPath(folderPath);
        List<CmisObject> children = new ArrayList<CmisObject>();
        for (CmisObject cmisObject : folder.getChildren()) {
            children.add(cmisObject);
        }
        return children;
    }

    public Document uploadNewDocument(String parentFolder, String documentName, byte[] documentContent, String mimeType) {
        Folder parent = getFolderByPath(parentFolder);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, documentName);

        InputStream stream = new ByteArrayInputStream(documentContent);
        ContentStream contentStream = new ContentStreamImpl(documentName, BigInteger.valueOf(documentContent.length), mimeType, stream);

        return parent.createDocument(properties, contentStream, VersioningState.MAJOR);
    }

    public Document uploadNewVersionOfDocument(String remote_document, byte[] documentContent, String contentMimeType) {
        Document document = (Document) session.getObjectByPath(remote_document);

        InputStream stream = new ByteArrayInputStream(documentContent);
        ContentStream contentStream = new ContentStreamImpl(document.getName(), BigInteger.valueOf(documentContent.length), contentMimeType, stream);
        document.setContentStream(contentStream, true);

        return document;
    }

    public void deleteVersionOfDocument(String documentVersionId) {
        Document document = (Document) session.getObject(documentVersionId);
        document.delete(false);
    }

    public void deleteObjectByPath(String objectPath) {
        session.getObjectByPath(objectPath).delete(true);
    }

    private String getRepositoryIdByName(final String username,
                                           final String password, final String url, final String binding,
                                           final String repositoryName) {
        final List<Repository> repositories = getRepositories(username,
                password, url, binding);
        int index = 0;

        while (index < repositories.size() && !repositories.get(index).getName().equals(repositoryName)) {
            index++;
        }

        if (index == repositories.size()) {
            return repositories.get(0).getId();
        } else {
            return repositories.get(index).getId();
        }
    }

    private Map<String, String> fixParameters(String username, String password, String url, String bindingType) {
        final Map<String, String> parameter = new HashMap<String, String>();
        if ("atompub".equals(bindingType)) {

            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        } else if ("webservices".equals(bindingType)) {
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());
            parameter.put(SessionParameter.WEBSERVICES_ACL_SERVICE, url
                    + "/ACLService?wsdl");
            parameter.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, url
                    + "/DiscoveryService?wsdl");
            parameter.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, url
                    + "/MultiFilingService?wsdl");
            parameter.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, url
                    + "/NavigationService?wsdl");
            parameter.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, url
                    + "/ObjectService?wsdl");
            parameter.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, url
                    + "/PolicyService?wsdl");
            parameter.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE,
                    url + "/RelationshipService?wsdl");
            parameter.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, url
                    + "/RepositoryService?wsdl");
            parameter.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, url
                    + "/VersioningService?wsdl");
        }

        parameter.put(SessionParameter.ATOMPUB_URL, url);
        parameter.put(SessionParameter.USER, username);
        parameter.put(SessionParameter.PASSWORD, password);


        return parameter;
    }

    private void createSessionByRepositoryId(final String username,
                                                final String password, final String url, final String bindingType,
                                                final String repositoryId) {
        final SessionFactory f = SessionFactoryImpl.newInstance();
        final Map<String, String> parameter = fixParameters(username, password,
                url, bindingType);
        parameter.put(SessionParameter.REPOSITORY_ID, repositoryId);
        session = f.createSession(parameter);
    }

    private void createSessionByRepositoryName(String username,String password, String url, String bindingType, String repositoryName) {
        String repositoryId = getRepositoryIdByName(username, password, url, bindingType, repositoryName);
        createSessionByRepositoryId(username, password, url, bindingType, repositoryId);
    }
}
