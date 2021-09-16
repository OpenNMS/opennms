/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.flow;

import static com.jayway.awaitility.Awaitility.await;
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
import org.opennms.netmgt.model.resource.ResourceDTO;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.telemetry.Packet;
import org.opennms.smoketest.telemetry.Packets;
import org.opennms.smoketest.telemetry.Sender;
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

        await().atMost(1, MINUTES).pollDelay(0, SECONDS).pollInterval(5, SECONDS)
                .until(() -> {
                    sendBmpTelemetryMessage(bgpTelemetryAddress);
                    return matchRrdFileFromNodeResource("SmokeTests:TestNode");
                });
    }
}
