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
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.snmp;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>CiscoIpSlaDetector class.</p>
 *
 * @author ronny
 * @version $Id: $
 */
@Scope("prototype")
public class CiscoIpSlaDetector extends SnmpDetector {
   
    /**
     * Name of monitored service.
     */
    private final String PROTOCOL_NAME = "CiscoIpSlaDetector";

    /**
     * A string which is used by a managing application to identify the RTT
     * target.
     */
    private final String RTT_ADMIN_TAG_OID = ".1.3.6.1.4.1.9.9.42.1.2.1.1.3";

    /**
     * The RttMonOperStatus object is used to manage the state.
     */
    private final String RTT_OPER_STATE_OID = ".1.3.6.1.4.1.9.9.42.1.2.9.1.10";
    
    /**
     * Implement the rttMonCtrlOperState
     */
    private enum RTT_MON_OPER_STATE {
        RESET(1), ORDERLY_STOP(2), IMMEDIATE_STOP(3), PENDING(4), INACTIVE(5), ACTIVE(
                6), RESTART(7);

        private final int state; // state code

        RTT_MON_OPER_STATE(int s) {
            this.state = s;
        }

        private int value() {
            return this.state;
        }
    }
    
    /**
     * <p>Constructor for CiscoIpSlaDetector</p>
     */
    public CiscoIpSlaDetector(){
        setServiceName(PROTOCOL_NAME);
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If
     * the protocol is not supported then a false value is returned to the
     * caller. The qualifier map passed to the method is used by the plugin to
     * return additional information by key-name. These key-value pairs can be
     * added to service events if needed.
     */
    @Override
    public boolean isServiceDetected(InetAddress address, DetectorMonitor detectMonitor) {
        boolean detected = false;
        
        SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(address);
        
        configureAgentPTR(agentConfig);

        configureAgentVersion(agentConfig);
        
        try {
            /*
             * Get two maps one with all configured administration tags and one with 
             * operational status
             */
            Map<SnmpInstId, SnmpValue> tagResults = getTable(agentConfig, RTT_ADMIN_TAG_OID);
            if (tagResults == null || tagResults.isEmpty()) {
                LogUtils.warnf(this, "%s: No admin tags received!", getServiceName());
                return detected;
            }

            Map<SnmpInstId, SnmpValue> operStateResults = getTable(agentConfig,RTT_OPER_STATE_OID);
            if (operStateResults == null || operStateResults.isEmpty()) {
                LogUtils.warnf(this, "%s: No oper status received!", getServiceName());
                return detected;
            }
            
            // Iterate over the list of configured IP SLAs
            for (SnmpInstId ipslaInstance : tagResults.keySet()) {
                LogUtils.debugf(this, "%s detect: [%s] compared with [%s]", getServiceName(), tagResults.get(ipslaInstance), getVbvalue());
                /*
                 * Check if a configured IP SLA with specific tag exist and the
                 * the operational state ACTIVE(6), detected with first match.
                 */
                if (tagResults.get(ipslaInstance).toString().equals(getVbvalue())
                        && operStateResults.get(ipslaInstance).toInt() == RTT_MON_OPER_STATE.ACTIVE.value()) {
                    LogUtils.debugf(this, "%s: admin tag [%s] found and status is %d", getServiceName(), getVbvalue(), operStateResults.get(ipslaInstance).toInt());
                    detected = true;
                    break; // detected leave for()
                } else {
                    LogUtils.debugf(this, "%s: admin tag [%s] not found and status is %d", getServiceName(), getVbvalue(), operStateResults.get(ipslaInstance).toInt());
                    detected = false; // not detected, check next or return with not detected
                }
            }
        } catch (final NullPointerException e) {
            LogUtils.warnf(this, e, "SNMP not available or CISCO-RTT-MON-MIB not supported!");
        } catch (final NumberFormatException e) {
            LogUtils.warnf(this, e, "Number operator used on a non-number.");
        } catch (final IllegalArgumentException e) {
            LogUtils.warnf(this, e, "Invalid SNMP criteria.");
        } catch (final Throwable t) {
            LogUtils.warnf(this, t, "Unexpected exception during SNMP poll of interface %s", address.getHostAddress());
        }
        return detected; // return detected
    }
    
    /**
     * Overwrite isTable from SnmpDetector. Parameter is not used and will be
     * ignored.
     * 
     * @param isTable @ {java.lang.String} object
     */
    public void setIsTable (String isTable) {
        LogUtils.warnf(this, "The paramater isTable is not used and will be ignored.");
    }
}