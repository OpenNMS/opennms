//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Aug 26: First version of this monitor, based on SnmpMonitor
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Level;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * status of services reported in the Host Resources SW Run Table. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 * 
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */

//this does snmp and there relies on the snmp configuration so it is not distributable

@Distributable(DistributionContext.DAEMON)
public class HostResourceSwRunMonitor extends SnmpMonitorStrategy {
    /**
     * Name of monitored service.
     */
    private static final String SERVICE_NAME = "HOST-RESOURCES";

    /**
     * Default OID for the table that represents the name of the software running.
     */
    private static final String HOSTRESOURCE_SW_NAME_OID = ".1.3.6.1.2.1.25.4.2.1.2";

    /**
     * Default OID for the table that represents the status of the software running.
     */
    private static final String HOSTRESOURCE_SW_STATUS_OID = ".1.3.6.1.2.1.25.4.2.1.7";

    /**
     * Interface attribute key used to store the interface's SnmpAgentConfig
     * object.
     */
    static final String SNMP_AGENTCONFIG_KEY = "org.opennms.netmgt.snmp.SnmpAgentConfig";

    /**
     * <P>
     * Returns the name of the service that the plug-in monitors ("SNMP").
     * </P>
     * 
     * @return The service that the plug-in monitors.
     */
    public String serviceName() {
        return SERVICE_NAME;
    }

    /**
     * <P>
     * Initialize the service monitor.
     * </P>
     * @param parameters
     *            Not currently used.
     * 
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     * 
     */
    public void initialize(Map<String, Object> parameters) {
        // Initialize the SnmpPeerFactory
        //
        try {
            SnmpPeerFactory.init();
        } catch (MarshalException ex) {
        	log().fatal("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
        	log().fatal("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
        	log().fatal("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }

        return;
    }

    /**
     * <P>
     * Called by the poller framework when an interface is being added to the
     * scheduler. Here we perform any necessary initialization to prepare the
     * NetworkInterface object for polling.
     * </P>
     * 
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                interface from being monitored.
     */
    public void initialize(MonitoredService svc) {
        NetworkInterface iface = svc.getNetInterface();
        // Log4j category
        //
        // Get interface address from NetworkInterface
        //
        super.initialize(svc);

        InetAddress ipAddr = (InetAddress) iface.getAddress();

        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(ipAddr);
        if (log().isDebugEnabled()) {
            log().debug("initialize: SnmpAgentConfig address: " + agentConfig);
        }

        // Add the snmp config object as an attribute of the interface
        //
        if (log().isDebugEnabled())
            log().debug("initialize: setting SNMP peer attribute for interface " + ipAddr.getHostAddress());

        iface.setAttribute(SNMP_AGENTCONFIG_KEY, agentConfig);

        log().debug("initialize: interface: " + agentConfig.getAddress() + " initialized.");

        return;
    }

    /**
     * <P>
     * The poll() method is responsible for polling the specified address for
     * SNMP service availability.
     * </P>
     * @param parameters
     *            The package parameters (timeout, retry, etc...) to be used for
     *            this poll.
     * @param iface
     *            The network interface to test the service on.
     * @return The availability of the interface and if a transition event
     *         should be supressed.
     * 
     * @exception RuntimeException
     *                Thrown for any uncrecoverable errors.
     */
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface iface = svc.getNetInterface();

        PollStatus status = PollStatus.unavailable();
        InetAddress ipaddr = (InetAddress) iface.getAddress();

        // Retrieve this interface's SNMP peer object
        //
        SnmpAgentConfig agentConfig = (SnmpAgentConfig) iface.getAttribute(SNMP_AGENTCONFIG_KEY);
        if (agentConfig == null) throw new RuntimeException("SnmpAgentConfig object not available for interface " + ipaddr);

        // Get configuration parameters
        //
        // This should never need to be overridden, but it can be in order to be used with similar tables.
        String serviceNameOid = ParameterMap.getKeyedString(parameters, "service-name-oid", HOSTRESOURCE_SW_NAME_OID);
        // This should never need to be overridden, but it can be in order to be used with similar tables.
        String serviceStatusOid = ParameterMap.getKeyedString(parameters, "service-status-oid", HOSTRESOURCE_SW_STATUS_OID);
        // This is the string that represents the service name to be monitored.
        String serviceName = ParameterMap.getKeyedString(parameters, "service-name", null);
        // The service name may appear in the table more than once. If this is set to true, all values must match the run level.
        String matchAll = ParameterMap.getKeyedString(parameters, "match-all", "false");
        // This is one of: 
        //                   running(1),
        //                   runnable(2),    -- waiting for resource
        //                                   -- (i.e., CPU, memory, IO)
        //                   notRunnable(3), -- loaded but waiting for event
        //                   invalid(4)      -- not loaded
        //
        // This represents the maximum run-level, i.e. 2 means either running(1) or runnable(2) pass.
        String runLevel = ParameterMap.getKeyedString(parameters, "run-level", "2");

        // set timeout and retries on SNMP peer object
        //
        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters, "retry", ParameterMap.getKeyedInteger(parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters, "port", agentConfig.getPort()));

        if (log().isDebugEnabled()) log().debug("poll: service= SNMP address= " + agentConfig);

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
            Map<SnmpInstId, SnmpValue> nameResults = SnmpUtils.getOidValues(agentConfig, "HostResourceSwRunMonitor", SnmpObjId.get(serviceNameOid));
            Map<SnmpInstId, SnmpValue> statusResults = SnmpUtils.getOidValues(agentConfig, "HostResourceSwRunMonitor", SnmpObjId.get(serviceStatusOid));

            // Iterate over the list of running services
            for(SnmpInstId nameInstance : nameResults.keySet()) {

                // See if the service name is in the list of running services
                if (nameResults.get(nameInstance).toString().equals(serviceName)) {
                    log().debug("poll: HostResourceSwRunMonitor poll succeeded, addr=" + ipaddr.getHostAddress() + " service name=" + serviceName + " value=" + nameResults.get(nameInstance));
                    // Using the instance of the service, get its status and see if it meets the criteria
                    if (meetsCriteria(statusResults.get(nameInstance), "<=", runLevel)) {
                        status = PollStatus.available();
                        // If we get here, that means the service passed the criteria, if only one match is desired we exit.
                        if ("false".equals(matchAll)) {
                           return status;
                        }
                    // if we get here, that means the meetsCriteria test failed. 
                    } else {
                        status = logDown(Level.DEBUG, "HostResourceSwRunMonitor poll failed, addr=" + ipaddr.getHostAddress() + " service name= " + serviceName + " status= " + statusResults.get(nameInstance) );
                        return status;
                    }
                }
            }

        } catch (NumberFormatException e) {
            status = logDown(Level.ERROR, "Number operator used on a non-number " + e.getMessage());
        } catch (IllegalArgumentException e) {
            status = logDown(Level.ERROR, "Invalid Snmp Criteria: " + e.getMessage());
        } catch (Throwable t) {
            status = logDown(Level.WARN, "Unexpected exception during SNMP poll of interface " + ipaddr.getHostAddress(), t);
        }

        // If matchAll is set to true, then the status is set to available above with a single match.
        // Otherwise, the service will be unavailable.

        return status;
    }

}
