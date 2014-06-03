/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;

import org.opennms.netmgt.model.IsIsElement;
import org.opennms.netmgt.model.IsIsElement.IsisAdminState;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.IsIsLink.IsisISAdjNeighSysType;
import org.opennms.netmgt.model.IsIsLink.IsisISAdjState;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.model.LldpElement.LldpChassisIdSubType;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.LldpLink.LldpPortIdSubType;
import org.opennms.netmgt.model.OspfElement;
import org.opennms.netmgt.model.OspfElement.Status;
import org.opennms.netmgt.model.OspfElement.TruthValue;
import org.opennms.netmgt.nb.TestNetworkBuilder;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitConfigurationEnvironment
public class EnLinkdSnmpTest extends TestNetworkBuilder implements InitializingBean {
    
	private final static Logger LOG = LoggerFactory.getLogger(EnLinkdSnmpTest.class);
    
	@Override
    public void afterPropertiesSet() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.opennms.mock.snmp", "WARN");
        p.setProperty("log4j.logger.org.opennms.core.test.snmp", "WARN");
        p.setProperty("log4j.logger.org.opennms.netmgt", "WARN");
        p.setProperty("log4j.logger.org.springframework","WARN");
        p.setProperty("log4j.logger.com.mchange.v2.resourcepool", "WARN");
        MockLogAppender.setupLogging(p);
    }

    @Test
    public void testInSameNetwork() throws Exception {
    	assertEquals(true, InetAddressUtils.inSameNetwork(InetAddress.getByName("192.168.0.1"),
    			InetAddress.getByName("192.168.0.2"),InetAddress.getByName("255.255.255.252")));
    	assertEquals(false, InetAddressUtils.inSameNetwork(InetAddress.getByName("192.168.0.1"),
    			InetAddress.getByName("192.168.0.5"),InetAddress.getByName("255.255.255.252")));
    	assertEquals(true, InetAddressUtils.inSameNetwork(InetAddress.getByName("10.10.0.1"),
    			InetAddress.getByName("10.168.0.5"),InetAddress.getByName("255.0.0.0")));
    }

    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt")
    })
    public void testOspfGeneralGroupWalk() throws Exception {
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH1_IP));
    	String trackerName = "ospfGeneralGroup";

        final OspfGeneralGroupTracker ospfGeneralGroup = new OspfGeneralGroupTracker();

    	SnmpWalker walker =  SnmpUtils.createWalker(config, trackerName, ospfGeneralGroup);

        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
            	LOG.info(
                        "run:Aborting node scan : Agent timed out while scanning the {} table", trackerName);
            }  else if (walker.failed()) {
            	LOG.info(
                        "run:Aborting node scan : Agent failed while scanning the {} table: {}", trackerName,walker.getErrorMessage());
            }
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }
        
        OspfElement ospfElement = ospfGeneralGroup.getOspfElement();
        assertEquals(InetAddress.getByName("192.168.100.246"), ospfElement.getOspfRouterId());
        assertEquals(null, ospfElement.getOspfRouterIdNetmask());
        assertEquals(null, ospfElement.getOspfRouterIdIfindex());
        assertEquals(Status.enabled, ospfElement.getOspfAdminStat());
        assertEquals(2, ospfElement.getOspfVersionNumber().intValue());
        assertEquals(TruthValue.FALSE, ospfElement.getOspfBdrRtrStatus());
        assertEquals(TruthValue.FALSE, ospfElement.getOspfASBdrRtrStatus());

        final OspfIpAddrTableGetter ipAddrTableGetter = new OspfIpAddrTableGetter(config);

        OspfElement ospfElementN = ipAddrTableGetter.get(ospfElement);
        assertEquals(InetAddress.getByName("192.168.100.246"), ospfElementN.getOspfRouterId());
        assertEquals(InetAddress.getByName("255.255.255.252"), ospfElementN.getOspfRouterIdNetmask());
        assertEquals(10101, ospfElementN.getOspfRouterIdIfindex().intValue());
        assertEquals(Status.enabled, ospfElementN.getOspfAdminStat());
        assertEquals(2, ospfElementN.getOspfVersionNumber().intValue());
        assertEquals(TruthValue.FALSE, ospfElementN.getOspfBdrRtrStatus());
        assertEquals(TruthValue.FALSE, ospfElementN.getOspfASBdrRtrStatus());

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt")
    })
    public void testOspfNbrTableWalk() throws Exception {
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH1_IP));
        String trackerName = "ospfNbrTable";
        OspfNbrTableTracker ospfNbrTableTracker = new OspfNbrTableTracker() {

        	public void processOspfNbrRow(final OspfNbrRow row) {
        		OspfLink link = row.getOspfLink();
        		try {
					assertEquals(InetAddress.getByName("192.168.100.249"), link.getOspfRemRouterId());
	        		assertEquals(InetAddress.getByName("192.168.100.245"), link.getOspfRemIpAddr());
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
        		assertEquals(0, link.getOspfRemAddressLessIndex().intValue());
        	}
        };

    	SnmpWalker walker =  SnmpUtils.createWalker(config, trackerName, ospfNbrTableTracker);

        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
            	LOG.info(
                        "run:Aborting node scan : Agent timed out while scanning the {} table", trackerName);
            }  else if (walker.failed()) {
            	LOG.info(
                        "run:Aborting node scan : Agent failed while scanning the {} table: {}", trackerName,walker.getErrorMessage());
            }
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt")
    })
    public void testOspfIfTableWalk() throws Exception {
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH1_IP));
        String trackerName = "ospfIfTable";
        final List<OspfLink> links = new ArrayList<OspfLink>();
        final OspfIpAddrTableGetter ipAddrTableGetter = new OspfIpAddrTableGetter(config);
        OspfIfTableTracker ospfIfTableTracker = new OspfIfTableTracker() {

        	public void processOspfIfRow(final OspfIfRow row) {
        		links.add(row.getOspfLink(ipAddrTableGetter));
         	}
        };

    	SnmpWalker walker =  SnmpUtils.createWalker(config, trackerName, ospfIfTableTracker);

        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
            	LOG.info(
                        "run:Aborting node scan : Agent timed out while scanning the {} table", trackerName);
            }  else if (walker.failed()) {
            	LOG.info(
                        "run:Aborting node scan : Agent failed while scanning the {} table: {}", trackerName,walker.getErrorMessage());
            }
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }
        
        for (OspfLink link: links) {
			assertEquals(0, link.getOspfAddressLessIndex().intValue());
			if (link.getOspfIpAddr().equals(InetAddress.getByName("192.168.100.246"))) {
				assertEquals(10101, link.getOspfIfIndex().intValue());
				assertEquals(InetAddress.getByName("255.255.255.252"), link.getOspfIpMask());
			} else if (link.getOspfIpAddr().equals(InetAddress.getByName("172.16.10.1"))){
				assertEquals(10, link.getOspfIfIndex().intValue());
				assertEquals(InetAddress.getByName("255.255.255.0"), link.getOspfIpMask());
			} else if (link.getOspfIpAddr().equals(InetAddress.getByName("172.16.20.1"))){
				assertEquals(20, link.getOspfIfIndex().intValue());
				assertEquals(InetAddress.getByName("255.255.255.0"), link.getOspfIpMask());
			} else if (link.getOspfIpAddr().equals(InetAddress.getByName("172.16.30.1"))){
				assertEquals(30, link.getOspfIfIndex().intValue());
				assertEquals(InetAddress.getByName("255.255.255.0"), link.getOspfIpMask());
			} else if (link.getOspfIpAddr().equals(InetAddress.getByName("172.16.40.1"))){
				assertEquals(40, link.getOspfIfIndex().intValue());
				assertEquals(InetAddress.getByName("255.255.255.0"), link.getOspfIpMask());
			} else {
				assertEquals(false, true);
			}

        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt")
    })
    public void testLldpLocalGroupWalk() throws Exception {

    	String trackerName = "lldpLocalGroup";
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH1_IP));
    	        LldpLocalGroupTracker lldpLocalGroup = new LldpLocalGroupTracker();
        SnmpWalker walker =  SnmpUtils.createWalker(config, trackerName, lldpLocalGroup);

        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
            	LOG.info(
                        "run:Aborting node scan : Agent timed out while scanning the {} table", trackerName);
            }  else if (walker.failed()) {
            	LOG.info(
                        "run:Aborting node scan : Agent failed while scanning the {} table: {}", trackerName,walker.getErrorMessage());
            }
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }

		LldpElement eiA = lldpLocalGroup.getLldpElement();
		System.err.println("local chassis type: " + LldpChassisIdSubType.getTypeString(eiA.getLldpChassisIdSubType().getValue()));
		System.err.println("local chassis id: " + eiA.getLldpChassisId());
		System.err.println("local sysname: " + eiA.getLldpSysname());
		
		assertEquals("0016c8bd4d80", eiA.getLldpChassisId());
		assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, eiA.getLldpChassisIdSubType());
		assertEquals("Switch1", eiA.getLldpSysname());
    }
    

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt")
    })
    public void testLldpLocGetter() throws Exception {

    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH1_IP));
		
    	final LldpLocPortGetter lldpLocPort = new LldpLocPortGetter(config);
		LldpLink link = lldpLocPort.get(9);
		assertEquals(9, link.getLldpLocalPortNum().intValue());
		assertEquals("Gi0/9", link.getLldpPortId());
		assertEquals("GigabitEthernet0/9", link.getLldpPortDescr());
		assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpPortIdSubType());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt")
    })
    public void testLldpRemTableWalk() throws Exception {
		
        final SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH1_IP));
        final LldpLocPortGetter lldpLocPort = new LldpLocPortGetter(snmpAgent);
        LldpRemTableTracker lldpRemTable = new LldpRemTableTracker() {
            
        	public void processLldpRemRow(final LldpRemRow row) {
        		
        		
        		System.err.println("----------lldp rem----------------");
        		System.err.println("columns number in the row: " + row.getColumnCount());

        		assertEquals(6, row.getColumnCount());
        		LldpLink link = row.getLldpLink(lldpLocPort);

        		System.err.println("local port number: " + row.getLldpRemLocalPortNum());
        		System.err.println("remote chassis: " + link.getLldpRemChassisId());
        		System.err.println("remote chassis type: " + LldpChassisIdSubType.getTypeString(link.getLldpRemChassisIdSubType().getValue()));
        		assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, link.getLldpRemChassisIdSubType());

        		System.err.println("remote port id: " + link.getLldpRemPortId());
        		System.err.println("remote port type: " + LldpPortIdSubType.getTypeString(link.getLldpRemPortIdSubType().getValue()));
        		assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpRemPortIdSubType());
            }
        };
        String trackerName = "lldpRemTable";
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, trackerName, lldpRemTable);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = SIEGFRIE_SNMP_RESOURCE)
    })
    public void testIsisSysObjectWalk() throws Exception {

    	String trackerName = "isisSysObject";
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SIEGFRIE_IP));
        IsisSysObjectGroupTracker tracker = new IsisSysObjectGroupTracker();
        SnmpWalker walker =  SnmpUtils.createWalker(config, trackerName, tracker);

        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
            	LOG.info(
                        "run:Aborting node scan : Agent timed out while scanning the {} table", trackerName);
            }  else if (walker.failed()) {
            	LOG.info(
                        "run:Aborting node scan : Agent failed while scanning the {} table: {}", trackerName,walker.getErrorMessage());
            }
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }

		IsIsElement eiA = tracker.getIsisElement();
		System.err.println("Is-Is Sys Id: " + eiA.getIsisSysID());
		System.err.println("Is-Is Sys Admin State: " + IsisAdminState.getTypeString(eiA.getIsisSysAdminState().getValue()));
		
		assertEquals(SIEGFRIE_ISIS_SYS_ID, eiA.getIsisSysID());
		assertEquals(IsisAdminState.on, eiA.getIsisSysAdminState());
    }
    

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = SIEGFRIE_SNMP_RESOURCE)
    })
    public void testIsisISAdjTableWalk() throws Exception {

    	final List<IsIsLink> links = new ArrayList<IsIsLink>();
    	String trackerName = "isisISAdjTable";
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SIEGFRIE_IP));
        IsisISAdjTableTracker tracker = new IsisISAdjTableTracker() {
        	public void processIsisAdjRow(final IsIsAdjRow row) {
        		assertEquals(5, row.getColumnCount());
        		links.add(row.getIsisLink());
            }
        };

        SnmpWalker walker =  SnmpUtils.createWalker(config, trackerName, tracker);

        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
            	LOG.info(
                        "run:Aborting node scan : Agent timed out while scanning the {} table", trackerName);
            }  else if (walker.failed()) {
            	LOG.info(
                        "run:Aborting node scan : Agent failed while scanning the {} table: {}", trackerName,walker.getErrorMessage());
            }
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }

        assertEquals(2, links.size());

        for (final IsIsLink link: links) {
    		assertEquals(1,link.getIsisISAdjIndex().intValue());
    		assertEquals(IsisISAdjState.up, link.getIsisISAdjState());
    		assertEquals(IsisISAdjNeighSysType.l1_IntermediateSystem, link.getIsisISAdjNeighSysType());
    		assertEquals(0, link.getIsisISAdjNbrExtendedCircID().intValue());
    		if (link.getIsisCircIndex().intValue() == 533) {
    			assertEquals("001f12accbf0", link.getIsisISAdjNeighSNPAAddress());
    			assertEquals("000110255062",link.getIsisISAdjNeighSysID());
    		} else if (link.getIsisCircIndex().intValue() == 552) {
    			assertEquals("0021590e47c2", link.getIsisISAdjNeighSNPAAddress());
    			assertEquals("000110088500",link.getIsisISAdjNeighSysID());
    		} else {
    			assertEquals(true, false);
    		}

        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = SIEGFRIE_SNMP_RESOURCE)
    })
    public void testIsisCircTableWalk() throws Exception {

    	final List<IsIsLink> links = new ArrayList<IsIsLink>();
    	String trackerName = "isisCircTable";
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SIEGFRIE_IP));
        IsisCircTableTracker tracker = new IsisCircTableTracker() {
        	public void processIsisCircRow(final IsIsCircRow row) {
        		assertEquals(2, row.getColumnCount());
        		links.add(row.getIsisLink());
            }
        };

        SnmpWalker walker =  SnmpUtils.createWalker(config, trackerName, tracker);

        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
            	LOG.info(
                        "run:Aborting node scan : Agent timed out while scanning the {} table", trackerName);
            }  else if (walker.failed()) {
            	LOG.info(
                        "run:Aborting node scan : Agent failed while scanning the {} table: {}", trackerName,walker.getErrorMessage());
            }
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }

        assertEquals(12, links.size());

        for (final IsIsLink link: links) {
    		if (link.getIsisCircIndex().intValue() == 533) {
    			assertEquals(533, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
    		} else if (link.getIsisCircIndex().intValue() == 552) {
    			assertEquals(552, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
    		} else if (link.getIsisCircIndex().intValue() == 13) {
    			assertEquals(13, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.off, link.getIsisCircAdminState());
    		} else if (link.getIsisCircIndex().intValue() == 16) {
    			assertEquals(16, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
    		} else if (link.getIsisCircIndex().intValue() == 504) {
    			assertEquals(504, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
    		} else if (link.getIsisCircIndex().intValue() == 507) {
    			assertEquals(507, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
    		} else if (link.getIsisCircIndex().intValue() == 508) {
    			assertEquals(508, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
    		} else if (link.getIsisCircIndex().intValue() == 512) {
    			assertEquals(512, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
       		} else if (link.getIsisCircIndex().intValue() == 514) {
    			assertEquals(514, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
       		} else if (link.getIsisCircIndex().intValue() == 531) {
    			assertEquals(531, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
       		} else if (link.getIsisCircIndex().intValue() == 572) {
    			assertEquals(572, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
       		} else if (link.getIsisCircIndex().intValue() == 573) {
    			assertEquals(573, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
     		} else {
    			assertEquals(true, false);
    		}

        }
    }
}
