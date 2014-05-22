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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.selenium.SeleneseTestBase;
import com.thoughtworks.selenium.SeleniumException;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

public class OpenNMSSeleniumTestCase extends SeleneseTestBase {
    protected static final long LOAD_TIMEOUT = 60000;
    protected static final String BASE_URL = "http://localhost:8980/";
    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSSeleniumTestCase.class);
    private WebDriver m_driver = null;
    private static final boolean usePhantomJS = Boolean.getBoolean("smoketest.usePhantomJS");

    @Before
    public void setUp() throws Exception {
        final String logLevel = System.getProperty("org.opennms.smoketest.logLevel", "DEBUG");
        //ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        final Logger logger = org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        if (logger instanceof ch.qos.logback.classic.Logger) {
            final ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
            logbackLogger.setLevel(ch.qos.logback.classic.Level.valueOf(logLevel));
        }

        final String driverClass = System.getProperty("webdriver.class");
        if (driverClass != null) {
            m_driver = (WebDriver)Class.forName(driverClass).newInstance();
        }

        // otherwise, PhantomJS if found, or fall back to Firefox
        if (m_driver == null) {
            if (usePhantomJS) {
                final File phantomJS = findPhantomJS();
                if (phantomJS != null) {
                    final DesiredCapabilities caps = new DesiredCapabilities();
                    caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomJS.toString());
                    m_driver = new PhantomJSDriver(caps);
                }
            }
            if (m_driver == null) {
                m_driver = new FirefoxDriver();
            }
        }

        LOG.debug("Using driver: {}", m_driver);

        selenium = new WebDriverBackedSelenium(m_driver, BASE_URL);
        selenium.open("/opennms/login.jsp");
        selenium.type("name=j_username", "admin");
        selenium.type("name=j_password", "admin");
        selenium.click("name=Login");
        waitForPageToLoad();
    }

    private File findPhantomJS() {
        final String os = System.getProperty("os.name").toLowerCase();
        final String extension = (os.indexOf("win") >= 0)? ".exe" : "";

        final String path = System.getenv("PATH");
        if (path == null) {
            LOG.debug("findPhantomJS(): Unable to get PATH.");
            final File phantomFile = new File("/usr/local/bin/phantomjs" + extension);
            LOG.debug("findPhantomJS(): trying {}", phantomFile);
            if (phantomFile.exists() && phantomFile.canExecute()) {
                return phantomFile;
            }
        } else {
            final List<String> paths = new ArrayList<String>(Arrays.asList(path.split(File.pathSeparator)));
            paths.add("/usr/local/bin");
            paths.add("/usr/local/sbin");
            LOG.debug("findPhantomJS(): paths = {}", paths);
            for (final String directory : paths) {
                final File phantomFile = new File(directory + File.separator + "phantomjs" + extension);
                LOG.debug("findPhantomJS(): trying {}", phantomFile);
                if (phantomFile.exists() && phantomFile.canExecute()) {
                    return phantomFile;
                }
            }
        }
        return null;
    }

    @After
    public void shutDownSelenium() throws Exception {
        if (selenium != null) {
            try {
                if (selenium.isElementPresent("link=Log out")) selenium.click("link=Log out");
            } catch (final SeleniumException e) {
                // don't worry about it, this is just for logging out
            }
            selenium.stop();
            if (m_driver != null) {
                m_driver.quit();
            }
            Thread.sleep(3000);
        }
    }

    protected void frontPage() throws Exception {
        selenium.open("/opennms/");
        waitForPageToLoad();
    }

    protected void clickAndWait(final String pattern) {
        LOG.debug("clickAndWait({})", pattern);
        selenium.click(pattern);
        waitForPageToLoad();
    }

    protected void clickAndVerifyText(final String pattern, final String expectedText) {
        LOG.debug("clickAndVerifyText({}, {})", pattern, expectedText);
        clickAndWait(pattern);
        assertTrue("'" + expectedText + "' must exist in page text: " + selenium.getHtmlSource(), selenium.isTextPresent(expectedText));
    }

    protected void goBack() {
        LOG.warn("goBack() is not supported on Safari!");
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
        waitForText(expectedText, timeout, true);
    }

    protected void waitForText(final String expectedText, final long timeout, boolean failOnError) throws InterruptedException {
        LOG.debug("waitForText({}, {}, {})", new Object[] { expectedText, timeout, failOnError });
        if (!selenium.isTextPresent(expectedText)) {
            final long timeoutTime = System.currentTimeMillis() + timeout;
            while (!selenium.isTextPresent(expectedText) && System.currentTimeMillis() <= timeoutTime) {
                Thread.sleep(timeout / 10);
            }
        }
        try {
            assertTrue("Expected text not found: " + expectedText, selenium.isTextPresent(expectedText));
        } catch (final AssertionError e) {
            if (failOnError) {
                throw e;
            } else {
                LOG.error("Failed to find text {} after {} milliseconds.", expectedText, timeout);
                LOG.error("Page body was:\n{}", selenium.getBodyText());
            }
        }
    }

    protected void waitForHtmlSource(final String expectedText) throws InterruptedException {
        waitForHtmlSource(expectedText, LOAD_TIMEOUT);
    }

    protected void waitForHtmlSource(final String expectedText, final long timeout) throws InterruptedException {
        waitForHtmlSource(expectedText, timeout, true);
    }

    protected void waitForHtmlSource(final String expectedText, final long timeout, boolean failOnError) throws InterruptedException {
        LOG.debug("waitForHtmlSource({}, {}, {})", new Object[] { expectedText, timeout, failOnError });
        if (!selenium.getHtmlSource().contains(expectedText)) {
            final long timeoutTime = System.currentTimeMillis() + timeout;
            while (!selenium.getHtmlSource().contains(expectedText) && System.currentTimeMillis() <= timeoutTime) {
                Thread.sleep(timeout / 10);
            }
        }
        try {
            assertTrue("HTML source not found: " + expectedText, selenium.getHtmlSource().contains(expectedText));
        } catch (final AssertionError e) {
            if (failOnError) {
                throw e;
            } else {
                LOG.error("Failed to find text {} after {} milliseconds.", expectedText, timeout);
                LOG.error("Page body was:\n{}", selenium.getBodyText());
            }
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
            assertTrue("Element not found: " + specification, selenium.isElementPresent(specification));
        } catch (final AssertionError e) {
            throw e;
            //LOG.error("Failed to find element {} after {} milliseconds.", specification, timeout);
            //LOG.error("Page body was:\n{} selenium.getBodyText());
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
