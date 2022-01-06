/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.config.SnmpPeerFactory;

import org.opennms.netmgt.enlinkd.model.BridgeElement;
import org.opennms.netmgt.enlinkd.model.BridgeElement.BridgeDot1dBaseType;
import org.opennms.netmgt.enlinkd.model.BridgeElement.BridgeDot1dStpProtocolSpecification;
import org.opennms.netmgt.enlinkd.model.BridgeStpLink;
import org.opennms.netmgt.enlinkd.model.BridgeStpLink.BridgeDot1dStpPortEnable;
import org.opennms.netmgt.enlinkd.model.BridgeStpLink.BridgeDot1dStpPortState;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia.IpNetToMediaType;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.IsIsElement;
import org.opennms.netmgt.enlinkd.model.IsIsElement.IsisAdminState;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.model.IsIsLink.IsisISAdjNeighSysType;
import org.opennms.netmgt.enlinkd.model.IsIsLink.IsisISAdjState;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.enlinkd.model.OspfElement;
import org.opennms.netmgt.enlinkd.model.OspfElement.Status;
import org.opennms.netmgt.enlinkd.model.OspfElement.TruthValue;
import org.opennms.netmgt.enlinkd.model.OspfLink;

import org.opennms.netmgt.enlinkd.service.api.BridgeForwardingTableEntry;
import org.opennms.netmgt.enlinkd.service.api.BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus;

import org.opennms.netmgt.enlinkd.snmp.Dot1dBaseTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dStpPortTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dTpFdbTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1qTpFdbTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dBasePortTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dBasePortTableTracker.Dot1dBasePortRow;

import org.opennms.netmgt.enlinkd.snmp.IsisCircTableTracker;
import org.opennms.netmgt.enlinkd.snmp.IsisISAdjTableTracker;
import org.opennms.netmgt.enlinkd.snmp.IsisSysObjectGroupTracker;

import org.opennms.netmgt.enlinkd.snmp.IpNetToMediaTableTracker;

import org.opennms.netmgt.enlinkd.snmp.LldpLocalGroupTracker;
import org.opennms.netmgt.enlinkd.snmp.LldpLocPortGetter;
import org.opennms.netmgt.enlinkd.snmp.LldpRemTableTracker;

import org.opennms.netmgt.enlinkd.snmp.TimeTetraLldpRemTableTracker;
import org.opennms.netmgt.enlinkd.snmp.TimeTetraLldpLocPortGetter;

import org.opennms.netmgt.enlinkd.snmp.MtxrNeighborTableTracker;
import org.opennms.netmgt.enlinkd.snmp.MtxrLldpLocalTableTracker;
import org.opennms.netmgt.enlinkd.snmp.MtxrLldpRemTableTracker;

import org.opennms.netmgt.enlinkd.snmp.OspfIpAddrTableGetter;
import org.opennms.netmgt.enlinkd.snmp.OspfGeneralGroupTracker;
import org.opennms.netmgt.enlinkd.snmp.OspfIfTableTracker;
import org.opennms.netmgt.enlinkd.snmp.OspfNbrTableTracker;

import org.opennms.netmgt.enlinkd.snmp.CdpCacheTableTracker;
import org.opennms.netmgt.enlinkd.snmp.CdpGlobalGroupTracker;
import org.opennms.netmgt.enlinkd.snmp.CdpInterfacePortNameGetter;

import org.opennms.netmgt.nb.NmsNetworkBuilder;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitConfigurationEnvironment
public class EnLinkdSnmpIT extends NmsNetworkBuilder implements InitializingBean {
    
    @Autowired
    LocationAwareSnmpClient m_client;
    private final static Logger LOG = LoggerFactory.getLogger(EnLinkdSnmpIT.class);
    
    @Override
    public void afterPropertiesSet() {
    }

    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.opennms.mock.snmp", "WARN");
        p.setProperty("log4j.logger.org.opennms.netmgt.snmp", "WARN");
        p.setProperty("log4j.logger.org.springframework","WARN");
        p.setProperty("log4j.logger.com.mchange.v2.resourcepool", "WARN");
        MockLogAppender.setupLogging(p);
    }

    @Test
    public void testInSameNetwork() throws Exception {
        assertTrue(InetAddressUtils.inSameNetwork(InetAddress.getByName("192.168.0.1"),
                InetAddress.getByName("192.168.0.2"), InetAddress.getByName("255.255.255.252")));
        assertFalse(InetAddressUtils.inSameNetwork(InetAddress.getByName("192.168.0.1"),
                InetAddress.getByName("192.168.0.5"), InetAddress.getByName("255.255.255.252")));
        assertTrue(InetAddressUtils.inSameNetwork(InetAddress.getByName("10.10.0.1"),
                InetAddress.getByName("10.168.0.5"), InetAddress.getByName("255.0.0.0")));
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = RPict001_IP, port = 161, resource = RPict001_SNMP_RESOURCE)
    })
    public void testCdpInterfaceGetter() throws Exception {
        SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(RPict001_IP));
        CdpInterfacePortNameGetter get = new CdpInterfacePortNameGetter(config, m_client, null);

        assertEquals("FastEthernet0", get.getInterfaceNameFromCiscoCdpMib(1).toDisplayString());
        assertEquals("FastEthernet1", get.getInterfaceNameFromCiscoCdpMib(2).toDisplayString());
        assertEquals("FastEthernet2", get.getInterfaceNameFromCiscoCdpMib(3).toDisplayString());
        assertEquals("FastEthernet3", get.getInterfaceNameFromCiscoCdpMib(4).toDisplayString());
        assertEquals("FastEthernet4", get.getInterfaceNameFromCiscoCdpMib(5).toDisplayString());
        assertEquals("Tunnel0", get.getInterfaceNameFromCiscoCdpMib(9).toDisplayString());
        assertEquals("Tunnel3", get.getInterfaceNameFromCiscoCdpMib(10).toDisplayString());

        assertEquals("FastEthernet0", get.getInterfaceNameFromMib2(1).toDisplayString());
        assertEquals("FastEthernet1", get.getInterfaceNameFromMib2(2).toDisplayString());
        assertEquals("FastEthernet2", get.getInterfaceNameFromMib2(3).toDisplayString());
        assertEquals("FastEthernet3", get.getInterfaceNameFromMib2(4).toDisplayString());
        assertEquals("FastEthernet4", get.getInterfaceNameFromMib2(5).toDisplayString());
        assertEquals("Tunnel0", get.getInterfaceNameFromMib2(9).toDisplayString());
        assertEquals("Tunnel3", get.getInterfaceNameFromMib2(10).toDisplayString());

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = RPict001_IP, port = 161, resource = RPict001_SNMP_RESOURCE)
    })
    public void testCdpGlobalGroupCollection() throws Exception {
        SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(RPict001_IP));

        String trackerName = "cdpGlobalGroup";

        final CdpGlobalGroupTracker cdpGlobalGroup = new CdpGlobalGroupTracker();

        try {
            m_client.walk(config,cdpGlobalGroup)
            .withDescription(trackerName)
            .withLocation(null)
            .execute()
            .get();
        } catch (final InterruptedException e) {
            LOG.error("run: Cdp Linkd collection interrupted, exiting",e);
            return;
        }

        assertEquals("r-ro-suce-pict-001.infra.u-ssi.net",cdpGlobalGroup.getCdpDeviceId());
        assertEquals(1,cdpGlobalGroup.getCdpGlobalRun().intValue());
        assertNull(cdpGlobalGroup.getCdpGlobalDeviceFormat());

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = CISCO_WS_C2948_IP, port = 161, resource = CISCO_WS_C2948_SNMP_RESOURCE )
    })
    public void testCdpGlobalGroupCollectionWithGlobalIdFormat() throws Exception {
        SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(CISCO_WS_C2948_IP));

        String trackerName = "cdpGlobalGroup";

        final CdpGlobalGroupTracker cdpGlobalGroup = new CdpGlobalGroupTracker();

        try {
            m_client.walk(config,cdpGlobalGroup)
            .withDescription(trackerName)
            .withLocation(null)
            .execute()
            .get();
        } catch (final InterruptedException e) {
            LOG.error("run: Cdp Linkd collection interrupted, exiting",e);
            return;
        }

        assertEquals("JAB043408B7",cdpGlobalGroup.getCdpDeviceId());
        assertEquals(1,cdpGlobalGroup.getCdpGlobalRun().intValue());
        assertEquals(3,cdpGlobalGroup.getCdpGlobalDeviceFormat().intValue());

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = RPict001_IP, port = 161, resource = RPict001_SNMP_RESOURCE)
    })
    public void testCdpCacheTableCollection() throws Exception {
        SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(RPict001_IP));

        class CdpCacheTableTrackerTester extends CdpCacheTableTracker {
            int count = 0;
            public int count() {
                return count;
            }
        }
        final CdpCacheTableTrackerTester cdpCacheTableTracker = new CdpCacheTableTrackerTester() {

            public void processCdpCacheRow(final CdpCacheRow row) {
                count++;
            }
            
        };

        String trackerName = "cdpCacheTable";

        try {
            m_client.walk(config,cdpCacheTableTracker)
            .withDescription(trackerName)
            .withLocation(null)
            .execute()
            .get();
        } catch (final InterruptedException e) {
            LOG.error("run: Cdp Linkd collection interrupted, exiting",e);
            return;
        }
        
        assertEquals(14, cdpCacheTableTracker.count());
    }

    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:/linkd/nms17216/switch1-walk.txt")
    })
    public void testOspfGeneralGroupWalk() throws Exception {
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH1_IP));
    	String trackerName = "ospfGeneralGroup";

        final OspfGeneralGroupTracker ospfGeneralGroup = new OspfGeneralGroupTracker();

        try {
            m_client.walk(config,ospfGeneralGroup)
            .withDescription(trackerName)
            .withLocation(null)
            .execute()
            .get();
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }
        
        OspfElement ospfElement = ospfGeneralGroup.getOspfElement();
        assertEquals(InetAddress.getByName("192.168.100.246"), ospfElement.getOspfRouterId());
        assertNull(ospfElement.getOspfRouterIdNetmask());
        assertNull(ospfElement.getOspfRouterIdIfindex());
        assertEquals(Status.enabled, ospfElement.getOspfAdminStat());
        assertEquals(2, ospfElement.getOspfVersionNumber().intValue());
        assertEquals(TruthValue.FALSE, ospfElement.getOspfBdrRtrStatus());
        assertEquals(TruthValue.FALSE, ospfElement.getOspfASBdrRtrStatus());

        final OspfIpAddrTableGetter ipAddrTableGetter = new OspfIpAddrTableGetter(config, m_client, null);

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
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:/linkd/nms17216/switch1-walk.txt")
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

        try {
            m_client.walk(config,ospfNbrTableTracker)
            .withDescription(trackerName)
            .withLocation(null)
            .execute()
            .get();
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:/linkd/nms17216/switch1-walk.txt")
    })
    public void testOspfIfTableWalk() throws Exception {
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH1_IP));
        String trackerName = "ospfIfTable";
        final List<OspfLink> links = new ArrayList<>();
        OspfIfTableTracker ospfIfTableTracker = new OspfIfTableTracker() {

        	public void processOspfIfRow(final OspfIfRow row) {
        		links.add(row.getOspfLink());
         	}
        };

        try {
            m_client.walk(config,ospfIfTableTracker)
            .withDescription(trackerName)
            .withLocation(null)
            .execute()
            .get();
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }
        
        final OspfIpAddrTableGetter ipAddrTableGetter = new OspfIpAddrTableGetter(config, m_client, null);
        for (OspfLink link: links) {
                link = ipAddrTableGetter.get(link);
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
                fail();
			}

        }
    }

    /**
     * This test is designed to test the issues in bug NMS-6921.
     * 
     * @see "https://issues.opennms.org/browse/NMS-6912"
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DW_IP, port=161, resource=DW_SNMP_RESOURCE)
    })
    public void testLldpDragonWaveLocalGroupWalk() throws Exception {

        String trackerName = "lldpLocalGroup";
        SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(DW_IP));
                LldpLocalGroupTracker lldpLocalGroup = new LldpLocalGroupTracker();

        try {
            m_client.walk(config,lldpLocalGroup)
            .withDescription(trackerName)
            .withLocation(null)
            .execute()
            .get();
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }

        LldpElement eiA = lldpLocalGroup.getLldpElement();
        System.err.println("local chassis type: " + LldpChassisIdSubType.getTypeString(eiA.getLldpChassisIdSubType().getValue()));
        System.err.println("local chassis id: " + eiA.getLldpChassisId());
        System.err.println("local sysname: " + eiA.getLldpSysname());
        
        assertEquals("cf", eiA.getLldpChassisId());
        assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT, eiA.getLldpChassisIdSubType());
        assertEquals("NuDesign", eiA.getLldpSysname());
    }

    /**
     * This test is designed to test the issues in bug NMS-6921.
     * 
     * @see "https://issues.opennms.org/browse/NMS-6912"

     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DW_IP, port=161, resource=DW_SNMP_RESOURCE)
    })
    public void testLldpDragonWaveLldpLocGetter() throws Exception {

        SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(DW_IP));
                
        final LldpLocPortGetter lldpLocPort = new LldpLocPortGetter(config, m_client, null);

        List<SnmpValue> val = lldpLocPort.get(1);
        assertEquals(3, val.size());
        assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACEALIAS, LldpPortIdSubType.get(val.get(0).toInt()));
        assertEquals("cf", LldpRemTableTracker.decodeLldpPortId(val.get(0).toInt(), val.get(1)));
        assertEquals("NuDesign", val.get(2).toDisplayString());
    }

    @Test
    @JUnitSnmpAgent(host=DW_IP, port=161, resource=DW_SNMP_RESOURCE)
    public void testLldpDragonWaveRemTableWalk() throws Exception {

        final SnmpAgentConfig config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(DW_IP));
        LldpRemTableTracker lldpRemTable = new LldpRemTableTracker() {

            public void processLldpRemRow(final LldpRemRow row) {

                System.err.println("----------lldp rem----------------");
                System.err.println("columns number in the row: "
                        + row.getColumnCount());

                assertEquals(6, row.getColumnCount());
                LldpLink link = row.getLldpLink();

                assertEquals(1, row.getLldpRemLocalPortNum().intValue());
                System.err.println("local port number: "
                        + row.getLldpRemLocalPortNum());

                assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT,
                             link.getLldpRemChassisIdSubType());
                System.err.println("remote chassis type: "
                        + LldpChassisIdSubType.getTypeString(link.getLldpRemChassisIdSubType().getValue()));

                assertEquals("cf", link.getLldpRemChassisId());
                System.err.println("remote chassis: "
                        + link.getLldpRemChassisId());

                assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACEALIAS,
                             link.getLldpRemPortIdSubType());
                System.err.println("remote port type: "
                        + LldpPortIdSubType.getTypeString(link.getLldpRemPortIdSubType().getValue()));


                assertEquals("cf", link.getLldpRemPortId());
                System.err.println("remote port id: "
                        + link.getLldpRemPortId());

                assertEquals("NuDesign", link.getLldpRemPortDescr());
                System.err.println("remote port descr: "
                        + link.getLldpRemPortDescr());
                

                assertEquals("NuDesign", link.getLldpRemSysname());
                System.err.println("remote sysname: "
                        + link.getLldpRemSysname());

            }
        };
        String trackerName = "lldpRemTable";

        try {
            m_client.walk(config,lldpRemTable)
            .withDescription(trackerName)
            .withLocation(null)
            .execute()
            .get();
        } catch (final InterruptedException e) {
            fail();
        }

    }

    /**
     * This test is designed to test the issues in bug NMS-13593.
     *
     * @see "https://issues.opennms.org/browse/NMS-13593"

     *
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ZHBGO1Zsr001_IP, port=161, resource=ZHBGO1Zsr001_RESOURCE),
            @JUnitSnmpAgent(host=ZHBGO1Zsr002_IP, port=161, resource=ZHBGO1Zsr002_RESOURCE)
    })
    public void testTimeTetraLldpWalk() throws Exception {
        String trackerName01 = "lldpLocalGroup01";
        SnmpAgentConfig  config01 = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(ZHBGO1Zsr001_IP));
        String trackerName02 = "lldpLocalGroup02";
        SnmpAgentConfig  config02 = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(ZHBGO1Zsr002_IP));
        LldpLocalGroupTracker lldpLocalGroup01 = new LldpLocalGroupTracker();
        LldpLocalGroupTracker lldpLocalGroup02 = new LldpLocalGroupTracker();
        final List<TimeTetraLldpRemTableTracker.TimeTetraLldpRemRow> links01 = new ArrayList<>();
        final List<TimeTetraLldpRemTableTracker.TimeTetraLldpRemRow> links02 = new ArrayList<>();

        try {
            m_client.walk(config01,lldpLocalGroup01)
                    .withDescription(trackerName01)
                    .withLocation(null)
                    .execute()
                    .get();
            m_client.walk(config02,lldpLocalGroup02)
                    .withDescription(trackerName02)
                    .withLocation(null)
                    .execute()
                    .get();
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }

        LldpElement lldpElement01 = lldpLocalGroup01.getLldpElement();
        LldpElement lldpElement02 = lldpLocalGroup02.getLldpElement();
        LOG.warn("01 local chassis type: " + LldpChassisIdSubType.getTypeString(lldpElement01.getLldpChassisIdSubType().getValue()));
        LOG.warn("01 local chassis id: " + lldpElement01.getLldpChassisId());
        LOG.warn("01 local sysname: " + lldpElement01.getLldpSysname());
        LOG.warn("02 local chassis type: " + LldpChassisIdSubType.getTypeString(lldpElement02.getLldpChassisIdSubType().getValue()));
        LOG.warn("02 local chassis id: " + lldpElement02.getLldpChassisId());
        LOG.warn("02 local sysname: " + lldpElement02.getLldpSysname());

        assertEquals(ZHBGO1Zsr001_LLDP_ID, lldpElement01.getLldpChassisId());
        assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, lldpElement01.getLldpChassisIdSubType());
        assertEquals(ZHBGO1Zsr001_NAME, lldpElement01.getLldpSysname());
        assertEquals(ZHBGO1Zsr002_LLDP_ID, lldpElement02.getLldpChassisId());
        assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, lldpElement02.getLldpChassisIdSubType());
        assertEquals(ZHBGO1Zsr002_NAME, lldpElement02.getLldpSysname());

        LldpRemTableTracker lldpRemTable01 = new LldpRemTableTracker() {

            public void processLldpRemRow(final LldpRemRow row) {
                fail();
            }
        };

        LldpRemTableTracker lldpRemTable02 = new LldpRemTableTracker() {

            public void processLldpRemRow(final LldpRemRow row) {
                fail();
            }
        };

        try {
            m_client.walk(config01,
                            lldpRemTable01)
                    .withDescription("lldpRemTable01")
                    .withLocation(null)
                    .execute()
                    .get();
            m_client.walk(config02,
                            lldpRemTable02)
                    .withDescription("lldpRemTable02")
                    .withLocation(null)
                    .execute()
                    .get();
        } catch (final InterruptedException e) {
            fail();
        }

        TimeTetraLldpRemTableTracker timetetralldpRemTable01
                = new TimeTetraLldpRemTableTracker() {

            public void processLldpRemRow(final TimeTetraLldpRemRow row) {
                assertEquals(6, row.getColumnCount());
                LldpLink link = row.getLldpLink();
                assertNotNull(link);
                assertNotNull(row.getTmnxLldpRemLocalDestMACAddress());
                assertNotNull(link.getLldpLocalPortNum());
                assertNotNull(link.getLldpPortIfindex());
                links01.add(row);
                assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, link.getLldpRemChassisIdSubType());
            }
        };
        TimeTetraLldpRemTableTracker timetetralldpRemTable02
                = new TimeTetraLldpRemTableTracker() {

            public void processLldpRemRow(final TimeTetraLldpRemRow row) {

                assertEquals(6, row.getColumnCount());
                LldpLink link = row.getLldpLink();
                assertNotNull(link);
                assertNotNull(row.getTmnxLldpRemLocalDestMACAddress());
                assertNotNull(link.getLldpLocalPortNum());
                assertNotNull(link.getLldpPortIfindex());
                links02.add(row);
                assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, link.getLldpRemChassisIdSubType());
            }
        };

        try {
            m_client.walk(config01,
                            timetetralldpRemTable01)
                    .withDescription("timetetralldpRemTable01")
                    .withLocation(null)
                    .execute()
                    .get();
            m_client.walk(config02,
                            timetetralldpRemTable02)
                    .withDescription("timetetralldpRemTable02")
                    .withLocation(null)
                    .execute()
                    .get();
        } catch (final InterruptedException e) {
            fail();
        }

        assertEquals(3,links01.size());
        assertEquals(4,links02.size());

        final TimeTetraLldpLocPortGetter ttlldpLocPort01 = new TimeTetraLldpLocPortGetter(config01,
                m_client,
                null);

        final TimeTetraLldpLocPortGetter ttlldpLocPort02 = new TimeTetraLldpLocPortGetter(config02,
                m_client,
                null);

        for (TimeTetraLldpRemTableTracker.TimeTetraLldpRemRow timeTetraLldpLink01 : links01) {
            LldpLink link01 = timeTetraLldpLink01.getLldpLink();
            assertNull(link01.getLldpPortId());
            assertNull(link01.getLldpPortIdSubType());
            assertNull(link01.getLldpPortDescr());
            assertEquals(1,timeTetraLldpLink01.getTmnxLldpRemLocalDestMACAddress().intValue());

            LldpLink updated = ttlldpLocPort01.getLldpLink(timeTetraLldpLink01);
            assertNotEquals("\"Not Found On lldpLocPortTable\"",updated.getLldpPortId());
            assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, updated.getLldpPortIdSubType());
            assertNotEquals("",updated.getLldpPortDescr());
            LOG.warn("01 {} ifindex {}",updated.getLldpLocalPortNum(),updated.getLldpPortIfindex());
            LOG.warn("01 {} portid {}",updated.getLldpLocalPortNum(),updated.getLldpPortId());
            LOG.warn("01 {} port subtype {}",updated.getLldpLocalPortNum(),updated.getLldpPortIdSubType());
            LOG.warn("01 {} portdescr {}",updated.getLldpLocalPortNum(),updated.getLldpPortDescr());
            LOG.warn("01 {} rem chassisId {}",updated.getLldpLocalPortNum(),updated.getLldpRemChassisId());
            LOG.warn("01 {} rem chassisId subtype {}",updated.getLldpLocalPortNum(),updated.getLldpRemChassisIdSubType());
            LOG.warn("01 {} rem sysname {}",updated.getLldpLocalPortNum(),updated.getLldpRemSysname());
            LOG.warn("01 {} rem portid {}",updated.getLldpLocalPortNum(),updated.getLldpRemPortId());
            LOG.warn("01 {} rem port subtype {}",updated.getLldpLocalPortNum(),updated.getLldpRemPortIdSubType());
            LOG.warn("01 {} rem portdescr {}",updated.getLldpLocalPortNum(),updated.getLldpRemPortDescr());
        }

        for (TimeTetraLldpRemTableTracker.TimeTetraLldpRemRow timeTetraLldpLink02 : links02) {
            LldpLink link02 = timeTetraLldpLink02.getLldpLink();
            assertNull(link02.getLldpPortId());
            assertNull(link02.getLldpPortIdSubType());
            assertNull(link02.getLldpPortDescr());
            assertEquals(1,timeTetraLldpLink02.getTmnxLldpRemLocalDestMACAddress().intValue());

            LldpLink updated = ttlldpLocPort02.getLldpLink(timeTetraLldpLink02);
            assertNotEquals("\"Not Found On lldpLocPortTable\"",updated.getLldpPortId());
            assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, updated.getLldpPortIdSubType());
            assertNotEquals("",updated.getLldpPortDescr());
            LOG.warn("02 {} ifindex {}",updated.getLldpLocalPortNum(),updated.getLldpPortIfindex());
            LOG.warn("02 {} portid {}",updated.getLldpLocalPortNum(),updated.getLldpPortId());
            LOG.warn("02 {} port subtype {}",updated.getLldpLocalPortNum(),updated.getLldpPortIdSubType());
            LOG.warn("02 {} portdescr {}",updated.getLldpLocalPortNum(),updated.getLldpPortDescr());
            LOG.warn("02 {} rem chassisId {}",updated.getLldpLocalPortNum(),updated.getLldpRemChassisId());
            LOG.warn("02 {} rem chassisId subtype {}",updated.getLldpLocalPortNum(),updated.getLldpRemChassisIdSubType());
            LOG.warn("02 {} rem sysname {}",updated.getLldpLocalPortNum(),updated.getLldpRemSysname());
            LOG.warn("02 {} rem portid {}",updated.getLldpLocalPortNum(),updated.getLldpRemPortId());
            LOG.warn("02 {} rem port subtype {}",updated.getLldpLocalPortNum(),updated.getLldpRemPortIdSubType());
            LOG.warn("02 {} rem portdescr {}",updated.getLldpLocalPortNum(),updated.getLldpRemPortDescr());

        }


    }

    /**
     * This test is designed to test the issues in bug NMS-13637.
     *
     * @see "https://issues.opennms.org/browse/NMS-13637"
     *
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MKT_CISCO_SW01_IP, port=161, resource=MKT_CISCO_SW01_RESOURCE)
    })
    public void testCiscoHomeLldpWalk() throws Exception {
        LOG.info(MKT_CISCO_SW01_IP);
        String trackerName00 = "lldpLocalGroup00";

        SnmpAgentConfig  config00 = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(MKT_CISCO_SW01_IP));
        LldpLocalGroupTracker lldpLocalGroup00 = new LldpLocalGroupTracker();
        try {
            m_client.walk(config00,lldpLocalGroup00)
                    .withDescription(trackerName00)
                    .withLocation(null)
                    .execute()
                    .get();
        } catch (final InterruptedException e) {
            LOG.info("run: collection interrupted, exiting {}",e.getMessage());
        }

        assertEquals(MKT_CISCO_SW01_LLDP_ID,lldpLocalGroup00.getLldpElement().getLldpChassisId());
        assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, lldpLocalGroup00.getLldpElement().getLldpChassisIdSubType());
        assertEquals(MKT_CISCO_SW01_NAME, lldpLocalGroup00.getLldpElement().getLldpSysname());

        final List<LldpRemTableTracker.LldpRemRow> links00 = new ArrayList<>();

        LldpRemTableTracker lldpRemTable00 = new LldpRemTableTracker() {

            public void processLldpRemRow(final LldpRemRow row) {
                links00.add(row);
            }
        };

        try {
            m_client.walk(config00,
                            lldpRemTable00)
                    .withDescription("lldpRemTable00")
                    .withLocation(null)
                    .execute()
                    .get();
        } catch (final InterruptedException e) {
            LOG.info("run: collection interrupted, exiting {}", e.getMessage());
        }

        assertEquals(9, links00.size());

        LldpLocPortGetter lldpLocPortGetter = new LldpLocPortGetter(config00, m_client, null);

        links00.stream().filter(row -> row.getLldpRemSysname().equals(MKTROUTER1_NAME))
                .forEach(row -> {
                    LldpLink link = lldpLocPortGetter.getLldpLink(row);
                    assertEquals(73, link.getLldpLocalPortNum().intValue());
                    assertEquals("gi5", link.getLldpPortId());
                    assertEquals("GigabitEthernet5", link.getLldpPortDescr());
                    assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpPortIdSubType());
                    assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, link.getLldpRemChassisIdSubType());
                    assertEquals(MKTROUTER1_ETHER1_MAC, link.getLldpRemChassisId());
                    assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpRemPortIdSubType());
                    assertEquals("ether1", link.getLldpRemPortId());
                });

        links00.stream().filter(row -> row.getLldpRemSysname().equals(MKTROUTER2_NAME))
                .forEach(row -> {
                    LldpLink link = lldpLocPortGetter.getLldpLink(row);
                    assertEquals(74, link.getLldpLocalPortNum().intValue());
                    assertEquals("gi5", link.getLldpPortId());
                    assertEquals("GigabitEthernet5", link.getLldpPortDescr());
                    assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpPortIdSubType());
                    assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, link.getLldpRemChassisIdSubType());
                    assertEquals(MKTROUTER2_ETHER1_MAC, link.getLldpRemChassisId());
                    assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpRemPortIdSubType());
                    assertEquals("ether1", link.getLldpRemPortId());
                });
    }

    /**
     * This test is designed to test the issues in bug NMS-13637.
     *
     * @see "https://issues.opennms.org/browse/NMS-13637"
     *
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MKTROUTER1_IP, port=161, resource=MKTROUTER1_RESOURCE)
    })
    public void testMikrotikRouter1LldpWalk() throws Exception {
        LOG.info(MKTROUTER1_IP);
        String trackerName01 = "lldpLocalGroup01";

        SnmpAgentConfig  config01 = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(MKTROUTER1_IP));
        LldpLocalGroupTracker lldpLocalGroup01 = new LldpLocalGroupTracker();
        try {
            m_client.walk(config01,lldpLocalGroup01)
                    .withDescription(trackerName01)
                    .withLocation(null)
                    .execute()
                    .get();
        } catch (final InterruptedException e) {
            LOG.info("run: collection interrupted, exiting {}", e.getMessage());
        }

        assertNull(lldpLocalGroup01.getLldpLocChassisid());
        assertNull(lldpLocalGroup01.getLldpLocChassisidSubType());
        assertEquals(MKTROUTER1_NAME, lldpLocalGroup01.getLldpLocSysname());

        final List<MtxrLldpRemTableTracker.MtxrLldpRemRow> links01 = new ArrayList<>();

        MtxrLldpRemTableTracker lldpRemTable01 = new MtxrLldpRemTableTracker() {

            public void processMtxrLldpRemRow(final MtxrLldpRemRow row) {
                links01.add(row);
            }
        };

        try {
            m_client.walk(config01,
                            lldpRemTable01)
                    .withDescription("lldpRemTable01")
                    .withLocation(null)
                    .execute()
                    .get();
        } catch (final InterruptedException e) {
            LOG.info("run: collection interrupted, exiting {}", e.getMessage());
        }

        assertEquals(5,links01.size());

        MtxrLldpLocalTableTracker mikrotikLldpLocalTable01 = new MtxrLldpLocalTableTracker();

        try {
            m_client.walk(config01,
                            mikrotikLldpLocalTable01)
                    .withDescription("mikrotikLldpLocalTable01")
                    .withLocation(null)
                    .execute()
                    .get();
        } catch (final InterruptedException e) {
            LOG.info("run: collection interrupted, exiting {}", e.getMessage());
        }

        MtxrNeighborTableTracker mikrotikMtxrIndexTable01 = new MtxrNeighborTableTracker();
        try {
            m_client.walk(config01,
                            mikrotikMtxrIndexTable01)
                    .withDescription("mikrotikMtxrIndexTable01")
                    .withLocation(null)
                    .execute()
                    .get();
        } catch (final InterruptedException e) {
            LOG.info("run: collection interrupted, exiting {}", e.getMessage());
        }

        links01.forEach(row -> {
                assertNull(row.getLldpLink().getLldpPortId());
                assertNotNull(row.getMtxrNeighborIndex());
                Integer mtxrIndex = mikrotikMtxrIndexTable01.getMtxrinterfaceId(row);
                assertEquals(1, mtxrIndex.intValue());
                LldpLink link = mikrotikLldpLocalTable01.getLldpLink(row, mtxrIndex);
                assertEquals("ether1",link.getLldpPortId());
                assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpPortIdSubType());
                assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, link.getLldpRemChassisIdSubType());
                assertEquals("",link.getLldpRemPortDescr());
                switch(link.getLldpLocalPortNum()) {
                    case 1:
                        assertEquals("gi5", link.getLldpRemPortId());
                        assertEquals(MKT_CISCO_SW01_NAME, link.getLldpRemSysname());
                        //this is a clear violation of LLDP-MIB implementation by Mikrotik
                        assertNotEquals(MKT_CISCO_SW01_LLDP_ID, link.getLldpRemChassisId());
                        assertEquals(MKT_CISCO_SW01_GB05_MAC,link.getLldpRemChassisId());
                        break;
                    case 2:
                        assertEquals("ether1", link.getLldpRemPortId());
                        assertEquals(MKTROUTER2_NAME, link.getLldpRemSysname());
                        assertEquals(MKTROUTER2_ETHER1_MAC, link.getLldpRemChassisId());
                        break;
                    case 3:
                        assertEquals("ens160", link.getLldpRemPortId());
                        assertEquals("elastic-01", link.getLldpRemSysname());
                        assertEquals(MKT_HOST3_LLDP_ID, link.getLldpRemChassisId());
                        break;
                    case 4:
                        assertEquals("vmx1", link.getLldpRemPortId());
                        assertEquals("opn-fw-01.clab.labmonkeys.tech", link.getLldpRemSysname());
                        assertEquals(MKT_HOST4_LLDP_ID, link.getLldpRemChassisId());
                        break;
                    case 5:
                        assertEquals("ens160", link.getLldpRemPortId());
                        assertEquals("onms-hzn", link.getLldpRemSysname());
                        assertEquals(MKT_HOST5_LLDP_ID, link.getLldpRemChassisId());
                        break;
                    default:
                        fail();
                        break;
                }
        });

        Map<Integer, MtxrLldpLocalTableTracker.LldpLocalPortRow> portRowMap = mikrotikLldpLocalTable01.getMtxrLldpLocalPortMap();
        assertTrue(portRowMap.containsKey(1));
        assertEquals(MKTROUTER1_ETHER1_MAC, portRowMap.get(1).getLldpLocPortId());
        assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, portRowMap.get(1).getLldpLocalPortIdSubtype());
    }


    /**
     * This test is designed to test the issues in bug NMS-13637.
     *
     * @see "https://issues.opennms.org/browse/NMS-13637"
     *
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MKTROUTER2_IP, port=161, resource=MKTROUTER2_RESOURCE)
    })
    public void testMikrotikRouter2LldpWalk() throws Exception {
        LOG.info(MKTROUTER2_IP);
        String trackerName02 = "lldpLocalGroup02";

        SnmpAgentConfig  config02 = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(MKTROUTER2_IP));
        LldpLocalGroupTracker lldpLocalGroup02 = new LldpLocalGroupTracker();
        try {
            m_client.walk(config02,lldpLocalGroup02)
                    .withDescription(trackerName02)
                    .withLocation(null)
                    .execute()
                    .get();
        } catch (final InterruptedException e) {
            LOG.info("run: collection interrupted, exiting {}", e.getMessage());
        }

        assertNull(lldpLocalGroup02.getLldpLocChassisid());
        assertNull(lldpLocalGroup02.getLldpLocChassisidSubType());
        assertEquals(MKTROUTER2_NAME, lldpLocalGroup02.getLldpLocSysname());

        final List<MtxrLldpRemTableTracker.MtxrLldpRemRow> links02 = new ArrayList<>();

        MtxrLldpLocalTableTracker mikrotikLldpLocalTable02 = new MtxrLldpLocalTableTracker();
        MtxrNeighborTableTracker mikrotikMtxrIndexTable02 = new MtxrNeighborTableTracker();
        MtxrLldpRemTableTracker mikrotikRemTable02 = new MtxrLldpRemTableTracker() {

            public void processMtxrLldpRemRow(final MtxrLldpRemRow row) {
                links02.add(row);
            }
        };

        try {
            m_client.walk(config02,
                            mikrotikRemTable02)
                    .withDescription("lldpRemTable02")
                    .withLocation(null)
                    .execute()
                    .get();
            m_client.walk(config02,
                            mikrotikLldpLocalTable02)
                    .withDescription("mikrotikLldpLocalTable02")
                    .withLocation(null)
                    .execute()
                    .get();
            m_client.walk(config02,
                            mikrotikMtxrIndexTable02)
                    .withDescription("mikrotikMtxrIndexTable02")
                    .withLocation(null)
                    .execute()
                    .get();
        } catch (final InterruptedException e) {
            LOG.info("run: collection interrupted, exiting {}",e.getMessage());
        }
        Map<Integer, MtxrLldpLocalTableTracker.LldpLocalPortRow> mikrotikLldpLocalPortMap02 = mikrotikLldpLocalTable02.getMtxrLldpLocalPortMap();
        Map<Integer, MtxrNeighborTableTracker.MtxrNeighborRow> mikrotikMtrxIndexMap02 = mikrotikMtxrIndexTable02.getMtxrNeighborMap();
        assertEquals(5, mikrotikMtrxIndexMap02.size());
        assertEquals(5, links02.size());
        assertEquals(1, mikrotikLldpLocalPortMap02.size());
        assertTrue(mikrotikLldpLocalPortMap02.containsKey(1));
        assertEquals(MKTROUTER2_ETHER1_MAC, mikrotikLldpLocalPortMap02.get(1).getLldpLocPortId());
        assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, mikrotikLldpLocalPortMap02.get(1).getLldpLocalPortIdSubtype());
        assertEquals("ether1", mikrotikLldpLocalPortMap02.get(1).getLldpLocPortDesc());

        LldpElement mktelem = mikrotikLldpLocalTable02.getLldpElement(lldpLocalGroup02.getLldpLocSysname());
        assertEquals(MKTROUTER2_ETHER1_MAC, mktelem.getLldpChassisId());
        assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, mktelem.getLldpChassisIdSubType());

        links02.forEach(row -> {
            assertNull(row.getLldpLink().getLldpPortId());
            assertNotNull(row.getMtxrNeighborIndex());
            Integer mtxrIndex = mikrotikMtxrIndexTable02.getMtxrinterfaceId(row);
            assertEquals(1, mtxrIndex.intValue());
            LldpLink link = mikrotikLldpLocalTable02.getLldpLink(row,mtxrIndex);
            assertEquals("ether1", link.getLldpPortId());
            assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpPortIdSubType());
            assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, link.getLldpRemChassisIdSubType());
            assertEquals("", link.getLldpRemPortDescr());
            switch(link.getLldpLocalPortNum()) {
                case 1:
                    assertEquals("ether1", link.getLldpRemPortId());
                    assertEquals(MKTROUTER1_NAME, link.getLldpRemSysname());
                    assertEquals(MKTROUTER1_ETHER1_MAC, link.getLldpRemChassisId());
                    break;
                case 2:
                    assertEquals("ens160", link.getLldpRemPortId());
                    assertEquals("elastic-01", link.getLldpRemSysname());
                    assertEquals(MKT_HOST3_LLDP_ID, link.getLldpRemChassisId());
                    break;
                case 3:
                    assertEquals("vmx1", link.getLldpRemPortId());
                    assertEquals("opn-fw-01.clab.labmonkeys.tech", link.getLldpRemSysname());
                    assertEquals(MKT_HOST4_LLDP_ID, link.getLldpRemChassisId());
                    break;
                case 4:
                    assertEquals("ens160", link.getLldpRemPortId());
                    assertEquals("onms-hzn", link.getLldpRemSysname());
                    assertEquals(MKT_HOST5_LLDP_ID, link.getLldpRemChassisId());
                    break;
                case 5:
                    assertEquals("gi5",link.getLldpRemPortId());
                    assertEquals(MKT_CISCO_SW01_NAME, link.getLldpRemSysname());
                    assertNotEquals(MKT_CISCO_SW01_LLDP_ID, link.getLldpRemChassisId());
                    assertEquals(MKT_CISCO_SW01_GB05_MAC, link.getLldpRemChassisId());
                    break;
                default:
                    fail();
                    break;
            }
        });



    }

    /**
     * This test is designed to test the issues in bug NMS-13637.
     *
     * @see "https://issues.opennms.org/browse/NMS-13637"
     *
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MKTROUTER3_IP, port=161, resource=MKTROUTER3_RESOURCE)
    })
    public void testMikrotikRouter3LldpWalk() throws Exception {
        LOG.info(MKTROUTER3_IP);
        String trackerName03 = "lldpLocalGroup03";

        SnmpAgentConfig  config03 = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(MKTROUTER3_IP));
        LldpLocalGroupTracker lldpLocalGroup03 = new LldpLocalGroupTracker();
        try {
            m_client.walk(config03,lldpLocalGroup03)
                    .withDescription(trackerName03)
                    .withLocation(null)
                    .execute()
                    .get();
        } catch (final InterruptedException e) {
            LOG.info("run: collection interrupted, exiting {}",e.getMessage());
        }

        assertNull(lldpLocalGroup03.getLldpLocChassisid());
        assertNull(lldpLocalGroup03.getLldpLocChassisidSubType());
        assertEquals(MKTROUTER3_NAME, lldpLocalGroup03.getLldpLocSysname());

        final List<MtxrLldpRemTableTracker.MtxrLldpRemRow> links03 = new ArrayList<>();

        MtxrLldpLocalTableTracker mikrotikLldpLocalTable03 = new MtxrLldpLocalTableTracker();
        MtxrNeighborTableTracker mikrotikMtxrIndexTable03 = new MtxrNeighborTableTracker();
        MtxrLldpRemTableTracker mikrotikRemTable03 = new MtxrLldpRemTableTracker() {

            public void processMtxrLldpRemRow(final MtxrLldpRemRow row) {
                links03.add(row);
            }
        };

        try {
            m_client.walk(config03,
                            mikrotikRemTable03)
                    .withDescription("lldpRemTable03")
                    .withLocation(null)
                    .execute()
                    .get();
            m_client.walk(config03,
                            mikrotikLldpLocalTable03)
                    .withDescription("mikrotikLldpLocalTable03")
                    .withLocation(null)
                    .execute()
                    .get();
            m_client.walk(config03,
                            mikrotikMtxrIndexTable03)
                    .withDescription("mikrotikMtxrIndexTable03")
                    .withLocation(null)
                    .execute()
                    .get();
        } catch (final InterruptedException e) {
            LOG.info("run: collection interrupted, exiting {}",e.getMessage());
        }
        Map<Integer, MtxrLldpLocalTableTracker.LldpLocalPortRow> mikrotikLldpLocalPortMap03 = mikrotikLldpLocalTable03.getMtxrLldpLocalPortMap();
        Map<Integer, MtxrNeighborTableTracker.MtxrNeighborRow> mikrotikMtrxIndexMap03 = mikrotikMtxrIndexTable03.getMtxrNeighborMap();
        assertEquals(27, mikrotikMtrxIndexMap03.size());
        assertEquals(27, links03.size());
        assertEquals(3, mikrotikLldpLocalPortMap03.size());
        assertTrue(mikrotikLldpLocalPortMap03.containsKey(1));
        assertTrue(mikrotikLldpLocalPortMap03.containsKey(2));
        assertTrue(mikrotikLldpLocalPortMap03.containsKey(3));
        assertEquals(MKTROUTER3_ETHER1_MAC, mikrotikLldpLocalPortMap03.get(1).getLldpLocPortId());
        assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, mikrotikLldpLocalPortMap03.get(1).getLldpLocalPortIdSubtype());
        assertEquals("ether1", mikrotikLldpLocalPortMap03.get(1).getLldpLocPortDesc());

        assertEquals(MKTROUTER3_ETHER2_MAC, mikrotikLldpLocalPortMap03.get(2).getLldpLocPortId());
        assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, mikrotikLldpLocalPortMap03.get(2).getLldpLocalPortIdSubtype());
        assertEquals("ether2",mikrotikLldpLocalPortMap03.get(2).getLldpLocPortDesc());

        assertEquals(MKTROUTER3_ETHER3_MAC, mikrotikLldpLocalPortMap03.get(3).getLldpLocPortId());
        assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, mikrotikLldpLocalPortMap03.get(3).getLldpLocalPortIdSubtype());
        assertEquals("ether3", mikrotikLldpLocalPortMap03.get(3).getLldpLocPortDesc());

        links03.forEach(row -> {
            assertNull(row.getLldpLink().getLldpPortId());
            assertNotNull(row.getMtxrNeighborIndex());
            Integer mtxrIndex = mikrotikMtxrIndexTable03.getMtxrinterfaceId(row);
            assertNotNull(mtxrIndex);
            LldpLink link = mikrotikLldpLocalTable03.getLldpLink(row,mtxrIndex);
            assertNotNull(link.getLldpPortId());
            assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpPortIdSubType());
            assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, link.getLldpRemChassisIdSubType());
            assertEquals("", link.getLldpRemPortDescr());
            if (link.getLldpRemSysname().equals(MKTROUTER3_NAME)) {
                LOG.error("self link {} -> {} id {}", link.getLldpPortId(), link.getLldpRemPortId(), link.getLldpRemChassisId() );
            }
        });

        LldpElement element = mikrotikLldpLocalTable03.getLldpElement(MKTROUTER3_NAME);
        assertEquals(MKTROUTER3_ETHER2_MAC, element.getLldpChassisId());
        assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, element.getLldpChassisIdSubType());

    }


    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:/linkd/nms17216/switch1-walk.txt")
    })
    public void testLldpLocalGroupWalk() throws Exception {

    	String trackerName = "lldpLocalGroup";
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH1_IP));
    	        LldpLocalGroupTracker lldpLocalGroup = new LldpLocalGroupTracker();

        try {
            m_client.walk(config,lldpLocalGroup)
            .withDescription(trackerName)
            .withLocation(null)
            .execute()
            .get();
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
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:/linkd/nms17216/switch1-walk.txt")
    })
    public void testLldpLocGetter() throws Exception {

    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH1_IP));
		
    	final LldpLocPortGetter lldpLocPort = new LldpLocPortGetter(config,m_client,null);
        List<SnmpValue> val = lldpLocPort.get(9);
        assertEquals(3, val.size());
        assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, LldpPortIdSubType.get(val.get(0).toInt()));
        assertEquals("Gi0/9", LldpRemTableTracker.decodeLldpPortId(val.get(0).toInt(), val.get(1)));
        assertEquals("GigabitEthernet0/9", val.get(2).toDisplayString());

        val = lldpLocPort.get(10);
        assertEquals(3, val.size());
        assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, LldpPortIdSubType.get(val.get(0).toInt()));
        assertEquals("Gi0/10", LldpRemTableTracker.decodeLldpPortId(val.get(0).toInt(), val.get(1)));
        assertEquals("GigabitEthernet0/10", val.get(2).toDisplayString());

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource="classpath:/linkd/nms17216/switch2-walk.txt")
    })
    public void test2LldpLocGetter() throws Exception {

        SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH2_IP));
                
        final LldpLocPortGetter lldpLocPort = new LldpLocPortGetter(config,m_client,null);
        List<SnmpValue> val = lldpLocPort.get(1);
        assertEquals(3, val.size());
        assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, LldpPortIdSubType.get(val.get(0).toInt()));
        assertEquals("Gi0/1", LldpRemTableTracker.decodeLldpPortId(val.get(0).toInt(), val.get(1)));
        assertEquals("GigabitEthernet0/1", val.get(2).toDisplayString());

        val = lldpLocPort.get(2);
        assertEquals(3, val.size());
        assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, LldpPortIdSubType.get(val.get(0).toInt()));
        assertEquals("Gi0/2", LldpRemTableTracker.decodeLldpPortId(val.get(0).toInt(), val.get(1)));
        assertEquals("GigabitEthernet0/2", val.get(2).toDisplayString());

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource="classpath:/linkd/nms17216/switch2-walk.txt")
    })
    public void test3LldpRemoteTableWalk() throws Exception {

        SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH2_IP));
        final List<LldpRemTableTracker.LldpRemRow> links = new ArrayList<>();
                
        LldpRemTableTracker lldpRemTable = new LldpRemTableTracker() {

            public void processLldpRemRow(final LldpRemRow row) {
                    links.add(row);
            }
        };
        try {
            m_client.walk(config,
                          lldpRemTable)
                          .withDescription("lldpRemTable")
                          .withLocation(null)
                          .execute()
                          .get();
        } catch (ExecutionException e) {
            // pass
            LOG.error("run: collection failed, exiting",e);
            return;
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }
        final LldpLocPortGetter lldpLocPort = new LldpLocPortGetter(config,
                                                                    m_client,
                                                                    null);

        for (LldpRemTableTracker.LldpRemRow row : links) {
            assertNotNull(row);
            LldpLink link = row.getLldpLink();
            assertNotNull(link.getLldpLocalPortNum());
            assertNull(link.getLldpPortId());
            assertNull(link.getLldpPortIdSubType());
            assertNull(link.getLldpPortDescr());
            
            LldpLink updated = lldpLocPort.getLldpLink(row);
            assertNotNull(updated.getLldpPortId());
            assertEquals(5, updated.getLldpPortIdSubType().getValue().intValue());
            assertNotNull(updated.getLldpPortDescr());
        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:/linkd/nms17216/switch1-walk.txt")
    })
    public void testLldpRemTableWalk() throws Exception {
		
        final SnmpAgentConfig config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH1_IP));
        LldpRemTableTracker lldpRemTable = new LldpRemTableTracker() {
            
        	public void processLldpRemRow(final LldpRemRow row) {
        		
        		
        		System.err.println("----------lldp rem----------------");
        		System.err.println("columns number in the row: " + row.getColumnCount());

        		assertEquals(6, row.getColumnCount());
        		LldpLink link = row.getLldpLink();

        		System.err.println("local port number: " + row.getLldpRemLocalPortNum());
        		System.err.println("remote chassis: " + link.getLldpRemChassisId());
        		System.err.println("remote chassis type: " + LldpChassisIdSubType.getTypeString(link.getLldpRemChassisIdSubType().getValue()));
        		assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, link.getLldpRemChassisIdSubType());

        		System.err.println("remote port id: " + link.getLldpRemPortId());
        		System.err.println("remote port type: " + LldpPortIdSubType.getTypeString(link.getLldpRemPortIdSubType().getValue()));
        		assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpRemPortIdSubType());
            }
        };

        try {
            m_client.walk(config,
                          lldpRemTable)
                          .withDescription("lldpRemTable")
                          .withLocation(null)
                          .execute()
                          .get();
        } catch (final InterruptedException e) {
            fail();
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

        try {
            m_client.walk(config,
                          tracker)
                          .withDescription(trackerName)
                          .withLocation(null)
                          .execute()
                          .get();
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

    	final List<IsIsLink> links = new ArrayList<>();
    	String trackerName = "isisISAdjTable";
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SIEGFRIE_IP));
        IsisISAdjTableTracker tracker = new IsisISAdjTableTracker() {
        	public void processIsisAdjRow(final IsIsAdjRow row) {
        		assertEquals(5, row.getColumnCount());
        		links.add(row.getIsisLink());
            }
        };

        try {
            m_client.walk(config,
                          tracker)
                          .withDescription(trackerName)
                          .withLocation(null)
                          .execute()
                          .get();
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
    		if (link.getIsisCircIndex() == 533) {
    			assertEquals("001f12accbf0", link.getIsisISAdjNeighSNPAAddress());
    			assertEquals("000110255062",link.getIsisISAdjNeighSysID());
    		} else if (link.getIsisCircIndex() == 552) {
    			assertEquals("0021590e47c2", link.getIsisISAdjNeighSNPAAddress());
    			assertEquals("000110088500",link.getIsisISAdjNeighSysID());
    		} else {
                fail();
    		}

        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = SIEGFRIE_SNMP_RESOURCE)
    })
    public void testIsisCircTableWalk() throws Exception {

    	final List<IsIsLink> links = new ArrayList<>();
    	String trackerName = "isisCircTable";
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SIEGFRIE_IP));
        IsisCircTableTracker tracker = new IsisCircTableTracker() {
        	public void processIsisCircRow(final IsIsCircRow row) {
        		assertEquals(2, row.getColumnCount());
        		links.add(row.getIsisLink());
            }
        };

        try {
            m_client.walk(config,
                          tracker)
                          .withDescription(trackerName)
                          .withLocation(null)
                          .execute()
                          .get();
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }

        assertEquals(12, links.size());

        for (final IsIsLink link: links) {
    		if (link.getIsisCircIndex() == 533) {
    			assertEquals(533, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
    		} else if (link.getIsisCircIndex() == 552) {
    			assertEquals(552, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
    		} else if (link.getIsisCircIndex() == 13) {
    			assertEquals(13, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.off, link.getIsisCircAdminState());
    		} else if (link.getIsisCircIndex() == 16) {
    			assertEquals(16, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
    		} else if (link.getIsisCircIndex() == 504) {
    			assertEquals(504, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
    		} else if (link.getIsisCircIndex() == 507) {
    			assertEquals(507, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
    		} else if (link.getIsisCircIndex() == 508) {
    			assertEquals(508, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
    		} else if (link.getIsisCircIndex() == 512) {
    			assertEquals(512, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
       		} else if (link.getIsisCircIndex() == 514) {
    			assertEquals(514, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
       		} else if (link.getIsisCircIndex() == 531) {
    			assertEquals(531, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
       		} else if (link.getIsisCircIndex() == 572) {
    			assertEquals(572, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
       		} else if (link.getIsisCircIndex() == 573) {
    			assertEquals(573, link.getIsisCircIfIndex().intValue());
            	assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
     		} else {
                fail();
    		}

        }
    }
        
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MIKROTIK_IP, port=161, resource=MIKROTIK_SNMP_RESOURCE)
    })
    public void testIpNetToMediaTableWalk() throws Exception {

    	final List<IpNetToMedia> rows = new ArrayList<>();
    	String trackerName = "ipNetToMediaTable";
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(MIKROTIK_IP));
        IpNetToMediaTableTracker tracker = new IpNetToMediaTableTracker() {
        	public void processIpNetToMediaRow(final IpNetToMediaRow row) {
        		rows.add(row.getIpNetToMedia());
            }
        };

        try {
            m_client.walk(config,
                          tracker)
                          .withDescription(trackerName)
                          .withLocation(null)
                          .execute()
                          .get();
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }

        assertEquals(6, rows.size());

        for (final IpNetToMedia row: rows) {
    		assertEquals(IpNetToMediaType.IPNETTOMEDIA_TYPE_DYNAMIC,row.getIpNetToMediaType());
            switch (row.getPhysAddress()) {
                case "00901a4222f8":
                    assertEquals(InetAddressUtils.addr("10.129.16.1"), row.getNetAddress());
                    assertEquals(1, row.getSourceIfIndex().intValue());
                    break;
                case "0013c8f1d242":
                    assertEquals(InetAddressUtils.addr("10.129.16.164"), row.getNetAddress());
                    assertEquals(1, row.getSourceIfIndex().intValue());
                    break;
                case "f0728c99994d":
                    assertEquals(InetAddressUtils.addr("192.168.0.13"), row.getNetAddress());
                    assertEquals(2, row.getSourceIfIndex().intValue());
                    break;
                case "0015999f07ef":
                    assertEquals(InetAddressUtils.addr("192.168.0.14"), row.getNetAddress());
                    assertEquals(2, row.getSourceIfIndex().intValue());
                    break;
                case "60334b0817a8":
                    assertEquals(InetAddressUtils.addr("192.168.0.16"), row.getNetAddress());
                    assertEquals(2, row.getSourceIfIndex().intValue());
                    break;
                case "001b63cda9fd":
                    assertEquals(InetAddressUtils.addr("192.168.0.17"), row.getNetAddress());
                    assertEquals(2, row.getSourceIfIndex().intValue());
                    break;
                default:
                    fail();
                    break;
            }
        }        
        
    }

    @Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host=DLINK1_IP, port=161, resource=DLINK1_SNMP_RESOURCE),
    })
    public void testDot1dBaseWalk() throws Exception {

    	String trackerName = "dot1dbase";
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(DLINK1_IP));
        Dot1dBaseTracker tracker = new Dot1dBaseTracker();

        try {
            m_client.walk(config,
                          tracker)
                          .withDescription(trackerName)
                          .withLocation(null)
                          .execute()
                          .get();
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }

    	final BridgeElement bridge =tracker.getBridgeElement();
    	assertEquals("001e58a32fcd", bridge.getBaseBridgeAddress());
    	assertEquals(26, bridge.getBaseNumPorts().intValue());
    	assertEquals(BridgeDot1dBaseType.DOT1DBASETYPE_TRANSPARENT_ONLY,bridge.getBaseType());
    	assertEquals(BridgeDot1dStpProtocolSpecification.DOT1D_STP_PROTOCOL_SPECIFICATION_IEEE8021D,bridge.getStpProtocolSpecification());
    	assertEquals(32768,bridge.getStpPriority().intValue());
    	assertEquals("0000000000000000",bridge.getStpDesignatedRoot());
    	assertEquals(0, bridge.getStpRootCost().intValue());
    	assertEquals(0, bridge.getStpRootPort().intValue());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DLINK1_IP, port=161, resource=DLINK1_SNMP_RESOURCE),
    })
    public void testDot1dBasePortTableWalk() throws Exception {

    	String trackerName = "dot1dbasePortTable";
    	final List<Dot1dBasePortRow> rows = new ArrayList<>();
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(DLINK1_IP));
        Dot1dBasePortTableTracker tracker = new Dot1dBasePortTableTracker() {
            @Override
        	public void processDot1dBasePortRow(final Dot1dBasePortRow row) {
            	rows.add(row);
            }
        };


        try {
            m_client.walk(config,
                          tracker)
                          .withDescription(trackerName)
                          .withLocation(null)
                          .execute()
                          .get();
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }

        assertEquals(26, rows.size());
        for (Dot1dBasePortRow row: rows) {
        	assertEquals(row.getBaseBridgePort().intValue(), row.getBaseBridgePortIfindex().intValue());
        }
    }

    @Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host=DLINK1_IP, port=161, resource=DLINK1_SNMP_RESOURCE),
    })
    public void testDot1dStpPortTableWalk() throws Exception {

    	String trackerName = "dot1dbaseStpTable";
    	final List<BridgeStpLink> links = new ArrayList<>();
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(DLINK1_IP));
        Dot1dStpPortTableTracker tracker = new Dot1dStpPortTableTracker() {
            @Override
        	public void processDot1dStpPortRow(final Dot1dStpPortRow row) {
            	links.add(row.getLink());
            }
        };

        try {
            m_client.walk(config,
                          tracker)
                          .withDescription(trackerName)
                          .withLocation(null)
                          .execute()
                          .get();
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }

        assertEquals(26, links.size());
        int i = 0;
        for (BridgeStpLink link: links) {
        	assertEquals(++i, link.getStpPort().intValue());
        	assertEquals(128, link.getStpPortPriority().intValue());
        	if (link.getStpPort() <= 6 || link.getStpPort() == 24 )
        		assertEquals(BridgeDot1dStpPortState.DOT1D_STP_PORT_STATUS_FORWARDING, link.getStpPortState());
        	else
        		assertEquals(BridgeDot1dStpPortState.DOT1D_STP_PORT_STATUS_DISABLED, link.getStpPortState());
        	assertEquals(BridgeDot1dStpPortEnable.DOT1D_STP_PORT_ENABLED, link.getStpPortEnable());
        	assertEquals(2000000,link.getStpPortPathCost().intValue());
        	assertEquals("0000000000000000",link.getDesignatedRoot());
        	assertEquals(0,link.getDesignatedCost().intValue());
        	assertEquals("0000000000000000",link.getDesignatedBridge());
        	assertEquals("0000",link.getDesignatedPort());
        }
    }

    @Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host=DLINK1_IP, port=161, resource=DLINK1_SNMP_RESOURCE),
    })
    public void testDot1dTpFdbTableWalk() throws Exception {

    	String trackerName = "dot1dTpFdbTable";
    	final List<BridgeForwardingTableEntry> links = new ArrayList<>();
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(DLINK1_IP));
        Dot1dTpFdbTableTracker tracker = new Dot1dTpFdbTableTracker() {
            @Override
        	public void processDot1dTpFdbRow(final Dot1dTpFdbRow row) {
            	links.add(row.getLink());
            }
        };

        try {
            m_client.walk(config,
                          tracker)
                          .withDescription(trackerName)
                          .withLocation(null)
                          .execute()
                          .get();
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }

        assertEquals(17, links.size());
        for (BridgeForwardingTableEntry link: links) {
        	assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
        	System.out.println(link.getMacAddress());
            switch (link.getMacAddress()) {
                case "000c29dcc076":
                case "f07d68711f89":
                case "f07d6876c565":
                    assertEquals(24, link.getBridgePort().intValue());
                    break;
                case "000ffeb10d1e":
                case "000ffeb10e26":
                case "001a4b802790":
                case "001d6004acbc":
                case "001e58865d0f":
                case "0021913b5108":
                case "002401ad3416":
                case "00248c4c8bd0":
                case "0024d608693e":
                case "1caff737cc33":
                case "1caff7443339":
                case "1cbdb9b56160":
                case "5cd998667abb":
                case "e0cb4e3e7fc0":
                    assertEquals(6, link.getBridgePort().intValue());
                    break;
                default:
                    fail();
                    break;
            }
        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DLINK1_IP, port=161, resource=DLINK1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=DLINK2_IP, port=161, resource=DLINK2_SNMP_RESOURCE)
    })
    public void testDot1qTpFdbTableWalk() throws Exception {

    	String trackerName = "dot1qTpFdbTable";
    	final Map<String,Integer> macs1 = new HashMap<>();
    	final Map<String,Integer> macs2 = new HashMap<>();
    	SnmpAgentConfig  config1 = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(DLINK1_IP));
        Dot1qTpFdbTableTracker tracker1 = new Dot1qTpFdbTableTracker() {
            @Override
        	public void processDot1qTpFdbRow(final Dot1qTpFdbRow row) {
            	macs1.put(row.getDot1qTpFdbAddress(),row.getDot1qTpFdbPort());
            }
        };

        try {
            m_client.walk(config1,
                          tracker1)
                          .withDescription(trackerName)
                          .withLocation(null)
                          .execute()
                          .get();
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }

        SnmpAgentConfig  config2 = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(DLINK2_IP));
        Dot1qTpFdbTableTracker tracker2 = new Dot1qTpFdbTableTracker() {
            @Override
        	public void processDot1qTpFdbRow(final Dot1qTpFdbRow row) {
            	macs2.put(row.getDot1qTpFdbAddress(),row.getDot1qTpFdbPort());
            }
        };

        try {
            m_client.walk(config2,
                          tracker2)
                          .withDescription(trackerName)
                          .withLocation(null)
                          .execute()
                          .get();
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }

        assertEquals(59, macs1.size());
        assertEquals(979, macs2.size());

       for (Entry<String,Integer> entry: macs1.entrySet()) {
        	if (macs2.containsKey(entry.getKey())) {
                System.out.println("-----------mac on 1 learned on 2 port-----------------");
        		System.out.println("Mac: " + entry.getKey());
        		System.out.println("learned on PortOn1: " + entry.getValue());
        		System.out.println("learned on PortOn2: " + macs2.get(entry.getKey()));
        	} else {
                System.out.println("-----------mac found on 1 not learned on 2 port-----------------");
        		System.out.println("Mac: " + entry.getKey());
        		System.out.println("learned on PortOn1: " + entry.getValue());
        	}
        }

        for (Entry<String,Integer> entry: macs2.entrySet()) {
        	if (macs1.containsKey(entry.getKey())) {
           	    System.out.println("-----------mac on 2 learned on 1 port-----------------");
        	    System.out.println("Mac: " + entry.getKey());
        		System.out.println("learned on PortOn2: " + entry.getValue());
        		System.out.println("learned on PortOn1: " + macs1.get(entry.getKey()));
        	}
        }

        
    }

}
