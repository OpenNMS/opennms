(function(PluginManager) {
  'use strict';

  angular.module('opennms.controllers.shared.reports', [
    'ui.router',
    'ui.bootstrap',
    'truncate',
    'angularMoment',
    'opennms.filters.global',
    'opennms.controllers.desktop.app',
    'opennms.services.shared.reports',
    'opennms.services.shared.menu',
    'opennms.directives.shared.reports'
  ])
    .controller('ReportsCtrl', ['$scope', '$log', 'ReportsService', function($scope, $log, ReportsService) {
        $log.debug('Initializing ReportsCtrl.');
        $scope.reports = [];

        $scope.limit = 10;
        $scope.offset = 0;
        $scope.fetchedAll = false;

        $scope.init = function() {
          $scope.fetchReports();
        };

        $scope.fetchMoreReports = function() {
          $scope.offset += $scope.limit;
          $scope.fetchReports();
        };

        $scope.fetchAllReports = function() {
          $scope.limit = 0;
          $scope.fetchReports();
        };

        $scope.fetchReports = function() {
          ReportsService.list($scope.offset, $scope.limit).then($scope.processReports);
        };

        $scope.processReports = function(reports) {
          $log.debug('Got reports:', reports);
          $scope.reports = $scope.reports.concat(reports);
        };

        $scope.init();
      }])
    .controller('ReportDetailCtrl', ['$scope', '$stateParams', '$log', 'ReportsService', function($scope, $stateParams, $log, ReportsService) {
        $log.debug('Initializing ReportDetailCtrl.');
        $scope.report = {};

        $scope.init = function() {
          $scope.fetchReport($stateParams.reportId);
        };

        $scope.processReport = function(report) {
          $log.debug('Got report:', report);
          $scope.report = report;
        };

        $scope.fetchReport = function(reportId) {
          ReportsService.get(reportId).then($scope.processReport);
        };

        /// Runtime stuff.
        if (!$scope.isTest) {
          // When testing we don't want to initialize
          $scope.init();
        }

      }])
    .controller('ReportGraphCtrl', ['$scope', '$stateParams', '$log', '$timeout', function($scope, $stateParams, $log, $timeout) {
        $log.debug('Initializing ReportGraphCtrl.', $scope);
        var re = /^node\[(\d+)\]\.(\S+?)\[(\S+)\]$/;

        $scope.getResourceLabel = function(graph) {
          var OK = re.exec(graph.resourceId);
          if (!OK) {
            return '';
          } else {
            return OK[2];
          }
        };

        $scope.interval = 30000; // 5 seconds.

        $scope.next = function() {
          $scope.end = moment();
          $scope.start = moment($scope.end).subtract('hour', 1);

          // These correspond to TIMESPAN_OPTIONS in the java class
          // org.opennms.netmgt.config.KSC_PerformanceReportFactory
          switch ($scope.graph.timespan) {
            case '1_hour':
              $scope.start = moment($scope.end).subtract('hours', 1);
              break;
            case '2_hour':
              $scope.start = moment($scope.end).subtract('hours', 2);
              break;
            case '4_hour':
              $scope.start = moment($scope.end).subtract('hours', 4);
              break;
            case '6_hour':
              $scope.start = moment($scope.end).subtract('hours', 6);
              break;
            case '8_hour':
              $scope.start = moment($scope.end).subtract('hours', 8);
              break;
            case '12_hour':
              $scope.start = moment($scope.end).subtract('hours', 12);
              break;
            case '1_day':
              $scope.start = moment($scope.end).subtract('days', 1);
              break;
            case '2_day':
              $scope.start = moment($scope.end).subtract('days', 2);
              break;
            case '7_day':
              $scope.start = moment($scope.end).subtract('days', 7);
              break;
            case '14_day':
              $scope.start = moment($scope.end).subtract('days', 14);
              break;
            case '1_month':
              $scope.start = moment($scope.end).subtract('months', 1);
              break;
            case '3_month':
              $scope.start = moment($scope.end).subtract('months', 3);
              break;
            case '6_month':
              $scope.start = moment($scope.end).subtract('months', 6);
              break;
            case '1_year':
              $scope.start = moment($scope.end).subtract('years', 1);
              break;
            case 'Today':
              $scope.start = moment($scope.end).startOf('day');
              break;
            case 'Yesterday':
              $scope.start = moment($scope.end).startOf('day').subtract(1, 'ms').startOf('day');
              $scope.end.startOf('day').subtract(1, 'ms');
              break;
            case 'Yesterday 9am-5pm':
              $scope.start = moment($scope.end).startOf('day').subtract(1, 'ms').startOf('day').hour(9);
              $scope.end.startOf('day').subtract(1, 'ms').hour(16);
              break;
            case 'Yesterday 5pm-10pm':
              $scope.start = moment($scope.end).startOf('day').subtract(1, 'ms').startOf('day').hour(17);
              $scope.end.startOf('day').subtract(1, 'ms').hour(21);
              break;
            case 'This Week':
              $scope.start = moment($scope.end).startOf('week');
              break;
            case 'Last Week':
              $scope.start = moment($scope.end).startOf('week').subtract(1, 'ms').startOf('week');
              $scope.end.startOf('week').subtract(1, 'ms');
              break;
            case 'Last Week':
              $scope.start = moment($scope.end).startOf('month');
              break;
            case 'Last Month':
              $scope.start = moment($scope.end).startOf('month').subtract(1, 'ms').startOf('month');
              $scope.end.startOf('month').subtract(1, 'ms');
              break;
            case 'This Quarter':
              $scope.start = moment($scope.end).startOf('quarter');
              break;
            case 'Last Quarter':
              $scope.start = moment($scope.end).startOf('quarter').subtract(1, 'ms').startOf('quarter');
              $scope.end.startOf('quarter').subtract(1, 'ms');
              break;
            case 'This Year':
              $scope.start = moment($scope.end).startOf('year');
              break;
            case 'Last Year':
              $scope.start = moment($scope.end).startOf('year').subtract(1, 'ms').startOf('year');
              $scope.end.startOf('year').subtract(1, 'ms');
              break;
          }
          ;

          $scope.imageUrl = '/opennms/graph/graph.png?resourceId=' + escape($scope.graph.resourceId) + '&report=' + $scope.graph.type + '&start=' + $scope.start.format('XSSS') + '&end=' + $scope.end.format('XSSS');
          $scope.resourceUrl = '/opennms/graph/results.htm?resourceId=' + escape($scope.graph.resourceId) + '&report=' + $scope.graph.type + '&start=' + $scope.start.format('XSSS') + '&end=' + $scope.end.format('XSSS');
        };

        var timeOut;
        $scope.reload = function() {
          $timeout.cancel(timeOut);
          timeOut = $timeout(function() {
            $scope.next();
            $scope.reload();
          }, $scope.interval);
        };
        $scope.next();
        $scope.reload();

        $scope.$on('$destroy', function(event) {
          if (timeOut) {
            $log.debug('cancel timeout:', timeOut);
            $timeout.cancel(timeOut);
          }
        });
      }])
    .controller('ReportNodeResourcesCtrl', ['$scope', '$stateParams', '$log', 'ReportsService', function($scope, $stateParams, $log, ReportsService) {
        $log.debug('Initializing ReportNodeResourcesCtrl.');
        $scope.resourcetypes = [];

        $scope.init = function() {
          $scope.fetchMetrics($stateParams.nodeId);
        };

        $scope.processReport = function(metrics) {
          $log.debug('Got metrics:', metrics);
          $scope.metrics = metrics;
        };

        $scope.fetchMetrics = function(nodeId) {
          ReportsService.getMetrics(nodeId).then($scope.processReport);
        };

        $scope.getResourceId = function(resource, metric) {
          return resource._name + '[' + metric._resourceName + ']';
        };

        /// Runtime stuff.
        if (!$scope.isTest) {
          // When testing we don't want to initialize
          $scope.init();
        }

      }])
    .controller('ReportsNodeResourcesListCtrl', ['$scope', '$stateParams', '$log', 'ReportsService', function($scope, $stateParams, $log, ReportsService) {
        $log.debug('Initializing ReportsNodeResourcesListCtrl.');
        $log.debug('$stateParams:', $stateParams);
        $scope.metrics = {};

        $scope.timeperiods = [
          {label: '1 Hour', value: '1_hour'},
          {label: '2 Hours', value: '2_hour'},
          {label: '4 Hours', value: '2_hour'},
          {label: '6 Hours', value: '2_hour'},
          {label: '8 Hours', value: '2_hour'},
          {label: '12 Hours', value: '2_hour'},
          {label: '1 Day', value: '1_day'},
          {label: '2 Days', value: '2_day'},
          {label: '7 Days', value: '7_day'},
          {label: '2 Weeks', value: '14_day'},
          {label: '1 Month', value: '1_month'},
          {label: '3 Month', value: '3_month'},
          {label: '6 Month', value: '6_month'},
          {label: '1 Year', value: '1_year'},
          {label: 'Today', value: 'Today'},
          {label: 'Yesterday', value: 'Yesterday'},
          {label: 'Yesterday 9am-5pm', value: 'Yesterday 9am-5pm'},
          {label: 'Yesterday 5pm-10pm', value: 'Yesterday 5pm-10pm'},
          {label: 'This Week', value: 'This Week'},
          {label: 'Last Week', value: 'Last Week'},
          {label: 'Last Month', value: 'Last Month'},
          {label: 'This Quarter', value: 'This Quarter'},
          {label: 'Last Quarter', value: 'Last Quarter'},
          {label: 'This Year', value: 'This Year'},
          {label: 'Last Year', value: 'Last Year'}
        ];
        $scope.timeperiod = $scope.timeperiods[8];

        $scope.refreshperiods = [
          5, 10, 15, 30, 60, 90, 120, 180, 300
        ];
        $scope.refresh = $scope.refreshperiods[3];

        $scope.timechange = function(period) {
          $log.debug('timechange:', period);
          //$log.debug('metrics:', $scope.metrics);
          $scope.metrics.resource.forEach(function(resource) {
            //$log.debug('resource:', resource);
            resource.metric.forEach(function(metric) {
              //$log.debug('metric:', metric);
              metric.graph.forEach(function(graph) {
                $log.debug('graph:', graph);
                graph.timespan = period.value;
              });
            });
          });
        };

        $scope.refreshchange = function(period) {
          $log.debug('refreshchange:', period);
        };

        $scope.init = function() {
          $scope.fetchMetrics($stateParams.nodeId, $stateParams.resourceId);
        };

        $scope.processReport = function(metrics) {
          $log.debug('Got metrics:', metrics);
          $scope.metrics = metrics;
        };

        $scope.fetchMetrics = function(nodeId, resourceId) {
          ReportsService.getGraphs(nodeId, resourceId).then($scope.processReport);
        };

        /// Runtime stuff.
        if (!$scope.isTest) {
          // When testing we don't want to initialize
          $scope.init();
        }

      }])

    .config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider) {
        $stateProvider.state('app.reports', {
          url: '/reports',
          views: {
            'mainContent': {
              templateUrl: 'templates/desktop/reports/list.html',
              controller: 'ReportsCtrl',
              title: 'Report List'
            }
          }
        })
          .state('app.reports.detail', {
            url: '/:reportId',
            views: {
              'reportDetails': {
                templateUrl: 'templates/desktop/reports/detail.html',
                controller: 'ReportDetailCtrl',
                title: 'Report Details'
              }
            }
          })
          .state('app.reports.node', {
            url: '/node/:nodeId',
            views: {
              'reportDetails': {
                templateUrl: 'templates/desktop/reports/resources.html',
                controller: 'ReportNodeResourcesCtrl',
                title: 'Report Details'
              }
            }
          })
          .state('app.reports.node-details', {
            url: '/node/:nodeId/resource/:resourceId',
            views: {
              'reportDetails': {
                templateUrl: 'templates/desktop/reports/resourcelist.html',
                controller: 'ReportsNodeResourcesListCtrl',
                title: 'Report Details'
              }
            }
          })
          ;
      }])

    .run(['$log', 'MenuService', function($log, menu) {
        menu.add('Info', '/app/reports', 'Reports');
      }])
    ;

  PluginManager.register('opennms.controllers.shared.reports');
}(PluginManager));