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

package org.opennms.netmgt.config.reporting;

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
 * Class Parameters.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "parameters")
@XmlAccessorType(XmlAccessType.FIELD)
public class Parameters implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "string-parm")
    private List<StringParm> stringParmList = new ArrayList<>();

    @XmlElement(name = "date-parm")
    private List<DateParm> dateParmList = new ArrayList<>();

    @XmlElement(name = "int-parm")
    private List<IntParm> intParmList = new ArrayList<>();

    /**
     * 
     * 
     * @param vDateParm
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addDateParm(final DateParm vDateParm) throws IndexOutOfBoundsException {
        this.dateParmList.add(vDateParm);
    }

    /**
     * 
     * 
     * @param index
     * @param vDateParm
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addDateParm(final int index, final DateParm vDateParm) throws IndexOutOfBoundsException {
        this.dateParmList.add(index, vDateParm);
    }

    /**
     * 
     * 
     * @param vIntParm
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addIntParm(final IntParm vIntParm) throws IndexOutOfBoundsException {
        this.intParmList.add(vIntParm);
    }

    /**
     * 
     * 
     * @param index
     * @param vIntParm
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addIntParm(final int index, final IntParm vIntParm) throws IndexOutOfBoundsException {
        this.intParmList.add(index, vIntParm);
    }

    /**
     * 
     * 
     * @param vStringParm
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addStringParm(final StringParm vStringParm) throws IndexOutOfBoundsException {
        this.stringParmList.add(vStringParm);
    }

    /**
     * 
     * 
     * @param index
     * @param vStringParm
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addStringParm(final int index, final StringParm vStringParm) throws IndexOutOfBoundsException {
        this.stringParmList.add(index, vStringParm);
    }

    /**
     * Method enumerateDateParm.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<DateParm> enumerateDateParm() {
        return Collections.enumeration(this.dateParmList);
    }

    /**
     * Method enumerateIntParm.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<IntParm> enumerateIntParm() {
        return Collections.enumeration(this.intParmList);
    }

    /**
     * Method enumerateStringParm.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<StringParm> enumerateStringParm() {
        return Collections.enumeration(this.stringParmList);
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
        
        if (obj instanceof Parameters) {
            Parameters temp = (Parameters)obj;
            boolean equals = Objects.equals(temp.stringParmList, stringParmList)
                && Objects.equals(temp.dateParmList, dateParmList)
                && Objects.equals(temp.intParmList, intParmList);
            return equals;
        }
        return false;
    }

    /**
     * Method getDateParm.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the DateParm at
     * the given index
     */
    public DateParm getDateParm(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.dateParmList.size()) {
            throw new IndexOutOfBoundsException("getDateParm: Index value '" + index + "' not in range [0.." + (this.dateParmList.size() - 1) + "]");
        }
        
        return (DateParm) dateParmList.get(index);
    }

    /**
     * Method getDateParm.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public DateParm[] getDateParm() {
        DateParm[] array = new DateParm[0];
        return (DateParm[]) this.dateParmList.toArray(array);
    }

    /**
     * Method getDateParmCollection.Returns a reference to 'dateParmList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<DateParm> getDateParmCollection() {
        return this.dateParmList;
    }

    /**
     * Method getDateParmCount.
     * 
     * @return the size of this collection
     */
    public int getDateParmCount() {
        return this.dateParmList.size();
    }

    /**
     * Method getIntParm.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the IntParm at the
     * given index
     */
    public IntParm getIntParm(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.intParmList.size()) {
            throw new IndexOutOfBoundsException("getIntParm: Index value '" + index + "' not in range [0.." + (this.intParmList.size() - 1) + "]");
        }
        
        return (IntParm) intParmList.get(index);
    }

    /**
     * Method getIntParm.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public IntParm[] getIntParm() {
        IntParm[] array = new IntParm[0];
        return (IntParm[]) this.intParmList.toArray(array);
    }

    /**
     * Method getIntParmCollection.Returns a reference to 'intParmList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<IntParm> getIntParmCollection() {
        return this.intParmList;
    }

    /**
     * Method getIntParmCount.
     * 
     * @return the size of this collection
     */
    public int getIntParmCount() {
        return this.intParmList.size();
    }

    /**
     * Method getStringParm.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the StringParm at
     * the given index
     */
    public StringParm getStringParm(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.stringParmList.size()) {
            throw new IndexOutOfBoundsException("getStringParm: Index value '" + index + "' not in range [0.." + (this.stringParmList.size() - 1) + "]");
        }
        
        return (StringParm) stringParmList.get(index);
    }

    /**
     * Method getStringParm.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public StringParm[] getStringParm() {
        StringParm[] array = new StringParm[0];
        return (StringParm[]) this.stringParmList.toArray(array);
    }

    /**
     * Method getStringParmCollection.Returns a reference to 'stringParmList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<StringParm> getStringParmCollection() {
        return this.stringParmList;
    }

    /**
     * Method getStringParmCount.
     * 
     * @return the size of this collection
     */
    public int getStringParmCount() {
        return this.stringParmList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            stringParmList, 
            dateParmList, 
            intParmList);
        return hash;
    }

    /**
     * Method iterateDateParm.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<DateParm> iterateDateParm() {
        return this.dateParmList.iterator();
    }

    /**
     * Method iterateIntParm.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<IntParm> iterateIntParm() {
        return this.intParmList.iterator();
    }

    /**
     * Method iterateStringParm.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<StringParm> iterateStringParm() {
        return this.stringParmList.iterator();
    }

    /**
     */
    public void removeAllDateParm() {
        this.dateParmList.clear();
    }

    /**
     */
    public void removeAllIntParm() {
        this.intParmList.clear();
    }

    /**
     */
    public void removeAllStringParm() {
        this.stringParmList.clear();
    }

    /**
     * Method removeDateParm.
     * 
     * @param vDateParm
     * @return true if the object was removed from the collection.
     */
    public boolean removeDateParm(final DateParm vDateParm) {
        boolean removed = dateParmList.remove(vDateParm);
        return removed;
    }

    /**
     * Method removeDateParmAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public DateParm removeDateParmAt(final int index) {
        Object obj = this.dateParmList.remove(index);
        return (DateParm) obj;
    }

    /**
     * Method removeIntParm.
     * 
     * @param vIntParm
     * @return true if the object was removed from the collection.
     */
    public boolean removeIntParm(final IntParm vIntParm) {
        boolean removed = intParmList.remove(vIntParm);
        return removed;
    }

    /**
     * Method removeIntParmAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public IntParm removeIntParmAt(final int index) {
        Object obj = this.intParmList.remove(index);
        return (IntParm) obj;
    }

    /**
     * Method removeStringParm.
     * 
     * @param vStringParm
     * @return true if the object was removed from the collection.
     */
    public boolean removeStringParm(final StringParm vStringParm) {
        boolean removed = stringParmList.remove(vStringParm);
        return removed;
    }

    /**
     * Method removeStringParmAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public StringParm removeStringParmAt(final int index) {
        Object obj = this.stringParmList.remove(index);
        return (StringParm) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vDateParm
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setDateParm(final int index, final DateParm vDateParm) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.dateParmList.size()) {
            throw new IndexOutOfBoundsException("setDateParm: Index value '" + index + "' not in range [0.." + (this.dateParmList.size() - 1) + "]");
        }
        
        this.dateParmList.set(index, vDateParm);
    }

    /**
     * 
     * 
     * @param vDateParmArray
     */
    public void setDateParm(final DateParm[] vDateParmArray) {
        //-- copy array
        dateParmList.clear();
        
        for (int i = 0; i < vDateParmArray.length; i++) {
                this.dateParmList.add(vDateParmArray[i]);
        }
    }

    /**
     * Sets the value of 'dateParmList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vDateParmList the Vector to copy.
     */
    public void setDateParm(final List<DateParm> vDateParmList) {
        // copy vector
        this.dateParmList.clear();
        
        this.dateParmList.addAll(vDateParmList);
    }

    /**
     * Sets the value of 'dateParmList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param dateParmList the Vector to set.
     */
    public void setDateParmCollection(final List<DateParm> dateParmList) {
        this.dateParmList = dateParmList;
    }

    /**
     * 
     * 
     * @param index
     * @param vIntParm
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setIntParm(final int index, final IntParm vIntParm) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.intParmList.size()) {
            throw new IndexOutOfBoundsException("setIntParm: Index value '" + index + "' not in range [0.." + (this.intParmList.size() - 1) + "]");
        }
        
        this.intParmList.set(index, vIntParm);
    }

    /**
     * 
     * 
     * @param vIntParmArray
     */
    public void setIntParm(final IntParm[] vIntParmArray) {
        //-- copy array
        intParmList.clear();
        
        for (int i = 0; i < vIntParmArray.length; i++) {
                this.intParmList.add(vIntParmArray[i]);
        }
    }

    /**
     * Sets the value of 'intParmList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vIntParmList the Vector to copy.
     */
    public void setIntParm(final List<IntParm> vIntParmList) {
        // copy vector
        this.intParmList.clear();
        
        this.intParmList.addAll(vIntParmList);
    }

    /**
     * Sets the value of 'intParmList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param intParmList the Vector to set.
     */
    public void setIntParmCollection(final List<IntParm> intParmList) {
        this.intParmList = intParmList;
    }

    /**
     * 
     * 
     * @param index
     * @param vStringParm
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setStringParm(final int index, final StringParm vStringParm) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.stringParmList.size()) {
            throw new IndexOutOfBoundsException("setStringParm: Index value '" + index + "' not in range [0.." + (this.stringParmList.size() - 1) + "]");
        }
        
        this.stringParmList.set(index, vStringParm);
    }

    /**
     * 
     * 
     * @param vStringParmArray
     */
    public void setStringParm(final StringParm[] vStringParmArray) {
        //-- copy array
        stringParmList.clear();
        
        for (int i = 0; i < vStringParmArray.length; i++) {
                this.stringParmList.add(vStringParmArray[i]);
        }
    }

    /**
     * Sets the value of 'stringParmList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vStringParmList the Vector to copy.
     */
    public void setStringParm(final List<StringParm> vStringParmList) {
        // copy vector
        this.stringParmList.clear();
        
        this.stringParmList.addAll(vStringParmList);
    }

    /**
     * Sets the value of 'stringParmList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param stringParmList the Vector to set.
     */
    public void setStringParmCollection(final List<StringParm> stringParmList) {
        this.stringParmList = stringParmList;
    }

}
