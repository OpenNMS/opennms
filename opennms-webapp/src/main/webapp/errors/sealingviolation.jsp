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
	isErrorPage="true"
        import="org.opennms.web.utils.ExceptionUtils"
%>

<%
    SecurityException e = ExceptionUtils.getRootCause(exception, SecurityException.class);
    if( !e.getMessage().equals( "sealing violation" )) {
        throw new ServletException( "security exception", e );
    }
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Error" />
  <jsp:param name="headTitle" value="Incorrect Jar Files" />
  <jsp:param name="headTitle" value="Error" />
  <jsp:param name="breadcrumb" value="Error" />
</jsp:include>

<h1>Incorrect Jar Files</h1>

<p>
  Some of the Java Archive files (jar files) in the Tomcat install
  are out of date.  Please replace them by going to this      
  <a href="http://faq.opennms.org/fom-serve/cache/55.html">OpenNMS FAQ
  entry</a> and following the directions there.  Otherwise, your OpenNMS
  Web system will not work correctly, and you will get undefined results.
</p>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
