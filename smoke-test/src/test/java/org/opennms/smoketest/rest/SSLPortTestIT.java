/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.*;
import org.opennms.smoketest.stacks.OpenNMSStack;

import io.restassured.RestAssured;

// Ensures if "X-Requeste-With" is set to "XMLHttpRequest" no "WWW-Authenticate" header is sent with the response

public class SSLPortTestIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.SSL;

    @Before
    public void setUp() {
        // Always reset the session before the test since we expect no existing session/cookies to be present
        RestAssured.reset();
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getSSLPort();
        RestAssured.basePath = "/opennms/";
    }

    @After
    public void tearDown() {
        RestAssured.reset();
    }

    @Test
    public void verifyOpenNmsSSL() {
        // Verify header exists by default, if not authorized
        RestAssured.given()
                .get()
                .then().statusCode(200);
    }
}
