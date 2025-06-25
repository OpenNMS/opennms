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
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.opennms.core.resource.Vault;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.support.menu.model.MainMenu;
import org.opennms.web.rest.support.menu.model.MenuEntry;
import org.opennms.web.rest.support.menu.model.TileProviderItem;
import org.opennms.web.rest.support.menu.xml.MenuXml;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

/**
 * Creates a MainMenu object that is used by the MenuRestService, which provides this data to the
 * new Vue UI Menubar.
 *
 * Reads opennms-webapp 'dispatcher-servlet.xml' to determine most of the menu structure, this allows old JSP and
 * new Vue Menubar to be in sync (note, there are some special case differences due to additional processing done
 * in navbar.ftl, etc.).
 *
 * This class does not convert items in 'dispatcher-servlet.xml' to actual Java Beans and evaluate them,
 * or read the full file, but reimplements just the needed functionality.
 *
 * MenuEntry is similar to NavBarEntry in opennms-webapp, but redefined and reimplemented here
 * partly to avoid 'opennms-webapp-rest' having a dependency on 'opennms-webapp'.
 *
 * This does some special-casing of role-based authentication (i.e. including or excluding menu entries based
 * on user roles), but will also respect items in 'dispatcher.servlet.xml' marked as 'RoleBasedNavBarEntry'.
 */
public class MenuProviderLegacy {
    public static final String ZENITH_CONNECT_ENABLED_KEY = "opennms.zenithConnect.enabled";
    public static final String ZENITH_CONNECT_BASE_URL_KEY = "opennms.zenithConnect.zenithBaseUrl";
    public static final String ZENITH_CONNECT_RELATIVE_URL_KEY = "opennms.zenithConnect.zenithRelativeUrl";

    private static final ImmutableSet<String> ADMIN_ROLES = ImmutableSet.of(
            Authentication.ROLE_ADMIN,
            Authentication.ROLE_FILESYSTEM_EDITOR
    );

    /** Full file path to dispatcher.servlet.xml file, see "applicationContext-cxf-rest-v2.xml" */
    private String dispatcherServletPath;

    /** Context that must be set before using the MenuProvider. */
    private MenuRequestContext menuRequestContext;

    public MenuProviderLegacy(String dispatcherServletPath, String menuTemplateFilePath) {
        this.dispatcherServletPath = dispatcherServletPath;
    }

    public void setMenuRequestContext(MenuRequestContext context) {
        this.menuRequestContext = context;
    }

    public String getDispatcherServletPath() {
        return this.dispatcherServletPath;
    }

    public void setDispatcherServletPath(String path) {
        this.dispatcherServletPath = path;
    }

    public String getMenuTemplateFilePath() {
        return "";
    }

    public void setMenuTemplateFilePath(String path) {
    }

    public MainMenu getMainMenu() throws Exception, IOException {
        ensureContext();
        MainMenu mainMenu = new MainMenu();

        try {
            final boolean isProvision = menuRequestContext.isUserInRole(Authentication.ROLE_PROVISION);
            final boolean isFlow = menuRequestContext.isUserInRole(Authentication.ROLE_FLOW_MANAGER);
            final boolean isAdmin = menuRequestContext.isUserInRole(Authentication.ROLE_ADMIN);

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

            var tileProviders = getTileProviders();

            if (!tileProviders.isEmpty()) {
                mainMenu.userTileProviders.clear();
                mainMenu.userTileProviders.addAll(tileProviders);
            }

            // Parse out menu data from "dispatcher-servlet.xml"
            MenuXml.BeansElement xBeans = null;

            final String path = this.dispatcherServletPath;

            try (var fis = new FileInputStream(path)) {
                xBeans = parseDispatcherServletXml(fis);
            } catch (FileNotFoundException fnfe) {
                throw fnfe;
            }

            List<MenuEntry> topMenuEntries = this.parseXmlToMenuEntries(xBeans);
            // Remove any MenuEntry items marked as RoleBased where user does not have any required role
            evaluateRoleBasedEntries(topMenuEntries);

            for (var topMenu : topMenuEntries) {
                mainMenu.addTopMenu(topMenu);
            }

            // These are taken from "navbar.ftl" and handled somewhat specially
            mainMenu.helpMenu = getHelpMenuEntry(isAdmin);
            mainMenu.selfServiceMenu = getSelfServiceMenuEntry(menuRequestContext.getRemoteUser());
            mainMenu.userNotificationMenu = getUserNotificationMenu(menuRequestContext.getRemoteUser());

            if (isAdmin || isProvision) {
                mainMenu.provisionMenu = getProvisionMenu();
            }

            if (isFlow) {
                mainMenu.flowsMenu = getFlowsMenu();
            }

            if (isAdmin) {
                mainMenu.configurationMenu = getConfigurationMenu();
            }
        } catch (IOException ioe) {
            throw ioe;
        }

        return mainMenu;
    }

    public MenuXml.BeansElement parseDispatcherServletXml(InputStream inputStream) {
        MenuXml.BeansElement xBeansElem = null;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MenuXml.BeansElement.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            xBeansElem = (MenuXml.BeansElement) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (JAXBException e) {
            String msg = e.getMessage();
        }

        return xBeansElem;
    }

    public List<MenuEntry> parseXmlToMenuEntries(MenuXml.BeansElement xBeansElem) throws Exception {
        List<MenuEntry> topMenuEntries = new ArrayList<>();

        Optional<MenuXml.BeanElement> xNavBarEntriesElem =
                xBeansElem.getBeans().stream()
                        .filter(b -> b.getId() != null && b.getId().equals("navBarEntries"))
                        .findFirst();

        if (!xNavBarEntriesElem.isPresent() || xNavBarEntriesElem.get().getConstructorArgElement() == null) {
            throw new Exception("Could not find 'navBarEntries' item");
        }

        List<MenuXml.BeanOrRefElement> xBeansOrRefs = xNavBarEntriesElem.get().getConstructorArgElement().getBeansOrRefs();

        for (MenuXml.BeanOrRefElement xTopLevelBeanOrRef : xBeansOrRefs) {
            Optional<MenuEntry> topEntry = Optional.empty();

            if (xTopLevelBeanOrRef instanceof MenuXml.BeanElement) {
                topEntry = parseTopMenuEntry((MenuXml.BeanElement) xTopLevelBeanOrRef);
            } else if (xTopLevelBeanOrRef instanceof MenuXml.BeanRefElement) {
                topEntry = parseTopMenuEntryFromRef((MenuXml.BeanRefElement) xTopLevelBeanOrRef, xBeansElem);
            }

            topEntry.ifPresent(topMenuEntries::add);
        }

        return topMenuEntries;
    }

    private void ensureContext() throws Exception {
        if (this.menuRequestContext == null) {
            throw new Exception("Must set MenuRequestContext when using a MenuProvider.");
        }
    }

    /**
     * Evaluate a list of MenuEntry and child MenuEntry items, removing any that are RoleBased
     * and where current user does not have the required role.
     * Note, this modifies the passed-in 'topMenuEntries' parameter!
     */
    private void evaluateRoleBasedEntries(List<MenuEntry> topMenuEntries) {
        for (MenuEntry topEntry : topMenuEntries) {
            // For now, we don't evaluate the top MenuEntries
            if (topEntry.items != null && !topEntry.items.isEmpty()) {
                for (int i = topEntry.items.size() - 1; i >= 0; i--) {
                    MenuEntry entry = topEntry.items.get(i);

                    if (!evaluateRoleBasedMenuEntry(entry)) {
                        topEntry.items.remove(i);
                    }
                }
            }
        }
    }

    private boolean evaluateRoleBasedMenuEntry(MenuEntry menuEntry) {
        if (menuEntry.roles != null && !menuEntry.roles.isEmpty()) {
            return menuRequestContext.isUserInAnyRole(menuEntry.roles);
        }

        return true;
    }

    /**
     * Evaluate any system properties on a RoleBasedMenuEntry to determine whether to add this menu item or not.
     * We do this separately from evaluateRoleBasedMenuEntry because we don't want to map the
     * systemProperty and systemPropertyValue properties to a MenuEntry,
     * since we don't want to return those properties in the Menu Rest API response.
     */
    private boolean evaluateSystemPropertyMenuEntry(String systemProperty, String systemPropertyValue) {
        if (!Strings.isNullOrEmpty(systemProperty) && !Strings.isNullOrEmpty(systemPropertyValue)) {
            String value = menuRequestContext.getSystemProperty(systemProperty, "");

            return !Strings.isNullOrEmpty(value) && value.equals(systemPropertyValue);
        }

        return true;
    }

    private Optional<MenuEntry> parseTopMenuEntry(MenuXml.BeanElement xTopLevelBean) {
        // Top level menu items, like "Info", "Status"
        MenuEntry topEntry = new MenuEntry();
        topEntry.id = xTopLevelBean.getId();

        final String[] topMenuSystemProp = new String[] { null, null };

        for (var prop : xTopLevelBean.getProperties()) {
            setFromBeanProperty(prop, "name", (s) -> topEntry.name = s);
            setFromBeanProperty(prop, "url", (s) -> topEntry.url = s);
            setFromBeanProperty(prop, "locationMatch", (s) -> topEntry.locationMatch = s);
            setFromBeanProperty(prop, "roles", (s) -> topEntry.roles = rolesToList(s));
            setFromBeanProperty(prop, "systemProperty", (s) -> topMenuSystemProp[0] = s);
            setFromBeanProperty(prop, "systemPropertyValue", (s) -> topMenuSystemProp[1] = s);
        }

        boolean isValid = false;

        if (!Strings.isNullOrEmpty(topEntry.name) && !Strings.isNullOrEmpty(topEntry.url) &&
                evaluateSystemPropertyMenuEntry(topMenuSystemProp[0], topMenuSystemProp[1])) {
            isValid = true;

            MenuXml.BeanPropertyElement xEntries =
                    xTopLevelBean.getProperties().stream()
                            .filter(p -> !Strings.isNullOrEmpty(p.getName()) && p.getName().equals("entries"))
                            .findFirst().orElse(null);

            if (xEntries != null) {
                for (var xBean : xEntries.getBeans()) {
                    MenuEntry menuEntry = new MenuEntry();
                    menuEntry.id = xBean.getId();

                    final String[] systemProp = new String[] { null, null };

                    for (var prop : xBean.getProperties()) {
                        setFromBeanProperty(prop, "name", (s) -> menuEntry.name = s);
                        setFromBeanProperty(prop, "url", (s) -> menuEntry.url = s);
                        setFromBeanProperty(prop, "locationMatch", (s) -> menuEntry.locationMatch = s);
                        setFromBeanProperty(prop, "roles", (s) -> menuEntry.roles = rolesToList(s));
                        setFromBeanProperty(prop, "systemProperty", (s) -> systemProp[0] = s);
                        setFromBeanProperty(prop, "systemPropertyValue", (s) -> systemProp[1] = s);
                    }

                    if (!Strings.isNullOrEmpty(menuEntry.name) && !Strings.isNullOrEmpty(menuEntry.url) &&
                            evaluateSystemPropertyMenuEntry(systemProp[0], systemProp[1])) {
                        topEntry.addItem(menuEntry);
                    }
                }
            }
        }

        return isValid ? Optional.of(topEntry) : Optional.empty();
    }

    private List<String> rolesToList(String s) {
        if (!Strings.isNullOrEmpty(s)) {
            String[] arr = s.split(",");
            return arr.length > 0 ? Arrays.stream(arr).toList() : null;
        }

        return null;
    }

    /**
     * This and the following are in navbar.ftl, we add them here as if they were just another menu entry.
     */
    private MenuEntry getHelpMenuEntry(boolean isAdmin) {
        MenuEntry helpMenu = new MenuEntry();
        helpMenu.name = "Help";

        MenuEntry helpEntry = new MenuEntry();
        helpEntry.name = "Help";
        helpEntry.url = "help/index.jsp";
        helpMenu.addItem(helpEntry);

        MenuEntry aboutEntry = new MenuEntry();
        aboutEntry.name = "About";
        aboutEntry.url = "about/index.jsp";
        helpMenu.addItem(aboutEntry);

        MenuEntry apiDocumentationEntry = new MenuEntry();
        apiDocumentationEntry.name = "API Documentation";
        apiDocumentationEntry.url = "ui/index.html#/open-api";
        helpMenu.addItem(apiDocumentationEntry);

        // only admin gets Support menu
        if (isAdmin) {
            MenuEntry supportEntry = new MenuEntry();
            supportEntry.name = "Support";
            supportEntry.url = "support/index.jsp";
            mergeRole(supportEntry, Authentication.ROLE_ADMIN);
            helpMenu.addItem(supportEntry);
        }

        return helpMenu;
    }

    private MenuEntry getSelfServiceMenuEntry(String username) {
        MenuEntry selfServiceMenu = new MenuEntry();
        selfServiceMenu.name = username;
        selfServiceMenu.url = "account/selfService/index.jsp";

        MenuEntry changePasswordMenu = new MenuEntry();
        changePasswordMenu.name = "Change Password";
        changePasswordMenu.url = "account/selfService/newPasswordEntry";
        selfServiceMenu.addItem(changePasswordMenu);

        MenuEntry logoutMenu = new MenuEntry();
        logoutMenu.id = "logout";
        logoutMenu.name = "Log Out";
        logoutMenu.url = "j_spring_security_logout";
        selfServiceMenu.addItem(logoutMenu);

        return selfServiceMenu;
    }

    private MenuEntry getUserNotificationMenu(String username) {
        MenuEntry notificationsMenu = new MenuEntry();

        // Note that the top menu is actually 2 items with notification counts,
        // UI implementation will need to add that
        MenuEntry userMenu = new MenuEntry();
        userMenu.url = "notification/browse?acktype=unack&filter=user==" + username;
        userMenu.id = "user";
        notificationsMenu.addItem(userMenu);

        MenuEntry teamMenu = new MenuEntry();
        teamMenu.url = "notification/browse?acktype=unack";
        teamMenu.id = "team";
        notificationsMenu.addItem(teamMenu);

        MenuEntry onCallMenu = new MenuEntry();
        onCallMenu.id = "oncall";
        onCallMenu.url = "roles";
        onCallMenu.name = "On-Call Schedule";
        notificationsMenu.addItem(onCallMenu);

        return notificationsMenu;
    }

    private MenuEntry getProvisionMenu() {
        MenuEntry provisionMenu = new MenuEntry();
        provisionMenu.name = "Quick-Add Node";
        provisionMenu.url = "admin/ng-requisitions/quick-add-node.jsp#/";
        mergeRole(provisionMenu, Authentication.ROLE_ADMIN);
        mergeRole(provisionMenu, Authentication.ROLE_PROVISION);

        return provisionMenu;
    }

    private MenuEntry getFlowsMenu() {
        MenuEntry flowsMenu = new MenuEntry();
        flowsMenu.name = "Flows Management";
        flowsMenu.url = "admin/classification/index.jsp";
        mergeRole(flowsMenu, Authentication.ROLE_FLOW_MANAGER);

        return flowsMenu;
    }

    private MenuEntry getConfigurationMenu() {
        MenuEntry configurationMenu = new MenuEntry();
        configurationMenu.name = "Configure OpenNMS";
        configurationMenu.url = "admin/index.jsp";
        mergeRole(configurationMenu, Authentication.ROLE_ADMIN);

        return configurationMenu;
    }

    private Optional<MenuEntry> parseTopMenuEntryFromRef(MenuXml.BeanRefElement xBeanRefElement, MenuXml.BeansElement xBeansElem) {
        String refName = xBeanRefElement.getBeanRef();

        Optional<MenuXml.BeanElement> xBean =
                xBeansElem.getBeans().stream()
                        .filter(b -> b.getName() != null && b.getName().equals(refName))
                        .findFirst();

        if (xBean.isPresent()) {
            return parseTopMenuEntry(xBean.get());
        }

        return Optional.empty();
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

    private void setFromBeanProperty(MenuXml.BeanPropertyElement propElem, String name, Consumer<String> consumer) {
        if (propElem.getName() != null && propElem.getName().equals(name)) {
            consumer.accept(propElem.getValue());
        }
    }

    private void mergeRole(MenuEntry entry, String role) {
        final String trimmedRole = role.trim();

        if (entry.roles == null) {
            entry.roles = new ArrayList<>();
        }

        if (entry.roles.stream().noneMatch(s -> s.equals(trimmedRole))) {
            entry.roles.add(trimmedRole);
        }
    }
}
