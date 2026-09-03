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
package org.opennms.smoketest;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.mock.wsman.FakeWsManAgent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.utility.MountableFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Manage WS-Man REST surface against a running system: a fake WS-Man agent
 * started inside the container is configured through the API, provisioned
 * through the definition's requisition, polled up and collected from.
 */
public class WsmanConfigIT {

    private static final Logger LOG = LoggerFactory.getLogger(WsmanConfigIT.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private static final String FAKE_JAR = "/tmp/mock-wsman-agent.jar";
    private static final int FAKE_PORT = 5985;
    private static final String FAKE_USER = "monitor";
    private static final String FAKE_PASSWORD = "smoke-secret";
    // the shipped Windows system definition matches on vendor and an "OS: ..." version
    private static final String FAKE_VENDOR = FakeWsManAgent.DEFAULT_VENDOR;
    private static final String FAKE_VERSION = FakeWsManAgent.DEFAULT_VERSION;
    private static final String REQUISITION = "wsman-smoke";
    private static final String SERVER = "127.0.0.1";

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    @Test
    public void configuresPollsAndCollectsAFakeAgentEndToEnd() throws Exception {
        startFakeAgentInContainer();

        // the shipped poller configuration has no WS-Man service: the page reports not ready
        JsonNode readiness = json(get("readiness"));
        assertFalse(readiness.get("ready").asBoolean());
        assertFalse(readiness.get("pollerService").asBoolean());
        readiness = json(post("readiness/enable-polling", ""));
        assertTrue(readiness.get("ready").asBoolean());

        // a server definition naming the fake, linked to a requisition
        final JsonNode config = json(get(""));
        final ObjectNode definition = JSON.createObjectNode()
                .put("requisition", REQUISITION)
                .put("username", FAKE_USER)
                .put("password", FAKE_PASSWORD)
                .put("ssl", false)
                .put("port", FAKE_PORT)
                .put("path", "/wsman")
                .put("productVendor", FAKE_VENDOR)
                .put("productVersion", FAKE_VERSION);
        definition.putArray("specifics").add(SERVER);
        final ObjectNode update = JSON.createObjectNode().put("version", config.get("version").asText());
        update.set("defaults", config.get("defaults"));
        update.putArray("definitions").add(definition);
        final Response saved = put("", update.toString());
        assertEquals(200, saved.getStatus());
        final JsonNode savedDefinition = json(saved).get("definitions").get(0);
        assertEquals(REQUISITION, savedDefinition.get("requisition").asText());
        assertTrue(savedDefinition.get("hasPassword").asBoolean());

        // sync provisions the address as a node with the WS-Man service
        final JsonNode sync = json(post("definitions/0/sync", ""));
        assertEquals(1, sync.get("addedNodes").size());
        final RestClient rest = stack.opennms().getRestClient();
        final String nodeCriteria = REQUISITION + ":" + SERVER;
        await().atMost(3, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS)
                .until(() -> rest.getResponseForService(nodeCriteria, SERVER, "WS-Man").getStatus(), is(200));
        final OnmsMonitoredService service = rest.getService(nodeCriteria, SERVER, "WS-Man");
        assertEquals("the service is polled, not marked N", "A", service.getStatus());

        // the status column sees one server, responding, none unpolled
        await().atMost(3, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS).until(() -> {
            final JsonNode bucket = json(get("status")).get("definitions").get(0);
            LOG.info("definition status: {}", bucket);
            return bucket.get("servers").asInt() == 1 && bucket.get("responding").asInt() == 1
                    && bucket.get("unpolled").asInt() == 0 && bucket.get("provisioned").asInt() == 1;
        });

        // and the collector stores the Windows OS group from the fake's dataset
        await().atMost(6, TimeUnit.MINUTES).pollInterval(15, TimeUnit.SECONDS).until(this::freePhysicalMemoryCollected, equalTo(true));
    }

    private boolean freePhysicalMemoryCollected() {
        final QueryRequest request = new QueryRequest();
        final long now = System.currentTimeMillis();
        request.setStart(now - TimeUnit.MINUTES.toMillis(15));
        request.setEnd(now);
        request.setStep(300000);
        final Source source = new Source();
        source.setLabel("freePhysMem");
        source.setResourceId("node[" + REQUISITION + ":" + SERVER + "].nodeSnmp[]");
        source.setAttribute("freePhysMem");
        source.setAggregation("AVERAGE");
        request.setSources(List.of(source));
        try {
            final QueryResponse response = stack.opennms().getRestClient().getMeasurements(request);
            for (final QueryResponse.WrappedPrimitive column : response.getColumns()) {
                for (final double value : column.getList()) {
                    if (!Double.isNaN(value)) {
                        LOG.info("collected freePhysMem={}", value);
                        return true;
                    }
                }
            }
        } catch (final Exception e) {
            LOG.info("measurements not available yet: {}", e.getMessage());
        }
        return false;
    }

    private void startFakeAgentInContainer() throws Exception {
        final OpenNMSContainer opennms = stack.opennms();
        // the fake is a JDK-only jar from the test dependencies; run it on the container's OpenNMS JVM (etc/java.conf)
        final Path jar = Paths.get(FakeWsManAgent.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        opennms.copyFileToContainer(MountableFile.forHostPath(jar), FAKE_JAR);
        final String command = "J=$(cat /opt/opennms/etc/java.conf 2>/dev/null); "
                + "nohup \"${J:-/usr}/bin/java\" -cp " + FAKE_JAR + " " + FakeWsManAgent.class.getName()
                + " --bind 127.0.0.1 --port " + FAKE_PORT + " --user " + FAKE_USER + " --password " + FAKE_PASSWORD
                + " > /tmp/fake-wsman.log 2>&1 &";
        final ExecResult started = opennms.execInContainer("sh", "-c", command);
        assertEquals(started.getStderr(), 0, started.getExitCode());
        await().atMost(2, TimeUnit.MINUTES).pollInterval(2, TimeUnit.SECONDS).until(() -> {
            final ExecResult log = opennms.execInContainer("cat", "/tmp/fake-wsman.log");
            LOG.debug("fake agent log: {}", log.getStdout());
            return log.getStdout().contains("listening on");
        });
    }

    private Invocation.Builder request(final String path) {
        final Client client = ClientBuilder.newClient();
        final String auth = Base64.getEncoder().encodeToString((OpenNMSContainer.ADMIN_USER + ":" + OpenNMSContainer.ADMIN_PASSWORD).getBytes());
        return client.target(stack.opennms().getBaseUrlExternal() + "opennms/api/v2/wsman-config").path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Basic " + auth);
    }

    private static JsonNode json(final Response response) throws Exception {
        return JSON.readTree(response.readEntity(String.class));
    }

    private Response get(final String path) {
        final Response response = request(path).get();
        assertEquals("GET " + path, 200, response.getStatus());
        return response;
    }

    private Response post(final String path, final String body) {
        final Response response = request(path).post(Entity.entity(body, MediaType.APPLICATION_JSON));
        assertEquals("POST " + path, 200, response.getStatus());
        return response;
    }

    private Response put(final String path, final String body) {
        return request(path).put(Entity.entity(body, MediaType.APPLICATION_JSON));
    }
}
