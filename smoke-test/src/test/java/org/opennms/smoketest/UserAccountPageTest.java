/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest;

import org.junit.Test;


public class UserAccountPageTest extends OpenNMSSeleniumTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        selenium.open("/opennms/account/selfService/index.jsp");
        waitForPageToLoad();
    }
    
    @Test
    public void testAllTextIsPresent() throws Exception {
        waitForText("User Account Self-Service");
        waitForText("Account Self-Service Options");
        waitForText("require further");
    }

    @Test 
    public void testAllLinksArePresent() throws InterruptedException {
        waitForElement("link=Change Password");
    }

    @Test
    public void testAllLinks() throws InterruptedException {
        clickAndWait("link=Change Password");
        waitForText("Please enter the old and new passwords and confirm.");
        waitForText("Current Password");
        waitForElement("link=Cancel");
    }

}
