/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.rpc.mock.MockRpcClientFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.netmgt.collectd.GenericIndexResource;
import org.opennms.netmgt.collectd.GenericIndexResourceType;
import org.opennms.netmgt.collectd.IfInfo;
import org.opennms.netmgt.collectd.IfResourceType;
import org.opennms.netmgt.collectd.NodeInfo;
import org.opennms.netmgt.collectd.NodeResourceType;
import org.opennms.netmgt.collectd.NumericAttributeType;
import org.opennms.netmgt.collectd.OnmsSnmpCollection;
import org.opennms.netmgt.collectd.SnmpAttribute;
import org.opennms.netmgt.collectd.SnmpAttributeType;
import org.opennms.netmgt.collectd.SnmpCollectionAgent;
import org.opennms.netmgt.collectd.SnmpCollectionResource;
import org.opennms.netmgt.collectd.SnmpIfData;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.opennms.netmgt.config.datacollection.Parameter;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.mock.MockDataCollectionConfig;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.snmp.proxy.common.LocationAwareSnmpClientRpcImpl;

/**
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 *
 */
public class CollectionResourceWrapperIT {
    private boolean m_ignoreWarnings = false;
    private LocationAwareSnmpClient m_locationAwareSnmpClient = new LocationAwareSnmpClientRpcImpl(new MockRpcClientFactory());

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        CollectionResourceWrapper.s_cache.clear();
        MockLogAppender.setupLogging();
    }

    @After
    public void tearDown() throws Exception {
        if (!m_ignoreWarnings ) MockLogAppender.assertNoWarningsOrGreater();
    }
    
    @Test
    public void testGetGaugeValue() throws Exception {
        // Create Resource
        SnmpCollectionAgent agent = createCollectionAgent();
        SnmpCollectionResource resource = createNodeResource(agent);
        
        // Add Gauge Attribute
        Map<String, CollectionAttribute> attributes = new HashMap<String, CollectionAttribute>();
        SnmpAttribute attribute = addAttributeToCollectionResource(resource, "myGauge", AttributeType.GAUGE, "0", "100");
        attributes.put(attribute.getName(), attribute);

        // Create Wrapper
        CollectionResourceWrapper wrapper = createWrapper(resource, attributes);
        
        // Get gauge value 3 times
        Assert.assertEquals(Double.valueOf(100.0), wrapper.getAttributeValue("myGauge"));
        Assert.assertEquals(Double.valueOf(100.0), wrapper.getAttributeValue("myGauge"));
        Assert.assertEquals(Double.valueOf(100.0), wrapper.getAttributeValue("myGauge"));
        
        EasyMock.verify(agent);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testBadConstructorCall() throws Throwable {
        try {
            new CollectionResourceWrapper(null, 1, "127.0.0.1", "HTTP", null, null, null, null);
        } catch (Throwable e) {
            //e.printStackTrace();
            throw e;
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadderConstructorCall() throws Throwable {
        try {
            new CollectionResourceWrapper(null, -1, null, null, null, null, null, null);
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetCounterValue() throws Exception {
        // Create Resource
        SnmpCollectionAgent agent = createCollectionAgent();
        SnmpCollectionResource resource = createNodeResource(agent);

        // Add Counter Attribute
        String attributeName = "myCounter";
        String attributeId = "node[1].resourceType[node].instance[null].metric[" + attributeName + "]";
        Map<String, CollectionAttribute> attributes = new HashMap<String, CollectionAttribute>();
        SnmpAttribute attribute = addAttributeToCollectionResource(resource, attributeName, AttributeType.COUNTER, "0", "1000");
        attributes.put(attribute.getName(), attribute);
        
        //We manipulate the Date objects passed to the CollectionResourceWrapper to simulate various collection intervals
        Date baseDate = new Date();
        
        // Get counter value - first time
        CollectionResourceWrapper wrapper = createWrapper(resource, attributes, baseDate);

        Assert.assertFalse(CollectionResourceWrapper.s_cache.containsKey(attributeId));
        Assert.assertEquals(Double.valueOf(Double.NaN), wrapper.getAttributeValue(attributeName)); // Last value is null
        Assert.assertEquals(Double.valueOf(Double.NaN), wrapper.getAttributeValue(attributeName)); // Last value is null
        Assert.assertEquals(Double.valueOf(1000.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());

        // Increase counter
        attribute = addAttributeToCollectionResource(resource, attributeName, AttributeType.COUNTER, "0", "2500");
        attributes.put(attribute.getName(), attribute);
        //Next wrapper is told the data was collected 5 minutes in the future (300 seconds)
        wrapper = createWrapper(resource, attributes, new Date(baseDate.getTime()+300000));
       
        // Get counter value - second time
        // Last value is 1000.0, so 2500-1000/300 = 1500/300 =  5.
        Assert.assertEquals(Double.valueOf(1000.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());
        Assert.assertEquals(Double.valueOf(5.0), wrapper.getAttributeValue(attributeName));
        //Validate that the cached counter value has been updated
        Assert.assertEquals(Double.valueOf(2500.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());
        //but that calling getAttributeValue doesn't re-calculate the rate inappropriately
        Assert.assertEquals(Double.valueOf(5.0), wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(Double.valueOf(2500.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());
        Assert.assertEquals(Double.valueOf(5.0), wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(Double.valueOf(2500.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());

        // Increase counter
        attribute = addAttributeToCollectionResource(resource, attributeName, AttributeType.COUNTER, "0", "5500");
        attributes.put(attribute.getName(), attribute);
        //Next wrapper is told the data was collected 10 minutes in the future (600 seconds), or after the first collection
        wrapper = createWrapper(resource, attributes, new Date(baseDate.getTime()+600000));

        // Get counter value - third time
        // Last value is 2500.0, so 5500-2500/300 = 3000/300 =  10;
        Assert.assertEquals(Double.valueOf(2500.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());
        Assert.assertEquals(Double.valueOf(10.0), wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(Double.valueOf(5500.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());
        Assert.assertEquals(Double.valueOf(10.0), wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(Double.valueOf(5500.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());
        Assert.assertEquals(Double.valueOf(10.0), wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(Double.valueOf(5500.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());
    }
        
    
     /**
      * Per bug report NMS-4244, multiple calls to the getCounter functionality on CollectionResourceWrapper used to pay
      * no mind of the actual time when samples were collected, instead assuming that it was the collection interval ago.
      * When a collection cycle fails (entirely or partly) and thresholded values weren't available, the counter value
      * was incorrectly calculated on the next succeeding cycle (taking the difference from the last successful 
      * collection and dividing by just a single collection interval.
      */
	@Test
	public void testGetCounterValueWithGap() throws Exception {
		m_ignoreWarnings = true; // we get a warning on the first getAttributeValue()

		SnmpCollectionAgent agent = createCollectionAgent();
		SnmpCollectionResource resource = createNodeResource(agent);

		// Add Counter Attribute
		String attributeName = "myCounter";
	        String attributeId = "node[1].resourceType[node].instance[null].metric[" + attributeName + "]";
		Map<String, CollectionAttribute> attributes = new HashMap<String, CollectionAttribute>();
		SnmpAttribute attribute = addAttributeToCollectionResource(resource, attributeName, AttributeType.COUNTER, "0", "1000");
		attributes.put(attribute.getName(), attribute);

		// We manipulate the Date objects passed to the
		// CollectionResourceWrapper to simulate various collection intervals
		Date baseDate = new Date();

		// Get counter value - first time
		CollectionResourceWrapper wrapper = createWrapper(resource, attributes,
				baseDate);

		Assert.assertFalse(CollectionResourceWrapper.s_cache
				.containsKey(attributeId));
		Assert.assertEquals(Double.valueOf(Double.NaN),
				wrapper.getAttributeValue(attributeName)); // Last value is null
		Assert.assertEquals(Double.valueOf(Double.NaN),
				wrapper.getAttributeValue(attributeName)); // Last value is null
		Assert.assertEquals(Double.valueOf(1000.0),
				CollectionResourceWrapper.s_cache.get(attributeId).getValue());

        // Increase counter
        attribute = addAttributeToCollectionResource(resource, attributeName, AttributeType.COUNTER, "0", "2500");
        attributes.put(attribute.getName(), attribute);
        //Next wrapper is told the data was collected 5 minutes in the future (300 seconds)
        wrapper = createWrapper(resource, attributes, new Date(baseDate.getTime()+300000));
       
        // Get counter value - second time
        // Last value is 1000.0, so 2500-1000/300 = 1500/300 =  5.
        Assert.assertEquals(Double.valueOf(1000.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());
        Assert.assertEquals(Double.valueOf(5.0), wrapper.getAttributeValue(attributeName));
        //Validate that the cached counter value has been updated
        Assert.assertEquals(Double.valueOf(2500.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());
        //but that calling getAttributeValue doesn't re-calculate the rate inappropriately or update the static cache
        Assert.assertEquals(Double.valueOf(5.0), wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(Double.valueOf(2500.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());

		// Now create a collection that is missing the counter value; we're
		// expecting null result and no cache updates
		attributes = new HashMap<String, CollectionAttribute>();
		attribute = addAttributeToCollectionResource(resource, "notMyCounter",
		        AttributeType.COUNTER, "0", "1000"); // We want a value, just not one called "myCounter"
		attributes.put(attribute.getName(), attribute);
		// Next collection is 10 minutes (600 seconds) after the first.
		wrapper = createWrapper(resource, attributes,
				new Date(baseDate.getTime() + 600000));

		// No change, so we expect the cache to have (and continue to) remain
		// the same, and to get no attribute value out
		Assert.assertEquals(Double.valueOf(2500.0),
				CollectionResourceWrapper.s_cache.get(attributeId).getValue());
		Assert.assertNull(wrapper.getAttributeValue(attributeName)); 
		Assert.assertEquals(Double.valueOf(2500.0),
				CollectionResourceWrapper.s_cache.get(attributeId).getValue());

		// Now if we collect successfully again, we expect the counter to be the
		// change divided by two collection cycles
		attributes = new HashMap<String, CollectionAttribute>();
		attribute = addAttributeToCollectionResource(resource, attributeName,
		        AttributeType.COUNTER, "0", "7300");
		attributes.put(attribute.getName(), attribute);
		
		// Next collection is 15 minutes (900 seconds) after the first.
		wrapper = createWrapper(resource, attributes,
				new Date(baseDate.getTime() + 900000));

		// Get counter value - fourth time
		// Last value is 5500, but we've had two collection cycles, so
		// 7300-2500/600 = 4800/600 = 8
		Assert.assertEquals(Double.valueOf(2500.0),
				CollectionResourceWrapper.s_cache.get(attributeId).getValue());
		Assert.assertEquals(Double.valueOf(8.0), wrapper.getAttributeValue(attributeName));
		Assert.assertEquals(Double.valueOf(7300.0),
				CollectionResourceWrapper.s_cache.get(attributeId).getValue());
		Assert.assertEquals(Double.valueOf(8.0), wrapper.getAttributeValue(attributeName));
		Assert.assertEquals(Double.valueOf(7300.0),
				CollectionResourceWrapper.s_cache.get(attributeId).getValue());
		Assert.assertEquals(Double.valueOf(8.0), wrapper.getAttributeValue(attributeName));
		Assert.assertEquals(Double.valueOf(7300.0),
				CollectionResourceWrapper.s_cache.get(attributeId).getValue());

		EasyMock.verify(agent);
	}

    @Test
    public void testGetCounterValueWithWrap() throws Exception {
        // Create Resource
        SnmpCollectionAgent agent = createCollectionAgent();
        SnmpCollectionResource resource = createNodeResource(agent);

		// We manipulate the Date objects passed to the
		// CollectionResourceWrapper to simulate various collection intervals
		Date baseDate = new Date();

        // Add Counter Attribute
        String attributeName = "myCounter";
        String attributeId = "node[1].resourceType[node].instance[null].metric[" + attributeName + "]";
        Map<String, CollectionAttribute> attributes = new HashMap<String, CollectionAttribute>();
        BigInteger initialValue = new BigDecimal(Math.pow(2, 32) - 20000).toBigInteger();
        SnmpAttribute attribute = addAttributeToCollectionResource(resource, attributeName, AttributeType.COUNTER, "0", initialValue);
        attributes.put(attribute.getName(), attribute);
        
        // Get counter value - first time
        CollectionResourceWrapper wrapper = createWrapper(resource, attributes, baseDate);
        Assert.assertFalse(CollectionResourceWrapper.s_cache.containsKey(attributeId));
        Assert.assertEquals(Double.valueOf(Double.NaN), wrapper.getAttributeValue(attributeName)); // Last value is null
        Assert.assertEquals(Double.valueOf(Double.NaN), wrapper.getAttributeValue(attributeName)); // Last value is null
        Assert.assertEquals(Double.valueOf(initialValue.doubleValue()), CollectionResourceWrapper.s_cache.get(attributeId).getValue());

        // Increase counter
        attribute = addAttributeToCollectionResource(resource, attributeName, AttributeType.COUNTER, "0", new BigInteger("40000"));
        attributes.put(attribute.getName(), attribute);
        wrapper = createWrapper(resource, attributes, new Date(baseDate.getTime() + 300000));

        // Get counter value - second time (wrap)
        // last = MAX - 20000, new = 40000; then last - new = 60000, rate: 60000/300 = 200
        Assert.assertEquals(Double.valueOf(initialValue.doubleValue()), CollectionResourceWrapper.s_cache.get(attributeId).getValue());
        Assert.assertEquals(Double.valueOf(200.0), wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(Double.valueOf(40000.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());
        Assert.assertEquals(Double.valueOf(200.0), wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(Double.valueOf(40000.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());
        Assert.assertEquals(Double.valueOf(200.0), wrapper.getAttributeValue(attributeName));
        Assert.assertEquals(Double.valueOf(40000.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());

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
        network.setIfIndex(ifIndex);
        network.addService("ICMP");
        network.addService("SNMP");
        network.addService("HTTP");
        MockDatabase db = new MockDatabase();
        db.populate(network);
        DataSourceFactory.setInstance(db);

        // Create Mock Collection Agent
        SnmpCollectionAgent agent = createCollectionAgent();

        // Create SnmpIfData
        OnmsNode node = new OnmsNode();
        node.setId(agent.getNodeId());
        node.setLabel("testNode");
        node.setForeignSource(agent.getForeignSource());
        node.setForeignId(agent.getForeignId());
        OnmsSnmpInterface snmpIface = new OnmsSnmpInterface(node, ifIndex);
        snmpIface.setIfDescr(ifName);
        snmpIface.setIfName(ifName);
        snmpIface.setIfAlias(ifName);
        snmpIface.setIfSpeed(10000000l);
        snmpIface.setPhysAddr("001122334455");
        SnmpIfData ifData = new SnmpIfData(snmpIface);

        // Creating IfResourceType
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), dataCollectionConfig, m_locationAwareSnmpClient);
        IfResourceType resourceType = new IfResourceType(agent, collection);

        // Creating Resource
        SnmpCollectionResource resource = new IfInfo(resourceType, agent, ifData);
        SnmpAttribute attribute = addAttributeToCollectionResource(resource, "ifInOctets", AttributeType.COUNTER, "ifIndex", "5000");
        Map<String, CollectionAttribute> attributes = new HashMap<String, CollectionAttribute>();
        attributes.put(attribute.getName(), attribute);
        
        // Create Wrapper
        CollectionResourceWrapper wrapper = createWrapper(resource, attributes);

        // Validations
        Assert.assertEquals(node.getId().intValue(), wrapper.getNodeId());
        Assert.assertEquals("127.0.0.1", wrapper.getHostAddress()); // Should be the address of the SNMP Agent (Bug 3808)
        Assert.assertEquals("eth0-001122334455", wrapper.getIfLabel());
        Assert.assertEquals("if", wrapper.getResourceTypeName());
        Assert.assertEquals("SNMP", wrapper.getServiceName());
        Assert.assertEquals(true, wrapper.isAnInterfaceResource());
        Assert.assertEquals(Integer.toString(ifIndex), wrapper.getInstance());
        Assert.assertEquals(Integer.toString(ifIndex), wrapper.getIfIndex());
        Assert.assertEquals(Integer.toString(ifIndex), wrapper.getIfIndex()); // IfLabel is called only once
        Assert.assertEquals(Integer.toString(ifIndex), wrapper.getIfIndex()); // IfLabel is called only once
        Assert.assertEquals("eth0", wrapper.getIfInfoValue("snmpifname"));  // IfLabel is called only once
        Assert.assertEquals("eth0-001122334455", wrapper.getInstanceLabel());
        Assert.assertEquals("nodeSource[JUnit:T001].interfaceSnmp[eth0-001122334455]", wrapper.getResourceId().toString());
    }

    @Test
    public void testGenericResource() throws Exception {
        SnmpCollectionAgent agent = createCollectionAgent();
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), dataCollectionConfig, m_locationAwareSnmpClient);
        ResourceType rt = new ResourceType();
        rt.setName("hrStorageIndex");
        rt.setLabel("host-resources storage");
        StorageStrategy strategy = new StorageStrategy();
        strategy.setClazz("org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy");
        strategy.addParameter(new Parameter("sibling-column-name", "hrStorageLabel"));
        strategy.addParameter(new Parameter("replace-all", "s/^-//"));
        rt.setStorageStrategy(strategy);
        PersistenceSelectorStrategy pstrategy = new PersistenceSelectorStrategy();
        pstrategy.setClazz("org.opennms.netmgt.collection.support.PersistAllSelectorStrategy");
        rt.setPersistenceSelectorStrategy(pstrategy);

        GenericIndexResourceType resourceType = new GenericIndexResourceType(agent, collection, rt);

        SnmpCollectionResource resource = new GenericIndexResource(resourceType, resourceType.getName(), new SnmpInstId(100));
        SnmpAttribute used = addAttributeToCollectionResource(resource, "hrStorageUsed", AttributeType.GAUGE, "hrStorageIndex", "5000");
        SnmpAttribute label = addAttributeToCollectionResource(resource, "hrStorageLabel", AttributeType.STRING, "hrStorageIndex", "/opt");
        Map<String, CollectionAttribute> attributes = new HashMap<String, CollectionAttribute>();
        attributes.put(used.getName(), used);
        attributes.put(label.getName(), label);

        CollectionResourceWrapper wrapper = createWrapper(resource, attributes);
        Assert.assertEquals("opt", wrapper.getInstanceLabel());
    }

    @Test
    public void testNumericFields() throws Exception {
        SnmpCollectionAgent agent = createCollectionAgent();
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), dataCollectionConfig, m_locationAwareSnmpClient);
        ResourceType rt = new ResourceType();
        rt.setName("dskIndex");
        rt.setLabel("Disk Table Index (UCD-SNMP MIB)");
        StorageStrategy strategy = new StorageStrategy();
        strategy.setClazz("org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy");
        strategy.addParameter(new Parameter("sibling-column-name", "ns-dskPath"));
        strategy.addParameter(new Parameter("replace-first", "s/^-$/_root_fs/"));
        strategy.addParameter(new Parameter("replace-all", "s/^-//"));
        strategy.addParameter(new Parameter("replace-all", "s/\\s//"));
        strategy.addParameter(new Parameter("replace-all","s/:\\\\.*//"));
        rt.setStorageStrategy(strategy);
        PersistenceSelectorStrategy pstrategy = new PersistenceSelectorStrategy();
        pstrategy.setClazz("org.opennms.netmgt.collection.support.PersistAllSelectorStrategy");
        rt.setPersistenceSelectorStrategy(pstrategy);

        GenericIndexResourceType resourceType = new GenericIndexResourceType(agent, collection, rt);

        SnmpCollectionResource resource = new GenericIndexResource(resourceType, resourceType.getName(), new SnmpInstId(100));
        SnmpAttribute total = addAttributeToCollectionResource(resource, "ns-dskTotal", AttributeType.GAUGE, "dskIndex", "10000");
        SnmpAttribute used = addAttributeToCollectionResource(resource, "ns-dskUsed", AttributeType.GAUGE, "dskIndex", "5000");
        SnmpAttribute label = addAttributeToCollectionResource(resource, "ns-dskPath", AttributeType.STRING, "dskIndex", "/opt");
        Map<String, CollectionAttribute> attributes = new HashMap<String, CollectionAttribute>();
        attributes.put(used.getName(), used);
        attributes.put(total.getName(), total);
        attributes.put(label.getName(), label);

        CollectionResourceWrapper wrapper = createWrapper(resource, attributes);
        Assert.assertEquals("opt", wrapper.getInstanceLabel());
        Assert.assertEquals(new Double("10000.0"), wrapper.getAttributeValue(total.getName()));
        Assert.assertEquals("10000.0", wrapper.getFieldValue(total.getName()));
    }

    @Test
    public void testCounterReset_NMS7106() throws Exception {
        // Create Resource
        SnmpCollectionAgent agent = createCollectionAgent();
        SnmpCollectionResource resource = createNodeResource(agent);
        Date baseDate = new Date();

        // Add Counter Attribute
        System.err.println("------------------------");
        String attributeName = "myCounter";
        String attributeId = "node[1].resourceType[node].instance[null].metric[" + attributeName + "]";
        Map<String, CollectionAttribute> attributes = new HashMap<String, CollectionAttribute>();
        BigInteger initialValue = new BigInteger("300");
        SnmpAttribute attribute = addAttributeToCollectionResource(resource, attributeName, AttributeType.COUNTER, "0", initialValue);
        attributes.put(attribute.getName(), attribute);

        // Get counter value - first time
        CollectionResourceWrapper wrapper = createWrapper(resource, attributes, baseDate);
        Assert.assertFalse(CollectionResourceWrapper.s_cache.containsKey(attributeId));
        Assert.assertEquals(Double.valueOf(Double.NaN), wrapper.getAttributeValue(attributeName)); // Last value is null
        Assert.assertEquals(Double.valueOf(Double.NaN), wrapper.getAttributeValue(attributeName)); // Last value is null
        Assert.assertEquals(Double.valueOf(initialValue.doubleValue()), CollectionResourceWrapper.s_cache.get(attributeId).getValue());
        Assert.assertTrue(wrapper.getAttributeValue(attributeName).isNaN());

        // Increase counter
        attribute = addAttributeToCollectionResource(resource, attributeName, AttributeType.COUNTER, "0", new BigInteger("600"));
        attributes.put(attribute.getName(), attribute);
        wrapper = createWrapper(resource, attributes, new Date(baseDate.getTime() + 300000));
        Assert.assertFalse(CollectionResourceWrapper.s_cache.get(attributeId).getValue().isNaN());
        Assert.assertEquals(Double.valueOf(300.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());
        Assert.assertEquals(Double.valueOf(1.0), wrapper.getAttributeValue(attributeName)); // 600 - 300 / 300 = 1.0

        // Increase counter again
        attribute = addAttributeToCollectionResource(resource, attributeName, AttributeType.COUNTER, "0", new BigInteger("900"));
        attributes.put(attribute.getName(), attribute);
        wrapper = createWrapper(resource, attributes, new Date(baseDate.getTime() + 600000));
        Assert.assertFalse(CollectionResourceWrapper.s_cache.get(attributeId).getValue().isNaN());
        Assert.assertEquals(Double.valueOf(600.0), CollectionResourceWrapper.s_cache.get(attributeId).getValue());
        Assert.assertEquals(Double.valueOf(1.0), wrapper.getAttributeValue(attributeName)); // 900 - 600 / 300 = 1.0

        // Emulate a sysUpTime restart
        attribute = addAttributeToCollectionResource(resource, attributeName, AttributeType.COUNTER, "0", new BigInteger("60"));
        attributes.put(attribute.getName(), attribute);
        wrapper = createWrapper(resource, attributes, new Date(baseDate.getTime() + 900000));
        wrapper.setCounterReset(true);
        Assert.assertTrue(wrapper.getAttributeValue(attributeName).isNaN());

        // Increase counter again
        attribute = addAttributeToCollectionResource(resource, attributeName, AttributeType.COUNTER, "0", new BigInteger("120"));
        attributes.put(attribute.getName(), attribute);
        wrapper = createWrapper(resource, attributes, new Date(baseDate.getTime() + 1200000));
        Assert.assertEquals(Double.valueOf(0.2), wrapper.getAttributeValue(attributeName)); // 120 - 60 / 300 = 0.2

        EasyMock.verify(agent);
    }

    private SnmpCollectionResource createNodeResource(SnmpCollectionAgent agent) {
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), dataCollectionConfig, m_locationAwareSnmpClient);
        NodeResourceType resourceType = new NodeResourceType(agent, collection);
        return new NodeInfo(resourceType, agent);
    }
    
    // Wrapper interval value for counter rates calculation should be expressed in seconds.
    private CollectionResourceWrapper createWrapper(SnmpCollectionResource resource, Map<String, CollectionAttribute> attributes, Date timestamp) {
        CollectionResourceWrapper wrapper = new CollectionResourceWrapper(timestamp, 1, "127.0.0.1", "SNMP", getRepository(), resource, attributes, getResourceStorageDao());
        return wrapper;    	
    }
    
    private CollectionResourceWrapper createWrapper(SnmpCollectionResource resource, Map<String, CollectionAttribute> attributes) {
    	return this.createWrapper(resource, attributes, new Date());
    }

    private SnmpCollectionAgent createCollectionAgent() {
        SnmpCollectionAgent agent = EasyMock.createMock(SnmpCollectionAgent.class);
        EasyMock.expect(agent.getNodeId()).andReturn(1).anyTimes();
        EasyMock.expect(agent.getHostAddress()).andReturn("127.0.0.1").anyTimes();
        EasyMock.expect(agent.getSnmpInterfaceInfo((IfResourceType)EasyMock.anyObject())).andReturn(new HashSet<IfInfo>()).anyTimes();
        EasyMock.expect(agent.getForeignSource()).andReturn("JUnit").anyTimes();
        EasyMock.expect(agent.getForeignId()).andReturn("T001").anyTimes();
        EasyMock.expect(agent.getStorageResourcePath()).andReturn(ResourcePath.get(ResourceTypeUtils.FOREIGN_SOURCE_DIRECTORY, "JUnit", "T001")).anyTimes();
        EasyMock.replay(agent);
        return agent;
    }

    private SnmpAttribute addAttributeToCollectionResource(SnmpCollectionResource resource, String attributeName, AttributeType attributeType, String attributeInstance, String value) {
        MibObject object = createMibObject(attributeType, attributeName, attributeInstance);
        SnmpAttributeType objectType = null;

        SnmpValue snmpValue = null;
        if (AttributeType.STRING.equals(attributeType)) {
            objectType = new SnmpAttributeType(resource.getResourceType(), "default", object, new AttributeGroupType("mibGroup", AttributeGroupType.IF_TYPE_IGNORE)) {
                @Override
                public AttributeType getType() {
                    return AttributeType.STRING;
                }

                @Override
                public void storeAttribute(CollectionAttribute attribute, Persister persister) {
                    persister.persistStringAttribute(attribute);
                }
            };
            snmpValue = SnmpUtils.getValueFactory().getOctetString(value.getBytes());
        } else {
            objectType = new NumericAttributeType(resource.getResourceType(), "default", object, new AttributeGroupType("mibGroup", AttributeGroupType.IF_TYPE_IGNORE));
            long v = Long.parseLong(value);
            snmpValue = AttributeType.COUNTER.equals(attributeType) ? SnmpUtils.getValueFactory().getCounter32(v) : SnmpUtils.getValueFactory().getGauge32(v);
        }
        resource.setAttributeValue(objectType, snmpValue);
        return new SnmpAttribute(resource, objectType, snmpValue);
    }

    private SnmpAttribute addAttributeToCollectionResource(SnmpCollectionResource resource, String attributeName, AttributeType attributeType, String attributeInstance, BigInteger value) {
        MibObject object = createMibObject(attributeType, attributeName, attributeInstance);
        SnmpAttributeType objectType = new NumericAttributeType(resource.getResourceType(), "default", object, new AttributeGroupType("mibGroup", AttributeGroupType.IF_TYPE_IGNORE));
        SnmpValue snmpValue = SnmpUtils.getValueFactory().getCounter64(value);
        resource.setAttributeValue(objectType, snmpValue);
        return new SnmpAttribute(resource, objectType, snmpValue);
    }

    private MibObject createMibObject(AttributeType type, String alias, String instance) {
        MibObject mibObject = new MibObject();
        mibObject.setOid(".1.1.1.1");
        mibObject.setAlias(alias);
        mibObject.setType(type.toString());
        mibObject.setInstance(instance);
        mibObject.setMaxval(null);
        mibObject.setMinval(null);
        return mibObject;
    }

    private RrdRepository getRepository() {
        RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(tempFolder.getRoot());
        return repo;		
    }

    private ResourceStorageDao getResourceStorageDao() {
        FilesystemResourceStorageDao resourceStorageDao = new FilesystemResourceStorageDao();
        resourceStorageDao.setRrdDirectory(tempFolder.getRoot());
        return resourceStorageDao;
    }
}
