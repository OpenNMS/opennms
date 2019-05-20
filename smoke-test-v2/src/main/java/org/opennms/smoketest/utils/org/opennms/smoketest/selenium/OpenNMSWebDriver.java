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

package org.opennms.smoketest.utils.org.opennms.smoketest.selenium;

import static org.junit.Assert.fail;

import java.util.Objects;

import org.opennms.smoketest.containers.OpenNMSContainer;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class OpenNMSWebDriver {

    private final OpenNMSContainer container;
    private final WebDriver driver;
    private final SeleniumUtils s;

    public OpenNMSWebDriver(OpenNMSContainer container, WebDriver driver) {
        this.container = Objects.requireNonNull(container);
        this.driver = Objects.requireNonNull(driver);
        s = new SeleniumUtils(driver);
    }

    public void go(String path) {
        driver.get(container.getBaseUrlInternal() + "opennms/" + path);
    }

    public void login() {
        driver.get(container.getBaseUrlInternal() + "opennms/login.jsp");

        waitForLogin();

        s.enterText(By.name("j_username"), "admin");
        s.enterText(By.name("j_password"), "admin");
        s.findElementByName("Login").click();

        s.getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='content']")));
        s.invokeWithImplicitWait(0, () -> {
            try {
                // Make sure that the 'login-attempt-failed' element is not present
                s.findElementById("login-attempt-failed");
                fail("Login failed: " + s.findElementById("login-attempt-failed-reason").getText());
            } catch (NoSuchElementException e) {
                // This is expected
            }
        });
        s.invokeWithImplicitWait(0, () -> {
            try {
                WebElement element = s.findElementById("datachoices-modal");
                if (element.isDisplayed()) { // datachoice modal is visible
                    s.findElementById("datachoices-disable").click(); // opt out
                }
            } catch (NoSuchElementException e) {
                // "datachoices-modal" is not visible or does not exist.
                // No further action required
            }
        });
    }

    private void waitForLogin() {
        // Wait until the login form is complete
        s.getWait().until(ExpectedConditions.visibilityOfElementLocated(By.name("j_username")));
        s.getWait().until(ExpectedConditions.visibilityOfElementLocated(By.name("j_password")));
        s.getWait().until(ExpectedConditions.elementToBeClickable(By.name("Login")));
    }
}
