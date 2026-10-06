/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2026 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2026 The OpenNMS Group, Inc.
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
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.io.StringWriter;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXB;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsUser;

import io.restassured.http.ContentType;

/**
 * Verifies the Spring Security rules for <code>/rest/requisitions/**</code>
 * in <code>applicationContext-spring-security.xml</code>.
 * <p>
 * Requisitions can contain asset credentials, so reads require ROLE_PROVISION or ROLE_ADMIN.
 * ROLE_REST can still write requisitions (NMS-20407). The rules are order-sensitive (first match wins)
 * and no unit test loads that file, so this test checks the behaviour against the live webapp.
 */
public class RequisitionRestAccessIT extends AbstractRestIT {

    private static final String REST_USER = "nms20407-rest-only";
    private static final String PROVISION_USER = "nms20407-provision-only";
    private static final String PASSWORD = "nms20407-password";
    private static final String USERS_PATH = "/opennms/rest/users";
    private static final String FOREIGN_SOURCE = "nms20407";
    private static final String REQUISITIONS_PATH = "/opennms/rest/requisitions";

    private static final String REQUISITION = "<model-import xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\""
            + " foreign-source=\"" + FOREIGN_SOURCE + "\">"
            + "<node foreign-id=\"n1\" node-label=\"n1\">"
            + "<asset name=\"password\" value=\"secretPass\"/>"
            + "</node>"
            + "</model-import>";

    public RequisitionRestAccessIT() {
        super(Version.V1, "requisitions");
    }

    @Before
    public void createUsers() {
        createUser(REST_USER, "ROLE_REST");
        createUser(PROVISION_USER, "ROLE_PROVISION");
    }

    @After
    public void cleanUp() {
        applyDefaultCredentials();
        given().delete("/" + FOREIGN_SOURCE)
                .then().log().status()
                .assertThat().statusCode(anyOf(is(202), is(204), is(404)));
        for (final String user : new String[] { REST_USER, PROVISION_USER }) {
            given().basePath(USERS_PATH + "/" + user)
                    .delete()
                    .then().log().status()
                    .assertThat().statusCode(anyOf(is(200), is(204), is(404)));
        }
    }

    @Test
    public void restUserCanWriteButNotReadRequisitions() {
        as(REST_USER);
        given().contentType(ContentType.XML).body(REQUISITION).post()
                .then().log().status()
                .assertThat().statusCode(202);

        given().get()
                .then().log().status()
                .assertThat().statusCode(403);
        given().get("/" + FOREIGN_SOURCE)
                .then().log().status()
                .assertThat().statusCode(403);
        given().get("/" + FOREIGN_SOURCE + "/nodes/n1/assets")
                .then().log().status()
                .assertThat().statusCode(403);
        given().head("/" + FOREIGN_SOURCE)
                .then().log().status()
                .assertThat().statusCode(403);

        // The v1 API maps the .xml and .json suffixes to the requisitions root.
        for (final String alias : new String[] { REQUISITIONS_PATH + ".xml", REQUISITIONS_PATH + ".json" }) {
            given().basePath(alias).get()
                    .then().log().status()
                    .assertThat().statusCode(403);
        }

        // The write from the ROLE_REST account was stored.
        applyDefaultCredentials();
        given().accept(ContentType.XML).get("/" + FOREIGN_SOURCE)
                .then().log().status()
                .assertThat().statusCode(200)
                .body(containsString("foreign-id=\"n1\""));
    }

    @Test
    public void provisionUserCanReadRequisitions() {
        given().contentType(ContentType.XML).body(REQUISITION).post()
                .then().log().status()
                .assertThat().statusCode(202);

        as(PROVISION_USER);
        given().accept(ContentType.XML).get("/" + FOREIGN_SOURCE)
                .then().log().status()
                .assertThat().statusCode(200)
                .body(containsString("foreign-id=\"n1\""));
        given().basePath(REQUISITIONS_PATH + ".xml").get()
                .then().log().status()
                .assertThat().statusCode(200);
    }

    private void createUser(final String username, final String role) {
        final OnmsUser user = new OnmsUser();
        user.setUsername(username);
        user.setFullName(role + " only");
        user.setPassword(PASSWORD);
        user.setPasswordSalted(false);
        user.setRoles(Collections.singletonList(role));

        final StringWriter xml = new StringWriter();
        JAXB.marshal(user, xml);

        // AbstractRestIT.before() has already selected admin credentials.
        given().basePath(USERS_PATH)
                .contentType(ContentType.XML)
                .queryParam("hashPassword", true)
                .body(xml.toString())
                .post()
                .then().log().status()
                .assertThat().statusCode(201);

        // UserFactory checks users.xml for changes at most once per second, so a new user cannot log in at once.
        // Wait until the login is accepted. A ROLE_PROVISION-only user gets 403 from whoami, but not 401.
        as(username);
        await().atMost(30, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> given().basePath("/opennms/rest/whoami").get().getStatusCode() != 401);
        applyDefaultCredentials();
    }

    private void as(final String username) {
        authentication = preemptive().basic(username, PASSWORD);
    }
}
