/* global moment: true */

/**
 * @ngdoc object
 * @name MonitoredService
 * @param {Object} svc an service JSON object
 * @constructor
 */
function MonitoredService(svc) {
  'use strict';

  var self = this;

  /**
   * @description
   * @ngdoc property
   * @name MonitoredService#id
   * @propertyOf MonitoredService
   * @returns {number} The unique service ID
   */
  self.id = svc['_id'];

  /**
   * @description
   * @ngdoc property
   * @name MonitoredService#ipINterfaceId
   * @propertyOf MonitoredService
   * @returns {number} The unique ID of the IP interface this monitored service is associated with.
   */
  self.ipInterfaceId = svc['ipInterfaceId'];

  /**
   * @description
   * @ngdoc property
   * @name MonitoredService#serviceId
   * @propertyOf MonitoredService
   * @returns {number} The unique ID of the service type this monitored service is of.
   */
  self.serviceId = svc['serviceType']['_id'];

  /**
   * @description
   * @ngdoc property
   * @name MonitoredService#serviceName
   * @propertyOf MonitoredService
   * @returns {string} The human-readable name of the service type this monitored service is of.
   */
  self.serviceName = svc['serviceType']['name'];

  /**
   * @description
   * @ngdoc property
   * @name MonitoredService#status
   * @propertyOf MonitoredService
   * @returns {*} the monitored service status.
   */
  self.status = svc['_status'];
}