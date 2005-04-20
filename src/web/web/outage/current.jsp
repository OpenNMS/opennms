<!--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com/
//

-->

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.outage.*,java.util.*" %>

<%!  
    protected OutageModel model = new OutageModel();
%>

<%  
    Outage[] outages = this.model.getCurrentOutages(); 
%>

<html>
<head>
  <title>Outages | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="css/styles.css" />
</head>

<body bgcolor="white" text="#000000" link="#0000cc" vlink="#800080" alink="#ff0000" marginwidth="0" marginheight="0" LEFTMARGIN="0" rightmargin="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='outage/index.jsp'>Outages</a>"; %>
<% String breadcrumb2 = "Current By Node"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Current Outages" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>
<!-- Body -->
<% if( outages == null || outages.length == 0 ) { %>
  <table border="0">
    <tr>
      <td>&nbsp;</td>
      <td align="left"><h2>All services are up!</h2></td>
      <td>&nbsp;</td>
    </tr>
  </table>
<% } else { %>
  <%
      int nodeCount = 0;
      int interfaceCount = 0;

      int lastNodeId = outages[0].getNodeId();
      String lastIp  = outages[0].getIpAddress();

      ArrayList nodeList  = new ArrayList();
      ArrayList intfList  = new ArrayList();
      ArrayList svcList = new ArrayList();

      ArrayList servicesPerNodeList = new ArrayList();
      int servicesPerNodeCnt = 0;

      for( int i=0; i < outages.length; i++ ) {
          int nodeId = outages[i].getNodeId();
          String ipAddr = outages[i].getIpAddress();

          if( !lastIp.equals(ipAddr) ) {               
             intfList.add( svcList );   
             svcList = new ArrayList();
             interfaceCount++;
          }

          if( nodeId != lastNodeId ) {               
             nodeList.add( intfList );
             servicesPerNodeList.add( new Integer(servicesPerNodeCnt) );

             intfList = new ArrayList();
             servicesPerNodeCnt = 0;
             nodeCount++;               
          }

          svcList.add( outages[i] );
          servicesPerNodeCnt++;

          lastNodeId = nodeId;
          lastIp = ipAddr;
      }

      //add the last one
      intfList.add( svcList );
      nodeList.add( intfList );
      servicesPerNodeList.add( new Integer(servicesPerNodeCnt) );

      interfaceCount++;
      nodeCount++;
  %>           
 
  <table width="100%" border="0" cellspacing="0" cellpadding="2" >
    <tr>
      <td>&nbsp;</td>

      <td width="100%" valign="top" > 
        <a href="outage/list" title="See all outages in the outage browser" >View All Outages</a>
        <%--&nbsp;&nbsp;&nbsp; <a href="outage/advsearch.jsp" title="More advanced searching and sorting options">Advanced Search</a>--%>
        &nbsp;&nbsp;&nbsp; <a href="outage/list?outtype=<%=OutageFactory.OutageType._CURRENT%>" title="A more powerful way of looking at outages">Query Current Outages</a>
        
        <h3>Current Outages</h3>
        
        <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
          <tr BGCOLOR="#999999">
            <td><b>Node</b></td>
            <td WIDTH="15%"><b>Interface</b></td>
            <td width="10%"><b>Service&nbsp;Down</b></td>
            <td WIDTH="30%"><b>Time&nbsp;Down</b></td>
            <td WIDTH="10%"><b>Outage&nbsp;ID</b></td>
          </tr>

          <% for( int nodeIndex=0; nodeIndex < nodeList.size(); nodeIndex++ ) { %>
            <%
                int serviceCnt = ((Integer)servicesPerNodeList.get( nodeIndex )).intValue();
                intfList = (ArrayList)nodeList.get(nodeIndex);
            %>

            <% for( int intfIndex=0; intfIndex < intfList.size(); intfIndex++ ) { %>
              <% svcList = (ArrayList)intfList.get(intfIndex); %>

              <% for( int svcIndex=0; svcIndex < svcList.size(); svcIndex++ ) { %>
                <%
                    Outage outage = (Outage)svcList.get(svcIndex);
                    int nodeId = outage.getNodeId();
                    String ipAddr = outage.getIpAddress();
		    int outageId = outage.getId();
                %>                 

                <tr valign="top" <% if( nodeIndex%2 == 0 ) out.print( "BGCOLOR=\"#cccccc\""); %>>
                  <% if( intfIndex==0 && svcIndex == 0) { %>
                    <td rowspan="<%=serviceCnt%>"><a name="node<%=nodeId%>"/><a HREF="element/node.jsp?node=<%=nodeId%>" title="General information about this node"><%=outage.getNodeLabel()%></a></td>
                  <% } %>

                  <% if( svcIndex==0 ) { %>
                    <td rowspan="<%=svcList.size()%>"><a HREF="element/interface.jsp?node=<%=nodeId%>&intf=<%=ipAddr%>" title="General information about this interface"><%=ipAddr%></a> <%=!ipAddr.equals(outage.getHostname()) ? "(" + outage.getHostname() + ")" : ""%></td>
                  <% } %>
                    
                  <td><a HREF="element/service.jsp?node=<%=nodeId%>&intf=<%=ipAddr%>&service=<%=outage.getServiceId()%>"><%=outage.getServiceName()%></a></td>
                  <td><%=org.opennms.netmgt.EventConstants.formatToUIString(outage.getTimeDown())%></td>
                  <td><a href="outage/detail.jsp?id=<%=outageId%>"><%=outageId%></a></td>
                </tr>
              <% } /* endfor service */ %>
            <% } /*endfor interface */ %>
          <% } /*endfor node */ %>
        
          <tr valign="top" BGCOLOR="#999999">
            <td colspan="5"> 
              <b><%=outages.length%> total services down on <%=interfaceCount%> interfaces of <%=nodeCount%> nodes</b>
            </td>
          </tr>
        </table>
      </td>
    
      <td>&nbsp;</td>
    </tr>
  </table>
<% } %>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="outages" />
</jsp:include>

</body>
</html>
