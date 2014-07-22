package org.opennms.netmgt.jmx;

public interface JmxSampleProcessor {
    void process(AttributeSample attributeSample);
    void process(CompositeSample compositeSample);
}
