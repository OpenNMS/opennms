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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
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
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.test.mock.MockLogAppender;

/**
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 *
 */
public class ThresholdingVisitorTest {

    Level m_defaultErrorLevelToCheck;
    ThresholdingVisitor m_visitor;
    FilterDao m_filterDao;
    EventAnticipator m_anticipator;
    List<Event> m_anticipatedEvents;
    
    @Before
    public void setUp() throws Exception {
        m_defaultErrorLevelToCheck = Level.WARN;
        MockLogAppender.setupLogging();

        m_filterDao = EasyMock.createMock(FilterDao.class);
        EasyMock.expect(m_filterDao.getIPList((String)EasyMock.anyObject())).andReturn(Collections.singletonList("127.0.0.1")).anyTimes();
        FilterDaoFactory.setInstance(m_filterDao);
        EasyMock.replay(m_filterDao);

        m_anticipator = new EventAnticipator();
        MockEventIpcManager eventMgr = new MockEventIpcManager();
        eventMgr.setEventAnticipator(m_anticipator);
        eventMgr.setSynchronous(true);
        EventIpcManager eventdIpcMgr = (EventIpcManager)eventMgr;
        EventIpcManagerFactory.setIpcManager(eventdIpcMgr);
        
        initFactories("/threshd-configuration.xml","/test-thresholds.xml");
        m_anticipatedEvents = new ArrayList<Event>();
    }

    private void initFactories(String threshd, String thresholds) throws Exception {
        ThresholdingConfigFactory.setInstance(new ThresholdingConfigFactory(getClass().getResourceAsStream(thresholds)));
        ThreshdConfigFactory.setInstance(new ThreshdConfigFactory(getClass().getResourceAsStream(threshd),"127.0.0.1", false));
    }
    
    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNotGreaterOrEqual(m_defaultErrorLevelToCheck);
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

    /*
     * This test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds.xml
     */
    @Test
    public void testResourceGaugeData() {
        addHighThresholdEvent(1, 10000, 5000, 15000, "Unknown", null, "freeMem", null, null);
        ThresholdingVisitor visitor = createVisitor();
        runGaugeDataTest(visitor, 15000);
        verifyEvents(0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds.xml
     */
    @Test
    public void testResourceCounterData() {
        ThresholdingVisitor visitor = createVisitor();

        CollectionAgent agent = createCollectionAgent();
        NodeResourceType resourceType = createNodeResourceType(agent);
        MibObject mibObject = createMibObject("counter", "freeMem", "0");
        SnmpAttributeType attributeType = new NumericAttributeType(resourceType, "default", mibObject, new AttributeGroupType("mibGroup", "ignore"));

        // Add Events
        addHighThresholdEvent(1, 10000, 5000, 15000, "Unknown", null, "freeMem", null, null);
        addHighRearmEvent(1, 10000, 5000, 1000, "Unknown", null, "freeMem", null, null);

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
        verifyEvents(0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds.xml
     */
    @Test
    public void testInterfaceResourceWithDBAttributeFilter() throws Exception {
        setupSnmpInterfaceDatabase("127.0.0.1", "wlan0");
        addHighThresholdEvent(1, 90, 50, 120, "Unknown", "1", "ifOutOctets", "wlan0", "1");
        addHighThresholdEvent(1, 90, 50, 120, "Unknown", "1", "ifInOctets", "wlan0", "1");
        runInterfaceResource("127.0.0.1", "wlan0", 100, 220); // real value = 220 - 100 = 120
        verifyEvents(0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds.xml
     */
    @Test
    public void testInterfaceResourceWithStringAttributeFilter() throws Exception {
        setupSnmpInterfaceDatabase("127.0.0.1", "sis0");
        addHighThresholdEvent(1, 90, 50, 120, "Unknown", "1", "ifOutOctets", "sis0", "1");
        addHighThresholdEvent(1, 90, 50, 120, "Unknown", "1", "ifInOctets", "sis0", "1");

        File resourceDir = new File(getRepository().getRrdBaseDir(), "1/sis0");
        resourceDir.deleteOnExit();
        resourceDir.mkdirs();
        Properties p = new Properties();
        p.put("myMockParam", "myMockValue");
        ResourceTypeUtils.saveUpdatedProperties(new File(resourceDir, "strings.properties"), p);
        
        runInterfaceResource("127.0.0.1", "sis0", 100, 220); // real value = 220 - 100 = 120
        verifyEvents(0);
        deleteDirectory(new File(getRepository().getRrdBaseDir(), "1"));
    }
    
    /*
     * Before call visitor.reload(), this test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds.xml
     * 
     * After call visitor.reload(), this test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds-2.xml
     */
    @Test
    public void testReloadConfiguration() throws Exception {
        ThresholdingVisitor visitor = createVisitor();
        
        // Step 1: No events
        addHighThresholdEvent(1, 10000, 5000, 4500, "Unknown", null, "freeMem", null, null);
        runGaugeDataTest(visitor, 4500);
        verifyEvents(1);
        
        // Step 2: Change configuration
        initFactories("/threshd-configuration.xml","/test-thresholds-2.xml");
        visitor.reload();
        resetAnticipator();
        
        // Step 3: Trigger threshold with new configuration values
        addHighThresholdEvent(1, 4000, 2000, 4500, "Unknown", null, "freeMem", null, null);
        runGaugeDataTest(visitor, 4500);
        verifyEvents(0);
    }

    /*
     * This bug has not been replicated, but this code covers the apparent scenario, and can be adapted to match
     * any scenario which can actually replicate the reported issue
     * 
     * This test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds-bug2746.xml
     */
    @Test
    public void testBug2746() throws Exception{
        initFactories("/threshd-configuration.xml","/test-thresholds-bug2746.xml");

        ThresholdingVisitor visitor = createVisitor();

        CollectionAgent agent = createCollectionAgent();
        NodeResourceType resourceType = createNodeResourceType(agent);
        MibObject mibObject = createMibObject("gauge", "bug2746", "0");
        SnmpAttributeType attributeType = new NumericAttributeType(resourceType, "default", mibObject, new AttributeGroupType("mibGroup", "ignore"));

        // Add Events
        addHighThresholdEvent(1, 50, 40, 60, "Unknown", null, "bug2746", null, null);

        // Step 1 : Initialize counter cache
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(20));
        resource.visit(visitor);
        
        // Repeat a couple of times with the same value, to replicate a steady state
        resource.visit(visitor);
        resource.visit(visitor);
        resource.visit(visitor);

        // Step 2 : Trigger
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(60));
        resource.visit(visitor);

        // Step 3 : Don't rearm, but do drop
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(45));
        resource.visit(visitor);

        // Step 4 : Shouldn't trigger again
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(55));
        resource.visit(visitor);

        EasyMock.verify(agent);
        verifyEvents(0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds.xml
     */
    @Test
    public void testBug3146_unrelatedChange() throws Exception {
        ThresholdingVisitor visitor = createVisitor();
        
        // Add Events
        addHighThresholdEvent(1, 10000, 5000, 12000, "Unknown", null, "freeMem", null, null);
        addHighRearmEvent(1, 10000, 5000, 1000, "Unknown", null, "freeMem", null, null);
        
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
     * - thresd-configuration.xml
     * - test-thresholds.xml
     * 
     * After call visitor.reload(), this test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds-2.xml
     */
    @Test
    public void testBug3146_reduceTrigger() throws Exception {
        ThresholdingVisitor visitor = createVisitor();

        // Add Events
        addHighThresholdEvent(1, 10000, 5000, 12000, "Unknown", null, "freeMem", null, null);
        addHighRearmEvent(1, 10000, 5000, Double.NaN, "Unknown", null, "freeMem", null, null);
        addHighThresholdEvent(1, 4000, 2000, 5000, "Unknown", null, "freeMem", null, null);
        addHighRearmEvent(1, 4000, 2000, 1000, "Unknown", null, "freeMem", null, null);

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
     * - thresd-configuration.xml
     * - test-thresholds.xml
     * 
     * After call visitor.reload(), this test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds-3.xml
     */
    @Test
    public void testBug3146_inceaseTrigger() throws Exception {
        ThresholdingVisitor visitor = createVisitor();

        // Add Events
        addHighThresholdEvent(1, 10000, 5000, 12000, "Unknown", null, "freeMem", null, null);
        addHighRearmEvent(1, 10000, 5000, Double.NaN, "Unknown", null, "freeMem", null, null);

        // Step 1: Trigger threshold
        runGaugeDataTest(visitor, 12000);
        
        // Step 2: Change Configuration (increasing value for already triggered threshold)
        initFactories("/threshd-configuration.xml","/test-thresholds-3.xml");
        
        // Step 3: Execute Merge Configuration (Rearmed Event must be sent).
        visitor.reload();
        verifyEvents(0);
        
        // Step 4: New collected data is not above the new threshold value. No Events generated
        resetAnticipator();
        addHighThresholdEvent(1, 15000, 14000, 13000, "Unknown", null, "freeMem", null, null);
        runGaugeDataTest(visitor, 13000);
        verifyEvents(1);
        
        // Step 5: Trigger and rearm a threshold using new configuration
        resetAnticipator();
        addHighThresholdEvent(1, 15000, 14000, 16000, "Unknown", null, "freeMem", null, null);
        addHighRearmEvent(1, 15000, 14000, 1000, "Unknown", null, "freeMem", null, null);
        runGaugeDataTest(visitor, 16000);
        runGaugeDataTest(visitor, 1000);
        verifyEvents(0);
    }

    /*
     * If I have a high threshold triggered, and then replace it with their equivalent low threshold,
     * The high definition must be removed from cache and rearmed event must be sent.
     * 
     * Before call visitor.reload(), this test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds.xml
     * 
     * After call visitor.reload(), this test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds-4.xml
     */
    @Test
    public void testBug3146_replaceThreshold() throws Exception {
        ThresholdingVisitor visitor = createVisitor();
        
        // Add Events
        String lowThresholdUei = "uei.opennms.org/threshold/lowThresholdExceeded";
        String highExpression = "(((hrStorageAllocUnits*hrStorageUsed)/(hrStorageAllocUnits*hrStorageSize))*100)";
        String lowExpression = "(100-((hrStorageAllocUnits*hrStorageUsed)/(hrStorageAllocUnits*hrStorageSize))*100)";
        addHighThresholdEvent(1, 30, 25, 50, "/opt", "1", highExpression, null, null);
        addHighRearmEvent(1, 30, 25, Double.NaN, "/opt", "1", highExpression, null, null);
        addEvent(lowThresholdUei, "127.0.0.1", "SNMP", 1, 10, 20, 5, "/opt", "1", lowExpression, null, null);

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
     * - thresd-configuration.xml
     * - test-thresholds-bug3193.xml
     */
    @Test
    public void testBug3193() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-bug3193.xml");
        ThresholdingVisitor visitor = createVisitor();

        CollectionAgent agent = createCollectionAgent();
        NodeResourceType resourceType = createNodeResourceType(agent);
        MibObject mibObject = createMibObject("counter", "myCounter", "0");
        SnmpAttributeType attributeType = new NumericAttributeType(resourceType, "default", mibObject, new AttributeGroupType("mibGroup", "ignore"));

        // Add Events
        addHighThresholdEvent(1, 100, 90, 110, "Unknown", null, "myCounter", null, null);
        addHighThresholdEvent(1, 70, 60, 80, "Unknown", null, "myCounter - 30", null, null);
        addHighRearmEvent(1, 100, 90, 40, "Unknown", null, "myCounter", null, null);
        addHighRearmEvent(1, 70, 60, 10, "Unknown", null, "myCounter - 30", null, null);

        // Collect Step 1 : First Data (Last should be NaN)
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(50));
        resource.visit(visitor);

        // Collect Step 2 : First Value (last - current = 60)
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(110));
        resource.visit(visitor);

        // Collect Step 3 : Second Value (last - current = 110). Trigger
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(220));
        resource.visit(visitor);

        // Collect Step 3 : Third Value (last - current = 40). Rearm
        resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(260));
        resource.visit(visitor);

        EasyMock.verify(agent);
        verifyEvents(0);
    }
    
    /*
     * This test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds-2.xml
     */
    @Test
    public void testBug2711_noIpAddress() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-2.xml");
        setupSnmpInterfaceWithoutIpDatabase("wlan0", 2, false);
        addEvent("uei.opennms.org/threshold/highThresholdExceeded", "0.0.0.0", "SNMP", 1, 90, 50, 120, "Unknown", "1", "ifOutOctets", "wlan0", "2");
        addEvent("uei.opennms.org/threshold/highThresholdExceeded", "0.0.0.0", "SNMP", 1, 90, 50, 120, "Unknown", "1", "ifInOctets", "wlan0", "2");
        runInterfaceResource("0.0.0.0", "wlan0", 100, 220); // real value = 220 - 100 = 120
        verifyEvents(0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds-2.xml
     */
    @Test
    public void testBug2711_noIP_noSnmpIfInfo() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-2.xml");
        setupSnmpInterfaceWithoutIpDatabase("wlan0", 2, true);
        addEvent("uei.opennms.org/threshold/highThresholdExceeded", "10.10.0.1", "SNMP", 1, 90, 50, 120, "Unknown", "1", "ifOutOctets", "wlan0", "2");
        addEvent("uei.opennms.org/threshold/highThresholdExceeded", "10.10.0.1", "SNMP", 1, 90, 50, 120, "Unknown", "1", "ifInOctets", "wlan0", "2");
        runInterfaceResource("10.10.0.1", "wlan0", 100, 220); // real value = 220 - 100 = 120
        verifyEvents(2);
    }

    /*
     * This test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds-2.xml
     */
    @Test
    public void testBug2711_noIP_badIfIndex() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-2.xml");
        setupSnmpInterfaceWithoutIpDatabase("wlan0", -100, false);
        addEvent("uei.opennms.org/threshold/highThresholdExceeded", "0.0.0.0", "SNMP", 1, 90, 50, 120, "Unknown", "1", "ifOutOctets", "wlan0", "2");
        addEvent("uei.opennms.org/threshold/highThresholdExceeded", "0.0.0.0", "SNMP", 1, 90, 50, 120, "Unknown", "1", "ifInOctets", "wlan0", "2");
        runInterfaceResource("0.0.0.0", "wlan0", 100, 220); // real value = 220 - 100 = 120
        verifyEvents(2);
    }

    /*
     * This test uses this files from src/test/resources:
     * - thresd-configuration.xml
     * - test-thresholds-2.xml
     * 
     * There is no Frame Relay related thresholds definitions on test-thresholds-2.xml
     * When visit resources, getEntityMap from ThresholdingSet must null
     */
    @Test
    public void testBug3227() throws Exception {
        initFactories("/threshd-configuration.xml","/test-thresholds-bug3227.xml");
        ThresholdingVisitor visitor = createVisitor();

        CollectionAgent agent = createCollectionAgent();
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, String>()), dataCollectionConfig);

        // Creating DataCollection ResourceType
        org.opennms.netmgt.config.datacollection.ResourceType type = new org.opennms.netmgt.config.datacollection.ResourceType();
        type.setName("frCircuitIfIndex");
        type.setLabel("Frame-Relay (RFC1315)");
        org.opennms.netmgt.config.datacollection.StorageStrategy strategy = new org.opennms.netmgt.config.datacollection.StorageStrategy();
        strategy.setClazz("org.opennms.netmgt.dao.support.IndexStorageStrategy");
        type.setStorageStrategy(strategy);
        org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy pstrategy = new org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy();
        pstrategy.setClazz("org.opennms.netmgt.collectd.PersistAllSelectorStrategy");
        type.setPersistenceSelectorStrategy(pstrategy);
        
        // Creating Generic ResourceType
        GenericIndexResourceType resourceType = new GenericIndexResourceType(agent, collection, type);

        // Creating Resource
        SnmpInstId inst = new SnmpInstId(100);
        SnmpCollectionResource resource = new GenericIndexResource(resourceType, "frCircuitIfIndex", inst);
        addAttributeToCollectionResource(resource, resourceType, "frReceivedOctets", "counter", "frCircuitIfIndex", 1000);
        addAttributeToCollectionResource(resource, resourceType, "frSentOctets", "counter", "frCircuitIfIndex", 1000);
        
        /*
         * Run Visitor
         * I must receive 3 warnings because getEntityMap should be called 3 times.
         * One for each attribute and one for each resource.
         * Original code will throw a NullPointerException after call getEntityMap.
         */
        m_defaultErrorLevelToCheck = Level.ERROR;
        resource.visit(visitor);
        LoggingEvent[] events = MockLogAppender.getEventsGreaterOrEqual(Level.WARN);
        Assert.assertEquals(3, events.length);
        for (LoggingEvent e : events) {
            Assert.assertEquals("getEntityMap: No thresholds configured for resource type frCircuitIfIndex. Not processing this collection.", e.getMessage());
        }
    }

    /*
     * Testing custom ThresholdingSet implementation for in-line Latency thresholds processing for Pollerd.
     * 
     * This test validate that Bug 1582 has been fixed.
     * ifLabel and ifIndex are set correctly based on Bug 2711
     */
    @Test    
    public void testLatencyThresholdingSet() throws Exception {
        setupSnmpInterfaceDatabase("127.0.0.1", "lo0");

        LatencyThresholdingSet thresholdingSet = new LatencyThresholdingSet(1, "127.0.0.1", "HTTP", getRepository());
        assertTrue(thresholdingSet.hasThresholds()); // Global Test
        assertTrue(thresholdingSet.hasThresholds("http")); // Datasource Test
        Map<String, Double> attributes = new HashMap<String, Double>();        

        attributes.put("http", 90.0);
        List<Event> triggerEvents = thresholdingSet.applyThresholds("http", attributes);
        assertTrue(triggerEvents.size() == 0);

        // Test Trigger
        attributes.put("http", 200.0);
        for (int i = 1; i < 5; i++) {
            log().debug("testLatencyThresholdingSet: run number " + i);
            if (thresholdingSet.hasThresholds("http")) {
                triggerEvents = thresholdingSet.applyThresholds("http", attributes);
                assertTrue(triggerEvents.size() == 0);
            }
        }
        if (thresholdingSet.hasThresholds("http")) {
            log().debug("testLatencyThresholdingSet: run number 5");
            triggerEvents = thresholdingSet.applyThresholds("http", attributes);
            assertTrue(triggerEvents.size() == 1);
        }
        
        // Test Rearm
        List<Event> rearmEvents = null;
        if (thresholdingSet.hasThresholds("http")) {
            attributes.put("http", 40.0);
            rearmEvents = thresholdingSet.applyThresholds("http", attributes);
            assertTrue(rearmEvents.size() == 1);
        }

        // Validate Events
        addEvent("uei.opennms.org/threshold/highThresholdExceeded", "127.0.0.1", "HTTP", 5, 100, 50, 200, "Unknown", "127.0.0.1[http]", "http", "lo0", "1");
        addEvent("uei.opennms.org/threshold/highThresholdRearmed", "127.0.0.1", "HTTP", 5, 100, 50, 40, "Unknown", "127.0.0.1[http]", "http", "lo0", "1");
        ThresholdingEventProxy proxy = new ThresholdingEventProxy();
        proxy.add(triggerEvents);
        proxy.add(rearmEvents);
        proxy.sendAllEvents();
        verifyEvents(0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - thresd-configuration.xml
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
    public void testThresholsFilters() throws Exception {
        ThresholdingVisitor visitor = createVisitor();
        
        String highExpression = "(((hrStorageAllocUnits*hrStorageUsed)/(hrStorageAllocUnits*hrStorageSize))*100)";
        addHighThresholdEvent(1, 30, 25, 50, "/opt", "1", highExpression, null, null);
        addHighThresholdEvent(1, 30, 25, 60, "/opt01", "2", highExpression, null, null);

        runFileSystemDataTest(visitor, 1, "/opt", 50, 100);
        runFileSystemDataTest(visitor, 2, "/opt01", 60, 100);
        runFileSystemDataTest(visitor, 3, "/home", 70, 100);
        
        verifyEvents(0);
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

    private void runInterfaceResource(String ipAddress, String ifName, long v1, long v2) {
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

    private void runFileSystemDataTest(ThresholdingVisitor visitor, int resourceId, String fs, long value, long max) throws Exception {
        CollectionAgent agent = createCollectionAgent();
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, String>()), dataCollectionConfig);
        // Creating DataCollection ResourceType
        org.opennms.netmgt.config.datacollection.ResourceType type = new org.opennms.netmgt.config.datacollection.ResourceType();
        type.setName("hrStorageIndex");
        type.setLabel("Storage (MIB-2 Host Resources)");
        org.opennms.netmgt.config.datacollection.StorageStrategy strategy = new org.opennms.netmgt.config.datacollection.StorageStrategy();
        strategy.setClazz("org.opennms.netmgt.dao.support.IndexStorageStrategy");
        type.setStorageStrategy(strategy);
        org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy pstrategy = new org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy();
        pstrategy.setClazz("org.opennms.netmgt.collectd.PersistAllSelectorStrategy");
        type.setPersistenceSelectorStrategy(pstrategy);
        // Creating Generic ResourceType
        GenericIndexResourceType resourceType = new GenericIndexResourceType(agent, collection, type);
        // Creating strings.properties file
        Properties p = new Properties();
        p.put("hrStorageType", ".1.3.6.1.2.1.25.2.1.4");
        p.put("hrStorageDescr", fs);
        File f = new File(getRepository().getRrdBaseDir(), "1/hrStorageIndex/" + resourceId + "/strings.properties");
        ResourceTypeUtils.saveUpdatedProperties(f, p);
        // Creating Resource
        SnmpInstId inst = new SnmpInstId(resourceId);
        SnmpCollectionResource resource = new GenericIndexResource(resourceType, "hrStorageIndex", inst);
        addAttributeToCollectionResource(resource, resourceType, "hrStorageUsed", "gauge", "hrStorageIndex", value);
        addAttributeToCollectionResource(resource, resourceType, "hrStorageSize", "gauge", "hrStorageIndex", max);
        addAttributeToCollectionResource(resource, resourceType, "hrStorageAllocUnits", "gauge", "hrStorageIndex", 1);
        // Run Visitor
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

    private void addHighThresholdEvent(int trigger, double threshold, double rearm, double value, String label, String instance, String ds, String ifLabel, String ifIndex) {
        addEvent("uei.opennms.org/threshold/highThresholdExceeded", "127.0.0.1", "SNMP", trigger, threshold, rearm, value, label, instance, ds, ifLabel, ifIndex);
    }

    private void addHighRearmEvent(int trigger, double threshold, double rearm, double value, String label, String instance, String ds, String ifLabel, String ifIndex) {
        addEvent("uei.opennms.org/threshold/highThresholdRearmed", "127.0.0.1", "SNMP", trigger, threshold, rearm, value, label, instance, ds, ifLabel, ifIndex);
    }

    private void addEvent(String uei, String ipaddr, String service, int trigger, double threshold, double rearm, double value, String label, String instance, String ds, String ifLabel, String ifIndex) {
        Event e = new Event();
        e.setUei(uei);
        e.setNodeid(1);
        e.setInterface(ipaddr);
        e.setService(service);
        Parms parms = new Parms();

        Parm p = new Parm();
        p.setParmName("label");
        Value v = new Value();
        v.setContent(label);
        p.setValue(v);
        parms.addParm(p);

        if (ifLabel != null) {
            p = new Parm();
            p.setParmName("ifLabel");
            v = new Value();
            v.setContent(ifLabel);
            p.setValue(v);
            parms.addParm(p);            
        }
        
        if (ifIndex != null) {
            p = new Parm();
            p.setParmName("ifIndex");
            v = new Value();
            v.setContent(ifIndex);
            p.setValue(v);
            parms.addParm(p);   
        }
        
        p = new Parm();
        p.setParmName("ds");
        v = new Value();
        v.setContent(ds);
        p.setValue(v);
        parms.addParm(p);
        
        p = new Parm();
        p.setParmName("value");
        v = new Value();
        v.setContent(Double.toString(value));
        p.setValue(v);
        parms.addParm(p);

        p = new Parm();
        p.setParmName("instance");
        v = new Value();
        v.setContent(instance);
        p.setValue(v);
        parms.addParm(p);

        p = new Parm();
        p.setParmName("trigger");
        v = new Value();
        v.setContent(Integer.toString(trigger));
        p.setValue(v);
        parms.addParm(p);

        p = new Parm();
        p.setParmName("threshold");
        v = new Value();
        v.setContent(Double.toString(threshold));
        p.setValue(v);
        parms.addParm(p);
        
        p = new Parm();
        p.setParmName("rearm");
        v = new Value();
        v.setContent(Double.toString(rearm));
        p.setValue(v);
        parms.addParm(p);

        e.setParms(parms);
        m_anticipator.anticipateEvent(e, true);
        m_anticipatedEvents.add(e);
    }
    
    void verifyEvents(int remainEvents) {
        if (remainEvents == 0) {
            List<Event> receivedList = m_anticipator.getAnticipatedEventsRecieved();
            log().info("verifyEvents: Anticipated=" + m_anticipatedEvents.size() + ", Received=" + receivedList.size());
            for (int i = 0; i < m_anticipatedEvents.size(); i++) {
                String anticipated = eventToString(m_anticipatedEvents.get(i));
                String received = eventToString(receivedList.get(i));
                log().info("verifyEvents: Anticipated " + anticipated);
                log().info("verifyEvents: Received    " + received);
                assertTrue(received.startsWith(anticipated));
            }
        }
        m_anticipator.verifyAnticipated(1000, 0, 0, remainEvents, 0);
    }
    
    public String eventToString(Event e) {
        StringBuffer b = new StringBuffer();
        b.append(e.getUei());
        b.append(";");
        b.append(e.getNodeid());
        b.append(";");
        b.append(e.getInterface());
        b.append(";");
        b.append(e.getService());
        b.append(";");
        for (Parm p : e.getParms().getParm()) {
            b.append(p.getParmName() + "=" + p.getValue().getContent());
            b.append(";");
        }
        return b.toString();
    }
    
    void resetAnticipator() {
        m_anticipator.reset();
        m_anticipatedEvents.clear();
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
        network.addService("HTTP");
        MockDatabase db = new MockDatabase();
        db.populate(network);
        db.update("update snmpinterface set snmpifname=?, snmpifdescr=? where id=?", ifName, ifName, 1);
        DataSourceFactory.setInstance(db);
    }
    
    private void setupSnmpInterfaceWithoutIpDatabase(String ifName, int ifIndex, boolean skipUpdate) throws Exception {
        // Setup Non-IP Address Interface
        MockNetwork network = new MockNetwork();
        network.setCriticalService("ICMP");
        network.addNode(1, "testNode");
        network.addInterface("127.0.0.1");
        network.setIfAlias("eth0");
        network.addService("ICMP");
        network.addService("SNMP");
        MockDatabase db = new MockDatabase();
        db.populate(network);
        // Updating SNMP ifData for eth0
        db.update("update snmpInterface set snmpifName=?, snmpifDescr=? where id=?", "eth0", "eth0", 1);
        // Adding non-IP Interface wlan0
        if (!skipUpdate)
            db.update("insert into snmpInterface (nodeID, ipAddr, snmpifAlias, snmpifName, snmpifDescr, snmpifIndex) values (?, ?, ?, ?, ?, ?)", 1, "0.0.0.0", ifName, ifName, ifName, ifIndex);
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
