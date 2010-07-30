package org.opennms.netmgt.config.jdbc;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

@XmlRootElement(name="column")
public class JdbcColumn implements Serializable, Comparable<JdbcColumn> {

    private static final long serialVersionUID = 2519632811400677757L;

    @XmlAttribute(name="name", required=true)
    private String m_columnName;
    
    @XmlAttribute(name="data-source-name", required=false)
    private String m_dataSourceName;
    
    @XmlAttribute(name="type", required=true)    
    private String m_dataType;
    
    @XmlAttribute(name="alias", required=true)
    private String m_alias;
    
    @XmlTransient
    public String getColumnName() {
        return m_columnName;
    }
    
    public void setColumnName(String columnName) {
        m_columnName = columnName;
    }
    
    @XmlTransient
    public String getDataSourceName() {
        return m_dataSourceName;
    }
    
    public void setDataSourceName(String dataSourceName) {
        m_dataSourceName = dataSourceName;
    }
    
    @XmlTransient
    public String getDataType() {
        return m_dataType;
    }
    
    public void setDataType(String dataType) {
        m_dataType = dataType;
    }
    
    
    @XmlTransient
    public String getAlias() {
        return m_alias;
    }

    public void setAlias(String alias) {
        m_alias = alias;
    }

    public int compareTo(JdbcColumn obj) {
        return new CompareToBuilder()
            .append(getColumnName(), obj.getColumnName())
            .append(getDataSourceName(), obj.getDataSourceName())
            .append(getDataType(), obj.getDataType())
            .append(getAlias(), obj.getAlias())
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JdbcColumn) {
            JdbcColumn other = (JdbcColumn) obj;
            return new EqualsBuilder()
                .append(getColumnName(), other.getColumnName())
                .append(getDataSourceName(), other.getDataSourceName())
                .append(getDataType(), other.getDataType())
                .append(getAlias(), other.getAlias())
                .isEquals();
        }
        return false;
    }
}
