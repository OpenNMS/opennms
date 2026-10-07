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
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXB;

import org.junit.Before;
import org.junit.ClassRule;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.smoketest.OpenNMSSeleniumIT;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public abstract class AbstractRestIT extends OpenNMSSeleniumIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    public enum Version {
        V1("/rest/"), V2("/api/v2/");

        private final String path;

        Version(String path) {
            this.path = Objects.requireNonNull(path);
        }
    }

    private static final String USERS_PATH = "/opennms/rest/users";

    private final String path;

    public AbstractRestIT(Version version, String path) {
        this.path = "/opennms" + version.path + Objects.requireNonNull(path);
    }

    @Before
    public void before() {
        // Always reset the session before the test since we expect no existing session/cookies to be present
        RestAssured.reset();
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.basePath = path;
        applyDefaultCredentials();
    }

    protected void applyDefaultCredentials() {
        authentication = preemptive().basic(OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD);
    }

    /**
     * Use these credentials for the RestAssured requests that follow.
     */
    protected void useCredentials(final String username, final String password) {
        authentication = preemptive().basic(username, password);
    }

    /**
     * Create a user with the given roles through the REST API, as the admin user.
     * Return when the new user can log in. The admin credentials are active when this method returns.
     */
    protected void addUser(final String username, final String password, final String... roles) {
        final OnmsUser user = new OnmsUser();
        user.setUsername(username);
        user.setFullName(String.join(" + ", roles));
        user.setPassword(password);
        user.setPasswordSalted(false);
        user.setRoles(Arrays.asList(roles));

        final StringWriter xml = new StringWriter();
        JAXB.marshal(user, xml);

        applyDefaultCredentials();
        given().basePath(USERS_PATH)
                .contentType(ContentType.XML)
                .queryParam("hashPassword", true)
                .body(xml.toString())
                .post()
                .then().log().status()
                .assertThat().statusCode(201);

        // UserFactory checks users.xml for changes at most once per second, so a new user cannot log in at once.
        // Wait until the login is accepted. Some roles get 403 from whoami, so wait for any status other than 401.
        useCredentials(username, password);
        await().atMost(30, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> given().basePath("/opennms/rest/whoami").get().getStatusCode() != 401);
        applyDefaultCredentials();
    }

    /**
     * Delete a user through the REST API, as the admin user. A user that does not exist is not an error.
     */
    protected void removeUser(final String username) {
        applyDefaultCredentials();
        given().basePath(USERS_PATH + "/" + username)
                .delete()
                .then().log().status()
                .assertThat().statusCode(anyOf(is(200), is(204), is(404)));
    }

}
