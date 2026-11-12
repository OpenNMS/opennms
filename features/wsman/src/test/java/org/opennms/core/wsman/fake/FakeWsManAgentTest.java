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
package org.opennms.core.wsman.fake;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.wsman.Identity;
import org.opennms.mock.wsman.FakeWsManAgent;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.WSManVersion;
import org.opennms.core.wsman.cxf.CXFWSManClientFactory;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.core.wsman.utils.ResponseHandlingUtils;
import org.w3c.dom.Node;

import com.google.common.collect.ListMultimap;

/**
 * The fake speaks to the real CXF client, which is what the daemons use.
 */
public class FakeWsManAgentTest {

    private static final String WMI = "http://schemas.microsoft.com/wbem/wsman/1/wmi/root/cimv2/";

    private FakeWsManAgent agent;

    @Before
    public void start() throws Exception {
        agent = FakeWsManAgent.onLoopback("monitor", "secret").start();
    }

    @After
    public void stop() {
        agent.close();
    }

    private WSManClient client(final String username, final String password) throws Exception {
        final WSManEndpoint endpoint = new WSManEndpoint.Builder(new URL(agent.getUrl()))
                .withServerVersion(WSManVersion.WSMAN_1_0)
                .withBasicAuth(username, password)
                .withConnectionTimeout(5000)
                .withReceiveTimeout(5000)
                .build();
        return new CXFWSManClientFactory().getClient(endpoint);
    }

    @Test
    public void identifiesItself() throws Exception {
        agent.withIdentity("Dell, Inc.", "iDRAC : System Type = 13G Monolithic");
        final Identity identity = client("monitor", "secret").identify();
        assertEquals("Dell, Inc.", identity.getProductVendor());
        assertEquals("iDRAC : System Type = 13G Monolithic", identity.getProductVersion());
        assertTrue(agent.getRequestLog().get(0).endsWith("/Identify"));
    }

    @Test
    public void rejectsWrongCredentials() throws Exception {
        try {
            client("monitor", "wrong").identify();
            fail("expected the client to fail on 401");
        } catch (final WSManException e) {
            // expected
        }
    }

    @Test
    public void enumeratesAClassAndProjectsAWqlFilter() throws Exception {
        final WSManClient client = client("monitor", "secret");
        // plain enumeration of a concrete class: every instance with every property
        final List<Node> cpus = new ArrayList<>();
        client.enumerateAndPull(WMI + "Win32_PerfFormattedData_PerfOS_Processor", cpus, true);
        assertEquals(2, cpus.size());
        final ListMultimap<String, String> cpu1 = ResponseHandlingUtils.toMultiMap(cpus.get(1));
        assertEquals("1", cpu1.get("Name").get(0));
        assertEquals("17", cpu1.get("PercentProcessorTime").get(0));

        // a WQL filter on the wildcard URI resolves the class and keeps only the selected columns
        final List<Node> os = new ArrayList<>();
        client.enumerateAndPullUsingFilter(WMI + "*", "http://schemas.microsoft.com/wbem/wsman/1/WQL",
                "select FreePhysicalMemory, TotalVisibleMemorySize from Win32_OperatingSystem", os, true);
        assertEquals(1, os.size());
        final ListMultimap<String, String> values = ResponseHandlingUtils.toMultiMap(os.get(0));
        assertEquals("9123456", values.get("FreePhysicalMemory").get(0));
        assertTrue(values.get("Caption").isEmpty());
    }

    @Test
    public void pullsWhenTheEnumerationIsNotOptimized() throws Exception {
        final WSManClient client = client("monitor", "secret");
        final String context = client.enumerate(WMI + "Win32_PerfFormattedData_PerfOS_Processor");
        final List<Node> items = new ArrayList<>();
        assertNull(client.pull(context, WMI + "Win32_PerfFormattedData_PerfOS_Processor", items, true));
        assertEquals(2, items.size());
    }

    @Test
    public void getsAnInstanceBySelectors() throws Exception {
        final WSManClient client = client("monitor", "secret");
        final Node os = client.get(WMI + "Win32_OperatingSystem", Map.of());
        assertEquals("Microsoft Windows Server 2022 Standard", ResponseHandlingUtils.toMultiMap(os).get("Caption").get(0));
        final Node cpu = client.get(WMI + "Win32_PerfFormattedData_PerfOS_Processor", Map.of("Name", "1"));
        assertEquals("17", ResponseHandlingUtils.toMultiMap(cpu).get("PercentProcessorTime").get(0));
        try {
            client.get(WMI + "Win32_PerfFormattedData_PerfOS_Processor", Map.of("Name", "9"));
            fail("expected a fault for a selector matching nothing");
        } catch (final WSManException e) {
            // expected
        }
    }

    @Test
    public void metricsChangeAtRuntime() throws Exception {
        agent.set("Win32_OperatingSystem.FreePhysicalMemory=42");
        final WSManClient client = client("monitor", "secret");
        final Node os = client.get(WMI + "Win32_OperatingSystem", Map.of());
        assertEquals("42", ResponseHandlingUtils.toMultiMap(os).get("FreePhysicalMemory").get(0));

        // and through the control endpoint a test can drive from outside the JVM
        final java.net.HttpURLConnection put = (java.net.HttpURLConnection) new URL("http://127.0.0.1:" + agent.getPort() + "/__fake/metrics").openConnection();
        put.setRequestMethod("PUT");
        put.setDoOutput(true);
        put.getOutputStream().write("Win32_OperatingSystem.FreePhysicalMemory=7\nWin32_PerfFormattedData_PerfOS_Processor[1].PercentProcessorTime=99\n".getBytes());
        assertEquals(200, put.getResponseCode());
        assertEquals("7", agent.get("Win32_OperatingSystem", 0, "FreePhysicalMemory"));
        assertEquals("99", agent.get("Win32_PerfFormattedData_PerfOS_Processor", 1, "PercentProcessorTime"));
    }
}
