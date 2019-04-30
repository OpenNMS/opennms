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

import org.opennms.netmgt.enlinkd.common.NodeCollector;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia.IpNetToMediaType;
import org.opennms.netmgt.enlinkd.service.api.IpNetToMediaTopologyService;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.enlinkd.snmp.IpNetToMediaTableTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session
 * creating and collection occurs in the main run method of the instance. This
 * allows the collection to occur in a thread if necessary.
 */
public final class NodeDiscoveryIpNetToMedia extends NodeCollector {
    
	private final static Logger LOG = LoggerFactory.getLogger(NodeDiscoveryIpNetToMedia.class);
	
	private final IpNetToMediaTopologyService m_ipNetToMediaTopologyService;
	/**
	 * Constructs a new SNMP collector for IpNetToMedia Node Discovery. 
	 * The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
	 * @param EnhancedLinkd linkd
	 * @param LinkableNode node
	 */
	public NodeDiscoveryIpNetToMedia(
	            final IpNetToMediaTopologyService ipNetToMediaTopologyService,
	            final LocationAwareSnmpClient locationAwareSnmpClient,
	            final long interval,final long initial,
	            final Node node) {
	        super(locationAwareSnmpClient, interval, initial,node);
    	m_ipNetToMediaTopologyService = ipNetToMediaTopologyService;
    }

    public void collect() {

    	final Date now = new Date(); 

        IpNetToMediaTableTracker ipNetToMediaTableTracker = new IpNetToMediaTableTracker() {
            public void processIpNetToMediaRow(final IpNetToMediaRow row) {
                IpNetToMedia macep = row.getIpNetToMedia();

                if (LOG.isDebugEnabled()) {
                    LOG.debug("processIpNetToMediaRow: node [{}], {}:{}:{}. ",
                          getNodeId(),
                          macep.getPhysAddress(),
                          str(macep.getNetAddress()),
                          macep.getIpNetToMediaType());
                }
                
                if (macep.getPhysAddress() == null
                        || macep.getNetAddress() == null
                        || macep.getIpNetToMediaType() == null
                        || macep.getIpNetToMediaType() == IpNetToMediaType.IPNETTOMEDIA_TYPE_INVALID) {
                    return;
                }
                m_ipNetToMediaTopologyService.store(getNodeId(), macep);
            }
        };
		
        SnmpAgentConfig peer = getSnmpAgentConfig();
        try {
            getLocationAwareSnmpClient().walk(peer,
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

        m_ipNetToMediaTopologyService.reconcile(getNodeId(), now);
    }

	@Override
	public String getName() {
		return "NodeDiscoveryIpNetToMedia";
	}

}
