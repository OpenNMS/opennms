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
 * Class Rows.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "rows")
@XmlAccessorType(XmlAccessType.FIELD)
public class Rows implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "row-def", required = true)
    private List<RowDef> rowDefList = new ArrayList<>();

    public RowDef addRow(String rowLabel, String categoryName) {
        RowDef rowDef = new RowDef();
        rowDef.setLabel(rowLabel);

        Category category = new Category();
        category.setName(categoryName);
        rowDef.addCategory(category);
        
        addRowDef(rowDef);
        return rowDef;
    }

    /**
     * 
     * 
     * @param vRowDef
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addRowDef(final RowDef vRowDef) throws IndexOutOfBoundsException {
        this.rowDefList.add(vRowDef);
    }

    /**
     * 
     * 
     * @param index
     * @param vRowDef
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addRowDef(final int index, final RowDef vRowDef) throws IndexOutOfBoundsException {
        this.rowDefList.add(index, vRowDef);
    }

    /**
     * Method enumerateRowDef.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<RowDef> enumerateRowDef() {
        return Collections.enumeration(this.rowDefList);
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
        
        if (obj instanceof Rows) {
            Rows temp = (Rows)obj;
            boolean equals = Objects.equals(temp.rowDefList, rowDefList);
            return equals;
        }
        return false;
    }

    /**
     * Method getRowDef.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the RowDef
     * at the given index
     */
    public RowDef getRowDef(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.rowDefList.size()) {
            throw new IndexOutOfBoundsException("getRowDef: Index value '" + index + "' not in range [0.." + (this.rowDefList.size() - 1) + "]");
        }
        
        return (RowDef) rowDefList.get(index);
    }

    /**
     * Method getRowDef.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public RowDef[] getRowDef() {
        RowDef[] array = new RowDef[0];
        return (RowDef[]) this.rowDefList.toArray(array);
    }

    /**
     * Method getRowDefCollection.Returns a reference to 'rowDefList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<RowDef> getRowDefCollection() {
        return this.rowDefList;
    }

    /**
     * Method getRowDefCount.
     * 
     * @return the size of this collection
     */
    public int getRowDefCount() {
        return this.rowDefList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            rowDefList);
        return hash;
    }

    /**
     * Method iterateRowDef.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<RowDef> iterateRowDef() {
        return this.rowDefList.iterator();
    }

    /**
     */
    public void removeAllRowDef() {
        this.rowDefList.clear();
    }

    /**
     * Method removeRowDef.
     * 
     * @param vRowDef
     * @return true if the object was removed from the collection.
     */
    public boolean removeRowDef(final RowDef vRowDef) {
        boolean removed = rowDefList.remove(vRowDef);
        return removed;
    }

    /**
     * Method removeRowDefAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public RowDef removeRowDefAt(final int index) {
        Object obj = this.rowDefList.remove(index);
        return (RowDef) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vRowDef
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setRowDef(final int index, final RowDef vRowDef) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.rowDefList.size()) {
            throw new IndexOutOfBoundsException("setRowDef: Index value '" + index + "' not in range [0.." + (this.rowDefList.size() - 1) + "]");
        }
        
        this.rowDefList.set(index, vRowDef);
    }

    /**
     * 
     * 
     * @param vRowDefArray
     */
    public void setRowDef(final RowDef[] vRowDefArray) {
        //-- copy array
        rowDefList.clear();
        
        for (int i = 0; i < vRowDefArray.length; i++) {
                this.rowDefList.add(vRowDefArray[i]);
        }
    }

    /**
     * Sets the value of 'rowDefList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vRowDefList the Vector to copy.
     */
    public void setRowDef(final List<RowDef> vRowDefList) {
        // copy vector
        this.rowDefList.clear();
        
        this.rowDefList.addAll(vRowDefList);
    }

    /**
     * Sets the value of 'rowDefList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param rowDefList the Vector to set.
     */
    public void setRowDefCollection(final List<RowDef> rowDefList) {
        this.rowDefList = rowDefList;
    }

}
