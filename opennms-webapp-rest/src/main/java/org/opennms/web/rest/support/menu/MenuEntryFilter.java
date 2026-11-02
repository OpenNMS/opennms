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
import java.util.List;

import org.opennms.web.rest.support.menu.model.MenuEntry;

import com.google.common.base.Strings;

/**
 * Filters menu entries by role and required system properties, then prunes groups that end up
 * empty and tidies separators. Stateless; does not modify its input.
 */
public final class MenuEntryFilter {
    private static final String LINK_ACTION = "link";

    private MenuEntryFilter() {
    }

    public static List<MenuEntry> filter(List<MenuEntry> entries, MenuFilterContext ctx) {
        return filter(entries, ctx, true);
    }

    private static List<MenuEntry> filter(List<MenuEntry> entries, MenuFilterContext ctx, boolean topLevel) {
        List<MenuEntry> result = new ArrayList<>();

        if (entries == null) {
            return result;
        }

        for (MenuEntry entry : entries) {
            if (!isVisible(entry, ctx)) {
                continue;
            }

            MenuEntry copy = copyOf(entry);
            boolean hadItems = entry.items != null && !entry.items.isEmpty();

            if (hadItems) {
                copy.items = filter(entry.items, ctx, false);

                if (copy.items.isEmpty() && !isNavigable(entry, topLevel)) {
                    continue;
                }
            }

            result.add(copy);
        }

        return trimSeparators(result);
    }

    /** Checks only this entry's roles and required system properties. */
    public static boolean isVisible(MenuEntry e, MenuFilterContext ctx) {
        if (e.roles != null && !e.roles.isEmpty() && !ctx.isUserInAnyRole(e.roles)) {
            return false;
        }

        if (e.requiredSystemProperties != null) {
            for (MenuEntry.RequiredSystemProperty p : e.requiredSystemProperties) {
                if (Strings.isNullOrEmpty(p.name) || Strings.isNullOrEmpty(p.value)) {
                    continue;
                }

                if (!p.value.equals(ctx.getSystemProperty(p.name, ""))) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Whether an entry emptied by filtering still works as a link. The side menu only turns a
     * top-level entry into a link when its action is "link"; nested entries with a url are links.
     */
    private static boolean isNavigable(MenuEntry e, boolean topLevel) {
        final boolean hasUrl = !Strings.isNullOrEmpty(e.url) && !"#".equals(e.url);

        if (topLevel) {
            return hasUrl && LINK_ACTION.equals(e.action);
        }

        return hasUrl || LINK_ACTION.equals(e.action);
    }

    private static boolean isSeparator(MenuEntry e) {
        return MenuEntry.MenuEntryTypes.TYPE_SEPARATOR.equals(e.type);
    }

    private static List<MenuEntry> trimSeparators(List<MenuEntry> entries) {
        List<MenuEntry> result = new ArrayList<>();

        for (MenuEntry e : entries) {
            if (isSeparator(e) && (result.isEmpty() || isSeparator(result.get(result.size() - 1)))) {
                continue;
            }

            result.add(e);
        }

        while (!result.isEmpty() && isSeparator(result.get(result.size() - 1))) {
            result.remove(result.size() - 1);
        }

        return result;
    }

    private static MenuEntry copyOf(MenuEntry e) {
        MenuEntry c = new MenuEntry();
        c.type = e.type;
        c.id = e.id;
        c.name = e.name;
        c.url = e.url;
        c.isExternalLink = e.isExternalLink;
        c.action = e.action;
        c.linkTarget = e.linkTarget;
        c.icon = e.icon;
        c.roles = e.roles;
        c.requiredSystemProperties = e.requiredSystemProperties;
        c.items = e.items;
        return c;
    }
}
