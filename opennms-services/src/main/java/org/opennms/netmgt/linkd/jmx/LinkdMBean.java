package org.opennms.netmgt.linkd.jmx;

public interface LinkdMBean
{
	public void init();
	public void start();
	public void stop();
	public int getStatus();
	public String status();
	public String getStatusText();
	
}
