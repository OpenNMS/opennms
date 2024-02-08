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
package org.opennms.web.rest.v1;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.rrd.RrdStrategyFactory;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class GraphRestServiceIT extends AbstractSpringJerseyRestTestCase {
    @Autowired
    private DatabasePopulator m_dbPopulator;

    @Autowired
    private FilesystemResourceStorageDao m_resourceStorageDao;

    @Autowired
    private RrdStrategyFactory m_rrdStrategyFactory;

    @Rule
    public TemporaryFolder m_tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Throwable {
        super.setUp();

        // Add some nodes
        m_dbPopulator.populateDatabase();

        // Point to our temporary directory
        m_resourceStorageDao.setRrdDirectory(m_tempFolder.getRoot());

        // Add some blank RRD files
        final String extension = m_rrdStrategyFactory.getStrategy().getDefaultFileExtension();
        File nodeSnmp1 = m_tempFolder.newFolder("snmp", "1");
        FileUtils.touch(new File(nodeSnmp1, "SwapIn" + extension));
        FileUtils.touch(new File(nodeSnmp1, "SwapOut" + extension));
    }

    @Test
    public void testGraphs() throws Exception {
        // Top level
        String url = "/graphs";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("mib2.packets"));

        // By name
        url = "/graphs/mib2.packets";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("Unicast Packets In/Out"));

        // 404 on invalid name
        url = "/graphs/should.not.exist";
        sendRequest(GET, url, 404);

        // By resource
        url = "/graphs/for/" + URLEncoder.encode("node[1].nodeSnmp[]", StandardCharsets.UTF_8.name());
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("netsnmp.swapinout"));

        // 404 on invalid resource
        url = "/graphs/for/" + URLEncoder.encode("node[99].nodeSnmp[]", StandardCharsets.UTF_8.name());
        sendRequest(GET, url, 404);

        url = "/graphs/fornode/1";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("netsnmp.swapinout"));
        assertTrue(xml.contains("Node-level Performance Data"));
    }

}
