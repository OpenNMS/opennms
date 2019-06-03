/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/


package org.opennms.netmgt.endpoints.grafana.service;

import static org.easymock.EasyMock.createNiceMock;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.easymock.EasyMock;
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
        final GrafanaEndpointDao grafanaEndpointDao = createNiceMock(GrafanaEndpointDao.class);
        final GrafanaClientFactory grafanaClientFactory = createNiceMock(GrafanaClientFactory.class);
        final SessionUtils sessionUtils = createNiceMock(SessionUtils.class);
        EasyMock.replay(grafanaEndpointDao);
        EasyMock.replay(grafanaClientFactory);
        EasyMock.replay(sessionUtils);

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