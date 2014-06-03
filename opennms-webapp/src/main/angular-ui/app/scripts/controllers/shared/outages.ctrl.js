(function(PluginManager) {
  'use strict';

  angular.module('opennms.controllers.shared.outages', [
    'ui.router', 'angularMoment',
    'opennms.controllers.desktop.app',
    'opennms.services.shared.outages',
    'opennms.services.shared.menu'
  ])
    .controller('OutageCtrl', ['$scope', '$log', '$filter', 'OutageService', function($scope, $log, $filter, OutageService) {
        $log.debug('Initializing OutagesCtrl.');
        $scope.$log = $log;
        $scope.items = {};

        $scope.init = function() {
          OutageService.list().then(function(outages) {
            $log.debug('Got outages:', outages);
            $scope.items = outages;
            $scope.search();
          });
        };

        $scope.filter = {
          type: 'both'
        };
        $scope.sort = {
          sortingOrder: 'id',
          reverse: false
        };
        $scope.gap = 5;
        $scope.filteredItems = [];
        $scope.groupedItems = [];
        $scope.itemsPerPage = 20;
        $scope.pagedItems = [];
        $scope.currentPage = 0;
        $scope.items = [];
        // limit: count
        // offset: (page - 1) * count
        // orderBy:
        // order:
        // comparator:
        var searchMatch = function(haystack, needle) {
          if (!needle) {
            return true;
          }
          return haystack.toLowerCase().indexOf(needle.toLowerCase()) !== -1;
        };
        // init the filtered items
        $scope.filterItems = function () {

        };
        $scope.search = function() {
          $scope.filteredItems = $filter('filter')($scope.items, function(item) {
            for (var attr in item) {
              if (searchMatch(item[attr], $scope.query))
                return true;
            }
            return false;
          });
          // take care of the sorting order
          if ($scope.sort.sortingOrder !== '') {
            $scope.filteredItems = $filter('orderBy')($scope.filteredItems, $scope.sort.sortingOrder, $scope.sort.reverse);
          }
          $scope.currentPage = 0;
          // now group by pages
          $scope.groupToPages();
        };
        // calculate page in place
        $scope.groupToPages = function() {
          $scope.pagedItems = [];
          for (var i = 0; i < $scope.filteredItems.length; i++) {
            if (i % $scope.itemsPerPage === 0) {
              $scope.pagedItems[Math.floor(i / $scope.itemsPerPage)] = [$scope.filteredItems[i]];
            } else {
              $scope.pagedItems[Math.floor(i / $scope.itemsPerPage)].push($scope.filteredItems[i]);
            }
          }
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
        $scope.firstPage = function() {
          if ($scope.currentPage > 0) {
            $scope.currentPage = 0;
          }
        };
        $scope.lastPage = function() {
          if ($scope.currentPage < $scope.pagedItems.length - 1) {
            $scope.currentPage = $scope.pagedItems.length - 1;
          }
        };
        $scope.prevPage = function() {
          if ($scope.currentPage > 0) {
            $scope.currentPage--;
          }
        };
        $scope.nextPage = function() {
          if ($scope.currentPage < $scope.pagedItems.length - 1) {
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

        $scope.init();
        $scope.search();

        $log.debug('Finished Initializing OutagesCtrl.');
      }])
    .controller('OutageDetailController', ['$scope', '$stateParams', 'OutageDetailService', function($scope, $stateParams, OutageDetailService) {
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
                templateUrl: 'templates/desktop/outages/detail.html',
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
