<%--

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	isErrorPage="true"
 %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Error" />
  <jsp:param name="headTitle" value="Unexpected Error" />
  <jsp:param name="headTitle" value="Error" />
  <jsp:param name="breadcrumb" value="Error " />
</jsp:include>

<%

    if (exception == null) {
        exception = (Throwable)request.getAttribute("javax.servlet.error.exception");
    }
%>

<h1>The Web User Interface Has Experienced an Unexpected Error</h1>

<p>
  The webUI has encountered an exception condition that it does
  not know how to handle.
</p>

<p>
  Possible causes could be that the database is not responding,
  the OpenNMS application has stopped or is not running, or there
  is an issue with the servlet container.
</p>

<p>
  You can try going to the main page and hitting "refresh"
  in your browser, but there is a good chance that will have
  no effect. Please bring this message to the attention of the
  person responsible for maintaining OpenNMS for your organization,
  and have them insure that OpenNMS, the external servlet container
  (if applicable), and the database are all running without errors.
</p>

<h3>Error Details</h3>

  <pre><%
    while (exception != null) {
      exception.printStackTrace(new java.io.PrintWriter(out));

      if (exception instanceof ServletException) {
        exception = ((ServletException) exception).getRootCause();
      } else {
        exception = exception.getCause();
      }

      if (exception != null) {
        out.print("Caused by: ");
      }
    }
  %></pre>

<jsp:include page="/includes/footer.jsp" flush="false" />
