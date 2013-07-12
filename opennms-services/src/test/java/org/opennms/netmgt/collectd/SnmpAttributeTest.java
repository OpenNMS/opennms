/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.matches;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.opennms.core.test.MockPlatformTransactionManager;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.mock.MockDataCollectionConfig;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValueFactory;
import org.opennms.test.mock.EasyMockUtils;

public class SnmpAttributeTest extends TestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    private IpInterfaceDao m_ipInterfaceDao = m_mocks.createMock(IpInterfaceDao.class);

    // Cannot avoid this warning since there is no way to fetch the class object for an interface
    // that uses generics
    @SuppressWarnings("unchecked")
    private RrdStrategy<Object, Object> m_rrdStrategy = m_mocks.createMock(RrdStrategy.class);

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        RrdUtils.setStrategy(m_rrdStrategy);
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();

        m_mocks.verifyAll();
    }

    public void testNumericAttributeFloatValueInString() throws Exception {
        String stringValue = "7.69";
        testPersisting(stringValue, new Snmp4JValueFactory().getOctetString(stringValue.getBytes()));
    }

    public void testNumericAttributeCounterValue() throws Exception {
        int intValue = 769;
        testPersisting(Integer.toString(intValue), new Snmp4JValueFactory().getCounter32(intValue));
    }

    public void testHexStringProtoCounter64ValueSmall() throws Exception {
        testPersisting("769", new Snmp4JValueFactory().getOctetString(new byte[]{ 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x03, 0x01 }));
    }

    public void testHexStringProtoCounter64ValueLT2_31() throws Exception {
        testPersisting("2000000000", new Snmp4JValueFactory().getOctetString(new byte[]{ 0x00, 0x00, 0x00, 0x00, 0x77, 0x35, (byte)0x94, 0x00 }));
    }

    public void testHexStringProtoCounter64ValueGT2_31() throws Exception {
        testPersisting("5000000000", new Snmp4JValueFactory().getOctetString(new byte[]{ 0x00, 0x00, 0x00, 0x01, 0x2a, 0x05, (byte)0xf2, 0x00 }));
    }

    public void testHexStringProtoCounter64ValueNear2_63() throws Exception {
        testPersisting("9223372036854775000", new Snmp4JValueFactory().getOctetString(new byte[]{ 0x7f, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xfc, (byte)0xd8 }));
    }

    public void testNumericAttributeHexStringValueInString() throws Exception {
        String stringValue = "769";
        byte[] bytes = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x01 };
        testPersisting(stringValue, new Snmp4JValueFactory().getOctetString(bytes));
    }

    @SuppressWarnings("unchecked")
    private void testPersisting(String matchValue, SnmpValue snmpValue) throws Exception {
        OnmsNode node = new OnmsNode();
        node.setId(3);

        OnmsIpInterface ipInterface = new OnmsIpInterface();
        ipInterface.setId(1);
        ipInterface.setNode(node);
        ipInterface.setIpAddress(InetAddressUtils.addr("192.168.1.1"));

        expect(m_ipInterfaceDao.load(1)).andReturn(ipInterface).times(3);

        expect(m_rrdStrategy.getDefaultFileExtension()).andReturn(".myLittleEasyMockedStrategyAndMe").anyTimes();
        expect(m_rrdStrategy.createDefinition(isA(String.class), isA(String.class), isA(String.class), anyInt(), isAList(RrdDataSource.class), isAList(String.class))).andReturn(new Object());

        m_rrdStrategy.createFile(isA(Object.class), (Map<String, String>) isNull());

        expect(m_rrdStrategy.openFile(isA(String.class))).andReturn(new Object());
        m_rrdStrategy.updateFile(isA(Object.class), isA(String.class), matches(".*:" + matchValue));
        m_rrdStrategy.closeFile(isA(Object.class));

        m_mocks.replayAll();

        CollectionAgent agent = DefaultCollectionAgent.create(ipInterface.getId(), m_ipInterfaceDao, new MockPlatformTransactionManager());
        OnmsSnmpCollection snmpCollection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), new MockDataCollectionConfig());
        NodeResourceType resourceType = new NodeResourceType(agent, snmpCollection);
        NodeInfo nodeInfo = resourceType.getNodeInfo();
        

        MibObject mibObject = new MibObject();
        mibObject.setOid(".1.3.6.1.4.1.12238.55.9997.4.1.2.9.116.101.109.112.95.117.108.107.111");
        mibObject.setInstance("1");
        mibObject.setAlias("temp_ulko");
        mibObject.setType("gauge");

        NumericAttributeType attributeType = new NumericAttributeType(resourceType, snmpCollection.getName(), mibObject, new AttributeGroupType("foo", "ignore"));

        attributeType.storeResult(new SnmpCollectionSet(agent, snmpCollection), null, new SnmpResult(mibObject.getSnmpObjId(), new SnmpInstId(mibObject.getInstance()), snmpValue));
        

        RrdRepository repository = new RrdRepository();
        repository.setRraList(new ArrayList<String>(Collections.singleton("RRA:AVERAGE:0.5:1:2016")));

        final BasePersister persister = new BasePersister(new ServiceParameters(new HashMap<String, Object>()), repository);
        persister.createBuilder(nodeInfo, "baz", attributeType);
        
        final AtomicInteger count = new AtomicInteger(0);
        
        nodeInfo.visit(new AbstractCollectionSetVisitor() {
			
			@Override
			public void visitAttribute(CollectionAttribute attr) {
		        attr.storeAttribute(persister);
		        count.incrementAndGet();
			}
			
		});

        assertEquals(1, count.get());
        persister.commitBuilder();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> isAList(Class<T> clazz) {
        return isA(List.class);
    }
}
