package org.opennms.netmgt.collectd.jdbc;

import org.opennms.netmgt.collectd.AbstractCollectionAttribute;
import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.CollectionAttributeType;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.collectd.ServiceParameters;

public class JdbcCollectionAttribute extends AbstractCollectionAttribute implements CollectionAttribute {
    String m_alias;
    String m_value;
    JdbcCollectionResource m_resource;
    CollectionAttributeType m_attribType;
    
    public JdbcCollectionAttribute(JdbcCollectionResource resource, CollectionAttributeType attribType, String alias, String value) {
        m_resource=resource;
        m_attribType=attribType;
        m_alias = alias;
        m_value = value;
    }
    
    public CollectionAttributeType getAttributeType() {
        return m_attribType;
    }
    
    public String getName() {
        return m_alias;
    }
    
    public String getNumericValue() {
        return m_value;
    }
    
    public CollectionResource getResource() {
        return m_resource;
    }
    
    public String getStringValue() {
        return m_value; //Should this be null instead?
    }
    
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }
    
    public String getType() {
        return m_attribType.getType();
    }
    
    public String toString() {
        return "JdbcCollectionAttribute " + m_alias+"=" + m_value;
    }

}
