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
package org.opennms.web.rest.support.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.startsWith;

import org.opennms.netmgt.config.menu.MainMenu;
import org.opennms.netmgt.config.menu.MenuEntry;

public class MenuProviderTest {
    final static String MENU_TEMPLATE_PATH = "src/test/resources/menu-template.json";

    final static String[] ADMIN_ROLES = new String[] { "ROLE_ADMIN", "ROLE_FLOW_MANAGER" };

    final static String[] USER_ROLES = new String[] { "ROLE_USER" };

    final static String[] FILESYSTEM_EDITOR_ROLES = new String[] { "ROLE_FILESYSTEM_EDITOR" };

    @Test
    public void testParseMainMenu() {
        MainMenu mainMenu = parseMainMenu(true, ADMIN_ROLES);
        assertNotNull(mainMenu);

        assertEquals("/opennms/", mainMenu.baseHref);
        assertEquals("/opennms/index.jsp", mainMenu.homeUrl);
        assertEquals("2025-03-01T20:30:00.000Z", mainMenu.formattedDateTime);
        assertEquals("2025-03-01", mainMenu.formattedDate);
        assertEquals("20:30:00 UTC+00", mainMenu.formattedTime);
        assertEquals("On", mainMenu.noticeStatus);
        assertEquals("admin1", mainMenu.username);
        assertEquals("element/node.jsp?node=", mainMenu.baseNodeUrl);
        assertTrue(mainMenu.zenithConnectEnabled);
        assertEquals("https://zenith.opennms.com", mainMenu.zenithConnectBaseUrl);
        assertEquals("/zenith-connect", mainMenu.zenithConnectRelativeUrl);
        // version should be "2002-CURRENT_DATE_YEAR"
        assertThat(mainMenu.copyrightDates, startsWith("2002-2"));
        // not testing mainMenu.version, it's probably null in test environment

        // Check top level menus and names
        final String[] expectedMenuNames = new String[] {
            "Dashboards", "Inventory", "Monitoring", "Maps", "Topologies", "Metrics (Resource Graphs)",
            "Distributed Monitoring", "Manage Inventory", "User Management", "Integrations",
            "Tools", "Administration", "Internal Logs", "User Profile", "API Documentation",
            "Help", "Support"
        };

        assertNotNull(mainMenu.menus);

        // +2: there are 2 separator menu items
        assertEquals(expectedMenuNames.length + 2, mainMenu.menus.size());

        List<String> actualMenuNames = mainMenu.menus.stream().filter(m -> m.name != null).map(m -> m.name).toList();
        assertThat(actualMenuNames, containsInAnyOrder(expectedMenuNames));

        assertNotNull(mainMenu.configurationMenu);
        assertNotNull(mainMenu.flowsMenu);
        assertNotNull(mainMenu.helpMenu);
        assertNotNull(mainMenu.provisionMenu);
        assertNotNull(mainMenu.selfServiceMenu);
        assertNotNull(mainMenu.userNotificationMenu);

        // Check names in sub-menus under the Info menu
        MenuEntry infoMenu = getMenuEntry(mainMenu.menus, "Inventory");

        final String[] expectedNames = new String[] {
                "Nodes", "Structured Node List", "Device Configs", "Assets", "Search Inventory"
        };
        List<String> actualNames = getMenuNames(infoMenu);
        assertEquals(expectedNames.length, actualNames.size());
        assertThat(actualNames, containsInAnyOrder(expectedNames));

        // Check Roles on Device Config entry
        MenuEntry dcbMenu = getMenuEntry(infoMenu.items, "Device Configs");

        final String[] expectedDeviceConfigRoles = new String[] {
                "ROLE_ADMIN",
                "ROLE_REST",
                "ROLE_DEVICE_CONFIG_BACKUP"
        };
        assertThat(dcbMenu.roles, containsInAnyOrder(expectedDeviceConfigRoles));
    }

    @Test
    public void testParseMainMenuZenithConnectDisabled() {
        MainMenu mainMenu = parseMainMenu(false, ADMIN_ROLES);
        assertNotNull(mainMenu);

        // Check names in sub-menus under the Info menu
        MenuEntry infoMenu = getMenuEntry(mainMenu.menus, "Inventory");

        final String[] expectedNames = new String[] {
            "Nodes", "Structured Node List", "Device Configs", "Assets", "Search Inventory"
        };

        List<String> actualNames = getMenuNames(infoMenu);
        assertEquals(expectedNames.length, actualNames.size());
        assertThat(actualNames, containsInAnyOrder(expectedNames));
    }

    @Test
    public void testParseMainMenuWithUserRole() {
        MainMenu mainMenu = parseMainMenu(false, USER_ROLES);
        assertNotNull(mainMenu);

        // Check names in sub-menus under the Tools menu
        MenuEntry infoMenu = getMenuEntry(mainMenu.menus, "Tools");

        // There is an ADMIN and FILESYSTEM_EDITOR role, should not show up here
        final String[] expectedNames = new String[]{
            "SNMP MIB Compiler", "JMX Metric Configuration Generator",
            "Import/Export Node Asset Information", "Send Custom Events"
        };

        List<String> actualNames = getMenuNames(infoMenu);
        assertEquals(expectedNames.length, actualNames.size());
        assertThat(actualNames, containsInAnyOrder(expectedNames));
    }

    @Test
    public void testParseMainMenuWithAdminRole() {
        MainMenu mainMenu = parseMainMenu(false, ADMIN_ROLES);
        assertNotNull(mainMenu);

        // Check names in sub-menus under the Tools menu
        MenuEntry infoMenu = getMenuEntry(mainMenu.menus, "Tools");

        // There is an ADMIN and FILESYSTEM_EDITOR role, should include the ADMIN one (SCV)
        final String[] expectedNames = new String[] {
                "SNMP MIB Compiler", "JMX Metric Configuration Generator",
                "Import/Export Node Asset Information", "Send Custom Events",
                "Secure Credentials Vault"
        };

        List<String> actualNames = getMenuNames(infoMenu);
        assertEquals(expectedNames.length, actualNames.size());
        assertThat(actualNames, containsInAnyOrder(expectedNames));
    }

    @Test
    public void testParseMainMenuWithFileSystemEditorRole() {
        MainMenu mainMenu = parseMainMenu(false, FILESYSTEM_EDITOR_ROLES);
        assertNotNull(mainMenu);

        // Check names in sub-menus under the Tools menu
        MenuEntry toolsMenu = getMenuEntry(mainMenu.menus, "Tools");

        // There is an ADMIN and FILESYSTEM_EDITOR role, should include the FILESYSTEM_EDITOR one (File Editor)
        // but not the ADMIN one (SCV)
        final String[] expectedNames = new String[] {
                "SNMP MIB Compiler", "JMX Metric Configuration Generator",
                "Import/Export Node Asset Information", "Send Custom Events",
                "File Editor"
        };

        List<String> actualNames = getMenuNames(toolsMenu);
        assertEquals(expectedNames.length, actualNames.size());
        assertThat(actualNames, containsInAnyOrder(expectedNames));
    }

    private MainMenu parseMainMenu(boolean isZenithConnectEnabled, String[] roles) {
        MainMenu mainMenu = null;
        List<String> roleList = Arrays.stream(roles).toList();

        try {
            MenuProvider provider = new MenuProvider(null, MENU_TEMPLATE_PATH);

            provider.setMenuRequestContext(
                    new MenuProviderTest.TestMenuRequestContext(isZenithConnectEnabled, roleList));
            mainMenu = provider.getMainMenu();
        } catch (Exception e) {
            Assert.fail("Error in MenuProvider.getMainMenu: " + e.getMessage());
        }

        return mainMenu;
    }

    private MenuEntry getMenuEntry(List<MenuEntry> menus, String menuName) {
        Optional<MenuEntry> menuOpt = menus.stream().filter(m -> m.name != null && m.name.equals(menuName)).findFirst();
        assertTrue(menuOpt.isPresent());

        MenuEntry menuEntry = menuOpt.get();
        assertEquals(menuName, menuEntry.name);

        return menuEntry;
    }

    private List<String> getMenuNames(MenuEntry menu) {
        return menu.items.stream().map(m -> m.name).toList();
    }

    public static class TestMenuRequestContext implements MenuRequestContext {
        public TestMenuRequestContext(boolean isZenithConnectEnabled, List<String> roles) {
            this.isZenithConnectEnabled = isZenithConnectEnabled;

            if (roles != null && !roles.isEmpty()) {
                this.userRoles.addAll(roles);
            }
        }

        final private boolean isZenithConnectEnabled;

        final private List<String> userRoles = new ArrayList<>();

        public String getRemoteUser() {
            return "admin1";
        }

        public String calculateUrlBase() {
            return "/opennms/";
        }

        public boolean isUserInRole(String role) {
            return userRoles.stream().anyMatch(r -> r.equals(role));
        }

        public boolean isUserInAnyRole(List<String> roles) {
            return roles.stream().anyMatch(this::isUserInRole);
        }

        public String getFormattedDateTime() {
            return "2025-03-01T20:30:00.000Z";
        }

        public String getFormattedDate() {
            return "2025-03-01";
        }

        public String getFormattedTime() {
            return "20:30:00 UTC+00";
        }

        public String getNoticeStatus() {
            return "On";
        }

        public String getSystemProperty(String name, String def) {
            switch (name) {
                case MenuProvider.ZENITH_CONNECT_ENABLED_KEY -> {
                    return this.isZenithConnectEnabled ? "true": "false";
                }
                case MenuProvider.ZENITH_CONNECT_BASE_URL_KEY -> {
                    return this.isZenithConnectEnabled ? "https://zenith.opennms.com" : "";
                }
                case MenuProvider.ZENITH_CONNECT_RELATIVE_URL_KEY -> {
                    return this.isZenithConnectEnabled ? "/zenith-connect" : "";
                }
            }

            return "";
        }
    }
}
