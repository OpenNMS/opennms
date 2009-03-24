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
	import="org.opennms.web.element.*,
		java.util.*,
		java.net.*,
        java.sql.SQLException,
        org.opennms.core.utils.IPSorter,
        org.opennms.web.acegisecurity.Authentication,
        org.opennms.web.svclayer.ResourceService,
        org.opennms.web.asset.Asset,
        org.opennms.web.asset.AssetModel,
        org.opennms.web.inventory.InventoryLayer,
        org.springframework.web.context.WebApplicationContext,
        org.springframework.web.context.support.WebApplicationContextUtils,
        org.opennms.web.element.ElementUtil"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<%!
//    private int m_telnetServiceId;
//    private int m_httpServiceId;
//    private int m_dellServiceId;
//    private int m_snmpServiceId;
    private ResourceService m_resourceService;
//	private AssetModel m_model = new AssetModel();

    public void init() throws ServletException {

	    WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		m_resourceService = (ResourceService) webAppContext.getBean("resourceService", ResourceService.class);
		
		InventoryLayer.init();
    }

%>

<%
	Map<String, Object> nodeModel = new TreeMap<String, Object>();
	Node node_db=null;
	int nodeId=0;
	String elementID="";

	try {
		
		node_db = ElementUtil.getNodeByParams(request);
		nodeId = node_db.getNodeId();
		
		elementID = node_db.getLabel();
	
	    String groupID = request.getParameter("groupname");
			
        if (groupID.compareTo("*") == 0){
        	nodeModel = InventoryLayer.getRancidNodeList(elementID);
        }
        else {
        	nodeModel = InventoryLayer.getRancidNodeList(elementID, groupID);
        }
	
	} catch (Exception e) {
		//throw new ServletException("Could node get Rancid Node ", e);
	}
    nodeModel.put("id", elementID);
    pageContext.setAttribute("model", nodeModel);
%>

<%
String nodeBreadCrumb = "<a href='element/node.jsp?node=" + nodeId  + "'>Node</a>";
String nodeBreadCrumb2 = "<a href='inventory/rancid.htm?node=" + nodeId  + "'>Rancid</a>";
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Rancid" />
  <jsp:param name="headTitle" value="${model.id}" />
  <jsp:param name="headTitle" value="Rancid" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<%= nodeBreadCrumb %>" />
  <jsp:param name="breadcrumb" value="<%= nodeBreadCrumb2 %>" />
  <jsp:param name="breadcrumb" value="Inventory List" />
  
</jsp:include>



<h2>Node: ${model.id} </h2>

<!-- general info box -->
<h3>Associated Elements</h3>

<table class="o-box">
<tr>
	<th>Group</th>
	<th>UrlViewVC</th>
	<th>Version</th>
	<th>Revision Update</th>
</tr>
<c:forEach items="${model.grouptable}" var="groupelm">
	<tr>
		<td>${groupelm.group}</td>
		<td><a href="${groupelm.urlViewVC}">${model.id}</td>
		<td><a href="inventory/invnode.jsp?node=<%=nodeId%>&groupname=${groupelm.group}&version=${groupelm.version}">${groupelm.version}</a></td>
		<td>${groupelm.date}</td>

	</tr>
</c:forEach>
</table>

<jsp:include page="/includes/footer.jsp" flush="false" />
