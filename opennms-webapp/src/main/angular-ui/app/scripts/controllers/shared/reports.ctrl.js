(function (PluginManager) {
  'use strict';

  angular.module('opennms.controllers.shared.reports', [
    'ui.router',
    'ui.bootstrap',
    'truncate',
    'opennms.controllers.desktop.app',
    'opennms.services.shared.reports',
    'opennms.services.shared.alarms',
    'opennms.services.shared.menu',
    'opennms.directives.shared.reports'
  ])
    .controller('ReportsCtrl', ['$scope', '$log', 'ReportsService', function ($scope, $log, ReportsService) {
      $log.debug('Initializing ReportsCtrl.');
      $scope.reports = [];

      $scope.limit = 10;
      $scope.offset = 0;
      $scope.fetchedAll = false;

      var self = this;

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

      $scope.getReportLink = function (node) {
        return '#/report/' + node.id;
      };

      $scope.fetchReports = function() {
        ReportsService.list($scope.offset, $scope.limit).then($scope.processReports);
      };

      $scope.processReports = function(reports) {
        $log.debug('Got reports:', reports);
        $scope.reports = $scope.reports.concat(reports);

        if ($scope.listInterfaces) {
          for (var i=0; i < reports.length; i++) {
            var nodeId = reports[i]['_id'];
            ReportsService.getIpInterfaces(nodeId).then(self.processIfaces);
          }
        }
      };

      $scope.init();
    }])
    .controller('ReportDetailCtrl', ['$scope', '$stateParams', '$log', 'ReportsService', 'AlarmService', function ($scope, $stateParams, $log, ReportsService, AlarmService) {
      $scope.node = {};

      $scope.init = function() {
        $scope.fetchReport($stateParams.nodeId);
      };

      $scope.processInterfaces = function(ifaces) {
        $log.debug('Got node ifaces:', ifaces);
        $scope.node.ifaces = ifaces;

        $scope.node.ifaces.forEach(function(iface, index) {
          ReportsService.getIpInterfaceServices($scope.node._id, iface.ipAddress).then(function(services) {
            $log.debug('Got node iface services:', services);
            $scope.node.ifaces[index].services = services;
          });
        });
      };

      $scope.processReport = function(node) {
        $log.debug('Got node:', node);
        $scope.node = node;

        // Fetch interfaces.
        ReportsService.getIpInterfaces(node._id).then($scope.processInterfaces);

        AlarmService.getByReport($scope.node._id).then(function(alarms) {
          $scope.node.alarms = alarms;
        });
      };

      $scope.fetchReport = function(nodeId) {
        ReportsService.get(nodeId).then($scope.processReport);
      };

      /// Runtime stuff.
      if(!$scope.isTest) {
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
          url: '/reports/:reportId',
          views: {
            'nodeDetails': {
              templateUrl: 'templates/desktop/reports/detail.html',
              controller: 'ReportDetailCtrl',
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