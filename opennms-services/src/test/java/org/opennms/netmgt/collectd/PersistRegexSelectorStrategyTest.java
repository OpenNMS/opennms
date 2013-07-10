/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.easymock.EasyMock;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.opennms.core.test.MockPlatformTransactionManager;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.config.datacollection.Parameter;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValueFactory;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Test class for PersistRegexSelectorStrategy
 * 
 * @author <a href="mail:agalue@opennms.org">Alejandro Galue</a>
 */
public class PersistRegexSelectorStrategyTest {

    private IpInterfaceDao ipInterfaceDao;
    private GenericIndexResource resourceA;
    private GenericIndexResource resourceB;
    private ServiceParameters serviceParams;

    @Before
    public void setUp() throws Exception {
        ipInterfaceDao = EasyMock.createMock(IpInterfaceDao.class);
        String localhost = InetAddress.getLocalHost().getHostAddress();
        OnmsDistPoller distPoller = new OnmsDistPoller("localhost", localhost);
        NetworkBuilder builder = new NetworkBuilder(distPoller);
        builder.addNode("myNode");
        builder.addInterface(localhost).setIsManaged("M").setIsSnmpPrimary("P");
        OnmsNode node = builder.getCurrentNode();
        node.setId(1);
        OnmsIpInterface ipInterface = node.getIpInterfaces().iterator().next();
        EasyMock.expect(ipInterfaceDao.load(1)).andReturn(ipInterface).anyTimes();
        EasyMock.replay(ipInterfaceDao);

        Package pkg = new Package();
        pkg.setName("junitTestPackage");
        Filter filter = new Filter();
        filter.setContent("IPADDR != '0.0.0.0'");
        pkg.setFilter(filter);
        Service service = new Service();
        service.setName("SNMP");
        pkg.addService(service);
        CollectdPackage collectdPkg = new CollectdPackage(pkg, "localhost", false);
        Map<String, Object> map = new TreeMap<String, Object>();
        List<org.opennms.netmgt.config.collectd.Parameter> params = collectdPkg.getService("SNMP").getParameterCollection();
        for (org.opennms.netmgt.config.collectd.Parameter p : params) {
            map.put(p.getKey(), p.getValue());
        }
        map.put("collection", "default");
        serviceParams = new ServiceParameters(map);

        PlatformTransactionManager ptm = new MockPlatformTransactionManager();
        CollectionAgent agent = DefaultCollectionAgent.create(1, ipInterfaceDao, ptm);
        OnmsSnmpCollection snmpCollection = new OnmsSnmpCollection(agent, serviceParams);

        org.opennms.netmgt.config.datacollection.ResourceType rt = new org.opennms.netmgt.config.datacollection.ResourceType();
        rt.setName("myResourceType");
        StorageStrategy storageStrategy = new StorageStrategy();
        storageStrategy.setClazz("org.opennms.netmgt.dao.support.IndexStorageStrategy");
        rt.setStorageStrategy(storageStrategy);
        PersistenceSelectorStrategy persistenceSelectorStrategy = new PersistenceSelectorStrategy();
        persistenceSelectorStrategy.setClazz("org.opennms.netmgt.collectd.PersistRegexSelectorStrategy");
        Parameter param = new Parameter();
        param.setKey(PersistRegexSelectorStrategy.MATCH_EXPRESSION);
        param.setValue("#name matches '^agalue.*$'");
        persistenceSelectorStrategy.addParameter(param);
        rt.setPersistenceSelectorStrategy(persistenceSelectorStrategy);
        GenericIndexResourceType resourceType = new GenericIndexResourceType(agent, snmpCollection, rt);

        resourceA = new GenericIndexResource(resourceType, rt.getName(), new SnmpInstId("1.2.3.4.5.6.7.8.9.1.1"));
        
        AttributeGroupType groupType = new AttributeGroupType("mib2-interfaces", "all");
        MibObject mibObject = new MibObject();
        mibObject.setOid(".1.2.3.4.5.6.7.8.9.2.1");
        mibObject.setInstance("1");
        mibObject.setAlias("name");
        mibObject.setType("string");
        StringAttributeType attributeType = new StringAttributeType(resourceType, snmpCollection.getName(), mibObject, groupType);
        SnmpValue snmpValue = new Snmp4JValueFactory().getOctetString("agalue rules!".getBytes());
        resourceA.setAttributeValue(attributeType, snmpValue);
        
        resourceB = new GenericIndexResource(resourceType, rt.getName(), new SnmpInstId("1.2.3.4.5.6.7.8.9.1.2"));
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(ipInterfaceDao);
    }

    @Test
    public void testPersistSelector() throws Exception {
        Assert.assertTrue(resourceA.shouldPersist(serviceParams));
        Assert.assertFalse(resourceB.shouldPersist(serviceParams));
    }

    @Test
    public void testSpringEl() throws Exception {
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression("#name matches '^Alejandro.*'");
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("name", "Alejandro Galue");
        boolean result = exp.getValue(context, Boolean.class);
        Assert.assertTrue(result);
    }
}
