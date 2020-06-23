/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.discovery.DiscoveryTaskExecutor;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DiscoveryRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    ServiceRegistry serviceRegistry;

    public DiscoveryRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");

        if (serviceRegistry.findProvider(DiscoveryTaskExecutor.class) == null) {
            serviceRegistry.register(new DiscoveryTaskExecutor() {
                @Override
                public CompletableFuture<Void> handleDiscoveryTask(DiscoveryConfiguration discoveryConfiguration) {
                    checkConfiguration(discoveryConfiguration);
                    return CompletableFuture.completedFuture(null);
                }
            }, DiscoveryTaskExecutor.class);
        }
    }

    public void checkConfiguration(final DiscoveryConfiguration discoveryConfiguration) {
        assertNotNull(discoveryConfiguration);
        assertTrue(discoveryConfiguration.getChunkSize().isPresent());
        assertEquals(200, discoveryConfiguration.getChunkSize().get().intValue());
        assertTrue(discoveryConfiguration.getRetries().isPresent());
        assertEquals(6, discoveryConfiguration.getRetries().get().intValue());
        assertTrue(discoveryConfiguration.getTimeout().isPresent());
        assertEquals(4000, discoveryConfiguration.getTimeout().get().intValue());
        assertTrue(discoveryConfiguration.getForeignSource().isPresent());
        assertEquals("My-ForeignSource", discoveryConfiguration.getForeignSource().get());
        assertTrue(discoveryConfiguration.getLocation().isPresent());
        assertEquals("My-Location", discoveryConfiguration.getLocation().get());
        assertNotNull(discoveryConfiguration.getSpecifics());
        assertEquals(1, discoveryConfiguration.getSpecifics().size());
        assertTrue(discoveryConfiguration.getSpecifics().get(0).getRetries().isPresent());
        assertEquals(3, discoveryConfiguration.getSpecifics().get(0).getRetries().get().intValue());
        assertTrue(discoveryConfiguration.getSpecifics().get(0).getTimeout().isPresent());
        assertEquals(2000, discoveryConfiguration.getSpecifics().get(0).getTimeout().get().intValue());
        assertEquals("192.0.2.1", discoveryConfiguration.getSpecifics().get(0).getAddress());
        assertNotNull(discoveryConfiguration.getIncludeRanges());
        assertEquals(1, discoveryConfiguration.getIncludeRanges().size());
        assertTrue(discoveryConfiguration.getIncludeRanges().get(0).getRetries().isPresent());
        assertEquals(3, discoveryConfiguration.getIncludeRanges().get(0).getRetries().get().intValue());
        assertTrue(discoveryConfiguration.getIncludeRanges().get(0).getTimeout().isPresent());
        assertEquals(2000, discoveryConfiguration.getIncludeRanges().get(0).getTimeout().get().intValue());
        assertEquals("192.0.2.128", discoveryConfiguration.getIncludeRanges().get(0).getBegin());
        assertEquals("192.0.2.254", discoveryConfiguration.getIncludeRanges().get(0).getEnd());
        assertNotNull(discoveryConfiguration.getExcludeRanges());
        assertEquals(1, discoveryConfiguration.getExcludeRanges().size());
        assertEquals("192.0.2.0", discoveryConfiguration.getExcludeRanges().get(0).getBegin());
        assertEquals("192.0.2.63", discoveryConfiguration.getExcludeRanges().get(0).getEnd());
        assertNotNull(discoveryConfiguration.getIncludeUrls());
        assertEquals(0, discoveryConfiguration.getIncludeUrls().size());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testPostOneTimeScan() throws Exception {
        String oneTimeScan =
                "<discoveryConfiguration>\n" +
                        "  <location>My-Location</location>\n" +
                        "  <retries>6</retries>\n" +
                        "  <timeout>4000</timeout>\n" +
                        "  <chunkSize>200</chunkSize>\n" +
                        "  <foreignSource>My-ForeignSource</foreignSource>\n" +
                        "  <specifics>\n" +
                        "    <specific>\n" +
                        "      <retries>3</retries>\n" +
                        "      <timeout>2000</timeout>\n" +
                        "      <content>192.0.2.1</content>\n" +
                        "    </specific>\n" +
                        "  </specifics>\n" +
                        "  <includeRanges>\n" +
                        "    <includeRange>\n" +
                        "      <retries>3</retries>\n" +
                        "      <timeout>2000</timeout>\n" +
                        "      <begin>192.0.2.128</begin>\n" +
                        "      <end>192.0.2.254</end>\n" +
                        "    </includeRange>\n" +
                        "  </includeRanges>\n" +
                        "  <excludeRanges>\n" +
                        "    <excludeRange>\n" +
                        "      <begin>192.0.2.0</begin>\n" +
                        "      <end>192.0.2.63</end>\n" +
                        "    </excludeRange>\n" +
                        "  </excludeRanges>\n" +
                        "  <includeUrls>\n" +
                        "  </includeUrls>\n" +
                        "</discoveryConfiguration>";

        assertNotNull(sendPost("/discovery", oneTimeScan, 200, null));
    }
}
