package org.opennms.netmgt.poller.remote;

public interface PollerConfiguration {
	
	PollConfiguration[] getConfigurationForPoller();
}
