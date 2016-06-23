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

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="Distributed Status History" />
	<jsp:param name="headTitle" value="Distributed Status History" />
	<jsp:param name="breadcrumb" value="<a href='distributedStatusSummary.htm'>Distributed Status</a>" />
	<jsp:param name="breadcrumb" value="History" />
</jsp:include>

<div class="panel panel-default">

  <div class="panel-heading">
    <h3 class="panel-title">Distributed Status History for ${historyModel.chosenApplication.name} from ${historyModel.chosenMonitor.name} over ${historyModel.chosenPeriod.name}</h3>
  </div>

  <c:if test="${!empty historyModel.errors}">
    <ul class="error">
      <c:forEach items="${historyModel.errors}" var="error">
        <li>${error}</li>
      </c:forEach>
    </ul>
  </c:if>

  <div class="panel-body">
    <br/>
    <form name="chooseForm" action="distributedStatusHistory.htm" role="form" class="form-horizontal">
      <input type="hidden" name="previousLocation" value="${historyModel.chosenLocation.locationName}"/>
      <div class="form-group">
        <label class="col-md-2 control-label" for="location">Location</label>
        <div class="col-md-4">
          <select class="form-control" name="location" id="location" onChange="document.chooseForm.submit();">
            <c:forEach items="${historyModel.locations}" var="location">
              <c:choose>
                <c:when test="${location.locationName == historyModel.chosenLocation.locationName}">
                  <option selected="selected">${location.locationName}</option>
                </c:when>
                <c:otherwise>
                  <option>${location.locationName}</option>
                </c:otherwise>
              </c:choose>
            </c:forEach>
          </select>
        </div>
    	</div>
      <div class="form-group">
        <label class="col-md-2 control-label" for="monitorId">Remote Poller</label>
        <div class="col-md-4">
          <c:choose>
            <c:when test="${empty historyModel.monitors}">
              <p class="form-control-static">No remote pollers have registered for this location.</p>
            </c:when>
        	  <c:otherwise>
              <select class="form-control" name="monitorId" id="monitor" onChange="document.chooseForm.submit();">
                <c:forEach items="${historyModel.monitors}" var="monitor">
                  <c:choose>
                    <c:when test="${monitor.id == historyModel.chosenMonitor.id}">
                      <option value="${monitor.id}" selected="selected">${monitor.name}</option>
                    </c:when>
                    <c:otherwise>
                      <option value="${monitor.id}">${monitor.name}</option>
                    </c:otherwise>
                  </c:choose>
                </c:forEach>
              </select>
              <c:url var="monitorLink" value="distributed/locationMonitorDetails.htm">
                <c:param name="monitorId" value="${historyModel.chosenMonitor.id}"/>
              </c:url>
              <a class="button" href="${monitorLink}">View remote poller details</a>
      	    </c:otherwise>
          </c:choose>
        </div>
      </div>
      <div class="form-group">
        <label class="col-md-2 control-label" for="application">Application</label>
        <div class="col-md-4">
          <select class="form-control" name="application" id="application" onChange="document.chooseForm.submit();">
            <c:forEach items="${historyModel.applications}" var="application">
              <c:choose>
                <c:when test="${application == historyModel.chosenApplication}">
                  <option selected="selected">${application.name}</option>
                </c:when>
                <c:otherwise>
                  <option>${application.name}</option>
                </c:otherwise>
              </c:choose>
            </c:forEach>
          </select>
        </div>
    	</div>
      <div class="form-group">
        <label class="col-md-2 control-label" for="timeSpan">Time Span</label>
        <div class="col-md-4">
      	  <select class="form-control" name="timeSpan" id="timeSpan" onChange="document.chooseForm.submit();">
      	    <c:forEach items="${historyModel.periods}" var="period">
                <c:choose>
                  <c:when test="${period == historyModel.chosenPeriod}">
                    <option value="${period.id}" selected="selected">${period.name}</option>
                  </c:when>
                  <c:otherwise>
      		      <option value="${period.id}">${period.name}</option>
                  </c:otherwise>
                </c:choose>
      		  </c:forEach>
          </select>
        </div>
      </div>
      <div class="form-group">
        <div class="col-md-offset-2 col-md-4">
          <button type="button" class="btn btn-default" onClick="document.viewStatusDetails.submit();">View Status Details</button>
        </div>
      </div>
    </form>

    <form action="distributedStatusDetails.htm" name="viewStatusDetails">
      <input type="hidden" name="location" value="${historyModel.chosenLocation.locationName}"/>
      <input type="hidden" name="application" value="${historyModel.chosenApplication.name}"/>
    </form>
      
    <c:set var="errors" value="0"/>

    <c:forEach items="${historyModel.serviceGraphs}" var="graph">
      <c:if test="${!empty graph.errors}">
        <c:if test="${errors == 0}">
          <c:out escapeXml="false" value="<span id='distributedStatusHistoryErroredServices' style='display: none;'>"/>
        </c:if>
        <c:set var="errors" value="${errors + 1}"/>
      </c:if>

      <p style="text-align: center">
        <c:url var="nodeUrl" value="element/node.jsp?node=${graph.service.ipInterface.node.id}"/>
        <c:url var="interfaceUrl" value="element/interface.jsp?ipinterfaceid=${graph.service.ipInterface.id}"/>
        <c:url var="serviceUrl" value="element/service.jsp?ifserviceid=${graph.service.id}"/>
        Node: <a href="${nodeUrl}">${graph.service.ipInterface.node.label}</a><br/>
        Interface: <a href="${interfaceUrl}">${graph.service.ipAddress.hostAddress}</a><br/>
        Service: <a href="${serviceUrl}">${graph.service.serviceName}</a><br/>
        <c:if test="${!empty graph.errors}">
          <span class="error">
            <c:forEach var="error" items="${graph.errors}">
              ${error}<br/>
            </c:forEach>
          </span>
        </c:if>
        <c:if test="${!empty graph.url}">
          <img src="${graph.url}"/>
        </c:if>
      </p>
    </c:forEach>

    <c:if test="${errors > 0}">
      <c:out escapeXml="false" value="</span>"/>
      <br/>
      <p id="distributedStatusHistoryErrorOverview">
        ${errors} services with no graphs are not shown.
        You can <a href="javascript:document.viewStatusDetails.submit();">view status details</a>
        for these services or <a href="javascript:hideErroredServices(0);">show all services</a>.
      </p>
      <p id="distributedStatusHistoryErrorDisplay" style="display: none;">
        ${errors} services with no graphs are shown.
        You can <a href="javascript:document.viewStatusDetails.submit();">view status details</a>
        for these services or <a href="javascript:hideErroredServices(1);">hide these services</a>.
      </p>
      <script type="text/javascript">
        function hideErroredServices(hide) {
          if (hide) {
            document.getElementById("distributedStatusHistoryErroredServices").style.display = "none";
            document.getElementById("distributedStatusHistoryErrorOverview").style.display = "block";
            document.getElementById("distributedStatusHistoryErrorDisplay").style.display = "none";
          } else {
            document.getElementById("distributedStatusHistoryErroredServices").style.display = "block";
            document.getElementById("distributedStatusHistoryErrorOverview").style.display = "none";
            document.getElementById("distributedStatusHistoryErrorDisplay").style.display = "block";
          }
        }
      </script>
    </c:if>

  </div> <!-- panel-body -->

</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
