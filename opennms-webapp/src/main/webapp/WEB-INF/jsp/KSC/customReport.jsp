<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2003-2014 The OpenNMS Group, Inc.
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
    org.opennms.web.controller.ksc.FormProcReportController
"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<% final String baseHref = org.opennms.web.api.Util.calculateUrlBase( request ); %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="headTitle" value="Performance" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="headTitle" value="KSC" />
  <jsp:param name="location" value="KSC Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='KSC/index.jsp'>KSC Reports</a>" />
  <jsp:param name="breadcrumb" value="Custom Report" />
  <jsp:param name="renderGraphs" value="true" />
</jsp:include>

<%-- A script to Save the file --%>
<script type="text/javascript">
  function saveReport()
  {
    document.customize_form.action.value = "<c:out value="<%=FormProcReportController.Actions.Save.toString()%>"/>"; 
    document.customize_form.submit();
  }

  function addNewGraph()
  {
    document.customize_form.action.value = "<c:out value="<%=FormProcReportController.Actions.AddGraph.toString()%>"/>"; 
    document.customize_form.submit();
  }

  function modifyGraph(graph_index)
  {
    document.customize_form.action.value = "<c:out value="<%=FormProcReportController.Actions.ModGraph.toString()%>"/>"; 
    document.customize_form.graph_index.value = graph_index; 
    document.customize_form.submit();
  }

  function deleteGraph(graph_index)
  {
    document.customize_form.action.value = "<c:out value="<%=FormProcReportController.Actions.DelGraph.toString()%>"/>";
    document.customize_form.graph_index.value = graph_index; 
    document.customize_form.submit();
  }

  function cancelReport()
  {
    if (confirm("Do you really want to cancel configuration changes?")) {
        setLocation("KSC/index.jsp");
    }
  }
</script>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Customized Report Configuration</h3>
  </div>
  <div class="panel-body">
    <form class="form-horizontal" name="customize_form" method="get" action="<%= baseHref %>KSC/formProcReport.htm">
      <input type="hidden" name="<%=FormProcReportController.Parameters.action%>" value="none"/>
      <input type="hidden" name="<%=FormProcReportController.Parameters.graph_index%>" value="-1"/>
      <div class="form-group">
        <div class="col-md-6 col-md-offset-3">
          <label class="label-control">Title:</label>
          <input class="form-control" type="text" name="<%=FormProcReportController.Parameters.report_title%>" value="${title}" size="80" maxlength="80"/>
        </div>
      </div>
      <table class="table table-condensed">
        <c:if test="${fn:length(resultSets) > 0}">
          <c:forEach var="graphNum" begin="0" end="${fn:length(resultSets) - 1}">
            <c:set var="resultSet" value="${resultSets[graphNum]}"/>
            <tr>
              <td class="col-md-1">
                <div class="btn-group-vertical" role="group">
                  <button class="btn btn-default" onclick="modifyGraph(${graphNum})">Modify</button>
                  <button class="btn btn-default" onclick="deleteGraph(${graphNum})">Delete</button>
                </div>
              </td>
              <td align="right" class="col-md-3">
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
                <br/>
                From: ${resultSet.start}
                <br/>
                To: ${resultSet.end}
              </td>
              <td align="left" style="col-md-8">
                <div class="graph-container" data-graph-zoomable="false" data-resource-id="${resultSet.resource.id}" data-graph-name="${resultSet.prefabGraph.name}" data-graph-title="${resultSet.prefabGraph.title}" data-graph-start="${resultSet.start.time}" data-graph-end="${resultSet.end.time}"></div>
              </td>
            </tr>
          </c:forEach>
        </c:if>
        <tr>
          <td colspan="3">
            <div class="form-group col-md-12">
              <c:choose>
                <c:when test="${showTimeSpan}">
                  <c:set var="checked" value="checked"/>
                </c:when>
                <c:otherwise>
                  <c:set var="checked" value=""/>
                </c:otherwise>
               </c:choose>
              <label>
                <input type="checkbox" name="<%=FormProcReportController.Parameters.show_timespan%>" ${checked} />
                Show Timespan Button (allows global manipulation of report timespan)
              </label>
            </div>
            <div class="form-group col-md-12">
              <c:choose>
                <c:when test="${showGraphType}">
                  <c:set var="checked" value="checked"/>
                </c:when>
                <c:otherwise>
                  <c:set var="checked" value=""/>
                </c:otherwise>
              </c:choose>
              <label>
                <input type="checkbox" name="<%=FormProcReportController.Parameters.show_graphtype%>" ${checked} />
                Show Graphtype Button (allows global manipulation of report prefabricated graph type)
              </label>
            </div>
            <div class="form-group">
              <div class="col-md-2">
              <label>Number of graphs to show per line in the report.</label>
              <select class="form-control" name="<%=FormProcReportController.Parameters.graphs_per_line%>">
                <c:choose>
                  <c:when test="${graphsPerLine == 0}">
                    <option selected value="0">default</option>
                  </c:when>
                  <c:otherwise>
                    <option value="0">default</option>
                  </c:otherwise>
                </c:choose>
                <c:choose>
                  <c:when test="${graphsPerLine == 1}">
                    <option selected value="1">1</option>
                  </c:when>
                  <c:otherwise>
                    <option value="1">1</option>
                  </c:otherwise>
                </c:choose>
                <c:choose>
                  <c:when test="${graphsPerLine == 2}">
                    <option selected value="2">2</option>
                  </c:when>
                  <c:otherwise>
                    <option value="2">2</option>
                  </c:otherwise>
                </c:choose>
                <c:choose>
                  <c:when test="${graphsPerLine == 3}">
                    <option selected value="3">3</option>
                  </c:when>
                  <c:otherwise>
                    <option value="3">3</option>
                  </c:otherwise>
                </c:choose>
                <c:choose>
                  <c:when test="${graphsPerLine == 4}">
                    <option selected value="4">4</option>
                  </c:when>
                  <c:otherwise>
                    <option value="4">4</option>
                  </c:otherwise>
                </c:choose>
                <c:choose>
                  <c:when test="${graphsPerLine == 5}">
                    <option selected value="5">5</option>
                  </c:when>
                  <c:otherwise>
                    <option value="5">5</option>
                  </c:otherwise>
                </c:choose>
                <c:choose>
                  <c:when test="${graphsPerLine == 6}">
                    <option selected value="6">6</option>
                  </c:when>
                  <c:otherwise>
                    <option value="6">6</option>
                  </c:otherwise>
                </c:choose>
              </select>
              </div>
            </div>
            <div class="btn-group">
              <button type="button" class="btn btn-default" onclick="addNewGraph()" alt="Add a new graph to the report">Add New Graph</button>
              <button type="button" class="btn btn-default" onclick="saveReport()" alt="Save the Report to File">Save Report</button>
              <button type="button" class="btn btn-default" onclick="cancelReport()" alt="Cancel the report configuration">Cancel</button>
            </div>
          </td>
        </tr>
        <tr>
          <td colspan="3">
            If you make any changes, please make sure to save the report
            when you are done.  Changes will only be saved by using the
            "Save" button on this page.
          </td>
        </tr>
      </table>
    </form>
  </div>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
