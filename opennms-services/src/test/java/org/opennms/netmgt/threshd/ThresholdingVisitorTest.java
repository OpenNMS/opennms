//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.threshd;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Category;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.AttributeGroupType;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.GenericIndexResource;
import org.opennms.netmgt.collectd.GenericIndexResourceType;
import org.opennms.netmgt.collectd.IfInfo;
import org.opennms.netmgt.collectd.IfResourceType;
import org.opennms.netmgt.collectd.NodeInfo;
import org.opennms.netmgt.collectd.NodeResourceType;
import org.opennms.netmgt.collectd.NumericAttributeType;
import org.opennms.netmgt.collectd.OnmsSnmpCollection;
import org.opennms.netmgt.collectd.ResourceType;
import org.opennms.netmgt.collectd.ServiceParameters;
import org.opennms.netmgt.collectd.SnmpAttributeType;
import org.opennms.netmgt.collectd.SnmpCollectionResource;
import org.opennms.netmgt.collectd.SnmpIfData;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.dao.FilterDao;
import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockDataCollectionConfig;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.MockLogAppender;

/**
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 *
 */
public class ThresholdingVisitorTest {

    ThresholdingVisitor m_visitor;
    FilterDao m_filterDao;
    EventAnticipator m_anticipator;
    
    @Before
    public void setUp() throws Exception {

        MockLogAppender.setupLogging();

        m_filterDao = EasyMock.createMock(FilterDao.class);
        EasyMock.expect(m_filterDao.getIPList((String)EasyMock.anyObject())).andReturn(Collections.singletonList("127.0.0.1")).anyTimes();
        FilterDaoFactory.setInstance(m_filterDao);
        EasyMock.replay(m_filterDao);
        initFactories("/threshd-configuration.xml","/test-thresholds.xml");

        m_anticipator = new EventAnticipator();
        MockEventIpcManager eventMgr = new MockEventIpcManager();
        eventMgr.setEventAnticipator(m_anticipator);
        eventMgr.setSynchronous(true);
        EventIpcManager eventdIpcMgr = (EventIpcManager)eventMgr;
        EventIpcManagerFactory.setIpcManager(eventdIpcMgr);
    }

    private void initFactories(String threshd, String thresholds) throws Exception {
        Reader reader;
        reader = new InputStreamReader(getClass().getResourceAsStream(thresholds));
        ThresholdingConfigFactory.setInstance(new ThresholdingConfigFactory(reader));
        reader = new InputStreamReader(getClass().getResourceAsStream(threshd));
        ThreshdConfigFactory.setInstance(new ThreshdConfigFactory(reader,"127.0.0.1", false));
    }
    
    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
        EasyMock.verify(m_filterDao);
    }

    @Test
    public void testCreateVisitor() {
        createVisitor();
    }

    @Test
    public void testCreateVisitorWithoutProperEnabledIt() {
        Map<String,String> params = new HashMap<String,String>();
        ThresholdingVisitor visitor = ThresholdingVisitor.create(1, "127.0.0.1", "SNMP", getRepository(), params);
        assertNull(visitor);
    }

    @Test
    public void testVisitResourceGaugeData() {
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        ThresholdingVisitor visitor = createVisitor();
        runGaugeDataTest(visitor, 15000);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
    }

    @Test
    public void testVisitResourceCounterData() {
        ThresholdingVisitor visitor = createVisitor();

        CollectionAgent agent = createCollectionAgent();
        NodeResourceType resourceType = createNodeResourceType(agent);
        MibObject mibObject = createMibObject("counter", "freeMem", "0");
        SnmpAttributeType attributeType = new NumericAttributeType(resourceType, "default", mibObject, new AttributeGroupType("mibGroup", "ignore"));

        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdRearmed");

        // Collect Step 1 : Initialize counter cache
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(15000));
        resource.visit(visitor);

        // Collect Step 2 : Trigger
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(30000));
        resource.visit(visitor);

        // Collect Step 3 : Rearm
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(4000));
        resource.visit(visitor);

        // Collect Step 3 : Reset counter (bad value)
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(10));
        resource.visit(visitor);

        // Collect Step 3 : Normal
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(1010));
        resource.visit(visitor);

        EasyMock.verify(agent);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
    }
    
    @Test
    public void testInterfaceResourceWithDBAttributeFilter() throws Exception {
        setupSnmpInterfaceDatabase("127.0.0.1", "wlan0");
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        runVisitInterfaceResource("127.0.0.1", "wlan0", 100, 220);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
    }

    @Test
    public void testInterfaceResourceWithStringAttributeFilter() throws Exception {
        setupSnmpInterfaceDatabase("127.0.0.1", "sis0");
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        
        File resourceDir = new File(getRepository().getRrdBaseDir(), "1/sis0");
        resourceDir.deleteOnExit();
        resourceDir.mkdirs();
        Properties p = new Properties();
        p.put("myMockParam", "myMockValue");
        ResourceTypeUtils.saveUpdatedProperties(new File(resourceDir, "strings.properties"), p);
        
        runVisitInterfaceResource("127.0.0.1", "sis0", 100, 220);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
        deleteDirectory(new File(getRepository().getRrdBaseDir(), "1"));
    }
    

    @Test
    public void testReloadConfiguration() throws Exception {
        ThresholdingVisitor visitor = createVisitor();
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        runGaugeDataTest(visitor, 4500);
        m_anticipator.verifyAnticipated(0, 0, 0, 1, 0);
        System.err.println("Reloading Config...");
        initFactories("/threshd-configuration.xml","/test-thresholds-2.xml");
        visitor.reload();
        m_anticipator.reset();
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        runGaugeDataTest(visitor, 4500);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
    }
    
    /**
     * This bug has not been replicated, but this code covers the apparent scenario, and can be adapted to match
     * any scenario which can actually replicate the reported issue
     * @throws Exception
     */
    @Test
    public void testBug2746() throws Exception{
        initFactories("/threshd-configuration.xml","/test-thresholds-bug2746.xml");

        ThresholdingVisitor visitor = createVisitor();

        CollectionAgent agent = createCollectionAgent();
        NodeResourceType resourceType = createNodeResourceType(agent);
        MibObject mibObject = createMibObject("gauge", "bug2746", "0");
        SnmpAttributeType attributeType = new NumericAttributeType(resourceType, "default", mibObject, new AttributeGroupType("mibGroup", "ignore"));

        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");

        // Collect Step 1 : Initialize counter cache
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(20));
        resource.visit(visitor);
        
        //Repeat a couple of times with the same value, to replicate a steady state
        resource.visit(visitor);
        resource.visit(visitor);
        resource.visit(visitor);

        // Collect Step 2 : Trigger
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(60));
        resource.visit(visitor);

        // Collect Step 3 : Don't rearm, but do drop
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(45));
        resource.visit(visitor);

        // Collect Step 4 : Shouldn't trigger again
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(55));
        resource.visit(visitor);

        EasyMock.verify(agent);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
    }

    /*
     * Trigger a threshold
     * Reload Configuration
     * Force rearmed
     * 
     * FIXME As the threshold is triggered, merge operation will force rearm. The problem is that
     * configuration never changes for this threshold; so this should be avoided. Before add rearm
     * to the queue we must validate that BasethresholdDef are not identical. Method 'equals' is
     * used, so we can create other method called 'identical' to check all attributes.
     */
    @Test
    public void testBug3146_unrelatedChange() throws Exception {
        ThresholdingVisitor visitor = createVisitor();
        
        // Trigger threshold
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        runGaugeDataTest(visitor, 12000);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
        
        // Reload Configuration
        visitor.reload();
        
        // Send Rearmed event
        m_anticipator.reset();
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdRearmed");
        runGaugeDataTest(visitor, 1000);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
    }
    
    /*
     * Trigger a threshold
     * Make a configuration change, reducing triggered value
     * Reload configuration
     * See if correct rearmed event is triggered
     */
    @Test
    public void testBug3146_reduceTrigger() throws Exception {
        ThresholdingVisitor visitor = createVisitor();

        // Trigger threshold
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        runGaugeDataTest(visitor, 12000);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
        
        // Change Configuration
        initFactories("/threshd-configuration.xml","/test-thresholds-2.xml");
        m_anticipator.reset();
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdRearmed");
        visitor.reload();
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);

        // Trigger threshold
        m_anticipator.reset();
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        runGaugeDataTest(visitor, 5000);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
        
        // Send Rearmed event
        m_anticipator.reset();
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdRearmed");
        runGaugeDataTest(visitor, 1000);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
    }

    /*
     * Trigger a threshold
     * Make a configuration change, incrasing triggered value
     * Reload configuration
     * See if correct rearmed event is triggered
     */
    @Test
    public void testBug3146_inceaseTrigger() throws Exception {
        ThresholdingVisitor visitor = createVisitor();

        // Trigger threshold
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        runGaugeDataTest(visitor, 12000);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
        
        // Change Configuration
        initFactories("/threshd-configuration.xml","/test-thresholds-3.xml");
        m_anticipator.reset();
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdRearmed");
        visitor.reload();
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);

        // Is not above the new threshold value
        m_anticipator.reset();
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdRearmed");
        runGaugeDataTest(visitor, 13000);
        m_anticipator.verifyAnticipated(0, 0, 0, 2, 0);

        // Trigger threshold
        m_anticipator.reset();
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        runGaugeDataTest(visitor, 16000);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
        
        // Send Rearmed event
        m_anticipator.reset();
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdRearmed");
        runGaugeDataTest(visitor, 1000);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
    }

    /*
     * If I have a high threshold triggered, and then replace it with their equivalent low threshold,
     * The high definition must be removed from cache and rearmed event must be sent.
     */
    @Test
    public void testBug3146_replaceThreshold() throws Exception {
        ThresholdingVisitor visitor = createVisitor();

        // Trigger threshold
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        runFileSystemDataTest(visitor, "/opt", 500, 1000);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
               
        m_anticipator.reset();
        log().debug("*************** Reset Configuration");
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdRearmed"); // Disarm triggered threshold
        initFactories("/threshd-configuration.xml","/test-thresholds-4.xml");
        visitor.reload();
        
        // Must trigger only one low threshold exceeded
        addAnticipatedEvent("uei.opennms.org/threshold/lowThresholdExceeded");
        runFileSystemDataTest(visitor, "/opt", 950, 1000);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
    }

    private ThresholdingVisitor createVisitor() {
        Map<String,String> params = new HashMap<String,String>();
        params.put("thresholding-enabled", "true");
        ThresholdingVisitor visitor = ThresholdingVisitor.create(1, "127.0.0.1", "SNMP", getRepository(), params);
        assertNotNull(visitor);
        return visitor;
    }

    private void runGaugeDataTest(ThresholdingVisitor visitor, long value) {
        CollectionAgent agent = createCollectionAgent();
        NodeResourceType resourceType = createNodeResourceType(agent);
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);
        addAttributeToCollectionResource(resource, resourceType, "freeMem", "gauge", "0", value);        
        resource.visit(visitor);
        EasyMock.verify(agent);
    }

    private void runVisitInterfaceResource(String ipAddress, String ifName, long v1, long v2) {
        ThresholdingVisitor visitor = createVisitor();
        
        SnmpIfData ifData = createSnmpIfData(ipAddress, ifName);
        CollectionAgent agent = createCollectionAgent();
        IfResourceType resourceType = createInterfaceResourceType(agent);

        // Step 1
        SnmpCollectionResource resource = new IfInfo(resourceType, agent, ifData);
        addAttributeToCollectionResource(resource, resourceType, "ifInOctets", "counter", "ifIndex", v1);
        addAttributeToCollectionResource(resource, resourceType, "ifOutOctets", "counter", "ifIndex", v1);
        resource.visit(visitor);
        
        // Step 2 - Increment Counters
        resource = new IfInfo(resourceType, agent, ifData);
        addAttributeToCollectionResource(resource, resourceType, "ifInOctets", "counter", "ifIndex", v2);
        addAttributeToCollectionResource(resource, resourceType, "ifOutOctets", "counter", "ifIndex", v2);
        resource.visit(visitor);

        EasyMock.verify(agent);
    }

    private void runFileSystemDataTest(ThresholdingVisitor visitor, String fs, long value, long max) throws Exception {
        CollectionAgent agent = createCollectionAgent();
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, String>()), dataCollectionConfig);
        org.opennms.netmgt.config.datacollection.ResourceType type = new org.opennms.netmgt.config.datacollection.ResourceType();
        type.setName("hrStorageIndex");
        type.setLabel("Storage (MIB-2 Host Resources)");
        org.opennms.netmgt.config.datacollection.StorageStrategy strategy = new org.opennms.netmgt.config.datacollection.StorageStrategy();
        strategy.setClazz("org.opennms.netmgt.dao.support.IndexStorageStrategy");
        type.setStorageStrategy(strategy);
        org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy pstrategy = new org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy();
        pstrategy.setClazz("org.opennms.netmgt.collectd.PersistAllSelectorStrategy");
        type.setPersistenceSelectorStrategy(pstrategy);
        GenericIndexResourceType resourceType = new GenericIndexResourceType(agent, collection, type);
        Properties p = new Properties();
        p.put("hrStorageType", ".1.3.6.1.2.1.25.2.1.4");
        p.put("hrStorageDescr", "/opt");
        File f = new File(getRepository().getRrdBaseDir(), "1/hrStorageIndex/1/strings.properties");
        ResourceTypeUtils.saveUpdatedProperties(f, p);
        SnmpInstId inst = new SnmpInstId(1);
        SnmpCollectionResource resource = new GenericIndexResource(resourceType, "hrStorageIndex", inst);
        addAttributeToCollectionResource(resource, resourceType, "hrStorageUsed", "gauge", "hrStorageIndex", value);
        addAttributeToCollectionResource(resource, resourceType, "hrStorageSize", "gauge", "hrStorageIndex", max);
        addAttributeToCollectionResource(resource, resourceType, "hrStorageAllocUnits", "gauge", "hrStorageIndex", 1);
        resource.visit(visitor);
        EasyMock.verify(agent);
        f.delete();
    }

    private CollectionAgent createCollectionAgent() {
        CollectionAgent agent = EasyMock.createMock(CollectionAgent.class);
        EasyMock.expect(agent.getNodeId()).andReturn(1).anyTimes();
        EasyMock.expect(agent.getHostAddress()).andReturn("127.0.0.1").anyTimes();
        EasyMock.expect(agent.getSnmpInterfaceInfo((IfResourceType)EasyMock.anyObject())).andReturn(new HashSet<IfInfo>()).anyTimes();
        EasyMock.replay(agent);
        return agent;
    }

    private NodeResourceType createNodeResourceType(CollectionAgent agent) {
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, String>()), dataCollectionConfig);
        return new NodeResourceType(agent, collection);
    }

    private IfResourceType createInterfaceResourceType(CollectionAgent agent) {
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, String>()), dataCollectionConfig);
        return new IfResourceType(agent, collection);
    }
    
    private void addAttributeToCollectionResource(SnmpCollectionResource resource, ResourceType type, String attributeName, String attributeType, String attributeInstance, long value) {
        MibObject object = createMibObject(attributeType, attributeName, attributeInstance);
        SnmpAttributeType objectType = new NumericAttributeType(type, "default", object, new AttributeGroupType("mibGroup", "ignore"));
        resource.setAttributeValue(objectType, SnmpUtils.getValueFactory().getCounter32(value));
    }

    private MibObject createMibObject(String type, String alias, String instance) {
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
        repo.setRrdBaseDir(new File("/tmp"));
        return repo;		
    }

    private void addAnticipatedEvent(String uei) {
        Event e = new Event();
        e.setUei(uei);
        e.setNodeid(1);
        e.setInterface("127.0.0.1");
        e.setService("SNMP");
        m_anticipator.anticipateEvent(e);
    }
    
    private SnmpIfData createSnmpIfData(String ipAddress, String ifName) {
        OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("testNode");
        OnmsSnmpInterface snmpIface = new OnmsSnmpInterface(ipAddress, 1, node);
        snmpIface.setIfDescr(ifName);
        snmpIface.setIfName(ifName);
        snmpIface.setIfAlias(ifName);
        snmpIface.setIfSpeed(10000000l);
        return new SnmpIfData(snmpIface);
    }
    
    private void setupSnmpInterfaceDatabase(String ipAddress, String ifName) throws Exception {
        MockNetwork network = new MockNetwork();
        network.setCriticalService("ICMP");
        network.addNode(1, "testNode");
        network.addInterface(ipAddress);
        network.setIfAlias(ifName);
        network.addService("ICMP");
        network.addService("SNMP");
        MockDatabase db = new MockDatabase();
        db.populate(network);
        db.update("update snmpinterface set snmpifname=?, snmpifdescr=? where id=?", ifName, ifName, 1);
        DataSourceFactory.setInstance(db);
    }
    
    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return path.delete();
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
