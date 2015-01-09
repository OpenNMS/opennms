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

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.smoketest.expectations.ExpectationBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AddNodePageTest extends OpenNMSSeleniumTestCase {
    private static final String m_unreachableIp = InetAddressUtils.str(InetAddressUtils.UNPINGABLE_ADDRESS);

    @Before
    public void setUp() throws Exception {
        deleteTestRequisition();
    }

    @After
    public void tearDown() throws Exception {
        deleteTestRequisition();
    }

    @Test
    public void testAddNodePage() throws Exception {
        provisioningPage();
        m_driver.findElement(By.cssSelector("form[name=takeAction] input[name=groupName]")).sendKeys(REQUISITION_NAME);
        m_driver.findElement(By.cssSelector("form[name=takeAction] input[type=submit]")).click();
        findElementByXpath("//input[@value='Synchronize']").click();

        frontPage();
        clickMenuItem("name=nav-admin-top", "Quick-Add Node", BASE_URL + "opennms/admin/node/add.htm");

        final WebElement submitButton = m_driver.findElement(By.cssSelector("input[type=submit][value=Provision]"));
        assertEquals("Provision", submitButton.getAttribute("value"));

        final WebElement selectElement = m_driver.findElement(By.cssSelector("select[name=foreignSource]"));
        final Select sel = new Select(selectElement);
        sel.selectByVisibleText(REQUISITION_NAME);

        findElementByName("ipAddress").sendKeys(m_unreachableIp);
        findElementByName("nodeLabel").sendKeys("AddNodePageTest");
        submitButton.click();

        findElementByLink("Provisioning Requisitions").click();
        findElementById("edit_req_anchor_" + REQUISITION_NAME).click();

        new ExpectationBuilder("css=input[value=AddNodePageTest]").check(m_driver);

        provisioningPage();
        wait.until(new WaitForNodesInDatabase(1));
    }

}
