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
 * Verifies the Spring Security rules guarding <code>/rest/config/**</code>
 * in <code>applicationContext-spring-security.xml</code>.
 * <p>
 * Reads stay open to ROLE_REST (and ROLE_MINION), but every write verb requires
 * ROLE_ADMIN (NMS-20382). The rules are order-sensitive (first match wins) and
 * no unit test loads that file, so this pins the behaviour against the live webapp.
 */
public class ConfigRestAccessIT extends AbstractRestIT {

    private static final String REST_USER = "nms20382-rest-only";
    private static final String REST_PASSWORD = "rest-only-password";
    private static final String USERS_PATH = "/opennms/rest/users";

    public ConfigRestAccessIT() {
        super(Version.V1, "config/email-nbi");
    }

    @Before
    public void createRestOnlyUser() {
        final OnmsUser user = new OnmsUser();
        user.setUsername(REST_USER);
        user.setFullName("ROLE_REST only");
        user.setPassword(REST_PASSWORD);
        user.setPasswordSalted(false);
        user.setRoles(Collections.singletonList("ROLE_REST"));

        final StringWriter xml = new StringWriter();
        JAXB.marshal(user, xml);

        // AbstractRestIT.before() has already selected admin credentials
        given().basePath(USERS_PATH)
                .contentType(ContentType.XML)
                .queryParam("hashPassword", true)
                .body(xml.toString())
                .post()
                .then().log().status()
                .assertThat().statusCode(201);

        // UserFactory throttles its users.xml change check to once per second, so the new user
        // is not visible to authentication immediately. Wait until it can log in.
        asRestUser();
        await().atMost(30, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> given().basePath("/opennms/rest/whoami").get().getStatusCode() == 200);
        applyDefaultCredentials();
    }

    @After
    public void deleteRestOnlyUser() {
        applyDefaultCredentials();
        given().basePath(USERS_PATH + "/" + REST_USER)
                .delete()
                .then().log().status()
                .assertThat().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    public void restUserCanReadConfig() {
        asRestUser();
        given().accept(ContentType.TEXT).get("/status")
                .then().log().status()
                .assertThat().statusCode(200);
    }

    @Test
    public void restUserCannotWriteConfig() {
        asRestUser();

        given().queryParam("enabled", false).put("/status")
                .then().log().status()
                .assertThat().statusCode(403);

        given().contentType(ContentType.URLENC)
                .formParam("name", "nms20382-should-not-exist")
                .post("/destinations")
                .then().log().status()
                .assertThat().statusCode(403);

        given().contentType(ContentType.URLENC)
                .formParam("name", "google")
                .put("/destinations/google")
                .then().log().status()
                .assertThat().statusCode(403);

        given().delete("/destinations/google")
                .then().log().status()
                .assertThat().statusCode(403);

        // nothing leaked through: the destination the DELETE targeted is still there
        applyDefaultCredentials();
        given().accept(ContentType.JSON).get("/destinations/google")
                .then().log().status()
                .assertThat().statusCode(200);
    }

    @Test
    public void adminCanWriteConfig() {
        // the shipped email-northbounder-configuration.xml has the NBI disabled; re-asserting
        // the same value exercises the write path without changing the stack's behaviour
        given().queryParam("enabled", false).put("/status")
                .then().log().status()
                .assertThat().statusCode(204);
    }

    private void asRestUser() {
        authentication = preemptive().basic(REST_USER, REST_PASSWORD);
    }
}
