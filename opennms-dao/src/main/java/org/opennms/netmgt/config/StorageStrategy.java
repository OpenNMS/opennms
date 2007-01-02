package org.opennms.netmgt.config;

public interface StorageStrategy {
    public String getRelativePathForAttribute(String resourceParent, String resource, String attribute);

    public void setResourceTypeName(String name);
}
