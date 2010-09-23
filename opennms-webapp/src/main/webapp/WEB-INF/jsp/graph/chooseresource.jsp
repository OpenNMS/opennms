<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Sep 25: Fix the domain topLevelResourceLink.
// 2006 Aug 24: List the node/domain. - dj@opennms.org
// 2006 Aug 08: Fix minor spelling error - dj@opennms.org
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com/
//

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.Util,
			org.opennms.web.element.*,
			java.util.*"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@page import="org.opennms.web.svclayer.ResourceService"%><jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Choose Resource" />
  <jsp:param name="headTitle" value="Choose" />
  <jsp:param name="headTitle" value="Resource Graphs" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='graph/index.jsp'>Resource Graphs</a>" />
  <jsp:param name="breadcrumb" value="Choose" />
  <jsp:param name="enableExtJS" value="true"/>
</jsp:include>

<c:set var="totalRecords" value="0"/>
<c:forEach var="resourceType" items="${model.resourceTypes}">
   <c:set var="totalRecords" value="${fn:length(resourceType.value) + totalRecords}"/>
</c:forEach>
    
<script type="text/javascript"> 
  var data = {total:"${totalRecords}", records:[
	<c:set var="first" value="true"/>
	<c:forEach var="resourceType" items="${model.resourceTypes}">
       <c:forEach var="resource" items="${resourceType.value}" >
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
	
  <script type="text/javascript" src="js/opennms/ux/PageableGrid.js" ></script>
  <script type="text/javascript" src="js/opennms/ux/DeleteBtnSelectionModel.js" ></script>
  <script type="text/javascript" src="js/opennms/ux/LocalPageableProxy.js" ></script>
  <script type="text/javascript" src="js/ChooseResourceView.js" ></script>
  <script type="text/javascript" >
  	  Ext.onReady(function(){
  		chooseResourceViewInit("resources-view", data, removeGraphStringIfIE("${model.endUrl}"));
  		
  	  });

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
  <div class="onms">
  <h2>
    ${model.resource.resourceType.label}: <a href="<c:url value='${model.resource.link}'/>">${model.resource.label}</a>
  </h2> 
  </div>
  <c:choose>
  <c:when test="${empty model.resourceTypes}">
      <p>
      No resources are available to graph.
      </p>
  </c:when>
  
  <c:otherwise>
  	<div id="resources-view"></div>
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
  
<jsp:include page="/includes/footer.jsp" flush="false" />
