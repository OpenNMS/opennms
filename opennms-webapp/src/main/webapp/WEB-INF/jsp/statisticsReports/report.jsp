<%--

/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Sep 09: Added support for cases where the resource couldn't be find. - dj@opennms.org
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
<%@ taglib prefix="ec" uri="http://www.extremecomponents.org" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Statistics Report" />
  <jsp:param name="headTitle" value="Statistics Report" />
  <jsp:param name="location" value="reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Report</a>"/>
  <jsp:param name="breadcrumb" value="<a href='statisticsReports/index.htm'>Statistics Reports</a>"/>
  <jsp:param name="breadcrumb" value="Report"/>
</jsp:include>

<c:choose>
  <c:when test="${empty model}">
    <h3>Report: ${model.report.description}</h3>
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
      <ec:table items="model.data" var="row"
        action="${relativeRequestPath}?${pageContext.request.queryString}"
        filterable="false"
        imagePath="images/table/compact/*.gif"
        title="Statistics Report: ${model.report.description}"
        tableId="reportList"
        form="form"
        rowsDisplayed="25"
        view="org.opennms.web.svclayer.etable.FixedRowCompact"
        showExports="true" showStatusBar="true" 
        autoIncludeParameters="false"
        >

			<ec:exportPdf fileName="${model.report.description} (${model.report.startDate} - ${model.report.endDate}.pdf" tooltip="Export PDF"
				headerColor="black" headerBackgroundColor="#b6c2da"
				headerTitle="${model.report.description}, for period ${model.report.startDate} - ${model.report.endDate}" />
			<ec:exportCsv fileName="${model.report.description} (${model.report.startDate} - ${model.report.endDate}.csv" tooltip="Export CSV" />
			<ec:exportXls fileName="${model.report.description} (${model.report.startDate} - ${model.report.endDate}.xls" tooltip="Export Excel" />

      
        <ec:row highlightRow="false">
          <ec:column property="resourceParentsReversed" title="Parent resource" sortable="false"  interceptor="org.opennms.web.svclayer.outage.GroupColumnInterceptor">
            <c:set var="count" value="0"/>
            <c:forEach var="parentResource" items="${row.resourceParentsReversed}">
              <c:if test="${count > 0}">
                <br/>
              </c:if>
              ${parentResource.resourceType.label}:
              <c:choose>
                <c:when test="${!empty parentResource.link}">
                  <c:url var="resourceLink" value="${parentResource.link}"/>
                  <a href="${resourceLink}">${parentResource.label}</a>
                </c:when>
                
                <c:otherwise>
                  ${parentResource.label}
                </c:otherwise>
              </c:choose>
              <c:set var="count" value="${count + 1}"/>
            </c:forEach>
            &nbsp;
          </ec:column>

          <ec:column property="resource" sortable="false">
            ${row.resource.label}
          </ec:column>
          
          <%--
          <ec:column property="resource" sortable="false">
            <c:choose>
              <c:when test="${!empty row.resourceThrowable}">
                <span title="Exception: ${row.resourceThrowable}">Could not find resource: ${row.resourceThrowableId}</span>
              </c:when>
              
              <c:when test="${!empty row.resource.link}">
                ${row.resource.resourceType.label}:
                <c:url var="resourceLink" value="${row.resource.link}"/>
                <a href="${resourceLink}">${row.resource.label}</a>
              </c:when>
                
              <c:otherwise>
                ${row.resource.resourceType.label}:
                ${row.resource.label}
              </c:otherwise>
            </c:choose>
          </ec:column>
          --%>
      
          <ec:column property="value"/>
          
          <ec:column property="resource.id" title="Graphs" sortable="false">
            <c:choose>
              <c:when test="${!empty row.resource}">
                <c:url var="graphUrl" value="graph/results.htm">
                  <c:param name="resourceId" value="${row.resource.id}"/>
                  <c:param name="start" value="${model.report.startDate.time}"/>
                  <c:param name="end" value="${model.report.endDate.time}"/>
                  <c:param name="reports" value="all"/>
                </c:url>
                <a href="${graphUrl}">Resource graphs</a>
              </c:when>
              
              <c:otherwise>
                -
              </c:otherwise>
            </c:choose>
          </ec:column>
        </ec:row>
      </ec:table>
    </form>
  </c:otherwise>
</c:choose>


<jsp:include page="/includes/footer.jsp" flush="false"/>
