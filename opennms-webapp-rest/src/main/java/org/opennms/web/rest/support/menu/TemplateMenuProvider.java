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

    /** An explicit template file, or null to use MenuTemplatePaths. */
    private final String menuTemplateFilePath;
    private volatile ServletContext servletContext;

    public TemplateMenuProvider() {
        this(null);
    }

    /** Reads this template file instead of the webapp's active one. For tests. */
    TemplateMenuProvider(String menuTemplateFilePath) {
        this.menuTemplateFilePath = menuTemplateFilePath;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public List<org.opennms.web.navigate.MenuEntry> getMenu(MenuContext context) {
        final String path = resolvePath();
        final MainMenu mainMenu = MenuProvider.parseMenuTemplate(path);

        if (mainMenu == null) {
            LOG.warn("No search menu actions available, because menu template '{}' could not be read", path);
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

        final List<MenuEntry> visible = MenuEntryFilter.filter(mainMenu.menus, filterContext);
        MenuProvider.replaceDocsTokens(visible);

        final List<org.opennms.web.navigate.MenuEntry> result = new ArrayList<>();
        collect(visible, true, result);
        return result;
    }

    private String resolvePath() {
        return menuTemplateFilePath != null ? menuTemplateFilePath : MenuTemplatePaths.resolve(servletContext);
    }

    private static void collect(List<MenuEntry> entries, boolean topLevel, List<org.opennms.web.navigate.MenuEntry> result) {
        if (entries == null) {
            return;
        }

        for (MenuEntry e : entries) {
            if (MenuEntryFilter.isLink(e, topLevel)) {
                result.add(new DefaultMenuEntry(e.name, e.url, DisplayStatus.DISPLAY_LINK,
                        Boolean.TRUE.equals(e.isExternalLink), e.linkTarget));
            }

            collect(e.items, false, result);
        }
    }
}
