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
<%@page language="java" contentType="text/html" session="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ec" uri="http://www.extremecomponents.org" %>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Statistics Report")
          .breadcrumb("Report", "report/index.jsp")
          .breadcrumb("Statistics Reports", "statisticsReports/index.htm")
          .breadcrumb("Report")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="card">
  <div class="card-header">
    <span>Statistics Report: ${model.report.description}</span>
  </div>
  <div class="card-body">

<c:choose>
  <c:when test="${empty model}">
      <p>
        None found.
      </p>
  </c:when>

  <c:otherwise>
    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="extremecomponents-js" />
      <jsp:param name="asset-type" value="js" />
    </jsp:include>

    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="onms-extremecomponents" />
    </jsp:include>

    <form id="form" action="${relativeRequestPath}" method="post">
      <ec:table items="model.data" var="row"
        action="${relativeRequestPath}?${pageContext.request.queryString}"
        filterable="false"
        imagePath="images/table/compact/*.gif"
        tableId="reportList"
        form="form"
        rowsDisplayed="25"
        view="org.opennms.web.svclayer.etable.FixedRowCompact"
        showExports="true" showStatusBar="true" 
        autoIncludeParameters="false"
        >

			<ec:exportPdf fileName="${model.report.description} (${model.report.startDate} - ${model.report.endDate}.pdf" tooltip="Export PDF"
				headerColor="black" headerBackgroundColor="#b6c2da"
				headerTitle="${model.report.description}, for period ${model.report.startDate} - ${model.report.endDate}" viewResolver="org.opennms.web.extremecomponent.view.resolver.OnmsPdfViewResolver"/>
			<ec:exportXls fileName="${model.report.description} (${model.report.startDate} - ${model.report.endDate}.xls" tooltip="Export Excel" />

      
        <ec:row highlightRow="false">
          <ec:column property="prettyResourceParentsReversed" title="Parent Resource(s)" sortable="false">
            <c:if test="${empty param.reportList_ev}"> <%-- We are in a web view --%>
	            <c:set var="count" value="0"/>
	            <c:forEach var="parentResource" items="${row.prettyResourceParentsReversed}">
	              <c:if test="${count > 0}">
	                <br/>
	              </c:if>
	              ${parentResource.resourceType.label}:
	              <c:choose>
	                <c:when test="${!empty parentResource.link}">
	                  <c:url var="resourceLink" value="${parentResource.link}"/>
	                  <a href="${resourceLink}">${parentResource.label}</a>
	                </c:when>
	                
	                <c:otherwise>
	                  ${parentResource.label}
	                </c:otherwise>
	              </c:choose>
	              <c:set var="count" value="${count + 1}"/>
	            </c:forEach>
	            &nbsp;
            </c:if>
          </ec:column>

          <ec:column property="prettyResource" sortable="false" title="Resource">
            <c:if test="${empty param.reportList_ev}"> <%-- We are in a web view --%>
            	${row.prettyResource.label}
            </c:if>
          </ec:column>

          <ec:column property="value"/>
          
          <c:if test="${empty param.reportList_ev}"> <%-- We are in a web view (exclude the Graphs column from PDF and XLS exports) --%>
	          <ec:column property="resource.id" title="Graphs" sortable="false">
	            <c:choose>
	              <c:when test="${!empty row.resource}">
	                <c:url var="graphUrl" value="graph/results.htm">
	                  <c:param name="resourceId" value="${row.resource.id}"/>
	                  <c:param name="start" value="${model.report.startDate.time}"/>
	                  <c:param name="end" value="${model.report.endDate.time}"/>
	                  <c:param name="reports" value="all"/>
	                </c:url>
	                <a href="${graphUrl}">Resource graphs</a>
	              </c:when>
	              
	              <c:otherwise>
	                -
	              </c:otherwise>
	            </c:choose>
	          </ec:column>
          </c:if>
        </ec:row>
      </ec:table>
    </form>
  </c:otherwise>
</c:choose>
  </div> <!-- card-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
