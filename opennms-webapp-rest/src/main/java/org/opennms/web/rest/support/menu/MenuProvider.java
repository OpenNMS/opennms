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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opennms.core.resource.Vault;
import org.opennms.web.api.Authentication;
import org.opennms.web.api.ElementConstants;
import org.opennms.web.rest.support.menu.model.MainMenu;
import org.opennms.web.rest.support.menu.model.MenuEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger(MenuProvider.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String ADMIN_ROLE_ICON = "fa-cogs";

    public static final String ZENITH_CONNECT_ENABLED_KEY = "opennms.zenithConnect.enabled";
    public static final String ZENITH_CONNECT_BASE_URL_KEY = "opennms.zenithConnect.zenithBaseUrl";
    public static final String ZENITH_CONNECT_RELATIVE_URL_KEY = "opennms.zenithConnect.zenithRelativeUrl";

    /** Tokens in template URLs, replaced with the docs site product and version (e.g. horizon/36). */
    public static final String PRODUCT_TOKEN = "$PRODUCT";
    public static final String DOCS_VERSION_TOKEN = "$DOCS_VERSION";

    private static final ImmutableSet<String> ADMIN_ROLES = ImmutableSet.of(
        Authentication.ROLE_ADMIN,
        Authentication.ROLE_FILESYSTEM_EDITOR
    );

    public MenuProvider() {
    }

    /**
     * Kept so that an applicationContext-cxf-rest-v2.xml from an earlier release, which passes the template
     * path here, still loads (it is a package conffile, so an edited copy can survive an upgrade).
     * The path is ignored: MenuTemplatePaths decides which file is read.
     */
    @Deprecated
    public MenuProvider(String ignoredMenuTemplateFilePath) {
        this();
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
        // The Vue Node Details page. No OpenNMS Vue code reads this any more (it links through the
        // router), but plugins may. Callers append the node id.
        mainMenu.baseNodeUrl = ElementConstants.NODE_DETAILS_URI;

        mainMenu.zenithConnectEnabled =
                Strings.nullToEmpty(menuRequestContext.getSystemProperty(ZENITH_CONNECT_ENABLED_KEY, "false")).equals("true");
        mainMenu.zenithConnectBaseUrl = menuRequestContext.getSystemProperty(ZENITH_CONNECT_BASE_URL_KEY, "");
        mainMenu.zenithConnectRelativeUrl = menuRequestContext.getSystemProperty(ZENITH_CONNECT_RELATIVE_URL_KEY, "");

        mainMenu.noticeStatus = menuRequestContext.getNoticeStatus();

        mainMenu.copyrightDates = copyrightDates();
        mainMenu.version = Vault.getProperty("version.display");
        mainMenu.product = Strings.emptyToNull(Vault.getProperty("opennms.product"));
        mainMenu.docsVersion = Strings.emptyToNull(Vault.getProperty("opennms.docs.version"));

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

        replaceDocsTokens(mainMenu.menus, mainMenu.product, mainMenu.docsVersion);
        replaceDocsTokens(Arrays.asList(mainMenu.helpMenu, mainMenu.selfServiceMenu, mainMenu.userNotificationMenu,
                mainMenu.provisionMenu, mainMenu.configurationMenu), mainMenu.product, mainMenu.docsVersion);

        return mainMenu;
    }

    /** The copyright range shown in the footer and on the About page, from the server's clock. */
    public static String copyrightDates() {
        return String.format("2002-%d", LocalDate.now().getYear());
    }

    /**
     * Replaces {@link #PRODUCT_TOKEN} and {@link #DOCS_VERSION_TOKEN} in the URLs of these entries and their
     * descendants. {@code product} and {@code docsVersion} are the raw values (null when unknown); the docs site
     * has no unversioned product root, so an unknown product falls back to the Horizon docs rather than a broken link.
     */
    public static void replaceDocsTokens(List<MenuEntry> entries, String product, String docsVersion) {
        final String docsProduct = product != null ? product : "horizon";
        final String version = product != null && docsVersion != null ? docsVersion : "latest";
        substituteDocsTokens(entries, docsProduct, version);
    }

    /** {@link #replaceDocsTokens(List, String, String)} with the product and docs version of this server. */
    public static void replaceDocsTokens(List<MenuEntry> entries) {
        replaceDocsTokens(entries, Strings.emptyToNull(Vault.getProperty("opennms.product")),
                Strings.emptyToNull(Vault.getProperty("opennms.docs.version")));
    }

    private static void substituteDocsTokens(List<MenuEntry> entries, String product, String docsVersion) {
        if (entries == null) {
            return;
        }
        for (MenuEntry entry : entries) {
            if (entry == null) {
                continue;
            }
            if (entry.url != null) {
                entry.url = entry.url.replace(PRODUCT_TOKEN, product).replace(DOCS_VERSION_TOKEN, docsVersion);
            }
            substituteDocsTokens(entry.items, product, docsVersion);
        }
    }

    /** Reads a menu template. Returns null, after logging why, when it is missing or invalid. */
    public static MainMenu parseMenuTemplate(String menuTemplateFile) {
        try (var fis = new FileInputStream(menuTemplateFile)) {
            return OBJECT_MAPPER.readValue(fis, MainMenu.class);
        } catch (Exception e) {
            LOG.warn("Unable to read menu template '{}': {}", menuTemplateFile, e.getMessage(), e);
            return null;
        }
    }
}
