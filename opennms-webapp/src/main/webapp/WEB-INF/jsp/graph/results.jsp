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
</c:import>

<div id="graph-results">

<div class="row">
  <div class="col-md-10 text-center">
    <%@ include file="/WEB-INF/jspf/relativetimeform.jspf" %>

    <c:set var="showCustom"></c:set>
    <c:if test="${results.relativeTime != 'custom'}">
        <c:set var="showCustom">style="display: none;"</c:set>
    </c:if>
    <div id="customTimeForm" name="customTimeForm" ${showCustom}>
        <form role="form" class="form-inline top-buffer" id="range_form" action="${requestScope.relativeRequestPath}" method="get">
            <c:forEach var="resultSet" items="${results.graphResultSets}">
                <input type="hidden" name="resourceId" value="${resultSet.resource.id}"/>
            </c:forEach>
            <c:forEach var="report" items="${results.reports}">
                <input type="hidden" name="reports" value="${report}"/>
            </c:forEach>
            <input type="hidden" name="relativetime" value="custom"/>
            <input type="hidden" name="zoom" value="${param.zoom}"/>

            <div class="row">
            <div class="form-group">
                <label>Start Time</label>
                <select class="form-control" name="startMonth">
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

                <input type="text" class="form-control" name="startDate" size="4" maxlength="2" value="${results.startCalendar.date}" />
                <input type="text" class="form-control" name="startYear" size="6" maxlength="4" value="${results.startCalendar.year}" />

                <select class="form-control" name="startHour">
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
              </div> <!-- form-group -->
              </div> <!-- row -->

              <div class="row">
              <div class="form-group">
                <label>End Time</label>
                <select class="form-control" name="endMonth">
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

                <input type="text" class="form-control" name="endDate" size="4" maxlength="2" value="${results.endCalendar.date}" />
                <input type="text" class="form-control" name="endYear" size="6" maxlength="4" value="${results.endCalendar.year}" />

                <select class="form-control" name="endHour">
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
            </div> <!-- form-group -->
            </div> <!-- row -->
            <button type="submit" class="btn btn-default">Apply Custom Time Period</button>
        </form>
    </div>

    <p>
        <strong>From</strong> ${results.start} <br/>
        <strong>To</strong> ${results.end} <br/>
    </p>
  </div> <!-- column -->
</div> <!-- row -->

<c:set var="showFootnote1" value="false"/>

<div class="row">

	<div class="col-md-10">
	<c:forEach var="resultSet" items="${results.graphResultSets}">
    <div class="panel panel-default text-center" id="panel-resource${resultSet.index}">
      <div class="panel-heading">
        <h3 class="panel-title">
            ${resultSet.resource.parent.resourceType.label}:
            <c:choose>
                <c:when test="${(!empty resultSet.resource.parent.link) && loggedIn}">
                    <a href="<c:url value='${resultSet.resource.parent.link}'/>">${resultSet.resource.parent.label}</a>
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
        </h3>
     </div> <!-- panel-heading -->
     <div class="panel-body">
        <!-- NRTG Starter script 'window'+resourceId+report -->
        <script type="text/javascript">
            function nrtgPopUp(resourceId, report) {
                window.open( getBaseHref() +'nrt/starter?resourceId='+resourceId+'&report='+report, '', 'width=1280, height=650, resizable=yes, scrollbars=yes, toolbar=no, location=no, directories=no, status=no, menubar=no' );
            }
        </script>

        <c:choose>
            <c:when test="${param.zoom == 'true'}">
                <c:url var="graphUrl" value="graph/graph.png">
                    <c:param name="resourceId" value="${resultSet.resource.id}"/>
                    <c:param name="report" value="${resultSet.graphs[0].name}"/>
                    <c:param name="start" value="${results.start.time}"/>
                    <c:param name="end" value="${results.end.time}"/>
                    <c:if test="${resultSet.graphs[0].graphWidth != null && resultSet.graphs[0].graphHeight != null}">
                        <c:param name="width" value="${resultSet.graphs[0].graphWidth}"/>
                        <c:param name="height" value="${resultSet.graphs[0].graphHeight}"/>
                    </c:if>
                </c:url>

                <script type="text/javascript">
                    var zoomGraphLeftOffset  = ${results.graphLeftOffset};
                    var zoomGraphRightOffset = ${results.graphRightOffset};
                    var zoomGraphStart       = ${results.start.time};
                    var zoomGraphEnd         = ${results.end.time};
                </script>


                <div align="center">
                    <div>
                    <div id="auxControls" class="graph-aux-controls" data-resource-id="${resultSet.resource.id}" data-graph-name="${resultSet.graphs[0].name}">
                    <opennms-addKscReport id="${resultSet.resource.id}.${resultSet.graphs[0].name}" reportName="${resultSet.graphs[0].name}" resourceId="${resultSet.resource.id}" graphTitle="${resultSet.graphs[0].title}" timespan="${results.relativeTime}" onclick="document.getElementById('auxControls').style.height = '120px';"></opennms-addKscReport>
                    <c:if test="${fn:contains(resultSet.resource.resourceType.label, 'SNMP') || fn:contains(resultSet.resource.resourceType.label, 'TCA') }">
                        <c:if test="${fn:contains(resultSet.resource.label,'(*)') != true}">
                            <a href="javascript:nrtgPopUp('${resultSet.resource.id}','${resultSet.graphs[0].name}')" title="Start NRT-Graphing for ${graph.title}"><button type="button" class="btn btn-default btn-xs" aria-label="Start NRT-Graphing for ${graph.title}"><span class="glyphicon glyphicon-flash" aria-hidden="true"></span></button></a><br/>
                        </c:if>
                    </c:if>
                    </div> <!-- graph-aux-controls -->
                    <img id="zoomImage" class="graphImg" data-imgsrc="${graphUrl}" src="#" alt="Resource graph: ${resultSet.graphs[0].title} (drag to zoom)" />
                    </div>
                </div>
            </c:when>

            <c:when test="${!empty resultSet.graphs}"> 
                <c:forEach var="graph" items="${resultSet.graphs}">
                    <c:url var="zoomUrl" value="${requestScope.relativeRequestPath}">
                        <c:param name="zoom" value="true"/>
                        <c:param name="relativetime" value="custom"/>
                        <c:param name="resourceId" value="${resultSet.resource.id}"/>
                        <c:param name="reports" value="${graph.name}"/>
                        <c:param name="start" value="${results.start.time}"/>
                        <c:param name="end" value="${results.end.time}"/>
                    </c:url>

                    <c:url var="graphUrl" value="graph/graph.png">
                        <c:param name="resourceId" value="${resultSet.resource.id}"/>
                        <c:param name="report" value="${graph.name}"/>
                        <c:param name="start" value="${results.start.time}"/>
                        <c:param name="end" value="${results.end.time}"/>
                    </c:url>

                    <div>

                    <div class="graph-aux-controls" data-resource-id="${resultSet.resource.id}" data-graph-name="${graph.name}">
                    <opennms-addKscReport id="${resultSet.resource.id}.${graph.name}" reportName="${graph.name}" resourceId="${resultSet.resource.id}" graphTitle="${graph.title}" timespan="${results.relativeTime}"></opennms-addKscReport>
                    <c:if test="${fn:contains(resultSet.resource.resourceType.label, 'SNMP') || fn:contains(resultSet.resource.resourceType.label, 'TCA') }">
                        <c:if test="${fn:contains(resultSet.resource.label,'(*)') != true}">
                            <a href="javascript:nrtgPopUp('${resultSet.resource.id}','${graph.name}')" title="Start NRT-Graphing for ${graph.title}"><button type="button" class="btn btn-default btn-xs" aria-label="Start NRT-Graphing for ${graph.title}"><span class="glyphicon glyphicon-flash" aria-hidden="true"></span></button></a><br/>
                        </c:if>
                    </c:if>
                    </div> <!-- graph-aux-controls -->
                    <a href="${zoomUrl}"><img class="graphImg" data-imgsrc="${graphUrl}" src="#" alt="Resource graph: ${graph.title} (click to zoom)" /></a>
                    </div>
                    <br/><br/>
                </c:forEach>
            </c:when>

            <c:otherwise>
                <p>
                    There is no data for this resource.
                </p>
            </c:otherwise>
        </c:choose>
    </div> <!-- panel-body -->
    </div> <!-- panel -->
    </c:forEach>

	</div> <!-- col-md-10 -->

	<div class="col-md-2">
	<div id="results-sidebar" class="resource-graphs-sidebar hidden-print hidden-xs hidden-sm sidebar-fixed">
        <ul class="nav nav-stacked">
            <c:forEach var="resourceType" items="${results.resourceTypes}">
            <li>
                <a href="${requestScope['javax.servlet.forward.request_uri']}?${pageContext.request.queryString}#panel-resource${results.graphResultMap[resourceType][0].index}" data-target="#panel-resource${results.graphResultMap[resourceType][0].index}">${resourceType}</a>
                <ul class="nav">
                    <c:forEach var="resultSet" items="${results.graphResultMap[resourceType]}">
                    <li><a href="${requestScope['javax.servlet.forward.request_uri']}?${pageContext.request.queryString}#panel-resource${resultSet.index}" data-target="#panel-resource${resultSet.index}">${resultSet.resource.label}</a></li> 
                    </c:forEach>
                </ul>
            </li>
            </c:forEach>
        </ul>
	</div>
    </div>

</div> <!-- row -->

</div> <!-- graph-results -->

<script type="text/javascript">
var e = $('#graph-results');
var imgs = e.find('img');
for (var i=0; i < imgs.length; i++) {
  var img = $(imgs[i]);
  var container = img.closest('div');
  var w = Math.round(container.width() * 0.8);
  var h = Math.round(w * 0.3);
  var imgsrc = img.data('imgsrc');
  if (imgsrc.indexOf("width=") > -1) {
    imgsrc = imgsrc.replace(/width=\d+/, "width=" + w);
  } else {
    imgsrc += "&width=" + w;
  }
  if (imgsrc.indexOf("height=") > -1) {
    imgsrc = imgsrc.replace(/height=\d+/, "height=" + h);
  } else {
    imgsrc += "&height=" + h;
  }
  img.attr('src', imgsrc);
}
</script>

<c:url var="relativeTimeReloadUrl" value="${requestScope.relativeRequestPath}">
    <c:forEach var="resultSet" items="${results.graphResultSets}">
        <c:param name="resourceId" value="${resultSet.resource.id}"/>
    </c:forEach>
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
        /*
         * This is used by the zoom page to reload the page with a new time period.
         */
        function reloadPage(newGraphStart, newGraphEnd) {
            setLocation('${zoomReloadUrl}'
                + "&start=" + newGraphStart + "&end=" + newGraphEnd);
        }
    </script>

    <script src="graph/cropper/lib/prototype.js" type="text/javascript"></script>      
    <script src="graph/cropper/lib/scriptaculous.js" type="text/javascript"></script>
    <script src="graph/cropper/cropper.js" type="text/javascript"></script>
    <script src="graph/cropper/zoom.js" type="text/javascript"></script>

    <script type="text/javascript">
        Event.observe(
        window,
        'load',
        function() {
            myCropper = new Cropper.Img(
            'zoomImage',
            {
                minHeight: $('zoomImage').getDimensions().height,
                maxHeight: $('zoomImage').getDimensions().height,
                onEndCrop: changeRRDImage
            }
        )
        }
    );
    </script>

</c:if>

<c:if test="${showFootnote1 == true}">
    <jsp:include page="/includes/footnote1.jsp" flush="false" />
</c:if>
<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
