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

import org.opennms.netmgt.config.api.collection.IColumn;
import org.opennms.netmgt.config.api.collection.ITable;

/**
 *  <table name="mib2-host-resources-storage" instance="hrStorageIndex">"
 *      <column oid=".1.3.6.1.2.1.25.2.3.1.4" alias="hrStorageAllocUnits" type="gauge" />
 *      <column oid=".1.3.6.1.2.1.25.2.3.1.5" alias="hrStorageSize"       type="gauge" />
 *      <column oid=".1.3.6.1.2.1.25.2.3.1.6" alias="hrStorageUse\"       type="gauge" />
 *  </table>
 *  
 * @author brozow
 *
 */
@XmlRootElement(name="table")
@XmlAccessorType(XmlAccessType.NONE)
public class Table implements ITable {

    @XmlAttribute(name="name")
    private String m_name;

    @XmlAttribute(name="instance")
    private String m_instance;

    @XmlAttribute(name="ifType")
    private String m_ifType;

    @XmlElement(name="column")
    private Column[] m_columns;

    @XmlTransient
    private ResourceType m_resourceType;

    public Table() {
    }

    public Table(final String name, final String instance, final String ifType) {
        m_name = name;
        m_instance = instance;
        m_ifType = ifType;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getInstance() {
        return m_instance;
    }

    public void setInstance(String instance) {
        m_instance = instance;
    }

    @Override
    public String getIfType() {
        return m_ifType;
    }

    public void setIfType(final String ifType) {
        m_ifType = ifType;
    }

    @Override
    public IColumn[] getColumns() {
        return m_columns;
    }

    public void setColumns(final IColumn[] columns) {
        m_columns = Column.asColumns(columns);
    }

    public void addColumn(final Column column) {
        final List<Column> columns = m_columns == null? new ArrayList<Column>() : new ArrayList<Column>(Arrays.asList(m_columns));
        columns.add(column);
        m_columns = columns.toArray(new Column[columns.size()]);
    }

    public void addColumn(final String oid, final String alias, final String type) {
        addColumn(new Column(oid, alias, type));
    }

    @Override
    public String toString() {
        return "Table [name=" + m_name + ", instance=" + m_instance + ", ifType=" + m_ifType + ", columns=" + Arrays.toString(m_columns) + ", resourceType=" + m_resourceType + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(m_columns);
        result = prime * result + ((m_instance == null) ? 0 : m_instance.hashCode());
        result = prime * result + ((m_ifType == null) ? 0 : m_ifType.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_resourceType == null) ? 0 : m_resourceType.hashCode());
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
        if (!(obj instanceof Table)) {
            return false;
        }
        final Table other = (Table) obj;
        if (!Arrays.equals(m_columns, other.m_columns)) {
            return false;
        }
        if (m_instance == null) {
            if (other.m_instance != null) {
                return false;
            }
        } else if (!m_instance.equals(other.m_instance)) {
            return false;
        }
        if (m_ifType == null) {
            if (other.m_ifType != null) {
                return false;
            }
        } else if (!m_ifType.equals(other.m_ifType)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_resourceType == null) {
            if (other.m_resourceType != null) {
                return false;
            }
        } else if (!m_resourceType.equals(other.m_resourceType)) {
            return false;
        }
        return true;
    }

}
