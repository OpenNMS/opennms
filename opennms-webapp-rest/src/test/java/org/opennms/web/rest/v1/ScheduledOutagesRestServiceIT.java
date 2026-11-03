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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.dao.outages.api.OverrideablePollOutagesDao;
import org.opennms.netmgt.config.dao.thresholding.api.OverrideableThreshdDao;
import org.opennms.netmgt.config.poller.outages.Outage;
import org.opennms.netmgt.config.poller.outages.Outages;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

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
public class ScheduledOutagesRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private JAXBContext m_jaxbContext;
    private FilterDao m_filterDao;
    private String m_onmsHome;

    @Autowired
    private ServletContext m_servletContext;
    
    @Autowired
    private OverrideableThreshdDao m_threshdDao;
    
    @Autowired
    private OverrideablePollOutagesDao m_pollOutagesDao;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private TransactionTemplate m_transactionTemplate;

    // Windows that do and do not cover the time the test runs, whenever that is.
    private static final String ALWAYS = "<time begins='01-Jan-2000 00:00:00' ends='31-Dec-2099 23:59:59' />";
    private static final String PAST = "<time begins='01-Jan-2000 00:00:00' ends='02-Jan-2000 00:00:00' />";

    @Override
    protected void beforeServletStart() throws Exception {
        MockLogAppender.setupLogging();
        File etc = new File("target/test-work-dir/etc");
        etc.mkdirs();
        m_onmsHome = etc.getParent();
        System.setProperty("opennms.home", m_onmsHome);
        ConfigurationTestUtils.setRelativeHomeDirectory(m_onmsHome);

        // Setup Scheduled Outages Configuration
        File outagesConfig = new File(etc, "poll-outages.xml");
        FileUtils.writeStringToFile(outagesConfig, "<?xml version=\"1.0\"?>"
                + "<outages>"
                + "<outage name='my-junit-test' type='weekly'>"
                + "<time day='monday' begins='13:30:00' ends='13:30:01'/>"
                + "<interface address='match-any'/>"
                + "<node id='18'/><node id='40'/>"
                + "</outage>"
                + "</outages>", Charset.defaultCharset());
        m_pollOutagesDao.overrideConfig(new FileSystemResource(outagesConfig).getInputStream());

        // Setup Filter DAO
        m_filterDao = mock(FilterDao.class);
        when(m_filterDao.getActiveIPAddressList("IPADDR != '0.0.0.0'")).thenReturn(Collections.singletonList(InetAddressUtils.getLocalHostAddress()));
        
        FilterDaoFactory.setInstance(m_filterDao);

        // Setup Collectd Configuration
        File collectdConfig = new File(etc, "collectd-configuration.xml");
        FileUtils.writeStringToFile(collectdConfig, "<?xml version=\"1.0\"?>"
                + "<collectd-configuration threads=\"50\">"
                + "<package name=\"example1\">"
                + "<filter>IPADDR != '0.0.0.0'</filter>"
                + "<include-range begin=\"1.1.1.1\" end=\"254.254.254.254\"/>"
                + "<service name=\"SNMP\" interval=\"300000\" user-defined=\"false\" status=\"on\">"
                + "<parameter key=\"collection\" value=\"default\"/>"
                + "</service>"
                + "</package>"
                + "<collector service=\"SNMP\" class-name=\"org.opennms.netmgt.collectd.SnmpCollector\"/>"
                + "</collectd-configuration>", Charset.defaultCharset());
        new CollectdConfigFactory(new FileInputStream(collectdConfig));

        // Setup Pollerd Configuration
        File pollerdConfig = new File(etc, "poller-configuration.xml");
        FileUtils.writeStringToFile(pollerdConfig, "<?xml version=\"1.0\"?>"
                + "<poller-configuration threads=\"10\" nextOutageId=\"SELECT nextval(\'outageNxtId\')\" serviceUnresponsiveEnabled=\"false\">"
                + "<node-outage status=\"on\" pollAllIfNoCriticalServiceDefined=\"true\"></node-outage>"
                + "<package name=\"example1\">"
                + "<filter>IPADDR != '0.0.0.0'</filter>"
                + "<rrd step = \"300\">"
                + "<rra>RRA:AVERAGE:0.5:1:2016</rra>"
                + "<rra>RRA:AVERAGE:0.5:12:4464</rra>"
                + "<rra>RRA:MIN:0.5:12:4464</rra>"
                + "<rra>RRA:MAX:0.5:12:4464</rra>"
                + "</rrd>"
                + "<service name=\"ICMP\" interval=\"300000\"/>"
                + "<downtime begin=\"0\" end=\"30000\"/>"
                + "</package>"
                + "<monitor service=\"ICMP\" class-name=\"org.opennms.netmgt.mock.MockMonitor\"/>"
                + "</poller-configuration>", Charset.defaultCharset());
        PollerConfigFactory.setInstance(new PollerConfigFactory(1, new FileInputStream(pollerdConfig)));

        // Setup Threshd Configuration
        File threshdConfig = new File(etc, "threshd-configuration.xml");
        FileUtils.writeStringToFile(threshdConfig, "<?xml version=\"1.0\"?>"
                + "<threshd-configuration threads=\"5\">"
                + "<package name=\"example1\">"
                + "<filter>IPADDR != '0.0.0.0'</filter>"
                + "<include-range begin=\"1.1.1.1\" end=\"254.254.254.254\"/>"
                + "<service name=\"SNMP\" interval=\"300000\" user-defined=\"false\" status=\"on\">"
                + "<parameter key=\"thresholding-group\" value=\"mib2\"/>"
                + "</service>"
                + "</package>"
                + "</threshd-configuration>", Charset.defaultCharset());
        m_threshdDao.overrideConfig(new FileInputStream(threshdConfig));

        // Setup Notifid Configuration
        FileUtils.writeStringToFile(new File(etc, "notifd-configuration.xml"), "<?xml version=\"1.0\"?>"
                + "<notifd-configuration status=\"off\" match-all=\"true\">"
                + "<queue><queue-id>default</queue-id><interval>20s</interval>"
                + "<handler-class><name>org.opennms.netmgt.notifd.DefaultQueueHandler</name></handler-class>"
                + "</queue>"
                + "</notifd-configuration>", Charset.defaultCharset());
        NotifdConfigFactory.init();

        m_jaxbContext = JaxbUtils.getContextFor(Outages.class);
    }

    // This is required in order to avoid override configuration files in opennms-base-assembly
    @Override
    public void afterServletStart() {
        System.setProperty("opennms.home", m_onmsHome);
        ConfigurationTestUtils.setRelativeHomeDirectory(m_onmsHome);
    }

    @Override
    public void afterServletDestroy() {
        verify(m_filterDao, atLeastOnce()).flushActiveIpAddressListCache();
        verify(m_filterDao, atLeastOnce()).getActiveIPAddressList(anyString());
        verifyNoMoreInteractions(m_filterDao);
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void testGetOutages() throws Exception {
        String url = "/sched-outages";
        Outages outages = getXmlObject(m_jaxbContext, url, 200, Outages.class);
        Assert.assertNotNull(outages);
        Assert.assertEquals(1, outages.getOutages().size());
        Assert.assertEquals("match-any", outages.getOutages().get(0).getInterfaces().get(0).getAddress());
    }

    @Test
    public void testGetOutage() throws Exception {
        String url = "/sched-outages/my-junit-test";
        Outage outage = getXmlObject(m_jaxbContext, url, 200, Outage.class);
        Assert.assertNotNull(outage);
        Assert.assertEquals("match-any", outage.getInterfaces().get(0).getAddress());
    }

    @Test
    public void testGetOutageJson() throws Exception {
        String url = "/sched-outages";

        // GET all items
        MockHttpServletRequest jsonRequest = createRequest(m_servletContext, GET, url);
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        String json = sendRequest(jsonRequest, 200);

        JSONObject restObject = new JSONObject(json);
        JSONObject expectedObject = new JSONObject(IOUtils.toString(new FileInputStream("src/test/resources/v1/sched-outages.json"), Charset.defaultCharset()));
        JSONAssert.assertEquals(expectedObject, restObject, true);
    }

    @Test
    public void testSetOutage() throws Exception {
        String url = "/sched-outages";
        String outage = "<?xml version=\"1.0\"?>" +
                "<outage name='test-outage' type='specific'>" +
                "<time day='friday' begins='13:20:00' ends='15:30:00' />" +
                "<time begins='17-Feb-2012 19:20:00' ends='18-Feb-2012 22:30:00' />" +
                "<node id='11' />" +
                "</outage>";
        sendPost(url, outage, 201, null);

        Outage out = getXmlObject(m_jaxbContext, "/sched-outages/test-outage", 200, Outage.class);
        Assert.assertNotNull(out);
        Assert.assertEquals("13:20:00", out.getTimes().get(0).getBegins());

        // Update the outage slightly
        outage = "<?xml version=\"1.0\"?>" +
                "<outage name='test-outage' type='specific'>" +
                "<time day='friday' begins='14:20:00' ends='15:30:00' />" +
                "<time begins='17-Feb-2012 19:20:00' ends='18-Feb-2012 22:30:00' />" +
                "<node id='11' />" +
                "</outage>";
        sendPost(url, outage, 204, null);

        out = getXmlObject(m_jaxbContext, "/sched-outages/test-outage", 200, Outage.class);
        Assert.assertNotNull(out);
        Assert.assertEquals("14:20:00", out.getTimes().get(0).getBegins());
    }

    @Test
    public void testDeleteOutage() throws Exception {
        sendRequest(DELETE, "/sched-outages/my-junit-test", 204);
        verify(m_filterDao, atLeastOnce()).validateRule(anyString());
    }

    @Test
    public void testUpdateCollectdConfig() throws Exception {
        sendRequest(PUT, "/sched-outages/my-junit-test/collectd/example1", 204);
        sendRequest(DELETE, "/sched-outages/my-junit-test/collectd/example1", 204);
    }

    @Test
    public void testUpdatePollerdConfig() throws Exception {
        sendRequest(PUT, "/sched-outages/my-junit-test/pollerd/example1", 204);
        sendRequest(DELETE, "/sched-outages/my-junit-test/pollerd/example1", 204);
        verify(m_filterDao, atLeastOnce()).validateRule(anyString());
    }

    @Test
    public void testUpdateThreshdConfig() throws Exception {
        sendRequest(PUT, "/sched-outages/my-junit-test/threshd/example1", 204);
        sendRequest(DELETE, "/sched-outages/my-junit-test/threshd/example1", 204);
    }

    @Test
    public void testUpdateNotifdConfig() throws Exception {
        sendRequest(PUT, "/sched-outages/my-junit-test/notifd", 204);
        sendRequest(DELETE, "/sched-outages/my-junit-test/notifd", 204);
    }

    @Test
    public void testNodeInOutage() throws Exception {
        Assert.assertEquals("false", sendRequest(GET, "/sched-outages/my-junit-test/nodeInOutage/1", 200));
        Assert.assertEquals("false", sendRequest(GET, "/sched-outages/nodeInOutage/1", 200));
    }

    @Test
    public void testInterfaceInOutage() throws Exception {
        Assert.assertEquals("false", sendRequest(GET, "/sched-outages/my-junit-test/interfaceInOutage/1.1.1.1", 200));
        Assert.assertEquals("false", sendRequest(GET, "/sched-outages/interfaceInOutage/1.1.1.1", 200));
    }

    @Test
    public void testGetApplicability() throws Exception {
        JSONObject applies = getApplicability("/sched-outages/my-junit-test/applies-to");
        Assert.assertFalse(applies.getBoolean("notifications"));
        // every subsystem lists the example1 package, unreferenced initially
        Assert.assertFalse(appliedFor(applies, "pollers", "example1"));
        Assert.assertFalse(appliedFor(applies, "collectors", "example1"));
        Assert.assertFalse(appliedFor(applies, "thresholders", "example1"));
    }

    @Test
    public void testApplicabilityReflectsMembership() throws Exception {
        sendRequest(PUT, "/sched-outages/my-junit-test/pollerd/example1", 204);
        sendRequest(PUT, "/sched-outages/my-junit-test/notifd", 204);

        JSONObject applies = getApplicability("/sched-outages/my-junit-test/applies-to");
        Assert.assertTrue(applies.getBoolean("notifications"));
        Assert.assertTrue(appliedFor(applies, "pollers", "example1"));
        Assert.assertFalse(appliedFor(applies, "collectors", "example1"));

        // the name-less variant exposes the raw calendars, so the list page can
        // derive every outage's memberships from a single call
        JSONObject all = getApplicability("/sched-outages/applies-to");
        Assert.assertTrue(calendarsFor(all, "pollers", "example1").contains("my-junit-test"));
        // single-element JAXB lists may collapse to a bare string in JSON
        Assert.assertTrue(all.get("notification-calendars").toString().contains("my-junit-test"));
        verify(m_filterDao, atLeastOnce()).validateRule(anyString());
    }

    @Test
    public void testGetApplicabilityForNewOutage() throws Exception {
        // the name-less variant lists packages with nothing applied
        JSONObject applies = getApplicability("/sched-outages/applies-to");
        Assert.assertFalse(applies.getBoolean("notifications"));
        Assert.assertFalse(appliedFor(applies, "pollers", "example1"));
    }

    @Test
    public void testActiveForNodeByNodeId() throws Exception {
        populate();
        final int node1 = m_databasePopulator.getNode1().getId();
        final int node2 = m_databasePopulator.getNode2().getId();
        postOutage("active-node", ALWAYS, "<node id='" + node1 + "' />");

        Assert.assertEquals(List.of("active-node"), activeOutageNames(node1));
        Assert.assertEquals(List.of(), activeOutageNames(node2));
    }

    // The node page also lists an outage that names none of the node's ids but covers one of its
    // interfaces.
    @Test
    public void testActiveForNodeByInterface() throws Exception {
        populate();
        final int node2 = m_databasePopulator.getNode2().getId();
        postOutage("active-interface", ALWAYS, "<interface address='192.168.2.2' />");

        Assert.assertEquals(List.of("active-interface"), activeOutageNames(node2));
        Assert.assertEquals(List.of(), activeOutageNames(m_databasePopulator.getNode3().getId()));
    }

    @Test
    public void testActiveForNodeIgnoresDeletedInterfaces() throws Exception {
        populate();
        final int node2 = m_databasePopulator.getNode2().getId();
        postOutage("active-interface", ALWAYS, "<interface address='192.168.2.2' />");
        m_transactionTemplate.execute(status -> {
            m_ipInterfaceDao.findByNodeIdAndIpAddress(node2, "192.168.2.2").setIsManaged("D");
            m_ipInterfaceDao.flush();
            return null;
        });

        Assert.assertEquals(List.of(), activeOutageNames(node2));
    }

    // Neither 'past' nor my-junit-test -- which covers every interface (match-any), but only for one
    // second on Mondays -- is in effect.
    @Test
    public void testActiveForNodeIgnoresOutagesNotInEffect() throws Exception {
        populate();
        final int node1 = m_databasePopulator.getNode1().getId();
        postOutage("past", PAST, "<node id='" + node1 + "' />");
        postOutage("active-node", ALWAYS, "<node id='" + node1 + "' />");

        Assert.assertEquals(List.of("active-node"), activeOutageNames(node1));
    }

    @Test
    public void testActiveForUnknownNode() throws Exception {
        Assert.assertEquals(List.of(), activeOutageNames(999999));
    }

    // The temporary database lives for the whole class, so reset first: each test starts from the
    // same rows whichever order they run in, and one that edits an interface cannot leak into another.
    private void populate() {
        m_transactionTemplate.execute(status -> {
            m_databasePopulator.resetDatabase();
            m_databasePopulator.populateDatabase();
            return null;
        });
    }

    private void postOutage(final String name, final String time, final String covers) throws Exception {
        sendPost("/sched-outages", "<?xml version=\"1.0\"?><outage name='" + name + "' type='specific'>"
                + time + covers + "</outage>", 201, null);
    }

    private List<String> activeOutageNames(final int nodeId) throws Exception {
        final MockHttpServletRequest request = createRequest(m_servletContext, GET, "/sched-outages/activeForNode/" + nodeId);
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        // A bare array of names: getString fails on anything that is not a string, such as a whole calendar.
        final JSONArray outages = new JSONArray(sendRequest(request, 200));
        final List<String> names = new ArrayList<>();
        for (int i = 0; i < outages.length(); i++) {
            names.add(outages.getString(i));
        }
        return names;
    }

    private JSONObject getApplicability(String url) throws Exception {
        MockHttpServletRequest request = createRequest(m_servletContext, GET, url);
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        return new JSONObject(sendRequest(request, 200));
    }

    private static boolean appliedFor(JSONObject root, String subsystem, String packageName) {
        JSONObject pkg = packageFor(root, subsystem, packageName);
        return pkg != null && pkg.getBoolean("applied");
    }

    private static String calendarsFor(JSONObject root, String subsystem, String packageName) {
        JSONObject pkg = packageFor(root, subsystem, packageName);
        // a single-element JAXB list may serialize as a bare string, so compare on toString
        return pkg == null || !pkg.has("calendars") ? "" : pkg.get("calendars").toString();
    }

    private static JSONObject packageFor(JSONObject root, String subsystem, String packageName) {
        org.json.JSONArray packages = root.optJSONArray(subsystem);
        if (packages == null) {
            return null;
        }
        for (int i = 0; i < packages.length(); i++) {
            JSONObject pkg = packages.getJSONObject(i);
            if (packageName.equals(pkg.getString("name"))) {
                return pkg;
            }
        }
        return null;
    }
}
