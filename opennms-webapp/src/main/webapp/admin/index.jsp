<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

<%@page import="java.util.Collection"%>
<%@page import="org.opennms.web.navigate.PageNavEntry"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.opennms.core.soa.ServiceRegistry"%>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@page language="java"
        contentType="text/html"
        session="true"
        import="org.opennms.netmgt.config.NotifdConfigFactory"
        %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
    <jsp:param name="title" value="Admin" />
    <jsp:param name="headTitle" value="Admin" />
    <jsp:param name="location" value="admin" />
    <jsp:param name="breadcrumb" value="Admin" />
</jsp:include>

<script type="text/javascript" >

    function addInterfacePost()
    {
        document.addInterface.action = "admin/newInterface.jsp?action=new";
        document.addInterface.submit();
    }

    function deletePost()
    {
        document.deleteNodes.submit();
    }

    function submitPost()
    {
        document.getNodes.submit();
    }

    function manageRanges()
    {
        document.manageRanges.submit();
    }

    function snmpManagePost()
    {
        document.snmpManage.submit();
    }

    function manageSnmp()
    {
        document.manageSnmp.submit();
    }

    function snmpConfigPost()
    {
        document.snmpConfig.action = "admin/snmpConfig?action=default";
        document.snmpConfig.submit();
    }

    function networkConnection()
    {
        document.networkConnection.submit();
    }

    function dns()
    {
        document.dns.submit();
    }

    function communication()
    {
        document.communication.submit();
    }

</script>

<form method="post" name="getNodes" action="admin/getNodes">
    <input type="hidden"/>
</form>

<form method="post" name="addInterface">
    <input type="hidden"/>
</form>

<form method="post" name="deleteNodes" action="admin/deleteNodes">
    <input type="hidden"/>
</form>

<form method="post" name="snmpManage" action="admin/snmpGetNodes">
    <input type="hidden"/>
</form>

<form method="post" name="snmpConfig" action="admin/snmpConfig">
    <input type="hidden"/>
</form>

<div class="row">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">OpenNMS System</h3>
      </div>
      <div class="panel-body">
        <ul class="list-unstyled">
            <li><a href="admin/userGroupView/index.jsp">Configure Users, Groups and On-Call Roles</a></li>
            <li><a href="admin/sysconfig.jsp">System Information</a></li>
            <li><a href="admin/nodemanagement/instrumentationLogReader.jsp">Instrumentation Log Reader</a></li>
        </ul>
      </div> <!-- panel-body -->
    </div> <!-- panel -->

    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Operations</h3>
      </div>
      <div class="panel-body">
        <ul class="list-unstyled">
            <li><a href="admin/discovery/modifyDiscoveryConfig">Configure Discovery</a></li>
            <li><a href="javascript:snmpConfigPost()">Configure SNMP Community Names by IP</a></li>
            <li><a href="javascript:snmpManagePost()">Configure SNMP Data Collection per Interface</a></li>
            <li><a href="javascript:submitPost()">Manage and Unmanage Interfaces and Services</a></li>
            <li><a href="admin/thresholds/index.htm">Manage Thresholds</a></li>
            <!-- Secret function 
                    <a href="admin/eventconf/list.jsp">Configure Events</a> 
            -->
            <li><a href="admin/sendevent.jsp">Send Event</a></li>
            <li><a href="admin/notification/index.jsp">Configure Notifications</a></li>
            <li><a href="admin/sched-outages/index.jsp">Scheduled Outages</a></li>
            <li><a href="admin/manageEvents.jsp">Manage Events Configuration</a></li>
            <li><a href="admin/manageSnmpCollections.jsp">Manage SNMP Collections and Data Collection Groups</a></li>
            <%=getAdminPageNavEntries("operations")%>
        </ul>
        <form role="form" class="form-inline pull-right" method="post" name="notificationStatus" action="admin/updateNotificationStatus">
            <%String status = "Unknown";
                try {
                    NotifdConfigFactory.init();
                    status = NotifdConfigFactory.getPrettyStatus();
                } catch (Throwable e) { /*if factory can't be initialized, status is already 'Unknown'*/ }
            %>
            <div class="form-group">
              <label class="control-label">Notification Status:<%if (status.equals("Unknown")) {%> Unknown<% }%></label>
              <input class="form-control" type="radio" name="status" id="on" value="on" <%=(status.equals("On") ? "checked" : "")%> /> <label for="on">On</label>&nbsp;&nbsp;&nbsp;
              <input class="form-control" type="radio" name="status" id="off" value="off" <%=(status.equals("Off") ? "checked" : "")%> /> <label for="off">Off</label>
            </div>
            <button type="submit" class="btn btn-default">Update</button>
        </form>
      </div> <!-- panel-body -->
    </div> <!-- panel -->

    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Node Provisioning</h3>
      </div>
      <div class="panel-body">
        <ul class="list-unstyled">
            <li><a href="javascript:addInterfacePost()">Add Interface for Scanning</a></li>
            <li><a href="admin/provisioningGroups.htm">Manage Provisioning Requisitions</a></li>
            <li><a href="admin/asset/index.jsp">Import and Export Asset Information</a></li>
            <li><a href="admin/categories.htm">Manage Surveillance Categories</a></li>
            <li><a href="javascript:deletePost()">Delete Nodes</a></li>
        </ul>
      </div> <!-- panel-body -->
    </div> <!-- panel -->

    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Distributed Monitoring</h3>
      </div>
      <div class="panel-body">
        <ul class="list-unstyled">
            <li><a href="admin/applications.htm">Manage Applications</a></li>
            <li><a href="distributed/locationMonitorList.htm">Manage Remote Pollers</a></li>
        </ul>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Descriptions</h3>
      </div>
      <div class="panel-body">
        <p>Detailed Documentation on all options can be found on <a title="The OpenNMS Project wiki" href="http://www.opennms.org" target="new">the OpenNMS wiki</a>.
        </p>
        <p><b>Configure Users, Groups and On-Call Roles</b>: Add, modify or delete
            existing users. Groups contain users. Roles are built from groups and provide
            a mechanism to implement calendar-based on-call staff rotations.
            (User: A person, Group: Administrators, Role: On Duty Staff)
        </p>

        <p><b>Notification Status</b>: Notifications will be sent out only if this setting is switched to <em>On</em>.
            This is a system-wide setting. As long as this is <em>Off</em> OpenNMS will create no notifications.
            The current status of notifications is also reflected in the upper right-hand
            corner of every OpenNMS screen with a banner denoting either <em>Notices On</em> or <em>Notices
                Off</em>.
        </p>

        <p><b>Configure Discovery</b>: Set up the IP addresses (individual addresses and/or ranges) that you want OpenNMS
            to ping periodically in order to detect new nodes.
        </p>

        <P><B>Configure SNMP Community Names by IP</b>:Configure the Community String used in SNMP Data Collection and other SNMP operations. OpenNMS is shipped with a community string of "public".
            If you have set a different <em>read</em> community on your devices you must put it here to be able to collect data from
            these devices.
        </P>           

        <P><B>Configure SNMP Data Collection per Interface</b>: This interface will allow you
            to configure which IP and non-IP interfaces are used in SNMP Data Collection.
        </P>

        <p><b>Manage and Unmanage Interfaces and Services</b>: <em>Managing</em> an interface or service means that
            OpenNMS performs tests on this interface or service. If you want to explicitly enable or disable testing you
            can set that up here. A typical case is if a web server is listening on both an internal and an external interface.
            If you manage the service on both interfaces, you will get two notifications if it fails. If you want only one,
            unmanage the service on one of the interfaces.
        </p>


        <p><b>Manage thresholds</b>: Allows you to configure (add/remove/modify) thresholds.</p>

        <p><b>Send Event</b>: Allows you to build a specific event and send it to the system.</p>

        <p><b>Configure Notifications</b>: Create and manage notification escalation
            plans, called <em>destination paths</em>. A destination path is associated to
            an OpenNMS event.  Each path can have any arbitrary number of escalations or 
            targets (users, groups, on-call roles) and can send notices through email, pagers, et cetera.  
            Each destination path can be triggered by any number of OpenNMS events and may 
            further be associated with specific interfaces or services.   
        </p>


        <p><b>Scheduled Outages</b>: Add and edit scheduled 
            outages. You can pause notifications, polling, thresholding and data collection 
            (or any combination of the four) for any interface/node for any time.  
        </p>

        <p><b>SNMP MIB Compiler</b>: Compile MIBs in order to generate events definitions from traps or
            data collection groups for performance metrics.</p>

        <p><b>Manage Events Configuration</b>: Add and edit configuration files for events definitions.</p>

        <p><b>Manage SNMP Collections and Data Collection Groups</b>: Manage SNMP Collections and the content
            of the files for data collection groups.</p>

        <p><b>Add Interface for Scanning</b>: Trigger a scan of an IPv4 or IPv6 interface. If the 
            IP address of the interface is contained in the IP address tables of an existing node, 
            the interface will be added into the node. Otherwise, a new node will be created.
        </p>

        <p><b>Manage Provisioning Requisitions</b>: Add nodes, interfaces and services to
            OpenNMS based partly or completely on the contents of a Requisition (formerly known
            as a Provisioning Group) rather than strictly by having OpenNMS discover the network.
        </p>

        <p><b>Import and Export Asset Information</b>: Export and import data into OpenNMS's asset inventory.             
            The comma-delimited file format is supported by most spreadsheet and
            database applications. Details for using the Import and Export
            functionalities can be found through this link as well.
        </p>

        <p><b>Manage Surveillance Categories</b>: Manage surveillance categories (also known
            as node categories) and edit the list of nodes belonging to each category.
        </p>

        <p><b>Delete Nodes</b>: Permanently delete nodes from the database.
        </p>
        <!--
                <p><b>Configure Pollers</b> provides an easy way to modify the polling status of 
                    standard services.  It also enables the user to add and delete custom services.
                </p>
        -->        

        <p><b>Manage Applications</b> and <b>Manage Remote Pollers</b>: Configure and administer 
        the operation of remote pollers that report back to this OpenNMS server to provide distributed
        status information.
        </p>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

<%!
    /**
     * Loads all in OSGI installed PageNavEntries with the properties
     * <ul>
     * <li>Page=admin</li>
     * <li><b>AND</li>
     * <li>Category=<category></li>
     * </ul>
 *
     */
    protected String getAdminPageNavEntries(final String category) {
        // create query string
        String queryString = "(Page=admin)";
        if (category != null && !category.isEmpty()) {
            queryString = "(&(Page=admin)(Category=" + category + "))";
        }


        String retVal = "";
        WebApplicationContext webappContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        ServiceRegistry registry = webappContext.getBean(ServiceRegistry.class);
        Collection<PageNavEntry> navEntries = registry.findProviders(PageNavEntry.class, queryString);
        for (PageNavEntry navEntry : navEntries) {
            retVal += "<li><a href=\"" + navEntry.getUrl() + "\" >" + navEntry.getName() + "</a></li>";
        }
        return retVal;
    }
%>
