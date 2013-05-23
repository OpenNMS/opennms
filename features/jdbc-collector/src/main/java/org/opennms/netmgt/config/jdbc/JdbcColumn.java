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

    @Override
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
