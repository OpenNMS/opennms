
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.                                                            

//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

//For more information contact: 
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/

package org.opennms.netmgt.threshd;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.opennms.netmgt.collectd.AttributeGroupType;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.NodeInfo;
import org.opennms.netmgt.collectd.NodeResourceType;
import org.opennms.netmgt.collectd.NumericAttributeType;
import org.opennms.netmgt.collectd.OnmsSnmpCollection;
import org.opennms.netmgt.collectd.ServiceParameters;
import org.opennms.netmgt.collectd.SnmpAttributeType;
import org.opennms.netmgt.collectd.SnmpCollectionResource;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.dao.FilterDao;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockDataCollectionConfig;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.MockLogAppender;

/**
 * @author <a href="mailto:agalue@opennms.org>Alejandro Galue</a>
 *
 */
public class ThresholdingVisitorTest extends TestCase {

    ThresholdingVisitor m_visitor;
    FilterDao m_filterDao;
    EventAnticipator m_anticipator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

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
    
    @Override
    protected void tearDown() throws Exception {
        EasyMock.verify(m_filterDao);
        super.tearDown();
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        MockLogAppender.assertNoWarningsOrGreater();
    }

    public void testCreateVisitorWithoutProperEnabledIt() {
        Map<String,String> params = new HashMap<String,String>();
        ThresholdingVisitor visitor = ThresholdingVisitor.createThresholdingVisitor(1, "127.0.0.1", "SNMP", getRepository(), params);
        assertNull(visitor);
    }

    public void testCreateVisitor() {
        createVisitor();
    }

    public void testVisitResourceGaugeData() {
        addAnticipatedEvent("uei.opennms.org/threshold/highThresholdExceeded");
        ThresholdingVisitor visitor = createVisitor();
        runGaugeDataTest(visitor, 15000);
        m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
    }

    public void testVisitResourceCounterData() {
        ThresholdingVisitor visitor = createVisitor();

        CollectionAgent agent = createCollectionAgent();
        NodeResourceType resourceType = createNodeResourceType(agent);
        MibObject mibObject = createMibObject("counter", "freeMem");
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
    
    /**
     * This bug has not been replicated, but this code covers the apparent scenario, and can be adapted to match
     * any scenario which can actually replicate the reported issue
     * @throws Exception
     */
    public void testBug2746() throws Exception{
        initFactories("/threshd-configuration.xml","/test-thresholds-bug2746.xml");

        ThresholdingVisitor visitor = createVisitor();

        CollectionAgent agent = createCollectionAgent();
        NodeResourceType resourceType = createNodeResourceType(agent);
        MibObject mibObject = createMibObject("gauge", "bug2746");
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
    
    public void testReload() throws Exception {
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

    private ThresholdingVisitor createVisitor() {
        Map<String,String> params = new HashMap<String,String>();
        params.put("thresholding-enabled", "true");
        ThresholdingVisitor visitor = ThresholdingVisitor.createThresholdingVisitor(1, "127.0.0.1", "SNMP", getRepository(), params);
        assertNotNull(visitor);
        return visitor;
    }

    private void runGaugeDataTest(ThresholdingVisitor visitor, long value) {
        CollectionAgent agent = createCollectionAgent();
        NodeResourceType resourceType = createNodeResourceType(agent);        
        MibObject mibObject = createMibObject("gauge", "freeMem");
        SnmpAttributeType attributeType = new NumericAttributeType(resourceType, "default", mibObject, new AttributeGroupType("mibGroup", "ignore"));
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);
        resource.setAttributeValue(attributeType, SnmpUtils.getValueFactory().getCounter32(value));
        resource.visit(visitor);
        EasyMock.verify(agent);
    }

    private CollectionAgent createCollectionAgent() {
        CollectionAgent agent = EasyMock.createMock(CollectionAgent.class);
        EasyMock.expect(agent.getNodeId()).andReturn(1).anyTimes();
        EasyMock.expect(agent.getHostAddress()).andReturn("127.0.0.1").anyTimes();
        EasyMock.replay(agent);
        return agent;
    }

    private NodeResourceType createNodeResourceType(CollectionAgent agent) {
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, String>()), dataCollectionConfig);
        return new NodeResourceType(agent, collection);
    }

    private MibObject createMibObject(String type, String alias) {
        MibObject mibObject = new MibObject();
        mibObject.setOid(".1.1.1.1");
        mibObject.setAlias(alias);
        mibObject.setType(type);
        mibObject.setInstance("0");
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

}
