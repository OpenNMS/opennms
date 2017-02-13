/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",

        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class PollerConfigurationResourceIT extends AbstractSpringJerseyRestTestCase {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(PollerConfigurationResourceIT.class);

    @Autowired
    private MonitoringLocationDao m_monitoringLocationDao;

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        OnmsMonitoringLocation location = new OnmsMonitoringLocation("RDU", "East Coast", new String[] { "example1" }, new String[] { "example1" }, "Research Triangle Park, NC", 35.715751f, -79.16262f, 1L);
        m_monitoringLocationDao.saveOrUpdate(location);
        location = new OnmsMonitoringLocation("00002", "IN", new String[] { "example2" }, new String[0], "2 Open St., Network, MS 00002", 38.2096f, -85.8704f, 100L, "even");
        m_monitoringLocationDao.saveOrUpdate(location);
        location = new OnmsMonitoringLocation("00003", "IN", new String[] { "example2" }, new String[] { "example2" }, "2 Open St., Network, MS 00002", 38.2096f, -85.8704f, 100L, "odd");
        m_monitoringLocationDao.saveOrUpdate(location);
    }
    
    @Test
    public void testPollerConfig() throws Exception {
        sendRequest(GET, "/config/foo/polling", 404);

        String xml = sendRequest(GET, "/config/RDU/polling", 200);
        PollerConfiguration config = JaxbUtils.unmarshal(PollerConfiguration.class, xml);
        assertNotNull(config);
        assertEquals(1, config.getPackages().size());
        assertEquals("example1", config.getPackages().get(0).getName());
        assertEquals(17, config.getMonitors().size());

        xml = sendRequest(GET, "/config/00002/polling", 200);
        config = JaxbUtils.unmarshal(PollerConfiguration.class, xml);
        assertNotNull(config);
        assertEquals(1, config.getPackages().size());
        assertEquals("example2", config.getPackages().get(0).getName());
        assertEquals(1, config.getMonitors().size());

        xml = sendRequest(GET, "/config/00003/polling", 200);
        config = JaxbUtils.unmarshal(PollerConfiguration.class, xml);
        assertNotNull(config);
        assertEquals(1, config.getPackages().size());
        assertEquals("example2", config.getPackages().get(0).getName());
        assertEquals(1, config.getMonitors().size());
    }

}
