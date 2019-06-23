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




import java.util.concurrent.ExecutionException;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.enlinkd.snmp.OspfGeneralGroupTracker;
import org.opennms.netmgt.enlinkd.snmp.OspfIfTableTracker;
import org.opennms.netmgt.enlinkd.snmp.OspfIpAddrTableGetter;
import org.opennms.netmgt.enlinkd.snmp.OspfNbrTableTracker;
import org.opennms.netmgt.model.OspfElement.Status;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
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

    protected void runNodeDiscovery() {

    	final Date now = new Date(); 

        SnmpAgentConfig peer = m_linkd.getSnmpAgentConfig(getPrimaryIpAddress(), getLocation());

        final OspfIpAddrTableGetter ipAddrTableGetter = new OspfIpAddrTableGetter(peer,
                                                                                  m_linkd.getLocationAwareSnmpClient(),
                                                                                  getLocation(),getNodeId());
        final OspfGeneralGroupTracker ospfGeneralGroup = new OspfGeneralGroupTracker();
        try {
            m_linkd.getLocationAwareSnmpClient().walk(peer, ospfGeneralGroup).
            withDescription("ospfGeneralGroup").
            withLocation(getLocation()).
            execute().
            get();
       } catch (ExecutionException e) {
           LOG.info("run: node [{}]: ExecutionException: ospf mib not supported {}", 
                    getNodeId(), e.getMessage());
           return;
       } catch (final InterruptedException e) {
           LOG.info("run: node [{}]: InterruptedException: ospf mib not supported {}", 
                    getNodeId(), e.getMessage());
           return;
       }

        
        if (ospfGeneralGroup.getOspfRouterId() == null ) {
    		LOG.info( "run: node[{}]: ospf mib not supported",
    				getNodeId());
            return;
        } 

        if (ospfGeneralGroup.getOspfRouterId().equals(InetAddressUtils.addr("0.0.0.0"))) {
    		LOG.info( "run: node[{}]: ospf mib not supported not valid ospf identifier 0.0.0.0",
    				getNodeId());
            return;
        }

        if (Status.get(ospfGeneralGroup.getOspfAdminStat()) == Status.disabled) {
    		LOG.info( "run: node[{}]: ospf mib not supported ospf status: disabled",
    				getNodeId());
            return;
        }

        m_linkd.getQueryManager().store(getNodeId(), ipAddrTableGetter.get(ospfGeneralGroup.getOspfElement()));

        final List<OspfLink> links = new ArrayList<>();
        OspfNbrTableTracker ospfNbrTableTracker = new OspfNbrTableTracker() {
    
            public void processOspfNbrRow(final OspfNbrRow row) {
    		links.add(row.getOspfLink());
	    }
        };

        try {
            m_linkd.getLocationAwareSnmpClient().walk(peer, ospfNbrTableTracker).
            withDescription("ospfNbrTable").
            withLocation(getLocation()).
            execute().
            get();
       } catch (ExecutionException e) {
           LOG.debug("run: node [{}]: ExecutionException: {}", 
                    getNodeId(), e.getMessage());
           return;
       } catch (final InterruptedException e) {
           LOG.debug("run: node [{}]: InterruptedException: {}", 
                     getNodeId(), e.getMessage());
            return;
       }

        List<OspfLink> localOspfPorts =  new ArrayList<>();
        OspfIfTableTracker ospfIfTableTracker = new OspfIfTableTracker() {
            public void processOspfIfRow(final OspfIfRow row) {
                localOspfPorts.add(row.getOspfLink());
            }
        };

        try {
            m_linkd.getLocationAwareSnmpClient().walk(peer, ospfIfTableTracker).
            withDescription("ospfIfTable").
            withLocation(getLocation()).
            execute().
            get();
       } catch (ExecutionException e) {
           LOG.debug("run: node [{}]: ExecutionException: {}", 
                    getNodeId(), e.getMessage());
           return;
       } catch (final InterruptedException e) {
           LOG.debug("run: node [{}]: InterruptedException: {}", 
                    getNodeId(), e.getMessage());
            return;
       }

        for (OspfLink link : links) {
            for (OspfLink localospfport: localOspfPorts) {
                if (localospfport.getOspfAddressLessIndex() != 0 && link.getOspfRemAddressLessIndex() != 0) {
                    link.setOspfIpAddr(localospfport.getOspfIpAddr());
                    link.setOspfAddressLessIndex(localospfport.getOspfAddressLessIndex());
                    link.setOspfIfIndex(localospfport.getOspfAddressLessIndex());
                    break;
                }
                if (localospfport.getOspfAddressLessIndex() == 0 && link.getOspfRemAddressLessIndex() != 0)
                    continue;
                if (localospfport.getOspfAddressLessIndex() != 0 && link.getOspfRemAddressLessIndex() == 0)
                    continue;
                localospfport = ipAddrTableGetter.get(localospfport);
                if (InetAddressUtils.inSameNetwork(localospfport.getOspfIpAddr(),link.getOspfRemIpAddr(),localospfport.getOspfIpMask())) {
                    link.setOspfIpAddr(localospfport.getOspfIpAddr());
                    link.setOspfAddressLessIndex(localospfport.getOspfAddressLessIndex());
                    link.setOspfIpMask(localospfport.getOspfIpMask());
                    link.setOspfIfIndex(localospfport.getOspfIfIndex());
                    break;
                }
            }
            m_linkd.getQueryManager().store(getNodeId(),link);
        }

        m_linkd.getQueryManager().reconcileOspf(getNodeId(),now);
    }

	@Override
	public String getName() {
		return "NodeDiscoveryOspf";
	}

}
