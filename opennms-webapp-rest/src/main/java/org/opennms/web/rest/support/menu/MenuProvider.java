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
import java.util.List;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opennms.core.resource.Vault;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.support.menu.model.MainMenu;
import org.opennms.web.rest.support.menu.model.MenuEntry;

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
    private final String ADMIN_ROLE_ICON = "fa-cogs";

    public static final String ZENITH_CONNECT_ENABLED_KEY = "opennms.zenithConnect.enabled";
    public static final String ZENITH_CONNECT_BASE_URL_KEY = "opennms.zenithConnect.zenithBaseUrl";
    public static final String ZENITH_CONNECT_RELATIVE_URL_KEY = "opennms.zenithConnect.zenithRelativeUrl";

    private static final ImmutableSet<String> ADMIN_ROLES = ImmutableSet.of(
        Authentication.ROLE_ADMIN,
        Authentication.ROLE_FILESYSTEM_EDITOR
    );

    /** Full file path to menu-template.json file, see "applicationContext-cxf-rest-v2.xml" */
    private final String menuTemplateFilePath;

    public MenuProvider(String menuTemplateFilePath) {
        this.menuTemplateFilePath = menuTemplateFilePath;
    }

    public String getMenuTemplateFilePath() {
        return this.menuTemplateFilePath;
    }

    /**
     * Build the menu for one request. Stateless: all per-request data comes from the arguments.
     */
    public MainMenu getMainMenu(MenuRequestContext menuRequestContext, String templatePath) throws Exception {
        MainMenu mainMenu = parseMenuTemplate(templatePath);

        if (mainMenu == null) {
            return null;
        }

        // The following should be set by the template. We do not override them here.
        // templateName, displayAddNodeButton, sideMenuInitialExpand

        mainMenu.baseHref = menuRequestContext.calculateUrlBase();
        mainMenu.homeUrl = mainMenu.baseHref + "index.jsp";
        mainMenu.formattedDateTime = menuRequestContext.getFormattedDateTime();
        mainMenu.formattedDate = menuRequestContext.getFormattedDate();
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

        List<MenuEntry> filteredMenus = MenuEntryFilter.filter(mainMenu.menus, menuRequestContext);
        mainMenu.menus.clear();
        mainMenu.menus.addAll(filteredMenus);

        // These are mostly in the template now
        // May have to remove some depending on Admin role, etc.
        mainMenu.helpMenu.items = MenuEntryFilter.filter(mainMenu.helpMenu.items, menuRequestContext);

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
        if (!MenuEntryFilter.isVisible(mainMenu.provisionMenu, menuRequestContext)) {
            mainMenu.provisionMenu = null;
        }

        if (!MenuEntryFilter.isVisible(mainMenu.configurationMenu, menuRequestContext)) {
            mainMenu.configurationMenu = null;
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
}
