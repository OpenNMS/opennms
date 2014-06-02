(function() {
  'use strict';

  angular.module('opennms.controllers.shared.menu', [
        'opennms.services.shared.menu'
  ])

  .controller('MenuCtrl', ['$log', '$scope', 'MenuService', function($log, $scope, menu) {
        //$log.info('menu data=', menu.get());
        $scope.menuItems = menu.get();
    }])

  ;
}());