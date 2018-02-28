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

<%@page language="java"
	contentType="text/html"
	session="true"
%>


<%@page import="org.opennms.core.resource.Vault"%>
<jsp:include page="/includes/bootstrap.jsp" flush="false">
    <jsp:param name="title" value="Reports" />
    <jsp:param name="headTitle" value="Reports" />
    <jsp:param name="location" value="report" />
    <jsp:param name="breadcrumb" value="Reports" />
</jsp:include>

<div class="row">
  <div class="col-md-5">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Reports</h3>
        </div>
        <div class="panel-body" id="onms-search">
            <div class="row">
                <div class="col-md-12">
                    <div class="pull-right">
                        <form class="form-inline" role="form" name="resourceGraphs">
                            <div class="form-group">
                                <label class="sr-only">Resource Graphs for Node</label>
                                <p class="form-control-static">Resource Graphs for Node</p>
                            </div>
                            <onms-search-nodes />
                        </form>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-12">
                </div>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <div class="pull-right">
                        <form class="form-inline" role="form" name="kscReports">
                            <div class="form-group">
                                <label class="sr-only">KSC Reports</label>
                                <p class="form-control-static">KSC Reports</p>
                            </div>
                            <onms-search-ksc />
                        </form>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <ul class="list-unstyled">
                        <li><a href="charts/index.jsp">Charts</a></li>
                        <li><a href="graph/index.jsp">Resource Graphs</a></li>
                        <li><a href="KSC/index.jsp">KSC Performance, Nodes, Domains</a></li>
                        <li><a href="report/database/index.htm">Database Reports</a></li>
                        <% if ("true".equalsIgnoreCase(Vault.getProperty("opennms.rancidIntegrationEnabled"))) {%>
                        <li><a href="inventory/rancidReport.htm">Inventory</a></li>
                        <% }%>
                        <li><a href="statisticsReports/index.htm">Statistics Reports</a></li>
                    </ul>
                </div>
            </div>
        </div>
        <!-- more reports will follow -->
    </div>
  </div>

  <div class="col-md-7">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Descriptions</h3>
        </div>
        <div class="panel-body">
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
  </div>
</div>
  <hr />

<jsp:include page="/js/angular.min.jsp" flush="false" />
<jsp:include page="/js/search.min.jsp" flush="false" />
<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
