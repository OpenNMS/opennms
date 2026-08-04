/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.container.web.bridge.rest;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.osgi.service.jaxrs.runtime.dto.ApplicationDTO;
import org.osgi.service.jaxrs.runtime.dto.ResourceDTO;
import org.osgi.service.jaxrs.runtime.dto.ResourceMethodInfoDTO;
import org.osgi.service.jaxrs.runtime.dto.RuntimeDTO;

public class RestEndpointRegistryImplTest {

    @Test
    public void verifyNormalizeBase() {
        assertEquals("/rest", RestEndpointRegistryImpl.normalizeBase("/rest"));
        assertEquals("/rest", RestEndpointRegistryImpl.normalizeBase("/rest/"));
        assertEquals("/rest", RestEndpointRegistryImpl.normalizeBase("/rest/*"));
        assertEquals("/api/v2", RestEndpointRegistryImpl.normalizeBase("/api/v2/*"));
        assertEquals("", RestEndpointRegistryImpl.normalizeBase("/"));
        assertEquals("", RestEndpointRegistryImpl.normalizeBase(null));
    }

    @Test
    public void verifyStaticPrefix() {
        assertEquals("/scv", RestEndpointRegistryImpl.staticPrefix("/scv"));
        assertEquals("/scv", RestEndpointRegistryImpl.staticPrefix("/scv/{alias}"));
        assertEquals("/scv", RestEndpointRegistryImpl.staticPrefix("scv/{alias}/detail"));
        assertEquals("/a/b", RestEndpointRegistryImpl.staticPrefix("/a/b/{c}/d"));
        assertEquals("", RestEndpointRegistryImpl.staticPrefix("/{id}"));
        assertEquals("", RestEndpointRegistryImpl.staticPrefix("/"));
        assertEquals("", RestEndpointRegistryImpl.staticPrefix(null));
    }

    /**
     * The endpoints must stay at resource granularity: the proxy filter matches them with
     * startsWith, so reporting the bare application base would make it claim every request below
     * /rest - including everything served by the ReST implementation of the web application.
     */
    @Test
    public void verifyEndpointsAreReportedPerResource() {
        final RuntimeDTO runtimeDTO = new RuntimeDTO();
        runtimeDTO.defaultApplication = application("/", resource("/ignored/{x}"));
        runtimeDTO.applicationDTOs = new ApplicationDTO[] {
                application("/rest", resource("/scv", "/scv/{alias}"), resource("/datachoices")),
                application("/api/v2/*", resource("/graphs", "/graphs/{container}"))
        };

        final RestEndpointRegistryImpl registry = new RestEndpointRegistryImpl();
        registry.setJaxrsServiceRuntime(() -> runtimeDTO);

        final List<String> endpoints = registry.getRestEndpoints();
        assertEquals(Arrays.asList("/ignored", "/rest/scv", "/rest/datachoices", "/api/v2/graphs"), endpoints);
    }

    @Test
    public void verifyMissingResourceMethodsAreTolerated() {
        final RuntimeDTO runtimeDTO = new RuntimeDTO();
        final ResourceDTO withoutMethods = new ResourceDTO();
        final ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.base = "/rest";
        applicationDTO.resourceDTOs = new ResourceDTO[] { withoutMethods };
        runtimeDTO.applicationDTOs = new ApplicationDTO[] { applicationDTO };

        final RestEndpointRegistryImpl registry = new RestEndpointRegistryImpl();
        registry.setJaxrsServiceRuntime(() -> runtimeDTO);

        assertEquals(0, registry.getRestEndpoints().size());
    }

    private static ApplicationDTO application(String base, ResourceDTO... resources) {
        final ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.base = base;
        applicationDTO.resourceDTOs = resources;
        return applicationDTO;
    }

    private static ResourceDTO resource(String... methodPaths) {
        final ResourceDTO resourceDTO = new ResourceDTO();
        resourceDTO.resourceMethods = new ResourceMethodInfoDTO[methodPaths.length];
        for (int i = 0; i < methodPaths.length; i++) {
            final ResourceMethodInfoDTO methodInfoDTO = new ResourceMethodInfoDTO();
            methodInfoDTO.path = methodPaths[i];
            resourceDTO.resourceMethods[i] = methodInfoDTO;
        }
        return resourceDTO;
    }
}
