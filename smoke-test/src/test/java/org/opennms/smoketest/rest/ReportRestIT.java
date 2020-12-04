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

import static com.jayway.awaitility.Awaitility.await;
import static io.restassured.RestAssured.authentication;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.bind.JAXB;

import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.netmgt.model.OnmsUser;

import io.restassured.http.ContentType;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ReportRestIT extends AbstractRestIT {

    private final static String REPORT_ID = "local_Early-Morning-Report";
    private final static String INSTANCE_ID_TEMPLATE = REPORT_ID + "-" + ReportRestIT.class.getSimpleName() + "_%user%";

    private JSONObject deliveryOptions;
    private JSONObject reportParameters;

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

        // Clear all reports created/scheduled
        given().delete("persisted").then().statusCode(202);
        given().delete("scheduled").then().statusCode(202);

        // Default Options
        deliveryOptions = new JSONObject();
        deliveryOptions.put("instanceId", ""); // Will be applied later
        deliveryOptions.put("sendMail", false);
        deliveryOptions.put("persist", true);
        deliveryOptions.put("format", "PDF");

        // Default Parameters
        reportParameters = new JSONObject();
        reportParameters.put("id", REPORT_ID);
        reportParameters.put("format", "PDF");
        reportParameters.put("parameters", new JSONArray());
        reportParameters.put("deliveryOptions", deliveryOptions); // only used for delivering reports

        // Default Cron Expression
        reportParameters.put("cronExpression", "0 */1 * * * ?"); // only used for scheduling reports
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        applyDefaultCredentials();

        // Deleted created users
        final JSONObject usersObject = getUsers();
        if (usersObject.getInt("count") > 2) { // Only delete users if created
            sendDelete("/rest/users/test");
            sendDelete("/rest/users/ulf");
        }

        // Clear all reports created/scheduled
        given().delete("persisted").then().statusCode(202);
        given().delete("scheduled").then().statusCode(202);
    }

    @Test
    public void verifyPermissionsForPrivilegedUsers() {
        final String[][] users = new String[][]{
                // username, password
                new String[]{"admin", "admin"},
                new String[]{"ulf", "admin"}
        };

         /*
         * Verify as privileged users
         */
        for (String[] user : users) {
            authentication = preemptive().basic(user[0], user[1]);

            // Verify list works
            given().get().then().statusCode(200);

            // Verify get specific report works
            given().get(REPORT_ID).then().statusCode(200);

            // Verify render report works
            given().body(reportParameters.toString())
                    .contentType(ContentType.JSON)
                    .log().all()
                    .post(REPORT_ID)
                    .then().statusCode(200);

            /**
             * Delivered Reports
             */
            // Verify list already persisted reports (none yet)
            given().get("persisted").then().statusCode(204);

            // Verify deliver report works
            deliveryOptions.put("instanceId", INSTANCE_ID_TEMPLATE.replaceAll("%user%", user[0]));
            given().body(reportParameters.toString())
                    .contentType(ContentType.JSON)
                    .log().all()
                    .post("persisted")
                    .then().statusCode(202);

            // Verify list already persisted reports work (one yet)
            final AtomicReference<Integer> persistedId = new AtomicReference<>(-1);
            await().atMost(5, MINUTES).pollInterval(5, SECONDS).until(() -> {
                        final String response = given().get("persisted")
                                .then().log().status()
                                .assertThat()
                                .statusCode(200)
                                .body("", Matchers.hasSize(1))
                                .extract().response().asString();
                        final JSONArray persistedReports = new JSONArray(response);
                        if (persistedReports.length() == 1) {
                            persistedId.set(persistedReports.getJSONObject(0).getInt("id"));
                            return; // pass
                        }
                        throw new IllegalStateException("Invalid Result returned. Expected 1 report, but got " + persistedReports.length());
                    }
            );

            // Verify deleting existing persisted report
            given().delete("persisted/" + persistedId.get()).then().statusCode(202);

            // Verify delete all persisted reports
            given().delete("persisted").then().statusCode(202);

            /**
             * Scheduled Reports
             */
            // Verify listing scheduled report works (none yet)
            given().get("scheduled").then().statusCode(204);

            // Verify Creating a scheduled Report works
            deliveryOptions.put("instanceId", INSTANCE_ID_TEMPLATE.replaceAll("%user%", user[0]));
            given().body(reportParameters.toString())
                    .contentType(ContentType.JSON)
                    .log().all()
                    .post("scheduled")
                    .then().statusCode(202);

            // Verify listing scheduled report works (one yet)
            final String response = given().get("scheduled")
                    .then().log().status()
                    .assertThat()
                    .statusCode(200)
                    .body("", Matchers.hasSize(1))
                    .extract().response().asString();
            final JSONArray scheduledReports = new JSONArray(response);
            if (scheduledReports.length() != 1) {
                throw new IllegalStateException("Expected one result, but got " + scheduledReports.length());
            }
            final String scheduledId = scheduledReports.getJSONObject(0).getString("triggerName");

            // Verify deleting specific scheduled report works
            given().delete("scheduled/" + scheduledId).then().statusCode(202);

            // Verify deleting scheduled reports work
            given().delete("scheduled").then().statusCode(202);
        }
    }

    @Test
    public void verifyPermissionsForUnprivilegedUsers() {
        authentication = preemptive().basic("test", "admin");

        // Verify list works
        given().get().then().statusCode(200);

        // Verify get specific report works
        given().get(REPORT_ID).then().statusCode(200);

        // Verify render report works
        given().body(reportParameters.toString())
                .contentType(ContentType.JSON)
                .log().all()
                .post(REPORT_ID)
                .then().statusCode(200);

        // Verify list already persisted reports
        given().get("persisted").then().statusCode(204);

        // Verify delete already persisted reports
        given().delete("persisted").then().statusCode(403);

        // Verify listing scheduled report works
        given().get("scheduled").then().statusCode(204);

        // Verify deleting scheduled reports work
        given().delete("scheduled").then().statusCode(403);
    }

    private static JSONObject getUsers() {
        final String responseBody = given().log().uri().accept(ContentType.JSON).get("../users").then().log().all().statusCode(200).extract().response().asString();
        final JSONObject usersObject = new JSONObject(responseBody);
        return usersObject;
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
