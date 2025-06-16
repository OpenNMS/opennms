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
     * @deprecated
     * @param index
     * @param vValue
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    @Hidden
    public void setValue(final int index, final Value vValue) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.valueList.size()) {
            throw new IndexOutOfBoundsException("setValue: Index value '" + index + "' not in range [0.." + (this.valueList.size() - 1) + "]");
        }
        
        this.valueList.set(index, vValue);
    }

    /**
     * 
     * @deprecated
     * @param vValueArray
     */
    @Hidden
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
    @Hidden
    public void setValueCollection(final java.util.List<Value> valueList) {
        this.valueList = valueList;
    }

}
