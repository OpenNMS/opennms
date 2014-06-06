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
    'opennms.services.shared.pagedresource',
    'opennms.services.shared.modelfactory',
    'opennms.models.node',
    'opennms.directives.shared.nodes'
  ])
    .controller('NodesCtrl', ['$scope', '$log', '$timeout', 'NodeService', 'PagedResourceFactory', 'ModelFactory', function ($scope, $log, $timeout, NodeService, PagedResourceFactory, ModelFactory) {
      $log.debug('Initializing NodesCtrl.');
      $scope.listInterfaces = true;
      $scope.nodes = [];
      $scope.ifaces = [];

      $scope.limit = 10;
      $scope.offset = 0;
      $scope.fetchedAll = false;

      $scope.filter = {
        type: 'both'
      };
      $scope.sort = {
        sortingOrder: 'id',
        reverse: false
      };
      $scope.gap = 3;
      $scope.itemsPerPage = 50;
      $scope.currentPage = 0;
      $scope.items = [];
      $scope.totalCount = 0;

      var nodeListResource = PagedResourceFactory.createResource('/nodes');
      nodeListResource.setLimit($scope.itemsPerPage);
      nodeListResource.setPage($scope.currentPage);
      nodeListResource.orderBy($scope.sort.sortingOrder);
      nodeListResource.order($scope.sort.reverse?'desc':'asc');

//      if ($scope.filter.type === 'current') {
//        nodeListResource.setParams({ ifRegainedService: 'null' });
//      } else if ($scope.filter.type === 'resolved') {
//        nodeListResource.setParams({ ifRegainedService: 'notnull' });
//      } else {
        nodeListResource.setParams({});
//      }

      var updateUI = function() {
        $timeout(function() {
          $log.debug('Updating the UI: ');
          nodeListResource.getCurrentResponse().then(function(results) {
            var models = ModelFactory.processResults(results);
            $scope.nodes = models.objects;
            $log.debug('Retrieved nodes (' + models.totalCount + '): ', $scope.nodes);
            $scope.totalCount = models.totalCount;
          },
          function(err) {
            $log.error('Retrieving node list failed.');
          });
        })
      };

      $scope.range = function(size, start, end) {
        var ret = [];
        //$log.debug('range(): size: '+size+', start: '+start+', end: '+end);

        if (size < end) {
          end = size;
          start = size - $scope.gap;
        }
        if (start < 0) {
          start = 0;
        }
        if (end > size) {
          end = size;
        }
        for (var i = start; i < end; i++) {
          ret.push(i);
        }
        //$log.debug('range(): ret:', ret);
        return ret;
      };

      $scope.totalPages = function() {
        return Math.ceil($scope.totalCount / $scope.itemsPerPage);
      };

      $scope.firstPage = function() {
        if ($scope.currentPage > 0) {
          $scope.currentPage = 0;
        }
      };
      $scope.lastPage = function() {
        $scope.currentPage = $scope.totalPages() - 1;
      };
      $scope.prevPage = function() {
        if ($scope.currentPage > 0) {
          $scope.currentPage--;
        }
      };
      $scope.nextPage = function() {
        if ($scope.currentPage < $scope.totalPages() - 1) {
          $log.debug('234')
          $scope.currentPage++;
        }
      };
      $scope.setPage = function() {
        $scope.currentPage = this.n;
      };


      $scope.$watch('currentPage', function(newValue, oldValue) {
        $log.debug('currentPage: newValue=',newValue);
        if (newValue !== oldValue) {
          nodeListResource.setPage(newValue);
          updateUI();
        }
      });

      updateUI();
    }])
    .controller('NodeDetailCtrl', ['$scope', '$stateParams', '$log', '$timeout', 'NodeService', 'AlarmService', 'PagedResourceFactory', 'ModelFactory', function ($scope, $stateParams, $log, $timeout, NodeService, AlarmService, PagedResourceFactory, ModelFactory) {
      $scope.currentNodeId = $stateParams.nodeId;

      $scope.node = {};

//      $scope.init = function() {
//        $scope.fetchNode($stateParams.nodeId);
//      };

      var nodeListResource = PagedResourceFactory.createResource('/nodes/' + $scope.currentNodeId);
//      nodeListResource.setLimit($scope.itemsPerPage);
//      nodeListResource.setPage($scope.currentPage);
//      nodeListResource.orderBy($scope.sort.sortingOrder);
//      nodeListResource.order($scope.sort.reverse?'desc':'asc');

//      if ($scope.filter.type === 'current') {
//        nodeListResource.setParams({ ifRegainedService: 'null' });
//      } else if ($scope.filter.type === 'resolved') {
//        nodeListResource.setParams({ ifRegainedService: 'notnull' });
//      } else {
      nodeListResource.setParams({});
//      }

      var updateUI = function() {
        $timeout(function() {
          $log.debug('Updating the UI: ');
          nodeListResource.getCurrentResponse().then(function(results) {
              var models = ModelFactory.processResults(results);
              $scope.node = models.objects;
              $log.debug('Retrieved node (' + models.totalCount + '): ', $scope.nodes);
              $scope.totalCount = models.totalCount;
              $scope.processNode();
            },
            function(err) {
              $log.error('Retrieving node list failed.');
            });
        })
      };

      updateUI();

      $scope.processInterfaces = function(ifaces) {
        $log.debug('Got node ifaces:', ifaces);
        $scope.node.ifaces = ifaces;

        $scope.node.ifaces.forEach(function(iface, index) {
          NodeService.getIpInterfaceServices($scope.node.id, iface.ipAddress).then(function(services) {
            $log.debug('Got node iface services:', services);
            $scope.node.ifaces[index].services = services;
          });
        });
      };

      $scope.processNode = function() {
        $log.debug('Got node:', $scope.node);
        //$scope.node = node;

        // Fetch interfaces.
        NodeService.getIpInterfaces($scope.node.id).then($scope.processInterfaces);

        AlarmService.getByNode($scope.node.id).then(function(alarms) {
          $log.debug('Got alarms: ', alarms);
          $scope.node.alarms = alarms;
        });
      };

      $scope.fetchNode = function(nodeId) {
        //NodeService.get(nodeId).then($scope.processNode);
      };

      /// Runtime stuff.
      //if(!$scope.isTest) {
        // When testing we don't want to initialize
        //$scope.init();
      //}

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