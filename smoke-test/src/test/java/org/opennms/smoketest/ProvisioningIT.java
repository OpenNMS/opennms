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

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProvisioningIT extends OpenNMSSeleniumTestCase {
    private static final String NODE_LABEL = "localNode";
    private static final Logger LOG = LoggerFactory.getLogger(ProvisioningIT.class);

    @Override
    protected void provisioningPage() {
        m_driver.get(getBaseUrl() + "opennms/admin/provisioningGroups.htm");
    }

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
                enterText(By.name(prefix + "." + suffix), value);
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

        clickElement(By.xpath("//input[contains(@onclick, '" + currentNode + "') and @value='Save']"));
        return currentNode;
    }

    protected String getCurrentNode() {
        return waitUntil(null, null, new Callable<String>() {
            @Override public String call() throws Exception {
                return getElementImmediately(By.xpath("//input[@name='currentNode']")).getAttribute("value");
            }
        });
    }

    @Test
    public void testRequisitionUI() throws Exception {
        waitUntil(null, null, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                final WebElement form = waitForElement(By.xpath("//form[@name='takeAction']"));
                form.findElement(By.cssSelector("input[type=text][name=groupName]")).click();
                form.findElement(By.cssSelector("input[type=text][name=groupName]")).sendKeys(REQUISITION_NAME, Keys.TAB);
                form.findElement(By.cssSelector("input[type=text][name=groupName]")).click();
                form.submit();
                return true;
            }
        });

        // edit the foreign source
        clickElement(By.id("edit_fs_anchor_" + REQUISITION_NAME));

        // add a detector
        clickElement(By.xpath("//input[@value='Add Detector']"));
        String detectorNode = setTreeFieldsAndSave("foreignSourceEditForm", type("name", "HTTP-8980"), select("pluginClass", "HTTP"));

        // set the port to 8980
        clickElement(By.xpath("//a[contains(@href, '"+detectorNode+"') and text() = '[Add Parameter]']"));
        setTreeFieldsAndSave("foreignSourceEditForm", select("key", "port"), type("value", "8980"));

        clickElement(By.xpath("//input[@value='Done']"));

        // add a node
        clickElement(By.id("edit_req_anchor_" + REQUISITION_NAME));
        clickElement(By.xpath("//input[@value='Add Node']"));
        String nodeForNode = setTreeFieldsAndSave("nodeEditForm", type("nodeLabel", NODE_LABEL), type("foreignId", NODE_LABEL));

        // add the node interface
        clickElement(By.xpath("//a[contains(@href, '" + nodeForNode + "') and text() = '[Add Interface]']"));
        setTreeFieldsAndSave("nodeEditForm", type("ipAddr", "::1"));

        // add the interface service
        clickElement(By.xpath("//a[text() = 'Add Service']"));
        setTreeFieldsAndSave("nodeEditForm", select("serviceName", "HTTP-8980"));

        // add the interface service
        clickElement(By.xpath("//a[text() = 'Add Service']"));
        setTreeFieldsAndSave("nodeEditForm", select("serviceName", "ICMP"));

        clickElement(By.xpath("//input[@value='Done']"));
        clickElement(By.xpath("//input[@value='Synchronize']"));

        assertTrue(wait.until(new WaitForNodesInDatabase(1)));
        LOG.debug("Found 1 node in the database.");

        // wait for the node scanning to complete
        Thread.sleep(5000);

        m_driver.get(getBaseUrl() + "opennms/element/node.jsp?node="+ REQUISITION_NAME + ":" + NODE_LABEL);
        waitForElement(By.xpath("//h3[text()='Availability']"));

        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("ICMP")));
        waitForElement(By.xpath("//a[contains(@href, 'element/interface.jsp') and text()='" + InetAddressUtils.normalize("::1") + "']"));
    }
}
