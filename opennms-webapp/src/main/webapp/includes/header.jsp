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

<%--

  Modifications:

  2005 Sep 30: Hacked up to use CSS for layout. -- DJ Gregor

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
	import="org.opennms.web.api.Util,org.opennms.netmgt.config.NotifdConfigFactory"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%!
    public void init() throws ServletException {
        try {
            NotifdConfigFactory.init();
        } catch (Throwable t) {
	    // notice status will be unknown if the factory can't be initialized
	}
    }
%>

<%
    String noticeStatus;
    try {
        noticeStatus = NotifdConfigFactory.getPrettyStatus();
        if ("Off".equals(noticeStatus)) {
          noticeStatus="<b id=\"notificationOff\">Off</b>";
        } else {
          noticeStatus="<b id=\"notificationOn\">On</b>";
        }
    } catch (Throwable t) {
        noticeStatus = "<b id=\"notificationOff\">Unknown</b>";
    }
    pageContext.setAttribute("noticeStatus", noticeStatus);
final String baseHref = Util.calculateUrlBase( request );
%>
<c:choose>
<c:when test="${param.docType == 'html5'}">
<!DOCTYPE html>
</c:when>
<c:when test="${param.docType == 'html'}">
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
</c:when>
<c:otherwise>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
</c:otherwise>
</c:choose>
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
  <meta http-equiv="X-UA-Compatible" content="IE=8"/>

  <!-- Set GWT property to get browsers locale -->
  <meta name="gwt:property" content="locale=<%=request.getLocale()%>">

  <c:forEach var="meta" items="${paramValues.meta}">
    <c:out value="${meta}" escapeXml="false"/>
  </c:forEach>
  <c:choose>
    <c:when test="${param.nobase != 'true' }">
        <base href="<%= baseHref %>" />
    </c:when>
  </c:choose>
  <!--  ${nostyles} -->
  <c:choose>
    <c:when test="${param.nostyles != 'true' }">
        <link rel="stylesheet" type="text/css" href="<%= baseHref %>css/styles.css" media="screen" />
        <link rel="stylesheet" type="text/css" href="<%= baseHref %>css/gwt-asset.css" media="screen" />
        <link rel="stylesheet" type="text/css" href="<%= baseHref %>css/onms-gwt-chrome.css" media="screen" />
        <link rel="stylesheet" type="text/css" href="<%= baseHref %>css/print.css" media="print" />
    </c:when>
  </c:choose>
  <link rel="shortcut icon" href="favicon.ico" />
  <c:forEach var="link" items="${paramValues.link}">
    <c:out value="${link}" escapeXml="false" />
  </c:forEach>
  <script type="text/javascript" src="<%= baseHref %>js/global.js"></script>
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
  <style type="text/css">
  div#footer { position:absolute; bottom:0; width:100%; }
  div#content { position:absolute; top:99px; left:0px; right:0px; bottom:53px; }
  </style>
</c:if>

</head>

<%-- The <body> tag is unmatched in this file (its matching tag is in the
     footer), so we hide it in a JSP code fragment so the Eclipse HTML
     validator doesn't complain.  See bug #1728. --%>
<%= "<body>" %>

<c:choose>
	<c:when test="${param.quiet == 'true'}">
	<!-- No visual header is being displayed -->
	</c:when>

	<c:otherwise>

	<!-- Header -->
	<div id="header">
		<h1 id="headerlogo"><a href="<%= baseHref %>index.jsp"><img src="<%= baseHref %>images/logo.png" alt="OpenNMS Web Console Home"/></a></h1>  
		<div id="headerinfo">
			<h2>${param.title}</h2>
			<p align="right">
				<c:choose>
					<c:when test="${!empty pageContext.request.remoteUser}">
						User: <a href="<%= baseHref %>account/selfService/index.jsp" title="Account self-service"><strong>${pageContext.request.remoteUser}</strong></a>&nbsp;(Notices <c:out value="${noticeStatus}" escapeXml="false"/>)
						- <a href="<%= baseHref %>j_spring_security_logout">Log out</a><br/>
					</c:when>
					<c:otherwise>
						User: &hellip;<br/>
					</c:otherwise>
				</c:choose>
                <jsp:useBean id="currentDate" class="java.util.Date" />
                <fmt:formatDate value="${currentDate}" type="date" dateStyle="medium"/>
                &nbsp;
                <fmt:formatDate value="${currentDate}" type="time" pattern="HH:mm z"/> 
			</p>
		</div>
		<hr />
		<c:choose>
			<c:when test="${(param.nonavbar != 'true') && (!empty pageContext.request.remoteUser)}">
				<div id="headernavbarright">
					<jsp:include page="/navBar.htm" flush="false"/>
				</div>
			</c:when>
			<c:otherwise>
				<c:if test="${param.nofaq != 'true'}">
					<div id="headernavbarright">
						<div class="navbar">
							<ul>
								<li class="last"><a href="http://www.opennms.org/index.php/FAQ">FAQs</a></li>
							</ul>
						</div>
					</div>
				</c:if>
			</c:otherwise>
		</c:choose>
	  <div class="spacer"><!-- --></div>
	</div>
	</c:otherwise>
</c:choose>

<!-- Body -->
<%-- This <div> tag is unmatched in this file (its matching tag is in the
     footer), so we hide it in a JSP code fragment so the Eclipse HTML
     validator doesn't complain.  See bug #1728. --%>
<%-- Internet Explorer gets miffed and refuses to display the page content
     for certain pages if the following div is empty. (ie when the outer if
     test fails.) Moving the <h2> tags outside the if statement makes it
     happy again --%>
<%= "<div id=\"content\">" %>
<div class="onms">
<h2>
<c:if test="${((param.nonavbar != 'true') && (!empty pageContext.request.remoteUser)) && param.nobreadcrumbs != 'true'}">
   <a href="<%= baseHref %>index.jsp">Home</a>
   <c:forEach var="breadcrumb" items="${paramValues.breadcrumb}">
     <c:if test="${breadcrumb != ''}">
           / <c:out value="${breadcrumb}" escapeXml="false"/>
     </c:if>
   </c:forEach>
</c:if>
</h2>
</div>
