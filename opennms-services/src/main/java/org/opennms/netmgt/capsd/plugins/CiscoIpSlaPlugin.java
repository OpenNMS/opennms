/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * This class is used to monitor if a particular Cisco IP-SLA is within a
 * configured threshold or has reached a timeout. The configured IP-SLA is
 * monitored by the specified "ip sla tag"
 *
 * @author <A HREF="mailto:r.trommer@open-factory.org">Ronny Trommer</A>
 * @version $Id: $
 */
public class CiscoIpSlaPlugin extends SnmpPlugin {

    /**
     * The protocol supported by this plugin
     */
    private static final String PROTOCOL_NAME = "Cisco_IP_SLA";

    /**
     * A string which is used by a managing application to identify the RTT
     * target.
     */
    private static final String RTT_ADMIN_TAG_OID = ".1.3.6.1.4.1.9.9.42.1.2.1.1.3";

    /**
     * The RttMonOperStatus object is used to manage the state.
     */
    private static final String RTT_OPER_STATE_OID = ".1.3.6.1.4.1.9.9.42.1.2.9.1.10";

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
    };

    /**
     * Returns the name of the protocol that this plugin checks on the target
     * system for support.
     *
     * @return The protocol name for this plugin.
     */
    @Override
    public String getProtocolName() {
        return PROTOCOL_NAME;
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
    public boolean isProtocolSupported(InetAddress ipaddr,
            Map<String, Object> parameters) {

        boolean status = false;

        try {
            String adminTag = ParameterMap.getKeyedString(parameters, "admin-tag", null);

            /*
             * Get configuration parameters This is the string that represents
             * the configured IP-SLA admin tag to be monitored.
             */
            if (adminTag == null) {
                log().warn("poll: No IP-SLA admin-tag defined!");
                return status;
            }

            SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(
                                                                                       ipaddr);
            if (agentConfig == null)
                throw new RuntimeException(
                                           "SnmpAgentConfig object not available for interface "
                                                   + ipaddr);

            if (parameters != null) {
                // "port" parm
                //
                if (parameters.get("port") != null) {
                    int port = ParameterMap.getKeyedInteger(
                                                            parameters,
                                                            "port",
                                                            agentConfig.getPort());
                    agentConfig.setPort(port);
                }

                // "timeout" parm
                //
                if (parameters.get("timeout") != null) {
                    int timeout = ParameterMap.getKeyedInteger(
                                                               parameters,
                                                               "timeout",
                                                               agentConfig.getTimeout());
                    agentConfig.setTimeout(timeout);
                }

                // "retry" parm
                //
                if (parameters.get("retry") != null) {
                    int retry = ParameterMap.getKeyedInteger(
                                                             parameters,
                                                             "retry",
                                                             agentConfig.getRetries());
                    agentConfig.setRetries(retry);
                }

                // "force version" parm
                //
                if (parameters.get("force version") != null) {
                    String version = (String) parameters.get("force version");
                    if (version.equalsIgnoreCase("snmpv1"))
                        agentConfig.setVersion(SnmpAgentConfig.VERSION1);
                    else if (version.equalsIgnoreCase("snmpv2")
                            || version.equalsIgnoreCase("snmpv2c"))
                        agentConfig.setVersion(SnmpAgentConfig.VERSION2C);

                    // TODO: make sure JoeSnmpStrategy correctly handles this.
                    else if (version.equalsIgnoreCase("snmpv3"))
                        agentConfig.setVersion(SnmpAgentConfig.VERSION3);
                }

                // Establish SNMP session with interface
                if (log().isDebugEnabled()) {
                    log().debug(
                                "poll: SnmpAgentConfig address: "
                                        + agentConfig);
                }

                /*
                 * Get two maps one with all configured admin tags and one of
                 * oper state
                 */
                Map<SnmpInstId, SnmpValue> tagResults = SnmpUtils.getOidValues(
                                                                               agentConfig,
                                                                               "CiscoIpSlaMonitor",
                                                                               SnmpObjId.get(RTT_ADMIN_TAG_OID));
                if (tagResults == null) {
                    log().warn("poll: No admin tags received! ");
                    return status;
                }

                Map<SnmpInstId, SnmpValue> operStateResults = SnmpUtils.getOidValues(
                                                                                     agentConfig,
                                                                                     "CiscoIpSlaMonitor",
                                                                                     SnmpObjId.get(RTT_OPER_STATE_OID));
                if (operStateResults == null) {
                    log().warn("poll: No oper state received! ");
                    return status;
                }

                // Iterate over the list of configured IP SLAs
                for (SnmpInstId ipslaInstance : tagResults.keySet()) {

                    log().debug(
                                "poll: " + "admin tag=" + adminTag
                                        + " value="
                                        + tagResults.get(ipslaInstance)
                                        + " oper state="
                                        + operStateResults.get(ipslaInstance));
                    /*
                     *  Check if a configured ip sla with specific tag exist
                     *  and is the operational state active 
                     */
                    if (tagResults.get(ipslaInstance).toString().equals(
                                                                        adminTag)
                            && operStateResults.get(ipslaInstance).toInt() == RTT_MON_OPER_STATE.ACTIVE.value()) {
                        log().debug("poll: admin tag found");
                        status = true;
                    }
                }
            }
        } catch (NullPointerException e) {
            log().warn(
                       "SNMP not available or CISCO-RTT-MON-MIB not supported!");
        } catch (NumberFormatException e) {
            log().warn(
                       "Number operator used on a non-number "
                               + e.getMessage());
        } catch (IllegalArgumentException e) {
            log().warn("Invalid SNMP Criteria: " + e.getMessage());
        } catch (Throwable t) {
            log().warn(
                       "Unexpected exception during SNMP poll of interface "
                               + InetAddressUtils.str(ipaddr), t);
        }
        return status;
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    public static ThreadCategory log() {
        return ThreadCategory.getInstance(CiscoIpSlaPlugin.class);
    }
}
