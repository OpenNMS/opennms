<%--

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
//      http://www.opennms.com///

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.WebSecurityUtils,
		org.opennms.web.element.*,
		org.opennms.web.*
	"
%>

<%
    String nodeIdString = request.getParameter("node");
    String ipAddr = request.getParameter("ipaddr");
    
    if( nodeIdString == null ) {
        throw new MissingParameterException("node");
    }
    
    int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
    String nodeLabel = NetworkElementFactory.getNodeLabel(nodeId);
        
    String returnUrl = null;        
    if( ipAddr == null ) {        
        returnUrl = "element/node.jsp?node=" + nodeIdString;
    }
    else {
        returnUrl = "element/interface.jsp?node=" + nodeIdString + "&intf=" + ipAddr;    
    }
%>

<% if( ipAddr == null ) { %>
  <% String breadcrumb1 = "<a href='element/index.jsp'>Search</a>"; %>
  <% String breadcrumb2 = "<a href='element/node.jsp?node=" + nodeId  + "'>Node</a>"; %>
  <% String breadcrumb3 = "Rescan"; %>
  <jsp:include page="/includes/header.jsp" flush="false" >
    <jsp:param name="title" value="Rescan" />
    <jsp:param name="headTitle" value="Rescan" />
    <jsp:param name="headTitle" value="Element" />
    <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
    <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
    <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  </jsp:include>
<% } else { %>
  <% String intfCrumb = ""; %>
  <% String breadcrumb1 = "<a href='element/index.jsp'>Search</a>"; %>
  <% String breadcrumb2 = "<a href='element/node.jsp?node=" + nodeId  + "'>Node</a>"; %>
  <% String breadcrumb3 = "<a href='element/interface.jsp?node=" + nodeId + "&intf=" + ipAddr  + "'>Interface</a>"; %>
  <% String breadcrumb4 = "Rescan"; %>
  <jsp:include page="/includes/header.jsp" flush="false" >
    <jsp:param name="title" value="Rescan" />
    <jsp:param name="headTitle" value="Rescan" />
    <jsp:param name="headTitle" value="Element" />
    <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
    <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
    <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
    <jsp:param name="breadcrumb" value="<%=breadcrumb4%>" />
  </jsp:include>
<% } %>

<div class="TwoColLAdmin">
      <h3>Capability Rescan</h3>
      
      <p>Are you sure you want to rescan the <nobr><%=nodeLabel%></nobr>      
        <% if( ipAddr==null ) { %>
            node?
        <% } else { %>
            (<%=ipAddr%>) interface?
        <% } %>
      </p>
      
      <form method="post" action="element/rescan">
        <p>
          <input type="hidden" name="node" value="<%=nodeId%>" />
          <input type="hidden" name="returnUrl" value="<%=returnUrl%>" />             

          <input type="submit" value="Rescan" />
          <input type="button" value="Cancel" onClick="window.open('<%=Util.calculateUrlBase(request) + "/" + returnUrl%>', '_self')" />             
        </p>
      </form>
  </div>

<div class="TwoColRAdmin">
      <h3>Capability Scanning</h3>
    
      <p>
        A <em>capability scan</em> is a suite of tests to determine what <em>capabilities</em>
        a node or interface has.  A capability is in most cases a service, like FTP or HTTP.
      </p>      
  </div>

<jsp:include page="/includes/footer.jsp" flush="false" />
