/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
	 */
	public NodeDiscoveryIpNetToMedia(
	            final NodeCollectionGroupIpNetToMedia group,
	            final Node node,
                final int priority) {
	        super(group.getLocationAwareSnmpClient(), node, priority);
    	m_ipNetToMediaTopologyService = group.getIpNetToMediaTopologyService();
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
