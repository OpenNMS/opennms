<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

<%@ page language="java" contentType="text/html" session="true" import="
	org.opennms.web.controller.ksc.FormProcGraphController
"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<% final String baseHref = org.opennms.web.api.Util.calculateUrlBase( request ); %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="headTitle" value="Performance " />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="headTitle" value="KSC" />
  <jsp:param name="location" value="KSC Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='KSC/index.htm'>KSC Reports</a>" />
  <jsp:param name="breadcrumb" value="Custom Graph" />
</jsp:include>

<script type="text/javascript">
 
    function saveGraph()
    {
        document.customize_graph.action.value="Save";
        document.customize_graph.submit();
    }

    function chooseResource()
    {
        document.customize_graph.action.value="ChooseResource";
        document.customize_graph.submit();
    }
        
    function updateGraph()
    {
        document.customize_graph.action.value="Update";
        document.customize_graph.submit();
    }
   
    function cancelGraph()
    {
        if (confirm("Do you really want to cancel graph configuration changes?")) {
            document.customize_graph.action.value="Cancel";
            document.customize_graph.submit();
        }
    }
  
</script>

<h2>Customized Report Graph Definition</h2>

  <c:choose>
    <c:when test="${fn:length(prefabGraphs) == 0}">
      <h3>No graph options available</h3>
      <div class="boxWrapper">
        <p>
          No graph options are available.
          This resource might not have any data that can be graphed with
          prefabricated graphs.
          Try selecting another resource.
          You can also check that the correct data is being collected and
          that appropriate reports are defined.  
        </p>
      </div>
    </c:when>
                
    <c:otherwise>
      <h3>Sample graph</h3>
      <div class="boxWrapper">
                    <table class="normal">
                        <tr>
                            <td class="normal" align="right">
                                ${resultSet.title}
                                <br/>
                                  <c:if test="${!empty resultSet.resource.parent}">
                                    ${resultSet.resource.parent.resourceType.label}:
                                    <c:choose>
                                      <c:when test="${!empty resultSet.resource.parent.link}">
                                        <a href="<c:url value='${resultSet.resource.parent.link}'/>">${resultSet.resource.parent.label}</a>
                                      </c:when>
                                      <c:otherwise>
                                        ${resultSet.resource.parent.label}
                                      </c:otherwise>
                                    </c:choose>
                                    <br />
                                  </c:if>
                              
                                  ${resultSet.resource.resourceType.label}:
                                  <c:choose>
                                    <c:when test="${!empty resultSet.resource.link}">
                                      <a href="<c:url value='${resultSet.resource.link}'/>">${resultSet.resource.label}</a>
                                    </c:when>
                                    <c:otherwise>
                                      ${resultSet.resource.label}
                                    </c:otherwise>
                                  </c:choose>

                                <br/>
                                <b>From</b> ${resultSet.start}
                                <br/>
                                <b>To</b> ${resultSet.end}
                            </td>
              
                            <td class="normal" align="left">
                              <c:url var="graphUrl" value="${baseHref}graph/graph.png">
                                <c:param name="resourceId" value="${resultSet.resource.id}"/>
                                <c:param name="report" value="${resultSet.prefabGraph.name}"/>
                                <c:param name="start" value="${resultSet.start.time}"/>
                                <c:param name="end" value="${resultSet.end.time}"/>
                                <c:param name="zoom" value="true"/>
                              </c:url>
                              
                              <img src="${graphUrl}" alt="Resource graph: ${resultSet.prefabGraph.title}" />
                            </td>
                        </tr>
                    </table>
      </div>

      <h3>Choose graph options</h3>
      <div class="boxWrapper">

      <form name="customize_graph" method="get" action="<%= baseHref %>KSC/formProcGraph.htm" >
        <input type="hidden" name="<%=FormProcGraphController.Parameters.action%>" value="none" />

                    <table class="normal">
                        <tr>
                          <td class="normal">
                            Title
                          </td>
                          <td class="normal">
                            <input type="text" name="<%=FormProcGraphController.Parameters.title%>" value="${resultSet.title}" size="40" maxlength="40"/>
                          </td>
                        </tr>
                        <tr>
                            <td class="normal">
                              Timespan
                            </td>
                            <td class="normal">
                                <select name="<%=FormProcGraphController.Parameters.timespan%>">
                                  <c:forEach var="option" items="${timeSpans}">
                                    <c:choose>
                                      <c:when test="${timeSpan == option.key}">
                                        <c:set var="timespanSelected">selected="selected"</c:set>
                                      </c:when>
                                      
                                      <c:otherwise>
                                        <c:set var="timespanSelected" value=""/>
                                      </c:otherwise>
                                    </c:choose>
                                    <option value="${option.key}" ${timespanSelected}>${option.value}</option>
                                  </c:forEach>
                                </select>  
                                (This selects the relative start and stop times for the report) 
                            </td>
                        </tr>
                        <tr>
                            <td class="normal">
                                Prefabricated Report
                            </td>
                            <td class="normal">
                                <select name="<%=FormProcGraphController.Parameters.graphtype%>">
                                  <c:forEach var="prefabGraph" items="${prefabGraphs}">
                                    <c:choose>
                                      <c:when test="${resultSet.prefabGraph.name == prefabGraph.name}">
                                        <c:set var="prefabSelected">selected="selected"</c:set>
                                      </c:when>
                                      
                                      <c:otherwise>
                                        <c:set var="prefabSelected" value=""/>
                                      </c:otherwise>
                                    </c:choose>
                                    <option value="${prefabGraph.name}" ${prefabSelected}>${prefabGraph.name}</option>
                                  </c:forEach>
                                </select>  
                                (This selects the prefabricated graph report to use) 
                            </td>
                        </tr>
                        <tr>
                            <td class="normal">
                                <!-- Select Graph Index -->
                                Graph Index  
                            </td>
                            <td class="normal">
                                <select name="<%=FormProcGraphController.Parameters.graphindex%>">
                                  <c:forEach var="index" begin="1" end="${maxGraphIndex}">
                                    <c:choose>
                                      <c:when test="${index == (graphIndex + 1)}">
                                        <c:set var="indexSelected">selected="selected"</c:set>
                                      </c:when>
                                      
                                      <c:otherwise>
                                        <c:set var="indexSelected" value=""/>
                                      </c:otherwise>
                                    </c:choose>
                                    <option value="${index}" ${indexSelected}>${index}</option>
                                  </c:forEach>
                                </select>  
                                (This selects the desired position in the report for the graph to be inserted) 
                            </td>
                        </tr>
                    </table>

                    <input type="button" value="Cancel edits to this graph" onclick="cancelGraph()" alt="Cancel this graph configuration"/>
                    <input type="button" value="Refresh sample view" onclick="updateGraph()" alt="Update changes to sample graph"/>
                    <input type="button" value="Choose different resource" onclick="chooseResource()" alt="Choose a different resource to graph"/>
                    <input type="button" value="Done with edits to this graph" onclick="saveGraph()" alt="Done with this graph configuration"/>
      </form>
      </div>

    </c:otherwise>
  </c:choose>

<jsp:include page="/includes/footer.jsp" flush="false" />
