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