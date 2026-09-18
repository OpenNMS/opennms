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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.shell.CommandResult;

/** The shell mode against a stubbed client, since the fake agent has no WinRS. */
public class WsManEventLogRpcModuleShellTest {

    private WSManClient client;
    private WsManEventLogRpcModule module;

    @Before
    public void setUp() {
        client = mock(WSManClient.class);
        final WSManClientFactory factory = mock(WSManClientFactory.class);
        when(factory.getClient(any())).thenReturn(client);
        module = new WsManEventLogRpcModule(factory, 1);
    }

    @After
    public void tearDown() {
        module.destroy();
    }

    @Test
    public void readsThroughGetWinEventAndCapsTheBatch() throws Exception {
        when(client.runCommand(eq("powershell.exe"), any(), any())).thenReturn(new CommandResult(0,
                line(21, 101) + line(22, 102) + line(23, 103), ""));
        final EventLogQueryDTO query = new EventLogQueryDTO("Microsoft-Windows-TaskScheduler/Operational");
        query.setMode(EventLogQueryDTO.MODE_SHELL);
        query.setAfterRecordNumber(20L);
        query.setMaxRecords(2);

        final EventLogBatchDTO batch = module.execute(request(query)).get().getBatches().get(0);
        assertTrue(batch.isTruncated());
        assertEquals(2, batch.getRecords().size());
        assertEquals(21L, batch.getRecords().get(0).getRecordNumber());
        assertEquals(Integer.valueOf(102), batch.getRecords().get(1).getEventCode());
        assertEquals("Microsoft-Windows-TaskScheduler/Operational", batch.getRecords().get(0).getLogfile());
    }

    @Test
    public void aFullPageIsNotTruncated() throws Exception {
        when(client.runCommand(eq("powershell.exe"), any(), any())).thenReturn(new CommandResult(0, line(21, 101) + line(22, 102), ""));
        final EventLogQueryDTO query = new EventLogQueryDTO("Setup");
        query.setMode(EventLogQueryDTO.MODE_SHELL);
        query.setMaxRecords(2);
        assertFalse(module.execute(request(query)).get().getBatches().get(0).isTruncated());
    }

    @Test
    public void aFailingCommandLandsInTheBatchError() throws Exception {
        when(client.runCommand(eq("powershell.exe"), any(), any())).thenReturn(new CommandResult(1, "", "Get-WinEvent : There is not an event log on the localhost computer that matches \"Nope\".\r\nmore"));
        final EventLogQueryDTO query = new EventLogQueryDTO("Nope");
        query.setMode(EventLogQueryDTO.MODE_SHELL);
        final EventLogBatchDTO batch = module.execute(request(query)).get().getBatches().get(0);
        assertNotNull(batch.getError());
        assertTrue(batch.getError(), batch.getError().contains("exited with 1"));
        assertTrue(batch.getRecords().isEmpty());
    }

    private static String line(long record, int id) {
        return record + "\t" + id + "\t2\t0\tProvider\t20260918120000.000000+000\tWIN-12\t"
                + Base64.getEncoder().encodeToString(("message " + record).getBytes(StandardCharsets.UTF_8)) + "\r\n";
    }

    private static EventLogRequestDTO request(EventLogQueryDTO query) {
        final EventLogRequestDTO request = new EventLogRequestDTO();
        request.setLocation("Default");
        request.setRetries(0);
        request.setEndpointAttributes(Map.of("url", "http://127.0.0.1:5985/wsman", "server-version", "WSMAN_1_0", "receive-timeout", "5000"));
        request.addQuery(query);
        return request;
    }
}
