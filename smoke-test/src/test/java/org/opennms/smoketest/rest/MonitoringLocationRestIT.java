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
package org.opennms.smoketest.rest;

import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertThat;

public class MonitoringLocationRestIT extends AbstractRestIT {

    public MonitoringLocationRestIT() {
        super(Version.V1, "monitoringLocations");
    }

    @Test
    public void testSorting() {
        final String jsonString = given()
                .accept(ContentType.JSON)
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .response()
                .asString();

        final JSONObject jsonObject = new JSONObject(jsonString);
        final JSONArray jsonArray = jsonObject.getJSONArray("location");

        assertThat(jsonArray.length(), Matchers.is(6));
        assertThat(jsonArray.getJSONObject(0).getString("location-name"), Matchers.is("AAA"));
        assertThat(jsonArray.getJSONObject(1).getString("location-name"), Matchers.is("BBB"));
        assertThat(jsonArray.getJSONObject(2).getString("location-name"), Matchers.is("CCC"));
        assertThat(jsonArray.getJSONObject(3).getString("location-name"), Matchers.is("DDD"));
        assertThat(jsonArray.getJSONObject(4).getString("location-name"), Matchers.is("Default"));
        assertThat(jsonArray.getJSONObject(5).getString("location-name"), Matchers.is("EEE"));
    }

    @Before
    public void setup() {
        addLocation("CCC");
        addLocation("BBB");
        addLocation("EEE");
        addLocation("AAA");
        addLocation("DDD");
    }

    @After
    public void tearDown() {
        given().delete("aaa");
        given().delete("bbb");
        given().delete("ccc");
        given().delete("ddd");
        given().delete("eee");
    }

    public void addLocation(final String locationName) {
        final String locationXml = "<location location-name=\""+locationName+"\" monitoring-area=\""+locationName+"\" priority=\"100\"/>";
        given().body(locationXml).contentType(ContentType.XML).post()
                .then().assertThat()
                .statusCode(201);
    }
}
