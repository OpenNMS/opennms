<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

<%@ page contentType="text/html;charset=UTF-8" language="java" 
  import="
    org.opennms.web.api.Authentication,
    org.springframework.security.core.GrantedAuthority,
    org.springframework.security.core.context.SecurityContextHolder"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
  boolean kscReadOnly = !request.isUserInRole(Authentication.ROLE_ADMIN) || request.isUserInRole(Authentication.ROLE_READONLY) || request.getRemoteUser() == null;
  pageContext.setAttribute("kscReadOnly", kscReadOnly);
  boolean isReadOnly = false;
  for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
    if (Authentication.ROLE_READONLY.equals(authority.getAuthority())){
      isReadOnly = true;
      break;
    }
  }
  pageContext.setAttribute("isReadOnly", isReadOnly);
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="headTitle" value="Performance" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="headTitle" value="KSC" />
  <jsp:param name="location" value="ksc" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="KSC Reports" />
</jsp:include>

<div class="container-fluid" ng-app="onms-ksc-wizard" ng-controller="KSCWizardCtrl">

  <div growl></div>

  <div class="row">

    <div class="col-md-6">
      <div class="panel panel-default">
        <div class="panel-heading">
          <h3 class="panel-title">Customized Reports</h3>
        </div>
        <div class="panel-body">
          <p>Choose the custom report title to view or modify from the list below. There are {{ reports.length }} custom reports to select from.</p>
          <div class="table-responsive">
            <div class="input-group">
              <span class="input-group-addon">
                <span class="glyphicon glyphicon-search"></span>
              </span>
              <input class="form-control" type="text" placeholder="Search/Filter Reports" ng-model="reportFilter"></input>
            </div>
            <table class="table table-condensed table-hover" name="reports">
              <thead>
                <tr>
                  <th>Reports</th>
                </tr>
              </thead>
              <tbody>
                <tr name="report:{{ report.label }}" ng-class="{success: report.id == reportSelected.id}" ng-click="selectReport(report)" ng-repeat="report in filteredReports | startFrom:(kscCurrentPage-1)*kscPageSize | limitTo:kscPageSize">
                  <td>{{ report.label }}</td>
                </tr>
             </tbody>
            </table>
          </div>
          <ul uib-pagination class="pagination-sm"
            total-items="kscTotalItems"
            num-pages="kscNumPages"
            ng-model="kscCurrentPage"
            max-size="kscMaxSize"
            boundary-links="true"
            ng-show="filteredReports.length > kscPageSize"></ul>
        </div>
        <div class="panel-footer">
          <form name="kscForm">
          <div class="btn-group btn-group-justified" role="group">
            <div class="btn-group" role="group">
              <button type="button" class="btn btn-default" ng-click="viewReport()">View</button>
            </div> 
            <c:choose>
              <c:when test="${isReadOnly == false}">
              <div class="btn-group" role="group">
                <button type="button" class="btn btn-default" ng-click="customizeReport()">Customize</button>
              </div> 
              <div class="btn-group" role="group">
                <button type="button" class="btn btn-default" ng-click="createReport()">Create New</button>
              </div> 
              <div class="btn-group" role="group">
                <button type="button" class="btn btn-default" ng-click="createReportFromExisting()">Create from Existing</button>
              </div> 
              <div class="btn-group" role="group">
                <button type="button" class="btn btn-default" ng-click="deleteReport()">Delete</button>
              </div> 
              </c:when>
            </c:choose>
          </div> 
          </form>
        </div>
      </div> 
      <div class="panel panel-default">
        <div class="panel-heading">
          <h3 class="panel-title">Node &amp; Domain Interface Reports</h3>
        </div>
        <div class="panel-body">
          <p>Select resource for desired performance report</p>
          <div class="table-responsive">
            <div class="input-group">
              <span class="input-group-addon">
                <span class="glyphicon glyphicon-search"></span>
              </span>
              <input class="form-control" type="text" placeholder="Search/Filter Resources" ng-model="domainFilter"></input>
            </div>
            <table class="table table-condensed" name="resources">
              <thead>
                <tr>
                  <th>Resources</th>
                </tr>
              </thead>
              <tbody>
                <tr name="resource:{{ resource.label }}" ng-click="selectResource(resource)" ng-repeat="resource in filteredResources | startFrom:(currentPage-1)*pageSize | limitTo:pageSize">
                  <td>{{ resource.label }}</td>
                </tr>
             </tbody>
            </table>
          </div>
          <ul uib-pagination class="pagination-sm"
            total-items="totalItems"
            num-pages="numPages"
            ng-model="currentPage"
            max-size="maxSize"
            boundary-links="true"
            ng-show="filteredResources.length > pageSize"></ul>
        </div>
      </div> 
    </div> 

    <div class="col-md-6">
      <div class="panel panel-default">
        <div class="panel-heading">
          <h3 class="panel-title">Descriptions</h3>
        </div>
        <div class="panel-body">
          <p>
            <b>Customized Reports</b>
            <c:choose>
              <c:when test="${kscReadOnly == false}">
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
              <c:when test="${kscReadOnly == false}">
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
          <br/>
        </div>
      </div>
      <c:choose>
        <c:when test="${isReadOnly == false}">
        <button class="btn btn-default" type="button" ng-click="reloadConfig()">Request a Reload of KSC Reports Configuration</button>
        </c:when>
      </c:choose>
    </div>

  </div>

</div>

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="ksc-wizard" />
</jsp:include>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
