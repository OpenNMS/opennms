/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpoint;
import io.restassured.http.ContentType;

public class GrafanaEndpointRestIT extends AbstractRestIT {

    public GrafanaEndpointRestIT() {
        super(Version.V1, "endpoints/grafana");
    }

    @Before
    public void setUp() throws IOException, InterruptedException {
        // Delete all endpoints
        sendDelete("rest/endpoints/grafana");
    }

    @Test
    public void verifyCRUD() {
        // Ensure nothing is created
        given().get()
                .then().log().all()
                .assertThat().statusCode(204);

        // Verify CREATE
        final GrafanaEndpoint endpoint = createDummyEndpoint();
        given().contentType(ContentType.JSON).body(endpoint).post()
                .then().log().all()
                .assertThat().statusCode(202);

        // Get list to verify creation
        final GrafanaEndpoint[] endpoints = given().get()
                .then().log().all()
                .assertThat().statusCode(200)
                .extract().as(GrafanaEndpoint[].class);
        assertEquals(1, endpoints.length);

        // Verify created element
        final GrafanaEndpoint persistedEndpoint = endpoints[0];
        endpoint.setId(persistedEndpoint.getId());
        assertEquals(endpoint, persistedEndpoint);

        // Verify UPDATE
        endpoint.setApiKey("new api key");
        given().contentType(ContentType.JSON).body(endpoint).put(endpoint.getId().toString())
                .then().log().all()
                .assertThat().statusCode(202);
        final GrafanaEndpoint updatedEndpoint = given().get(endpoint.getId().toString()).then().statusCode(200).extract().as(GrafanaEndpoint.class);
        assertEquals("new api key", updatedEndpoint.getApiKey());
        assertEquals(endpoint, updatedEndpoint);

        // Verify DELETE
        given().delete(endpoint.getId().toString()).then().log().all().assertThat().statusCode(202);
        given().get().then().log().all().assertThat().statusCode(204);
    }

    @Test
    public void verifyValidation() {
        final String REQUIRE_VALUE_TEXT = "Please provide a value";
        final GrafanaEndpoint endpoint = createDummyEndpoint();

        // Ensure UID is required
        endpoint.setUid(null);
        given().contentType(ContentType.JSON).body(endpoint).post()
                .then().log().all()
                .assertThat().statusCode(400)
                    .body("context", is("uid"))
                    .body("message", is(REQUIRE_VALUE_TEXT));
        endpoint.setUid("UID"); // Reset value

        // Ensure apiKey is required
        endpoint.setApiKey(null);
        given().contentType(ContentType.JSON).body(endpoint).post()
                .then().log().all()
                .assertThat().statusCode(400)
                .body("context", is("apiKey"))
                .body("message", is(REQUIRE_VALUE_TEXT));
        endpoint.setApiKey("apiKey"); // Reset value

        // Ensure url is required
        endpoint.setUrl(null);
        given().contentType(ContentType.JSON).body(endpoint).post()
                .then().log().all()
                .assertThat().statusCode(400)
                .body("context", is("url"))
                .body("message", is(REQUIRE_VALUE_TEXT));
        endpoint.setUrl("unknown");
        given().contentType(ContentType.JSON).body(endpoint).post()
                .then().log().all()
                .assertThat().statusCode(400)
                .body("context", is("url"))
                .body("message", allOf(containsString("The provided URL"), containsString("not valid")));
    }

    @Test
    public void verifyUidIsUnique() {
        final GrafanaEndpoint endpoint = createDummyEndpoint();
        // First creation should succeed
        given().contentType(ContentType.JSON).body(endpoint).post()
                .then().log().all()
                .assertThat().statusCode(202);
        // Creating again, should fail as the uid is not unique
        given().contentType(ContentType.JSON).body(endpoint).post()
                .then().log().all()
                .assertThat().statusCode(400)
                .body("context", is("uid"))
                .body("message", is(String.format("An endpoint with uid '%s' already exists.", endpoint.getUid())));
    }

    public static GrafanaEndpoint createDummyEndpoint() {
        final GrafanaEndpoint endpoint = new GrafanaEndpoint();
        endpoint.setUid("7775ad83-4393-4803-9895-7d50dc292b4f");
        endpoint.setApiKey("dummy-key");
        endpoint.setUrl("https://grafana.com:3000");
        endpoint.setDescription("dummy description");
        endpoint.setReadTimeout(200);
        endpoint.setConnectTimeout(300);
        return endpoint;
    }
}
