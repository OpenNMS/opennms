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
package org.opennms.smoketest.flow;

import static org.awaitility.Awaitility.await;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.resource.ResourceDTO;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.telemetry.Packet;
import org.opennms.smoketest.telemetry.Packets;
import org.opennms.smoketest.telemetry.Sender;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.smoketest.utils.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.FlakyTests.class)
public class BmpIT {
    private static final Logger LOG = LoggerFactory.getLogger(BmpIT.class);

    private final static Set<String> EXPECTED_RRD_FILES = Sets.newHashSet(
            "inv_cl_loop.rrd",
            "export_rib.rrd",
            "inv_as_path_loop.rrd",
            "adj_rib_out.rrd",
            "rejected.rrd",
            "duplicate_update.rrd",
            "inv_originator_id.rrd",
            "inv_as_confed_loop.rrd",
            "duplicate_withdraw.rrd",
            "duplicate_prefix.rrd",
            "adj_rib_in.rrd",
            "local_rib.rrd",
            "prefix_withdraw.rrd",
            "update_withdraw.rrd"
    );

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withTelemetryProcessing()
            .build());

    @Before
    public void setUp() {
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.basePath = "/opennms/rest";
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
    }

    @After
    public void tearDown() {
        given().delete("SmokeTests:TestNode");

        RestAssured.reset();
    }

    private String getLocalAddress() {
        return stack.opennms()
                .getCurrentContainerInfo()
                .getNetworkSettings()
                .getNetworks()
                .entrySet()
                .stream()
                .findFirst()
                .get()
                .getValue()
                .getGateway();
    }

    private void sendBmpTelemetryMessage(final InetSocketAddress tcpAddress) {
        try {
            new Packet(Packets.BMP.getPayload()).send(Sender.tcp(tcpAddress));
        } catch (IOException e) {
            LOG.error("Exception while sending BMP packets", e);
        }
    }

    private boolean matchRrdFileFromNodeResource(final String foreignId) {
        final RestClient client = stack.opennms().getRestClient();
        final ResourceDTO resources = client.getResourcesForNode(foreignId);

        final Set<String> rrdFiles = resources.getChildren().getObjects().stream()
                .flatMap(r -> r.getRrdGraphAttributes().values().stream())
                .map(e -> e.getRrdFile())
                .collect(Collectors.toSet());

        return rrdFiles.containsAll(EXPECTED_RRD_FILES);
    }

    @Test
    public void verifyBmpProcessing() {
        final InetSocketAddress bgpTelemetryAddress = stack.opennms().getNetworkProtocolAddress(NetworkProtocol.BMP);

        final String node = "<node type=\"A\" label=\"TestNode\" foreignSource=\"SmokeTests\" foreignId=\"TestNode\">" +
                            "<labelSource>H</labelSource>" +
                            "<sysContact>Me</sysContact>" +
                            "<sysDescription>Black Ops 4</sysDescription>" +
                            "<sysLocation>German DevJam 2020</sysLocation>" +
                            "<sysName>TestNode</sysName>" +
                            "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                            "<createTime>2020-02-25T13:20:00.123-04:00</createTime>" +
                            "<lastCapsdPoll>2020-02-25T13:21:00.456-04:00</lastCapsdPoll>" +
                            "</node>";

        given().body(node)
                .basePath("/opennms/rest/nodes")
                .contentType(ContentType.XML).post()
                .then().assertThat()
                .statusCode(201);

        final String ipInterface = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\">" +
                                   "<ipAddress>" + getLocalAddress() + "</ipAddress>" +
                                   "<hostName>test-machine1.local</hostName>" +
                                   "</ipInterface>";

        given().body(ipInterface)
                .basePath("/opennms/rest/nodes/SmokeTests:TestNode/ipinterfaces")
                .contentType(ContentType.XML).post()
                .then().assertThat()
                .statusCode(201);

        // Restarting collectd after we have added a node to the db
        final EventBuilder builder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, getClass().getSimpleName());
        builder.setParam(EventConstants.PARM_DAEMON_NAME, "collectd");
        stack.opennms().getRestClient().sendEvent(builder.getEvent());
        // Added command to build the cache after adding node through API
        KarafShell karafShell = new KarafShell(stack.opennms().getSshAddress());
        karafShell.runCommand("opennms:sync-node-cache");

        await().atMost(1, MINUTES).pollDelay(0, SECONDS).pollInterval(5, SECONDS)
                .until(() -> {
                    sendBmpTelemetryMessage(bgpTelemetryAddress);
                    return matchRrdFileFromNodeResource("SmokeTests:TestNode");
                });
    }
}
