/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/org/opennms/web/rest/applicationContext-test.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-reportingCore.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/org/opennms/web/svclayer/applicationContext-svclayer.xml",
        "classpath:/META-INF/opennms/applicationContext-mockEventProxy.xml",
        "classpath:/applicationContext-jersey-test.xml",
        "classpath:/META-INF/opennms/applicationContext-reporting.xml",
        "classpath:/META-INF/opennms/applicationContext-mock-usergroup.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-spring-security.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-jersey.xml",
        "classpath:/applicationContext-collectd-rest-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class JmxDataCollectionConfigResourceTest extends AbstractSpringJerseyRestTestCase {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(PollerConfigurationResourceTest.class);

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    public void testPollerConfig() throws Exception {
        sendRequest(GET, "/config/jmx/notfound", 404);

        String xml = sendRequest(GET, "/config/jmx", 200);
        JmxDatacollectionConfig config = JaxbUtils.unmarshal(JmxDatacollectionConfig.class, xml);

        assertNotNull(config);

        assertEquals(2, config.getJmxCollectionCount());
        assertEquals(config.getJmxCollection(0).getRrd().getStep(), 300);
        assertEquals("jboss", config.getJmxCollection(0).getName());
        assertEquals(4, config.getJmxCollection(0).getMbeans().getMbeanCount());
        assertEquals(config.getJmxCollection(1).getRrd().getStep(), 300);
        assertEquals("jsr160", config.getJmxCollection(1).getName());
        assertEquals(22, config.getJmxCollection(1).getMbeans().getMbeanCount());
    }

}
