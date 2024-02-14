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
@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.FlakyTests.class)
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
