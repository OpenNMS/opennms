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


import java.util.ArrayList;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the database-schema.xml
 *  configuration file.
 */
@XmlRootElement(name = "database-schema")
@XmlAccessorType(XmlAccessType.FIELD)
public class DatabaseSchema implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "table")
    private java.util.List<Table> tableList = new ArrayList<>();

    public DatabaseSchema() {
    }

    /**
     * 
     * 
     * @param vTable
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addTable(final Table vTable) throws IndexOutOfBoundsException {
        this.tableList.add(vTable);
    }

    /**
     * 
     * 
     * @param index
     * @param vTable
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addTable(final int index, final Table vTable) throws IndexOutOfBoundsException {
        this.tableList.add(index, vTable);
    }

    /**
     * Method enumerateTable.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<Table> enumerateTable() {
        return java.util.Collections.enumeration(this.tableList);
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
        
        if (obj instanceof DatabaseSchema) {
            DatabaseSchema temp = (DatabaseSchema)obj;
            boolean equals = Objects.equals(temp.tableList, tableList);
            return equals;
        }
        return false;
    }

    /**
     * Method getTable.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Table at the
     * given index
     */
    public Table getTable(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.tableList.size()) {
            throw new IndexOutOfBoundsException("getTable: Index value '" + index + "' not in range [0.." + (this.tableList.size() - 1) + "]");
        }
        
        return (Table) tableList.get(index);
    }

    /**
     * Method getTable.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Table[] getTable() {
        Table[] array = new Table[0];
        return (Table[]) this.tableList.toArray(array);
    }

    /**
     * Method getTableCollection.Returns a reference to 'tableList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Table> getTableCollection() {
        return this.tableList;
    }

    /**
     * Method getTableCount.
     * 
     * @return the size of this collection
     */
    public int getTableCount() {
        return this.tableList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            tableList);
        return hash;
    }

    /**
     * Method iterateTable.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<Table> iterateTable() {
        return this.tableList.iterator();
    }

    /**
     */
    public void removeAllTable() {
        this.tableList.clear();
    }

    /**
     * Method removeTable.
     * 
     * @param vTable
     * @return true if the object was removed from the collection.
     */
    public boolean removeTable(final Table vTable) {
        boolean removed = tableList.remove(vTable);
        return removed;
    }

    /**
     * Method removeTableAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Table removeTableAt(final int index) {
        Object obj = this.tableList.remove(index);
        return (Table) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vTable
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setTable(final int index, final Table vTable) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.tableList.size()) {
            throw new IndexOutOfBoundsException("setTable: Index value '" + index + "' not in range [0.." + (this.tableList.size() - 1) + "]");
        }
        
        this.tableList.set(index, vTable);
    }

    /**
     * 
     * 
     * @param vTableArray
     */
    public void setTable(final Table[] vTableArray) {
        //-- copy array
        tableList.clear();
        
        for (int i = 0; i < vTableArray.length; i++) {
                this.tableList.add(vTableArray[i]);
        }
    }

    /**
     * Sets the value of 'tableList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vTableList the Vector to copy.
     */
    public void setTable(final java.util.List<Table> vTableList) {
        // copy vector
        this.tableList.clear();
        
        this.tableList.addAll(vTableList);
    }

    /**
     * Sets the value of 'tableList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param tableList the Vector to set.
     */
    public void setTableCollection(final java.util.List<Table> tableList) {
        this.tableList = tableList == null? new ArrayList<>() : tableList;
    }

}
