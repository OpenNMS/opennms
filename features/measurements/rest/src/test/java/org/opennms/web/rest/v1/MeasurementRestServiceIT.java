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


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.common.collect.Maps;

/**
 * Used to make calls to an instance of MeasurementRestService.
 *
 * @author jwhite
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath*:/META-INF/opennms/component-measurement.xml",
        "classpath:/META-INF/opennms/applicationContext-measurements-rest-test.xml",
        "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml",
        "file:../../../opennms-webapp-rest/src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:../../../opennms-webapp-rest/src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.rrd.strategyClass=org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy"
})
@JUnitTemporaryDatabase
public class MeasurementRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    protected MonitoringLocationDao m_locationDao;

    @Autowired
    protected NodeDao m_nodeDao;

    @Autowired
    protected SnmpInterfaceDao m_snmpInterfaceDao;

    @Autowired
    private ServletContext m_context;

    @Autowired
    protected FilesystemResourceStorageDao m_resourceStorageDao;

    @Autowired
    private MeasurementsRestService restService;

    @Autowired
    private MeasurementsService service;

    
    public MeasurementRestServiceIT() {
        super("file:../../../opennms-webapp-rest/src/main/webapp/WEB-INF/applicationContext-cxf-rest-v1.xml");
    }

    @Before
    public void setUp() throws Throwable {
        super.setUp();

        BeanUtils.assertAutowiring(this);
        assertNotNull(restService);
        assertNotNull(service);

        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "node1");
        node.setId(1);

        OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface(node, 12);
        snmpInterface.setIfName("eth0");
        snmpInterface.setPhysAddr("04013f75f101");
        m_nodeDao.save(node);
        m_nodeDao.flush();

        File rrdDirectory = new File("src/test/resources/share/jrb");
        assertTrue(rrdDirectory.canRead());

        m_resourceStorageDao.setRrdDirectory(rrdDirectory);

        System.setProperty("rrd.base.dir", rrdDirectory.getAbsolutePath());
    }

    @Test
    public void notFoundOnMissingResource() throws Exception {
        sendRequest(GET, "/measurements/should_not_/exist", 404);
    }

    @Test
    public void verifyRelaxMode() throws Exception {
        sendRequest(GET, "/measurements/should_not_/exist?relaxed=true", 204);
    }

    /**
     * Used to test the marshaling of the result set to both
     * XML and JSON.
     */
    @Test
    public void canRetrieveMeasurementsAsXmlOrJson() throws Exception {
        final String url = String.format("/measurements/%s/%s",
                URLEncoder.encode("node[1].interfaceSnmp[eth0-04013f75f101]", StandardCharsets.UTF_8.name()),
                "ifInOctets");

        final Map<String, String> parameters = Maps.newHashMap();
        parameters.put("start", Long.toString(1414602000000L));
        parameters.put("end", Long.toString(1417046400000L));

        final MockHttpServletRequest request = createRequest(m_context, GET, url);
        request.setParameters(parameters);
        String xml = sendRequest(request, 200);

        // Now set Accept header and re-issue the request
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        String json = sendRequest(request, 200);

        assertThat(xml, containsString("<columns>"));
        assertThat(xml, containsString("<resources>"));
        assertThat(xml, containsString("<resource id="));
        assertThat(json, containsString("\"columns\":"));
        assertThat(json, containsString("\"resources\":"));
    }

    /**
     * Here we query the same resource as above, but refer to it using the ifIndex
     * instead of the interface name.
     */
    @Test
    public void canRetrieveMeasurementsUsingIfIndexAlias() throws Exception {
        final String url = String.format("/measurements/%s/%s",
                URLEncoder.encode("node[1].interfaceSnmpByIfIndex[12]", StandardCharsets.UTF_8.name()),
                "ifInOctets");

        final Map<String, String> parameters = Maps.newHashMap();
        parameters.put("start", Long.toString(1414602000000L));
        parameters.put("end", Long.toString(1417046400000L));

        final MockHttpServletRequest request = createRequest(m_context, GET, url);
        request.setParameters(parameters);
        String xml = sendRequest(request, 200);
        assertThat(xml, containsString("<columns>"));
    }

    @Test
    public void canRetrieveFilters() throws Exception {
        // Retrieve all filters
        String filtersXml = sendRequest(GET, "/measurements/filters", 200);
        assertThat(filtersXml, containsString("Chomp"));

        // Retrieve a specific filter by name
        filtersXml = sendRequest(GET, "/measurements/filters/chomp", 200);
        assertThat(filtersXml, containsString("Chomp"));
    }
}
