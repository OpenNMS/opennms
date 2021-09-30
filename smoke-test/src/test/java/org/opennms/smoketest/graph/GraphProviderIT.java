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

package org.opennms.smoketest.graph;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Before;
import org.junit.Test;
import org.opennms.smoketest.OpenNMSSeleniumIT;
import org.opennms.smoketest.utils.KarafShell;
import org.rnorth.ducttape.unreliables.Unreliables;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * Verifies if exposing a GraphProvider will result in an exposed GraphContainerProvider
 */
public class GraphProviderIT extends OpenNMSSeleniumIT {

    private KarafShell karafShell = new KarafShell(stack.opennms().getSshAddress());

    @Before
    public void setUp() {
        // Configure rest
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.basePath = "/opennms/api/v2/graphs";
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
    }

    // Here we verify that the graph provider is exposed correctly
    @Test
    public void canExposeGraphProvider() {
        try {
            karafShell.runCommand("opennms:bsm-generate-hierarchies 5 2");
            karafShell.runCommand("opennms:graph-get --container bsm --namespace bsm", output -> {
                final JSONObject jsonGraph = readGraph(output);
                return jsonGraph.getString("label").equals("Business Service Graph")
                        && jsonGraph.getJSONArray("vertices").length() == 5;
            });
        } finally {
            karafShell.runCommand("opennms:bsm-delete-generated-hierarchies");
        }
    }

    @Test
    public void canImportGraphRepository() {
        karafShell.runCommand("feature:install opennms-graph-provider-persistence-test");
        karafShell.runCommand("feature:list -i", output -> output.contains("opennms-graphs") && output.contains("opennms-graph-provider-persistence-test"));
        karafShell.runCommand("opennms:graph-get --container persistence-example --namespace persistence-example.graph", output -> {
            final JSONObject jsonGraph = readGraph(output);
            return jsonGraph.getString("label").equals("Graph")
                    && jsonGraph.getString("namespace").equals("persistence-example.graph");
        });
    }

    /*
     * At some point while working on the new Graph Service API reloading the Bsmd did not correctly
     * reload the BusinessServiceGraphProvider, instead a ClassNotFoundException was raised.
     * When looking into the issue, it could not be reproduced anymore.
     * However this test is going to fail if the issue re-surfaces at some later point.
     */
    @Test
    public void canReloadBsmGraphProvider() {
        final String containerId = "bsm";
        final String namespace = containerId;

        // By default no business services should be available
        given().log().ifValidationFails()
                .accept(ContentType.JSON)
                .get("/{containerId}/{namespace}", containerId, namespace)
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("vertices", Matchers.hasSize(0))
                .body("edges", Matchers.hasSize(0));

        // Generate hierarchie
        karafShell.runCommand("opennms:bsm-generate-hierarchies 5 2");
        Unreliables.retryUntilSuccess(30, TimeUnit.SECONDS, () -> {
            given().log().ifValidationFails()
                    .accept(ContentType.JSON)
                    .get("/{containerId}/{namespace}", containerId, namespace)
                    .then().log().ifValidationFails()
                    .statusCode(200)
                    .body("vertices", Matchers.hasSize(5))
                    .body("edges", Matchers.hasSize(0));
            return null;
        });

        // Delete hierarchy and verify daemon reloaded successful
        karafShell.runCommand("opennms:bsm-delete-generated-hierarchies");
        Unreliables.retryUntilSuccess(30, TimeUnit.SECONDS, () -> {
            given().log().ifValidationFails()
                    .accept(ContentType.JSON)
                    .get("/{containerId}/{namespace}", containerId, namespace)
                    .then().log().ifValidationFails()
                    .statusCode(200)
                    .body("vertices", Matchers.hasSize(0))
                    .body("edges", Matchers.hasSize(0));
            return null;
        });
    }

    protected JSONObject readGraph(String input) {
        final int startIndex = input.indexOf("{");
        final int endIndex = input.lastIndexOf("log:display");
        String json = input.substring(startIndex, endIndex);
        json = json.substring(0, json.lastIndexOf("}") + 1);
        final JSONObject jsonObject = new JSONObject(new JSONTokener(json));
        return jsonObject;
    }
}
