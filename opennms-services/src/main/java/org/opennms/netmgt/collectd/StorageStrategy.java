package org.opennms.netmgt.collectd;

public interface StorageStrategy {
    public String getRelativePathForAttribute(String resourceParent, String resource, String attribute);

    public void setResourceTypeName(String name);
}
