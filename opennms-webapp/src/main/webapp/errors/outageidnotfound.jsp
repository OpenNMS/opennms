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
	import="org.opennms.web.outage.*, org.opennms.web.utils.ExceptionUtils"
%>

<%
     OutageIdNotFoundException einfe = ExceptionUtils.getRootCause(exception, OutageIdNotFoundException.class);
%>


<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Error" />
  <jsp:param name="headTitle" value="Outage ID Not Found" />
  <jsp:param name="headTitle" value="Error" />
  <jsp:param name="breadcrumb" value="Error" />
</jsp:include>

<h1>Outage ID Not Found</h1>

<p>
  The outage ID <%=einfe.getBadID()%> is invalid. <%=einfe.getMessage()%>
  <br/>
  You can re-enter it here or <a href="outage/list.htm">browse all of the
  outages</a> to find the outage you are looking for.
</p>

<form role="form" method="get" action="outage/detail.htm">
  <div class="row">
    <div class="form-group col-md-2">
      <label for="input_id">Get&nbsp;details&nbsp;for&nbsp;Outage&nbsp;ID:</label>
      <input type="text" class="form-control" name="id"/>
    </div>
  </div>
  <button type="submit" class="btn btn-default">Search</button>
</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
