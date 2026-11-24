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
package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.core.mate.api.EmptyScope;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.core.wsman.shell.CommandResult;
import org.opennms.core.wsman.shell.ShellOptions;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.config.wsman.credentials.Definition;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;

import com.google.common.collect.Maps;

public class WsManShellMonitorTest {
    private static final String SC_RUNNING = "\r\nSERVICE_NAME: w32time \r\n        TYPE               : 20  WIN32_SHARE_PROCESS  \r\n"
            + "        STATE              : 4  RUNNING \r\n";
    private static final String SC_STOPPED = "\r\nSERVICE_NAME: w32time \r\n        STATE              : 1  STOPPED \r\n";

    private WSManClient client;
    private WSManClientFactory clientFactory;
    private WsManShellMonitor monitor;
    private MonitoredService svc;
    private Definition agentConfig;

    @Before
    public void setUp() throws UnknownHostException {
        agentConfig = new Definition();
        WSManConfigDao configDao = mock(WSManConfigDao.class);
        when(configDao.getAgentConfig(any())).thenReturn(agentConfig);

        client = mock(WSManClient.class);
        clientFactory = mock(WSManClientFactory.class);
        when(clientFactory.getClient(any())).thenReturn(client);

        monitor = new WsManShellMonitor();
        monitor.setWSManConfigDao(configDao);
        monitor.setWSManClientFactory(clientFactory);

        svc = mock(MonitoredService.class);
        when(svc.getAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        when(svc.getIpAddr()).thenReturn("127.0.0.1");
        when(svc.getNodeLabel()).thenReturn("win-host");
    }

    @Test
    public void isUpWhenExitCodeAndBannerMatch() {
        when(client.runCommand(eq("sc"), any(), any(), any())).thenReturn(new CommandResult(0, SC_RUNNING, ""));
        PollStatus status = poll(params("command", "sc", "args", "query w32time", "banner", "~.*STATE\\s*:\\s*4\\s+RUNNING.*"));
        assertEquals(status.getReason(), PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertTrue(status.getResponseTime() >= 0);

        ArgumentCaptor<String[]> args = ArgumentCaptor.forClass(String[].class);
        ArgumentCaptor<Duration> timeout = ArgumentCaptor.forClass(Duration.class);
        ArgumentCaptor<ShellOptions> options = ArgumentCaptor.forClass(ShellOptions.class);
        verify(client).runCommand(eq("sc"), args.capture(), timeout.capture(), options.capture());
        assertArrayEquals(new String[] {"query w32time"}, args.getValue());
        assertEquals(Duration.ofMillis(WsManShellMonitor.DEFAULT_TIMEOUT), timeout.getValue());
        assertTrue(options.getValue().isNoProfile());
        verify(client).close();
    }

    @Test
    public void isUpWithSubstringBannerAndNoArgs() {
        when(client.runCommand(eq("hostname"), any(), any(), any())).thenReturn(new CommandResult(0, "WIN-HOST\r\n", ""));
        PollStatus status = poll(params("command", "hostname", "banner", "WIN-HOST"));
        assertEquals(status.getReason(), PollStatus.SERVICE_AVAILABLE, status.getStatusCode());

        ArgumentCaptor<String[]> args = ArgumentCaptor.forClass(String[].class);
        verify(client).runCommand(eq("hostname"), args.capture(), any(), any());
        assertEquals(0, args.getValue().length);
    }

    @Test
    public void isDownWhenBannerDoesNotMatch() {
        when(client.runCommand(eq("sc"), any(), any(), any())).thenReturn(new CommandResult(0, SC_STOPPED, ""));
        PollStatus status = poll(params("command", "sc", "args", "query w32time", "banner", "~.*STATE\\s*:\\s*4\\s+RUNNING.*"));
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertTrue(status.getReason(), status.getReason().contains("Banner"));
        assertTrue(status.getReason(), status.getReason().contains("STOPPED"));
    }

    @Test
    public void isDownWhenConfiguredExitCodeDoesNotMatch() {
        when(client.runCommand(eq("sc"), any(), any(), any())).thenReturn(new CommandResult(1060, "", "The specified service does not exist"));
        PollStatus status = poll(params("command", "sc", "args", "query nosuchsvc", "exit-code", "0"));
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertTrue(status.getReason(), status.getReason().contains("1060"));
        assertTrue(status.getReason(), status.getReason().contains("does not exist"));
    }

    @Test
    public void ignoresExitCodeByDefault() {
        when(client.runCommand(eq("sc"), any(), any(), any())).thenReturn(new CommandResult(1060, "nope", ""));
        PollStatus status = poll(params("command", "sc", "args", "query nosuchsvc", "banner", "nope"));
        assertEquals(status.getReason(), PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
    }

    @Test
    public void isDownWhenCommandFails() {
        when(client.runCommand(eq("sc"), any(), any(), any())).thenThrow(new WSManException("WinRS Receive failed"));
        PollStatus status = poll(params("command", "sc", "args", "query w32time"));
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertTrue(status.getReason(), status.getReason().contains("WinRS Receive failed"));
    }

    @Test
    public void retriesFailedAttempts() {
        when(client.runCommand(eq("sc"), any(), any(), any()))
                .thenThrow(new WSManException("transient"))
                .thenReturn(new CommandResult(0, SC_RUNNING, ""));
        PollStatus status = poll(params("command", "sc", "args", "query w32time", "banner", "RUNNING", "retry", "1"));
        assertEquals(status.getReason(), PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        verify(client, times(2)).runCommand(eq("sc"), any(), any(), any());
    }

    @Test
    public void usesRetryFromWsmanConfigWhenServiceHasNone() {
        agentConfig.setRetry(2);
        when(client.runCommand(eq("sc"), any(), any(), any())).thenThrow(new WSManException("down"));
        PollStatus status = poll(params("command", "sc"));
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        verify(client, times(3)).runCommand(eq("sc"), any(), any(), any());
    }

    @Test
    public void serviceRetryOverridesWsmanConfig() {
        agentConfig.setRetry(2);
        when(client.runCommand(eq("sc"), any(), any(), any())).thenThrow(new WSManException("down"));
        poll(params("command", "sc", "retry", "0"));
        verify(client, times(1)).runCommand(eq("sc"), any(), any(), any());
    }

    @Test
    public void timeoutBoundsCommandAndEveryExchange() {
        when(client.runCommand(any(), any(), any(), any())).thenReturn(new CommandResult(0, "", ""));

        // Default
        poll(params("command", "dir"));
        assertTimeouts(WsManShellMonitor.DEFAULT_TIMEOUT);

        // wsman-config.xml default applies when the service has none
        agentConfig.setTimeout(7000);
        poll(params("command", "dir"));
        assertTimeouts(7000);

        // The service definition wins over wsman-config.xml
        poll(params("command", "dir", "timeout", "1500"));
        assertTimeouts(1500);
    }

    private void assertTimeouts(int expectedMillis) {
        ArgumentCaptor<WSManEndpoint> endpoint = ArgumentCaptor.forClass(WSManEndpoint.class);
        verify(clientFactory, atLeastOnce()).getClient(endpoint.capture());
        assertEquals(Integer.valueOf(expectedMillis), endpoint.getValue().getConnectionTimeout());
        assertEquals(Integer.valueOf(expectedMillis), endpoint.getValue().getReceiveTimeout());

        ArgumentCaptor<Duration> timeout = ArgumentCaptor.forClass(Duration.class);
        verify(client, atLeastOnce()).runCommand(any(), any(), timeout.capture(), any());
        assertEquals(Duration.ofMillis(expectedMillis), timeout.getValue());
    }

    @Test
    public void doesNotSubstitutePlaceholdersIntoCommandOrArgs() {
        when(client.runCommand(any(), any(), any(), any())).thenReturn(new CommandResult(0, "", ""));
        poll(params("command", "{nodeLabel}", "args", "query {nodeLabel} {ipAddr}"));

        ArgumentCaptor<String[]> args = ArgumentCaptor.forClass(String[].class);
        verify(client).runCommand(eq("{nodeLabel}"), args.capture(), any(), any());
        assertArrayEquals(new String[] {"query {nodeLabel} {ipAddr}"}, args.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonNumericCodepage() {
        poll(params("command", "dir", "codepage", "utf8"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonPositiveCodepage() {
        poll(params("command", "dir", "codepage", "0"));
    }

    @Test
    public void isDownWhenClientCannotBeCreated() {
        when(clientFactory.getClient(any())).thenThrow(new WSManException("no kerberos login"));
        PollStatus status = poll(params("command", "dir"));
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertTrue(status.getReason(), status.getReason().contains("no kerberos login"));
    }

    @Test
    public void passesTimeoutAndShellOptions() {
        when(client.runCommand(any(), any(), any(), any())).thenReturn(new CommandResult(0, "", ""));
        poll(params("command", "dir", "timeout", "2500", "no-profile", "false", "codepage", "437", "working-directory", "C:\\Temp"));

        ArgumentCaptor<Duration> timeout = ArgumentCaptor.forClass(Duration.class);
        ArgumentCaptor<ShellOptions> options = ArgumentCaptor.forClass(ShellOptions.class);
        verify(client).runCommand(eq("dir"), any(), timeout.capture(), options.capture());
        assertEquals(Duration.ofMillis(2500), timeout.getValue());
        assertEquals(false, options.getValue().isNoProfile());
        assertEquals(437, options.getValue().getCodepage());
        assertEquals("C:\\Temp", options.getValue().getWorkingDirectory());
    }

    @Test
    public void substitutesPlaceholders() {
        when(client.runCommand(eq("hostname"), any(), any(), any())).thenReturn(new CommandResult(0, "win-host\r\n", ""));
        PollStatus status = poll(params("command", "hostname", "banner", "{nodeLabel}"));
        assertEquals(status.getReason(), PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
    }

    @Test
    public void passesQuotedArgumentsFromXmlConfigVerbatim() {
        // The args value as it would be written in poller-configuration.xml, with &quot; for the double quotes
        final String xml = "<service name=\"Cluster-Health\" interval=\"300000\" user-defined=\"true\" status=\"on\">"
                + "<parameter key=\"command\" value=\"powershell\"/>"
                + "<parameter key=\"args\" value=\"-NoProfile -NonInteractive -Command &quot;if ((Get-ClusterNode -Name $env:COMPUTERNAME).State -ne 'Up') { exit 1 }&quot;\"/>"
                + "<parameter key=\"exit-code\" value=\"0\"/>"
                + "</service>";
        final Service service = JaxbUtils.unmarshal(Service.class, xml);
        final Map<String, Object> parameters = Maps.newHashMap(service.getParameterMap());

        when(client.runCommand(eq("powershell"), any(), any(), any())).thenReturn(new CommandResult(0, "", ""));
        PollStatus status = poll(parameters);
        assertEquals(status.getReason(), PollStatus.SERVICE_AVAILABLE, status.getStatusCode());

        // The XML parser restores the quotes and the monitor hands the string over untouched
        ArgumentCaptor<String[]> args = ArgumentCaptor.forClass(String[].class);
        verify(client).runCommand(eq("powershell"), args.capture(), any(), any());
        assertArrayEquals(new String[] {
                "-NoProfile -NonInteractive -Command \"if ((Get-ClusterNode -Name $env:COMPUTERNAME).State -ne 'Up') { exit 1 }\""},
                args.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void requiresCommand() {
        poll(params("banner", "x"));
    }

    private PollStatus poll(Map<String, Object> parameters) {
        Map<String, Object> subbedParams = Interpolator.interpolateAttributes(monitor.getRuntimeAttributes(svc, parameters), EmptyScope.EMPTY);
        // this would normally happen in the poller request builder implementation
        parameters.putAll(subbedParams);
        return monitor.poll(svc, parameters);
    }

    private static Map<String, Object> params(String... keyValues) {
        Map<String, Object> parameters = Maps.newHashMap();
        for (int i = 0; i < keyValues.length; i += 2) {
            parameters.put(keyValues[i], keyValues[i + 1]);
        }
        return parameters;
    }
}
