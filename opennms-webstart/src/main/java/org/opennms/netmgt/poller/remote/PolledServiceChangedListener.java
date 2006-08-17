package org.opennms.netmgt.poller.remote;

import java.util.EventListener;


public interface PolledServiceChangedListener extends EventListener{
	
	public void polledServiceChanged(PolledServiceChangedEvent e);

}
