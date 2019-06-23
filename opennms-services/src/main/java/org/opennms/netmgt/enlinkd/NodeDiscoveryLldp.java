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

import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.netmgt.enlinkd.snmp.LldpLocPortGetter;
import org.opennms.netmgt.enlinkd.snmp.LldpLocalGroupTracker;
import org.opennms.netmgt.enlinkd.snmp.LldpRemTableTracker;
import org.opennms.netmgt.model.LldpLink;
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
public final class NodeDiscoveryLldp extends NodeDiscovery {
    private final static Logger LOG = LoggerFactory.getLogger(NodeDiscoveryLldp.class);

    private final static String DW_SYSOID=".1.3.6.1.4.1.7262.2.4";
    private static final String DW_NULL_CHASSIS_ID="cf";
    private static final String DW_NULL_SYSOID_ID="NuDesign";

    /**
     * Constructs a new SNMP collector for Lldp Node Discovery. 
     * The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
     * @param EnhancedLinkd linkd
     * 
     * @param LinkableNode node
     * 
     */
    public NodeDiscoveryLldp(final EnhancedLinkd linkd, final Node node) {
    	super(linkd, node);
    }

    protected void runNodeDiscovery() {

    	final Date now = new Date(); 

        final LldpLocalGroupTracker lldpLocalGroup = new LldpLocalGroupTracker();
        SnmpAgentConfig peer = m_linkd.getSnmpAgentConfig(getPrimaryIpAddress(), getLocation());

        try {
            m_linkd.getLocationAwareSnmpClient().walk(peer,
                          lldpLocalGroup)
                          .withDescription("lldpLocalGroup")
                          .withLocation(getLocation())
                          .execute()
                          .get();
        } catch (ExecutionException e) {
            LOG.info("run: node [{}]: ExecutionException: LLDP_MIB not supported {}", 
                     getNodeId(), e.getMessage());
                return;
        } catch (final InterruptedException e) {
            LOG.info("run: node [{}]: InterruptedException: LLDP_MIB not supported {}", 
                     getNodeId(), e.getMessage());
                return;
        }
        
        if (lldpLocalGroup.getLldpLocChassisid() == null ) {
    		LOG.info( "run: node[{}]: LLDP_MIB not supported",
    				getNodeId());
            return;
        } else {
    		LOG.debug( "run: node[{}]: lldp identifier : {}",
    				getNodeId(),
    				lldpLocalGroup.getLldpElement());
        }

        m_linkd.getQueryManager().store(getNodeId(),
                lldpLocalGroup.getLldpElement());

        if (getSysoid() == null || getSysoid().equals(DW_SYSOID) ) {
            if (lldpLocalGroup.getLldpLocChassisid().toHexString().equals(DW_NULL_CHASSIS_ID) &&
                    lldpLocalGroup.getLldpLocChassisidSubType() == LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT.getValue()) {
        		LOG.info( "run: node[{}]: address {}. lldp identifier : {}. lldp not active for Dragon Wave Device.",
        				getNodeId(),
        				getPrimaryIpAddressString(),
        				lldpLocalGroup.getLldpElement());
                return;
            }
    
            if (lldpLocalGroup.getLldpLocSysname().equals(DW_NULL_SYSOID_ID) ) {
        		LOG.info( "run: node[{}]: lldp identifier : {}. lldp not active for Dragon Wave Device.",
        				getNodeId(),
        				lldpLocalGroup.getLldpElement());
                return;
            }
        }

        List<LldpLink> links = new ArrayList<>();
        LldpRemTableTracker lldpRemTable = new LldpRemTableTracker() {

        	public void processLldpRemRow(final LldpRemRow row) {
        	    links.add(row.getLldpLink());
        	}
        };
        try {
            m_linkd.getLocationAwareSnmpClient().walk(peer,
                                      lldpRemTable)
                                  .withDescription("lldpRemTable")
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
        
        
        final LldpLocPortGetter lldpLocPort = 
                new LldpLocPortGetter(peer,
                                m_linkd.getLocationAwareSnmpClient(),
                                getLocation(),getNodeId());
        for (LldpLink link: links) {
            m_linkd.getQueryManager().store(getNodeId(),lldpLocPort.getLldpLink(link));
        }
        
        m_linkd.getQueryManager().reconcileLldp(getNodeId(),now);
    }

	@Override
	public String getName() {
		return "NodeDiscoveryLldp";
	}

}
