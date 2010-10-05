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
// 2008 Oct 22: Use new ResourceDao method names. - dj@opennms.org
// 2008 Sep 28: Handle XSS security issues.
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
	import="
        java.util.LinkedHashMap,
        java.util.Map,
        org.opennms.web.MissingParameterException,
        org.opennms.web.Util,
        org.opennms.netmgt.model.OnmsResource,
        org.opennms.web.svclayer.ResourceService,
        org.springframework.web.context.WebApplicationContext,
        org.springframework.web.context.support.WebApplicationContextUtils,
        org.opennms.web.XssRequestWrapper"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%!
    public ResourceService m_resourceService;

    public static final Map<Integer, String[]> s_colors = new LinkedHashMap<Integer, String[]>();
    
    static {
        s_colors.put(0, new String[] { "Red", "ff0000" });
        s_colors.put(1, new String[] { "Green", "00ff00" });
        s_colors.put(2, new String[] { "Blue", "ff00ff" });
        s_colors.put(3, new String[] { "Black", "000000" });
    }
    
    public void init() throws ServletException {
	    WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        m_resourceService = (ResourceService) webAppContext.getBean("resourceService", ResourceService.class);
    }%>
 
<%
    String[] requiredParameters = new String[] {
        "resourceId"
    };

    HttpServletRequest req = new XssRequestWrapper(request);
    for (String requiredParameter : requiredParameters) {
        if (req.getParameter(requiredParameter) == null) {
            throw new MissingParameterException(requiredParameter,
                                                requiredParameters);
        }
    }

    if (req.getParameterValues("resourceId").length > 1) {
        pageContext.setAttribute("tooManyResourceIds", "true");
    } else {
        String resourceId = req.getParameter("resourceId");
        OnmsResource resource = m_resourceService.getResourceById(resourceId);
        m_resourceService.promoteGraphAttributesForResource(resource);
        pageContext.setAttribute("resource", resource);
        pageContext.setAttribute("colors", s_colors);
    }
    
    

%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Custom Resource Graphs" />
  <jsp:param name="headTitle" value="Custom" />
  <jsp:param name="headTitle" value="Resource Graphs" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='graph/index.jsp'>Resource Graphs</a>" />
  <jsp:param name="breadcrumb" value="Custom" />
</jsp:include>


<c:choose>
  <c:when test="${tooManyResourceIds}">
    <h2>Error</h2>
    
    <h3>Only one resource supported</h3>

    <p>
      At this time, only one resource at a time is supported in custom resource
      graphs.  Please go back and choose a single resource.
    </p>
  </c:when>
  
  <c:otherwise>
    <h2>Step 2: Choose Data Sources</h2>

    <h3>
      ${resource.parent.resourceType.label}:
      <c:choose>
        <c:when test="${!empty resource.parent.link}">
          <a href="<c:url value='${resource.parent.link}'/>">${resource.parent.label}</a>
        </c:when>
        <c:otherwise>
          ${resource.parent.label}
        </c:otherwise>
      </c:choose>
  
      <c:if test="${!empty resource}">
        <br />
        ${resource.resourceType.label}:
        <c:choose>
          <c:when test="${!empty resource.link}">
            <a href="<c:url value='${resource.link}'/>">${resource.label}</a>
          </c:when>
          <c:otherwise>
            ${resource.label}
          </c:otherwise>
        </c:choose>
      </c:if>
    </h3>
    

    <form method="get" action="graph/adhoc3.jsp" >
      <%=Util.makeHiddenTags(request)%>

      <table width="100%" cellspacing="2" cellpadding="2" border="0">
        <c:set var="anythingSelected" value="false"/>
        <c:forEach var="dsIndex" begin="0" end="3">
          <!-- Data Source ${dsIndex} -->     
          <tr>
            <td valign="top">
              Data Source ${dsIndex + 1}
              <c:choose>
                <c:when test="${dsIndex == 0}">
                  (required):
                </c:when>
                <c:otherwise>
                  (optional):
                </c:otherwise>
              </c:choose>
              
              <br/>

              <select name="ds" size="6">
                <c:forEach var="attribute" items="${resource.attributes}">
                  <c:choose>
                    <c:when test="${! anythingSelected}">
                      <c:set var="selected">selected="selected"</c:set>
                      <c:set var="anythingSelected" value="true"/>
                    </c:when>
                    <c:otherwise>
                      <c:set var="selected" value=""/>
                    </c:otherwise>
                  </c:choose>
                  <option ${selected}>
                    ${attribute.name}
                  </option>
                </c:forEach>
              </select>
            </td>

            <td valign="top">
              <table width="100%" cellspacing="0" cellpadding="2">
                <tr>
                  <td width="5%">Title:</td>
                  <td><input type="input" name="dstitle" value="Data Source ${dsIndex + 1}" /></td>
                </tr>

                <tr>
                  <td width="5%">Color:</td> 
                  <td> 
                    <select name="color">
                      <c:forEach var="color" items="${colors}">
                        <c:choose>
                          <c:when test="${(dsIndex % fn:length(colors)) == color.key}">
                            <c:set var="selected">selected="selected"</c:set>
                          </c:when>
                          <c:otherwise>
                            <c:set var="selected" value=""/>
                          </c:otherwise>
                        </c:choose>
                        
                        <option value="${color.value[1]}" ${selected}>${color.value[0]}</option>
                      </c:forEach>
                    </select>
                  </td>
                </tr>

                <tr>
                  <td width="5%">Style:</td> 
                  <td> 
                    <select name="style">
                      <option value="LINE1">Thin Line</option>
                      <option value="LINE2" selected="selected">Medium Line</option>
                      <option value="LINE3">Thick Line</option>
                      <option value="AREA">Area</option>
                      <c:if test="${dsIndex != 0}">
                        <option value="STACK">Stack</option>
                      </c:if>
                    </select>
                  </td>
                </tr>

                <tr>
                  <td width="5%">Value&nbsp;Type:</td> 
                  <td> 
                    <select name="agfunction">
                      <option value="AVERAGE" selected="selected">Average</option>
                      <option value="MIN">Minimum</option>
                      <option value="MAX">Maximum</option>
                    </select>
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <tr><td colspan="2"><hr></td></tr>
        </c:forEach>
      </table> 

      <input type="submit" value="Next"/>
      <input type="reset" />
    </form>
  </c:otherwise>
</c:choose>

<jsp:include page="/includes/footer.jsp" flush="false" />
