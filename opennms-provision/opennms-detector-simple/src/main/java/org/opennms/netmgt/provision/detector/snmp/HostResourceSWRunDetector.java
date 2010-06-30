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

import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
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
    public boolean isServiceDetected(InetAddress address, DetectorMonitor detectMonitor) {
        
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

        if (log().isDebugEnabled()) log().debug("capsd: service= SNMP address= " + agentConfig);

        // Establish SNMP session with interface
        //
        try {
            if (log().isDebugEnabled()) {
                log().debug("HostResourceSwRunMonitor.poll: SnmpAgentConfig address: " +agentConfig);
            }

            if (serviceName == null) {
                log().warn("HostResourceSwRunMonitor.poll: No Service Name Defined! ");
                return status;
            }

            // This returns two maps: one of instance and service name, and one of instance and status.
            Map<SnmpInstId, SnmpValue> nameResults = SnmpUtils.getOidValues(agentConfig, "HostResourceSwRunMonitor", SnmpObjId.get(getServiceNameOid()));

            // Iterate over the list of running services
            for(SnmpInstId nameInstance : nameResults.keySet()) {
                
                // See if the service name is in the list of running services
                if (stripExtraParens(nameResults.get(nameInstance).toString()).equals(serviceName) && !status) {
                    log().debug("poll: HostResourceSwRunMonitor poll succeeded, addr=" + address.getHostAddress() + " service name=" + serviceName + " value=" + nameResults.get(nameInstance));
                        status = true;
                }
            }

        } catch (NumberFormatException e) {
            log().warn("Number operator used on a non-number " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log().warn("Invalid Snmp Criteria: " + e.getMessage());
        } catch (Throwable t) {
            log().warn("Unexpected exception during SNMP poll of interface " + address.getHostAddress(), t);
        }

        return status;
        
    }

    private String stripExtraParens(String string) {
        String retString = "";
        if(string.startsWith("\"")){
            String temp = StringUtils.stripFront(string, '"');
            retString = StringUtils.stripBack(temp, '"');
        }
        return retString;
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
