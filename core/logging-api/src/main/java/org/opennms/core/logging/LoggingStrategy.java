package org.opennms.core.logging;

public interface LoggingStrategy {

    public void configureLogging();

    public void shutdownLogging();

    public void availabilityReportConfigureLogging(String prefix);

    public void configureInstallerLogging();

}
