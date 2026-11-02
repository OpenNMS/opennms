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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opennms.web.navigate.DisplayStatus;
import org.opennms.web.navigate.MenuContext;
import org.opennms.web.navigate.MenuEntry;

public class TemplateMenuProviderTest {
    private static final String SHIPPED = "src/main/webapp/WEB-INF/menu/menu-template.json";

    private static MenuContext ctx(Set<String> roles, boolean all) {
        return new MenuContext() {
            @Override public String getLocation() { return null; }
            @Override public boolean isUserInRole(String role) { return all || roles.contains(role); }
        };
    }

    private static List<MenuEntry> menu(MenuContext c) {
        return new TemplateMenuProvider(SHIPPED).getMenu(c);
    }

    private static MenuEntry byName(List<MenuEntry> entries, String name) {
        return entries.stream().filter(e -> name.equals(e.getName())).findFirst().orElse(null);
    }

    @Test
    public void adminSeesAdminLeavesAsFlatList() {
        List<MenuEntry> entries = menu(ctx(Set.of(), true));
        assertNotNull(byName(entries, "Configure OpenNMS"));
        assertNotNull(byName(entries, "Manage Users"));
        assertNotNull(byName(entries, "Alarms"));
        for (MenuEntry e : entries) {
            assertTrue(e.getEntries().isEmpty());
            assertEquals(DisplayStatus.DISPLAY_LINK, e.getDisplayStatus());
        }
    }

    @Test
    public void plainUserDoesNotSeeAdminLeaves() {
        List<MenuEntry> entries = menu(ctx(Set.of("ROLE_USER"), false));
        assertFalse(entries.isEmpty());
        for (MenuEntry e : entries) {
            assertFalse(e.getUrl(), e.getUrl().startsWith("admin/"));
        }
        assertEquals(null, byName(entries, "Manage Users"));
    }

    @Test
    public void externalLinksAreFlagged() {
        MenuEntry chat = byName(menu(ctx(Set.of(), true)), "Chat");
        assertNotNull(chat);
        assertTrue(chat.isExternalLink());
        assertEquals("_blank", chat.getLinkTarget());
    }

    @Test
    public void skipsGroupsSeparatorsPluginsAndLogout() {
        for (MenuEntry e : menu(ctx(Set.of(), true))) {
            assertNotNull(e.getName(), e.getUrl());
            assertFalse(e.getName(), "#".equals(e.getUrl()));
            assertFalse(e.getName(), e.getUrl().contains("j_spring_security_logout"));
        }
    }

    @Test
    public void includesTopLevelLinkEntries() {
        MenuEntry map = byName(menu(ctx(Set.of(), true)), "Geographical Map");
        assertNotNull(map);
        assertEquals("ui/index.html#/map", map.getUrl());
    }

    @Test
    public void topLevelGroupWithUrlIsNotIncluded() {
        List<MenuEntry> entries = menu(ctx(Set.of(), true));
        for (MenuEntry e : entries) {
            assertFalse("https://docs.opennms.com/horizon/latest".equals(e.getUrl()));
        }
        MenuEntry about = byName(entries, "About");
        assertNotNull(about);
        assertEquals("about/index.jsp", about.getUrl());
    }

    @Test
    public void missingTemplateYieldsEmptyList() {
        assertTrue(new TemplateMenuProvider("/nonexistent/menu-template.json").getMenu(ctx(Set.of(), true)).isEmpty());
    }
}
