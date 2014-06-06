(function() {
  'use strict';

  angular.module('opennms.models.node', ['opennms.services.shared.modelfactory'])
    .run(['$log', 'ModelFactory', function($log, ModelFactory) {
      var Node = function(node) {
        var self = this;

        self.id = node['_id'];
        self.label = node['_label'];
        self.type = node['_type'];
        self.assetRecord = node['assetRecord'];
        self.categories = node['categories'];
        self.createTime = moment(node['createTime']);
        self.labelSource = node['labelSource'];
        self.lastCapsdPoll = moment(node['lastCapsdPoll']);
        self.sysContact = node['sysContact'];
        self.sysDescription = node['sysDescription'];
        self.sysLocation = node['sysLocation'];
        self.sysName = node['sysName'];
        self.sysObjectId = node['sysObjectId'];

        if(node['_foreignId'] !== undefined) {
          self.foreignId = node['_foreignId'];
        }

        if(node['foreignSource'] !== undefined) {
          self.foreignSource = node['foreignSource'];
        }

        self.className = 'Node';
        return self;
      };

      $log.debug('-- Registering "nodes" model with factory');
      ModelFactory.register(['nodes', 'node'], {
        restBase: 'node',
        restModel: Node
      });
    }]);
}());