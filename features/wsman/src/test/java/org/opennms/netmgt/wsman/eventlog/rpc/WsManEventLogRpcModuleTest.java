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
package org.opennms.netmgt.wsman.eventlog.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.WSManVersion;
import org.opennms.core.wsman.cxf.CXFWSManClientFactory;
import org.opennms.mock.wsman.FakeWsManAgent;
import org.opennms.netmgt.provision.detector.wsman.WsmanEndpointUtils;

/**
 * Drives the module against the fake agent, which honours the WQL WHERE clause,
 * so cursor, lookback, level filter and cap are exercised as the real agent would.
 */
public class WsManEventLogRpcModuleTest {

    private FakeWsManAgent agent;
    private WsManEventLogRpcModule module;

    @Before
    public void setUp() throws Exception {
        agent = FakeWsManAgent.onLoopback("wsman", "secret").start();
        agent.withInstances(EventLogWql.CLASS_NAME, List.of(
                record("System", 10, 6008, 1, "EventLog", "20260918100000.000000+000", "The previous system shutdown was unexpected."),
                record("System", 11, 7036, 3, "Service Control Manager", "20260918100100.000000+000", "The Spooler service entered the running state."),
                record("System", 12, 1074, 3, "User32", "20260918100200.000000+000", "The process winlogon.exe has initiated the restart."),
                record("System", 13, 4, 2, "Kernel", "20260918100300.000000+000", "A warning."),
                record("Application", 5, 1000, 1, "Application Error", "20260918100000.000000+000", "Faulting application.")));
        module = new WsManEventLogRpcModule(new CXFWSManClientFactory(), 1);
    }

    @After
    public void tearDown() {
        module.destroy();
        agent.close();
    }

    @Test
    public void readsEverythingAfterTheCursorInRecordOrder() throws Exception {
        final EventLogQueryDTO query = new EventLogQueryDTO("System");
        query.setAfterRecordNumber(10L);
        final EventLogResponseDTO response = module.execute(request(query)).get();

        assertNull(response.getErrorMessage());
        assertEquals(1, response.getBatches().size());
        final EventLogBatchDTO batch = response.getBatches().get(0);
        assertNull(batch.getError());
        assertFalse(batch.isTruncated());
        assertEquals(List.of(11L, 12L, 13L), batch.getRecords().stream().map(EventLogRecordDTO::getRecordNumber).collect(Collectors.toList()));
        final EventLogRecordDTO first = batch.getRecords().get(0);
        assertEquals("System", first.getLogfile());
        assertEquals(Integer.valueOf(7036), first.getEventCode());
        assertEquals(Integer.valueOf(3), first.getEventType());
        assertEquals("Service Control Manager", first.getSourceName());
        assertEquals("20260918100100.000000+000", first.getTimeGenerated());
        assertEquals("WIN-12", first.getComputerName());
        assertEquals("The Spooler service entered the running state.", first.getMessage());
    }

    @Test
    public void firstPollUsesTheLookbackAndLevels() throws Exception {
        final EventLogQueryDTO query = new EventLogQueryDTO("System");
        query.setSinceTime("20260918100100.000000+000");
        query.setEventTypes(List.of(1, 2));
        final EventLogBatchDTO batch = module.execute(request(query)).get().getBatches().get(0);

        // 11 and 12 are informational, 10 is before the lookback
        assertEquals(List.of(13L), batch.getRecords().stream().map(EventLogRecordDTO::getRecordNumber).collect(Collectors.toList()));
    }

    @Test
    public void capsTheBatchAndFlagsTruncation() throws Exception {
        final EventLogQueryDTO query = new EventLogQueryDTO("System");
        query.setAfterRecordNumber(0L);
        query.setMaxRecords(2);
        final EventLogBatchDTO batch = module.execute(request(query)).get().getBatches().get(0);

        assertTrue(batch.isTruncated());
        assertEquals(List.of(10L, 11L), batch.getRecords().stream().map(EventLogRecordDTO::getRecordNumber).collect(Collectors.toList()));
    }

    @Test
    public void readsSeveralLogsInOneRequest() throws Exception {
        final EventLogQueryDTO system = new EventLogQueryDTO("System");
        system.setAfterRecordNumber(12L);
        final EventLogQueryDTO application = new EventLogQueryDTO("Application");
        application.setAfterRecordNumber(0L);
        final EventLogResponseDTO response = module.execute(request(system, application)).get();

        assertEquals(2, response.getBatches().size());
        assertEquals(1, response.getBatches().get(0).getRecords().size());
        assertEquals("Application", response.getBatches().get(1).getLogfile());
        assertEquals(Integer.valueOf(1000), response.getBatches().get(1).getRecords().get(0).getEventCode());
    }

    @Test
    public void reportsAFailedReadInTheBatchNotTheResponse() throws Exception {
        final EventLogRequestDTO request = request(new EventLogQueryDTO("System"));
        final Map<String, String> attributes = request.getEndpointAttributes();
        attributes.put("password", "wrong");
        request.setEndpointAttributes(attributes);
        request.setRetries(0);
        final EventLogResponseDTO response = module.execute(request).get();

        assertNull(response.getErrorMessage());
        final EventLogBatchDTO batch = response.getBatches().get(0);
        assertNotNull(batch.getError());
        assertTrue(batch.getRecords().isEmpty());
    }

    private EventLogRequestDTO request(EventLogQueryDTO... queries) throws Exception {
        final WSManEndpoint endpoint = new WSManEndpoint.Builder(new URL(agent.getUrl()))
                .withServerVersion(WSManVersion.WSMAN_1_0)
                .withBasicAuth("wsman", "secret")
                .withMaxElements(2)
                .build();
        final EventLogRequestDTO request = new EventLogRequestDTO();
        request.setLocation("Default");
        request.setRetries(0);
        request.setEndpointAttributes(WsmanEndpointUtils.toMap(endpoint));
        for (EventLogQueryDTO query : queries) {
            request.addQuery(query);
        }
        return request;
    }

    private static Map<String, String> record(String logfile, long recordNumber, int eventCode, int eventType, String source, String time, String message) {
        final Map<String, String> m = new java.util.LinkedHashMap<>();
        m.put("RecordNumber", Long.toString(recordNumber));
        m.put("Logfile", logfile);
        m.put("EventCode", Integer.toString(eventCode));
        m.put("EventType", Integer.toString(eventType));
        m.put("SourceName", source);
        m.put("TimeGenerated", time);
        m.put("ComputerName", "WIN-12");
        m.put("Message", message);
        return m;
    }
}
