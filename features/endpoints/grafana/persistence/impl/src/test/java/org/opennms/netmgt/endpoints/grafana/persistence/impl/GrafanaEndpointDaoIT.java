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

package org.opennms.netmgt.endpoints.grafana.persistence.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpoint;
import org.opennms.netmgt.endpoints.grafana.persistence.api.GrafanaEndpointDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class GrafanaEndpointDaoIT {

    @Autowired
    private GrafanaEndpointDao grafanaEndpointDao;

    @Test
    public void verifyCRUD() {
        // Verify empty
        assertThat(grafanaEndpointDao.findAll(), Matchers.hasSize(0));

        // Create endpoint
        final GrafanaEndpoint endpoint = new GrafanaEndpoint();
        endpoint.setConnectTimeout(5);
        endpoint.setReadTimeout(10);
        endpoint.setApiKey("MY_KEY");
        endpoint.setDescription("DUMMY DESCRIPTION");
        endpoint.setUid("aBcDEfG");
        endpoint.setUrl("http://opennms.org");

        grafanaEndpointDao.save(endpoint);

        // Verify Creation
        assertThat(grafanaEndpointDao.findAll(), Matchers.hasSize(1));

        final GrafanaEndpoint persistedEndpoint = grafanaEndpointDao.get(endpoint.getId());
        assertEquals(endpoint.getApiKey(), persistedEndpoint.getApiKey());
        assertEquals(endpoint.getConnectTimeout(), persistedEndpoint.getConnectTimeout());
        assertEquals(endpoint.getDescription(), persistedEndpoint.getDescription());
        assertEquals(endpoint.getId(), persistedEndpoint.getId());
        assertEquals(endpoint.getReadTimeout(), persistedEndpoint.getReadTimeout());
        assertEquals(endpoint.getUid(), persistedEndpoint.getUid());
        assertEquals(endpoint.getUrl(), persistedEndpoint.getUrl());

        // Update Endpoint
        final GrafanaEndpoint updatedEndpoint = new GrafanaEndpoint();
        updatedEndpoint.setUid("dummy Uid");
        updatedEndpoint.setUrl("dummy Url");
        updatedEndpoint.setDescription("dummy description");
        updatedEndpoint.setApiKey("dummy api key");
        updatedEndpoint.setReadTimeout(200);
        updatedEndpoint.setConnectTimeout(300);
        persistedEndpoint.merge(updatedEndpoint);

        grafanaEndpointDao.update(persistedEndpoint);

        // Verify Update
        updatedEndpoint.setId(persistedEndpoint.getId());
        Assert.assertEquals(updatedEndpoint, grafanaEndpointDao.get(persistedEndpoint.getId()));

        // Delete
        grafanaEndpointDao.delete(persistedEndpoint);
        assertThat(grafanaEndpointDao.findAll(), Matchers.hasSize(0));
    }

}