/*
 * This file is part of BlueBirdOps(tm).
 *
 * BlueBirdOps is Copyright (C) 2024 BlueBirdOps Contributors.
 *
 * Portions Copyright (C) 2002-2024 The OpenNMS Group, Inc.
 *
 * See the LICENSE.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * BlueBirdOps is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * BlueBirdOps is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with BlueBirdOps. If not, see <https://www.gnu.org/licenses/>.
 */
package org.opennms.smoketest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

public class AboutPageIT extends OpenNMSSeleniumIT {

    @Before
    public void setUp() throws Exception {
        driver.get(getBaseUrlInternal() + "opennms/about/index.jsp");
    }

    @Test
    public void hasAllPanels() throws Exception {
        assertEquals(5, countElementsMatchingCss("div.card-header"));
    }

    @Test
    public void hasContent() {
        assertNotNull(driver.findElement(By.xpath("//span[text()='License and Copyright']")));
        assertNotNull(driver.findElement(By.xpath("//th[text()='Version:']")));
    }
}
