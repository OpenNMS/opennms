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
package org.opennms.smoketest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper;
import org.opennms.smoketest.stacks.OpenNMSStack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

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
    //  {"packageDescription":"OpenNMS","displayVersion":"XX.X.X-SNAPSHOT","packageName":"opennms","version":"XX.X.X", "ticketerConfig":{"enabled":false, "plugin": null}}
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
        .header("Set-Cookie", matchesPattern("^JSESSIONID=.*SameSite=Strict$"))
        .header("Cache-Control", is("no-store"))
        .header("Pragma", is("no-cache"));
  }

  @Test
  public void verifyNoCachingOfRequestWithSessionIdInUrl() {
    // if an internal page is called we get a 302 with the Location header that contains the sessionid which we don't want to cache
    String sessionId = given().redirects().follow(false)
        .log().all()
        .get("admin/classification/index.jsp")
        .getHeader("Set-Cookie").split("=")[1].split(";")[0];

    RequestSpecification noEncoding = new RequestSpecBuilder().setUrlEncodingEnabled(false).build();
    given()
        .log().all()
        .spec(noEncoding)
        .get("login.jsp;jsessionid="+sessionId)
        .then().assertThat()
        .log().all()
        .statusCode(200)
        .header("Cache-Control", is("no-store"))
        .header("Pragma", is("no-cache"));
  }

  @Test
  public void verifyNoCachingOnStartPage() {
    given().get("index.jsp").then().assertThat()
            .statusCode(200)
            .header("Cache-Control", is("no-store"))
            .header("Pragma", is("no-cache"));
  }

  @Test
  public void verifyCachingOnStaticAssets() {
    given().get("assets/vendor.min.js").then().assertThat()
            .statusCode(200)
            .header("Cache-Control", not("no-store"))
            .header("Pragma", not("no-cache"));
  }

  /**
   * NMS-20174: the Vue menu bundle must be accessible without authentication.
   * bootstrap.jsp preloads it from the (unauthenticated) login page; if that
   * request is answered with an auth redirect instead, Safari and Firefox cache
   * the text/html response under the asset URL and the menu never mounts on any
   * JSP page after login.
   */
  @Test
  public void verifyMenuBundleJsAssetIsAnonymouslyAccessible() {
    given().redirects().follow(false)
        .get("ui-components/assets/index.js")
        .then().assertThat()
        .statusCode(200)
        .header("Content-Type", containsString("javascript"));
  }

  @Test
  public void verifyMenuBundleCssAssetIsAnonymouslyAccessible() {
    given().redirects().follow(false)
        .get("ui-components/assets/index.css")
        .then().assertThat()
        .statusCode(200)
        .header("Content-Type", containsString("css"));
  }

  /**
   * NMS-20180: only the menu bundle's assets/ directory is anonymous. The
   * dist-menu artifact also ships an index.html (the Vite build input) at
   * /ui-components/, which nothing links to and which must keep requiring
   * authentication like any other page.
   */
  @Test
  public void verifyMenuIndexHtmlRequiresAuthentication() {
    given().redirects().follow(false)
        .get("ui-components/index.html")
        .then().assertThat()
        .statusCode(302)
        .header("Location", containsString("login.jsp"));
  }

  /**
   * NMS-20174: the login page references the menu bundle only as preload links
   * (deliberate cache warming for the first post-login page) and must never
   * execute it. An unauthenticated menu run fires REST calls that trigger the
   * browser's native basic-auth popup on the login page and pollute the
   * post-login saved request (the password gate's Skip button then redirects
   * to /rest, which browsers download as a file).
   */
  @Test
  public void verifyLoginPageDoesNotExecuteMenuBundle() {
    final String body = given()
        .get("login.jsp")
        .then().assertThat()
        .statusCode(200)
        .extract().response().body().asString();

    // Positive control: the page still references the bundle (as a preload) —
    // this keeps the negative assertion below meaningful if URLs change shape.
    assertTrue("expected login.jsp to preload the menu bundle. Body: " + body,
        body.contains("ui-components/assets/index.js"));
    assertFalse("login.jsp must not contain a script tag executing the menu bundle. Body: " + body,
        Pattern.compile("<script[^>]+ui-components/assets/index\\.js").matcher(body).find());
  }

  /**
   * The Vue UI's static assets may be cached but must be revalidated on each use,
   * because their file names are not content-hashed and change between releases.
   */
  @Test
  public void verifyRevalidationCachingOnVueUiStaticAssets() {
    for (String asset : Arrays.asList("ui/assets/index.js", "ui-components/assets/index.js")) {
      given()
              .auth().preemptive().basic(AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME, AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD)
              .get(asset)
              .then().assertThat()
              .statusCode(200)
              .header("Cache-Control", is("no-cache"))
              .header("Pragma", not("no-cache"));
    }
  }

}
