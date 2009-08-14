<%--

/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2009 Aug 13: Added columns for minimum and maximum values. ayres@opennms.org
 * 2008 Feb 16: Remove CSV export since eXtremeTable does not handle embedded commas
 				at all gracefully. Remove custom column interceptor as it is unneeded
 				and causes the bottom cell to lack a bottom border. - jeffg@opennms.org
 * 2008 Feb 15: Remove lots of nerd info from the report list, add CSV export. - jeffg@opennms.org
 * 2007 Apr 10: Created this file. - dj@opennms.org
 * 
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

--%>

<%@page language="java" contentType="text/html" session="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ec" uri="http://www.extremecomponents.org" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Statistics Reports List" />
  <jsp:param name="headTitle" value="Statistics Reports List" />
  <jsp:param name="location" value="reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Report</a>"/>
  <jsp:param name="breadcrumb" value="<a href='statisticsReports/index.htm'>Statistics Reports</a>"/>
  <jsp:param name="breadcrumb" value="List"/>
</jsp:include>

<c:choose>
  <c:when test="${empty model}">
    <h3>Report List</h3>
    <div class="boxWrapper">
      <p>
        None found.
      </p>
    </div>
  </c:when>

  <c:otherwise>
    <!-- We need the </script>, otherwise IE7 breaks -->
    <script type="text/javascript" src="js/extremecomponents.js"></script>
      
    <link rel="stylesheet" type="text/css" href="css/onms-extremecomponents.css"/>
        
    <form id="form" action="${relativeRequestPath}" method="post">
      <ec:table items="model" var="row"
        action="${relativeRequestPath}?${pageContext.request.queryString}"
        filterable="false"
        imagePath="images/table/compact/*.gif"
        title="Statistics Report List"
        tableId="reportList"
        form="form"
        rowsDisplayed="25"
        view="org.opennms.web.svclayer.etable.FixedRowCompact"
        showExports="true" showStatusBar="true" 
        autoIncludeParameters="false"
        >
      
        <ec:exportPdf fileName="Statistics Report List.pdf" tooltip="Export PDF"
          headerColor="black" headerBackgroundColor="#b6c2da"
          headerTitle="Statistics Report List" />
        <ec:exportXls fileName="Statistics Report List.xls" tooltip="Export Excel" />
      
        <ec:row highlightRow="false">
        <%--
          <ec:column property="name" interceptor="org.opennms.web.svclayer.outage.GroupColumnInterceptor"/>
          --%>

          <ec:column property="description" title="Report Description">
          	<c:url var="reportUrl" value="statisticsReports/report.htm">
          		<c:param name="id" value="${row.id}" />
          	</c:url>
          	<a href="${reportUrl}">${row.description}</a>
          </ec:column>

          <ec:column property="startDate" title="Reporting Period Start" cell="date" format="MMM d, yyyy  HH:mm:ss"/>
          <ec:column property="endDate" title="Reporting Period End"  cell="date" format="MMM d, yyyy  HH:mm:ss"/>
		  <ec:column property="duration" title="Run Interval">
            ${row.durationString}
          </ec:column>
          
          
        <%--
          <ec:column property="jobStartedDate" title="Job Started"  cell="date" format="MMM d, yyyy  HH:mm:ss"/>
          <ec:column property="jobCompletedDate" title="Job Completed"  cell="date" format="MMM d, yyyy  HH:mm:ss"/>
          <ec:column property="jobDuration" title="Job Run Time">
            ${row.jobDurationString}
          </ec:column>
        --%>

          <ec:column property="maxDatumValue" title="Max Value">
            ${row.maxDatumValue}
          </ec:column>
          <ec:column property="minDatumValue" title="Min Value">
            ${row.minDatumValue}
          </ec:column>

          <ec:column property="purgeDate" title="Keep Until At Least" cell="date" format="MMM d, yyyy  HH:mm:ss"/>
        </ec:row>
      </ec:table>
    </form>
  </c:otherwise>
</c:choose>


<jsp:include page="/includes/footer.jsp" flush="false"/>
