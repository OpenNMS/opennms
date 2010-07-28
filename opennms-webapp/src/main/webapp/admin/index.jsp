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
    <h3>OpenNMS System</h3>
    <div class="boxWrapper">
      <ul class="plain">  
        <li><a href="admin/userGroupView/index.jsp">Configure Users, Groups and Roles</a></li>
        <li><a href="admin/sysconfig.jsp">System Information</a></li>
        <li><a href="admin/nodemanagement/instrumentationLogReader.jsp">Instrumentation Log Reader</a></li>
      </ul>
    </div>

    <h3>Operations</h3>
    
    <div class="boxWrapper">
      <ul class="plain">  
        <li><a href="admin/discovery/modifyDiscoveryConfig">Configure Discovery</a></li>
        <li><a href="javascript:snmpConfigPost()">Configure SNMP Community Names by IP</a></li>
        <li><a href="javascript:snmpManagePost()">Configure SNMP Data Collection per Interface</a></li>
		<!-- Removed this - see bug 586
        	<li><a href="admin/pollerConfig/index.jsp">Configure Pollers</a></li>
		-->        
        <li><a href="javascript:submitPost()">Manage and Unmanage Interfaces and Services</a></li>
        <li><a href="admin/thresholds/index.htm">Manage Thresholds</a></li>
        <!-- Secret function 
        	<a href="admin/eventconf/list.jsp">Configure Events</a> 
        -->
        <li><a href="admin/notification/index.jsp">Configure Notifications</a></li>
        <li><a href="admin/sched-outages/index.jsp">Scheduled Outages</a></li>
      </ul>
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

    <h3>Nodes</h3>
    <div class="boxWrapper">
      <ul class="plain">  
        <li><a href="javascript:addInterfacePost()">Add Interface</a></li>
        <li><a href="admin/provisioningGroups.htm">Manage Provisioning Groups</a></li>
        <li><a href="admin/asset/index.jsp">Import and Export Asset Information</a></li>
        <li><a href="admin/categories.htm">Manage Surveillance Categories</a></li>
        <li><a href="javascript:deletePost()">Delete Nodes</a></li>
      </ul>
    </div>

	<h3>Distributed Monitoring</h3>
    <div class="boxWrapper">
      <ul class="plain">  
        <li><a href="admin/applications.htm">Manage Applications</a></li>
        <li><a href="distributed/locationMonitorList.htm">Manage Location Monitors</a></li>
      </ul>
    </div>


    
  </div>
      
  <div class="TwoColRight">
      <h3>Descriptions</h3>
      <div class="boxWrapper">
      <p>Detailed Documentation on all options can be found on <a title="The OpenNMS Project wiki" href="http://www.opennms.org" target="new">the OpenNMS wiki</a>.
      </p>
        <p><b>Configure Users, Groups and Roles</b>: Add, modify or delete
            existing users. Groups contain users. Roles are then assigned to Groups.
            (User: A person, Group: Administrators, Role: On Duty Administrator)
        </p>
        
       <p><b>Notification Status</b>: Notifications will be sent out only if this setting is switched to <em>On</em>.
			This is a system-wide setting. As long as this is <em>Off</em> OpenNMS will create no notifications.
			The current status of notifications is also reflected in the upper right-hand
            corner of every OpenNMS screen with a banner denoting either <em>Notices On</em> or <em>Notices
            Off</em>.
        </p>
        
        <p><b>Configure Discovery</b>: Set up the IP addresses (individual addresses and/or ranges) that you want OpenNMS to scan for new nodes.
         </p>
        
	<P><B>Configure SNMP Community Names by IP</b>:Configure the Community String used in SNMP Data Collection. OpenNMS is shipped with a community string of "public".
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
              

     <p><b>Manage thresholds</b>: Allows you to configure (add/remove/modify) thresholds. 

        <p><b>Configure Notifications</b>: Create and manage notification escalation
            plans, called <em>destination paths</em>. A destination path is associated to
            an OpenNMS event.  Each path can have any arbitrary number of escalations or 
            targets (users or groups) and can send notices through email, pagers, et cetera.  
            Each destination path can be triggered by any number of OpenNMS events and can 
            further be associated with specific interfaces or services.   
        </p>


	<p><b>Scheduled Outages</b>: Add and edit scheduled 
	    outages. You can pause notifications, polling, thresholding and data collection 
            (or any combination of the four) for any interface/node for any time.  
	</p>



        <p><b>Add Interface</b>: Add an interfaces to the database. If the 
            IP address of the interface is contained in the ipAddrTable of an existing node, 
            the interface will be added into the node. Otherwise, a new node will be created.
        </p>

	<p><b>Manage Provisioning Groups</b>: Manually add nodes, interfaces
	and services to OpenNMS.  The creation of these entities is managed completely by you 
	rather than by having OpenNMS discover the network.
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



	<p><b>Manage applications</b> and <b>Manage Location Monitors</b>: Distributed Monitoring Configuration.
	</p>
	

 
      </div>
  </div>
  <hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>
