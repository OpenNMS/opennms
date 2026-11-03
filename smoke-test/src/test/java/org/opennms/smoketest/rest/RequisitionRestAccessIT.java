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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        addUser(REST_USER, PASSWORD, "ROLE_REST");
        addUser(PROVISION_USER, PASSWORD, "ROLE_PROVISION");
    }

    @After
    public void cleanUp() {
        applyDefaultCredentials();
        given().delete("/" + FOREIGN_SOURCE)
                .then().log().status()
                .assertThat().statusCode(anyOf(is(202), is(204), is(404)));
        removeUser(REST_USER);
        removeUser(PROVISION_USER);
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

    private void as(final String username) {
        useCredentials(username, PASSWORD);
    }
}
