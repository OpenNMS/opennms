/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class FooterIT extends OpenNMSSeleniumTestCase {

    @Before
    public void setUp() throws Exception {
        m_driver.get(getBaseUrl() + "opennms/login.jsp");
    }

    @Test
    public void verifyDisplayVersionForLoggedInUser() throws Exception {
        assertNotNull(findElementByXpath("//*[@id=\"footer\"]/p"));
        assertNotNull(findElementByXpath("//*[@id=\"footer\"]/p[contains(.,'Version')]"));
    }

    @Test
    public void verifyDoNotDisplayVersionForAnonymous() throws Exception {
        final String adminMenuName = "name=nav-admin-top";
        clickMenuItem(adminMenuName, "Log Out", "j_spring_security_logout");
        assertNotNull(findElementByXpath("//*[@id=\"footer\"]/p[not(contains(.,'Version'))]"));
    }
}
