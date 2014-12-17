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

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProvisioningTest extends OpenNMSSeleniumTestCase {
    private static final String NODE_LABEL = "localNode";
    private static final Logger LOG = LoggerFactory.getLogger(ProvisioningTest.class);

    @Before
    public void setUp() throws Exception {
        deleteTestRequisition();
        provisioningPage();
    }

    interface Setter {
        public void setField(String prefix);
    }

    Setter type(final String suffix, final String value) {
        return new Setter() {
            public void setField(final String prefix) {
                final WebElement element = findElementByName(prefix + "." + suffix);
                element.clear();
                element.sendKeys(value);
            }
        };
    }

    Setter select(final String suffix, final String value) {
        return new Setter() {
            public void setField(final String prefix) {
                new Select(findElementByName(prefix + "." + suffix)).selectByVisibleText(value);
            }
        };
    }

    String setTreeFieldsAndSave(final String formName, final Setter... setters) throws InterruptedException {
        final String currentNode = getCurrentNode();
        LOG.debug("setTreeFieldsAndSave: currentNode={}", currentNode);
        assertTrue(currentNode.startsWith(formName+"."));
        final String prefix = currentNode.replace(formName+".", "");

        for (final Setter setter : setters) {
            setter.setField(prefix);
        }

        findElementByXpath("//input[contains(@onclick, '" + currentNode + "') and @value='Save']").click();
        return currentNode;
    }

    protected String getCurrentNode() {
        return findElementByXpath("//input[@name='currentNode']").getAttribute("value");
    }

    @Test
    public void testRequisitionUI() throws Exception {
        final WebElement form = findElementByXpath("//form[@name='takeAction']");
        form.findElement(By.cssSelector("input[type=text][name=groupName]")).sendKeys(REQUISITION_NAME);
        form.submit();

        // edit the foreign source
        findElementById("edit_fs_anchor_" + REQUISITION_NAME).click();

        // add a detector
        findElementByXpath("//input[@value='Add Detector']").click();
        String detectorNode = setTreeFieldsAndSave("foreignSourceEditForm", type("name", "HTTP-8980"), select("pluginClass", "HTTP"));

        // set the port to 8980
        findElementByXpath("//a[contains(@href, '"+detectorNode+"') and text() = '[Add Parameter]']").click();
        setTreeFieldsAndSave("foreignSourceEditForm", select("key", "port"), type("value", "8980"));

        findElementByXpath("//input[@value='Done']").click();

        // add a node
        findElementById("edit_req_anchor_" + REQUISITION_NAME).click();
        findElementByXpath("//input[@value='Add Node']").click();
        String nodeForNode = setTreeFieldsAndSave("nodeEditForm", type("nodeLabel", NODE_LABEL));

        // add the node interface
        findElementByXpath("//a[contains(@href, '" + nodeForNode + "') and text() = '[Add Interface]']").click();
        setTreeFieldsAndSave("nodeEditForm", type("ipAddr", "::1"));

        // add the interface service
        findElementByXpath("//a[text() = 'Add Service']").click();
        setTreeFieldsAndSave("nodeEditForm", select("serviceName", "HTTP-8980"));

        findElementByXpath("//input[@value='Done']").click();
        findElementByXpath("//input[@value='Synchronize']").click();

        assertTrue(wait.until(new WaitForNodesInDatabase(1)));
        LOG.debug("Found 1 node in the database.");

        clickMenuItem("Info", "Nodes", "element/nodeList.htm");

        try {
            findElementByXpath("//h3[text()='Availability']");
        } catch (final Exception e) {
            // We should be on the node list page, click through to the node
            findElementByLink(NODE_LABEL).click();
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("ICMP")));
        findElementByXpath("//a[contains(@href, 'element/interface.jsp') and text()='" + InetAddressUtils.normalize("::1") + "']");
    }
}
