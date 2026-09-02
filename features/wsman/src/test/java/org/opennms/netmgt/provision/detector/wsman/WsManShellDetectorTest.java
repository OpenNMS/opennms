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
package org.opennms.netmgt.provision.detector.wsman;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.time.Duration;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.core.wsman.shell.CommandResult;
import org.opennms.core.wsman.shell.ShellOptions;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.DetectResults;

import com.google.common.collect.ImmutableMap;

public class WsManShellDetectorTest {
    private static final String SC_RUNNING = "SERVICE_NAME: w32time\r\n        STATE              : 4  RUNNING\r\n";

    private WSManClient client;
    private WsManShellDetectorFactory factory;
    private WSManEndpoint endpoint;
    private InetAddress address;

    @Before
    public void setUp() throws Exception {
        client = mock(WSManClient.class);
        WSManClientFactory clientFactory = mock(WSManClientFactory.class);
        when(clientFactory.getClient(any())).thenReturn(client);
        factory = new WsManShellDetectorFactory(clientFactory);
        endpoint = new WSManEndpoint.Builder("http://127.0.0.1:5985/wsman").withBasicAuth("user", "pass").build();
        address = InetAddress.getByName("127.0.0.1");
    }

    @Test
    public void detectsWhenExitCodeAndBannerMatch() {
        when(client.runCommand(eq("sc"), any(), any(), any())).thenReturn(new CommandResult(0, SC_RUNNING, ""));
        WsManShellDetector detector = factory.createDetector(ImmutableMap.of(
                "command", "sc", "args", "query w32time", "banner", "~.*STATE\\s*:\\s*4\\s+RUNNING.*"));
        assertEquals("WsManShell", detector.getServiceName());

        assertTrue(detector.isServiceDetected(address, endpoint).isServiceDetected());

        ArgumentCaptor<String[]> args = ArgumentCaptor.forClass(String[].class);
        ArgumentCaptor<Duration> timeout = ArgumentCaptor.forClass(Duration.class);
        ArgumentCaptor<ShellOptions> options = ArgumentCaptor.forClass(ShellOptions.class);
        verify(client).runCommand(eq("sc"), args.capture(), timeout.capture(), options.capture());
        assertArrayEquals(new String[] {"query w32time"}, args.getValue());
        assertEquals(Duration.ofMillis(detector.getTimeout()), timeout.getValue());
        assertEquals(2000, detector.getTimeout());
        assertTrue(options.getValue().isNoProfile());
        verify(client).close();
    }

    @Test
    public void doesNotDetectWhenBannerDoesNotMatch() {
        when(client.runCommand(eq("sc"), any(), any(), any())).thenReturn(new CommandResult(0, "STATE : 1 STOPPED", ""));
        WsManShellDetector detector = factory.createDetector(ImmutableMap.of("command", "sc", "args", "query w32time", "banner", "RUNNING"));
        assertFalse(detector.isServiceDetected(address, endpoint).isServiceDetected());
    }

    @Test
    public void checksExitCodeOnlyWhenConfigured() {
        when(client.runCommand(eq("sc"), any(), any(), any())).thenReturn(new CommandResult(1060, "", "no such service"));
        // Ignored by default
        WsManShellDetector detector = factory.createDetector(ImmutableMap.of("command", "sc", "args", "query nosuchsvc"));
        assertTrue(detector.isServiceDetected(address, endpoint).isServiceDetected());

        // Enforced when set
        detector = factory.createDetector(ImmutableMap.of("command", "sc", "args", "query nosuchsvc", "exitCode", "0"));
        assertFalse(detector.isServiceDetected(address, endpoint).isServiceDetected());
    }

    @Test
    public void doesNotDetectWhenCommandFails() {
        when(client.runCommand(any(), any(), any(), any())).thenThrow(new WSManException("boom"));
        WsManShellDetector detector = factory.createDetector(ImmutableMap.of("command", "sc"));
        assertFalse(detector.isServiceDetected(address, endpoint).isServiceDetected());
    }

    @Test
    public void bindsAllProperties() {
        when(client.runCommand(any(), any(), any(), any())).thenReturn(new CommandResult(0, "", ""));
        WsManShellDetector detector = factory.createDetector(ImmutableMap.<String, String>builder()
                .put("command", "dir")
                .put("serviceName", "TempDir")
                .put("timeout", "5000")
                .put("noProfile", "false")
                .put("codepage", "437")
                .put("workingDirectory", "C:\\Temp")
                .build());
        assertEquals("TempDir", detector.getServiceName());
        detector.isServiceDetected(address, endpoint);

        ArgumentCaptor<Duration> timeout = ArgumentCaptor.forClass(Duration.class);
        ArgumentCaptor<ShellOptions> options = ArgumentCaptor.forClass(ShellOptions.class);
        verify(client).runCommand(eq("dir"), any(), timeout.capture(), options.capture());
        assertEquals(Duration.ofMillis(5000), timeout.getValue());
        assertFalse(options.getValue().isNoProfile());
        assertEquals(437, options.getValue().getCodepage());
        assertEquals("C:\\Temp", options.getValue().getWorkingDirectory());
    }

    @Test
    public void detectsFromRequestRuntimeAttributes() throws MalformedURLException {
        when(client.runCommand(eq("hostname"), any(), any(), any())).thenReturn(new CommandResult(0, "WIN-HOST", ""));
        WsManShellDetector detector = factory.createDetector(ImmutableMap.of("command", "hostname", "banner", "WIN-HOST"));

        Map<String, String> runtimeAttributes = WsmanEndpointUtils.toMap(endpoint);
        DetectRequest request = mock(DetectRequest.class);
        when(request.getAddress()).thenReturn(address);
        when(request.getRuntimeAttributes()).thenReturn(runtimeAttributes);

        DetectResults results = detector.detect(request);
        assertTrue(results.isServiceDetected());
    }

    @Test(expected = IllegalArgumentException.class)
    public void requiresCommand() {
        factory.createDetector(ImmutableMap.of("banner", "x")).isServiceDetected(address, endpoint);
    }
}
