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

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.MockPlatformTransactionManager;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.mock.MockDataCollectionConfig;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.test.FileAnticipator;
import org.opennms.test.mock.MockUtil;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

/**
 * JUnit TestCase for PersistOperationBuilder.
 *  
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class PersistOperationBuilderTest extends TestCase {
    private FileAnticipator m_fileAnticipator;
    private File m_snmpDirectory;
    private OnmsIpInterface m_intf;
    private OnmsNode m_node;
    private PlatformTransactionManager m_transMgr = new MockPlatformTransactionManager();

    private IpInterfaceDao m_ifDao;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockUtil.println("------------ Begin Test " + getName() + " --------------------------");
        MockLogAppender.setupLogging();

        m_fileAnticipator = new FileAnticipator();
        
        m_intf = new OnmsIpInterface();
        m_node = new OnmsNode();
        m_node.setId(1);
        m_intf.setNode(m_node);
        m_intf.setIpAddress(InetAddressUtils.addr("1.1.1.1"));
        m_intf.setId(27);
        
        m_ifDao = EasyMock.createMock(IpInterfaceDao.class);
        EasyMock.expect(m_ifDao.load(m_intf.getId())).andReturn(m_intf).anyTimes();
        
        EasyMock.replay(m_ifDao);
    }
    
    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        MockLogAppender.assertNoWarningsOrGreater();
        m_fileAnticipator.deleteExpected();
    }
    
    @Override
    protected void tearDown() throws Exception {
        m_fileAnticipator.deleteExpected(true);
        m_fileAnticipator.tearDown();
        MockUtil.println("------------ End Test " + getName() + " --------------------------");
        super.tearDown();
    }
    
    private CollectionAgent getCollectionAgent() {
        
        return DefaultCollectionAgent.create(m_intf.getId(), m_ifDao, m_transMgr);
    }
    
    public void testCommitWithNoDeclaredAttributes() throws Exception {
        RrdRepository repository = createRrdRepository();

        CollectionAgent agent = getCollectionAgent();
        
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();
        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), dataCollectionConfig);
        
        NodeResourceType resourceType = new NodeResourceType(agent, collection);
        
        CollectionResource resource = new NodeInfo(resourceType, agent);
        
        PersistOperationBuilder builder = new PersistOperationBuilder(repository, resource, "rrdName");
        builder.commit();
    }
    

    public void testCommitWithDeclaredAttribute() throws Exception {
        File nodeDir = m_fileAnticipator.expecting(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.expecting(nodeDir, "rrdName" + RrdUtils.getExtension());
        m_fileAnticipator.expecting(nodeDir, "rrdName" + ".meta");
        
        RrdRepository repository = createRrdRepository();

        CollectionAgent agent = getCollectionAgent();
        
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();
        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), dataCollectionConfig);
        
        NodeResourceType resourceType = new NodeResourceType(agent, collection);
        
        CollectionResource resource = new NodeInfo(resourceType, agent);
        
        MibObject mibObject = new MibObject();
        mibObject.setOid(".1.1.1.1");
        mibObject.setAlias("mibObjectAlias");
        mibObject.setType("string");
        mibObject.setInstance("0");
        mibObject.setMaxval(null);
        mibObject.setMinval(null);
        
        SnmpCollectionSet collectionSet = new SnmpCollectionSet(agent, collection);
        
        SnmpAttributeType attributeType = new StringAttributeType(resourceType, "some-collection", mibObject, new AttributeGroupType("mibGroup", "ignore"));
        attributeType.storeResult(collectionSet, null, new SnmpResult(mibObject.getSnmpObjId(), new SnmpInstId(mibObject.getInstance()), SnmpUtils.getValueFactory().getOctetString("hello".getBytes())));
          
        PersistOperationBuilder builder = new PersistOperationBuilder(repository, resource, "rrdName");
        builder.declareAttribute(attributeType);
        builder.commit();
    }
    

    public void testCommitWithDeclaredAttributeAndValue() throws Exception {
        File nodeDir = m_fileAnticipator.expecting(getSnmpRrdDirectory(), m_node.getId().toString());
        m_fileAnticipator.expecting(nodeDir, "rrdName" + RrdUtils.getExtension());
        m_fileAnticipator.expecting(nodeDir, "rrdName" + ".meta");
        
        RrdRepository repository = createRrdRepository();

        CollectionAgent agent = getCollectionAgent();
        
        MockDataCollectionConfig dataCollectionConfig = new MockDataCollectionConfig();
        
        OnmsSnmpCollection collection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, Object>()), dataCollectionConfig);
        
        NodeResourceType resourceType = new NodeResourceType(agent, collection);
        
        CollectionResource resource = new NodeInfo(resourceType, agent);
        
        MibObject mibObject = new MibObject();
        mibObject.setOid(".1.1.1.1");
        mibObject.setAlias("mibObjectAlias");
        mibObject.setType("string");
        mibObject.setInstance("0");
        mibObject.setMaxval(null);
        mibObject.setMinval(null);
        
        SnmpCollectionSet collectionSet = new SnmpCollectionSet(agent, collection);
        
        SnmpAttributeType attributeType = new StringAttributeType(resourceType, "some-collection", mibObject, new AttributeGroupType("mibGroup", "ignore"));
        attributeType.storeResult(collectionSet, null, new SnmpResult(mibObject.getSnmpObjId(), new SnmpInstId(mibObject.getInstance()), SnmpUtils.getValueFactory().getOctetString("hello".getBytes())));
          
        PersistOperationBuilder builder = new PersistOperationBuilder(repository, resource, "rrdName");
        builder.declareAttribute(attributeType);
        builder.setAttributeValue(attributeType, "6.022E23");
        builder.commit();
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
