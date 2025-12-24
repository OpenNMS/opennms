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
package org.opennms.web.rest.v1.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class JmxDataCollectionConfigResourceIT extends AbstractSpringJerseyRestTestCase {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(JmxDataCollectionConfigResourceIT.class);

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    public void testJmxConfig() throws Exception {
        sendRequest(GET, "/config/jmx/notfound", 404);

        String xml = sendRequest(GET, "/config/jmx", 200);
        JmxDatacollectionConfig config = JaxbUtils.unmarshal(JmxDatacollectionConfig.class, xml);

        assertNotNull(config);

        assertThat(config.getJmxCollectionCount(), greaterThanOrEqualTo(6));

        assertEquals("jmx-jboss", config.getJmxCollection("jmx-jboss").getName());
        assertEquals(300, config.getJmxCollection("jmx-jboss").getRrd().getStep());
        assertThat(config.getJmxCollection("jmx-jboss").getMbeanCount(), greaterThanOrEqualTo(4));

        assertEquals("jsr160", config.getJmxCollection("jsr160").getName());
        assertEquals(300, config.getJmxCollection("jsr160").getRrd().getStep());
        assertThat(config.getJmxCollection("jsr160").getMbeanCount(), greaterThanOrEqualTo(38));

        assertEquals("jmx-minion", config.getJmxCollection("jmx-minion").getName());
        assertEquals(300, config.getJmxCollection("jmx-minion").getRrd().getStep());
        assertThat(config.getJmxCollection("jmx-minion").getMbeanCount(), greaterThanOrEqualTo(10));

        assertEquals("jmx-cassandra30x", config.getJmxCollection("jmx-cassandra30x").getName());
        assertEquals(300, config.getJmxCollection("jmx-cassandra30x").getRrd().getStep());
        assertThat(config.getJmxCollection("jmx-cassandra30x").getMbeanCount(), greaterThanOrEqualTo(53));

        assertEquals("jmx-cassandra30x-newts", config.getJmxCollection("jmx-cassandra30x-newts").getName());
        assertEquals(300, config.getJmxCollection("jmx-cassandra30x-newts").getRrd().getStep());
        assertThat(config.getJmxCollection("jmx-cassandra30x-newts").getMbeanCount(), greaterThanOrEqualTo(22));
    }

}
