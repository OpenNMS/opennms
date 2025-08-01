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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opennms.netmgt.enlinkd.common.NodeCollector;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.OspfElement.TruthValue;
import org.opennms.netmgt.enlinkd.service.api.CdpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.enlinkd.snmp.CdpCacheTableTracker;
import org.opennms.netmgt.enlinkd.snmp.CdpGlobalGroupTracker;
import org.opennms.netmgt.enlinkd.snmp.CdpInterfacePortNameGetter;
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
public final class NodeDiscoveryCdp extends NodeCollector {
	private final static Logger LOG = LoggerFactory.getLogger(NodeDiscoveryCdp.class);

	private final CdpTopologyService m_cdpTopologyService;
	/**
	 * Constructs a new SNMP collector for Cdp Node Discovery. 
	 * The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
	 */
    public NodeDiscoveryCdp(
            NodeCollectionGroupCdp group,
            final Node node,
            final int priority) {
        super(group.getLocationAwareSnmpClient(), node, priority);
    	m_cdpTopologyService = group.getCdpTopologyService();
    }

    public void collect() {

    	final Date now = new Date(); 
        final CdpGlobalGroupTracker cdpGlobalGroup = new CdpGlobalGroupTracker();

        SnmpAgentConfig peer = getSnmpAgentConfig();
        try {
            getLocationAwareSnmpClient().walk(peer, cdpGlobalGroup).
            withDescription("cdpGlobalGroup").
            withLocation(getLocation()).
            execute().
            get();
       } catch (ExecutionException e) {
           LOG.info("run: node [{}]: ExecutionException: CDP_MIB not supported {}", 
                    getNodeId(), e.getMessage());
           return;
       } catch (final InterruptedException e) {
           LOG.info("run: node [{}]: InterruptedException: CDP_MIB not supported {}", 
                    getNodeId(), e.getMessage());
           return;
       }
       if (cdpGlobalGroup.getCdpDeviceId() == null ) {
            LOG.info("run: node [{}]: CDP_MIB not supported", 
                     getNodeId());
            return;
       } 
       CdpElement cdpElement = cdpGlobalGroup.getCdpElement();
       m_cdpTopologyService.store(getNodeId(), cdpElement);
       if (cdpElement.getCdpGlobalRun() == TruthValue.FALSE) {
           LOG.info("run: node [{}]. CDP_MIB disabled.",
                    getNodeId());
           return;
       }
        
       List<CdpLink> links = new ArrayList<>();
        CdpCacheTableTracker cdpCacheTable = new CdpCacheTableTracker() {

            public void processCdpCacheRow(final CdpCacheRow row) {
                links.add(row.getLink());
            }
       };

        try {
            getLocationAwareSnmpClient().walk(peer, cdpCacheTable).
            withDescription("cdpCacheTable").
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
        final CdpInterfacePortNameGetter cdpInterfacePortNameGetter = new CdpInterfacePortNameGetter(peer, 
                                                                                                     getLocationAwareSnmpClient(),
                                                                                                     getLocation());
        for (CdpLink link: links)
            m_cdpTopologyService.store(getNodeId(),cdpInterfacePortNameGetter.get(link));
        
        m_cdpTopologyService.reconcile(getNodeId(),now);
    }

	@Override
	public String getName() {
		return "NodeDiscoveryCdp";
	}

}
