(function() {
  'use strict';

  angular.module('opennms.services.shared.networkDashboard', [
    'opennms.services.shared.config'
  ])

    .factory('NetworkDashboardService', ['$q', '$log', '$http', 'ConfigService', function($q, $log, $http, config) {
        $log.debug('NetworkDashboardService Initializing.');

        return {
        };
      }])
    ;
}());