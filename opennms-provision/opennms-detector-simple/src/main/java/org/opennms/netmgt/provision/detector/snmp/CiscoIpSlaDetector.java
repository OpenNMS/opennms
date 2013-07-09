/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.snmp;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>CiscoIpSlaDetector class.</p>
 *
 * @author agalue
 * @version $Id: $
 */
@Scope("prototype")
public class CiscoIpSlaDetector extends SnmpDetector {

	private static final Logger LOG = LoggerFactory.getLogger(CiscoIpSlaDetector.class);

	/**
     * Name of monitored service.
     */
    private static final String PROTOCOL_NAME = "Cisco_IP_SLA";

    /**
     * A string which identify the active operational state
     */
    private static final int RTT_MON_OPER_STATE_ACTIVE = 6;

    /**
     * A string which is used by a managing application to identify the RTT
     * target.
     */
    private static final String RTT_ADMIN_TAG_OID = ".1.3.6.1.4.1.9.9.42.1.2.1.1.3";

    /**
     * The RttMonOperStatus object is used to manage the state.
     */
    private static final String RTT_OPER_STATE_OID = ".1.3.6.1.4.1.9.9.42.1.2.9.1.10";

    private String m_adminTag;

    /**
     * <p>Constructor for CiscoIpSlaDetector.</p>
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
    public boolean isServiceDetected(InetAddress address) {
        try {
            SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(address);
            configureAgentPTR(agentConfig);
            configureAgentVersion(agentConfig);

            Map<SnmpInstId, SnmpValue> tagResults = SnmpUtils.getOidValues(agentConfig, "CiscoIpSlaDetector", SnmpObjId.get(RTT_ADMIN_TAG_OID));
            if (tagResults == null) {
                LOG.warn("isServiceDetected: No admin tags received!");
                return false;
            }

            Map<SnmpInstId, SnmpValue> operStateResults = SnmpUtils.getOidValues(agentConfig, "CiscoIpSlaDetector", SnmpObjId.get(RTT_OPER_STATE_OID));
            if (operStateResults == null) {
                LOG.warn("isServiceDetected: No operational states received!");
                return false;
            }

            // Iterate over the list of configured IP SLAs
            for (Entry<SnmpInstId,SnmpValue> ipslaEntry : tagResults.entrySet()) {
                SnmpValue status = operStateResults.get(ipslaEntry.getKey());
                LOG.debug("isServiceDetected: admin-tag={} value={} oper-state={}", m_adminTag, formatValue(ipslaEntry.getValue()), status.toInt());
                //  Check if a configured IP SLA with specific tag exist and is the operational state active 
                if (m_adminTag.equals(formatValue(ipslaEntry.getValue())) && status.toInt() == RTT_MON_OPER_STATE_ACTIVE) {
                    LOG.debug("isServiceDetected: admin tag found");
                    return true;
                }
            }
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
        return false;
    }

    private String formatValue(SnmpValue value) {
        return value.isNull()  ? null : value.toString().replaceAll("\"", "");
    }

    public String getAdminTag() {
        return m_adminTag;
    }

    public void setAdminTag(String adminTag) {
        this.m_adminTag = adminTag;
    }

}
