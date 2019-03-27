<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

--%>

<%@ page language="java" contentType="text/html" session="true" %>
<%@ page import="java.util.Collection" %>
<%@ page import="org.opennms.core.soa.ServiceRegistry" %>
<%@ page import="org.opennms.web.navigate.PageNavEntry" %>
<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>

<nav class="col-md-2 d-none d-md-block sidebar pr-0">
    <div class="">
        <ul class="nav flex-column flex-row">
            <li class="nav-item">
                <a class="nav-link dropdown-toggle" href="#systemSubmenu" data-toggle="collapse" aria-expanded="false"><span><i class="fa fa-terminal"></i></span> System</a>
                <ul class="nav flex-column pl-3 collapse subMenu" id="systemSubmenu">
                    <li class="nav-item"><a class="nav-link" href="admin/sysconfig.jsp">Information</a></li>
                </ul>
            </li>
            <li class="nav-item">
                <a class="nav-link dropdown-toggle" href="#userSubMenu" data-toggle="collapse" aria-expanded="false"><span><i class="fa fa-terminal"></i></span> User and Groups</a>
                <ul class="nav flex-column pl-3 collapse subMenu" id="userSubMenu">
                    <li class="nav-item"><a class="nav-link" href="admin/userGroupView/users/list.jsp">Users</a></li>
                    <li class="nav-item"><a class="nav-link" href="admin/userGroupView/groups/list.htm">Groups</a></li>
                    <li class="nav-item"><a class="nav-link" href="admin/userGroupView/roles">On-Call Roles</a></li>
                </ul>
            </li>
            <li class="nav-item">
                <a class="nav-link dropdown-toggle" href="#provisionSubmenu" data-toggle="collapse" aria-expanded="false"><span><i class="fa fa-terminal"></i></span> Provisioning</a>
                <ul class="nav flex-column pl-3 collapse subMenu" id="provisionSubmenu">
                    <li class="nav-item"><a class="nav-link" href="admin/ng-requisitions/index.jsp">Manage Provisioning Requisitions</a></li>
                    <li class="nav-item"><a class="nav-link" href="admin/asset/index.jsp">Import and Export Asset Information</a></li>
                    <li class="nav-item"><a class="nav-link" href="admin/categories.htm">Manage Surveillance Categories</a></li>
                    <li class="nav-item"><a class="nav-link" href="admin/discovery/edit-config.jsp">Configure Discovery</a></li>
                    <li class="nav-item"><a class="nav-link" href="admin/discovery/edit-scan.jsp">Run Single Discovery Scan</a></li>
                    <li class="nav-item"><a class="nav-link" href="javascript:snmpConfigPost()">Configure SNMP Community Names by IP Address</a></li>
                    <li class="nav-item"><a class="nav-link" href="javascript:addInterfacePost()">Manually Add an Interface</a></li>
                    <li class="nav-item"><a class="nav-link" href="javascript:deletePost()">Delete Nodes</a></li>
                </ul>
            </li>
            <li class="nav-item">
                <a class="nav-link dropdown-toggle" href="#eventManagementSubMenu" data-toggle="collapse" aria-expanded="false"><span><i class="fa fa-terminal"></i></span> Event Management</a>
                <ul class="nav flex-column pl-3 collapse subMenu" id="eventManagementSubMenu">
                    <li class="nav-item"><a class="nav-link" href="admin/sendevent.htm">Manually Send an Event</a></li>
                    <!-- Secret function
                            <a href="admin/eventconf/list.jsp">Configure Events</a>
                    -->
                    <li class="nav-item"><a class="nav-link" href="admin/notification/index.jsp">Configure Notifications</a></li>
                    <li class="nav-item"><a class="nav-link" href="admin/manageEvents.jsp">Customize Event Configurations</a></li>
                    <li class="nav-item">TODO UPDATE NOTIFICATION STATUS (ON/OFF)</li>
                </ul>
            </li>
            <li class="nav-item">
                <a class="nav-link dropdown-toggle" href="#flowManagementSubMenu" data-toggle="collapse" aria-expanded="false"><span><i class="fa fa-terminal"></i></span> Flow Management</a>
                <ul class="nav flex-column pl-3 collapse subMenu" id="flowManagementSubMenu">
                    <li class="nav-item"><a class="nav-link" href="admin/classification/index.jsp">Manage Flow Classification</a></li>
                </ul>
            </li>
            <li class="nav-item">
                <a class="nav-link dropdown-toggle" href="#serviceMonitoringSubMenu" data-toggle="collapse" aria-expanded="false"><span><i class="fa fa-terminal"></i></span> Service Monitoring</a>
                <ul class="nav flex-column pl-3 collapse subMenu" id="serviceMonitoringSubMenu">
                    <li class="nav-item"><a class="nav-link" href="admin/sched-outages/index.jsp">Configure Scheduled Outages</a></li>
                    <li class="nav-item"><a class="nav-link" href="javascript:submitPost()">Manage and Unmanage Interfaces and Services</a></li>
                    <%=getAdminPageMenuEntries("service-monitoring")%>
                </ul>
            </li>
            <li class="nav-item">
                <a class="nav-link dropdown-toggle" href="#performanceMeasurementSubMenu" data-toggle="collapse" aria-expanded="false"><span><i class="fa fa-terminal"></i></span> Performance Measurement</a>
                <ul class="nav flex-column pl-3 collapse subMenu" id="performanceMeasurementSubMenu">
                    <li class="nav-item"><a class="nav-link" href="admin/manageSnmpCollections.jsp">Configure SNMP Collections and Data Collection Groups</a></li>
                    <li class="nav-item"><a class="nav-link" href="javascript:snmpManagePost()">Configure SNMP Data Collection per Interface</a></li>
                    <li class="nav-item"><a class="nav-link" href="admin/thresholds/index.htm">Configure Thresholds</a></li>
                </ul>
            </li>
            <li class="nav-item">
                <a class="nav-link dropdown-toggle" href="#distributedMonitoringSubMenu" data-toggle="collapse" aria-expanded="false"><span><i class="fa fa-terminal"></i></span> Distributed Monitoring</a>
                <ul class="nav flex-column pl-3 collapse subMenu" id="distributedMonitoringSubMenu">
                    <li class="nav-item"><a class="nav-link" href="locations/index.jsp">Manage Monitoring Locations</a></li>
                    <li class="nav-item"><a class="nav-link" href="admin/applications.htm">Manage Applications</a></li>
                    <li class="nav-item"><a class="nav-link" href="distributed/locationMonitorList.htm">Manage Remote Pollers</a></li>
                    <li class="nav-item"><a class="nav-link" href="minion/index.jsp">Manage Minions</a></li>
                </ul>
            </li>
            <li class="nav-item">
                <a class="nav-link dropdown-toggle" href="#additionalToolsSubMenu" data-toggle="collapse" aria-expanded="false"><span><i class="fa fa-terminal"></i></span> Additional Tools</a>
                <ul class="nav flex-column pl-3 collapse subMenu" id="additionalToolsSubMenu">
                    <li class="nav-item"><a class="nav-link" href="admin/nodemanagement/instrumentationLogReader.jsp">Instrumentation Log Reader</a></li>
                    <%=getAdminPageMenuEntries("operations")%>
                </ul>
            </li>
        </ul>
    </div>
</nav>

<script type="text/javascript" >

    var activate = function(element) {
        console.log(element);
        $(element).addClass("active");
        $(element).parent().addClass("active");
    };

    // Add on click for elements
    $(document).ready(function() {
        // When menu is changed (but not URL), toggle
        $('.sidebar li').children('a').click(function() {
            activate(this);
        });

        // When page is loading, activate its menu
        var theLocation = $(location).attr("href");
        $('.sidebar .subMenu li a').filter(function() {
            var href = $(this).attr("href");
            return theLocation.includes(href);
        }).each(function() {
            console.log(this);
            $(this).parent().parent().parent().children("a").click();
            $(this).addClass("active");
        });
    });
</script>

<style type="text/css">
    /***    left menu ****/
    /*.sidebar .nav-link.active {*/
    /*color: #007bff;*/
    /*}*/

    /*.sidebar .nav-link:hover {*/
    /*color: #007bff;*/
    /*}*/

    .sidebar .nav-link {
        font-weight: 500;
        color: #333;
    }

    .sidebar .dropdown-toggle[aria-expanded="false"]:after {
        transform: rotate(-90deg);
    }

    .sidebar .dropdown-toggle[aria-expanded="true"]:after {
        transform: rotate(0deg);
    }

    /*for the animation*/
    .sidebar .dropdown-toggle:after {
        transition: 0.5s;
    }

    .sidebar li ul .nav-link {
        font-weight: normal;
    }

    .sidebar .nav-link:hover {
        background-color: #dee2e6; /* gray 300 */
    }


    .sidebar .nav-link.active {
        /*background-color: #f8f9fa; !* gray 100 *!*/
        /*background-color: #e9ecef; !* gray 200 *!*/
        background-color: #dee2e6 ;/* gray 300 */
        /*background-color: #ced4da ;!* gray 400 *!*/
        /*background-color: #adb5bd ;!* gray 500 *!*/
        /*background-color: #6c757d ;!* gray 600 *!*/
        /*background-color: #495057 ;!* gray 700 *!*/
        /*background-color: #343a40 ;!* gray 800 *!*/
        /*background-color: #212529 ;!* gray 900 *!*/
    }

    .sidebar .subMenu .nav-link.active {
        /*background-color: #f8f9fa; !* gray 100 *!*/
        /*background-color: #e9ecef; !* gray 200 *!*/
        background-color: #dee2e6 ;/* gray 300 */
        /*background-color: #ced4da ;!* gray 400 *!*/
        /*background-color: #adb5bd ;!* gray 500 *!*/
        /*background-color: #6c757d ;!* gray 600 *!*/
        /*background-color: #495057 ;!* gray 700 *!*/
        /*background-color: #343a40 ;!* gray 800 *!*/
        /*background-color: #212529 ;!* gray 900 *!*/
    }

    .sidebar .subMenu .nav-link {
        background-color: !important;
    }

    .sidebar .dropdown-toggle:after {
        left: auto;
        right: 5px;
        position: absolute;
    }

    .sidebar .nav-item:hover::before {
        content: "";
        position: absolute;
        left: 0;
        /*border-left: 20px solid #007bff; !* primary *!*/
        border-left: 20px solid #6c757d; /* secondary */
        width: 20px;
        height: 37px;
    }

    .sidebar .nav-item.active::before {
        content: "";
        position: absolute;
        left: 0;
        /*border-left: 20px solid #007bff; !* primary *!*/
        border-left: 20px solid #6c757d; /* secondary */
        width: 20px;
        height: 37px;
    }

    .sidebar .nav-link.active::before {
        content: "";
        position: absolute;
        left: 0;
        /*border-left: 20px solid #007bff; !* primary *!*/
        border-left: 20px solid #6c757d; /* secondary */
        width: 20px;
        height: 37px;
    }
</style>

<%!
    protected String getAdminPageMenuEntries(final String category) {
        String retVal = "";
        for (PageNavEntry navEntry : getNavEntries(category)) {
            retVal += "<li class=\"nav-item\"><a class=\"nav-link\" href=\"" + navEntry.getUrl() + "\" >" + navEntry.getName() + "</a></li>";
        }
        return retVal;
    }

    protected Collection<PageNavEntry> getNavEntries(final String category) {
        // create query string
        String queryString = "(Page=admin)";
        if (category != null && !category.isEmpty()) {
            queryString = "(&(Page=admin)(Category=" + category + "))";
        }


        final WebApplicationContext webappContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        final ServiceRegistry registry = webappContext.getBean(ServiceRegistry.class);
        final Collection<PageNavEntry> navEntries = registry.findProviders(PageNavEntry.class, queryString);
        return navEntries;
    }
%>
