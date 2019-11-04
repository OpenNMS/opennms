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

import static com.jayway.awaitility.Awaitility.await;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.smoketest.OpenNMSSeleniumIT;
import org.opennms.smoketest.topo.GraphMLTopologyIT;
import org.opennms.smoketest.utils.RestClient;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class GraphRestServiceIT extends OpenNMSSeleniumIT {

    private static final String CONTAINER_ID = "test";

    private final RestClient restClient = stack.opennms().getRestClient();

    @Before
    public void setUp() {
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.basePath = "/opennms/api/v2/graphs";
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);

        // Ensure no graph exists
        deleteGraphMLIfExists();
    }

    @After
    public void tearDown() {
        RestAssured.reset();
        deleteGraphMLIfExists();
    }

    @Test
    public void verifyContainerListInfos() {
        // Explicitly ask for json
        given().accept(ContentType.JSON).then().statusCode(204);

        // Verify without content type
        given().then().statusCode(204);

        // Post a Graph
        createGraphML();

        // Verify creation if asked explicitly for JSON
        await().atMost(30, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> {
            given().accept(ContentType.JSON).get()
                    .then().statusCode(200)
                    .contentType(ContentType.JSON)
                    .content("[0].id", Matchers.is("application"))
                    .content("[0].label", Matchers.is("Application Graph"))
                    .content("[0].graphs.size()", Matchers.is(1))
                    .content("[0].graphs[0].namespace", Matchers.is("application"))
                    .content("[0].graphs[0].label", Matchers.is("Application Graph"))
                    .content("[0].graphs[0].description", Matchers.is("This Topology Provider displays all defined Applications and their calculated states."))

                    .content("[1].id", Matchers.is("bsm"))
                    .content("[1].label", Matchers.is("Business Service Graph"))
                    .content("[1].graphs.size()", Matchers.is(1))
                    .content("[1].graphs[0].namespace", Matchers.is("bsm"))
                    .content("[1].graphs[0].label", Matchers.is("Business Service Graph"))
                    .content("[1].graphs[0].description", Matchers.is("This Topology Provider displays the hierarchy of the defined Business Services and their computed operational states."))

                    .content("[2].id", Matchers.is("nodes"))
                    .content("[2].label", Matchers.is("Enhanced Linkd Topology Provider"))
                    .content("[2].graphs.size()", Matchers.is(1))
                    .content("[2].graphs[0].namespace", Matchers.is("nodes"))
                    .content("[2].graphs[0].label", Matchers.is("Enhanced Linkd Topology Provider"))
                    .content("[2].graphs[0].description", Matchers.is("This Topology Provider displays the topology information discovered by the Enhanced Linkd daemon. It uses the SNMP information of several protocols like OSPF, ISIS, LLDP and CDP to generate an overall topology."))

                    .content("[3].id", Matchers.is(CONTAINER_ID))
                    .content("[3].label", Matchers.is(GraphMLTopologyIT.LABEL))
                    .content("[3].graphs.size()", Matchers.is(2))
                    .content("[3].graphs[0].namespace", Matchers.is("acme:markets"))
                    .content("[3].graphs[0].label", Matchers.is("Markets"))
                    .content("[3].graphs[0].description", Matchers.is("The Markets Layer"))
                    .content("[3].graphs[1].namespace", Matchers.is("acme:regions"))

                    .content("[4].id", Matchers.is("vmware"))
                    .content("[4].label", Matchers.is("VMware Topology Provider"))
                    .content("[4].graphs.size()", Matchers.is(1))
                    .content("[4].graphs[0].namespace", Matchers.is("vmware"))
                    .content("[4].graphs[0].label", Matchers.is("VMware Topology Provider"))
                    .content("[4].graphs[0].description", Matchers.is("The VMware Topology Provider displays the infrastructure information gathered by the VMware Provisioning process."))
                    ;
        });
    }

    @Test
    public void verifyGetContainer() {
        createGraphMLAndWaitUntilDone();
        given().get(CONTAINER_ID).then()
                .contentType(ContentType.JSON)
                .content("graphs", Matchers.hasSize(2))
                .content("graphs[0].id", Matchers.is("markets"))
                .content("graphs[0].namespace", Matchers.is("acme:markets"))
                .content("graphs[0].defaultFocus.type", Matchers.is("SELECTION"))
                .content("graphs[0].defaultFocus.vertexIds.size()", Matchers.is(1))
                .content("graphs[0].defaultFocus.vertexIds[0].id", Matchers.is("north.4"))
                .content("graphs[0].vertices", Matchers.hasSize(16))
                .content("graphs[0].edges", Matchers.hasSize(0))

                .content("graphs[1].id", Matchers.is("regions"))
                .content("graphs[1].namespace", Matchers.is("acme:regions"))
                .content("graphs[1].defaultFocus.type", Matchers.is("ALL"))
                .content("graphs[1].defaultFocus.vertexIds.size()", Matchers.is(4))
                .content("graphs[1].vertices", Matchers.hasSize(4))
                .content("graphs[1].edges", Matchers.hasSize(16));
    }


    @Test
    public void verifyGetGraph() {
        createGraphMLAndWaitUntilDone();
        given().get(CONTAINER_ID + "/acme:markets")
                .then()
                .contentType(ContentType.JSON)
                .content("id", Matchers.is("markets"))
                .content("namespace", Matchers.is("acme:markets"))
                .content("graphs[0].defaultFocus.type", Matchers.is("ALL"))
                .content("graphs[0].vertices", Matchers.hasSize(16))
                .content("graphs[0].edges", Matchers.hasSize(0));
    }

    @Test
    public void verifySuggest() {
    	createGraphMLAndWaitUntilDone();
        given().log().ifValidationFails()
               .params("s", "unknown")
               .accept(ContentType.JSON)
               .get("/search/suggestions/{namespace}/", "acme:regions")
               .then().log().ifValidationFails()
               .statusCode(204);

        given().log().ifValidationFails()
               .params("s", "North Region")
               .accept(ContentType.JSON)
               .get("/search/suggestions/{namespace}/", "acme:regions")
               .then().log().ifValidationFails()
               .statusCode(200)
               .contentType(ContentType.JSON)
               .content("[0].context", Matchers.is("GenericVertex"))
               .content("[0].label", Matchers.is("North Region"))      
               .content("[0].provider", Matchers.is("LabelSearchProvider"))
               .content("", Matchers.hasSize(1));
    }

    @Test
    public void verifySearch() {
    	createGraphMLAndWaitUntilDone();
        given().log().ifValidationFails()
               .params("providerId", "LabelSearchProvider")
               .params("criteria", "unknown")
               .params("context", "GenericVertex")
               .accept(ContentType.JSON)
               .get("/search/results/{namespace}/", "acme:regions")
               .then().log().ifValidationFails()
               .statusCode(204);

        given().log().ifValidationFails()
               .params("providerId", "LabelSearchProvider")
               .params("criteria", "North Region")
               .params("context", "GenericVertex")
               .accept(ContentType.JSON)
               .get("/search/results/{namespace}/", "acme:regions")
               .then().log().ifValidationFails()
               .statusCode(200)
               .contentType(ContentType.JSON)
               .content("[0].properties.namespace", Matchers.is("acme:regions"))
               .content("[0].label", Matchers.is("North Region"))      
               .content("[0].id", Matchers.is("north"))
               .content("", Matchers.hasSize(1));
    }


    @Test
    public void verifyDefaultFocus() {
        if (restClient.getGraphML(CONTAINER_ID).getStatus() == 404) {
            restClient.sendGraphML(CONTAINER_ID, getClass().getResourceAsStream("/topology/graphml/test-topology.xml"));
        }
        await().atMost(30, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> {
            given().accept(ContentType.JSON).get()
                    .then().statusCode(200)
                    .contentType(ContentType.JSON)
                    .content("[0].id", Matchers.is("application"))
                    .content("[1].id", Matchers.is(CONTAINER_ID));
        });
        given().post(CONTAINER_ID + "/test")
                .then()
                .contentType(ContentType.JSON)
                .content("id", Matchers.is("test"))
                .content("namespace", Matchers.is("test"))
                .content("defaultFocus.type", Matchers.is("FIRST"))
                .content("vertices", Matchers.hasSize(1))
                .content("edges", Matchers.hasSize(0))
                .content("vertices[0].id", Matchers.is("v1"));
    }

    @Test
    public void verifySemanticZoomLevel() {
        if (restClient.getGraphML(CONTAINER_ID).getStatus() == 404) {
            restClient.sendGraphML(CONTAINER_ID, getClass().getResourceAsStream("/topology/graphml/test-topology.xml"));
        }
        await().atMost(30, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> {
            given().accept(ContentType.JSON).get()
                    .then().statusCode(200)
                    .contentType(ContentType.JSON)
                    .content("[0].id", Matchers.is("application"))
                    .content("[1].id", Matchers.is(CONTAINER_ID));
        });
        given().post(CONTAINER_ID + "/test", new JSONObject().put("szl", 1))
                .then()
                .contentType(ContentType.JSON)
                .content("id", Matchers.is("test"))
                .content("namespace", Matchers.is("test"))
                .content("vertices", Matchers.hasSize(3))
                .content("edges", Matchers.hasSize(2))
                .content("vertices[0].id", Matchers.is("v1"))
                .content("vertices[1].id", Matchers.is("v1.1"))
                .content("vertices[2].id", Matchers.is("v1.2"));
    }

    @Test
    public void verifyCustomFocus() {
        if (restClient.getGraphML(CONTAINER_ID).getStatus() == 404) {
            restClient.sendGraphML(CONTAINER_ID, getClass().getResourceAsStream("/topology/graphml/test-topology.xml"));
        }
        await().atMost(30, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> {
            given().accept(ContentType.JSON).get()
                    .then().statusCode(200)
                    .contentType(ContentType.JSON)
                    .content("[0].id", Matchers.is("application"))
                    .content("[1].id", Matchers.is(CONTAINER_ID));
        });
        given().post(CONTAINER_ID + "/test", new JSONObject().put("szl", 1).put("verticesInFocus", new JSONArray().put("v1.1.1")))
                .then()
                .contentType(ContentType.JSON)
                .content("id", Matchers.is("test"))
                .content("namespace", Matchers.is("test"))
                .content("vertices", Matchers.hasSize(3))
                .content("edges", Matchers.hasSize(2))
                .content("vertices[0].id", Matchers.is("v1.1"))
                .content("vertices[1].id", Matchers.is("v1.1.1"))
                .content("vertices[2].id", Matchers.is("v1.1.2"));
    }

    private void createGraphMLAndWaitUntilDone() {
    	createGraphML();
    	await().atMost(30, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> {
            given().accept(ContentType.JSON).get()
                    .then().statusCode(200)
                    .contentType(ContentType.JSON)
                    .content("[0].id", Matchers.is("application"))
                    .content("[1].id", Matchers.is("bsm"))
                    .content("[2].id", Matchers.is("nodes"))
                    .content("[3].id", Matchers.is(CONTAINER_ID))
                    .content("[4].id", Matchers.is("vmware"));
        });
    }

    private void createGraphML() {
        if (restClient.getGraphML(CONTAINER_ID).getStatus() == 404) {
            restClient.sendGraphML(CONTAINER_ID, getClass().getResourceAsStream("/topology/graphml/test-topology.xml"));
        }
    }

    private void deleteGraphMLIfExists() {
        if (restClient.getGraphML(CONTAINER_ID).getStatus() != 404) {
            restClient.deleteGraphML(CONTAINER_ID);
        }
    }

}
