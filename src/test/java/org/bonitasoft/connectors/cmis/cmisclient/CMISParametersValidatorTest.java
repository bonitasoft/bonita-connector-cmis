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
package org.bonitasoft.connectors.cmis.cmisclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CMISParametersValidatorTest {

    @Test
    void testValidateCommonParameters() throws Exception {
        final CMISParametersValidator cmisParametersValidator = new CMISParametersValidator(
                Collections.<String, Object> emptyMap());
        assertThat(cmisParametersValidator.validateCommonParameters()).contains(
                "Binding type is not set",
                "Repository must be set",
                "Username is not set",
                "Password is not set");
    }

    @Test
    void testBinding() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("binding_type", "b");

        final CMISParametersValidator cmisParametersValidator = new CMISParametersValidator(parameters);
        assertThat(cmisParametersValidator.validateCommonParameters(), hasItem(
                "Binding type should be either atompub or webservices"));
    }

    @Test
    void testValidateSpecificParameters() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("subfolder_name", null);
        parameters.put("folder_path", null);
        parameters.put("document_path", null);
        parameters.put("document_id", null);
        parameters.put("query", null);
        parameters.put("document", null);
        parameters.put("destinationName", null);
        parameters.put("remote_document", null);

        final CMISParametersValidator cmisParametersValidator = new CMISParametersValidator(parameters);

        assertThat(cmisParametersValidator.validateSpecificParameters()).contains(
                "Document must be set",
                "Folder path must be set",
                "Destination name must be set",
                "Remote document must be set",
                "Query must be set",
                "Sub folder must be set",
                "Document path must be set",
                "Document ID must be set",
                "Query must be set");
    }

    @Test
    void testNoSpecificParameters() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        final CMISParametersValidator cmisParametersValidator = new CMISParametersValidator(parameters);
        assertThat(cmisParametersValidator.validateSpecificParameters()).isEmpty();
    }
}
