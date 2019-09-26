<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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
        %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:import url="/includes/bootstrap.jsp">
    <c:param name="title" value="Resource Graph Results" />
    <c:param name="headTitle" value="Results" />
    <c:param name="headTitle" value="Resource Graphs" />
    <c:param name="headTitle" value="Reports" />
    <c:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
    <c:param name="breadcrumb" value="<a href='graph/index.jsp'>Resource Graphs</a>"/>
    <c:param name="breadcrumb" value="Results" />
    <c:param name="scrollSpy" value="#results-sidebar" />
    <c:param name="meta"       value="<meta http-equiv='X-UA-Compatible' content='IE=Edge' />"/>
    <c:param name="renderGraphs" value="true" />
</c:import>

<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="add-to-ksc" />
</jsp:include>

<div id="graph-results">

<div class="row">
  <div class="col-md-10">
    <%@ include file="/WEB-INF/jspf/relativetimeform.jspf" %>

    <c:set var="showCustom"></c:set>
    <c:if test="${results.relativeTime != 'custom'}">
        <c:set var="showCustom">style="display: none;"</c:set>
    </c:if>
    <div id="customTimeForm" class="mb-3" name="customTimeForm" ${showCustom}>
        <form role="form" class="form top-buffer" id="range_form" action="${requestScope.relativeRequestPath}" method="get">
            <c:if test="${empty results.generatedId && empty results.nodeCriteria}">
                <c:forEach var="resultSet" items="${results.graphResultSets}">
                    <input type="hidden" name="resourceId" value="${resultSet.resource.id}"/>
                </c:forEach>
            </c:if>
            <c:if test="${not empty results.generatedId}">
                <input type="hidden" name="generatedId" value="${results.generatedId}"/>
            </c:if>
            <c:if test="${not empty results.nodeCriteria}">
                <input type="hidden" name="nodeCriteria" value="${results.nodeCriteria}"/>
            </c:if>
            <c:forEach var="report" items="${results.reports}">
                <input type="hidden" name="reports" value="${report}"/>
            </c:forEach>
            <input type="hidden" name="relativetime" value="custom"/>
            <input type="hidden" name="zoom" value="${param.zoom}"/>

            <div class="form-row form-group mb-1">
                <label class="col-form-label col-lg-1 col-md-2 col-4">Start Time</label>
                <select class="form-control custom-select col-lg-1 col-2" name="startMonth">
                    <c:forEach var="month" items="${results.monthMap}">
                        <c:choose>
                            <c:when test="${month.key == results.startCalendar.month}">
                                <c:set var="selected">selected="selected"</c:set>
                            </c:when>
                            <c:otherwise>
                                <c:set var="selected" value=""/>
                            </c:otherwise>
                        </c:choose>
                        <option value="${month.key}" ${selected}>${month.value}</option>
                    </c:forEach>
                </select>

                <input type="text" class="form-control col-lg-1 col-2" name="startDate" size="4" maxlength="2" value="${results.startCalendar.date}" />
                <input type="text" class="form-control col-lg-1 col-2" name="startYear" size="6" maxlength="4" value="${results.startCalendar.year}" />

                <select class="form-control custom-select col-lg-1 col-2" name="startHour">
                    <c:forEach var="hour" items="${results.hourMap}">
                        <c:choose>
                            <c:when test="${hour.key == results.startCalendar.hourOfDay}">
                                <c:set var="selected">selected="selected"</c:set>
                            </c:when>
                            <c:otherwise>
                                <c:set var="selected" value=""/>
                            </c:otherwise>
                        </c:choose>
                        <option value="${hour.key}" ${selected}>${hour.value}</option>
                    </c:forEach>
                </select>
            </div> <!-- row -->

            <div class="form-row form-group mb-1">
                <label class="col-form-label col-lg-1 col-md-2 col-4">End Time</label>
                <select class="form-control custom-select col-lg-1 col-2" name="endMonth">
                    <c:forEach var="month" items="${results.monthMap}">
                        <c:choose>
                            <c:when test="${month.key == results.endCalendar.month}">
                                <c:set var="selected">selected="selected"</c:set>
                            </c:when>
                            <c:otherwise>
                                <c:set var="selected" value=""/>
                            </c:otherwise>
                        </c:choose>
                        <option value="${month.key}" ${selected}>${month.value}</option>
                    </c:forEach>
                </select>

                <input type="text" class="form-control col-lg-1 col-2" name="endDate" size="4" maxlength="2" value="${results.endCalendar.date}" />
                <input type="text" class="form-control col-lg-1 col-2" name="endYear" size="6" maxlength="4" value="${results.endCalendar.year}" />

                <select class="form-control custom-select col-lg-1 col-2" name="endHour">
                    <c:forEach var="hour" items="${results.hourMap}">
                        <c:choose>
                            <c:when test="${hour.key == results.endCalendar.hourOfDay}">
                                <c:set var="selected">selected="selected"</c:set>
                            </c:when>
                            <c:otherwise>
                                <c:set var="selected" value=""/>
                            </c:otherwise>
                        </c:choose>
                        <option value="${hour.key}" ${selected}>${hour.value}</option>
                    </c:forEach>
                </select>
            </div> <!-- row -->
            <button type="submit" class="btn btn-secondary btn-sm col-lg-5 col-md-10 col-12">Apply Custom Time Period</button>
        </form>
    </div>
  </div> <!-- column -->
</div> <!-- row -->
<div class="row">
    <div class="row col-md-10">
        <label class="col-form-label col-lg-1 col-md-2 col-4"><strong>From</strong></label>
        <span class="col-lg-11 col-md-10 col-8 form-control-plaintext">${results.start}</span>
    </div>
</div>
<div class="row">
    <div class="row col-md-10">
        <label class="col-form-label col-lg-1 col-md-2 col-4"><strong>To</strong></label>
        <span class="col-lg-11 col-md-10 col-8 form-control-plaintext">${results.end}</span>
    </div>
</div>

<c:set var="showFootnote1" value="false"/>


<div class="row" ng-app="onms-ksc" ng-controller="AddToKscCtrl">

    <div class="col-md-10" ng-controller="graphSearchBoxCtrl" id="search-graphs">
        <form class="form-inline pull-right mb-4">
            <div class="input-group mr-4">
                <div class="input-group" class="col-md-auto">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <span class="fa fa-search"></span>
                        </div>
                    </div>
                    <input type="text" id="graphsearch" placeholder="Filter Graphs" ng-model="searchQuery" name="filter">
                    <div class="input-group-prepend" ng-show="searchQuery.length > 0">
                        <div class="input-group-text">
                            <span class="fa fa-remove" ng-click="searchQuery = ''"></span>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </div>
    
	<div class="col-md-10">
	<c:forEach var="resultSet" items="${results.graphResultSets}">
    <div class="card text-center" id="panel-resource${resultSet.index}">
      <div class="card-header">
        <span>
            ${resultSet.resource.parent.resourceType.label}:
            <c:choose>
                <c:when test="${(!empty resultSet.resource.parent.link) && loggedIn}">
                    <a href="<c:url value='${resultSet.resource.parent.link}'/>"><c:out value="${resultSet.resource.parent.label}"/></a>
                </c:when>
                <c:otherwise>
                    ${resultSet.resource.parent.label}
                </c:otherwise>
            </c:choose>

            <c:if test="${!empty resultSet.resource}">
                <br />
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
            </c:if>
        </span>
     </div> <!-- card-header -->
     <div class="card-body">
        <div growl></div>
        <!-- NRTG Starter script 'window'+resourceId+report -->
        <script type="text/javascript">
            function popUp(url) {
                window.open(getBaseHref() + url, '', 'width=1280, height=650, resizable=yes, scrollbars=yes, toolbar=no, location=no, directories=no, status=no, menubar=no' );
            }
        </script>

        <c:choose>
            <c:when test="${!empty resultSet.graphs}">
              <c:set var="nodeId" value="0"/>
              <c:set var="ifIndex" value="0"/>
              <c:forEach var="attribute" items="${resultSet.resource.attributes}">
                <c:if test="${fn:contains(attribute.name, 'ifIndex')}">
                  <c:set var="ifIndex" value="${attribute.value}"/>
                </c:if>
                <c:if test="${fn:contains(attribute.name, 'nodeId')}">
                  <c:set var="nodeId" value="${attribute.value}"/>
                </c:if>
              </c:forEach>
              <div style="display: inline" ng-controller="checkFlowsCtrl" ng-init="getFlowInfo(${nodeId}, ${ifIndex}, ${results.start.time}, ${results.end.time})">
                <c:forEach var="graph" items="${resultSet.graphs}">
                    <c:url var="specificGraphUrl" value="${requestScope.relativeRequestPath}">
                        <c:param name="reports" value="${graph.name}"/>
                        <c:param name="resourceId" value="${resultSet.resource.id}"/>
                    </c:url>
                    <c:url var="nrtgGraphUrl" value="graph/nrtg.jsp">
                        <c:param name="report" value="${graph.name}"/>
                        <c:param name="resourceId" value="${resultSet.resource.id}"/>
                    </c:url>
                    <c:url var="forecastGraphUrl" value="graph/forecast.jsp">
                        <c:param name="report" value="${graph.name}"/>
                        <c:param name="resourceId" value="${resultSet.resource.id}"/>
                    </c:url>
                    <!-- graph-search div should be immediate parent to graph-container as search depends on this. -->
                    <div class="graph-search" ng-show="enableGraph" ng-controller="graphSearchCtrl" resourceid ="${resultSet.resource.id}" graphname="${graph.name}" graphtitle = "${graph.title}">
	                    <div class="graph-aux-controls" style="padding-bottom: 5px" data-resource-id="${resultSet.resource.id}" data-graph-name="${graph.name}">
                            <a style="padding-right: 3px" title="Add ${graph.title} to KSC Report">
                                <button type="button" class="btn btn-secondary btn-sm" ng-click="open('${resultSet.resource.id}','${resultSet.resource.label}','${graph.name}','${graph.title}')">
                                    <i class="fa fa-plus" aria-hidden="true"></i>
                                </button>
                            </a>
		                    <c:if test="${fn:length(resultSet.graphs) > 1}">
		                        <a href="${specificGraphUrl}" style="padding-right: 3px" title="Open ${graph.title}"><button type="button" class="btn btn-secondary btn-sm"><i class="fa fa-binoculars" aria-hidden="true"></i></span></button></a>
		                    </c:if>
                                    <a href="javascript:popUp('${forecastGraphUrl}')" style="padding-right: 3px" title="Forecast ${graph.title}"><button type="button" class="btn btn-secondary btn-sm"><i class="fa fa-line-chart" aria-hidden="true"></i></span></button></a>
		                    <c:if test="${fn:contains(resultSet.resource.resourceType.label, 'SNMP') || fn:contains(resultSet.resource.resourceType.label, 'TCA') }">
		                        <c:if test="${fn:contains(resultSet.resource.label,'(*)') != true}">
		                            <a href="javascript:popUp('${nrtgGraphUrl}')" title="Start NRT-Graphing for ${graph.title}"><button type="button" class="btn btn-secondary btn-sm" aria-label="Start NRT-Graphing for ${graph.title}"><span class="fa fa-bolt" aria-hidden="true"></span></button></a>
		                        </c:if>
		                    </c:if>
                              <div style="display: inline" ng-if="flowsEnabled">
                                <a ng-href="{{flowGraphUrl}}" target="_blank" style="padding-right: 3px" title="{{ hasFlows ? 'Open flow graphs' : 'No flows were found in current time range'}}">
                                <span> <button type="button" ng-disabled="!hasFlows" class="btn btn-secondary btn-sm">
                                  <i class="fa fa-exchange" aria-hidden="true"></i>
                                  </button>
                                </span>
                              </a>
                            </div>
	                    </div> <!-- graph-aux-controls -->
                        <div class="graph-container" data-graph-zoomable="true" data-resource-id="${resultSet.resource.id}" data-graph-name="${graph.name}" data-graph-title="${graph.title}" data-graph-start="${results.start.time}" data-graph-end="${results.end.time}" data-graph-zooming="${param.zoom}"></div>
                        <br/><br/>
                    </div>
                </c:forEach>
                <div ng-show="nomatchingGraphs">
                    <p>
                        <b>No matching graphs found for this resource.</b>
                    </p>
                </div>
              </div>
            </c:when>

            <c:otherwise>
                <p>
                    <b>There is no data for this resource.</b>
                </p>
            </c:otherwise>
        </c:choose>
    </div> <!-- card-body -->
    </div> <!-- panel -->
    </c:forEach>

	</div> <!-- col-md-10 -->

	<div class="col-md-2">
	<div id="results-sidebar" class="resource-graphs-sidebar d-print-none d-none d-sm-block d-md-block sidebar-fixed">
        <ul class="nav flex-column">
            <c:forEach var="resourceType" items="${results.resourceTypes}">
            <li class="nav-item">
                <a class="nav-link" href="${requestScope['javax.servlet.forward.request_uri']}?${pageContext.request.queryString}#panel-resource${results.graphResultMap[resourceType][0].index}" data-target="#panel-resource${results.graphResultMap[resourceType][0].index}">${resourceType}</a>
                <ul class="nav">
                    <c:forEach var="resultSet" items="${results.graphResultMap[resourceType]}">
                    <li class="nav-item"><a class="nav-link" href="${requestScope['javax.servlet.forward.request_uri']}?${pageContext.request.queryString}#panel-resource${resultSet.index}" data-target="#panel-resource${resultSet.index}">${resultSet.resource.label}</a></li>
                    </c:forEach>
                </ul>
            </li>
            </c:forEach>
        </ul>
	</div>
    </div>

</div> <!-- row -->
</div> <!-- graph-results -->

<c:url var="relativeTimeReloadUrl" value="${requestScope.relativeRequestPath}">
    <c:if test="${empty results.generatedId && empty results.nodeCriteria}">
        <c:forEach var="resultSet" items="${results.graphResultSets}">
            <c:param name="resourceId" value="${resultSet.resource.id}"/>
        </c:forEach>
   </c:if>
   <c:if test="${not empty results.generatedId}">
        <c:param name="generatedId" value="${results.generatedId}"/>
   </c:if>
   <c:if test="${not empty results.nodeCriteria}">
        <c:param name="nodeCriteria" value="${results.nodeCriteria}"/>
   </c:if>
    <c:forEach var="report" items="${results.reports}">
        <c:param name="reports" value="${report}"/>
    </c:forEach>
</c:url>


<script type="text/javascript">

     function relativeTimeFormChange() {
        for (i = 0; i < document.reltimeform.rtstatus.length; i++) {
            if (document.reltimeform.rtstatus[i].selected) {
                var value = document.reltimeform.rtstatus[i].value;
                if (value == "custom") {
                    document.getElementById("customTimeForm").style.display = "block";
                } else {
                    goRelativeTime(value);
                }
            }
        }
    }

    /*
     * This is used by the relative time form to reload the page with a new
     * time period.
     */
    function goRelativeTime(relativeTime) {
        setLocation('${relativeTimeReloadUrl}'
            + "&relativetime=" + relativeTime);
    }
</script>

<c:if test="${param.zoom == 'true'}">
    <c:url var="zoomReloadUrl" value="${requestScope.relativeRequestPath}">
        <c:param name="zoom" value="true"/>
        <c:param name="relativetime" value="custom"/>
        <c:forEach var="resultSet" items="${results.graphResultSets}">
            <c:param name="resourceId" value="${resultSet.resource.id}"/>
        </c:forEach>
        <c:param name="reports" value="${results.reports[0]}"/>
    </c:url>

	<script type="text/javascript">
		var zoomGraphLeftOffset  = ${results.graphLeftOffset};
		var zoomGraphRightOffset = ${results.graphRightOffset};
		var zoomGraphStart       = ${results.start.time};
		var zoomGraphEnd         = ${results.end.time};
	</script>

    <script type="text/javascript">
        /*
         * This is used by the zoom page to reload the page with a new time period.
         */
        function reloadPage(newGraphStart, newGraphEnd) {
            setLocation('${zoomReloadUrl}'
                + "&start=" + newGraphStart + "&end=" + newGraphEnd);
        }
    </script>

    <jsp:include page="/assets/load-assets.jsp" flush="false">
        <jsp:param name="asset" value="cropper-js" />
    </jsp:include>

    <script type="text/javascript">
    var myCropper; // zoom.js expects this global
    var $j = jQuery.noConflict(); // Avoid conflicts with prototype.js used by graph/cropper/zoom.js
    $j(document).on("graphLoaded", {}, function(event, width, height) {
        myCropper = new Cropper.Img(
            'zoomImage',
            {
                minHeight: width,
                maxHeight: height,
                onEndCrop: changeRRDImage
            }
        );
    });
    </script>

</c:if>

<c:if test="${showFootnote1 == true}">
    <jsp:include page="/includes/footnote1.jsp" flush="false" />
</c:if>
<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
