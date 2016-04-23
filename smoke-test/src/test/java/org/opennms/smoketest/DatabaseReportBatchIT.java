/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Verifies the options "deliver" and "schedule" functions of the database reports.
 *
 * Unless {@link DatabaseReportIT} we only verify if the FIRST report can be delivered/scheduled.
 * We do NOT test ALL.
 */
public class DatabaseReportBatchIT extends OpenNMSSeleniumTestCase {

    @Before
    public void before() {
        reportsPage();
        findElementByLink("Database Reports").click();
        findElementByLink("List reports").click();

        // we do not want to wait 2 minutes, we only want to wait n seconds
        m_driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @Test
    public void testDeliver() {
        findElementByXpath("//td[@class=\"o-report-deliver\"]/a").click();
        List<WebElement> elements = m_driver.findElements(By.xpath("//h3[contains(text(), 'Error')]"));
        Assert.assertEquals(0, elements.size());
    }


    @Test
    public void testSchedule() {
        findElementByXpath("//td[@class=\"o-report-schedule\"]/a").click();
        List<WebElement> elements = m_driver.findElements(By.xpath("//h3[contains(text(), 'Error')]"));
        Assert.assertEquals(0, elements.size());
    }
}
