(function(PluginManager) {
  'use strict';

  /* Filters */

  angular.module('opennms.filters.global', [])
    .filter('interpolate', ['serviceStatus', function() {
        return function(status) {
          var status_map = {
            'A': 'Managed',
            'U': 'Unmanaged',
            'D': 'Deleted',
            'F': 'Forced Unmanaged',
            'N': 'Not Monitored',
            'R': 'Rescan to Resume',
            'S': 'Rescan to Suspend',
            'X': 'Remotely Monitored'
          };
          if (status_map.hasOwnProperty(status)) {
            return status_map[status];
          }
          return '';
        };
      }])
    .filter('escape', function() {
      return window.escape;
    })
    ;

  PluginManager.register('opennms.filters.global');
}(PluginManager));
