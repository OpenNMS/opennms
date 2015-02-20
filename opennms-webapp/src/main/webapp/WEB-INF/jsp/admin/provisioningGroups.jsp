<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib tagdir="/WEB-INF/tags/tree" prefix="tree" %>
<%@ taglib tagdir="/WEB-INF/tags/springx" prefix="springx" %>
<%@ taglib tagdir="/WEB-INF/tags/js" prefix="js" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="Provisioning Requisitions" /> 
	<jsp:param name="headTitle" value="Provisioning Requisitions" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/provisioningGroups.htm'>Provisioning Requisitions</a>" />
	<jsp:param name="script" value="<script type='text/javascript' src='js/provisioningGroups.js'></script>" />
</jsp:include>

<c:url var="editRequisitionUrl" value="admin/editProvisioningGroup.htm" />
<form action="${editRequisitionUrl}" name="editRequisitionForm" method="post">
  <input name="groupName" type="hidden"/>
</form>

<c:url var="editForeignSourceUrl" value="admin/editForeignSource.htm" />
<form action="${editForeignSourceUrl}" name="editForeignSourceForm" method="post">
  <input name="foreignSourceName" type="hidden"/>
</form>

<br />
<form role="form" class="form-inline" action="${relativeRequestPath}" name="takeAction" method="post">
<div>
<input type="text" class="form-control" name="groupName" size="20" placeholder="New_Requisition"/>
<input type="hidden" name="action" value="addGroup" />
<input type="hidden" name="actionTarget" value="" />
<input type="submit" class="btn btn-default" value="Add New Requisition"/>
</div>

<div class="row top-buffer">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Default Foreign Source Definition</h3>
      </div>
      <div class="panel-body">
        <p>Defines scanning and policy behavior for auto-provisioned nodes</p>
        <input type="button" class="btn btn-default" value="Edit Default Foreign Source Definition" onclick="editForeignSource('default')" />
        <input type="button" class="btn btn-default" value="Reset Default Foreign Source Definition" onclick="resetDefaultForeignSource()" />
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

</form>

<c:forEach var="foreignSourceName" items="${foreignSourceNames}">
<c:if test='${foreignSourceName != "default"}'>
<span data-foreignSource="${foreignSourceName}">
<div class="row">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">${foreignSourceName}</h3>
      </div>
      <div class="panel-body">
    <c:choose>
      <c:when test="${dbNodeCounts[foreignSourceName] > 0}">
        <input type="button" class="btn btn-default" value="Delete Nodes" onclick="confirmAction(<js:quote value="${foreignSourceName}"/>, 'deleteNodes', <js:quote value="Are you sure you want to delete all the nodes from group ${fn:escapeXml(foreignSourceName)}? This CANNOT be undone."/>)" />
      </c:when>
      <c:otherwise>
        <input type="button" class="btn btn-default" value="Delete Requisition" onclick="doAction(<js:quote value="${foreignSourceName}"/>, 'deleteGroup')" />
      </c:otherwise>
    </c:choose>
    <c:if test="${!empty groups[foreignSourceName]}">
      <input type="button" class="btn btn-default" value="Synchronize" onclick="doAction(<js:quote value="${foreignSourceName}"/>, 'import')" />
    </c:if>
  <br />
 
  <table class="table table-condensed table-borderless">
  	<tr>
  	  <td>
  	  	<label>Requisition:</label>
  	  	<h5>Defines nodes, interfaces, and services for synchronization.</h5>
  	  </td>
  	  <td>
        <button id="edit_req_anchor_${foreignSourceName}" class="btn btn-default" onclick="editRequisition(<js:quote value="${foreignSourceName}"/>)">Edit</button>
  	  	<br />
        <h5>
          <c:choose>
            <c:when test="${empty groups[foreignSourceName]}">
              <span data-requisitionedNodes="0" data-databaseNodes="0">
                0 nodes defined,
                0 nodes in database
              </span>
            </c:when>
            <c:otherwise>
              <span data-requisitionedNodes="${groups[foreignSourceName].nodeCount}" data-databaseNodes="${dbNodeCounts[foreignSourceName]}">
                ${groups[foreignSourceName].nodeCount} nodes defined,
                ${dbNodeCounts[foreignSourceName]} nodes in database
              </span>
            </c:otherwise>
          </c:choose>
          <br />
          Last Modified:
          <c:choose>
            <c:when test="${empty groups[foreignSourceName].dateStamp}">Never</c:when>
            <c:otherwise>${groups[foreignSourceName].dateStamp}</c:otherwise>
          </c:choose><br />
          Last Synchronization Requested:
          <c:choose>
            <c:when test="${empty groups[foreignSourceName].lastImport}">Never</c:when>
            <c:otherwise>${groups[foreignSourceName].lastImport}</c:otherwise>
          </c:choose>
        </h5>
  	  </td>
  	</tr>
  	<tr>
  	  <td>
  	    <label>Foreign Source Definition:</label>
  	    <h5>Defines scanning and policy behavior for synchronization.</h5>
  	  </td>
  	  <td>
  	    <c:choose>
  	  	<c:when test="${!empty foreignSources[foreignSourceName]}">
        <div class="btn-group" role="group"><button class="btn btn-default" id="edit_fs_anchor_${foreignSourceName}" onclick="editForeignSource(<js:quote value="${foreignSourceName}"/>)">Edit</button><button class="btn btn-default" onclick="cloneForeignSource(<js:quote value="${foreignSourceName}"/>)">Clone</button></div>
  	  	<br />
          <h5>
            Last Modified:
  	        <c:choose>
  	          <c:when test="${empty foreignSources[foreignSourceName].dateStamp}">Never (default foreign source definition is in use)</c:when>
  	          <c:otherwise>${foreignSources[foreignSourceName].dateStampAsDate}</c:otherwise>
  	        </c:choose>
  	      </h5>
  	    </c:when>
  	    <c:otherwise>
        <button id="edit_fs_anchor_${foreignSourceName}" class="btn btn-default" onclick="editForeignSource(<js:quote value="${foreignSourceName}"/>)">Fork and Edit</button>
  	  	<br />
          <h5>
            The default foreign-source definition is currently in effect for this requisition.
  	      </h5>
  	    </c:otherwise>
  	    </c:choose>
  	  </td>
  	</tr>
  </table>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->
</span>
</c:if>
</c:forEach>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
