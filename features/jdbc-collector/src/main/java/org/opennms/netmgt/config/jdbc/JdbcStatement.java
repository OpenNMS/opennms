package org.opennms.netmgt.config.jdbc;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

public class JdbcStatement implements Serializable, Comparable<JdbcStatement> {

    private static final long serialVersionUID = 883422287764280313L;
    
    @XmlElement(name="queryString",required=true)
    private String m_jdbcQuery;
    
    @XmlTransient
    public String getJdbcQuery() {
        return m_jdbcQuery;
    }
    
    public void setJdbcQuery(String jdbcQuery) {
        m_jdbcQuery = jdbcQuery;
    }
    
    public int compareTo(JdbcStatement obj) {
        return new CompareToBuilder()
            .append(getJdbcQuery(), obj.getJdbcQuery())
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JdbcStatement) {
            JdbcStatement other = (JdbcStatement) obj;
            return new EqualsBuilder()
                .append(getJdbcQuery(), other.getJdbcQuery())
                .isEquals();
        }
        return false;
    }
}
