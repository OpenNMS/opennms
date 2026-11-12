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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import org.opennms.core.resource.Vault;
import org.opennms.web.navigate.DisplayStatus;
import org.opennms.web.navigate.MenuContext;
import org.opennms.web.navigate.MenuEntry;
import org.springframework.mock.web.MockServletContext;

public class TemplateMenuProviderTest {
    private static final String SHIPPED = "src/main/webapp/WEB-INF/menu/menu-template.json";

    private static final String TEST_PROP = "org.opennms.test.menuTemplateProviderTest.flag";

    @After
    public void clearProperty() {
        System.clearProperty(TEST_PROP);
    }

    private static Path writeTemplate(String json) throws Exception {
        Path dir = Files.createTempDirectory("menu-template-test");
        Path file = dir.resolve("WEB-INF/menu/menu-template.json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, json);
        file.toFile().deleteOnExit();
        return file;
    }

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
        MenuEntry chat = byName(menu(ctx(Set.of(), true)), "Source Code");
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
        // the top-level "Help" group links to the docs root; only its children are search results
        assertEquals(null, byName(entries, "Help"));
        MenuEntry about = byName(entries, "About");
        assertNotNull(about);
        assertEquals("ui/index.html#/about", about.getUrl());
    }

    @Test
    public void docsUrlTokensAreReplaced() {
        try {
            Vault.getProperties().setProperty("opennms.product", "meridian");
            Vault.getProperties().setProperty("opennms.docs.version", "2026");
            List<MenuEntry> entries = menu(ctx(Set.of(), true));
            MenuEntry restApi = byName(entries, "REST API Reference Documentation");
            assertNotNull(restApi);
            assertEquals("https://docs.opennms.com/meridian/2026/development/rest/rest-api.html", restApi.getUrl());
            for (MenuEntry e : entries) {
                assertFalse(e.getName(), e.getUrl().contains("$"));
            }
        } finally {
            Vault.getProperties().remove("opennms.product");
            Vault.getProperties().remove("opennms.docs.version");
        }
    }

    @Test
    public void missingTemplateYieldsEmptyList() {
        assertTrue(new TemplateMenuProvider("/nonexistent/menu-template.json").getMenu(ctx(Set.of(), true)).isEmpty());
    }

    @Test
    public void requiredSystemPropertiesAreHonored() throws Exception {
        Path file = writeTemplate("{\"menus\":[{\"name\":\"Gated\",\"url\":\"gated.jsp\",\"action\":\"link\","
                + "\"requiredSystemProperties\":[{\"name\":\"" + TEST_PROP + "\",\"value\":\"true\"}]}]}");
        TemplateMenuProvider provider = new TemplateMenuProvider(file.toString());

        assertEquals(null, byName(provider.getMenu(ctx(Set.of(), true)), "Gated"));

        System.setProperty(TEST_PROP, "true");
        assertNotNull(byName(provider.getMenu(ctx(Set.of(), true)), "Gated"));
    }

    @Test
    public void deeplyNestedLeafIsIncluded() throws Exception {
        Path file = writeTemplate("{\"menus\":[{\"name\":\"Top\",\"url\":\"#\",\"items\":["
                + "{\"name\":\"Mid\",\"url\":\"#\",\"items\":[{\"name\":\"Deep\",\"url\":\"deep.jsp\"}]}]}]}");
        List<MenuEntry> entries = new TemplateMenuProvider(file.toString()).getMenu(ctx(Set.of(), true));

        assertNotNull(byName(entries, "Deep"));
        assertEquals(1, entries.size());
    }

    @Test
    public void defaultReadsTheTemplateUnderTheServletContextWebInf() throws Exception {
        Path file = writeTemplate("{\"menus\":[{\"name\":\"Deployed\",\"url\":\"d.jsp\",\"action\":\"link\"}]}");
        Path webInf = file.getParent().getParent();
        MockServletContext sc = new MockServletContext() {
            @Override public String getRealPath(String path) { return "/WEB-INF".equals(path) ? webInf.toString() : null; }
        };
        TemplateMenuProvider provider = new TemplateMenuProvider();
        provider.setServletContext(sc);

        assertNotNull(byName(provider.getMenu(ctx(Set.of(), true)), "Deployed"));
    }
}
