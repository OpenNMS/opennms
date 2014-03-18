package org.opennms.netmgt.config.internal.collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IGroupReference;

@XmlRootElement(name="includedGroup")
@XmlAccessorType(XmlAccessType.NONE)
public class GroupReference implements IGroupReference {

    @XmlAttribute(name="dataCollectionGroup")
    public String m_dataCollectionGroup;

    @Override
    public String getDataCollectionGroup() {
        return m_dataCollectionGroup;
    }

    public void setDataCollectionGroup(final String group) {
        m_dataCollectionGroup = group;
    }
}
