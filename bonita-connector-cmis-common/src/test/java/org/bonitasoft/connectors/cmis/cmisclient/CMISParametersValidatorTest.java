package org.bonitasoft.connectors.cmis.cmisclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


public class CMISParametersValidatorTest {
    @Test
    public void testValidateCommonParameters() throws Exception {
        final CMISParametersValidator cmisParametersValidator = new CMISParametersValidator(Collections.<String, Object>emptyMap());
        assertThat(cmisParametersValidator.validateCommonParameters(), hasItems(
                "Binding type is not set",
                "Repository must be set",
                "Username is not set",
                "Password is not set"
        ));
    }

    @Test
    public void testBinding() throws Exception {
        final Map<String,Object> parameters = new HashMap<String, Object>();
        parameters.put("binding_type", "b");

        final CMISParametersValidator cmisParametersValidator = new CMISParametersValidator(parameters);
        assertThat(cmisParametersValidator.validateCommonParameters(), hasItem(
                "Binding type should be either atompub or webservices"
        ));
    }

    @Test
    public void testValidateSpecificParameters() throws Exception {
        final Map<String,Object> parameters = new HashMap<String, Object>();
        parameters.put("subfolder_name", null);
        parameters.put("folder_path", null);
        parameters.put("document_path", null);
        parameters.put("document_id", null);
        parameters.put("query", null);
        parameters.put("document", null);
        parameters.put("destinationName", null);
        parameters.put("remote_document", null);

        final CMISParametersValidator cmisParametersValidator = new CMISParametersValidator(parameters);

        assertThat(cmisParametersValidator.validateSpecificParameters(), hasItems(
                "Document must be set",
                "Folder path must be set",
                "Destination name must be set",
                "Remote document must be set",
                "Query must be set",
                "Sub folder must be set",
                "Document path must be set",
                "Document ID must be set",
                "Query must be set"
        ));
    }

    @Test
    public void testNoSpecificParameters() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        final CMISParametersValidator cmisParametersValidator = new CMISParametersValidator(parameters);
        assertThat(cmisParametersValidator.validateSpecificParameters().size(), is(0));
    }
}
