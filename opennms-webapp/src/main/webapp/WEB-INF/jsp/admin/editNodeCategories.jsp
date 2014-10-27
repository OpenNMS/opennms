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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Category" />
	<jsp:param name="headTitle" value="Category" />
	<jsp:param name="breadcrumb"
               value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb"
	           value="<a href='admin/categories.htm'>Category</a>" />
	<jsp:param name="breadcrumb" value="Show" />
</jsp:include>

<script type="text/javascript">
function toggleFormEnablement() {
  [ "toAdd", "addButton", "removeButton", "toDelete" ].forEach( function(elemId) {
    var elem = document.getElementById(elemId);
    elem.disabled = (!elem.disabled);
  } );
}
</script>

<h3>Edit surveillance categories on ${model.node.label}</h3>

<p>
Node <a href="<c:url value='element/node.jsp?node=${model.node.id}'/>">${model.node.label}</a> (node ID: ${model.node.id}) has ${fn:length(model.node.categories)} categories 
</p>

<div class="TwoColLeft">
<form action="admin/categories.htm" method="get">
  <input type="hidden" name="node" value="${model.node.id}"/>
  <input type="hidden" name="edit" value=""/>
  
  <table class="normal">
    <tr>
      <td class="normal" align="center">
		Available categories
      </td>
      
      <td class="normal">  
      </td>

      <td class="normal" align="center">
      	Categories on node
      </td>
    </tr>
      
    <tr>
      <td class="normal">  
    <select id="toAdd" name="toAdd" size="20" multiple="true" <c:if test="${! empty model.node.foreignSource}">disabled="true"</c:if>>
	  <c:forEach items="${model.categories}" var="category">
	    <option value="${category.id}">${category.name}</option>
	  </c:forEach>
    </select>
      </td>
      
      <td class="normal" style="text-align:center; vertical-align:middle;">  
        <input id="addButton" type="submit" name="action" value="Add &#155;&#155;" <c:if test="${! empty model.node.foreignSource}">disabled="true"</c:if>/>
        <br/>
        <br/>
        <input id="removeButton" type="submit" name="action" value="&#139;&#139; Remove" <c:if test="${! empty model.node.foreignSource}">disabled="true"</c:if>/>
      </td>
    
      <td class="normal">  
    <select id="toDelete" name="toDelete" size="20" multiple="true" <c:if test="${! empty model.node.foreignSource}">disabled="true"</c:if>>
	  <c:forEach items="${model.sortedCategories}" var="category">
	    <option value="${category.id}">${category.name}</option>
	  </c:forEach>
    </select>
      </td>
    </tr>
    <tr>
      <td colspan="3">
      <input id="toggleCheckbox" type="checkbox" onchange="javascript:toggleFormEnablement()" />
      <label for="toggleCheckbox">Check this box to enable controls (see warning above for why)</label>
      </td>
    </tr>
  </table>
</form>
</div>

<div class="TwoColRight">
<c:if test="${!empty model.node.foreignSource}">
  <h3>Warning</h3>
  <div class="boxWrapper">
    <p>
    You are editing category memberships for a node that was provisioned
    through a requisition. Any edits made here will be rolled back the next
    time the requisition "<em>${model.node.foreignSource}</em>" is
    synchronized (typically every 24 hours) or the node manually rescanned.
    To make permanent changes, do one of the following:
    </p>
    <p>
      <strong>Edit the requisition</strong> from the web UI, if you know that
      this is how category assignments in this requisition are managed.
    </p>
    <p>
      <strong>Edit the appropriate foreign-source definition</strong> from the
      web UI, if you know that categories for this requisition's nodes are
      automatically assigned by a <em>Set Node Category</em> foreign-source policy.
    </p>
    <p>
      <strong>Ask your OpenNMS administrator</strong> if you aren't sure, or if
      you know that the requisition "<em>${model.node.foreignSource}</em>" is created
      from some data source outside OpenNMS. 
    </p>
  </div>
</c:if>
</div>

<jsp:include page="/includes/footer.jsp" flush="false"/>
