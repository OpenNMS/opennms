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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.netmgt.flows.classification.error.Errors;
import org.opennms.netmgt.flows.classification.persistence.api.Groups;
import org.opennms.netmgt.flows.rest.classification.ClassificationRequestDTO;
import org.opennms.netmgt.flows.rest.classification.GroupDTO;
import org.opennms.netmgt.flows.rest.classification.GroupDTOBuilder;
import org.opennms.netmgt.flows.rest.classification.RuleDTO;
import org.opennms.netmgt.flows.rest.classification.RuleDTOBuilder;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class ClassificationRestIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    private List<Integer> groupIsToDelete;

    private GroupDTO userDefinedGroup;

    @Before
    public void setUp() {
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.basePath = "/opennms/rest/classifications";
        RestAssured.authentication = preemptive().basic(OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD);
        setEnabled(1, true);
        setEnabled(2, true);
        userDefinedGroup = getGroup(2);
        groupIsToDelete = new ArrayList<>();
    }

    @After
    public void tearDown() {
        given().param("groupId", 2).delete(); // delete all rules in 'user-defined' group
        for(Integer groupId : groupIsToDelete) {
            given().delete("groups/" + groupId);
        }
        RestAssured.reset();
    }

    @Test
    public void verifyCRUDforRule() {
        // Verify GET Rules
        given().get().then().assertThat().statusCode(200); // 200 because "system defined" rules are enabled

        // Verify GET Groups (system-defined and user-defined rules should be there)
        given().get("/groups").then().assertThat().statusCode(200).body("", hasSize(2));

        // Disable "system-defined" rules
        setEnabled(1, false);
        given().get().then().assertThat().statusCode(204); // 204 because "system-defined" rules are disabled

        // POST (create) a rule
        final RuleDTO httpRule = builder().withName("http").withGroup(userDefinedGroup).withDstPort("80,8080")
                .withProtocol("tcp,udp").build();
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
            .body(builder().withName("https").withGroup(userDefinedGroup).withDstPort("443").withProtocol("tcp").build())
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
    public void verifyChangeGroupOfRule() {

        // POST (create) two groups
        final GroupDTO group3 = saveAndRetrieveGroup(new GroupDTOBuilder().withName("group3").withDescription("another user defined group with name group3")
                .withEnabled(true).withReadOnly(false).build());
        final GroupDTO group4 = saveAndRetrieveGroup(new GroupDTOBuilder().withName("group4").withDescription("another user defined group with name group4")
                .withEnabled(true).withReadOnly(false).build());

        // Create rule with group3
        RuleDTO rule = builder().withName("myrule").withDstPort("80").withProtocol("TCP").withGroup(group3).build();
        rule = saveAndRetrieveRule(rule);

        // Move rule to another group
        rule.setGroup(group4);
        updateAndRetrieveRule(rule);

        // Create similar rule in group3 and try to move the rule back to group3 => should not work since a similar rule exists there already
        saveAndRetrieveRule(builder().withName("myrule").withDstPort("80").withProtocol("TCP").withGroup(group3).build());
        rule.setGroup(group3);
        given().contentType(ContentType.JSON)
                .body(rule)
                .log().all()
                .put(Integer.toString(rule.getId()))
                .then().assertThat()
                .log().all()
                .statusCode(400)
                .body("message", equalTo(Errors.GROUP_DUPLICATE_RULE.getMessage()));

    }

    @Test
    public void verifyCRUDforGroup() {
        // POST (create) group
        final GroupDTO group3 = saveAndRetrieveGroup(new GroupDTOBuilder().withName("group3").withDescription("another user defined group with name group3")
                .withEnabled(true).withReadOnly(true).withPosition(0).build());

        //  POST (create) group with same name => shouldn't be allowed
        given().contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(group3)
                .post("/groups").then().assertThat().statusCode(400) // bad request
                .body(is(String.format("{\"message\":\"A group with name '%s' already exists\",\"context\":\"entity\",\"key\":\"group.name.duplicate\"}", group3.getName())));

        // UPDATE
        group3.setName("newNameOfGroup3");
        group3.setReadOnly(true);
        updateGroup(group3);
        GroupDTO updatedGroup3 = getGroup(group3.getId());
        assertEquals("newNameOfGroup3", updatedGroup3.getName());
        assertFalse(updatedGroup3.isReadOnly()); // readonly cannot be changed

        // DELETE group
        given().delete("groups/" + group3.getId())
                .then().statusCode(204);
        given().get("groups/" + group3.getId())
                .then()
                .assertThat()
                .statusCode(404); // not found => group is really gone
        // Saving the same group should work again...
        saveAndRetrieveGroup(group3);

        // try to delete predefined group => should not work
        int predefinedGroupId = 1;
        given().param("groupId", predefinedGroupId).delete()
                .then().statusCode(400);
    }


    @Test
    public void verifyImmutabilityOfPredefinedGroup() {
        // The predefined group and its rules should not be able to be altered.
        GroupDTO predefinedGroup = getGroup(1);
        assertThat(predefinedGroup.getName(), is(Groups.SYSTEM_DEFINED));
        assertThat(userDefinedGroup.getName(), is(Groups.USER_DEFINED));

        // try to add new rule to group
        final RuleDTO httpRule = builder().withName("http").withGroup(predefinedGroup).withDstPort("80")
                .withProtocol("tcp").build();
        given().contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(httpRule)
                .post().then().assertThat().statusCode(400);

        // try to add existing rule to predefined group
        RuleDTO rule = saveAndRetrieveRule(builder().withName("http").withGroup(userDefinedGroup).withDstPort("80")
                .withProtocol("tcp").build());
        rule.setGroup(predefinedGroup);
        given().contentType(ContentType.JSON)
                .body(rule)
                .log().all()
                .put(Integer.toString(rule.getId()))
                .then().assertThat()
                .log().all()
                .statusCode(400);

        // try to delete a rule in predefined group
        rule = given()
                .param("groupFilter", predefinedGroup.getId())
                .param("limit", 1)
                .get()
                .then()
                .extract().response().body().jsonPath().getList(".", RuleDTO.class).get(0);
        given().delete(Integer.toString(rule.getId())).then().statusCode(400);

        // try to modify group parameters
        GroupDTO updatedGroup = given().contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new GroupDTOBuilder()
                        .withId(predefinedGroup.getId())
                        .withName("new name")
                        .withDescription("new description")
                        .withEnabled(false)
                        .withReadOnly(false)
                        .build())
                .put("/groups/"+predefinedGroup.getId())
                .then().assertThat().statusCode(200)
                .extract().response().as(GroupDTO.class);
        assertThat(updatedGroup.getId(), is(predefinedGroup.getId()));
        assertThat(updatedGroup.getName(), is(predefinedGroup.getName()));
        assertThat(updatedGroup.getDescription(), is(predefinedGroup.getDescription()));
        assertThat(updatedGroup.isEnabled(), is(false));

    }

    private GroupDTO saveAndRetrieveGroup(GroupDTO groupDTO) {
        int groupId = saveGroup(groupDTO);
        final GroupDTO receivedGroup = getGroup(groupId);
        assertThat(receivedGroup.getId(), is(groupId));
        assertThat(receivedGroup.getDescription(), is(groupDTO.getDescription()));
        assertThat(receivedGroup.getName(), is(groupDTO.getName()));
        assertThat(receivedGroup.getRuleCount(), is(0)); // we just created group => must be empty
        assertThat(receivedGroup.isReadOnly(), is(false)); // must always be false no matter what we tried to save
        assertThat(receivedGroup.isEnabled(), is(groupDTO.isEnabled()));
        return receivedGroup;
    }

    private int saveGroup(GroupDTO groupDTO) {
        final String header = given().contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(groupDTO)
                .post("/groups").then().assertThat().statusCode(201) // created
                .extract().header("Location");
        final String[] split = header.split("/");
        int groupId = Integer.parseInt(split[split.length - 1]);
        this.groupIsToDelete.add(groupId);
        return groupId;
    }

    private void updateGroup(GroupDTO groupDTO) {
        given().contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(groupDTO)
                .put("groups/" + groupDTO.getId())
                .then().log().body(true)
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    private GroupDTO getGroup(int groupId) {
        return given().get("groups/" + groupId)
                .then().log().body(true)
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().as(GroupDTO.class);
    }

    private RuleDTO saveAndRetrieveRule(RuleDTO ruleDTO) {
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

    private RuleDTO updateAndRetrieveRule(RuleDTO ruleDTO) {
        given().contentType(ContentType.JSON)
                .body(ruleDTO)
                .log().all()
                .put(Integer.toString(ruleDTO.getId()))
                .then().assertThat()
                .log().all()
                .statusCode(200);

        // Verify update worked
        final RuleDTO updatedRule = given().get(Integer.toString(ruleDTO.getId()))
                .then()
                .log().body(true)
                .assertThat()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .extract().response().as(RuleDTO.class);
        assertThat(updatedRule.getId(), is(ruleDTO.getId()));
        assertThat(updatedRule.getName(), is(ruleDTO.getName()));
        assertThat(updatedRule.getDstAddress(), is(ruleDTO.getDstAddress()));
        assertThat(updatedRule.getProtocols(), is(ruleDTO.getProtocols()));
        assertThat(updatedRule.getGroup().getName(), is(ruleDTO.getGroup().getName()));
        return updatedRule;
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
        final String importCsv = "name;protocol;srcAddress;srcPort;dstAddress;dstPort;exporterFilter;omnidirectional\nmagic-ulf;tcp;;;;1337;;";
        given().contentType("text/comma-separated-values")
                .body(importCsv)
                .post("groups/2")
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
