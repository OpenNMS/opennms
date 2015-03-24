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


import java.util.List;

import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.CdpLink.CiscoNetworkProtocolType;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OspfElement;
import org.opennms.netmgt.model.OspfElement.Status;
import org.opennms.netmgt.model.OspfElement.TruthValue;
import org.opennms.netmgt.model.OspfLink;
import static org.opennms.core.utils.InetAddressUtils.str;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */

public abstract class EnLinkdTestHelper {
        
	protected static void printLldpTopology(List<LldpLink> lldplinks) {
		for (LldpLink link: lldplinks)
			printLldpLink(link);
	}

	protected static void printLldpElements(List<LldpElement> lldpelements) {
		for (LldpElement element: lldpelements)
			printLldpElement(element);
	}

    protected static void printCdpElement(final CdpElement cdpElement) {
        System.err.println("----------cdp element --------");
        System.err.println("Nodeid: " + cdpElement.getNode().getId());
        System.err.println("Cdp Global Device Id: " + cdpElement.getCdpGlobalDeviceId());
        System.err.println("Cdp Global Run: " + TruthValue.getTypeString(cdpElement.getCdpGlobalRun().getValue()));
    }

    protected static void printCdpLink(CdpLink link) {
        System.err.println("----------cdp link --------");
        System.err.println("Create time: " + link.getCdpLinkCreateTime());
        System.err.println("Last Poll time: " + link.getCdpLinkLastPollTime());
        System.err.println("----------Source Node--------");
        System.err.println("Nodeid: " + link.getNode().getId());
        System.err.println("----------Source Port--------");
        System.err.println("cdpcacheifindex: " + link.getCdpCacheIfIndex());
        System.err.println("cdpcachedeviceindex: " + link.getCdpCacheDeviceIndex());
        System.err.println("cdpinterfacename: " + link.getCdpInterfaceName());
        System.err.println("----------Rem Node--------");
        System.err.println("cdpcacheaddresstype: " + CiscoNetworkProtocolType.getTypeString(link.getCdpCacheAddressType().getValue()));
        System.err.println("cdpcacheaddress: " + link.getCdpCacheAddress());
        System.err.println("cdpcacheversion: " + link.getCdpCacheVersion());
        System.err.println("cdpcachedeviceid: " + link.getCdpCacheDeviceId());
        System.err.println("cdpcachedeviceplatform: " + link.getCdpCacheDevicePlatform());
        System.err.println("----------Remote Port--------");
        System.err.println("cdpcachedeviceport: " + link.getCdpCacheDevicePort());
        System.err.println("");
    }

        protected static void printLldpElement(final LldpElement lldpElement) {
    	System.err.println("----------lldp element --------");
    	System.err.println("Nodeid: " + lldpElement.getNode().getId());
    	System.err.println("lldp chassis type/id: " + LldpChassisIdSubType.getTypeString(lldpElement.getLldpChassisIdSubType().getValue())+"/"+lldpElement.getLldpChassisId());
    	System.err.println("lldp sysname: " + lldpElement.getLldpSysname());
	}
	
	protected static void printLldpLink(LldpLink link) {
    	System.err.println("----------lldp link --------");
    	System.err.println("Create time: " + link.getLldpLinkCreateTime());
    	System.err.println("Last Poll time: " + link.getLldpLinkLastPollTime());
    	System.err.println("----------Source Node--------");
    	System.err.println("Nodeid: " + link.getNode().getId());
    	System.err.println("----------Source Port--------");
    	System.err.println("lldp port num: " + link.getLldpLocalPortNum());
        System.err.println("lldp port ifindex: " + link.getLldpPortIfindex());
    	System.err.println("lldp port type/id: " + LldpPortIdSubType.getTypeString(link.getLldpPortIdSubType().getValue())+"/" + link.getLldpPortId());
    	System.err.println("lldp port descr: " + link.getLldpPortDescr());
    	System.err.println("----------Rem Node--------");
    	System.err.println("lldp rem chassis type/id: " + LldpChassisIdSubType.getTypeString(link.getLldpRemChassisIdSubType().getValue())+"/"+link.getLldpRemChassisId());
    	System.err.println("lldp rem sysname: " + link.getLldpRemSysname());
    	System.err.println("----------Remote Port--------");
    	System.err.println("lldp rem port type/id: " + LldpPortIdSubType.getTypeString(link.getLldpRemPortIdSubType().getValue())+"/" + link.getLldpRemPortId());
    	System.err.println("lldp rem port descr: " + link.getLldpRemPortDescr());
    	System.err.println("");
    }
	
	protected static void printOspfTopology(List<OspfLink> ospflinks) {
		for (OspfLink link: ospflinks)
			printOspfLink(link);
	}

	protected static void printOspfElements(List<OspfElement> ospfelements) {
		for (OspfElement element: ospfelements)
			printOspfElement(element);
	}
	protected static void printOspfElement(final OspfElement element) {
    	System.err.println("----------ospf element --------");
    	System.err.println("Nodeid: " + element.getNode().getId());
    	System.err.println("ospf router id/mask/ifindex: " + str(element.getOspfRouterId())+"/"+str(element.getOspfRouterIdNetmask())+"/"+element.getOspfRouterIdIfindex());
    	System.err.println("ospf admin status: " + Status.getTypeString(element.getOspfAdminStat().getValue()));
    	System.err.println("ospf version number: " + element.getOspfVersionNumber());
    	System.err.println("ospf Border Router Status: " + TruthValue.getTypeString(element.getOspfBdrRtrStatus().getValue()));
    	System.err.println("ospf AS Boder Router Status: " + TruthValue.getTypeString(element.getOspfASBdrRtrStatus().getValue()));
       	System.err.println("");
        	}
	
	protected static void printOspfLink(OspfLink link) {
    	System.err.println("----------ospf link --------");
    	System.err.println("Create time: " + link.getOspfLinkCreateTime());
    	System.err.println("Last Poll time: " + link.getOspfLinkLastPollTime());
    	System.err.println("----------Source Node--------");
    	System.err.println("Nodeid: " + link.getNode().getId());
    	System.err.println("----------Source Port--------");
    	System.err.println("ospf router id/mask/ifindex/addressleifindex: " + str(link.getOspfIpAddr())+"/"+str(link.getOspfIpMask())+"/"+link.getOspfIfIndex()+"/"+link.getOspfAddressLessIndex());
    	System.err.println("----------Rem Node--------");
    	System.err.println("ospf rem router id: " + str(link.getOspfRemRouterId()));
    	System.err.println("----------Remote Port--------");
    	System.err.println("ospf rem router ip: " + str(link.getOspfRemIpAddr()));
       	System.err.println("ospf rem router address less ifindex: " + link.getOspfRemAddressLessIndex());
    	System.err.println("");
    }
	
	protected static void printBridgeMacLink(BridgeMacLink link) {
    	System.err.println("----------mac link --------");
    	System.err.println("Create time: " + link.getBridgeMacLinkCreateTime());
    	System.err.println("Last Poll time: " + link.getBridgeMacLinkLastPollTime());
    	System.err.println("----------Source Mac Address--------");
    	System.err.println("Mac: " + link.getMacAddress());
    	System.err.println("----------Target Node--------");
    	System.err.println("Nodeid: " + link.getNode().getId());
    	System.err.println("----------Target Bridge Port--------");
    	System.err.println("BridgePort: " + link.getBridgePort());
    	System.err.println("----------Target IfIndex--------");
    	System.err.println("IfIndex: " + link.getBridgePortIfIndex());
    	System.err.println("");
	}


	protected static void printBackboneBridgeMacLink(BridgeMacLink link1, BridgeMacLink link2) {
    	
		if (link1.getMacAddress().equals(link2.getMacAddress())) {
		System.err.println("nodeid: "+link1.getNode().getId()+" port:"
    	+ link1.getBridgePort() + "-->" +
				link1.getMacAddress() 
    	+ "<-- port: " + link2.getBridgePort() + " nodeid: " + link2.getNode().getId());  
		}
	}

	
}
