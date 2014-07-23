package org.opennms.netmgt.jmx;

import org.opennms.netmgt.config.collectd.jmx.Mbean;

import javax.management.Attribute;
import javax.management.ObjectName;

public class AttributeSample {
    private final Mbean mbean;
    private final ObjectName objectName;
    private final Attribute attribute;

    public AttributeSample(Mbean mbean, ObjectName objectName, Attribute attribute) {
        this.mbean = mbean;
        this.objectName = objectName;
        this.attribute = attribute;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public Mbean getMbean() {
        return mbean;
    }

    public String getValueAsString() {
        final Object value = attribute.getValue();
        if (value != null) {
            return value.toString();
        }
        return null;
    }
}
