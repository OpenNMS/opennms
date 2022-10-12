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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import joptsimple.internal.Strings;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.support.menu.xml.MenuXml;
import org.springframework.core.io.InputStreamSource;

public class MenuProvider {
    final private InputStreamSource dispatcherServletResource;

    public MenuProvider(InputStreamSource dispatcherServletResource) {
        this.dispatcherServletResource = dispatcherServletResource;
    }

    public MainMenu getMainMenu(final MenuRequestContext context) throws Exception, IOException {
        MainMenu mainMenu = new MainMenu();

        try {
            final boolean isProvision = context.isUserInRole(Authentication.ROLE_PROVISION);
            final boolean isFlow = context.isUserInRole(Authentication.ROLE_FLOW_MANAGER);
            final boolean isAdmin = context.isUserInRole(Authentication.ROLE_ADMIN);

            mainMenu.baseHref = context.calculateUrlBase();
            mainMenu.formattedTime = context.getFormattedTime();
            mainMenu.username = context.getRemoteUser();
            mainMenu.noticeStatus = context.getNoticeStatus();
            mainMenu.notices = buildNotices(context);

            // Parse out menu data from "dispatcher-servlet.xml"
            MenuXml.BeansElement xBeans = null;

            try {
                xBeans = parseDispatcherServletXml(this.dispatcherServletResource.getInputStream());
            } finally {
                this.dispatcherServletResource.getInputStream().close();
            }

            List<TopMenuEntry> topMenuEntries = this.parseXmlToMenuEntries(xBeans);

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

    private Optional<TopMenuEntry> parseTopMenuEntry(MenuXml.BeanElement xTopLevelBean) {
        // Top level menu items, like "Info", "Status"
        TopMenuEntry topEntry = new TopMenuEntry();
        topEntry.id = xTopLevelBean.getId();
        topEntry.className = xTopLevelBean.getClassName();

        for (var prop : xTopLevelBean.getProperties()) {
            setFromBeanProperty(prop, "name", (s) -> topEntry.name = s);
            setFromBeanProperty(prop, "url", (s) -> topEntry.url = s);
            setFromBeanProperty(prop, "locationMatch", (s) -> topEntry.locationMatch = s);
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
     * This is in navbar.ftl, we add it here as if it were just another menu entry.
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

        // only admin gets Support menu
        if (isAdmin) {
            MenuEntry supportEntry = new MenuEntry();
            supportEntry.name = "Support";
            supportEntry.url = "support/index.jsp";
            supportEntry.iconType = "fa";
            supportEntry.icon = "fa-life-ring";
            supportEntry.requiredRoles = List.of(Authentication.ROLE_ADMIN);
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
        provisionMenu.requiredRoles = List.of(Authentication.ROLE_ADMIN, Authentication.ROLE_PROVISION);

        return provisionMenu;
    }

    private MenuEntry getFlowsMenu() {
        MenuEntry flowsMenu = new MenuEntry();
        flowsMenu.name = "Flows Management";
        flowsMenu.url = "admin/classification/index.jsp";
        flowsMenu.iconType = "fa";
        flowsMenu.icon = "fa-minus-circle";
        flowsMenu.requiredRoles = List.of(Authentication.ROLE_FLOW_MANAGER);

        return flowsMenu;
    }

    private MenuEntry getConfigurationMenu() {
        MenuEntry configurationMenu = new MenuEntry();
        configurationMenu.name = "Configure OpenNMS";
        configurationMenu.url = "admin/index.jsp";
        configurationMenu.iconType = "fa";
        configurationMenu.icon = "fa-cogs";
        configurationMenu.requiredRoles = List.of(Authentication.ROLE_ADMIN);

        return configurationMenu;
    }

    private Notices buildNotices(MenuRequestContext context) {
        Notices notices = new Notices();

        notices.status = context.getNoticeStatus();

        // TODO: figure this out, code in navbar.ftl
        // Javascript code plus a Rest API call.......
        // Could maybe build here or else have UI do code similar to Javascript in navbar.ftl

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
}
