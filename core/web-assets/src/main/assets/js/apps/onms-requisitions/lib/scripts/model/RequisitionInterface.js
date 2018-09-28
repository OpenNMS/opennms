/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

/**
* @ngdoc object
* @name RequisitionInterface
* @module onms-requisitions
* @param {Object} intf an OpenNMS interface JSON object
* @constructor
*/
const RequisitionInterface = function RequisitionInterface(intf) {
  'use strict';

  const self = this;

  /**
   * @description The IP Address of the interface
   * @ngdoc property
   * @name RequisitionInterface#ipAddress
   * @propertyOf RequisitionInterface
   * @returns {string} The IP Address of the interface
   */
  self.ipAddress = intf['ip-addr'];

  /**
   * @description The description of the interface
   * @ngdoc property
   * @name RequisitionInterface#description
   * @propertyOf RequisitionInterface
   * @returns {string} The description of the interface
   */
  self.description = intf['descr'];

  /**
   * @description The primary flag ('P' for primary, 'S' for secondary or 'N' for None)
   * @ngdoc property
   * @name RequisitionInterface#snmpPrimary
   * @propertyOf RequisitionInterface
   * @returns {string} The primary flag
   */
  self.snmpPrimary = intf['snmp-primary'];

  /**
   * @description The status of the interface (managed or unmanaged)
   * @ngdoc property
   * @name RequisitionInterface#status
   * @propertyOf RequisitionInterface
   * @returns {string} The status
   */
  self.status = 'managed';
  if (intf && intf['status']) {
    self.status = intf['status'] === '1' ? 'managed' : 'unmanaged';
  }


  /**
   * @description The array of services. Each service is an object with a name property, for example: { name: 'ICMP' }
   * @ngdoc property
   * @name RequisitionInterface#services
   * @propertyOf RequisitionInterface
   * @returns {array} The services
   */
  self.services = [];

  angular.forEach(intf['monitored-service'], function(svc) {
    self.services.push({ name: svc['service-name'] });
  });

  /**
  * @description Adds a new monitored service to the interface
  *
  * @name RequisitionInterface:addNewService
  * @ngdoc method
  * @methodOf RequisitionInterface
  * @returns {object} the new service Object
  */
  self.addNewService = function() {
    self.services.push({ name: '' });
    return self.services.length - 1;
  };

  self.className = 'RequisitionInterface';

  return self;
}

module.exports = RequisitionInterface;