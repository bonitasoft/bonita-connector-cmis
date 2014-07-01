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
import java.io.IOException;
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
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.bonitasoft.engine.connector.ConnectorException;

/**
 * This class is responsible for all business level interaction with a CMIS server
 * @author Frederic Bouquet
 * @author Romain Bioteau
 */
public abstract class AbstractCmisClient {

    protected Session session;
    private final String username;
    private final String password;
    private Repository repository;
    private final String repositoryName;

    public AbstractCmisClient(final String username, final String password, final String repositoryName) {
        this.username = username;
        this.password = password;
        this.repositoryName = repositoryName;
    }

    public void disconnect(){
        if(session != null){
            session.clear();
            session = null;
        }
    }

    public void connect() {
        if (session != null) {
            session.clear();
            session = null;
        }
        if (repository == null) {
            repository = getRepositoryByName(repositoryName);
        }

        session = repository.createSession();
    }

    /**
     * Retrieve the repositories
     * @param username the username to access the server
     * @param password the password to access the server
     * @param url the URL of the server. It may differ depending on the binding type
     * @param bindingType the protocol to use to connect the server. Atom or Webservice are supported
     * @return The list of repositories
     */
    public List<Repository> getRepositories() {
        final SessionFactory f = SessionFactoryImpl.newInstance();
        final Map<String, String> parameter = configure();
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
    public Boolean checkIfObjectExists(final String objectPath) {
        try {
            session.getObjectByPath(objectPath);
            return true;
        } catch (final CmisObjectNotFoundException e) {
            return false;
        }
    }

    /**
     * Create a folder
     * @param parentPath Path in which to create the folder
     * @param folderName the folder name
     */
    public void createSubFolder(final String parentPath, final String folderName) {
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
    public List<String> deleteFolderByPath(final String folderPath) {
        final Folder folder = (Folder) session.getObjectByPath(folderPath);
        return folder.deleteTree(true, UnfileObject.DELETE, true);
    }

    /**
     * List the content of a folder
     * @param folderPath the folder to list
     * @return the list of contents
     */
    public List<CmisObject> listFolder(final String folderPath) {
        final Folder folder = (Folder) session.getObjectByPath(folderPath);
        final List<CmisObject> children = new ArrayList<CmisObject>();
        for (final CmisObject cmisObject : folder.getChildren()) {
            children.add(cmisObject);
        }
        return children;
    }

    public Document uploadNewDocument(final String parentFolder, final String documentName, final byte[] documentContent, final String mimeType)
            throws ConnectorException {
        final Folder folder = getFolderByPath(parentFolder);

        final Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, documentName);
        final InputStream stream = new ByteArrayInputStream(documentContent);
        final ContentStream contentStream = new ContentStreamImpl(documentName, BigInteger.valueOf(documentContent.length), mimeType, stream);
        try {
            return folder.createDocument(properties, contentStream, VersioningState.MAJOR);
        } finally {
            if (contentStream != null && contentStream.getStream() != null) {
                try {
                    contentStream.getStream().close();
                } catch (final IOException e) {
                    throw new ConnectorException(e);
                }
            }
        }
    }

    public Document uploadNewVersionOfDocument(final String remote_document, final byte[] documentContent, final String contentMimeType)
            throws ConnectorException {
        final Document document = (Document) session.getObjectByPath(remote_document);

        final InputStream stream = new ByteArrayInputStream(documentContent);
        final ContentStream contentStream = new ContentStreamImpl(document.getName(), BigInteger.valueOf(documentContent.length), contentMimeType, stream);
        try {
            document.setContentStream(contentStream, true);
        } finally {
            if (contentStream != null && contentStream.getStream() != null) {
                try {
                    contentStream.getStream().close();
                } catch (final IOException e) {
                    throw new ConnectorException(e);
                }
            }
        }
        return document;
    }

    public void deleteVersionOfDocumentById(final String documentVersionId) {
        final Document document = (Document) session.getObject(documentVersionId);
        document.delete(false);
    }

    public void deleteVersionOfDocumentByLabel(final String documentPath, final String versionLabel) {
        final Document document = (Document) session.getObjectByPath(documentPath);
        for(final Document version : document.getAllVersions()){
            final String label = version.getVersionLabel();
            if(label != null && label.equals(versionLabel)){
                version.delete(false);
            }
        }
    }

    public void deleteObjectByPath(final String objectPath) {
        session.getObjectByPath(objectPath).delete(true);
    }

    protected Repository getRepositoryByName(final String repositoryName) {
        final List<Repository> repositories = getRepositories();
        int index = 0;

        while (index < repositories.size() && !repositories.get(index).getName().equals(repositoryName)) {
            index++;
        }

        if (index == repositories.size()) {
            return repositories.get(0);
        } else {
            return repositories.get(index);
        }
    }

    protected Map<String, String> configure() {
        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(SessionParameter.USER, getUsername());
        parameters.put(SessionParameter.PASSWORD, getPassword());
        return parameters;

    }


    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
