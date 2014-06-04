(function() {
  'use strict';

  angular.module('opennms.services.shared.nodes', [
    'opennms.services.shared.config'
  ])

    .factory('NodeService', ['$q', '$log', '$http', 'ConfigService', function($q, $log, $http, config) {
      $log.debug('NodeService Initializing.');
      /* global X2JS: true */
      var x2js = new X2JS();

      var getIpInterfaces = function(id) {
        var ipUrl = config.getRoot() + '/rest/nodes/' + id + '/ipinterfaces';
        $log.debug('getIpInterfaces: GET ' + ipUrl);

        var deferred = $q.defer();
        $http({
          'method': 'GET',
          'url': ipUrl,
          'headers': {
            'Accept': 'application/xml'
          }
        }).success(function(data, status, headers, config) {
          var results = x2js.xml_str2json(data);
          var ifaces = [];
          if (results && results.ipInterfaces && results.ipInterfaces.ipInterface) {
            if(angular.isArray(results.ipInterfaces.ipInterface)) {
              ifaces = results.ipInterfaces.ipInterface;
            } else {
              ifaces.push(results.ipInterfaces.ipInterface);
            }

            for (var i=0; i < ifaces.length; i++) {
              ifaces[i]['_id']      = parseInt(ifaces[i]['_id']);
              ifaces[i]['_ifIndex'] = parseInt(ifaces[i]['_ifIndex']);
              ifaces[i].nodeId      = parseInt(ifaces[i].nodeId);
            }
          }
          deferred.resolve(ifaces);
        }).error(function(data, status, headers, config) {

        });
        return deferred.promise;
      };

      var getIpInterfaceServices = function(nodeId, ipAddress) {
        var ipsUrl = config.getRoot() + '/rest/nodes/' + nodeId + '/ipinterfaces/' + ipAddress + '/services';
        $log.debug('getIpInterfaceServices: GET ' + ipsUrl);

        var deferred = $q.defer();
        $http({
          'method': 'GET',
          'url': ipsUrl,
          'headers': {
            'Accept': 'application/xml'
          }
        }).success(function(data, status, headers, config) {
          var results = x2js.xml_str2json(data);
          var ifaceServices = [];
          if (results && results.services && results.services.service) {
            if(angular.isArray(results.services.service)) {
              ifaceServices = results.services.service;
            } else {
              ifaceServices.push(results.services.service);
            }

//            for (var i=0; i < ifaces.length; i++) {
//              ifaces[i]['_id']      = parseInt(ifaces[i]['_id']);
//              ifaces[i]['_ifIndex'] = parseInt(ifaces[i]['_ifIndex']);
//              ifaces[i].nodeId      = parseInt(ifaces[i].nodeId);
//            }
          }
          deferred.resolve(ifaceServices);
        }).error(function(data, status, headers, config) {

        });
        return deferred.promise;
      };

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
          var node = undefined;
          if (results && results.node) {
            node = results.node;
            node._id = parseInt(node._id);
          }
          deferred.resolve(node);
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
            if(!angular.isArray(results.nodes.node)) {
              nodes.push(results.nodes.node);
            } else {
              nodes = results.nodes.node;
            }
            for (var i = 0; i < nodes.length; i++) {
              nodes[i]['_id'] = parseInt(nodes[i]['_id']);
            }
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
        'get': getNode,
        'getIpInterfaces': getIpInterfaces,
        'getIpInterfaceServices': getIpInterfaceServices
      };
    }])

  ;
}());