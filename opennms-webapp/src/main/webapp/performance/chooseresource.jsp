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
	import="java.util.*,
		org.opennms.web.*,
		org.opennms.web.performance.*,
		org.opennms.netmgt.config.DataCollectionConfigFactory,
		org.opennms.web.element.NetworkElementFactory
	"
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<%!
    public PerformanceModel model = null;
    
    public void init() throws ServletException {
        try {
            DataCollectionConfigFactory.init();
        } catch (Throwable t) {
            throw new ServletException("Could not initialize the DataCollectionConfigFactory", t);
        }
        try {
            this.model = new PerformanceModel(ServletInitializer.getHomeDir());
        } catch (Throwable t) {
            throw new ServletException("Could not initialize the PerformanceModel", t);
        }
    } 
%>

<%
    String[] requiredParameters = new String[] { "node or domain", "endUrl" };

    String nodeId = request.getParameter("node");
    String domain = request.getParameter("domain");
    String endUrl = request.getParameter("endUrl");
    
    if (request.getParameter("endUrl") == null) {
        throw new MissingParameterException("endUrl", requiredParameters);
    }
    
    if (nodeId != null) {
		request.setAttribute("resources",
		                     this.model.getResourceForNode(Integer.parseInt(nodeId)));
		request.setAttribute("topLevelResourceLabel", NetworkElementFactory.getNodeLabel(Integer.parseInt(nodeId)));
		request.setAttribute("topLevelResourceLink", "element/node.jsp?node=" + nodeId);
		request.setAttribute("topLevelResourceType", "node");
		request.setAttribute("topLevelResourceTypeLabel", "Node");
    } else if (domain != null) {
		request.setAttribute("resources",
	    	                 this.model.getResourceForDomain(domain));
		request.setAttribute("topLevelResourceLabel", domain);
		request.setAttribute("topLevelResourceLink", "performance/chooseresource.jsp?domain=" + domain + "&relativetime=lastday&endUrl=performance%2FaddReportsToUrl");
		request.setAttribute("topLevelResourceType", "domain");
		request.setAttribute("topLevelResourceTypeLabel", "Domain");
    } else {
        throw new MissingParameterException("node or domain", requiredParameters);
    }
%>


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Choose Resource" />
  <jsp:param name="headTitle" value="Choose Resource" />
  <jsp:param name="headTitle" value="Performance" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='performance/index.jsp'>Performance</a>" />
  <jsp:param name="breadcrumb" value="Choose Resource" />
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

  <c:choose>
	<c:when test="${empty resources}">
	  No resources are available to graph for this <c:out value="${elementType}"/>
	</c:when>
	
	<c:otherwise>
	  <h2>
        <c:out value="${topLevelResourceTypeLabel}"/>: <a href="<c:out value="${topLevelResourceLink}"/>"><c:out value="${topLevelResourceLabel}"/></a>
	  </h2>
	  
      <h3>
        Choose a Resource to Query
      </h3>

      <p>
        The <c:out value="${topLevelResourceType}"/>
        that you have chosen has performance data for multiple resources.
        Please choose the resource that you wish to query.
      </p>
  
      <c:forEach var="resourceType" items="${resources}">
      
        <h3><c:out value='${resourceType.key.label}'/></h3>
      
        <form method="GET" name="report" action="<c:out value="${param.endUrl}"/>">
          <%=Util.makeHiddenTags(request, new String[] {"endUrl"})%>
          <input type="hidden" name="resourceType" value="<c:out value="${resourceType.key.name}"/>"/>

          <%
            Map.Entry entry = (Map.Entry) pageContext.getAttribute("resourceType");
            pageContext.setAttribute("resourceSize", ((Collection)entry.getValue()).size());
          %>
          
          <c:choose>
            <c:when test="${resourceSize < 10}">
              <c:set var="selectSize" value="${resource.count}"/>
            </c:when>
            
            <c:otherwise>
              <c:set var="selectSize" value="10"/>
            </c:otherwise>
          </c:choose>
          
          <c:choose>
            <c:when test="${resourceSize == 1}">
              <input type="hidden" name="resource" value="<c:out value="${resourceType.value[0].name}"/>"/>
              <c:out value="${resourceType.value[0].label}"/>
            </c:when>
            
            <c:otherwise>
              <select name="resource" size="<c:out value="${selectSize}"/>">
                <c:forEach var="resource" items="${resourceType.value}">
                  <option value="<c:out value="${resource.name}"/>">
                    <c:out value="${resource.label}"/>
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
