package org.opennms.netmgt.poller.remote;

import java.util.EventListener;


public interface ServicePollStateChangedListener extends EventListener{
	
	public void pollStateChange(ServicePollStateChangedEvent e);

}
