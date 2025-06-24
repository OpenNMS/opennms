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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opennms.core.resource.Vault;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.support.menu.model.MainMenu;
import org.opennms.web.rest.support.menu.model.MenuEntry;
import org.opennms.web.rest.support.menu.model.TileProviderItem;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

/**
 * Creates a MainMenu object that is used by the MenuRestService, which provides this data to the
 * new Vue UI Menubar.
 *
 * Reads menu-template.json to determine most of the menu structure. Makes additional modifications
 * due to runtime data and role or system property evaluation.
 */
public class MenuProvider {
    /** Fully qualified classname of RoleBasedNavBarEntry class, from opennms-webapp. */
    private final String ROLE_BASED_NAV_BAR_ENTRY_CLASS = "org.opennms.web.navigate.RoleBasedNavBarEntry";

    private final String ADMIN_ROLE_ICON = "fa-cogs";

    public static final String ZENITH_CONNECT_ENABLED_KEY = "opennms.zenithConnect.enabled";
    public static final String ZENITH_CONNECT_BASE_URL_KEY = "opennms.zenithConnect.zenithBaseUrl";
    public static final String ZENITH_CONNECT_RELATIVE_URL_KEY = "opennms.zenithConnect.zenithRelativeUrl";

    private static final ImmutableSet<String> ADMIN_ROLES = ImmutableSet.of(
        Authentication.ROLE_ADMIN,
        Authentication.ROLE_FILESYSTEM_EDITOR
    );

    /** Full file path to menu-template.json file, see "applicationContext-cxf-rest-v2.xml" */
    private String menuTemplateFilePath;

    /** Context that must be set before using the MenuProvider. */
    private MenuRequestContext menuRequestContext;

    public MenuProvider(String dispatcherServletPath, String menuTemplateFilePath) {
        this.menuTemplateFilePath = menuTemplateFilePath;
    }

    public void setMenuRequestContext(MenuRequestContext context) {
        this.menuRequestContext = context;
    }

    public String getDispatcherServletPath() {
        return "";
    }

    public void setDispatcherServletPath(String path) {
    }

    public String getMenuTemplateFilePath() {
        return this.menuTemplateFilePath;
    }

    public void setMenuTemplateFilePath(String path) {
        this.menuTemplateFilePath = path;
    }

    public MainMenu getMainMenu() throws Exception, IOException {
        ensureContext();

        MainMenu mainMenu = generateMenuFromTemplate();

        return mainMenu;
    }

    public MainMenu generateMenuFromTemplate() throws Exception, IOException {
        return generateMenuFromTemplate(null);
    }

    public MainMenu generateMenuFromTemplate(String menuTemplateFile) throws Exception, IOException {
        final String templatePath = menuTemplateFile != null ? menuTemplateFile : this.menuTemplateFilePath;

        MainMenu mainMenu = parseMenuTemplate(templatePath);

        if (mainMenu == null) {
            return null;
        }

        try {
            mainMenu.baseHref = menuRequestContext.calculateUrlBase();
            mainMenu.homeUrl = mainMenu.baseHref + "index.jsp";
            mainMenu.formattedTime = menuRequestContext.getFormattedTime();
            mainMenu.username = menuRequestContext.getRemoteUser();
            // for navigating to a specific node id
            mainMenu.baseNodeUrl = "element/node.jsp?node=";

            mainMenu.zenithConnectEnabled =
                    Strings.nullToEmpty(menuRequestContext.getSystemProperty(ZENITH_CONNECT_ENABLED_KEY, "false")).equals("true");
            mainMenu.zenithConnectBaseUrl = menuRequestContext.getSystemProperty(ZENITH_CONNECT_BASE_URL_KEY, "");
            mainMenu.zenithConnectRelativeUrl = menuRequestContext.getSystemProperty(ZENITH_CONNECT_RELATIVE_URL_KEY, "");

            mainMenu.noticeStatus = menuRequestContext.getNoticeStatus();

            mainMenu.copyrightDates = String.format("2002-%d", LocalDate.now().getYear());
            mainMenu.version = Vault.getProperty("version.display");

            var tileProviders = getTileProviders();

            if (!tileProviders.isEmpty()) {
                mainMenu.userTileProviders.clear();
                mainMenu.userTileProviders.addAll(tileProviders);
            }

            // Remove any MenuEntry items marked as RoleBased where user does not have any required role
            List<MenuEntry> filteredTopMenuEntries = filterMenuEntriesByRole(mainMenu.menus);
            filteredTopMenuEntries = filterMenuEntriesBySystemProperties(filteredTopMenuEntries);

            mainMenu.menus.clear();
            mainMenu.menus.addAll(filteredTopMenuEntries);

            // These are mostly in the template now
            // May have to remove some depending on Admin role, etc.
            mainMenu.helpMenu.items = filterMenuEntriesByRole(mainMenu.helpMenu.items);

            mainMenu.selfServiceMenu.name = menuRequestContext.getRemoteUser();

            // User notification User menu needs the username in the url
            // TODO: user should be url encoded
            MenuEntry userMenu = mainMenu.userNotificationMenu.items.stream()
                    .filter(i -> i.id != null && i.id.equals("userNotificationUser"))
                    .findFirst().orElse(null);

            if (userMenu != null && userMenu.url != null) {
                userMenu.url = userMenu.url.replace("$USER", menuRequestContext.getRemoteUser());
            }

            // Template should include ROLE_ADMIN and ROLE_PROVISION
            if (!evaluateRoleBasedMenuEntry(mainMenu.provisionMenu)) {
                mainMenu.provisionMenu = null;
            }

            if (!evaluateRoleBasedMenuEntry(mainMenu.flowsMenu)) {
                mainMenu.flowsMenu = null;
            }

            if (!evaluateRoleBasedMenuEntry(mainMenu.configurationMenu)) {
                mainMenu.configurationMenu = null;
            }
        } catch (IOException ioe) {
            throw ioe;
        }

        return mainMenu;
    }

    public MainMenu parseMenuTemplate() {
        return parseMenuTemplate(null);
    }

    public MainMenu parseMenuTemplate(String menuTemplateFile) {
        final String MENU_TEMPLATE_FILE = "./menu-template.json";

        var objectMapper = new ObjectMapper();
        MainMenu template = null;
        String path = menuTemplateFile != null ? menuTemplateFile : MENU_TEMPLATE_FILE;

        try (var fis = new FileInputStream(path)) {
            template = objectMapper.readValue(fis, MainMenu.class);
        } catch (FileNotFoundException fnfe) {
            // TODO: Log error and return null
            System.out.println("ERROR: FileNotFoundException: " + fnfe.getMessage());
            return null;
        } catch (DatabindException dbex) {
            System.out.println("ERROR: DatabindException: " + dbex.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("ERROR: Exception: " + e.getMessage());
            return null;
        }

        return template;
    }

    private void ensureContext() throws Exception {
        if (this.menuRequestContext == null) {
            throw new Exception("Must set MenuRequestContext when using a MenuProvider.");
        }
    }

    private List<MenuEntry> filterMenuEntriesByRole(List<MenuEntry> entries) {
        // remove any top-level menu entries that have roles that the user is not part of
        List<MenuEntry> filteredEntries = entries.stream()
            .filter(this::evaluateRoleBasedMenuEntry).toList();

        // If the entry has child items, filter those
        for (MenuEntry filteredEntry : filteredEntries) {
            if (filteredEntry.items != null && !filteredEntry.items.isEmpty()) {
                filteredEntry.items = filterMenuEntriesByRole(filteredEntry.items);
            }
        }

        return filteredEntries;
    }

    private List<MenuEntry> filterMenuEntriesBySystemProperties(List<MenuEntry> entries) {
        // remove any top-level menu entries where the required system properties do not match
        List<MenuEntry> filteredEntries = entries.stream()
            .filter(this::evaluateRequiredSystemProperties).toList();

        // If the entry has child items, filter those
        for (MenuEntry filteredEntry : filteredEntries) {
            if (filteredEntry.items != null && !filteredEntry.items.isEmpty()) {
                filteredEntry.items = filterMenuEntriesBySystemProperties(filteredEntry.items);
            }
        }

        return filteredEntries;
    }

    private boolean evaluateRoleBasedMenuEntry(MenuEntry menuEntry) {
        if (menuEntry.roles != null && !menuEntry.roles.isEmpty()) {
            return menuRequestContext.isUserInAnyRole(menuEntry.roles);
        }

       return true;
    }

    private boolean evaluateRequiredSystemProperties(MenuEntry menuEntry) {
        if (menuEntry.requiredSystemProperties != null && !menuEntry.requiredSystemProperties.isEmpty()) {
            return menuEntry.requiredSystemProperties.stream()
                    .allMatch(p -> evaluateSystemPropertyMenuEntry(p.name, p.value));
        }

        return true;
    }

    /**
     * Evaluate any required system properties to determine whether to add this menu item or not.
     */
    private boolean evaluateSystemPropertyMenuEntry(String systemProperty, String systemPropertyValue) {
        if (!Strings.isNullOrEmpty(systemProperty) && !Strings.isNullOrEmpty(systemPropertyValue)) {
            String value = menuRequestContext.getSystemProperty(systemProperty, "");

            return !Strings.isNullOrEmpty(value) && value.equals(systemPropertyValue);
        }

        return true;
    }

    /**
     * Get a list of user-defined Tile Providers. Currently we only actually support a single user-defined one.
     * These are specified in the `opennms/etc/opennms.properties' file.
     * The tile providers are used in the Vue Geographical Map, for example if the user wants to specify
     * a map tile provider server in their own private network.
     * If 'userDefinedAsDefault' is true, the user-defined tile provider will appear first on the Geographical Map
     * and be loaded by default.
     */
    private List<TileProviderItem> getTileProviders() throws Exception {
        ensureContext();

        final var list = new ArrayList<TileProviderItem>();
        final String name = menuRequestContext.getSystemProperty("gwt.openlayers.name", "");
        final String url = menuRequestContext.getSystemProperty("gwt.openlayers.url", "");
        final String attribution = menuRequestContext.getSystemProperty("gwt.openlayers.options.attribution", "");
        final String userDefinedAsDefault = menuRequestContext.getSystemProperty("gwt.openlayers.userDefinedAsDefault", "");

        if (!Strings.isNullOrEmpty(url)) {
            var item = new TileProviderItem();
            item.name = !Strings.isNullOrEmpty(name) ? name : "User-Defined";
            item.url = url;
            item.attribution = !Strings.isNullOrEmpty(attribution) ? attribution : "";
            item.userDefinedAsDefault = !Strings.isNullOrEmpty(userDefinedAsDefault) && userDefinedAsDefault.equals("true");

            list.add(item);
        }

        return list;
    }
}
