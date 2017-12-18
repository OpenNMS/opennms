/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.opennms.netmgt.flows.rest.classification.ClassificationBuilder.classification;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.flows.rest.classification.ClassificationDTO;
import org.opennms.netmgt.flows.rest.classification.ClassificationRequestDTO;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class ClassificationRestIT extends OpenNMSSeleniumTestCase {

    @Before
    public void setUp() {
        RestAssured.baseURI = getBaseUrl();
        RestAssured.port = getServerHttpPort();
        RestAssured.basePath = "/opennms/rest/classifications";
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
    }

    @After
    public void tearDown() {
        given().delete();
        RestAssured.reset();
    }

    @Test
    public void verifyCRUD() {
        // Nothing there yet
        given().get().then().assertThat().statusCode(204); // no content

        // POST one rule
        final ClassificationDTO httpRule = classification().withName("http").withPort("80,8080").withProtocol("tcp,udp").build();
        String header = given().contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(httpRule)
                .post().then().assertThat().statusCode(201) // created
                .extract().header("Location");
        final String[] split = header.split("/");
        int classificationId = Integer.parseInt(split[split.length - 1]);

        // Verify Creation of 1st element
        final ClassificationDTO receivedHttpRule = given().get("" + classificationId)
                .then().log().body(true)
                .assertThat()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                .extract().response().as(ClassificationDTO.class);
        assertEquals(httpRule, receivedHttpRule);

        // Post another rule
        given().contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(classification().withName("https").withPort("443").withProtocol("tcp").build())
            .post().then().assertThat().statusCode(201); // created

        // Verify creation worked
        given().get()
                .then()
                    .log().body(true)
                .assertThat()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("", hasSize(2))
                .extract().response().asString();

        // UPDATE 1st rule
        receivedHttpRule.setName("http-opennms");
        receivedHttpRule.setPort("8980");
        receivedHttpRule.setProtocol("tcp");
        receivedHttpRule.setIpAddress("127.0.0.1");
        given().contentType(ContentType.JSON)
                .body(receivedHttpRule)
                .put(Integer.toString(classificationId))
                .then().assertThat()
                .statusCode(200);

        // Verify update worked
        final ClassificationDTO updatedRule = given().get(Integer.toString(classificationId))
                .then()
                    .log().body(true)
                .assertThat()
                    .contentType(ContentType.JSON)
                    .statusCode(200)
                .extract().response().as(ClassificationDTO.class);
        assertEquals(receivedHttpRule, updatedRule);

        // Delete 1st rule
        given().delete(Integer.toString(classificationId))
                .then().statusCode(200);

        // Verify deleted
        given().get()
                .then()
                    .log().body(true)
                .assertThat()
                    .contentType(ContentType.JSON)
                    .statusCode(200)
                    .body("", hasSize(1));

        // Delete ALL
        given().delete().then().assertThat().statusCode(200);

        // Verify deleted
        given().get()
                .then().log().body(true)
                .assertThat().statusCode(204);
    }

    @Test
    public void verifyCreateNull() {
        given().contentType(ContentType.JSON)
                .body("{}")
                .post()
                .then().assertThat()
                .statusCode(400);
    }

    @Test
    public void verifyDeleteNonExisting() {
        given().delete("10").then().statusCode(404);
    }

    @Test
    public void verifyUpdateNonExisting() {
        given().contentType(ContentType.JSON)
                .body("{}")
                .put("1000")
                .then().assertThat()
                .statusCode(404);
    }

    @Test
    public void verifyGetNonExisting() {
        given().get("10").then().statusCode(404);
    }

    @Test
    public void verifyClassify() {
        final ClassificationRequestDTO request = new ClassificationRequestDTO();
        request.setIpAddress("10.0.0.1");
        request.setPort(24005);
        request.setProtocol(6);
        final String application = given().basePath("/opennms/rest/classifications")
                .contentType(ContentType.JSON)
                .body(request)
                .post("check")
                .then()
                .assertThat()
                    .statusCode(200)
                    .extract().body().asString();
        assertThat(application, equalTo("med-ci"));

        request.setPort(50000);
        given().basePath("/opennms/rest/classifications")
                .contentType(ContentType.JSON)
                .body(request).post("check")
                .then().assertThat().statusCode(204);
    }

    @Test
    public void verifyClassifyEmpty() {
        given().basePath("/opennms/rest/classifications")
                .contentType(ContentType.JSON)
                .body("{}")
                .post("check")
                .then().assertThat()
                .statusCode(204);
    }

    @Test
    public void verifyImport() {
        // IMPORT
        final String importCsv = "service,port,protocol\nmagic-ulf-protocol,1337,tcp";
        given().contentType("text/comma-separated-values")
                .body(importCsv)
                .post()
                .then()
                    .log().body(true)
                .assertThat()
                    .statusCode(200);

        // verify rule
        given()
            .get()
                .then()
                    .log().body(true)
                .assertThat()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("", hasSize(1))
                    .body("[0].name", equalTo("magic-ulf-protocol"))
                    .body("[0].ipAddress", nullValue())
                    .body("[0].port", equalTo("1337"))
                    .body("[0].protocol", equalTo("tcp"));
    }

    @Test
    public void verifyProtocols() {
        given()
            .basePath("/opennms/rest/classifications/protocols")
            .get()
            .then()
                .log().body(true)
            .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("[6].decimal", equalTo(6))
                .body("[6].keyword", equalTo("TCP"))
                .body("[6].description", equalTo("Transmission Control"))
                .body("", hasSize(147));
    }

}
