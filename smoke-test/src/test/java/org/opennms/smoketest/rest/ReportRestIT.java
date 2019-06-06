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

package org.opennms.smoketest.rest;

import static io.restassured.RestAssured.authentication;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.bind.JAXB;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsUser;

import io.restassured.http.ContentType;

public class ReportRestIT extends AbstractRestIT {

    public ReportRestIT() {
        super(Version.V1, "reports");
    }

    @Before
    public void setUp() throws IOException, InterruptedException {
        final JSONObject usersObject = getUsers();
        if (usersObject.getInt("count") == 2) { // Only create users if not already created
            postUser(createUser("test", "Test User", "test@opennms.org", "21232F297A57A5A743894A0E4A801FC3" /* admin */,  "ROLE_USER"));
            postUser(createUser("ulf", "Report Designer", "ulf@opennms.org", "21232F297A57A5A743894A0E4A801FC3" /* admin */, "ROLE_REPORT_DESIGNER", "ROLE_USER"));
        }
    }

//    @After
//    public void tearDown() throws IOException, InterruptedException {
//        applyDefaultCredentials();
//        final JSONObject usersObject = getUsers();
//        if (usersObject.getInt("count") > 2) { // Only delete users if created
//            sendDelete("/rest/users/test");
//            sendDelete("/rest/users/ulf");
//        }
//    }

    private JSONObject getUsers() {
        final String responseBody = given().log().uri().accept(ContentType.JSON).get("../users").then().log().all().statusCode(200).extract().response().asString();
        final JSONObject usersObject = new JSONObject(responseBody);
        return usersObject;
    }

    @Test
    public void verifyPermissions() {
        final String ReportId = "local_Early-Morning-Report";
        final String[][] users = new String[][] {
                new String[]{"admin", "admin"},
                new String[]{"ulf", "admin"}
        };

        /*
         * Verify as privilegued users
         */
        for (String[] user : users) {
            authentication = preemptive().basic(user[0], user[1]);

            // Verify list works
            given().get().then().statusCode(200);

            // Verify get specific report works
            given().get(ReportId).then().statusCode(200);

            // Verify render report works
            // TODO MVR implement
//        RestAssured.given().body(TODO MVR).post(ReportId).then().statusCode(200);

            // Verify list already persisted reports
            given().get("persisted").then().statusCode(204);

            // Verify delete already persisted reports
            given().delete("persisted").then().statusCode(202);

            // Verify deleting existing persisted report
            // TODO MVR
            // RestAssured.given().delete("persisted/" + ReportId).then().statusCode(202);

            // Verify listing scheduled report works
            given().get("scheduled").then().statusCode(204);

            // Verify deleting scheduled reports work
            given().delete("scheduled").then().statusCode(202);

            // Verify deleting specific scheduled report works
            // TODO MVR
            // RestAssured.given().delete("scheduled/" + ReportId).then().statusCode(202);
        }

        // Now try with not authenticated user
        authentication = preemptive().basic("test", "admin");

        // Verify list works
        given().get().then().statusCode(200);

        // Verify get specific report works
        given().get(ReportId).then().statusCode(200);

        // Verify render report works
        // TODO MVR implement
//        RestAssured.given().body(TODO MVR).post(ReportId).then().statusCode(200);

        // Verify list already persisted reports
        given().get("persisted").then().statusCode(204);

        // Verify delete already persisted reports
        given().delete("persisted").then().statusCode(403);

        // Verify deleting existing persisted report
        // TODO MVR
        // RestAssured.given().delete("persisted/" + ReportId).then().statusCode(202);

        // Verify listing scheduled report works
        given().get("scheduled").then().statusCode(204);

        // Verify deleting scheduled reports work
        given().delete("scheduled").then().statusCode(403);

        // Verify deleting specific scheduled report works
        // TODO MVR
        // RestAssured.given().delete("scheduled/" + ReportId).then().statusCode(202);
    }

    private void postUser(final OnmsUser user) throws IOException, InterruptedException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JAXB.marshal(user, outputStream);

        sendPost("/rest/users", new String(outputStream.toByteArray()));
    }

    private static OnmsUser createUser(String userId, String username, String userEmail, String userPasswordHash, String... roles) {
        final OnmsUser user = new OnmsUser();
        user.setUsername(userId);
        user.setFullName(username);
        user.setEmail(userEmail);
        user.setPassword(userPasswordHash);
        user.setPasswordSalted(false);
        user.setRoles(Arrays.asList(roles));
        return user;
    }
}
