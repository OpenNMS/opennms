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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserIT extends OpenNMSSeleniumIT {
    private static final Logger LOG = LoggerFactory.getLogger(UserIT.class);

    @Before
    public void setUp() throws Exception {
        driver.get(getBaseUrlInternal() + "opennms/account/selfService/index.jsp");
    }

    @Test
    public void testExpectedTextAndLinksArePresent() throws Exception {
        final List<WebElement> headers = driver.findElements(By.xpath("//div[@class='card-header']/span"));
        assertEquals("Account page should have 2 panels", 2, headers.size());
        assertEquals("Account page should have \"User Account Self-Service\" panel", "User Account Self-Service", headers.get(0).getText());
        assertEquals("Account page should have \"User Account Self-Service Options\" panel", "Account Self-Service Options", headers.get(1).getText());
    }

    @Test
    public void testSubmitWithWrongPassword() throws InterruptedException {
        driver.findElement(By.linkText("Change Password")).click();
        enterText(By.cssSelector("input[type=password][name=oldpass]"), "12345");
        enterText(By.cssSelector("input[type=password][name=pass1]"), "23456");
        enterText(By.cssSelector("input[type=password][name=pass2]"), "34567");
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        try {
            final Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.dismiss();
        } catch (final Exception e) {
            LOG.debug("Got an exception waiting for a 'wrong password' alert.", e);
            throw e;
        }
    }

    @Test
    public void testUsersAndGroups() throws Exception {
        adminPage();
        findElementByLink("Configure Users, Groups and On-Call Roles").click();
        findElementByLink("Configure Users").click();
        findElementByLink("Add new user").click();

        enterText(By.id("userID"), USER_NAME);
        enterText(By.id("pass1"), "SmokeTestPassword");
        enterText(By.id("pass2"), "SmokeTestPassword");
        findElementByXpath("//button[@type='submit' and text()='OK']").click();

        findElementById("saveUserButton").click();
        findElementById("users(" + USER_NAME + ").doDetails");

        adminPage();
        findElementByLink("Configure Users, Groups and On-Call Roles").click();
        findElementByLink("Configure Groups").click();
        findElementByLink("Add new group").click();

        enterText(By.id("groupName"), GROUP_NAME);
        enterText(By.id("groupComment"), "Test");
        findElementByXpath("//button[@type='submit' and text()='OK']").click();

        final Select select = new Select(findElementByName("availableUsers"));
        select.selectByVisibleText(USER_NAME);
        findElementById("users.doAdd").click();

        findElementByName("finish").click();

        findElementByLink(GROUP_NAME).click();
        driver.findElement(By.xpath("//div[@class='card-header']/span[text()='Details for Group: " + GROUP_NAME + "']"));

        findElementByLink("Group List").click();
        findElementById(GROUP_NAME + ".doDelete").click();
        handleAlert("Are you sure you want to delete the group " + GROUP_NAME + "?");
        assertElementDoesNotExist(By.id(GROUP_NAME));

        findElementByLink("Users and Groups").click();
        findElementByLink("Configure Users").click();
        findElementById("user-" + USER_NAME);
        findElementById("users(" + USER_NAME + ").doDelete").click();
        handleAlert("Are you sure you want to delete the user " + USER_NAME + "?");
        assertElementDoesNotExist(By.id(USER_NAME));
    }

}
