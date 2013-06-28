package org.opennms.core.logging.log4j;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.opennms.core.logging.LoggingStrategy;

public class Log4jLoggingStrategy implements LoggingStrategy {

    public void availabilityReportConfigureLogging(String prefix) {
        // Spit warning level and higher messages out to the console
        ConsoleAppender consoleAppender = new ConsoleAppender(new PatternLayout("%m%n"), ConsoleAppender.SYSTEM_ERR);
        consoleAppender.setThreshold(Level.WARN);
        Logger logger = Logger.getLogger(prefix);
        logger.addAppender(consoleAppender);
    }

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
                throw new IllegalStateException(
                                                "Could not find a Log4j configuration file at "
                                                        + xmlFile.getAbsolutePath()
                                                        + " or "
                                                        + propertiesFile.getAbsolutePath()
                                                        + ".  Exiting.");
            }
        }
    }

    @Override
    public void shutdownLogging() {
        LogManager.shutdown();
    }

    @Override
    public void configureInstallerLogging() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);
        
    }

}
