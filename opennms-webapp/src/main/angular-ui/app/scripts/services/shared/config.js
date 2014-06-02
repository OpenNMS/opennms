(function() {
  'use strict';

  angular.module('opennms.services.shared.config', [
  ])

  .factory('ConfigService', ['$log', '$location', function($log, $location) {
    return {
      'getRoot': function() {
        return '/opennms';
      }
    };
  }])

  ;
}());
