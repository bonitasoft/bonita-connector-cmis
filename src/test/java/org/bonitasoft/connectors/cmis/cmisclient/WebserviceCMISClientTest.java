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

import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.webservices.CmisWebServicesSpi;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.junit.jupiter.api.Test;

class WebserviceCMISClientTest {

    @Test
    void should_select_cxf_as_session_parameter_without_touching_system_properties() {
        var client = new WebserviceCMISClient("user", "password", "repository",
                Map.of(SessionParameter.WEBSERVICES_OBJECT_SERVICE, "http://cmis/services/ObjectService?wsdl"));

        assertThat(client.configure())
                .containsEntry(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value())
                .containsEntry(SessionParameter.WEBSERVICES_JAXWS_IMPL, CmisWebServicesSpi.JAXWS_IMPL_CXF)
                .containsEntry(SessionParameter.WEBSERVICES_OBJECT_SERVICE, "http://cmis/services/ObjectService?wsdl");
        // the Bonita 12 runtime forbids connectors to write system properties
        assertThat(System.getProperty(SessionParameter.WEBSERVICES_JAXWS_IMPL)).isNull();
        assertThat(System.getProperty("javax.xml.parsers.DocumentBuilderFactory")).isNull();
    }

}
