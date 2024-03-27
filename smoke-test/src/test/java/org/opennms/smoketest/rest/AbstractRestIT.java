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
import static io.restassured.RestAssured.preemptive;

import java.util.Objects;

import org.junit.Before;
import org.junit.ClassRule;
import org.opennms.smoketest.OpenNMSSeleniumIT;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;

import io.restassured.RestAssured;

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

}
