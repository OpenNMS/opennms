package org.opennms.netmgt.jmx;

import org.opennms.netmgt.jmx.connection.MBeanServerConnectionException;

public interface WiuJmxCollector {


    void collect(WiuJmxConfig config, JmxSampleProcessor sampleProcessor) throws MBeanServerConnectionException;
}
