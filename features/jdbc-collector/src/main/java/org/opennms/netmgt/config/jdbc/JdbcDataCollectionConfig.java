/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
import org.opennms.netmgt.rrd.RrdRepository;

@XmlRootElement(name="jdbc-datacollection-config")
public class JdbcDataCollectionConfig implements Serializable, Comparable<JdbcDataCollectionConfig> {
    private static final long serialVersionUID = -7884808717236892997L;

    private static final JdbcDataCollection[] OF_DATA_COLLECTIONS = new JdbcDataCollection[0];
    
    @XmlElement(name="jdbc-collection")
    private List<JdbcDataCollection> m_jdbcDataCollections = new ArrayList<>();
    
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
