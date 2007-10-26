<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Aug 15: Fix HTML issues per bug #1558. - dj@opennms.org
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 2002 Sep 24: Added a "select" option for SNMP data and a config page.
// 2002 Sep 19: Added a "delete nodes" page to the webUI.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.netmgt.config.NotifdConfigFactory"
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Admin" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="Admin" />
</jsp:include>

<script type="text/javascript" >

  function addInterfacePost()
  {
      document.addInterface.action="admin/newInterface.jsp?action=new";
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
    document.snmpConfig.action="admin/snmpConfig.jsp";
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

  <div class="TwoColLeft">
    <h3>Options</h3>
    <div class="boxWrapper">
      <ul class="plain">  
        <li><a href="admin/discovery/index.jsp">Configure Discovery</a></li>
        <li><a href="admin/userGroupView/index.jsp">Configure Users, Groups and Roles</a></li>
        <!-- <a href="admin/eventconf/list.jsp">Configure Events</a> -->
        <li><a href="admin/notification/index.jsp">Configure Notifications</a></li>
        <li><a href="javascript:submitPost()">Manage and Unmanage Interfaces and Services</a></li>
        <li><a href="javascript:snmpManagePost()">Configure SNMP Data Collection per Interface</a></li>
        <li><a href="javascript:snmpConfigPost()">Configure SNMP Community Names by IP</a></li>
        <li><a href="javascript:addInterfacePost()">Add Interface</a></li>
        <li><a href="javascript:deletePost()">Delete Nodes</a></li>
<!-- Removed this - see bug 586
        <li><a href="admin/pollerConfig/index.jsp">Configure Pollers</a></li>
-->
        <li><a href="admin/asset/index.jsp">Import and Export Asset Information</a></li>
        <li><a href="admin/sched-outages/index.jsp">Scheduled Outages</a></li>
        <li><a href="admin/categories.htm">Manage Surveillance Categories</a></li>
        <li><a href="admin/applications.htm">Manage Applications</a></li>
        <li><a href="admin/provisioningGroups.htm">Manage Provisioning Groups</a></li>
        <li><a href="admin/thresholds/index.htm">Manage Thresholds</a></li>
        <li><a href="distributed/locationMonitorList.htm">Manage Location Monitors</a></li>
      </ul>
      <!-- security link -->
    </div>
    <div class="boxWrapper">
      <form method="post" name="notificationStatus" action="admin/updateNotificationStatus">
        <%String status = "Unknown";
         try
          {
            NotifdConfigFactory.init();
            status = NotifdConfigFactory.getInstance().getPrettyStatus();
          } catch (Exception e) { /*if factory can't be initialized, status is already 'Unknown'*/ }
        %>
          <p align="right">Notification Status:
            <%if (status.equals("Unknown")) { %>
              Unknown<br />
            <% } %>
              <input type="radio" name="status" id="on" value="on" <%=(status.equals("On") ? "checked" : "")%> /> <label for="on">On</label>&nbsp;&nbsp;&nbsp;
              <input type="radio" name="status" id="off" value="off" <%=(status.equals("Off") ? "checked" : "")%> /> <label for="off">Off</label>
              <input type="submit" value="Update" />
            </p>
        </form>
      </div>
  </div>
      
  <div class="TwoColRight">
      <h3>Option Descriptions</h3>
      <div class="boxWrapper">
        <p><b>Configure Discovery</b> allows you, the Administrator, to add or delete
            ip address specific and range to discover. 
        </p>   
              
        <p><b>Configure Users and Groups</b> allows you, the Administrator, to add, modify or delete
            existing users.   If adding or modifying users, be prepared with user
            IDs, passwords, notification contact information (pager numbers and/or
            email addresses), and duty schedule information.  You can then Add users
            to <em>Groups</em>.
        </p>

        <p><b>Configure Notifications</b> allows you to create new notification escalation
            plans, called <em>notification paths</em>, and then associate a notification path 
            with an OpenNMS event.  Each path can have any arbitrary number of escalations or 
            targets (users or groups) and can send notices through email, pagers, et cetera.  
            Each notification path can be triggered by any number of OpenNMS events and can 
            further be associated with specific interfaces or services.   
        </p>

        <p>When OpenNMS was first started, the nodes, interfaces, and services
            in the network were <em>discovered</em>.  As your network grows and changes, the TCP/IP
            ranges you want to manage, as well as the interfaces and services within those ranges,
            may change. <b>Manage and Unmanage Interfaces and Services</b> allows you to change 
            your OpenNMS configuration along with your network.
        </p>

	<P><B>Manage SNMP Data Collection per Interface</b>: This interface will allow you
	to configure which non-IP interfaces are used in SNMP Data Collection.
	</P>

	<P><B>Configure SNMP Community Names by IP</b>: This interface will allow you
	to configure the Community String used in SNMP Data Collection.
	</P>

        <p><b>Add Interface</b> is an interface to add an interface to the database. If the 
            IP address of the interface is contained in the ipAddrTable of an existing node, 
            the interface will be added into the node. Otherwise, a new node will be created.
        </p>
        
        <p><b>Delete Nodes</b> is an interface to permanently delete nodes from the database.
        </p>
<!--
        <p><b>Configure Pollers</b> provides an easy way to modify the polling status of 
            standard services.  It also enables the user to add and delete custom services.
        </p>
-->        
        <p><b>Import and Export Asset Information</b> provides an easy-to-use interface
            for adding data to OpenNMS's asset inventory from your database
            or spreadsheet application, as well as extracting data from the asset
            inventory for use in your favorite spreadsheet or database.  Our
            comma-delimited file format is supported by most spreadsheet and
            database applications, and details for using the Import and Export
            functionalities can be found through this link as well.
        </p>

	<p><b>Scheduled Outages</b> provides an interface for adding and editing scheduled 
	    outages.  You can pause notifications, polling, thresholding and data collection 
            (or any combination of the four) for any interface/node for any time.  
	</p>

	<p><b>Manage node categories</b> allows you to manage node categories
	  (groups of nodes).
	</p>

	<p><b>Manage applications</b> allows you to manage applications (groups
	  of services on interfaces).
	</p>
	
	<p><b>Manually Provisioned Nodes</b> allows you to manually add nodes interfaces
	and services to OpenNMS.  The creation of these nodes are managed completely by you 
	rather by creating an import file and sending the data to the OpenNMS importer.
	</p>

        <p><b>Notification Status</b> provides both a visual reminder as to whether your users 
            are being paged/emailed when important network events are received, as well as 
            providing a way to turn the notification system on or off.  <b>This is a system-wide
            setting affecting all notifications and all users.</b>  Note that
            the current status of notifications is reflected in the upper right-hand
            corner of every OpenNMS screen with either a <em>Notices On</em> or <em>Notices
            Off</em> banner.  
        </p>
     <p><b>Manage thresholds</b> allows you to configure (add/remove/modify) thresholds.  
      </div>
  </div>
  <hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>
