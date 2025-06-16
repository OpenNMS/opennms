/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
