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

<%@page language="java" contentType="text/html" session="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ec" uri="http://www.extremecomponents.org" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Statistics Report" />
  <jsp:param name="headTitle" value="Statistics Report" />
  <jsp:param name="location" value="reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Report</a>"/>
  <jsp:param name="breadcrumb" value="<a href='statisticsReports/index.htm'>Statistics Reports</a>"/>
  <jsp:param name="breadcrumb" value="Report"/>
</jsp:include>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Statistics Report: ${model.report.description}</h3>
  </div>
  <div class="panel-body">

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
  </div> <!-- panel-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
