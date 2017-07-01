/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the SNMP service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 * <p>
 * This does SNMP and therefore relies on the SNMP configuration so it is not distributable.
 * </p>
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
@Distributable(DistributionContext.DAEMON)
public class SnmpMonitor extends SnmpMonitorStrategy {
    
    public static final Logger LOG = LoggerFactory.getLogger(SnmpMonitor.class);

    /**
     * Default object to collect if "oid" property not available.
     */
    private static final String DEFAULT_OBJECT_IDENTIFIER = ".1.3.6.1.2.1.1.2.0"; // MIB-II
                                                                                // System
                                                                                // Object
                                                                                // Id

    private static final String DEFAULT_REASON_TEMPLATE = "Observed value '${observedValue}' does not meet criteria '${operator} ${operand}'";

    /**
     * {@inheritDoc}
     *
     * <P>
     * The poll() method is responsible for polling the specified address for
     * SNMP service availability.
     * </P>
     * @exception RuntimeException
     *                Thrown for any unrecoverable errors.
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        PollStatus status = PollStatus.unavailable();
        InetAddress ipaddr = svc.getAddress();

        // Retrieve this interface's SNMP peer object
        //
        final SnmpAgentConfig agentConfig = getAgentConfig(svc, parameters);
        final String hostAddress = InetAddressUtils.str(ipaddr);

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
        String hexstr = ParameterMap.getKeyedString(parameters, "hex", "false");

        hex = "true".equalsIgnoreCase(hexstr);
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
        svcParams.setProperty("ipaddr", hostAddress);
        svcParams.setProperty("port", String.valueOf(agentConfig.getPort()));
        svcParams.setProperty("hex", hexstr);


        // Establish SNMP session with interface
        //
        try {

            TimeoutTracker tracker = new TimeoutTracker(parameters, agentConfig.getRetries(), agentConfig.getTimeout());
            tracker.reset();
            tracker.startAttempt();

            SnmpObjId snmpObjectId = SnmpObjId.get(oid);

            // This if block will count the number of matches within a walk and mark the service
            // as up if it is between the minimum and maximum number, down if otherwise. Setting
            // the parameter "matchall" to "count" will act as if "walk" has been set to "true".
            if ("count".equals(matchstr)) {
                if (DEFAULT_REASON_TEMPLATE.equals(reasonTemplate)) {
                    reasonTemplate = "Value: ${matchCount} outside of range Min: ${minimum} to Max: ${maximum}";
                }
                int matchCount = 0;
                List<SnmpValue> results = SnmpUtils.getColumns(agentConfig, "snmpPoller", snmpObjectId);
                for(SnmpValue result : results) {

                    if (result != null) {
                        LOG.debug("poll: SNMPwalk poll succeeded, addr={} oid={} value={}", hostAddress, oid, result);
                        if (meetsCriteria(result, operator, operand)) {
                            matchCount++;
                        }
                    }
                }
                svcParams.setProperty("matchCount", String.valueOf(matchCount));
                LOG.debug("poll: SNMPwalk count succeeded, total={} min={} max={}", matchCount, countMin, countMax);
                if ((countMin <= matchCount) && (matchCount <= countMax)) {
                    status = PollStatus.available(tracker.elapsedTimeInMillis());
                } else {
                    String reason = PropertiesUtils.substitute(reasonTemplate, svcParams);
                    LOG.debug(reason);
                    status = PollStatus.unavailable(reason);
                    return status;
                }
            } else if ("true".equals(walkstr)) {
                if (DEFAULT_REASON_TEMPLATE.equals(reasonTemplate)) {
                    reasonTemplate = "SNMP poll failed, addr=${ipaddr} oid=${oid}";
                }
                List<SnmpValue> results = SnmpUtils.getColumns(agentConfig, "snmpPoller", snmpObjectId);
                for(SnmpValue result : results) {
                    if (result != null) {
                        svcParams.setProperty("observedValue", getStringValue(result));
                        if (meetsCriteria(result, operator, operand)) {
                            status = PollStatus.available(tracker.elapsedTimeInMillis());
                            if ("false".equals(matchstr)) {
                                return status;
                            }
                        } else if ("true".equals(matchstr)) {
                            String reason = PropertiesUtils.substitute(reasonTemplate, svcParams);
                            LOG.debug(reason);
                            status = PollStatus.unavailable(reason);
                            return status;
                        }
                    }
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
                    svcParams.setProperty("observedValue", getStringValue(result));
                    LOG.debug("poll: SNMP poll succeeded, addr={} oid={} value={}", hostAddress, oid, result);
                    
                    if (meetsCriteria(result, operator, operand)) {
                        status = PollStatus.available(tracker.elapsedTimeInMillis());
                    } else {
                        status = PollStatus.unavailable(PropertiesUtils.substitute(reasonTemplate, svcParams));
                    }
                } else {
                    String reason = "SNMP poll failed, addr=" + hostAddress + " oid=" + oid;
                    LOG.debug(reason);
                    status = PollStatus.unavailable(reason);
                }
            }

        } catch (NumberFormatException e) {
            String reason = "Number operator used on a non-number " + e.getMessage();
            LOG.debug(reason);
            status = PollStatus.unavailable(reason);
        } catch (IllegalArgumentException e) {
            String reason = "Invalid SNMP Criteria: " + e.getMessage();
            LOG.debug(reason);
            status = PollStatus.unavailable(reason);
        } catch (Throwable t) {
            String reason = "Unexpected exception during SNMP poll of interface " + hostAddress;
            LOG.debug(reason, t);
            status = PollStatus.unavailable(reason);
        }

        return status;
    }

}
