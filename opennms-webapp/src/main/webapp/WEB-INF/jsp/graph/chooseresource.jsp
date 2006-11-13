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
	import="org.opennms.web.Util"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Choose Resource" />
  <jsp:param name="headTitle" value="Choose" />
  <jsp:param name="headTitle" value="Resource Graphs" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='graph/index.jsp'>Resource Graphs</a>" />
  <jsp:param name="breadcrumb" value="Choose" />
</jsp:include>

  <script language="Javascript" type="text/javascript" >
      function validateRRD() {
          var isChecked = false
          for( i = 0; i < document.report.resource.length; i++ ) {
              // make sure something is checked before proceeding
              if (document.report.resource[i].selected) {
                  isChecked=true;
              }
          }
  
          if (!isChecked){
              alert("Please check the resources that you would like to report on.");
          }
          return isChecked;
      }
  
      function submitForm() {
          if (validateRRD()) {
              document.report.submit();
          }
      }
  </script>

  <h2>
    ${model.resourceTypeLabel}: <a href="<c:url value='${model.resourceLink}'/>">${model.resourceLabel}</a>
  </h2> 

  <c:choose>
	<c:when test="${empty model.resourceTypes}">
      <p>
	    No resources are available to graph for this ${model.resourceTypeLabel}
      </p>
	</c:when>
	
	<c:otherwise>
      <h3>
        Choose a Resource to Query
      </h3>

      <p>
        The ${model.resourceTypeLabel}
        that you have chosen has performance data for multiple resources.
        Please choose the resource that you wish to query.
      </p>
  
      <c:forEach var="resourceType" items="${model.resourceTypes}">
      
        <h3>${resourceType.key.label}</h3>
      
        <form method="GET" name="report" action="${model.endUrl}">
          <%=Util.makeHiddenTags(request, new String[] {"endUrl"})%>
          <input type="hidden" name="type" value="performance"/>
          <input type="hidden" name="resourceType" value="${resourceType.key.name}"/>

          <c:choose>
            <c:when test="${fn:length(resourceType.value) < 10}">
              <c:set var="selectSize" value="${resource.count}"/>
            </c:when>
            
            <c:otherwise>
              <c:set var="selectSize" value="10"/>
            </c:otherwise>
          </c:choose>
          
          <c:choose>
            <c:when test="${fn:length(resourceType.value) == 1}">
              <input type="hidden" name="resource" value="${resourceType.value[0].name}"/>
              ${resourceType.value[0].label}
            </c:when>
            
            <c:otherwise>
              <select name="resource" size="${selectSize}">
                <c:forEach var="resource" items="${resourceType.value}">
                  <option value="${resource.name}">
                    ${resource.label}
                  </option>
                </c:forEach>
            </select>
          </c:otherwise>
        </c:choose>
          
  	      <input type="submit" value="Submit"/>
<!--      <input type="button" value="Submit" onclick="submitForm()" /> -->
        </form>
        
      </c:forEach>
	</c:otherwise>
  </c:choose>
  
<jsp:include page="/includes/footer.jsp" flush="false" />
