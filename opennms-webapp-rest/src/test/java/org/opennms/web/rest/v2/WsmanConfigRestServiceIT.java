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
package org.opennms.web.rest.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.mate.api.EmptyScope;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.core.xml.AbstractMergingJaxbConfigDao;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.dao.WSManDataCollectionConfigDao;
import org.opennms.netmgt.collectd.WsManCollector;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
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
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class WsmanConfigRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static final String DEFINITION_10 = "{\"ranges\":[{\"begin\":\"10.0.0.1\",\"end\":\"10.0.0.50\"}],\"specifics\":[],\"ipMatches\":[],"
            + "\"username\":\"monitor\",\"password\":\"secret-one\",\"clearPassword\":false,\"ssl\":false,\"port\":5985}";

    @Autowired
    private WSManConfigDao m_wsManConfigDao;

    @Autowired
    private WSManDataCollectionConfigDao m_wsManDataCollectionConfigDao;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private SessionUtils m_sessionUtils;

    private Resource m_originalResource;
    private File m_workingCopy;
    private String m_shippedContent;
    private java.nio.file.Path m_originalHome;
    private java.nio.file.Path m_workingHome;

    public WsmanConfigRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    // The service reads and rewrites the DAO's config resource, so point the
    // shared DAO at a scratch copy of the shipped file for the duration of a test.
    @Before
    public void useWorkingCopy() throws Exception {
        final AbstractJaxbConfigDao<?, ?> dao = (AbstractJaxbConfigDao<?, ?>) m_wsManConfigDao;
        m_originalResource = dao.getConfigResource();
        m_shippedContent = new String(Files.readAllBytes(m_originalResource.getFile().toPath()), StandardCharsets.UTF_8);
        m_workingCopy = new File("target/test-work-dir", "wsman-config-" + UUID.randomUUID() + ".xml");
        m_workingCopy.getParentFile().mkdirs();
        Files.write(m_workingCopy.toPath(), m_shippedContent.getBytes(StandardCharsets.UTF_8));
        dao.setConfigResource(new FileSystemResource(m_workingCopy));
        // rebuild the reload container on the copy, checking on every access,
        // so the daemons' view follows the file the service rewrites
        dao.setReloadCheckInterval(0L);
        dao.afterPropertiesSet();

        // and a scratch opennms.home holding copies of the data collection files
        final AbstractMergingJaxbConfigDao<?, ?> dcDao = (AbstractMergingJaxbConfigDao<?, ?>) m_wsManDataCollectionConfigDao;
        m_originalHome = dcDao.getOpennmsHome();
        m_workingHome = new File("target/test-work-dir", "wsman-home-" + UUID.randomUUID()).toPath();
        final java.nio.file.Path etc = m_workingHome.resolve("etc");
        Files.createDirectories(etc.resolve("wsman-datacollection.d"));
        Files.copy(m_originalHome.resolve("etc/wsman-datacollection-config.xml"), etc.resolve("wsman-datacollection-config.xml"));
        try (java.util.stream.Stream<java.nio.file.Path> s = Files.list(m_originalHome.resolve("etc/wsman-datacollection.d"))) {
            for (final java.nio.file.Path f : (Iterable<java.nio.file.Path>) s::iterator) {
                Files.copy(f, etc.resolve("wsman-datacollection.d").resolve(f.getFileName()));
            }
        }
        dcDao.setOpennmsHome(m_workingHome);
    }

    @After
    public void restoreShippedFile() throws Exception {
        final AbstractJaxbConfigDao<?, ?> dao = (AbstractJaxbConfigDao<?, ?>) m_wsManConfigDao;
        dao.setConfigResource(m_originalResource);
        dao.afterPropertiesSet();
        ((AbstractMergingJaxbConfigDao<?, ?>) m_wsManDataCollectionConfigDao).setOpennmsHome(m_originalHome);
        // never let a test leave the shipped file modified
        final String now = new String(Files.readAllBytes(m_originalResource.getFile().toPath()), StandardCharsets.UTF_8);
        if (!now.equals(m_shippedContent)) {
            Files.write(m_originalResource.getFile().toPath(), m_shippedContent.getBytes(StandardCharsets.UTF_8));
            throw new AssertionError("the shipped wsman-config.xml was modified by the test; restored");
        }
    }

    @Test
    public void testReadsShippedDefaultsWithoutExposingThePassword() throws Exception {
        // the shipped wsman-config.xml sets username/password and ssl/path on the root element
        final String body = getJson("/wsman-config", 200);
        final JSONObject defaults = new JSONObject(body).getJSONObject("defaults");
        assertEquals("root", defaults.getString("username"));
        assertTrue(defaults.getBoolean("hasPassword"));
        assertTrue(defaults.getBoolean("ssl"));
        assertEquals("/wsman", defaults.getString("path"));
        assertEquals(0, new JSONObject(body).getJSONArray("definitions").length());
        assertFalse("the password value must never be returned", body.contains("calvin"));
        assertFalse(body.contains("\"password\""));
    }

    @Test
    public void testDataCollectionListsEveryFileWithItsObjects() throws Exception {
        final JSONObject body = new JSONObject(getJson("/wsman-config/data-collection", 200));
        // the root file first, then the shipped drop-ins in name order
        final JSONArray sources = body.getJSONArray("sources");
        assertEquals("wsman-datacollection-config.xml", sources.getString(0));
        assertTrue(sources.toString().contains("dell-idrac.xml"));
        assertTrue(sources.toString().contains("microsoft-windows.xml"));

        final JSONObject collection = body.getJSONArray("collections").getJSONObject(0);
        assertEquals("default", collection.getString("name"));
        assertEquals("wsman-datacollection-config.xml", collection.getString("source"));
        assertTrue(collection.getBoolean("includeAllSystemDefinitions"));
        assertEquals(300, collection.getInt("rrdStep"));
        assertEquals(5, collection.getJSONArray("rras").length());

        JSONObject powerSupply = null;
        final JSONArray groups = body.getJSONArray("groups");
        for (int i = 0; i < groups.length(); i++) {
            if ("drac-power-supply".equals(groups.getJSONObject(i).getString("name"))) {
                powerSupply = groups.getJSONObject(i);
            }
        }
        assertTrue("expected the drac-power-supply group", powerSupply != null);
        assertEquals("dell-idrac.xml", powerSupply.getString("source"));
        assertEquals("dracPowerSupplyIndex", powerSupply.getString("resourceType"));
        assertEquals(5, powerSupply.getJSONArray("attributes").length());
        assertEquals("gauge", powerSupply.getJSONArray("attributes").getJSONObject(0).getString("type"));

        JSONObject idrac8 = null;
        final JSONArray sysDefs = body.getJSONArray("systemDefinitions");
        for (int i = 0; i < sysDefs.length(); i++) {
            if ("Dell iDRAC 8".equals(sysDefs.getJSONObject(i).getString("name"))) {
                idrac8 = sysDefs.getJSONObject(i);
            }
        }
        assertTrue("expected the Dell iDRAC 8 system definition", idrac8 != null);
        assertEquals(1, idrac8.getJSONArray("rules").length());
        assertEquals("drac-system-board", idrac8.getJSONArray("includedGroups").getString(0));
    }

    @Test
    public void testDataCollectionFilesCanBeRewrittenWithReferencesChecked() throws Exception {
        JSONObject dc = new JSONObject(getJson("/wsman-config/data-collection", 200));
        final int groupsBefore = dc.getJSONArray("groups").length();

        // a new drop-in with a group and a system definition using it
        String body = sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config/data-collection?file=custom.xml",
                "{\"collections\":[],\"groups\":[{\"name\":\"custom-cpu\",\"resourceType\":\"node\",\"resourceUri\":\"http://schemas.microsoft.com/wbem/wsman/1/wmi/root/cimv2/*\","
                + "\"filter\":\"select LoadPercentage from Win32_Processor\",\"attributes\":[{\"name\":\"LoadPercentage\",\"alias\":\"cpuLoad\",\"type\":\"gauge\"}]}],"
                + "\"systemDefinitions\":[{\"name\":\"Custom Windows\",\"rules\":[\"#productVendor matches '^Microsoft.*'\"],\"includedGroups\":[\"custom-cpu\"]}]}", 200).getContentAsString();
        dc = new JSONObject(body);
        assertTrue(dc.getJSONArray("sources").toString().contains("custom.xml"));
        assertEquals(groupsBefore + 1, dc.getJSONArray("groups").length());
        assertTrue(Files.exists(m_workingHome.resolve("etc/wsman-datacollection.d/custom.xml")));
        final String customVersion = dc.getJSONObject("versions").getString("custom.xml");

        // a stale version on an existing file is refused; a version on a new name is refused
        sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config/data-collection?file=custom.xml",
                "{\"version\":\"stale\",\"collections\":[],\"groups\":[],\"systemDefinitions\":[]}", 409);
        sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config/data-collection?file=other.xml",
                "{\"version\":\"x\",\"collections\":[],\"groups\":[],\"systemDefinitions\":[]}", 400);

        // removing the group while its system definition still references it is refused
        final String orphan = sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config/data-collection?file=custom.xml",
                "{\"version\":\"" + customVersion + "\",\"collections\":[],\"groups\":[],"
                + "\"systemDefinitions\":[{\"name\":\"Custom Windows\",\"rules\":[\"true\"],\"includedGroups\":[\"custom-cpu\"]}]}", 400).getContentAsString();
        assertTrue(orphan, orphan.contains("does not exist"));

        // a name already used by another file is refused, as is a bad RRA or type
        sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config/data-collection?file=custom.xml",
                "{\"version\":\"" + customVersion + "\",\"collections\":[],\"groups\":[{\"name\":\"drac-system\",\"resourceType\":\"node\",\"resourceUri\":\"x\",\"attributes\":[{\"name\":\"a\",\"alias\":\"a\",\"type\":\"gauge\"}]}],\"systemDefinitions\":[]}", 400);
        sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config/data-collection?file=custom.xml",
                "{\"version\":\"" + customVersion + "\",\"collections\":[{\"name\":\"c\",\"rrdStep\":300,\"rras\":[\"bogus\"],\"includeAllSystemDefinitions\":true}],\"groups\":[],\"systemDefinitions\":[]}", 400);
        sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config/data-collection?file=custom.xml",
                "{\"version\":\"" + customVersion + "\",\"collections\":[],\"groups\":[{\"name\":\"g\",\"resourceType\":\"node\",\"resourceUri\":\"x\",\"attributes\":[{\"name\":\"a\",\"alias\":\"a\",\"type\":\"float\"}]}],\"systemDefinitions\":[]}", 400);

        // the root file keeps its repository when the request does not set it
        final String rootVersion = dc.getJSONObject("versions").getString("wsman-datacollection-config.xml");
        body = sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config/data-collection?file=wsman-datacollection-config.xml",
                "{\"version\":\"" + rootVersion + "\",\"collections\":[{\"name\":\"default\",\"rrdStep\":600,\"rras\":[\"RRA:AVERAGE:0.5:1:2016\"],\"includeAllSystemDefinitions\":false,\"includedSystemDefinitions\":[\"Custom Windows\"]}],\"groups\":[],\"systemDefinitions\":[]}", 200).getContentAsString();
        dc = new JSONObject(body);
        assertEquals(600, dc.getJSONArray("collections").getJSONObject(0).getInt("rrdStep"));
        assertEquals("Custom Windows", dc.getJSONArray("collections").getJSONObject(0).getJSONArray("includedSystemDefinitions").getString(0));
        assertFalse(dc.isNull("rrdRepository"));
        assertTrue(new String(Files.readAllBytes(m_workingHome.resolve("etc/wsman-datacollection-config.xml")), StandardCharsets.UTF_8).startsWith("<?xml version=\"1.0\""));
        // and the shipped drop-ins are untouched
        assertEquals(groupsBefore + 1, dc.getJSONArray("groups").length());
    }

    @Test
    public void testStatusCountsPolledServersPerDefinition() throws Exception {
        // two WS-Man services on the populator's nodes: 192.168.1.1 (up) and 192.168.2.1 (down);
        // the REST test base opens a read-only session, so seed in a write transaction
        m_databasePopulator.populateDatabase();
        final AtomicReference<Integer> upId = new AtomicReference<>();
        m_sessionUtils.withTransaction(() -> {
            final OnmsServiceType wsman = serviceType("WS-Man");
            upId.set(addService(m_databasePopulator.getNode1(), "192.168.1.1", wsman, new Date(1700000000000L)).getId());
            final OnmsMonitoredService down = addService(m_databasePopulator.getNode2(), "192.168.2.1", wsman, null);
            final OnmsOutage outage = new OnmsOutage();
            outage.setMonitoredService(down);
            outage.setIfLostService(new Date());
            m_databasePopulator.getOutageDao().save(outage);
            m_databasePopulator.getOutageDao().flush();
            return null;
        });

        // one definition covering the 192.168.1.x range; the other server falls through to the defaults
        put("{\"defaults\":{},\"definitions\":[{\"ranges\":[{\"begin\":\"192.168.1.1\",\"end\":\"192.168.1.254\"}],\"username\":\"monitor\"}]}", 200);

        final JSONObject status = new JSONObject(getJson("/wsman-config/status", 200));
        assertEquals("WS-Man", status.getString("serviceName"));
        assertEquals(2, status.getInt("servers"));
        final JSONObject first = status.getJSONArray("definitions").getJSONObject(0);
        assertEquals(0, first.getInt("index"));
        assertEquals(1, first.getInt("servers"));
        assertEquals(1, first.getInt("responding"));
        assertEquals(0, first.getInt("down"));
        assertFalse(first.isNull("lastResponse"));
        final JSONObject defaults = status.getJSONObject("defaults");
        assertEquals(1, defaults.getInt("servers"));
        assertEquals(0, defaults.getInt("responding"));
        assertEquals(1, defaults.getInt("down"));

        // an unmanaged service is not a server the poller checks
        m_sessionUtils.withTransaction(() -> {
            final OnmsMonitoredService up = m_databasePopulator.getMonitoredServiceDao().get(upId.get());
            up.setStatus("F");
            m_databasePopulator.getMonitoredServiceDao().saveOrUpdate(up);
            m_databasePopulator.getMonitoredServiceDao().flush();
            return null;
        });
        assertEquals(0, new JSONObject(getJson("/wsman-config/status", 200)).getJSONArray("definitions").getJSONObject(0).getInt("servers"));
    }

    /**
     * End to end inside one JVM: a definition saved through the API is what the
     * real collector hands to the WS-Man client for a matching server, with no
     * restart in between (the shared DAO re-reads the rewritten file).
     */
    @Test
    public void testDefinitionSavedThroughTheApiReachesTheCollector() throws Exception {
        put("{\"defaults\":{\"username\":\"root\"},\"definitions\":[{\"specifics\":[\"10.20.30.40\"],\"username\":\"monitor\",\"password\":\"secret-one\",\"ssl\":false,\"port\":5985,\"path\":\"/wsman\"}]}", 200);

        final AtomicReference<WSManEndpoint> seen = new AtomicReference<>();
        final WSManClientFactory factory = endpoint -> {
            seen.set(endpoint);
            return mock(WSManClient.class);
        };
        // the system-definition rules read the node's vendor and model
        final OnmsNode node = mock(OnmsNode.class);
        when(node.getAssetRecord()).thenReturn(new OnmsAssetRecord());
        final NodeDao nodeDao = mock(NodeDao.class);
        when(nodeDao.get(any(Integer.class))).thenReturn(node);

        final WsManCollector collector = new WsManCollector();
        collector.setWSManClientFactory(factory);
        collector.setWSManConfigDao(m_wsManConfigDao);
        collector.setWSManDataCollectionConfigDao(m_wsManDataCollectionConfigDao);
        collector.setNodeDao(nodeDao);

        final CollectionAgent agent = mock(CollectionAgent.class);
        when(agent.getAddress()).thenReturn(InetAddressUtils.addr("10.20.30.40"));
        when(agent.getNodeId()).thenReturn(1);
        when(agent.getStorageResourcePath()).thenReturn(ResourcePath.get());

        final Map<String, Object> params = new HashMap<>();
        params.put("collection", "default");
        params.putAll(Interpolator.interpolateAttributes(collector.getRuntimeAttributes(agent, params), EmptyScope.EMPTY));
        final CollectionSet set = collector.collect(agent, params);

        assertEquals(CollectionStatus.SUCCEEDED, set.getStatus());
        assertEquals("monitor", seen.get().getUsername());
        assertEquals("secret-one", seen.get().getPassword());
        assertEquals("http://10.20.30.40:5985/wsman", seen.get().getUrl().toString());

        // a server no definition matches gets the defaults
        when(agent.getAddress()).thenReturn(InetAddressUtils.addr("10.99.99.99"));
        final Map<String, Object> defaultParams = new HashMap<>();
        defaultParams.put("collection", "default");
        defaultParams.putAll(Interpolator.interpolateAttributes(collector.getRuntimeAttributes(agent, defaultParams), EmptyScope.EMPTY));
        collector.collect(agent, defaultParams);
        assertEquals("root", seen.get().getUsername());
    }

    private OnmsServiceType serviceType(final String name) {
        OnmsServiceType type = m_databasePopulator.getServiceTypeDao().findByName(name);
        if (type == null) {
            type = new OnmsServiceType(name);
            m_databasePopulator.getServiceTypeDao().save(type);
            m_databasePopulator.getServiceTypeDao().flush();
        }
        return type;
    }

    private OnmsMonitoredService addService(final OnmsNode node, final String ip, final OnmsServiceType type, final Date lastGood) {
        final OnmsIpInterface iface = m_databasePopulator.getIpInterfaceDao().findByNodeIdAndIpAddress(node.getId(), ip);
        final OnmsMonitoredService svc = new OnmsMonitoredService(iface, type);
        svc.setStatus("A");
        svc.setLastGood(lastGood);
        m_databasePopulator.getMonitoredServiceDao().save(svc);
        m_databasePopulator.getMonitoredServiceDao().flush();
        return svc;
    }

    @Test
    public void testForbiddenForNonAdmin() throws Exception {
        setUser("user", new String[]{ "ROLE_USER" });
        try {
            getJson("/wsman-config", 403);
            getJson("/wsman-config/data-collection", 403);
            getJson("/wsman-config/status", 403);
            sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config/data-collection?file=custom.xml", "{\"collections\":[],\"groups\":[],\"systemDefinitions\":[]}", 403);
            sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config", "{\"defaults\":{},\"definitions\":[]}", 403);
        } finally {
            setUser("admin", new String[]{ "ROLE_ADMIN" });
        }
    }

    @Test
    public void testUpdateKeepsReplacesAndClearsPasswords() throws Exception {
        // editing the defaults without a password keeps the stored one
        String body = put("{\"defaults\":{\"username\":\"operator\",\"ssl\":true,\"path\":\"/wsman\",\"timeout\":15000,\"retry\":2},\"definitions\":[]}", 200);
        JSONObject defaults = new JSONObject(body).getJSONObject("defaults");
        assertEquals("operator", defaults.getString("username"));
        assertEquals(15000, defaults.getInt("timeout"));
        assertTrue(defaults.getBoolean("hasPassword"));
        assertTrue(fileContent().contains("password=\"calvin\""));
        assertTrue("the rewritten file keeps an XML declaration", fileContent().startsWith("<?xml version=\"1.0\""));
        assertFalse(body.contains("calvin"));

        // a new definition with its own password
        body = put("{\"defaults\":{\"username\":\"operator\"},\"definitions\":[" + DEFINITION_10 + "]}", 200);
        final JSONObject def = new JSONObject(body).getJSONArray("definitions").getJSONObject(0);
        assertTrue(def.getBoolean("hasPassword"));
        assertEquals("10.0.0.1", def.getJSONArray("ranges").getJSONObject(0).getString("begin"));
        assertFalse(body.contains("secret-one"));
        assertTrue(fileContent().contains("secret-one"));

        // re-saving it by sourceIndex without a password keeps it; a second new one goes in front of it
        body = put("{\"defaults\":{\"username\":\"operator\"},\"definitions\":["
                + "{\"specifics\":[\"192.168.1.9\"],\"clearPassword\":false},"
                + "{\"sourceIndex\":0,\"ranges\":[{\"begin\":\"10.0.0.1\",\"end\":\"10.0.0.50\"}],\"username\":\"monitor\",\"clearPassword\":false}]}", 200);
        assertEquals(2, new JSONObject(body).getJSONArray("definitions").length());
        assertFalse(new JSONObject(body).getJSONArray("definitions").getJSONObject(0).getBoolean("hasPassword"));
        assertTrue(new JSONObject(body).getJSONArray("definitions").getJSONObject(1).getBoolean("hasPassword"));
        assertTrue(fileContent().contains("secret-one"));

        // clearing removes it from the file
        put("{\"defaults\":{\"username\":\"operator\",\"clearPassword\":true},\"definitions\":["
                + "{\"sourceIndex\":1,\"ranges\":[{\"begin\":\"10.0.0.1\",\"end\":\"10.0.0.50\"}],\"clearPassword\":true}]}", 200);
        body = getJson("/wsman-config", 200);
        assertFalse(new JSONObject(body).getJSONObject("defaults").getBoolean("hasPassword"));
        assertFalse(new JSONObject(body).getJSONArray("definitions").getJSONObject(0).getBoolean("hasPassword"));
        assertFalse(fileContent().contains("secret-one"));
        assertFalse(fileContent().contains("calvin"));
    }

    @Test
    public void testInvalidUpdatesAreRejectedAndLeaveTheFileAlone() throws Exception {
        final String before = fileContent();
        final String[] bad = {
            // end before begin, mixed families, no criteria
            "{\"defaults\":{},\"definitions\":[{\"ranges\":[{\"begin\":\"10.0.0.50\",\"end\":\"10.0.0.1\"}]}]}",
            "{\"defaults\":{},\"definitions\":[{\"ranges\":[{\"begin\":\"10.0.0.1\",\"end\":\"fe80::1\"}]}]}",
            "{\"defaults\":{},\"definitions\":[{\"username\":\"x\"}]}",
            // a host name is not an address, even a resolvable one
            "{\"defaults\":{},\"definitions\":[{\"specifics\":[\"localhost\"]}]}",
            "{\"defaults\":{},\"definitions\":[{\"specifics\":[\"not-an-ip\"]}]}",
            // IPLIKE: too few fields, IPv6 (not in the schema), reversed range, double dash, octet above 255
            "{\"defaults\":{},\"definitions\":[{\"ipMatches\":[\"10.0.*\"]}]}",
            "{\"defaults\":{},\"definitions\":[{\"ipMatches\":[\"fe80:*:*:*:*:*:*:*\"]}]}",
            "{\"defaults\":{},\"definitions\":[{\"ipMatches\":[\"10.0.0.50-1\"]}]}",
            "{\"defaults\":{},\"definitions\":[{\"ipMatches\":[\"10.0.1-2-3.*\"]}]}",
            "{\"defaults\":{},\"definitions\":[{\"ipMatches\":[\"10.0.0.999\"]}]}",
            "{\"defaults\":{},\"definitions\":[{\"ipMatches\":[\"1000.*.*.*\"]}]}",
            // out-of-range settings and whitespace in the path
            "{\"defaults\":{\"port\":70000},\"definitions\":[]}",
            "{\"defaults\":{\"timeout\":-1},\"definitions\":[]}",
            "{\"defaults\":{\"path\":\"wsman path\"},\"definitions\":[]}",
            // a new password and a clear request at once
            "{\"defaults\":{\"password\":\"x\",\"clearPassword\":true},\"definitions\":[]}",
            // a stale source index
            "{\"defaults\":{},\"definitions\":[{\"sourceIndex\":3,\"specifics\":[\"10.0.0.1\"]}]}"
        };
        for (final String request : bad) {
            final String response = put(request, 400);
            assertFalse("expected a plain-text reason for: " + request, response.isBlank());
        }
        // an omitted definitions list would silently drop every definition
        assertFalse(put("{\"defaults\":{\"username\":\"operator\"}}", 400).isBlank());
        assertEquals("a rejected update must not touch the file", before, fileContent());

        put("{\"defaults\":{},\"definitions\":[" + DEFINITION_10 + "]}", 200);
        // the same stored definition claimed twice
        put("{\"defaults\":{},\"definitions\":[{\"sourceIndex\":0,\"specifics\":[\"10.0.0.1\"]},{\"sourceIndex\":0,\"specifics\":[\"10.0.0.2\"]}]}", 400);
    }

    @Test
    public void testAcceptsWhatTheDaemonAccepts() throws Exception {
        // the endpoint builder prepends the slash a path lacks, a zero timeout is
        // legal, and an existing value must never block an unrelated save
        final String body = put("{\"defaults\":{\"path\":\"wsman\",\"timeout\":0,\"username\":\" spaced \"},\"definitions\":["
                + "{\"ipMatches\":[\"10.0.1-5,9.*\"],\"specifics\":[\"fe80::1\"]}]}", 200);
        final JSONObject defaults = new JSONObject(body).getJSONObject("defaults");
        assertEquals("wsman", defaults.getString("path"));
        assertEquals(0, defaults.getInt("timeout"));
        assertEquals(" spaced ", defaults.getString("username"));
        assertEquals("fe80::1", new JSONObject(body).getJSONArray("definitions").getJSONObject(0).getJSONArray("specifics").getString(0));
    }

    @Test
    public void testStaleVersionIsRefused() throws Exception {
        final String stale = currentVersion();
        put("{\"defaults\":{\"username\":\"first\"},\"definitions\":[]}", 200);
        // a page loaded before that save must not overwrite it
        final String response = sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config",
                "{\"version\":\"" + stale + "\",\"defaults\":{\"username\":\"second\"},\"definitions\":[]}", 409).getContentAsString();
        assertTrue(response.contains("changed since"));
        assertTrue(fileContent().contains("username=\"first\""));
        // and a request with no version at all is a 400, not a blind write
        sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config", "{\"defaults\":{\"username\":\"third\"},\"definitions\":[]}", 400);
        assertTrue(fileContent().contains("username=\"first\""));
    }

    @Test
    public void testRewriteKeepsFileMode() throws Exception {
        // the file holds credentials; an operator's 0600 must survive a save
        final Set<PosixFilePermission> restricted = PosixFilePermissions.fromString("rw-------");
        Files.setPosixFilePermissions(m_workingCopy.toPath(), restricted);
        put("{\"defaults\":{\"username\":\"operator\"},\"definitions\":[]}", 200);
        assertEquals(restricted, Files.getPosixFilePermissions(m_workingCopy.toPath()));
        assertTrue(fileContent().startsWith("<?xml version=\"1.0\""));
    }

    private String currentVersion() throws Exception {
        return new JSONObject(getJson("/wsman-config", 200)).getString("version");
    }

    // every body is sent with the version the file has right now
    private String put(final String bodyWithoutVersion, final int expectedStatus) throws Exception {
        final String body = "{\"version\":\"" + currentVersion() + "\"," + bodyWithoutVersion.substring(1);
        return sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config", body, expectedStatus).getContentAsString();
    }

    private String fileContent() throws Exception {
        return new String(Files.readAllBytes(m_workingCopy.toPath()), StandardCharsets.UTF_8);
    }

    private String getJson(final String url, final int expectedStatus) throws Exception {
        final MockHttpServletRequest request = createRequest(GET, url);
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        return sendRequest(request, expectedStatus);
    }
}
