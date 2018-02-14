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

package org.opennms.reporting.availability;


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

    @XmlElement(name = "row")
    private java.util.List<Row> rowList;

    public Rows() {
        this.rowList = new java.util.ArrayList<>();
    }

    /**
     * 
     * 
     * @param vRow
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addRow(final Row vRow) throws IndexOutOfBoundsException {
        this.rowList.add(vRow);
    }

    /**
     * 
     * 
     * @param index
     * @param vRow
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addRow(final int index, final Row vRow) throws IndexOutOfBoundsException {
        this.rowList.add(index, vRow);
    }

    /**
     * Method enumerateRow.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<Row> enumerateRow() {
        return java.util.Collections.enumeration(this.rowList);
    }

    /**
     * Method getRow.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Row at the
     * given index
     */
    public Row getRow(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.rowList.size()) {
            throw new IndexOutOfBoundsException("getRow: Index value '" + index + "' not in range [0.." + (this.rowList.size() - 1) + "]");
        }
        
        return (Row) rowList.get(index);
    }

    /**
     * Method getRow.Returns the contents of the collection in an Array.  <p>Note:
     *  Just in case the collection contents are changing in another thread, we
     * pass a 0-length Array of the correct type into the API call.  This way we
     * <i>know</i> that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Row[] getRow() {
        Row[] array = new Row[0];
        return (Row[]) this.rowList.toArray(array);
    }

    /**
     * Method getRowCollection.Returns a reference to 'rowList'. No type checking
     * is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Row> getRowCollection() {
        return this.rowList;
    }

    /**
     * Method getRowCount.
     * 
     * @return the size of this collection
     */
    public int getRowCount() {
        return this.rowList.size();
    }

    /**
     * Method iterateRow.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<Row> iterateRow() {
        return this.rowList.iterator();
    }

    /**
     */
    public void removeAllRow() {
        this.rowList.clear();
    }

    /**
     * Method removeRow.
     * 
     * @param vRow
     * @return true if the object was removed from the collection.
     */
    public boolean removeRow(final Row vRow) {
        boolean removed = rowList.remove(vRow);
        return removed;
    }

    /**
     * Method removeRowAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Row removeRowAt(final int index) {
        Object obj = this.rowList.remove(index);
        return (Row) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vRow
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setRow(final int index, final Row vRow) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.rowList.size()) {
            throw new IndexOutOfBoundsException("setRow: Index value '" + index + "' not in range [0.." + (this.rowList.size() - 1) + "]");
        }
        
        this.rowList.set(index, vRow);
    }

    /**
     * 
     * 
     * @param vRowArray
     */
    public void setRow(final Row[] vRowArray) {
        //-- copy array
        rowList.clear();
        
        for (int i = 0; i < vRowArray.length; i++) {
                this.rowList.add(vRowArray[i]);
        }
    }

    /**
     * Sets the value of 'rowList' by copying the given Vector. All elements will
     * be checked for type safety.
     * 
     * @param vRowList the Vector to copy.
     */
    public void setRow(final java.util.List<Row> vRowList) {
        // copy vector
        this.rowList.clear();
        
        this.rowList.addAll(vRowList);
    }

    /**
     * Sets the value of 'rowList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param rowList the Vector to set.
     */
    public void setRowCollection(final java.util.List<Row> rowList) {
        this.rowList = rowList;
    }

}
