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

    public DataCollectionGroup() {
    }

    public DataCollectionGroup(final String name) {
        m_name = name;
    }

    @Override
    public IGroup[] getGroups() {
        return (IGroup[]) m_groups;
    }

    @Override
    public ITable[] getTables() {
        return (ITable[]) m_tables;
    }

    public void addTable(final Table table) {
        final List<Table> tables = m_tables == null? new ArrayList<Table>() : new ArrayList<Table>(Arrays.asList(m_tables));
        tables.add(table);
        m_tables = tables.toArray(new Table[tables.size()]);
    }

    @Override
    public ISystemDef[] getSystemDefs() {
        return (ISystemDef[]) m_systemDefs;
    }

    public void addSystemDef(final SystemDef def) {
        final List<SystemDef> defs = m_systemDefs == null? new ArrayList<SystemDef>() : new ArrayList<SystemDef>(Arrays.asList(m_systemDefs));
        defs.add(def);
        m_systemDefs = defs.toArray(new SystemDef[defs.size()]);
    }

    @Override
    public IResourceType[] getResourceTypes() {
        return m_resourceTypes;
    }

    public void addResourceType(final ResourceType resourceType) {
        final List<ResourceType> types = m_resourceTypes == null? new ArrayList<ResourceType>() : new ArrayList<ResourceType>(Arrays.asList(m_resourceTypes));
        types.add(resourceType);
        m_resourceTypes = types.toArray(new ResourceType[types.size()]);
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public String toString() {
        return "DataCollectionGroup [name=" + m_name + ", resourceTypes=" + Arrays.toString(m_resourceTypes) + ", tables=" + Arrays.toString(m_tables) + ", groups="
                + Arrays.toString(m_groups) + ", systemDefs=" + Arrays.toString(m_systemDefs) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(m_groups);
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + Arrays.hashCode(m_resourceTypes);
        result = prime * result + Arrays.hashCode(m_systemDefs);
        result = prime * result + Arrays.hashCode(m_tables);
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
        if (!(obj instanceof DataCollectionGroup)) {
            return false;
        }
        final DataCollectionGroup other = (DataCollectionGroup) obj;
        if (!Arrays.equals(m_groups, other.m_groups)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (!Arrays.equals(m_resourceTypes, other.m_resourceTypes)) {
            return false;
        }
        if (!Arrays.equals(m_systemDefs, other.m_systemDefs)) {
            return false;
        }
        if (!Arrays.equals(m_tables, other.m_tables)) {
            return false;
        }
        return true;
    }

}
