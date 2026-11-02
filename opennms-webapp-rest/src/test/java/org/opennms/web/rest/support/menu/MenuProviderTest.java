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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Strings;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.startsWith;

import org.opennms.web.rest.support.menu.model.MainMenu;
import org.opennms.web.rest.support.menu.model.MenuEntry;

public class MenuProviderTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void parsesTemplateWithUnknownProperties() throws Exception {
        File file = tempFolder.newFile("menu-template.json");
        String json = "{\"menus\": [{\"name\": \"Info\", \"locationMatch\": \"x\", \"someFutureKey\": 1,"
                + " \"requiredSystemProperties\": [{\"name\": \"a\", \"value\": \"b\", \"another\": 2}]}],"
                + " \"unknownTopLevel\": true}";
        Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));

        MainMenu menu = new MenuProvider(null).parseMenuTemplate(file.getAbsolutePath());

        assertNotNull(menu);
        assertEquals(1, menu.menus.size());
        assertEquals("Info", menu.menus.get(0).name);
    }
    final static String MENU_TEMPLATE_PATH = "src/test/resources/menu/menu-template.json";

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
        assertNotNull(mainMenu.menus);
        assertEquals(8, mainMenu.menus.size());

        List<String> menuNames = mainMenu.menus.stream().map(m -> m.name).toList();

        final String[] expectedMenuNames = new String[] {
            "Search", "Info", "Status", "Reports", "Dashboards", "Maps", "Administration", "Support"
        };
        assertThat(menuNames, containsInAnyOrder(expectedMenuNames));

        assertNotNull(mainMenu.configurationMenu);
        assertNotNull(mainMenu.helpMenu);
        assertNotNull(mainMenu.provisionMenu);
        assertNotNull(mainMenu.selfServiceMenu);
        assertNotNull(mainMenu.userNotificationMenu);

        // Check names in sub-menus under the Info menu
        MenuEntry infoMenu = getMenuEntry(mainMenu.menus, "Info");

        final String[] expectedNames = new String[] {
                "Nodes", "Assets", "Path Outages", "Device Configs", "External Requisitions",
                "Logs", "Secure Credentials Vault", "Connect to Zenith"
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
        MenuEntry infoMenu = getMenuEntry(mainMenu.menus, "Info");

        final String[] expectedNames = new String[] {
                "Nodes", "Assets", "Path Outages", "Device Configs", "External Requisitions",
                "Logs", "Secure Credentials Vault"
        };

        List<String> actualNames = getMenuNames(infoMenu);
        assertEquals(expectedNames.length, actualNames.size());
        assertThat(actualNames, containsInAnyOrder(expectedNames));
    }

    @Test
    public void testParseMainMenuWithUserRole() {
        MainMenu mainMenu = parseMainMenu(false, USER_ROLES);
        assertNotNull(mainMenu);

        // Check names in sub-menus under the Info menu
        MenuEntry infoMenu = getMenuEntry(mainMenu.menus, "Info");

        // Should not have any admin-only roles
        final String[] expectedNames = new String[] {
            "Nodes", "Assets", "Path Outages"
        };

        List<String> actualNames = getMenuNames(infoMenu);
        assertEquals(expectedNames.length, actualNames.size());
        assertThat(actualNames, containsInAnyOrder(expectedNames));
    }

    @Test
    public void testParseMainMenuWithFileSystemEditorRole() {
        MainMenu mainMenu = parseMainMenu(false, FILESYSTEM_EDITOR_ROLES);
        assertNotNull(mainMenu);

        // Check names in sub-menus under the Info menu
        MenuEntry infoMenu = getMenuEntry(mainMenu.menus, "Info");

        // Should not have any admin-only roles, but should have file system manager roles
        final String[] expectedNames = new String[] {
            "Nodes", "Assets", "Path Outages", "File Editor"
        };

        List<String> actualNames = getMenuNames(infoMenu);
        assertEquals(expectedNames.length, actualNames.size());
        assertThat(actualNames, containsInAnyOrder(expectedNames));
    }

    @Test
    public void concurrentContextsDoNotLeak() throws Exception {
        final MenuProvider provider = new MenuProvider(MENU_TEMPLATE_PATH);
        final ExecutorService executor = Executors.newFixedThreadPool(8);

        try {
            List<Future<Boolean>> futures = new ArrayList<>();

            for (int i = 0; i < 200; i++) {
                final boolean admin = i % 2 == 0;
                futures.add(executor.submit(() -> {
                    var ctx = new TestMenuRequestContext(admin ? "admin1" : "user1", false,
                            Arrays.asList(admin ? "ROLE_ADMIN" : "ROLE_USER"));
                    MainMenu menu = provider.getMainMenu(ctx, MENU_TEMPLATE_PATH);
                    assertEquals(admin ? "admin1" : "user1", menu.username);
                    boolean hasLogs = getMenuNames(getMenuEntry(menu.menus, "Info")).contains("Logs");
                    assertEquals(admin, hasLogs);
                    return true;
                }));
            }

            for (Future<Boolean> f : futures) {
                assertTrue(f.get(30, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private MainMenu parseMainMenu(boolean isZenithConnectEnabled, String[] roles) {
        MainMenu mainMenu = null;
        List<String> roleList = Arrays.stream(roles).toList();

        try {
            MenuProvider provider = new MenuProvider(MENU_TEMPLATE_PATH);

            mainMenu = provider.getMainMenu(
                    new MenuProviderTest.TestMenuRequestContext("admin1", isZenithConnectEnabled, roleList),
                    MENU_TEMPLATE_PATH);
        } catch (Exception e) {
            Assert.fail("Error in MenuProvider.getMainMenu: " + e.getMessage());
        }

        return mainMenu;
    }

    private MenuEntry getMenuEntry(List<MenuEntry> menus, String menuName) {
        Optional<MenuEntry> menuOpt = menus.stream().filter(m -> m.name.equals(menuName)).findFirst();
        assertTrue(menuOpt.isPresent());

        MenuEntry menuEntry = menuOpt.get();
        assertEquals(menuName, menuEntry.name);

        return menuEntry;
    }

    private List<String> getMenuNames(MenuEntry menu) {
        return menu.items.stream().map(m -> m.name).toList();
    }

    public static class TestMenuRequestContext implements MenuRequestContext {
        public TestMenuRequestContext(String username, boolean isZenithConnectEnabled, List<String> roles) {
            this.username = username;
            this.isZenithConnectEnabled = isZenithConnectEnabled;

            if (roles != null && !roles.isEmpty()) {
                this.userRoles.addAll(roles);
            }
        }

        final private String username;

        final private boolean isZenithConnectEnabled;

        final private List<String> userRoles = new ArrayList<>();

        public String getRemoteUser() {
            return username;
        }

        public String calculateUrlBase() {
            return "/opennms/";
        }

        public boolean isUserInRole(String role) {
            return userRoles.isEmpty() || userRoles.stream().anyMatch(r -> r.equals(role));
        }

        public boolean isUserInAnyRole(List<String> roles) {
            if (userRoles.isEmpty()) {
                return true;
            }

            return userRoles.stream().anyMatch(userRole -> roles.stream().anyMatch(r -> r.equals(userRole)));
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
                    return "https://zenith.opennms.com";
                }
                case MenuProvider.ZENITH_CONNECT_RELATIVE_URL_KEY -> {
                    return "/zenith-connect";
                }
            }

            return Strings.nullToEmpty(System.getProperty(name, def));
        }
    }
}
