/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

        SnmpObjId[] oids = new SnmpObjId[2 * mifaces.size()];
        //int maxVarsPerPdu = agentConfig.getMaxVarsPerPdu();
       
        for (int i=0;i < mifaces.size(); i++) {
            SnmpMinimalPollInterface miface = mifaces.get(i);
            miface.setStatus(PollStatus.unavailable());
            mifaces.set(i, miface);
            oids[i] = SnmpObjId.get(IF_ADMIN_STATUS_OID + miface.getIfindex());
            log().debug("Adding oid: " + oids[i] + " at position " + i);
            oids[i+mifaces.size()] = SnmpObjId.get(IF_OPER_STATUS_OID + miface.getIfindex());
            log().debug("Adding oid: " + oids[i+mifaces.size()] + " at position " + (i+mifaces.size()));
        }

        try {
        	SnmpValue[] results = SnmpUtils.get(agentConfig, oids);
    		log().debug("got " + results.length +" SnmpValues");
            int i=0;
            for(SnmpValue result : results) {
                if (result != null) {
                    log().debug("Snmp Value is "+ result.toInt() + " for oid: " + oids[i]);
                    if (i< mifaces.size()) {
                        SnmpMinimalPollInterface miface = mifaces.get(i);
                        miface.setStatus(PollStatus.up());
                        miface.setAdminstatus(result.toInt());
                    } else {
                        SnmpMinimalPollInterface miface = mifaces.get(i-mifaces.size());
                        miface.setStatus(PollStatus.up());
                        miface.setOperstatus(result.toInt());
                    }
                } else {
                    log().error("Snmp Value is null for oid: " + oids[i]);
                }
                i++;
            }
        } catch (NumberFormatException e) {
            log().error("Number operator used on a non-number " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log().error("Invalid Snmp Criteria: " + e.getMessage());
        } catch (Throwable t) {
            log().error("Unexpected exception during SNMP poll of interface " + agentConfig, t);
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
