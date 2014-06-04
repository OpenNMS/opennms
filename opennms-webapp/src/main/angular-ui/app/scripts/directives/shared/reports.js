(function() {
  'use strict';

  angular.module('opennms.directives.shared.reports', [
    'opennms.services.shared.config'
  ])

    .directive('reportListPopover', function() {
      return {
        restrict: 'A',
        scope: {
          currentReport: '&popoverReport'
        },
        link: function(scope, element, attrs) {
          console.log(attrs);
          var reports = scope.currentReport();

          var content = '<div>';
          content += '<strong>' + reports.title + '</strong> ID: ' + reports.id + '<br/>';

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
