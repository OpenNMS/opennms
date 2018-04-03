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
import java.util.ArrayList;
import java.util.Map;

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
 * <p>
 * Check for process via UCD-SNMP-MIB .
 * </p>
 * <p>
 * This does SNMP and therefore relies on the SNMP configuration so it is not distributable.
 * </p>
 *
 * @author <A HREF="mailto:cliles@capario.com">Chris Liles</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</a>
 * @version $Id: $
 */
@Distributable(DistributionContext.DAEMON)
final public class PrTableMonitor extends SnmpMonitorStrategy {
    public static final Logger LOG = LoggerFactory.getLogger(PrTableMonitor.class);

    private static final String prTableErrorFlag = "1.3.6.1.4.1.2021.2.1.100";
    private static final String prTableErrorMsg = "1.3.6.1.4.1.2021.2.1.101";

    /**
     * {@inheritDoc}
     *
     * <P>
     * The poll() method is responsible for polling the specified address for
     * SNMP service availability.
     * </P>
     * @exception RuntimeException
     *                Thrown for any uncrecoverable errors.
     */
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        PollStatus status = PollStatus.available();
        InetAddress ipaddr = svc.getAddress();

        ArrayList<String> errorStringReturn = new ArrayList<>();

        // Retrieve this interface's SNMP peer object
        final SnmpAgentConfig agentConfig = getAgentConfig(svc, parameters);
        final String hostAddress = InetAddressUtils.str(ipaddr);
        LOG.debug("poll: setting SNMP peer attribute for interface {}", hostAddress);

        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters, "retry", ParameterMap.getKeyedInteger(parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters, "port", agentConfig.getPort()));

        LOG.debug("poll: service= SNMP address= {}", agentConfig);

        try {
            LOG.debug("PrTableMonitor.poll: SnmpAgentConfig address: {}", agentConfig);
            SnmpObjId prTableErrorSnmpObject = SnmpObjId.get(prTableErrorFlag);

            Map<SnmpInstId, SnmpValue> flagResults = SnmpUtils.getOidValues(agentConfig, "PrTableMonitor", prTableErrorSnmpObject);

            if(flagResults.size() == 0) {
                LOG.debug("SNMP poll failed: no results, addr={} oid={}", hostAddress, prTableErrorSnmpObject);
                return PollStatus.unavailable();
            }

            for (Map.Entry<SnmpInstId, SnmpValue> e : flagResults.entrySet()) { 
                LOG.debug("poll: SNMPwalk poll succeeded, addr={} oid={} instance={} value={}", hostAddress, prTableErrorSnmpObject, e.getKey(), e.getValue());

                if (e.getValue().toString().equals("1")) {
                    LOG.debug("PrTableMonitor.poll: found errorFlag=1");

                    SnmpObjId prTableErrorMsgSnmpObject = SnmpObjId.get(prTableErrorMsg + "." + e.getKey().toString());
                    String PrErrorMsg = SnmpUtils.get(agentConfig,prTableErrorMsgSnmpObject).toDisplayString();

                    //Stash the error in an ArrayList to then enumerate over later
                    errorStringReturn.add(PrErrorMsg);
                }
            }

            //Check the arraylist and construct return value
            if (errorStringReturn.size() > 0) {
                return PollStatus.unavailable(errorStringReturn.toString());
            }
            else {
                return status;
            }

        } catch (NumberFormatException e) {
            String reason1 = "Number operator used on a non-number " + e.getMessage();
            LOG.error(reason1, e);
            return PollStatus.unavailable(reason1);
        } catch (IllegalArgumentException e) {
            String reason1 = "Invalid SNMP Criteria: " + e.getMessage();
            LOG.error(reason1, e);
            return PollStatus.unavailable(reason1);
        } catch (Throwable t) {
            String reason1 = "Unexpected exception during SNMP poll of interface " + hostAddress;
            LOG.warn(reason1, t);
            return PollStatus.unavailable(reason1);
        }

    }

}
