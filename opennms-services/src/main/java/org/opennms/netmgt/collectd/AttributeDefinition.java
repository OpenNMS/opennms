package org.opennms.netmgt.collectd;

public interface AttributeDefinition {

    public abstract String getType();

    public abstract String getName();
    
    public abstract boolean equals(Object o);
    
    public abstract int hashCode();

}