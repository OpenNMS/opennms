package org.opennms.core.logging.log4j;

import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.opennms.core.logging.LoggingStrategy;

public class Log4jLoggingStrategy implements LoggingStrategy {

	@Override
	public void configureLogging() {
		File homeDir = new File(System.getProperty("opennms.home"));
	    File etcDir = new File(homeDir, "etc");
	    
	    File xmlFile = new File(etcDir, "log4j.xml");
	    if (xmlFile.exists()) {
	        DOMConfigurator.configureAndWatch(xmlFile.getAbsolutePath());
	    } else {
	        File propertiesFile = new File(etcDir, "log4j.properties");
	        if (propertiesFile.exists()) {
	            PropertyConfigurator.configureAndWatch(propertiesFile.getAbsolutePath());
	        } else {
	            throw new IllegalStateException("Could not find a Log4j configuration file at "
	                    + xmlFile.getAbsolutePath() + " or "
	                    + propertiesFile.getAbsolutePath() + ".  Exiting.");
	        }
	    }
	}

	@Override
	public void shutdownLogging() {
		LogManager.shutdown();
	}

}
