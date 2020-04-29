/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.remotepollerng;

import java.net.InetAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;

import org.opennms.core.rpc.api.RpcExceptionHandler;
import org.opennms.core.rpc.api.RpcExceptionUtils;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.poller.MonitoredService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemotePollJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(RemotePollJob.class);

    protected static final String LOCATION_NAME = "locationName";
    protected static final String POLLED_SERVICE = "polledService";
    protected static final String REMOTE_POLLER_BACKEND = "remotePollerBackend";

    @Override
    public void execute(JobExecutionContext context) {
        final JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        final String locationName = Objects.requireNonNull((String) dataMap.get(LOCATION_NAME), "location name required");
        final RemotePolledService svc = Objects.requireNonNull((RemotePolledService) dataMap.get(POLLED_SERVICE), "svc required");
        final RemotePollerd backend = Objects.requireNonNull((RemotePollerd) dataMap.get(REMOTE_POLLER_BACKEND), "backend required");

        LOG.debug("Poll triggered for {} at {}", svc, locationName);

        // TODO: Use distributed tracing here to add more context around span

        // Issue the call and process the results asynchronously
        backend.getLocationAwarePollerClient().poll()
                .withService(toMonitoredService(locationName, svc.getMonitoredService()))
                .withTimeToLive(svc.getService().getInterval())
                // HACK to override location
                .withMonitor(svc.getServiceMonitor())
                .withAttributes(createParameterMap(svc.getService()))
                .execute()
                .whenComplete((res,ex) -> {
                    if (ex == null) {
                        LOG.debug("Poll for {} at {} completed successfully: {}", svc, locationName, res);
                        backend.reportResult(locationName, svc, res.getPollStatus());
                    } else {
                        RpcExceptionUtils.handleException(ex, new RpcExceptionHandler<Void>() {
                            @Override
                            public Void onInterrupted(Throwable t) {
                                LOG.warn("Interrupted.");
                                return null;
                            }

                            @Override
                            public Void onTimedOut(Throwable t) {
                                LOG.warn("RPC timed out.", t);
                                return null;
                            }

                            @Override
                            public Void onRejected(Throwable t) {
                                LOG.warn("Rejected call.", t);
                                return null;
                            }

                            @Override
                            public Void onUnknown(Throwable t) {
                                LOG.warn("Unknown exception.", t);
                                return null;
                            }
                        });
                    }
                });
    }

    private static MonitoredService toMonitoredService(String locationName, MonitoredService svc) {
        return new MonitoredService() {
            @Override
            public String getSvcName() {
                return svc.getSvcName();
            }

            @Override
            public String getIpAddr() {
                return svc.getIpAddr();
            }

            @Override
            public int getNodeId() {
                return svc.getNodeId();
            }

            @Override
            public String getNodeLabel() {
                return svc.getNodeLabel();
            }

            @Override
            public String getNodeLocation() {
                return locationName;
            }

            @Override
            public InetAddress getAddress() {
                return svc.getAddress();
            }
        };
    }

    private static Map<String,Object> createParameterMap(final Service svc) {
        final Map<String,Object> m = new ConcurrentSkipListMap<String,Object>();
        for (final Parameter p : svc.getParameters()) {
            Object val = p.getValue();
            if (val == null) {
                val = (p.getAnyObject() == null ? "" : p.getAnyObject());
            }

            m.put(p.getKey(), val);
        }
        return m;
    }
}
