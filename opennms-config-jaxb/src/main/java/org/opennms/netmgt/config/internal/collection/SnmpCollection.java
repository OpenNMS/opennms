package org.opennms.netmgt.config.internal.collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.netmgt.config.api.collection.IDataCollectionGroup;
import org.opennms.netmgt.config.api.collection.IGroupReference;
import org.opennms.netmgt.config.api.collection.ISnmpCollection;

@XmlRootElement(name="snmp-collection")
@XmlAccessorType(XmlAccessType.NONE)
public class SnmpCollection implements ISnmpCollection {

    @XmlAttribute(name="name")
    private String m_name="default";

    @XmlAttribute(name="snmpStorageFlag")
    private String m_snmpStorageFlag;

    @XmlElement(name="include-collection")
    private GroupReference[] m_includedGroups;

    @XmlTransient
    private DataCollectionGroup[] m_dataCollectionGroups;

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public String getSnmpStorageFlag() {
        return m_snmpStorageFlag;
    }

    @Override
    public IGroupReference[] getIncludedGroups() {
        return m_includedGroups;
    }

    @Override
    public IDataCollectionGroup[] getDataCollectionGroups() {
        return m_dataCollectionGroups;
    }

}
