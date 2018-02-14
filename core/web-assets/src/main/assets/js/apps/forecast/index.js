const angular = require('vendor/angular-js');

const Backshift = require('vendor/backshift-js');
const $ = require('vendor/jquery-js');
const _ = require('vendor/underscore-js');

const INTEGER_REGEXP = /^-?\d+$/;

function getBaseHref() {
    return window.getBaseHref();
}

var app = angular.module('forecast', []);

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

app.controller('forecastCtrl', function($scope) {
    // Use the first div we find with the data-graph-report attribute
    $scope.graphElement = $('div[data-graph-report]').first();

    // Holds a reference to the pristine graph definition, once loaded
    $scope.graphDef = null;

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
        var url = getBaseHref() + 'rest/graphs/' + encodeURIComponent(report);
        // Pull in the graph definition
        $.ajax({
            url: url,
            dataType: 'json'
        }).done(function (graphDef) {
            // Save the pristine definition in the scope
            $scope.graphDef = graphDef;
            $scope.resource = resource;

            // Convert the graph definition
            var rrdGraphConverter = new Backshift.Utilities.RrdGraphConverter({
                graphDef: $scope.graphDef,
                resourceId: $scope.resource
            });

            // Render the graph using the pristine model
            renderGraph(rrdGraphConverter.model);

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
        var ds = new Backshift.DataSource.OpenNMS({
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
        // Re-render the original graph model
        var rrdGraphConverter = new Backshift.Utilities.RrdGraphConverter({
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
        var rrdGraphConverter = new Backshift.Utilities.RrdGraphConverter({
            graphDef: $scope.graphDef,
            resourceId: $scope.resource
        });
        var graphModel = rrdGraphConverter.model;

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
            'color': '#0000ff'
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

        var numberOfSecondsInADay = 24*60*60;
        var now = Date.now();
        var trainingStartInMillis = now - ($scope.forecastingOptions.trainingStart * numberOfSecondsInADay * 1000);
        var graphStartInMillis = now - ($scope.forecastingOptions.graphStart * numberOfSecondsInADay * 1000);
        var graphEndInMillis = now;

        // Add metric filters to prepare, trend and forecast the target metric
        graphModel.metrics.push({
            'type': 'filter',
            'name': 'Chomp',
            'parameter': [{
                'key': 'stripNaNs',
                'value': 'true'
            }]
        });
        graphModel.metrics.push({
            'type': 'filter',
            'name': 'Outlier',
            'parameter': [{
                'key': 'inputColumn',
                'value': $scope.metricToForecast.metric
            }, {
                'key': 'quantile',
                'value': String($scope.forecastingOptions.outlierThreshold)
            }]
        });
        graphModel.metrics.push({
            'type': 'filter',
            'name': 'HoltWinters',
            'parameter': [{
                'key': 'inputColumn',
                'value': $scope.metricToForecast.metric
            }, {
                'key': 'outputPrefix',
                'value': 'HW'
            }, {
                'key': 'numPeriodsToForecast',
                'value': String($scope.forecastingOptions.forecasts)
            }, {
                'key': 'periodInSeconds',
                'value': String($scope.forecastingOptions.season * numberOfSecondsInADay)
            }, {
                'key': 'confidenceLevel',
                'value': String($scope.forecastingOptions.confidenceLevel)
            }]
        });
        graphModel.metrics.push({
            'type': 'filter',
            'name': 'Trend',
            'parameter': [{
                'key': 'inputColumn',
                'value': $scope.metricToForecast.metric
            }, {
                'key': 'outputColumn',
                'value': 'Trend'
            }, {
                'key': 'secondsAhead',
                'value': String($scope.forecastingOptions.forecasts * $scope.forecastingOptions.season * numberOfSecondsInADay)
            }, {
                'key': 'polynomialOrder',
                'value': $scope.forecastingOptions.trendOrder
            }]
        });

        // Add another filter to trim all of the records in the training period
        graphModel.metrics.push({
            'type': 'filter',
            'name': 'Chomp',
            'parameter': [{
                'key': 'stripNaNs',
                'value': 'false'
            }, {
                'key': 'cutoffDate',
                'value': String(graphStartInMillis)
            }]
        });
        renderGraph(graphModel, trainingStartInMillis, graphEndInMillis);
    };

    if (window.forecastError) {
        $scope.error = window.forecastError;
    } else {
        // Fetch the graph definition and load the original graph
        getGraphDefinition($scope.graphElement.data('graph-report'), $scope.graphElement.data('graph-resource'));
    }
});
