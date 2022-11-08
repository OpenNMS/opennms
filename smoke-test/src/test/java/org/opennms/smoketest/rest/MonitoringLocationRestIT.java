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
