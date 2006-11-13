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
// 2005 Oct 01: Convert to use CSS for layout. -- DJ Gregor
// 2005 Oct 01: Refactor relative date code. -- DJ Gregor
// 2003 Feb 28: Corrected day/week/month/year reports on some browsers.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2003 Feb 28: Added day/week/month/year reports.
// 2002 Nov 26: Fixed breadcrumbs issue.
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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:import url="/includes/header.jsp">
  <c:param name="title" value="Resource Graph Results" />
  <c:param name="headTitle" value="Results" />
  <c:param name="headTitle" value="Resource Graphs" />
  <c:param name="headTitle" value="Reports" />
  <c:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <c:param name="breadcrumb" value="<a href='graph/index.jsp'>Resource Graphs</a>"/>
  <c:param name="breadcrumb" value="Results" />
</c:import>

<c:set var="topOffset">0</c:set>
<c:set var="rightOffset">0</c:set>

<c:if test="${fn:contains(header.user-agent, 'MSIE')}">
  <c:set var="topOffset">-13</c:set>
  <c:set var="rightOffset">-20</c:set>
</c:if>

<script type="text/javascript">
  var cZoomBoxTopOffsetWText = ${rrdStrategy.graphTopOffsetWithText} + ${topOffset};
  var cZoomBoxRightOffset = ${rrdStrategy.graphRightOffset} + ${rightOffset};
</script>


<div id="graph-results">
  <h3>
    ${results.parentResourceTypeLabel}:
    <c:choose>
      <c:when test="${!empty results.parentResourceLink}">
        <a href="<c:url value='${results.parentResourceLink}'/>">${results.parentResourceLabel}</a>
      </c:when>
      <c:otherwise>
        ${results.parentResourceLabel}
      </c:otherwise>
    </c:choose>
    
    <c:if test="${!empty results.resource}">
      <br/>
      ${results.resourceTypeLabel}:
      <c:choose>
        <c:when test="${!empty results.resourceLink}">
          <a href="<c:url value='${results.resourceLink}'/>">${results.resourceLabel}</a>
        </c:when>
        <c:otherwise>
          ${results.resourceLabel}
        </c:otherwise>
      </c:choose>
    </c:if>
  </h3>

    <%@ include file="/WEB-INF/jspf/relativetimeform.jspf" %>

    <c:set var="showCustom"></c:set>
    <c:if test="${param.relativetime != 'custom'}">
      <c:set var="showCustom">style="display: none;"</c:set>
    </c:if>
    <div id="customTimeForm" name="customTimeForm" ${showCustom}>
    
    <form action="${requestScope.relativeRequestPath}" method="get">
        <input type="hidden" name="type" value="${results.type}"/>
        <input type="hidden" name="parentResourceType" value="${results.parentResourceType}"/>
        <input type="hidden" name="parentResource" value="${results.parentResource}"/>
        <input type="hidden" name="resourceType" value="${results.resourceType}"/>
        <input type="hidden" name="resource" value="${results.resource}"/>
        <c:forEach var="graph" items="${results.graphs}">
          <input type="hidden" name="reports" value="${graph.name}"/>
        </c:forEach>
        <input type="hidden" name="relativetime" value="custom"/>
        <input type="hidden" name="zoom" value="${param.zoom}"/>
    
    <p>
      Start Time

      <select name="startMonth" size="1">
        <c:forEach var="month" items="${results.monthMap}">
          <option value="${month.key}" <c:if test="${month.key == results.startCalendar.month}">selected</c:if>>${month.value}</option>
        </c:forEach>
      </select>

      <input type="text" name="startDate" size="4" maxlength="2" value="${results.startCalendar.date}" />
      <input type="text" name="startYear" size="6" maxlength="4" value="${results.startCalendar.year}" />

      <select name="startHour" size="1">
        <c:forEach var="hour" items="${results.hourMap}">
          <option value="${hour.key}" <c:if test="${hour.key == results.startCalendar.hourOfDay}">selected</c:if>>${hour.value}</option>
        </c:forEach>
      </select>          

      <br/>

      End Time

      <select name="endMonth" size="1">
        <c:forEach var="month" items="${results.monthMap}">
          <option value="${month.key}" <c:if test="${month.key == results.endCalendar.month}">selected</c:if>>${month.value}</option>
        </c:forEach>
      </select>

      <input type="text" name="endDate" size="4" maxlength="2" value="${results.endCalendar.date}" />
      <input type="text" name="endYear" size="6" maxlength="4" value="${results.endCalendar.year}" />

      <select name="endHour" size="1">
        <c:forEach var="hour" items="${results.hourMap}">
          <option value="${hour.key}" <c:if test="${hour.key == results.endCalendar.hourOfDay}">selected</c:if>>${hour.value}</option>
        </c:forEach>
      </select>          

    </p>
    <input type="submit" value="Apply Custom Time Period"/>
    </form>
    </div>
    
  <p>
    <strong>From</strong> ${results.start} <br/>
    <strong>To</strong> ${results.end} <br/>
  </p>

  <c:choose>
    <c:when test="${param.zoom == 'true'}">
	  <div id='zoomBox' style='position:absolute; overflow:none; left:0px; top:0px; width:0px; height:0px; visibility:visible; background:red; filter:alpha(opacity=50); -moz-opacity:0.5; -khtml-opacity:.5; opacity:0.5'></div>
	  <div id='zoomSensitiveZone' style='position:absolute; overflow:none; left:0px; top:0px; width:0px; height:0px; visibility:visible; cursor:crosshair; background:blue; filter:alpha(opacity=0); -moz-opacity:0; -khtml-opacity:0; opacity:0' oncontextmenu='return false'></div>

	  <style media="print">
	    /*Turn off the zoomBox*/
	    div#zoomBox, div#zoomSensitiveZone {display: none}
	    /*This keeps IE from cutting things off*/
	    #why {position: static; width: auto}
	  </style>
	  
	  <c:url var="graphUrl" value="graph/graph.png">
        <c:param name="type" value="${results.type}"/>
        <c:param name="parentResourceType" value="${results.parentResourceType}"/>
        <c:param name="parentResource" value="${results.parentResource}"/>
        <c:param name="resourceType" value="${results.resourceType}"/>
        <c:param name="resource" value="${results.resource}"/>
        <c:param name="report" value="${results.graphs[0].name}"/>
        <c:param name="start" value="${results.start.time}"/>
        <c:param name="end" value="${results.end.time}"/>
        <c:param name="graph_width" value="${results.graphs[0].graphWidth}"/>
        <c:param name="graph_height" value="${results.graphs[0].graphHeight}"/>
      </c:url>

      <img id="zoomGraphImage" src="${graphUrl}"/>
    </c:when>

    <c:when test="${!empty results.graphs}"> 
      <c:forEach var="graph" items="${results.graphs}">
        <c:url var="zoomUrl" value="${requestScope.relativeRequestPath}">
          <c:param name="zoom" value="true"/>
          <c:param name="relativetime" value="custom"/>
          <c:param name="type" value="${results.type}"/>
          <c:param name="parentResourceType" value="${results.parentResourceType}"/>
          <c:param name="parentResource" value="${results.parentResource}"/>
          <c:param name="resourceType" value="${results.resourceType}"/>
          <c:param name="resource" value="${results.resource}"/>
          <c:param name="reports" value="${graph.name}"/>
          <c:param name="start" value="${results.start.time}"/>
          <c:param name="end" value="${results.end.time}"/>
		</c:url>
		
		<c:url var="graphUrl" value="graph/graph.png">
          <c:param name="type" value="${results.type}"/>
          <c:param name="parentResourceType" value="${results.parentResourceType}"/>
          <c:param name="parentResource" value="${results.parentResource}"/>
          <c:param name="resourceType" value="${results.resourceType}"/>
          <c:param name="resource" value="${results.resource}"/>
          <c:param name="report" value="${graph.name}"/>
          <c:param name="start" value="${results.start.time}"/>
          <c:param name="end" value="${results.end.time}"/>
		</c:url>
		
	    <a href="${zoomUrl}"><img src="${graphUrl}"/></a>
		<br/>
      </c:forEach>
    </c:when>

    <c:otherwise>
      There is no data for this resource.
    </c:otherwise>
  </c:choose>

  <c:import url="/includes/bookmark.jsp"/>
</div>

<c:url var="relativeTimeReloadUrl" value="${requestScope.relativeRequestPath}">
  <c:param name="type" value="${results.type}"/>
  <c:param name="parentResourceType" value="${results.parentResourceType}"/>
  <c:param name="parentResource" value="${results.parentResource}"/>
  <c:param name="resourceType" value="${results.resourceType}"/>
  <c:param name="resource" value="${results.resource}"/>
  <c:forEach var="graph" items="${results.graphs}">
    <c:param name="reports" value="${graph.name}"/>
  </c:forEach>
</c:url>

<script type="text/javascript">
    function relativeTimeFormChange() {
        for (i = 0; i < document.reltimeform.rtstatus.length; i++) {
            if (document.reltimeform.rtstatus[i].selected) {
                var value = document.reltimeform.rtstatus[i].value;
                if (value == "custom") {
                  document.getElementById("customTimeForm").style.display = "block";
                } else {
                  goRelativeTime(value);
                }  
            }
        }
    }
  
    /*
     * This is used by the relative time form to reload the page with a new
     * time period.
     */
    function goRelativeTime(relativeTime) {
        setLocation('${relativeTimeReloadUrl}'
                    + "&relativetime=" + relativeTime);
    }
</script>


<c:if test="${param.zoom == 'true'}">
  <c:url var="zoomReloadUrl" value="${requestScope.relativeRequestPath}">
    <c:param name="zoom" value="true"/>
    <c:param name="relativetime" value="custom"/>
    <c:param name="type" value="${results.type}"/>
    <c:param name="parentResourceType" value="${results.parentResourceType}"/>
    <c:param name="parentResource" value="${results.parentResource}"/>
    <c:param name="resourceType" value="${results.resourceType}"/>
    <c:param name="resource" value="${results.resource}"/>
    <c:param name="reports" value="${results.graphs[0].name}"/>
  </c:url>

  <script type="text/javascript">
  /*
   * This is used by the zoom page to reload the page with a new time period.
   */
  function reloadPage(newGraphStart, newGraphEnd) {
        setLocation('${zoomReloadUrl}'
                    + "&start=" + newGraphStart + "&end=" + newGraphEnd);
  }
  </script>
  
  <script type="text/javascript" src="js/zoom.js"></script>
</c:if>

<jsp:include page="/includes/footer.jsp" flush="false" />
