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

import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.netmgt.enlinkd.snmp.LldpLocPortGetter;
import org.opennms.netmgt.enlinkd.snmp.LldpLocalGroupTracker;
import org.opennms.netmgt.enlinkd.snmp.LldpRemTableTracker;
import org.opennms.netmgt.model.topology.LinkableSnmpNode;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
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
    public NodeDiscoveryLldp(final EnhancedLinkd linkd, final LinkableSnmpNode node) {
    	super(linkd, node);
    }

    protected void runCollection() {

    	final Date now = new Date(); 

    	String trackerName = "lldpLocalGroup";
        final LldpLocalGroupTracker lldpLocalGroup = new LldpLocalGroupTracker();
		LOG.info( "run: collecting {} on: {}",trackerName, str(getTarget()));
        SnmpWalker walker =  SnmpUtils.createWalker(getPeer(), trackerName, lldpLocalGroup);
        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
            	LOG.info(
                        "run:Aborting Lldp Linkd node scan : Agent timed out while scanning the {} table", trackerName);
            	return;
            }  else if (walker.failed()) {
            	LOG.info(
                        "run:Aborting Lldp Linkd node scan : Agent failed while scanning the {} table: {}", trackerName,walker.getErrorMessage());
            	return;
            }
        } catch (final InterruptedException e) {
            LOG.error("run: Lldp Linkd node collection interrupted, exiting", e);
            return;
        }
        
        if (lldpLocalGroup.getLldpLocChassisid() == null ) {
            LOG.info( "lldp mib not supported on: {}", str(getPeer().getAddress()));
            return;
        } else {
            LOG.info( "found lldp identifier : {}", lldpLocalGroup.getLldpElement());
        }
        
        m_linkd.getQueryManager().store(getNodeId(), lldpLocalGroup.getLldpElement());

        if (getSysoid() == null || getSysoid().equals(DW_SYSOID) ) {
            if (lldpLocalGroup.getLldpLocChassisid().toHexString().equals(DW_NULL_CHASSIS_ID) &&
                    lldpLocalGroup.getLldpLocChassisidSubType() == LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT.getValue()) {
                LOG.info( "lldp not active for Dragon Wave Device identifier : {}", lldpLocalGroup.getLldpElement());
                return;
            }
    
            if (lldpLocalGroup.getLldpLocSysname().equals(DW_NULL_SYSOID_ID) ) {
                LOG.info( "lldp not active for Dragon Wave Device identifier : {}", lldpLocalGroup.getLldpElement());
                return;
            }
        }

        final LldpLocPortGetter lldpLocPort = new LldpLocPortGetter(getPeer());
        trackerName = "lldpRemTable";
        LldpRemTableTracker lldpRemTable = new LldpRemTableTracker() {

        	public void processLldpRemRow(final LldpRemRow row) {
        	    m_linkd.getQueryManager().store(getNodeId(),row.getLldpLink(lldpLocPort));
        	}
        };

		LOG.info( "run: collecting {} on: {}",trackerName, str(getTarget()));
        walker = SnmpUtils.createWalker(getPeer(), trackerName, lldpRemTable);
        walker.start();
        
        try {
            walker.waitFor();
            if (walker.timedOut()) {
            	LOG.info(
                        "run:Aborting node scan : Agent timed out while scanning the {} table", trackerName);
            }  else if (walker.failed()) {
            	LOG.info(
                        "run:Aborting node scan : Agent failed while scanning the {} table: {}", trackerName,walker.getErrorMessage());
            }
        } catch (final InterruptedException e) {
            LOG.error("run: collection interrupted, exiting",e);
            return;
        }
        m_linkd.getQueryManager().reconcileLldp(getNodeId(),now);
    }

	@Override
	public String getInfo() {
        return "ReadyRunnable:LldpLinkNodeDiscovery node: "+ getNodeId() + " ip:" + str(getTarget())
                + " package:" + getPackageName();
	}

	@Override
	public String getName() {
		return "LldpLinkDiscovery";
	}

}
