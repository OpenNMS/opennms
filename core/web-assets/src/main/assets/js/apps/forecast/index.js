/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
const angular = require('vendor/angular-js');
require('lib/onms-http');

const Backshift = require('vendor/backshift-js');
const $ = require('vendor/jquery-js');
const _ = require('vendor/underscore-js');
const buildFilter = require('./buildFilter');
const checkForecastWarning = require('./checkForecastWarning');
require('apps/onms-default-apps');

const INTEGER_REGEXP = /^-?\d+$/;

function getBaseHref() {
    return window.getBaseHref();
}

const app = angular.module('forecast', ['onms.http', 'onms.default.apps'])
  .config(['$locationProvider', function($locationProvider) {
    $locationProvider.hashPrefix('');
  }]);

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

app.controller('forecastCtrl', /* @ngInject */ function($scope) {
    // Use the first div we find with the data-graph-report attribute
    $scope.graphElement = $('div[data-graph-report]').first();

    // Holds a reference to the pristine graph definition, once loaded
    $scope.graphDef = null;

    // Holds a reference to the graph, once rendered
    $scope.graph = null;

    // Populated with the list of available series from the graph model
    $scope.series = [];

    // The different list of options which all forecasting templates inherit
    const defaultForecastingOptions = {
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
        id: '1day',
        name: '1 day forecast',
        options: _.extend(_.clone(defaultForecastingOptions), {})
    },{
        id: '7day',
        name: '7 day forecast',
        options: _.extend(_.clone(defaultForecastingOptions), {
            trainingStart: 60,
            graphStart: 30,
            forecasts: 7
        })
    },{
        id: '31day',
        name: '31 day forecast',
        options: _.extend(_.clone(defaultForecastingOptions), {
            trainingStart: 365,
            graphStart: 90,
            forecasts: 4,
            season: 7.0
        })
    },{
        id: 'custom',
        name: 'Custom',
        options: _.extend(_.clone(defaultForecastingOptions), {})
    }];

    // User input
    $scope.metricToForecast = null;
    $scope.forecastingOptions = null;

    $scope.onForecastingTemplateChange = function() {
        // Deep clone the template's options when a template is selected
        $scope.forecastingOptions = jQuery.extend(true, {}, $scope.forecastingTemplate.options);
    };

    function clearUserInput() {
        $scope.metricToForecast = null;
        $scope.forecastingTemplate = $scope.forecastingTemplates[0];
        $scope.onForecastingTemplateChange();
    }
    clearUserInput();

    function getGraphDefinition(report, resource) {
        const url = getBaseHref() + 'rest/graphs/' + encodeURIComponent(report);
        // Pull in the graph definition
        $.ajax({
            url: url,
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            dataType: 'json'
        }).done(function (graphDef) {
            // Save the pristine definition in the scope
            $scope.graphDef = graphDef;
            $scope.resource = resource;

            // Convert the graph definition
            const rrdGraphConverter = new Backshift.Utilities.RrdGraphConverter({
                graphDef: $scope.graphDef,
                resourceId: $scope.resource
            });

            // Render the graph using the pristine model
            renderGraph(rrdGraphConverter.model); // eslint-disable-line @typescript-eslint/no-use-before-define

            // Pull the list of named series from the model
            $scope.series = _.filter(rrdGraphConverter.model.series, function(series){ return !_.isEmpty(series.name); });
            $scope.$apply();
        }).fail(function() {
            $scope.error = 'Failed to retrieve the graph definition for the report named: ' + report;
            $scope.$apply();
        })
    }

    function renderGraph(graphModel, s, e) {
        // If no date range is set, use the last 7 days

        const end = e === undefined? Date.now() : e;
        const start = s === undefined? end - (7*24*60*60*1000) : s;

        // Destroy the existing graph, if any
        if ($scope.graph !== null) {
            $scope.graph.destroy();
            $scope.graph = null;
        }

        // Build the data-source
        const ds = new Backshift.DataSource.OpenNMS({
            url: getBaseHref() + 'rest/measurements',
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
        $scope.forecastWarning = null;
        // Re-render the original graph model
        const rrdGraphConverter = new Backshift.Utilities.RrdGraphConverter({
            graphDef: $scope.graphDef,
            resourceId: $scope.resource
        });
        renderGraph(rrdGraphConverter.model);
    };

    $scope.canForecast = function() {
        return $scope.metricToForecast !== null && $scope.forecastingTemplate !== null
            && $scope.form.$valid
            && ($scope.forecastingOptions.season * 2) < $scope.forecastingOptions.trainingStart;
    };

    $scope.forecast = function() {
        const rrdGraphConverter = new Backshift.Utilities.RrdGraphConverter({
            graphDef: $scope.graphDef,
            resourceId: $scope.resource
        });
        const graphModel = rrdGraphConverter.model;

        // Add series for the trend, forecast and bounds
        graphModel.series.push({
            'name': 'Trend',
            'metric': 'Trend',
            'type': 'line',
            'color': '#00ffff'
        });
        graphModel.series.push({
            'name': 'HW Fit',
            'metric': 'HWFit',
            'type': 'line',
            'color': '#9d4edd'
        });
        graphModel.series.push({
            'name': 'HW Lwr',
            'metric': 'HWLwr',
            'type': 'line',
            'color': '#ff0000'
        });
        graphModel.series.push({
            'name': 'HW Upr',
            'metric': 'HWUpr',
            'type': 'line',
            'color': '#ff0000'
        });

        const numberOfSecondsInADay = 24*60*60;
        const now = Date.now();
        const trainingStartInMillis = now - ($scope.forecastingOptions.trainingStart * numberOfSecondsInADay * 1000);
        const graphStartInMillis = now - ($scope.forecastingOptions.graphStart * numberOfSecondsInADay * 1000);
        const graphEndInMillis = now;

        // Add metric filters to prepare, trend and forecast the target metric.
        // buildFilter() wraps every value in String() so numeric Angular inputs
        // don't leak raw numbers into the REST payload.
        graphModel.metrics.push(buildFilter('Chomp', {
            stripNaNs: true
        }));
        graphModel.metrics.push(buildFilter('Outlier', {
            inputColumn: $scope.metricToForecast.metric,
            quantile: $scope.forecastingOptions.outlierThreshold
        }));
        graphModel.metrics.push(buildFilter('HoltWinters', {
            inputColumn: $scope.metricToForecast.metric,
            outputPrefix: 'HW',
            numPeriodsToForecast: $scope.forecastingOptions.forecasts,
            periodInSeconds: $scope.forecastingOptions.season * numberOfSecondsInADay,
            confidenceLevel: $scope.forecastingOptions.confidenceLevel
        }));
        graphModel.metrics.push(buildFilter('Trend', {
            inputColumn: $scope.metricToForecast.metric,
            outputColumn: 'Trend',
            secondsAhead: $scope.forecastingOptions.forecasts * $scope.forecastingOptions.season * numberOfSecondsInADay,
            polynomialOrder: $scope.forecastingOptions.trendOrder
        }));

        // Trim all of the records in the training period
        graphModel.metrics.push(buildFilter('Chomp', {
            stripNaNs: false,
            cutoffDate: graphStartInMillis
        }));
        renderGraph(graphModel, trainingStartInMillis, graphEndInMillis);

        // Wrap drawChart to surface forecast failures in the UI. The REST
        // response arrives asynchronously; we inspect its columns to detect
        // (a) a completely bailed forecast (no HWFit column) or (b) bounds
        // that coincide with the fit due to zero residual variance.
        var origDrawChart = $scope.graph.drawChart.bind($scope.graph);
        $scope.graph.drawChart = function(results) {
            $scope.$apply(function() {
                $scope.forecastWarning = checkForecastWarning(results);
            });
            return origDrawChart(results);
        };
    };

    if (window.forecastError) {
        $scope.error = window.forecastError;
    } else {
        // Fetch the graph definition and load the original graph
        getGraphDefinition($scope.graphElement.data('graph-report'), $scope.graphElement.data('graph-resource'));
    }
});
