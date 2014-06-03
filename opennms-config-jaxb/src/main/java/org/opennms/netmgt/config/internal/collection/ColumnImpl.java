package org.opennms.netmgt.config.internal.collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.config.api.collection.IColumn;
import org.opennms.netmgt.config.api.collection.IMibObject;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpObjIdXmlAdapter;

/**
 * &lt;column oid=".1.3.6.1.2.1.25.2.3.1.2" alias="hrStorageType"  type="string" display-hint="1x:" /&gt;
 * 
 * @author brozow
 *
 */
@XmlRootElement(name="column")
@XmlAccessorType(XmlAccessType.FIELD)
public class ColumnImpl implements IColumn {

    @XmlAttribute(name="oid")
    @XmlJavaTypeAdapter(SnmpObjIdXmlAdapter.class)
    private SnmpObjId m_oid;

    @XmlAttribute(name="alias")
    private String m_alias;

    @XmlAttribute(name="type")
    private String m_type;

    @XmlAttribute(name="display-hint")
    private String m_displayHint;

    public ColumnImpl() {}

    public ColumnImpl(final String oid, final String alias, final String type) {
        m_oid = SnmpObjId.get(oid);
        m_alias = alias;
        m_type = type;
    }

    public ColumnImpl(String oid, String alias, String type, String displayHint) {
        m_oid = SnmpObjId.get(oid);
        m_alias = alias;
        m_type = type;
        m_displayHint = displayHint;
    }

    public ColumnImpl(final IMibObject mibObject) {
        m_oid = mibObject.getOid();
        m_alias = mibObject.getAlias();
        m_type = mibObject.getType();
    }

    public SnmpObjId getOid() {
        return m_oid;
    }

    public void setOid(final SnmpObjId oid) {
        m_oid = oid;
    }

    public void setOid(final String oid) {
        m_oid = SnmpObjId.get(oid);
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

    public static ColumnImpl asColumn(final IColumn column) {
        if (column == null) return null;

        if (column instanceof ColumnImpl) {
            return (ColumnImpl)column;
        } else {
            final ColumnImpl newColumn = new ColumnImpl();
            newColumn.setOid(column.getOid());
            newColumn.setAlias(column.getAlias());
            newColumn.setType(column.getType());
            newColumn.setDisplayHint(column.getDisplayHint());
            return newColumn;
        }
    }

    public static ColumnImpl[] asColumns(final IColumn[] columns) {
        if (columns == null) return null;

        final ColumnImpl[] newColumns = new ColumnImpl[columns.length];
        for (int i=0; i < columns.length; i++) {
            newColumns[i] = ColumnImpl.asColumn(columns[i]);
        }
        return newColumns;
    }

    @Override
    public String toString() {
        return "ColumnImpl [oid=" + m_oid + ", alias=" + m_alias + ", type=" + m_type + ", displayHint=" + m_displayHint + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_alias == null) ? 0 : m_alias.hashCode());
        result = prime * result + ((m_displayHint == null) ? 0 : m_displayHint.hashCode());
        result = prime * result + ((m_oid == null) ? 0 : m_oid.hashCode());
        result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
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
        if (!(obj instanceof ColumnImpl)) {
            return false;
        }
        final ColumnImpl other = (ColumnImpl) obj;
        if (m_alias == null) {
            if (other.m_alias != null) {
                return false;
            }
        } else if (!m_alias.equals(other.m_alias)) {
            return false;
        }
        if (m_displayHint == null) {
            if (other.m_displayHint != null) {
                return false;
            }
        } else if (!m_displayHint.equals(other.m_displayHint)) {
            return false;
        }
        if (m_oid == null) {
            if (other.m_oid != null) {
                return false;
            }
        } else if (!m_oid.equals(other.m_oid)) {
            return false;
        }
        if (m_type == null) {
            if (other.m_type != null) {
                return false;
            }
        } else if (!m_type.equals(other.m_type)) {
            return false;
        }
        return true;
    }

}
