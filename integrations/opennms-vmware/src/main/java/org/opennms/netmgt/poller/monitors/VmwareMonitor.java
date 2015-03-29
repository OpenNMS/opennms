/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostSystemPowerState;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.VirtualMachine;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.protocols.vmware.VmwareViJavaAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * The Class VmwareMonitor
 * <p/>
 * This class represents a monitor for Vmware related queries
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
@Distributable(DistributionContext.DAEMON)
public class VmwareMonitor extends AbstractServiceMonitor {

    /**
     * logging for VMware monitor
     */
    private final Logger logger = LoggerFactory.getLogger("OpenNMS.VMware." + VmwareMonitor.class.getName());

    /**
     * the node dao object for retrieving assets
     */
    private NodeDao m_nodeDao = null;

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

        if (m_nodeDao == null) {
            m_nodeDao = BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class);

            if (m_nodeDao == null) {
                logger.error("Node dao should be a non-null value.");
                return PollStatus.unknown();
            }
        }

        boolean ignoreStandBy = getKeyedBoolean(parameters, "ignoreStandBy", false);

        OnmsNode onmsNode = m_nodeDao.get(svc.getNodeId());

        // retrieve the assets and
        String vmwareManagementServer = onmsNode.getAssetRecord().getVmwareManagementServer();
        String vmwareManagedEntityType = onmsNode.getAssetRecord().getVmwareManagedEntityType();
        String vmwareManagedObjectId = onmsNode.getForeignId();

        TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

        PollStatus serviceStatus = PollStatus.unknown();

        for (tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {

            VmwareViJavaAccess vmwareViJavaAccess = null;

            try {
                vmwareViJavaAccess = new VmwareViJavaAccess(vmwareManagementServer);
            } catch (MarshalException e) {
                logger.warn("Error initialising VMware connection to '{}': '{}'", vmwareManagementServer, e.getMessage());
                return PollStatus.unavailable("Error initialising VMware connection to '" + vmwareManagementServer + "'");
            } catch (ValidationException e) {
                logger.warn("Error initialising VMware connection to '{}': '{}'", vmwareManagementServer, e.getMessage());
                return PollStatus.unavailable("Error initialising VMware connection to '" + vmwareManagementServer + "'");
            } catch (IOException e) {
                logger.warn("Error initialising VMware connection to '{}': '{}'", vmwareManagementServer, e.getMessage());
                return PollStatus.unavailable("Error initialising VMware connection to '" + vmwareManagementServer + "'");
            }

            try {
                vmwareViJavaAccess.connect();
            } catch (MalformedURLException e) {
                logger.warn("Error connecting VMware management server '{}': '{}' exception: {} cause: '{}'", vmwareManagementServer, e.getMessage(), e.getClass().getName(), e.getCause());
                return PollStatus.unavailable("Error connecting VMware management server '" + vmwareManagementServer + "'");
            } catch (RemoteException e) {
                logger.warn("Error connecting VMware management server '{}': '{}' exception: {} cause: '{}'", vmwareManagementServer, e.getMessage(), e.getClass().getName(), e.getCause());
                return PollStatus.unavailable("Error connecting VMware management server '" + vmwareManagementServer + "'");
            }

            if (!vmwareViJavaAccess.setTimeout(tracker.getConnectionTimeout())) {
                logger.warn("Error setting connection timeout for VMware management server '{}'", vmwareManagementServer);
            }

            String powerState = "unknown";

            if ("HostSystem".equals(vmwareManagedEntityType)) {
                HostSystem hostSystem = vmwareViJavaAccess.getHostSystemByManagedObjectId(vmwareManagedObjectId);
                if (hostSystem == null) {
                    return PollStatus.unknown("hostSystem=null");
                } else {
                    HostRuntimeInfo hostRuntimeInfo = hostSystem.getRuntime();
                    if (hostRuntimeInfo == null) {
                        return PollStatus.unknown("hostRuntimeInfo=null");
                    } else {
                        HostSystemPowerState hostSystemPowerState = hostRuntimeInfo.getPowerState();
                        if (hostSystemPowerState == null) {
                            return PollStatus.unknown("hostSystemPowerState=null");
                        } else {
                            powerState = hostSystemPowerState.toString();
                        }
                    }
                }
            } else {
                if ("VirtualMachine".equals(vmwareManagedEntityType)) {
                    VirtualMachine virtualMachine = vmwareViJavaAccess.getVirtualMachineByManagedObjectId(vmwareManagedObjectId);
                    if (virtualMachine == null) {
                        return PollStatus.unknown("virtualMachine=null");
                    } else {
                        VirtualMachineRuntimeInfo virtualMachineRuntimeInfo = virtualMachine.getRuntime();
                        if (virtualMachineRuntimeInfo == null) {
                            return PollStatus.unknown("virtualMachineRuntimeInfo=null");
                        } else {
                            VirtualMachinePowerState virtualMachinePowerState = virtualMachineRuntimeInfo.getPowerState();
                            if (virtualMachinePowerState == null) {
                                return PollStatus.unknown("virtualMachinePowerState=null");
                            } else {
                                powerState = virtualMachinePowerState.toString();
                            }
                        }
                    }
                } else {
                    logger.warn("Error getting '{}' for '{}'", vmwareManagedEntityType, vmwareManagedObjectId);

                    vmwareViJavaAccess.disconnect();

                    return serviceStatus;
                }
            }

            if ("poweredOn".equals(powerState)) {
                serviceStatus = PollStatus.available();
            } else {
                if (ignoreStandBy && "standBy".equals(powerState)) {
                    serviceStatus = PollStatus.up();
                } else {
                    serviceStatus = PollStatus.unavailable("The system's state is '" + powerState + "'");
                }
            }

            vmwareViJavaAccess.disconnect();
        }

        return serviceStatus;
    }
}
