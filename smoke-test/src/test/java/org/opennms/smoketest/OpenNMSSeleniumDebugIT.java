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

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper;
import org.opennms.smoketest.utils.DevDebugUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * This class is used to help debug and develop Selenium based tests.
 *
 * To use it, run the main() method in one session, and update your test to
 * extend this class temporarily.
 *
 * In order to test against a local environment (one that is already setup on the host running the tests)
 * use the constructor that only specific the web driver URL.
 *
 * In order to test against an existing environments created in the containers, set both the
 * web driver URL and the web URL.
 *
 * @author jwhite
 */
public class OpenNMSSeleniumDebugIT extends AbstractOpenNMSSeleniumHelper {

    private final String opennmsWebUrl;
    public final RemoteWebDriver driver;

    public OpenNMSSeleniumDebugIT(String webDriverUrl) {
        this(webDriverUrl, "http://localhost:8980/");
    }

    public OpenNMSSeleniumDebugIT(String webDriverUrl, String opennmsWebUrl) {
        this.opennmsWebUrl = Objects.requireNonNull(opennmsWebUrl);
        Objects.requireNonNull(webDriverUrl);
        try {
            driver = new RemoteWebDriver(new URL(webDriverUrl), OpenNMSSeleniumIT.getFirefoxOptions());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WebDriver getDriver() {
        return driver;
    }

    @Override
    public String getBaseUrlInternal() {
        // In order to support local setups where Selenium is running in a container, but OpenNMS is running on the target
        // host, we need to alter the internal URL to something that the container can reach
        return DevDebugUtils.convertToContainerAccessibleUrl(opennmsWebUrl, OpenNMSContainer.ALIAS, OpenNMSContainer.OPENNMS_WEB_PORT);
    }

    @Override
    public String getBaseUrlExternal() {
        return opennmsWebUrl;
    }

    public static class DebugIT extends OpenNMSSeleniumIT {
        @Test
        public void canDebug() throws InterruptedException {
            System.out.printf("\n\nWeb driver is available at: %s\n", firefox.getSeleniumAddress());
            System.out.printf("OpenNMS is available at: %s\n", stack.opennms().getBaseUrlExternal());
            Thread.sleep(TimeUnit.HOURS.toMillis(8));
        }
    }

    public static void main(String... args) {
        JUnitCore.runClasses(DebugIT.class);
    }
}
