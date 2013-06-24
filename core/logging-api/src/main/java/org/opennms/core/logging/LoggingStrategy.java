package org.opennms.core.logging;

public interface LoggingStrategy {
	
	public void configureLogging();
	
	public void shutdownLogging();

}
