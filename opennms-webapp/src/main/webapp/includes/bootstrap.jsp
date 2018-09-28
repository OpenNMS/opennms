<!doctype html><%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
<%--
  This page is included by other JSPs to create a uniform header. 
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
  
  This include JSP takes two parameters:
    title (required): used in the middle of the header bar
    location (optional): used to "dull out" the item in the menu bar
      that has a link to the location given  (for example, on the
      outage/index.jsp, give the location "outages")
--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="
		org.opennms.core.utils.TimeSeries,
		org.opennms.web.api.Util,
		org.opennms.netmgt.config.NotifdConfigFactory
	"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%
	final String baseHref = Util.calculateUrlBase( request );
%>
<%-- The <html> tag is unmatched in this file (its matching tag is in the
     footer), so we hide it in a JSP code fragment so the Eclipse HTML
     validator doesn't complain.  See bug #1728. --%>
<%= "<html lang=\"en\">" %>
<head>
  <title>
    <c:forEach var="headTitle" items="${paramValues.headTitle}">
      <c:out value="${headTitle}" escapeXml="false"/> |
    </c:forEach>
    OpenNMS Web Console
  </title>
  <c:if test="${param.nobase != 'true' }">
    <base href="<%= baseHref %>" />
  </c:if>

  <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
  <meta http-equiv="Content-Style-Type" content="text/css"/>
  <meta http-equiv="Content-Script-Type" content="text/javascript"/>
  <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
  <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no, width=device-width">
  <meta name="apple-itunes-app" content="app-id=968875097">

  <!-- Set GWT property to get browsers locale -->
  <meta name="gwt:property" content="locale=<%=request.getLocale()%>">

  <c:forEach var="meta" items="${paramValues.meta}">
    <c:out value="${meta}" escapeXml="false"/>
  </c:forEach>

  <jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="manifest" />
    <jsp:param name="asset-type" value="js" />
  </jsp:include>

  <!--  ${nostyles} -->
  <c:if test="${param.nostyles != 'true' }">
    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="bootstrap" />
      <jsp:param name="asset-media" value="screen" />
      <jsp:param name="asset-type" value="css" />
    </jsp:include>
    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="opennms-theme" />
      <jsp:param name="asset-media" value="screen" />
      <jsp:param name="asset-type" value="css" />
    </jsp:include>
    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="font-awesome" />
      <jsp:param name="asset-type" value="css" />
    </jsp:include>
    <!-- we used to include the "print.css" here but it turns out it looks like crap -->
  </c:if>
  <link rel="shortcut icon" href="<%= baseHref %>favicon.ico" />
  <c:forEach var="link" items="${paramValues.link}">
    <c:out value="${link}" escapeXml="false" />
  </c:forEach>

  <jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="vendor" />
  </jsp:include>
  <jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="global" />
  </jsp:include>

    <c:if test="${param.storageAdmin == 'true'}">
      <jsp:include page="/assets/load-assets.jsp" flush="false">
        <jsp:param name="asset" value="rws-storage" />
      </jsp:include>
    </c:if>

    <c:if test="${param.renderGraphs == 'true'}">
      <!-- Graphing -->
      <script type="text/javascript">
        // Global scope
        window.onmsGraphContainers = {
          'engine': '<%= TimeSeries.getGraphEngine() %>',
          'baseHref': '<%= baseHref %>'
        };
      </script>
	  <jsp:include page="/assets/load-assets.jsp" flush="false">
	    <jsp:param name="asset" value="onms-graph" />
	  </jsp:include>
	</c:if>

<c:if test="${param.usebackshift == 'true'}">
  <%-- This allows pages to explicitly use Backshift instead of relying on graph.js (which may not use Backshift) --%>
  <jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="d3-js" />
  </jsp:include>
  <jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="flot-js" />
  </jsp:include>
  <jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="backshift-js" />
  </jsp:include>
</c:if>

<c:if test="${param.useionicons == 'true'}">
  <jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="ionicons-css" />
  </jsp:include>
</c:if>

<c:if test="${not empty param.script}">
</c:if>
<c:forEach var="script" items="${paramValues.script}">
    <!-- WARNING: "script" parameters are deprecated. Remove the next line from your JSP. -->
    <c:out value="${script}" escapeXml="false" />
  </c:forEach>

<c:forEach var="extras" items="${paramValues.extras}">
    <!-- WARNING: "extras" parameters are deprecated. Remove the next line from your JSP. -->
  <c:out value="${extras}" escapeXml="false" />
</c:forEach>

<c:if test="${param.vaadinEmbeddedStyles == 'true'}">
  <!-- embedded Vaadin app, fix container to leave room for headers -->
  <style type="text/css">
    div#footer { position:absolute; bottom:0; width:100%; }
    div#content { position:absolute; top:50px; left:0px; right:0px; bottom:90px; }
  </style>
</c:if>

</head>

<%-- The <body> tag is unmatched in this file (its matching tag is in the
     footer), so we hide it in a JSP code fragment so the Eclipse HTML
     validator doesn't complain. --%>
<%= "<body role=\"document\" " %>
<c:if test="${param.ngapp != null}">
  ng-app="${param.ngapp}"
</c:if>
<c:if test="${param.scrollSpy != null}">
  data-spy="scroll" data-target="${param.scrollSpy}"
</c:if>

<%-- Don't add any padding when the visual heading is not being displayed --%>
<c:if test="${param.quiet != 'true'}">
  class="fixed-nav"
</c:if>
<%= ">" %>

<!-- Bootstrap header -->
<c:choose>
  <c:when test="${param.quiet == 'true'}">
    <!-- No visual header is being displayed -->
  </c:when>
  <c:otherwise>
    <jsp:include page="/navBar.htm" flush="false" />
  </c:otherwise>
</c:choose>
<!-- End bootstrap header -->

<!-- Body -->
<%-- This <div> tag is unmatched in this file (its matching tag is in the
     footer), so we hide it in a JSP code fragment so the Eclipse HTML
     validator doesn't complain.  See bug #1728. --%>
<%= "<div id=\"content\" class=\"container-fluid\">" %>
<c:if test="${((param.nonavbar != 'true') && (!empty pageContext.request.remoteUser)) && param.nobreadcrumbs != 'true'}">
  <ol class="breadcrumb">
    <li><a href="<%= baseHref %>index.jsp">Home</a></li>
    <c:forEach var="breadcrumb" items="${paramValues.breadcrumb}">
      <c:if test="${breadcrumb != ''}">
        <li><c:out value="${breadcrumb}" escapeXml="false"/></li>
      </c:if>
    </c:forEach>
  </ol>
</c:if>
