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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;

import io.restassured.RestAssured;

// Ensures if "X-Requeste-With" is set to "XMLHttpRequest" no "WWW-Authenticate" header is sent with the response
public class XRequestedWithRestIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    @Before
    public void setUp() {
        // Always reset the session before the test since we expect no existing session/cookies to be present
        RestAssured.reset();
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.basePath = "/opennms/rest/";
    }

    @After
    public void tearDown() {
        RestAssured.reset();
    }

    @Test
    public void verifyWWWAuthenticate() {
        // Verify header exists by default, if not authorized
        RestAssured.given()
                .get()
                .then().assertThat()
                    .header("WWW-Authenticate", containsString("Basic"));

        // Verify header does not exist, if X-Request-With is set accordingly
        RestAssured.given()
                    .header("X-Requested-With", "XMLHttpRequest")
                .get()
                .then().assertThat()
                    .header("WWW-Authenticate", is(nullValue()));
    }
}
