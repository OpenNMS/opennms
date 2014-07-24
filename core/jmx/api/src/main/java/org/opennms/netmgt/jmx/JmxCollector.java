package org.opennms.netmgt.jmx;

import org.opennms.netmgt.jmx.connection.JmxServerConnectionException;

public interface JmxCollector {


    void collect(JmxCollectorConfig config, JmxSampleProcessor sampleProcessor) throws JmxServerConnectionException;
}
