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


import java.util.List;

import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpElement.LldpChassisIdSubType;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.LldpLink.LldpPortIdSubType;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */

public abstract class EnLinkdTestHelper {
        
	protected static void printLldpElement(final LldpElement lldpElement) {
    	System.err.println("----------lldp element --------");
    	System.err.println("Nodeid: " + lldpElement.getNode().getId());
    	System.err.println("lldp chassis type/id: " + LldpChassisIdSubType.getTypeString(lldpElement.getLldpChassisIdSubType().getValue())+"/"+lldpElement.getLldpChassisId());
    	System.err.println("lldp sysname: " + lldpElement.getLldpSysname());
	}
	
	protected static void printLldpTopology(List<LldpLink> lldplinks) {
		for (LldpLink link: lldplinks)
			printLldpLink(link);
	}
	/*
    protected static List<EndPoint> printEndPointTopology(final List<TopologyElement> topology) {

    	List<EndPoint> endpoints = new ArrayList<EndPoint>();

        for (final TopologyElement e: topology) {
        	System.err.println("---------- element --------");
			if (e == null)
				continue;
        	for (ElementIdentifier iden: e.getElementIdentifiers()) {
        		printElementIdentifier(iden);
        	}
        	for (EndPoint ep: e.getEndpoints()) {
        		if (!endpoints.contains(ep)) {
        			endpoints.add(ep);
        			printEndPoint(ep);
        		}
        	}
        	System.err.println("");
        }
        return endpoints;
	
    }
    
    protected static List<Link> printLinkTopology(final List<TopologyElement> topology) {

    	List<Link> links = new ArrayList<Link>();

        for (final TopologyElement e: topology) {
			if (e == null)
				continue;
        	for (EndPoint ep: e.getEndpoints()) {
        		if (ep.hasLink() && !links.contains(ep.getLink())) {
        			links.add(ep.getLink());
        			printLink(ep.getLink());
        		}
        	}
        	
        }
        return links;
	
    }

    protected static void printElementIdentifier(ElementIdentifier iden) {
    	if (iden instanceof NodeElementIdentifier) 
			System.err.println("node: " + ((NodeElementIdentifier)iden).getNodeid()+" " + iden.getLastPoll());
		else if (iden instanceof LldpElementIdentifier)
			System.err.println("lldp: " + ((LldpElementIdentifier)iden).getLldpChassisId()+" " + iden.getLastPoll());
		else if (iden instanceof CdpElementIdentifier)
			System.err.println("cdp: " + ((CdpElementIdentifier)iden).getCdpDeviceId()+" " + iden.getLastPoll());
		else if (iden instanceof OspfElementIdentifier)
			System.err.println("ospf: " + str(((OspfElementIdentifier)iden).getOspfRouterId())+" " + iden.getLastPoll());    	
		else if (iden instanceof BridgeElementIdentifier)
			System.err.println("bridge: " + ((BridgeElementIdentifier)iden).getBridgeAddress()+" " + iden.getLastPoll());    	
		else if (iden instanceof MacAddrElementIdentifier)
			System.err.println("mac: " + ((MacAddrElementIdentifier)iden).getMacAddr()+" " + iden.getLastPoll());    	
		else if (iden instanceof InetElementIdentifier)
			System.err.println("inet: " + str(((InetElementIdentifier)iden).getInet())+" " + iden.getLastPoll());    	
		else if (iden instanceof PseudoBridgeElementIdentifier) {
			System.err.println("pseudo linked bridge/port: "
					+ ((PseudoBridgeElementIdentifier) iden)
							.getLinkedBridgeIdentifier()
					+ "/"
					+ ((PseudoBridgeElementIdentifier) iden)
							.getLinkedBridgePort() + " " + iden.getLastPoll());
		}
    }
    
    protected static void printEndPoint(EndPoint ep) {
		if (ep instanceof LldpEndPoint) {
			LldpEndPoint lldpep = (LldpEndPoint) ep;
			System.err.println("Lldp Endpoint: " + lldpep.getLldpPortId() + " "
					+ ep.getLastPoll());
		} else if (ep instanceof CdpEndPoint) {
			CdpEndPoint cdpep = (CdpEndPoint) ep;
			System.err.println("Cdp Endpoint Port/IfIndex: "
					+ cdpep.getCdpCacheDevicePort() + "/"
					+ cdpep.getIfIndex() + " " + ep.getLastPoll());
		} else if (ep instanceof OspfEndPoint) {
			OspfEndPoint ospfep = (OspfEndPoint) ep;
			System.err
					.println("Ospf Endpoint ipAddress/Address less IfIndex/NetMask/IfIndex "
							+ str(ospfep.getOspfIpAddr())
							+ "/"
							+ ospfep.getOspfAddressLessIndex()
							+ "/"
							+ str(ospfep.getOspfIpMask())
							+ "/"
							+ ospfep.getOspfIfIndex() + " " + ep.getLastPoll());
		} else if (ep instanceof BridgeEndPoint) {
			BridgeEndPoint bridgeep = (BridgeEndPoint) ep;
			System.err.println("Bridge Endpoint port: "
					+ bridgeep.getBridgePort() + " " + ep.getLastPoll());
		} else if (ep instanceof MacAddrEndPoint) {
			MacAddrEndPoint macep = (MacAddrEndPoint) ep;
			System.err.println("Mac Endpoint: " + macep.getMacAddress() + " "
					+ ep.getLastPoll());
		} else if (ep instanceof PseudoMacEndPoint) {
			PseudoMacEndPoint pseudoep = (PseudoMacEndPoint) ep;
			System.err.println("Pseudo Mac Endpoint:linked bridge/port/mac:  "
					+ pseudoep.getLinkedBridgeIdentifier()+"/" + pseudoep.getLinkedBridgePort()+"/"+pseudoep.getLinkedMacAddress() + " " + ep.getLastPoll());
		} else if (ep instanceof PseudoBridgeEndPoint) {
			PseudoBridgeEndPoint pseudoep = (PseudoBridgeEndPoint) ep;
			System.err.println("Pseudo Bridge Endpoint:linked bridge/port:  "
					+ pseudoep.getLinkedBridgeIdentifier()+"/" + pseudoep.getLinkedBridgePort()+ " " + ep.getLastPoll());
		}
    }
    */
    private static void printLldpLink(LldpLink link) {
    	System.err.println("----------lldp link --------");
    	System.err.println("Create time: " + link.getLldpLinkCreateTime());
    	System.err.println("Last Poll time: " + link.getLldpLinkLastPollTime());
    	System.err.println("----------Source Node--------");
    	System.err.println("Nodeid: " + link.getNode().getId());
    	System.err.println("----------Source Port--------");
    	System.err.println("lldp port num: " + link.getLldpLocalPortNum());
    	System.err.println("lldp port type/id: " + LldpPortIdSubType.getTypeString(link.getLldpPortIdSubType().getValue())+"/" + link.getLldpPortId());
    	System.err.println("lldp port descr: " + link.getLldpPortDescr());
    	System.err.println("----------Rem Node--------");
    	System.err.println("lldp rem chassis type/id: " + LldpChassisIdSubType.getTypeString(link.getLldpRemChassisIdSubType().getValue())+"/"+link.getLldpRemChassisId());
    	System.err.println("lldp rem sysname: " + link.getLldpRemSysname());
    	System.err.println("----------Source Port--------");
    	System.err.println("lldp rem port type/id: " + LldpPortIdSubType.getTypeString(link.getLldpRemPortIdSubType().getValue())+"/" + link.getLldpRemPortId());
    	System.err.println("lldp rem port descr: " + link.getLldpRemPortDescr());
    	System.err.println("");
    }
}
