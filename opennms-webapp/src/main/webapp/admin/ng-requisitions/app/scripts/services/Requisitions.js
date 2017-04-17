/*global RequisitionsData:true, Requisition:true, RequisitionNode:true */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

// http://jsfiddle.net/zMjVp/

(function() {

  'use strict';

  angular.module('onms-requisitions')

  /**
  * @ngdoc service
  * @name RequisitionsService
  * @module onms-requisitions
  *
  * @requires $q Angular promise/deferred implementation
  * @requires $cacheFactory Angular cache management
  * @requires $window Document window
  * @requires $http Angular service that facilitates communication with the remote HTTP servers
  * @requires $timeout Angular service that facilitates timeout functions
  * @requires $log Angular log facility
  *
  * @description The RequisitionsService provides all the required methods to access ReST API for the OpenNMS requisitions.
  *
  * It uses Angular's Cache service to store localy all the requisitions after retrieving them from the server the first time.
  * This helps in terms of performance and responsiveness of the UI. Only changes are pushed back to the server.
  *
  * Conflicts may accour if someone else is changing the requisitions at the same time.
  *
  * If the cache is not going to be used, the controllers are responsible for maintaining the state of the data.
  */
  .factory('RequisitionsService', ['$q', '$cacheFactory', '$window', '$http', '$timeout', '$log', function($q, $cacheFactory, $window, $http, $timeout, $log) {

    $log.debug('Initializing RequisitionsService');

    var requisitionsService = {};
    requisitionsService.internal = {};

    // Declaring Service Variables

    var baseHref = $window.ONMS_BASE_HREF === undefined ? '' : $window.ONMS_BASE_HREF;
    $log.debug('baseHref = "' + baseHref + '"');

    // Cache Configuration

    requisitionsService.internal.cacheEnabled = true;
    requisitionsService.internal.cache = $cacheFactory('RequisitionsService');

    // URLs

    requisitionsService.internal.requisitionsUrl = baseHref + 'rest/requisitions';
    requisitionsService.internal.requisitionNamesUrl = baseHref + 'rest/requisitionNames';
    requisitionsService.internal.foreignSourcesUrl = baseHref + 'rest/foreignSources';
    requisitionsService.internal.foreignSourcesConfigUrl = baseHref + 'rest/foreignSourcesConfig';
    requisitionsService.internal.snmpConfigUrl = baseHref + 'rest/snmpConfig';
    requisitionsService.internal.errorHelp = ' Check the OpenNMS logs for more details, or try again later.';

    // Timeouts

    requisitionsService.internal.defaultTimeout = 3; // Time to wait in seconds
    requisitionsService.internal.timingStatus = { isRunning: false };

    /**
    * @description (Internal) Gets the data from the internal cache
    *
    * @private
    * @name RequisitionsService:internal.getCatchedConfigData
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} configName The name of the config object
    * @returns {object} the internal cache content
    */
    requisitionsService.internal.getCatchedConfigData = function(configName) {
      return requisitionsService.internal.cache.get(configName);
    };

    /**
    * @description (Internal) Saves the data into internal cache
    *
    * @private
    * @name RequisitionsService:internal.setCatchedConfigData
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} configName The name of the config object
    * @param {object} configObject The config object
    */
    requisitionsService.internal.setCatchedConfigData = function(configName, configObject) {
      if (requisitionsService.internal.cacheEnabled) {
        requisitionsService.internal.cache.put(configName, configObject);
      }
    };

    /**
    * @description (Internal) Gets the requisitions from the internal cache
    *
    * @private
    * @name RequisitionsService:internal.getCatchedConfigData
    * @ngdoc method
    * @methodOf RequisitionsService
    * @returns {object} the internal cache content
    */
    requisitionsService.internal.getCachedRequisitionsData = function() {
      return requisitionsService.internal.getCatchedConfigData('requisitionsData');
    };

    /**
    * @description (Internal) Saves the requisitions data into internal cache
    *
    * @private
    * @name RequisitionsService:internal.setCachedRequisitionsData
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {object} requisitionsData The requisitions data
    */
    requisitionsService.internal.setCachedRequisitionsData = function(requisitionsData) {
      return requisitionsService.internal.setCatchedConfigData('requisitionsData', requisitionsData);
    };

    /**
    * @description (Internal) Gets a specific requisition object from the cache.
    *
    * @private
    * @name RequisitionsService:internal.getCachedRequisition
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} foreignSource The requisition's name (a.k.a. foreign source)
    * @returns {object} the requisition object.
    */
    requisitionsService.internal.getCachedRequisition = function(foreignSource) {
      var requisitionsData = requisitionsService.internal.getCachedRequisitionsData();
      if (requisitionsData == null) {
        return null;
      }
      return requisitionsData.getRequisition(foreignSource);
    };

    /**
    * @description (Internal) Gets a specific node object from the cache.
    *
    * @private
    * @name RequisitionsService:internal.getCachedNode
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} foreignSource The requisition's name (a.k.a. foreign source)
    * @param {string} foreignId The foreign Id
    * @returns {object} the node object.
    */
    requisitionsService.internal.getCachedNode = function(foreignSource, foreignId) {
      var requisition = requisitionsService.internal.getCachedRequisition(foreignSource);
      if (requisition == null) {
        return null;
      }
      return requisition.getNode(foreignId);
    };

    /**
    * @description (Internal) Quick-Add a new node to an existing requisition
    *
    * @private
    * @name RequisitionsService:addQuickNode
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {object} node the QuickNode object
    */
    requisitionsService.internal.addQuickNode = function(quickNode) {
      var deferred = $q.defer();
      var node = quickNode.createRequisitionedNode();

      requisitionsService.saveNode(node).then(
        function() { // saveNode:success
          $log.debug('addQuickNode: the node ' + node.nodeLabel + ' has been saved.');
          requisitionsService.synchronizeRequisition(node.foreignSource, 'false').then(
            function() { // synchronizeRequisition:success
              $log.debug('addQuickNode: the requisition ' + node.foreignSource + ' has been synchronized.');
              deferred.resolve(node);
            },
            function() { // synchronizeRequisition:failure
              deferred.reject('Cannot synchronize requisition ' + node.foreignSource);
            }
          );
        },
        function() { // saveNode:failure
          deferred.reject('Cannot quick-add node to requisition ' + node.foreignSource);
        }
      );

      return deferred.promise;
    };

    /**
    * @description (Internal) Updates a Requisition object based on a Deployed Statistics Object.
    *
    * @private
    * @name RequisitionsService:updateRequisition
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {object} existingReq the existing requisition object
    * @param {object} deployedReq the deployed statistics object
    */
    requisitionsService.internal.updateRequisition = function(existingReq, deployedReq) {
      $log.debug('updateRequisition: updating deployed statistics for requisition ' + deployedReq.name + '.');
      var foreignIds = deployedReq['foreign-id'];
      existingReq.nodesInDatabase = foreignIds.length;
      existingReq.deployed = foreignIds.length > 0;
      existingReq.lastImport = deployedReq['last-imported'];
      for (var idx = 0; idx < foreignIds.length; idx++) {
        var existingNode = existingReq.getNode(foreignIds[idx]);
        if (existingNode != null) {
          existingNode.deployed = true;
        }
      }
    };

    /**
    * @description Clears all the internal cache.
    *
    * This forces the service to retrieve the data from the server on next request.
    *
    * @name RequisitionsService:internal.clearCache
    * @ngdoc method
    * @methodOf RequisitionsService
    */
    requisitionsService.clearCache = function() {
      $log.debug('clearCache: removing everything from the internal cache');
      requisitionsService.internal.cache.removeAll();
    };

    /**
    * @description Removes a specific requisition from the internal cache
    *
    * This forces the service to retrieve the data from the server on next request.
    *
    * @name RequisitionsService:internal.removeRequisitionFromCache
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} foreignSource The requisition's name (a.k.a. foreignSource)
    */
    requisitionsService.removeRequisitionFromCache = function(foreignSource) {
      var requisitionsData = requisitionsService.internal.getCachedRequisitionsData();
      if (requisitionsData != null) {
        var reqIdx = requisitionsData.indexOf(foreignSource);
        if (reqIdx >= 0) {
          $log.debug('clearRequisitionCache: removing requisition ' + foreignSource + ' from the internal cache');
          requisitionsData.requisitions.splice(reqIdx, 1);
        }
      }
    };

    /**
    * @description Gets the timing status object
    * The reason for using this is because of NMS-7872.
    *
    * @name RequisitionsService:startTiming
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {integer} ts The timeout in seconds (optional)
    * @returns {object} the timing status object
    */
    requisitionsService.startTiming = function(ts) {
      if (ts == null || ts == undefined) {
        ts = requisitionsService.internal.defaultTimeout;
      }
      $log.debug('startTiming: starting timeout of ' + ts + ' seconds.');
      requisitionsService.internal.timingStatus.isRunning = true;
      $timeout(function() {
        requisitionsService.internal.timingStatus.isRunning = false;
      }, ts * 1000);
    };

    /**
    * @description Gets the timing status object
    *
    * @name RequisitionsService:getTiming
    * @ngdoc method
    * @methodOf RequisitionsService
    * @returns {object} the timing status object
    */
    requisitionsService.getTiming = function() {
      return requisitionsService.internal.timingStatus;
    };

    /**
    * @description Requests all the requisitions (pending and deployed) from OpenNMS.
    *
    * If the data exists on the cache, that will be used instead of retrieving the data
    * from the OpenNMS server.
    *
    * After retrieving the requisitions, the deployed statistics will be retrieved, and the
    * statistics of the requisitions will be updated. Then, the data will be saved on the
    * internal cache.
    *
    * @name RequisitionsService:getRequisitions
    * @ngdoc method
    * @methodOf RequisitionsService
    * @returns {object} a promise. On success, it provides a RequisitionsData object.
    */
    requisitionsService.getRequisitions = function() {
      var deferred = $q.defer();

      var requisitionsData = requisitionsService.internal.getCachedRequisitionsData();
      if (requisitionsData != null) {
        $log.debug('getRequisitions: returning a cached copy of the requisitions data');
        deferred.resolve(requisitionsData);
        return deferred.promise;
      }

      var url = requisitionsService.internal.requisitionsUrl;
      $log.debug('getRequisitions: retrieving requisitions.');
      $http.get(url)
      .success(function(data) {
        var requisitionsData = new RequisitionsData();
        angular.forEach(data['model-import'], function(onmsRequisition) {
          var requisition = new Requisition(onmsRequisition, false);
          $log.debug('getRequisitions: adding requisition ' + requisition.foreignSource + '.');
          requisitionsData.requisitions.push(requisition);
        });
        requisitionsService.updateDeployedStats(requisitionsData).then(
          function() { // success;
            requisitionsService.internal.setCachedRequisitionsData(requisitionsData);
            deferred.resolve(requisitionsData);
          },
          function(error) { // error
            deferred.reject(error);
          }
        );
      })
      .error(function(error, status) {
        $log.error('getRequisitions: GET ' + url + ' failed:', error, status);
        deferred.reject('Cannot retrieve the requisitions.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Gets the requisition names.
    *
    * If the data exists on the cache, that will be used instead of retrieving the data
    * from the OpenNMS server.
    *
    * After retrieving the requisitions, the data will be saved on the internal cache.
    *
    * @name RequisitionsService:getRequisitionNames
    * @ngdoc method
    * @methodOf RequisitionsService
    * @returns {object} a promise. On success, it provides a list of requisition names.
    */
    requisitionsService.getRequisitionNames = function() {
      var deferred = $q.defer();

      var config = requisitionsService.internal.getCatchedConfigData('requisitionNames');
      if (config != null) {
        $log.debug('getRequisitionNames: returning a cached copy of requisition names');
        deferred.resolve(config);
        return deferred.promise;
      }

      var url = requisitionsService.internal.requisitionNamesUrl;
      $log.debug('getRequisitionNames: getting requisition names');
      $http.get(url)
      .success(function(data) {
        $log.debug('getRequisitionNames: got requisition names');
        requisitionsService.internal.setCatchedConfigData('requisitionNames', data['foreign-source']);
        deferred.resolve(data['foreign-source']);
      })
      .error(function(error, status) {
        $log.error('getRequisitionNames: GET ' + url + ' failed:', error, status);
        deferred.reject('Cannot retrieve requisition names.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Updates the requisitions data object with the deployed statistics.
    *
    * After retrieving the data, the provided object will be updated.
    *
    * @name RequisitionsService:updateDeployedStats
    * @ngdoc method
    * @param {object} requisitionsData The requisitions data object
    * @methodOf RequisitionsService
    * @returns {object} a promise. On success, it provides the updated RequisitionsData object.
    */
    requisitionsService.updateDeployedStats = function(requisitionsData) {
      var deferred = $q.defer();
      var url = requisitionsService.internal.requisitionsUrl + '/deployed/stats';
      $log.debug('updateDeployedStats: retrieving deployed statistics.');
      $http.get(url)
      .success(function(data) {
        angular.forEach(requisitionsData.requisitions, function(existingReq) {
          var deployedReq = null;
          angular.forEach(data['foreign-source'], function(r) {
            if (r.name == existingReq.foreignSource) {
              deployedReq = r;
            }
          });
          if (deployedReq == null) {
            existingReq.setDeployed(false);
          } else {
            requisitionsService.internal.updateRequisition(existingReq, deployedReq);
          }
        });
        deferred.resolve(requisitionsData);
      })
      .error(function(error, status) {
        $log.error('updateDeployedStats: GET ' + url + ' failed:', error, status);
        deferred.reject('Cannot retrieve the deployed statistics.' + requisitionsService.internal.errorHelp);
      });
      return deferred.promise;
    };

    /**
    * @description Updates the requisition object with the deployed statistics.
    *
    * After retrieving the data, the provided object will be updated.
    *
    * @name RequisitionsService:updateDeployedStatsForRequisition
    * @ngdoc method
    * @param {object} requisition The requisition object
    * @methodOf RequisitionsService
    * @returns {object} a promise. On success, it provides a Requisition object.
    */
    requisitionsService.updateDeployedStatsForRequisition = function(existingReq) {
      var deferred = $q.defer();
      var url = requisitionsService.internal.requisitionsUrl + '/deployed/stats/' + encodeURIComponent(existingReq.foreignSource);
      $log.debug('updateDeployedStatsForRequisition: retrieving deployed statistics for requisition ' + existingReq.foreignSource);
      $http.get(url)
      .success(function(deployedReq) {
        requisitionsService.internal.updateRequisition(existingReq, deployedReq);
        deferred.resolve(existingReq);
      })
      .error(function(error, status) {
        $log.error('updateDeployedStatsForRequisition: GET ' + url + ' failed:', error, status);
        deferred.reject('Cannot retrieve the deployed statistics for requisition ' + existingReq.foreignSource + '. ' + requisitionsService.internal.errorHelp);
      });
      return deferred.promise;
    };

    /**
    * @description Request a sepcific requisition from OpenNMS.
    *
    * If the data exists on the cache, that will be used instead of retrieving the data
    * from the OpenNMS server.
    *
    * After retrieving the requisitions, the data will be saved on the internal cache.
    *
    * @name RequisitionsService:getRequisition
    * @ngdoc method
    * @param {string} foreignSource The requisition's name (a.k.a. foreignSource)
    * @methodOf RequisitionsService
    * @returns {object} a promise. On success, it provides a Requisition object.
    */
    requisitionsService.getRequisition = function(foreignSource) {
      var deferred = $q.defer();

      var requisition = requisitionsService.internal.getCachedRequisition(foreignSource);
      if (requisition != null) {
        $log.debug('getRequisition: returning a cached copy of ' + foreignSource);
        deferred.resolve(requisition);
        return deferred.promise;
      }

      var url = requisitionsService.internal.requisitionsUrl + '/' + encodeURIComponent(foreignSource);
      $log.debug('getRequisition: getting requisition ' + foreignSource);
      $http.get(url)
      .success(function(data) {
        var requisition = new Requisition(data);
        $log.debug('getRequisition: got requisition ' + foreignSource);
        var requisitionsData = requisitionsService.internal.getCachedRequisitionsData();
        if (requisitionsData != null) {
          $log.debug('getRequisition: updating cache for requisition ' + foreignSource);
          requisitionsData.setRequisition(requisition);
        }
        deferred.resolve(requisition);
      })
      .error(function(error, status) {
        $log.error('getRequisition: GET ' + url + ' failed:', error, status);
        deferred.reject('Cannot retrieve the requisition ' + foreignSource + '.' + requisitionsService.internal.errorHelp);
      });
      return deferred.promise;
    };

    /**
    * @description Request the synchronization/import of a requisition on the OpenNMS server.
    *
    * If the data exists on the cache, and the provided foreign source doesn't exist, the
    * request will be rejected.
    *
    * After retrieving the requisitions, the data on the internal cache will be updated.
    *
    * @name RequisitionsService:synchronizeRequisition
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} foreignSource The requisition's name (a.k.a. foreign source)
    * @param {string} rescanExisting [true, false, dbonly]
    * @returns {object} a promise.
    */
    requisitionsService.synchronizeRequisition = function(foreignSource, rescanExisting) {
      var deferred = $q.defer();

      var requisitionsData = requisitionsService.internal.getCachedRequisitionsData();
      if (requisitionsData != null) {
        var reqIdx = requisitionsData.indexOf(foreignSource);
        if (reqIdx < 0) {
          deferred.reject('The foreignSource ' + foreignSource + ' does not exist.');
          return deferred.promise;
        }
      }

      var url = requisitionsService.internal.requisitionsUrl + '/' + encodeURIComponent(foreignSource) + '/import';
      $log.debug('synchronizeRequisition: synchronizing requisition ' + foreignSource + ' with rescanExisting=' + rescanExisting);
      $http({ method: 'PUT', url: url, params: { rescanExisting: rescanExisting }})
      .success(function(data) {
        $log.debug('synchronizeRequisition: synchronized requisition ' + foreignSource);
        var r = requisitionsService.internal.getCachedRequisition(foreignSource);
        if (r != null) {
          $log.debug('synchronizeRequisition: updating deployed status of requisition ' + foreignSource);
          r.setDeployed(true);
        }
        deferred.resolve(data);
      })
      .error(function(error, status) {
        $log.error('synchronizeRequisition: PUT ' + url + ' failed:', error, status);
        deferred.reject('Cannot synchronize the requisition ' + foreignSource + '.' + requisitionsService.internal.errorHelp);
      });
      return deferred.promise;
    };

    /**
    * @description Request the creation of a new requisition on the OpenNMS server.
    *
    * If the data exists on the cache, and the provided foreign source exist, the
    * request will be rejected, because a foreign source must be unique.
    *
    * After retrieving the requisitions, the data on the internal cache will be updated.
    *
    * @name RequisitionsService:addRequisition
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} foreignSource The requisition's name (a.k.a. foreign source)
    * @returns {object} a promise. On success, it provides a Requisition object.
    */
    requisitionsService.addRequisition = function(foreignSource) {
      var deferred = $q.defer();

      var req = requisitionsService.internal.getCachedRequisition(foreignSource);
      if (req != null) {
        deferred.reject('Invalid foreignSource ' + foreignSource + ', it already exist.');
        return deferred.promise;
      }

      var emptyReq = { 'foreign-source': foreignSource, node: [] };
      var url = requisitionsService.internal.requisitionsUrl;
      $log.debug('addRequisition: adding requisition ' + foreignSource);
      $http.post(url, emptyReq)
      .success(function() {
        var requisition = new Requisition(emptyReq, false);
        $log.debug('addRequisition: added requisition ' + requisition.foreignSource);
        var data = requisitionsService.internal.getCachedRequisitionsData();
        if (data != null) {
          $log.debug('addRequisition: pushing requisition ' + foreignSource + ' into the internal cache');
          data.requisitions.push(requisition);
        }
        deferred.resolve(requisition);
      }).error(function(error, status) {
        $log.error('addRequisition: POST ' + url + ' failed:', error, status);
        deferred.reject('Cannot add the requisition ' + foreignSource + '.' + requisitionsService.internal.errorHelp);
      });
      return deferred.promise;
    };

    /**
    * @description Request the deletion of a new requisition on the OpenNMS server.
    *
    * If the data exists on the cache, and the provided foreign source doesn't exist, the
    * request will be rejected. Also, if the requisition exist and it contains nodes (i.e.
    * it is not empty), the request will be rejected.
    *
    * After retrieving the requisitions, the data on the internal cache will be updated.
    *
    * @name RequisitionsService:deleteRequisition
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} foreignSource The requisition's name (a.k.a. foreign source)
    * @returns {object} a promise.
    */
    requisitionsService.deleteRequisition = function(foreignSource) {
      var deferred = $q.defer();

      var requisitionsData = requisitionsService.internal.getCachedRequisitionsData();
      if (requisitionsData != null) {
        var reqIdx = requisitionsData.indexOf(foreignSource);
        if (reqIdx < 0) {
          deferred.reject('The foreignSource ' + foreignSource + ' does not exist.');
          return deferred.promise;
        }
        var req = requisitionsData.requisitions[reqIdx];
        if (req.nodesInDatabase > 0) {
          deferred.reject('The foreignSource ' + foreignSource + ' contains ' + req.nodesInDatabase + ' nodes on the database, it cannot be deleted.');
          return deferred.promise;
        }
      }

      $log.debug('deleteRequisition: deleting requisition ' + foreignSource);
      var deferredReqPending  = $http.delete(requisitionsService.internal.requisitionsUrl + '/' + encodeURIComponent(foreignSource));
      var deferredReqDeployed = $http.delete(requisitionsService.internal.requisitionsUrl + '/deployed/' + encodeURIComponent(foreignSource));
      var deferredFSPending  = $http.delete(requisitionsService.internal.foreignSourcesUrl + '/' + encodeURIComponent(foreignSource));
      var deferredFSDeployed = $http.delete(requisitionsService.internal.foreignSourcesUrl + '/deployed/' + encodeURIComponent(foreignSource));

      $q.all([ deferredReqPending, deferredReqDeployed, deferredFSPending, deferredFSDeployed ])
      .then(function(results) {
        $log.debug('deleteRequisition: deleted requisition ' + foreignSource);
        requisitionsService.removeRequisitionFromCache(foreignSource);
        deferred.resolve(results);
      }, function(error, status) {
        $log.error('deleteRequisition: DELETE operation failed:', error, status);
        deferred.reject('Cannot delete the requisition ' + foreignSource + '.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Request the removal of all from an existing requisition on the OpenNMS server.
    *
    * If the data exists on the cache, and the provided foreign source doesn't exist, the
    * request will be rejected.
    *
    * After retrieving the requisitions, the data on the internal cache will be updated.
    * After updating the requisition, a synchronization with rescanExisting=false will be performed.
    *
    * @name RequisitionsService:removeAllNodesFromRequisition
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} foreignSource The requisition's name (a.k.a. foreign source)
    * @returns {object} a promise.
    */
    requisitionsService.removeAllNodesFromRequisition = function(foreignSource) {
      var deferred = $q.defer();

      var requisitionsData = requisitionsService.internal.getCachedRequisitionsData();
      if (requisitionsData != null) {
        if (requisitionsData.getRequisition(foreignSource) == null) {
          deferred.reject('The foreignSource ' + foreignSource + ' does not exist.');
          return deferred.promise;
        }
      }

      var requisition = {'foreign-source': foreignSource, node: []};
      $log.debug('removeAllNodesFromRequisition: removing nodes from requisition ' + foreignSource);
      var url = requisitionsService.internal.requisitionsUrl;
      $http.post(url, requisition)
      .success(function(data) {
        $log.debug('removeAllNodesFromRequisition: removed nodes from requisition ' + foreignSource);
        requisitionsService.synchronizeRequisition(foreignSource, 'false').then(
          function() { // synchronizeRequisition:success
            $log.debug('removeAllNodesFromRequisition: rhe requisition ' + foreignSource + ' has been synchronized.');
            var req = requisitionsService.internal.getCachedRequisition(foreignSource);
            if (req != null) {
              $log.debug('removeAllNodesFromRequisition: updating requisition ' + foreignSource + ' on the internal cache');
              req.reset();
            }
            deferred.resolve(data);
          },
          function() { // synchronizeRequisition:failure
            deferred.reject('Cannot synchronize requisition ' + foreignSource);
          }
        );
      }).error(function(error, status) {
        $log.error('removeAllNodesFromRequisition: POST ' + url + ' failed:', error, status);
        deferred.reject('Cannot remove all nodes from requisition ' + foreignSource + '.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Request a sepcific node from a requisition from OpenNMS.
    *
    * If the data exists on the cache, that will be used instead of retrieving the data
    * from the OpenNMS server.
    *
    * After retrieving the node, the data will be saved on the internal cache.
    *
    * @name RequisitionsService:getNode
    * @ngdoc method
    * @param {string} foreignSource The requisition's name (a.k.a. foreign source)
    * @param {string} foreignId The foreignId of the node
    * @methodOf RequisitionsService
    * @returns {object} a promise. On success, it provides a RequisitionNode object.
    */
    requisitionsService.getNode = function(foreignSource, foreignId) {
      var deferred = $q.defer();

      var node = requisitionsService.internal.getCachedNode(foreignSource, foreignId);
      if (node != null) {
        $log.debug('getNode: returning a cached copy of ' + foreignId + '@' + foreignSource);
        deferred.resolve(node);
        return deferred.promise;
      }

      var url  = requisitionsService.internal.requisitionsUrl + '/' + encodeURIComponent(foreignSource) + '/nodes/' + encodeURIComponent(foreignId);
      $log.debug('getNode: getting node ' + foreignId + '@' + foreignSource);
      $http.get(url)
      .success(function(data) {
        var node = new RequisitionNode(foreignSource, data);
        $log.debug('getNode: got node ' + foreignId + '@' + foreignSource);
        var requisition = requisitionsService.internal.getCachedRequisition(foreignSource);
        if (requisition != null) {
          $log.debug('getNode: updating cache for requisition ' + foreignSource);
          requisition.setNode(node);
        }
        deferred.resolve(node);
      })
      .error(function(error, status) {
        $log.error('getNode: GET ' + url + ' failed:', error, status);
        deferred.reject('Cannot retrieve node ' + foreignId + ' from requisition ' + foreignSource + '.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Updates a node on an existing requisition on the OpenNMS server.
    *
    * The internal cache will be updated after the request is completed successfully if exist,
    * depending if the save operation is related with the update of an existing node, or if it
    * is related with the creation of a new node.
    *
    * @name RequisitionsService:removeAllNodesFromRequisition
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {object} node The RequisitionNode Object
    * @returns {object} a promise. 
    */
    // TODO If the parent properties are updated, verify they are valid through ReST prior, adding the node.
    requisitionsService.saveNode = function(node) {
      var deferred = $q.defer();
      var requisitionNode = node.getOnmsRequisitionNode();

      var url = requisitionsService.internal.requisitionsUrl + '/' + encodeURIComponent(node.foreignSource) + '/nodes';
      $log.debug('saveNode: saving node ' + node.nodeLabel + ' on requisition ' + node.foreignSource);
      $http.post(url, requisitionNode)
      .success(function(data) {
        $log.debug('saveNode: saved node ' + node.nodeLabel + ' on requisition ' + node.foreignSource);
        node.modified = true;
        var requisition = requisitionsService.internal.getCachedRequisition(node.foreignSource);
        if (requisition != null) {
          requisition.setNode(node);
        }
        deferred.resolve(data);
      }).error(function(error, status) {
        $log.error('saveNode: POST ' + url + ' failed:', error, status);
        deferred.reject('Cannot save node ' + node.foreignId + ' on requisition ' + node.foreignSource + '.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Request the removal of a node from an existing requisition on the OpenNMS server.
    *
    * The internal cache will be updated after the request is completed successfully if exist.
    *
    * @name RequisitionsService:deleteNode
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {object} node The RequisitionNode Object
    * @returns {object} a promise.
    */
    requisitionsService.deleteNode = function(node) {
      var deferred = $q.defer();

      var url = requisitionsService.internal.requisitionsUrl + '/' + encodeURIComponent(node.foreignSource) + '/nodes/' + encodeURIComponent(node.foreignId);
      $log.debug('deleteNode: deleting node ' + node.nodeLabel + ' from requisition ' + node.foreignSource);
      $http.delete(url)
      .success(function(data) {
        $log.debug('deleteNode: deleted node ' + node.nodeLabel + ' on requisition ' + node.foreignSource);
        var r = requisitionsService.internal.getCachedRequisition(node.foreignSource);
        if (r != null) {
          var idx = r.indexOf(node.foreignId);
          if (idx > -1) {
            $log.debug('deleteNode: removing node ' + node.foreignId + '@' + node.foreignSource + ' from the internal cache');
            r.nodes.splice(idx, 1);
            r.nodesDefined--;
            r.modified = true;
            r.dateStamp = Date.now();
          }
        }
        deferred.resolve(data);
      }).error(function(error, status) {
        $log.error('deleteNode: DELETE ' + url + ' failed:', error, status);
        deferred.reject('Cannot delete node ' + node.foreignId + ' from requisition ' + node.foreignSource + '.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Request a foreign source definition from OpenNMS for a given requisition.
    *
    * The foreign source definition contains the set of policies and detectors, as well as the scan frequency.
    * The information is not stored on cache. Each request will perform a server call.
    *
    * @name RequisitionsService:getForeignSourceDefinition
    * @ngdoc method
    * @param {string} foreignSource The requisition's name (a.k.a. foreign source), use 'default' for the default foreign source.
    * @methodOf RequisitionsService
    * @returns {object} a promise. On success, it provides a foreign source object.
    */
    requisitionsService.getForeignSourceDefinition = function(foreignSource) {
      var deferred = $q.defer();

      var url = requisitionsService.internal.foreignSourcesUrl + '/' + encodeURIComponent(foreignSource);
      $log.debug('getForeignSourceDefinition: getting definition for requisition ' + foreignSource);
      $http.get(url)
      .success(function(data) {
        $log.debug('getForeignSourceDefinition: got definition for requisition ' + foreignSource);
        deferred.resolve(data);
      })
      .error(function(error, status) {
        $log.error('getForeignSourceDefinition: GET ' + url + ' failed:', error, status);
        deferred.reject('Cannot retrieve foreign source definition (detectors and policies) for requisition ' + foreignSource + '.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Updates the foreign source definition on the OpenNMS server for a given requisition.
    *
    * The foreign source definition contains the set of policies and detectors, as well as the scan frequency.
    *
    * @name RequisitionsService:saveForeignSourceDefinition
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {object} foreignSourceDef The requisition foreign source Object
    * @returns {object} a promise.
    */
    requisitionsService.saveForeignSourceDefinition = function(foreignSourceDef) {
      var deferred = $q.defer();
      var foreignSource = foreignSourceDef.name;

      var url = requisitionsService.internal.foreignSourcesUrl;
      $log.debug('saveForeignSourceDefinition: saving definition for requisition ' + foreignSource);
      $http.post(url, foreignSourceDef)
      .success(function(data) {
        $log.debug('saveForeignSourceDefinition: saved definition for requisition ' + foreignSource);
        deferred.resolve(data);
      }).error(function(error, status) {
        $log.error('saveForeignSourceDefinition: POST ' + url + ' failed:', error, status);
        deferred.reject('Cannot save foreign source definition (detectors and policies) for requisition ' + foreignSource + '.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Clones an existing foreign source definition to another.
    *
    * The foreign source definition contains the set of policies and detectors, as well as the scan frequency.
    * If the source or the target requisition doesn't appear on the existing requisitions reported by the
    * OpenNMS server, the operation will be rejected.
    *
    * @name RequisitionsService:cloneForeignSourceDefinition
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} sourceRequisition The name of the source requisition
    * @param {string} targetRequisition The name of the target requisition
    * @returns {object} a promise.
    */
    requisitionsService.cloneForeignSourceDefinition = function(sourceRequisition, targetRequisition) {
      var deferred = $q.defer();

      requisitionsService.getRequisitionNames().then(
        function(requisitions) { // success
          if (requisitions.indexOf(sourceRequisition) < 0) {
            deferred.reject('The source requisition ' + sourceRequisition + ' does not exist.');
            return;
          }
          if (requisitions.indexOf(targetRequisition) < 0) {
            deferred.reject('The target requisition ' + targetRequisition + ' does not exist.');
            return;
          }
          requisitionsService.getForeignSourceDefinition(sourceRequisition).then(
            function(fsDef) { // success
              fsDef.name = targetRequisition;
              requisitionsService.saveForeignSourceDefinition(fsDef).then(
                function() { // success
                  deferred.resolve(fsDef);
                },
                function() { // error
                  deferred.reject('Cannot save foreign source definition for requisition ' + targetRequisition);
                }
              );
            },
            function() { // error
              deferred.reject('Cannot get foreign source definition for requisition ' + sourceRequisition);
            }
          );
        },
        function() { // error
          deferred.reject('Cannot validate the existance of the source and target requisitions.');
        }
      );

      return deferred.promise;
    };

    /**
    * @description Request the removal of a foreign source definition on the OpenNMS server.
    *
    * @name RequisitionsService:deleteForeignSourceDefinition
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} foreignSource The requisition's name (a.k.a. foreign source), use 'default' for the default foreign source.
    * @returns {object} a promise.
    */
    requisitionsService.deleteForeignSourceDefinition = function(foreignSource) {
      var deferred = $q.defer();

      $log.debug('deleteForeignSourceDefinition: deleting foreign source definition ' + foreignSource);
      var deferredFSPending  = $http.delete(requisitionsService.internal.foreignSourcesUrl + '/' + encodeURIComponent(foreignSource));
      var deferredFSDeployed = $http.delete(requisitionsService.internal.foreignSourcesUrl + '/deployed/' + encodeURIComponent(foreignSource));

      $q.all([ deferredFSPending, deferredFSDeployed ])
      .then(function(results) {
        $log.debug('deleteForeignSourceDefinition: deleted foreign source definition ' + foreignSource);
        deferred.resolve(results);
      }, function(error, status) {
        $log.error('deleteForeignSourceDefinition: DELETE operation failed:', error, status);
        deferred.reject('Cannot delete the foreign source definition ' + foreignSource + '.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Gets the available detectors.
    *
    * If the data exists on the cache, that will be used instead of retrieving the data
    * from the OpenNMS server.
    *
    * After retrieving the node, the data will be saved on the internal cache.
    *
    * The data return by the promise should be an array of objects.
    * Each object contains the name of the detector and the full class name.
    *
    * @name RequisitionsService:getAvailableDetectors
    * @ngdoc method
    * @methodOf RequisitionsService
    * @returns {object} a promise. On success, it provides a list of available detector objects.
    */
    requisitionsService.getAvailableDetectors = function() {
      var deferred = $q.defer();

      var config = requisitionsService.internal.getCatchedConfigData('detectorsConfig');
      if (config != null) {
        $log.debug('getAvailableDetectors: returning a cached copy of detectors configuration');
        deferred.resolve(config);
        return deferred.promise;
      }

      var url = requisitionsService.internal.foreignSourcesConfigUrl + '/detectors';
      $log.debug('getAvailableDetectors: getting available detectors');
      $http.get(url)
      .success(function(data) {
        $log.debug('getAvailableDetectors: got available detectors');
        requisitionsService.internal.setCatchedConfigData('detectorsConfig', data.plugins);
        deferred.resolve(data.plugins);
      })
      .error(function(error, status) {
        $log.error('getAvailableDetectors: GET ' + url + ' failed:', error, status);
        deferred.reject('Cannot retrieve available detectors.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Gets the available policies.
    *
    * If the data exists on the cache, that will be used instead of retrieving the data
    * from the OpenNMS server.
    *
    * After retrieving the node, the data will be saved on the internal cache.
    *
    * The data return by the promise should be an array of objects.
    * Each object contains the name of the policy and the full class name.
    *
    * @name RequisitionsService:getAvailablePolicies
    * @ngdoc method
    * @methodOf RequisitionsService
    * @returns {object} a promise. On success, it provides a list of available polict objects.
    */
    requisitionsService.getAvailablePolicies = function() {
      var deferred = $q.defer();

      var config = requisitionsService.internal.getCatchedConfigData('policiesConfig');
      if (config) {
        $log.debug('getAvailablePolicies: returning a cached copy of policies configuration');
        deferred.resolve(config);
        return deferred.promise;
      }

      var url = requisitionsService.internal.foreignSourcesConfigUrl + '/policies';
      $log.debug('getAvailablePolicies: getting available policies');
      $http.get(url)
      .success(function(data) {
        $log.debug('getAvailablePolicies: got available policies');
        requisitionsService.internal.setCatchedConfigData('policiesConfig', data.plugins);
        deferred.resolve(data.plugins);
      })
      .error(function(error, status) {
        $log.error('getAvailablePolicies: GET ' + url + ' failed:', error, status);
        deferred.reject('Cannot retrieve available policies.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Gets the available services.
    *
    * If the data exists on the cache, that will be used instead of retrieving the data
    * from the OpenNMS server.
    *
    * After retrieving the node, the data will be saved on the internal cache.
    *
    * The data return by the promise should be an array of strings.
    * Each strings contains the name of the service.
    *
    * @example [ 'ICMP', 'SNMP' ]
    *
    * @name RequisitionsService:getAvailableServices
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} foreignSource The requisition's name (a.k.a. foreign source), use 'default' for the default foreign source.
    * @returns {object} a promise. On success, it provides a list of available services.
    */
    // FIXME Does make sense to cache this information ?
    requisitionsService.getAvailableServices = function(foreignSource) {
      var deferred = $q.defer();

      var config = requisitionsService.internal.getCatchedConfigData('servicesConfig');
      if (config) {
        $log.debug('getAvailableServices: returning a cached copy of services configuration');
        deferred.resolve(config);
        return deferred.promise;
      }

      var url = requisitionsService.internal.foreignSourcesConfigUrl + '/services/' + encodeURIComponent(foreignSource);
      $log.debug('getAvailableServices: getting available services');
      $http.get(url)
      .success(function(data) {
        $log.debug('getAvailableServices: got available services');
        requisitionsService.internal.setCatchedConfigData('servicesConfig', data.element);
        deferred.resolve(data.element);
      })
      .error(function(error, status) {
        $log.error('getAvailableServices: GET ' + url + ' failed:', error, status);
        deferred.reject('Cannot retrieve available services.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Gets the available assets.
    *
    * If the data exists on the cache, that will be used instead of retrieving the data
    * from the OpenNMS server.
    *
    * After retrieving the node, the data will be saved on the internal cache.
    *
    * The data return by the promise should be an array of strings.
    * Each string is a valid asset field.
    *
    * @example [ 'address1, 'city', 'zip' ]
    *
    * @name RequisitionsService:getAvailableAssets
    * @ngdoc method
    * @methodOf RequisitionsService
    * @returns {object} a promise. On success, it provides a list of available assets.
    */
    requisitionsService.getAvailableAssets = function() {
      var deferred = $q.defer();

      var config = requisitionsService.internal.getCatchedConfigData('assetsConfig');
      if (config) {
        $log.debug('getAvailableAssets: returning a cached copy of assets configuration');
        deferred.resolve(config);
        return deferred.promise;
      }

      var url = requisitionsService.internal.foreignSourcesConfigUrl + '/assets';
      $log.debug('getAvailableAssets: getting available assets');
      $http.get(url)
      .success(function(data) {
        $log.debug('getAvailableAssets: got available assets');
        requisitionsService.internal.setCatchedConfigData('assetsConfig', data.element);
        deferred.resolve(data.element);
      })
      .error(function(error, status) {
        $log.error('getAvailableAssets: GET ' + url + ' failed:', error, status);
        deferred.reject('Cannot retrieve available assets.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Gets the available categories.
    *
    * If the data exists on the cache, that will be used instead of retrieving the data
    * from the OpenNMS server.
    *
    * After retrieving the node, the data will be saved on the internal cache.
    *
    * The data return by the promise should be an array of strings.
    * Each string is a valid category name.
    *
    * @example [ 'Production, 'Development', 'Testing' ]
    *
    * @name RequisitionsService:getAvailableCategories
    * @ngdoc method
    * @methodOf RequisitionsService
    * @returns {object} a promise. On success, it provides a list of available categories.
    */
    // FIXME Does make sense to cache this information ?
    requisitionsService.getAvailableCategories = function() {
      var deferred = $q.defer();

      var config = requisitionsService.internal.getCatchedConfigData('categoriesConfig');
      if (config) {
        $log.debug('getAvailableCategories: returning a cached copy of categories configuration');
        deferred.resolve(config);
        return deferred.promise;
      }

      var url = requisitionsService.internal.foreignSourcesConfigUrl + '/categories';
      $log.debug('getAvailableCategories: getting available categories');
      $http.get(url)
      .success(function(data) {
        $log.debug('getAvailableCategories: got available categories');
        requisitionsService.internal.setCatchedConfigData('categoriesConfig', data.element);
        deferred.resolve(data.element);
      })
      .error(function(error, status) {
        $log.error('getAvailableCategories: GET ' + url + ' failed:', error, status);
        deferred.reject('Cannot retrieve available categories.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Checks if a given foreignId exists on a specific requisition
    *
    * @name RequisitionsService:isForeignIdOnRequisition
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} foreignSource The requisition's name (a.k.a. foreign source)
    * @param {string} foreignId The foreign Id
    * @returns {object} a promise. On success, return true if the foreignId exists on the requisition.
    */
    requisitionsService.isForeignIdOnRequisition = function(foreignSource, foreignId) {
      var deferred = $q.defer();

      requisitionsService.getRequisition(foreignSource)
      .then(function(req) {
        var found = false;
        angular.forEach(req.nodes, function(n) {
          if (n.foreignId == foreignId) {
            found = true;
          }
        });
        deferred.resolve(found);
      }, function(err) {
        var message = 'cannot verify foreignId ' + foreignId + '@' + foreignSource + '. ';
        $log.error('isForeignIdOnRequisition: ' + message, err);
        deferred.reject(message + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Checks if a given nodeLabel exists on a specific requisition
    *
    * @name RequisitionsService:isForeignIdOnRequisition
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} foreignSource The requisition's name (a.k.a. foreign source)
    * @param {string} nodeLabel The node label
    * @returns {object} a promise. On success, return true if the nodeLabel exists on the requisition.
    */
    requisitionsService.isNodeLabelOnRequisition = function(foreignSource, nodeLabel) {
      var deferred = $q.defer();

      requisitionsService.getRequisition(foreignSource)
      .then(function(req) {
        var found = false;
        angular.forEach(req.nodes, function(n) {
          if (n.nodeLabel == nodeLabel) {
            found = true;
          }
        });
        deferred.resolve(found);
      }, function(err) {
        var message = 'cannot verify nodeLabel ' + nodeLabel + '@' + foreignSource + '. ';
        $log.error('isForeignIdOnRequisition: ' + message, err);
        deferred.reject(message + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Checks if a given IP Address exists on a specific node
    *
    * @name RequisitionsService:isIpAddressOnNode
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} foreignSource The requisition's name (a.k.a. foreign source)
    * @param {string} foreignId The foreign Id of the node
    * @param {string} ipAddress The IP address to check
    * @returns {object} a promise. On success, return  true if the IP Address exists on the node.
    */
    requisitionsService.isIpAddressOnNode = function(foreignSource, foreignId, ipAddress) {
      var deferred = $q.defer();

      requisitionsService.getNode(foreignSource, foreignId)
      .then(function(node) {
        var found = false;
        angular.forEach(node.interfaces, function(intf) {
          if (intf.ipAddress == ipAddress) {
            found = true;
          }
        });
        deferred.resolve(found);
      }, function(err) {
        var message = 'cannot verify ipAddress on node ' + foreignId + '@' + foreignSource + '. ';
        $log.error('isIpAddressOnNode: ' + message, err);
        deferred.reject(message + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Checks if a given category exists on a specific node
    *
    * @name RequisitionsService:isCategoryOnNode
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} foreignSource The requisition's name (a.k.a. foreign source)
    * @param {string} foreignId The foreign Id of the node
    * @param {string} category The category to check
    * @returns {object} a promise. On success, return true if the category exists on the node.
    */
    requisitionsService.isCategoryOnNode = function(foreignSource, foreignId, category) {
      var deferred = $q.defer();

      requisitionsService.getNode(foreignSource, foreignId)
      .then(function(node) {
        var found = false;
        angular.forEach(node.categories, function(cat) {
          if (cat.name == category) {
            found = true;
          }
        });
        deferred.resolve(found);
      }, function(err) {
        var message = 'cannot verify category on node ' + foreignId + '@' + foreignSource + '. ';
        $log.error('isCategoryOnNode: ' + message, err);
        deferred.reject(message + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Checks if a given category exists on a specific node
    *
    * @name RequisitionsService:isCategoryOnNode
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} foreignSource The requisition's name (a.k.a. foreign source)
    * @param {string} foreignId The foreign Id of the node
    * @param {string} ipAddress The IP address to check
    * @param {string} service The service to check
    * @returns {object} a promise. On success, return true if the service exists on the Node/IP.
    */
    requisitionsService.isServiceOnNode = function(foreignSource, foreignId, ipAddress, service) {
      var deferred = $q.defer();

      requisitionsService.getNode(foreignSource, foreignId)
      .then(function(node) {
        var found = false;
        angular.forEach(node.interfaces, function(intf) {
          angular.forEach(intf.services, function(svc) {
            if (svc.name == service) {
              found = true;
            }
          });
        });
        deferred.resolve(found);
      }, function(err) {
        var message = 'cannot verify category on node ' + foreignId + '@' + foreignSource + '. ';
        $log.error('isIpAddressOnNode: ' + message, err);
        deferred.reject(message + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Update the SNMP credentials for a given IP Address.
    *
    * @name RequisitionsService:addRequisition
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {string} ipAddress The IP Address
    * @param {string} snmpCommunity The SNMP Community String
    * @param {string} snmpVersion The SNMP Version
    * @returns {object} a promise.
    */
    requisitionsService.updateSnmpCommunity = function(ipAddress, snmpCommunity, snmpVersion) {
      var deferred = $q.defer();

      var url = requisitionsService.internal.snmpConfigUrl + '/' + ipAddress;
      $log.debug('updateSnmpCommunity: updating snmp community for ' + ipAddress);
      $http.put(url, {'readCommunity' : snmpCommunity, 'version' : snmpVersion})
      .success(function() {
        $log.debug('updateSnmpCommunity: updated snmp community for ' + ipAddress);
        deferred.resolve(ipAddress);
      }).error(function(error, status) {
        $log.error('updateSnmpCommunity: PUT ' + url + ' failed:', error, status);
        deferred.reject('Cannot update snmp community for ' + ipAddress + '.' + requisitionsService.internal.errorHelp);
      });

      return deferred.promise;
    };

    /**
    * @description Quick add a node to a requisition.
    *
    * @name RequisitionsService:quickAddNode
    * @ngdoc method
    * @methodOf RequisitionsService
    * @param {object} quickNode The QuickNode object
    * @returns {object} a promise.
    */
    requisitionsService.quickAddNode = function(quickNode) {
      if (quickNode.noSnmp == false && quickNode.snmpCommunity != '') {
        var deferred = $q.defer();
        requisitionsService.updateSnmpCommunity(quickNode.ipAddress, quickNode.snmpCommunity, quickNode.snmpVersion).then(
          function() { // updateSnmpCommunity:success
            requisitionsService.internal.addQuickNode(quickNode).then(
              function(node) { // addQuickNode:success
                deferred.resolve(node);
              },
              function(msg) { // addQuickNode:failure
                deferred.reject(msg);
              }
            );
          },
          function() { // updateSnmpCommunity:failure
            deferred.reject('Cannot update SNMP credentials for ' + quickNode.ipAddress);
          }
        );
        return deferred.promise;
      }
      return requisitionsService.internal.addQuickNode(quickNode);
    };

    return requisitionsService;
  }]);

}());
