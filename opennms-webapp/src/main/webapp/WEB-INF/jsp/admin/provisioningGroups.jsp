<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib tagdir="/WEB-INF/tags/tree" prefix="tree" %>
<%@ taglib tagdir="/WEB-INF/tags/springx" prefix="springx" %>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Provisioning Groups" /> 
	<jsp:param name="headTitle" value="Provisioning Groups" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/provisioningGroups.htm'>Provisioning Groups</a>" />
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
<form action="${relativeRequestPath}" name="takeAction" method="post"><input type="text" name="groupName" size="20"/><input type="hidden" name="action" value="addGroup" /><input type="submit" value="Add New Group"/></form>

<c:forEach var="foreignSourceName" items="${foreignSourceNames}">
  <h3 style="vertical-align: middle; margin: 25px 0px 5px 0px; padding: 5px">
    <span style="font-size: large"><c:out value="${foreignSourceName}" />
  </h3>
  <span style="font-size: large; text-align: right">
    <c:choose>
      <c:when test="${dbNodeCounts[group.foreignSource] > 0}">
        <input type="button" value="Delete Nodes" onclick="javascript:confirmAction('${groups[foreignSourceName].foreignSource}', 'deleteNodes', 'Are you sure you want to delete all the nodes from group ${group.foreignSource}. This CANNOT be undone.')" />
      </c:when>
      <c:otherwise>
        <input type="button" value="Delete Group" onclick="javascript:doAction('${group.foreignSource}', 'deleteGroup')" />
      </c:otherwise>
    </c:choose>
    <c:if test="${!empty groups[foreignSourceName]}">
      <input type="button" value="Import" onclick="javascript:doAction('${groups[foreignSourceName].foreignSource}', 'import')" />
    </c:if>
  </span>
  <br />
 
  <table class="top" border="0">
  	<tr>
  	  <td>
  	  	Requisition (Provisioning Group):<br />
  	  	<span style="font-size: smaller">Define node and interface data for import.</span>
  	  </td>
  	  <td>
  	  	<a href="javascript:editRequisition('${foreignSourceName}')">EDIT</a><br />
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
          last modified:
          <c:choose>
            <c:when test="${empty groups[foreignSourceName].dateStamp}">never</c:when>
            <c:otherwise>${groups[foreignSourceName].dateStamp}</c:otherwise>
          </c:choose><br />
          last import requested:
          <c:choose>
            <c:when test="${empty groups[foreignSourceName].lastImport}">never</c:when>
            <c:otherwise>${groups[foreignSourceName].lastImport}</c:otherwise>
          </c:choose>
        </span>
  	  </td>
  	</tr>
  	<tr>
  	  <td>
  	    Foreign Source:<br />
  	    <span style="font-size: smaller">Define scanning behavior for import.</span>
  	  </td>
  	  <td>
  	  	<a href="javascript:editForeignSource('${foreignSourceName}')">EDIT</a><br />
  	  	<c:if test="${!empty foreignSources[foreignSourceName]}">
          <span style="font-size: smaller">
            last modified:
  	        <c:choose>
  	          <c:when test="${empty foreignSources[foreignSourceName].dateStamp}">never</c:when>
  	          <c:otherwise>${foreignSources[foreignSourceName].dateStampAsDate}</c:otherwise>
  	        </c:choose>
  	      </span>
  	    </c:if>
  	  </td>
  	</tr>
  </table>

</c:forEach>

<jsp:include page="/includes/footer.jsp" flush="false"/>
