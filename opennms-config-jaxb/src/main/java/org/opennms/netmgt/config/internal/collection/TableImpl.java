/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
 *  &lt;table name="mib2-host-resources-storage" instance="hrStorageIndex"&gt;"
 *      &lt;column oid=".1.3.6.1.2.1.25.2.3.1.4" alias="hrStorageAllocUnits" type="gauge" /&gt;
 *      &lt;column oid=".1.3.6.1.2.1.25.2.3.1.5" alias="hrStorageSize"       type="gauge" /&gt;
 *      &lt;column oid=".1.3.6.1.2.1.25.2.3.1.6" alias="hrStorageUse"        type="gauge" /&gt;
 *  &lt;/table&gt;
 *  
 * @author brozow
 *
 */
@XmlRootElement(name="table")
@XmlAccessorType(XmlAccessType.NONE)
public class TableImpl implements ITable {

    @XmlAttribute(name="name")
    private String m_name;

    @XmlAttribute(name="instance")
    private String m_instance;

    @XmlElement(name="column")
    private ColumnImpl[] m_columns;

    @XmlTransient
    private ResourceTypeImpl m_resourceType;

    public TableImpl() {
    }

    public TableImpl(final String name, final String instance) {
        m_name = name;
        m_instance = instance;
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
    public IColumn[] getColumns() {
        return m_columns;
    }

    public void setColumns(final IColumn[] columns) {
        m_columns = ColumnImpl.asColumns(columns);
    }

    public void addColumn(final ColumnImpl column) {
        final List<ColumnImpl> columns = m_columns == null? new ArrayList<ColumnImpl>() : new ArrayList<ColumnImpl>(Arrays.asList(m_columns));
        columns.add(column);
        m_columns = columns.toArray(new ColumnImpl[columns.size()]);
    }

    public void addColumn(final String oid, final String alias, final String type) {
        addColumn(new ColumnImpl(oid, alias, type));
    }

    @Override
    public String toString() {
        return "TableImpl [name=" + m_name + ", instance=" + m_instance + ", columns=" + Arrays.toString(m_columns) + ", resourceType=" + m_resourceType + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(m_columns);
        result = prime * result + ((m_instance == null) ? 0 : m_instance.hashCode());
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
        if (!(obj instanceof TableImpl)) {
            return false;
        }
        final TableImpl other = (TableImpl) obj;
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

    public static TableImpl asTable(final ITable table) {
        if (table == null) return null;
        
        if (table instanceof TableImpl) {
            return (TableImpl)table;
        } else {
            final TableImpl newTable = new TableImpl();
            newTable.setName(table.getName());
            newTable.setInstance(table.getInstance());
            newTable.setColumns(ColumnImpl.asColumns(table.getColumns()));
            return newTable;
        }
    }

    public static TableImpl[] asTables(final ITable[] tables) {
        if (tables == null) return null;
        
        final TableImpl[] newTables = new TableImpl[tables.length];
        for (int i=0; i < tables.length; i++) {
            newTables[i] = TableImpl.asTable(tables[i]);
        }
        return newTables;
    }

}
