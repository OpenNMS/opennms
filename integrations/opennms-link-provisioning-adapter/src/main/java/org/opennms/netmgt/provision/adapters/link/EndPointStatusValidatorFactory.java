/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.provision.LinkMonitorValidatorTest.EndPointTypeConfigContainer;


public interface EndPointStatusValidatorFactory{
    public EndPointTypeConfigContainer getContainer();
}