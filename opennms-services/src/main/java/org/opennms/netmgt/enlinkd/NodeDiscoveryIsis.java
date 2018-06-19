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

import org.opennms.netmgt.enlinkd.snmp.IsisCircTableTracker;
import org.opennms.netmgt.enlinkd.snmp.IsisISAdjTableTracker;
import org.opennms.netmgt.enlinkd.snmp.IsisSysObjectGroupTracker;
import org.opennms.netmgt.model.IsIsLink;
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
public final class NodeDiscoveryIsis extends NodeDiscovery {
private final static Logger LOG = LoggerFactory.getLogger(NodeDiscoveryIsis.class);
	/**
	 * Constructs a new SNMP collector for IsIs Node Discovery. 
	 * The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
	 * @param EnhancedLinkd linkd
	 * @param LinkableNode node
	 */
    public NodeDiscoveryIsis(final EnhancedLinkd linkd, final Node node) {
    	super(linkd, node);
    }

    protected void runNodeDiscovery() {

    	final Date now = new Date(); 

        final IsisSysObjectGroupTracker isisSysObject = new IsisSysObjectGroupTracker();

        SnmpAgentConfig peer = m_linkd.getSnmpAgentConfig(getPrimaryIpAddress(), getLocation());
        try {
            m_linkd.getLocationAwareSnmpClient().walk(peer,
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

        m_linkd.getQueryManager().store(getNodeId(),
                                        isisSysObject.getIsisElement());
    
        
        final List<IsIsLink> links = new ArrayList<>();
        final IsisISAdjTableTracker isisISAdjTableTracker = new IsisISAdjTableTracker() {
    	   @Override
    	   public void processIsisAdjRow(IsIsAdjRow row) {
    		links.add(row.getIsisLink());
    	   }
        };
        
        try {
            m_linkd.getLocationAwareSnmpClient().walk(peer,
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
            m_linkd.getLocationAwareSnmpClient().walk(peer,
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
            m_linkd.getQueryManager().store(getNodeId(), link);
        }
        m_linkd.getQueryManager().reconcileIsis(getNodeId(), now);
    }

	@Override
	public String getName() {
		return "NodeDiscoveryIsis";
	}

}
