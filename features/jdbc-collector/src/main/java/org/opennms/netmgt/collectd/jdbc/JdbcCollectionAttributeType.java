package org.opennms.netmgt.collectd.jdbc;

import org.opennms.netmgt.collectd.AttributeGroupType;
import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.CollectionAttributeType;
import org.opennms.netmgt.collectd.Persister;
import org.opennms.netmgt.config.jdbc.JdbcColumn;

public class JdbcCollectionAttributeType implements CollectionAttributeType {
    JdbcColumn m_column;
    AttributeGroupType m_groupType;
    
    public JdbcCollectionAttributeType(JdbcColumn column, AttributeGroupType groupType) {
        m_groupType=groupType;
        m_column=column;
    }
    
    public AttributeGroupType getGroupType() {
        return m_groupType;
    }
    
    public void storeAttribute(CollectionAttribute attribute, Persister persister) {
        if (m_column.getDataType().equalsIgnoreCase("string")) {
            persister.persistStringAttribute(attribute);
        } else {
            persister.persistNumericAttribute(attribute);
        }
    }
    
    public String getName() {
        return m_column.getAlias();
    }
    
    public String getType() {
        return m_column.getDataType();
    }
}
