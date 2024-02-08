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
package org.opennms.netmgt.endpoints.grafana.service;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaClientFactory;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpoint;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpointException;
import org.opennms.netmgt.endpoints.grafana.persistence.api.GrafanaEndpointDao;

public class GrafanaEndpointServiceImplTest {

    private GrafanaEndpointServiceImpl endpointService;
    private GrafanaEndpoint endpoint;



    @Before
    public void setUp() {
        final GrafanaEndpointDao grafanaEndpointDao = mock(GrafanaEndpointDao.class);
        final GrafanaClientFactory grafanaClientFactory = mock(GrafanaClientFactory.class);
        final SessionUtils sessionUtils = mock(SessionUtils.class);

        endpointService = new GrafanaEndpointServiceImpl(
                grafanaEndpointDao,
                grafanaClientFactory,
                sessionUtils);
        endpoint = new GrafanaEndpoint();
        endpoint.setApiKey("RandomKey");
        endpoint.setUrl("http://grafana.opennms.org:3000");
        endpoint.setUid("Random-UID");
        endpointService.validate(endpoint);
    }

    @Test
    public void verifyUrlValidation() {
        // Ensure null is working
        try {
            endpoint.setUrl(null);
            endpointService.validate(endpoint);
            fail("Expected exception was not thrown");
        } catch (GrafanaEndpointException ex) {
            assertEquals("url", ex.getContext());
            assertEquals(GrafanaEndpointServiceImpl.PROVIDE_A_VALUE_TEXT, ex.getRawMessage());
        }
        // Ensure empty is working
        try {
            endpoint.setUrl("");
            endpointService.validate(endpoint);
            fail("Expected exception was not thrown");
        } catch (GrafanaEndpointException ex) {
            assertEquals("url", ex.getContext());
            assertEquals(GrafanaEndpointServiceImpl.PROVIDE_A_VALUE_TEXT, ex.getRawMessage());
        }

        // Ensure invalid URL is working
        try {
            endpoint.setUrl("xxx");
            endpointService.validate(endpoint);
            fail("Expected exception was not thrown");
        } catch (GrafanaEndpointException ex) {
            assertEquals("url", ex.getContext());
            assertThat(ex.getMessage(), allOf(containsString("provided URL"), containsString("is not valid")));
        }
    }

    @Test
    public void verifyApiKeyValidation() {
        // Ensure null is working
        try {
            endpoint.setApiKey(null);
            endpointService.validate(endpoint);
            fail("Expected exception was not thrown");
        } catch (GrafanaEndpointException ex) {
            assertEquals("apiKey", ex.getContext());
            assertEquals(GrafanaEndpointServiceImpl.PROVIDE_A_VALUE_TEXT, ex.getRawMessage());
        }

        // Ensure empty is working
        try {
            endpoint.setApiKey(null);
            endpointService.validate(endpoint);
            fail("Expected exception was not thrown");
        } catch (GrafanaEndpointException ex) {
            assertEquals("apiKey", ex.getContext());
            assertEquals(GrafanaEndpointServiceImpl.PROVIDE_A_VALUE_TEXT, ex.getRawMessage());
        }
    }

    @Test
    public void verifyConnectTimeoutValidation() {
        // ensure < 0 is working
        try {
            endpoint.setConnectTimeout(-1000);
            endpointService.validate(endpoint);
            fail("Expected exception was not thrown");
        } catch (GrafanaEndpointException ex) {
            assertEquals("connectTimeout", ex.getContext());
        }
        // Ensure null is working
        endpoint.setConnectTimeout(null);
        endpointService.validate(endpoint);
    }

    @Test
    public void verifyReadTimeoutValidation() {
        // ensure < 0 is working
        try {
            endpoint.setReadTimeout(-1000);
            endpointService.validate(endpoint);
            fail("Expected exception was not thrown");
        } catch (GrafanaEndpointException ex) {
            assertEquals("readTimeout", ex.getContext());
        }
        // Ensure null is working
        endpoint.setReadTimeout(null);
        endpointService.validate(endpoint);
    }

    @Test
    public void verifyUidValidation() {
        // Ensure null is working
        try {
            endpoint.setUid(null);
            endpointService.validate(endpoint);
            fail("Expected exception was not thrown");
        } catch (GrafanaEndpointException ex) {
            assertEquals("uid", ex.getContext());
            assertEquals(GrafanaEndpointServiceImpl.PROVIDE_A_VALUE_TEXT, ex.getRawMessage());
        }

        // Ensure empty is working
        try {
            endpoint.setUid("");
            endpointService.validate(endpoint);
            fail("Expected exception was not thrown");
        } catch (GrafanaEndpointException ex) {
            assertEquals("uid", ex.getContext());
            assertEquals(GrafanaEndpointServiceImpl.PROVIDE_A_VALUE_TEXT, ex.getRawMessage());
        }

        // Ensure it can not start with - or _
        try {
            endpoint.setUid("-");
            endpointService.validate(endpoint);
            fail("Expected exception was not thrown");
        } catch (GrafanaEndpointException ex) {
            assertEquals("uid", ex.getContext());
        }
        try {
            endpoint.setUid("_");
            endpointService.validate(endpoint);
            fail("Expected exception was not thrown");
        } catch (GrafanaEndpointException ex) {
            assertEquals("uid", ex.getContext());
            assertThat(ex.getMessage(), allOf(containsString("provided Grafana ID"),
                                                containsString("is not valid"),
                                                containsString(GrafanaEndpointServiceImpl.UID_PATTERN.pattern())));
        }

        // Ensure UUID works
        endpoint.setUid(UUID.randomUUID().toString());
        endpointService.validate(endpoint);
    }
}