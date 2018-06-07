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

import static org.opennms.core.utils.InetAddressUtils.str;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.opennms.netmgt.enlinkd.snmp.IpNetToMediaTableTracker;
import org.opennms.netmgt.model.IpNetToMedia;
import org.opennms.netmgt.model.IpNetToMedia.IpNetToMediaType;
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
public final class NodeDiscoveryIpNetToMedia extends NodeDiscovery {
    
	private final static Logger LOG = LoggerFactory.getLogger(NodeDiscoveryIpNetToMedia.class);
	
	/**
	 * Constructs a new SNMP collector for IpNetToMedia Node Discovery. 
	 * The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
	 * @param EnhancedLinkd linkd
	 * @param LinkableNode node
	 */
    public NodeDiscoveryIpNetToMedia(final EnhancedLinkd linkd, final Node node) {
    	super(linkd, node);
    }

    protected void runNodeDiscovery() {

    	final Date now = new Date(); 

        IpNetToMediaTableTracker ipNetToMediaTableTracker = new IpNetToMediaTableTracker() {
            public void processIpNetToMediaRow(final IpNetToMediaRow row) {
                IpNetToMedia macep = row.getIpNetToMedia();
                if (macep.getPhysAddress() != null && 
                        macep.getNetAddress() != null && 
                        macep.getSourceIfIndex() != null &&
                        (macep.getIpNetToMediaType() == IpNetToMediaType.IPNETTOMEDIA_TYPE_DYNAMIC
                        || macep.getIpNetToMediaType() == IpNetToMediaType.IPNETTOMEDIA_TYPE_STATIC)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("processIpNetToMediaRow: node [{}], mac address {} and ip {} mediatype {}. saving",
                              getNodeId(),
                              macep.getPhysAddress(), 
                              str(macep.getNetAddress()),
                              macep.getIpNetToMediaType());
                    }
                    m_linkd.getQueryManager().store(getNodeId(), macep);
                    return;
                } 
                if (macep.getPhysAddress() == null && macep.getNetAddress() == null) {
                    LOG.debug("processIpNetToMediaRow: node [{}], null:null:{}. ip and mac addresses null. skipping",
                              getNodeId(),
                              macep.getIpNetToMediaType());
                    return;
                } 
                if (macep.getPhysAddress() == null) {
                        LOG.debug("processIpNetToMediaRow: node [{}], null:{}:{}. mac address null. skipping",
                                  getNodeId(),
                                  str(macep.getNetAddress()),
                                  macep.getIpNetToMediaType());
                        return;
                } 
                if (macep.getNetAddress() == null) {
                    LOG.debug("processIpNetToMediaRow: node [{}], {}:null:{}. ip address null. skipping",
                             getNodeId(),
                             macep.getPhysAddress(), 
                             macep.getIpNetToMediaType());
                    return;
                } 
                LOG.debug("processIpNetToMediaRow: node [{}],  {}:{}:{}:{}. not valid. skipping",
                             getNodeId(),
                             macep.getPhysAddress(), 
                             str(macep.getNetAddress()),
                             macep.getIpNetToMediaType(),
                             macep.getSourceIfIndex());
            }
        };
		
        SnmpAgentConfig peer = m_linkd.getSnmpAgentConfig(getPrimaryIpAddress(), getLocation());
        try {
            m_linkd.getLocationAwareSnmpClient().walk(peer,
                                                      ipNetToMediaTableTracker).withDescription("ipNetToMedia").withLocation(getLocation()).execute().get();
        } catch (ExecutionException e) {
            LOG.debug("run: node [{}]: ExecutionException: {}", 
                     getNodeId(), e.getMessage());
            return;
        } catch (final InterruptedException e) {
            LOG.debug("run: node [{}]: InterruptedException: {}",
                     getNodeId(),e.getMessage());
            return;       
        }

        m_linkd.getQueryManager().reconcileIpNetToMedia(getNodeId(), now);
    }

	@Override
	public String getName() {
		return "NodeDiscoveryIpNetToMedia";
	}

}
