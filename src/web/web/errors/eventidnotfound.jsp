<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//

-->

<%@page language="java" contentType="text/html" session="true" isErrorPage="true" import="org.opennms.web.event.*"%>

<html>
<head>
  <title>Event Id Not Found | Error | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("Error"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Error" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
</jsp:include>

<br>

<%
     EventIdNotFoundException einfe = null;
    
    if( exception instanceof EventIdNotFoundException ) {
        einfe = (EventIdNotFoundException)exception;
    }
    else if( exception instanceof ServletException ) {
        einfe = (EventIdNotFoundException)((ServletException)exception).getRootCause();
    }
    else {
        throw new ServletException( "This error page does not handle this exception type.", exception );
    }
%>


<!-- Body -->

<table width="100%" border="0" cellspacing="0" cellpadding="2">
  <tr>
    <td> &nbsp; </td>

    <td>
      <h1>Event Id Not Found</h1>

      <p>
        The event id <%=einfe.getBadID()%> is invalid. <%=einfe.getMessage()%><br>
        You can re-enter it here or <a href="event/list?acktyp=unack">browse all of the events</a> to find the event you are looking for.
      </p>

      <p>
        <form METHOD="GET" ACTION="event/detail.jsp" >
          Get&nbsp;details&nbsp;for&nbsp;Event&nbsp;ID:<br>
        <input type="TEXT" NAME="id" />
        <input type="submit" value="Search" />
      </form>
      </p>
    </td>

    <td> &nbsp; </td>
  </tr>
</table>                               

<br>


<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>
