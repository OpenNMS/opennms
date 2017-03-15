/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.siteStatusViews;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class View.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "view")
@XmlAccessorType(XmlAccessType.FIELD)

@SuppressWarnings("all") public class View implements java.io.Serializable {

    private static final String DEFAULT_TABLE_NAME = "assets";
    private static final String DEFAULT_COLUMN_NAME = "building";
    private static final String DEFAULT_COLUMN_TYPE = "varchar";

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "table-name")
    private String tableName;

    @XmlAttribute(name = "column-name")
    private String columnName;

    @XmlAttribute(name = "column-type")
    private String columnType;

    @XmlAttribute(name = "column-value")
    private String columnValue;

    @XmlElement(name = "rows", required = true)
    private Rows rows;

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
        
        if (obj instanceof View) {
            View temp = (View)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.tableName, tableName)
                && Objects.equals(temp.columnName, columnName)
                && Objects.equals(temp.columnType, columnType)
                && Objects.equals(temp.columnValue, columnValue)
                && Objects.equals(temp.rows, rows);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'columnName'.
     * 
     * @return the value of field 'ColumnName'.
     */
    public String getColumnName() {
        return this.columnName != null ? this.columnName : DEFAULT_COLUMN_NAME;
    }

    /**
     * Returns the value of field 'columnType'.
     * 
     * @return the value of field 'ColumnType'.
     */
    public String getColumnType() {
        return this.columnType != null ? this.columnType : DEFAULT_COLUMN_TYPE;
    }

    /**
     * Returns the value of field 'columnValue'.
     * 
     * @return the value of field 'ColumnValue'.
     */
    public String getColumnValue() {
        return this.columnValue;
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of field 'rows'.
     * 
     * @return the value of field 'Rows'.
     */
    public Rows getRows() {
        return this.rows;
    }

    /**
     * Returns the value of field 'tableName'.
     * 
     * @return the value of field 'TableName'.
     */
    public String getTableName() {
        return this.tableName != null ? this.tableName : DEFAULT_TABLE_NAME;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            name, 
            tableName, 
            columnName, 
            columnType, 
            columnValue, 
            rows);
        return hash;
    }

    /**
     * Sets the value of field 'columnName'.
     * 
     * @param columnName the value of field 'columnName'.
     */
    public void setColumnName(final String columnName) {
        this.columnName = columnName;
    }

    /**
     * Sets the value of field 'columnType'.
     * 
     * @param columnType the value of field 'columnType'.
     */
    public void setColumnType(final String columnType) {
        this.columnType = columnType;
    }

    /**
     * Sets the value of field 'columnValue'.
     * 
     * @param columnValue the value of field 'columnValue'.
     */
    public void setColumnValue(final String columnValue) {
        this.columnValue = columnValue;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the value of field 'rows'.
     * 
     * @param rows the value of field 'rows'.
     */
    public void setRows(final Rows rows) {
        this.rows = rows;
    }

    /**
     * Sets the value of field 'tableName'.
     * 
     * @param tableName the value of field 'tableName'.
     */
    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

}
