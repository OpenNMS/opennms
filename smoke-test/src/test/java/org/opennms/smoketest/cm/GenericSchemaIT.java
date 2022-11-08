/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.cm;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.restassured.RestAssured.preemptive;
import static org.junit.Assert.assertTrue;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME;

public class GenericSchemaIT {
    private static final Logger LOG = LoggerFactory.getLogger(GenericSchemaIT.class);
    private static final String CM_BASEPATH = "/opennms/rest/cm";

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    @Before
    public void before() {
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
    }

    @Test
    public void testConfigurationLoadFromConfiguration() throws IOException {
        RestAssured.basePath = CM_BASEPATH;
        Response response = RestAssured.get();
        response.then().assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON);
        JSONArray schemaNamesList = new JSONArray(response.getBody().print());
        for (var schemaNameObject : schemaNamesList) {
            LOG.info("Working on {}", schemaNameObject.toString());
            String schemaName = schemaNameObject.toString();
            RestAssured.basePath = CM_BASEPATH + "/schema/" + schemaName;
            Response schemaResponse = RestAssured.given().header("accept", ContentType.JSON).when().get();
            schemaResponse.then().assertThat()
                    .statusCode(200)
                    .contentType(ContentType.JSON);
            JSONObject jsonObject = new JSONObject(schemaResponse.getBody().print());
            JSONObject path = new JSONObject(jsonObject.get("paths").toString());
            LOG.info("path : " + path);
            JSONObject get = new JSONObject(path.get("/rest/cm/" + schemaName).toString());
            JSONObject getDetails = new JSONObject(get.get("get").toString());
            boolean responseCodeCheck = getDetails.get("responses").toString().contains("200");
            boolean tagCheck = getDetails.get("tags").toString().contains(schemaName);
            assertTrue(tagCheck);
            assertTrue(responseCodeCheck);
        }

    }
}