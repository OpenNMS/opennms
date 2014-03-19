package org.opennms.netmgt.config.internal.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IDataCollectionGroup;
import org.opennms.netmgt.config.api.collection.IGroupReference;
import org.opennms.netmgt.config.api.collection.ISnmpCollection;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SnmpCollection;

@XmlRootElement(name="snmp-collection")
@XmlAccessorType(XmlAccessType.NONE)
public class SnmpCollectionImpl implements ISnmpCollection {

    @XmlAttribute(name="name")
    private String m_name="default";

    @XmlElement(name="include-collection")
    private GroupReferenceImpl[] m_includedGroups;

    @XmlElement(name="datacollection-group")
    private DataCollectionGroupImpl[] m_dataCollectionGroups;

    public SnmpCollectionImpl() {
    }

    public SnmpCollectionImpl(final String name) {
        m_name = name;
    }

    public SnmpCollectionImpl(final SnmpCollection oldCollection) {
        m_name = oldCollection.getName();
        final DataCollectionGroupImpl dcg = new DataCollectionGroupImpl("all");

        final ResourceTypeImpl ifIndexResourceType = new ResourceTypeImpl("ifIndex", "Interfaces (MIB-2 ifTable)");
        ifIndexResourceType.setResourceNameTemplate("${ifDescr}-${ifPhysAddr}");
        ifIndexResourceType.setResourceLabelTemplate("${ifDescr}-${ifPhysAddr}");
        ifIndexResourceType.setResourceKindTemplate("${ifType}");
        ifIndexResourceType.addColumn(".1.3.6.1.2.1.2.2.1.2", "ifDescr", "string");
        ifIndexResourceType.addColumn(".1.3.6.1.2.1.2.2.1.6", "ifPhysAddr", "string", "1x:"); 
        ifIndexResourceType.addColumn(".1.3.6.1.2.1.2.2.1.3", "ifType", "string");
        ifIndexResourceType.addColumn(".1.3.6.1.2.1.31.1.1.1.1", "ifName", "string");
        dcg.addResourceType(ifIndexResourceType);

        for (final ResourceType oldResourceType : oldCollection.getResourceTypes()) {
            final ResourceTypeImpl newResourceType = new ResourceTypeImpl(oldResourceType);
            dcg.addResourceType(newResourceType);
        }

        m_dataCollectionGroups = new DataCollectionGroupImpl[] { dcg };
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public IGroupReference[] getIncludedGroups() {
        return m_includedGroups;
    }

    public void addIncludedGroup(final String groupName) {
        final List<GroupReferenceImpl> groups = m_includedGroups == null? new ArrayList<GroupReferenceImpl>() : new ArrayList<GroupReferenceImpl>(Arrays.asList(m_includedGroups));
        groups.add(new GroupReferenceImpl(groupName));
        m_includedGroups = groups.toArray(new GroupReferenceImpl[groups.size()]);
    }

    @Override
    public IDataCollectionGroup[] getDataCollectionGroups() {
        return m_dataCollectionGroups;
    }

    public void addDataCollectionGroup(final DataCollectionGroupImpl group) {
        final List<DataCollectionGroupImpl> groups = m_dataCollectionGroups == null? new ArrayList<DataCollectionGroupImpl>() : new ArrayList<DataCollectionGroupImpl>(Arrays.asList(m_dataCollectionGroups));
        groups.add(group);
        m_dataCollectionGroups = groups.toArray(new DataCollectionGroupImpl[groups.size()]);
    }

    @Override
    public String toString() {
        return "SnmpCollectionImpl [name=" + m_name + ", includedGroups=" + Arrays.toString(m_includedGroups)
                + ", dataCollectionGroups=" + Arrays.toString(m_dataCollectionGroups) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(m_dataCollectionGroups);
        result = prime * result + Arrays.hashCode(m_includedGroups);
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
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
        if (!(obj instanceof SnmpCollectionImpl)) {
            return false;
        }
        final SnmpCollectionImpl other = (SnmpCollectionImpl) obj;
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
        return true;
    }

}
