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

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Level;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.utils.ParameterMap;

/**
 * <p>
 * This class is used to monitor Dell OpenManage chassis. The specific OIDs
 * referenced to "SNMP Reference Guide", available from
 * http://support.dell.com/support/edocs/software/svradmin/6.1/en
 * </p>
 * 
 * @author <A HREF="mailto:r.trommer@open-factory.org">Ronny Trommer</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS</A>
 */

// this does snmp and there relies on the snmp configuration so it is not
// distributable
@Distributable(DistributionContext.DAEMON)
final public class OpenManageChassisMonitor extends SnmpMonitorStrategy {
    /**
     * Name of monitored service.
     */
    private static final String m_serviceName = "Dell_OpenManageChassis";

    /**
     * Interface attribute key used to store the interface's SnmpAgentConfig
     * object.
     */
    static final String SNMP_AGENTCONFIG_KEY = "org.opennms.netmgt.snmp.SnmpAgentConfig";

    /**
     * Defines the status of the chassis.
     */
    private static final String CHASSIS_STATUS_OID = ".1.3.6.1.4.1.674.10892.1.300.10.1.4.1";

    /**
     * Defines the overall status of this chassis (ESM) event log.
     */
    private static final String EVENT_LOG_STATUS_OID = ".1.3.6.1.4.1.674.10892.1.200.10.1.41.1";

    /**
     * Defines the manufacturer’s name for this chassis.
     */
    private static final String MANUFACTURER_OID = ".1.3.6.1.4.1.674.10892.1.300.10.1.8.1";

    /**
     * Defines the status of the chassis.
     */
    private static final String MODEL_NAME_OID = "1.3.6.1.4.1.674.10892.1.300.10.1.9.1";

    /**
     * Defines the service tag name for this chassis.
     */
    private static final String SERVICE_TAG_OID = ".1.3.6.1.4.1.674.10892.1.300.10.1.11.1";

    /**
     * Implement the dell status
     */
    private enum DELL_STATUS {
        OTHER(1), UNKNOWN(2), OK(3), NON_CRITICAL(4), CRITICAL(5), NON_RECOVERABLE(
                6);

        private final int state; // state code

        DELL_STATUS(int s) {
            this.state = s;
        }

        private int value() {
            return this.state;
        }
    };

    /**
     * <P>
     * Returns the name of the service that the plug-in monitors
     * </P>
     * 
     * @return The service that the plug-in monitors.
     */
    public String serviceName() {
        return m_serviceName;
    }

    /**
     * <P>
     * Initialize the service monitor.
     * </P>
     * 
     * @param parameters
     *            Not currently used.
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents
     *                the plug-in from functioning.
     */
    public void initialize(Map parameters) {
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
     *                Thrown if an unrecoverable error occurs that prevents
     *                the interface from being monitored.
     */
    public void initialize(MonitoredService svc) {
        NetworkInterface iface = svc.getNetInterface();
        // Log4j category
        //
        // Get interface address from NetworkInterface
        //
        super.initialize(svc);

        InetAddress ipAddr = (InetAddress) iface.getAddress();

        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(
                                                                                   ipAddr);
        if (log().isDebugEnabled()) {
            log().debug("initialize: SnmpAgentConfig address: " + agentConfig);
        }

        // Add the snmp config object as an attribute of the interface
        //
        if (log().isDebugEnabled())
            log().debug(
                        "initialize: OpenManageChassisMonitor setting SNMP peer attribute for interface "
                                + ipAddr.getHostAddress());

        iface.setAttribute(SNMP_AGENTCONFIG_KEY, agentConfig);

        log().debug(
                    "initialize: interface: " + agentConfig.getAddress()
                            + " initialized.");

        return;
    }

    /**
     * <P>
     * The poll() method is responsible for polling the specified address for
     * SNMP service availability.
     * </P>
     * 
     * @param parameters
     *            The package parameters (timeout, retry, etc...) to be used
     *            for this poll.
     * @param iface
     *            The network interface to test the service on.
     * @return The availability of the interface and if a transition event
     *         should be supressed.
     * @exception RuntimeException
     *                Thrown for any uncrecoverable errors.
     */
    public PollStatus poll(MonitoredService svc, Map parameters) {
        NetworkInterface iface = svc.getNetInterface();

        String returnValue = new String();

        PollStatus status = PollStatus.unavailable();
        InetAddress ipaddr = (InetAddress) iface.getAddress();

        // Initialize the messages if the session is down
        String eventLogStatusTxt = "N/A";
        String manufacturerName = "N/A";
        String modelName = "N/A";
        String serviceTagTxt = "N/A";
        String chassisStatusTxt = "N/A";

        // Retrieve this interface's SNMP peer object
        //
        SnmpAgentConfig agentConfig = (SnmpAgentConfig) iface.getAttribute(SNMP_AGENTCONFIG_KEY);
        if (agentConfig == null)
            throw new RuntimeException(
                                       "SnmpAgentConfig object not available for interface "
                                               + ipaddr);

        // set timeout and retries on SNMP peer object
        //
        agentConfig.setTimeout(ParameterMap.getKeyedInteger(
                                                            parameters,
                                                            "timeout",
                                                            agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(
                                                            parameters,
                                                            "retry",
                                                            ParameterMap.getKeyedInteger(
                                                                                         parameters,
                                                                                         "retries",
                                                                                         agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(
                                                         parameters,
                                                         "port",
                                                         agentConfig.getPort()));

        // Establish SNMP session with interface
        //
        try {
            if (log().isDebugEnabled()) {
                log().debug("poll: SnmpAgentConfig address: " + agentConfig);
            }

            // Get the chassis status
            SnmpObjId chassisStatusSnmpObject = SnmpObjId.get(CHASSIS_STATUS_OID);
            SnmpValue chassisStatus = SnmpUtils.get(agentConfig,
                                                    chassisStatusSnmpObject);

            // If no chassis status is received or SNMP is not possible,
            // service is down
            if (chassisStatus == null) {
                log().warn("No chassis status received!");
                return status;
            } else {
                if (log().isDebugEnabled()) {
                    log().debug("poll: chassis status: " + chassisStatus);
                }
            }

            /*
             * Do no unnecessary SNMP requests, if chassis status is OK,
             * return with service available and go away.
             */
            if (chassisStatus.toInt() == DELL_STATUS.OK.value()) {
                if (log().isDebugEnabled()) {
                    log().debug(
                                "poll: chassis status: "
                                        + chassisStatus.toInt());
                }
                return PollStatus.available();
            } else {
                if (log().isDebugEnabled()) {
                    log().debug(
                                "poll: chassis status: "
                                        + chassisStatus.toInt());
                }
                chassisStatusTxt = resolveDellStatus(chassisStatus.toInt());
            }

            // Chassis status is not OK gather some information
            SnmpObjId eventLogStatusSnmpObject = SnmpObjId.get(EVENT_LOG_STATUS_OID);
            SnmpValue eventLogStatus = SnmpUtils.get(agentConfig,
                                                     eventLogStatusSnmpObject);
            // Check correct MIB-Support
            if (eventLogStatus == null) {
                log().warn("Cannot receive eventLogStatus.");
            } else {
                if (log().isDebugEnabled()) {
                    log().debug("poll: eventLogStatus: " + eventLogStatus);
                }
                eventLogStatusTxt = resolveDellStatus(eventLogStatus.toInt());
            }

            SnmpObjId manufacturerSnmpObject = SnmpObjId.get(MANUFACTURER_OID);
            SnmpValue manufacturer = SnmpUtils.get(agentConfig,
                                                   manufacturerSnmpObject);
            // Check correct MIB-Support
            if (manufacturer == null) {
                log().warn("Cannot receive manufacturer.");
            } else {
                if (log().isDebugEnabled()) {
                    log().debug("poll: manufacturer: " + manufacturer);
                }
                manufacturerName = manufacturer.toString();
            }

            SnmpObjId modelSnmpObject = SnmpObjId.get(MODEL_NAME_OID);
            SnmpValue model = SnmpUtils.get(agentConfig, modelSnmpObject);
            // Check correct MIB-Support
            if (model == null) {
                log().warn("Cannot receive model name.");
            } else {
                if (log().isDebugEnabled()) {
                    log().debug("poll: model name: " + model);
                }
                modelName = model.toString();
            }

            SnmpObjId serviceTagSnmpObject = SnmpObjId.get(SERVICE_TAG_OID);
            SnmpValue serviceTag = SnmpUtils.get(agentConfig,
                                                 serviceTagSnmpObject);
            // Check correct MIB-Support
            if (serviceTag == null) {
                log().warn("Cannot receive service tag");
            } else {
                if (log().isDebugEnabled()) {
                    log().debug("poll: service tag: " + serviceTag);
                }
                serviceTagTxt = serviceTag.toString();
            }

            returnValue = "Chassis status from "
                    + manufacturerName
                    + " "
                    + modelName
                    + " with service tag "
                    + serviceTagTxt
                    + " is "
                    + chassisStatusTxt
                    + ". Last event log status is "
                    + eventLogStatusTxt
                    + ". For further information, check your OpenManage website!";
            // Set service down and return gathered information
            status = PollStatus.unavailable(returnValue);

        } catch (NullPointerException e) {
            status = logDown(Level.WARN,
                             "Unexpected error during SNMP poll of interface "
                                     + ipaddr.getHostAddress(), e);
        } catch (NumberFormatException e) {
            status = logDown(Level.WARN,
                             "Number operator used on a non-number "
                                     + e.getMessage());
        } catch (IllegalArgumentException e) {
            status = logDown(Level.WARN, "Invalid Snmp Criteria: "
                    + e.getMessage());
        } catch (Throwable t) {
            status = logDown(Level.WARN,
                             "Unexpected exception during SNMP poll of interface "
                                     + ipaddr.getHostAddress(), t);
        }

        // If matchAll is set to true, then the status is set to available
        // above with a single match.
        // Otherwise, the service will be unavailable.
        return status;
    }

    /**
     * Method to resolve a given Dell status to human readable string.
     * 
     * @param sc
     *            Dell status code
     * @return Human readable Dell status
     */
    private String resolveDellStatus(int sc) {
        String name = "N/A";
        if (DELL_STATUS.OTHER.value() == sc)
            name = DELL_STATUS.OTHER.name();
        if (DELL_STATUS.UNKNOWN.value() == sc)
            name = DELL_STATUS.UNKNOWN.name();
        if (DELL_STATUS.OK.value() == sc)
            name = DELL_STATUS.OK.name();
        if (DELL_STATUS.NON_CRITICAL.value() == sc)
            name = DELL_STATUS.NON_CRITICAL.name();
        if (DELL_STATUS.CRITICAL.value() == sc)
            name = DELL_STATUS.CRITICAL.name();
        if (DELL_STATUS.NON_RECOVERABLE.value() == sc)
            name = DELL_STATUS.NON_RECOVERABLE.name();
        return name;
    }
}
