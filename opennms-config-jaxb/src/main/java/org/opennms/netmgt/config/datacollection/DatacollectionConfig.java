/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.datacollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.api.collection.IDataCollectionConfig;
import org.opennms.netmgt.config.internal.collection.DataCollectionConfigConverter;
import org.opennms.netmgt.config.internal.collection.DatacollectionConfigVisitor;

/**
 * Top-level element for the datacollection-config.xml
 *  configuration file.
 */

@XmlRootElement(name="datacollection-config", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("datacollection-config.xsd")
public class DatacollectionConfig implements Serializable {
    private static final long serialVersionUID = 8822093542080103175L;
    private static final List<IncludeCollection> EMPTY_INCLUDE_LIST = Collections.emptyList();

    /**
     * full path to the RRD repository for collected SNMP data
     */
    @XmlAttribute(name="rrdRepository")
    private String m_rrdRepository;

    /**
     * SNMP data collection element
     */
    @XmlElement(name="snmp-collection")
    private List<SnmpCollection> m_snmpCollections = new ArrayList<>();


    public DatacollectionConfig() {
        super();
    }

    /**
     * full path to the RRD repository for collected SNMP data
     */
    public String getRrdRepository() {
        return m_rrdRepository;
    }

    public void setRrdRepository(final String rrdRepository) {
        m_rrdRepository = rrdRepository.intern();
    }

    public List<SnmpCollection> getSnmpCollections() {
        if (m_snmpCollections == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_snmpCollections);
        }
    }

    public void setSnmpCollections(final List<SnmpCollection> snmpCollections) {
        m_snmpCollections = new ArrayList<SnmpCollection>(snmpCollections);
    }

    public SnmpCollection getSnmpCollection(final String name) {
        for (final SnmpCollection collection : m_snmpCollections) {
            if (name.equals(collection.getName())) {
                return collection;
            }
        }
        return null;
    }

    public void addSnmpCollection(final SnmpCollection snmpCollection) throws IndexOutOfBoundsException {
        m_snmpCollections.add(snmpCollection);
    }

    public boolean removeSnmpCollection(final SnmpCollection snmpCollection) {
        return m_snmpCollections.remove(snmpCollection);
    }

    public void insertSnmpCollection(final SnmpCollection resourceTypeCollection) {
        m_snmpCollections.add(0, resourceTypeCollection);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_rrdRepository == null) ? 0 : m_rrdRepository.hashCode());
        result = prime * result + ((m_snmpCollections == null) ? 0 : m_snmpCollections.hashCode());
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
        if (!(obj instanceof DatacollectionConfig)) {
            return false;
        }
        final DatacollectionConfig other = (DatacollectionConfig) obj;
        if (m_rrdRepository == null) {
            if (other.m_rrdRepository != null) {
                return false;
            }
        } else if (!m_rrdRepository.equals(other.m_rrdRepository)) {
            return false;
        }
        if (m_snmpCollections == null) {
            if (other.m_snmpCollections != null) {
                return false;
            }
        } else if (!m_snmpCollections.equals(other.m_snmpCollections)) {
            return false;
        }
        return true;
    }

    public IDataCollectionConfig toDataCollectionConfig() {
        final DatacollectionConfig modifiable = new DatacollectionConfig();
        modifiable.setRrdRepository(this.getRrdRepository());

        final String resourceTypeName = "__resource_type_collection";
        final SnmpCollection resourceTypeCollection = this.getSnmpCollection(resourceTypeName);

        for (final SnmpCollection collection : this.getSnmpCollections()) {
            if (resourceTypeName.equals(collection.getName())) {
                // skip the special case collection
                continue;
            }
            final SnmpCollection cloned = collection.clone();
            // DefaultDataCollectionConfigDao already does all the include work, so don't pass them along
            cloned.setIncludeCollections(EMPTY_INCLUDE_LIST);
            if (resourceTypeCollection != null) {
                cloned.setResourceTypes(resourceTypeCollection.getResourceTypes());
            }
            modifiable.addSnmpCollection(cloned);
        }

        final DataCollectionConfigConverter converter = new DataCollectionConfigConverter();
        modifiable.visit(converter);
        return converter.getDataCollectionConfig();
    }

    @Override
    public String toString() {
        return "DatacollectionConfig [rrdRepository=" + m_rrdRepository + ", snmpCollections=" + m_snmpCollections + "]";
    }

    public void visit(final DatacollectionConfigVisitor visitor) {
        visitor.visitDatacollectionConfig(this);

        for (final SnmpCollection collection : m_snmpCollections) {
            collection.visit(visitor);
        }

        visitor.visitDatacollectionConfigComplete();
    }

}
