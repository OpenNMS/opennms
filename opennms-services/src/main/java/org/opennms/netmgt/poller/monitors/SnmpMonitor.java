//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Added the ability to imbed RRA information in poller packages.
// 2003 Jan 31: Cleaned up some unused imports.
// 2003 Jan 29: Added response times to certain monitors.
// 2009 Mar 29: Added reason code template feature for enhancement bug #3065. - jeffg@opennms.org
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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Level;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.PropertiesUtils;
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

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the SNMP service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 * 
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */

//this does snmp and there relies on the snmp configuration so it is not distributable
@Distributable(DistributionContext.DAEMON)
public class SnmpMonitor extends SnmpMonitorStrategy {
    /**
     * Name of monitored service.
     */
    private static final String SERVICE_NAME = "SNMP";

    /**
     * Default object to collect if "oid" property not available.
     */
    private static final String DEFAULT_OBJECT_IDENTIFIER = ".1.3.6.1.2.1.1.2.0"; // MIB-II
                                                                                // System
                                                                                // Object
                                                                                // Id

    /**
     * Interface attribute key used to store the interface's SnmpAgentConfig
     * object.
     */
    static final String SNMP_AGENTCONFIG_KEY = "org.opennms.netmgt.snmp.SnmpAgentConfig";

    private static final String DEFAULT_REASON_TEMPLATE = "Observed value '${observedValue}' does not meet criteria '${operator} ${operand}'";

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
    public PollStatus poll(MonitoredService svc, Map parameters) {
        NetworkInterface iface = svc.getNetInterface();

        PollStatus status = PollStatus.unavailable();
        InetAddress ipaddr = (InetAddress) iface.getAddress();

        // Retrieve this interface's SNMP peer object
        //
        SnmpAgentConfig agentConfig = (SnmpAgentConfig) iface.getAttribute(SNMP_AGENTCONFIG_KEY);
        if (agentConfig == null) throw new RuntimeException("SnmpAgentConfig object not available for interface " + ipaddr);

        // Get configuration parameters
        //
        String oid = ParameterMap.getKeyedString(parameters, "oid", DEFAULT_OBJECT_IDENTIFIER);
        String operator = ParameterMap.getKeyedString(parameters, "operator", null);
        String operand = ParameterMap.getKeyedString(parameters, "operand", null);
        String walkstr = ParameterMap.getKeyedString(parameters, "walk", "false");
        String matchstr = ParameterMap.getKeyedString(parameters, "match-all", "true");
        int countMin = ParameterMap.getKeyedInteger(parameters, "minimum", 0);
        int countMax = ParameterMap.getKeyedInteger(parameters, "maximum", 0);
        String reasonTemplate = ParameterMap.getKeyedString(parameters, "reason-template", DEFAULT_REASON_TEMPLATE);

        // set timeout and retries on SNMP peer object
        //
        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters, "retry", ParameterMap.getKeyedInteger(parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters, "port", agentConfig.getPort()));

        // Squirrel the configuration parameters away in a Properties for later expansion if service is down
        Properties svcParams = new Properties();
        svcParams.setProperty("oid", oid);
        svcParams.setProperty("operator", String.valueOf(operator));
        svcParams.setProperty("operand", String.valueOf(operand));
        svcParams.setProperty("walk", walkstr);
        svcParams.setProperty("matchAll", matchstr);
        svcParams.setProperty("minimum", String.valueOf(countMin));
        svcParams.setProperty("maximum", String.valueOf(countMax));
        svcParams.setProperty("timeout", String.valueOf(agentConfig.getTimeout()));
        svcParams.setProperty("retry", String.valueOf(agentConfig.getRetries()));
        svcParams.setProperty("retries", svcParams.getProperty("retry"));
        svcParams.setProperty("ipaddr", ipaddr.getHostAddress());
        svcParams.setProperty("port", String.valueOf(agentConfig.getPort()));

        if (log().isDebugEnabled()) log().debug("poll: service= SNMP address= " + agentConfig);

        // Establish SNMP session with interface
        //
        try {
            if (log().isDebugEnabled()) {
                log().debug("SnmpMonitor.poll: SnmpAgentConfig address: " +agentConfig);
            }
            SnmpObjId snmpObjectId = SnmpObjId.get(oid);

            if ("true".equals(walkstr)) {
                if (DEFAULT_REASON_TEMPLATE.equals(reasonTemplate)) {
                    reasonTemplate = "SNMP poll failed, addr=${ipaddr} oid=${oid}";
                }
                List<SnmpValue> results = SnmpUtils.getColumns(agentConfig, "snmpPoller", snmpObjectId);
                for(SnmpValue result : results) {
                    svcParams.setProperty("observedValue", result.toString());
                    if (result != null) {
                        log().debug("poll: SNMPwalk poll succeeded, addr=" + ipaddr.getHostAddress() + " oid=" + oid + " value=" + result);
                        if (meetsCriteria(result, operator, operand)) {
                            status = PollStatus.available();
                            if ("false".equals(matchstr)) {
                                return status;
                            }
                        } else if ("true".equals(matchstr)) {
                            status = logDown(Level.DEBUG, PropertiesUtils.substitute(reasonTemplate, svcParams));
                            return status;
                        }
                    }
                }

                // This if block will count the number of matches within a walk and mark the service
                // as up if it is between the minimum and maximum number, down if otherwise. Setting
                // the parameter "matchall" to "count" will act as if "walk" has been set to "true".
            } else if ("count".equals(matchstr)) {
                if (DEFAULT_REASON_TEMPLATE.equals(reasonTemplate)) {
                    reasonTemplate = "Value: ${matchCount} outside of range Min: ${minimum} to Max: ${maximum}";
                }
                int matchCount = 0;
                List<SnmpValue> results = SnmpUtils.getColumns(agentConfig, "snmpPoller", snmpObjectId);
                for(SnmpValue result : results) {

                    if (result != null) {
                        log().debug("poll: SNMPwalk poll succeeded, addr=" + ipaddr.getHostAddress() + " oid=" + oid + " value=" + result);
                        if (meetsCriteria(result, operator, operand)) {
                            matchCount++;
                        }
                    }
                }
                svcParams.setProperty("matchCount", String.valueOf(matchCount));
                log().debug("poll: SNMPwalk count succeeded, total=" + matchCount + " min=" + countMin + " max=" + countMax);
                if ((matchCount < countMax) && (matchCount > countMin)) {
                    status = PollStatus.available();
                } else {
                    status = logDown(Level.DEBUG, PropertiesUtils.substitute(reasonTemplate, svcParams));
                    return status;
                }

            } else {
                if (DEFAULT_REASON_TEMPLATE.equals(reasonTemplate)) {
                    if (operator != null) {
                        reasonTemplate = "Observed value '${observedValue}' does not meet criteria '${operator} ${operand}'";
                    } else {
                        reasonTemplate = "Observed value '${observedValue}' was null";
                    }
                }

                SnmpValue result = SnmpUtils.get(agentConfig, snmpObjectId);

                if (result != null) {
                    svcParams.setProperty("observedValue", result.toString());
                    log().debug("poll: SNMP poll succeeded, addr=" + ipaddr.getHostAddress() + " oid=" + oid + " value=" + result);
                    
                    if (meetsCriteria(result, operator, operand)) {
                        status = PollStatus.available();
                    } else {
                        status = PollStatus.unavailable(PropertiesUtils.substitute(reasonTemplate, svcParams));
                    }
                } else {
                    status = logDown(Level.DEBUG, "SNMP poll failed, addr=" + ipaddr.getHostAddress() + " oid=" + oid);
                }
            }

        } catch (NumberFormatException e) {
            status = logDown(Level.ERROR, "Number operator used on a non-number " + e.getMessage());
        } catch (IllegalArgumentException e) {
            status = logDown(Level.ERROR, "Invalid Snmp Criteria: " + e.getMessage());
        } catch (Throwable t) {
            status = logDown(Level.WARN, "Unexpected exception during SNMP poll of interface " + ipaddr.getHostAddress(), t);
        }

        return status;
    }

}
