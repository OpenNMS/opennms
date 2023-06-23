/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.support.menu;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.opennms.core.resource.Vault;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.support.menu.xml.MenuXml;
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
 * TopMenuEntry and MenuEntry are similar to NavBarEntry in opennms-webapp, but redefined and reimplemented here
 * partly to avoid 'opennms-webapp-rest' having a dependency on 'opennms-webapp'.
 *
 * This does some special-casing of role-based authentication (i.e. including or excluding menu entries based
 * on user roles), but will also respect items in 'dispatcher.servlet.xml' marked as 'RoleBasedNavBarEntry'.
 */
public class MenuProvider {
    /** Fully qualified classname of RoleBasedNavBarEntry class, from opennms-webapp. */
    private final String ROLE_BASED_NAV_BAR_ENTRY_CLASS = "org.opennms.web.navigate.RoleBasedNavBarEntry";

    private final String ADMIN_ROLE_ICON = "fa-cogs";

    private static final ImmutableSet<String> ADMIN_ROLES = ImmutableSet.of(
        Authentication.ROLE_ADMIN,
        Authentication.ROLE_FILESYSTEM_EDITOR
    );

    /** Full file path to dispatcher.servlet.xml file, see "applicationContext-cxf-rest-v2.xml" */
    private String dispatcherServletPath;

    public MenuProvider(String dispatcherServletPath) {
        this.dispatcherServletPath = dispatcherServletPath;
    }

    public String getDispatcherServletPath() {
        return this.dispatcherServletPath;
    }

    public void setDispatcherServletPath(String path) {
        this.dispatcherServletPath = path;
    }

    public MainMenu getMainMenu(final MenuRequestContext context) throws Exception, IOException {
        MainMenu mainMenu = new MainMenu();

        try {
            final boolean isProvision = context.isUserInRole(Authentication.ROLE_PROVISION);
            final boolean isFlow = context.isUserInRole(Authentication.ROLE_FLOW_MANAGER);
            final boolean isAdmin = context.isUserInRole(Authentication.ROLE_ADMIN);

            mainMenu.baseHref = context.calculateUrlBase();
            mainMenu.homeUrl = mainMenu.baseHref + "index.jsp";
            mainMenu.formattedTime = context.getFormattedTime();
            mainMenu.username = context.getRemoteUser();
            // for navigating to a specific node id
            mainMenu.baseNodeUrl = "element/node.jsp?node=";
            mainMenu.noticeStatus = context.getNoticeStatus();
            // TODO: Remove
            mainMenu.notices = buildNotices(context);

            mainMenu.copyrightDates = String.format("2002-%d", LocalDate.now().getYear());
            mainMenu.version = Vault.getProperty("version.display");

            // Parse out menu data from "dispatcher-servlet.xml"
            MenuXml.BeansElement xBeans = null;

            final String path = this.dispatcherServletPath;

            try (var fis = new FileInputStream(path)) {
                xBeans = parseDispatcherServletXml(fis);
            } catch (FileNotFoundException fnfe) {
                throw fnfe;
            }

            List<TopMenuEntry> topMenuEntries = this.parseXmlToMenuEntries(xBeans);
            // Remove any MenuEntry items marked as RoleBased where user does not have any required role
            evaluateRoleBasedEntries(topMenuEntries, context);

            for (var topMenu : topMenuEntries) {
                mainMenu.addTopMenu(topMenu);
            }

            // These are taken from "navbar.ftl" and handled somewhat specially
            mainMenu.helpMenu = getHelpMenuEntry(isAdmin);
            mainMenu.selfServiceMenu = getSelfServiceMenuEntry(context.getRemoteUser());
            mainMenu.userNotificationMenu = getUserNotificationMenu(context.getRemoteUser());

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

    public List<TopMenuEntry> parseXmlToMenuEntries(MenuXml.BeansElement xBeansElem) throws Exception {
        List<TopMenuEntry> topMenuEntries = new ArrayList<>();

        Optional<MenuXml.BeanElement> xNavBarEntriesElem =
            xBeansElem.getBeans().stream()
                .filter(b -> b.getId() != null && b.getId().equals("navBarEntries"))
                .findFirst();

        if (!xNavBarEntriesElem.isPresent() || xNavBarEntriesElem.get().getConstructorArgElement() == null) {
            throw new Exception("Could not find 'navBarEntries' item");
        }

        List<MenuXml.BeanOrRefElement> xBeansOrRefs = xNavBarEntriesElem.get().getConstructorArgElement().getBeansOrRefs();

        for (MenuXml.BeanOrRefElement xTopLevelBeanOrRef : xBeansOrRefs) {
            Optional<TopMenuEntry> topEntry = Optional.empty();

            if (xTopLevelBeanOrRef instanceof MenuXml.BeanElement) {
                topEntry = parseTopMenuEntry((MenuXml.BeanElement) xTopLevelBeanOrRef);
            } else if (xTopLevelBeanOrRef instanceof MenuXml.BeanRefElement) {
                topEntry = parseTopMenuEntryFromRef((MenuXml.BeanRefElement) xTopLevelBeanOrRef, xBeansElem);
            }

            topEntry.ifPresent(topMenuEntries::add);
        }

        return topMenuEntries;
    }

    /**
     * Evaluate a list of TopMenuEntry and child MenuEntry items, removing any that are RoleBased
     * and where current user does not have the required role.
     * Also sets ADMIN_ROLE_ICON for items that require Admin access.
     * Note, this modifies the passed-in 'topMenuEntries' parameter!
     */
    private void evaluateRoleBasedEntries(List<TopMenuEntry> topMenuEntries, final MenuRequestContext context) {
        for (TopMenuEntry topEntry : topMenuEntries) {
            // For now, we don't evaluate the TopMenuEntries
            if (topEntry.items != null && !topEntry.items.isEmpty()) {
                for (int i = topEntry.items.size() - 1; i >= 0; i--) {
                    MenuEntry entry = topEntry.items.get(i);

                    if (!evaluateRoleBasedMenuEntry(entry, context)) {
                        topEntry.items.remove(i);
                    } else if (isInAnyRole(ADMIN_ROLES, entry)) {
                        entry.iconType = "fa";
                        entry.icon = ADMIN_ROLE_ICON;
                    }
                }
            }
        }
    }

    private boolean evaluateRoleBasedMenuEntry(MenuEntry menuEntry, final MenuRequestContext context) {
        if (menuEntry.className != null &&
            menuEntry.className.equals(ROLE_BASED_NAV_BAR_ENTRY_CLASS)) {
            List<String> roles = rolesAsList(menuEntry.roles);

            if (!roles.isEmpty() && roles.stream().noneMatch(context::isUserInRole)) {
                return false;
            }
        }

       return true;
    }

    private Optional<TopMenuEntry> parseTopMenuEntry(MenuXml.BeanElement xTopLevelBean) {
        // Top level menu items, like "Info", "Status"
        TopMenuEntry topEntry = new TopMenuEntry();
        topEntry.id = xTopLevelBean.getId();
        topEntry.className = xTopLevelBean.getClassName();

        for (var prop : xTopLevelBean.getProperties()) {
            setFromBeanProperty(prop, "name", (s) -> topEntry.name = s);
            setFromBeanProperty(prop, "url", (s) -> topEntry.url = s);
            setFromBeanProperty(prop, "locationMatch", (s) -> topEntry.locationMatch = s);
            setFromBeanProperty(prop, "roles", (s) -> topEntry.roles = s);
        }

        boolean isValid = false;

        if (!Strings.isNullOrEmpty(topEntry.name) && !Strings.isNullOrEmpty(topEntry.url)) {
            isValid = true;

            MenuXml.BeanPropertyElement xEntries =
                xTopLevelBean.getProperties().stream()
                    .filter(p -> !Strings.isNullOrEmpty(p.getName()) && p.getName().equals("entries"))
                    .findFirst().orElse(null);

            if (xEntries != null) {
                for (var xBean : xEntries.getBeans()) {
                    MenuEntry menuEntry = new MenuEntry();
                    menuEntry.id = xBean.getId();
                    menuEntry.className = xBean.getClassName();

                    for (var prop : xBean.getProperties()) {
                        setFromBeanProperty(prop, "name", (s) -> menuEntry.name = s);
                        setFromBeanProperty(prop, "url", (s) -> menuEntry.url = s);
                        setFromBeanProperty(prop, "locationMatch", (s) -> menuEntry.locationMatch = s);
                        setFromBeanProperty(prop, "roles", (s) -> menuEntry.roles = s);
                    }

                    if (!Strings.isNullOrEmpty(menuEntry.name) && !Strings.isNullOrEmpty(menuEntry.url)) {
                        topEntry.addItem(menuEntry);
                    }
                }
            }
        }

        return isValid ? Optional.of(topEntry) : Optional.empty();
    }

    /**
     * This and the following are in navbar.ftl, we add them here as if they were just another menu entry.
     */
    private TopMenuEntry getHelpMenuEntry(boolean isAdmin) {
        TopMenuEntry helpMenu = new TopMenuEntry();
        helpMenu.name = "Help";

        MenuEntry helpEntry = new MenuEntry();
        helpEntry.name = "Help";
        helpEntry.url = "help/index.jsp";
        helpEntry.iconType = "fa";
        helpEntry.icon = "fa-question-circle";
        helpMenu.addItem(helpEntry);

        MenuEntry aboutEntry = new MenuEntry();
        aboutEntry.name = "About";
        aboutEntry.url = "about/index.jsp";
        aboutEntry.iconType = "fa";
        aboutEntry.icon = "fa-info-circle";
        helpMenu.addItem(aboutEntry);

        MenuEntry apiDocumentationEntry = new MenuEntry();
        apiDocumentationEntry.name = "API Documentation";
        apiDocumentationEntry.url = "ui/index.html#/open-api";
        apiDocumentationEntry.iconType = "fa";
        apiDocumentationEntry.icon = "fa-info-circle";
        helpMenu.addItem(apiDocumentationEntry);

        // only admin gets Support menu
        if (isAdmin) {
            MenuEntry supportEntry = new MenuEntry();
            supportEntry.name = "Support";
            supportEntry.url = "support/index.jsp";
            supportEntry.iconType = "fa";
            supportEntry.icon = ADMIN_ROLE_ICON;
            mergeRole(supportEntry, Authentication.ROLE_ADMIN);
            helpMenu.addItem(supportEntry);
        }

        return helpMenu;
    }

    private TopMenuEntry getSelfServiceMenuEntry(String username) {
        TopMenuEntry selfServiceMenu = new TopMenuEntry();
        selfServiceMenu.name = username;
        selfServiceMenu.url = "account/selfService/index.jsp";
        selfServiceMenu.iconType = "fa";
        selfServiceMenu.icon = "fa-user";

        MenuEntry changePasswordMenu = new MenuEntry();
        changePasswordMenu.name = "Change Password";
        changePasswordMenu.iconType = "fa";
        changePasswordMenu.icon = "fa-key";
        changePasswordMenu.url = "account/selfService/newPasswordEntry";
        selfServiceMenu.addItem(changePasswordMenu);

        MenuEntry logoutMenu = new MenuEntry();
        logoutMenu.id = "logout";
        logoutMenu.name = "Log Out";
        logoutMenu.iconType = "fa";
        logoutMenu.icon = "fa-sign-out";
        logoutMenu.url = "j_spring_security_logout";
        selfServiceMenu.addItem(logoutMenu);

        return selfServiceMenu;
    }

    private TopMenuEntry getUserNotificationMenu(String username) {
        TopMenuEntry notificationsMenu = new TopMenuEntry();

        // Note that the top menu is actually 2 items with notification counts,
        // UI implementation will need to add that
        MenuEntry userMenu = new MenuEntry();
        userMenu.url = "notification/browse?acktype=unack&filter=user==" + username;
        userMenu.id = "user";
        userMenu.icon = "fa-user";
        userMenu.iconType = "fa";
        notificationsMenu.addItem(userMenu);

        MenuEntry teamMenu = new MenuEntry();
        teamMenu.url = "notification/browse?acktype=unack";
        teamMenu.id = "team";
        teamMenu.icon = "fa-users";
        teamMenu.iconType = "fa";
        notificationsMenu.addItem(teamMenu);

        MenuEntry onCallMenu = new MenuEntry();
        onCallMenu.id = "oncall";
        onCallMenu.url = "roles";
        onCallMenu.name = "On-Call Schedule";
        onCallMenu.icon = "fa-calendar";
        onCallMenu.iconType = "fa";
        notificationsMenu.addItem(onCallMenu);

        return notificationsMenu;
    }

    private MenuEntry getProvisionMenu() {
        MenuEntry provisionMenu = new MenuEntry();
        provisionMenu.name = "Quick-Add Node";
        provisionMenu.url = "admin/ng-requisitions/quick-add-node.jsp#/";
        provisionMenu.iconType = "fa";
        provisionMenu.icon = "fa-plus-circle";
        mergeRole(provisionMenu, Authentication.ROLE_ADMIN);
        mergeRole(provisionMenu, Authentication.ROLE_PROVISION);

        return provisionMenu;
    }

    private MenuEntry getFlowsMenu() {
        MenuEntry flowsMenu = new MenuEntry();
        flowsMenu.name = "Flows Management";
        flowsMenu.url = "admin/classification/index.jsp";
        flowsMenu.iconType = "fa";
        flowsMenu.icon = "fa-minus-circle";
        mergeRole(flowsMenu, Authentication.ROLE_FLOW_MANAGER);

        return flowsMenu;
    }

    private MenuEntry getConfigurationMenu() {
        MenuEntry configurationMenu = new MenuEntry();
        configurationMenu.name = "Configure OpenNMS";
        configurationMenu.url = "admin/index.jsp";
        configurationMenu.iconType = "fa";
        configurationMenu.icon = ADMIN_ROLE_ICON;
        mergeRole(configurationMenu, Authentication.ROLE_ADMIN);

        return configurationMenu;
    }

    // TODO: Remove, we are handling this in UI with separate Rest call to 'rest/notifications/summary'
    private Notices buildNotices(MenuRequestContext context) {
        Notices notices = new Notices();

        notices.status = context.getNoticeStatus();

        return notices;
    }

    private Optional<TopMenuEntry> parseTopMenuEntryFromRef(MenuXml.BeanRefElement xBeanRefElement, MenuXml.BeansElement xBeansElem) {
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

    private void setFromBeanProperty(MenuXml.BeanPropertyElement propElem, String name, Consumer<String> consumer) {
        if (propElem.getName() != null && propElem.getName().equals(name)) {
            consumer.accept(propElem.getValue());
        }
    }

    private void mergeRole(MenuEntry entry, String role) {
        final String trimmedRole = role.trim();

        if (Strings.isNullOrEmpty(entry.roles)) {
            entry.roles = trimmedRole;
            return;
        }

        List<String> roles = rolesAsList(entry.roles);

        if (roles.stream().noneMatch(s -> s.equals(trimmedRole))) {
            roles.add(trimmedRole);
            entry.roles = String.join(",", roles);
        }
    }

    private List<String> rolesAsList(String roles) {
        if (Strings.isNullOrEmpty(roles)) {
            return new ArrayList<>();
        }

        return new ArrayList<String>(Arrays.asList(roles.split(",")));
    }

    /** Do any roles in 'entry' match 'role'? */
    private boolean isInRole(String role, MenuEntry entry) {
        return rolesAsList(entry.roles).stream().anyMatch(s -> s.equals(role));
    }

    /** Do any roles in 'entry' match any role in 'requiredRoles'? */
    private boolean isInAnyRole(Set<String> requiredRoles, MenuEntry entry) {
        List<String> entryRoles = rolesAsList(entry.roles);

        return entryRoles.stream().anyMatch(requiredRoles::contains);
    }
}
