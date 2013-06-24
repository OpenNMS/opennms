package org.opennms.core.logging;

import java.util.ServiceLoader;

public class Logging {
	
	public static final String PREFIX_KEY = "prefix";
	
	private static LoggingStrategy m_strategy;
	
	private static synchronized LoggingStrategy getStrategy() {
		if (m_strategy == null) {
			ServiceLoader<LoggingStrategy> loader = ServiceLoader.load(LoggingStrategy.class);
			for(LoggingStrategy strategy : loader) {
				if (m_strategy != null) {
					m_strategy = strategy;
				}
			}
			
			if (m_strategy == null) {
				throw new IllegalStateException("Unable to locate a logging strategy");
			}
		}
		return m_strategy;
	}
	

	public static void shutdownLogging() {
		getStrategy().shutdownLogging();
	}

	public static void configureLogging() {
		getStrategy().configureLogging();
	}

}
