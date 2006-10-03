package org.opennms.netmgt.poller.remote;

import java.util.Date;

public interface PollerConfiguration {
    
    Date getConfigurationTimestamp();
	
	PollConfiguration[] getConfigurationForPoller();
}
