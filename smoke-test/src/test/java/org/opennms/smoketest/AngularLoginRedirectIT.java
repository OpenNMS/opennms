/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class AngularLoginRedirectIT extends OpenNMSSeleniumTestCase {

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
            this.actionToPerformAfterLogout = actionToPerformAfterLogout;
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
                        findElementById("refresh-requisitions").click();
                        sleep(2000); // encounter for UI Delay
                        m_driver.findElement(By.xpath("//button[@data-bb-handler='reloadAll']")).click();
                        sleep(2000); // encounter for UI Delay
                        m_driver.findElement(By.xpath("//button[@data-bb-handler='confirm']")).click();
                        sleep(2000); // encounter for UI Delay
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
            // Go to Page
            login();
            m_driver.get(getBaseUrl() + "opennms/" + eachCheck.url);

            // Verify Page loaded
            eachCheck.verifyPageLoaded.run();

            // Run action
            eachCheck.actionToPerform.run();

            // Wait before logging out as the http request may be still in progress
            // and may result in a pre-mature redirect, causing the test to fail
            sleep(2000);

            // Logout (via HttpGet, so we are still on the page)
            simulateSessionTimeout();
            sleep(2000);

            // Verify we are still on the page
            eachCheck.verifyPageLoaded.run();

            // Run action (again or an individual one), which should still pass
            if (eachCheck.actionToPerformAfterLogout != null) {
                eachCheck.actionToPerformAfterLogout.run();
            } else {
                eachCheck.actionToPerform.run();
            }

            // Verify we have been forwarded to the login page
            new WebDriverWait(m_driver, 5).until(
                    (Predicate<WebDriver>) input -> Objects.equals(getBaseUrl() + "opennms/login.jsp?session_expired=true", m_driver.getCurrentUrl())
            );
        }
    }

    private void simulateSessionTimeout() throws IOException {
        final HttpGet httpGet = new HttpGet(getBaseUrl() + "opennms/j_spring_security_logout");
        httpGet.addHeader("Cookie", "JSESSIONID="+m_driver.manage().getCookieNamed("JSESSIONID").getValue());

        try (CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling().build();
             CloseableHttpResponse response = client.execute(httpGet)
        ) {
            assertEquals(302, response.getStatusLine().getStatusCode());
        }
    }
}
