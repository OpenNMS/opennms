/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.sentinel;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.netmgt.flows.rest.classification.GroupDTO;
import org.opennms.netmgt.flows.rest.classification.RuleDTO;
import org.opennms.netmgt.flows.rest.classification.RuleDTOBuilder;
import org.opennms.plugins.elasticsearch.rest.SearchResultUtils;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.telemetry.Packets;
import org.opennms.smoketest.utils.KarafShell;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

// Verifies that the classification engine is reloaded periodically on sentinel.
// See NMS-12259 for more details
public class ClassificationEngineReloadIT {

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);

    @Rule
    public final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinion()
            .withSentinel()
            .withIpcStrategy(IpcStrategy.JMS)
            .withTelemetryProcessing()
            .build());

    @Test
    public void verifyClassificationEngineReloads() throws IOException {
        final InetSocketAddress sentinelSshAddress = stack.sentinel().getSshAddress();
        final String elasticRestUrl = stack.elastic().getRestAddressString();
        final InetSocketAddress minionFlowAddress = stack.minion().getNetworkProtocolAddress(NetworkProtocol.FLOWS);
        final InetSocketAddress opennmsWebAddress = stack.opennms().getWebAddress();

        waitForSentinelStartup(sentinelSshAddress);

        // Enable faster reloading on sentinel, default is 5 minutes.
        final KarafShell karafShell = new KarafShell(sentinelSshAddress);
        karafShell.runCommand(
                "config:edit org.opennms.features.flows.classification\n" +
                "config:property-set sentinel.cache.engine.reloadInterval 5\n" + // 5 Seconds
                "config:update");

        // Build the Elastic Rest Client
        final JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(elasticRestUrl)
                .connTimeout(5000)
                .readTimeout(10000)
                .multiThreaded(true).build());
        try (JestClient client = factory.getObject()) {
            // Verify nothing is created yet
            await().atMost(2, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS).until(() -> {
                final Search query = new Search.Builder("").build();
                final SearchResult result = client.execute(query);
                return SearchResultUtils.getTotal(result) == 0;
            });

            // Send flow
            Packets.Netflow5.setDestinationAddress(minionFlowAddress);
            Packets.Netflow5.send();

            // Verify it was classified properly
            await().atMost(2, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS).until(() -> {
                // Verify it has been created properly
                final Search query = new Search.Builder(buildApplicationQuery("ssh")).build();
                final SearchResult result = client.execute(query);
                return SearchResultUtils.getTotal(result) == 2;
            });

            // Update rule definitions
            RestAssured.baseURI = "http://" + opennmsWebAddress.getHostName();
            RestAssured.port = opennmsWebAddress.getPort();
            RestAssured.basePath = "/opennms/rest/classifications";
            RestAssured.authentication = preemptive().basic(OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD);
            final GroupDTO group = getGroup(2);
            final RuleDTO rule = new RuleDTOBuilder()
                    .withName("custom-rule")
                    .withDstPort("22")
                    .withOmnidirectional(true)
                    .withGroup(group)
                    .build();
            saveAndRetrieveRule(rule);

            // Verify that sentinel reloaded the rules
            new KarafShell(sentinelSshAddress).runCommand(
                    "classification:classify --protocol tcp --srcAddress 127.0.0.1 --srcPort 55000 --dstAddress 8.8.8.8 --destPort 22 --exporterAddress 127.0.0.1",
                    output -> output.contains("custom-rule")
            );

            // Send Flow again
            Packets.Netflow5.send();

            // Verify it was classified according the new rule
            await().atMost(2, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS).until(() -> {
                // Verify it has been created properly
                final Search query = new Search.Builder(buildApplicationQuery("custom-rule")).build();
                final SearchResult result = client.execute(query);
                return SearchResultUtils.getTotal(result) == 2;
            });
        }
    }

    private static String buildApplicationQuery(String application) {
        final String query = String.format("{\n" +
            "  \"query\": {\n" +
            "    \"match\": {\n" +
            "      \"netflow.application\": {\n" +
            "        \"query\": \"%s\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}", application);
        return query;
    }

    private static GroupDTO getGroup(int groupId) {
        return given().get("groups/" + groupId)
                .then().log().body(true)
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().as(GroupDTO.class);
    }

    private static RuleDTO saveAndRetrieveRule(RuleDTO ruleDTO) {
        String header = given().contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(ruleDTO)
                .post().then().assertThat().statusCode(201) // created
                .extract().header("Location");
        final String[] split = header.split("/");
        int classificationId = Integer.parseInt(split[split.length - 1]);

        // Verify Creation of rule
        final RuleDTO receivedHttpRule = given().get("" + classificationId)
                .then().log().body(true)
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().as(RuleDTO.class);
        assertThat(receivedHttpRule.getId(), is(classificationId));
        assertThat(receivedHttpRule.getName(), is(ruleDTO.getName()));
        assertThat(receivedHttpRule.getDstAddress(), is(ruleDTO.getDstAddress()));
        assertThat(receivedHttpRule.getProtocols(), is(ruleDTO.getProtocols()));
        assertThat(receivedHttpRule.getGroup().getName(), is(ruleDTO.getGroup().getName()));
        return receivedHttpRule;
    }

    private static void waitForSentinelStartup(InetSocketAddress sentinelSshAddress) {
        new KarafShell(sentinelSshAddress).verifyLog(shellOutput -> {
            final String sentinelReadyString = getSentinelReadyString();
            final boolean routeStarted = shellOutput.contains(sentinelReadyString);
            return routeStarted;
        });
    }

    private static String getSentinelReadyString() {
        return "Route: Sink.Server.Telemetry-Netflow-5 started and consuming from: queuingservice://OpenNMS.Sink.Telemetry-Netflow-5";
    }

}
