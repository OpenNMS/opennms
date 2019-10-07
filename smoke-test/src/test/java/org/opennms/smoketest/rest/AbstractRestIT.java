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
