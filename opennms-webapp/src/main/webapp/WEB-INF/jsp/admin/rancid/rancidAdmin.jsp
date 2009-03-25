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
	session="true"%>
<%@page language="java" contentType="text/html" session="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Rancid" />
  <jsp:param name="headTitle" value="${model.id}" />
  <jsp:param name="headTitle" value="Rancid" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<a href='element/node.jsp?node=${model.db_id}'>Node</a>" />
  <jsp:param name="breadcrumb" value="Rancid" />
</jsp:include>

<h2>Node: ${model.id} </h2>

<div class="TwoColLeft">
    <!-- general info box -->
    <h3>General (Status: ${model.status_general})</h3>
    <table class="o-box">
		<tr>
			<th width="50%">Node</th>
	  		<td><a href="element/node.jsp?node=${model.db_id}">${model.id}</a></td>
	  	</tr>
	</table>
  
	<h3>Rancid Info</h3>
	<form id="newUserForm2" method="post" name="newUserForm2">	
	<table class="o-box">
		<tr>
			<th width="50%">Device Name</th>
			<td>${model.id}</td>
		</tr>
		<tr>
		<th>Group</th>
		<td>${model.groupname}</td>
	</tr>	
		<tr>
			<th>Device Type</th>
			<td>${model.devicetype}</td>
		</tr>
		<tr>
			<th>Comment</th>
			<td>${model.comment}</td>
		</tr>
	
		<tr>
			<th>Status</th>
			<td><em>${model.status}</em>
				<input name="newStatus" id="doOKStatus" type="submit" value="Switch" onClick="validateFormInputStatus()">
			</td>
		</tr>
	</table> 
	<INPUT TYPE="hidden" NAME="statusName" VALUE="${model.status}">
	<INPUT TYPE="hidden" NAME="groupName" VALUE="${model.groupname}"> 
	<INPUT TYPE="hidden" NAME="deviceName" VALUE="${model.id}"> 
	</form>

	<c:choose>
    <c:when test="${model.permitModifyClogin}">
	<h3>Clogin Info</h3>
	<form id="newUserForm" method="post" name="newUserForm">
		 <INPUT TYPE="hidden" NAME="groupName" VALUE="${model.groupname}"> 
		 <INPUT TYPE="hidden" NAME="deviceName" VALUE="${model.id}"> 
		<table class="o-box">
			<tr>
			    <th><label id="userIDLabel" for="userID">Username:</label></th>
			    <td><input id="userID" type="text" name="userID" value="${model.cloginuser}"></td>
			 </tr>
		
			 <tr>
			 	<th><label id="pass1Label" for="password">Password:</label></th>
			 	<td><input id="pass" type="text" name="pass" value="${model.cloginpassword}" ></td>
			 </tr>
			 <tr>
			 	<th><label id="enpass1Label" for="enpassword">Enable password:</label></th>
			 	<td><input id="enpass" type="text" name="enpass" value="${model.cloginenablepass}" ></td>
			 </tr>
			 <tr>
				 <th><label id="loginMethodLabel" for="loginMethod">Connection Method:</label></th>
				 <td>
					  <select name="loginM" size="1">
					  <option value="${model.cloginconnmethod}">${model.cloginconnmethod}</option>
					  <option value="ssh">ssh</option>
					  <option value="telnet">telnet</option>
					  </select>
				 </td>
			 </tr>
			 <tr>
			 	<th><label id="autoEnableLabel" for="autoEnable">AutoEnable:</label></th>
			 	<td>
				  <select name="autoE" size="1">
				  <option value="${model.cloginautoenable}">${model.cloginautoenable}</option>
				  <option value="1">1</option>
				  <option value="0">0</option>
				  </select>
				</td>
			 </tr>
		
			 <tr>
			 	<th></th>
			 	<th><input id="doCancel" type="button" value="Cancel" onClick="cancelUser()">
			 		<input id="doOK" type="submit" value="OK" onClick="validateFormInput()">
			 	</th>
			 </tr>	
		 </table>
	 </form>
	 </c:when>
	 <c:otherwise>
		<h3>Clogin Info for Provisioned Node</h3>
		<table class="o-box">
			 <tr>
			 	<th width="50%">Provisioning group: </th>
			 	<td>${model.foreignSource}
					<a href="admin/provisioningGroups.htm">(provisioning)</a>
					<a href="asset/modify.jsp?node=${model.db_id}">(asset)</a>
				</td>
			<tr>
			    <th><label id="userIDLabel" for="userID">Username:</label></th>
			    <td>${model.cloginuser}</td>
			 </tr>
		
			 <tr>
			 	<th><label id="pass1Label" for="password">Password:</label></th>
			 	<td>${model.cloginpassword}</td>
			 </tr>
			 <tr>
			 	<th><label id="enpass1Label" for="enpassword">Enable password:</label></th>
			 	<td>${model.cloginenablepass}</td>
			 </tr>
			 <tr>
				 <th><label id="loginMethodLabel" for="loginMethod">Connection Method:</label></th>
				 <td>${model.cloginconnmethod}</td>
			 </tr>
			 <tr>
			 	<th><label id="autoEnableLabel" for="autoEnable">AutoEnable:</label></th>
			 	<td>${model.cloginautoenable}</td>
			 </tr>
		
		 </table>
	 
	 </c:otherwise>
	 </c:choose>
</div>

  <div class="TwoColRight">
      <h3>Descriptions</h3>
      <div class="boxWrapper">
      <p>Detailed Documentation on all options can be found on <a title="The OpenNMS Project wiki" href="http://www.opennms.org" target="new">the OpenNMS wiki</a>.
      </p>
        <p><b>Switch Status</b>: Switch the Rancid status from <em>up</em> to <em>down</em> or from <em>down</em> to <em>up</em>
        </p>
        
       <p><b>Clogin box for provisioned Node</b>: In the case the node was provisioned the <b>Clogin</b> box shows the Foreign Source
             under which the node was provisioned.
             Click on (asset) page or on group (provisiong) page to modify asset information for the node.
             It must reimported the Foreign Source group to modify the Clogin information on Rancid.
        </p>
        
       <p><b>Clogin box </b>: Modify the data according to the authentication information.
        Click <b>doOk</b> to commit changes to Rancid.        
        </p>
        

 
      </div>
  </div>
  <hr />

<jsp:include page="/includes/footer.jsp" flush="false" />

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
  document.newUserForm.action="admin/rancid/rancidClogin.htm?node=${model.db_id}";
  document.newUserForm.submit();
}    
function cancelUser()
{
    document.newUserForm.action="admin/rancid/rancidAdmin.jsp?node=${model.db_id}";
    document.newUserForm.submit();
}
function validateFormInputStatus() {
	  document.newUserForm2.action="admin/rancid/rancidStatus.htm?node=${model.db_id}";
	  document.newUserForm2.submit();
}

</script>

