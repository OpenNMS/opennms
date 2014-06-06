(function() {
  'use strict';

  angular.module('opennms.directives.shared.nodes', [
    'opennms.services.shared.config'
  ])

    .directive('nodeListPopover', function() {
      return {
        restrict: 'A',
        scope: {
          currentNode: '&popoverNode'
        },
        link: function(scope, element, attrs) {
          console.log(attrs);
          var node = scope.currentNode();

          var content = '<div>';
          content += '<strong>' + node.label + '</strong> ID: ' + node.id + '<br/>';

          if(node._foreignId) {
            content += '<strong>Foreign Source:</strong> ' + node.foreignSource +'<br/><strong>Foreign ID:</strong> ' + node.foreignId + '<br/>'
          }

          content += '</div>';
          $(element).popover({
            trigger: 'hover',
            html: 'true',
            container: 'body',
            content: content
          });
        }
      }
    });

}());
//<span ng-if="node._foreignId">, </span>)</h5>
//<div style="padding-left: 20px" ng-if="ifaces[node._id]">
//  <div ng-repeat="iface in ifaces[node._id] | orderBy: '_ifIndex' track by iface._id">
//    <strong>{{iface.ipAddress}}</strong><span ng-if="iface._ifIndex">: ifIndex={{iface._ifIndex}}</span>
//  </div>
//</div>

//customDirectives = angular.module('customDirectives', []);
//customDirectives.directive('customPopover', function () {
//  return {
//    restrict: 'A',
//    template: '<span>{{label}}</span>',
//    link: function (scope, el, attrs) {
//      scope.label = attrs.popoverLabel;
//
//      $(el).popover({
//        trigger: 'click',
//        html: true,
//        content: attrs.popoverHtml,
//        placement: attrs.popoverPlacement
//      });
//    }
//  };
//});