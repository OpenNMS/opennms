(function() {
  'use strict';

  angular.module('opennms.services.shared.wallboard', [
    'opennms.services.shared.config'
  ])

    .factory('WallboardService', ['$q', '$log', '$http', 'ConfigService', function($q, $log, $http, config) {
        $log.debug('WallboardService Initializing.');

        var getBoard = function(url) {
          var boardUrl = config.getRoot() + url;
          $log.debug('getBoard: GET ' + boardUrl);

          var deferred = $q.defer();
          $http({
            'method': 'GET',
            'url': boardUrl,
            params: {
              'quiet': 'true',
              'nonavbar': 'true'
            }
          }).success(function(data, status, headers, config) {
            deferred.resolve(data);
          }).error(function(data, status, headers, config) {

          });
          return deferred.promise;
        };

        var getUrl = function(url, params) {
          var boardUrl = config.getRoot() + url;

          var str = Object.keys(params).map(function(key) {
            return encodeURIComponent(key) + '=' + encodeURIComponent(params[key]);
          }).join('&');
          
          $log.debug('getURL: GET ' + boardUrl + '?' + str);
          return boardUrl+'?'+str;
        };

        return {
          'board': getBoard,
          'url': getUrl
        };
      }])

    ;
}());