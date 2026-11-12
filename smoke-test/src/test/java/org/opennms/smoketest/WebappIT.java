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
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

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
    // if an internal page is called we get a 302 with the Location header that contains the sessionid which we don't want to cache.
    // Accept text/html like a browser: only a page navigation is saved for after login, and only then is a session created (NMS-20178).
    String sessionId = given().redirects().follow(false)
        .log().all()
        .header("Accept", "text/html")
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
   * NMS-20178: the static files of the Vue SPA and of the Vue menu bundle are for logged-in
   * users only. Without login, the server sends a plain, uncacheable 401: no redirect to
   * login.jsp, because rendering the login page for an asset creates a session, which can
   * replace the session of a login form that is open at the same time; and nothing that
   * Safari or Firefox can cache under the asset URL (NMS-20174). No WWW-Authenticate header,
   * so the browser shows no login dialog.
   */
  @Test
  public void verifyVueAssetsRequireAuthenticationWithPlain401() {
    for (String asset : Arrays.asList("ui/assets/index.js", "ui-components/assets/index.js", "ui-components/assets/index.css")) {
      given().redirects().follow(false)
          .get(asset)
          .then().assertThat()
          .statusCode(401)
          .header("Cache-Control", containsString("no-store"))
          .header("WWW-Authenticate", nullValue())
          .header("Set-Cookie", nullValue())
          .header("Location", nullValue());
    }
  }

  /**
   * NMS-20178: the pages open to anonymous users (login.jsp, logoff.jsp, accessDenied.jsp, error
   * pages) load the menu bundle's base styles from this one anonymous file.
   */
  @Test
  public void verifyAnonymousPagesCssIsAnonymouslyAccessible() {
    given().redirects().follow(false)
        .get("ui-components/assets/anonymous-pages.css")
        .then().assertThat()
        .statusCode(200)
        .header("Content-Type", containsString("css"))
        .header("Set-Cookie", nullValue());
  }

  @Test
  public void verifyMenuBundleJsAssetIsAccessibleWhenAuthenticated() {
    given().redirects().follow(false)
        .auth().preemptive().basic(AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME, AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD)
        .get("ui-components/assets/index.js")
        .then().assertThat()
        .statusCode(200)
        .header("Content-Type", containsString("javascript"));
  }

  @Test
  public void verifyMenuBundleCssAssetIsAccessibleWhenAuthenticated() {
    given().redirects().follow(false)
        .auth().preemptive().basic(AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME, AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD)
        .get("ui-components/assets/index.css")
        .then().assertThat()
        .statusCode(200)
        .header("Content-Type", containsString("css"));
  }

  /**
   * NMS-20180: the dist-menu artifact also ships an index.html (the Vite build
   * input) at /ui-components/, which nothing links to. Like any other page, it
   * requires authentication and redirects to the login page.
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
   * NMS-18978, NMS-20178: the login page never mounts the Vue menu, and without
   * login the menu bundle files get a 401, so the login page must not reference
   * them at all (bootstrap.jsp loads the menu bundle only for logged-in users).
   * It links the anonymous base styles (anonymous-pages.css) instead.
   * In particular it must never execute the bundle (NMS-20174): an
   * unauthenticated menu run fires REST calls that pollute the post-login
   * saved request.
   */
  @Test
  public void verifyLoginPageDoesNotReferenceMenuBundle() {
    final String body = given()
        .get("login.jsp")
        .then().assertThat()
        .statusCode(200)
        .extract().response().body().asString();

    // Positive controls: this really is the rendered login page, and it links
    // the anonymous base styles from the same directory, so the negative
    // assertions below can't pass vacuously on an error page or if the asset
    // URLs change shape.
    assertTrue("expected login.jsp to render the login form. Body: " + body,
        body.contains("id=\"loginForm\""));
    assertTrue("expected login.jsp to link the anonymous base styles. Body: " + body,
        body.contains("ui-components/assets/anonymous-pages.css"));
    assertFalse("login.jsp must not reference the menu bundle JavaScript. Body: " + body,
        body.contains("ui-components/assets/index.js"));
    assertFalse("login.jsp must not reference the menu bundle stylesheet. Body: " + body,
        body.contains("ui-components/assets/index.css"));
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
