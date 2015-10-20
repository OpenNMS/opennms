<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
	import="
        java.util.LinkedHashMap,
        java.util.Map,
        org.opennms.web.servlet.MissingParameterException,
        org.opennms.web.api.Util,
        org.opennms.netmgt.model.OnmsResource,
        org.opennms.web.svclayer.api.ResourceService,
        org.springframework.web.context.WebApplicationContext,
        org.springframework.web.context.support.WebApplicationContextUtils,
        org.opennms.web.servlet.XssRequestWrapper"
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

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
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
    <c:set var="showFootnote1" value="false"/>
    
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
        <c:choose>
          <c:when test="${fn:contains(resource.label,'(*)')}">
            <c:set var="showFootnote1" value="true"/>
            Resource:
          </c:when>
          <c:otherwise>
            ${resource.resourceType.label}:
          </c:otherwise>
        </c:choose>
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

      <table width="100%">
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

              <select class="multi-select" name="ds" size="6">
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
              <table width="100%">
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

<c:if test="${showFootnote1 == true}">
  <jsp:include page="/includes/footnote1.jsp" flush="false" />
</c:if>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
