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
        org.opennms.web.svclayer.ResourceService,
        org.opennms.web.inventory.InventoryLayer,
        org.springframework.web.context.WebApplicationContext,
        org.springframework.web.context.support.WebApplicationContextUtils,
        org.opennms.web.element.ElementUtil"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<%!
    private ResourceService m_resourceService;

    public void init() throws ServletException {

	    WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		m_resourceService = (ResourceService) webAppContext.getBean("resourceService", ResourceService.class);
		
		InventoryLayer.init();
    }
%>

<%

	Node node_db = ElementUtil.getNodeByParams(request);
	int nodeId = node_db.getNodeId();
	
	
	//nodeModel.put("id", Integer.toString(nodeId));
	//nodeModel.put("label", node_db.getLabel());
	String elementID = node_db.getLabel();
	
    String groupID = request.getParameter("groupname");
    String versionId = request.getParameter("version");
    
	Map<String, Object> nodeModel = new TreeMap<String, Object>();
	Map<String, Object> nodeModel2 = new TreeMap<String, Object>();
	try {	

		nodeModel = InventoryLayer.getInventoryNode(elementID, groupID, versionId);
		//nodeModel2 = InventoryLayer.getInventoryNodeList(elementID);

	} catch (Exception e) {
		//throw new ServletException("Could node get Rancid Node ", e);
	}
    nodeModel.put("id", elementID);
    pageContext.setAttribute("model", nodeModel);
%>
<%
String nodeBreadCrumb = "<a href='element/node.jsp?node=" + nodeId  + "'>Node</a>";
String nodeBreadCrumb2 = "<a href='inventory/rancid.jsp?node=" + nodeId  + "'>Rancid</a>";
%>
<jsp:include page="/includes/header.jsp" flush="false" >
<jsp:param name="title" value="Inventory" />
<jsp:param name="headTitle" value="${model.id}" />
<jsp:param name="headTitle" value="Rancid" />
<jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
<jsp:param name="breadcrumb" value="<%= nodeBreadCrumb %>" />
<jsp:param name="breadcrumb" value="<%= nodeBreadCrumb2 %>" />
<jsp:param name="breadcrumb" value="Inventory" />
</jsp:include>


<h2>Node: ${model.id}</h2>


<div class="TwoColLeft">
  <!-- general info box -->
  <h3>General (Status: ${model.status})</h3>
	<table>
  	<tr>
  		<th>Node</th>
  		<td><a href="element/node.jsp?node=<%=nodeId%>"><%=elementID%></a></td>
  </tr>
  </table>
  <h3>Element info</h3>

  <table>
	<tr>
		<th>Group Name</th>
		<th>${model.groupname}</th>
	</tr>
	<tr>
		<th>Version</th>
		<th>${model.version}</th>
	</tr>
	<tr>
		<th>Rancid Name</th>
		<th>${model.devicename}</th>
	</tr>
	<tr>
	    <th>Creation Date</th>
	    <th>${model.creationdate}</th>
	</tr>
   </table>

	<h3>Software configuration</h3>
	
	 <h3>Configuration info</h3>

	 <table>
	    <tr>
		<th>Software Configuration Url</th>
		<th><a href = "${model.swconfigurationurl}" > ${model.swconfigurationurl} </a></th>
		</tr>
    <tr>
	<th>Root Configuration Url</th>
	<th><a href = "${model.configurationurl}" > ${model.configurationurl} </a></th>
	</tr>
	</table>
	
</div>
<div class="TwoColRight">
<!-- general info box -->
<h3>Associated Elements</h3>

<table>
<tr><th><a href="inventory/invelement.jsp?rancidnode=7206PED.wind.lab?group=laboratorio">Slot 0 (Nome) </a></th>
<th>Inventory Type</th>
<th>Vendor</th>
</tr>
<c:forEach items="${model2.inventory}" var="invel">
<tr>
<th>Element </th>
<th>${invel.elementName}</th>
</tr>
</c:forEach>
</table>
</div>

<jsp:include page="/includes/footer.jsp" flush="false" />
