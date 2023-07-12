package org.opennms.smoketest.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import io.restassured.http.ContentType;

public class InfoRestIT extends AbstractRestIT {

    public InfoRestIT() {
        super(Version.V1, "info");
    }

    @Test
    public void testServices() throws InterruptedException {
        given().accept(ContentType.JSON).get()
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .body("packageName", is("opennms"))
                .body("packageDescription", is("OpenNMS"))
                .body("services.Eventd", is("running"))
                .body("services.Pollerd", is("running"));
    }
}
