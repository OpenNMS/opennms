//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.collectd;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.easymock.EasyMock;
import org.opennms.core.concurrent.BarrierSignaler;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.collectd.AttributeVisitor;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.collectd.OnmsSnmpCollection;
import org.opennms.netmgt.collectd.ServiceParameters;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.mock.MockDataCollectionConfig;
import org.opennms.netmgt.mock.MockPlatformTransactionManager;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.PrimaryType;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.core.io.ClassPathResource;

public class SnmpCollectorTestCase extends OpenNMSTestCase {

    private final class AttributeVerifier extends AttributeVisitor {
		private final List list;

		public int attributeCount = 0;
		private AttributeVerifier(List list) {
			this.list = list;
		}

                @Override
                public void visitAttribute(CollectionAttribute attribute) {
                    visitAttribute((SnmpAttribute)attribute);
                }
                
		public void visitAttribute(SnmpAttribute attribute) {
			attributeCount++;
		    assertMibObjectPresent(attribute, list);
		}



	}

	protected BarrierSignaler m_signaler;
    public MockDataCollectionConfig m_config;
    
    protected SnmpObjId m_sysNameOid;
    protected SnmpObjId m_ifDescr;
    protected SnmpObjId m_ifOutOctets;
    protected SnmpObjId m_invalid;
    
    protected OnmsNode m_node;
    protected OnmsIpInterface m_iface;
    
    protected CollectionAgent m_agent;
    private SnmpWalker m_walker;
    protected SnmpCollectionSet m_collectionSet;
    
    protected MockSnmpAgent m_mockAgent;
    protected IpInterfaceDao m_ifaceDao;
    protected EasyMockUtils m_easyMockUtils;
    
    public void setVersion(int version) {
        super.setVersion(version);
    }

    protected void setUp() throws Exception {
        setStartEventd(false);
        super.setUp();
        
        m_mockAgent = MockSnmpAgent.createAgentAndRun(new ClassPathResource("org/opennms/netmgt/snmp/snmpTestData1.properties"), myLocalHost().getHostAddress() + "/9161");
        
        m_config = new MockDataCollectionConfig();
        DataCollectionConfigFactory.setInstance(m_config);
        
        m_signaler = new BarrierSignaler(1);

        m_sysNameOid = SnmpObjId.get(".1.3.6.1.2.1.1.5");
        m_ifOutOctets = SnmpObjId.get("..1.3.6.1.2.1.2.2.1.16");
        m_invalid = SnmpObjId.get(".1.5.6.1.2.1.1.5");
        m_ifDescr = SnmpObjId.get(".1.3.6.1.2.1.2.2.1.2");
        
        m_easyMockUtils = new EasyMockUtils();
        m_ifaceDao = m_easyMockUtils.createMock(IpInterfaceDao.class);

        createAgent(1, PrimaryType.PRIMARY);
        
    }

    protected void tearDown() throws Exception {
    	m_mockAgent.shutDown();
    	while(!m_mockAgent.isStopped()) {
    		Thread.sleep(10);
    	}
        super.tearDown();
    }
    
    protected void assertMibObjectsPresent(CollectionResource resource, final List attrList) {
        assertNotNull(resource);
        
        AttributeVerifier attributeVerifier = new AttributeVerifier(attrList);
		resource.visit(attributeVerifier);
		assertEquals("Unexpected number of attributes", attrList.size(), attributeVerifier.attributeCount);
    }

    protected void assertMibObjectPresent(SnmpAttribute attribute, List attrList) {
        for (Iterator it = attrList.iterator(); it.hasNext();) {
            MibObject mibObj = (MibObject) it.next();
            if (mibObj.getOid().equals(attribute.getAttributeType().getOid()))
                return;
        }
        fail("Unable to find attribue "+attribute+" in attribute list");
    }

    protected void addIfNumber() {
        addAttribute("ifNumber",    ".1.3.6.1.2.1.2.1", "0", "integer");
    }

    protected void addSystemGroup() {
        addSysDescr();
        addSysOid();
//        addSysContact();
        addSysName();
        addSysLocation();
    }

    protected void addSysLocation() {
        addAttribute("sysLocation", ".1.3.6.1.2.1.1.6", "0", "string");
    }

    protected void addSysName() {
        addAttribute("sysName",     ".1.3.6.1.2.1.1.5", "0", "string");
    }

    protected void addSysContact() {
        addAttribute("sysContact",  ".1.3.6.1.2.1.1.4", "0", "string");
    }

    protected void addSysUptime() {
        addAttribute("sysUptime",   ".1.3.6.1.2.1.1.3", "0", "timeTicks");
    }

    protected void addSysOid() {
        addAttribute("sysOid",      ".1.3.6.1.2.1.1.2", "0", "string");
    }

    protected void addSysDescr() {
        addAttribute("sysDescr", ".1.3.6.1.2.1.1.1", "0", "string");
    }

    protected void addAttribute(String alias, String oid, String inst, String type) {
        m_config.addAttributeType(alias, oid, inst, type);
    }

    protected void addIfTable() {
        addIfSpeed();
        addIfInOctets();
        addIfOutOctets();
        addIfInErrors();
        addIfOutErrors();
        addIfInDiscards();
    }
    
    protected void addIpAddrTable() {
        addIpAdEntAddr();
        addIpAdEntIfIndex();
        addIpAdEntNetMask();
        addIpAdEntBcastAddr();
    }
    
    protected void addInvalid() {
        addAttribute("invalid", ".1.5.6.1.2.1.4.20.1.4", "ifIndex", "counter");
        
    }
    
    

    protected void addIpAdEntBcastAddr() {
        // .1.3.6.1.2.1.4.20.1.4
        // FIXME: be better about non specific instances.. They are not all ifIndex but we are using that to mean a column
        addAttribute("addIpAdEntBcastAddr", ".1.3.6.1.2.1.4.20.1.4", "ifIndex", "ipAddress");
    }

    protected void addIpAdEntNetMask() {
        // .1.3.6.1.2.1.4.20.1.3
        addAttribute("addIpAdEntNetMask", ".1.3.6.1.2.1.4.20.1.3", "ifIndex", "ipAddress");
        
    }

    protected void addIpAdEntIfIndex() {
        // .1.3.6.1.2.1.4.20.1.2
        addAttribute("addIpAdEntIfIndex", ".1.3.6.1.2.1.4.20.1.2", "ifIndex", "integer");
        
    }

    protected void addIpAdEntAddr() {
        // .1.3.6.1.2.1.4.20.1.1
        addAttribute("addIpAdEntAddr", ".1.3.6.1.2.1.4.20.1.1", "ifIndex", "ipAddress");
        
    }

    protected void addIfInDiscards() {
        addAttribute("ifInDiscards", ".1.3.6.1.2.1.2.2.1.13", "ifIndex", "counter");
    }

    protected void addIfOutErrors() {
        addAttribute("ifOutErrors", ".1.3.6.1.2.1.2.2.1.20", "ifIndex", "counter");
    }

    protected void addIfInErrors() {
        addAttribute("ifInErrors", ".1.3.6.1.2.1.2.2.1.14", "ifIndex", "counter");
    }

    protected void addIfOutOctets() {
        addAttribute("ifOutOctets", ".1.3.6.1.2.1.2.2.1.16", "ifIndex", "counter");
    }

    protected void addIfInOctets() {
        addAttribute("ifInOctets", ".1.3.6.1.2.1.2.2.1.10", "ifIndex", "counter");
    }

    protected void addIfSpeed() {
        addAttribute("ifSpeed", ".1.3.6.1.2.1.2.2.1.5", "ifIndex", "gauge");
    }
    
    public void testDoNothing() {}

    public List getAttributeList() {
        return m_config.getAttrList();
    }

    protected void createAgent(int ifIndex, PrimaryType ifCollType) {
        m_node = new OnmsNode();
        m_node.setSysObjectId(".1.2.3.4.5.6.7");
        
        OnmsSnmpInterface snmpIface = new OnmsSnmpInterface(m_node, ifIndex);
    
    	m_iface = new OnmsIpInterface();
        m_iface.setId(123);
        m_iface.setInetAddress(myLocalHost());
    	m_iface.setIsSnmpPrimary(ifCollType);
    	m_iface.setSnmpInterface(snmpIface);
    	m_node.addIpInterface(m_iface);
        

    	EasyMock.expect(m_ifaceDao.load(m_iface.getId())).andReturn(m_iface).anyTimes();
        
        m_easyMockUtils.replayAll();
        
        m_agent = DefaultCollectionAgent.create(m_iface.getId(), m_ifaceDao, new MockPlatformTransactionManager());
        
    }
    
    protected void initializeAgent() {
        ServiceParameters params = new ServiceParameters(new HashMap<String, String>());
        OnmsSnmpCollection snmpCollection = new OnmsSnmpCollection(m_agent, params);
        m_collectionSet = snmpCollection.createCollectionSet(m_agent);
        m_agent.validateAgent();
    }
    
    protected SnmpCollectionSet getCollectionSet() {
        return m_collectionSet;
    }

    protected void createWalker(CollectionTracker collector) {
        m_walker = SnmpUtils.createWalker(m_agent.getAgentConfig(), getName(), collector);
        m_walker.start();
    }

    protected void waitForSignal() throws InterruptedException {
        m_walker.waitFor();
    }
    
    

}
