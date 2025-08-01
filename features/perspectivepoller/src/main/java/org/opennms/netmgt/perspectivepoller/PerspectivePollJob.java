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
                .withMonitorLocator(svc.getServiceMonitorLocator())
                .withAttributes(svc.getServiceConfig().getParameterMap())
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
}
