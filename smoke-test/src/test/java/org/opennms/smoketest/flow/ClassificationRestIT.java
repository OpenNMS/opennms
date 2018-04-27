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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.flows.classification.persistence.api.Groups;
import org.opennms.netmgt.flows.rest.classification.ClassificationRequestDTO;
import org.opennms.netmgt.flows.rest.classification.RuleDTO;
import org.opennms.netmgt.flows.rest.classification.RuleDTOBuilder;
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
        setEnabled(1, true);
        setEnabled(2, true);
    }

    @After
    public void tearDown() {
        given().param("groupId", "2").delete();
        RestAssured.reset();
    }

    @Test
    public void verifyCRUD() {
        // Verify GET Rules
        given().get().then().assertThat().statusCode(200); // 200 because "system defined" rules are enabled

        // Verify GET Groups (system-defined and user-defined rules should be there)
        given().get("/groups").then().assertThat().statusCode(200).body("", hasSize(2));

        // Disable "system-defined" rules
        setEnabled(1, false);
        given().get().then().assertThat().statusCode(204); // 204 because "system-defined" rules are disabled

        // POST (create) a rule
        final RuleDTO httpRule = builder().withName("http").withDstPort("80,8080").withProtocol("tcp,udp").build();
        String header = given().contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(httpRule)
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
        assertThat(receivedHttpRule.getName(), is(httpRule.getName()));
        assertThat(receivedHttpRule.getDstAddress(), is(httpRule.getDstAddress()));
        assertThat(receivedHttpRule.getProtocols(), is(httpRule.getProtocols()));
        assertThat(receivedHttpRule.getGroup().getName(), is(Groups.USER_DEFINED));

        // Post another rule
        given().contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(builder().withName("https").withDstPort("443").withProtocol("tcp").build())
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
        receivedHttpRule.setDstPort("8980");
        receivedHttpRule.setProtocol("tcp");
        receivedHttpRule.setDstAddress("127.0.0.1");
        given().contentType(ContentType.JSON)
                .body(receivedHttpRule)
                .log().all()
                .put(Integer.toString(classificationId))
                .then().assertThat()
                .log().all()
                .statusCode(200);

        // Verify update worked
        final RuleDTO updatedRule = given().get(Integer.toString(classificationId))
                .then()
                    .log().body(true)
                .assertThat()
                    .contentType(ContentType.JSON)
                    .statusCode(200)
                .extract().response().as(RuleDTO.class);
        assertThat(updatedRule.getId(), is(classificationId));
        assertThat(updatedRule.getName(), is(receivedHttpRule.getName()));
        assertThat(updatedRule.getDstAddress(), is(receivedHttpRule.getDstAddress()));
        assertThat(updatedRule.getProtocols(), is(receivedHttpRule.getProtocols()));
        assertThat(updatedRule.getGroup().getName(), is(Groups.USER_DEFINED));

        // Delete 1st rule
        given().delete(Integer.toString(classificationId)).then().statusCode(204);

        // Verify deleted
        given().get()
                .then()
                    .log().body(true)
                .assertThat()
                    .contentType(ContentType.JSON)
                    .statusCode(200)
                    .body("", hasSize(1));

        // DELETE group
        given().param("groupId", "2").delete()
                .then().statusCode(204);

        // Verify Group deleted
        given().get().then().statusCode(204);

        // Verify DELETE ALL is not allowed
        given().delete().then().assertThat().statusCode(400);
    }

    @Test
    public void verifyExportGroup(){

        // CSV & Name was not specified
        given().param("format", "csv").get("/groups/1").then()
                .assertThat().statusCode(200)
                .assertThat().contentType("text/comma-separated-values")
                .assertThat().header("Content-Disposition","attachment; filename=\"1_rules.csv\"" );

        // CSV & valid Name was specified
        given().param("format", "csv").param("").param("filename", "a b.csv")
                .get("/groups/1").then()
                .assertThat().statusCode(200)
                .assertThat().contentType("text/comma-separated-values")
                .assertThat().header("Content-Disposition","attachment; filename=\"a b.csv\"" );

        // CSV & invalid Name was specified
        given().param("format", "csv").param("").param("filename", "$b.csv")
                .get("/groups/1").then()
                .assertThat().statusCode(400);

        // CSV specified in header
        given().header("Accept", "text/comma-separated-values")
                .get("/groups/1").then()
                .assertThat().statusCode(200)
                .assertThat().contentType("text/comma-separated-values")
                .assertThat().header("Content-Disposition","attachment; filename=\"1_rules.csv\"" );


        // JSON
        given().param("format", "json").get("/groups/1").then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/json");

        // Default: JSON
        given().get("/groups/1").then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/json");

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
        given().delete("-1").then().statusCode(404);
    }

    @Test
    public void verifyUpdateNonExisting() {
        given().contentType(ContentType.JSON)
                .body("{}")
                .put("-1")
                .then().assertThat()
                .statusCode(404);
    }

    @Test
    public void verifyGetNonExisting() {
        given().get("-1").then().statusCode(404);
    }

    @Test
    public void verifyDisableGroup() {
        given().contentType(ContentType.JSON)
                .body("{ \"name\": \"system-defined\", \"readOnly\": true, \"enabled\": false }")
                .put("groups/1")
                .then().assertThat()
                .statusCode(200);
    }

    @Test
    public void verifyClassify() {
        final ClassificationRequestDTO request = new ClassificationRequestDTO();
        request.setSrcAddress("127.0.0.1");
        request.setSrcPort("55557");
        request.setExporterAddress("10.0.0.5");
        request.setDstAddress("10.0.0.1");
        request.setDstPort("24005");
        request.setProtocol("tcp");
        final String application = given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("classify")
                .then()
                .assertThat()
                    .statusCode(200)
                    .extract().body().asString();
        assertThat(application, equalTo("{\"classification\":\"med-ci\"}"));

        request.setDstPort("50000");
        given().contentType(ContentType.JSON)
                .body(request).post("classify")
                .then().assertThat().statusCode(204);
    }

    @Test
    public void verifyClassifyEmpty() {
        given().contentType(ContentType.JSON)
                .body("{}")
                .post("classify")
                .then().assertThat()
                .statusCode(400);
    }

    @Test
    public void verifyImport() {
        // IMPORT
        final String importCsv = "name;protocol;srcAddress;srcPort;dstAddress;dstPort;exporterFilter\nmagic-ulf;tcp;;;;1337;";
        given().contentType("text/comma-separated-values")
                .body(importCsv)
                .post()
                .then()
                .assertThat().statusCode(204);

        // verify rule
        given()
            .param("groupFilter", "2")
            .get()
                .then()
                    .log().body(true)
                .assertThat()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("", hasSize(1))
                    .body("[0].name", equalTo("magic-ulf"))
                    .body("[0].srcAddress", nullValue())
                    .body("[0].srcPort", nullValue())
                    .body("[0].dstAddress", nullValue())
                    .body("[0].dstPort", equalTo("1337"))
                    .body("[0].protocols[0]", equalTo("tcp"));
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


    // Enable/disable given group
    private void setEnabled(int groupId, boolean enabled) {
        given().contentType(ContentType.JSON)
                .body("{\"enabled\": " + enabled + "}")
                .put("/groups/" + groupId).then()
                .log().all()
                .assertThat().statusCode(200);
    }

    private static RuleDTOBuilder builder() {
        return new RuleDTOBuilder();
    }

}
