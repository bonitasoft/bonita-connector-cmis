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
package org.bonitasoft.connectors.cmis.cmisclient;

import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.webservices.CmisWebServicesSpi;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

/**
 * @author Romain Bioteau
 *
 */
public class WebserviceCMISClient extends AbstractCmisClient {

    private final Map<String, String> serviceBinding;

    public WebserviceCMISClient(final String username, final String password, final String repositoryName, final Map<String,String> serviceBinding) {
        super(username, password, repositoryName);
        ckeckServiceBinding(serviceBinding);
        this.serviceBinding = serviceBinding;
        System.setProperty(SessionParameter.WEBSERVICES_JAXWS_IMPL, CmisWebServicesSpi.JAXWS_IMPL_CXF);
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
    }


    private static void ckeckServiceBinding(final Map<String, String> serviceBinding) {
        if (serviceBinding == null || serviceBinding.isEmpty()) {
            throw new IllegalArgumentException("serviceBinding cannot be null or empty");
        }
    }

    @Override
    protected Map<String, String> configure() {
        final Map<String, String> parameters = super.configure();
        parameters.putAll(serviceBinding);
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());
        if (!parameters.containsKey(SessionParameter.WEBSERVICES_ACL_SERVICE)) {
            parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, "");
        }

        if (!parameters.containsKey(SessionParameter.WEBSERVICES_ACL_SERVICE)) {
            parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, "");
        }

        if (!parameters.containsKey(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE)) {
            parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, "");
        }

        if (!parameters.containsKey(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE)) {
            parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, "");
        }

        if (!parameters.containsKey(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE)) {
            parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, "");
        }

        if (!parameters.containsKey(SessionParameter.WEBSERVICES_OBJECT_SERVICE)) {
            parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, "");
        }

        if (!parameters.containsKey(SessionParameter.WEBSERVICES_POLICY_SERVICE)) {
            parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, "");
        }

        if (!parameters.containsKey(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE)) {
            parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, "");
        }

        if (!parameters.containsKey(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE)) {
            parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, "");
        }

        if (!parameters.containsKey(SessionParameter.WEBSERVICES_VERSIONING_SERVICE)) {
            parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, "");
        }
        return parameters;
    }


}
