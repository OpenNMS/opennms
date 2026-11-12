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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.web.rest.support.menu.MenuProviderTest.TestMenuRequestContext;
import org.opennms.web.rest.support.menu.model.MainMenu;
import org.opennms.web.rest.support.menu.model.MenuEntry;

/**
 * Verifies the menu template that ships with the product.
 */
public class ShippedMenuTemplateTest {
    private static final String SHIPPED = "src/main/webapp/WEB-INF/menu/menu-template.json";
    private static final String DEFAULT = "src/main/webapp/WEB-INF/menu/menu-template-default.json";

    /*
     * Hand-kept copy of the admin-only pages, NOT derived from the code that enforces them. When you add an
     * admin-only page, add its URL here and "roles": ["ROLE_ADMIN"] to its template entry. Sources:
     * - Vue routes guarded by requireRole('adminRole', ...) in ui/src/main/router/index.ts
     * - URL rules in opennms-webapp/src/main/webapp/WEB-INF/applicationContext-spring-security.xml
     */
    private static final Set<String> ADMIN_ONLY_URLS = Set.of(
            "ui/index.html#/admin/users", "ui/index.html#/admin/groups", "ui/index.html#/admin/oncall-roles",
            "ui/index.html#/usage-statistics",
            "ui/index.html#/system-report", "ui/index.html#/logs", "ui/index.html#/scv",
            "ui/index.html#/distributed-monitoring", "ui/index.html#/scheduled-outages",
            "ui/index.html#/notifications-config", "ui/index.html#/mib-compiler",
            "ui/index.html#/admin/wsman-config", "ui/index.html#/configuration");

    private static MainMenu menuFor(String... roles) throws Exception {
        return new MenuProvider().getMainMenu(
                new TestMenuRequestContext("tester", false, Arrays.asList(roles)), SHIPPED);
    }

    private static void collect(List<MenuEntry> entries, List<MenuEntry> out) {
        if (entries == null) {
            return;
        }
        for (MenuEntry e : entries) {
            out.add(e);
            collect(e.items, out);
        }
    }

    private static List<MenuEntry> all(MainMenu menu) {
        List<MenuEntry> out = new ArrayList<>();
        collect(menu.menus, out);
        return out;
    }

    private static boolean isSeparator(MenuEntry e) {
        return MenuEntry.MenuEntryTypes.TYPE_SEPARATOR.equals(e.type);
    }

    private static boolean hasId(MainMenu menu, String id) {
        return all(menu).stream().anyMatch(e -> id.equals(e.id));
    }

    private static void assertSeparatorsTidy(List<MenuEntry> level) {
        if (level == null || level.isEmpty()) {
            return;
        }
        assertFalse("level starts with separator", isSeparator(level.get(0)));
        assertFalse("level ends with separator", isSeparator(level.get(level.size() - 1)));
        for (int i = 1; i < level.size(); i++) {
            assertFalse("two separators in a row", isSeparator(level.get(i)) && isSeparator(level.get(i - 1)));
        }
        level.forEach(e -> assertSeparatorsTidy(e.items));
    }

    @Test
    public void defaultTemplateIsIdenticalToShippedTemplate() throws Exception {
        assertArrayEquals(Files.readAllBytes(Paths.get(SHIPPED)), Files.readAllBytes(Paths.get(DEFAULT)));
    }

    @Test
    public void shippedTemplateHasNoLocationMatch() throws Exception {
        final String text = new String(Files.readAllBytes(Paths.get(SHIPPED)), StandardCharsets.UTF_8);
        assertFalse(text.contains("\"locationMatch\""));
    }

    @Test
    public void shippedTemplateForPlainUserHasNoAdminEntriesOrEmptyGroups() throws Exception {
        final MainMenu menu = menuFor("ROLE_USER");

        for (MenuEntry e : all(menu)) {
            if (e.url != null) {
                assertFalse("admin url visible: " + e.id, e.url.startsWith("admin/"));
                assertFalse("admin-only url visible: " + e.id, ADMIN_ONLY_URLS.contains(e.url));
            }
            boolean isGroup = (e.url == null || "#".equals(e.url)) && !isSeparator(e)
                    && e.type == null;
            if (isGroup) {
                assertTrue("empty group: " + e.id, e.items != null && !e.items.isEmpty());
            }
        }

        assertSeparatorsTidy(menu.menus);

        final List<String> topIds = menu.menus.stream().map(e -> e.id).collect(Collectors.toList());
        for (String id : new String[] {"dashboardsMenu", "inventoryMenu", "monitoringMenu", "administrationMenu"}) {
            assertTrue("missing " + id, topIds.contains(id));
        }
        final MenuEntry admin = menu.menus.stream().filter(e -> "administrationMenu".equals(e.id)).findFirst().get();
        final List<String> adminIds = admin.items.stream().map(e -> e.id).collect(Collectors.toList());
        assertTrue(adminIds.containsAll(Arrays.asList("notifications", "manageEventConfigurations", "manageSnmpDataCollection")));
    }

    @Test
    public void shippedTemplateForProvisionUserShowsRequisitionsButNotDeleteNodes() throws Exception {
        final MainMenu menu = menuFor("ROLE_USER", "ROLE_PROVISION");
        assertTrue(hasId(menu, "provisioningRequisitions"));
        assertTrue(hasId(menu, "addSingleNode"));
        assertFalse(hasId(menu, "deleteNodes"));
    }

    @Test
    public void shippedTemplateForAdminKeepsAdminEntries() throws Exception {
        final MainMenu menu = menuFor("ROLE_ADMIN");
        for (String id : new String[] {"surveillanceCategories", "manageUsers", "systemReport", "deleteNodes"}) {
            assertTrue("missing " + id, hasId(menu, id));
        }
    }
}
