(function (PluginManager) {
  'use strict';

  angular.module('opennms.controllers.shared.nodes', [
    'ui.router',
    'ui.bootstrap',
    'truncate',
    'opennms.controllers.desktop.app',
    'opennms.services.shared.nodes',
    'opennms.services.shared.alarms',
    'opennms.services.shared.menu',
    'opennms.directives.shared.nodes'
  ])
    .controller('NodesCtrl', ['$scope', '$log', 'NodeService', function ($scope, $log, NodeService) {
      $log.debug('Initializing NodesCtrl.');
      $scope.listInterfaces = true;
      $scope.nodes = [];
      $scope.ifaces = [];

      $scope.limit = 10;
      $scope.offset = 0;
      $scope.fetchedAll = false;

      var self = this;

      $scope.init = function() {
        $scope.fetchNodes();
      };

      $scope.fetchMoreNodes = function() {
        $scope.offset += $scope.limit;
        $scope.fetchNodes();
      };

      $scope.fetchAllNodes = function() {
        $scope.limit = 0;
        $scope.fetchNodes();
      };

      $scope.getNodeLink = function (node) {
        return '#/node/' + node.id;
      };

      $scope.fetchNodes = function() {
        NodeService.list($scope.offset, $scope.limit).then($scope.processNodes);
      };

      $scope.processNodes = function(nodes) {
        $log.debug('Got nodes:', nodes);
        $scope.nodes = $scope.nodes.concat(nodes);

        if ($scope.listInterfaces) {
          for (var i=0; i < nodes.length; i++) {
            var nodeId = nodes[i]['_id'];
            NodeService.getIpInterfaces(nodeId).then(self.processIfaces);
          }
        }
      };

      self.processIfaces = function(ifaces) {
        if (ifaces) {
          if (!angular.isArray(ifaces)) {
            ifaces = [ifaces];
          }

          var nodeId = ifaces[0].nodeId;
          $log.debug('Interfaces for node ' + nodeId + ':', ifaces);
          $scope.ifaces[nodeId] = ifaces;
        }
      };

      $scope.init();
    }])
    .controller('NodeDetailCtrl', ['$scope', '$stateParams', '$log', 'NodeService', 'AlarmService', function ($scope, $stateParams, $log, NodeService, AlarmService) {
      $scope.node = {};

      $scope.init = function() {
        $scope.fetchNode($stateParams.nodeId);
      };

      $scope.processInterfaces = function(ifaces) {
        $log.debug('Got node ifaces:', ifaces);
        $scope.node.ifaces = ifaces;

        $scope.node.ifaces.forEach(function(iface, index) {
          NodeService.getIpInterfaceServices($scope.node._id, iface.ipAddress).then(function(services) {
            $log.debug('Got node iface services:', services);
            $scope.node.ifaces[index].services = services;
          });
        });
      };

      $scope.processNode = function(node) {
        $log.debug('Got node:', node);
        $scope.node = node;

        // Fetch interfaces.
        NodeService.getIpInterfaces(node._id).then($scope.processInterfaces);

        AlarmService.getByNode($stateParams.nodeId).then(function(alarms) {
          $scope.node.alarms = alarms;
        });
      };

      $scope.fetchNode = function(nodeId) {
        NodeService.get(nodeId).then($scope.processNode);
      };

      /// Runtime stuff.
      if(!$scope.isTest) {
        // When testing we don't want to initialize
        $scope.init();
      }

    }])

    .config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider) {
      $stateProvider.state('app.nodes', {
        url: '/nodes',
        views: {
          'mainContent': {
            templateUrl: 'templates/desktop/nodes/list.html',
            controller: 'NodesCtrl',
            title: 'Node List'
          }
        }
      })
        .state('app.nodes.detail', {
          url: '/node/:nodeId',
          views: {
            'nodeDetails': {
              templateUrl: 'templates/desktop/nodes/detail.html',
              controller: 'NodeDetailCtrl',
              title: 'Node Details'
            }
          }
        })
      ;
    }])

    .run(['$log', 'MenuService', function($log, menu) {
      menu.add('Info', '/app/nodes', 'Nodes');
    }])
  ;

  PluginManager.register('opennms.controllers.shared.nodes');
}(PluginManager));