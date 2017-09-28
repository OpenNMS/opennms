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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;




import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.enlinkd.snmp.OspfGeneralGroupTracker;
import org.opennms.netmgt.enlinkd.snmp.OspfIfTableTracker;
import org.opennms.netmgt.enlinkd.snmp.OspfIpAddrTableGetter;
import org.opennms.netmgt.enlinkd.snmp.OspfNbrTableTracker;
import org.opennms.netmgt.model.OspfElement.Status;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session
 * creating and collection occurs in the main run method of the instance. This
 * allows the collection to occur in a thread if necessary.
 */
public final class NodeDiscoveryOspf extends NodeDiscovery {
    
	private final static Logger LOG = LoggerFactory.getLogger(NodeDiscoveryOspf.class);
	/**
	 * Constructs a new SNMP collector for Ospf Node Discovery. 
	 * The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
	 * @param EnhancedLinkd linkd
	 * @param LinkableNode node
	 */
    public NodeDiscoveryOspf(final EnhancedLinkd linkd, final Node node) {
    	super(linkd, node);
    }

    protected void runCollection() {

    	final Date now = new Date(); 

    	String trackerName = "ospfGeneralGroup";
        final OspfGeneralGroupTracker ospfGeneralGroup = new OspfGeneralGroupTracker();
		LOG.info( "run: node[{}]: collecting {} on: {}",
				getNodeId(),
				trackerName, 
				getPrimaryIpAddressString());
        SnmpWalker walker =  SnmpUtils.createWalker(getPeer(), trackerName, ospfGeneralGroup);

        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
            	LOG.info(
                        "run:Aborting Ospf Linkd node scan : Agent timed out while scanning the {} table", trackerName);
            	return;
            }  else if (walker.failed()) {
            	LOG.info(
                        "run:Aborting Ospf Linkd node scan : Agent failed while scanning the {} table: {}", trackerName,walker.getErrorMessage());
            	return;
            }
        } catch (final InterruptedException e) {
            LOG.error( "run: Ospf Linkd node collection interrupted, exiting",e);
            return;
        }
        
        if (ospfGeneralGroup.getOspfRouterId() == null ) {
    		LOG.info( "run: node[{}]: address {}. ospf mib not supported",
    				getNodeId(),
    				getPrimaryIpAddressString());
            return;
        } 

        if (ospfGeneralGroup.getOspfRouterId().equals(InetAddressUtils.addr("0.0.0.0"))) {
    		LOG.info( "run: node[{}]: address {}. ospf identifier 0.0.0.0 is not valid",
    				getNodeId(),
    				getPrimaryIpAddressString());
            return;
        } 

        if (Status.get(ospfGeneralGroup.getOspfAdminStat()) == Status.disabled) {
    		LOG.info( "run: node[{}]: address {}. ospf status: disabled",
    				getNodeId(),
    				getPrimaryIpAddressString());
            return;
        }
        
        final OspfIpAddrTableGetter ipAddrTableGetter = new OspfIpAddrTableGetter(getPeer());

        m_linkd.getQueryManager().store(getNodeId(), ipAddrTableGetter.get(ospfGeneralGroup.getOspfElement()));

        trackerName = "ospfNbrTable";
        final List<OspfLink> links = new ArrayList<OspfLink>();
        OspfNbrTableTracker ospfNbrTableTracker = new OspfNbrTableTracker() {

        	public void processOspfNbrRow(final OspfNbrRow row) {
        		links.add(row.getOspfLink());
        	}
        };

		LOG.info( "run: node[{}]: collecting {} on: {}",
				getNodeId(),
				trackerName, 
				getPrimaryIpAddressString());
        walker = SnmpUtils.createWalker(getPeer(), trackerName, ospfNbrTableTracker);
        walker.start();
        
        try {
            walker.waitFor();
            if (walker.timedOut()) {
            	LOG.info(
                        "run:Aborting Ospf Linkd node scan : Agent timed out while scanning the {} table", trackerName);
            	return;
            }  else if (walker.failed()) {
            	LOG.info(
                        "run:Aborting Ospf Linkd node scan : Agent failed while scanning the {} table: {}", trackerName,walker.getErrorMessage());
            	return;
            }
        } catch (final InterruptedException e) {
            LOG.error( "run: collection interrupted, exiting",e);
            return;
        }

        trackerName = "ospfIfTable";
        OspfIfTableTracker ospfIfTableTracker = new OspfIfTableTracker() {

        	public void processOspfIfRow(final OspfIfRow row) {
        		OspfLink link = row.getOspfLink(ipAddrTableGetter);
    			for (OspfLink nbrlink : links) {
    				if (InetAddressUtils.inSameNetwork(link.getOspfIpAddr(),nbrlink.getOspfRemIpAddr(),link.getOspfIpMask())) {
    					nbrlink.setOspfIpAddr(link.getOspfIpAddr());
    					nbrlink.setOspfAddressLessIndex(link.getOspfAddressLessIndex());
    					nbrlink.setOspfIpMask(link.getOspfIpMask());
    					nbrlink.setOspfIfIndex(link.getOspfIfIndex());
    				}
    			}
        	}

        };

		LOG.info( "run: node[{}]: collecting {} on: {}",
				getNodeId(),
				trackerName, 
				getPrimaryIpAddressString());
        walker = SnmpUtils.createWalker(getPeer(), trackerName, ospfIfTableTracker);
        walker.start();
        
        try {
            walker.waitFor();
            if (walker.timedOut()) {
            	LOG.info(
                        "run:Aborting Ospf Linkd node scan : Agent timed out while scanning the {} table", trackerName);
            	return;
            }  else if (walker.failed()) {
            	LOG.info(
                        "run:Aborting Ospf Linkd node scan : Agent failed while scanning the {} table: {}", trackerName,walker.getErrorMessage());
            	return;
            }
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }

        for (OspfLink link: links)
    		m_linkd.getQueryManager().store(getNodeId(),link);

        m_linkd.getQueryManager().reconcileOspf(getNodeId(),now);
    }

	@Override
	public String getName() {
		return "OspfLinkDiscovery";
	}

}
