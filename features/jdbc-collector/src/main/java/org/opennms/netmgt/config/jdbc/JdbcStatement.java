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
    
    @Override
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
