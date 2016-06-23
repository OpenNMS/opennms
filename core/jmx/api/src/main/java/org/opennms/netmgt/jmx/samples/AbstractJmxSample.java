package org.opennms.netmgt.jmx.samples;

import javax.management.Attribute;

import org.opennms.netmgt.config.collectd.jmx.Mbean;

public abstract class AbstractJmxSample {
    /**
     * The MBean to which the attribute belongs.
     */
    private final Mbean mbean;

    /**
     * The collected attribute returned from MBeanServer.
     */
    private final Attribute attribute;

    public AbstractJmxSample(Mbean mbean, Attribute attribute) {
        this.mbean = mbean;
        this.attribute = attribute;
    }

    public Attribute getCollectedAttribute() {
        return attribute;
    }

    public Mbean getMbean() {
        return mbean;
    }

    public abstract String getCollectedValueAsString();

}
