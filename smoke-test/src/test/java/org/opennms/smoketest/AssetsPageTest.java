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

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AssetsPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        clickAndWait("link=Assets");
    }

    @Test
    public void a_testAllTextIsPresent() throws InterruptedException { 
        waitForText("Search Asset Information");
        waitForText("Assets Inventory");
        waitForText("nter the data by hand");
        waitForText("Assets with asset numbers");
        waitForText("Assets in category");
    }    

    @Test 
    public void b_testAllLinksArePresent() throws InterruptedException {
        waitForElement("css=input[type=submit]");
        waitForElement("name=searchvalue");
        waitForElement("link=All nodes with asset info");
    }
    @Test
    public void c_testAllLinks() throws InterruptedException {
        clickAndWait("link=All nodes with asset info");
        waitForText("Assets");
        clickAndWait("//div[@id='content']/div/h2/a[2]");
    }

}
