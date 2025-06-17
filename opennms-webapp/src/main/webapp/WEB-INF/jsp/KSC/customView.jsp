<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

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

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Performance")
          .headTitle("Reports")
          .headTitle("KSC")
          .breadcrumb("Reports", "report/index.jsp")
          .breadcrumb("KSC Reports", "KSC/index.jsp")
          .breadcrumb("Custom View")
          .flags("renderGraphs")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

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
  <div class="card">
    <div class="card-header">
      <span>No graphs defined</span>
    </div>
    <div class="card-body">
      <p>There are no graphs defined for this report.</p>
    </div>
  </div>
  </c:when>

  <c:otherwise>
    <div class="card">
      <div class="card-header">
        <span>Custom View: ${title}</span>
      </div>
      <div class="card-body">
        <form class="form-horizontal" name="view_form" method="get" action="<%= baseHref %>KSC/formProcView.htm">
          <input type="hidden" name="<%=FormProcViewController.Parameters.type%>" value="${reportType}" >
          <input type="hidden" name="<%=FormProcViewController.Parameters.action%>" value="none">
          <c:if test="${!empty report}">
            <input type="hidden" name="<%=FormProcViewController.Parameters.report%>" value="${report}">
          </c:if>
          <table id="graph-results" class="table table-sm" align="center">
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
                      <table class="table table-sm">
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
                              <c:param name="reports" value="${resultSet.prefabGraph.name}"/>
                              <c:param name="start" value="${resultSet.start.time}"/>
                              <c:param name="end" value="${resultSet.end.time}"/>
                            </c:url>
                            <a href="${detailUrl}">Detail</a>                            
                          </th>
                        </tr>
                      </table>
                      <div class="graph-container" data-graph-zoomable="true" data-resource-id="${resultSet.resource.id}" data-graph-name="${resultSet.prefabGraph.name}" data-graph-title="${resultSet.prefabGraph.title}" data-graph-start="${resultSet.start.time}" data-graph-end="${resultSet.end.time}"></div>
                    </td>
                    <c:set var="graphNum" value="${graphNum + 1}"/>
                  </c:if>
                </c:forEach>
              </tr>
            </c:forEach>
          </table>
          <!-- Select Timespan Input --> 
          <c:if test="${!empty timeSpan}">
            <div class="form-group">
              <label class="col-md-2 label-control">Override Graph Timespan</label>
              <div class="col-md-4">
                <select class="form-control custom-select" name="timespan">
                  <c:forEach var="option" items="${timeSpans}">
                    <c:choose>
                      <c:when test="${timeSpan == option.key}">
                        <c:set var="selected">selected="selected"</c:set>
                      </c:when>
                      <c:otherwise>
                        <c:set var="selected" value=""/>
                      </c:otherwise>
                    </c:choose>
                    <option value="${option.key}" ${selected}>${option.value.replaceAll("_", " ")}</option>
                  </c:forEach>
                </select>
                <span class="form-text text-muted">Press update button to reflect option changes to ALL graphs</span>
              </div>
            </div>
          </c:if>
          <!-- Select Graph Type --> 
          <c:if test="${!empty graphType}">
            <div class="form-group">
              <label class="col-md-2 label-control">Override Graph Type</label>
              <div class="col-md-4">
                <select class="form-control custom-select" name="graphtype">
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
                <span class="form-text text-muted">Press update button to reflect option changes to ALL graphs</span>
              </div>
            </div>
          </c:if>
          <!-- Button bar -->
          <div class="btn-group">
            <button class="btn btn-secondary" type="button" onclick="exitReport()">Exit Report Viewer</button>
            <c:if test="${!empty timeSpan || !empty graphType}">
              <button class="btn btn-secondary" type="button" onclick="updateReport()">Update Report View</button>
            </c:if>
            <c:if test="${showCustomizeButton}">
              <button class="btn btn-secondary" type="button" onclick="customizeReport()">Customize This Report</button>
            </c:if>
          </div>
        </form>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </c:otherwise>
</c:choose>

<c:if test="${showFootnote1 == true}">
  <jsp:include page="/includes/footnote1.jsp" flush="false" />
</c:if>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
