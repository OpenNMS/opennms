(function() {
  'use strict';

  angular.module('opennms.services.shared.modelfactory', [])
    .factory('ModelFactory', ['$log', function($log) {
      $log.info('Initializing Model Factory');
      var modelFactory = new Object();
      modelFactory.factories = new Array();

      modelFactory.register = function(factoryName, factoryObject) {
        if(angular.isArray(factoryName)) {
          factoryName.forEach(function(name) {
            $log.info('ModelFactory: registering ' + factoryName);
            modelFactory.factories[name] = factoryObject;
          });
        } else {
          $log.info('ModelFactory: registering ' + factoryName);
          modelFactory.factories[factoryName] = factoryObject;
        }

      };

      modelFactory.get = function(factoryName) {
        return modelFactory.factories[factoryName];
      };

      modelFactory.processResults = function(results) {
        var restRoot = Object.keys(results)[0];
        var factoryObject = modelFactory.get(restRoot);
        var response = undefined;

        if(factoryObject === undefined) {
          $log.error('Unable to find model factory for: ' + restRoot);
          return;
        }

        var restBaseNode = results[restRoot][factoryObject.restBase];
        if(restBaseNode === undefined) {
          $log.debug('Processing a single result');
          response = {
            totalCount: totalCount,
            objects: new factoryObject.restModel(results[restRoot])
          };
        } else if(restBaseNode instanceof Array) {
          $log.debug('Processing an array of results');
          var totalCount = 0;
          if(results[restRoot]['_totalCount'] !== undefined) {
            totalCount = Number(results[restRoot]['_totalCount'])
          }
          response = {
            totalCount: totalCount,
            objects: []
          }
          restBaseNode.forEach(function(modelElement) {
            response.objects.push(new factoryObject.restModel(modelElement));
          });
        }

        return response;
      };

      return modelFactory;
    }]);

}());