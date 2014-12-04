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

<%@page language="java"
  contentType="text/html"
  session="true"
  import="
  org.opennms.web.api.Util,
  org.opennms.web.servlet.XssRequestWrapper
  "
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
    final HttpServletRequest req = new XssRequestWrapper(request);
    final String match = req.getParameter("match");
    pageContext.setAttribute("match", match);
%>
<c:set var="baseHref" value="<%=Util.calculateUrlBase(request)%>"/>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="headTitle" value="Performance" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="headTitle" value="KSC" />
  <jsp:param name="location" value="ksc" />
  <jsp:param name="breadcrumb" value="<a href='${baseHref}report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="KSC Reports" />
</jsp:include>

<div class="row">

  <div class="col-md-5">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Customized Reports</h3>
      </div>
      <div class="panel-body">
        <p>Choose the custom report title to view or modify from the list below. There are ${fn:length(reports)} custom reports to select from.</p>
        <script type="text/javascript">
          var customData = {
            total: "${fn:length(reports)}",
            records: [
              <c:set var="first" value="true"/>
              <c:forEach var="report" items="${reports}">
                <c:if test="${match == null || match == '' || fn:containsIgnoreCase(report.value,match)}">
                  <c:choose>
                    <c:when test="${first == true}">
                      <c:set var="first" value="false"/>
                      {id:"${report.key}", value:"${report.value}", type:"custom"}
                    </c:when>
                    <c:otherwise>
                      ,{id:"${report.key}", value:"${report.value}", type:"custom"}
                    </c:otherwise>
                  </c:choose>
                </c:if>
              </c:forEach>
            ]
          };
        </script>
        <opennms:kscCustomReportList id="kscReportList" dataObject="customData" isreadonly="${isReadOnly}"></opennms:kscCustomReportList>
        <!-- For IE Only -->
        <div name="opennms-kscCustomReportList" id="kscReportList-ie" dataObject="customData" isreadonly="${isReadOnly}"></div>
      </div>
    </div>
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Node &amp; Domain Interface Reports</h3>
      </div>
      <div class="panel-body">
        <p>Select resource for desired performance report</p>
        <script type="text/javascript">
          var standardResourceData = {
            total: "${fn:length(topLevelResources)}",
            records: [
              <c:set var="first" value="true"/>
              <c:forEach var="resource" items="${topLevelResources}" varStatus="resourceCount">
                <c:if test="${match == null || match == '' || fn:containsIgnoreCase(resource.label,match)}">
                  <c:choose>
                    <c:when test="${first == true}">
                      <c:set var="first" value="false"/>
                      {id:"${resource.name}", value:"${resource.resourceType.label}: ${resource.label}", type:"${resource.resourceType.name}"}
                      </c:when>
                    <c:otherwise>
                      ,{id:"${resource.name}", value:"${resource.resourceType.label}: ${resource.label}", type:"${resource.resourceType.name}"}
                    </c:otherwise>
                  </c:choose>
                </c:if>
              </c:forEach>
            ]
          };
        </script>
        <div id="snmp-reports"></div>
        <opennms:nodeSnmpReportList id="nodeSnmpList" dataObject="standardResourceData"></opennms:nodeSnmpReportList>
        <div name="opennms-nodeSnmpReportList" id="nodeSnmpList-ie" dataObject="standardResourceData"></div>
      </div>
    </div>
  </div>

  <div class="col-md-7">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Descriptions</h3>
      </div>
      <div class="panel-body">
        <p>
          <b>Customized Reports</b>
          <c:choose>
            <c:when test="${kscReadOnly == false }">
              allow users to create, view, and edit customized reports containing
              any number of prefabricated reports from any available graphable
              resource.
            </c:when>
            <c:otherwise>
              allow users to view customized reports containing any number of
              prefabricated reports from any available graphable resource.
            </c:otherwise>
          </c:choose>
        </p>
        <p>
          <b>Node and Domain Interface Reports</b>
          <c:choose>
            <c:when test="${kscReadOnly == false }">
              allow users to view automatically generated reports for interfaces on
              any node or domain.These reports can be further edited and saved just
              like other customized reports.These reports list only the interfaces
              on the selected node or domain, but they can be customized to include
              any graphable resource.
            </c:when>
            <c:otherwise>
              allow users to view automatically generated reports for interfaces on
              any node or domain.
            </c:otherwise>
          </c:choose>
        </p>
      </div>
    </div>
    <script type="text/javascript">
      function doReload() {
        if (confirm("Are you sure you want to do this?")) {
          document.location = "<%=Util.calculateUrlBase(request, "KSC/index.htm?reloadConfig=true")%>";
        }
      }
    </script>
    <button class="btn btn-default" type="button" onclick="doReload()">Request a Reload of KSC Reports Configuration</button>
  </div>

</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
