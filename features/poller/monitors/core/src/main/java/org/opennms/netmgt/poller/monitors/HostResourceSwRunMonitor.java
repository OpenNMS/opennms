/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.poller.monitors;


import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.StringUtils;
import org.opennms.core.utils.TimeoutTracker;
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
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * status of services reported in the Host Resources SW Run Table. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 * <p>
 * This does SNMP and therefore relies on the SNMP configuration so it is not distributable.
 * </p>
 *
 * Feature Added (2013-01-23):
 *
 * <p>This class uses TableTracker to request multiple columns at once and avoid possible problems,
 * if the table is re-indexed at the moment is being collected, which is a problem with volatile
 * services, or service with several forks like crond.</p>
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:agalue@opennms.org">Alejandro Galue </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class HostResourceSwRunMonitor extends SnmpMonitorStrategy {
    
    private static final Logger LOG = LoggerFactory.getLogger(HostResourceSwRunMonitor.class);

    /**
     * Default OID for the table that represents the name of the software running.
     */
    private static final String HOSTRESOURCE_SW_NAME_OID = ".1.3.6.1.2.1.25.4.2.1.2";

    /**
     * Default OID for the table that represents the status of the software running.
     */
    private static final String HOSTRESOURCE_SW_STATUS_OID = ".1.3.6.1.2.1.25.4.2.1.7";

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
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        InetAddress ipaddr =  svc.getAddress();

        // Retrieve this interface's SNMP peer object
        //
        final SnmpAgentConfig agentConfig = getAgentConfig(svc, parameters);
        final String hostAddress = InetAddressUtils.str(ipaddr);
		LOG.debug("poll: setting SNMP peer attribute for interface {}", hostAddress);

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
        // If "match-all" is true, there can be an optional "min-services" and "max-services" parameters that can define a range. The service is up if:
        // a) services_count >= min-services and services_count <= max-services
        // b) either one is not defined, then only one has to pass.
        // c) neither are defined, the monitor acts just like it used to - checking all instances to see if they are all running.
        // It is assumed that all services would have to pass the minimum run state test, no matter what the count.
        int minServices = ParameterMap.getKeyedInteger(parameters, "min-services", -1);
        int maxServices = ParameterMap.getKeyedInteger(parameters, "max-services", -1);

        // set timeout and retries on SNMP peer object
        //
        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters, "retry", ParameterMap.getKeyedInteger(parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters, "port", agentConfig.getPort()));

        LOG.debug("poll: service= SNMP address= {}", agentConfig);
        PollStatus status = PollStatus.unavailable("HostResourceSwRunMonitor service not found, addr=" + hostAddress + ", service-name=" + serviceName);

        // Establish SNMP session with interface
        //
        int matches = 0;
        try {
            LOG.debug("HostResourceSwRunMonitor.poll: SnmpAgentConfig address: {}", agentConfig);

            if (serviceName == null) {
                status.setReason("HostResourceSwRunMonitor no service-name defined, addr=" + hostAddress);
                LOG.warn("HostResourceSwRunMonitor.poll: No Service Name Defined! ");
		return status;
            }

            if (minServices > 0 && maxServices > 0 && minServices >= maxServices) {
                String reason = "min-services(" + minServices + ") should be less than max-services(" + maxServices + ")";
                status.setReason("HostResourceSwRunMonitor " + reason + ", addr=" + hostAddress+ ", service-name=" + serviceName);
                LOG.warn("HostResourceSwRunMonitor.poll: {}.", reason);
                return status;
            }

            // This updates two maps: one of instance and service name, and one of instance and status.
            final SnmpObjId serviceNameOidId = SnmpObjId.get(serviceNameOid);
            final SnmpObjId serviceStatusOidId = SnmpObjId.get(serviceStatusOid);
            final Map<SnmpInstId, SnmpValue> nameResults = new HashMap<SnmpInstId, SnmpValue>();
            final Map<SnmpInstId, SnmpValue> statusResults = new HashMap<SnmpInstId, SnmpValue>();
            RowCallback callback = new RowCallback() {
                @Override
                public void rowCompleted(SnmpRowResult result) {
                    nameResults.put(result.getInstance(), result.getValue(serviceNameOidId));
                    statusResults.put(result.getInstance(), result.getValue(serviceStatusOidId));
                }
            };
            TimeoutTracker tracker = new TimeoutTracker(parameters, agentConfig.getRetries(), agentConfig.getTimeout());
            tracker.reset();
            tracker.startAttempt();

            TableTracker tableTracker = new TableTracker(callback, serviceNameOidId, serviceStatusOidId);
            try(SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "HostResourceSwRunMonitor", tableTracker)) {
                walker.start();
                walker.waitFor();

                String error = walker.getErrorMessage();
                if (error != null && !error.trim().equals("")) {
                    LOG.warn(error);
                    return PollStatus.unavailable(error);
                }
            }

            // Iterate over the list of running services
            for(SnmpInstId nameInstance : nameResults.keySet()) {
                final SnmpValue name = nameResults.get(nameInstance);
                final SnmpValue value = statusResults.get(nameInstance);
                // See if the service name is in the list of running services
                if (name != null && value != null && match(serviceName, StringUtils.stripExtraQuotes(name.toString()))) {
                    matches++;
                    LOG.debug("poll: HostResourceSwRunMonitor poll succeeded, addr={}, service-name={}, value={}", hostAddress, serviceName, nameResults.get(nameInstance));
                    // Using the instance of the service, get its status and see if it meets the criteria
                    if (meetsCriteria(value, "<=", runLevel)) {
                        status = PollStatus.available(tracker.elapsedTimeInMillis());
                        // If we get here, that means the service passed the criteria, if only one match is desired we exit.
                        if ("false".equals(matchAll)) {
                           return status;
                        }
                    // if we get here, that means the meetsCriteria test failed. 
                    } else {
                        String reason = "HostResourceSwRunMonitor poll failed, addr=" + hostAddress + ", service-name=" + serviceName + ", status=" + statusResults.get(nameInstance);
                        LOG.debug(reason);
                        status = PollStatus.unavailable(reason);
                        return status;
                    }
                }
            }
            LOG.debug("poll: HostResourceSwRunMonitor the number of matches found for {} was {}", serviceName, matches);

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
        // This will be executed only if match-all=true
        boolean minOk = minServices > 0 ? matches >= minServices : true;
        boolean maxOk = maxServices > 0 ? matches <= maxServices : true;
        if (!minOk && maxServices < 0) { // failed min-services only
            String reason = "HostResourceSwRunMonitor poll failed: service-count(" + matches + ") >= min-services(" + minServices + "), addr=" + hostAddress + ", service-name=" + serviceName;
            LOG.debug(reason);
            status = PollStatus.unavailable(reason);
        }
        if (!maxOk && minServices < 0) { // failed max-services only
            String reason = "HostResourceSwRunMonitor poll failed: service-count(" + matches + ") <= max-services(" + maxServices + "), addr=" + hostAddress + ", service-name=" + serviceName;
            LOG.debug(reason);
            status = PollStatus.unavailable(reason);
        }
        if ((!minOk || !maxOk) && minServices > 0 && maxServices > 0) { // failed both (bad range)
            String reason = "HostResourceSwRunMonitor poll failed: min-services(" + minServices + ") >= service-count(" + matches + ") <= max-services(" + maxServices + "), addr=" + hostAddress + ", service-name=" + serviceName;
            LOG.debug(reason);
            status = PollStatus.unavailable(reason);
        }

        // If matchAll is set to true, then the status is set to available above with a single match.
        // Otherwise, the service will be unavailable.

        return status;
    }

    private boolean match(String expectedText, String currentText) {
        if (expectedText.startsWith("~")) {
            return currentText.matches(expectedText.replaceFirst("~", ""));
        }
        return currentText.equalsIgnoreCase(expectedText);
    }

}
