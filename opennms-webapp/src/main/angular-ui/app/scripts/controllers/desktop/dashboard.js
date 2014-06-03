(function(PluginManager) {
  'use strict';

  angular.module('opennms.controllers.desktop.dashboard', [
    'ui.router',
    'opennms.controllers.desktop.app',
    'opennms.services.shared.alarms',
    'opennms.services.shared.outages'
  ])

  .filter('to_trusted', ['$sce', function($sce){
    return function(text) {
      return $sce.trustAsHtml(text);
    };
  }])

  .filter('fuzzy', [function() {
    /* global moment: true */

    moment.lang('en', {
      relativeTime : {
        future: "in %s",
        past: "%s ago",
        s:  "seconds",
        m:  "1 minute",
        mm: "%d minutes",
        h:  "1 hour",
        hh: "%d hours",
        d:  "1 day",
        dd: "%d days",
        M:  "1 month",
        MM: "%d months",
        y:  "1 year",
        yy: "%d years"
      }
    });

    return function(date) {
      return moment(date).fromNow(true);
    };
  }])

    .controller('DashboardCtrl', ['$log', '$scope', 'AlarmService', 'OutageService', function($log, $scope, alarms, outages) {
    alarms.summaries().then(function(alarms) {
      $log.debug('Got summaries:',alarms);
      $scope.alarms = alarms;
    });
    outages.summaries().then(function(outages) {
      $log.debug('Got outages:',outages);
      $scope.outages = outages;
    });
  }])

  .config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider) {
    $stateProvider.state('app.dashboard', {
      url: '/dashboard',
      views: {
        'mainContent': {
          templateUrl: 'templates/desktop/dashboard.html',
          controller: 'DashboardCtrl'
        }
      }
    });
    $urlRouterProvider.otherwise('/app/dashboard');
  }])
  ;

  PluginManager.register('opennms.controllers.desktop.dashboard');
}(PluginManager));
