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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class AngularLoginRedirectIT extends OpenNMSSeleniumIT {

    private static final int SLEEP_TIME = 2000;

    private static final Logger LOG = LoggerFactory.getLogger(AngularLoginRedirectIT.class);

    private static class Check {
        private String url;
        private Runnable verifyPageLoaded;
        private Runnable actionToPerform;
        private final Runnable actionToPerformAfterLogout;
        private Runnable verifyAfterLogout;

        public Check(String url, Runnable verifyPageLoaded, Runnable actionToPerform,
                     Runnable actionToPerformAfterLogout, Runnable verifyAfterLogout) {
            this.url = Objects.requireNonNull(url);
            this.verifyPageLoaded = Objects.requireNonNull(verifyPageLoaded);
            this.actionToPerform = Objects.requireNonNull(actionToPerform);
            this.actionToPerformAfterLogout = actionToPerformAfterLogout == null ? actionToPerform : actionToPerformAfterLogout;
            this.verifyAfterLogout = verifyAfterLogout;
        }
    }

    private List<Check> checks = Lists.newArrayList(
            new Check(
                    "admin/classification/index.jsp",
                    () -> pageContainsText("Classification rules defined by the user"),
                    () -> findElementById("action.refresh").click(),
                    null,
                    () -> validateRedirectedToLoginPage()),
            new Check(
                    "admin/ng-requisitions/index.jsp#/requisitions",
                    () -> pageContainsText("There are no requisitions"),
                    () -> {
                        sleep(SLEEP_TIME); // encounter for UI Delay
                        findElementById("refresh-requisitions").click();
                        sleep(SLEEP_TIME); // encounter for UI Delay
                        driver.findElement(By.xpath("//button[text()='Reload Everything']")).click();
                        sleep(SLEEP_TIME); // encounter for UI Delay
                        driver.findElement(By.xpath("//button[text()='OK']")).click();
                        sleep(SLEEP_TIME); // encounter for UI Delay
                    },
                    null,
                    () -> validateCannotRetrieveRequisitionsAfterLogout()),
            new Check(
                    "admin/geoservice/index.jsp",
                    () -> pageContainsText("Settings"),
                    () -> findElementByLink("Google").click(),
                    () -> findElementByLink("Settings").click(),
                    () -> validateGeoserviceLogout())
    );

    @Before
    public void before() {
        setImplicitWait(5, TimeUnit.SECONDS);
        logout();
    }

    @After
    public void after() {
        setImplicitWait();
    }

    @Test
    public void testAngularLogout() throws IOException {
        for (Check eachCheck : checks) {
            LOG.info("{}: Run test for page", eachCheck.url);

            // Go to Page
            login();
            driver.get(getBaseUrlInternal() + "opennms/" + eachCheck.url);

            // Verify Page loaded
            LOG.info("{}: Verify that page loaded", eachCheck.url);
            eachCheck.verifyPageLoaded.run();

            // Run action
            LOG.info("{}: Perform action", eachCheck.url);
            eachCheck.actionToPerform.run();

            // Wait before logging out as the http request may be still in progress
            // and may result in a pre-mature redirect, causing the test to fail
            sleep(SLEEP_TIME);

            // Logout (via HttpGet, so we are still on the page)
            LOG.info("{}: Simulate session timeout", eachCheck.url);
            simulateSessionTimeout(eachCheck.url);
            sleep(SLEEP_TIME);

            // Verify we are still on the page
            LOG.info("{}: Verify that page is still loaded", eachCheck.url);
            eachCheck.verifyPageLoaded.run();
            sleep(SLEEP_TIME);

            // Run action (again or an individual one), which should still pass
            LOG.info("{}: Perform action after logout.", eachCheck.url);
            try {
                eachCheck.actionToPerformAfterLogout.run();
            } catch (Exception e) {
                // Sometimes we get logged out directly so the actionToPerform might fail.
                // This is fine as long as the logout itself happened which we test below.
            }
            sleep(SLEEP_TIME);

            LOG.info("| Url before calling verifyAfterLogout: {}", driver.getCurrentUrl());

            eachCheck.verifyAfterLogout.run();

            LOG.info("{}: Test passed", eachCheck.url);
        }
    }

    private void validateRedirectedToLoginPage() {
        final String loginRegex = "http://(opennms|localhost):\\d+/opennms/?(login\\.jsp)?";

        // Verify we have been forwarded to the login page
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(input -> {
                LOG.info("Verify redirect to login.jsp occurred");
                LOG.info("| Current Url: {}", driver.getCurrentUrl());

                return driver.getCurrentUrl().matches(loginRegex);
            }
        );
    }

    private void validateCannotRetrieveRequisitionsAfterLogout() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'growl-message ng-binding') and contains(text(), 'Cannot retrieve the requisitions.')]")));
    }

    private void validateGeoserviceLogout() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'alert-danger')]/span[contains(text(), 'An unexpected error occurred: Unauthorized')]")));
    }

    private void simulateSessionTimeout(final String url) throws IOException {
        final Set<Cookie> cookies = driver.manage().getCookies();

        for (Cookie eachCookie : cookies) {
            LOG.info("{}: simulateSessionTimeout handling cookie: {}={}", url, eachCookie.getName(), eachCookie.getValue());

            if (eachCookie.getName().equalsIgnoreCase("JSESSIONID")) {
                LOG.info("{}: simulateSessionTimeout found JSESSIONID, attempting to log out", url);

                final HttpPost httpPost = new HttpPost(getBaseUrlExternal() + "opennms/j_spring_security_logout");
                httpPost.addHeader("Cookie", eachCookie.getName() + "=" + eachCookie.getValue());

                try (CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling().build();
                     CloseableHttpResponse response = client.execute(httpPost)
                ) {
                    assertEquals(302, response.getStatusLine().getStatusCode());
                    LOG.info("{}: simulateSessionTimeout successfully logged out, redirected to {}", url, response.getFirstHeader("Location"));
                }
            }
        }

        driver.manage().deleteAllCookies();
    }
}
