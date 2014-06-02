(function() {
  'use strict';

  angular.module('opennms.services.shared.nodes', [])
    .factory('nodeFactory', function($http) {
      var nodes = [
        {'id': 1, 'label': 'node8', 'foreignSource': 'bigreq', 'foreignId': 'node8'},
        {'id': 2, 'label': 'node3'},
        {'id': 3, 'label': 'node7'},
        {'id': 4, 'label': 'node5'},
        {'id': 5, 'label': 'node6'},
        {'id': 6, 'label': 'node4'},
        {'id': 7, 'label': 'node2'},
        {'id': 8, 'label': 'node1'},
        {'id': 9, 'label': 'node10'},
        {'id': 10, 'label': 'node9'},
        {'id': 11, 'label': 'localhost'},
        {'id': 12, 'label': 'Test Node'},
        {'id': 13, 'label': 'roskens-fedora'}
      ];
      var factory = {};
      factory.getNodes = function() {
        return nodes;
      };
      return factory;
    })
    .factory('nodeDetailFactory', function($http) {
      var node = {'id': 1, 'label': 'node8', 'foreignSource': 'bigreq', 'foreignId': 'node8'};
      var factory = {};
      factory.getNode = function(id) {
        return node;
      };
      return factory;
    });
}());