/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
