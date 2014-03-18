package org.opennms.netmgt.config.internal.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.netmgt.config.api.collection.IDataCollectionGroup;
import org.opennms.netmgt.config.api.collection.IGroupReference;
import org.opennms.netmgt.config.api.collection.IRrd;
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

    @XmlElement(name="rrd")
    private Rrd m_rrd;

    @XmlTransient
    private DataCollectionGroup[] m_dataCollectionGroups;

    public SnmpCollection() {
    }

    public SnmpCollection(final String name, final String snmpStorageFlag) {
        m_name = name;
        m_snmpStorageFlag = snmpStorageFlag;
    }

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

    public void addIncludedGroup(final String groupName) {
        final List<GroupReference> groups = m_includedGroups == null? new ArrayList<GroupReference>() : new ArrayList<GroupReference>(Arrays.asList(m_includedGroups));
        groups.add(new GroupReference(groupName));
        m_includedGroups = groups.toArray(new GroupReference[groups.size()]);
    }

    @Override
    public IDataCollectionGroup[] getDataCollectionGroups() {
        return m_dataCollectionGroups;
    }

    @Override
    public IRrd getRrd() {
        return m_rrd;
    }

    public void setRrd(final Rrd rrd) {
        m_rrd = rrd;
    }

    @Override
    public String toString() {
        return "SnmpCollection [name=" + m_name + ", snmpStorageFlag=" + m_snmpStorageFlag + ", includedGroups=" + Arrays.toString(m_includedGroups) + ", rrd=" + m_rrd
                + ", dataCollectionGroups=" + Arrays.toString(m_dataCollectionGroups) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(m_dataCollectionGroups);
        result = prime * result + Arrays.hashCode(m_includedGroups);
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_rrd == null) ? 0 : m_rrd.hashCode());
        result = prime * result + ((m_snmpStorageFlag == null) ? 0 : m_snmpStorageFlag.hashCode());
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
        if (!(obj instanceof SnmpCollection)) {
            return false;
        }
        final SnmpCollection other = (SnmpCollection) obj;
        if (!Arrays.equals(m_dataCollectionGroups, other.m_dataCollectionGroups)) {
            return false;
        }
        if (!Arrays.equals(m_includedGroups, other.m_includedGroups)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_rrd == null) {
            if (other.m_rrd != null) {
                return false;
            }
        } else if (!m_rrd.equals(other.m_rrd)) {
            return false;
        }
        if (m_snmpStorageFlag == null) {
            if (other.m_snmpStorageFlag != null) {
                return false;
            }
        } else if (!m_snmpStorageFlag.equals(other.m_snmpStorageFlag)) {
            return false;
        }
        return true;
    }

}
