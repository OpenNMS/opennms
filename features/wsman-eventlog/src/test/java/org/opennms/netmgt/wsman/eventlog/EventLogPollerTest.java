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
package org.opennms.netmgt.wsman.eventlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.rpc.mock.MockRpcClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.WSManVersion;
import org.opennms.core.wsman.cxf.CXFWSManClientFactory;
import org.opennms.mock.wsman.FakeWsManAgent;
import org.opennms.netmgt.config.wsman.eventlog.EventMapping;
import org.opennms.netmgt.config.wsman.eventlog.Log;
import org.opennms.netmgt.config.wsman.eventlog.Package;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.wsman.eventlog.rpc.EventLogWql;
import org.opennms.netmgt.wsman.eventlog.rpc.LocationAwareWsManEventLogClientRpcImpl;
import org.opennms.netmgt.wsman.eventlog.rpc.WsManEventLogRpcModule;
import org.opennms.netmgt.xml.event.Event;

/**
 * The whole core-side chain against the fake agent: request built from the cursor,
 * events forwarded, cursor advanced, cap and backoff honoured.
 */
public class EventLogPollerTest {

    private FakeWsManAgent agent;
    private WsManEventLogRpcModule module;
    private EventLogCursorStore cursors;
    private List<Event> sent;
    private WsManEventLogdMetrics metrics;
    private EventLogStatusStore statusStore;
    private EventLogPoller poller;
    private final EventLogTarget target = new EventLogTarget(42, "win-12", EventLogEventMapperTest.addr("127.0.0.1"), "Default");
    private final Package pkg = new Package();

    @Before
    public void setUp() throws Exception {
        agent = FakeWsManAgent.onLoopback("wsman", "secret").start();
        agent.withInstances(EventLogWql.CLASS_NAME, List.of(
                record("System", 10, 6008, 1, "EventLog", "20260918100000.000000+000"),
                record("System", 11, 7036, 3, "Service Control Manager", "20260918100100.000000+000"),
                record("System", 12, 1074, 3, "User32", "20260918100200.000000+000"),
                record("System", 13, 4, 2, "Kernel", "20260918100300.000000+000")));
        module = new WsManEventLogRpcModule(new CXFWSManClientFactory(), 1);
        final WSManEndpoint endpoint = new WSManEndpoint.Builder(new URL(agent.getUrl()))
                .withServerVersion(WSManVersion.WSMAN_1_0).withBasicAuth("wsman", "secret").withConnectionTimeout(5000).withReceiveTimeout(5000).build();
        final WSManConfigDao configDao = mock(WSManConfigDao.class);
        when(configDao.getEndpoint(any())).thenReturn(endpoint);
        cursors = new EventLogCursorStore(new InMemoryJsonStore());
        sent = new ArrayList<>();
        final EventForwarder forwarder = mock(EventForwarder.class);
        org.mockito.Mockito.doAnswer(i -> { sent.add(i.getArgument(0)); return null; }).when(forwarder).sendNow(any(Event.class));
        metrics = new WsManEventLogdMetrics();
        statusStore = new EventLogStatusStore(new InMemoryJsonStore());
        poller = new EventLogPoller(configDao, new LocationAwareWsManEventLogClientRpcImpl(new MockRpcClientFactory(), module), cursors,
                new EventLogEventMapper("WsManEventLogd"), forwarder, metrics, statusStore, 0);
        pkg.setName("test");
        pkg.setFilter("IPADDR != '0.0.0.0'");
        pkg.getEventMappings().add(EventLogEventMapperTest.mapping("System", null, 6008, "uei.opennms.org/wsman/eventlog/unexpectedShutdown", "Major"));
    }

    @After
    public void tearDown() {
        module.destroy();
        agent.close();
    }

    @Test
    public void firstPollReadsTheLookbackThenOnlyNewRecords() {
        final Log log = log("System", 500, "Error,Warning", null);
        poller.poll(pkg, log, target);

        // every record is within the (very long) lookback; only Error/Warning pass the level filter
        assertEquals(List.of("uei.opennms.org/wsman/eventlog/unexpectedShutdown", "uei.opennms.org/wsman/eventlog/System"),
                sent.stream().map(Event::getUei).collect(Collectors.toList()));
        assertEquals(Long.valueOf(13), cursors.get(42, "System"));
        assertEquals(2, metrics.getRecordsRead());
        assertEquals(1, metrics.getPollsCompleted());
        final EventLogReadStatus.LogStatus status = statusStore.get(42).orElseThrow().forLog("test", "System");
        assertTrue(status.lastSuccess > 0);
        assertEquals(2, status.recordsRead);
        assertEquals(Long.valueOf(13), status.cursor);
        assertNull(status.lastError);

        // nothing new: no events, cursor unchanged
        sent.clear();
        poller.poll(pkg, log, target);
        assertTrue(sent.isEmpty());
        assertEquals(Long.valueOf(13), cursors.get(42, "System"));

        // a new record after the cursor is picked up on the next poll
        agent.withInstances(EventLogWql.CLASS_NAME, List.of(record("System", 14, 41, 1, "Kernel-Power", "20260918100400.000000+000")));
        poller.poll(pkg, log, target);
        assertEquals(1, sent.size());
        assertEquals("41", EventLogEventMapperTest.parms(sent.get(0)).get("eventId"));
        assertEquals(Long.valueOf(14), cursors.get(42, "System"));
    }

    @Test
    public void capSendsTruncatedWarningAndResumesFromTheCursor() {
        final Log log = log("System", 2, null, null);
        poller.poll(pkg, log, target);

        assertEquals(List.of("uei.opennms.org/wsman/eventlog/unexpectedShutdown", "uei.opennms.org/wsman/eventlog/System", EventLogEventMapper.UEI_TRUNCATED),
                sent.stream().map(Event::getUei).collect(Collectors.toList()));
        assertEquals(Long.valueOf(11), cursors.get(42, "System"));
        assertEquals(1, metrics.getPollsTruncated());

        sent.clear();
        poller.poll(pkg, log, target);
        assertEquals(2, sent.size());
        assertEquals(Long.valueOf(13), cursors.get(42, "System"));
    }

    // a cleared log restarts its numbering below the cursor; the cursor is dropped and the lookback applies again
    @Test
    public void aClearedLogResetsTheCursor() {
        final Log log = log("System", 500, null, null);
        cursors.put(42, "System", 5000L);
        poller.poll(pkg, log, target);

        assertTrue(sent.isEmpty());
        assertNull(cursors.get(42, "System"));
        poller.poll(pkg, log, target);
        assertEquals(4, sent.size());
        assertEquals(Long.valueOf(13), cursors.get(42, "System"));
    }

    @Test
    public void includeAndExcludeIdsFilterWithoutMovingTheCursorBackwards() {
        final Log log = log("System", 500, null, "7036,1074");
        log.setExcludeEventIds("1074");
        poller.poll(pkg, log, target);

        assertEquals(List.of("7036"), sent.stream().map(e -> EventLogEventMapperTest.parms(e).get("eventId")).collect(Collectors.toList()));
        assertEquals(Long.valueOf(13), cursors.get(42, "System"));
    }

    @Test
    public void failedPollsBackOffAndRecover() {
        final Log log = log("System", 500, null, null);
        agent.withPassword("rotated");
        poller.poll(pkg, log, target);
        assertEquals(1, metrics.getPollsFailed());
        assertTrue(sent.isEmpty());
        assertNull(cursors.get(42, "System"));
        final EventLogReadStatus.LogStatus failed = statusStore.get(42).orElseThrow().forLog("test", "System");
        assertEquals(1, failed.consecutiveFailures);
        assertTrue(failed.backingOff);
        assertTrue(failed.lastError, failed.lastError.contains("Could not send Message") || failed.lastError.contains("401"));

        // the next poll is skipped while backing off, then the password is fixed and the poll succeeds
        poller.poll(pkg, log, target);
        assertEquals(1, metrics.getPollsFailed());
        assertEquals(0, metrics.getPollsCompleted());
        agent.withPassword("secret");
        poller.poll(pkg, log, target);
        assertEquals(1, metrics.getPollsCompleted());
        assertEquals(4, sent.size());
    }

    private static Log log(String name, int max, String levels, String include) {
        final Log log = new Log();
        log.setName(name);
        log.setMaxRecords(max);
        log.setLookback("3650d");
        log.setLevels(levels);
        log.setIncludeEventIds(include);
        return log;
    }

    private static Map<String, String> record(String logfile, long recordNumber, int eventCode, int eventType, String source, String time) {
        final Map<String, String> m = new LinkedHashMap<>();
        m.put("RecordNumber", Long.toString(recordNumber));
        m.put("Logfile", logfile);
        m.put("EventCode", Integer.toString(eventCode));
        m.put("EventType", Integer.toString(eventType));
        m.put("SourceName", source);
        m.put("TimeGenerated", time);
        m.put("ComputerName", "WIN-12");
        m.put("Message", source + " message");
        return m;
    }
}
