/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collectd.tca.config;

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
import org.opennms.netmgt.rrd.RrdRepository;

/**
 * The Class TcaDataCollectionConfig.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="tca-datacollection-config")
public class TcaDataCollectionConfig implements Serializable, Comparable<TcaDataCollectionConfig> {
    private static final long serialVersionUID = 3833896228303605667L;

    /** The Constant TCA_DATACOLLECTION_CONFIG_FILE. */
    public static final String TCA_DATACOLLECTION_CONFIG_FILE = "tca-datacollection-config.xml";

    /** The RRD Repository. */
    @XmlAttribute(name="rrdRepository", required=true)
    private String m_rrdRepository;

    /** The Constant OF_DATA_COLLECTIONS. */
    private static final TcaDataCollection[] OF_DATA_COLLECTIONS = new TcaDataCollection[0];

    /** The TCA data collections list. */
    @XmlElement(name="tca-collection")
    private List<TcaDataCollection> m_tcaDataCollections = new ArrayList<>();

    /**
     * Instantiates a new TCA data collection configuration.
     */
    public TcaDataCollectionConfig() {
    }

    /**
     * Gets the RRD repository.
     *
     * @return the RRD repository
     */
    @XmlTransient
    public String getRrdRepository() {
        return m_rrdRepository;
    }

    /**
     * Sets the RRD repository.
     *
     * @param rrdRepository the new RRD repository
     */
    public void setRrdRepository(final String rrdRepository) {
        m_rrdRepository = rrdRepository;
    }

    /**
     * Gets the TCA data collections.
     *
     * @return the TCA data collections
     */
    @XmlTransient
    public List<TcaDataCollection> getTcaDataCollections() {
        return m_tcaDataCollections;
    }

    /**
     * Sets the TCA data collections.
     *
     * @param tcaDataCollections the new TCA data collections
     */
    public void setTcaDataCollections(final List<TcaDataCollection> tcaDataCollections) {
        m_tcaDataCollections = tcaDataCollections;
    }

    /**
     * Adds the data collection.
     *
     * @param dataCollection the data collection
     */
    public void addDataCollection(final TcaDataCollection dataCollection) {
        m_tcaDataCollections.add(dataCollection);
    }

    /**
     * Removes the data collection.
     *
     * @param dataCollection the data collection
     */
    public void removeDataCollection(final TcaDataCollection dataCollection) {
        m_tcaDataCollections.remove(dataCollection);
    }

    /**
     * Removes the data collection by name.
     *
     * @param name the name
     */
    public void removeDataCollectionByName(final String name) {
        for (final Iterator<TcaDataCollection> itr = m_tcaDataCollections.iterator(); itr.hasNext(); ) {
            final TcaDataCollection dataCollection = itr.next();
            if(dataCollection.getName().equals(name)) {
                m_tcaDataCollections.remove(dataCollection);
                return;
            }
        }
    }

    /**
     * Gets the data collection by name.
     *
     * @param name the name
     * @return the data collection by name
     */
    public TcaDataCollection getDataCollectionByName(final String name) {
        for (final TcaDataCollection dataCol :  m_tcaDataCollections) {
            if(dataCol.getName().equals(name)) {
                return dataCol;
            }
        }
        return null;
    }

    /**
     * Builds the RRD repository.
     *
     * @param collectionName the collection name
     * @return the RRD repository
     */
    public RrdRepository buildRrdRepository(final String collectionName) {
        final TcaDataCollection collection = getDataCollectionByName(collectionName);
        if (collection == null) {
            return null;
        }
        final TcaRrd rrd = collection.getRrd();
        final RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(new File(getRrdRepository()));
        repo.setRraList(rrd.getRras());
        repo.setStep(rrd.getStep()); // Step should be 1 second
        repo.setHeartBeat(rrd.getStep()); // Heartbeat should be equal to the step.
        return repo;
    }

    @Override
    public int compareTo(final TcaDataCollectionConfig obj) {
        return new CompareToBuilder()
        .append(getRrdRepository(), obj.getRrdRepository())
        .append(getTcaDataCollections().toArray(OF_DATA_COLLECTIONS), obj.getTcaDataCollections().toArray(OF_DATA_COLLECTIONS))
        .toComparison();
    }

    @Override
    public int hashCode() {
        final int prime = 1069;
        int result = 1;
        result = prime * result + ((m_rrdRepository == null) ? 0 : m_rrdRepository.hashCode());
        result = prime * result + ((m_tcaDataCollections == null) ? 0 : m_tcaDataCollections.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TcaDataCollectionConfig)) {
            return false;
        }
        final TcaDataCollectionConfig other = (TcaDataCollectionConfig) obj;
        if (m_rrdRepository == null) {
            if (other.m_rrdRepository != null) {
                return false;
            }
        } else if (!m_rrdRepository.equals(other.m_rrdRepository)) {
            return false;
        }
        if (m_tcaDataCollections == null) {
            if (other.m_tcaDataCollections != null) {
                return false;
            }
        } else if (!m_tcaDataCollections.equals(other.m_tcaDataCollections)) {
            return false;
        }
        return true;
    }
}
