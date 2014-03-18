package org.opennms.netmgt.config.internal.collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.config.api.collection.IColumn;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpObjIdXmlAdapter;

/**
 * <column oid=".1.3.6.1.2.1.25.2.3.1.2" alias="hrStorageType"  type="string" display-hint="1x:" />
 * 
 * @author brozow
 *
 */
@XmlRootElement(name="column")
@XmlAccessorType(XmlAccessType.FIELD)
public class Column implements IColumn {

    @XmlAttribute(name="oid")
    @XmlJavaTypeAdapter(SnmpObjIdXmlAdapter.class)
    private SnmpObjId m_oid;

    @XmlAttribute(name="alias")
    private String m_alias;

    @XmlAttribute(name="type")
    private String m_type;

    @XmlAttribute(name="display-hint")
    private String m_displayHint;

    public SnmpObjId getOid() {
        return m_oid;
    }

    public void setOid(SnmpObjId oid) {
        m_oid = oid;
    }

    public String getAlias() {
        return m_alias;
    }

    public void setAlias(String alias) {
        m_alias = alias;
    }

    public String getType() {
        return m_type;
    }

    public void setType(String type) {
        m_type = type;
    }
    
    public String getDisplayHint() {
        return m_displayHint;
    }
    
    public void setDisplayHint(final String displayHint) {
        m_displayHint = displayHint;
    }

    public static Column asColumn(final IColumn column) {
        if (column == null) return null;

        if (column instanceof Column) {
            return (Column)column;
        } else {
            final Column newColumn = new Column();
            newColumn.setOid(column.getOid());
            newColumn.setAlias(column.getAlias());
            newColumn.setType(column.getType());
            newColumn.setDisplayHint(column.getDisplayHint());
            return newColumn;
        }
    }

    public static Column[] asColumns(final IColumn[] columns) {
        if (columns == null) return null;

        final Column[] newColumns = new Column[columns.length];
        for (int i=0; i < columns.length; i++) {
            newColumns[i] = Column.asColumn(columns[i]);
        }
        return newColumns;
    }

}
