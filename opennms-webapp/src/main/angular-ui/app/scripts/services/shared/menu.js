(function() {
  'use strict';

  angular.module('opennms.services.shared.menu', [
        'ui.router'
    ])

  .factory('MenuService', ['$log', function($log) {
        var _menuData = {};

        var getMenuEntries = function() {
            return angular.copy(_menuData);
        };

        var addMenuItem = function(location, route, label) {
            if (route === undefined || label === undefined) {
                $log.warn('Cannot add menu item, route or label undefined. (location=' + location + ', route=' + route + ', label=' + label + ')');
                return;
            }
            if (location === '') {
                location = undefined;
            }
            
            $log.warn('location=',location);
            $log.warn('route=',route);
            $log.warn('label=',label);

            var menuEntry = {
                name: label,
                route: route
            };
            if (location === undefined) {
                _menuData[label] = menuEntry;
            } else {
                if (!_menuData[location]) {
                    _menuData[location] = {
                        name: location,
                        entries: []
                    };
                }
                _menuData[location].entries.push(menuEntry);
            }
        };

        return {
            get: getMenuEntries,
            add: addMenuItem
        };
  }])

  ;
}());
