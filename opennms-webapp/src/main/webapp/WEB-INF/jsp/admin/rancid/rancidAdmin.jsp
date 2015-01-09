<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java"
	contentType="text/html"
	session="true"%>
<%@page language="java" contentType="text/html" session="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Rancid" />
  <jsp:param name="headTitle" value="${model.id}" />
  <jsp:param name="headTitle" value="Admin Rancid" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Admin Rancid" />
</jsp:include>

<div class="row">
  <div class="col-md-6">
    <!-- general info box -->
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">General (Status: ${model.status_general})</h3>
      </div>
      <table class="table table-condensed">
		<tr>
			<th>Node</th>
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
    </div> <!-- panel -->

    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Rancid Info</h3>
      </div>
      <table class="table table-condensed">
	<c:choose>
    <c:when test="${model.permitModifyClogin}">
		<c:choose>
		<c:when test="${model.deviceexist}">
<form id="updateForm" method="post" name="updateForm" onsubmit="return validateFormUpdate();">
			<input type="hidden" name="groupName" value="${model.groupname}"/>
			<input type="hidden" name="deviceName" value="${model.id}"/>
		<tr>
			<th>Device Name</th>
			<td>${model.id}</td>
		</tr>		 
		<tr>
			<th>Group</th>
			<td>${model.groupname}</td>
		</tr>	
		<tr>
			<th>Device Type</th>
			<td>					
			<select name="deviceTypeName" class="form-control">
			 <option value="${model.devicetype}">${model.devicetype}</option>
			<c:forEach items="${model.devicetypelist}" var="devicetypelem">
			 <option value="${devicetypelem}">${devicetypelem}</option>
			</c:forEach>
			</select>
			</td>
		</tr>
		<tr>
			<th>Comment</th>
			<td><input id="comment" type="text" class="form-control" name="comment" value="${model.comment}"/></td>
		</tr>
		
		<tr>
			<th>Status</th>
			<td><em>
			<select name="statusName" class="form-control" onChange="switchStatus()">
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
			<input name="updateInput" id="updateButton" class="btn btn-default" type="submit" value="Update"/>
			<input name="deleteInput" id="deleteButton" class="btn btn-default" type="button" value="Delete" onclick="validateFormDelete()"/>
</form>
		</th>
		</tr>
		</c:when>
		<c:otherwise>
 <form id="createForm" method="post" name="createForm" onsubmit="return validateFormCreate();">
			<input type="hidden" name="groupName" value="${model.groupname}"/>
			<input type="hidden" name="deviceName" value="${model.id}"/>
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
			<select name="deviceTypeName" class="form-control">
			 <option value="${model.devicetype}">${model.devicetype}</option>
			<c:forEach items="${model.devicetypelist}" var="devicetypelem">
			 <option value="${devicetypelem}">${devicetypelem}</option>
			</c:forEach>
			</select>
			</td>
		</tr>
		<tr>
			<th>Comment</th>
			<td><input id="comment" type="text" class="form-control" name="comment" value="${model.comment}"/></td>
		</tr>
		
		<tr>
			<th>Status</th>
			<td><em>
			<select name="statusName" class="form-control">
			<option value="up">up</option>
			<option value="down">down</option>
			</select>
			</em></td>
		</tr>
		<tr>
		<th></th>
		<th>
			<input name="createInput" id="createButton" class="btn btn-default" type="submit" value="Create"/>
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
			<td>
			 <c:if test="${!empty model.status}">			
			<em>${model.status}</em>
	<form id="newUserForm2" method="post" name="newUserForm2" onsubmit="return validateFormInputStatus();">	
	<input name="newStatus" class="btn btn-default" id="doOKStatus" type="submit" value="Switch"/>
	<input type="hidden" name="statusName" value="${model.status}"/>
	<input type="hidden" name="groupName" value="${model.groupname}"/>
	<input type="hidden" name="deviceName" value="${model.id}"/>
	</form>
	</c:if>
			</td>
		</tr>
		</c:otherwise>
		</c:choose>
	</table> 
    </div> <!-- panel -->
	

	<c:choose>
    <c:when test="${model.permitModifyClogin}">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Clogin Info</h3>
      </div>
	<form id="newUserForm" method="post" name="newUserForm" onsubmit="return validateFormInput();">
		 <input type="hidden" name="groupName" value="${model.groupname}"> 
		 <input type="hidden" name="deviceName" value="${model.id}"> 
		<table class="table table-condensed">
			<tr>
			    <th><label id="userIDLabel" for="userID">Username:</label></th>
			    <td><input id="userID" type="text" class="form-control" name="userID" value="${model.cloginuser}"/></td>
			 </tr>
		
			 <tr>
			 	<th><label id="pass1Label" for="password">Password:</label></th>
			 	<td><input id="pass" type="text" class="form-control" name="pass" value="${model.cloginpassword}"/></td>
			 </tr>
			 <tr>
			 	<th><label id="enpass1Label" for="enpassword">Enable password:</label></th>
			 	<td><input id="enpass" type="text" class="form-control" name="enpass" value="${model.cloginenablepass}"/></td>
			 </tr>
			 <tr>
				 <th><label id="loginMethodLabel" for="loginMethod">Connection Method:</label></th>
				 <td>
					  <select name="loginM" class="form-control">
					  <option value="${model.cloginconnmethod}">${model.cloginconnmethod}</option>
					  <option value="ssh">ssh</option>
					  <option value="telnet">telnet</option>
					  </select>
				 </td>
			 </tr>
			 <tr>
			 	<th><label id="autoEnableLabel" for="autoEnable">AutoEnable:</label></th>
			 	<td>
				  <select name="autoE" class="form-control">
				  <option value="${model.cloginautoenable}">${model.cloginautoenable}</option>
				  <option value="1">1</option>
				  <option value="0">0</option>
				  </select>
				</td>
			 </tr>
		
			 <tr>
			 	<th></th>
			 	<th><input id="doCancel" type="button" class="btn btn-default" value="Cancel" onclick="cancelUser()"/>
			 		<input id="doOK" type="submit" class="btn btn-default" value="OK"/>
			 		<input id="doDelete" type="button" class="btn btn-default" value="Delete" onclick="deleteCloginInfo()"/>
			 	</th>
			 </tr>	
		 </table>
	 </form>
    </div> <!-- panel -->
	 </c:when>
	 <c:otherwise>
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Clogin Info (Requisitioned Node)</h3>
      </div>
      <table class="table table-condensed">
			<tr>
			 	<th>Requisition: </th>
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
    </div> <!-- panel -->
	 
	 </c:otherwise>
	 </c:choose>

    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Select Group</h3>
      </div>
      <table class="table table-condensed">
	<tr>
	<th>Group</th>
	</tr>
	<c:forEach items="${model.grouplist}" var="groupelem">	
	<tr><td><a href="admin/rancid/rancidAdmin.htm?node=${model.db_id}&group=${groupelem}">${groupelem}</a></td></tr>
	</c:forEach>
      </table>
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Descriptions</h3>
      </div>
      <div class="panel-body">
        <p> 
            Set to <em>true</em> the opennms.rancidIntegrationUseOnlyRancidAdapter property in <em>opennms.properties</em> 
            if you want use only the RancidAdapter to provision nodes to Rancid.
        </p>
        <p>Detailed Documentation on all options can be found on <a title="The OpenNMS Project wiki" href="http://www.opennms.org" target="new">the OpenNMS wiki</a>.
        </p>
          <p><b>Select Group </b>: select the <em>Rancid group</em> to work on</p>
        
          <p><b>Rancid Info</b>: Switch the Rancid status from <em>up</em> to <em>down</em> or from <em>down</em> to <em>up</em>
          	for selected group.
          	You are able to create, delete and modify the node data in rancid.db by default unless the opennms.rancidIntegrationUseOnlyRancidAdapter 
            property is set to <em>true</em> in opennms.properties.
          </p>
          
         <p><b>Clogin Info</b>:  Modify the data according to the authentication information.
          	Click <b>OK</b> to commit changes to Rancid. 
              Also you are able to override the clogin data in .cloginrc by default unless the opennms.rancidIntegrationUseOnlyRancidAdapter 
              property is set to <em>true</em> in opennms.properties.
          	In the case the node was requisitioned the <b>Clogin</b> box shows the name of the
              requisition under which the node was added.
              Click on the asset page or edit the requisition to modify asset information for the node.
              You must re-synchronize the requisition to modify the Clogin information in Rancid.
          </p>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<script type="text/javascript">
function deleteCloginInfo() {
	  document.newUserForm.action="admin/rancid/rancidDeleteClogin.htm?node=${model.db_id}";
	  document.newUserForm.submit();	
}

function validateFormInput() 
{
	  
  if (document.newUserForm.userID.value == "") {
	  alert("The user field cannot be empty");
	  return false;
  }
  if (document.newUserForm.pass.value == "") {
	  alert("The password field cannot be empty");
	  return false;
  }
  if (document.newUserForm.loginM.value == "") {
	  alert("The login method field cannot be empty");
	  return false;
  }
  document.newUserForm.action="admin/rancid/rancidClogin.htm?node=${model.db_id}";
  return true;
}    
function cancelUser()
{
    document.newUserForm.action="admin/rancid/rancidAdmin.htm?node=${model.db_id}";
    document.newUserForm.submit();
}
function validateFormInputStatus() {
	  document.newUserForm2.action="admin/rancid/rancidStatus.htm?node=${model.db_id}";
	  return true;
}

function validateFormCreate() {
	  if (document.createForm.deviceTypeName.value == "") {
		  alert("The Device Type field cannot be empty");
		  return false;
	  }

	  if (document.createForm.groupName.value == "") {
		  alert("The group field cannot be empty");
		  return false;
	  }

	  if (document.createForm.statusName.value == "") {
		  alert("The Status field cannot be empty");
		  return false;
	  }
	  
	  document.createForm.action="admin/rancid/rancidCreate.htm?node=${model.db_id}";
	  return true;
}

function validateFormDelete() {
	
	  document.updateForm.action="admin/rancid/rancidDelete.htm?node=${model.db_id}";
	  document.updateForm.submit();
}

function validateFormUpdate() {

	  document.updateForm.action="admin/rancid/rancidUpdate.htm?node=${model.db_id}";
	  return true;
}

</script>

