/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.filter;


import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Join.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "join")
@XmlAccessorType(XmlAccessType.FIELD)
public class Join implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_TYPE = "inner";

    @XmlAttribute(name = "type")
    private String type;

    @XmlAttribute(name = "column", required = true)
    private String column;

    @XmlAttribute(name = "table", required = true)
    private String table;

    @XmlAttribute(name = "table-column", required = true)
    private String tableColumn;

    public Join() {
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof Join) {
            Join temp = (Join)obj;
            boolean equals = Objects.equals(temp.type, type)
                && Objects.equals(temp.column, column)
                && Objects.equals(temp.table, table)
                && Objects.equals(temp.tableColumn, tableColumn);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'column'.
     * 
     * @return the value of field 'Column'.
     */
    public String getColumn() {
        return this.column;
    }

    /**
     * Returns the value of field 'table'.
     * 
     * @return the value of field 'Table'.
     */
    public String getTable() {
        return this.table;
    }

    /**
     * Returns the value of field 'tableColumn'.
     * 
     * @return the value of field 'TableColumn'.
     */
    public String getTableColumn() {
        return this.tableColumn;
    }

    /**
     * Returns the value of field 'type'.
     * 
     * @return the value of field 'Type'.
     */
    public String getType() {
        return this.type != null ? this.type : DEFAULT_TYPE;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            type, 
            column, 
            table, 
            tableColumn);
        return hash;
    }

    /**
     * Sets the value of field 'column'.
     * 
     * @param column the value of field 'column'.
     */
    public void setColumn(final String column) {
        if (column == null) {
            throw new IllegalArgumentException("'column' is a required attribute!");
        }
        this.column = column;
    }

    /**
     * Sets the value of field 'table'.
     * 
     * @param table the value of field 'table'.
     */
    public void setTable(final String table) {
        if (table == null) {
            throw new IllegalArgumentException("'table' is a required attribute!");
        }
        this.table = table;
    }

    /**
     * Sets the value of field 'tableColumn'.
     * 
     * @param tableColumn the value of field 'tableColumn'.
     */
    public void setTableColumn(final String tableColumn) {
        if (tableColumn == null) {
            throw new IllegalArgumentException("'table-column' is a required attribute!");
        }
        this.tableColumn = tableColumn;
    }

    /**
     * Sets the value of field 'type'.
     * 
     * @param type the value of field 'type'.
     */
    public void setType(final String type) {
        this.type = type;
    }

}
