/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service;

import static org.opennms.core.utils.LogUtils.debugf;
import static org.opennms.core.utils.LogUtils.infof;

import java.net.InetAddress;
import java.util.Collection;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.tasks.Async;
import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.Callback;
import org.opennms.core.tasks.RunInBatch;
import org.opennms.core.tasks.Task;
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;

public class IpInterfaceScan implements RunInBatch {

    private ProvisionService m_provisionService;
    private InetAddress m_address;
    private Integer m_nodeId;
    private String m_foreignSource;

    public IpInterfaceScan(Integer nodeId, InetAddress address, String foreignSource, ProvisionService provisionService) {
        m_nodeId = nodeId;
        m_address = address;
        m_foreignSource = foreignSource;
        m_provisionService = provisionService;
    }

    public String getForeignSource() {
        return m_foreignSource;
    }

    public Integer getNodeId() {
        return m_nodeId;
    }

    public InetAddress getAddress() {
        return m_address;
    }

    public ProvisionService getProvisionService() {
        return m_provisionService;
    }

    public String toString() {
        return new ToStringBuilder(this).append("address", m_address).append("foreign source", m_foreignSource).append("node ID", m_nodeId).toString();
    }

    public Callback<Boolean> servicePersister(final BatchTask currentPhase, final String serviceName) {
        return new Callback<Boolean>() {
            public void complete(Boolean serviceDetected) {
                infof(this, "Attempted to detect service %s on address %s: %s", serviceName, getAddress().getHostAddress(), serviceDetected);
                if (serviceDetected) {

                    currentPhase.getBuilder().addSequence(
                            new RunInBatch() {

                                public void run(BatchTask batch) {

                                    if ("SNMP".equals(serviceName)) {
                                        setupAgentInfo(currentPhase);
                                    }

                                }
                            }, 
                            new RunInBatch() {

                                public void run(BatchTask batch) {
                                    getProvisionService().addMonitoredService(getNodeId(), getAddress().getHostAddress(), serviceName);
                                }
                            });


                    

                }
            }

            public void handleException(Throwable t) {
                infof(this, t, "Exception occurred while trying to detect service %s on address %s", serviceName, getAddress().getHostAddress());
            }
        };
    }

    Runnable runDetector(final SyncServiceDetector detector, final Callback<Boolean> cb) {
        return new Runnable() {
            public void run() {
                try {
                    infof(this, "Attemping to detect service %s on address %s", detector.getServiceName(), getAddress().getHostAddress());
                    cb.complete(detector.isServiceDetected(getAddress(), new NullDetectorMonitor()));
                } catch (Throwable t) {
                    cb.handleException(t);
                } finally {
                    detector.dispose();
                }
            }

            @Override
            public String toString() {
                return String.format("Run detector %s on address %s", detector.getServiceName(), getAddress().getHostAddress());
            }

        };
    }

    Async<Boolean> runDetector(AsyncServiceDetector detector) {
        return new AsyncDetectorRunner(this, detector);
    }

    Task createDetectorTask(BatchTask currentPhase, ServiceDetector detector) {
        if (detector instanceof SyncServiceDetector) {
            return createSyncDetectorTask(currentPhase, (SyncServiceDetector) detector);
        } else {
            return createAsyncDetectorTask(currentPhase, (AsyncServiceDetector) detector);
        }
    }

    private Task createAsyncDetectorTask(BatchTask currentPhase, AsyncServiceDetector asyncDetector) {
        return currentPhase.getCoordinator().createTask(currentPhase, runDetector(asyncDetector), servicePersister(currentPhase, asyncDetector.getServiceName()));
    }

    private Task createSyncDetectorTask(BatchTask currentPhase, SyncServiceDetector syncDetector) {
        return currentPhase.getCoordinator().createTask(currentPhase, runDetector(syncDetector, servicePersister(currentPhase, syncDetector.getServiceName())));
    }

    public void run(BatchTask currentPhase) {

        Collection<ServiceDetector> detectors = getProvisionService().getDetectorsForForeignSource(getForeignSource() == null ? "default" : getForeignSource());

        debugf(this, "detectServices for %d : %s: found %d detectors", getNodeId(), getAddress().getHostAddress(), detectors.size());

        for (ServiceDetector detector : detectors) {
            currentPhase.add(createDetectorTask(currentPhase, detector));
        }

    }

    private void setupAgentInfo(BatchTask currentphase) {
        getProvisionService().setIsPrimaryFlag(getNodeId(), getAddress().getHostAddress());
    }

}