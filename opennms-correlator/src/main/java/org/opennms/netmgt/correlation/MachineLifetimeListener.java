package org.opennms.netmgt.correlation;

import java.util.EventListener;

public interface MachineLifetimeListener extends EventListener {
	
	public void machineCreated(MachineLifetimeEvent event);
	
	public void machineCompleted(MachineLifetimeEvent event);

}
