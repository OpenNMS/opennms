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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProvisioningTest extends OpenNMSSeleniumTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(ProvisioningTest.class);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        provisioningPage();
    }

    @After
    public void tearDown() throws Exception {
        deleteTestRequisition();
        deleteTestUser();
        deleteTestGroup();

        super.tearDown();
    }

    interface Setter {
        public void setField(String prefix);
    }

    Setter type(final String suffix, final String value) {
        return new Setter() {
            public void setField(final String prefix) {
                final WebElement element = m_driver.findElement(By.name(prefix + "." + suffix));
                element.clear();
                element.sendKeys(value);
            }
        };
    }

    Setter select(final String suffix, final String value) {
        return new Setter() {
            public void setField(final String prefix) {
                new Select(m_driver.findElement(By.name(prefix + "." + suffix))).selectByVisibleText(value);
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

        m_driver.findElement(By.xpath("//input[contains(@onclick, '" + currentNode + "') and @value='Save']")).click();
        return currentNode;
    }

    protected String getCurrentNode() {
        final WebElement element = m_driver.findElement(By.xpath("//input[@name='currentNode']"));
        return element.getAttribute("value");
    }

    @Test
    public void testRequisitionUI() throws Exception {
        final WebElement form = m_driver.findElement(By.cssSelector("form[name=takeAction]"));
        form.findElement(By.cssSelector("input[type=text][name=groupName]")).sendKeys(REQUISITION_NAME);
        form.submit();

        // edit the foreign source
        m_driver.findElement(By.id("edit_fs_anchor_" + REQUISITION_NAME)).click();

        // add a detector
        m_driver.findElement(By.xpath("//input[@value='Add Detector']")).click();
        String detectorNode = setTreeFieldsAndSave("foreignSourceEditForm", type("name", "HTTP-8980"), select("pluginClass", "HTTP"));

        // set the port to 8980
        m_driver.findElement(By.xpath("//a[contains(@href, '"+detectorNode+"') and text() = '[Add Parameter]']")).click();
        setTreeFieldsAndSave("foreignSourceEditForm", select("key", "port"), type("value", "8980"));

        m_driver.findElement(By.xpath("//input[@value='Done']")).click();

        // add a node
        m_driver.findElement(By.id("edit_req_anchor_" + REQUISITION_NAME)).click();
        m_driver.findElement(By.xpath("//input[@value='Add Node']")).click();
        String nodeForNode = setTreeFieldsAndSave("nodeEditForm", type("nodeLabel", "localNode"));

        // add the node interface
        m_driver.findElement(By.xpath("//a[contains(@href, '" + nodeForNode + "') and text() = '[Add Interface]']")).click();
        setTreeFieldsAndSave("nodeEditForm", type("ipAddr", "::1"));

        // add the interface service
        m_driver.findElement(By.xpath("//a[text() = 'Add Service']")).click();
        setTreeFieldsAndSave("nodeEditForm", select("serviceName", "HTTP-8980"));

        m_driver.findElement(By.xpath("//input[@value='Done']")).click();
        m_driver.findElement(By.xpath("//input[@value='Synchronize']")).click();

        assertTrue(wait.until(new WaitForNodesInDatabase(1)));
        LOG.debug("Found 1 node in the database.");

        m_driver.findElement(By.linkText("Node List")).click();

        assertNotNull(m_driver.findElement(By.linkText("ICMP")));
        assertNotNull(m_driver.findElement(By.xpath("//a[contains(@href, 'element/interface.jsp') and text()='" + InetAddressUtils.normalize("::1") + "']")));
    }

}
