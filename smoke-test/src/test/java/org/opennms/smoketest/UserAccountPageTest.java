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

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class UserAccountPageTest extends OpenNMSSeleniumTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        m_driver.get(BASE_URL + "opennms/account/selfService/index.jsp");
    }

    @Test
    public void testExpectedTextAndLinksArePresent() throws Exception {
        final List<WebElement> h3s = m_driver.findElements(By.tagName("h3"));
        assertEquals(2, h3s.size());
        assertEquals("User Account Self-Service", h3s.get(0).getText());
        assertEquals("Account Self-Service Options", h3s.get(1).getText());
    }

    @Test
    public void testSubmitWithWrongPassword() throws InterruptedException {
        m_driver.findElement(By.linkText("Change Password")).click();
        m_driver.findElement(By.cssSelector("input[type=password][name=oldpass]")).sendKeys("12345");
        m_driver.findElement(By.cssSelector("input[type=password][name=pass1]")).sendKeys("23456");
        m_driver.findElement(By.cssSelector("input[type=password][name=pass2]")).sendKeys("34567");
        m_driver.findElement(By.cssSelector("input[type=submit][value=OK]")).click();

        final WebDriverWait wait = new WebDriverWait(m_driver, TimeUnit.SECONDS.convert(LOAD_TIMEOUT, TimeUnit.MILLISECONDS));
        assertNotNull(wait.until(ExpectedConditions.alertIsPresent()));
        m_driver.switchTo().alert().dismiss();
    }

}
