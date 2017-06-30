<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="headTitle" value="Performance " />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="headTitle" value="KSC" />
  <jsp:param name="location" value="KSC Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='KSC/index.jsp'>KSC Reports</a>" />
  <jsp:param name="breadcrumb" value="Custom Graph" />
  <jsp:param name="renderGraphs" value="true" />
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

<h4>Customized Report Graph Definition</h4>

<c:choose>
  <c:when test="${fn:length(prefabGraphs) == 0}">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">No graph options available</h3>
      </div>
      <div class="panel-body">
        <p>
          No graph options are available.
          This resource might not have any data that can be graphed with
          prefabricated graphs.
          Try selecting another resource.
          You can also check that the correct data is being collected and
          that appropriate reports are defined.  
        </p>
      </div>
    </div>
  </c:when>
  <c:otherwise>
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Sample graph</h3>
      </div>
      <table class="table">
        <tr>
          <td align="right" class="col-md-4">
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
          <td align="left" class="col-md-8">
            <div class="graph-container" data-graph-zoomable="true" data-resource-id="${resultSet.resource.id}" data-graph-name="${resultSet.prefabGraph.name}" data-graph-title="${resultSet.prefabGraph.title}" data-graph-start="${resultSet.start.time}" data-graph-end="${resultSet.end.time}"></div>
          </td>
        </tr>
      </table>
    </div>
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Choose graph options</h3>
      </div>
      <div class="panel-body">
        <form class="form-horizontal" name="customize_graph" method="get" action="<%= baseHref %>KSC/formProcGraph.htm">
          <input type="hidden" name="<%=FormProcGraphController.Parameters.action%>" value="none" />
          <div class="form-group">
            <label class="col-md-2 label-control">Title</label>
            <div class="col-md-4">
              <input class="form-control" type="text" name="<%=FormProcGraphController.Parameters.title%>" value="${resultSet.title}" size="40" maxlength="40"/>
            </div>
          </div>
          <div class="form-group">
            <label class="col-md-2 label-control">Timespan</label>
            <div class="col-md-4">
              <select class="form-control" name="<%=FormProcGraphController.Parameters.timespan%>">
                <c:forEach var="option" items="${timeSpans}">
                  <c:choose>
                    <c:when test="${timeSpan == option.key}">
                      <c:set var="timespanSelected">selected="selected"</c:set>
                    </c:when>
                    <c:otherwise>
                      <c:set var="timespanSelected" value=""/>
                    </c:otherwise>
                  </c:choose>
                  <option value="${option.key}" ${timespanSelected}>${option.value.replaceAll("_", " ")}</option>
                </c:forEach>
              </select>
              <span class="help-block">This selects the relative start and stop times for the report</span>
            </div>
          </div>
          <div class="form-group">
            <label class="col-md-2 label-control">Prefabricated Report</label>
            <div class="col-md-4">
              <select class="form-control col-md-4" name="<%=FormProcGraphController.Parameters.graphtype%>">
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
              <span class="help-block">This selects the relative start and stop times for the report</span>
            </div>
          </div>
          <div class="form-group">
            <label class="col-md-2 label-control">Graph Index</label>
            <div class="col-md-4">
              <select class="form-control col-md-4" name="<%=FormProcGraphController.Parameters.graphindex%>">
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
              <span class="help-block">This selects the relative start and stop times for the report</span>
            </div>
          </div>
          <div class="btn-group">
            <button type="button" class="btn btn-default" onclick="cancelGraph()" alt="Cancel this graph configuration">Cancel edits to this graph</button>
            <button type="button" class="btn btn-default" onclick="updateGraph()" alt="Update changes to sample graph">Refresh sample view</button>
            <button type="button" class="btn btn-default" onclick="chooseResource()" alt="Choose a different resource to graph">Choose different resource</button>
            <button type="button" class="btn btn-default" onclick="saveGraph()" alt="Done with this graph configuration">Done with edits to this graph</button>
          </div>
        </form>
      </div>
    </div>
  </c:otherwise>
</c:choose>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
