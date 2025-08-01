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
package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.rpc.mock.MockRpcClientFactory;
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
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.mock.MockDataCollectionConfig;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.snmp.proxy.common.LocationAwareSnmpClientRpcImpl;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValueFactory;
import org.opennms.test.FileAnticipator;

public class SnmpAttributeTest {
    private IpInterfaceDao m_ipInterfaceDao = mock(IpInterfaceDao.class);

    private FileAnticipator m_fileAnticipator = null;
    private File m_snmpDirectory = null;

    // Cannot avoid this warning since there is no way to fetch the class object for an interface
    // that uses generics
    @SuppressWarnings("unchecked")
    private RrdStrategy<Object, Object> m_rrdStrategy = mock(RrdStrategy.class);

    private FilesystemResourceStorageDao m_resourceStorageDao = mock(FilesystemResourceStorageDao.class);

    private LocationAwareSnmpClient m_locationAwareSnmpClient = new LocationAwareSnmpClientRpcImpl(new MockRpcClientFactory());

    @Before
    public void setUp() throws IOException {
        m_fileAnticipator = new FileAnticipator();
    }

    @After
    public void tearDown() {
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
    private void testPersisting(String matchValue, SnmpValue snmpValue) throws Exception {
        OnmsNode node = new OnmsNode();
        node.setId(3);

        OnmsIpInterface ipInterface = new OnmsIpInterface();
        ipInterface.setId(1);
        ipInterface.setNode(node);
        ipInterface.setIpAddress(InetAddressUtils.addr("192.168.1.1"));

        when(m_ipInterfaceDao.load(1)).thenReturn(ipInterface); // It used to be 3, but I think it is more correct to use getStoreDir from DefaultCollectionAgentService on DefaultCollectionAgent (NMS-7516)

        when(m_rrdStrategy.getDefaultFileExtension()).thenReturn(".myLittleMockedStrategyAndMe");
        when(m_rrdStrategy.createDefinition(isA(String.class), isA(String.class), isA(String.class), anyInt(), isAList(RrdDataSource.class), isAList(String.class))).thenReturn(new Object());

        m_rrdStrategy.createFile(isA(Object.class));

        when(m_rrdStrategy.openFile(isA(String.class))).thenReturn(new Object());
        m_rrdStrategy.updateFile(isA(Object.class), isA(String.class), matches(".*:" + matchValue));
        m_rrdStrategy.closeFile(isA(Object.class));

        SnmpCollectionAgent agent = DefaultSnmpCollectionAgent.create(ipInterface.getId(), m_ipInterfaceDao, new MockPlatformTransactionManager());
        OnmsSnmpCollection snmpCollection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), new MockDataCollectionConfig(), m_locationAwareSnmpClient);
        NodeResourceType resourceType = new NodeResourceType(agent, snmpCollection);
        NodeInfo nodeInfo = resourceType.getNodeInfo();
        

        MibObject mibObject = new MibObject();
        mibObject.setOid(".1.3.6.1.4.1.12238.55.9997.4.1.2.9.116.101.109.112.95.117.108.107.111");
        mibObject.setInstance("1");
        mibObject.setAlias("temp_ulko");
        mibObject.setType("gauge");

        NumericAttributeType attributeType = new NumericAttributeType(resourceType, snmpCollection.getName(), mibObject, new AttributeGroupType("foo", AttributeGroupType.IF_TYPE_IGNORE));

        attributeType.storeResult(new SnmpCollectionSet(agent, snmpCollection, m_locationAwareSnmpClient), null, new SnmpResult(mibObject.getSnmpObjId(), new SnmpInstId(mibObject.getInstance()), snmpValue));

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
            new Snmp4JValueFactory().getOctetString(longValue.getBytes(StandardCharsets.UTF_8))
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
