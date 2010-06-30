/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.core.utils.ParameterMap;

/**
 * <p>
 * This class is used to test if Dell OpenManage chassis monitoring is possible.
 * The specific OIDs referenced to "SNMP Reference Guide", available from
 *
 *      http://support.dell.com/support/edocs/software/svradmin/6.1/en
 * </p>
 *
 * @author <A HREF="mailto:r.trommer@open-factory.org">Ronny Trommer</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS</A>
 * @author <A HREF="mailto:r.trommer@open-factory.org">Ronny Trommer</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS</A>
 * @version $Id: $
 */
public final class OpenManageChassisPlugin extends SnmpPlugin {
    /**
     * Name of monitored service.
     */
    private static final String PROTOCOL_NAME = "Dell_OpenManageChassis";

    /**
     * This attribute defines the status of this chassis.
     */
    private static final String CHASSIS_STATUS_OID = ".1.3.6.1.4.1.674.10892.1.200.10.1.4.1";

    /**
     * Implement the chassis status
     */
    private enum DELL_STATUS {
        OTHER(1), UNKNOWN(2), OK(3), NON_CRITICAL(4), CRITICAL(5), NON_RECOVERABLE(6);

        private final int state; // state code

        DELL_STATUS(int s) {
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
    public boolean isProtocolSupported(InetAddress ipaddr,
            Map<String, Object> qualifiers) {
        try {
            
            SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(ipaddr);
            if (agentConfig == null) throw new RuntimeException("SnmpAgentConfig object not available for interface " + ipaddr);
            
            if (qualifiers != null) {
                // "port" parm
                //
                if (qualifiers.get("port") != null) {
                    int port = ParameterMap.getKeyedInteger(qualifiers,"port",agentConfig.getPort());
                    agentConfig.setPort(port);
                }

                // "timeout" parm
                //
                if (qualifiers.get("timeout") != null) {
                    int timeout = ParameterMap.getKeyedInteger(qualifiers,"timeout",agentConfig.getTimeout());
                    agentConfig.setTimeout(timeout);
                }

                // "retry" parm
                //
                if (qualifiers.get("retry") != null) {
                    int retry = ParameterMap.getKeyedInteger(qualifiers,"retry",agentConfig.getRetries());
                    agentConfig.setRetries(retry);
                }

                // "force version" parm
                //
                if (qualifiers.get("force version") != null) {
                    String version = (String) qualifiers.get("force version");
                    if (version.equalsIgnoreCase("snmpv1"))
                        agentConfig.setVersion(SnmpAgentConfig.VERSION1);
                    else if (version.equalsIgnoreCase("snmpv2")
                            || version.equalsIgnoreCase("snmpv2c"))
                        agentConfig.setVersion(SnmpAgentConfig.VERSION2C);

                    // TODO: make sure JoeSnmpStrategy correctly handles this.
                    else if (version.equalsIgnoreCase("snmpv3"))
                        agentConfig.setVersion(SnmpAgentConfig.VERSION3);
                }

                // Get the OpenManage chassis status
                SnmpObjId chassisStatusSnmpObject = SnmpObjId.get(CHASSIS_STATUS_OID);
                SnmpValue chassisStatus = SnmpUtils.get(agentConfig, chassisStatusSnmpObject);
                
                // If no chassis status received, do not detect the protocol and quit
                if (chassisStatus == null)
                {
                    log().warn("Cannot receive chassis status");
                    return false;
                } else {
                    if (log().isDebugEnabled()) {
                        log().debug("poll: OpenManageChassis: " + chassisStatus);
                    }
                }

                // Validate chassis status, check status is somewhere between OTHER and NON_RECOVERABLE
                if  (Integer.parseInt(chassisStatus.toString()) >= DELL_STATUS.OTHER.value() && 
                    Integer.parseInt(chassisStatus.toString()) <= DELL_STATUS.NON_RECOVERABLE.value())
                {
                    // OpenManage chassis status detected
                    if (log().isDebugEnabled()) {
                        log().debug("poll: OpenManageChassis: is valid, protocol supported.");
                    }
                    return true;
                }
            }
        } catch (NullPointerException e) {
            log().warn("SNMP not available!");
        } catch (NumberFormatException e) {
            log().warn("Number operator used on a non-number " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log().warn("Invalid Snmp Criteria: " + e.getMessage());
        } catch (Throwable t) {
            log().warn("Unexpected exception during SNMP poll of interface " + ipaddr, t);
        }
        return false;
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    public static ThreadCategory log() {
        return ThreadCategory.getInstance(OpenManageChassisPlugin.class);
    }
}
