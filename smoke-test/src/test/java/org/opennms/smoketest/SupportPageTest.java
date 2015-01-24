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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class SupportPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        supportPage();
    }

    @Test
    public void testAllLinksArePresent() throws InterruptedException {
        for (final String text : new String[] {
                "About the OpenNMS Web Console",
                "Release Notes",
                "Online Documentation",
                "Generate a System Report",
                "Open a Bug or Enhancement Request",
                "Chat with Developers on IRC"
        }) {
            assertNotNull("Link with text '" + text + "' must exist.", m_driver.findElement(By.linkText(text)));
        }
    }

    @Test
    public void testAllFormsArePresent() throws InterruptedException {
        final WebElement form = m_driver.findElement(By.cssSelector("form[action='support/index.htm']"));
        assertNotNull(form);
        assertNotNull(form.findElement(By.cssSelector("input[type=text][name=username]")));
        assertNotNull(form.findElement(By.cssSelector("input[type=password][name=password]")));
    }

    @Test
    public void testAboutPage() throws Exception {
        final WebElement about = m_driver.findElement(By.linkText("About the OpenNMS Web Console"));
        assertNotNull(about);
        about.click();
        assertNotNull(m_driver.findElement(By.xpath("//h3[text()='License and Copyright']")));
        assertNotNull(m_driver.findElement(By.xpath("//th[text()='Version:']")));
    }
    
    @Test
    public void testSystemReport() throws Exception {
        final WebElement generate = m_driver.findElement(By.linkText("Generate a System Report"));
        assertNotNull(generate);
        generate.click();
        // checkboxes are selected by default
        final WebElement allCheckbox = m_driver.findElement(By.cssSelector("input[type=checkbox][name=all]"));
        assertNotNull(allCheckbox);
        assertTrue(m_driver.findElement(By.cssSelector("input[type=checkbox][name=plugins][value=Java]")).isSelected());
        // deselect the "all" checkbox
        allCheckbox.click();
        assertFalse(m_driver.findElement(By.cssSelector("input[type=checkbox][name=plugins][value=Java]")).isSelected());
    }

}
