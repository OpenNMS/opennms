/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.deviceconfig.monitors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.features.deviceconfig.retrieval.api.Retriever;
import org.opennms.netmgt.poller.DeviceConfig;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;

import io.vavr.control.Either;

public class DeviceConfigMonitorTest {

    private static final MonitoredService svc = Mockito.mock(MonitoredService.class);
    private static final Map<String, Object> params = new HashMap<>() {{
        put(DeviceConfigMonitor.SCRIPT, "");
        put(DeviceConfigMonitor.USERNAME, "");
        put(DeviceConfigMonitor.PASSWORD, "");
        put(DeviceConfigMonitor.TIMEOUT, 1000);
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

        when(retriever.retrieveConfig(any(), any(), any(), any(), any(), anyInt(), any(), any(), any())).thenReturn(
                CompletableFuture.completedFuture(Either.right(new Retriever.Success(config, filename)))
        );

        var pollStatus = deviceConfigMonitor.poll(svc, params);

        assertThat(pollStatus.getStatusCode(), is(PollStatus.SERVICE_AVAILABLE));
        assertThat(pollStatus.getDeviceConfig().content, is(config));
        assertThat(pollStatus.getDeviceConfig().filename, is(filename));
    }

    @Test
    public void shouldReturnPollStatusUnavailableOnFailure() {

        var retriever = mock(Retriever.class);
        var deviceConfigMonitor = new DeviceConfigMonitor();
        deviceConfigMonitor.setRetriever(retriever);

        var retrievalFailure = "retrieval failure";

        when(retriever.retrieveConfig(any(), any(), any(), any(), any(), anyInt(), any(), any(), any())).thenReturn(
                CompletableFuture.completedFuture(Either.left(new Retriever.Failure(retrievalFailure)))
        );

        var pollStatus = deviceConfigMonitor.poll(svc, params);

        assertThat(pollStatus.getStatusCode(), is(PollStatus.SERVICE_UNAVAILABLE));
        assertThat(pollStatus.getReason(), containsString(retrievalFailure));
    }

    @Test
    public void testLastRetrieval() {
        final Instant schedule = Instant.now().minus(5, ChronoUnit.MINUTES);
        int minute = schedule.atZone(TimeZone.getDefault().toZoneId()).getMinute();
        int hour = schedule.atZone(TimeZone.getDefault().toZoneId()).getHour();

        final Map<String, Object> params = new HashMap<>(this.params);
        params.put(DeviceConfigMonitor.SCHEDULE, "0 " + minute + " " + hour + " * * ?");

        params.put(DeviceConfigMonitor.LAST_RETRIEVAL, String.valueOf(Instant.now().minus(6, ChronoUnit.MINUTES).toEpochMilli()));
        assertThat(doesItRun(params), is(true));

        params.put(DeviceConfigMonitor.LAST_RETRIEVAL, String.valueOf(Instant.now().minus(3, ChronoUnit.MINUTES).toEpochMilli()));
        assertThat(doesItRun(params), is(false));
    }

    public boolean doesItRun(final Map<String, Object> params) {
        final DeviceConfigMonitor deviceConfigMonitor = new DeviceConfigMonitor();
        final Retriever retriever = mock(Retriever.class);

        deviceConfigMonitor.setRetriever(retriever);
        when(retriever.retrieveConfig(any(), any(), any(), any(), any(), anyInt(), any(), any(), any())).thenReturn(
                CompletableFuture.completedFuture(Either.left(new Retriever.Failure("didRun")))
        );

        return !deviceConfigMonitor.poll(svc, params).isUnknown();
    }

    @Test
    public void shouldReturnPollStatusUnavailableOnTimeout() {

        var retriever = mock(Retriever.class);
        var deviceConfigMonitor = new DeviceConfigMonitor();
        deviceConfigMonitor.setRetriever(retriever);

        when(retriever.retrieveConfig(any(), any(), any(), any(), any(), anyInt(), any(), any(), any())).thenReturn(
                new CompletableFuture()
        );

        var pollStatus = deviceConfigMonitor.poll(svc, params);

        assertThat(pollStatus.getStatusCode(), is(PollStatus.SERVICE_UNAVAILABLE));
    }

}
