package org.opennms.netmgt.config.internal.collection;

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

    @XmlElement(name="column")
    private Column[] m_columns;

    @XmlTransient
    private ResourceType m_resourceType;

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
    public IColumn[] getColumns() {
        return m_columns;
    }

    public void setColumns(final IColumn[] columns) {
        m_columns = Column.asColumns(columns);
    }

}
