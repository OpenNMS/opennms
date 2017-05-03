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

package org.opennms.netmgt.collectd;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.matches;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.opennms.core.test.MockPlatformTransactionManager;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.persistence.rrd.RrdPersisterFactory;
import org.opennms.netmgt.collection.support.CollectionSetVisitorWrapper;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.mock.MockDataCollectionConfig;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValueFactory;
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.test.FileAnticipator;

public class SnmpAttributeTest {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    private IpInterfaceDao m_ipInterfaceDao = m_mocks.createMock(IpInterfaceDao.class);

    private FileAnticipator m_fileAnticipator = null;
    private File m_snmpDirectory = null;

    // Cannot avoid this warning since there is no way to fetch the class object for an interface
    // that uses generics
    @SuppressWarnings("unchecked")
    private RrdStrategy<Object, Object> m_rrdStrategy = m_mocks.createMock(RrdStrategy.class);

    private ResourceStorageDao m_resourceStorageDao = m_mocks.createMock(ResourceStorageDao.class);

    @Before
    public void setUp() throws IOException {
        m_fileAnticipator = new FileAnticipator();
    }

    @After
    public void tearDown() {
        m_mocks.verifyAll();
        m_fileAnticipator.deleteExpected();
        m_fileAnticipator.tearDown();
    }

    @Test
    public void testNumericAttributeFloatValueInString() throws Exception {
        String stringValue = "7.69";
        testPersisting(stringValue, new Snmp4JValueFactory().getOctetString(stringValue.getBytes()));
    }

    @Test
    public void testNumericAttributeCounterValue() throws Exception {
        int intValue = 769;
        testPersisting(Integer.toString(intValue), new Snmp4JValueFactory().getCounter32(intValue));
    }

    @Test
    public void testHexStringProtoCounter64ValueSmall() throws Exception {
        testPersisting("769", new Snmp4JValueFactory().getOctetString(new byte[]{ 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x03, 0x01 }));
    }

    @Test
    public void testHexStringProtoCounter64ValueLT2_31() throws Exception {
        testPersisting("2000000000", new Snmp4JValueFactory().getOctetString(new byte[]{ 0x00, 0x00, 0x00, 0x00, 0x77, 0x35, (byte)0x94, 0x00 }));
    }

    @Test
    public void testHexStringProtoCounter64ValueGT2_31() throws Exception {
        testPersisting("5000000000", new Snmp4JValueFactory().getOctetString(new byte[]{ 0x00, 0x00, 0x00, 0x01, 0x2a, 0x05, (byte)0xf2, 0x00 }));
    }

    @Test
    public void testHexStringProtoCounter64ValueNear2_63() throws Exception {
        testPersisting("9223372036854775000", new Snmp4JValueFactory().getOctetString(new byte[]{ 0x7f, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xfc, (byte)0xd8 }));
    }

    @Test
    public void testNumericAttributeHexStringValueInString() throws Exception {
        String stringValue = "769";
        byte[] bytes = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x01 };
        testPersisting(stringValue, new Snmp4JValueFactory().getOctetString(bytes));
    }

    @Ignore
    @SuppressWarnings("unchecked")
    private void testPersisting(String matchValue, SnmpValue snmpValue) throws Exception {
        OnmsNode node = new OnmsNode();
        node.setId(3);

        OnmsIpInterface ipInterface = new OnmsIpInterface();
        ipInterface.setId(1);
        ipInterface.setNode(node);
        ipInterface.setIpAddress(InetAddressUtils.addr("192.168.1.1"));

        expect(m_ipInterfaceDao.load(1)).andReturn(ipInterface).times(7); // It used to be 3, but I think it is more correct to use getStoreDir from DefaultCollectionAgentService on DefaultCollectionAgent (NMS-7516)

        expect(m_rrdStrategy.getDefaultFileExtension()).andReturn(".myLittleEasyMockedStrategyAndMe").anyTimes();
        expect(m_rrdStrategy.createDefinition(isA(String.class), isA(String.class), isA(String.class), anyInt(), isAList(RrdDataSource.class), isAList(String.class))).andReturn(new Object());

        m_rrdStrategy.createFile(isA(Object.class));

        expect(m_rrdStrategy.openFile(isA(String.class))).andReturn(new Object());
        m_rrdStrategy.updateFile(isA(Object.class), isA(String.class), matches(".*:" + matchValue));
        m_rrdStrategy.closeFile(isA(Object.class));

        m_mocks.replayAll();

        SnmpCollectionAgent agent = DefaultCollectionAgent.create(ipInterface.getId(), m_ipInterfaceDao, new MockPlatformTransactionManager());
        OnmsSnmpCollection snmpCollection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), new MockDataCollectionConfig());
        NodeResourceType resourceType = new NodeResourceType(agent, snmpCollection);
        NodeInfo nodeInfo = resourceType.getNodeInfo();
        

        MibObject mibObject = new MibObject();
        mibObject.setOid(".1.3.6.1.4.1.12238.55.9997.4.1.2.9.116.101.109.112.95.117.108.107.111");
        mibObject.setInstance("1");
        mibObject.setAlias("temp_ulko");
        mibObject.setType("gauge");

        NumericAttributeType attributeType = new NumericAttributeType(resourceType, snmpCollection.getName(), mibObject, new AttributeGroupType("foo", AttributeGroupType.IF_TYPE_IGNORE));

        attributeType.storeResult(new SnmpCollectionSet(agent, snmpCollection), null, new SnmpResult(mibObject.getSnmpObjId(), new SnmpInstId(mibObject.getInstance()), snmpValue));

        RrdRepository repository = createRrdRepository();
        repository.setRraList(Collections.singletonList("RRA:AVERAGE:0.5:1:2016"));

        RrdPersisterFactory persisterFactory = new RrdPersisterFactory();
        persisterFactory.setRrdStrategy(m_rrdStrategy);
        persisterFactory.setResourceStorageDao(m_resourceStorageDao);
        CollectionSetVisitor persister = persisterFactory.createPersister(new ServiceParameters(Collections.emptyMap()), repository);

        final AtomicInteger count = new AtomicInteger(0);
        
        nodeInfo.visit(new CollectionSetVisitorWrapper(persister) {
            @Override
            public void visitAttribute(CollectionAttribute attribute) {
                super.visitAttribute(attribute);
                count.incrementAndGet();
            }
        });

        assertEquals(1, count.get());
    }

    /**
     * @see http://issues.opennms.org/browse/NMS-6202
     */
    public void test8DigitDecimalNumericAttributeStringValue() throws Exception {
        String longValue = "49197860";
        testPersisting(
            new Double(longValue).toString(),
            new Snmp4JValueFactory().getOctetString(longValue.getBytes("UTF-8"))
        );
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> isAList(Class<T> clazz) {
        return isA(List.class);
    }

    private RrdRepository createRrdRepository() throws IOException {
        RrdRepository repository = new RrdRepository();
        repository.setRrdBaseDir(getSnmpRrdDirectory());
        repository.setHeartBeat(600);
        repository.setStep(300);
        repository.setRraList(Collections.singletonList("RRA:AVERAGE:0.5:1:100"));
        return repository;
    }

    private File getSnmpRrdDirectory() throws IOException {
        if (m_snmpDirectory == null) {
            m_snmpDirectory = m_fileAnticipator.tempDir("snmp"); 
        }
        return m_snmpDirectory;
    }

}
