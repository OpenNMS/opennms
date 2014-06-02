(function (PluginManager) {
  'use strict';

  angular.module('opennms.controllers.shared.nodes', [
    'opennms.services.shared.nodes'
  ])
    .controller('NodesController', ['$scope', 'nodeFactory', function ($scope, nodeFactory) {
      $scope.listInterfaces = false;
      $scope.nodes = [];

      $scope.init = function() {
        $scope.nodes = nodeFactory.getNodes();
      }
      ;
      $scope.getNodeLink = function (node) {
        return '#/node/' + node.id;
      };

      /// Runtime stuff.
      $scope.init();
    }])
    .controller('NodeDetailController', ['$scope', '$stateParams', 'nodeDetailFactory', function ($scope, $stateParams, nodeDetailFactory) {
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
        $scope.node = nodeDetailFactory.getNode($stateParams.id);
      }
      ;
      console.log($scope.node);

      /// Runtime stuff.
      $scope.init();
    }])

      .config(['$stateProvider', function($stateProvider) {
		$stateProvider.state('app.nodes', {
            url: '/nodes',
            views: {
                'mainContent': {
                    templateUrl: 'templates/desktop/nodes.html',
                    controller: 'NodesController'
                }
            }
        });
    }])

    .run(['$log', 'MenuService', function($log, menu) {
        menu.add('Info', '/app/nodes', 'Nodes');
    }])

    ;
    
    PluginManager.register('opennms.controllers.shared.nodes');
  ;
}(PluginManager));





