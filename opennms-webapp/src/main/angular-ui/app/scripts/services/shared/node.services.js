(function() {
  'use strict';

  angular.module('opennms.services.shared.nodes', [
    'opennms.services.shared.config'
  ])
    /**
     * @ngdoc service
     * @name NodeService
     *
     * @description The NodeService provides components with access to the OpenNMS nodes REST resource.
     */
    .factory('NodeService', ['$q', '$log', '$http', 'ConfigService', function($q, $log, $http, config) {
      $log.debug('NodeService Initializing.');
      /* global X2JS: true */
      var x2js = new X2JS();

      var nodeService = new Object();
      nodeService.internal = new Object();

      nodeService.getIpInterfaces = function(id) {
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

      nodeService.getIpInterfaceServices = function(nodeId, ipAddress) {
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

      nodeService.get = function(id) {
        var nodeUrl = config.getRoot() + '/rest/nodes/'+ id;
        $log.debug('getNode: GET ' + nodeUrl);

        return nodeService.internal.fetchNode(nodeUrl);
      };

      nodeService.internal.fetchNode = function(nodeUrl) {
        var deferred = $q.defer();
        $http({
          'method': 'GET',
          'url': nodeUrl,
          'headers': {
            'Accept': 'application/xml'
          }
        }).success(nodeService.internal.getNodeSuccessHandler(deferred)).error(function(data, status, headers, config) {
          $log.error('GET ' + nodeUrl + ' failed:', data, status);
          deferred.reject(status);
        });
        return deferred.promise;
      };

      nodeService.internal.getNodeSuccessHandler = function(deferred) {
        var handler = function(data, status, headers, config) {
          var results = x2js.xml_str2json(data);
          var node = nodeService.internal.processNode(results);

          deferred.resolve(node);
        };

        return handler;
      };

      nodeService.internal.processNode = function(results) {
        var node = {}
        if (results && results.node) {
          node = results.node;
          node._id = parseInt(node._id);
        }

        return node;
      };

      nodeService.list = function(offset, limit) {
        if (limit === undefined) {
          limit = 50;
        }
        if (offset === undefined) {
          offset = 0;
        }

        var nodesUrl = config.getRoot() + '/rest/nodes?limit=' + limit + '&offset=' + offset;
        $log.debug('getNodes: GET ' + nodesUrl);

        return nodeService.internal.fetchNodes(nodesUrl)
      };

      nodeService.internal.fetchNodes = function(nodesUrl) {
        var deferred = $q.defer();
        $http({
          'method': 'GET',
          'url': nodesUrl,
          'headers': {
            'Accept': 'application/xml'
          }
        }).success(nodeService.internal.getNodeListSuccessHandler(deferred))
          .error(nodeService.internal.getNodeListErrorHandler(deferred));
        return deferred.promise;
      };

      nodeService.internal.getNodeListErrorHandler = function(deferred) {
        var handler = function(data, status, headers, config) {
          $log.error('GET ' + nodesUrl + ' failed:', data, status);
          deferred.reject(status);
        };

        return handler;
      }
      nodeService.internal.getNodeListSuccessHandler = function(deferred) {
        var handler = function(data, status, headers, config) {
          var results = x2js.xml_str2json(data);
          var nodes = nodeService.internal.processNodes(results);

          deferred.resolve(nodes);
        };

        return handler;
      };

      nodeService.internal.processNodes = function(results) {
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
        return nodes;
      };
      return nodeService;
//      return {
//        'list': getNodes,
//        'get': getNode,
//        'getIpInterfaces': getIpInterfaces,
//        'getIpInterfaceServices': getIpInterfaceServices
//      };
    }])

  ;
}());