package org.opennms.netmgt.config.internal.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IDataCollectionConfig;
import org.opennms.netmgt.config.api.collection.ISnmpCollection;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.SnmpCollection;

@XmlRootElement(name="datacollection-config")
@XmlAccessorType(XmlAccessType.NONE)
public class DataCollectionConfigImpl implements IDataCollectionConfig {

    @XmlElement(name="snmp-collection")
    SnmpCollectionImpl[] m_snmpCollections;

    public DataCollectionConfigImpl() {
    }

    public DataCollectionConfigImpl(final DatacollectionConfig oldConfig) {
        final List<SnmpCollectionImpl> newCollections = new ArrayList<SnmpCollectionImpl>();
        for (final SnmpCollection oldCollection : oldConfig.getSnmpCollections()) {
            newCollections.add(new SnmpCollectionImpl(oldCollection));
        }
        m_snmpCollections = newCollections.toArray(new SnmpCollectionImpl[newCollections.size()]);
    }

    public ISnmpCollection[] getSnmpCollections() {
        return m_snmpCollections;
    }

    public void addSnmpCollection(final SnmpCollectionImpl collection) {
        final List<SnmpCollectionImpl> collections = m_snmpCollections == null? new ArrayList<SnmpCollectionImpl>() : Arrays.asList(m_snmpCollections);
        collections.add(collection);
        m_snmpCollections = collections.toArray(new SnmpCollectionImpl[collections.size()]);
    }

    @Override
    public String toString() {
        return "DataCollectionConfig [snmpCollections=" + Arrays.toString(m_snmpCollections) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(m_snmpCollections);
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
        if (!(obj instanceof DataCollectionConfigImpl)) {
            return false;
        }
        final DataCollectionConfigImpl other = (DataCollectionConfigImpl) obj;
        if (!Arrays.equals(m_snmpCollections, other.m_snmpCollections)) {
            return false;
        }
        return true;
    }

}
