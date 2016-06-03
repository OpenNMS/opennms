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
  <jsp:param name="script" value='<script type="text/javascript" src="js/angular-1.4.8.min.js"></script>' />
  <jsp:param name="script" value='<script type="text/javascript" src="js/underscore-min.js"></script>' />
</jsp:include>

<div class="row-fluid" ng-app="forecast" ng-controller="forecastCtrl">
    <div class="col-md-12">
      <div ng-cloak class="alert alert-danger" role="alert" ng-show="error">
	    <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
	    <span class="sr-only">Error:</span>
	    {{error}}
	  </div>

      <div class="panel panel-default" ng-hide="error">
      <div class="panel-heading text-center">
          <h3 class="panel-title">Forecasting <c:out value="${report}"/> on <c:out value="${resourceId}"/> </h3>
      </div> <!-- panel-heading -->
      <div class="panel-body">
		<div class="row-fluid">
			<div class="col-md-7">
		        <div data-graph-report="<c:out value="${report}"/>" data-graph-resource="<c:out value="${resourceId}"/>"></div>
		    </div>
		    <div class="col-md-4" ng-show="series.length < 1">
		        <p>The graph does not contain any series that can be forecasted.</p>
		    </div>
		    <div class="col-md-4" ng-hide="series.length < 1">
		        <form class="form-horizontal" name="form">
		            <div class="form-group">
		                <div class="col-sm-12">
		                    <label for="select-metric">Select the metric to forecast:</label>
		                    <select class="form-control" id="select-metric"
		                            ng-model="metricToForecast"
		                            ng-options="s.name for s in series track by s.metric"
		                    >
		                    </select>
		                </div>
		            </div>

		            <div class="form-group" ng-show="metricToForecast">
		                <div class="col-sm-12">
		                    <label for="select-template">Select a template:</label>
		                    <select class="form-control" id="select-template"
		                            ng-model="forecastingTemplate"
		                            ng-options="t.name for t in forecastingTemplates track by t.id"
		                            ng-change="onForecastingTemplateChange()">
		                    </select>
		                    <span class="help-block">Choose from one of the available forecasting templates, or configure your own options.</span>
		                </div>
		            </div>

		            <div class="form-group" ng-show="forecastingTemplate.id === 'custom'">
		                <div role="tabpanel" id="options">
		                    <!-- Nav tabs -->
		                    <ul class="nav nav-tabs" role="tablist">
		                        <li role="presentation" class="active"><a href="#time" role="tab" data-toggle="tab">Time Span</a></li>
		                        <li role="presentation"><a href="#stat" role="tab" data-toggle="tab">Trend and Forecast</a></li>
		                    </ul>
		                    <!-- Tab panes -->
		                    <div class="tab-content">
		                        <div role="tabpanel" class="tab-pane active" id="time">
		                            <div class="form-group" ng-class="{'has-error': form.trainingStart.$invalid}">
		                                <label class="col-sm-3 control-label">Training Start</label>
		                                <div class="col-sm-9">
		                                    <input type="number" integer min="0" ng-required="true" class="form-control" name="trainingStart" ng-model="forecastingOptions.trainingStart">
		                                    <span class="help-block">Samples from this number of days ago will be used to train the model, but won't be shown on the graph.</span>
		                                </div>
		                            </div>

		                            <div class="form-group" ng-class="{'has-error': form.graphStart.$invalid}">
		                                <label class="col-sm-3 control-label">Graph Start</label>
		                                <div class="col-sm-9">
		                                    <input type="number" integer min="1" ng-required="true" class="form-control" name="graphStart" ng-model="forecastingOptions.graphStart">
		                                    <span class="help-block">Samples from this number of days ago will be shown on the graph.</span>
		                                </div>
		                            </div>

		                            <div class="form-group" ng-class="{'has-error': form.forecasts.$invalid}">
		                                <label class="col-sm-3 control-label">Forecasts</label>
		                                <div class="col-sm-9">
		                                    <input type="number" integer min="1" ng-required="true" class="form-control" name="forecasts" ng-model="forecastingOptions.forecasts">
		                                    <span class="help-block">Number of seasons to forecast.</span>
		                                </div>
		                            </div>
		                        </div>
		                        <div role="tabpanel" class="tab-pane" id="stat">
		                            <div class="form-group" ng-class="{'has-error': form.season.$invalid}">
		                                <label class="col-sm-3 control-label">Season</label>
		                                <div class="col-sm-9">
		                                    <input type="number" greater-than-zero ng-required="true" class="form-control" name="season" ng-model="forecastingOptions.season">
		                                    <span class="help-block">Seasonality in days of the sample data. The training set must contain at least two seasons worth of data.</span>
		                                </div>
		                            </div>

		                            <div class="form-group" ng-class="{'has-error': form.trendOrder.$invalid}">
		                                <label class="col-sm-3 control-label">Trend Order</label>
		                                <div class="col-sm-9">
		                                    <input type="number" integer min="1" ng-required="true" class="form-control" name="trendOrder" ng-model="forecastingOptions.trendOrder">
		                                    <span class="help-block">Order of the polynomial used to estimate the trend. Set to this 1 for a line or higher for a curve.</span>
		                                </div>
		                            </div>

		                            <div class="form-group" ng-class="{'has-error': form.confidenceLevel.$invalid}">
		                                <label class="col-sm-3 control-label">Confidence Level</label>
		                                <div class="col-sm-9">
		                                    <input type="number" greater-than-zero max="1" ng-required="true" class="form-control" name="confidenceLevel" ng-model="forecastingOptions.confidenceLevel">
		                                    <span class="help-block">Level used to calculate the upper and lower confidence bounds.</span>
		                                </div>
		                            </div>
		
		                            <div class="form-group" ng-class="{'has-error': form.outlierThreshold.$invalid}">
		                                <label class="col-sm-3 control-label">Outlier Threshold</label>
		                                <div class="col-sm-9">
		                                    <input type="number" greater-than-zero max="1" ng-required="true" class="form-control" name="outlierThreshold" ng-model="forecastingOptions.outlierThreshold">
		                                    <span class="help-block">Percentile used to eliminate outliers. Outliers and missing values are automatically interpolated.</span>
		                                </div>
		                            </div>
		                        </div>
		                    </div>
		                </div>
		            </div>
		
		            <div class="form-group">
		                <div class="col-sm-12">
		                    <button type="button" id="reset" ng-click="reset()" ng-show="graphModel !== null" class="btn btn-danger pull-left">Reset</button>
		                    <button type="button" id="forecast" ng-click="forecast()" ng-disabled="!canForecast()" class="btn btn-primary pull-right">Forecast</button>
		                </div>
		            </div>
		        </form>
		    </div>
		  </div>
      </div> <!-- panel-body -->
      </div> <!-- panel -->
    </div> <!-- col-md-12 -->
</div> <!-- row -->


<script>
    function getBaseHref() {
        return "<%= baseHref %>";
    }

    var app = angular.module("forecast", []);

    var INTEGER_REGEXP = /^\-?\d+$/;
    app.directive('integer', function() {
      return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
          ctrl.$validators.integer = function(modelValue, viewValue) {
            if (ctrl.$isEmpty(modelValue)) {
              // consider empty models to be valid
              return true;
            }

            if (INTEGER_REGEXP.test(viewValue)) {
              // it is valid
              return true;
            }

            // it is invalid
            return false;
          };
        }
      };
    });

    app.directive('greaterThanZero', function() {
        return {
          require: 'ngModel',
          link: function(scope, elm, attrs, ctrl) {
            ctrl.$validators.integer = function(modelValue, viewValue) {
              if (ctrl.$isEmpty(modelValue)) {
                return false;
              }
              return viewValue > 0;
            };
          }
        };
      });

    app.controller("forecastCtrl", function($scope) {
        // Use the first div we find with the data-graph-report attribute
        $scope.graphElement = $("div[data-graph-report]").first();

        // Holds a reference to the pristine graph model, once loaded
        $scope.graphModel = null;

        // Holds a reference to the graph, once rendered
        $scope.graph = null;

        // Populated with the list of available series from the graph model
        $scope.series = [];

        // The different list of options which all forecasting templates inherit
        var defaultForecastingOptions = {
            trainingStart: 14,
            graphStart: 7,
            season: 1.0,
            forecasts: 1,
            outlierThreshold: 0.975,
            confidenceLevel: 0.95,
            trendOrder: 3
        };

        // Different forecasting templates available
        $scope.forecastingTemplates = [{
            id: "1day",
            name: "1 day forecast",
            options: _.extend(_.clone(defaultForecastingOptions), {})
        },{
            id: "7day",
            name: "7 day forecast",
            options: _.extend(_.clone(defaultForecastingOptions), {
                trainingStart: 60,
                graphStart: 30,
                forecasts: 7
            })
        },{
            id: "31day",
            name: "31 day forecast",
            options: _.extend(_.clone(defaultForecastingOptions), {
                trainingStart: 365,
                graphStart: 90,
                forecasts: 4,
                season: 7.0
            })
        },{
            id: "custom",
            name: "Custom",
            options: _.extend(_.clone(defaultForecastingOptions), {})
        }];

        // User input
        $scope.metricToForecast = null;
        $scope.forecastingOptions = null;

        $scope.onForecastingTemplateChange = function() {
            // Deep clone the template's options when a template is selected
            $scope.forecastingOptions = JSON.parse(JSON.stringify($scope.forecastingTemplate.options));
        };

        function clearUserInput() {
            $scope.metricToForecast = null;
            $scope.forecastingTemplate = $scope.forecastingTemplates[0];
            $scope.onForecastingTemplateChange();
        }
        clearUserInput();

        function getGraphDefinition(report, resource) {
            var url = getBaseHref() + 'rest/graphs/' + encodeURIComponent(report);
            // Pull in the graph definition
            $.ajax({
                url: url,
                dataType: 'json',
                context: $(this)
            }).done(function (graphDef) {
                // Convert the graph definition
                var rrdGraphConverter = new Backshift.Utilities.RrdGraphConverter({
                    graphDef: graphDef,
                    resourceId: resource
                });

                // Save the pristine model in the scope
                $scope.graphModel = rrdGraphConverter.model;

                // Render the graph using the pristine model
                renderGraph($scope.graphModel);

                // Pull the list of named series from the model
                $scope.series = _.filter($scope.graphModel.series, function(series){ return !_.isEmpty(series.name); });
                $scope.$apply();
            }).fail(function() {
                $scope.error = "Failed to retrieve the graph definition for the report named: " + report;
                $scope.$apply();
            })
        }

        function renderGraph(graphModel, start, end) {
            // If no date range is set, use the last 7 days
            if (end === undefined) {
                end = Date.now();
            }
            if (start === undefined) {
                start = end - (7*24*60*60*1000);
            }

            // Destroy the existing graph, if any
            if ($scope.graph !== null) {
                $scope.graph.destroy();
                $scope.graph = null;
            }

            // Build the data-source
            var ds = new Backshift.DataSource.OpenNMS({
                url: getBaseHref() + "rest/measurements",
                metrics: graphModel.metrics
            });

            // Build and render the graph
            $scope.graph = new Backshift.Graph.Flot({
                width: $scope.graphElement.width(),
                height: $scope.graphElement.width() * 2/3,
                element: $scope.graphElement,
                start: start,
                end: end,
                dataSource: ds,
                model: graphModel,
                title: graphModel.title,
                verticalLabel: graphModel.verticalLabel
            });
            $scope.graph.render();
        }

        $scope.reset = function() {
            clearUserInput();
            // Re-render the original graph model
            renderGraph($scope.graphModel);
        };

        $scope.canForecast = function() {
            return $scope.metricToForecast !== null && $scope.forecastingTemplate !== null
                && $scope.form.$valid
                && ($scope.forecastingOptions.season * 2) < $scope.forecastingOptions.trainingStart;
        };

        $scope.forecast = function() {
            // Clone the original graph model
            var graphModel = JSON.parse(JSON.stringify($scope.graphModel));

            // Add series for the trend, forecast and bounds
            graphModel.series.push({
                "name": "Trend",
                "metric": "Trend",
                "type": "line",
                "color": "#00ffff"
            });
            graphModel.series.push({
                "name": "HW Fit",
                "metric": "HWFit",
                "type": "line",
                "color": "#0000ff"
            });
            graphModel.series.push({
                "name": "HW Lwr",
                "metric": "HWLwr",
                "type": "line",
                "color": "#ff0000"
            });
            graphModel.series.push({
                "name": "HW Upr",
                "metric": "HWUpr",
                "type": "line",
                "color": "#ff0000"
            });

            var numberOfSecondsInADay = 24*60*60;
            var now = Date.now();
            var trainingStartInMillis = now - ($scope.forecastingOptions.trainingStart * numberOfSecondsInADay * 1000);
            var graphStartInMillis = now - ($scope.forecastingOptions.graphStart * numberOfSecondsInADay * 1000);
            var graphEndInMillis = now;

            // Add metric filters to prepare, trend and forecast the target metric
            graphModel.metrics.push({
                "type": "filter",
                "name": "Chomp",
                "parameter": [{
                    "key": "stripNaNs",
                    "value": "true"
                }]
            });
            graphModel.metrics.push({
                "type": "filter",
                "name": "Outlier",
                "parameter": [{
                    "key": "inputColumn",
                    "value": $scope.metricToForecast.metric
                }, {
                    "key": "quantile",
                    "value": String($scope.forecastingOptions.outlierThreshold)
                }]
            });
            graphModel.metrics.push({
                "type": "filter",
                "name": "HoltWinters",
                "parameter": [{
                    "key": "inputColumn",
                    "value": $scope.metricToForecast.metric
                }, {
                    "key": "outputPrefix",
                    "value": "HW"
                }, {
                    "key": "numPeriodsToForecast",
                    "value": String($scope.forecastingOptions.forecasts)
                }, {
                    "key": "periodInSeconds",
                    "value": String($scope.forecastingOptions.season * numberOfSecondsInADay)
                }, {
                    "key": "confidenceLevel",
                    "value": String($scope.forecastingOptions.confidenceLevel)
                }]
            });
            graphModel.metrics.push({
                "type": "filter",
                "name": "Trend",
                "parameter": [{
                    "key": "inputColumn",
                    "value": $scope.metricToForecast.metric
                }, {
                    "key": "outputColumn",
                    "value": "Trend"
                }, {
                    "key": "secondsAhead",
                    "value": String($scope.forecastingOptions.forecasts * $scope.forecastingOptions.season * numberOfSecondsInADay)
                }, {
                    "key": "polynomialOrder",
                    "value": $scope.forecastingOptions.trendOrder
                }]
            });

            // Add another filter to trim all of the records in the training period
            graphModel.metrics.push({
                "type": "filter",
                "name": "Chomp",
                "parameter": [{
                    "key": "stripNaNs",
                    "value": "false"
                }, {
                    "key": "cutoffDate",
                    "value": String(graphStartInMillis)
                }]
            });
            renderGraph(graphModel, trainingStartInMillis, graphEndInMillis);
        };

        if (${canForecast}) {
            // Fetch the graph definition and load the original graph
            getGraphDefinition($scope.graphElement.data('graph-report'), $scope.graphElement.data('graph-resource'));
        } else {
            $scope.error = "One or more dependencies required for forecasting were not found or configured incorrectly. "
                         + "Please ensure that R is correctly installed. "
                         + "See the installation guide for details.";
        }
    });
</script>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false">
  <jsp:param name="quiet" value="true" />
</jsp:include>
