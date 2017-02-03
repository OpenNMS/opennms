/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.rpc.mock.MockRpcClientFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collectd.AliasedResource;
import org.opennms.netmgt.collectd.GenericIndexResource;
import org.opennms.netmgt.collectd.GenericIndexResourceType;
import org.opennms.netmgt.collectd.IfInfo;
import org.opennms.netmgt.collectd.IfResourceType;
import org.opennms.netmgt.collectd.NodeInfo;
import org.opennms.netmgt.collectd.NodeResourceType;
import org.opennms.netmgt.collectd.NumericAttributeType;
import org.opennms.netmgt.collectd.OnmsSnmpCollection;
import org.opennms.netmgt.collectd.ResourceType;
import org.opennms.netmgt.collectd.SnmpAttributeType;
import org.opennms.netmgt.collectd.SnmpCollectionAgent;
import org.opennms.netmgt.collectd.SnmpCollectionResource;
import org.opennms.netmgt.collectd.SnmpIfData;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigManager;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.JdbcFilterDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.mock.MockDataCollectionConfig;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.snmp.proxy.common.LocationAwareSnmpClientRpcImpl;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.test.FileAnticipator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

/**
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 *
 */
public class ThresholdingVisitorIT {
    private static final Logger LOG = LoggerFactory.getLogger(ThresholdingVisitorIT.class);

    FilterDao m_filterDao;
    EventAnticipator m_anticipator;
    FileAnticipator m_fileAnticipator;
    Map<Integer, File> m_hrStorageProperties;
    List<Event> m_anticipatedEvents;
    private FilesystemResourceStorageDao m_resourceStorageDao;
    private LocationAwareSnmpClient m_locationAwareSnmpClient = new LocationAwareSnmpClientRpcImpl(new MockRpcClientFactory());

    private static final Comparator<Parm> PARM_COMPARATOR = new Comparator<Parm>() {
        @Override
        public int compare(Parm o1, Parm o2) {
            if (o1 == null && o2 == null) return 0;
            if (o1 == null && o2 != null) return 1;
            if (o1 != null && o2 == null) return -1;

            int retVal = o1.getParmName().compareTo(o2.getParmName());
            if (retVal == 0) {
                String c1 = o1.getValue().getContent();
                String c2 = o2.getValue().getContent();
                if (c1 == null && c2 == null) return 0;
                if (c1 == null && c2 != null) return 1;
                if (c1 != null && c2 == null) return -1;

                retVal = c1.compareTo(c2);
            }
            return retVal;
        }
    };

    private static final Comparator<Event> EVENT_COMPARATOR = new Comparator<Event>() {

        private int compareStrings(String s1, String s2) {
            if (s1 == null && s2 == null) return 0;
            if (s1 == null && s2 != null) return 1;
            if (s1 != null && s2 == null) return -1;
            return (s1.compareTo(s2));
        }

        @Override
        public int compare(Event e1, Event e2) {
            if (e1 == null && e2 == null) return 0;
            if (e1 == null && e2 != null) return 1;
            if (e1 != null && e2 == null) return -1;

            int retVal = compareStrings(e1.getUei(), e2.getUei());
            if (retVal == 0) {
                retVal = InetAddressUtils.toInteger(e1.getInterfaceAddress()).compareTo(InetAddressUtils.toInteger(e2.getInterfaceAddress()));
            }
            if (retVal == 0) {
                retVal = compareStrings(e1.getService(), e2.getService());
            }
            if (retVal == 0) {
                List<Parm> anticipatedParms = e1.getParmCollection();
                List<Parm> receivedParms = e2.getParmCollection();
                Collections.sort(anticipatedParms, PARM_COMPARATOR);
                Collections.sort(receivedParms, PARM_COMPARATOR);
                if (anticipatedParms.size() != receivedParms.size()) {
                    retVal = Integer.valueOf(anticipatedParms.size()).compareTo(Integer.valueOf(receivedParms.size()));
                }
                if (retVal == 0) {
                    for (int i = 0; i < anticipatedParms.size(); i++) {
                        Parm anticipated = anticipatedParms.get(i);
                        Parm received = receivedParms.get(i);

                        retVal = compareStrings(anticipated.getParmName(), received.getParmName());
                        if (retVal == 0) {
                            retVal = compareStrings(anticipated.getValue().getContent(), received.getValue().getContent());
                        }
                        if (retVal != 0) {
                            break;
                        }
                    }
                }
            }

            return retVal;
        }
    };

    @Before
    public void setUp() throws Exception {
        // Resets Counters Cache Data
        CollectionResourceWrapper.s_cache.clear();

        MockLogAppender.setupLogging();

        m_fileAnticipator = new FileAnticipator();
        m_hrStorageProperties = new HashMap<Integer, File>();

        m_resourceStorageDao = new FilesystemResourceStorageDao();
        m_resourceStorageDao.setRrdDirectory(new File(m_fileAnticipator.getTempDir(), "snmp"));

        m_filterDao = EasyMock.createMock(FilterDao.class);
        EasyMock.expect(m_filterDao.getActiveIPAddressList((String)EasyMock.anyObject())).andReturn(Collections.singletonList(addr("127.0.0.1"))).anyTimes();
        m_filterDao.flushActiveIpAddressListCache();
        EasyMock.expectLastCall().anyTimes();
        FilterDaoFactory.setInstance(m_filterDao);
        EasyMock.replay(m_filterDao);

        m_anticipator = new EventAnticipator();
        MockEventIpcManager eventMgr = new MockEventIpcManager();
        eventMgr.setEventAnticipator(m_anticipator);
        eventMgr.setSynchronous(true);
        EventIpcManager eventdIpcMgr = (EventIpcManager)eventMgr;
        EventIpcManagerFactory.setIpcManager(eventdIpcMgr);
        
        DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        StringBuffer sb = new StringBuffer("<?xml version=\"1.0\"?>");
        sb.append("<outages>");
        sb.append("<outage name=\"junit outage\" type=\"specific\">");
        sb.append("<time begins=\"");
        sb.append(formatter.format(new Date(System.currentTimeMillis() - 3600000)));
        sb.append("\" ends=\"");
        sb.append(formatter.format(new Date(System.currentTimeMillis() + 3600000)));
        sb.append("\"/>");
        sb.append("<interface address=\"match-any\"/>");
        sb.append("</outage>");
        sb.append("</outages>");
        File file = new File("target/poll-outages.xml");
        FileWriter writer = new FileWriter(file);
        writer.write(sb.toString());
        writer.close();
        PollOutagesConfigFactory.setInstance(new PollOutagesConfigFactory(new FileSystemResource(file)));
        PollOutagesConfigFactory.getInstance().afterPropertiesSet();
        initFactories("/threshd-configuration.xml","/test-thresholds.xml");
        m_anticipatedEvents = new ArrayList<Event>();
    };
    
    private void initFactories(String threshd, String thresholds) throws Exception {
        LOG.info("Initialize Threshold Factories");
        ThresholdingConfigFactory.setInstance(new ThresholdingConfigFactory(getClass().getResourceAsStream(thresholds)));
        ThreshdConfigFactory.setInstance(new ThreshdConfigFactory(getClass().getResourceAsStream(threshd),"127.0.0.1", false));
    }

    @After
    public void checkWarnings() throws Throwable {
//        MockLogAppender.assertNoWarningsOrGreater();
        m_fileAnticipator.deleteExpected();
    }
    
    @After
    public void tearDown() throws Exception {
        EasyMock.verify(m_filterDao);
        m_fileAnticipator.deleteExpected(true);
        m_fileAnticipator.tearDown();
    }

    @Test
    public void testCreateVisitor() {
        createVisitor();
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds.xml
     */
    @Test
    public void testResourceGaugeData() {
        addHighThresholdEvent(1, 10000, 5000, 15000, "node", "node", "freeMem", null, null);
        ThresholdingVisitor visitor = createVisitor();
        runGaugeDataTest(visitor, 15000);
        verifyEvents(0);
    }

    @Test
    public void testTriggersNodeResource() throws Exception {
        initFactories("/threshd-configuration.xml", "/test-thresholds-triggers.xml");
        addHighThresholdEvent(3, 10000, 5000, 22000, "node", "node", "freeMem", null, null);
        ThresholdingVisitor visitor = createVisitor();
        
        // Trigger = 1
        runGaugeDataTest(visitor, 15000);
        
        // Trigger = 2
        runGaugeDataTest(visitor, 18000);

        // Drop bellow trigger value
        runGaugeDataTest(visitor, 8000);

        // Should not trigger
        runGaugeDataTest(visitor, 20000);

        // Trigger = 2
        runGaugeDataTest(visitor, 21000);

        // Trigger = 3
        runGaugeDataTest(visitor, 22000);

        verifyEvents(0);
    }

    @Test
    public void testTriggersGenericResource() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-triggers.xml");
        addEvent(EventConstants.LOW_THRESHOLD_EVENT_UEI, "127.0.0.1", "SNMP", 3, 10.0, 15.0, 7.0, "/opt", "1", "hrStorageSize-hrStorageUsed", null, null, m_anticipator, m_anticipatedEvents);
        ThresholdingVisitor visitor = createVisitor();

        // Trigger = 1
        runFileSystemDataTest(visitor, 1, "/opt", 95, 100);

        // Trigger = 2
        runFileSystemDataTest(visitor, 1, "/opt", 96, 100);

        // Raise above trigger value
        runFileSystemDataTest(visitor, 1, "/opt", 80, 100);

        // Trigger = 1
        runFileSystemDataTest(visitor, 1, "/opt", 91, 100);

        // Trigger = 2
        runFileSystemDataTest(visitor, 1, "/opt", 92, 100);

        // Trigger = 3
        runFileSystemDataTest(visitor, 1, "/opt", 93, 100);

        verifyEvents(0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds.xml
     * 
     * Updated to reflect the fact that counter are treated as rates (counter wrap is not checked here anymore).
     */
    @Test
    public void testResourceCounterData() throws Exception {
        initFactories("/threshd-configuration.xml", "/test-thresholds-counters.xml");
        ThresholdingVisitor visitor = createVisitor();

        SnmpCollectionAgent agent = createCollectionAgent();
        NodeResourceType resourceType = createNodeResourceType(agent);
        MibObject mibObject = createMibObject("counter", "myCounter", "0");
        SnmpAttributeType attributeType = new NumericAttributeType(resourceType, "default", mibObject, new AttributeGroupType("mibGroup", AttributeGroupType.IF_TYPE_IGNORE));

        // Add Events
        addHighThresholdEvent(1, 10, 5, 15, "node", "node", "myCounter", null, null);
        addHighRearmEvent(1, 10, 5, 2, "node", "node", "myCounter", null, null);

        long baseDate = new Date().getTime();
        // Step 0: Visit a CollectionSet with a timestamp, so that the thresholder knows how when the collection was held 
        // Normally visiting the CollectionSet would end up visiting the resources, but we're fudging that for the test
        visitor.visitCollectionSet(createAnonymousCollectionSet(baseDate));

        // Collect Step 1 : Initialize counter cache.
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(1000));
        resource.visit(visitor);

        // Collect Step 2 : Trigger. (last-current)/step => (5500-1000)/300=15
        visitor.visitCollectionSet(createAnonymousCollectionSet(baseDate+300000));
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(5500));
        resource.visit(visitor);

        // Collect Step 3 : Rearm. (last-current)/step => (6100-5500)/300=2
        visitor.visitCollectionSet(createAnonymousCollectionSet(baseDate+600000));
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(6100));
        resource.visit(visitor);

        EasyMock.verify(agent);
        verifyEvents(0);
    }

    @Test
    public void testZeroIntervalResourceCounterData() throws Exception {
        initFactories("/threshd-configuration.xml", "/test-thresholds-counters.xml");
        ThresholdingVisitor visitor = createVisitor();

        SnmpCollectionAgent agent = createCollectionAgent();
        NodeResourceType resourceType = createNodeResourceType(agent);
        MibObject mibObject = createMibObject("counter", "myCounter", "0");
        SnmpAttributeType attributeType = new NumericAttributeType(resourceType, "default", mibObject, new AttributeGroupType("mibGroup", AttributeGroupType.IF_TYPE_IGNORE));

        // Add Events
        addHighThresholdEvent(1, 10, 5, 15, "node", "node", "myCounter", null, null);
        addHighRearmEvent(1, 10, 5, 2, "node", "node", "myCounter", null, null);

        long baseDate = new Date().getTime();
        // Step 0: Visit a CollectionSet with a timestamp, so that the thresholder knows how when the collection was held 
        // Normally visiting the CollectionSet would end up visiting the resources, but we're fudging that for the test
        visitor.visitCollectionSet(createAnonymousCollectionSet(baseDate));

        // Collect Step 1 : Initialize counter cache.
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(1000));
        resource.visit(visitor);

        // Collect Step 1.5 : Send a new data point with the same timestamp as before and a
        // different value. This should not trigger a threshold violation because the time
        // interval is zero. It should also not reset the cached value for the attribute
        // because the time interval is zero.
        visitor.visitCollectionSet(createAnonymousCollectionSet(baseDate));
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(5500));
        resource.visit(visitor);

        // Collect Step 2 : Trigger. (last-current)/step => (5500-1000)/300=15
        visitor.visitCollectionSet(createAnonymousCollectionSet(baseDate+300000));
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(5500));
        resource.visit(visitor);

        // Collect Step 3 : Rearm. (last-current)/step => (6100-5500)/300=2
        visitor.visitCollectionSet(createAnonymousCollectionSet(baseDate+600000));
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(6100));
        resource.visit(visitor);

        EasyMock.verify(agent);
        verifyEvents(0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds.xml
     * 
     * Updated to reflect the fact that counter are treated as rates.
     */
    @Test
    public void testInterfaceResourceWithDBAttributeFilter() throws Exception {
        Integer ifIndex = 1;
        Long ifSpeed = 10000000l;
        String ifName = "wlan0";
        addHighThresholdEvent(1, 90, 50, 120, ifName, ifIndex.toString(), "ifOutOctets", ifName, ifIndex.toString());
        addHighThresholdEvent(1, 90, 50, 120, ifName, ifIndex.toString(), "ifInOctets", ifName, ifIndex.toString());
        
        ThresholdingVisitor visitor = createVisitor();
        visitor.visitCollectionSet(createAnonymousCollectionSet(new Date().getTime()));
        runInterfaceResource(visitor, "127.0.0.1", ifName, ifSpeed, ifIndex, 10000, 46000); // real value = (46000 - 10000)/300 = 120
        verifyEvents(0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds.xml
     * 
     * Updated to reflect the fact that counter are treated as rates.
     */
    @Test
    public void testInterfaceResourceWithStringAttributeFilter() throws Exception {
        Integer ifIndex = 1;
        Long ifSpeed = 10000000l;
        String ifName = "sis0";
        addHighThresholdEvent(1, 90, 50, 120, ifName, ifIndex.toString(), "ifOutOctets", ifName, ifIndex.toString());
        addHighThresholdEvent(1, 90, 50, 120, ifName, ifIndex.toString(), "ifInOctets", ifName, ifIndex.toString());


        // Creating strings.properties file
        ResourcePath path = ResourcePath.get("snmp", "1", ifName);
        m_resourceStorageDao.setStringAttribute(path, "myMockParam", "myMockValue");

        ThresholdingVisitor visitor = createVisitor();
        visitor.visitCollectionSet(createAnonymousCollectionSet(new Date().getTime()));

        runInterfaceResource(visitor, "127.0.0.1", ifName, ifSpeed, ifIndex, 10000, 46000); // real value = (46000 - 10000)/300 = 120
        verifyEvents(0);
    }

    /*
     * Before call visitor.reload(), this test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds.xml
     * 
     * After call visitor.reload(), this test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-2.xml
     */
    @Test
    public void testReloadThresholdsConfig() throws Exception {
        ThresholdingVisitor visitor = createVisitor();
        
        // Step 1: No events
        addHighThresholdEvent(1, 10000, 5000, 4500, "node", "node", "freeMem", null, null);
        runGaugeDataTest(visitor, 4500);
        verifyEvents(1);
        
        // Step 2: Change configuration
        initFactories("/threshd-configuration.xml","/test-thresholds-2.xml");
        visitor.reload();
        resetAnticipator();
        
        // Step 3: Trigger threshold with new configuration values
        addHighThresholdEvent(1, 4000, 2000, 4500, "node", "node", "freeMem", null, null);
        runGaugeDataTest(visitor, 4500);
        verifyEvents(0);
    }

    /*
     * Use case A:
     * 
     * I have 5 nodes. The current threshd-config matches 2 of them. The new threshd-config will match the other 2, by
     * adding a new threshold package. For example: n1 y n2 belongs to category CAT1, n2, n3 y n4 belongs to category CAT2.
     * The initial configuration is related with CAT1 and the new package is related with CAT2. In both cases, n5 should
     * never match any threshold package.
     * 
     * Use case B:
     * 
     * I have a package with SNMP thresholds. Then update the package by adding HTTP thresholds. The test node should
     * support both services.
     * 
     * IMPORTANT:
     *     The reload should be do it first, then notify all visitors (I think this is the current behavior)
     *     The reload should not be executed inside the visitor because every collector thread has their own visitor.
     */
    @Test
    public void testReloadThreshdConfig() throws Exception {
        String baseIpAddress = "10.0.0.";

        // Initialize Mock Network
        MockNetwork network = new MockNetwork();
        network.setCriticalService("ICMP");
        for (int i=1; i<=5; i++) {
            String ipAddress = baseIpAddress + i;
            network.addNode(i, "testNode-" + ipAddress);
            network.addInterface(ipAddress);
            network.setIfAlias("eth0");
            network.addService("ICMP");
            network.addService("SNMP");
            if (i == 5) {
                network.addService("HTTP"); // Adding HTTP on node 5
            }
        }
        network.addPathOutage(1, InetAddressUtils.addr("192.168.1.1"), "ICMP");

        MockDatabase db = new MockDatabase();
        db.populate(network);
        db.update("insert into categories (categoryid, categoryname) values (?, ?)", 10, "CAT1");
        db.update("insert into categories (categoryid, categoryname) values (?, ?)", 11, "CAT2");
        for (int i=1; i<=5; i++) {
            db.update("update snmpinterface set snmpifname=?, snmpifdescr=? where id=?", "eth0", "eth0", i);
            db.update("update node set nodesysoid=? where nodeid=?", ".1.3.6.1.4.1.9.1.222", i);
        }
        for (int i=1; i<=2; i++) {
            db.update("insert into category_node values (?, ?)", 10, i);
        }
        for (int i=3; i<=5; i++) {
            db.update("insert into category_node values (?, ?)", 11, i);
        }
        DataSourceFactory.setInstance(db);

        // Initialize Filter DAO
        System.setProperty("opennms.home", "src/test/resources");
        DatabaseSchemaConfigFactory.init();
        JdbcFilterDao jdbcFilterDao = new JdbcFilterDao();
        jdbcFilterDao.setDataSource(db);
        jdbcFilterDao.setDatabaseSchemaConfigFactory(DatabaseSchemaConfigFactory.getInstance());
        jdbcFilterDao.afterPropertiesSet();
        FilterDaoFactory.setInstance(jdbcFilterDao);

        // Initialize Factories
        initFactories("/threshd-configuration-reload-use-case-a.xml","/test-thresholds-reload-use-cases.xml");

        // Initialize Thresholding Visitors
        System.err.println("-----------------------------------------------------------------------------------");
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("thresholding-enabled", "true");
        ServiceParameters svcParams = new ServiceParameters(params);
        List<ThresholdingVisitor> visitors = new ArrayList<ThresholdingVisitor>();
        for (int i=1; i<=5; i++) {
            String ipAddress = baseIpAddress + i;
            ThresholdingVisitor visitor = ThresholdingVisitor.create(i, ipAddress, "SNMP", getRepository(), svcParams, m_resourceStorageDao);
            assertNotNull(visitor);
            visitors.add(visitor);
            if (i == 5) {
                ThresholdingVisitor httpVisitor = ThresholdingVisitor.create(i, ipAddress, "HTTP", getRepository(), svcParams, m_resourceStorageDao);
                assertNotNull(httpVisitor);
                visitors.add(httpVisitor);
            }
        }
        System.err.println("-----------------------------------------------------------------------------------");

        // Check Visitors
        for (int i=0; i<2; i++) { // Nodes n1 and n2 has thresholds defined on one threshold group.
            assertTrue(visitors.get(i).hasThresholds());
            assertEquals(1, visitors.get(i).getThresholdGroups().size());
        }
        for (int i=2; i<6; i++) { // Nodes n3, n4 and n5 should not have thresholds defined.
            assertFalse(visitors.get(i).hasThresholds());
            assertEquals(0, visitors.get(i).getThresholdGroups().size());
        }

        // Re-Initialize Factories
        initFactories("/threshd-configuration-reload-use-case-b.xml","/test-thresholds-reload-use-cases.xml");

        // Reload state on each visitor
        System.err.println("-----------------------------------------------------------------------------------");
        for (ThresholdingVisitor visitor : visitors) {
            visitor.reload();
        }
        System.err.println("-----------------------------------------------------------------------------------");

        // Check Visitors
        for (int i=0; i<6; i++) {
            assertTrue(visitors.get(i).hasThresholds());
            assertEquals(1, visitors.get(i).getThresholdGroups().size());
            if (i == 5) {
                assertEquals("web-services", visitors.get(i).getThresholdGroups().get(0).getName());
            }
        }
    }

    /*
     * This bug has not been replicated, but this code covers the apparent scenario, and can be adapted to match
     * any scenario which can actually replicate the reported issue
     * 
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-bug2746.xml
     */
    @Test
    public void testBug2746() throws Exception{
        initFactories("/threshd-configuration.xml","/test-thresholds-bug2746.xml");

        ThresholdingVisitor visitor = createVisitor();

        SnmpCollectionAgent agent = createCollectionAgent();
        NodeResourceType resourceType = createNodeResourceType(agent);
        MibObject mibObject = createMibObject("gauge", "bug2746", "0");
        SnmpAttributeType attributeType = new NumericAttributeType(resourceType, "default", mibObject, new AttributeGroupType("mibGroup", AttributeGroupType.IF_TYPE_IGNORE));

        // Add Events
        addHighThresholdEvent(1, 50, 40, 60, "node", "node", "bug2746", null, null);

        // Step 1 : Execute visitor
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getGauge32(20));
        resource.visit(visitor);
        
        // Step 2 : Repeat a couple of times with the same value, to replicate a steady state
        resource.visit(visitor);
        resource.visit(visitor);
        resource.visit(visitor);

        // Step 3 : Trigger
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getGauge32(60));
        resource.visit(visitor);

        // Step 4 : Don't rearm, but do drop
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getGauge32(45));
        resource.visit(visitor);

        // Step 5 : Shouldn't trigger again
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getGauge32(55));
        resource.visit(visitor);

        EasyMock.verify(agent);
        verifyEvents(0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds.xml
     */
    @Test
    public void testBug3146_unrelatedChange() throws Exception {
        ThresholdingVisitor visitor = createVisitor();
        
        // Add Events
        addHighThresholdEvent(1, 10000, 5000, 12000, "node", "node", "freeMem", null, null);
        addHighRearmEvent(1, 10000, 5000, 1000, "node", "node", "freeMem", null, null);
        
        // Step 1: Trigger threshold
        runGaugeDataTest(visitor, 12000);
        
        // Step 2: Reload Configuration (changes are not related to triggered threshold)
        visitor.reload();
        
        // Step 3: Send Rearmed event
        runGaugeDataTest(visitor, 1000);
        
        // Verify Events
        verifyEvents(0);
    }
    
    /*
     * Before call visitor.reload(), this test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds.xml
     * 
     * After call visitor.reload(), this test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-2.xml
     */
    @Test
    public void testBug3146_reduceTrigger() throws Exception {
        ThresholdingVisitor visitor = createVisitor();

        // Add Events
        addHighThresholdEvent(1, 10000, 5000, 12000, "node", "node", "freeMem", null, null);
        addHighRearmEvent(1, 10000, 5000, Double.NaN, "node", "node", "freeMem", null, null);
        addHighThresholdEvent(1, 4000, 2000, 5000, "node", "node", "freeMem", null, null);
        addHighRearmEvent(1, 4000, 2000, 1000, "node", "node", "freeMem", null, null);

        // Step 1: Trigger threshold
        runGaugeDataTest(visitor, 12000);
        
        // Step 2: Change Configuration (reducing value for already triggered threshold)
        initFactories("/threshd-configuration.xml","/test-thresholds-2.xml");

        // Step 3: Execute Merge Configuration
        visitor.reload();

        // Step 4: Trigger threshold (with new value)
        runGaugeDataTest(visitor, 5000);
        
        // Step 5: Send Rearmed event (with new value)
        runGaugeDataTest(visitor, 1000);
        
        // Verify Events
        verifyEvents(0);
    }

    /*
     * Before call visitor.reload(), this test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds.xml
     * 
     * After call visitor.reload(), this test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-3.xml
     */
    @Test
    public void testBug3146_inceaseTrigger() throws Exception {
        ThresholdingVisitor visitor = createVisitor();

        // Add Events
        addHighThresholdEvent(1, 10000, 5000, 12000, "node", "node", "freeMem", null, null);
        addHighRearmEvent(1, 10000, 5000, Double.NaN, "node", "node", "freeMem", null, null);

        // Step 1: Trigger threshold
        runGaugeDataTest(visitor, 12000);
        
        // Step 2: Change Configuration (increasing value for already triggered threshold)
        initFactories("/threshd-configuration.xml","/test-thresholds-3.xml");
        
        // Step 3: Execute Merge Configuration (Rearmed Event must be sent).
        visitor.reload();
        verifyEvents(0);
        
        // Step 4: New collected data is not above the new threshold value. No Events generated
        resetAnticipator();
        addHighThresholdEvent(1, 15000, 14000, 13000, "node", "node", "freeMem", null, null);
        runGaugeDataTest(visitor, 13000);
        verifyEvents(1);
        
        // Step 5: Trigger and rearm a threshold using new configuration
        resetAnticipator();
        addHighThresholdEvent(1, 15000, 14000, 16000, "node", "node", "freeMem", null, null);
        addHighRearmEvent(1, 15000, 14000, 1000, "node", "node", "freeMem", null, null);
        runGaugeDataTest(visitor, 16000);
        runGaugeDataTest(visitor, 1000);
        verifyEvents(0);
    }

    /*
     * If I have a high threshold triggered, and then replace it with their equivalent low threshold,
     * The high definition must be removed from cache and rearmed event must be sent.
     * 
     * Before call visitor.reload(), this test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds.xml
     * 
     * After call visitor.reload(), this test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-4.xml
     */
    @Test
    public void testBug3146_replaceThreshold() throws Exception {
        ThresholdingVisitor visitor = createVisitor();
        
        // Add Events
        String lowThresholdUei = EventConstants.LOW_THRESHOLD_EVENT_UEI;
        String highExpression = "(((hrStorageAllocUnits*hrStorageUsed)/(hrStorageAllocUnits*hrStorageSize))*100)";
        String lowExpression = "(100-((hrStorageAllocUnits*hrStorageUsed)/(hrStorageAllocUnits*hrStorageSize))*100)";
        addHighThresholdEvent(1, 30, 25, 50, "/opt", "1", highExpression, null, null);
        addHighRearmEvent(1, 30, 25, Double.NaN, "/opt", "1", highExpression, null, null);
        addEvent(lowThresholdUei, "127.0.0.1", "SNMP", 1, 10.0, 20.0, 5.0, "/opt", "1", lowExpression, null, null, m_anticipator, m_anticipatedEvents);

        // Step 1: Trigger threshold
        runFileSystemDataTest(visitor, 1, "/opt", 500, 1000);

        // Step 2: Reload Configuration (merge). Threshold definition was replaced.
        initFactories("/threshd-configuration.xml","/test-thresholds-4.xml");
        visitor.reload();
        
        // Step 3: Must trigger only one low threshold exceeded
        runFileSystemDataTest(visitor, 1, "/opt", 950, 1000);
        
        verifyEvents(0);
    }
    
    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-bug3193.xml
     * 
     * Updated to reflect the fact that counter are treated as rates.
     */
    @Test
    public void testBug3193() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-bug3193.xml");
        ThresholdingVisitor visitor = createVisitor();

        SnmpCollectionAgent agent = createCollectionAgent();
        NodeResourceType resourceType = createNodeResourceType(agent);
        MibObject mibObject = createMibObject("counter", "myCounter", "0");
        SnmpAttributeType attributeType = new NumericAttributeType(resourceType, "default", mibObject, new AttributeGroupType("mibGroup", AttributeGroupType.IF_TYPE_IGNORE));

        // Add Events
        addHighThresholdEvent(1, 100, 90, 110, "node", "node", "myCounter", null, null);
        addHighThresholdEvent(1, 70, 60, 80, "node", "node", "myCounter - 30", null, null);
        addHighRearmEvent(1, 100, 90, 40, "node", "node", "myCounter", null, null);
        addHighRearmEvent(1, 70, 60, 10, "node", "node", "myCounter - 30", null, null);
        
        long baseDate = new Date().getTime();
        // Collect Step 1 : First Data: Last should be NaN
        visitor.visitCollectionSet(createAnonymousCollectionSet(baseDate));
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(2000));
        resource.visit(visitor);

        // Collect Step 2 : First Value: (last-current)/step => (20000-2000)/300=60
        visitor.visitCollectionSet(createAnonymousCollectionSet(baseDate+300000));
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(20000));
        resource.visit(visitor);

        // Collect Step 3 : Second Value: (last-current)/step => (53000-20000)/300=110 => Trigger
        visitor.visitCollectionSet(createAnonymousCollectionSet(baseDate+600000));
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(53000));
        resource.visit(visitor);

        // Collect Step 3 : Third Value (last-current)/step => (65000-53000)/300=40 => Rearm
        visitor.visitCollectionSet(createAnonymousCollectionSet(baseDate+900000));
		resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(65000));
        resource.visit(visitor);

        EasyMock.verify(agent);
        verifyEvents(0);
    }
    
    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-2.xml
     * 
     * Updated to reflect the fact that counter are treated as rates.
     */
    @Test
    public void testBug2711_noIpAddress() throws Exception {
        runTestForBug2711(2, 0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-2.xml
     * 
     * Updated to reflect the fact that counter are treated as rates.
     */
    @Test
    public void testBug2711_noIP_badIfIndex() throws Exception {
        runTestForBug2711(-100, 2);
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-bug3227.xml
     * 
     * There is no Frame Relay related thresholds definitions on test-thresholds-bug3227.xml.
     * When visit resources, getEntityMap from ThresholdingSet must be null.
     * Updated to reflect the fact that counter are treated as rates.
     */
    @Test
    public void testBug3227() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-bug3227.xml");
        ThresholdingVisitor visitor = createVisitor();
        SnmpCollectionAgent agent = createCollectionAgent();
        GenericIndexResourceType resourceType = createGenericIndexResourceType(agent, "frCircuitIfIndex");

        // Creating Resource
        SnmpInstId inst = new SnmpInstId(100);
        SnmpCollectionResource resource = new GenericIndexResource(resourceType, "frCircuitIfIndex", inst);
        addAttributeToCollectionResource(resource, resourceType, "frReceivedOctets", "counter", "frCircuitIfIndex", 1000);
        addAttributeToCollectionResource(resource, resourceType, "frSentOctets", "counter", "frCircuitIfIndex", 1000);
        
        /*
         * Run Visitor
         * I must receive 2 special info events because getEntityMap should be called 2 times.
         * One for each attribute and one for each resource.
         * Original code will throw a NullPointerException after call getEntityMap.
         * Original code expects WARNs, but this message is now an INFO.
         */
        resource.visit(visitor);
    }

    /*
     * Testing 32-bit counter wrapping on ifOutOctets
     */
    @Test
    public void testBug3194_32bits() throws Exception {
        runCounterWrapTest(32, 200);
    }

    /*
     * Testing 64-bit counter wrapping on ifOutOctets
     */
    @Test
    public void testBug3194_64bits() throws Exception {
        runCounterWrapTest(64, 201.6);
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-bug3333.xml
     */
    @Test
    public void testBug3333() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-bug3333.xml");
        ThresholdingVisitor visitor = createVisitor();
        String expression = "hrStorageSize-hrStorageUsed";

        // Trigger Low Threshold
        addEvent(EventConstants.LOW_THRESHOLD_EVENT_UEI, "127.0.0.1", "SNMP", 1, 10.0, 15.0, 5.0, "/opt", "1", expression, null, null, m_anticipator, m_anticipatedEvents);
        runFileSystemDataTest(visitor, 1, "/opt", 95, 100);
        verifyEvents(0);

        // Rearm Low Threshold and Trigger High Threshold
        addEvent(EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI, "127.0.0.1", "SNMP", 1, 10.0, 15.0, 60.0, "/opt", "1", expression, null, null, m_anticipator, m_anticipatedEvents);
        addHighThresholdEvent(1, 50, 45, 60, "/opt", "1", expression, null, null);
        runFileSystemDataTest(visitor, 1, "/opt", 40, 100);
        verifyEvents(0);
    }
    
    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration-bug3390.xml
     * - test-thresholds-bug3390.xml
     * 
     * The idea is to define many threshold-group parameters on a service inside a package
     */
    @Test
    public void testBug3390() throws Exception {
        initFactories("/threshd-configuration-bug3390.xml","/test-thresholds-bug3390.xml");
        
        // Validating threshd-configuration.xml
        ThreshdConfigManager configManager = ThreshdConfigFactory.getInstance();
        assertEquals(1, configManager.getConfiguration().getPackageCount());
        org.opennms.netmgt.config.threshd.Package pkg = configManager.getConfiguration().getPackage(0);
        assertEquals(1, pkg.getServiceCount());
        org.opennms.netmgt.config.threshd.Service svc = pkg.getService(0);
        assertEquals(5, svc.getParameterCount());
        int count = 0;
        for (org.opennms.netmgt.config.threshd.Parameter parameter : svc.getParameter()) {
            if (parameter.getKey().equals("thresholding-group"))
                count++;
        }
        assertEquals(5, count);

        // Validating Thresholding Set
        ThresholdingVisitor visitor = createVisitor();
        assertEquals(5, visitor.getThresholdGroups().size());
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration-bug3554.xml
     * - test-thresholds-bug3554.xml
     */
    @Test
    public void testBug3554_withMockFilterDao() throws Exception {
        initFactories("/threshd-configuration-bug3554.xml","/test-thresholds-bug3554.xml");
        
        // Visitor with Mock FavoriteFilterDao
        ThresholdingVisitor visitor = createVisitor();
        visitor.visitCollectionSet(createAnonymousCollectionSet(new Date().getTime()));
        // Do nothing, just to check visitor
        runInterfaceResource(visitor, "127.0.0.1", "eth0", 10000000l, 1, 10000, 46000); // real value = (46000 - 10000)/300 = 120
        
        // Do nothing, just to check visitor
        runGaugeDataTest(visitor, 12000);
        
        // Do nothing, just to check visitor
        SnmpCollectionAgent agent = createCollectionAgent();
        GenericIndexResourceType resourceType = createGenericIndexResourceType(agent, "ciscoEnvMonTemperatureStatusIndex");
        SnmpCollectionResource resource = new GenericIndexResource(resourceType, "ciscoEnvMonTemperatureStatusIndex", new SnmpInstId(45));
        resource.visit(visitor);
        EasyMock.verify(agent);
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration-bug3554.xml
     * - test-thresholds-bug3554.xml
     * 
     * The problem is that every time we create a ThresholdingVisitor instance, the method
     * ThreshdConfigFactory.interfaceInPackage is called. This methods uses JdbcFilterDao
     * to evaluate node filter.
     * 
     * This filter evaluation is the reason of why collectd take too much to initialize on
     * large networks when in-line thresholding is enabled.
     * 
     * From test log, you can see that JdbcFilterDao is invoked on each visitor creation
     * iteration.
     */
    @Test
    public void testBug3554_withDBFilterDao() throws Exception {
        runTestForBug3554();

    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration-bug3554.xml
     * - test-thresholds-bug3554.xml
     * 
     * This test demonstrate that we can force filter auto-reload.
     */
    @Test
    public void testBug3720() throws Exception {
        runTestForBug3554();
        
        // Validate FavoriteFilterDao Calls
        HashSet<String> filters = new HashSet<String>();
        for (org.opennms.netmgt.config.threshd.Package pkg : ThreshdConfigFactory.getInstance().getConfiguration().getPackage()) {
            filters.add(pkg.getFilter().getContent());
        }


    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration-bug3748.xml
     * - test-thresholds-bug3748.xml
     * 
     * This test has been created to validate absolute thresholds.
     */
    @Test
    public void testBug3748() throws Exception {
        initFactories("/threshd-configuration-bug3748.xml","/test-thresholds-bug3748.xml");
        // Absolute threshold evaluator doesn't show threshold and rearm levels on the event.
        addEvent(EventConstants.ABSOLUTE_CHANGE_THRESHOLD_EVENT_UEI, "127.0.0.1", "SNMP", 1, null, null, 6.0, "node", "node", "freeMem", null, null, m_anticipator, m_anticipatedEvents);
        ThresholdingVisitor visitor = createVisitor();
        runGaugeDataTest(visitor, 2); // Set initial value
        runGaugeDataTest(visitor, 6); // Increment the value above configured threshold level: 6 - lastValue > 3, where lastValue=2
        verifyEvents(0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-NMS5115.xml
     * 
     * The idea is to be able to use any numeric metric inside the resource filters. NMS-5115 is a valid use case for this.
     */
    @Test
    public void testNMS5115() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-NMS5115.xml");

        addEvent(EventConstants.LOW_THRESHOLD_EVENT_UEI, "127.0.0.1", "SNMP", 1, null, null, 5.0, "node", "node", "memAvailSwap / memTotalSwap * 100.0", null, null, m_anticipator, m_anticipatedEvents);
        ThresholdingVisitor visitor = createVisitor();

        SnmpCollectionAgent agent = createCollectionAgent();
        NodeResourceType resourceType = createNodeResourceType(agent);
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);

        addAttributeToCollectionResource(resource, resourceType, "memAvailSwap", "gauge", "0", 5);
        addAttributeToCollectionResource(resource, resourceType, "memTotalSwap", "gauge", "0", 100);

        resource.visit(visitor);
        EasyMock.verify(agent);
        verifyEvents(0);
    }

    // Execute an interface test where the physical interface doesn't have any IPAddress (i.e. ipAddr='0.0.0.0')
    // The event will always be associated to Agent Interface (see Bug 3808)
    private void runTestForBug2711(Integer ifIndex, Integer remainingEvents) throws Exception {
        Long ifSpeed = 10000000l;
        String ifName = "wlan0";
        initFactories("/threshd-configuration.xml","/test-thresholds-2.xml");
        addEvent(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "127.0.0.1", "SNMP", 1, 90.0, 50.0, 120.0, ifName, ifIndex.toString(), "ifOutOctets", ifName, ifIndex.toString(), m_anticipator, m_anticipatedEvents);
        addEvent(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "127.0.0.1", "SNMP", 1, 90.0, 50.0, 120.0, ifName, ifIndex.toString(), "ifInOctets", ifName, ifIndex.toString(), m_anticipator, m_anticipatedEvents);
        ThresholdingVisitor visitor = createVisitor();
        visitor.visitCollectionSet(createAnonymousCollectionSet(new Date().getTime()));
        runInterfaceResource(visitor, "0.0.0.0", ifName, ifSpeed, ifIndex, 10000, 46000); // real value = (46000 - 10000)/300 = 120
        verifyEvents(remainingEvents);
    }

    private void runTestForBug3554() throws Exception {
        MockLogAppender.resetEvents();
        System.err.println("----------------------------------------------------------------------------------- begin test");

        String baseIpAddress = "10.0.0.";
        int numOfNodes = 5;

        // Initialize Mock Network

        MockNetwork network = new MockNetwork();
        network.setCriticalService("ICMP");


        for (int i=1; i<=numOfNodes; i++) {
            String ipAddress = baseIpAddress + i;
            network.addNode(i, "testNode-" + ipAddress);
            network.addInterface(ipAddress);
            network.setIfAlias("eth0");
            network.addService("ICMP");
            network.addService("SNMP");
        }
        network.addPathOutage(1, InetAddressUtils.addr("192.168.1.1"), "ICMP");

        MockDatabase db = new MockDatabase();
        db.populate(network);
        db.update("insert into categories (categoryid, categoryname) values (?, ?)", 10, "IPRA");
        db.update("insert into categories (categoryid, categoryname) values (?, ?)", 11, "NAS");
        for (int i=1; i<=numOfNodes; i++) {
            db.update("update snmpinterface set snmpifname=?, snmpifdescr=? where id=?", "eth0", "eth0", i);
            db.update("update node set nodesysoid=? where nodeid=?", ".1.3.6.1.4.1.9.1.222", i);
            db.update("insert into category_node values (?, ?)", 10, i);
            db.update("insert into category_node values (?, ?)", 11, i);
        }
        DataSourceFactory.setInstance(db);

        // Initialize Filter DAO

        System.setProperty("opennms.home", "src/test/resources");
        DatabaseSchemaConfigFactory.init();
        JdbcFilterDao jdbcFilterDao = new JdbcFilterDao();
        jdbcFilterDao.setDataSource(db);
        jdbcFilterDao.setDatabaseSchemaConfigFactory(DatabaseSchemaConfigFactory.getInstance());
        jdbcFilterDao.afterPropertiesSet();
        FilterDaoFactory.setInstance(jdbcFilterDao);

        // Initialize Factories

        initFactories("/threshd-configuration-bug3554.xml","/test-thresholds-bug3554.xml");

        // Initialize Thresholding Visitors

        Map<String,Object> params = new HashMap<String,Object>();
        params.put("thresholding-enabled", "true");
        ServiceParameters svcParams = new ServiceParameters(params);

        for (int i=1; i<=numOfNodes; i++) {
            System.err.println("----------------------------------------------------------------------------------- visitor #" + i);
            String ipAddress = baseIpAddress + i;
            ThresholdingVisitor visitor = ThresholdingVisitor.create(1, ipAddress, "SNMP", getRepository(), svcParams, m_resourceStorageDao);
            assertNotNull(visitor);
            assertEquals(4, visitor.getThresholdGroups().size()); // mib2, cisco, ciscoIPRA, ciscoNAS
        }
        System.err.println("----------------------------------------------------------------------------------- end");
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration-bug3487.xml
     * - test-thresholds.xml
     */
    @Test
    public void testBug3487() throws Exception {
        initFactories("/threshd-configuration-bug3487.xml","/test-thresholds.xml");
        assertNotNull(createVisitor());
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-bug3428.xml
     * 
     * Updated to reflect the fact that counter are treated as rates.
     */
    @Test
    public void testBug3428_noMatch() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-bug3428.xml");
        Integer ifIndex = 1;
        Long ifSpeed = 10000000l; // 10Mbps - Bad Speed
        String ifName = "wlan0";
        addHighThresholdEvent(1, 90, 50, 120, "Unknown", ifIndex.toString(), "ifInOctets", ifName, ifIndex.toString());
        ThresholdingVisitor visitor = createVisitor();
        visitor.visitCollectionSet(createAnonymousCollectionSet(new Date().getTime()));
        runInterfaceResource(visitor, "127.0.0.1", ifName, ifSpeed, ifIndex, 10000, 46000); // real value = (46000 - 10000)/300 = 120
        verifyEvents(1);
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-bug3428.xml
     * 
     * Updated to reflect the fact that counter are treated as rates.
     */
    @Test
    public void testBug3428_match() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-bug3428.xml");
        Integer ifIndex = 1;
        Long ifSpeed = 100000000l; // 100Mbps - Correct Speed!
        String ifName = "wlan0";
        addHighThresholdEvent(1, 90, 50, 120, ifName, ifIndex.toString(), "ifInOctets", ifName, ifIndex.toString());
        ThresholdingVisitor visitor = createVisitor();
        visitor.visitCollectionSet(createAnonymousCollectionSet(new Date().getTime()));
        runInterfaceResource(visitor, "127.0.0.1", ifName, ifSpeed, ifIndex, 10000, 46000); // real value = (46000 - 10000)/300 = 120
        verifyEvents(0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-bug3664.xml
     * 
     * Updated to reflect the fact that counter are treated as rates.
     */
    @Test
    public void testBug3664() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-bug3664.xml");
        Integer ifIndex = 1;
        Long ifSpeed = 10000000l;
        String ifName = "wlan0";
        String domain = "myDomain";
        String ifAlias = ifName;
        String ifAliasComment = "#";

        String label = domain + "/" + ifAlias;
        addHighThresholdEvent(1, 90, 50, 120, label, "Unknown", "ifOutOctets", label, ifIndex.toString());
        addHighThresholdEvent(1, 90, 50, 120, label, "Unknown", "ifInOctets", label, ifIndex.toString());

        Map<String,Object> params = new HashMap<String,Object>();
        params.put("thresholding-enabled", "true");
        params.put("storeByIfAlias", "true");
        ThresholdingVisitor visitor = createVisitor(params);

        SnmpIfData ifData = createSnmpIfData("127.0.0.1", ifName, ifSpeed, ifIndex, true);
        SnmpCollectionAgent agent = createCollectionAgent();
        IfResourceType resourceType = createInterfaceResourceType(agent);

        long timestamp = new Date().getTime();
        // Step 1
        visitor.visitCollectionSet(ThresholdingVisitorIT.createAnonymousCollectionSet(timestamp));
        IfInfo ifInfo = new IfInfo(resourceType, agent, ifData);
        addAttributeToCollectionResource(ifInfo, resourceType, "ifInOctets", "counter", "ifIndex", 10000);
        addAttributeToCollectionResource(ifInfo, resourceType, "ifOutOctets", "counter", "ifIndex", 10000);
        AliasedResource resource = new AliasedResource(resourceType, domain, ifInfo, ifAliasComment, ifAlias);
        resource.visit(visitor);

        // Step 2 - Increment Counters
        visitor.visitCollectionSet(ThresholdingVisitorIT.createAnonymousCollectionSet(timestamp+300000));
        ifInfo = new IfInfo(resourceType, agent, ifData);
        addAttributeToCollectionResource(ifInfo, resourceType, "ifInOctets", "counter", "ifIndex", 46000);
        addAttributeToCollectionResource(ifInfo, resourceType, "ifOutOctets", "counter", "ifIndex", 46000);
        resource = new AliasedResource(resourceType, domain, ifInfo, ifAliasComment, ifAlias);
        resource.visit(visitor);

        EasyMock.verify(agent);
        verifyEvents(0);
    }

    /**
     * This test uses this files from src/test/resources:
     * - threshd-configuration-outages.xml
     * - test-thresholds.xml
     */
     @Test
     public void testBug4261_scheduledOutages() throws Exception {
         initFactories("/threshd-configuration-outages.xml","/test-thresholds.xml");
         ThresholdingVisitor visitor = createVisitor();
         Assert.assertEquals(1, visitor.getScheduledOutages().size());
         Assert.assertTrue("is node on outage", visitor.isNodeInOutage());
     }

     
     /**
      * This test uses this files from src/test/resources:
      * - threshd-configuration.xml
      * - test-thresholds-bug5258-a.xml
      * - test-thresholds-bug5258-b.xml
      */
     @Test
     public void testBug5258() throws Exception {
         initFactories("/threshd-configuration.xml","/test-thresholds-bug5258-a.xml");
         ThresholdingVisitor visitor = createVisitor();

         // Define Main Events
         addEvent(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "127.0.0.1", "SNMP", 1, 50.0, 45.0, 65.0, "/opt", "1", "hrStorageUsed", null, null, m_anticipator, m_anticipatedEvents);
         addEvent(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "127.0.0.1", "SNMP", 1, 50.0, 45.0, 70.0, "/var", "1", "hrStorageUsed", null, null, m_anticipator, m_anticipatedEvents);

         // Define Rearm Event - This is because the configuration of an already triggered threshold has been changed.
         addEvent(EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI, "127.0.0.1", "SNMP", 1, 50.0, 45.0, Double.NaN, "/opt", "1", "hrStorageUsed", null, null, m_anticipator, m_anticipatedEvents);

         // Trigger high Threshold for /opt
         runFileSystemDataTest(visitor, 1, "/opt", 65, 100);

         // Change the filter
         initFactories("/threshd-configuration.xml","/test-thresholds-bug5258-b.xml");
         visitor.reload();
         // Trigger high Threshold for /var
         runFileSystemDataTest(visitor, 1, "/var", 70, 100);

         // Verify Events
         verifyEvents(0);
     }

     @Test
     public void testBug5764() throws Exception {
         ThresholdingVisitor visitor = createVisitor();


         initFactories("/threshd-configuration.xml","/test-thresholds-bug5764.xml");
         
         visitor.reload();

     }

     /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-bug3664.xml
     * 
     * Updated to reflect the fact that counter are treated as rates.
     */
    @Test
    public void testIgnoreAliasedResources() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-bug3664.xml");
        Integer ifIndex = 1;
        Long ifSpeed = 10000000l;
        String ifName = "wlan0";
        String domain = "myDomain";
        String ifAlias = ifName;
        String ifAliasComment = "#";

        ThresholdingVisitor visitor = createVisitor(); // equals to storeByIfAlias = false

        SnmpIfData ifData = createSnmpIfData("127.0.0.1", ifName, ifSpeed, ifIndex, true);
        SnmpCollectionAgent agent = createCollectionAgent();
        IfResourceType resourceType = createInterfaceResourceType(agent);

        // Step 1
        IfInfo ifInfo = new IfInfo(resourceType, agent, ifData);
        addAttributeToCollectionResource(ifInfo, resourceType, "ifInOctets", "counter", "ifIndex", 10000);
        addAttributeToCollectionResource(ifInfo, resourceType, "ifOutOctets", "counter", "ifIndex", 10000);
        AliasedResource resource = new AliasedResource(resourceType, domain, ifInfo, ifAliasComment, ifAlias);
        resource.visit(visitor);

        // Step 2 - Increment Counters
        ifInfo = new IfInfo(resourceType, agent, ifData);
        addAttributeToCollectionResource(ifInfo, resourceType, "ifInOctets", "counter", "ifIndex", 46000);
        addAttributeToCollectionResource(ifInfo, resourceType, "ifOutOctets", "counter", "ifIndex", 46000);
        resource = new AliasedResource(resourceType, domain, ifInfo, ifAliasComment, ifAlias);
        resource.visit(visitor);

        EasyMock.verify(agent);
        verifyEvents(0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-bug3428.xml
     * 
     * Updated to reflect the fact that counter are treated as rates.
     * 
     * This is related with the cutomer support ticket number 300
     */
    @Test
    public void testDisabledCollection() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-bug3428.xml");
        Integer ifIndex = 1;
        Long ifSpeed = 100000000l;
        String ifName = "wlan0";
        addHighThresholdEvent(1, 90, 50, 120, ifName, ifIndex.toString(), "ifInOctets", ifName, ifIndex.toString());
        
        // Create interface resource with data collection disabled
        SnmpIfData ifData = createSnmpIfData("127.0.0.1", ifName, ifSpeed, ifIndex, false);
        SnmpCollectionAgent agent = createCollectionAgent();
        IfResourceType resourceType = createInterfaceResourceType(agent);
        ThresholdingVisitor visitor = createVisitor();
        
        // Step 1 (should be ignored)
        SnmpCollectionResource resource = new IfInfo(resourceType, agent, ifData);
        addAttributeToCollectionResource(resource, resourceType, "ifInOctets", "counter", "ifIndex", 10000);
        addAttributeToCollectionResource(resource, resourceType, "ifOutOctets", "counter", "ifIndex", 10000);
        resource.visit(visitor);
        
        // Step 2 (should be ignored) - Increment Counters; real value = (46000 - 10000)/300 = 120
        resource = new IfInfo(resourceType, agent, ifData);
        addAttributeToCollectionResource(resource, resourceType, "ifInOctets", "counter", "ifIndex", 46000);
        addAttributeToCollectionResource(resource, resourceType, "ifOutOctets", "counter", "ifIndex", 46000);
        resource.visit(visitor);

        EasyMock.verify(agent);
        verifyEvents(1);
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds.xml
     * 
     * It is important to add ".*" at the end of resource-filter tag definition in order to match many resources
     * like this test; for example:
     * 
     * <resource-filter field="hrStorageDescr">^/opt.*</resource-filter>
     * 
     * If we forgot it, /opt01 will not pass threshold filter
     */
    @Test
    public void testThresholsFiltersOnGenericResource() throws Exception {
        ThresholdingVisitor visitor = createVisitor();
        
        String highExpression = "(((hrStorageAllocUnits*hrStorageUsed)/(hrStorageAllocUnits*hrStorageSize))*100)";
        addHighThresholdEvent(1, 30, 25, 50, "/opt", "1", highExpression, null, null);
        addHighThresholdEvent(1, 30, 25, 60, "/opt01", "2", highExpression, null, null);

        runFileSystemDataTest(visitor, 1, "/opt", 50, 100);
        runFileSystemDataTest(visitor, 2, "/opt01", 60, 100);
        runFileSystemDataTest(visitor, 3, "/home", 70, 100);
        
        verifyEvents(0);
    }

    /*
     * NMS-6278
     * 
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-numeric-filter.xml
     */
    @Test
    public void testNumericThresholdFiltersOnGenericResource() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-numeric-filter.xml");
        ThresholdingVisitor visitor = createVisitor();
        
        addHighThresholdEvent(1, 30, 25, 50, "/opt", "1", "hrStorageUsed", null, null);

        runFileSystemDataTest(visitor, 1, "/opt", 50, 100);
        
        verifyEvents(0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration.xml
     * - test-thresholds-5.xml
     */
    @Test
    public void testThresholdsFiltersOnNodeResource() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-5.xml");
        ThresholdingVisitor visitor = createVisitor();
        
        // Adding Expected Thresholds
        addHighThresholdEvent(1, 30, 25, 50, "/home", "node", "(hda1_hrStorageUsed/hda1_hrStorageSize)*100", null, null);
        addHighThresholdEvent(1, 50, 45, 60, "/opt", "node", "(hda2_hrStorageUsed/hda2_hrStorageSize)*100", null, null);

        // Creating Node ResourceType
        SnmpCollectionAgent agent = createCollectionAgent();
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), dataCollectionConfig, m_locationAwareSnmpClient);
        NodeResourceType resourceType = new NodeResourceType(agent, collection);

        // Creating strings.properties file
        ResourcePath path = ResourcePath.get("snmp", "1");
        m_resourceStorageDao.setStringAttribute(path, "hda1_hrStorageDescr", "/home");
        m_resourceStorageDao.setStringAttribute(path, "hda2_hrStorageDescr", "/opt");
        m_resourceStorageDao.setStringAttribute(path, "hda3_hrStorageDescr", "/usr");

        // Creating Resource
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);
        addAttributeToCollectionResource(resource, resourceType, "hda1_hrStorageUsed", "gauge", "node", 50);
        addAttributeToCollectionResource(resource, resourceType, "hda1_hrStorageSize", "gauge", "node", 100);
        addAttributeToCollectionResource(resource, resourceType, "hda2_hrStorageUsed", "gauge", "node", 60);
        addAttributeToCollectionResource(resource, resourceType, "hda2_hrStorageSize", "gauge", "node", 100);
        addAttributeToCollectionResource(resource, resourceType, "hda3_hrStorageUsed", "gauge", "node", 70);
        addAttributeToCollectionResource(resource, resourceType, "hda3_hrStorageSize", "gauge", "node", 100);

        // Run Visitor and Verify Events
        resource.visit(visitor);
        EasyMock.verify(agent);
        verifyEvents(0);
    }

    private ThresholdingVisitor createVisitor() {
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("thresholding-enabled", "true");
        return createVisitor(params);
    }

    private ThresholdingVisitor createVisitor(Map<String,Object> params) {
        ServiceParameters svcParams = new ServiceParameters(params);
        ThresholdingVisitor visitor = ThresholdingVisitor.create(1, "127.0.0.1", "SNMP", getRepository(), svcParams, m_resourceStorageDao);
        assertNotNull(visitor);
        return visitor;
    }

    private void runGaugeDataTest(ThresholdingVisitor visitor, long value) {
        SnmpCollectionAgent agent = createCollectionAgent();
        NodeResourceType resourceType = createNodeResourceType(agent);
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);
        addAttributeToCollectionResource(resource, resourceType, "freeMem", "gauge", "0", value);        
        resource.visit(visitor);
        EasyMock.verify(agent);
    }

    private void runInterfaceResource(ThresholdingVisitor visitor, String ipAddress, String ifName, Long ifSpeed, Integer ifIndex, long v1, long v2) {
        SnmpIfData ifData = createSnmpIfData(ipAddress, ifName, ifSpeed, ifIndex, true);
        SnmpCollectionAgent agent = createCollectionAgent();
        IfResourceType resourceType = createInterfaceResourceType(agent);

        // Step 1
        visitor.visitCollectionSet(createAnonymousCollectionSet(visitor.getCollectionTimestamp().getTime()));
        SnmpCollectionResource resource = new IfInfo(resourceType, agent, ifData);
        addAttributeToCollectionResource(resource, resourceType, "ifInOctets", "counter", "ifIndex", v1);
        addAttributeToCollectionResource(resource, resourceType, "ifOutOctets", "counter", "ifIndex", v1);
        resource.visit(visitor);
        
        // Step 2 - Increment Counters
        visitor.visitCollectionSet(createAnonymousCollectionSet(visitor.getCollectionTimestamp().getTime()+300000));
        resource = new IfInfo(resourceType, agent, ifData);
        addAttributeToCollectionResource(resource, resourceType, "ifInOctets", "counter", "ifIndex", v2);
        addAttributeToCollectionResource(resource, resourceType, "ifOutOctets", "counter", "ifIndex", v2);
        resource.visit(visitor);

        EasyMock.verify(agent);
    }

    private void runFileSystemDataTest(ThresholdingVisitor visitor, int resourceId, String fs, long value, long max) throws Exception {
        SnmpCollectionAgent agent = createCollectionAgent();
        // Creating Generic ResourceType
        GenericIndexResourceType resourceType = createGenericIndexResourceType(agent, "hrStorageIndex");
        // Creating strings.properties file
        ResourcePath path = ResourcePath.get("snmp", "1", "hrStorageIndex", Integer.toString(resourceId));
        m_resourceStorageDao.setStringAttribute(path, "hrStorageType", ".1.3.6.1.2.1.25.2.1.4");
        m_resourceStorageDao.setStringAttribute(path, "hrStorageDescr", fs);
        // Creating Resource
        SnmpInstId inst = new SnmpInstId(resourceId);
        SnmpCollectionResource resource = new GenericIndexResource(resourceType, "hrStorageIndex", inst);
        addAttributeToCollectionResource(resource, resourceType, "hrStorageUsed", "gauge", "hrStorageIndex", value);
        addAttributeToCollectionResource(resource, resourceType, "hrStorageSize", "gauge", "hrStorageIndex", max);
        addAttributeToCollectionResource(resource, resourceType, "hrStorageAllocUnits", "gauge", "hrStorageIndex", 1);
        // Run Visitor
        resource.visit(visitor);
        EasyMock.verify(agent);
    }

    /*
     * Parameter expectedValue should be around 200:
     * Initial counter value is 20000 below limit.
     * Next value is 40000, so the difference will be 60000.
     * Counters are treated as rates so 60000/300 is 200.
     */
    private void runCounterWrapTest(double bits, double expectedValue) throws Exception {
        Integer ifIndex = 1;
        Long ifSpeed = 10000000l;
        String ifName = "wlan0";

        initFactories("/threshd-configuration.xml","/test-thresholds-bug3194.xml");
        addHighThresholdEvent(1, 100, 90, expectedValue, ifName, "1", "ifOutOctets", ifName, ifIndex.toString());
        ThresholdingVisitor visitor = createVisitor();
        
        // Creating Interface Resource Type
        SnmpIfData ifData = createSnmpIfData("127.0.0.1", ifName, ifSpeed, ifIndex, true);
        SnmpCollectionAgent agent = createCollectionAgent();
        IfResourceType resourceType = createInterfaceResourceType(agent);

        // Creating Data Source
        MibObject object = createMibObject("counter", "ifOutOctets", "ifIndex");
        SnmpAttributeType objectType = new NumericAttributeType(resourceType, "default", object, new AttributeGroupType("mibGroup", AttributeGroupType.IF_TYPE_IGNORE));

        long timestamp = new Date().getTime();
        // Step 1 - Initialize Counter
        visitor.visitCollectionSet(ThresholdingVisitorIT.createAnonymousCollectionSet(timestamp));
        BigDecimal n = new BigDecimal(Math.pow(2, bits) - 20000);
        SnmpValue snmpValue1 = SnmpUtils.getValueFactory().getCounter64(n.toBigInteger());
        SnmpCollectionResource resource1 = new IfInfo(resourceType, agent, ifData);
        resource1.setAttributeValue(objectType, snmpValue1);
        resource1.visit(visitor);
        
        // Step 2 - Wrap Counter
        visitor.visitCollectionSet(ThresholdingVisitorIT.createAnonymousCollectionSet(timestamp+300000));
        SnmpValue snmpValue2 = SnmpUtils.getValueFactory().getCounter64(new BigInteger("40000"));
        SnmpCollectionResource resource2 = new IfInfo(resourceType, agent, ifData);
        resource2.setAttributeValue(objectType, snmpValue2);
        resource2.visit(visitor);

        // Verify Events
        EasyMock.verify(agent);        
        verifyEvents(0);
    }
    
    private static SnmpCollectionAgent createCollectionAgent() {
        SnmpCollectionAgent agent = EasyMock.createMock(SnmpCollectionAgent.class);
        EasyMock.expect(agent.getNodeId()).andReturn(1).anyTimes();
        EasyMock.expect(agent.getStorageResourcePath()).andReturn(ResourcePath.get(String.valueOf(1))).anyTimes();
        EasyMock.expect(agent.getHostAddress()).andReturn("127.0.0.1").anyTimes();
        EasyMock.expect(agent.getSnmpInterfaceInfo((IfResourceType)EasyMock.anyObject())).andReturn(new HashSet<IfInfo>()).anyTimes();
        EasyMock.replay(agent);
        return agent;
    }

    private NodeResourceType createNodeResourceType(SnmpCollectionAgent agent) {
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), dataCollectionConfig, m_locationAwareSnmpClient);
        return new NodeResourceType(agent, collection);
    }

    private IfResourceType createInterfaceResourceType(SnmpCollectionAgent agent) {
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), dataCollectionConfig, m_locationAwareSnmpClient);
        return new IfResourceType(agent, collection);
    }

    private GenericIndexResourceType createGenericIndexResourceType(SnmpCollectionAgent agent, String resourceTypeName) {
        org.opennms.netmgt.config.datacollection.ResourceType type = new org.opennms.netmgt.config.datacollection.ResourceType();
        type.setName(resourceTypeName);
        type.setLabel(resourceTypeName);
        org.opennms.netmgt.config.datacollection.StorageStrategy strategy = new org.opennms.netmgt.config.datacollection.StorageStrategy();
        strategy.setClazz("org.opennms.netmgt.collection.support.IndexStorageStrategy");
        type.setStorageStrategy(strategy);
        org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy pstrategy = new org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy();
        pstrategy.setClazz("org.opennms.netmgt.collection.support.PersistAllSelectorStrategy");
        type.setPersistenceSelectorStrategy(pstrategy);
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), dataCollectionConfig, m_locationAwareSnmpClient);
        return new GenericIndexResourceType(agent, collection, type);
    }

    private static void addAttributeToCollectionResource(SnmpCollectionResource resource, ResourceType type, String attributeName, String attributeType, String attributeInstance, long value) {
        MibObject object = createMibObject(attributeType, attributeName, attributeInstance);
        SnmpAttributeType objectType = new NumericAttributeType(type, "default", object, new AttributeGroupType("mibGroup", AttributeGroupType.IF_TYPE_IGNORE));
        SnmpValue snmpValue = attributeType.equals("counter") ? SnmpUtils.getValueFactory().getCounter32(value) : SnmpUtils.getValueFactory().getGauge32(value);
        resource.setAttributeValue(objectType, snmpValue);
    }

    private static MibObject createMibObject(String type, String alias, String instance) {
        MibObject mibObject = new MibObject();
        mibObject.setOid(".1.1.1.1");
        mibObject.setAlias(alias);
        mibObject.setType(type);
        mibObject.setInstance(instance);
        mibObject.setMaxval(null);
        mibObject.setMinval(null);
        return mibObject;
    }

    private RrdRepository getRepository() {
        RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(new File(m_fileAnticipator.getTempDir(), "snmp"));
        return repo;
    }

    private void addHighThresholdEvent(int trigger, double threshold, double rearm, double value, String label, String instance, String ds, String ifLabel, String ifIndex) {
        addEvent(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "127.0.0.1", "SNMP", trigger, threshold, rearm, value, label, instance, ds, ifLabel, ifIndex, m_anticipator, m_anticipatedEvents);
    }

    private void addHighRearmEvent(int trigger, double threshold, double rearm, double value, String label, String instance, String ds, String ifLabel, String ifIndex) {
        addEvent(EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI, "127.0.0.1", "SNMP", trigger, threshold, rearm, value, label, instance, ds, ifLabel, ifIndex, m_anticipator, m_anticipatedEvents);
    }

    private static void addEvent(String uei, String ipaddr, String service, Integer trigger, Double threshold, Double rearm, Double value, String label, String instance, String ds, String ifLabel, String ifIndex, EventAnticipator anticipator, List<Event> anticipatedEvents) {
        
        EventBuilder bldr = new EventBuilder(uei, "ThresholdingVisitorTest");
        bldr.setNodeid(1);
        bldr.setInterface(addr(ipaddr));
        bldr.setService(service);

        bldr.addParam("label", label);

        if (ifLabel != null) {
            bldr.addParam("ifLabel", ifLabel);
        }
        
        if (ifIndex != null) {
            bldr.addParam("ifIndex", ifIndex);
        }

        bldr.addParam("ds", ds);
        
        if (value != null) {
            String pattern = System.getProperty("org.opennms.threshd.value.decimalformat", "###.##"); // See Bug 3427
            DecimalFormat valueFormatter = new DecimalFormat(pattern);
            bldr.addParam("value", value.isNaN() ? AbstractThresholdEvaluatorState.FORMATED_NAN : valueFormatter.format(value));
        }

        bldr.addParam("instance", instance);

        bldr.addParam("trigger", trigger);

        if (threshold != null) {
            bldr.addParam("threshold", threshold);
        }

        if (rearm != null) {
            bldr.addParam("rearm", rearm);
        }

        anticipator.anticipateEvent(bldr.getEvent(), true);
        anticipatedEvents.add(bldr.getEvent());
    }

    private void verifyEvents(int remainEvents) {
        if (remainEvents == 0) {
            List<Event> receivedList = new ArrayList<>(m_anticipator.getAnticipatedEventsReceived());
            
            Collections.sort(receivedList, EVENT_COMPARATOR);
            Collections.sort(m_anticipatedEvents, EVENT_COMPARATOR);
            LOG.info("verifyEvents: Anticipated={}, Received= {}", receivedList.size(), m_anticipatedEvents.size());
            if (m_anticipatedEvents.size() != receivedList.size()) {
                for (Event e : m_anticipatedEvents) {
                    System.err.println("expected event " + e.getUei() + ": " + e.getDescr());
                }
                System.err.println("anticipated = " + m_anticipatedEvents + "\nreceived = " + receivedList);
                fail("Anticipated event count (" + m_anticipatedEvents.size() + ") is different from received event count (" + receivedList.size() + ").");
            }
            for (int i = 0; i < m_anticipatedEvents.size(); i++) {
                LOG.info("verifyEvents: processing event {}", (i+1));
                compareEvents(m_anticipatedEvents.get(i), receivedList.get(i));
            }
        }
        m_anticipator.verifyAnticipated(0, 0, 0, remainEvents, 0);
    }
    
    private static void compareEvents(Event anticipated, Event received) {
        assertEquals("UEIs must match", anticipated.getUei(), received.getUei());
        assertEquals("NodeIDs must match", anticipated.getNodeid(), received.getNodeid());
        assertEquals("interfaces must match", anticipated.getInterface(), received.getInterface());
        assertEquals("services must match", anticipated.getService(), received.getService());
        compareParms(anticipated.getParmCollection(), received.getParmCollection());
    }

    private static void compareParms(List<Parm> anticipatedParms, List<Parm> receivedParms) {
        Collections.sort(anticipatedParms, PARM_COMPARATOR);
        Collections.sort(receivedParms, PARM_COMPARATOR);
        for (Parm source : anticipatedParms) {
            Parm found = null;
            for (Parm p : receivedParms) {
                if (p.getParmName().equals(source.getParmName()))
                    found = p;
            }
            assertNotNull("parameter " + source.getParmName() + " must be found on the received event", found);
            if (source.getValue().getContent() == null) source.getValue().setContent("null");
            assertEquals("content must match for parameter " + source.getParmName(), source.getValue().getContent(), found.getValue().getContent());
        }
    }
    
    private void resetAnticipator() {
        m_anticipator.reset();
        m_anticipatedEvents.clear();
    }

    private static SnmpIfData createSnmpIfData(String ipAddress, String ifName, Long ifSpeed, Integer ifIndex, boolean collectionEnabled) {
        OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("testNode");
        OnmsSnmpInterface snmpIface = new OnmsSnmpInterface(node, ifIndex);
        snmpIface.setIfDescr(ifName);
        snmpIface.setIfName(ifName);
        snmpIface.setIfAlias(ifName);
        snmpIface.setIfSpeed(ifSpeed);
        // If the SNMP interface doesn't have collection enable, threshold processing will be ignored for the interface
        snmpIface.setCollectionEnabled(collectionEnabled);
        return new SnmpIfData(snmpIface);
    }

    private static CollectionSet createAnonymousCollectionSet(long timestamp) {
    	final Date internalTimestamp = new Date(timestamp);
    	return new CollectionSet() {
			@Override
			public void visit(CollectionSetVisitor visitor) {
				//Nothing to do
			}
			
			@Override
			public boolean ignorePersist() {
				return true;
			}
			
			@Override
			public int getStatus() {
				return ServiceCollector.COLLECTION_SUCCEEDED;
			}
			
			@Override
			public Date getCollectionTimestamp() {
				return internalTimestamp;
			}
		};
    }

}
