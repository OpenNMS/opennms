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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

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
    public void testAllButtonsArePresent() throws Exception {
        final String[] links = new String[] {
                "the OpenNMS.com support page",
                "Generate System Report",
                "Collectd Statistics",
                "About OpenNMS",
                "Commercial Support",
                "Web Chat",
                "Mailing Lists",
                "Questions & Answers"
        };
        assertEquals(links.length, countElementsMatchingCss("div.panel-body a"));
        for (final String text : links) {
            assertNotNull("Link with text '" + text + "' must exist.", m_driver.findElement(By.linkText(text)));
        }
    }

    @Test
    public void testAllFormsArePresent() {
        final WebElement form = m_driver.findElement(By.cssSelector("form[action='support/index.htm']"));
        assertNotNull(form.findElement(By.cssSelector("input[type=text][name=username]")));
        assertNotNull(form.findElement(By.cssSelector("input[type=password][name=password]")));
    }

    @Test
    public void testSystemReport() {
        m_driver.findElement(By.linkText("Generate System Report")).click();

        // checkboxes are selected by default
        final WebElement allCheckbox = m_driver.findElement(By.cssSelector("input[type=checkbox][name=all]"));
        assertThat(m_driver.findElement(By.cssSelector("input[type=checkbox][name=plugins][value=Java]")).isSelected(), is(true));

        // deselect the "all" checkbox
        allCheckbox.click();
        assertThat(m_driver.findElement(By.cssSelector("input[type=checkbox][name=plugins][value=Java]")).isSelected(), is(false));
    }
}
