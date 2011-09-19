/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.snmpinterfacepoller;

import java.util.List;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterface.SnmpMinimalPollInterface;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the SNMP service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class SnmpPollInterfaceMonitor {

    /**
     * ifAdminStatus table from MIB-2.
     */
    private static final String IF_ADMIN_STATUS_OID = ".1.3.6.1.2.1.2.2.1.7.";
    
    /**
     * ifOperStatus table from MIB-2.
     */
    private static final String IF_OPER_STATUS_OID = ".1.3.6.1.2.1.2.2.1.8.";

    /**
     * <p>poll</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param mifaces a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    public List<SnmpMinimalPollInterface> poll(SnmpAgentConfig agentConfig, List<SnmpMinimalPollInterface> mifaces) {

        if (mifaces == null ) {
            log().error("Null Interfaces passed to Monitor, exiting");
            return null;
        }
        
        log().debug("Got " + mifaces.size() + " interfaces to poll");
        
        // Retrieve this interface's SNMP peer object
        //
        if (agentConfig == null) throw new RuntimeException("SnmpAgentConfig object not available");

        SnmpObjId[] adminoids = new SnmpObjId[mifaces.size()];
        SnmpObjId[] operooids = new SnmpObjId[mifaces.size()];

        for (int i=0;i < mifaces.size(); i++) {
        	SnmpMinimalPollInterface miface = mifaces.get(i);
            miface.setStatus(PollStatus.unavailable());
            adminoids[i] = SnmpObjId.get(IF_ADMIN_STATUS_OID + miface.getIfindex());
            operooids[i] = SnmpObjId.get(IF_OPER_STATUS_OID + miface.getIfindex());
            log().debug("Adding Admin/Oper oids: " + adminoids[i] + "/" +operooids[i]);
        }


        SnmpValue[] adminresults = new SnmpValue[mifaces.size()];
        SnmpValue[] operoresults = new SnmpValue[mifaces.size()];

        log().debug("try to get admin statuses");
        adminresults = SnmpUtils.get(agentConfig, adminoids);
        log().debug("got admin status " + adminresults.length +" SnmpValues");
        if (adminresults.length != mifaces.size()) {
        	log().warn("Snmp Admin statuses collection failed");
        	return mifaces;
        }
		
        log().debug("try to get admin statuses");
        operoresults = SnmpUtils.get(agentConfig, operooids);
        log().debug("got operational status " + operoresults.length +" SnmpValues");
        
        if (operoresults.length != mifaces.size()) {
        	log().warn("Snmp Operational statuses collection failed");
        	return mifaces;        	
        }
        
        for (int i=0; i< mifaces.size(); i++) {
            SnmpMinimalPollInterface miface = mifaces.get(i);

            if (adminresults[i] != null && operoresults[i] != null ) {
                try {
                	miface.setAdminstatus(adminresults[i].toInt());
                	miface.setOperstatus(operoresults[i].toInt());
                	miface.setStatus(PollStatus.up());
                    log().debug("SNMP Value is "+ adminresults[i].toInt() + " for oid: " + adminoids[i]);
                    log().debug("SNMP Value is "+ operoresults[i].toInt() + " for oid: " + operooids[i]);
                } catch (Exception e) {
                    log().warn("SNMP Value is "+ adminresults[i].toDisplayString() + " for oid: " + adminoids[i]);
                    log().warn("SNMP Value is "+ operoresults[i].toDisplayString() + " for oid: " + operooids[i]);
				}
            } else {
                log().info("SNMP Value is null for oid: " + adminoids[i]+"/"+operooids[i]);
            }
        }

        return mifaces;
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
