<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

<jsp:include page="/includes/header.jsp" flush="false">
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
<form action="${relativeRequestPath}" name="takeAction" method="post"><input type="text" name="groupName" size="20"/><input type="hidden" name="action" value="addGroup" /><input type="hidden" name="actionTarget" value="" />
<input type="submit" value="Add New Requisition"/>
<input type="button" value="Edit Default Foreign Source Definition" onclick="javascript:editForeignSource('default')" />
<input type="button" value="Reset Default Foreign Source Definition" onclick="javascript:resetDefaultForeignSource()" />
</form>

<c:forEach var="foreignSourceName" items="${foreignSourceNames}">
<c:if test='${foreignSourceName != "default"}'>
  <h3 style="vertical-align: middle; margin: 25px 0px 5px 0px; padding: 5px">
    <span style="font-size: large"><c:out value="${foreignSourceName}" /></span>
  </h3>
  <span style="font-size: large; text-align: right">
    <c:choose>
      <c:when test="${dbNodeCounts[foreignSourceName] > 0}">
        <input type="button" value="Delete Nodes" onclick="javascript:confirmAction(<js:quote value="${foreignSourceName}"/>, 'deleteNodes', <js:quote value="Are you sure you want to delete all the nodes from group ${fn:escapeXml(foreignSourceName)}? This CANNOT be undone."/>)" />
      </c:when>
      <c:otherwise>
        <input type="button" value="Delete Requisition" onclick="javascript:doAction(<js:quote value="${foreignSourceName}"/>, 'deleteGroup')" />
      </c:otherwise>
    </c:choose>
    <c:if test="${!empty groups[foreignSourceName]}">
      <input type="button" value="Synchronize" onclick="javascript:doAction(<js:quote value="${foreignSourceName}"/>, 'import')" />
    </c:if>
  </span>
  <br />
 
  <table class="top" border="0">
  	<tr>
  	  <td>
  	  	Requisition:<br />
  	  	<span style="font-size: smaller">Define node, interface, and service data for synchronization.</span>
  	  </td>
  	  <td>
  	  	<a href="javascript:editRequisition(<js:quote value="${foreignSourceName}"/>)">Edit</a>
  	  	<br />
        <span style="font-size: smaller">
          <c:choose>
            <c:when test="${empty groups[foreignSourceName]}">
              0 nodes defined, 0 nodes in database
            </c:when>
            <c:otherwise>
              ${groups[foreignSourceName].nodeCount} nodes defined,
              ${dbNodeCounts[foreignSourceName]} nodes in database
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
        </span>
  	  </td>
  	</tr>
  	<tr>
  	  <td>
  	    Foreign Source Definition:<br />
  	    <span style="font-size: smaller">Define scanning behavior for synchronization.</span>
  	  </td>
  	  <td>
  	  	<a href="javascript:editForeignSource(<js:quote value="${foreignSourceName}"/>)">Edit</a> |
  	  	<a href="javascript:cloneForeignSource(<js:quote value="${foreignSourceName}"/>)">Clone</a>
  	  	<br />
  	  	<c:if test="${!empty foreignSources[foreignSourceName]}">
          <span style="font-size: smaller">
            Last Modified:
  	        <c:choose>
  	          <c:when test="${empty foreignSources[foreignSourceName].dateStamp}">Never</c:when>
  	          <c:otherwise>${foreignSources[foreignSourceName].dateStampAsDate}</c:otherwise>
  	        </c:choose>
  	      </span>
  	    </c:if>
  	  </td>
  	</tr>
  </table>
</c:if>
</c:forEach>

<jsp:include page="/includes/footer.jsp" flush="false"/>
