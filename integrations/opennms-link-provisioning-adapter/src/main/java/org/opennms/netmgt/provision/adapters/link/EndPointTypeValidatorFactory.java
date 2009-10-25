/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointTypeValidator;


public interface EndPointTypeValidatorFactory {
    public EndPointTypeValidator getContainer();
}