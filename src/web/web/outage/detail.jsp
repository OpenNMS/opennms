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

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.outage.*,java.text.DateFormat" %>

<%!
    public static DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
%>

<%
    String outageIdString = request.getParameter( "id" );

    if( outageIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "id" );
    }

    int outageId = -1;

    try {
        outageId = Integer.parseInt( outageIdString );
    }
    catch( NumberFormatException e ) {
        throw new OutageIdNotFoundException( "The outage id must be an integer.", outageIdString );
    }

    Outage outage = OutageFactory.getOutage( outageId );

    if( outage == null ) {
        throw new OutageIdNotFoundException( "An outage with this id was not found.", String.valueOf(outageId) );
    }
    
    String action = null;
    String buttonName=null;    
%>

<html>
<head>
  <title>Detail | Outages | OpenNMS Web Console</title>
  <base href="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" leftmargin="0" rightmargin="0" topmargin="0">

<% String breadcrumb1 = "<a href='outage/index.jsp'>Outages</a>"; %>
<% String breadcrumb2 = "Detail"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Outage Details" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br />

<!-- Body -->
<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>
    
    <td width="99%" valign="top">    
      <!-- page title -->
      <h3>Outage: <%=outage.getId()%></h3>
          
      <table width="100%" cellspacing="0" cellpadding="2" class="widget-box" border="1" bordercolor="black">
        <tr>
          <td width="10%" class="widget-box-fieldname">Node:</td>
          <td>
            <% if( outage.getNodeId() > 0 ) { %>
              <a href="element/node.jsp?node=<%=outage.getNodeId()%>"><%=outage.getNodeLabel()%></a>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          
          <td width="10%" class="widget-box-fieldname">Lost&nbsp;Service&nbsp;Time:</td>
          <td><%=DATE_FORMAT.format(outage.getLostServiceTime())%></td>
          
          <td width="10%" class="widget-box-fieldname">Lost&nbsp;Service&nbsp;Event:</td>
          <td><a href="event/detail.jsp?id=<%=outage.getLostServiceEventId()%>"><%=outage.getLostServiceEventId()%></a></td>          
          
        </tr>
        <tr>
          <td class="widget-box-fieldname">Interface:</td>
          <td>
            <% if( outage.getIpAddress() != null ) { %>
              <% if( outage.getNodeId() > 0 ) { %>
                <a href="element/interface.jsp?node=<%=outage.getNodeId()%>&intf=<%=outage.getIpAddress()%>"><%=outage.getIpAddress()%></a>
              <% } else { %>
                <%=outage.getIpAddress()%>
              <% } %>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          
          <td class="widget-box-fieldname">Regained&nbsp;Service:</td>
          <td>
            <% Date regainTime = outage.getRegainedServiceTime(); %>
            
            <% if(regainTime != null) { %>
              <%=DATE_FORMAT.format(regainTime)%>
            <% } else { %>
              <% String label = OutageUtil.getStatusLabel(outage); %>              
              <%=(label == null) ? "&nbsp;" : label %>
            <% } %>
          </td>

          <td class="widget-box-fieldname">Regained&nbsp;Service&nbsp;Event:</td>
          <td>
            <% Integer regainedEventId = outage.getRegainedServiceEventId(); %>
            <% if(regainedEventId != null) { %>
              <a href="event/detail.jsp?id=<%=regainedEventId%>">
                <%=regainedEventId%>
              </a>
            <% } else { %>
              <% String label = OutageUtil.getStatusLabel(outage); %>              
              <%=(label == null) ? "&nbsp;" : label %>
            <% } %>
          </td>          
          
        </tr>
        <tr>
          <td class="widget-box-fieldname">Service:</td>
          <td>
            <% if( outage.getServiceName() != null ) { %>
              <% if( outage.getIpAddress() != null && outage.getNodeId() > 0 ) { %>
                <a href="element/service.jsp?node=<%=outage.getNodeId()%>&intf=<%=outage.getIpAddress()%>&service=<%=outage.getServiceId()%>"><%=outage.getServiceName()%></a>              
              <% } else { %>
                <%=outage.getServiceName()%>
              <% } %>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <td colspan="4">&nbsp;</td>
        </tr>
      </table>
    </td>
    
    <td width="1%" valign="top">&nbsp;</td>
  </tr>
</table>

<br />

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>
