package org.opennms.netmgt.model;

import org.opennms.core.fiber.PausableFiber;

public interface ServiceDaemon extends PausableFiber {
	
	 public String status();

}
