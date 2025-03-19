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

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Strings;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.startsWith;
import org.opennms.web.rest.support.menu.xml.MenuXml;

public class MenuProviderTest {
    final static String RESOURCE_PATH = "src/test/resources/dispatcher-servlet.xml";

    @Test
    public void testParseBeansXml() {
        MenuProvider provider = new MenuProvider(getResourcePath());
        provider.setMenuRequestContext(new TestMenuRequestContext());

        MenuXml.BeansElement xBeansElem = null;

        try (var inputStream = new FileInputStream(getResourcePath())) {
            xBeansElem = provider.parseDispatcherServletXml(inputStream);
        } catch (Exception e) {
            Assert.fail("Could not open file resource: " + e.getMessage());
        }

        assertNotNull(xBeansElem);

        List<MenuXml.BeanElement> topLevelBeans = xBeansElem.getBeans();

        Optional<MenuXml.BeanElement> navBarBean = topLevelBeans.stream()
            .filter(e -> e.getId() != null && e.getId().equals("navBarEntries"))
            .findFirst();

        List<TopMenuEntry> topMenuEntries = null;

        if (navBarBean.isPresent()) {
            try {
                topMenuEntries = provider.parseXmlToMenuEntries(xBeansElem);
            } catch (Exception e) {
                Assert.fail("Error parsing XML to MenuEntries: " + e.getMessage());
            }
        }

        assertNotNull(topMenuEntries);
        assertFalse(topLevelBeans.isEmpty());
    }

    @Test
    public void testParseMainMenu() {
        MainMenu mainMenu = null;

        try {
            MenuProvider provider = new MenuProvider(getResourcePath());
            provider.setMenuRequestContext(new TestMenuRequestContext());
            mainMenu = provider.getMainMenu();
        } catch (Exception e) {
            Assert.fail("Error in MenuProvider.getMainMenu: " + e.getMessage());
        }

        assertNotNull(mainMenu);

        assertEquals("/opennms/", mainMenu.baseHref);
        assertEquals("/opennms/index.jsp", mainMenu.homeUrl);
        assertEquals("2025-03-01T20:30:00.000Z", mainMenu.formattedTime);
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
        assertEquals(6, mainMenu.menus.size());

        List<String> menuNames = mainMenu.menus.stream().map(m -> m.name).toList();

        final String[] expectedMenuNames = new String[] {
            "Search", "Info", "Status", "Reports", "Dashboards", "Maps"
        };
        assertThat(menuNames, containsInAnyOrder(expectedMenuNames));

        assertNotNull(mainMenu.configurationMenu);
        assertNotNull(mainMenu.flowsMenu);
        assertNotNull(mainMenu.helpMenu);
        assertNotNull(mainMenu.provisionMenu);
        assertNotNull(mainMenu.selfServiceMenu);
        assertNotNull(mainMenu.userNotificationMenu);

        // Check names in sub-menus under the Info menu
        Optional<TopMenuEntry> infoMenuOpt = mainMenu.menus.stream().filter(m -> m.name.equals("Info")).findFirst();
        assertTrue(infoMenuOpt.isPresent());

        TopMenuEntry infoMenu = infoMenuOpt.get();

        assertEquals("Info", infoMenu.name);

        List<String> infoEntryNames = infoMenu.items.stream().map(m -> m.name).toList();
        assertEquals(10, infoEntryNames.size());

        final String[] expectedInfoNames = new String[] {
                "Nodes", "Assets", "Path Outages", "Device Configs", "External Requisitions",
                "File Editor", "Logs", "Endpoints", "Secure Credentials Vault", "Connect to Zenith"
        };
        assertThat(infoEntryNames, containsInAnyOrder(expectedInfoNames));

        // Check Roles on Device Config entry
        Optional<MenuEntry> deviceConfigMenuOpt = infoMenu.items.stream().filter(m -> m.name.equals("Device Configs")).findFirst();
        assertTrue(deviceConfigMenuOpt.isPresent());
        assertEquals("ROLE_ADMIN,ROLE_REST,ROLE_DEVICE_CONFIG_BACKUP", deviceConfigMenuOpt.get().roles);
    }

    private String getResourcePath() {
        Path p = Paths.get(RESOURCE_PATH);
        return p.toFile().getAbsolutePath();
    }

    public static class TestMenuRequestContext implements MenuRequestContext {
        public String getRemoteUser() {
            return "admin1";
        }

        public String calculateUrlBase() {
            return "/opennms/";
        }

        public boolean isUserInRole(String role) {
            return true;
        }

        public String getFormattedTime() {
            return "2025-03-01T20:30:00.000Z";
        }

        public String getNoticeStatus() {
            return "On";
        }

        public String getSystemProperty(String name, String def) {
            switch (name) {
                case MenuProvider.ZENITH_CONNECT_ENABLED_KEY -> {
                    return "true";
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
