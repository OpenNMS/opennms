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

package org.opennms.netmgt.perspectivepoller;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;

import org.opennms.core.rpc.api.RpcExceptionHandler;
import org.opennms.core.rpc.api.RpcExceptionUtils;
import org.opennms.core.tracing.api.TracerConstants;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentracing.Span;
import io.opentracing.Tracer;

public class PerspectivePollJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(PerspectivePollJob.class);

    public static final String SERVICE = "service";
    public static final String BACKEND = "backend";
    public static final String TRACER = "tracer";

    @Override
    public void execute(JobExecutionContext context) {
        final JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        final PerspectivePolledService svc = Objects.requireNonNull((PerspectivePolledService) dataMap.get(SERVICE), "service required");
        final PerspectivePollerd backend = Objects.requireNonNull((PerspectivePollerd) dataMap.get(BACKEND), "backend required");
        final Tracer tracer = Objects.requireNonNull((Tracer) dataMap.get(TRACER), "tracer required");

        LOG.debug("Poll triggered for {}", svc);

        final Span span = tracer.buildSpan(PerspectivePollerd.NAME).start();
        span.setTag(TracerConstants.TAG_LOCATION, svc.getPerspectiveLocation());
        span.setTag(TracerConstants.TAG_THREAD, Thread.currentThread().getName());

        // Issue the call and process the results asynchronously
        backend.getLocationAwarePollerClient().poll()
                .withService(svc.getMonitoredService())
                .withTimeToLive(svc.getServiceConfig().getInterval())
                .withMonitor(svc.getServiceMonitor())
                .withAttributes(createParameterMap(svc.getServiceConfig()))
                .withPatternVariables(svc.getPatternVariables())
                .execute()
                .whenComplete((res, ex) -> {
                    if (ex == null) {
                        LOG.debug("Poll for {} completed successfully: {}", svc, res);

                        // Report the result to the backend
                        backend.reportResult(svc, res.getPollStatus());

                        // Persist the response times from the result
                        backend.persistResponseTimeData(svc, res.getPollStatus());

                    } else {
                        span.setTag(TracerConstants.TAG_THREAD, "true");
                        span.log(ex.getMessage());

                        RpcExceptionUtils.handleException(ex, new RpcExceptionHandler<Void>() {
                            @Override
                            public Void onInterrupted(final Throwable t) {
                                LOG.warn("Interrupted.");
                                return null;
                            }

                            @Override
                            public Void onTimedOut(final Throwable t) {
                                LOG.warn("RPC timed out.", t);
                                return null;
                            }

                            @Override
                            public Void onRejected(final Throwable t) {
                                LOG.warn("Rejected call.", t);
                                return null;
                            }

                            @Override
                            public Void onUnknown(final Throwable t) {
                                LOG.warn("Unknown exception.", t);
                                return null;
                            }
                        });
                    }

                    span.finish();
                });
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
