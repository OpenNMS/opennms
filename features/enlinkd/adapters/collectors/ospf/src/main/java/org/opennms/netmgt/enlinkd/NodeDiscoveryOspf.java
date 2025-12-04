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

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.enlinkd.common.NodeCollector;
import org.opennms.netmgt.enlinkd.model.OspfArea;
import org.opennms.netmgt.enlinkd.model.OspfElement.Status;
import org.opennms.netmgt.enlinkd.model.OspfIf;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.enlinkd.service.api.OspfTopologyService;
import org.opennms.netmgt.enlinkd.snmp.OspfAreaTableTracker;
import org.opennms.netmgt.enlinkd.snmp.OspfGeneralGroupTracker;
import org.opennms.netmgt.enlinkd.snmp.OspfIfTableTracker;
import org.opennms.netmgt.enlinkd.snmp.OspfIpAddrTableGetter;
import org.opennms.netmgt.enlinkd.snmp.OspfNbrTableTracker;
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
public final class NodeDiscoveryOspf extends NodeCollector {
    
	private final static Logger LOG = LoggerFactory.getLogger(NodeDiscoveryOspf.class);
	
	private final OspfTopologyService m_ospfTopologyService;

    public NodeDiscoveryOspf(
            final NodeCollectionGroupOspf group,
            final Node node,
            final int priority) {
        super(group.getLocationAwareSnmpClient(), node, priority);
    	m_ospfTopologyService = group.getOspfTopologyService();
    }

    public void collect() {

    	final Date now = new Date(); 

        SnmpAgentConfig peer = getSnmpAgentConfig();

        final OspfIpAddrTableGetter ipAddrTableGetter = new OspfIpAddrTableGetter(peer,
                                                                                  getLocationAwareSnmpClient(),
                                                                                  getLocation());
        final OspfGeneralGroupTracker ospfGeneralGroup = new OspfGeneralGroupTracker();
        try {
            getLocationAwareSnmpClient().walk(peer, ospfGeneralGroup).
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

        m_ospfTopologyService.store(getNodeId(), ipAddrTableGetter.get(ospfGeneralGroup.getOspfElement()));

        final List<OspfLink> links = new ArrayList<>();
        OspfNbrTableTracker ospfNbrTableTracker = new OspfNbrTableTracker() {
    
            public void processOspfNbrRow(final OspfNbrRow row) {
    		links.add(row.getOspfLink());
	    }
        };

        try {
            getLocationAwareSnmpClient().walk(peer, ospfNbrTableTracker).
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

        List<OspfIf> localOspfPorts =  new ArrayList<>();
        List<OspfIf> portsNeedingLookup = new ArrayList<>();
        OspfIfTableTracker ospfIfTableTracker = new OspfIfTableTracker() {
            public void processOspfIfRow(final OspfIfRow row) {
                if (row.getOspfIf().getOspfIfAddressLessIf() != 0) {
                    localOspfPorts.add(row.getOspfIf());
                } else {
                    // Don't block in callback - defer lookup until after walk completes
                    portsNeedingLookup.add(row.getOspfIf());
                }
            }
        };

        try {
            getLocationAwareSnmpClient().walk(peer, ospfIfTableTracker).
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

       for (OspfIf ospfIf : portsNeedingLookup) {
           try {
               localOspfPorts.add(ipAddrTableGetter.get(ospfIf));
           } catch (Exception e) {
               LOG.warn("run: node [{}]: failed to lookup IP for OSPF interface: {}",
                        getNodeId(), e.getMessage(), e);
           }
       }

        for (OspfLink link : links) {
            for (OspfIf localospfport: localOspfPorts) {
                if (localospfport.getOspfIfAddressLessIf() != 0 && link.getOspfRemAddressLessIndex() != 0) {
                    link.setOspfIpAddr(localospfport.getOspfIfIpaddress());
                    link.setOspfAddressLessIndex(localospfport.getOspfIfAddressLessIf());
                    link.setOspfIfIndex(localospfport.getOspfIfAddressLessIf());
                    link.setOspfIfAreaId(localospfport.getOspfIfAreaId());
                    break;
                }
                if (localospfport.getOspfIfAddressLessIf()== 0 && link.getOspfRemAddressLessIndex() != 0)
                    continue;
                if (localospfport.getOspfIfAddressLessIf() != 0 && link.getOspfRemAddressLessIndex() == 0)
                    continue;
                if (InetAddressUtils.inSameNetwork(localospfport.getOspfIfIpaddress(),link.getOspfRemIpAddr(),localospfport.getOspfIfNetmask())) {
                    link.setOspfIpAddr(localospfport.getOspfIfIpaddress());
                    link.setOspfAddressLessIndex(localospfport.getOspfIfAddressLessIf());
                    link.setOspfIfAreaId(localospfport.getOspfIfAreaId());
                    link.setOspfIpMask(localospfport.getOspfIfNetmask());
                    link.setOspfIfIndex(localospfport.getOspfIfIfindex());
                    break;
                }
            }
            m_ospfTopologyService.store(getNodeId(),link);
        }

        // Areas
        List<OspfArea> areas =  new ArrayList<>();
        OspfAreaTableTracker ospfAreaTableTracker = new OspfAreaTableTracker() {
            public void processOspfAreaRow(final OspfAreaRow row) {
                areas.add(row.getOspfArea());
            }
        };

        try {
            getLocationAwareSnmpClient().walk(peer, ospfAreaTableTracker).
                    withDescription("ospfAreaTable").
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
        // Areas
        for (OspfArea area : areas) {
            m_ospfTopologyService.store(getNodeId(),area);
        }
        m_ospfTopologyService.reconcile(getNodeId(),now);
    }

	@Override
	public String getName() {
		return "NodeDiscoveryOspf";
	}

}
