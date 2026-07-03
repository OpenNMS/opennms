package org.opennms.smoketest.rest;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME;

public class HawtioRestIT {

    private static final Logger LOG = LoggerFactory.getLogger(HawtioRestIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    @Before
    public void setUp() {
        RestAssured.baseURI = stack.opennms().getWebUrl().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
    }


    @Test
    public void testHawtio() {
        given().get("/hawtio/jolokia/version")
                .then().assertThat()
                .statusCode(200);
    }
}
