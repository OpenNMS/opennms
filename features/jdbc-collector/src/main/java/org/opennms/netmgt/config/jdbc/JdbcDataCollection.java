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
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

@XmlType(name="jdbc-collection")
public class JdbcDataCollection implements Serializable, Comparable<JdbcDataCollection> {
    private static final long serialVersionUID = -7451959128852991463L;
    
    private static final JdbcQuery[] OF_QUERIES = new JdbcQuery[0];
    
    @XmlAttribute(name="name")
    private String m_name;
    
    @XmlElement(name="rrd")
    private JdbcRrd m_jdbcRrd;
    
    @XmlElementWrapper(name="queries")
    @XmlElement(name="query")
    private List<JdbcQuery> m_jdbcQueries = new ArrayList<JdbcQuery>();
    
    public JdbcDataCollection() {
        
    }
    
    @XmlTransient
    public JdbcRrd getJdbcRrd() {
        return m_jdbcRrd;
    }
    
    public void setJdbcRrd(JdbcRrd jdbcRrd) {
        m_jdbcRrd = jdbcRrd;
    }
    
    @XmlTransient
    public List<JdbcQuery> getQueries() {
        return m_jdbcQueries;
    }

    public void setQueries(List<JdbcQuery> jdbcQueries) {
        m_jdbcQueries = jdbcQueries;
    }

    @XmlTransient
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public void addQuery(JdbcQuery query) {
        m_jdbcQueries.add(query);
    }
    
    public void removeQuery(JdbcQuery query) {
        m_jdbcQueries.remove(query);
    }
    
    public void removeQueryByName(String name) {
        for (Iterator<JdbcQuery> itr = m_jdbcQueries.iterator(); itr.hasNext(); ) {
            JdbcQuery query = itr.next();
            if(query.getQueryName().equals(name)) {
                m_jdbcQueries.remove(query);
                return;
            }
        }
    }
    
    @Override
    public int compareTo(JdbcDataCollection obj) {
        return new CompareToBuilder()
            .append(getName(), obj.getName())
            .append(getJdbcRrd(), obj.getJdbcRrd())
            .append(getQueries().toArray(OF_QUERIES), obj.getQueries().toArray(OF_QUERIES))
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JdbcDataCollection) {
            JdbcDataCollection other = (JdbcDataCollection) obj;
            return new EqualsBuilder()
                .append(getName(), other.getName())
                .append(getJdbcRrd(), other.getJdbcRrd())
                .append(getQueries().toArray(OF_QUERIES), other.getQueries().toArray(OF_QUERIES))
                .isEquals();
        }
        return false;
    }
    
}
