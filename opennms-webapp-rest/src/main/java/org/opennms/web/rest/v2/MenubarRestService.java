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

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Web Service using REST for retrieving information to dynamically build the Vue webapp's Menubar component.
 */
@Component
@Path("menubar")
public class MenubarRestService {
    private static final Logger LOG = LoggerFactory.getLogger(org.opennms.web.rest.v2.MenubarRestService.class);

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
    public Response getMainMenu() {
        MainMenu mainMenu = buildMenuDefinition();

        return Response.ok(mainMenu).build();
    }

    private MainMenu buildMenuDefinition() {
        // TODO: Get this from session
        final String username = "admin1";

        // fake data for now
        MainMenu menu = new MainMenu();
        menu.displayAdminLink = true;
        menu.countNoticesAssignedToUser = 0;
        menu.countNoticesAssignedToOtherThanUser = 1;
        menu.noticesAssignedToUserLink = "/opennms/notification/browse?acktype=unack&filter=user==" + username;
        menu.noticesAssignedToOtherThanUserLink = "/opennms/notification/browse?acktype=unack";
        menu.noticeStatus = "off";
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
}
