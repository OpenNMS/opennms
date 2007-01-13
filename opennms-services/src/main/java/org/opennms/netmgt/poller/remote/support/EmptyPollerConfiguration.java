/**
 * 
 */
package org.opennms.netmgt.poller.remote.support;

import java.util.Date;

import org.opennms.netmgt.poller.remote.PolledService;
import org.opennms.netmgt.poller.remote.PollerConfiguration;

class EmptyPollerConfiguration implements PollerConfiguration {

    public Date getConfigurationTimestamp() {
        return new Date(0);
    }

    public PolledService[] getPolledServices() {
        return new PolledService[0];
    }
    
}