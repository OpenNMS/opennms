package org.opennms.netmgt.config.internal.collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IDataCollectionConfig;
import org.opennms.netmgt.config.api.collection.ISnmpCollection;

@XmlRootElement(name="datacollection-config")
@XmlAccessorType(XmlAccessType.NONE)
public class DataCollectionConfig implements IDataCollectionConfig {

    @XmlElement(name="snmp-collection")
    SnmpCollection[] m_snmpCollections;

    public ISnmpCollection[] getSnmpCollections() {
        return m_snmpCollections;
    }

}
