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
// 2002 Nov 12: Added response time, based on original  performance code.
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

<%--
  This page is included by other JSPs to create a box containing an
  entry to the performance reporting system.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>


<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		org.opennms.web.Util,
		org.opennms.web.performance.*,
		org.opennms.netmgt.config.kscReports.*,
		org.opennms.netmgt.config.KSC_PerformanceReportFactory"
%>


<%@ include file="/WEB-INF/jspf/KSC/init2.jspf" %>

<%
    PerformanceModel.QueryableNode[] nodes = this.model.getQueryableNodes();
%>

<%
    int r_count=0;
    ReportsList report_configuration = this.reportFactory.getConfiguration();  
    Report[] report_array = null;
    try {
         if (report_configuration == null){
            throw new ServletException ( "Couldn't retrieve KSC Report File configuration");
         }
         else {
            r_count = report_configuration.getReportCount(); 
            report_array = report_configuration.getReport();
         } 
    }
    catch( Exception e ) {
        throw new ServletException ( "Couldn't retrieve reports from KSC_PerformanceReportFactory.", e );
    }
%>

<%-- Start the HTML Page Definition here --%>

<h3><a href="KSC/index.jsp">KSC Reports</a></h3>
<div class="boxWrapper">
    <td class="standardmorepadding">
      <form method="get" name="choose_report" action="KSC/form_proc_main.jsp">
         <input type="hidden" name="report_action" value="View">
	    <% if (report_array.length < 1) { %>
	      <p>No KSC reports defined</p>
	    <% } else { %>
    		<p>Choose a <label for="KSCReport">report to view</label>:</p>
         <select style="width: 100%;" name="report" id="KSCReport">
         <% for( int i=0; i < r_count; i++ ) { %>
             <option value="<%=i%>"> <%=report_array[i].getTitle()%></option>
         <% } %>
			</select>
			<input type="submit" value="Execute Query" />
		<% } %>
	</form>
</div>
