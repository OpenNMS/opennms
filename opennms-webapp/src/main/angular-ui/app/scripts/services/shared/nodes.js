(function() {
  'use strict';

  angular.module('opennms.services.shared.nodes', [
    'opennms.services.shared.config'
  ])

    .factory('NodeService', ['$q', '$log', '$http', 'ConfigService', function($q, $log, $http, config) {
      /* global X2JS: true */
      var x2js = new X2JS();

      var getNode = function(id) {
        var nodeUrl = config.getRoot() + '/rest/nodes/'+ id;
        $log.debug('getNode: GET ' + nodeUrl);

        var deferred = $q.defer();
        $http({
          'method': 'GET',
          'url': nodeUrl,
          'headers': {
            'Accept': 'application/xml'
          }
        }).success(function(data, status, headers, config) {
          var results = x2js.xml_str2json(data);
          var nodes = [];
          if (results && results.nodes && results.nodes.node) {
            nodes = results.nodes.node;
          }
          deferred.resolve(nodes);
        }).error(function(data, status, headers, config) {
          $log.error('GET ' + nodeUrl + ' failed:', data, status);
          deferred.reject(status);
        });
        return deferred.promise;
      };

      var getNodes = function(offset, limit) {
        if (limit === undefined) {
          limit = 50;
        }
        if (offset === undefined) {
          offset = 0;
        }

        var nodesUrl = config.getRoot() + '/rest/nodes?limit=' + limit + '&offset=' + offset;
        $log.debug('getNodes: GET ' + nodesUrl);

        var deferred = $q.defer();
        $http({
          'method': 'GET',
          'url': nodesUrl,
          'headers': {
            'Accept': 'application/xml'
          }
        }).success(function(data, status, headers, config) {
          var results = x2js.xml_str2json(data);
          var nodes = [];
          if (results && results.nodes && results.nodes.node) {
            nodes = results.nodes.node;
          }
          deferred.resolve(nodes);
        }).error(function(data, status, headers, config) {
          $log.error('GET ' + nodesUrl + ' failed:', data, status);
          deferred.reject(status);
        });
        return deferred.promise;
      };

      return {
        'list': getNodes,
        'get': getNode
      };
    }])

  ;
}());
