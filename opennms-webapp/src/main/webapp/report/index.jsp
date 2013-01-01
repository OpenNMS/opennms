<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
%>


<%@page import="org.opennms.core.resource.Vault"%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Reports" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="location" value="report" />
  <jsp:param name="breadcrumb" value="Reports" />
</jsp:include>


  <div class="TwoColLeft">
    <h3>Reports</h3>
    <div class="boxWrapper">
      <form action="graph/index.jsp" method="get">
        <p align="right">Name contains
        <input type="text" name="match" size="16" />
        <input type="submit" value="Resource Graphs"/></p>
      </form>
      <form action="KSC/index.htm" method="get">
        <p align="right">Name contains
        <input type="text" name="match" />
        <input type="submit" value="KSC Reports"/></p>
      </form>
      <ul class="plain">
        <li><a href="graph/index.jsp">Resource Graphs</a></li>
        <li><a href="KSC/index.htm">KSC Performance, Nodes, Domains</a></li>
        <li><a href="report/database/index.htm">Database Reports</a></li>
<% if ("true".equalsIgnoreCase(Vault.getProperty("opennms.rancidIntegrationEnabled"))) {%>
        <li><a href="inventory/rancidReport.htm">Inventory</a></li>
<% }%>
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

      <p><b>Database Reports</b> provide graphical or numeric
          view of your service level metrics for the current
          month-to-date, previous month, and last twelve months by categories.
      </p>
      
<% if ("true".equalsIgnoreCase(Vault.getProperty("opennms.rancidIntegrationEnabled"))) {%>
      <p><b>Inventory Reports</b> provide html or XML report list of 
       nodes inventories and rancid devices matching at a specific date using
       a search matching criteria .
      </p>
<% } %> 
      <p><b>Statistics Reports</b> provide regularly scheduled statistical
          reports on collected numerical data (response time, SNMP performance
          data, etc.).
      </p>
    </div>
  </div>
  <hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>
