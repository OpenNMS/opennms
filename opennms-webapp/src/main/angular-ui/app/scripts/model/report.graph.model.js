/* global moment: true */

/**
 * @ngdoc object
 * @name ReportGraph
 * @param {Object} graph a report graph JSON object
 * @constructor
 */
function ReportGraph(graph) {
  'use strict';

  var self = this;
  var re = /^node\[(\d+)\]\.(\S+?)\[(\S+)\]$/;

  /**
   * @description
   * @ngdoc property
   * @name ReportGraph#title
   * @propertyOf ReportGraph
   * @returns {string} ReportGraph Title
   */
  self.title = graph['_title'];

  /**
   * @description
   * @ngdoc property
   * @name ReportGraph#type
   * @propertyOf ReportGraph
   * @returns {string} The graph type
   */
  self.type = graph['_graphtype'];

  /**
   * @description
   * @ngdoc property
   * @name ReportGraph#resourceId
   * @propertyOf ReportGraph
   * @returns {string} The graph resource id
   */
  self.resourceId = graph['_resourceId'];

  /**
   * @description
   * @ngdoc property
   * @name ReportGraph#timespan
   * @propertyOf ReportGraph
   * @returns {string} The graph timespan
   */
  self.timespan = graph['_timespan'];

  var OK = re.exec(self.resourceId);
  if (!!OK) {
    /**
     * @description
     * @ngdoc property
     * @name ReportGraph#nodeId
     * @propertyOf ReportGraph
     * @returns {number} Node id
     */
    self.nodeId = Number(OK[1]);

    /**
     * @description
     * @ngdoc property
     * @name ReportGraph#resourceType
     * @propertyOf ReportGraph
     * @returns {string} The resource type
     */
    self.resourceType = OK[2];

    /**
     * @description
     * @ngdoc property
     * @name ReportGraph#resourceIndex
     * @propertyOf ReportGraph
     * @returns {string} The resource index
     */
    self.resourceIndex = OK[3];

  }
}
