/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
import java.util.Map;
import java.util.regex.Pattern;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check for disks via HOST-RESOURCES-MIB. This should be extended to
 * support BOTH UCD-SNMP-MIB and HOST-RESOURCES-MIB
 * <p>
 * This does SNMP and therefore relies on the SNMP configuration so it is not distributable.
 *
 * @author <a href="mailto:jason.aras@gmail.com">Jason Aras</a>
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 */
@Distributable(DistributionContext.DAEMON)
final public class DiskUsageMonitor extends SnmpMonitorStrategy {

    public static final Logger LOG = LoggerFactory.getLogger(DiskUsageMonitor.class);

    private static final String m_serviceName = "DISK-USAGE";

    private static final String hrStorageDescr = ".1.3.6.1.2.1.25.2.3.1.3";
    private static final String hrStorageSize = ".1.3.6.1.2.1.25.2.3.1.5";
    private static final String hrStorageUsed = ".1.3.6.1.2.1.25.2.3.1.6";

    /**
     * The available match-types for this monitor
     */
    private enum MatchType {

        EXACT, STARTSWITH, ENDSWITH, REGEX
    };

    /**
     * The available require-types for this monitor
     */
    private enum RequireType {

        NONE, ANY, ALL
    };

    /**
     * <P>
     * Returns the name of the service that the plug-in monitors ("DISK-USAGE").
     * </P>
     *
     * @return The service that the plug-in monitors.
     */
    public String serviceName() {
        return m_serviceName;
    }

    /**
     * {@inheritDoc}
     *
     * The poll() method is responsible for polling the specified address for
     * SNMP service availability.
     *
     * @param svc
     * @param parameters
     * @return PollStatus
     * <p>
     * @exception RuntimeException
     *                             Thrown for any unrecoverable errors.
     */
    @Override
    public PollStatus poll(final MonitoredService svc, final Map<String, Object> parameters) {
        MatchType matchType = MatchType.EXACT;
        RequireType reqType = RequireType.ALL;

        PollStatus status = PollStatus.available();
        InetAddress ipaddr = svc.getAddress();

        // Retrieve this interface's SNMP peer object
        //
        final SnmpAgentConfig agentConfig = getAgentConfig(svc, parameters);
        final String hostAddress = InetAddressUtils.str(ipaddr);
        LOG.debug("poll: setting SNMP peer attribute for interface {}", hostAddress);

        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters, "retry", ParameterMap.getKeyedInteger(parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters, "port", agentConfig.getPort()));

        String diskNamePattern = ParameterMap.getKeyedString(parameters, "disk", null);
        if (diskNamePattern == null) {
            throw new RuntimeException("Invalid null value for parameter 'disk'");
        }
        Integer percentFree = ParameterMap.getKeyedInteger(parameters, "free", 15);

        String matchTypeStr = ParameterMap.getKeyedString(parameters, "match-type", "exact");
        try {
            matchType = MatchType.valueOf(matchTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unknown value '" + matchTypeStr + "' for parameter 'match-type'");
        }

        String reqTypeStr = ParameterMap.getKeyedString(parameters, "require-type", "all");
        try {
            reqType = RequireType.valueOf(reqTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unknown value '" + reqTypeStr + "' for parameter 'require-type'");
        }

        LOG.debug("poll: diskNamePattern={}", diskNamePattern);
        LOG.debug("poll: percentfree={}", percentFree);
        LOG.debug("poll: matchType={}", matchTypeStr);
        LOG.debug("poll: reqType={}", reqTypeStr);

        LOG.debug("poll: service={} address={}", svc, agentConfig);

        try {
            LOG.debug("poll: SnmpAgentConfig address: {}", agentConfig);
            SnmpObjId hrStorageDescrSnmpObject = SnmpObjId.get(hrStorageDescr);

            Map<SnmpInstId, SnmpValue> results = SnmpUtils.getOidValues(agentConfig, "DiskUsagePoller", hrStorageDescrSnmpObject);

            if (results.isEmpty()) {
                LOG.debug("SNMP poll failed: no results, addr={} oid={}", hostAddress, hrStorageDescrSnmpObject);
                return PollStatus.unavailable("No entries found in hrStorageDescr");
            }

            boolean foundDisk = false;

            for (Map.Entry<SnmpInstId, SnmpValue> e : results.entrySet()) {
                LOG.debug("poll: SNMPwalk poll succeeded, addr={} oid={} instance={} value={}", hostAddress, hrStorageDescrSnmpObject, e.getKey(), e.getValue());
                final String snmpInstance = e.getKey().toString();
                final String diskName = e.getValue().toString();

                if (isMatch(diskName, diskNamePattern, matchType)) {
                    LOG.debug("poll: found disk={}", diskName);

                    final SnmpObjId hrStorageSizeSnmpObject = SnmpObjId.get(hrStorageSize, snmpInstance);
                    final SnmpObjId hrStorageUsedSnmpObject = SnmpObjId.get(hrStorageUsed, snmpInstance);

                    final SnmpValue snmpSize = SnmpUtils.get(agentConfig, hrStorageSizeSnmpObject);
                    final SnmpValue snmpUsed = SnmpUtils.get(agentConfig, hrStorageUsedSnmpObject);
                    float calculatedPercentage = ((((float) snmpSize.toLong() - (float) snmpUsed.toLong()) / (float) snmpSize.toLong())) * 100;

                    LOG.debug("poll: calculatedPercentage={} percentFree={}", calculatedPercentage, percentFree);

                    if (calculatedPercentage < percentFree) {

                        return PollStatus.unavailable(diskName + " usage high (" + (100 - (int) calculatedPercentage) + "%)");

                    } else {
                        if (matchType == MatchType.EXACT || reqType == RequireType.ANY) {
                            return status;
                        }
                        if (reqType == RequireType.ALL) {
                            foundDisk = true;
                        }
                    }
                }
            }

            if (foundDisk) {
                return status;
            }
            // if we get here.. it means we did not find the disk...  which means we should not be monitoring it.
            LOG.debug("poll: no disks found");
            return PollStatus.unavailable("Could not find " + diskNamePattern + " in hrStorageTable");

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

    private boolean isMatch(final String candidate, final String target, final MatchType matchType) {
        boolean matches = false;
        LOG.debug("isMatch: candidate is '{}', matching against target '{}'", candidate, target);
        if (matchType == MatchType.EXACT) {
            LOG.debug("isMatch: Attempting equality match: candidate '{}', target '{}'", candidate, target);
            matches = candidate.equals(target);
        } else if (matchType == MatchType.STARTSWITH) {
            LOG.debug("isMatch: Attempting startsWith match: candidate '{}', target '{}'", candidate, target);
            matches = candidate.startsWith(target);
        } else if (matchType == MatchType.ENDSWITH) {
            LOG.debug("isMatch: Attempting endsWith match: candidate '{}', target '{}'", candidate, target);
            matches = candidate.endsWith(target);
        } else if (matchType == MatchType.REGEX) {
            LOG.debug("isMatch: Attempting regex match: candidate '{}', target '{}'", candidate, target);
            matches = Pattern.compile(target).matcher(candidate).find();
        }
        LOG.debug("isMatch: Match is {}", matches ? "positive" : "negative");
        return matches;
    }
}
