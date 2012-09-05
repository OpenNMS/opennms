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

<%@ page language="java" contentType="text/html" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.Enumeration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%!
public void outputNamesInScope(JspWriter out, PageContext context, String title, int scope) throws IOException {
    out.println("<h3>"+title+"</h3>");
    out.println("<DL>");
	Enumeration en = context.getAttributeNamesInScope(scope);
	while (en.hasMoreElements()) {
	    String item = (String)en.nextElement();
	    out.println("<DT>"+item+"</DT>");
	    out.println("<DD>"+context.getAttribute(item)+"</DD>");
	}
    out.println("</DL>");
}
 %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html">
<title>Insert title here</title>
</head>
<body>

<c:set var="testVar">
Test Value
</c:set>

<hr/>
<h3>Above c:out</h3>
<c:out value="${testVar}"/>
<h3>Below c:out</h3>
<hr/>

<jsp:useBean id="testVar" type="java.lang.String" />

<hr/>
<h3>Above sriptlet</h3>
<%= pageContext.getAttribute("testVar") %>
<h3>Below scriptlet</h3>
<hr/>

<hr/>
<h3>Above useBean2</h3>
<%= testVar %>
<h3>Below useBean2</h3>
<hr/>

<h3>Parameters</h3>
<dl>
<c:forEach var="parm" items="${param}">
	<dt><c:out value="${parm.key}"/></dt>
	<dd><c:out value="${parm.value}"/></dd>
</c:forEach>
</dl>
<%
outputNamesInScope(out, pageContext, "PageScope", PageContext.PAGE_SCOPE);
outputNamesInScope(out, pageContext, "ApplicationScope", PageContext.APPLICATION_SCOPE);
outputNamesInScope(out, pageContext, "RequestScope", PageContext.REQUEST_SCOPE);
outputNamesInScope(out, pageContext, "SessionScope", PageContext.SESSION_SCOPE);
%>
</body>
</html>
