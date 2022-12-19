/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class JaegerTracingIT {
    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withJaeger()
            .withMinion()
            .build());

    @Before
    public void before() {
        String host = stack.jaeger().getHost();
        Integer port = stack.jaeger().getMappedPort(16686);

        RestAssured.reset();
        RestAssured.baseURI = "http://" + host + ":" + port.toString();
        RestAssured.port = port;
        RestAssured.basePath = "/api";
    }

    @Test
    public void horizonTrapdListenerConfigTraceCheck() throws Exception {
        /*
         * I'm just checking for a random trace that Horizon generates.
         * This one seems consistent on startup and usually has two spans.
         */
        var json = given().accept(ContentType.JSON)
                .param("service", "OpenNMS")
                .param("operation", "trapd.listener.config")
                .get("/traces")
                .then().log().ifValidationFails()
                .assertThat()
                .statusCode(200)
                .extract().jsonPath();

        // Make sure we have at least one trace
        var traces = json.getList("data.spans", List.class);
        assertThat("trace count; traces " + traces, traces.size(), greaterThanOrEqualTo(1));

        // Make sure each trace has two spans
        for (var spans : traces) {
            assertThat("spans in trace; spans: " + spans, spans.size(), equalTo(2));
        }
    }

    @Test
    public void minionEcho() throws Exception {
        var json = given().accept(ContentType.JSON)
                .param("service", "OpenNMS")
                .param("operation", "Echo")
                .get("/traces")
                .then().log().ifValidationFails()
                .assertThat()
                .statusCode(200)
                .extract().jsonPath();

        // Make sure we have at least one trace
        var traces = json.getList("data.spans", List.class);
        assertThat("trace count; traces " + traces, traces.size(), greaterThanOrEqualTo(1));

        // Make sure each trace has two items
        for (var spans : traces) {
            // If we wanted extra credit, we could make sure one is from OpenNMS and another is from the Minion
            assertThat("spans in trace; spans: " + spans, spans.size(), equalTo(2));
        }
    }
}