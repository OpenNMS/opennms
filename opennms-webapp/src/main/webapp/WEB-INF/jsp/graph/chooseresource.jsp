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

<%@page language="java"
	contentType="text/html"
	session="true"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Choose Resource" />
  <jsp:param name="headTitle" value="Choose" />
  <jsp:param name="headTitle" value="Resource Graphs" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='graph/index.jsp'>Resource Graphs</a>" />
  <jsp:param name="breadcrumb" value="Choose" />
</jsp:include>

<c:set var="totalRecords" value="0"/>
<c:set var="showFootnote1" value="false"/>
<c:forEach var="resourceType" items="${model.resourceTypes}">
   <c:set var="totalRecords" value="${fn:length(resourceType.value) + totalRecords}"/>
</c:forEach>
    
<script type="text/javascript"> 
  var data = {total:"${totalRecords}", records:[
	<c:set var="first" value="true"/>
	<c:forEach var="resourceType" items="${model.resourceTypes}">
       <c:forEach var="resource" items="${resourceType.value}" >
            <c:if test="${fn:contains(resource.label,'(*)')}">
               <c:set var="showFootnote1" value="true"/>
            </c:if>
	   		<c:choose>
	   			<c:when test="${first == true}">
	   			<c:set var="first" value="false"/>
					{ id: "${resource.id}", value:"${resource.label}", type: "${resourceType.key.label}" }
				</c:when>
				<c:otherwise>
					,{ id: "${resource.id}", value:"${resource.label}", type: "${resourceType.key.label}" }
				</c:otherwise>
	   		</c:choose>
        </c:forEach>
    </c:forEach>
  		]};

	
</script>
  <script type="text/javascript" >
	  function removeGraphStringIfIE(modelUrl){
		  if(Ext.isIE){
			  if(modelUrl.substring(0,6) == "graph/"){
					return modelUrl.substring(6, modelUrl.length);
			  }else{
				 return modelUrl;
			  }
		  }else{
			return modelUrl;
		  }
		
	  }
  	  
      function isSomethingSelected(node) {
         return recursiveIsSomethingSelected(node, 5);
      }
      
      function recursiveIsSomethingSelected(node, depth) {
         if (node.nodeName && (node.nodeName.toUpperCase() == "SELECT") && node.selectedIndex != -1) {
               return true;
         }
          for (var i = 0; i < node.length; i++) {
              if (node[i].nodeName.toUpperCase() == "SELECT") {
                  if (node[i].selectedIndex != -1) {
                      return true;
                  } else {
                      continue;
                  }
              } else {
                  if (depth == 0) {
                      alert("Max depth encountered while checking to see if something is selected.  Please report this as a bug.");
                  } else if (recursiveIsSomethingSelected(node[i], depth - 1)) {
                      return true;
                  }
              }
          }
          
          return false;
      }
  
      function submitForm(selectNode, itemName) {
          if (isSomethingSelected(selectNode)) {
              return true;
          } else {
              alert("Please select at least one " + itemName + ".");
              return false;
          }
      }
      
      function selectAll(name, selected) {
          recursiveSelectAll(document.getElementsByName(name), selected, 5);
      }
      
      function recursiveSelectAll(node, selected, depth) {
          for (var i = 0; i < node.length; i++) {
              if (node[i].nodeName.toUpperCase() == "SELECT") {
                  for (var j = 0; j < node[i].options.length; j++) {
                      node[i].options[j].selected = selected;
                  }
              } else {
                  if (depth == 0) {
                      alert("Max depth encountered while setting item selection to " + selected + ".  Please report this as a bug.");
                  } else {
                      recursiveSelectAll(node[i], selected, depth - 1);
                  }
              }
          }
      }
      
      function selectIfOnlyOneResource(name) {
          return recursiveSelectIfOnlyOneResource(document.getElementsByName(name), 5);
      }

      function recursiveSelectIfOnlyOneResource(node, depth) {
          if (depth == 0) {
              alert("Max depth encountered while checking resources.  Please report this as a bug.");
              return false;
          } else if (node.length == null) {
              node.selected = true;
              return true;
          } else if (node.length != 1) {
              return false;
          } else {
              return recursiveSelectIfOnlyOneResource(node[0], depth - 1);
          }
      }
  </script>
  <h4><strong>${model.resource.resourceType.label}:</strong> <a href="<c:url value='${model.resource.link}'/>"><c:out value="${model.resource.label}"/></a></h4>
  <c:choose>
  <c:when test="${empty model.resourceTypes}">
      <p>
      No resources are available to graph.
      </p>
  </c:when>
  
  <c:otherwise>
  <div class="panel panel-default">
    <div class="panel-heading">
      <h3 class="panel-title">Node Resources</h3>
    </div>
    <div class="panel-body">
        <opennms:reportSelectionList id="choose-resource" dataObject="data" targetUrl="${model.endUrl}"></opennms:reportSelectionList>
        <!-- for IE -->
        <div name="opennms-reportSelectionList" id="choose-resource-ie" dataObject="data" targetUrl="${model.endUrl}"></div>
    </div> <!-- panel-body -->
  </div> <!-- panel -->
      <%--<h3 class="o-box">
        Choose resources to query
      </h3>

      <p>
        Please choose one or more resources that you wish to query.
      </p>

      <form method="get" name="report" action="${model.endUrl}" onsubmit="return submitForm(document.report.resourceId, 'resource');">
        <%=Util.makeHiddenTags(request, new String[] { "parentResourceId", "parentResourceType", "parentResource", "endUrl" })%>
  
        <c:set var="num" value="0"/>
        <c:forEach var="resourceType" items="${model.resourceTypes}">
          <h3 class="o-box">${resourceType.key.label}</h3>

          <c:choose>
            <c:when test="${fn:length(resourceType.value) < 10}">
              <c:set var="selectSize" value="${resource.count}"/>
            </c:when>
            
            <c:otherwise>
              <c:set var="selectSize" value="10"/>
            </c:otherwise>
          </c:choose>
        
          <select name="resourceId" id="resource-select-${num}" size="${selectSize}" multiple>
            <c:forEach var="resource" items="${resourceType.value}">
              <option value="${resource.id}">
                ${resource.label}
              </option>
            </c:forEach>
          </select>
        
          <c:set var="num" value="${num + 1}"/>
        </c:forEach>
      
        <br/>
        <br/>
        <input type="submit" value="Submit" />
        <input type="button" value="Select All" onclick="selectAll('resourceId', true)" />
        <input type="button" value="Unselect All" onclick="selectAll('resourceId', false)" />
      </form>
      
      <script type="text/javascript">
          selectIfOnlyOneResource("resourceId");
      </script>--%>
      
  </c:otherwise>
  </c:choose>
  <c:if test="${showFootnote1 == true}">
      <jsp:include page="/includes/footnote1.jsp" flush="false" />
  </c:if>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
