/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.jdbc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

public class JdbcQuery implements Serializable, Comparable<JdbcQuery> {
    
    private static final long serialVersionUID = -9083835215058208854L;

    private static final JdbcColumn[] OF_JDBC_COLUMNS = new JdbcColumn[0];
    
    @XmlAttribute(name="name", required=true)
    private String m_queryName;
    
    @XmlAttribute(name="recheckInterval")
    private int m_recheckInterval;
    
    @XmlAttribute(name="ifType", required=true)
    private String m_ifType;
    
    @XmlAttribute(name="resourceType", required=true)
    private String m_resourceType;

    @XmlElement(name="statement", required=true)
    private JdbcStatement m_jdbcStatement;
    
    @XmlAttribute(name="instance-column", required=false)
    private String m_instanceColumn;
    
    @XmlElementWrapper(name="columns")
    @XmlElement(name="column")
    private List<JdbcColumn> m_jdbcColumns = new ArrayList<JdbcColumn>();
    
    @XmlTransient
    public String getQueryName() {
        return m_queryName;
    }
    
    public void setQueryName(String queryName) {
        m_queryName = queryName;
    }
    
    @XmlTransient
    public JdbcStatement getJdbcStatement() {
        return m_jdbcStatement;
    }
    
    public void setJdbcStatement(JdbcStatement jdbcStatement) {
        m_jdbcStatement = jdbcStatement;
    }
    
    @XmlTransient
    public List<JdbcColumn> getJdbcColumns() {
        return m_jdbcColumns;
    }
    
    public void setJdbcColumns(List<JdbcColumn> jdbcColumns) {
        m_jdbcColumns = jdbcColumns;
    }
    
    public void addJdbcColumn(JdbcColumn column) {
        m_jdbcColumns.add(column);
    }
    
    public void removeJdbcColumn(JdbcColumn column) {
        m_jdbcColumns.remove(column);
    }
    
    public void removeColumnByName(String name) {
        for (Iterator<JdbcColumn> itr = m_jdbcColumns.iterator(); itr.hasNext(); ) {
            JdbcColumn column = itr.next();
            if(column.getColumnName().equals(name)) {
                m_jdbcColumns.remove(column);
                return;
            }
        }
    }
    
    @XmlTransient
    public int getRecheckInterval() {
        return m_recheckInterval;
    }

    public void setRecheckInterval(int recheckInterval) {
        m_recheckInterval = recheckInterval;
    }
    
    @XmlTransient
    public String getIfType() {
        return m_ifType;
    }

    public void setIfType(String ifType) {
        m_ifType = ifType;
    }
    
    
    @XmlTransient
    public String getResourceType() {
        return m_resourceType;
    }

    public void setResourceType(String resourceType) {
        m_resourceType = resourceType;
    }

    @XmlTransient
    public String getInstanceColumn() {
        return m_instanceColumn;
    }

    public void setInstanceColumn(String instanceColumn) {
        m_instanceColumn = instanceColumn;
    }

    @Override
    public int compareTo(JdbcQuery obj) {
        return new CompareToBuilder()
            .append(getQueryName(), obj.getQueryName())
            .append(getJdbcStatement(), obj.getJdbcStatement())
            .append(getJdbcColumns().toArray(OF_JDBC_COLUMNS), obj.getJdbcColumns().toArray(OF_JDBC_COLUMNS))
            .append(getRecheckInterval(), obj.getRecheckInterval())
            .append(getIfType(), obj.getIfType())
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JdbcQuery) {
            JdbcQuery other = (JdbcQuery) obj;
            return new EqualsBuilder()
                .append(getQueryName(), other.getQueryName())
                .append(getJdbcStatement(), other.getJdbcStatement())
                .append(getJdbcColumns().toArray(OF_JDBC_COLUMNS), other.getJdbcColumns().toArray(OF_JDBC_COLUMNS))
                .isEquals();
        }
        return false;
    }
}
