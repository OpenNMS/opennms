package org.opennms.netmgt.poller.remote;

public interface PollerConfiguration {
	
	ServicePollConfiguration[] getConfigurationForPoller(String poller);
}
