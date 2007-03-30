package org.opennms.netmgt.daemon;

public interface BaseOnmsMBean {

    public abstract void init();

    public abstract void start();

    public abstract void stop();

    public abstract int getStatus();

    public abstract String status();

    public abstract String getStatusText();

}