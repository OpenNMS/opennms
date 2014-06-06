/* global moment: true */

/**
 * @ngdoc object
 * @name Report
 * @param {Object} report a report JSON object
 * @constructor
 */
function Report(report) {
  'use strict';

  var self = this;

  /**
   * @description
   * @ngdoc property
   * @name Report#id
   * @propertyOf Report
   * @returns {number} The unique report ID
   */
  self.id = Number(report['_id']);

  /**
   * @description
   * @ngdoc property
   * @name Report#title
   * @propertyOf Report
   * @returns {string} Report Title
   */
  self.title = report['_label'];

  if (report['_show_graphtype_button']) {
    /**
     * @description
     * @ngdoc property
     * @name Report#graphtype_button
     * @propertyOf Report
     * @returns {boolean} True the report should show a button to override the graph type.
     */
    self.graphtype_button = report['_show_graphtype_button'];
  }

  if (report['_show_timespan_button']) {
    /**
     * @description
     * @ngdoc property
     * @name Report#timespan_button
     * @propertyOf Report
     * @returns {boolean} True the report should show a button to override the timespan.
     */
    self.timespan_button = report['_show_timespan_button'];
  }

  if (report['_graphs_per_line']) {
    /**
     * @description
     * @ngdoc property
     * @name Report#graphsPerLine
     * @propertyOf Report
     * @returns {number} The number of graphs to display per row.
     */
    self.graphsPerLine = report['_graphs_per_line'];
  }

  /**
   * @description
   * @ngdoc property
   * @name Report#graphs
   * @propertyOf Report
   * @returns {array} An array of graphs to display.
   */
  self.graphs = [];
  if (report['kscGraph']) {
    console.log('graphs: ', report['kscGraph']);
    if (angular.isArray(report['kscGraph'])) {
      report['kscGraph'].map(function(graph) {
        self.graphs.push(new ReportGraph(graph));
      });
    } else {
      self.graphs.push(new ReportGraph(report['kscGraph']));
    }
  }

}
