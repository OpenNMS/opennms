package org.opennms.netmgt.config.internal.collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IDataCollectionGroup;
import org.opennms.netmgt.config.api.collection.IGroup;
import org.opennms.netmgt.config.api.collection.IResourceType;
import org.opennms.netmgt.config.api.collection.ISystemDef;
import org.opennms.netmgt.config.api.collection.ITable;

@XmlRootElement(name="datacollection-group")
@XmlAccessorType(XmlAccessType.NONE)
public class DataCollectionGroup implements IDataCollectionGroup {

    @XmlAttribute(name="name")
    String m_name;

    @XmlElement(name="resourceType")
    ResourceType[] m_resourceTypes = new ResourceType[0];

    @XmlElement(name="table")
    Table[] m_tables = new Table[0];

    @XmlElement(name="group")
    Group[] m_groups = new Group[0];

    @XmlElement(name="systemDef")
    SystemDef[] m_systemDefs = new SystemDef[0];

    @Override
    public IGroup[] getGroups() {
        return (IGroup[]) m_groups;
    }

    @Override
    public ITable[] getTables() {
        return (ITable[]) m_tables;
    }

    @Override
    public ISystemDef[] getSystemDefs() {
        return (ISystemDef[]) m_systemDefs;
    }

    @Override
    public IResourceType[] getResourceTypes() {
        return m_resourceTypes;
    }

    @Override
    public String getName() {
        return m_name;
    }

}
