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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.smoketest.OpenNMSSeleniumIT;
import org.opennms.smoketest.topo.GraphMLTopologyIT;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.smoketest.utils.RestClient;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class GraphRestServiceIT extends OpenNMSSeleniumIT {

    private static final String CONTAINER_ID = "test";
    private KarafShell karafShell;
    private RestClient restClient;

    @Before
    public void setUp() {
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.basePath = "/opennms/api/v2/graphs";
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);

        // Install features
        karafShell = new KarafShell(stack.opennms().getSshAddress());
        karafShell.runCommand("feature:install opennms-graphs opennms-graph-provider-graphml");

        // Set up rest client
        restClient = stack.opennms().getRestClient();

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

                    .content("[1].id", Matchers.is(CONTAINER_ID))
                    .content("[1].label", Matchers.is(GraphMLTopologyIT.LABEL))
                    .content("[1].graphs.size()", Matchers.is(2))
                    .content("[1].graphs[0].namespace", Matchers.is("acme:regions"))
                    .content("[1].graphs[1].namespace", Matchers.is("acme:markets"))
                    .content("[1].graphs[1].label", Matchers.is("Markets"))
                    .content("[1].graphs[1].description", Matchers.is("The Markets Layer"));
        });
    }

    @Test
    public void verifyGetContainer() {
        createGraphML();
        await().atMost(30, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> {
            given().get(CONTAINER_ID).then()
                    .contentType(ContentType.JSON)
                    .content("graphs", Matchers.hasSize(2))
                    .content("graphs[0].id", Matchers.is("regions"))
                    .content("graphs[0].namespace", Matchers.is("acme:regions"))
                    .content("graphs[0].vertices", Matchers.hasSize(4))
                    .content("graphs[0].edges", Matchers.hasSize(16))

                    .content("graphs[1].id", Matchers.is("markets"))
                    .content("graphs[1].namespace", Matchers.is("acme:markets"))
                    .content("graphs[1].vertices", Matchers.hasSize(16))
                    .content("graphs[1].edges", Matchers.hasSize(0));
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
