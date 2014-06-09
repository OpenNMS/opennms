(function() {
  'use strict';

  angular.module('opennms.directives.shared.pagedtable', [
    'opennms.services.shared.pagedresource',
    'opennms.services.shared.config'
  ])

  .directive('paged', ['$log', function($log) {
    return {
      restrict: 'C',
      scope: {
      },
      link: function(scope, element, attrs) {
        if (attrs.modelName !== undefined) {
          scope.modelName=attrs.modelName;
        }
      }
    }
  }])

  .directive('ptTh', ['$log', function($log) {
    return {
      restrict: 'E',
      transclude: true,
      replace: true,
      template: '<th ng-transclude></th>',
      link: function(scope, element, attrs) {
        if (scope.modelProperties === undefined) {
          scope.modelProperties = [];
        }
        if (attrs.property) {
          scope.modelProperties.push(attrs.property);
        }
      }
    }
  }])

  ;

  /*
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
*/
}());
