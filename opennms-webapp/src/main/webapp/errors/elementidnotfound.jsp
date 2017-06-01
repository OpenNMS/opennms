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
	import="org.opennms.web.element.*"
%>
<%@page import="org.opennms.core.utils.WebSecurityUtils" %>

<%
     ElementIdNotFoundException einfe = null;
    
    if( exception instanceof ElementIdNotFoundException ) {
        einfe = (ElementIdNotFoundException)exception;
    }
    else if( exception instanceof ServletException ) {
        einfe = (ElementIdNotFoundException)((ServletException)exception).getRootCause();
    }
    else {
        throw new ServletException( "This error page does not handle this exception type.", exception );
    }
    
%>


<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Error" />
  <jsp:param name="headTitle" value="ID Not Found for <%=einfe.getElemType()%>" />
  <jsp:param name="headTitle" value="Error" />
  <jsp:param name="breadcrumb" value="Error" />
</jsp:include>

<h1><%=einfe.getElemType(true)%> ID Not Found</h1>

<p>
  The <%=einfe.getElemType()%> ID <%=einfe.getBadID()%> is invalid. <%=WebSecurityUtils.sanitizeString(einfe.getMessage())%>
  <br/>
  <% if (einfe.getDetailUri() != null) { %>
  <p>
  To search again by <%=einfe.getElemType()%> ID, enter the ID here:
  </p>

  <form role="form" method="get" action="<%=einfe.getDetailUri()%>">
    <div class="row">
      <div class="form-group col-md-2">
        <label for="input_text">Get&nbsp;details&nbsp;for&nbsp;<%=einfe.getElemType()%>&nbsp;ID:</label>
        <input type="text" class="form-control" id="input_text" name="<%=einfe.getDetailParam()%>"/>
      </div>
    </div>
    <button type="submit" class="btn btn-default">Search</button>
  </form>
  <% } %>
  
  <% if (einfe.getBrowseUri() != null) { %>
  <p>
  To find the <%=einfe.getElemType()%> you are looking for, you can
  browse the <a href="<%=einfe.getBrowseUri()%>"><%=einfe.getElemType()%> list</a>.
  </p>
  <% } %>
</p>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
