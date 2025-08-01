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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ec" uri="http://www.extremecomponents.org" %>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Statistics Reports List")
          .breadcrumb("Report", "report/index.jsp")
          .breadcrumb("Statistics Reports", "statisticsReports/index.htm")
          .breadcrumb("List")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="card">
  <div class="card-header">
    <span>Statistics Report List</span>
  </div>
  <div class="card-body">

<c:choose>
  <c:when test="${empty model}">
    <p>None found.</p>
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
      <ec:table items="model" var="row"
        action="${relativeRequestPath}?${pageContext.request.queryString}"
        filterable="true"
        imagePath="images/table/compact/*.gif"
        tableId="reportList"
        form="form"
        rowsDisplayed="25"
        view="org.opennms.web.svclayer.etable.FixedRowCompact"
        showExports="true" showStatusBar="true" 
        autoIncludeParameters="false"
        >
      
        <ec:exportPdf fileName="Statistics Report List.pdf" tooltip="Export PDF"
          viewResolver="org.opennms.web.extremecomponent.view.resolver.OnmsPdfViewResolver"
          headerColor="black" headerBackgroundColor="#b6c2da"
          headerTitle="Statistics Report List" />
        <ec:exportXls fileName="Statistics Report List.xls" tooltip="Export Excel" />
      
        <ec:row highlightRow="false">

          <ec:column property="description" title="Enter Filter Text Above <br/> <br/> Report Description">
          	<c:url var="reportUrl" value="statisticsReports/report.htm">
          		<c:param name="id" value="${row.id}" />
          	</c:url>
          	<a href="${reportUrl}">${row.description}</a>
          </ec:column>

          <ec:column property="startDate" title="Enter Filter Text Above <br/> <br/> Reporting Period Start" cell="date" format="MMM d, yyyy  HH:mm:ss"/>
          <ec:column property="endDate" title="Enter Filter Text Above <br/> <br/> Reporting Period End"  cell="date" format="MMM d, yyyy  HH:mm:ss"/>
		  <ec:column property="duration" title="Enter Filter Text Above <br/> <br/> Run Interval">
            ${row.durationString}
          </ec:column>
          

          <ec:column property="maxDatumValue" title="Enter Filter Text Above <br/> <br/> Max Value">
            ${row.maxDatumValue}
          </ec:column>
          <ec:column property="minDatumValue" title="Enter Filter Text Above <br/> <br/> Min Value">
            ${row.minDatumValue}
          </ec:column>

          <ec:column property="purgeDate" title="Enter Filter Text Above <br/> <br/> Keep Until At Least" cell="date" format="MMM d, yyyy  HH:mm:ss"/>
        </ec:row>
      </ec:table>
    </form>
  </c:otherwise>
</c:choose>
  </div> <!-- card-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
