package org.opennms.netmgt.poller.jmx;

public interface RemotePollerBackEndMBean {
    public void init();

    public void start();

    public void stop();

    public int getStatus();

    public String status();

    public String getStatusText();

}
