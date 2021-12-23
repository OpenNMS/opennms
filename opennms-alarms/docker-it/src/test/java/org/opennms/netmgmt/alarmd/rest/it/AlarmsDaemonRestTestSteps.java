/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2021 The OpenNMS Group, Inc.
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
 ******************************************************************************/

package org.opennms.netmgmt.alarmd.rest.it;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AlarmsDaemonRestTestSteps {

    public static final int DEFAULT_HTTP_SOCKET_TIMEOUT = 15_000;

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(AlarmsDaemonRestTestSteps.class);
//    private static final io.restassured.config.HttpClientConfig HttpClientConfig = ;

    private Logger log = DEFAULT_LOGGER;

    //
    // Injected Dependencies
    //
    private RetryUtils retryUtils;


    //
    // Test Configuration
    //
    private String applicationBaseUrl;
    private String path;
    private String username;
    private String password;
    private String acceptEncoding;


    //
    // Test Runtime Data
    //
    private Response restAssuredResponse;
    private JsonPath parsedJsonResponse;

//========================================
// Constructor
//========================================

    public AlarmsDaemonRestTestSteps(RetryUtils retryUtils) {
        this.retryUtils = retryUtils;
    }


//========================================
// Gherkin Rules
//========================================


    @Given("^application base url in system property \"([^\"]*)\"$")
    public void applicationBaseUrlInSystemProperty(String systemProperty) throws Throwable {
        this.applicationBaseUrl = System.getProperty(systemProperty);

        this.log.info("Using BASE URL {}", this.applicationBaseUrl);
    }

    @Given("^http username \"([^\"]*)\" password \"([^\"]*)\"$")
    public void httpUsernamePassword(String username, String password) throws Throwable {
        this.username = username;
        this.password = password;
    }
    @Given("^JSON accept encoding$")
    public void jsonAcceptEncoding() throws Throwable {
        this.acceptEncoding = "application/json";
    }

    @Then("^send GET request at path \"([^\"]*)\" with retry timeout (\\d+)$")
    public void sendGETRequestAtPath(String path, int retryTimeout) throws Throwable {
        URL requestUrl = new URL(new URL(this.applicationBaseUrl), path);

        RestAssuredConfig restAssuredConfig = this.createRestAssuredTestConfig();

        RequestSpecification requestSpecification =
                RestAssured
                        .given()
                        .config(restAssuredConfig)
                        ;

        if (this.username != null) {
            requestSpecification =
                    requestSpecification
                            .auth()
                            .preemptive()
                            .basic(this.username, this.password)
                            ;
        }

        if (this.acceptEncoding != null) {
            requestSpecification =
                    requestSpecification
                            .header("Accept", this.acceptEncoding)
                            ;
        }

        final RequestSpecification finalRequestSpecification = requestSpecification;
        Supplier<Response> operation =
            () ->
                    finalRequestSpecification
                            .get(requestUrl)
                            .thenReturn()
                            ;

        //
        // Retry the operation until success or timeout
        //
        this.restAssuredResponse =
            this.retryUtils.retry(operation, (response) -> ( ( response != null ) && ( response.getStatusCode() != 500 ) ), 1000, retryTimeout, null);

        assertNotNull(this.restAssuredResponse);
    }

    @Then("^verify the response code (\\d+) was returned$")
    public void verifyTheResponseCodeWasReturned (int expectedResponseCode) {
        assertEquals(expectedResponseCode, this.restAssuredResponse.getStatusCode());
    }

    @Then("^parse the JSON response$")
    public void parseTheJsonResponse() {
        this.parsedJsonResponse = JsonPath.from((this.restAssuredResponse.getBody().asString()));
    }

    @Then("^verify JSON path expressions match$")
    public void verifyJsonPathExpressionsMatch(List<String> pathExpressions) {
        for (String onePathExpression : pathExpressions) {
            this.verifyJsonPathExpressionMatch(this.parsedJsonResponse, onePathExpression);
        }
    }

    @Then("^DEBUG dump the response body$")
    public void debugDumpTheResponseBody() {
        this.log.info("RESPONSE BODY = {}", this.restAssuredResponse.getBody().asString());
    }

//========================================
// Internals
//========================================

    private RestAssuredConfig createRestAssuredTestConfig() {
        return RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", DEFAULT_HTTP_SOCKET_TIMEOUT)
                        .setParam("http.socket.timeout", DEFAULT_HTTP_SOCKET_TIMEOUT)
                );
    }

    private void verifyJsonPathExpressionMatch(JsonPath jsonPath, String pathExpression) {
        String[] parts = pathExpression.split(" == ", 2);

        if (parts.length == 2) {
            // Expression and value to match - evaluate as a string and compare
            String actualValue = jsonPath.getString(parts[0]);
            String actualTrimmed = actualValue.trim();

            String expectedTrimmed = parts[1].trim();

            assertEquals("matching to JSON path " + jsonPath, expectedTrimmed, actualTrimmed);
        } else {
            // Just an expression - evaluate as a boolean
            assertTrue("verifying JSON path expression " + pathExpression, jsonPath.getBoolean(pathExpression));
        }
    }
}
