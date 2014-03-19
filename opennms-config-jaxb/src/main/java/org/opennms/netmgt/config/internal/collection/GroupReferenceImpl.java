package org.opennms.netmgt.config.internal.collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IGroupReference;

@XmlRootElement(name="includedGroup")
@XmlAccessorType(XmlAccessType.NONE)
public class GroupReferenceImpl implements IGroupReference {

    @XmlAttribute(name="dataCollectionGroup")
    public String m_dataCollectionGroup;

    public GroupReferenceImpl() {
    }

    public GroupReferenceImpl(final String groupName) {
        m_dataCollectionGroup = groupName;
    }

    @Override
    public String getDataCollectionGroup() {
        return m_dataCollectionGroup;
    }

    public void setDataCollectionGroup(final String group) {
        m_dataCollectionGroup = group;
    }

    @Override
    public String toString() {
        return "GroupReferenceImpl [dataCollectionGroup=" + m_dataCollectionGroup + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_dataCollectionGroup == null) ? 0 : m_dataCollectionGroup.hashCode());
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
        if (!(obj instanceof GroupReferenceImpl)) {
            return false;
        }
        final GroupReferenceImpl other = (GroupReferenceImpl) obj;
        if (m_dataCollectionGroup == null) {
            if (other.m_dataCollectionGroup != null) {
                return false;
            }
        } else if (!m_dataCollectionGroup.equals(other.m_dataCollectionGroup)) {
            return false;
        }
        return true;
    }
}
