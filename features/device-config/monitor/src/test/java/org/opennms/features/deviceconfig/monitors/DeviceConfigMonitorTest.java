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
package org.opennms.features.deviceconfig.monitors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.retrieval.api.Retriever;
import org.opennms.features.deviceconfig.service.DeviceConfigConstants;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;

import io.vavr.control.Either;

public class DeviceConfigMonitorTest {

    private static final MonitoredService svc = Mockito.mock(MonitoredService.class);
    private static final Map<String, Object> params = new HashMap<>() {{
        put(DeviceConfigMonitor.SCRIPT, "");
        put(DeviceConfigMonitor.USERNAME, "");
        put(DeviceConfigMonitor.PASSWORD, "");
        put(DeviceConfigMonitor.SSH_TIMEOUT, 1000);
    }};

    @BeforeClass
    public static void beforeAll() throws Exception {
        when(svc.getIpAddr()).thenReturn("localhost");
        when(svc.getAddress()).thenReturn(InetAddress.getLocalHost());
    }

    @Test
    public void shouldReturnPollStatusAvailableOnSuccess() {

        var retriever = mock(Retriever.class);
        var deviceConfigMonitor = new DeviceConfigMonitor();
        deviceConfigMonitor.setRetriever(retriever);

        var config = new byte[] {1, 2, 3};
        var filename = "filename";

        when(retriever.retrieveConfig(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
                CompletableFuture.completedFuture(Either.right(new Retriever.Success(config, filename)))
        );

        var pollStatus = deviceConfigMonitor.poll(svc, params);

        assertThat(pollStatus.getStatusCode(), is(PollStatus.SERVICE_AVAILABLE));
        assertThat(pollStatus.getDeviceConfig().getContent(), is(config));
        assertThat(pollStatus.getDeviceConfig().getFilename(), is(filename));
    }

    @Test
    public void shouldReturnPollStatusUnavailableOnFailure() {

        var retriever = mock(Retriever.class);
        var deviceConfigMonitor = new DeviceConfigMonitor();
        deviceConfigMonitor.setRetriever(retriever);

        var retrievalFailure = "retrieval failure";

        when(retriever.retrieveConfig(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
                CompletableFuture.completedFuture(Either.left(new Retriever.Failure(retrievalFailure)))
        );

        var pollStatus = deviceConfigMonitor.poll(svc, params);

        assertThat(pollStatus.getStatusCode(), is(PollStatus.SERVICE_UNAVAILABLE));
        assertThat(pollStatus.getReason(), containsString(retrievalFailure));
    }

    @Test
    public void testTriggeredRetrieval() {
        final Instant schedule = Instant.now().minus(5, ChronoUnit.MINUTES);
        int minute = schedule.atZone(TimeZone.getDefault().toZoneId()).getMinute();
        int hour = schedule.atZone(TimeZone.getDefault().toZoneId()).getHour();

        final Map<String, Object> params = new HashMap<>(this.params);
        params.put(DeviceConfigConstants.SCHEDULE, "0 " + minute + " " + hour + " * * ?");
        params.put(DeviceConfigConstants.TRIGGERED_POLL, "true");

        params.put(DeviceConfigMonitor.LAST_RETRIEVAL, String.valueOf(Instant.now().minus(3, ChronoUnit.MINUTES).toEpochMilli()));
        assertThat(doesItRun(params), is(true));
    }

    @Test
    public void testLastRetrieval() {
        final Instant schedule = Instant.now().minus(5, ChronoUnit.MINUTES);
        int minute = schedule.atZone(TimeZone.getDefault().toZoneId()).getMinute();
        int hour = schedule.atZone(TimeZone.getDefault().toZoneId()).getHour();

        final Map<String, Object> params = new HashMap<>(this.params);
        params.put(DeviceConfigConstants.SCHEDULE, "0 " + minute + " " + hour + " * * ?");

        params.put(DeviceConfigMonitor.LAST_RETRIEVAL, String.valueOf(Instant.now().minus(6, ChronoUnit.MINUTES).toEpochMilli()));
        assertThat(doesItRun(params), is(true));

        params.put(DeviceConfigMonitor.LAST_RETRIEVAL, String.valueOf(Instant.now().minus(3, ChronoUnit.MINUTES).toEpochMilli()));
        assertThat(doesItRun(params), is(false));
    }

    @Test
    public void testNeverSchedule() {
        final Map<String, Object> params = new HashMap<>(this.params);
        params.put(DeviceConfigConstants.SCHEDULE, "never");

        params.put(DeviceConfigMonitor.LAST_RETRIEVAL, String.valueOf(Instant.now().minus(6, ChronoUnit.MINUTES).toEpochMilli()));

        final DeviceConfigMonitor deviceConfigMonitor = new DeviceConfigMonitor();
        final Retriever retriever = mock(Retriever.class);

        deviceConfigMonitor.setRetriever(retriever);
        when(retriever.retrieveConfig(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
                CompletableFuture.completedFuture(Either.left(new Retriever.Failure("didRun")))
        );

        PollStatus pollStatus = deviceConfigMonitor.poll(svc, params);

        assertThat(pollStatus.getStatusCode(), is(PollStatus.SERVICE_UNKNOWN));
        assertThat(pollStatus.getReason(), is("Not scheduled"));
    }

    @Test
    public void testTriggerNeverSchedule() {
        final Map<String, Object> params = new HashMap<>(this.params);
        params.put(DeviceConfigConstants.SCHEDULE, "never");

        params.put(DeviceConfigMonitor.LAST_RETRIEVAL, String.valueOf(Instant.now().minus(6, ChronoUnit.MINUTES).toEpochMilli()));
        params.put(DeviceConfigConstants.TRIGGERED_POLL, "true");

        final DeviceConfigMonitor deviceConfigMonitor = new DeviceConfigMonitor();
        final Retriever retriever = mock(Retriever.class);

        deviceConfigMonitor.setRetriever(retriever);
        when(retriever.retrieveConfig(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
                CompletableFuture.completedFuture(Either.left(new Retriever.Failure("didRun")))
        );

        PollStatus pollStatus = deviceConfigMonitor.poll(svc, params);

        assertThat(pollStatus.getStatusCode() != PollStatus.SERVICE_UNKNOWN, is(true));
    }

    public boolean doesItRun(final Map<String, Object> params) {
        final DeviceConfigMonitor deviceConfigMonitor = new DeviceConfigMonitor();
        final Retriever retriever = mock(Retriever.class);

        deviceConfigMonitor.setRetriever(retriever);
        when(retriever.retrieveConfig(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
                CompletableFuture.completedFuture(Either.left(new Retriever.Failure("didRun")))
        );

        return !deviceConfigMonitor.poll(svc, params).isUnknown();
    }

    @Test
    public void shouldReturnPollStatusUnavailableOnTimeout() {

        var retriever = mock(Retriever.class);
        var deviceConfigMonitor = new DeviceConfigMonitor();
        deviceConfigMonitor.setRetriever(retriever);

        when(retriever.retrieveConfig(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
                new CompletableFuture()
        );

        var pollStatus = deviceConfigMonitor.poll(svc, params);

        assertThat(pollStatus.getStatusCode(), is(PollStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    public void testParsingScriptFile() {
        DeviceConfigDao deviceConfigDao = mock(DeviceConfigDao.class);
        IpInterfaceDao ipInterfaceDao = mock(IpInterfaceDao.class);
        SessionUtils sessionUtils = mock(SessionUtils.class);
        MonitoredService monitoredService = mock(MonitoredService.class);
        OnmsIpInterface ipInterface = mock(OnmsIpInterface.class);
        OnmsNode node = mock(OnmsNode.class);
        when(sessionUtils.withReadOnlyTransaction((Supplier<?>) Mockito.any())).thenAnswer((inv) -> inv.getArgument(0, Supplier.class).get());
        when(deviceConfigDao.getLatestConfigForInterface(Mockito.any(), Mockito.anyString())).thenReturn(Optional.empty());
        when(ipInterfaceDao.findByNodeIdAndIpAddress(Mockito.anyInt(), Mockito.anyString())).thenReturn(ipInterface);
        when(ipInterface.getNode()).thenReturn(node);
        when(node.getCreateTime()).thenReturn(new Date());
        when(monitoredService.getNodeId()).thenReturn(0);
        when(monitoredService.getIpAddr()).thenReturn("::1");
        String testResourcePath = Paths.get("src","test","resources").toFile().getAbsolutePath();
        System.setProperty("opennms.home",  testResourcePath);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("script-file", "test-script.conf");

        var deviceConfigMonitor = new DeviceConfigMonitor();
        deviceConfigMonitor.setDeviceConfigDao(deviceConfigDao);
        deviceConfigMonitor.setIpInterfaceDao(ipInterfaceDao);
        deviceConfigMonitor.setSessionUtils(sessionUtils);
        Map<String, Object> params = deviceConfigMonitor.getRuntimeAttributes(monitoredService, parameters);
        assertFalse(params.isEmpty());
        String script = (String)params.get("script");
        String[] scriptArray = script.split("\\\\n|\\n");
        assertThat(scriptArray.length, is(8));
    }

}
