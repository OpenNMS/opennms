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

package org.opennms.netmgt.config.translator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An element representing a value to be used in a
 *  translation. 
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "value")
@XmlAccessorType(XmlAccessType.FIELD)
public class Value implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "result", required = true)
    private String result;

    @XmlAttribute(name = "matches")
    private String matches;

    @XmlAttribute(name = "type", required = true)
    private String type;

    @XmlAttribute(name = "name")
    private String name;

    /**
     * An element representing a value to be used in a
     *  translation. 
     *  
     */
    @XmlElement(name = "value")
    private List<Value> valueList = new ArrayList<>();

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
    public Enumeration<Value> enumerateValue() {
        return Collections.enumeration(this.valueList);
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
        
        if (obj instanceof Value) {
            Value temp = (Value)obj;
            boolean equals = Objects.equals(temp.result, result)
                && Objects.equals(temp.matches, matches)
                && Objects.equals(temp.type, type)
                && Objects.equals(temp.name, name)
                && Objects.equals(temp.valueList, valueList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'matches'.
     * 
     * @return the value of field 'Matches'.
     */
    public String getMatches() {
        return this.matches;
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
     * Returns the value of field 'result'.
     * 
     * @return the value of field 'Result'.
     */
    public String getResult() {
        return this.result;
    }

    /**
     * Returns the value of field 'type'.
     * 
     * @return the value of field 'Type'.
     */
    public String getType() {
        return this.type;
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
    public List<Value> getValueCollection() {
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
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            result, 
            matches, 
            type, 
            name, 
            valueList);
        return hash;
    }

    /**
     * Method iterateValue.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Value> iterateValue() {
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
     * Sets the value of field 'matches'.
     * 
     * @param matches the value of field 'matches'.
     */
    public void setMatches(final String matches) {
        this.matches = matches;
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
     * Sets the value of field 'result'.
     * 
     * @param result the value of field 'result'.
     */
    public void setResult(final String result) {
        this.result = result;
    }

    /**
     * Sets the value of field 'type'.
     * 
     * @param type the value of field 'type'.
     */
    public void setType(final String type) {
        this.type = type;
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
    public void setValue(final List<Value> vValueList) {
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
    public void setValueCollection(final List<Value> valueList) {
        this.valueList = valueList;
    }

}
