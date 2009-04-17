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
  <jsp:param name="headTitle" value="Admin Rancid" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Admin Rancid" />
</jsp:include>

<div class="TwoColLeft">
    <!-- general info box -->
    <h3>General (Status: ${model.status_general})</h3>
    <table class="o-box">
		<tr>
			<th width="50%">Node</th>
	  		<td><a href="element/node.jsp?node=${model.db_id}">${model.id}</a></td>
	  	</tr>
		<tr>
	  		<th>Foreign Source</th>
	  		<td>${model.foreignSource}</td>
	  	</tr>
		<tr>
	  		<th>RWS status</th>
	  		<td>${model.RWSStatus}</td>
	  	</tr>
	  	
	</table>

	<h3>Select Group</h3>
	<table class="o-box">
	<tr>
	<th>Group</th>
	</tr>
	<c:forEach items="${model.grouplist}" var="groupelem">	
	<tr><td><a href="admin/rancid/rancidAdmin.htm?node=${model.db_id}?group=${groupelem}">${groupelem}</a></td></tr>
	</c:forEach>
	</table>
  
	<h3>Rancid Info</h3>
		<table class="o-box">
	<c:choose>
    <c:when test="${model.permitModifyClogin}">
		<c:choose>
		<c:when test="${model.deviceexist}">
<form id="updateForm" method="post" name="updateForm">				
			<INPUT TYPE="hidden" NAME="groupName" VALUE="${model.groupname}"> 
			<INPUT TYPE="hidden" NAME="deviceName" VALUE="${model.id}">			
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
			<td>					
			<select name="deviceTypeName" size="1">
			 <option value="${model.devicetype}">${model.devicetype}</option>
			<c:forEach items="${model.devicetypelist}" var="devicetypelem">
			 <option value="${devicetypelem}">${devicetypelem}</option>
			</c:forEach>
			</select>
			</td>
		</tr>
		<tr>
			<th>Comment</th>
			<td><input id="comment" type="text" name="comment" value="${model.comment}"></td>
		</tr>
		
		<tr>
			<th>Status</th>
			<td><em>
			<select name="statusName" size="1" onChange="switchStatus()">
			<option value="${model.status}">${model.status}</option>
			<c:choose> 
  				<c:when test="${model.status == 'up'}" >
  			<option value="down">down</option>
  				</c:when>
  				<c:otherwise>
  			<option value="up">up</option>
  				</c:otherwise>
  			</c:choose>
			</select>
			</em></td>
		</tr>
		<tr>
		<th></th>
		<th>
			<input name="updateInput" id="updateButton" type="submit" value="Update" onClick="validateFormUpdate()">
			<input name="deleteInput" id="deleteButton" type="submit" value="Delete" onClick="validateFormDelete()">
</form>
		</th>
		</tr>
		</c:when>
		<c:otherwise>
 <form id="createForm" method="post" name="createForm">
			<INPUT TYPE="hidden" NAME="deviceName" VALUE="${model.id}"> 			
		<tr>
			<th width="50%">Device Name</th>
			<td>${model.id}</td>
		</tr>		 
		<tr>
			<th>Group</th><td>
			<select name="groupName" size="1">
	<c:forEach items="${model.grouplist}" var="groupelem">
	<option value="${groupelem}">${groupelem}</option>	
	</c:forEach>
	</select>
		</td>
		</tr>	
		<tr>
			<th>Device Type</th>
			<td>					
			<select name="deviceTypeName" size="1">
			 <option value="${model.devicetype}">${model.devicetype}</option>
			<c:forEach items="${model.devicetypelist}" var="devicetypelem">
			 <option value="${devicetypelem}">${devicetypelem}</option>
			</c:forEach>
			</select>
			</td>
		</tr>
		<tr>
			<th>Comment</th>
			<td><input id="comment" type="text" name="comment" value="${model.comment}"></td>
		</tr>
		
		<tr>
			<th>Status</th>
			<td><em>
			<select name="statusName" size="1">
			<option value="up">up</option>
			<option value="down">down</option>
			</select>
			</em></td>
		</tr>
		<tr>
		<th></th>
		<th>
			<input name="createInput" id="createButton" type="submit" value="Create" onClick="validateFormCreate()">
</form>
		</th>
		</tr>
				
	</c:otherwise>
	</c:choose>
	</c:when>
	<c:otherwise>
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
	<form id="newUserForm2" method="post" name="newUserForm2">	
	<input name="newStatus" id="doOKStatus" type="submit" value="Switch" onClick="validateFormInputStatus()">
	<INPUT TYPE="hidden" NAME="statusName" VALUE="${model.status}">
	<INPUT TYPE="hidden" NAME="groupName" VALUE="${model.groupname}"> 
	<INPUT TYPE="hidden" NAME="deviceName" VALUE="${model.id}"> 
	</form>
			</td>
		</tr>
		</c:otherwise>
		</c:choose>
	</table> 
	

	<c:choose>
    <c:when test="${model.permitModifyClogin}">
	<h3>Clogin Info</h3>
	<form id="newUserForm" method="post" name="newUserForm">
		 <INPUT TYPE="hidden" NAME="groupName" VALUE="${model.groupname}"> 
		 <INPUT TYPE="hidden" NAME="deviceName" VALUE="${model.id}"> 
		<table class="o-box">
			<tr>
			    <th width="50%"><label id="userIDLabel" for="userID">Username:</label></th>
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
			 		<input id="doDelete" type="submit" value="Delete" onClick="deleteCloginInfo()">
			 	</th>
			 </tr>	
		 </table>
	 </form>
	 </c:when>
	 <c:otherwise>
		<h3>Clogin Info (Provisioned Node)</h3>
		<table class="o-box">
			<tr>
			 	<th width="50%">Provisioning group: </th>
			 	<td>${model.foreignSource}
					<a href="admin/provisioningGroups.htm">(provisioning)</a>
					<a href="asset/modify.jsp?node=${model.db_id}">(asset)</a>
				</td>
			</tr>
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
      <p> 
          Set to <em>true</em> the opennms.rancidIntegrationUseOnlyRancidAdaper property in <em>opennms.properties</em> 
          if you want use only the RancidAdapter to provision nodes to Rancid.
      </p>
      <p>Detailed Documentation on all options can be found on <a title="The OpenNMS Project wiki" href="http://www.opennms.org" target="new">the OpenNMS wiki</a>.
      </p>
        <p><b>Select Group </b>: select the <em>Rancid group</em> to work on</p>
      
        <p><b>Rancid Info</b>: Switch the Rancid status from <em>up</em> to <em>down</em> or from <em>down</em> to <em>up</em>
        	for selected group.
        	You are able to create, delete and modify the node data in rancid.db by default unless the opennms.rancidIntegrationUseOnlyRancidAdaper 
          property is set to <em>true</em> in opennms.properties.
        </p>
        
       <p><b>Clogin Info</b>:  Modify the data according to the authentication information.
        	Click <b>doOk</b> to commit changes to Rancid. 
            Also you are able to overwrite the clogin data in .cloginrc by default unless the opennms.rancidIntegrationUseOnlyRancidAdaper 
            property is set to <em>true</em> in opennms.properties.
        	In the case the node was provisioned the <b>Clogin</b> box shows the Foreign Source
            under which the node was provisioned.
            Click on (asset) page or on group (provisiong) page to modify asset information for the node.
            It must reimported the Foreign Source group to modify the Clogin information on Rancid.
        </p>
                

 
      </div>
  </div>
  <hr />

<jsp:include page="/includes/footer.jsp" flush="false" />

<script language="JavaScript">
function deleteCloginInfo() {
	  document.newUserForm.action="admin/rancid/rancidDeleteClogin.htm?node=${model.db_id}";
	  document.newUserForm.submit();	
}

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
    document.newUserForm.action="admin/rancid/rancidAdmin.htm?node=${model.db_id}";
    document.newUserForm.submit();
}
function validateFormInputStatus() {
	  document.newUserForm2.action="admin/rancid/rancidStatus.htm?node=${model.db_id}";
	  document.newUserForm2.submit();
}

function validateFormCreate() {
	  if (document.createForm.deviceTypeName.value == "") {
		  alert("The Device Type field cannot be empty");
		  return;
	  }

	  if (document.createForm.groupName.value == "") {
		  alert("The group field cannot be empty");
		  return;
	  }

	  if (document.createForm.statusName.value == "") {
		  alert("The Status field cannot be empty");
		  return;
	  }
	  
	  document.createForm.action="admin/rancid/rancidCreate.htm?node=${model.db_id}";
	  document.createForm.submit();
}

function validateFormDelete() {
	
	  document.updateForm.action="admin/rancid/rancidDelete.htm?node=${model.db_id}";
	  document.updateForm.submit();
}

function validateFormUpdate() {

	  document.updateForm.action="admin/rancid/rancidUpdate.htm?node=${model.db_id}";
	  document.updateForm.submit();
}

</script>

