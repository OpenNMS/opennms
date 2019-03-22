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
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.containers.PostgreSQLContainer;
import org.opennms.smoketest.utils.org.opennms.smoketest.selenium.OpenNMSWebDriver;
import org.opennms.smoketest.utils.org.opennms.smoketest.selenium.SeleniumMatchers;
import org.openqa.selenium.By;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.Network;

/**
 * Verify that we can login to the web application
 * and render the about page.
 */
public class AboutPageIT {

    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static final OpenNMSContainer opennmsContainer = new OpenNMSContainer();

    private static final BrowserWebDriverContainer firefox = (BrowserWebDriverContainer) new BrowserWebDriverContainer()
            .withCapabilities(new FirefoxOptions())
            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, new File("target"))
            .withNetwork(Network.SHARED);

    @ClassRule
    public static TestRule chain = RuleChain
            .outerRule(postgreSQLContainer)
            .around(opennmsContainer)
            .around(firefox);

    private RemoteWebDriver driver;

    @Before
    public void setUp() {
        driver = firefox.getWebDriver();
        OpenNMSWebDriver onms = new OpenNMSWebDriver(opennmsContainer, driver);

        // Login
        onms.login();

        // Navigate to the about page
        onms.go("about/index.jsp");
    }

    @Test
    public void hasAllPanels(){
        assertEquals(4, SeleniumMatchers.countElementsMatchingCss(driver,"div.card-header"));
    }

    @Test
    public void hasContent() {
        assertNotNull(driver.findElement(By.xpath("//span[text()='License and Copyright']")));
        assertNotNull(driver.findElement(By.xpath("//th[text()='Version:']")));
    }

}
