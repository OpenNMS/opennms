/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>NetScalerGroupHealthMonitor class.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Distributable(DistributionContext.DAEMON)
public class NetScalerGroupHealthMonitor extends SnmpMonitorStrategy {
    private static final String SVC_GRP_MEMBER_STATE = ".1.3.6.1.4.1.5951.4.1.2.7.1.6";
    private static final Logger LOG = LoggerFactory.getLogger(NetScalerGroupHealthMonitor.class);

    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        InetAddress ipaddr = svc.getAddress();

        final SnmpAgentConfig agentConfig = getAgentConfig(svc, parameters);
        final String hostAddress = InetAddressUtils.str(ipaddr);

        PollStatus status = PollStatus.unavailable("NetScalerGroupHealthMonitor: cannot determinate group health, addr=" + hostAddress);

        int groupHealth = ParameterMap.getKeyedInteger(parameters, "group-health", 60);
        String groupName = ParameterMap.getKeyedString(parameters, "group-name", null);
        if (groupName == null) {
            status.setReason("NetScalerGroupHealthMonitor no group-name defined, addr=" + hostAddress);
            LOG.warn("NetScalerGroupHealthMonitor.poll: No Service Name Defined!");
            return status;
        }

        int snLength = groupName.length();
        final StringBuilder serviceOidBuf = new StringBuilder(SVC_GRP_MEMBER_STATE);
        serviceOidBuf.append(".").append(Integer.toString(snLength));
        for (byte thisByte : groupName.getBytes()) {
            serviceOidBuf.append(".").append(Byte.toString(thisByte));
        }
        LOG.debug("For group name '{}', OID to check is {}", groupName, serviceOidBuf.toString());

        try {
            final SnmpObjId groupStateOid = SnmpObjId.get(serviceOidBuf.toString());
            final Map<SnmpInstId, SnmpValue> hostResults = new HashMap<SnmpInstId, SnmpValue>();
            RowCallback callback = new RowCallback() {
                @Override
                public void rowCompleted(SnmpRowResult result) {
                    hostResults.put(result.getInstance(), result.getValue(groupStateOid));
                }
            };
            TableTracker tracker = new TableTracker(callback, groupStateOid);
            try (SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "NetScalerGroupHealthMonitor", tracker)) {
                walker.start();
                walker.waitFor();
            }

            int totalServers = hostResults.size();
            if (totalServers == 0) {
                status = PollStatus.unavailable("NetScalerGroupHealthMonitor poll failed: there are 0 servers on group " + groupName + " for " + hostAddress);
                LOG.debug(status.getReason());
            }

            int activeServers = 0;
            for (SnmpValue v : hostResults.values()) {
                if (v.toInt() == 7) {
                    activeServers++;
                }
            }

            double health = (new Double(activeServers)/new Double(totalServers)) * 100.0;
            LOG.debug("There are {} of {} active servers ({}%) on group {} for NetScaler {}", activeServers, totalServers, health, groupName, hostAddress);

            if (health >= groupHealth) {
                status = PollStatus.available();
            } else {
                status = PollStatus.unavailable("NetScalerGroupHealthMonitor poll failed: there are " + activeServers + " of " + totalServers + " servers active (" + health + "%) on group " + groupName + ", which is less than " + groupHealth + "% for " + hostAddress);
                LOG.debug(status.getReason());
            }
        } catch (Throwable t) {
            status = PollStatus.unavailable("Unexpected exception during SNMP poll of interface " + hostAddress);
            LOG.warn(status.getReason(), t);
        }
        return status;
    }
}
