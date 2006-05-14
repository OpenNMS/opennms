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

    public void visit(CollectionSetVisitor visitor) {
        visitor.visitAttribute(this);
        visitor.completeAttribute(this);
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

    public SnmpValue getValue() {
        return m_val;
    }

    void store(Persister persister) {
        getAttributeType().storeAttribute(this, persister);
    }

    void storeAttribute(Persister persister) {
        getAttributeType().storeAttribute(this, persister);
    }

    public String toString() {
        return getResource()+"."+getAttributeType()+" = "+getValue();
    }

    public String getGroupName() {
        return getAttributeType().getGroupName();
    }
    
    public String getType() {
        return getAttributeType().getType();
    }

    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    public String getGroupIfType() {
        return getAttributeType().getGroupIfType();
    }

    public String getName() {
        return getAttributeType().getName();
    }

}
