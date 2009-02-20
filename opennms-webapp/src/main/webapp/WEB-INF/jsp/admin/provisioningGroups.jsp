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
</jsp:include>

<h3>Provisioning Groups</h3>

<script language="Javascript" type="text/javascript" >
	function takeAction(group, action) {
	    document.takeAction.groupName.value = group;
	    document.takeAction.action.value = action;
		document.takeAction.submit();
	}
	
   function edit(group) {
	    document.edit.groupName.value = group;
		document.edit.submit();
	}
	
	function deleteNodes(group) {
	   alert("Are you sure you want to delete all the nodes from the group?  This CANNOT be undone.");
	}
	
</script>



<c:url var="editUrl" value="admin/editProvisioningGroup.htm" />

<form action="${editUrl}" name="edit" method="post">
  <input name="groupName" type="hidden"/>
</form>

<table style="width: auto">

<tr>
  <th>Delete</th>
  <th>Import</th>
  <th>Group Name</th>
  <th>Nodes in Group/Nodes in DB</th>
  <th>Last Import Request</th>
  <th>Last Changed</th>
</tr>

<c:forEach var="group" items="${groups}">
<tr>
  <td>
    <c:choose>
      <c:when test="${dbNodeCounts[group.foreignSource] > 0}">
        <a href="javascript:takeAction('${group.foreignSource}', 'deleteNodes')" onclick="return confirm('Are you sure you want to delete all the nodes from group ${group.foreignSource}. This CANNOT be undone.')">Delete Nodes</a>
      </c:when>
      <c:otherwise>
        <a href="javascript:takeAction('${group.foreignSource}', 'deleteGroup')">Delete Group</a>
      </c:otherwise>
    </c:choose>
  </td> 
  
  
  <td><a href="javascript:takeAction('${group.foreignSource}', 'import')">Import</a></td>
  <td><a href="javascript:edit('${group.foreignSource}')">${group.foreignSource}</a></td>
  <td>${group.nodeCount}/${dbNodeCounts[group.foreignSource]}</td>
  <td>${group.lastImport}</td>
  <td>${group.dateStamp}</td>
</tr>
</c:forEach>
<tr>
   <td></td>
   <td colspan="7"><form action="${relativeRequestPath}" name="takeAction" method="post"><input type="text" name="groupName" size="20"/><input type="hidden" name="action" value="addGroup" /><input type="submit" value="Add New Group"/></form></td>
</tr>
</table>

<jsp:include page="/includes/footer.jsp" flush="false"/>
