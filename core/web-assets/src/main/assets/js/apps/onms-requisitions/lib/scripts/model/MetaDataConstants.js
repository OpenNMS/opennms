/**
 * @author Jesse White <jesse@opennms.org>
 * @copyright 2019 The OpenNMS Group, Inc.
 */

/**
 * Available scopes for meta-data entries.
 */
const Scope = {
    NODE: 'node',
    INTERFACE: 'interface',
    SERVICE: 'service'
};

const RequisitionContext = 'requisition';

module.exports = {Scope, RequisitionContext};
