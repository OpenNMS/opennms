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

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InstrumentationLogReaderPageIT extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        adminPage();
        clickElement(By.linkText("Instrumentation Log Reader"));
    }

    @Test
    public void testInstrumentationLogReaderPage() throws Exception {
        enterText(By.name("searchString"), "test");
        assertEquals("test", findElementByName("searchString").getAttribute("value"));
        clickElement(By.xpath("//button[@type='submit' and text()='Submit']"));
        clickElement(By.xpath("//button[@type='submit' and text()='Reset']"));
        assertEquals("", findElementByName("searchString").getAttribute("value"));
    }

    @Test
    public void testSortingLinks() throws InterruptedException {
        clickElement(By.linkText("Collections"));
        clickElement(By.xpath("//a[text()='Collections ^']"));
        clickElement(By.xpath("//a[text()='Collections v']"));
        clickElement(By.linkText("Average Successful Collection Time"));
        clickElement(By.xpath("//a[text()='Average Successful Collection Time ^']"));
        clickElement(By.xpath("//a[text()='Average Successful Collection Time v']"));
        clickElement(By.linkText("Average Persistence Time"));
        clickElement(By.xpath("//a[text()='Average Persistence Time ^']"));
        clickElement(By.xpath("//a[text()='Average Persistence Time v']"));
    }
}
