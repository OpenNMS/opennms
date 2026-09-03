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
package org.opennms.core.wsman.fake;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.mate.api.EmptyScope;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.collection.api.ResourceTypeMapper;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.ResourceTypes;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.WSManVersion;
import org.opennms.core.wsman.cxf.CXFWSManClientFactory;
import org.opennms.netmgt.collectd.WsManCollector;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.core.collection.test.CollectionSetUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.jaxb.WSManConfigDaoJaxb;
import org.opennms.netmgt.dao.jaxb.WSManDataCollectionConfigDaoJaxb;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.monitors.WsManMonitor;
import org.opennms.netmgt.provision.DetectResults;
import org.opennms.netmgt.provision.detector.wsman.WsManDetector;
import org.springframework.core.io.FileSystemResource;

/**
 * The real detector, monitor and collector against the fake agent, driven by
 * a wsman-config.xml written the way the Manage WS-Man page writes it and by
 * the shipped Windows data collection files.
 */
public class WsManEndToEndTest {

    private static final String WMI = "http://schemas.microsoft.com/wbem/wsman/1/wmi/root/cimv2/";

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private FakeWsManAgent agent;
    private WSManConfigDaoJaxb configDao;
    private WSManDataCollectionConfigDaoJaxb dataCollectionDao;
    private InetAddress address;

    @Before
    public void start() throws Exception {
        agent = FakeWsManAgent.onLoopback("LAB\\\\wsman-monitor", "lab-secret").start();
        address = InetAddressUtils.addr("127.0.0.1");

        // wsman-config.xml as the page writes it: a definition naming this server with its credentials
        final File home = temp.newFolder("opennms-home");
        final File etc = new File(home, "etc");
        etc.mkdirs();
        Files.write(new File(etc, "wsman-config.xml").toPath(), (""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<wsman-config xmlns=\"http://xmlns.opennms.org/xsd/config/wsman\" retry=\"1\" timeout=\"5000\" ssl=\"true\" username=\"root\" password=\"calvin\">\n"
                + "   <definition requisition=\"wsman-lab\" username=\"LAB\\\\wsman-monitor\" password=\"lab-secret\" port=\"" + agent.getPort() + "\" ssl=\"false\" path=\"/wsman\" product-vendor=\"Microsoft\" product-version=\"" + FakeWsManAgent.DEFAULT_VERSION + "\">\n"
                + "      <specific>127.0.0.1</specific>\n"
                + "   </definition>\n"
                + "</wsman-config>\n").getBytes(StandardCharsets.UTF_8));
        configDao = new WSManConfigDaoJaxb();
        configDao.setConfigResource(new FileSystemResource(new File(etc, "wsman-config.xml")));
        configDao.afterPropertiesSet();

        // the shipped data collection files, from the copies bundled for the reset feature
        copyResource("wsman-defaults/wsman-datacollection-config.xml", new File(etc, "wsman-datacollection-config.xml"));
        final File dropIns = new File(etc, "wsman-datacollection.d");
        dropIns.mkdirs();
        copyResource("wsman-defaults/wsman-datacollection.d/microsoft-windows.xml", new File(dropIns, "microsoft-windows.xml"));
        dataCollectionDao = new WSManDataCollectionConfigDaoJaxb();
        dataCollectionDao.setOpennmsHome(home.toPath());

        // the shipped resource types the Windows groups store their rows under
        final Map<String, ResourceType> resourceTypes = new HashMap<>();
        try (var in = WsManEndToEndTest.class.getClassLoader().getResourceAsStream("wsman-defaults/resource-types.d/wsman-microsoft-windows.xml")) {
            assertTrue("missing shipped resource types", in != null);
            for (final ResourceType type : JaxbUtils.unmarshal(ResourceTypes.class, new InputStreamReader(in, StandardCharsets.UTF_8)).getResourceTypes()) {
                resourceTypes.put(type.getName(), type);
            }
        }
        ResourceTypeMapper.getInstance().setResourceTypeMapper(resourceTypes::get);
    }

    private static void copyResource(final String name, final File target) throws Exception {
        try (var in = WsManEndToEndTest.class.getClassLoader().getResourceAsStream(name)) {
            assertTrue("missing bundled resource " + name, in != null);
            Files.copy(in, target.toPath());
        }
    }

    @After
    public void stop() {
        agent.close();
        ResourceTypeMapper.getInstance().setResourceTypeMapper(null);
    }

    @Test
    public void detectorIdentifiesTheAgentWithTheDefinitionsCredentials() {
        final WsManDetector detector = new WsManDetector();
        detector.setClientFactory(new CXFWSManClientFactory());
        final WSManEndpoint endpoint = configDao.getEndpoint(address);
        assertEquals("LAB\\\\wsman-monitor", endpoint.getUsername());
        final DetectResults results = detector.isServiceDetected(address, endpoint);
        assertTrue(results.isServiceDetected());
        assertEquals(FakeWsManAgent.DEFAULT_VENDOR, results.getServiceAttributes().get("product-vendor"));

        // wrong credentials, as when the definition is edited badly, are not detected
        final WSManEndpoint bad = new WSManEndpoint.Builder(endpoint.getUrl()).withServerVersion(WSManVersion.WSMAN_1_0).withBasicAuth("root", "calvin").build();
        assertFalse(detector.isServiceDetected(address, bad).isServiceDetected());
    }

    @Test
    public void monitorPollsUpThroughTransferGetAndDownWhenTheRuleFails() {
        final WsManMonitor monitor = new WsManMonitor();
        monitor.setWSManClientFactory(new CXFWSManClientFactory());
        monitor.setWSManConfigDao(configDao);
        final MonitoredService svc = mock(MonitoredService.class);
        when(svc.getAddress()).thenReturn(address);
        when(svc.getIpAddr()).thenReturn("127.0.0.1");
        when(svc.getNodeId()).thenReturn(1);

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("resource-uri", WMI + "Win32_OperatingSystem");
        parameters.put("rule", "#Caption matches '.*'");
        parameters.putAll(Interpolator.interpolateAttributes(monitor.getRuntimeAttributes(svc, parameters), EmptyScope.EMPTY));
        assertEquals(PollStatus.SERVICE_AVAILABLE, monitor.poll(svc, parameters).getStatusCode());

        parameters.put("rule", "#Caption matches 'Linux.*'");
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, monitor.poll(svc, parameters).getStatusCode());
    }

    @Test
    public void collectorCollectsTheShippedWindowsGroupsAndFollowsMetricChanges() throws Exception {
        final OnmsNode node = mock(OnmsNode.class);
        when(node.getAssetRecord()).thenReturn(new OnmsAssetRecord());
        final NodeDao nodeDao = mock(NodeDao.class);
        when(nodeDao.get(any(Integer.class))).thenReturn(node);

        final WsManCollector collector = new WsManCollector();
        collector.setWSManClientFactory(new CXFWSManClientFactory());
        collector.setWSManConfigDao(configDao);
        collector.setWSManDataCollectionConfigDao(dataCollectionDao);
        collector.setNodeDao(nodeDao);

        final CollectionAgent agent = mock(CollectionAgent.class);
        when(agent.getAddress()).thenReturn(address);
        when(agent.getNodeId()).thenReturn(1);
        when(agent.getStorageResourcePath()).thenReturn(ResourcePath.get());

        // the Windows system definition matches through the vendor the definition carries
        final Map<String, Object> params = new HashMap<>();
        params.put("collection", "default");
        params.putAll(Interpolator.interpolateAttributes(collector.getRuntimeAttributes(agent, params), EmptyScope.EMPTY));
        CollectionSet set = collector.collect(agent, params);
        assertEquals(CollectionStatus.SUCCEEDED, set.getStatus());
        Map<String, CollectionAttribute> values = CollectionSetUtils.getAttributesByName(set);
        assertEquals(9123456.0, values.get("freePhysMem").getNumericValue().doubleValue(), 0.0);
        assertEquals(16776692.0, values.get("totalVisibleMem").getNumericValue().doubleValue(), 0.0);
        assertTrue("per-CPU group produced attributes: " + values.keySet(), values.containsKey("wrmOSCpuPctProcTime"));
        // the shipped Windows memory and process graphs read these node-level attributes
        assertEquals(9342418944.0, values.get("wrmOSMemAvailBytes").getNumericValue().doubleValue(), 0.0);
        assertEquals(187.0, values.get("wrmOSObjProcesses").getNumericValue().doubleValue(), 0.0);
        final long cpuResources = CollectionSetUtils.getResourcesByLabel(set).values().stream()
                .filter(r -> "wrmOSCpu".equals(r.getResourceTypeName())).count();
        assertEquals("one resource per fake CPU instance", 2, cpuResources);

        // a value changed on the agent shows up in the next collection
        this.agent.set("Win32_OperatingSystem.FreePhysicalMemory=1234");
        set = collector.collect(agent, params);
        values = CollectionSetUtils.getAttributesByName(set);
        assertEquals(1234.0, values.get("freePhysMem").getNumericValue().doubleValue(), 0.0);
    }

    @Test
    public void definitionFileEditsReachTheDaemonsWithoutARestart() throws Exception {
        // rewrite the file the way a PUT does: same server, different credentials
        final Path file = new File(configDao.getConfigResource().getFile().getParentFile(), "wsman-config.xml").toPath();
        final String edited = new String(Files.readAllBytes(file), StandardCharsets.UTF_8).replace("lab-secret", "rotated");
        Thread.sleep(1100); // FileReloadContainer compares mtime at second granularity on some filesystems
        Files.write(file, edited.getBytes(StandardCharsets.UTF_8));
        assertEquals("rotated", configDao.getEndpoint(address).getPassword());

        final WsManDetector detector = new WsManDetector();
        detector.setClientFactory(new CXFWSManClientFactory());
        assertFalse("the agent still expects the old password", detector.isServiceDetected(address, configDao.getEndpoint(address)).isServiceDetected());
        agent.close();
        agent = new FakeWsManAgent("127.0.0.1", configDao.getEndpoint(address).getUrl().getPort(), "LAB\\\\wsman-monitor", "rotated").start();
        assertTrue(detector.isServiceDetected(address, configDao.getEndpoint(address)).isServiceDetected());
    }
}
