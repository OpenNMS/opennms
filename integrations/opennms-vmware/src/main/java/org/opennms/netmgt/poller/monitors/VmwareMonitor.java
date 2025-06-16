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

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.provision.service.vmware.VmwareImporter;
import org.opennms.protocols.vmware.VmwareViJavaAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostSystemPowerState;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * The Class VmwareMonitor
 * <p/>
 * This class represents a monitor for Vmware related queries
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
public class VmwareMonitor extends AbstractVmwareMonitor {
    /**
     * valid states for vSphere alarms
     */
    private static final Set<String> VALID_VSPHERE_ALARM_STATES = Sets.newHashSet("red", "yellow", "green", "gray");

    /**
     * logging for VMware monitor
     */
    private final Logger logger = LoggerFactory.getLogger(VmwareMonitor.class);

    /*
    * default retries
    */
    private static final int DEFAULT_RETRY = 0;

    /*
     * default timeout
     */
    private static final int DEFAULT_TIMEOUT = 3000;

    /**
     * This method queries the Vmware vCenter server for sensor data.
     *
     * @param svc        the monitored service
     * @param parameters the parameter map
     * @return the poll status for this system
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        final boolean ignoreStandBy = getKeyedBoolean(parameters, "ignoreStandBy", false);
        final List<String> severitiesToReport =
                Splitter.on(",")
                        .trimResults()
                        .omitEmptyStrings()
                        .splitToList(getKeyedString(parameters, "reportAlarms", ""))
                        .stream()
                        .filter(e -> VALID_VSPHERE_ALARM_STATES.contains(e))
                        .collect(Collectors.toList());
        final String vmwareManagementServer = getKeyedString(parameters, VmwareImporter.METADATA_MANAGEMENT_SERVER, null);
        final String vmwareManagedEntityType = getKeyedString(parameters, VmwareImporter.METADATA_MANAGED_ENTITY_TYPE, null);
        final String vmwareManagedObjectId = getKeyedString(parameters, VmwareImporter.METADATA_MANAGED_OBJECT_ID, null);
        final String vmwareMangementServerUsername = getKeyedString(parameters, VmwareImporter.VMWARE_MANAGEMENT_SERVER_USERNAME_KEY, null);
        final String vmwareMangementServerPassword = getKeyedString(parameters, VmwareImporter.VMWARE_MANAGEMENT_SERVER_PASSWORD_KEY, null);
        final TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

        PollStatus serviceStatus = PollStatus.unknown();

        for (tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {
            try (final VmwareViJavaAccess vmwareViJavaAccess = new VmwareViJavaAccess(vmwareManagementServer, vmwareMangementServerUsername, vmwareMangementServerPassword)) {
                vmwareViJavaAccess.connect(tracker.getConnectionTimeout());
                String powerState = "unknown";
                final Map<String, Long> alarmCountMap;
                if ("HostSystem".equals(vmwareManagedEntityType)) {
                    final HostSystem hostSystem = vmwareViJavaAccess.getHostSystemByManagedObjectId(vmwareManagedObjectId);
                    if (hostSystem == null) {
                        return PollStatus.unknown("hostSystem=null");
                    } else {
                        final HostRuntimeInfo hostRuntimeInfo = hostSystem.getRuntime();
                        if (hostRuntimeInfo == null) {
                            return PollStatus.unknown("hostRuntimeInfo=null");
                        } else {
                            final HostSystemPowerState hostSystemPowerState = hostRuntimeInfo.getPowerState();
                            if (hostSystemPowerState == null) {
                                return PollStatus.unknown("hostSystemPowerState=null");
                            } else {
                                powerState = hostSystemPowerState.toString();
                                alarmCountMap = getUnacknowledgedAlarmCountForEntity(hostSystem, severitiesToReport);
                            }
                        }
                    }
                } else {
                    if ("VirtualMachine".equals(vmwareManagedEntityType)) {
                        final VirtualMachine virtualMachine = vmwareViJavaAccess.getVirtualMachineByManagedObjectId(vmwareManagedObjectId);
                        if (virtualMachine == null) {
                            return PollStatus.unknown("virtualMachine=null");
                        } else {
                            final VirtualMachineRuntimeInfo virtualMachineRuntimeInfo = virtualMachine.getRuntime();
                            if (virtualMachineRuntimeInfo == null) {
                                return PollStatus.unknown("virtualMachineRuntimeInfo=null");
                            } else {
                                final VirtualMachinePowerState virtualMachinePowerState = virtualMachineRuntimeInfo.getPowerState();
                                if (virtualMachinePowerState == null) {
                                    return PollStatus.unknown("virtualMachinePowerState=null");
                                } else {
                                    powerState = virtualMachinePowerState.toString();
                                    alarmCountMap = getUnacknowledgedAlarmCountForEntity(virtualMachine, severitiesToReport);
                                }
                            }
                        }
                    } else {
                        logger.warn("Error getting '{}' for '{}'", vmwareManagedEntityType, vmwareManagedObjectId);
                        return serviceStatus;
                    }
                }
                final boolean anyUnacknowledgedAlarms = severitiesToReport.size() > 0 && alarmCountMap != null && alarmCountMap.size() > 0;
                final String alarmCountString = getMessageForAlarmCountMap(alarmCountMap);
                if ("poweredOn".equals(powerState)) {
                    serviceStatus = anyUnacknowledgedAlarms ? PollStatus.unavailable(alarmCountString) : PollStatus.available();
                } else {
                    if (ignoreStandBy && "standBy".equals(powerState)) {
                        serviceStatus = anyUnacknowledgedAlarms ? PollStatus.unavailable(alarmCountString) : PollStatus.available();
                    } else {
                        serviceStatus = PollStatus.unavailable("The system's state is '" + powerState + "'" + (anyUnacknowledgedAlarms ? ", " + alarmCountString : ""));
                    }
                }
            } catch (MalformedURLException | RemoteException e) {
                logger.warn("Error connecting VMware management server '{}': '{}' exception: {} cause: '{}'", vmwareManagementServer, e.getMessage(), e.getClass().getName(), e.getCause());
                return PollStatus.unavailable("Error connecting VMware management server '" + vmwareManagementServer + "'");
            }
        }
        return serviceStatus;
    }

    private Map<String, Long> getUnacknowledgedAlarmCountForEntity(final ManagedEntity managedEntity, final List<String> severitiesToReport) {
        return Arrays.stream(managedEntity.getDeclaredAlarmState()).filter(s -> !s.acknowledged && severitiesToReport.contains(s.overallStatus.name())).collect(Collectors.groupingBy(s -> s.overallStatus.name(), Collectors.counting()));
    }

    private String getMessageForAlarmCountMap(final Map<String, Long> alarmCountMap) {
        if (alarmCountMap == null || alarmCountMap.isEmpty()) {
            return "Alarms: no alarms found";
        }

        StringBuilder alarmCountString = new StringBuilder();
        for (final String alarmSeverity : VALID_VSPHERE_ALARM_STATES) {
            if (alarmCountMap.containsKey(alarmSeverity)) {
                alarmCountString.append(alarmCountString.length() > 0 ? ", " : "").append(alarmCountMap.get(alarmSeverity)).append(" ").append(alarmSeverity);
            }
        }
        return "Alarms: " + alarmCountString;
    }
}
