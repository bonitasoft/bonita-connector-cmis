/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
 */
package org.bonitasoft.connectors.cmis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.bonitasoft.connectors.cmis.cmisclient.AbstractCmisClient;
import org.bonitasoft.connectors.cmis.cmisclient.AtompubCMISClient;
import org.bonitasoft.connectors.cmis.cmisclient.CMISParametersValidator;
import org.bonitasoft.connectors.cmis.cmisclient.WebserviceCMISClient;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;


/**
 * @author Romain Bioteau
 *
 */
public abstract class AbstractCMISConnector extends AbstractConnector {

    public static final String ATOMPUB = "atompub";
    public static final String WEBSERVICE = "webservices";
    public static final String URL = "url";
    public static final String REPOSITORY = "repository";
    public static final String BINDING = "binding_type";
    public static final String WEBSERVICES_OBJECT_SERVICE = "wsObjectServiceUrl";
    public static final String WEBSERVICES_OBJECT_SERVICE_ENDPOINT = "wsObjectServiceEndpointUrl";
    public static final String WEBSERVICES_REPOSITORY_SERVICE = "wsRepositoryServiceUrl";
    public static final String WEBSERVICES_REPOSITORY_SERVICE_ENDPOINT = "wsRepositoryServiceEndpointUrl";
    public static final String WEBSERVICES_VERSIONING_SERVICE = "wsVersioningServiceUrl";
    public static final String WEBSERVICES_VERSIONING_SERVICE_ENDPOINT = "wsVersioningServiceEndpointUrl";
    public static final String WEBSERVICES_NAVIGATION_SERVICE = "wsNavigationServiceUrl";
    public static final String WEBSERVICES_NAVIGATION_SERVICE_ENDPOINT = "wsNavigationServiceEndpointUrl";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    private String url;

    private String bindingType;

    private String username;

    private String password;

    private String repository;

    private Map<String, String> serviceBinding;

    private Map<String, Object> inputParameters;

    private AbstractCmisClient cmisClient;

    @Override
    public void connect() throws ConnectorException {
        super.connect();
        if (ATOMPUB.equals(bindingType)) {
            cmisClient = new AtompubCMISClient(username, password, repository, url);
        } else if (WEBSERVICE.equals(bindingType)) {
            cmisClient = new WebserviceCMISClient(username, password, repository, serviceBinding);
        }else{
            throw new IllegalArgumentException("Invalid binding type : "+bindingType);
        }
        cmisClient.connect();
    }

    @Override
    public void disconnect() throws ConnectorException {
        super.disconnect();
        if (cmisClient != null) {
            cmisClient.disconnect();
            cmisClient = null;
        }
    }

    protected AbstractCmisClient getClient() {
        return cmisClient;
    }

    protected Map<String, Object> getInputParameters() {
        return inputParameters;
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.engine.connector.Connector#validateInputParameters()
     */
    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        final List<String> messages = new ArrayList<String>(0);

        final CMISParametersValidator cmisParametersValidator = new CMISParametersValidator(inputParameters);
        messages.addAll(cmisParametersValidator.validateCommonParameters());
        messages.addAll(cmisParametersValidator.validateSpecificParameters());

        if (!messages.isEmpty()) {
            throw new ConnectorValidationException(this, messages);
        }
    }

    @Override
    public void setInputParameters(final Map<String, Object> parameters) {
        inputParameters = parameters;
        final Logger logger = Logger.getLogger(this.getClass().toString());
        url = (String) parameters.get(URL);
        logger.log(Level.ALL, url);
        bindingType = (String) parameters.get(BINDING);
        logger.log(Level.ALL, BINDING);
        username = (String) parameters.get(USERNAME);
        logger.log(Level.ALL, USERNAME);
        password = (String) parameters.get(PASSWORD);
        logger.log(Level.ALL, PASSWORD);
        repository = (String) parameters.get(REPOSITORY);
        logger.log(Level.ALL, REPOSITORY);

        serviceBinding = new HashMap<String, String>();
        final String objectServiceUrl = (String) parameters.get(WEBSERVICES_OBJECT_SERVICE);
        if (objectServiceUrl != null && !objectServiceUrl.isEmpty()) {
            serviceBinding.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, objectServiceUrl);
        }
        final String objectServiceEndpoint = (String) parameters.get(WEBSERVICES_OBJECT_SERVICE_ENDPOINT);
        if (objectServiceEndpoint != null && !objectServiceEndpoint.isEmpty()) {
            serviceBinding.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE_ENDPOINT, objectServiceEndpoint);
        }
        final String repoServiceUrl = (String) parameters.get(WEBSERVICES_REPOSITORY_SERVICE);
        if (repoServiceUrl != null && !repoServiceUrl.isEmpty()) {
            serviceBinding.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, repoServiceUrl);
        }
        final String repoServiceEndpoint = (String) parameters.get(WEBSERVICES_OBJECT_SERVICE_ENDPOINT);
        if (repoServiceEndpoint != null && !repoServiceEndpoint.isEmpty()) {
            serviceBinding.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE_ENDPOINT, repoServiceEndpoint);
        }
        final String versioningServiceUrl = (String) parameters.get(WEBSERVICES_VERSIONING_SERVICE);
        if (versioningServiceUrl != null && !versioningServiceUrl.isEmpty()) {
            serviceBinding.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, versioningServiceUrl);
        }
        final String versioningServiceEndpoint = (String) parameters.get(WEBSERVICES_VERSIONING_SERVICE_ENDPOINT);
        if (versioningServiceEndpoint != null && !versioningServiceEndpoint.isEmpty()) {
            serviceBinding.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE_ENDPOINT, versioningServiceEndpoint);
        }
    }

}
