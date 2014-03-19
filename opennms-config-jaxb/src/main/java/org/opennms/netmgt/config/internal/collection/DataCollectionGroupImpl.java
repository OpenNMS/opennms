package org.opennms.netmgt.config.internal.collection;

import java.util.Arrays;

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
public class DataCollectionGroupImpl implements IDataCollectionGroup {

    @XmlAttribute(name="name")
    String m_name;

    @XmlElement(name="resourceType")
    ResourceTypeImpl[] m_resourceTypes = new ResourceTypeImpl[0];

    @XmlElement(name="table")
    TableImpl[] m_tables = new TableImpl[0];

    @XmlElement(name="group")
    GroupImpl[] m_groups = new GroupImpl[0];

    @XmlElement(name="systemDef")
    SystemDefImpl[] m_systemDefs = new SystemDefImpl[0];

    public DataCollectionGroupImpl() {
    }

    public DataCollectionGroupImpl(final String name) {
        m_name = name;
    }

    @Override
    public IGroup[] getGroups() {
        return (IGroup[]) m_groups;
    }

    public void addGroup(final GroupImpl group) {
        m_groups = ArrayUtils.append(m_groups, group);
    }

    @Override
    public ITable[] getTables() {
        return (ITable[]) m_tables;
    }

    public void addTable(final TableImpl table) {
        m_tables = ArrayUtils.append(m_tables, table);
    }

    @Override
    public ISystemDef[] getSystemDefs() {
        return (ISystemDef[]) m_systemDefs;
    }

    public void addSystemDef(final SystemDefImpl def) {
        m_systemDefs = ArrayUtils.append(m_systemDefs, def);
    }

    @Override
    public IResourceType[] getResourceTypes() {
        return m_resourceTypes;
    }

    public void addResourceType(final ResourceTypeImpl resourceType) {
        m_resourceTypes = ArrayUtils.append(m_resourceTypes, resourceType);
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public String toString() {
        return "DataCollectionGroupImpl [name=" + m_name + ", resourceTypes=" + Arrays.toString(m_resourceTypes) + ", tables=" + Arrays.toString(m_tables) + ", groups="
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
        if (!(obj instanceof DataCollectionGroupImpl)) {
            return false;
        }
        final DataCollectionGroupImpl other = (DataCollectionGroupImpl) obj;
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
