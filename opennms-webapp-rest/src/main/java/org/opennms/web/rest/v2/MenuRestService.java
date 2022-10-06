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

package org.opennms.web.rest.v2;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.opennms.core.time.CentralizedDateTimeFormat;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.support.menu.MenuProvider;
import org.opennms.web.rest.support.menu.TopMenuEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Web Service using REST for retrieving information to dynamically build the Vue webapp's Menubar component.
 */
@Component
@Path("menu")
public class MenuRestService {
    private static final Logger LOG = LoggerFactory.getLogger(MenuRestService.class);
    private CentralizedDateTimeFormat dateTimeFormat = new CentralizedDateTimeFormat();

    @Autowired
    private MenuProvider menuProvider;

    // DTO that is returned
    public static class MainMenu {
        public static class MenuItem {
            public String name;
            public String url;
            public String icon;
            public boolean isAbsoluteUrl;
            public boolean isVueLink;

            public MenuItem(String name) {
                this.name = name;
            }
        }

        public static class TopMenuItem extends MenuItem {
            public List<MenuItem> items;

            public TopMenuItem(String name) {
                super(name);
            }

            public void addItem(String name, String url) {
                this.addItem(name, url, "", false, false);
            }

            public void addItem(String name, String url, String icon, boolean isAbsoluteUrl, boolean isVueLink) {
                if (this.items == null) {
                    this.items = new ArrayList<>();
                }

                var item = new MenuItem(name);
                item.url = url;
                item.icon = icon;
                item.isAbsoluteUrl = isAbsoluteUrl;
                item.isVueLink = isVueLink;

                this.items.add(item);
            }
        }

        public String baseHref;
        public String formattedTime;
        public boolean displayAdminLink;
        public Integer countNoticesAssignedToUser;
        public Integer countNoticesAssignedToOtherThanUser;
        public String noticesAssignedToUserLink;
        public String noticesAssignedToOtherThanUserLink;
        public String noticeStatus;
        public String adminLink;
        public String rolesLink;
        public String quickAddNodeLink;
        public String searchLink;
        public String selfServiceLink;
        public String username;
        public List<TopMenuItem> menuItems;
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getMainMenu(final @Context HttpServletRequest request) {
        MainMenu mainMenu = buildMenuDefinition(request);

        return Response.ok(mainMenu).build();
    }

    /**
     * Build the menu definition.
     * Should correspond to logic in opennms-webapp org.opennms.web.controller.NavBarController
     * as well as opennms-webapp webapp/WEB-INF/templates/navbar.ftl.
     */
    private MainMenu buildMenuDefinition(final HttpServletRequest request) {
        final String baseHref = org.opennms.web.api.Util.calculateUrlBase(request);
        final String remoteUser = request.getRemoteUser();

        boolean isProvision = request.isUserInRole(Authentication.ROLE_PROVISION);
        boolean isFlow = request.isUserInRole(Authentication.ROLE_FLOW_MANAGER);
        boolean isAdmin = request.isUserInRole(Authentication.ROLE_ADMIN);
        final String formattedTime = this.dateTimeFormat.format(Instant.now(), extractUserTimeZone(request));

        String noticeStatus = "Unknown";
        try {
            noticeStatus = NotifdConfigFactory.getPrettyStatus();
        } catch (final Throwable t) {
        }

        boolean testFlag = true;

        List<TopMenuEntry> menuEntries = null;

        if (testFlag) {
            if (this.menuProvider != null) {
                try {
                    menuEntries = this.menuProvider.getMenu(request);
                } catch (Exception e) {
                    LOG.error("Error creating menu entries: " + e.getMessage(), e);
                }
            }
        }


        // TODO: Get this from session
        final String username = remoteUser;

        // fake data for now
        MainMenu menu = new MainMenu();
        menu.baseHref = baseHref;
        menu.formattedTime = formattedTime;
        menu.displayAdminLink = true;
        menu.countNoticesAssignedToUser = 0;
        menu.countNoticesAssignedToOtherThanUser = 1;
        menu.noticesAssignedToUserLink = "/opennms/notification/browse?acktype=unack&filter=user==" + username;
        menu.noticesAssignedToOtherThanUserLink = "/opennms/notification/browse?acktype=unack";
        menu.noticeStatus = noticeStatus;
        menu.adminLink = "/opennms/admin/index.jsp";
        menu.rolesLink = "/opennms/roles";
        menu.searchLink = "/opennms/element/index.jsp";
        menu.selfServiceLink = "/opennms/account/selfService/";
        menu.quickAddNodeLink = "/opennms/admin/ng-requisitions/quick-add-node.jsp";
        menu.username = username;

        menu.menuItems = new ArrayList<>();

        var infoMenu = new MainMenu.TopMenuItem("Info");
        infoMenu.addItem("Nodes", "/opennms/element/nodeList.htm");
        infoMenu.addItem("Assets", "/opennms/asset/index.jsp");
        infoMenu.addItem("Path Outages", "/opennms/pathOutage/index.jsp");
        infoMenu.addItem("Device Configs", "/opennms/ui/index.html#/device-config-backup", "", false, true);
        menu.menuItems.add(infoMenu);

        var statusMenu = new MainMenu.TopMenuItem("Status");
        statusMenu.addItem("Events", "/opennms/event/index");
        statusMenu.addItem("Alarms", "/opennms/alarm/index.htm");
        statusMenu.addItem("Notifications", "/opennms/notification/index.jsp");
        statusMenu.addItem("Outages", "/opennms/outage/index.jsp");
        statusMenu.addItem("Surveillance", "/opennms/surveillance-view.jsp");
        statusMenu.addItem("Heatmap", "/opennms/heatmap/index.jsp");
        statusMenu.addItem("Trend", "/opennms/trend/index.jsp");
        statusMenu.addItem("Application", "/opennms/application/index.jsp");
        menu.menuItems.add(statusMenu);

        var reportsMenu = new MainMenu.TopMenuItem("Reports");
        reportsMenu.addItem("Charts", "/opennms/charts/index.jsp");
        reportsMenu.addItem("Resource Graphs", "/opennms/graph/index.jsp");
        reportsMenu.addItem("KSC Reports", "/opennms/KSC/index.jsp");
        reportsMenu.addItem("Database Reports", "/opennms/report/database/index.jsp");
        reportsMenu.addItem("Statistics", "/opennms/statisticsReports/index.htm");
        menu.menuItems.add(reportsMenu);

        var dashboardsMenu = new MainMenu.TopMenuItem("Dashboards");
        dashboardsMenu.addItem("Dashboard", "/opennms/dashboard.jsp");
        dashboardsMenu.addItem("Ops Board", "/opennms/vaadin-wallboard");
        menu.menuItems.add(dashboardsMenu);

        var mapsMenu = new MainMenu.TopMenuItem("Maps");
        mapsMenu.addItem("Topology", "/opennms/topology");
        mapsMenu.addItem("Geographical", "/opennms/node-maps");
        menu.menuItems.add(mapsMenu);

        var helpMenu = new MainMenu.TopMenuItem("Help");
        helpMenu.addItem("Help", "/opennms/help/index.jsp");
        helpMenu.addItem("About", "/opennms/about/index.jsp");
        helpMenu.addItem("Support", "/opennms/support/index.jsp");
        menu.menuItems.add(helpMenu);

        var userMenu = new MainMenu.TopMenuItem(username);
        userMenu.icon = "Person";
        userMenu.url = "/opennms/account/selfService";
        userMenu.addItem("Account", "/opennms/account/selfService");
        userMenu.addItem("Change Password", "/opennms/account/selfService/newPasswordEntry");
        userMenu.addItem("Log Out", "/opennms/j_spring_security_logout");
        menu.menuItems.add(userMenu);

        return menu;
    }

    private ZoneId extractUserTimeZone(HttpServletRequest request){
        ZoneId timeZoneId = (ZoneId) request.getSession().getAttribute(CentralizedDateTimeFormat.SESSION_PROPERTY_TIMEZONE_ID);

        if (timeZoneId == null) {
            timeZoneId = ZoneId.systemDefault();
        }

        return timeZoneId;
    }
}
