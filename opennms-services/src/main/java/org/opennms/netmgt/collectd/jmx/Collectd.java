package org.opennms.netmgt.collectd.jmx;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;

public class Collectd extends AbstractSpringContextJmxServiceDaemon implements
        CollectdMBean {

    @Override
    protected String getLoggingPrefix() {
        return "OpenNMS.Collectd";
    }

    @Override
    protected String getSpringContext() {
        return "collectdContext";
    }

}
