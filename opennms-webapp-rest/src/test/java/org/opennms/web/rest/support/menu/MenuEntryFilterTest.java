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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opennms.web.rest.support.menu.model.MenuEntry;

public class MenuEntryFilterTest {
    private static MenuFilterContext ctx(List<String> userRoles, Map<String, String> props) {
        return new MenuFilterContext() {
            @Override
            public boolean isUserInAnyRole(List<String> roles) {
                return roles.stream().anyMatch(userRoles::contains);
            }

            @Override
            public String getSystemProperty(String name, String def) {
                return props.getOrDefault(name, def);
            }
        };
    }

    private static final MenuFilterContext USER = ctx(List.of("ROLE_USER"), Map.of("a", "false"));

    private static MenuEntry entry(String name, String url) {
        MenuEntry e = new MenuEntry();
        e.name = name;
        e.url = url;
        return e;
    }

    private static MenuEntry adminOnly(String name) {
        MenuEntry e = entry(name, "x.jsp");
        e.roles = List.of("ROLE_ADMIN");
        return e;
    }

    private static MenuEntry separator() {
        MenuEntry e = new MenuEntry();
        e.type = MenuEntry.MenuEntryTypes.TYPE_SEPARATOR;
        return e;
    }

    private static MenuEntry group(String name, String url, MenuEntry... children) {
        MenuEntry e = entry(name, url);
        e.items = Arrays.asList(children);
        return e;
    }

    private static List<String> names(List<MenuEntry> entries) {
        return entries.stream().map(e -> e.type != null ? e.type : e.name).toList();
    }

    @Test
    public void dropsEntryWhenUserLacksAllRoles() {
        assertTrue(MenuEntryFilter.filter(List.of(adminOnly("A")), USER).isEmpty());
    }

    @Test
    public void keepsEntryWithNoRoles() {
        assertEquals(List.of("A"), names(MenuEntryFilter.filter(List.of(entry("A", "a.jsp")), USER)));
    }

    @Test
    public void dropsEntryWhenRequiredSystemPropertyDiffers() {
        MenuEntry e = entry("A", "a.jsp");
        MenuEntry.RequiredSystemProperty p = new MenuEntry.RequiredSystemProperty();
        p.name = "a";
        p.value = "true";
        e.requiredSystemProperties = List.of(p);

        assertTrue(MenuEntryFilter.filter(List.of(e), USER).isEmpty());
    }

    @Test
    public void dropsGroupWhoseChildrenAreAllFiltered() {
        assertTrue(MenuEntryFilter.filter(List.of(group("G", "#", adminOnly("child"))), USER).isEmpty());
    }

    @Test
    public void keepsLinkParentWithNoVisibleChildren() {
        MenuEntry parent = group("Maps", "ui/index.html#/map", adminOnly("child"));
        parent.action = "link";

        List<MenuEntry> result = MenuEntryFilter.filter(List.of(parent), USER);

        assertEquals(List.of("Maps"), names(result));
        assertTrue(result.get(0).items.isEmpty());
    }

    @Test
    public void dropsTopLevelGroupWithUrlButNoLinkActionAndNoVisibleChildren() {
        MenuEntry parent = group("Help", "https://docs.example.com", adminOnly("child"));

        assertTrue(MenuEntryFilter.filter(List.of(parent), USER).isEmpty());
    }

    @Test
    public void keepsTopLevelGroupWithLinkActionAndNoVisibleChildren() {
        MenuEntry parent = group("Help", "https://docs.example.com", adminOnly("child"));
        parent.action = "link";

        assertEquals(List.of("Help"), names(MenuEntryFilter.filter(List.of(parent), USER)));
    }

    @Test
    public void keepsNestedGroupWithUrlAndNoVisibleChildren() {
        MenuEntry nested = group("Nested", "nested.jsp", adminOnly("child"));
        MenuEntry top = group("Top", "#", nested);

        List<MenuEntry> result = MenuEntryFilter.filter(List.of(top), USER);

        assertEquals(List.of("Top"), names(result));
        assertEquals(List.of("Nested"), names(result.get(0).items));
    }

    @Test
    public void collapsesSeparators() {
        List<MenuEntry> in = List.of(separator(), entry("A", "a"), separator(), separator(), entry("B", "b"), separator());
        assertEquals(List.of("A", "separator", "B"), names(MenuEntryFilter.filter(in, USER)));

        List<MenuEntry> in2 = List.of(entry("A", "a"), separator(), group("G", "#", adminOnly("c")), separator(), entry("B", "b"));
        assertEquals(List.of("A", "separator", "B"), names(MenuEntryFilter.filter(in2, USER)));
    }

    private static MenuEntry withAction(MenuEntry e, String action) {
        e.action = action;
        return e;
    }

    @Test
    public void topLevelEntryIsALinkOnlyWithLinkAction() {
        assertTrue(MenuEntryFilter.isLink(withAction(entry("Maps", "ui/index.html#/map"), "link"), true));
        assertFalse(MenuEntryFilter.isLink(entry("Help", "https://docs.example.com"), true));
        assertFalse(MenuEntryFilter.isLink(withAction(entry("G", "#"), "link"), true));
    }

    @Test
    public void nestedEntryIsALinkWhenItHasAUrlAndNoOtherAction() {
        assertTrue(MenuEntryFilter.isLink(entry("A", "a.jsp"), false));
        assertTrue(MenuEntryFilter.isLink(withAction(entry("A", "a.jsp"), "link"), false));
        assertFalse(MenuEntryFilter.isLink(withAction(entry("Log Out", "logout.jsp"), "logout"), false));
        assertFalse(MenuEntryFilter.isLink(withAction(entry("NoUrl", null), "link"), false));
        assertFalse(MenuEntryFilter.isLink(entry("Hash", "#"), false));
        assertFalse(MenuEntryFilter.isLink(separator(), false));
    }

    @Test
    public void dropsNestedGroupWithLinkActionButNoUrlAndNoVisibleChildren() {
        MenuEntry nested = withAction(group("Nested", null, adminOnly("child")), "link");
        MenuEntry top = group("Top", "#", nested, entry("Other", "o.jsp"));

        assertEquals(List.of("Other"), names(MenuEntryFilter.filter(List.of(top), USER).get(0).items));
    }

    /** Fails when a field is added to MenuEntry but not to MenuEntryFilter.copyOf. */
    @Test
    public void copyOfCopiesEveryField() throws Exception {
        MenuEntry original = new MenuEntry();
        List<Field> fields = new ArrayList<>();

        for (Field f : MenuEntry.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) {
                continue;
            }
            f.setAccessible(true);
            f.set(original, sampleValue(f));
            fields.add(f);
        }

        MenuEntry copy = MenuEntryFilter.copyOf(original);

        for (Field f : fields) {
            assertNotNull(f.getName(), f.get(original));
            assertSame("MenuEntryFilter.copyOf does not copy MenuEntry." + f.getName(), f.get(original), f.get(copy));
        }
    }

    private static Object sampleValue(Field f) {
        if (f.getType() == String.class) {
            return new String(f.getName());
        }
        if (f.getType() == Boolean.class) {
            return Boolean.TRUE;
        }
        if (f.getType() == List.class) {
            return new ArrayList<>();
        }
        fail("copyOfCopiesEveryField has no sample value for MenuEntry." + f.getName() + " (" + f.getType() + ")");
        return null;
    }
}
