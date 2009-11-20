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

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.resource.Vault;
import org.opennms.netmgt.collectd.AttributeGroupType;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.IfInfo;
import org.opennms.netmgt.collectd.IfResourceType;
import org.opennms.netmgt.collectd.NodeInfo;
import org.opennms.netmgt.collectd.NodeResourceType;
import org.opennms.netmgt.collectd.NumericAttributeType;
import org.opennms.netmgt.collectd.OnmsSnmpCollection;
import org.opennms.netmgt.collectd.ServiceParameters;
import org.opennms.netmgt.collectd.SnmpAttribute;
import org.opennms.netmgt.collectd.SnmpAttributeType;
import org.opennms.netmgt.collectd.SnmpCollectionResource;
import org.opennms.netmgt.collectd.SnmpIfData;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.mock.MockDataCollectionConfig;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.test.mock.MockLogAppender;

/**
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 *
 */
public class CollectionResourceWrapperTest {

    Level m_defaultErrorLevelToCheck;
    
    @Before
    public void setUp() throws Exception {
        CollectionResourceWrapper.s_cache.clear();
        m_defaultErrorLevelToCheck = Level.WARN;
        MockLogAppender.setupLogging();
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNotGreaterOrEqual(m_defaultErrorLevelToCheck);
    }
    
    @Test
    public void testGetGaugeValue() throws Exception {
        // Create Resource
        CollectionAgent agent = createCollectionAgent();
        SnmpCollectionResource resource = createNodeResource(agent);
        
        // Add Gauge Attribute
        Map<String, CollectionAttribute> attributes = new HashMap<String, CollectionAttribute>();
        SnmpAttribute attribute = addAttributeToCollectionResource(resource, "myGauge", "gauge", "0", 100);
        attributes.put(attribute.getName(), attribute);

        // Create Wrapper
        CollectionResourceWrapper wrapper = createWrapper(resource, attributes);
        
        // Get gauge value 3 times
        Assert.assertEquals(100.0, wrapper.getAttributeValue("myGauge"));
        Assert.assertEquals(100.0, wrapper.getAttributeValue("myGauge"));
        Assert.assertEquals(100.0, wrapper.getAttributeValue("myGauge"));
        
        EasyMock.verify(agent);
    }
    
    @Test
    public void testGetCounterValue() throws Exception {
        // Create Resource
        CollectionAgent agent = createCollectionAgent();
        SnmpCollectionResource resource = createNodeResource(agent);

        // Add Counter Attribute
        String attributeName = "myCounter";
        String attributeId = "node[1]." + attributeName;
        Map<String, CollectionAttribute> attributes = new HashMap<String, CollectionAttribute>();
        SnmpAttribute attribute = addAttributeToCollectionResource(resource, attributeName, "counter", "0", 1000);
        attributes.put(attribute.getName(), attribute);
        
        // Get counter value - first time
        CollectionResourceWrapper wrapper = createWrapper(resource, attributes);
        Assert.assertFalse(CollectionResourceWrapper.s_cache.containsKey(attributeId));
        Assert.assertEquals(Double.NaN, wrapper.getAttributeValue(attributeName)); // Last value is null
        Assert.assertEquals(Double.NaN, wrapper.getAttributeValue(attributeName)); // Last value is null
        Assert.assertEquals(1000.0, CollectionResourceWrapper.s_cache.get(attributeId));

        // Increase counter
        attribute = addAttributeToCollectionResource(resource, attributeName, "counter", "0", 2500);
        attributes.put(attribute.getName(), attribute);
        wrapper = createWrapper(resource, attributes);

        // Get counter value - second time
        // Last value is 1000.0, so 2500-1000/300 = 1500/300 =  5;
        Assert.assertEquals(1000.0, CollectionResourceWrapper.s_cache.get(attributeId));
        Assert.assertEquals(5.0, wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(2500.0, CollectionResourceWrapper.s_cache.get(attributeId));
        Assert.assertEquals(5.0, wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(2500.0, CollectionResourceWrapper.s_cache.get(attributeId));
        Assert.assertEquals(5.0, wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(2500.0, CollectionResourceWrapper.s_cache.get(attributeId));

        // Increase counter
        attribute = addAttributeToCollectionResource(resource, attributeName, "counter", "0", 5500);
        attributes.put(attribute.getName(), attribute);
        wrapper = createWrapper(resource, attributes);

        // Get counter value - third time
        // Last value is 2500.0, so 5500-2500/300 = 3000/300 =  10;
        Assert.assertEquals(2500.0, CollectionResourceWrapper.s_cache.get(attributeId));
        Assert.assertEquals(10.0, wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(5500.0, CollectionResourceWrapper.s_cache.get(attributeId));
        Assert.assertEquals(10.0, wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(5500.0, CollectionResourceWrapper.s_cache.get(attributeId));
        Assert.assertEquals(10.0, wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(5500.0, CollectionResourceWrapper.s_cache.get(attributeId));

        EasyMock.verify(agent);
    }

    @Test
    public void testGetCounterValueWithWrap() throws Exception {
        // Create Resource
        CollectionAgent agent = createCollectionAgent();
        SnmpCollectionResource resource = createNodeResource(agent);

        // Add Counter Attribute
        String attributeName = "myCounter";
        String attributeId = "node[1]." + attributeName;
        Map<String, CollectionAttribute> attributes = new HashMap<String, CollectionAttribute>();
        BigInteger initialValue = new BigDecimal(Math.pow(2, 32) - 20000).toBigInteger();
        SnmpAttribute attribute = addAttributeToCollectionResource(resource, attributeName, "counter", "0", initialValue);
        attributes.put(attribute.getName(), attribute);
        
        // Get counter value - first time
        CollectionResourceWrapper wrapper = createWrapper(resource, attributes);
        Assert.assertFalse(CollectionResourceWrapper.s_cache.containsKey(attributeId));
        Assert.assertEquals(Double.NaN, wrapper.getAttributeValue(attributeName)); // Last value is null
        Assert.assertEquals(Double.NaN, wrapper.getAttributeValue(attributeName)); // Last value is null
        Assert.assertEquals(initialValue.doubleValue(), CollectionResourceWrapper.s_cache.get(attributeId));

        // Increase counter
        attribute = addAttributeToCollectionResource(resource, attributeName, "counter", "0", new BigInteger("40000"));
        attributes.put(attribute.getName(), attribute);
        wrapper = createWrapper(resource, attributes);

        // Get counter value - second time (wrap)
        // last = MAX - 20000, new = 40000; then last - new = 60000, rate: 60000/300 = 200
        Assert.assertEquals(initialValue.doubleValue(), CollectionResourceWrapper.s_cache.get(attributeId));
        Assert.assertEquals(200.0, wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(40000.0, CollectionResourceWrapper.s_cache.get(attributeId));
        Assert.assertEquals(200.0, wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(40000.0, CollectionResourceWrapper.s_cache.get(attributeId));
        Assert.assertEquals(200.0, wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(40000.0, CollectionResourceWrapper.s_cache.get(attributeId));

        EasyMock.verify(agent);
    }
    
    @Test
    public void testInterfaceResource() throws Exception {
        // Set Defaults
        String ipAddress = "10.0.0.1";
        String ifName = "eth0";
        int ifIndex = 2;
        
        // Initialize Database
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
        db.update("update snmpinterface set snmpifindex=?, snmpifname=?, snmpifdescr=? where id=?", ifIndex, ifName, ifName, 1);
        DataSourceFactory.setInstance(db);
        Vault.setDataSource(db);
        
        // Create SnmpIfData
        OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("testNode");
        OnmsSnmpInterface snmpIface = new OnmsSnmpInterface(ipAddress, ifIndex, node);
        snmpIface.setIfDescr(ifName);
        snmpIface.setIfName(ifName);
        snmpIface.setIfAlias(ifName);
        snmpIface.setIfSpeed(10000000l);
        SnmpIfData ifData = new SnmpIfData(snmpIface);

        // Creating IfResourceType
        CollectionAgent agent = createCollectionAgent();
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, String>()), dataCollectionConfig);
        IfResourceType resourceType = new IfResourceType(agent, collection);

        // Creating Resource
        SnmpCollectionResource resource = new IfInfo(resourceType, agent, ifData);
        SnmpAttribute attribute = addAttributeToCollectionResource(resource, "ifInOctets", "counter", "ifIndex", 5000);
        Map<String, CollectionAttribute> attributes = new HashMap<String, CollectionAttribute>();
        attributes.put(attribute.getName(), attribute);
        
        // Create Wrapper
        CollectionResourceWrapper wrapper = createWrapper(resource, attributes);

        // Validations
        Assert.assertEquals(node.getId().intValue(), wrapper.getNodeId());
        Assert.assertEquals(ipAddress, wrapper.getHostAddress());
        Assert.assertEquals("eth0", wrapper.getIfLabel());
        Assert.assertEquals("if", wrapper.getResourceTypeName());
        Assert.assertEquals("SNMP", wrapper.getServiceName());
        Assert.assertEquals(true, wrapper.isAnInterfaceResource());
        Assert.assertEquals(Integer.toString(ifIndex), wrapper.getInstance());
        Assert.assertEquals(Integer.toString(ifIndex), wrapper.getIfIndex());
        Assert.assertEquals(Integer.toString(ifIndex), wrapper.getIfIndex()); // IfLabel is called only once
        Assert.assertEquals(Integer.toString(ifIndex), wrapper.getIfIndex()); // IfLabel is called only once
        Assert.assertEquals("eth0", wrapper.getIfInfoValue("snmpifname"));  // IfLabel is called only once
    }
    
    private SnmpCollectionResource createNodeResource(CollectionAgent agent) {
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, String>()), dataCollectionConfig);
        NodeResourceType resourceType = new NodeResourceType(agent, collection);
        return new NodeInfo(resourceType, agent);
    }
    
    // Wrapper interval value for counter rates calculation should be expressed in seconds.
    private CollectionResourceWrapper createWrapper(SnmpCollectionResource resource, Map<String, CollectionAttribute> attributes) {
        CollectionResourceWrapper wrapper = new CollectionResourceWrapper(300, 1, "127.0.0.1", "SNMP", getRepository(), resource, attributes);
        return wrapper;
    }

    private CollectionAgent createCollectionAgent() {
        CollectionAgent agent = EasyMock.createMock(CollectionAgent.class);
        EasyMock.expect(agent.getNodeId()).andReturn(1).anyTimes();
        EasyMock.expect(agent.getHostAddress()).andReturn("127.0.0.1").anyTimes();
        EasyMock.expect(agent.getSnmpInterfaceInfo((IfResourceType)EasyMock.anyObject())).andReturn(new HashSet<IfInfo>()).anyTimes();
        EasyMock.replay(agent);
        return agent;
    }

    private SnmpAttribute addAttributeToCollectionResource(SnmpCollectionResource resource, String attributeName, String attributeType, String attributeInstance, long value) {
        MibObject object = createMibObject(attributeType, attributeName, attributeInstance);
        SnmpAttributeType objectType = new NumericAttributeType(resource.getResourceType(), "default", object, new AttributeGroupType("mibGroup", "ignore"));
        SnmpValue snmpValue = attributeType.equals("counter") ? SnmpUtils.getValueFactory().getCounter32(value) : SnmpUtils.getValueFactory().getGauge32(value);
        resource.setAttributeValue(objectType, snmpValue);
        return new SnmpAttribute(resource, objectType, snmpValue);
    }

    private SnmpAttribute addAttributeToCollectionResource(SnmpCollectionResource resource, String attributeName, String attributeType, String attributeInstance, BigInteger value) {
        MibObject object = createMibObject(attributeType, attributeName, attributeInstance);
        SnmpAttributeType objectType = new NumericAttributeType(resource.getResourceType(), "default", object, new AttributeGroupType("mibGroup", "ignore"));
        SnmpValue snmpValue = SnmpUtils.getValueFactory().getCounter64(value);
        resource.setAttributeValue(objectType, snmpValue);
        return new SnmpAttribute(resource, objectType, snmpValue);
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

}
