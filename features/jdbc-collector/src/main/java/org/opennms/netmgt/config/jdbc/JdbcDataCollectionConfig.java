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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.opennms.netmgt.model.RrdRepository;

@XmlRootElement(name="jdbc-datacollection-config")
public class JdbcDataCollectionConfig implements Serializable, Comparable<JdbcDataCollectionConfig> {
    private static final long serialVersionUID = -7884808717236892997L;

    private static final JdbcDataCollection[] OF_DATA_COLLECTIONS = new JdbcDataCollection[0];
    
    @XmlElement(name="jdbc-collection")
    private List<JdbcDataCollection> m_jdbcDataCollections = new ArrayList<JdbcDataCollection>();
    
    @XmlAttribute(name="rrdRepository")
    private String m_rrdRepository;
    
    public JdbcDataCollectionConfig() {
        
    }

    @XmlTransient
    public List<JdbcDataCollection> getJdbcDataCollections() {
        return m_jdbcDataCollections;
    }

    public void setJdbcDataCollections(List<JdbcDataCollection> jdbcDataCollections) {
        m_jdbcDataCollections = jdbcDataCollections;
    }
    
    @XmlTransient
    public String getRrdRepository() {
        return m_rrdRepository;
    }
    
    public void setRrdRepository(String rrdRepository) {
        m_rrdRepository = rrdRepository;
    }
    
    public void addDataCollection(JdbcDataCollection dataCollection) {
        m_jdbcDataCollections.add(dataCollection);
    }
    
    public void removeDataCollection(JdbcDataCollection dataCollection) {
        m_jdbcDataCollections.remove(dataCollection);
    }
    
    public void removeDataCollectionByName(String name) {
        for (Iterator<JdbcDataCollection> itr = m_jdbcDataCollections.iterator(); itr.hasNext(); ) {
            JdbcDataCollection dataCollection = itr.next();
            if(dataCollection.getName().equals(name)) {
                m_jdbcDataCollections.remove(dataCollection);
                return;
            }
        }
    }
    
    public JdbcDataCollection getDataCollectionByName(String name) {
        for (JdbcDataCollection dataCol :  m_jdbcDataCollections) {
            if(dataCol.getName().equals(name)) {
                return dataCol;
            }
        }
        
        return null;
    }
    
    public RrdRepository buildRrdRepository(String collectionName) {
        JdbcRrd rrd = getDataCollectionByName(collectionName).getJdbcRrd();
        RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(new File(getRrdRepository()));
        repo.setRraList(rrd.getJdbcRras());
        repo.setStep(rrd.getStep());
        repo.setHeartBeat((2 * rrd.getStep()));
        return repo;
    }
    
    @Override
    public int compareTo(JdbcDataCollectionConfig obj) {
        return new CompareToBuilder()
            .append(getRrdRepository(), obj.getRrdRepository())
            .append(getJdbcDataCollections().toArray(OF_DATA_COLLECTIONS), obj.getJdbcDataCollections().toArray(OF_DATA_COLLECTIONS))
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JdbcDataCollectionConfig) {
            JdbcDataCollectionConfig other = (JdbcDataCollectionConfig) obj;
            return new EqualsBuilder()
                .append(getRrdRepository(), other.getRrdRepository())
                .append(getJdbcDataCollections().toArray(OF_DATA_COLLECTIONS), other.getJdbcDataCollections().toArray(OF_DATA_COLLECTIONS))
                .isEquals();
        }
        return false;
    }
}
