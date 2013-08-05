/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.thoughtworks.selenium.SeleneseTestBase;

public class OpenNMSSeleniumTestCase extends SeleneseTestBase {
    protected static final long LOAD_TIMEOUT = 60000;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");

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
            driver = new FirefoxDriver();
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
        if (selenium.isTextPresent("Log out")) clickAndWait("link=Log out");
        if (selenium != null) selenium.stop();
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
    
    protected void waitForText(final String expectedText, final long timeout) throws InterruptedException {
        final long timeoutTime = System.currentTimeMillis() + timeout;
        while (!selenium.isTextPresent(expectedText) && System.currentTimeMillis() <= timeoutTime) {
            Thread.sleep(timeout / 10);
        }
        assertTrue(selenium.isTextPresent(expectedText));
    }

    @Test
    public void testHeaderMenuLinks() throws Exception {
        clickAndWait("link=Node List");
        clickAndVerifyText("link=Search", "Search for Nodes");
        clickAndVerifyText("link=Outages", "Outage Menu");
        clickAndVerifyText("link=Path Outages", "All path outages");
        clickAndWait("link=Dashboard");
        waitForText("Surveillance View:", LOAD_TIMEOUT);
        clickAndVerifyText("link=Events", "Event Queries");
        clickAndVerifyText("link=Alarms", "Alarm Queries");
        clickAndVerifyText("link=Notifications", "Notification queries");
        clickAndVerifyText("link=Assets", "Search Asset Information");
        clickAndVerifyText("link=Reports", "Resource Graphs");
        clickAndVerifyText("link=Charts", "/ Charts");
        clickAndWait("link=Surveillance");
        waitForText("Surveillance View:", LOAD_TIMEOUT);
        clickAndWait("link=Distributed Status");
        assertTrue(selenium.isTextPresent("Distributed Poller Status Summary") || selenium.isTextPresent("No applications have been defined for this system"));
        clickAndVerifyText("//a[@href='maps.htm']", "OpenNMS Maps");
        clickAndVerifyText("//div[@id='content']//a[contains(text(), 'Distributed')]", "clear selected tags");
        goBack();

        // the vaadin apps are finicky
        clickAndWait("//div[@id='content']//a[contains(text(), 'Topology')]");
        Thread.sleep(1000);
        assertTrue(selenium.getHtmlSource().contains("vaadin.initApplication(\"opennmstopology"));
        handleVaadinErrorButtons();
        goBack();
        goBack();

        clickAndVerifyText("//a[@href='maps.htm']", "OpenNMS Maps");
        clickAndWait("//div[@id='content']//a[contains(text(), 'Geographical')]");
        Thread.sleep(1000);
        assertTrue(selenium.getHtmlSource().contains("vaadin.initApplication(\"opennmsnodemaps"));
        handleVaadinErrorButtons();

        clickAndVerifyText("//a[@href='maps.htm']", "OpenNMS Maps");
        clickAndWait("//div[@id='content']//a[contains(text(), 'SVG')]");
        waitForText("/ Network Topology Maps", LOAD_TIMEOUT);

        clickAndVerifyText("link=Add Node", "Community String:");
        clickAndVerifyText("link=Admin", "Configure Users, Groups and On-Call Roles");
        clickAndVerifyText("link=Support", "Enter your OpenNMS Group commercial support login");
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
