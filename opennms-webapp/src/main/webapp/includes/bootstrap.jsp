<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
		org.opennms.web.api.Util,
		org.opennms.netmgt.config.NotifdConfigFactory
	"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%
	final String baseHref = Util.calculateUrlBase( request );
%>
<!DOCTYPE html>
<%-- The <html> tag is unmatched in this file (its matching tag is in the
     footer), so we hide it in a JSP code fragment so the Eclipse HTML
     validator doesn't complain.  See bug #1728. --%>
<%= "<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en' xmlns:opennms='xsds/coreweb.xsd'>" %>
<head>
  <title>
    <c:forEach var="headTitle" items="${paramValues.headTitle}">
      <c:out value="${headTitle}" escapeXml="false"/> |
    </c:forEach>
    OpenNMS Web Console
  </title>
  <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
  <meta http-equiv="Content-Style-Type" content="text/css"/>
  <meta http-equiv="Content-Script-Type" content="text/javascript"/>
  <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
  <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no, width=device-width">

  <!-- Set GWT property to get browsers locale -->
  <meta name="gwt:property" content="locale=<%=request.getLocale()%>">

  <c:forEach var="meta" items="${paramValues.meta}">
    <c:out value="${meta}" escapeXml="false"/>
  </c:forEach>
  <c:if test="${param.nobase != 'true' }">
    <base href="<%= baseHref %>" />
  </c:if>
  <!--  ${nostyles} -->
  <c:if test="${param.nostyles != 'true' }">
    <link rel="stylesheet" type="text/css" href="<%= baseHref %>css/bootstrap.css" media="screen" />
    <link rel="stylesheet" type="text/css" href="<%= baseHref %>css/opennms-theme.css" media="screen" />
    <link rel="stylesheet" type="text/css" href="<%= baseHref %>css/font-awesome-4.3.0/css/font-awesome.min.css" />
    <link rel="stylesheet" type="text/css" href="<%= baseHref %>css/print.css" media="print" />
  </c:if>
  <link rel="shortcut icon" href="<%= baseHref %>favicon.ico" />
  <c:forEach var="link" items="${paramValues.link}">
    <c:out value="${link}" escapeXml="false" />
  </c:forEach>
  <script type="text/javascript" src="<%= baseHref %>js/global.js"></script>

    <script type="text/javascript" src="lib/jquery/dist/jquery.js"></script>
    <script type="text/javascript" src="lib/bootstrap/dist/js/bootstrap.js"></script>

    <c:if test="${!empty pageContext.request.remoteUser && !param.disableCoreWeb}">
        <script type="text/javascript" src="<%= baseHref %>coreweb/coreweb.nocache.js"></script>
    </c:if>
    <c:if test="${param.storageAdmin == 'true'}">
      <script type='text/javascript' src='<%= baseHref %>js/rwsStorage.js'></script>
    </c:if>

    <c:if test="${param.enableSpringDojo == 'true'}">	
      <script type="text/javascript" src='<%= baseHref %>resources/dojo/dojo.js'></script>
      <script type="text/javascript" src='<%= baseHref %>resources/spring/Spring.js'></script>
      <script type="text/javascript" src='<%= baseHref %>resources/spring/Spring-Dojo.js'></script>
    </c:if>

<c:forEach var="script" items="${paramValues.script}">
    <c:out value="${script}" escapeXml="false" />
  </c:forEach>

<c:forEach var="extras" items="${paramValues.extras}">
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
