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
<%@ page contentType="text/html;charset=UTF-8" language="java" import="org.opennms.web.springframework.security.AclUtils"%>

<% pageContext.setAttribute("nodeId", request.getParameter("node")); %>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Modify")
          .headTitle("Asset")
          .breadcrumb("Assets", "asset/index.jsp")
          .breadcrumb("Modify")
          .ngApp("onms-assets")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="jquery-ui-js" />
</jsp:include>
<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="bootbox-js" />
</jsp:include>
<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="onms-assets" />
</jsp:include>

<%
  AclUtils.NodeAccessChecker accessChecker = AclUtils.getNodeAccessChecker(getServletContext());

  Integer nodeId = null;

  try {
    nodeId = Integer.valueOf(request.getParameter("node"));
  } catch (NumberFormatException e) {
%>
    <h2>Error parsing node parameter.</h2>
<%
  }

  if (nodeId != null) {
    if (accessChecker.isNodeAccessible(nodeId)) {
%>
      <div class="container-fluid" ng-controller="NodeAssetsCtrl" ng-init="init(${nodeId})">

        <div growl></div>

        <h4>
          Node: <strong><a href="element/node.jsp?node=${nodeId}">{{ nodeLabel }}</a></strong>
        </h4>
        <p>
          Last modified by {{ (master['lastModifiedBy'] || 'no one') }} at {{ master['lastModifiedDate'] | onmsDate }}
        </p>

        <form name="assetForm" novalidate>
          <div class="row">
            <div class="col">
              <div class="btn-toolbar mb-4" role="toolbar">
                  <button type="button" class="btn btn-secondary mr-2" ng-click="save()" id="save-asset" ng-disabled="assetForm.$invalid">
                    <i class="fa fa-save"></i> Save
                  </button>
                  <button type="button" class="btn btn-secondary" ng-click="reset()" id="reset-asset">
                    <i class="fa fa-refresh"></i> Reset
                  </button>
              </div>
            </div>
          </div>

          <div class="row" ng-repeat="row in config.rows">
            <div ng-class="col.class" ng-repeat="col in row.columns">
              <div class="card" ng-repeat="panel in col.panels">
                <div class="card-header">
                  <span>{{ panel.title }}</span>
                </div>
                <div class="card-body">
                  <div class="form-horizontal" ng-repeat="field in panel.fields">
                    <div class="form-group">
                      <label class="col-form-label col-md-3" for="{{ field.model }}" uib-tooltip="{{ field.tooltip  }}">{{ field.label }}
                        <span class="badge badge-secondary ml-2" ng-show="(assetForm[field.model].$dirty && !(assetForm[field.model].$invalid && !assetForm[field.model].$pristine))">modified</span>
                      </label>
                      <div class="col-md-9">
                        <%-- Static/ReadOnly fields --%>
                        <p class="form-control-plaintext" ng-if="field.type=='static'">{{ asset[field.model] }}</p>
                        <%-- Standard fields with typeahead suggestions --%>
                        <input type="text" class="form-control" id="{{ field.model }}" name="{{ field.model }}" ng-model="asset[field.model]" ng-if="field.type=='text'"
                          typeahead-editable="true" typeahead-min-length="0" ng-pattern="field.pattern"
                          uib-typeahead="_.escape(suggestion) for suggestion in getSuggestions(field.model) | filter:$viewValue"
                          ng-class="{ 'is-invalid': assetForm[field.model].$invalid && !assetForm[field.model].$pristine }">
                        <%-- Password fields --%>
                        <%-- Set `autocomplete="new-password"` to prevent autocomplete.
                             See MDN: https://developer.mozilla.org/en-US/docs/Web/Security/Securing_your_site/Turning_off_form_autocompletion#preventing_autofilling_with_autocompletenew-password --%>
                        <input type="password" class="form-control" ng-model="asset[field.model]" ng-if="field.type=='password'"
                               ng-class="{ 'is-invalid': assetForm[field.model].$invalid && !assetForm[field.model].$pristine}"
                               autocomplete="new-password">
                        <%-- Textarea fields --%>
                        <textarea class="form-control" style="height: 20em;" ng-model="asset[field.model]" ng-if="field.type=='textarea'"
                                  ng-class="{ 'is-invalid': assetForm[field.model].$invalid && !assetForm[field.model].$pristine}"></textarea>
                        <%-- Date fields with Popup Picker --%>
                        <div class="input-group" ng-if="field.type=='date'">
                          <input type="text" class="form-control" uib-datepicker-popup="{{ dateFormat }}" is-open="field.open"
                                 ng-model="asset[field.model]" placeholder="Specify date using this format: {{ dateFormat }}"
                                 ng-class="{ 'is-invalid': assetForm[field.model].$invalid && !assetForm[field.model].$pristine}" />
                          <div class="input-group-append">
                            <button type="button" class="btn btn-secondary" ng-click="field.open=true"><i class="fa fa-calendar"></i></button>
                          </div>
                        </div>
                        <%-- List/Select fields --%>
                        <select class="form-control custom-select" ng-model="asset[field.model]" ng-if="field.type=='select'"
                                ng-class="{ 'is-invalid': assetForm[field.model].$invalid && !assetForm[field.model].$pristine}">
                          <option ng-repeat="value in field.options">{{value}}</option>
                        </select>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

        </form>
      </div>
<%
    } else {
%>
      <h2>Access denied.</h2>
<%
    }
  }
%>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

