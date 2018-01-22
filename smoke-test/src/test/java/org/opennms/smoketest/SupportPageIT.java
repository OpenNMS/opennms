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

import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SupportPageIT extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        supportPage();
    }

    @Test
    public void testAllLinksArePresent() throws Exception {
        Thread.sleep(4000);
        assertEquals(4, countElementsMatchingCss("h3.panel-title"));
        final String[] links = new String[] {
                "the OpenNMS.com support page",
                "About the OpenNMS Web Console",
                "Release Notes",
                // Online docs links
                "Installation Guide",
                "Users Guide",
                "Administrators Guide",
                "Developers Guide",
                "Online Wiki Documentation",
                // Offline docs links
                "Installation Guide",
                "Users Guide",
                "Administrators Guide",
                "Developers Guide",
                "Online Wiki Documentation",
                "Generate a System Report",
                "Open a Bug or Enhancement Request",
                "Chat with Developers on IRC"
        };
        assertEquals(links.length, countElementsMatchingCss("div.panel-body a"));
        for (final String text : links) {
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
        clickElement(By.linkText("About the OpenNMS Web Console"));
        assertNotNull(m_driver.findElement(By.xpath("//h3[text()='License and Copyright']")));
        assertNotNull(m_driver.findElement(By.xpath("//th[text()='Version:']")));
    }
    
    public void testSystemReport() {
        clickElement(By.linkText("Generate System Report"));

        final By javaCheckbox = By.cssSelector("input[type=checkbox][name=plugins][value=Java]");
        assertThat(m_driver.findElement(javaCheckbox).isSelected(), is(true));

        // deselect the "all" checkbox
        clickElement(By.cssSelector("input[type=checkbox][name=all]"));
        assertThat(m_driver.findElement(javaCheckbox).isSelected(), is(false));
    }

}
