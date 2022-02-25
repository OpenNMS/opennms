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

package org.opennms.smoketest.sentinel;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;


import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.junit.SentinelTests;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;

import java.util.Arrays;
import java.util.List;


@Category(SentinelTests.class)
public class SentinelRestIT {

    private static final Logger LOG = LoggerFactory.getLogger(SentinelRestIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.SENTINEL;

    @Before
    public void setUp() {
        RestAssured.baseURI = stack.sentinel().getWebUrl().toString();
        RestAssured.port = stack.sentinel().getWebPort();
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
    }

    @Test
    public void testRestHealthServiceOnSentinel() throws Exception {

        LOG.info("testing /sentinel/rest/health .........");
        await()
        .atMost(5, MINUTES)
        .pollInterval(10, SECONDS)
        .until(SentinelRestIT::isServiceOk, Matchers.equalTo(true));

        LOG.info("testing /sentinel/rest/health?tag=local .........");
        List<String> localDescriptions = Arrays.asList("Verifying installed bundles", "Retrieving NodeDao", "DNS Lookups (Netty)");
        List<String> descriptions = given().get("/sentinel/rest/health?tag=local")
                .then()
                .log().ifValidationFails().log().ifStatusCodeIsEqualTo(200)
                .statusCode(200)
                .body("healthy", Matchers.notNullValue())
                .extract()
                .body()
                .jsonPath().getList("responses.description",String.class);

        LOG.info("descriptions in tag 'local' is: {}", Arrays.toString(descriptions.toArray()));
        descriptions.stream().forEach(d-> Assert.assertTrue(localDescriptions.contains(d) || d.contains("Verifying Listener")));

        LOG.info("testing /sentinel/rest/health/probe?tag=local  .......");
        given().get("/sentinel/rest/health/probe?tag=local")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .body(Matchers.anyOf(Matchers.equalTo("Everything is awesome"), Matchers.equalTo("Oh no, something is wrong")));
    }

    private static boolean isServiceOk(){
         return given().get("/sentinel/rest/health")
        .then().log().ifValidationFails().log().ifStatusCodeIsEqualTo(200)
        .extract().statusCode() == 200;

    }
}
