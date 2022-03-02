package org.opennms.smoketest.rest;

import io.restassured.http.ContentType;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class InfoRestIT extends AbstractRestIT {

    public InfoRestIT() {
        super(Version.V1, "info");
    }

    @Test
    public void testServices() {
        given().accept(ContentType.JSON).get()
                .then().log().status()
                .assertThat()
                .statusCode(200)
                .body("packageName", is("opennms"))
                .body("packageDescription", is("OpenNMS"))
                .body("services.Eventd", is("running"))
                .body("services.Pollerd", is("running"));
    }
}
