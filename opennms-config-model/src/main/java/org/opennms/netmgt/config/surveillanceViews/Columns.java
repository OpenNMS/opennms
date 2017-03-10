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

package org.opennms.netmgt.config.surveillanceViews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Columns.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "columns")
@XmlAccessorType(XmlAccessType.FIELD)
public class Columns implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "column-def", required = true)
    private List<ColumnDef> columnDefList = new ArrayList<>();

    public ColumnDef addColumn(String columnLabel, String categoryName) {
        ColumnDef colDef = new ColumnDef();
        colDef.setLabel(columnLabel);
        
        Category category = new Category();
        category.setName(categoryName);
        colDef.addCategory(category);

        addColumnDef(colDef);
        return colDef;
    }

    /**
     * 
     * 
     * @param vColumnDef
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addColumnDef(final ColumnDef vColumnDef) throws IndexOutOfBoundsException {
        this.columnDefList.add(vColumnDef);
    }

    /**
     * 
     * 
     * @param index
     * @param vColumnDef
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addColumnDef(final int index, final ColumnDef vColumnDef) throws IndexOutOfBoundsException {
        this.columnDefList.add(index, vColumnDef);
    }

    /**
     * Method enumerateColumnDef.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<ColumnDef> enumerateColumnDef() {
        return Collections.enumeration(this.columnDefList);
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
        
        if (obj instanceof Columns) {
            Columns temp = (Columns)obj;
            boolean equals = Objects.equals(temp.columnDefList, columnDefList);
            return equals;
        }
        return false;
    }

    /**
     * Method getColumnDef.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the
     * ColumnDef at the given index
     */
    public ColumnDef getColumnDef(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.columnDefList.size()) {
            throw new IndexOutOfBoundsException("getColumnDef: Index value '" + index + "' not in range [0.." + (this.columnDefList.size() - 1) + "]");
        }
        
        return (ColumnDef) columnDefList.get(index);
    }

    /**
     * Method getColumnDef.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public ColumnDef[] getColumnDef() {
        ColumnDef[] array = new ColumnDef[0];
        return (ColumnDef[]) this.columnDefList.toArray(array);
    }

    /**
     * Method getColumnDefCollection.Returns a reference to 'columnDefList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<ColumnDef> getColumnDefCollection() {
        return this.columnDefList;
    }

    /**
     * Method getColumnDefCount.
     * 
     * @return the size of this collection
     */
    public int getColumnDefCount() {
        return this.columnDefList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            columnDefList);
        return hash;
    }

    /**
     * Method iterateColumnDef.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<ColumnDef> iterateColumnDef() {
        return this.columnDefList.iterator();
    }

    /**
     */
    public void removeAllColumnDef() {
        this.columnDefList.clear();
    }

    /**
     * Method removeColumnDef.
     * 
     * @param vColumnDef
     * @return true if the object was removed from the collection.
     */
    public boolean removeColumnDef(final ColumnDef vColumnDef) {
        boolean removed = columnDefList.remove(vColumnDef);
        return removed;
    }

    /**
     * Method removeColumnDefAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public ColumnDef removeColumnDefAt(final int index) {
        Object obj = this.columnDefList.remove(index);
        return (ColumnDef) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vColumnDef
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setColumnDef(final int index, final ColumnDef vColumnDef) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.columnDefList.size()) {
            throw new IndexOutOfBoundsException("setColumnDef: Index value '" + index + "' not in range [0.." + (this.columnDefList.size() - 1) + "]");
        }
        
        this.columnDefList.set(index, vColumnDef);
    }

    /**
     * 
     * 
     * @param vColumnDefArray
     */
    public void setColumnDef(final ColumnDef[] vColumnDefArray) {
        //-- copy array
        columnDefList.clear();
        
        for (int i = 0; i < vColumnDefArray.length; i++) {
                this.columnDefList.add(vColumnDefArray[i]);
        }
    }

    /**
     * Sets the value of 'columnDefList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vColumnDefList the Vector to copy.
     */
    public void setColumnDef(final List<ColumnDef> vColumnDefList) {
        // copy vector
        this.columnDefList.clear();
        
        this.columnDefList.addAll(vColumnDefList);
    }

    /**
     * Sets the value of 'columnDefList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param columnDefList the Vector to set.
     */
    public void setColumnDefCollection(final List<ColumnDef> columnDefList) {
        this.columnDefList = columnDefList;
    }

}
