package org.opennms.netmgt.collectd;

public interface Persister {

    public abstract void persistNumericAttribute(Attribute attribute);

    public abstract void persistStringAttribute(Attribute attribute);

}