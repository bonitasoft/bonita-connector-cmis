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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.connectors.cmis.cmisclient.CMISParametersValidator;
import org.bonitasoft.connectors.cmis.cmisclient.CmisClient;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

public class DeleteFolder extends AbstractConnector {
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String URL = "url";
    public static final String BINDING_TYPE = "binding_type";
    public static final String REPOSITORY = "repository";

    public static final String FOLDER_PATH = "folder_path";
    private Map<String, Object> parameters;
    private String username;
    private String password;
    private String url;
    private String bindingType;
    private String repository;
    private String folderPath;
    private CmisClient cmisClient;

    @Override
    public void setInputParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        username = (String) parameters.get(USERNAME);
        password = (String) parameters.get(PASSWORD);
        url = (String) parameters.get(URL);
        bindingType = (String) parameters.get(BINDING_TYPE);
        repository = (String) parameters.get(REPOSITORY);
        folderPath = (String) parameters.get(FOLDER_PATH);
    }
    
    @Override
  	public void connect() throws ConnectorException {
  		super.connect();
  		cmisClient = new CmisClient(username, password, url, bindingType, repository);
  	}

  	@Override
  	public void disconnect() throws ConnectorException {
  		super.disconnect();
  		if(cmisClient != null){
  			cmisClient.clearSession();
  			cmisClient = null;
  		}
  	}

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
    	 if (!cmisClient.checkIfObjectExists(folderPath)) {
             throw new ConnectorException("Folder "+folderPath+" does not exist!");
         }
        cmisClient.deleteFolderByPath(folderPath);
    }

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        List<String> errors = new ArrayList<String>();
        CMISParametersValidator cmisParametersValidator = new CMISParametersValidator(parameters);
        errors.addAll(cmisParametersValidator.validateCommonParameters());
        errors.addAll(cmisParametersValidator.validateSpecificParameters());

        if (!errors.isEmpty()) {
            throw new ConnectorValidationException(this, errors);
        }
    }
}
