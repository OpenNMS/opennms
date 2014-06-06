(function(PluginManager) {
  'use strict';

  angular.module('opennms.controllers.shared.outages', [
    'ui.router', 'angularMoment',
    'opennms.controllers.desktop.app',
    'opennms.services.shared.pagedresource',
    'opennms.services.shared.outages',
    'opennms.services.shared.menu'
  ])
    .controller('OutageCtrl', ['$scope', '$log', '$timeout', '$filter', 'OutageService', 'PagedResourceFactory', function($scope, $log, $timeout, $filter, OutageService, PagedResourceFactory) {
        $log.debug('Initializing OutagesCtrl.');
        $scope.$log = $log;
        $scope.items = {};

        $scope.filter = {
          type: 'both'
        };
        $scope.sort = {
          sortingOrder: 'id',
          reverse: false
        };
        $scope.gap = 5;
        $scope.itemsPerPage = 20;
        $scope.currentPage = 0;
        $scope.items = [];
        $scope.totalCount = 0;

        var outageResource = PagedResourceFactory.createResource('/outages');
        outageResource.setLimit($scope.itemsPerPage);
        outageResource.setPage($scope.currentPage);
        outageResource.orderBy($scope.sort.sortingOrder);
        outageResource.order($scope.sort.reverse? 'desc' : 'asc');
        if ($scope.filter.type === 'current') {
          outageResource.setParams({ ifRegainedService: 'null' });
        } else if ($scope.filter.type === 'resolved') {
          outageResource.setParams({ ifRegainedService: 'notnull' });
        } else {
          outageResource.setParams({});
        }

        var updateUI = function() {
          $timeout(function() {
            $log.debug('Updating UI:');
            outageResource.getCurrentResponse().then(function(results) {
              $log.debug('results:',results);
              var items = [];
              var totalCount = 0;
              if (results && results['outages']) {
                if (results['outages']['outage']) {
                  var outage = results['outages']['outage'];
                  for (var i = 0; i < outage.length; i++) {
                    items.push(new Outage(outage[i]));
                  }
                }
                if (results['outages']['_totalCount']) {
                  totalCount = Number(results['outages']['_totalCount']);
                }
              }
              $scope.items = items;
              $scope.totalCount = totalCount;
            }, function(err) {
              $log.error("Failed to update outages list:",err);
            });
          });
        };

        $scope.$watch('itemsPerPage', function(newValue, oldValue) {
          $log.debug('itemsPerPage: newValue=',newValue);
          if (newValue !== oldValue) {
            outageResource.setLimit(newValue);
            $scope.currentPage = 0;
            updateUI();
          }
        });
        $scope.$watch('currentPage', function(newValue, oldValue) {
          $log.debug('currentPage: newValue=',newValue);
          if (newValue !== oldValue) {
            outageResource.setPage(newValue);
            updateUI();
          }
        });
        $scope.$watch('filter.type', function(newValue, oldValue) {
          $log.debug('filter.type: newValue=',newValue);
          if (newValue !== oldValue) {
            if (newValue === 'current') {
              outageResource.setParams({ ifRegainedService: 'null' });
            } else if (newValue === 'resolved') {
              outageResource.setParams({ ifRegainedService: 'notnull' });
            } else {
              outageResource.setParams({});
            }
            updateUI();
          }
        });
        $scope.$watch('sort.sortingOrder', function(newValue, oldValue) {
          $log.debug('sort.sortingOrder: newValue=',newValue);
          if (newValue !== oldValue) {
            outageResource.orderBy(newValue);
            updateUI();
          }
        });
        $scope.$watch('sort.reverse', function(newValue, oldValue) {
          if (newValue !== oldValue) {
            outageResource.order(newValue? 'desc' : 'asc');
            updateUI();
          }
        });

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
            $scope.currentPage++;
          }
        };
        $scope.setPage = function() {
          $scope.currentPage = this.n;
        };
        $scope.getStatusLabel = function(outage) {
          if (!outage.hasOwnProperty('serviceRegainedEvent') || outage.serviceRegainedEvent === null) {
            return 'DOWN';
          }
        };


        updateUI();
        //$scope.init();
        //$scope.search();

        $log.debug('Finished Initializing OutagesCtrl.');
      }])
    .controller('OutageDetailCtrl', ['$scope', '$stateParams', 'OutageDetailService', function($scope, $stateParams, OutageDetailService) {
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
          $scope.outage = OutageService.get($stateParams.id);
        };

        console.log($scope.outage);

        /// Runtime stuff.
        $scope.init();
      }])

    .config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider) {
        $stateProvider.state('app.outages', {
          url: '/outages',
          views: {
            'mainContent': {
              templateUrl: 'templates/desktop/outages/search.html'
            }
          }
        })
          .state('app.outages-list', {
            url: '/outages/list',
            views: {
              'mainContent': {
                templateUrl: 'templates/desktop/outages/list.html',
                controller: 'OutageCtrl'
              }
            }
          })
          .state('app.outages-detail', {
            url: '/outages/{outageId:\d+}',
            views: {
              'mainContent': {
                templateUrl: 'templates/desktop/outages/outage.html',
                controller: 'OutageDetailCtrl'
              }
            }
          })

          ;
      }])

    .run(['$log', 'MenuService', function($log, menu) {
        menu.add('Info', '/app/outages', 'Outages');
      }])
    ;

  PluginManager.register('opennms.controllers.shared.outages');
}(PluginManager));
