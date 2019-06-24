<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
            org.opennms.web.servlet.MissingParameterException,
            org.opennms.web.api.Util
    "%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
final String baseHref = Util.calculateUrlBase(request);
final String report = request.getParameter("report");
final String resourceId = request.getParameter("resourceId");
String[] requiredParameters = new String[] {"report", "resourceId"};

if (report == null) {
    throw new MissingParameterException("report", requiredParameters);
} else if (resourceId == null) {
    throw new MissingParameterException("resourceId", requiredParameters);
}

pageContext.setAttribute("report", report);
pageContext.setAttribute("resourceId", resourceId);
%>

<%
//  Verify the forecasting dependencies.
boolean canForecast = true;
try {
    org.opennms.netmgt.measurements.filters.impl.HWForecast.checkForecastSupport();
} catch (Throwable t) {
    canForecast = false;
}
pageContext.setAttribute("canForecast", canForecast);
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Forecasting" />
  <jsp:param name="quiet" value="true" />
  <jsp:param name="nobreadcrumbs" value="true" />
  <jsp:param name="usebackshift" value="true" />
</jsp:include>

<script>
if (! <%= canForecast %>) {
window.forecastError = "One or more dependencies required for forecasting "
    + "were not found or configured incorrectly. "
    + "Please ensure that R is correctly installed. "
    + "See the installation guide for details.";
}
</script>

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="angular-js" />
</jsp:include>
<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="forecast" />
</jsp:include>

<div class="row-fluid" ng-app="forecast" ng-controller="forecastCtrl">
    <div class="col-md-12">
      <div ng-cloak class="alert alert-danger" role="alert" ng-show="error">
		<span class="fa fa-exclamation-circle" aria-hidden="true"></span>
	    <span class="sr-only">Error:</span>
	    {{error}}
	  </div>

      <div class="card" ng-hide="error">
      <div class="card-header text-center">
          <span>Forecasting <c:out value="${report}"/> on <c:out value="${resourceId}"/> </span>
      </div> <!-- card-header -->
      <div class="card-body">
		<div class="row-fluid">
			<div class="col-md-7">
		        <div data-graph-report="<c:out value="${report}"/>" data-graph-resource="<c:out value="${resourceId}"/>"></div>
		    </div>
		    <div class="col-md-4" ng-show="series.length < 1">
		        <p>The graph does not contain any series that can be forecasted.</p>
		    </div>
		    <div class="col-md-4 mt-4" ng-hide="series.length < 1">
		        <form class="form" name="form">
		            <div class="form-group form-row">
		                <div class="col-sm-12">
		                    <label for="select-metric">Select the metric to forecast</label>
		                    <select class="form-control custom-select" id="select-metric"
		                            ng-model="metricToForecast"
		                            ng-options="s.name for s in series track by s.metric"
		                    >
		                    </select>
		                </div>
		            </div>

		            <div class="form-group form-row" ng-show="metricToForecast">
		                <div class="col-sm-12">
		                    <label for="select-template">Select a template</label>
		                    <select class="form-control custom-select" id="select-template"
		                            ng-model="forecastingTemplate"
		                            ng-options="t.name for t in forecastingTemplates track by t.id"
		                            ng-change="onForecastingTemplateChange()">
		                    </select>
		                    <span class="form-text text-muted">Choose from one of the available forecasting templates, or configure your own options.</span>
		                </div>
		            </div>

		            <div class="form-group form-row" ng-show="forecastingTemplate.id === 'custom'">
		                <div role="tabpanel" id="options">
		                    <!-- Nav tabs -->
		                    <ul class="nav nav-tabs" role="tablist">
		                        <li role="presentation" class="nav-item"><a href="#time" role="tab" data-toggle="tab" class="nav-link active">Time Span</a></li>
		                        <li role="presentation" class="nav-item"><a href="#stat" role="tab" data-toggle="tab" class="nav-link">Trend and Forecast</a></li>
		                    </ul>
		                    <!-- Tab panes -->
		                    <div class="tab-content">
		                        <div role="tabpanel" class="tab-pane active mt-2" id="time">
		                            <div class="form-group form-row">
		                                <label class="col-sm-3 col-form-label">Training Start</label>
		                                <div class="col-sm-9">
		                                    <input type="number" integer min="0" ng-required="true" class="form-control" name="trainingStart" ng-model="forecastingOptions.trainingStart"
												   ng-class="{'is-invalid': form.trainingStart.$invalid}">
		                                    <span class="form-text text-muted">Samples from this number of days ago will be used to train the model, but won't be shown on the graph.</span>
		                                </div>
		                            </div>

		                            <div class="form-group form-row">
		                                <label class="col-sm-3 col-form-label">Graph Start</label>
		                                <div class="col-sm-9">
		                                    <input type="number" integer min="1" ng-required="true" class="form-control" name="graphStart" ng-model="forecastingOptions.graphStart"
												   ng-class="{'is-invalid': form.graphStart.$invalid}">
		                                    <span class="form-text text-muted">Samples from this number of days ago will be shown on the graph.</span>
		                                </div>
		                            </div>

		                            <div class="form-group form-row">
		                                <label class="col-sm-3 col-form-label">Forecasts</label>
		                                <div class="col-sm-9">
		                                    <input type="number" integer min="1" ng-required="true" class="form-control" name="forecasts" ng-model="forecastingOptions.forecasts"
												   ng-class="{'is-invalid': form.forecasts.$invalid}">
		                                    <span class="form-text text-muted">Number of seasons to forecast.</span>
		                                </div>
		                            </div>
		                        </div>
		                        <div role="tabpanel" class="tab-pane mt-2" id="stat">
		                            <div class="form-group form-row">
		                                <label class="col-sm-3 col-form-label">Season</label>
		                                <div class="col-sm-9">
		                                    <input type="number" greater-than-zero ng-required="true" class="form-control" name="season" ng-model="forecastingOptions.season"
												   ng-class="{'is-invalid': form.season.$invalid}">
		                                    <span class="form-text text-muted">Seasonality in days of the sample data. The training set must contain at least two seasons worth of data.</span>
		                                </div>
		                            </div>

		                            <div class="form-group form-row">
		                                <label class="col-sm-3 col-form-label">Trend Order</label>
		                                <div class="col-sm-9">
		                                    <input type="number" integer min="1" ng-required="true" class="form-control" name="trendOrder" ng-model="forecastingOptions.trendOrder"
												   ng-class="{'is-invalid': form.trendOrder.$invalid}">
		                                    <span class="form-text text-muted">Order of the polynomial used to estimate the trend. Set to this 1 for a line or higher for a curve.</span>
		                                </div>
		                            </div>

		                            <div class="form-group form-row">
		                                <label class="col-sm-3 col-form-label">Confidence Level</label>
		                                <div class="col-sm-9">
		                                    <input type="number" greater-than-zero max="1" ng-required="true" class="form-control" name="confidenceLevel" ng-model="forecastingOptions.confidenceLevel"
												   ng-class="{'is-invalid': form.confidenceLevel.$invalid}">
		                                    <span class="form-text text-muted">Level used to calculate the upper and lower confidence bounds.</span>
		                                </div>
		                            </div>
		
		                            <div class="form-group form-row">
		                                <label class="col-sm-3 col-form-label">Outlier Threshold</label>
		                                <div class="col-sm-9">
		                                    <input type="number" greater-than-zero max="1" ng-required="true" class="form-control" name="outlierThreshold" ng-model="forecastingOptions.outlierThreshold"
												   ng-class="{'is-invalid': form.outlierThreshold.$invalid}">
		                                    <span class="form-text text-muted">Percentile used to eliminate outliers. Outliers and missing values are automatically interpolated.</span>
		                                </div>
		                            </div>
		                        </div>
		                    </div>
		                </div>
		            </div>
		
		            <div class="form-group form-row">
		                <div class="col-sm-12">
		                    <button type="button" id="reset" ng-click="reset()" ng-show="graphModel !== null" class="btn btn-danger pull-left">Reset</button>
		                    <button type="button" id="forecast" ng-click="forecast()" ng-disabled="!canForecast()" class="btn btn-primary pull-right">Forecast</button>
		                </div>
		            </div>
		        </form>
		    </div>
		  </div>
      </div> <!-- card-body -->
      </div> <!-- panel -->
    </div> <!-- col-md-12 -->
</div> <!-- row -->


<jsp:include page="/includes/bootstrap-footer.jsp" flush="false">
  <jsp:param name="quiet" value="true" />
</jsp:include>
