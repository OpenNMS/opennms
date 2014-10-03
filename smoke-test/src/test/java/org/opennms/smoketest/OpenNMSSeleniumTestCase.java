/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
    protected static final Logger LOG = LoggerFactory.getLogger(OpenNMSSeleniumTestCase.class);
    protected static final long LOAD_TIMEOUT = 60000;
    protected static final String BASE_URL = "http://localhost:8980/";
    protected static final String REQUISITION_NAME = "SeleniumTestGroup";
    protected static final String USER_NAME = "SmokeTestUser";
    protected static final String GROUP_NAME = "SmokeTestGroup";

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
        // Change the timeout from the default of 30 seconds to 60 seconds
        // since we have to launch the browser and visit the front page of
        // the OpenNMS web UI in this amount of time and on the Bamboo
        // machines, 30 seconds is cutting it close. :)
        selenium.setTimeout("60000");
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

    protected void goToMainPage() {
        selenium.open("/opennms");
        waitForPageToLoad();
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
        LOG.debug("waitForText({}, {}, {})", expectedText, timeout, failOnError);
        final long timeoutTime = System.currentTimeMillis() + timeout;
        while (!selenium.isTextPresent(expectedText) && System.currentTimeMillis() <= timeoutTime) {
            Thread.sleep(timeout / 10);
        }
        try {
            assertTrue(String.format("Failed to find text %s after %d milliseconds", expectedText, timeout), selenium.isTextPresent(expectedText));
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
        LOG.debug("waitForHtmlSource({}, {}, {})", expectedText, timeout, failOnError);
        final long timeoutTime = System.currentTimeMillis() + timeout;
        while (!selenium.getHtmlSource().contains(expectedText) && System.currentTimeMillis() <= timeoutTime) {
            Thread.sleep(timeout / 10);
        }
        try {
            assertTrue(String.format("Failed to find text %s after %d milliseconds", expectedText, timeout), selenium.getHtmlSource().contains(expectedText));
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
        final long timeoutTime = System.currentTimeMillis() + timeout;
        while (!selenium.isElementPresent(specification) && System.currentTimeMillis() <= timeoutTime) {
            Thread.sleep(timeout / 10);
        }
        try {
            assertTrue(String.format("Failed to find element %s after %d milliseconds", specification, timeout), selenium.isElementPresent(specification));
        } catch (final AssertionError e) {
            throw e;
            //LOG.error("Failed to find element {} after {} milliseconds.", specification, timeout);
            //LOG.error("Page body was:\n{}", selenium.getBodyText());
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

    private void doRequest(final HttpRequestBase request) throws ClientProtocolException, IOException, InterruptedException {
        final CountDownLatch waitForCompletion = new CountDownLatch(1);

        final URI uri = request.getURI();
        final HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()), new UsernamePasswordCredentials("admin", "admin"));
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        final CloseableHttpClient client = HttpClients.createDefault();

        final ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            @Override
            public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                try {
                    final int status = response.getStatusLine().getStatusCode();
                    // 404 because it's OK if it's already not there
                    if (status >= 200 && status < 300 || status == 404) {
                        final HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                } finally {
                    waitForCompletion.countDown();
                }
            }
        };

        client.execute(targetHost, request, responseHandler, context);

        waitForCompletion.await();
        client.close();
    }

    protected void deleteTestRequisition() throws Exception {
        doRequest(new HttpDelete(BASE_URL + "/opennms/rest/requisitions/" + REQUISITION_NAME));
        doRequest(new HttpDelete(BASE_URL + "/opennms/rest/requisitions/deployed/" + REQUISITION_NAME));
        doRequest(new HttpGet(BASE_URL + "/opennms/rest/requisitions"));
    }

    protected void deleteTestUser() throws Exception {
        doRequest(new HttpDelete(BASE_URL + "/opennms/rest/users/" + USER_NAME));
    }
    
    protected void deleteTestGroup() throws Exception {
        doRequest(new HttpDelete(BASE_URL + "/opennms/rest/groups/" + GROUP_NAME));
    }
}
