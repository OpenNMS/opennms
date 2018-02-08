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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;

import io.restassured.RestAssured;

// Ensures if "X-Requeste-With" is set to "XMLHttpRequest" no "WWW-Authenticate" header is sent with the response
public class XRequestedWithRestIT extends OpenNMSSeleniumTestCase {

    @Before
    public void setUp() {
        RestAssured.baseURI = getBaseUrl();
        RestAssured.port = getServerHttpPort();
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
