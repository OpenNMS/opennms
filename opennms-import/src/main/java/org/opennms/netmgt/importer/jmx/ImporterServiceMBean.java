package org.opennms.netmgt.importer.jmx;

public interface ImporterServiceMBean {

    public void init();

    public void start();

    public void stop();

    public int getStatus();

    public String status();
	
    public String getStats();

}
