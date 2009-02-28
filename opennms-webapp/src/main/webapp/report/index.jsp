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
// 2009 Feb 27: Add substring match capability to resource graphs and ksc reports. ayres@opennms.org
// 2007 Apr 10: Add a link to statistics reports. - dj@opennms.org
// 2003 Feb 07: Fixed URLEncoder issues.
// 2003 Feb 04: Added Key SNMP Custom Performance Reports.
// 2003 Jan 27: Removed reference to e-mailing reports.
// 2002 Nov 12: Added response time reports to webUI. Based on original
//              performance reports.
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
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Reports" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="location" value="report" />
  <jsp:param name="breadcrumb" value="Reports" />
</jsp:include>

  <div class="TwoColLeft">
    <h3>Reports</h3>
    <div class="boxWrapper">
      <form action="graph/index.jsp" method="GET">
        <p align="right">Name contains
        <input type="text" name="match" size="16" />
        <input type="submit" value="Resource Graphs"/></p>
      </form>
      <form action="KSC/index.htm" method="GET">
        <p align="right">Name contains
        <input type="text" name="match" />
        <input type="submit" value="KSC Reports"/></p>
      </form>
      <ul class="plain">
        <li><a href="graph/index.jsp">Resource Graphs</a></li>
        <li><a href="KSC/index.htm">KSC Performance, Nodes, Domains</a></li>
        <li><a href="availability/index.jsp">Availability</a></li>
        <li><a href="statisticsReports/index.htm">Statistics Reports</a></li>
      </ul>
    </div>
  <!-- more reports will follow -->
  </div>

  <div class="TwoColRight">
    <h3>Descriptions</h3>
    <div class="boxWrapper">
      <p><b>Resource Graphs</b> provide an easy way to visualize the critical
          SNMP, response time, and other data collected from managed nodes
          throughout your network.
      </p>
      <p>You may narrow your selection of resources by entering a search
          string in the "Name contains" field. This will invoke a case-insensitive
          substring match on resource names.
      </p>
      <p><b>Key SNMP Customized (KSC) Performance Reports</b>, <b>Node Reports</b>
          and <b>Domain Reports</b>. KSC reports allow the user to create and view
          SNMP performance data using prefabricated graph types. The reports
          provide a great deal of flexibility in timespans and graphtypes. KSC
          report configurations may be saved allowing the user to define key reports
          that may be referred to at future dates. Node reports show SNMP data for
          all SNMP interfaces on a node. Domain reports show SNMP data for all SNMP
          interfaces in a domain. Node reports and domain reports may be loaded into
          the customizer and saved as a KSC report.
      </p>
      <p>You may narrow your selection of resources by entering a search string
          in the "Name contains" field. This will invoke a case-insensitive substring
          match on resource names.
      </p>

      <p><b>Availability Reports</b> provide graphical or numeric
          view of your service level metrics for the current
          month-to-date, previous month, and last twelve months by categories.
      </p>
      
      <p><b>Statistics Reports</b> provide regularly scheduled statistical
          reports on collected numerical data (response time, SNMP performance
          data, etc.).
      </p>
    </div>
  </div>
  <hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>
