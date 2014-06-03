(function (PluginManager) {
  'use strict';

  angular.module('opennms.controllers.shared.nodes', [
    'ui.router',
    'opennms.controllers.desktop.app',
    'opennms.services.shared.nodes',
    'opennms.services.shared.menu'
  ])
    .controller('NodesCtrl', ['$scope', '$log', 'NodeService', function ($scope, $log, NodeService) {
      $log.debug('Initializing NodesCtrl.');
      $scope.listInterfaces = true;
      //$scope.nodes = [];
      $scope.ifaces = {};

      $scope.init = function() {
        NodeService.list().then(function(nodes) {
          $log.debug('Got nodes:', nodes);
          $scope.nodes = nodes;

          if ($scope.listInterfaces) {
            for (var i=0; i < nodes.length; i++) {
              var nodeId = nodes[i]['_id'];
              NodeService.getIpInterfaces(nodeId).then(function(ifaces) {
                if (ifaces) {
                  if (!angular.isArray(ifaces)) {
                    ifaces = [ifaces];
                  }

                  var nodeId = ifaces[0].nodeId;
                  $log.debug('Interfaces for node ' + nodeId + ':', ifaces);
                  $scope.ifaces[nodeId] = ifaces;
                }
              });
            }
          }

        });
      };

      $scope.getNodeLink = function (node) {
        return '#/node/' + node.id;
      };

      $scope.init();
    }])
    .controller('NodeDetailCtrl', ['$scope', '$stateParams', 'NodeService', function ($scope, $stateParams, NodeService) {
      $scope.node = {};
      $scope.node.label = 'node8';
      $scope.node.id = 1;
      $scope.node.foreignSource = 'bigreq';
      $scope.node.foreignId = 'node8';
      $scope.node.statusSite = '';
      $scope.node.links = [];
      $scope.node.resources = [];
      $scope.node.navEntries = [];
      $scope.node.schedOutages = '';
      $scope.node.asset = {
        'description': '',
        'comments': ''
      };
      $scope.node.snmp = {
        'sysName': '',
        'sysObjectId': '',
        'sysLocation': '',
        'sysContact': '',
        'sysDescription': ''
      };

      $scope.init = function() {
        $scope.node = NodeService.get($stateParams.nodeId);
      };

      console.log($scope.node);

      /// Runtime stuff.
      $scope.init();
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