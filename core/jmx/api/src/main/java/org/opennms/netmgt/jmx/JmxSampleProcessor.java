package org.opennms.netmgt.jmx;

import org.opennms.netmgt.jmx.samples.JmxAttributeSample;
import org.opennms.netmgt.jmx.samples.JmxCompositeSample;

public interface JmxSampleProcessor {
    void process(JmxAttributeSample attributeSample);
    void process(JmxCompositeSample compositeSample);
}
