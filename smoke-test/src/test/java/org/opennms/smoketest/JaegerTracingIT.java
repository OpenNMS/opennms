/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;

import io.restassured.RestAssured;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@Category(org.opennms.smoketest.junit.MinionTests.class)
public class JaegerTracingIT {
    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withJaeger()
            .withMinion()
            .build());

    @Before
    public void before() throws MalformedURLException {
        RestAssured.reset();
        RestAssured.baseURI = stack.jaeger().getURL("").toString();
        RestAssured.basePath = "/api";
    }

    @Test
    public void horizonTrapdListenerConfigTraceCheck() throws Exception {
        /*
         * I'm just checking for a random trace that Horizon generates.
         * This one seems consistent on startup and usually has two spans.
         * It might take a short while for the traces to show up, so we
         * poll for a little while.
         */
        await("Wait for a 'trapd.listener.config' trace with two spans")
                .atMost(20, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .pollDelay(0, TimeUnit.SECONDS)
                .ignoreException(AssertionError.class)
                .until(
                        () -> {
                            given().accept(ContentType.JSON)
                                    .param("service", SystemInfoUtils.getInstanceId())
                                    .param("operation", "trapd.listener.config")
                                    .param("limit", 1)
                                    .get("/traces")
                                    .then().log().ifValidationFails()
                                    .assertThat()
                                    .statusCode(200)
                                    .body("data[0].spans.size()", Matchers.is(2));
                            return true;
                        });

    }

    @Test
    public void minionEcho() throws Exception {
        await("Wait for a 'Echo' trace with two spans")
                .atMost(20, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .pollDelay(0, TimeUnit.SECONDS)
                .ignoreException(AssertionError.class)
                .untilAsserted(() ->
                        givenWithFailureDetails((spec) -> {
                            spec
                                    .accept(ContentType.JSON)
                                    .param("service", SystemInfoUtils.getInstanceId())
                                    .param("operation", "Echo")
                                    .param("limit", 1)
                                    .get("/traces")
                                    .then()
                                    .assertThat()
                                    .statusCode(200)
                                    .body("data[0].spans.size()", Matchers.is(2)); }
                        ));
    }

    /**
     * Wrap REST Assured calls to include additional failure details when there is an AssertionError.
     *
     * @param consumer Consumer callback that will be passed the output of given() with our filter() applied to capture
     *                 the response.
     *
     * If the callback throws an AssertionError, a new AssertionError will be generated that includes the response
     * details along with the original AssertionError as its cause.
     */
    public static void givenWithFailureDetails(final Consumer<? super RequestSpecification> consumer) {
        var responseDetails = new ByteArrayOutputStream();

        try {
            consumer.accept(given().filter(ResponseLoggingFilter.logResponseTo(new PrintStream(responseDetails))));
        } catch (AssertionError e) {
            throw new AssertionError("REST Assured assertion failed, response body (if any) shown below: "
                    + e.getMessage()
                    + "\n" + responseDetails.toString(), e);
        }
    }
}
