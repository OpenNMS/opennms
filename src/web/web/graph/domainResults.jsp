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

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<c:import url="/includes/header.jsp">
  <c:param name="title" value="${param.name} Results" />
  <c:param name="headTitle" value="Results" />
  <c:param name="headTitle" value="${param.name}" />
  <c:param name="headTitle" value="Reports" />
  <c:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <c:param name="breadcrumb" value="${param.reportbreadcrumb}"/>
  <c:param name="breadcrumb" value="Results" />
</c:import>

<div id="graph-results">
  <h3>
    Domain: <c:out value="${results.domain}"/><br/>
    Interface: <c:out value="${results.intf}"/>
  </h3>

  <c:if test="${empty param.zoom}">
    <%@ include file="/WEB-INF/jspf/relativetimeformforDomain.jspf" %>
  </c:if>

  <h3>Interface <c:out value="${param.name}"/> Data</h3>
  <strong>From</strong> <c:out value="${results.start}"/> <br/>
  <strong>To</strong> <c:out value="${results.end}"/> <br/>

  <c:choose>
    <c:when test="${!empty param.zoom}">
      <div id='zoomSensitiveZone' oncontextmenu='return false'></div>
      <div id='zoomBox'></div>
      <img id='zoomGraphImage' src="<c:out value="${results.graphs[0].graphURL}"/>&amp;props=<c:out value="${results.domain}"/>/strings.properties&amp;type=<c:out value="${param.type}"/>&amp;domain=<c:out value="${results.domain}"/>&amp;intf=<c:out value="${results.intf}"/>"/>
      <br/>
    </c:when>

    <c:when test="${!empty results.graphs}">
      <c:forEach var="graph" items="${results.graphs}">
	<a href="graph/domainResults?zoom=true&type=<c:out value="${param.type}"/>&intf=<c:out value="${graph.intf}"/>&amp;domain=<c:out value="${graph.domain}"/>&amp;reports=<c:out value="${graph.name}"/>&amp;start=<c:out value="${graph.start.time}"/>&amp;end=<c:out value="${graph.end.time}"/>&amp;props=<c:out value="${results.domain}"/>/strings.properties">
          <img src="<c:out value="${graph.graphURL}"/>&amp;props=<c:out value="${results.domain}"/>/strings.properties"/>
	</a>
	<br/>
      </c:forEach>
    </c:when>

    <c:otherwise>
      <c:out value="${param.noDataMessage}"/>
    </c:otherwise>
  </c:choose>

  <c:if test="${empty param.zoom}">
    <%@ include file="/WEB-INF/jspf/relativetimeformforDomain.jspf" %>
  </c:if>

  <c:import url="/includes/bookmark.jsp"/>
</div>

<script type="text/javascript">
function goRelativeTime(relativeTime) {
      setLocation('graph/domainResults'
        + '?type=<c:out value="${param.type}"/>'
        + '&relativetime=' + relativeTime
        + '&intf=<c:out value="${requestScope.results.intf}"/>'
        + '&domain=<c:out value="${requestScope.results.domain}"/>'
        + '<c:out value="${reportList}" escapeXml="false"/>');
}
</script>

<c:if test="${!empty param.zoom}">
  <script type="text/javascript" src="js/domainZoom.js"></script>
</c:if>

<jsp:include page="/includes/footer.jsp" flush="false" />
