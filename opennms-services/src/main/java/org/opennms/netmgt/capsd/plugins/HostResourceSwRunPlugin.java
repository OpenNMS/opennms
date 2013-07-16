/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import antlr.StringUtils;

/**
 * This class is used to test if a particular process is listed
 * in the HOST-RESOURCES running software table.
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog</A>
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 */
public class HostResourceSwRunPlugin extends AbstractPlugin {
    
    
    private static final Logger LOG = LoggerFactory.getLogger(HostResourceSwRunPlugin.class);

    /**
     * The protocol supported by this plugin
     */
    private static final String PROTOCOL_NAME = "HOST-RESOURCES";

    /**
     * Default OID for the table that represents the name of the software running.
     */
    private static final String HOSTRESOURCE_SW_NAME_OID = ".1.3.6.1.2.1.25.4.2.1.2";

    /**
     * Interface attribute key used to store the interface's SnmpAgentConfig
     * object.
     */
    static final String SNMP_AGENTCONFIG_KEY = "org.opennms.netmgt.snmp.SnmpAgentConfig";

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
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     */
    @Override
    public boolean isProtocolSupported(InetAddress address) {
        return isProtocolSupported(address, null);
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * The qualifier map passed to the method is used by the plugin to return
     * additional information by key-name. These key-value pairs can be added to
     * service events if needed.
     */
    @Override
    public boolean isProtocolSupported(InetAddress ipaddr, Map<String, Object> parameters) {
        
        boolean status = false;

        // Retrieve this interface's SNMP peer object
        //
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(ipaddr);
        if (agentConfig == null) throw new RuntimeException("SnmpAgentConfig object not available for interface " + ipaddr);

        // Get configuration parameters
        //
        // This should never need to be overridden, but it can be in order to be used with similar tables.
        String serviceNameOid = ParameterMap.getKeyedString(parameters, "service-name-oid", HOSTRESOURCE_SW_NAME_OID);
        // This is the string that represents the service name to be monitored.
        String serviceName = ParameterMap.getKeyedString(parameters, "service-name", null);

        // set timeout and retries on SNMP peer object
        //
        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters, "retry", ParameterMap.getKeyedInteger(parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters, "port", agentConfig.getPort()));

        LOG.debug("capsd: service= SNMP address= {}", agentConfig);

        try {
            LOG.debug("HostResourceSwRunMonitor.poll: SnmpAgentConfig address: {}", agentConfig);

            if (serviceName == null) {
                LOG.warn("HostResourceSwRunMonitor.poll: No Service Name Defined! ");
                return status;
            }

            // This returns two maps: one of instance and service name, and one of instance and status.
            Map<SnmpInstId, SnmpValue> nameResults = SnmpUtils.getOidValues(agentConfig, "HostResourceSwRunMonitor", SnmpObjId.get(serviceNameOid));

            // Iterate over the list of running services
            for(SnmpInstId nameInstance : nameResults.keySet()) {

                // See if the service name is in the list of running services
                if (match(serviceName, stripExtraQuotes(nameResults.get(nameInstance).toString()))) {
                    LOG.debug("poll: HostResourceSwRunMonitor poll succeeded, addr={} service name={} value={}", InetAddressUtils.str(ipaddr), serviceName, nameResults.get(nameInstance));
                    status = true;
                    break;
                }
            }

        } catch (NumberFormatException e) {
            LOG.warn("Number operator used on a non-number {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid SNMP Criteria: {}", e.getMessage());
        } catch (Throwable t) {
            LOG.warn("Unexpected exception during SNMP poll of interface {}", InetAddressUtils.str(ipaddr), t);
        }

        return status;
        
    }

    private boolean match(String expectedText, String currentText) {
        if (expectedText.startsWith("~")) {
            return currentText.matches(expectedText.replaceFirst("~", ""));
        }
        return currentText.equalsIgnoreCase(expectedText);
    }

    private String stripExtraQuotes(String string) {
        return StringUtils.stripFrontBack(string, "\"", "\"");
    }
}
