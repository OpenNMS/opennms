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
package org.opennms.core.wsman.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.ArrayList;

import javax.management.ObjectName;

import org.junit.Test;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.core.wsman.shell.CommandResult;

public class MeteredWSManClientTest {

    @Test
    public void countsRequestsFailuresAndCommands() {
        final WSManClient delegate = mock(WSManClient.class);
        when(delegate.enumerate(anyString())).thenReturn("ctx");
        when(delegate.pull(anyString(), anyString(), any(), any(Boolean.class))).thenThrow(new WSManException("boom"));
        when(delegate.runCommand(anyString(), any(), any(Duration.class))).thenReturn(new CommandResult(0, "", ""));
        final WsManMetrics metrics = WsManMetrics.INSTANCE;
        final long requests = metrics.getRequests();
        final long failures = metrics.getRequestFailures();
        final long commands = metrics.getCommands();

        final WSManClient client = new MeteredWSManClient(delegate);
        assertEquals("ctx", client.enumerate("uri"));
        try {
            client.pull("ctx", "uri", new ArrayList<>(), false);
        } catch (WSManException expected) {
            // counted below
        }
        client.runCommand("hostname", new String[0], Duration.ofSeconds(1));

        assertEquals(requests + 3, metrics.getRequests());
        assertEquals(failures + 1, metrics.getRequestFailures());
        assertEquals(commands + 1, metrics.getCommands());
    }

    @Test
    public void registersTheMBean() throws Exception {
        assertTrue(WsManMetrics.INSTANCE.getRequests() >= 0);
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName(WsManMetrics.OBJECT_NAME)));
    }
}
