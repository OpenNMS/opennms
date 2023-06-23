/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
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

        public Check(String url, Runnable verifyPageLoaded, Runnable actionToPerform) {
            this(url, verifyPageLoaded, actionToPerform, null);
        }

        public Check(String url, Runnable verifyPageLoaded, Runnable actionToPerform, Runnable actionToPerformAfterLogout) {
            this.url = Objects.requireNonNull(url);
            this.verifyPageLoaded = Objects.requireNonNull(verifyPageLoaded);
            this.actionToPerform = Objects.requireNonNull(actionToPerform);
            this.actionToPerformAfterLogout = actionToPerformAfterLogout == null? actionToPerform : actionToPerformAfterLogout;
        }
    }

    private List<Check> checks = Lists.newArrayList(
            new Check(
                    "admin/classification/index.jsp",
                    () -> pageContainsText("Classification rules defined by the user"),
                    () -> findElementById("action.refresh").click()),
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
                    }),
            new Check(
                    "admin/geoservice/index.jsp",
                    () -> pageContainsText("Settings"),
                    () -> findElementByLink("Google").click(),
                    () -> findElementByLink("Settings").click())
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
    public void verifyRedirectToLogin() throws IOException {
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
            LOG.info("{}: Perform action again. Should redirect to login page.", eachCheck.url);
            try {
                eachCheck.actionToPerformAfterLogout.run();
            } catch(Exception e) {
                // Sometimes we get logged out directly so the actionToPerform might fail.
                // This is fine as long as the logout itself happened which we test below.
            }
            sleep(SLEEP_TIME);

            // Verify we have been forwarded to the login page
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(input -> {
                    LOG.info("{}: Verify redirect to login.jsp occurred", eachCheck.url);
                    return driver.getCurrentUrl().matches("http://opennms:8980/opennms/login\\.jsp");
                }
            );
            LOG.info("{}: Test passed", eachCheck.url);
        }
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
