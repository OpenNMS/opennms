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
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

@XmlRootElement(name="rrd")
public class JdbcRrd implements Serializable, Comparable<JdbcRrd> {

    private static final long serialVersionUID = 143526958273169546L;
    
    @XmlAttribute(name="step")
    private Integer m_step;
    
    @XmlElement(name="rra")
    private List<String> m_jdbcRras = new ArrayList<String>();
    
    public JdbcRrd() {
        
    }

    @XmlTransient
    public Integer getStep() {
        return m_step;
    }

    public void setStep(Integer step) {
        m_step = step;
    }

    @XmlTransient
    public List<String> getJdbcRras() {
        return m_jdbcRras;
    }

    public void setJdbcRras(List<String> jdbcRras) {
        m_jdbcRras = jdbcRras;
    }
    
    public void addRra(String rra) {
        m_jdbcRras.add(rra);
    }
    
    public void removeRra(String rra) {
        m_jdbcRras.remove(rra);
    }
    
    @Override
    public int compareTo(JdbcRrd obj) {
        return new CompareToBuilder()
            .append(getStep(), obj.getStep())
            .append(getJdbcRras().toArray(), obj.getJdbcRras().toArray())
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JdbcRrd) {
            JdbcRrd other = (JdbcRrd) obj;
            return new EqualsBuilder()
                .append(getStep(), other.getStep())
                .append(getJdbcRras().toArray(), other.getJdbcRras().toArray())
                .isEquals();
        }
        return false;
    }
}
