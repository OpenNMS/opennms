/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.web.navigate.LocationBasedNavBarEntry;
import org.opennms.web.navigate.MenuDropdownNavBarEntry;
import org.opennms.web.navigate.NavBarEntry;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;

import com.google.common.collect.Lists;

public class NavBarControllerTest {
    NavBarController navBarController;

    @Before
    public void setUp() throws Exception {
        // Build a simple menu
        LocationBasedNavBarEntry alarms = new LocationBasedNavBarEntry();
        alarms.setName("Alarms");
        alarms.setUrl("alarm/index.htm");
        alarms.setLocationMatch("alarm");

        MenuDropdownNavBarEntry menu = new MenuDropdownNavBarEntry();
        menu.setName("Menu");
        menu.setEntries(Lists.newArrayList((NavBarEntry) alarms));

        List<NavBarEntry> navBarEntries = Lists.newArrayList((NavBarEntry) menu);

        // Instantiate the controller
        navBarController = new NavBarController();
        navBarController.setServletContext(new MockServletContext("file:src/main/webapp"));
        navBarController.setNavBarItems(navBarEntries);
        navBarController.afterPropertiesSet();
    }

    @Test
    public void canRenderHeaderHtml() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRemoteUser("admin");
        String headerHtml = navBarController.getHeaderHtml(request);
        assertTrue(headerHtml.contains("<nav"));
    }
}
