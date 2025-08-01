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
<%@ page language="java" contentType="text/html" session="true" import="
	org.opennms.web.controller.ksc.FormProcGraphController
"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<% final String baseHref = org.opennms.web.api.Util.calculateUrlBase( request ); %>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Performance ")
          .headTitle("Reports")
          .headTitle("KSC")
          .breadcrumb("Reports", "report/index.jsp")
          .breadcrumb("KSC Reports", "KSC/index.jsp")
          .breadcrumb("Custom Graph")
          .flags("renderGraphs")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

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
    <div class="card">
      <div class="card-header">
        <span>No graph options available</span>
      </div>
      <div class="card-body">
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
    <div class="card">
      <div class="card-header">
        <span>Sample graph</span>
      </div>
      <table class="table">
        <tr>
          <td align="right" class="w-25">
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
          <td align="left" class="w-75">
            <div class="graph-container" data-graph-zoomable="true" data-resource-id="${resultSet.resource.id}" data-graph-name="${resultSet.prefabGraph.name}" data-graph-title="${resultSet.prefabGraph.title}" data-graph-start="${resultSet.start.time}" data-graph-end="${resultSet.end.time}"></div>
          </td>
        </tr>
      </table>
    </div>
    <div class="card">
      <div class="card-header">
        <span>Choose graph options</span>
      </div>
      <div class="card-body">
        <div class="col-lg-5 col-md-8 col-sm-12 col-xs-12">
          <form class="form" name="customize_graph" method="get" action="<%= baseHref %>KSC/formProcGraph.htm">
            <input type="hidden" name="<%=FormProcGraphController.Parameters.action%>" value="none" />
            <div class="form-group">
              <label>Title</label>
              <input class="form-control" type="text" name="<%=FormProcGraphController.Parameters.title%>" value="${resultSet.title}" size="40" maxlength="40"/>
            </div>
            <div class="form-group">
              <label>Timespan</label>
              <select class="form-control custom-select" name="<%=FormProcGraphController.Parameters.timespan%>">
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
              <span class="form-text text-muted">This selects the relative start and stop times for the report</span>
            </div>
            <div class="form-group">
              <label>Prefabricated Report</label>
              <select class="form-control custom-select" name="<%=FormProcGraphController.Parameters.graphtype%>">
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
              <span class="form-text text-muted">This selects the relative start and stop times for the report</span>
            </div>
            <div class="form-group">
            <label>Graph Index</label>
              <select class="form-control custom-select" name="<%=FormProcGraphController.Parameters.graphindex%>">
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
              <span class="form-text text-muted">This selects the relative start and stop times for the report</span>
            </div>
            <div class="btn-group">
              <button type="button" class="btn btn-secondary" onclick="cancelGraph()" alt="Cancel this graph configuration">Cancel edits to this graph</button>
              <button type="button" class="btn btn-secondary" onclick="updateGraph()" alt="Update changes to sample graph">Refresh sample view</button>
              <button type="button" class="btn btn-secondary" onclick="chooseResource()" alt="Choose a different resource to graph">Choose different resource</button>
              <button type="button" class="btn btn-secondary" onclick="saveGraph()" alt="Done with this graph configuration">Done with edits to this graph</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </c:otherwise>
</c:choose>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
