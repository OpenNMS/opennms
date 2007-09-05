package org.opennms.netmgt.queued.jmx;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;

public class Queued extends AbstractSpringContextJmxServiceDaemon implements
        QueuedMBean {

    @Override
    protected String getLoggingPrefix() {
        return "OpenNMS.Queued";
    }

    @Override
    protected String getSpringContext() {
        return "queuedContext";
    }

}
