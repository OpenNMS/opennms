(function (PluginManager) {
  'use strict';

  angular.module('opennms.controllers.shared.nodes', [
    'ui.router',
    'ui.bootstrap',
    'truncate',
    'opennms.controllers.desktop.app',
    'opennms.services.shared.nodes',
    'opennms.services.shared.menu',
    'opennms.directives.shared.nodes'
  ])
    .controller('NodesCtrl', ['$scope', '$log', 'NodeService', function ($scope, $log, NodeService) {
      $log.debug('Initializing NodesCtrl.');
      $scope.listInterfaces = true;
      //$scope.nodes = [];
      $scope.ifaces = {};

      $scope.limit = 10;
      $scope.offset = 0;

      $scope.init = function() {
        NodeService.list($scope.offset, $scope.limit).then(function(nodes) {
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

      $scope.fetchMoreNodes = function() {
        $scope.offset += $scope.limit;
      };

      $scope.getNodeLink = function (node) {
        return '#/node/' + node.id;
      };

      $scope.init();
    }])
    .controller('NodeDetailCtrl', ['$scope', '$stateParams', '$log', 'NodeService', function ($scope, $stateParams, $log, NodeService) {
      $scope.node = {};
//      $scope.node.label = 'node8';
//      $scope.node.id = 1;
//      $scope.node.foreignSource = 'bigreq';
//      $scope.node.foreignId = 'node8';
//      $scope.node.statusSite = '';
//      $scope.node.links = [];
//      $scope.node.resources = [];
//      $scope.node.navEntries = [];
//      $scope.node.schedOutages = '';
//      $scope.node.asset = {
//        'description': '',
//        'comments': ''
//      };
//      $scope.node.snmp = {
//        'sysName': '',
//        'sysObjectId': '',
//        'sysLocation': '',
//        'sysContact': '',
//        'sysDescription': ''
//      };

      $scope.init = function() {
        NodeService.get($stateParams.nodeId).then(function(node) {
          $log.debug('Got node:', node);
          $scope.node = node;
          NodeService.getIpInterfaces(node._id).then(function(ifaces) {
            $log.debug('Got node ifaces:', ifaces);
            $scope.node.ifaces = ifaces;
            $scope.node.ifaces.forEach(function(iface, index) {
              NodeService.getIpInterfaceServices($scope.node._id, iface.ipAddress).then(function(services) {
                $log.debug('Got node iface services:', services);
                $scope.node.ifaces[index].services = services;
              });
            });
          })
        });
      };

      $scope.getServicesForInterface = function(iface) {

      }
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