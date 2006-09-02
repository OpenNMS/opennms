package org.opennms.netmgt.collectd;

import java.io.File;

import org.opennms.netmgt.utils.RrdFileConstants;

public class IndexStorageStrategy implements StorageStrategy {
    private String m_resourceTypeName;

    public String getRelativePathForAttribute(String resourceParent, String resource,
            String attribute) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(resourceParent);
        buffer.append(File.separator);
        buffer.append(m_resourceTypeName);
        buffer.append(File.separator);
        buffer.append(resource);
        buffer.append(File.separator);
        buffer.append(attribute);
        buffer.append(RrdFileConstants.RRD_SUFFIX);
        return buffer.toString();
    }

    public void setResourceTypeName(String name) {
        m_resourceTypeName = name;
    }

    public String getResourceTypeName() {
        return m_resourceTypeName;
    }
}
