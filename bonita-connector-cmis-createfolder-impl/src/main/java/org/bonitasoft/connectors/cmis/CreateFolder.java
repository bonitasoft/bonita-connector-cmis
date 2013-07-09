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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bonitasoft.connectors.cmis.cmisclient.CMISParametersValidator;
import org.bonitasoft.connectors.cmis.cmisclient.CmisClient;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

public class CreateFolder extends AbstractConnector {

    public static final String URL = "url";

    public static final String BINDING = "binding_type";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String REPOSITORY = "repository";

    public static final String PARENT_FOLDER_PATH = "folder_path";

    public static final String FOLDER_NAME = "subfolder_name";

    private String url;

    private String bindingType;

    private String username;

    private String password;

    private String parentFolderPath;

    private String folderName;

    private String repository;

    private Map<String, Object> parameters;

    @Override
    public void executeBusinessLogic() throws ConnectorException {
        new CmisClient(username, password, url, bindingType, repository).createSubFolder(parentFolderPath, folderName);
    }

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        List<String> messages = new ArrayList<String>(0);

        CMISParametersValidator cmisParametersValidator = new CMISParametersValidator(parameters);
        messages.addAll(cmisParametersValidator.validateCommonParameters());
        messages.addAll(cmisParametersValidator.validateSpecificParameters());

        if (!messages.isEmpty()) {
            throw new ConnectorValidationException(this, messages);
        }
    }

    @Override
    public void setInputParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        Logger logger = Logger.getLogger(this.getClass().toString());
        url = (String) parameters.get(URL);
        System.err.println(url);
        logger.log(Level.ALL, url);
        bindingType = (String) parameters.get(BINDING);
        System.err.println(bindingType);
        logger.log(Level.ALL, BINDING);
        username = (String) parameters.get(USERNAME);
        System.err.println(username);
        logger.log(Level.ALL, USERNAME);
        password = (String) parameters.get(PASSWORD);
        System.err.println(password);
        logger.log(Level.ALL, PASSWORD);
        repository = (String) parameters.get(REPOSITORY);
        System.err.println(repository);
        logger.log(Level.ALL, REPOSITORY);
        parentFolderPath = (String) parameters.get(PARENT_FOLDER_PATH);
        System.err.println(parentFolderPath);
        logger.log(Level.ALL, PARENT_FOLDER_PATH);
        folderName = (String) parameters.get(FOLDER_NAME);
        System.err.println(folderName);
        logger.log(Level.ALL, FOLDER_NAME);
    }
}
