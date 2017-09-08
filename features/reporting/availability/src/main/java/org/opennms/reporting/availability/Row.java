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
 * Class Row.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "row")
@XmlAccessorType(XmlAccessType.FIELD)
public class Row implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "value")
    private java.util.List<Value> valueList;

    public Row() {
        this.valueList = new java.util.ArrayList<>();
    }

    /**
     * 
     * 
     * @param vValue
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addValue(final Value vValue) throws IndexOutOfBoundsException {
        this.valueList.add(vValue);
    }

    /**
     * 
     * 
     * @param index
     * @param vValue
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addValue(final int index, final Value vValue) throws IndexOutOfBoundsException {
        this.valueList.add(index, vValue);
    }

    /**
     * Method enumerateValue.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<Value> enumerateValue() {
        return java.util.Collections.enumeration(this.valueList);
    }

    /**
     * Method getValue.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Value at the
     * given index
     */
    public Value getValue(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.valueList.size()) {
            throw new IndexOutOfBoundsException("getValue: Index value '" + index + "' not in range [0.." + (this.valueList.size() - 1) + "]");
        }
        
        return (Value) valueList.get(index);
    }

    /**
     * Method getValue.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Value[] getValue() {
        Value[] array = new Value[0];
        return (Value[]) this.valueList.toArray(array);
    }

    /**
     * Method getValueCollection.Returns a reference to 'valueList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Value> getValueCollection() {
        return this.valueList;
    }

    /**
     * Method getValueCount.
     * 
     * @return the size of this collection
     */
    public int getValueCount() {
        return this.valueList.size();
    }

    /**
     * Method iterateValue.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<Value> iterateValue() {
        return this.valueList.iterator();
    }

    /**
     */
    public void removeAllValue() {
        this.valueList.clear();
    }

    /**
     * Method removeValue.
     * 
     * @param vValue
     * @return true if the object was removed from the collection.
     */
    public boolean removeValue(final Value vValue) {
        boolean removed = valueList.remove(vValue);
        return removed;
    }

    /**
     * Method removeValueAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Value removeValueAt(final int index) {
        Object obj = this.valueList.remove(index);
        return (Value) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vValue
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setValue(final int index, final Value vValue) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.valueList.size()) {
            throw new IndexOutOfBoundsException("setValue: Index value '" + index + "' not in range [0.." + (this.valueList.size() - 1) + "]");
        }
        
        this.valueList.set(index, vValue);
    }

    /**
     * 
     * 
     * @param vValueArray
     */
    public void setValue(final Value[] vValueArray) {
        //-- copy array
        valueList.clear();
        
        for (int i = 0; i < vValueArray.length; i++) {
                this.valueList.add(vValueArray[i]);
        }
    }

    /**
     * Sets the value of 'valueList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vValueList the Vector to copy.
     */
    public void setValue(final java.util.List<Value> vValueList) {
        // copy vector
        this.valueList.clear();
        
        this.valueList.addAll(vValueList);
    }

    /**
     * Sets the value of 'valueList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param valueList the Vector to set.
     */
    public void setValueCollection(final java.util.List<Value> valueList) {
        this.valueList = valueList;
    }

}
