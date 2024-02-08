/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.reporting.availability;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.Hidden;

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
     * @deprecated
     * @param index
     * @param vRow
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    @Hidden
    public void setRow(final int index, final Row vRow) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.rowList.size()) {
            throw new IndexOutOfBoundsException("setRow: Index value '" + index + "' not in range [0.." + (this.rowList.size() - 1) + "]");
        }
        
        this.rowList.set(index, vRow);
    }

    /**
     * 
     * @deprecated
     * @param vRowArray
     */
    @Hidden
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
    @Hidden
    public void setRowCollection(final java.util.List<Row> rowList) {
        this.rowList = rowList;
    }

}
