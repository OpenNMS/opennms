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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DistributedMapIT extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        m_driver.get(getBaseUrl() + "opennms/RemotePollerMap/index.jsp");
    }

    @Test
    public void testDistributedMap() throws Exception {
        // switchTo() by xpath is much faster than by ID
        //m_driver.switchTo().frame("app");
        m_driver.switchTo().frame(findElementByXpath("/html/body/div/iframe"));

        // first 5 checkboxes are checked by default
        for (int i=1; i <= 5; i++) {
            assertTrue("checkbox gwt-uid-" + i + " should be checked.", m_driver.findElement(By.id("gwt-uid-" + i)).isSelected());
        }
        assertFalse("checkbox gwt-uid-6 should be unchecked.", m_driver.findElement(By.id("gwt-uid-6")).isSelected());
    }

}
