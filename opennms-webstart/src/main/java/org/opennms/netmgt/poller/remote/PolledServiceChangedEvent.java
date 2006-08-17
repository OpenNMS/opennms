package org.opennms.netmgt.poller.remote;

import java.util.EventObject;

public class PolledServiceChangedEvent extends EventObject {
	
	private static final long serialVersionUID = 5224040562319082465L;

	private int m_index;

	public PolledServiceChangedEvent(PolledService polledService, int index) {
		super(polledService);
		m_index = index;
	}
	
	public PolledService getPolledService() {
		return (PolledService)getSource();
	}
	
	public int getIndex() {
		return m_index;
	}

}
