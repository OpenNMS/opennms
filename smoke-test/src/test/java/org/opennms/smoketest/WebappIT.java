package org.opennms.smoketest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.util.Arrays;
import org.apache.http.client.ClientProtocolException;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper;
import org.opennms.smoketest.stacks.OpenNMSStack;

/** Contains cross concern  */
public class WebappIT {

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

  /**
   * Verifies that each of the known keys in the output contains some value.
   *
   * See NMS-9103.
   */
  @Test
  public void canRetrieveProductInfo() throws ClientProtocolException, IOException, InterruptedException {
    // Retrieve the info summary
    final String json  = given()
        .auth().basic(AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME, AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD)
        .get("rest/info")
        .then().statusCode(200)
        .extract().response().body().print();

    // The expected payload looks like:
    //  {"packageDescription":"OpenNMS","displayVersion":"25.0.0-SNAPSHOT","packageName":"opennms","version":"25.0.0", "ticketerConfig":{"enabled":false, "plugin": null}}
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode infoObject = mapper.readTree(json);

    // Verify that some value is present for each of the known keys
    for (String key : Arrays.asList("packageDescription", "displayVersion", "packageName", "version")) {
      assertTrue(String.format("Expected value for key '%s', but none was found. Info returned: %s", key, json),
          !Strings.isNullOrEmpty(infoObject.get(key).asText()));
    }
    assertNotNull(infoObject.get("ticketerConfig"));
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
