package org.opennms.smoketest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;

public class SessionIT {

  @ClassRule
  public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

  @Before
  public void setUp() {
    RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
    RestAssured.port = stack.opennms().getWebPort();
    RestAssured.basePath = "/opennms/";
  }

  @After
  public void tearDown() {
    RestAssured.reset();
  }

  @Test
  public void verifyNoCachingOfRequestWithSessionCookie() {
    given().get("login.jsp").then().assertThat()
        .statusCode(200)
        .header("Set-Cookie", containsString("JSESSIONID"))
        .header("Cache-Control", is("no-cache"));
  }

  @Test
  public void verifyNoCachingOfRequestWithSessionIdInUrl() {
    // if an internal page is called we get a 302 with the Location header that contains the sessionid which we don't want to cache
    String sessionId = given().redirects().follow(false)
        .log().all()
        .get("admin/classification/index.jsp")
        .getHeader("Set-Cookie").split("=")[1];

    RequestSpecification noEncoding = new RequestSpecBuilder().setUrlEncodingEnabled(false).build();
    given()
        .log().all()
        .spec(noEncoding)
        .get("login.jsp;jsessionid="+sessionId)
        .then().assertThat()
        .log().all()
        .statusCode(200)
        .header("Cache-Control", is("no-cache"));
  }
}
