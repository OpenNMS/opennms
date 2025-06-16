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
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.service.api.IsisTopologyService;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.enlinkd.snmp.IsisCircTableTracker;
import org.opennms.netmgt.enlinkd.snmp.IsisISAdjTableTracker;
import org.opennms.netmgt.enlinkd.snmp.IsisSysObjectGroupTracker;
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
public final class NodeDiscoveryIsis extends NodeCollector {

    private final static Logger LOG = LoggerFactory.getLogger(NodeDiscoveryIsis.class);
    
    private final IsisTopologyService m_isisTopologyService;
	/**
	 * Constructs a new SNMP collector for IsIs Node Discovery. 
	 * The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
	 */
    public NodeDiscoveryIsis(
            final NodeCollectionGroupIsis group,
            final Node node,
            final int priority) {
        super(group.getLocationAwareSnmpClient(), node, priority);
    	m_isisTopologyService = group.getIsisTopologyService();
    }

    public void collect() {

    	final Date now = new Date(); 

        final IsisSysObjectGroupTracker isisSysObject = new IsisSysObjectGroupTracker();

        SnmpAgentConfig peer = getSnmpAgentConfig();
        try {
            getLocationAwareSnmpClient().walk(peer,
                                                      isisSysObject).withDescription("isisSysObjectCollection").withLocation(getLocation()).execute().get();
        } catch (ExecutionException e) {
            LOG.info("run: node [{}]: ExecutionException: Is-Is mib not supported: {}", 
                     getNodeId(), e.getMessage());
            return;
        } catch (final InterruptedException e) {
            LOG.info("run: node [{}]: InterruptedException: Is-Is mib not supported: {}", 
                     getNodeId(), e.getMessage());
            return;
        }
        
        if (isisSysObject.getIsisSysId() == null ) {
            LOG.info( "run: node [{}]: Is-Is mib not supported ", 
            		getNodeId());
            return;
        }

        m_isisTopologyService.store(getNodeId(),
                                        isisSysObject.getIsisElement());
    
        
        final List<IsIsLink> links = new ArrayList<>();
        final IsisISAdjTableTracker isisISAdjTableTracker = new IsisISAdjTableTracker() {
    	   @Override
    	   public void processIsisAdjRow(IsIsAdjRow row) {
    		links.add(row.getIsisLink());
    	   }
        };
        
        try {
            getLocationAwareSnmpClient().walk(peer,
                      isisISAdjTableTracker)
                      .withDescription("isisISAdjTable")
                      .withLocation(getLocation())
                      .execute()
                      .get();
        } catch (ExecutionException e) {
            LOG.debug("run: node [{}]: ExecutionException: {}", 
                     getNodeId(), e.getMessage());
            return;
       } catch (final InterruptedException e) {
           LOG.debug("run: node [{}]: InterruptedException: {}", 
                    getNodeId(), e.getMessage());
            return;
        }
        
        final IsisCircTableTracker isisCircTableTracker = new IsisCircTableTracker() {
    	@Override
    	public void processIsisCircRow(IsIsCircRow row) {
    		IsIsLink link = row.getIsisLink();
    		for (IsIsLink adjlink:links) {
    			if (link.getIsisCircIndex().intValue() == adjlink.getIsisCircIndex().intValue()) {
    				adjlink.setIsisCircIfIndex(link.getIsisCircIfIndex());
    				adjlink.setIsisCircAdminState(link.getIsisCircAdminState());
    			}
    		}
    	}
        };

        try {
            getLocationAwareSnmpClient().walk(peer,
                              isisCircTableTracker)
                              .withDescription("isisCircTable")
                              .withLocation(getLocation())
                              .execute().get();
        } catch (ExecutionException e) {
            LOG.debug("run: node [{}]: ExecutionException: isisCircTable: {}", 
                     getNodeId(), e.getMessage());
            return;
        } catch (final InterruptedException e) {
            LOG.debug("run: node [{}]: InterruptedException: isisCircTable: {}", 
                     getNodeId(), e.getMessage());
            return;
        }
        
        for (IsIsLink link:links) {
            m_isisTopologyService.store(getNodeId(), link);
        }
        m_isisTopologyService.reconcile(getNodeId(), now);
    }

	@Override
	public String getName() {
		return "NodeDiscoveryIsis";
	}

}
