<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

<%@page language="java"
    contentType="text/html"
    session="true"
    import="
    org.opennms.web.controller.ksc.FormProcViewController
"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<% final String baseHref = org.opennms.web.api.Util.calculateUrlBase( request ); %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="headTitle" value="Performance" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="headTitle" value="KSC" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='KSC/index.htm'>KSC Reports</a>" />
  <jsp:param name="breadcrumb" value="Custom View" />
</jsp:include>


<%-- A script to Save the file --%>
<script type="text/javascript">
    function customizeReport()
    {
        document.view_form.action.value = "<c:out value="<%=FormProcViewController.Actions.Customize.toString()%>"/>"; 
        document.view_form.submit();
    }
 
    function updateReport()
    {
        document.view_form.action.value = "<c:out value="<%=FormProcViewController.Actions.Update.toString()%>"/>"; 
        document.view_form.submit();
    }
 
    function exitReport()
    {
        document.view_form.action.value = "<c:out value="<%=FormProcViewController.Actions.Exit.toString()%>"/>"; 
        document.view_form.submit();
    }
</script>


<c:choose>
  <c:when test="${fn:length(resultSets) <= 0}">
    <h3>No graphs defined</h3>
    <div class="boxWrapper">
      <p>There are no graphs defined for this report.</p>
    </div>
  </c:when>

  <c:otherwise>
    <h3>Custom View: ${title}</h3>
    <div class="boxWrapper">
    <form name="view_form" method="get" action="<%= baseHref %>KSC/formProcView.htm">
      <input type="hidden" name="<%=FormProcViewController.Parameters.type%>" value="${reportType}" >
      <input type="hidden" name="<%=FormProcViewController.Parameters.action%>" value="none">
      <c:if test="${!empty report}">
        <input type="hidden" name="<%=FormProcViewController.Parameters.report%>" value="${report}">
      </c:if>

            <table class="normal" align="center">
              <c:set var="graphNum" value="0"/>
              <c:set var="showFootnote1" value="false"/>

              <%-- Loop over each row in the table --%>
              <c:forEach begin="0" end="${(fn:length(resultSets) / graphsPerLine)}">
                <tr>
                  <%-- Then loop over each column in the row --%>
                  <c:forEach begin="1" end="${graphsPerLine}">
                    <%-- Since a row might not be full, check to see if we've run out of graphs --%>
                    <c:if test="${graphNum < fn:length(resultSets)}">
                      <c:set var="resultSet" value="${resultSets[graphNum]}"/>
                      
                      <td align="center">
                        <table>
                          <tr>
                            <th>
                              ${resultSet.title} <br/>
                              From: ${resultSet.start} <br/>
                              To: ${resultSet.end}
                            </th>
                            
                            <th>
                              <c:if test="${!empty resultSet.resource.parent}">
                                ${resultSet.resource.parent.resourceType.label}:
                                <c:choose>
                                  <c:when test="${(!empty resultSet.resource.parent.link) && loggedIn}">
                                    <a href="<c:url value='${resultSet.resource.parent.link}'/>">${resultSet.resource.parent.label}</a>
                                  </c:when>
                                  <c:otherwise>
                                    ${resultSet.resource.parent.label}
                                  </c:otherwise>
                                </c:choose>
                                <br />
                              </c:if>
                          
                              <c:choose>
                                <c:when test="${fn:contains(resultSet.resource.label,'(*)')}">
                                  <c:set var="showFootnote1" value="true"/>
                                  Resource:
                                </c:when>
                                <c:otherwise>
                                  ${resultSet.resource.resourceType.label}:
                                </c:otherwise>
                              </c:choose>
                              <c:choose>
                                <c:when test="${(!empty resultSet.resource.link) && loggedIn}">
                                  <a href="<c:url value='${resultSet.resource.link}'/>">${resultSet.resource.label}</a>
                                </c:when>
                                <c:otherwise>
                                  ${resultSet.resource.label}
                                </c:otherwise>
                              </c:choose>
                              
                              <c:url var="detailUrl" value="${baseHref}graph/results.htm">
                                <c:param name="resourceId" value="${resultSet.resource.id}"/>
                                <c:param name="reports" value="all"/>
                                <c:param name="start" value="${resultSet.start.time}"/>
                                <c:param name="end" value="${resultSet.end.time}"/>
                              </c:url>
  
                              <a href="${detailUrl}">Detail</a>                            
                            </th>
                          </tr>
                        </table>
  
                        <c:url var="zoomUrl" value="${baseHref}graph/results.htm">
                          <c:param name="resourceId" value="${resultSet.resource.id}"/>
                          <c:param name="reports" value="${resultSet.prefabGraph.name}"/>
                          <c:param name="start" value="${resultSet.start.time}"/>
                          <c:param name="end" value="${resultSet.end.time}"/>
                          <c:param name="zoom" value="true"/>
                        </c:url>
  
                        <c:url var="graphUrl" value="${baseHref}graph/graph.png">
                          <c:param name="resourceId" value="${resultSet.resource.id}"/>
                          <c:param name="report" value="${resultSet.prefabGraph.name}"/>
                          <c:param name="start" value="${resultSet.start.time}"/>
                          <c:param name="end" value="${resultSet.end.time}"/>
                          <c:param name="zoom" value="true"/>
                        </c:url>
                        
                        <a href="${zoomUrl}">
                          <img src="${graphUrl}" alt="Resource graph: ${resultSet.prefabGraph.title} (click to zoom)"/>
                        </a>
                        
                      </td>
                      
                      <c:set var="graphNum" value="${graphNum + 1}"/>
                    </c:if>
                  </c:forEach>
                </tr>
              </c:forEach>
            </table>  


            <table class="normal">
              <!-- Select Timespan Input --> 
              <c:if test="${!empty timeSpan}">
                <tr>
                  <td class="normal">
                    Override Graph Timespan
                  </td>
                  <td class="normal">
                    <select name="timespan">
                      <c:forEach var="option" items="${timeSpans}">
                        <c:choose>
                          <c:when test="${timeSpan == option.key}">
                            <c:set var="selected">selected="selected"</c:set>
                          </c:when>
                          
                          <c:otherwise>
                            <c:set var="selected" value=""/>
                          </c:otherwise>
                        </c:choose>
                        <option value="${option.key}" ${selected}>${option.value}</option>
                      </c:forEach>
                    </select>  
                    
                    (Press update button to reflect option changes to ALL graphs) 
                  </td>
                </tr>
              </c:if>
  
              <!-- Select Graph Type --> 
              <c:if test="${!empty graphType}">
                <tr>
                  <td class="normal">
                    Override Graph Type
                  </td>
                  <td class="normal">
                    <select name="graphtype">
                      <c:forEach var="option" items="${graphTypes}">
                        <c:choose>
                          <c:when test="${graphType == option.key}">
                            <c:set var="selected">selected="selected"</c:set>
                          </c:when>
                          
                          <c:otherwise>
                            <c:set var="selected" value=""/>
                          </c:otherwise>
                        </c:choose>
                        <option value="${option.key}" ${selected}>${option.value}</option>
                      </c:forEach>
                    </select>  
                    
                    (Press update button to reflect option changes to ALL graphs) 
                  </td>
                </tr>
              </c:if>
            </table>

            <p>
              <input type="button" value="Exit Report Viewer" onclick="exitReport()">
              <c:if test="${!empty timeSpan || !empty graphType}">
                <input type="button" value="Update Report View" onclick="updateReport()">
              </c:if>
                
              <c:if test="${showCustomizeButton}">
                <input type="button" value="Customize This Report" onclick="customizeReport()">
              </c:if>
            <p>

    </form>
    </div>

  </c:otherwise>
</c:choose>

<c:if test="${showFootnote1 == true}">
  <jsp:include page="/includes/footnote1.jsp" flush="false" />
</c:if>

<jsp:include page="/includes/footer.jsp" flush="false"/>
