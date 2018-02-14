<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="headTitle" value="Choose Resource" />
  <jsp:param name="headTitle" value="KSC" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='KSC/index.jsp'>KSC Reports</a>" />
  <jsp:param name="breadcrumb" value="Custom Graph" />
</jsp:include>

<div class="container-fluid" ng-app="onms-ksc-wizard" ng-controller="KSCResourceCtrl">

  <%-- Title --%>

  <h4 ng-if="level == 0">Top-level</h4>
  <h4 ng-if="level > 0">Node: <a href="{{ selectedNode.link }}">{{ selectedNode.label }}</a></h4>
  <h4 ng-if="level == 2 && selectedResource.link == null">{{ selectedResource.typeLabel }}: {{ selectedResource.label }}</h4>
  <h4 ng-if="level == 2 && selectedResource.link != null">{{ selectedResource.typeLabel }}: <a href="{{ selectedResource.link }}">{{ selectedResource.label }}</a></h4>

  <div class="row">

    <div class="col-md-6">

      <%-- Panel 1 --%>

      <div class="panel panel-default">
        <div class="panel-heading">
          <h3 class="panel-title">Choose the current resource</h3>
        </div>
        <div class="panel-body">
          <p ng-show="level == 0">
            You are currently at the top-level resources.
            Select a child resource.
          </p>
          <p ng-show="level == 1">
            This resource has no available prefabricated graphs.
            Select a child resource or the parent resource (if any).
          </p>
          <div ng-show="level == 2">
            <p>
              This resource has the following prefabricated graphs available:
            </p>
            <ul>
              <li ng-repeat="(key,graph) in selectedResource.rrdGraphAttributes" name="graph.{{ key }}">{{ key }}</li>
            </ul>
            <br/>
            <button class="btn btn-default" type="button" ng-click="chooseResource()">Choose this resource</button>
          </div>
        </div>
      </div>

      <%-- Panel 2 --%>

      <div class="panel panel-default">
        <div class="panel-heading">
          <h3 class="panel-title">View child resources</h3>
        </div>
        <div class="panel-body">
          <p ng-show="resources.length == 0">
            No child resources found on this resource.
          </p>
          <div class="table-responsive" ng-show="resources.length > 0 && level < 2">
            <div class="input-group">
              <span class="input-group-addon">
                <span class="glyphicon glyphicon-search"></span>
              </span>
              <input class="form-control" type="text" placeholder="Search/Filter Reports" ng-model="resourceFilter"></input>
            </div>
            <table class="table table-condensed table-hover ">
              <thead>
                <tr>
                  <th>Resources</th>
                </tr>
              </thead>
              <tbody>
                <tr name="subresource:{{ resource.typeLabel }}:{{ resource.label }}" ng-class="{success: resource.id == selectedResource.id}" ng-click="selectResource(resource)" ng-repeat="resource in filteredResources | startFrom:(currentPage-1)*pageSize | limitTo:pageSize">
                  <td>{{ resource.typeLabel }} : {{ resource.label }}</td>
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
        <div class="pabel-footer">
          <div class="btn-group" role="group"ng-show=" resources.length > 0 && level < 2">
            <button type="button" class="btn btn-default" ng-click="viewResource()">View Child Resource</button>
            <button type="button" class="btn btn-default" ng-click="chooseResource()">Choose Child Resource</button>
          </div> 
        </div>
      </div>

      <%-- Panel 3 --%>

      <div class="panel panel-default">
        <div class="panel-heading">
          <h3 class="panel-title">View the parent resource</h3>
        </div>
        <div class="panel-body">
          <p ng-show="level == 0">
            This resource has no parent.  You are looking at the top-level resources.
          </p>
          <div ng-show="level == 1">
            <p>
              This resource has no parent.  You can use the "View top-level resources"
              button to see all top-level resources.
            </p>
            <button type="button" class="btn btn-default" ng-click="goBack()">View top-level resources</button>
          </div>
          <div ng-show="level == 2">
            <button type="button" class="btn btn-default" ng-click="goBack()">View the parent resource</button>
          </div> 
        </div>
      </div>

    </div> <!-- left-column -->

    <div class="col-md-6">

      <div class="panel panel-default">
        <div class="panel-heading">
          <h3 class="panel-title">Descriptions</h3>
        </div>
        <div class="panel-body">
          <p>
            The menu on the left lets you choose a specific resource that you want
            to use in a graph.
            A resource can be any graphable resource such as SNMP data (node-level,
            interface-level or generic indexed data), response time data, or
            distributed response time data.
          </p>
          <p>
            These resources are organized first by top-level resources, such as
            nodes or domains (if enabled), and then by child resources under the
            top-level resources, like SNMP node-level data, response time data,
            etc..
          </p>
          <p>
            The resource you are currently looking at (if any) is shown just below
            the menu-bar on the left side of the page.
            If the resource has any available prefabricated graphs, they will be
            listed in the <b>Choose the current resource</b> box along with a
            "Choose this resource" button which will take you to the graph
            customization page.
          </p>
          <p>
            If the current resource has child resources (or if you are at the
            top-level) a list of available child resources will be shown in the
            <b>View child resources</b> box.
            You can select a child resource and click the "View child resource"
            button to view the details of the selected child resource, including
            any available graphs and any sub-children.
            If you know the resource you are selecting has graphs, you can go
            straight to the graph customization page by clicking "Choose child
            resource".
          </p>
          <p>
            The <b>View the parent resource</b> box lets you see the parent resource
            of the current resource (or see all top-level resources).
            For example, if you are looking at an SNMP interface resource, its
            parent resource would be the node which owns that SNMP interface.
            If you are looking at a node, you would have the option to see all
            top-level resources.
          </p>
        </div>
      </div>

    </div> <!-- right-column -->

  </div> <!-- row -->

<div>

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="ksc-wizard" />
</jsp:include>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
