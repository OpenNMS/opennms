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
import java.util.concurrent.TimeUnit;

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
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.thoughtworks.selenium.SeleniumException;

public class OpenNMSSeleniumTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSSeleniumTestCase.class);

    public static final long   LOAD_TIMEOUT       = Long.getLong("org.opennms.smoketest.web-timeout", 120000l);
    public static final String OPENNMS_WEB_HOST   = System.getProperty("org.opennms.smoketest.web-host", "localhost");
    public static final int    OPENNMS_WEB_PORT   = Integer.getInteger("org.opennms.smoketest.web-port", 8980);
    public static final String OPENNMS_EVENT_HOST = System.getProperty("org.opennms.smoketest.event-host", OPENNMS_WEB_HOST);
    public static final int    OPENNMS_EVENT_PORT = Integer.getInteger("org.opennms.smoketest.event-port", 5817);

    public static final String BASE_URL           = "http://" + OPENNMS_WEB_HOST + ":" + OPENNMS_WEB_PORT + "/";
    public static final String REQUISITION_NAME   = "SeleniumTestGroup";
    public static final String USER_NAME          = "SmokeTestUser";
    public static final String GROUP_NAME         = "SmokeTestGroup";

    protected static final boolean usePhantomJS = Boolean.getBoolean("org.opennms.smoketest.webdriver.use-phantomjs") || Boolean.getBoolean("smoketest.usePhantomJS");

    protected WebDriver m_driver = null;
    protected WebDriverWait wait = null;

    @Rule
    public TestWatcher m_watcher = new TestWatcher() {
        @Override
        protected void starting(final Description description) {
            try {
                m_driver = getDriver();
                LOG.debug("Using driver: {}", m_driver);
                m_driver.manage().timeouts().implicitlyWait(LOAD_TIMEOUT, TimeUnit.MILLISECONDS);
                wait = new WebDriverWait(m_driver, TimeUnit.SECONDS.convert(LOAD_TIMEOUT, TimeUnit.MILLISECONDS));

                m_driver.get(BASE_URL + "opennms/login.jsp");
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("j_username")));
                enterText(By.name("j_username"), "admin");
                enterText(By.name("j_password"), "admin");
                findElementByName("Login").click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='content']")));
            } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                LOG.debug("Failed to get driver", e);
                throw new RuntimeException("Tests aren't going to work.  Bailing.");
            }
        }

        @Override
        protected void failed(final Throwable e, final Description description) {
            final String testName = description.getMethodName();
            LOG.debug("Test {} failed... attempting to take screenshot.", testName);
            if (m_driver != null && m_driver instanceof TakesScreenshot) {
                final TakesScreenshot shot = (TakesScreenshot)m_driver;
                try {
                    final File from = shot.getScreenshotAs(OutputType.FILE);
                    final String screenshotFileName = "target" + File.separator + "screenshots" + File.separator + description.getClassName() + "." + testName + ".png";
                    final File to = new File(screenshotFileName);
                    LOG.debug("Screenshot saved to: {}", from);
                    try {
                        to.getParentFile().mkdirs();
                        Files.move(from, to);
                        LOG.debug("Screenshot moved to: {}", to);
                    } catch (final IOException ioe) {
                        LOG.debug("Failed to move screenshot from {} to {}", from, to, ioe);
                    }
                } catch (final Exception sse) {
                    LOG.debug("Failed to take screenshot.", sse);
                }
            } else {
                LOG.debug("Driver can't take screenshots.");
            }
            LOG.debug("Current URL: {}", m_driver.getCurrentUrl());
            m_driver.navigate().back();
            LOG.debug("Previous URL: {}", m_driver.getCurrentUrl());
        }

        @Override
        protected void finished(final Description description) {
            try {
                deleteTestRequisition();
                deleteTestUser();
                deleteTestGroup();
            } catch (final Exception e) {
                LOG.error("Cleaning up failed. Future tests will be in an unhandled state.", e);
            }

            LOG.debug("Shutting down Selenium.");
            if (m_driver != null) {
                try {
                    m_driver.get(BASE_URL + "opennms/j_spring_security_logout");
                } catch (final SeleniumException e) {
                    // don't worry about it, this is just for logging out
                }
                try {
                    m_driver.quit();
                } catch (final Exception e) {
                    LOG.error("Failed while shutting down WebDriver for test {}.", description.getMethodName(), e);
                }
                m_driver = null;
            }

            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
            }
        }
    };

    @BeforeClass
    public static void configureLogging() throws Exception {
        final String logLevel = System.getProperty("org.opennms.smoketest.logLevel", "DEBUG");
        final Logger logger = org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        if (logger instanceof ch.qos.logback.classic.Logger) {
            final ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
            logbackLogger.setLevel(ch.qos.logback.classic.Level.valueOf(logLevel));
        }
    }

    protected WebDriver getDriver() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        WebDriver driver = null;
        final String driverClass = System.getProperty("org.opennms.smoketest.webdriver.class", System.getProperty("webdriver.class"));
        if (driverClass != null) {
            driver = (WebDriver)Class.forName(driverClass).newInstance();
        }

        // otherwise, PhantomJS if found, or fall back to Firefox
        if (driver == null) {
            if (usePhantomJS) {
                final File phantomJS = findPhantomJS();
                if (phantomJS != null) {
                    final DesiredCapabilities caps = new DesiredCapabilities();
                    caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomJS.toString());
                    driver = new PhantomJSDriver(caps);
                }
            }
            if (driver == null) {
                driver = new FirefoxDriver();
            }
        }
        return driver;
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

    protected WebDriverWait waitFor(final long seconds) {
        return new WebDriverWait(m_driver, seconds);
    }

    protected ExpectedCondition<Boolean> pageContainsText(final String text) {
        final String escapedText = text.replace("\'", "\\\'");
        return new ExpectedCondition<Boolean>() {
            @Override public Boolean apply(final WebDriver driver) {
                final String xpathExpression = "//*[contains(., '" + escapedText + "')]";
                LOG.debug("XPath expression: {}", xpathExpression);
                final WebElement element = driver.findElement(By.xpath(xpathExpression));
                return element != null;
            }
        };
    }

    protected String handleAlert() {
        try {
            final Alert alert = m_driver.switchTo().alert();
            final String alertText = alert.getText();
            alert.dismiss();
            return alertText;
        } catch (final NoAlertPresentException e) {
            LOG.debug("handleAlert: no alert is active");
        }
        return null;
    }

    protected void setChecked(final By by) {
        final WebElement element = m_driver.findElement(by);
        if (element.isSelected()) {
            return;
        } else {
            element.click();
        }
    }

    protected void setUnchecked(final By by) {
        final WebElement element = m_driver.findElement(by);
        if (element.isSelected()) {
            element.click();
        } else {
            return;
        }
    }

    protected void clickMenuItem(final String menuItemText, final String submenuItemText, final String submenuItemHref) {
        final Actions action = new Actions(m_driver);

        final WebElement menuElement;
        if (menuItemText.startsWith("name=")) {
            final String menuItemName = menuItemText.replaceFirst("name=", "");
            menuElement = findElementByName(menuItemName);
        } else {
            menuElement = findElementByXpath("//a[contains(text(), '" + menuItemText + "')]");
        }
        action.moveToElement(menuElement, 2, 2).perform();

        final WebElement submenuElement;
        if (submenuItemText != null) {
            if (submenuItemHref == null) {
                submenuElement = findElementByXpath("//a[contains(text(), '" + submenuItemText + "')]");
            } else {
                submenuElement = findElementByXpath("//a[@href='" + submenuItemHref + "' and contains(text(), '" + submenuItemText + "')]");
            }
        } else {
            submenuElement = null;
        }

        if (submenuElement == null) {
            // no submenu given, just click the main element
            // wait until the element is visible, not just present in the DOM
            wait.until(ExpectedConditions.visibilityOf(menuElement));
            menuElement.click();
        } else {
            // we want a submenu item, click it instead
            // wait until the element is visible, not just present in the DOM
            wait.until(ExpectedConditions.visibilityOf(submenuElement));
            submenuElement.click();
        }
    }

    protected void frontPage() {
        m_driver.get(BASE_URL + "opennms/");
        m_driver.findElement(By.id("index-contentleft"));
    }

    public void adminPage() {
        m_driver.get(BASE_URL + "opennms/admin/index.jsp");
    }

    protected void nodePage() {
        m_driver.get(BASE_URL + "opennms/element/nodeList.htm");
    }

    protected void notificationsPage() {
        m_driver.get(BASE_URL + "opennms/notification/index.jsp");
    }

    protected void outagePage() {
        m_driver.get(BASE_URL + "opennms/outage/index.jsp");
    }

    protected void provisioningPage() {
        m_driver.get(BASE_URL + "opennms/admin/index.jsp");
        m_driver.findElement(By.linkText("Manage Provisioning Requisitions")).click();
    }

    protected void reportsPage() {
        m_driver.get(BASE_URL + "opennms/report/index.jsp");
    }

    protected void searchPage() {
        m_driver.get(BASE_URL + "opennms/element/index.jsp");
    }

    protected void supportPage() {
        m_driver.get(BASE_URL + "opennms/support/index.htm");
    }

    protected void goBack() {
        LOG.warn("goBack() is supposedly not supported on Safari!");
        m_driver.navigate().back();
    }

    public WebElement findElementById(final String id) {
        return m_driver.findElement(By.id(id));
    }

    public WebElement findElementByLink(final String link) {
        return m_driver.findElement(By.linkText(link));
    }

    public WebElement findElementByName(final String name) {
        return m_driver.findElement(By.name(name));
    }

    public WebElement findElementByXpath(final String xpath) {
        return m_driver.findElement(By.xpath(xpath));
    }

    protected WebElement enterText(final By selector, final String text) {
        LOG.debug("Enter text: '{}' into selector: {}", text, selector);
        final WebElement element = m_driver.findElement(selector);
        element.clear();
        element.sendKeys(text);
        return element;
    }

    private Integer doRequest(final HttpRequestBase request) throws ClientProtocolException, IOException, InterruptedException {
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

        final ResponseHandler<Integer> responseHandler = new ResponseHandler<Integer>() {
            @Override
            public Integer handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                try {
                    final int status = response.getStatusLine().getStatusCode();
                    // 400 because we return that if you try to delete something that is already deleted
                    // 404 because it's OK if it's already not there
                    if (status >= 200 && status < 300 || status == 400 || status == 404) {
                        EntityUtils.consume(response.getEntity());
                        return status;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                } finally {
                    waitForCompletion.countDown();
                }
            }
        };

        final Integer status = client.execute(targetHost, request, responseHandler, context);

        waitForCompletion.await();
        client.close();
        return status;
    }

    public void deleteExistingRequisition(final String foreignSource) {
        provisioningPage();

        LOG.debug("deleteExistingRequisition: Deleting Requisition: {}", foreignSource);
        if (getForeignSourceElement(foreignSource) == null) {
            LOG.debug("deleteExistingRequisition: Requisition {} is already gone.", foreignSource);
            return;
        }

        do {
            long nodesInRequisition = -1;
            long nodesInDatabase = -1;

            try {
                m_driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

                final WebElement foreignSourceElement = getForeignSourceElement(foreignSource);

                nodesInRequisition = getNodesInRequisition(foreignSourceElement);
                nodesInDatabase = getNodesInDatabase(foreignSourceElement);

                LOG.debug("deleteExistingRequisition: nodesInRequisition={}, nodesInDatabase={}", nodesInRequisition, nodesInDatabase);

                if (nodesInDatabase > 0) {
                    if (nodesInRequisition > 0) {
                        LOG.debug("deleteExistingRequisition: We have requisitioned nodes, deleting them.");
                        try {
                            final WebElement deleteNodesButton = foreignSourceElement.findElement(By.xpath("//input[@type='button' and @value='Delete Nodes']"));
                            deleteNodesButton.click();
                            wait.until(ExpectedConditions.alertIsPresent()).accept();
                        } catch (final NoSuchElementException e) {
                        }
                    } else {
                        LOG.debug("deleteExistingRequisition: We have no requisitioned nodes, but there are nodes in the database. Synchronizing.");
                        final WebElement synchronizeButton = foreignSourceElement.findElement(By.xpath("//input[@type='button' and @value='Synchronize']"));
                        synchronizeButton.click();
                        wait.until(new WaitForNodesInDatabase(0));
                    }
                } else {
                    // no nodes in the database
                    try {
                        LOG.debug("deleteExistingRequisition: We have no nodes in the database, time to delete the requisition. PUSH THE BUTTON FRANK.");
                        final WebElement deleteRequisitionButton = foreignSourceElement.findElement(By.xpath("//input[@type='button' and @value='Delete Requisition']"));
                        deleteRequisitionButton.click();
                    } catch (final NoSuchElementException e) {
                    }
                }
            } finally {
                m_driver.manage().timeouts().implicitlyWait(LOAD_TIMEOUT, TimeUnit.MILLISECONDS);
            }
        } while (getForeignSourceElement(foreignSource) != null);
    }

    protected WebElement getForeignSourceElement(final String requisitionName) {
        final String selector = "//span[@data-foreignSource='" + requisitionName + "']";
        WebElement foreignSourceElement = null;
        try {
            m_driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
            foreignSourceElement = m_driver.findElement(By.xpath(selector));
        } catch (final NoSuchElementException e) {
            // no match, treat as a no-op
            LOG.debug("Could not find: {}", selector);
            return null;
        } finally {
            m_driver.manage().timeouts().implicitlyWait(LOAD_TIMEOUT, TimeUnit.MILLISECONDS);
        }
        return foreignSourceElement;
    }

    protected void deleteTestRequisition() throws Exception {
        final Integer responseCode = doRequest(new HttpGet(BASE_URL + "/opennms/rest/requisitions/" + REQUISITION_NAME));
        LOG.debug("Checking for existing test requisition: {}", responseCode);
        if (responseCode == 404 || responseCode == 204) {
            LOG.debug("deleteTestRequisition: already deleted");
            return;
        }

        deleteExistingRequisition(REQUISITION_NAME);
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

    protected long getNodesInRequisition(final WebElement element) {
        try {
            final WebElement match = element.findElement(By.xpath("//span[@data-requisitionedNodes]"));
            final String nodes = match.getAttribute("data-requisitionedNodes");
            if (nodes != null) {
                final Long nodeCount = Long.valueOf(nodes);
                LOG.debug("{} requisitioned nodes found.", nodeCount);
                return nodeCount;
            }
        } catch (final NoSuchElementException e) {
        }
        LOG.debug("0 requisitioned nodes found.");
        return 0;
    }

    protected long getNodesInDatabase(final WebElement element) {
        try {
            final WebElement match = element.findElement(By.xpath("//span[@data-databaseNodes]"));
            final String nodes = match.getAttribute("data-databaseNodes");
            if (nodes != null) {
                final Long nodeCount = Long.valueOf(nodes);
                LOG.debug("{} database nodes found.", nodeCount);
                return nodeCount;
            }
        } catch (final NoSuchElementException e) {
        }
        LOG.debug("0 database nodes found.");
        return 0;
    }

    protected final class WaitForNodesInDatabase implements ExpectedCondition<Boolean> {
        private final int m_numberToMatch;
        public WaitForNodesInDatabase(int numberOfNodes) {
            m_numberToMatch = numberOfNodes;
        }

        @Override public Boolean apply(final WebDriver input) {
            provisioningPage();
            final long nodes = getNodesInDatabase(getForeignSourceElement(REQUISITION_NAME));
            if (nodes == m_numberToMatch) {
                return true;
            } else {
                return null;
            }
        }
    }

}
