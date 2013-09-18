/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.opennms.core.test.MockLogAppender;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.selenium.SeleneseTestBase;
import com.thoughtworks.selenium.SeleniumException;

public class OpenNMSSeleniumTestCase extends SeleneseTestBase {
    protected static final Logger LOG = LoggerFactory.getLogger(OpenNMSSeleniumTestCase.class);
    protected static final long LOAD_TIMEOUT = 60000;

    @Before
    public void setUp() throws Exception {
        final String logLevel = System.getProperty("org.opennms.smoketest.logLevel", "DEBUG");
        MockLogAppender.setupLogging(true, logLevel);

        WebDriver driver = null;

        // Google Chrome if chrome driver property is set
        final String chromeDriverLocation = System.getProperty("webdriver.chrome.driver");
        if (chromeDriverLocation != null) {
            final File chromeDriverFile = new File(chromeDriverLocation);
            if (chromeDriverFile.exists() && chromeDriverFile.canExecute()) {
                System.err.println("using chrome driver");
                driver = new ChromeDriver();
            }
        }

        final String driverClass = System.getProperty("webdriver.class");
        if (driverClass != null) {
            driver = (WebDriver)Class.forName(driverClass).newInstance();
        }

        // otherwise, Firefox
        if (driver == null) {
            //final File phantomJS = new File("/usr/local/bin/phantomjs");
            //if (phantomJS.exists()) {
            //    final DesiredCapabilities caps = new DesiredCapabilities();
            //    caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "/usr/local/bin/phantomjs");
            //    driver = new PhantomJSDriver(caps);
            //} else {
                driver = new FirefoxDriver();
            //}
        }

        String baseUrl = "http://localhost:8980/";
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        selenium.open("/opennms/login.jsp");
        selenium.type("name=j_username", "admin");
        selenium.type("name=j_password", "admin");
        selenium.click("name=Login");
        waitForPageToLoad();
    }

    @After
    public void tearDown() throws Exception {
        if (selenium != null) {
            try {
                if (selenium.isElementPresent("link=Log out")) selenium.click("link=Log out");
            } catch (final SeleniumException e) {
                // don't worry about it, this is just for logging out
            }
            selenium.stop();
        }
    }

    protected void clickAndWait(final String pattern) {
        selenium.click(pattern);
        waitForPageToLoad();
    }

    protected void clickAndVerifyText(final String pattern, final String expectedText) {
        clickAndWait(pattern);
        assertTrue("'" + expectedText + " must exist in page", selenium.isTextPresent(expectedText));
    }

    protected void goBack() {
        selenium.goBack();
        waitForPageToLoad();
    }

    protected void waitForPageToLoad() {
        selenium.waitForPageToLoad(String.valueOf(LOAD_TIMEOUT));
    }

    protected void waitForText(final String expectedText) throws InterruptedException {
        waitForText(expectedText, LOAD_TIMEOUT);
    }

    protected void waitForText(final String expectedText, final long timeout) throws InterruptedException {
        if (!selenium.isTextPresent(expectedText)) {
            final long timeoutTime = System.currentTimeMillis() + timeout;
            while (!selenium.isTextPresent(expectedText) && System.currentTimeMillis() <= timeoutTime) {
                Thread.sleep(timeout / 10);
            }
        }
        try {
            assertTrue(selenium.isTextPresent(expectedText));
        } catch (final AssertionError e) {
            LOG.error("Failed to find text {} after {} milliseconds.", expectedText, timeout);
            LOG.error("Page body was:\n{}", selenium.getBodyText());
        }
    }

    protected void waitForElement(final String specification) throws InterruptedException {
        waitForElement(specification, LOAD_TIMEOUT);
    }

    protected void waitForElement(final String specification, final long timeout) throws InterruptedException {
        if (!selenium.isElementPresent(specification)) {
            final long timeoutTime = System.currentTimeMillis() + timeout;
            while (!selenium.isElementPresent(specification) && System.currentTimeMillis() <= timeoutTime) {
                Thread.sleep(timeout / 10);
            }
        }
        try {
            assertTrue(selenium.isElementPresent(specification));
        } catch (final AssertionError e) {
            LOG.error("Failed to find element {} after {} milliseconds.", specification, timeout);
            LOG.error("Page body was:\n{}", selenium.getBodyText());
        }
    }


    protected void handleVaadinErrorButtons() throws InterruptedException {
        if (selenium.isAlertPresent()) {
            selenium.keyPressNative("10");
            Thread.sleep(1000);
        }
        if (selenium.isElementPresent("//input[@type='button' and @value='OK']")) {
            selenium.click("//input[@type='button' and @value='OK']");
        } else if (selenium.isElementPresent("//button[contains(text(), 'OK')]")) {
            selenium.click("//button[contains(text(), 'OK')]");
        }
    }

}
