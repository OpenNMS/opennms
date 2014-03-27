package org.opennms.netmgt.config.internal.collection;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IDataCollectionConfig;
import org.opennms.netmgt.config.api.collection.ISnmpCollection;

@XmlRootElement(name="datacollection-config")
@XmlAccessorType(XmlAccessType.NONE)
public class DataCollectionConfigImpl implements IDataCollectionConfig {

    @XmlElement(name="snmp-collection")
    SnmpCollectionImpl[] m_snmpCollections;

    public DataCollectionConfigImpl() {
    }

    public ISnmpCollection[] getSnmpCollections() {
        return m_snmpCollections;
    }

    public void addSnmpCollection(final SnmpCollectionImpl collection) {
        m_snmpCollections = ArrayUtils.append(m_snmpCollections, collection);
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
