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
package org.opennms.netmgt.collection.persistence.rrd;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opennms.core.rpc.mock.MockRpcClientFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.MockPlatformTransactionManager;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collectd.DefaultSnmpCollectionAgent;
import org.opennms.netmgt.collectd.NodeInfo;
import org.opennms.netmgt.collectd.NodeResourceType;
import org.opennms.netmgt.collectd.OnmsSnmpCollection;
import org.opennms.netmgt.collectd.SnmpAttribute;
import org.opennms.netmgt.collectd.SnmpAttributeType;
import org.opennms.netmgt.collectd.SnmpCollectionAgent;
import org.opennms.netmgt.collectd.SnmpCollectionResource;
import org.opennms.netmgt.collectd.StringAttributeType;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.dao.support.RrdResourceAttributeUtils;
import org.opennms.netmgt.mock.MockDataCollectionConfig;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.snmp.proxy.common.LocationAwareSnmpClientRpcImpl;
import org.opennms.test.FileAnticipator;
import org.opennms.test.mock.MockUtil;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.common.collect.Sets;

/**
 * JUnit TestCase for the BasePersister.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class BasePersisterTest {
    private FileAnticipator m_fileAnticipator;
    private File m_snmpDirectory;
    private BasePersister m_persister;
    private OnmsIpInterface m_intf;
    private OnmsNode m_node;
    private PlatformTransactionManager m_transMgr = new MockPlatformTransactionManager();
    private IpInterfaceDao m_ifDao;
    private ServiceParameters m_serviceParams;
    private RrdStrategy<?, ?> m_rrdStrategy;
    private FilesystemResourceStorageDao m_resourceStorageDao;
    private LocationAwareSnmpClient m_locationAwareSnmpClient = new LocationAwareSnmpClientRpcImpl(new MockRpcClientFactory());

    /* erg, Rule fields must be public */
    @Rule public TestName m_testName = new TestName();

    @Before
    public void setUp() throws Exception {
        MockUtil.println("------------ Begin Test " + m_testName.getMethodName() + " --------------------------");
        MockLogAppender.setupLogging();

        m_fileAnticipator = new FileAnticipator();

        m_rrdStrategy = new JRobinRrdStrategy();
        m_resourceStorageDao = new FilesystemResourceStorageDao();
        m_resourceStorageDao.setRrdDirectory(m_fileAnticipator.getTempDir());

        m_intf = new OnmsIpInterface();
        m_node = new OnmsNode();
        m_node.setId(1);
        m_intf.setId(25);
        m_intf.setNode(m_node);
        m_intf.setIpAddress(InetAddressUtils.addr("1.1.1.1"));
        
        m_ifDao = mock(IpInterfaceDao.class);
        m_serviceParams = new ServiceParameters(new HashMap<String,Object>());
        
    }
    
    @After
    public void checkWarnings() throws Throwable {
        MockLogAppender.assertNoWarningsOrGreater();
        m_fileAnticipator.deleteExpected();
        verifyNoMoreInteractions(m_ifDao);
    }
    
    @After
    public void tearDown() throws Exception {
        m_fileAnticipator.deleteExpected(true);
        m_fileAnticipator.tearDown();
        MockUtil.println("------------ End Test " + m_testName.getMethodName() + " --------------------------");
    }
    
    @Test
    public void testPersistStringAttributeWithExistingPropertiesFile() throws Exception {
        initPersister();
        
        File nodeDir = m_fileAnticipator.tempDir(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.tempFile(nodeDir, RrdResourceAttributeUtils.STRINGS_PROPERTIES_FILE_NAME, "#just a test");
        
        CollectionAttribute attribute = buildStringAttribute();
        m_persister.persistStringAttribute(attribute);

        verify(m_ifDao, atLeastOnce()).load(anyInt());
    }

    @Test
    public void testPersistStringAttributeWithParentDirectory() throws Exception {
        initPersister();
        
        File nodeDir = m_fileAnticipator.tempDir(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.expecting(nodeDir, RrdResourceAttributeUtils.STRINGS_PROPERTIES_FILE_NAME);
        
        CollectionAttribute attribute = buildStringAttribute();
        m_persister.persistStringAttribute(attribute);

        verify(m_ifDao, atLeastOnce()).load(anyInt());
    }

    @Test
    public void testPersistStringAttributeWithNoParentDirectory() throws Exception {
        initPersister();
        
        File nodeDir = m_fileAnticipator.expecting(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.expecting(nodeDir, RrdResourceAttributeUtils.STRINGS_PROPERTIES_FILE_NAME);
        
        CollectionAttribute attribute = buildStringAttribute();
        m_persister.persistStringAttribute(attribute);

        verify(m_ifDao, atLeastOnce()).load(anyInt());
    }

    /**
     * Test for bug #1817 where a string attribute will get persisted to
     * both strings.properties and an RRD file if it is a numeric value.
     */
    @Test
    public void testPersistStringAttributeUsingBuilder() throws Exception {
        initPersister();
        
        File nodeDir = m_fileAnticipator.expecting(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.expecting(nodeDir, RrdResourceAttributeUtils.STRINGS_PROPERTIES_FILE_NAME);
        
        CollectionAttribute attribute = buildStringAttribute();

        m_persister.pushShouldPersist(attribute.getResource());
        
        m_persister.pushShouldPersist(attribute);

        m_persister.setBuilder(m_persister.createBuilder(attribute.getResource(), attribute.getName(), Sets.newHashSet(attribute.getAttributeType())));

        // This will end up calling m_persister.persistStringAttribute(attribute);
        m_persister.storeAttribute(attribute);

        m_persister.commitBuilder();
        
        m_persister.popShouldPersist();

        verify(m_ifDao, atLeastOnce()).load(anyInt());
    }

    @Test
    public void testBug2733() throws Exception {
        m_serviceParams.getParameters().put("storing-enabled", "false");
        testPersistStringAttributeUsingBuilder();

        verify(m_ifDao, atLeastOnce()).load(anyInt());
    }

    private SnmpAttribute buildStringAttribute() {
        
        when(m_ifDao.load(m_intf.getId())).thenReturn(m_intf);
        
        SnmpCollectionAgent agent = DefaultSnmpCollectionAgent.create(m_intf.getId(), m_ifDao, m_transMgr);
        
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();

        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), dataCollectionConfig, m_locationAwareSnmpClient);

        NodeResourceType resourceType = new NodeResourceType(agent, collection);
        
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);
        
        MibObject mibObject = new MibObject();
        mibObject.setOid(".1.1.1.1");
        mibObject.setAlias("mibObjectAlias");
        mibObject.setType("string");
        mibObject.setInstance("0");
        mibObject.setMaxval(null);
        mibObject.setMinval(null);
        
        SnmpAttributeType attributeType = new StringAttributeType(resourceType, "some-collection", mibObject, new AttributeGroupType("mibGroup", AttributeGroupType.IF_TYPE_IGNORE));
        
        return new SnmpAttribute(resource, attributeType, SnmpUtils.getValueFactory().getOctetString("foo".getBytes()));
    }

    private void initPersister() throws IOException {
        m_persister = new BasePersister(m_serviceParams, createRrdRepository(), m_rrdStrategy, m_resourceStorageDao);
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
