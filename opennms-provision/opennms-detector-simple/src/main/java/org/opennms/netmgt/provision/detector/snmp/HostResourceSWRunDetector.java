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

package org.opennms.netmgt.provision.detector.snmp;

import java.net.InetAddress;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import antlr.StringUtils;

@Component
/**
 * <p>HostResourceSWRunDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
public class HostResourceSWRunDetector extends SnmpDetector {

    private static final Logger LOG = LoggerFactory.getLogger(HostResourceSWRunDetector.class);

    /**
     * The protocol supported by this detector
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
    
    private String m_serviceToDetect;
    
    private String m_serviceNameOid;
    /**
     * <p>Constructor for HostResourceSWRunDetector.</p>
     */
    public HostResourceSWRunDetector(){
        setServiceName(PROTOCOL_NAME);
        setServiceNameOid(HOSTRESOURCE_SW_NAME_OID);
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
    public boolean isServiceDetected(InetAddress address) {
        
        boolean status = false;

        // Retrieve this interface's SNMP peer object
        //
        SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(address);
        if (agentConfig == null) throw new RuntimeException("SnmpAgentConfig object not available for interface " + address);

        // Get configuration parameters
        //
        
        // This is the string that represents the service name to be monitored.
        String serviceName = getServiceToDetect(); 

        // set timeout and retries on SNMP peer object
        //
        configureAgentPTR(agentConfig);

        LOG.debug("capsd: service= SNMP address={}", agentConfig);

        // Establish SNMP session with interface
        //
        final String hostAddress = InetAddressUtils.str(address);
		try {
            LOG.debug("HostResourceSwRunMonitor.poll: SnmpAgentConfig address: {}", agentConfig);

            if (serviceName == null) {
                LOG.warn("HostResourceSwRunMonitor.poll: No Service Name Defined! ");
                return status;
            }

            // This returns two maps: one of instance and service name, and one of instance and status.
            Map<SnmpInstId, SnmpValue> nameResults = SnmpUtils.getOidValues(agentConfig, "HostResourceSwRunMonitor", SnmpObjId.get(getServiceNameOid()));

            // Iterate over the list of running services
            for(Entry<SnmpInstId, SnmpValue> entry  : nameResults.entrySet()) {
                SnmpValue value = entry.getValue();

                // See if the service name is in the list of running services
                if (match(serviceName, stripExtraQuotes(value.toString())) && !status) {
                    LOG.debug("poll: HostResourceSwRunMonitor poll succeeded, addr={} service name={} value={}", hostAddress, serviceName, value);
                    status = true;
                    break;
                }
            }

        } catch (NumberFormatException e) {
            LOG.warn("Number operator used on a non-number {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid SNMP Criteria: {}", e.getMessage());
        } catch (Throwable t) {
            LOG.warn("Unexpected exception during SNMP poll of interface {}", hostAddress, t);
        }

        return status;
        
    }

    private boolean match(String expectedText, String currentText) {
        if (expectedText.startsWith("~")) {
            return currentText.matches(expectedText.replaceFirst("~", ""));
        }
        return currentText.equalsIgnoreCase(expectedText);
    }

    private static String stripExtraQuotes(String string) {
        return StringUtils.stripFrontBack(string, "\"", "\"");
    }

    /**
     * <p>setServiceNameOid</p>
     *
     * @param serviceNameOid a {@link java.lang.String} object.
     */
    public void setServiceNameOid(String serviceNameOid) {
        m_serviceNameOid = serviceNameOid;
    }

    /**
     * <p>getServiceNameOid</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceNameOid() {
        return m_serviceNameOid;
    }

    /**
     * <p>setServiceToDetect</p>
     *
     * @param hostService a {@link java.lang.String} object.
     */
    public void setServiceToDetect(String hostService) {
        m_serviceToDetect = hostService;
    }

    /**
     * <p>getServiceToDetect</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceToDetect() {
        return m_serviceToDetect;
    }
}
