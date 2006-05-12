package org.opennms.netmgt.collectd;

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
    
    

}
