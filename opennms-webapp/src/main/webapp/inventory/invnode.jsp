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
        org.springframework.web.context.support.WebApplicationContextUtils"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<%!
    private int m_telnetServiceId;
    private int m_httpServiceId;
    private int m_dellServiceId;
    private int m_snmpServiceId;
    private ResourceService m_resourceService;
	private AssetModel m_model = new AssetModel();

    public void init() throws ServletException {

	    WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		m_resourceService = (ResourceService) webAppContext.getBean("resourceService", ResourceService.class);
		
		InventoryLayer.init();
    }
%>

<%

    String elementID = request.getParameter("rancidnode");
    String groupID = request.getParameter("group");
	Map<String, Object> nodeModel = new TreeMap<String, Object>();
	Map<String, Object> nodeModel2 = new TreeMap<String, Object>();
	String url = "http://www.rionero.com/rws-current";
	try {	

		nodeModel = InventoryLayer.getInventoryNode(url,groupID, elementID);
		nodeModel2 = InventoryLayer.getInventoryNodeList(url,groupID, elementID);

	} catch (Exception e) {
		//throw new ServletException("Could node get Rancid Node ", e);
	}
    nodeModel.put("id", elementID);
    nodeModel.put("group", groupID);
    nodeModel.put("url", url);
    pageContext.setAttribute("model", nodeModel);
    pageContext.setAttribute("model2", nodeModel2);
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Inventory" />
  <jsp:param name="headTitle" value="${model.id}" />
  <jsp:param name="headTitle" value="Inventory" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="Inventory" />
</jsp:include>

<h2>Node: ${model.id}</h2>


<div class="TwoColLeft">
  <!-- general info box -->
  <h3>General (Status: ${model.status})</h3>

  <h3>Element info</h3>

  <table>
	<tr>
		<th>RWS Url</th>
		<th>${model.url}</th>
		</tr>
	<tr>
		<th>Group Name</th>
		<th>${model.group}</th>
	</tr>
	<tr>
		<th>Rancid Name</th>
		<th>${model.id}</th>
	</tr>
	
	<tr>
	    <th>Creation Date</th>
	    <th>${model.datecrea}</th>
	</tr>
	<tr>
	    <th>Expiration Date</th>
	    <th>${model.dateexp}</th>
	</tr>
   </table>

	<h3>Software configuration</h3>

	<table>
	  <tr>
	    <th>Software Image</th>
	    <th>${model.softimage}</th>
     </tr>
     <tr>
     	<th>Software Version</th>
     	<th>${model.softversion}</th>
     </tr>
	</table>
	
	 <h3>Configuration info</h3>

	 <table>
	    <tr>
		<th>Software Configuration Url</th>
		<th>${model.softconfigurl}</th>
		</tr>
    <tr>
	<th>Root Configuration Url</th>
	<th>${model.rootconfigurl}</th>
	</tr>
	</table>
	
	<h3>Linked Nodes</h3>
	<table>
	<tr>
		<th>Inventory Element</th>
		<th>
			<c:url var="viewInvNode" value="inventory/invelement.jsp">
			<c:param name="rancidnode" value="${model.id}"/>
			<c:param name="group" value="${model.group}"/>
			</c:url>
			<li>
				<a href="${viewInvNode}">Inventory Element</a>
			</li>
			</th>
	</tr>
	<tr>
	<th>Rancid Node</th>
		<th>
			<c:url var="viewRancidNode" value="inventory/rancid.jsp">
			<c:param name="rancidnode" value="${model.id}"/>
			<c:param name="group" value="${model.group}"/>
			</c:url>
			<li>
				<a href="${viewRancidNode}">Rancid Node</a>
			</li>
		</th>
	</tr>
</table>
</div>
<div class="TwoColRight">
<!-- general info box -->
<h3>Associated Elements</h3>

<h3>List</h3>
<table>
<c:forEach items="${model2.inventory}" var="invel">
<tr>
<th>Element </th>
<th>${invel.elementName}</th>
</tr>
</c:forEach>
</table>
</div>

<jsp:include page="/includes/footer.jsp" flush="false" />
