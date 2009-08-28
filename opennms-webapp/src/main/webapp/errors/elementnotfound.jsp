<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
//
// Modifications:
//
// 2009 Aug 27: Created
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
// Foundation, Inc.:
// 51 Franklin Street
// 5th Floor
// Boston, MA 02110-1301
// USA
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
	isErrorPage="true"
	import="org.opennms.web.element.*"
%>

<%
     ElementNotFoundException enfe = null;
    
    if( exception instanceof ElementNotFoundException ) {
        enfe = (ElementNotFoundException)exception;
    }
    else if( exception instanceof ServletException ) {
        enfe = (ElementNotFoundException)((ServletException)exception).getRootCause();
    }
    else {
        throw new ServletException( "This error page does not handle this exception type.", exception );
    }
    
%>


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Error" />
  <jsp:param name="headTitle" value="Element Not Found" />
  <jsp:param name="headTitle" value="Error" />
  <jsp:param name="breadcrumb" value="Error" />
</jsp:include>

<h1><%=enfe.getElemType(true)%>  Not Found</h1>

<p>
  The <%=enfe.getElemType()%> is invalid. <%=enfe.getMessage()%>
  <br/>
  <% if (enfe.getDetailUri() != null) { %>
  <p>
  To search again by <%=enfe.getElemType()%> ID, enter the ID here:
  </p>
  <form method="get" action="<%=enfe.getDetailUri()%>">
  <p>
    Get&nbsp;details&nbsp;for&nbsp;<%=enfe.getElemType()%>&nbsp;:
    <br/>
    <input type="text" name="<%=enfe.getDetailParam()%>"/>
    <input type="submit" value="Search"/>
  </p>
  </form>
  <% } %>
  
  <% if (enfe.getBrowseUri() != null) { %>
  <p>
  To find the <%=enfe.getElemType()%> you are looking for, you can
  browse the <a href="<%=enfe.getBrowseUri()%>"><%=enfe.getElemType()%> list</a>.
  </p>
  <% } %>
</p>

<jsp:include page="/includes/footer.jsp" flush="false" />
