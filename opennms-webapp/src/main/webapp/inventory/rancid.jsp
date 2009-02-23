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

	String elementID = node_db.getLabel();


	Map<String, Object> nodeModel = new TreeMap<String, Object>();
	Map<String, Object> nodeModel2 = new TreeMap<String, Object>();
	try {	

		//Get the list of groups
	    //Get rancid Info in first group
	    nodeModel = InventoryLayer.getRancidNode(elementID,request.getRemoteUser());

	} catch (Exception e) {
		//throw new ServletException("Could node get Rancid Node ", e);
	}
    nodeModel.put("id", elementID);
    pageContext.setAttribute("model", nodeModel);
%>

<%
String nodeBreadCrumb = "<a href='element/node.jsp?node=" + nodeId  + "'>Node</a>";
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Rancid" />
  <jsp:param name="headTitle" value="${model.id}" />
  <jsp:param name="headTitle" value="Rancid" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<%= nodeBreadCrumb %>" />
  <jsp:param name="breadcrumb" value="Rancid" />
</jsp:include>

<script language="JavaScript">
function validateFormInput() 
{
	
  
  if (document.newUserForm.userID.value == "") {
	  alert("The user field cannot be empty");
	  return;
  }
  if (document.newUserForm.pass.value == "") {
	  alert("The password field cannot be empty");
	  return;
  }
  if (document.newUserForm.loginM.value == "") {
	  alert("The login method field cannot be empty");
	  return;
  }
  //document.newUserForm.redirect.value="/inventory/rancid.jsp?node="+request.getParameter("node");;
  document.newUserForm.action="inventory/invClogin";
  document.newUserForm.submit();
}    
function cancelUser()
{
    document.newUserForm.action="inventory/rancid.jsp?node=<%=nodeId%>";
    document.newUserForm.submit();
}

</script>

<h2>Node: ${model.id} </h2>


<div class="TwoColLeft">
  <!-- general info box -->
  <h3>General (Status: ${model.status})</h3>
  	<table>
  	<tr>
  		<th>Node</th>
  		<td><a href="element/node.jsp?node=<%=nodeId%>"><%=elementID%></a></td>
  </tr>
  </table>
  
  <h3>Rancid info</h3>

  <table>

	<tr>
		<th>Device Name</th>
		<th>${model.id}</th>
	</tr>	
	<tr>
		<th>Device Type</th>
		<th>${model.devicetype}</th>
	</tr>
	<tr>
		<th>Comment</th>
		<th>${model.comment}</th>
	</tr>
	
</table>


<h3>Clogin info</h3>
<form id="newUserForm" method="post" name="newUserForm">
<table>
  <tr>
    <td width="10%"><label id="userIDLabel" for="userID">User:</label></td>
    <td width="100%"><input id="userID" type="text" name="userID" value="${model.cloginuser}"></td>
  </tr>

  <tr>
    <td width="10%"><label id="pass1Label" for="password">Password:</label></td>
    <td width="100%"><input id="pass" type="text" name="pass" value="${model.cloginpassword}" ></td>
  </tr>
  <tr>
  <td width="10%"><label id="enpass1Label" for="enpassword">Enable password:</label></td>
  <td width="100%"><input id="enpass" type="text" name="enpass" value="${model.cloginenablepass}" ></td>
</tr>
 <tr>
  <td width="10%"><label id="loginMethodLabel" for="loginMethod">Connection Method:</label></td>
  <td>
	  <select name="loginM" size="1">
	  <option value="${model.cloginconnmethod}">${model.cloginconnmethod}</option>
	  <option value="ssh">ssh</option>
	  <option value="telnet">telnet</option>
	  </select>
	  </td>
</tr>
<tr>
  <td width="10%"><label id="autoEnableLabel" for="autoEnable">AutoEnable:</label></td>
  <td>
	  <select name="autoE" size="1">
	  <option value="${model.cloginautoenable}">${model.cloginautoenable}</option>
	  <option value="1">1</option>
	  <option value="0">0</option>
	  </select>
	  </td>
 </tr>

  <tr>
    <td><input id="doCancel" type="button" value="Cancel" onClick="cancelUser()"></td>
    <td><input id="doOK" type="submit" value="OK" onClick="validateFormInput()"></td>
  </tr>
</table>
	<INPUT TYPE="hidden" NAME="groupName" VALUE="${model.group}"> 
	<INPUT TYPE="hidden" NAME="deviceName" VALUE="${model.id}"> 
	</form>
</div>

<div class="TwoColRight">
<!-- general info box -->
<h3>Associated Elements</h3>

<table>
<tr>
	<th>Group</th>
	<th>UrlViewVC</th>
	<th>Date</th>
	<th>Total revisions</th>
	<th>Head version</th>
</tr>
<c:forEach items="${model.grouptable}" var="groupelm" begin ="0" end="9">
	<tr>
		<th>${groupelm.group}</th>
		<th><a href="${groupelm.rootConfigurationUrl}">${groupelm.rootConfigurationUrl}</th>
		<th>${groupelm.expirationDate}</th>
		<th><a href="inventory/rancidList.jsp?node=<%=nodeId%>&groupname=${groupelm.group}">${groupelm.totalRevisions}</a></th>
		<th><a href="inventory/invnode.jsp?node=<%=nodeId%>&groupname=${groupelm.group}&version=${groupelm.headRevision}">${groupelm.headRevision}</th>
	</tr>
</c:forEach>
	<th></th>
	<th><a href="inventory/rancidList.jsp?node=<%=nodeId%>&groupname=*">full list...</a></th>
	<th></th>
	<th></th>
	<th></th>
</table>
</div>
<jsp:include page="/includes/footer.jsp" flush="false" />
