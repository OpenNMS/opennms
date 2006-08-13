package org.opennms.netmgt.model;

import java.util.Collection;

public class AggregateStatusView {
    
    private Integer m_id;
    private String m_name;
    private String m_tableName;
    private String m_columnName;
    private String m_columnValue;
    private Collection<AggregateStatusDefinition> m_statusDefinitions;
    
    
    /*
     * Getters/Setters
     */
    public String getColumnName() {
        return m_columnName;
    }
    public void setColumnName(String columnName) {
        m_columnName = columnName;
    }
    public String getColumnValue() {
        return m_columnValue;
    }
    public void setColumnValue(String columnValue) {
        m_columnValue = columnValue;
    }
    public Integer getId() {
        return m_id;
    }
    public void setId(Integer id) {
        m_id = id;
    }
    public String getName() {
        return m_name;
    }
    public void setName(String name) {
        m_name = name;
    }
    public Collection<AggregateStatusDefinition> getStatusDefinitions() {
        return m_statusDefinitions;
    }
    public void setStatusDefinitions(Collection<AggregateStatusDefinition> statusDefinitions) {
        m_statusDefinitions = statusDefinitions;
    }
    public String getTableName() {
        return m_tableName;
    }
    public void setTableName(String tableName) {
        m_tableName = tableName;
    }
    

}
