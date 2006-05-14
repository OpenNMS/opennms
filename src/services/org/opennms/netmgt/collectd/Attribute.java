package org.opennms.netmgt.collectd;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpValue;

public class Attribute {

    private CollectionResource m_resource;
    private AttributeType m_type;
    private SnmpValue m_val;

    public Attribute(CollectionResource resource, AttributeType type, SnmpValue val) {
        m_resource = resource;
        m_type = type;
        m_val = val;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Attribute) {
            Attribute attr = (Attribute) obj;
            return (m_resource.equals(attr.m_resource) && m_type.equals(m_type));
        }
        return false;
    }

    public int hashCode() {
        return (m_resource.hashCode() ^ m_type.hashCode());
    }

    public void visitAttribute(CollectionSetVisitor visitor) {
        visitor.visitAttribute(this);
    }

    public AttributeType getAttributeType() {
        return m_type;
    }

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public CollectionResource getResource() {
        return m_resource;
    }

    void logNoDataForAttribute() {
        Category log = log();
        if (log.isDebugEnabled()) {
            log.debug(
        			"updateRRDs: Skipping update, "
        					+ "no data retrieved for resource: " + getResource() + 
                            " attribute: " + getAttributeType().getName());
        }
    }

    void logUpdateException(IllegalArgumentException e) {
        log().warn("updateRRDs: exception saving data for resource: " + getResource()
        + " datasource: " + getAttributeType().getName(), e);
    }

    public SnmpValue getValue() {
        return m_val;
    }

    void logUpdateFailed() {
        log().warn("updateRRDs: ds.performUpdate() failed for resource: "
        + getResource()
        + " datasource: "
        + getAttributeType().getName());
    }

    void store(RrdRepository repository) {
        if (getAttributeType().performUpdate(repository, this)) {
            logUpdateFailed();
        }
    }

    void storeAttribute(RrdRepository repository) {
        try {
            if (getValue() == null) {
                logNoDataForAttribute();
            } else {
                store(repository);
            }
        } catch (IllegalArgumentException e) {
            logUpdateException(e);
        }
    }

}
