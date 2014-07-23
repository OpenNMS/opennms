package org.opennms.netmgt.jmx;

import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.Mbean;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

public class CompositeSample extends AttributeSample {

    private final CompositeData compositeData;

    private final CompMember compositeMember;

    public CompositeSample(Mbean mbean, ObjectName objectName, Attribute attribute, CompositeData compositeData, CompMember compositeMember) {
        super(mbean, objectName, attribute);
        this.compositeData = compositeData;
        this.compositeMember = compositeMember;
    }

    public String getCompositeKey() {
        return compositeMember.getName();
    }

    @Override
    public String getValueAsString() {
        Object value = compositeData.get(getCompositeKey());
        if (value != null) {
            return value.toString();
        }
        return null;
    }
}
