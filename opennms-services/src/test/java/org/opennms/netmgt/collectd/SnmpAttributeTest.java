//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Mar 04: Created this file. - dj@opennms.org
//
// Copyright (C) 2008 Daniel J. Gregor, Jr..  All rights reserved.
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
//     OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.collectd;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.core.utils.TimeKeeper;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.support.RrdTestUtils;
import org.opennms.netmgt.mock.MockDataCollectionConfig;
import org.opennms.netmgt.mock.MockPlatformTransactionManager;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValueFactory;
import org.opennms.test.mock.EasyMockUtils;

public class SnmpAttributeTest extends TestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    private IpInterfaceDao m_ipInterfaceDao = m_mocks.createMock(IpInterfaceDao.class);
    private RrdStrategy m_rrdStrategy = m_mocks.createMock(RrdStrategy.class);
    private TimeKeeper m_timeKeeper = m_mocks.createMock(TimeKeeper.class);
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        RrdTestUtils.initializeNullStrategy();
        RrdUtils.setStrategy(m_rrdStrategy);
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        
        m_mocks.verifyAll();
    }
    
    // FIXME: there are tests in this file other than the disabled one
    public void testBogus() {
        m_mocks.replayAll();
    }

    // FIXME: This test doesn't work because of bug #2018
    public void FIXMEtestNumericAttributeFloatValue() throws Exception {
        long time = System.currentTimeMillis();
        String stringValue = "7.69";

        OnmsNode node = new OnmsNode();
        node.setId(3);
        
        OnmsIpInterface ipInterface = new OnmsIpInterface();
        ipInterface.setId(1);
        ipInterface.setNode(node);
        ipInterface.setIpAddress("192.168.1.1");
        
        expect(m_ipInterfaceDao.load(1)).andReturn(ipInterface).times(3);

        expect(m_timeKeeper.getCurrentTime()).andReturn(time);

        
        expect(m_rrdStrategy.getDefaultFileExtension()).andReturn(".mockRrdStrategy").anyTimes();
        expect(m_rrdStrategy.createDefinition(isA(String.class), isA(String.class), isA(String.class), anyInt(), isAList(RrdDataSource.class), isAList(String.class))).andReturn(new Object());
        m_rrdStrategy.createFile(isA(Object.class));
        expect(m_rrdStrategy.openFile(isA(String.class))).andReturn(new Object());
        m_rrdStrategy.updateFile(isA(Object.class), isA(String.class), eq((time / 1000) + ":" + stringValue));
        m_rrdStrategy.closeFile(isA(Object.class));
        
        m_mocks.replayAll();
        
        CollectionAgent agent = DefaultCollectionAgent.create(ipInterface.getId(), m_ipInterfaceDao, new MockPlatformTransactionManager());
        OnmsSnmpCollection snmpCollection = new OnmsSnmpCollection(agent, new ServiceParameters(new HashMap<String, String>()), new MockDataCollectionConfig());
        NodeResourceType resourceType = new NodeResourceType(agent, snmpCollection);
        NodeInfo nodeInfo = new NodeInfo(resourceType, agent);
        
        MibObject mibObject = new MibObject();
        mibObject.setOid(".1.3.6.1.4.1.12238.55.9997.4.1.2.9.116.101.109.112.95.117.108.107.111");
        mibObject.setInstance("1");
        mibObject.setAlias("temp_ulko");
        mibObject.setType("gauge");
        
        NumericAttributeType attributeType = new NumericAttributeType(resourceType, snmpCollection.getName(), mibObject, new AttributeGroupType("foo", "ignore"));
        
        SnmpValue value = new Snmp4JValueFactory().getOctetString(stringValue.getBytes());
        SnmpAttribute attr = new SnmpAttribute(nodeInfo, attributeType, value);
        
        RrdRepository repository = new RrdRepository();
        repository.setRraList(new ArrayList<String>(Collections.singleton("RRA:AVERAGE:0.5:1:2016")));
        
        BasePersister persister = new BasePersister(new ServiceParameters(new HashMap<String, String>()), repository);
        persister.createBuilder(nodeInfo, "baz", attributeType);
        persister.getBuilder().setTimeKeeper(m_timeKeeper);
        
        attr.storeAttribute(persister);
        
        persister.commitBuilder();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> isAList(Class<T> clazz) {
        return isA(List.class);
    }
}
