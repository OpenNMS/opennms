package org.opennms.netmgt.daemon;

import org.opennms.core.fiber.PausableFiber;

public interface ServiceDaemon extends PausableFiber {
	
	 public String status();

}
