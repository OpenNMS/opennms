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
import java.util.List;




import java.util.concurrent.ExecutionException;

import org.opennms.netmgt.enlinkd.snmp.MtxrWlRtabTableTracker;
import org.opennms.netmgt.enlinkd.snmp.MtxrWlRtabTableTracker.MtrxWlRTabRow;
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
public final class NodeDiscoveryWifi extends NodeDiscovery {
    
	private final static Logger LOG = LoggerFactory.getLogger(NodeDiscoveryWifi.class);
	/**
	 * Constructs a new SNMP collector for Wifi Node Discovery. 
	 * The collection does not occur until the
	 * <code>run</code> method is invoked.
         * 
	 * @param EnhancedLinkd linkd
	 * @param LinkableNode node
	 */
    public NodeDiscoveryWifi(final EnhancedLinkd linkd, final Node node) {
    	super(linkd, node);
    }

    protected void runCollection() {

        SnmpAgentConfig peer = m_linkd.getSnmpAgentConfig(getPrimaryIpAddress(), getLocation());

        final List<MtrxWlRTabRow> links = new ArrayList<MtrxWlRTabRow>();
        MtxrWlRtabTableTracker ospfNbrTableTracker = new MtxrWlRtabTableTracker() {
    
            public void processMtrxWlRTabRow(final MtrxWlRTabRow row) {
    		links.add(row);
	    }
        };

        try {
            m_linkd.getLocationAwareSnmpClient().walk(peer, ospfNbrTableTracker).
            withDescription("mtrxWlRTab").
            withLocation(getLocation()).
            execute().
            get();
       } catch (ExecutionException e) {
           LOG.error("run: collection execution failed, exiting",e);
           return;
       } catch (final InterruptedException e) {
            LOG.error( "run: collection interrupted, exiting",e);
            return;
       }

    }

	@Override
	public String getName() {
		return "WifiLinkDiscovery";
	}

}
