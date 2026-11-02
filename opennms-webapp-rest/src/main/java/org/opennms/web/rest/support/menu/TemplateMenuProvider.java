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

import javax.servlet.ServletContext;

import org.opennms.web.navigate.DefaultMenuEntry;
import org.opennms.web.navigate.DisplayStatus;
import org.opennms.web.navigate.MenuContext;
import org.opennms.web.rest.support.menu.model.MainMenu;
import org.opennms.web.rest.support.menu.model.MenuEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;

/**
 * OSGi-exported {@link org.opennms.web.api.MenuProvider} backed by menu-template.json.
 * Supplies a flat list of the clickable menu entries to the search "Open ..." actions.
 */
public class TemplateMenuProvider implements org.opennms.web.api.MenuProvider, ServletContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(TemplateMenuProvider.class);

    private static final String WEB_INF = "/WEB-INF";

    private final String menuTemplateFilePath;
    private volatile ServletContext servletContext;

    public TemplateMenuProvider(String menuTemplateFilePath) {
        this.menuTemplateFilePath = menuTemplateFilePath;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public List<org.opennms.web.navigate.MenuEntry> getMenu(MenuContext context) {
        final String path = resolvePath();
        final MainMenu mainMenu = new MenuProvider(path).parseMenuTemplate(path);

        if (mainMenu == null) {
            LOG.warn("Unable to parse menu template '{}'; no menu actions available", path);
            return new ArrayList<>();
        }

        final MenuFilterContext filterContext = new MenuFilterContext() {
            @Override
            public boolean isUserInAnyRole(List<String> roles) {
                return roles != null && roles.stream().anyMatch(context::isUserInRole);
            }

            @Override
            public String getSystemProperty(String name, String def) {
                return System.getProperty(name, def);
            }
        };

        final List<org.opennms.web.navigate.MenuEntry> result = new ArrayList<>();
        collect(MenuEntryFilter.filter(mainMenu.menus, filterContext), true, result);
        return result;
    }

    private String resolvePath() {
        final ServletContext sc = this.servletContext;

        if (sc == null) {
            return menuTemplateFilePath;
        }

        final String realWebInf = sc.getRealPath("/WEB-INF");

        if (realWebInf == null) {
            // e.g. unexpanded WAR
            LOG.warn("Could not determine real path of /WEB-INF; using configured menu template path '{}'", menuTemplateFilePath);
            return menuTemplateFilePath;
        }

        // The placeholder may already have been resolved by Spring, so re-root on the WEB-INF segment itself.
        final int idx = menuTemplateFilePath.lastIndexOf(WEB_INF);

        if (idx < 0) {
            return menuTemplateFilePath;
        }

        return realWebInf + menuTemplateFilePath.substring(idx + WEB_INF.length());
    }

    private static void collect(List<MenuEntry> entries, boolean topLevel, List<org.opennms.web.navigate.MenuEntry> result) {
        if (entries == null) {
            return;
        }

        for (MenuEntry e : entries) {
            if (isClickable(e, topLevel)) {
                result.add(new DefaultMenuEntry(e.name, e.url, DisplayStatus.DISPLAY_LINK,
                        Boolean.TRUE.equals(e.isExternalLink), e.linkTarget));
            }

            collect(e.items, false, result);
        }
    }

    private static boolean isClickable(MenuEntry e, boolean topLevel) {
        final String type = e.type;

        if (MenuEntry.MenuEntryTypes.TYPE_SEPARATOR.equals(type) || "header".equals(type) || "plugins".equals(type)) {
            return false;
        }

        if (e.url == null || e.url.isEmpty() || "#".equals(e.url)) {
            return false;
        }

        if (e.action != null && !"link".equals(e.action)) {
            return false;
        }

        return !topLevel || "link".equals(e.action);
    }
}
