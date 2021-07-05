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
	import="org.opennms.web.event.*, org.opennms.web.utils.ExceptionUtils, org.opennms.core.utils.WebSecurityUtils"
%>

<%
    EventIdNotFoundException einfe = ExceptionUtils.getRootCause(exception, EventIdNotFoundException.class);
%>


<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Error" />
  <jsp:param name="headTitle" value="Event ID Not Found" />
  <jsp:param name="headTitle" value="Error" />
  <jsp:param name="breadcrumb" value="Error" />
</jsp:include>

<h1>Event ID Not Found</h1>

<p>
  The event ID <%=WebSecurityUtils.sanitizeString(einfe.getBadID())%> is invalid. <%=WebSecurityUtils.sanitizeString(einfe.getMessage())%>
  <br/>
  You can re-enter it here or <a href="event/list.htm?acktyp=unack">browse all
  of the events</a> to find the event you are looking for.
</p>

<form method="get" action="event/detail.jsp">
  <p>
    Get&nbsp;details&nbsp;for&nbsp;Event&nbsp;ID
    <br/>
    <input type="text" name="id"/>
    <input type="submit" value="Search"/>
  </p>
</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
