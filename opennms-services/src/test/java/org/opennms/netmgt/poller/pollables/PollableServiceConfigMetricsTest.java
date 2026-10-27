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
package org.opennms.netmgt.poller.pollables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;
import org.opennms.core.rpc.api.RequestTimedOutException;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.dao.outages.api.ReadablePollOutagesDao;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.poller.LocationAwarePollerClient;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.PollerResponse;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.scheduler.Timer;
import org.opennms.netmgt.threshd.api.ThresholdingService;

import com.codahale.metrics.MetricRegistry;

public class PollableServiceConfigMetricsTest {

    /**
     * Every poll is timed, failed ones included, under both the overall
     * and the per-service timer.
     */
    @Test
    public void timesSucceededAndFailedPolls() {
        final PollerResponse response = mock(PollerResponse.class);
        when(response.getPollStatus()).thenReturn(PollStatus.up());
        final CompletableFuture<PollerResponse> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RequestTimedOutException(new Exception("Test")));

        final LocationAwarePollerClient client = mock(LocationAwarePollerClient.class, RETURNS_DEEP_STUBS);
        when(client.poll()
                .withService(any())
                .withMonitorLocator(any())
                .withTimeToLive(any())
                .withAttributes(any())
                .withAdaptor(any())
                .withAdaptor(any())
                .withAdaptor(any())
                .withAdaptor(any())
                .withPatternVariables(any())
                .execute())
                .thenReturn(CompletableFuture.completedFuture(response))
                .thenReturn(failed);

        final PollableService pollableSvc = mock(PollableService.class);
        when(pollableSvc.getSvcName()).thenReturn("ICMP");
        when(pollableSvc.getContext()).thenReturn(mock(PollContext.class));
        final Service configuredSvc = new Service();
        configuredSvc.setName("ICMP");
        final Package pkg = mock(Package.class);
        when(pkg.findService("ICMP")).thenReturn(Optional.of(new Package.ServiceMatch(pkg, configuredSvc)));
        final PollerConfig pollerConfig = mock(PollerConfig.class);
        when(pollerConfig.getServiceMonitorLocator(any())).thenReturn(Optional.of(mock(ServiceMonitorLocator.class)));

        final MetricRegistry metricRegistry = new MetricRegistry();
        final PollableServiceConfig psc = new PollableServiceConfig(pollableSvc, pollerConfig, pkg, mock(Timer.class),
                mock(PersisterFactory.class), mock(ThresholdingService.class), client,
                mock(ReadablePollOutagesDao.class), (svc, parameters, status) -> status, metricRegistry);

        assertTrue(psc.poll().isUp());
        assertTrue(psc.poll().isUnknown());

        assertEquals(2, metricRegistry.timer("polls").getCount());
        assertEquals(2, metricRegistry.timer("polls.ICMP").getCount());
        assertTrue(metricRegistry.timer("polls.ICMP").getSnapshot().getMax() > 0);
    }

    /**
     * Services matched by a pattern share the configured service's timer, so the
     * number of timers is bounded by the poller configuration.
     */
    @Test
    public void keysPatternServicesByConfiguredName() {
        final PollableService pollableSvc = mock(PollableService.class);
        when(pollableSvc.getSvcName()).thenReturn("HTTP-www.example.com");
        final Service configuredSvc = new Service();
        configuredSvc.setName("HTTP");
        final Package pkg = mock(Package.class);
        when(pkg.findService("HTTP-www.example.com")).thenReturn(Optional.of(
                new Package.ServiceMatch(pkg, configuredSvc, "HTTP-www.example.com", Collections.singletonMap("vhost", "www.example.com"))));

        final MetricRegistry metricRegistry = new MetricRegistry();
        new PollableServiceConfig(pollableSvc, mock(PollerConfig.class), pkg, mock(Timer.class),
                mock(PersisterFactory.class), mock(ThresholdingService.class), mock(LocationAwarePollerClient.class),
                mock(ReadablePollOutagesDao.class), (svc, parameters, status) -> status, metricRegistry);

        assertEquals(new TreeSet<>(Arrays.asList("polls", "polls.HTTP")), metricRegistry.getNames());
    }
}
