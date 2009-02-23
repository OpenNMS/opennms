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
	String url = "http://www.rionero.com/rws-current";
	try {	

		nodeModel = InventoryLayer.getInventoryElement(elementID);


	} catch (Exception e) {
		//throw new ServletException("Could node get Rancid Node ", e);
	}
    nodeModel.put("id", elementID);
    pageContext.setAttribute("model", nodeModel);
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Inventory" />
  <jsp:param name="headTitle" value="${model.id}" />
  <jsp:param name="headTitle" value="Inventory" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="Inventory" />
</jsp:include>

<h2>Element: ${model.id}</h2>

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
        <th>Element Name</th>
        <th>${model.name}</th>
    </tr>
    <tr>
	    <th>Vendor</th>
	    <th>${model.vendor}</th>
    </tr>
    <tr>
	    <th>SysOid</th>
	    <th>${model.sysoid}</th>
    </tr>
    <tr>
		<th>Model Type</th>
		<th>${model.modeltype}</th>
	</tr>
	
    <tr>
	    <th>Serial Number</th>
	    <th>${model.serialnumber}</th>
	   </tr>
	<tr>
		<th>Product Part Number</th>
		<th>${model.productpartnumber}</th>
	</tr>
	<tr>
		<th>Hardware Version</th>
		<th>${model.hardwareversion}</th>
	</tr>
	<tr>
		<th>Ram Size</th>
		<th>${model.ramsize}</th>
	</tr>
	<tr>
		<th>Nw Ram Size</th>
		<th>${model.nwramsize}</th>
	</tr>
	
</table>
</div>

<jsp:include page="/includes/footer.jsp" flush="false" />
