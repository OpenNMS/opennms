package org.opennms.netmgt.poller.remote;

import java.beans.PropertyChangeEvent;
import java.util.EventListener;

public interface ConfigurationChangedListener extends EventListener {
	
	public void configurationChanged(PropertyChangeEvent e);

}
