/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.minion;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;

import java.util.Arrays;

@Category(MinionTests.class)
public class MinionRestIT {

    private static final Logger LOG = LoggerFactory.getLogger(MinionRestIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINION;

    @Before
    public void setUp() {
        RestAssured.baseURI = stack.minion().getWebUrl().toString();
        RestAssured.port = stack.minion().getWebPort();
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
    }

    @Test
    public void testMbeansOnMinionWithJolokia() throws Exception {

        given().get("/jolokia")
                .then().assertThat()
                .statusCode(200);

        given().get("/jolokia/read/java.lang:type=Memory/HeapMemoryUsage")
                .then().assertThat().body(Matchers.containsString("HeapMemoryUsage"));

        given().get("/jolokia/read/org.opennms.core.ipc.sink.producer:name=*.dispatch")
                .then().assertThat().body(Matchers.containsString("Heartbeat"));
    }

    @Test
    public void testRestHealthServiceOnMinion() throws Exception {

        given().get("/minion/rest/health")
                .then().assertThat()
                .statusCode(200);

        given().get("/minion/rest/health?tag=local")
                .then()
                .assertThat()
                .statusCode(200)
                .body("healthy", Matchers.oneOf(true, false))
                .assertThat()
                .body("responses.description", Matchers.anyOf(Matchers.hasItem("Verifying installed bundles"),
                        Matchers.hasItem("Verifying Listener "),
                        Matchers.hasItem("Retrieving NodeDao"),
                        Matchers.hasItem("DNS Lookups (Netty)")));

        given().get("/minion/rest/health/probe")
                .then()
                .assertThat()
                .statusCode(200)
                .body("healthy", Matchers.oneOf(true, false));

        given().get("/minion/rest/health/probe?tag=local")
                .then()
                .assertThat()
                .statusCode(200)
                .body("healthy", Matchers.oneOf(true, false));
    }
}
