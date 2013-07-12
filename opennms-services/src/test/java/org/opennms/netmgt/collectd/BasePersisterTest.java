/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opennms.core.test.MockLogAppender;
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
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.test.FileAnticipator;
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.test.mock.MockUtil;
import org.springframework.transaction.PlatformTransactionManager;
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
    private EasyMockUtils m_easyMockUtils = new EasyMockUtils();
    private IpInterfaceDao m_ifDao;
    private ServiceParameters m_serviceParams;

    /* erg, Rule fields must be public */
    @Rule public TestName m_testName = new TestName();

    @Before
    public void setUp() throws Exception {
        MockUtil.println("------------ Begin Test " + m_testName.getMethodName() + " --------------------------");
        MockLogAppender.setupLogging();

        m_fileAnticipator = new FileAnticipator();
        
        m_intf = new OnmsIpInterface();
        m_node = new OnmsNode();
        m_node.setId(1);
        m_intf.setId(25);
        m_intf.setNode(m_node);
        m_intf.setIpAddress(InetAddressUtils.addr("1.1.1.1"));
        
        m_ifDao = m_easyMockUtils.createMock(IpInterfaceDao.class);
        m_serviceParams = new ServiceParameters(new HashMap<String,Object>());
        
    }
    
    @After
    public void checkWarnings() throws Throwable {
        MockLogAppender.assertNoWarningsOrGreater();
        m_fileAnticipator.deleteExpected();
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
        m_fileAnticipator.tempFile(nodeDir, "strings.properties", "#just a test");
        
        CollectionAttribute attribute = buildStringAttribute();
        m_persister.persistStringAttribute(attribute);
    }
    
    @Test
    public void testPersistStringAttributeWithParentDirectory() throws Exception {
        initPersister();
        
        File nodeDir = m_fileAnticipator.tempDir(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.expecting(nodeDir, "strings.properties");
        
        CollectionAttribute attribute = buildStringAttribute();
        m_persister.persistStringAttribute(attribute);
    }
    
    @Test
    public void testPersistStringAttributeWithNoParentDirectory() throws Exception {
        initPersister();
        
        File nodeDir = m_fileAnticipator.expecting(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.expecting(nodeDir, "strings.properties");
        
        CollectionAttribute attribute = buildStringAttribute();
        m_persister.persistStringAttribute(attribute);
    }
    
    /**
     * Test for bug #1817 where a string attribute will get persisted to
     * both strings.properties and an RRD file if it is a numeric value.
     */
    @Test
    public void testPersistStringAttributeUsingBuilder() throws Exception {
        initPersister();
        
        File nodeDir = m_fileAnticipator.expecting(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.expecting(nodeDir, "strings.properties");
        
        CollectionAttribute attribute = buildStringAttribute();

        m_persister.pushShouldPersist(attribute.getResource());
        
        m_persister.pushShouldPersist(attribute);

        m_persister.createBuilder(attribute.getResource(), attribute.getName(), attribute.getAttributeType());

        // This will end up calling m_persister.persistStringAttribute(attribute);
        m_persister.storeAttribute(attribute);

        m_persister.commitBuilder();
        
        m_persister.popShouldPersist();
    }

    @Test
    public void testBug2733() throws Exception {
        m_serviceParams.getParameters().put("storing-enabled", "false");
        testPersistStringAttributeUsingBuilder();
    }

    private SnmpAttribute buildStringAttribute() {
        
        EasyMock.expect(m_ifDao.load(m_intf.getId())).andReturn(m_intf).anyTimes();
        
        m_easyMockUtils.replayAll();
        
        CollectionAgent agent = DefaultCollectionAgent.create(m_intf.getId(), m_ifDao, m_transMgr);
        
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();
        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), dataCollectionConfig);
        
        NodeResourceType resourceType = new NodeResourceType(agent, collection);
        
        SnmpCollectionResource resource = new NodeInfo(resourceType, agent);
        
        MibObject mibObject = new MibObject();
        mibObject.setOid(".1.1.1.1");
        mibObject.setAlias("mibObjectAlias");
        mibObject.setType("string");
        mibObject.setInstance("0");
        mibObject.setMaxval(null);
        mibObject.setMinval(null);
        
        SnmpAttributeType attributeType = new StringAttributeType(resourceType, "some-collection", mibObject, new AttributeGroupType("mibGroup", "ignore"));
        
        return new SnmpAttribute(resource, attributeType, SnmpUtils.getValueFactory().getOctetString("foo".getBytes()));
    }

    private void initPersister() throws IOException {
        m_persister = new BasePersister(m_serviceParams, createRrdRepository());
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
