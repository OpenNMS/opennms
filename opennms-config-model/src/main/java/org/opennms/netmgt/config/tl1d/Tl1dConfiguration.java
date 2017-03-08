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

package org.opennms.netmgt.config.tl1d;

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
 * Class Tl1dConfiguration.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "tl1d-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tl1dConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "tl1-element")
    private List<Tl1Element> tl1ElementList = new ArrayList<>();

    /**
     * 
     * 
     * @param vTl1Element
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addTl1Element(final Tl1Element vTl1Element) throws IndexOutOfBoundsException {
        this.tl1ElementList.add(vTl1Element);
    }

    /**
     * 
     * 
     * @param index
     * @param vTl1Element
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addTl1Element(final int index, final Tl1Element vTl1Element) throws IndexOutOfBoundsException {
        this.tl1ElementList.add(index, vTl1Element);
    }

    /**
     * Method enumerateTl1Element.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Tl1Element> enumerateTl1Element() {
        return Collections.enumeration(this.tl1ElementList);
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
        
        if (obj instanceof Tl1dConfiguration) {
            Tl1dConfiguration temp = (Tl1dConfiguration)obj;
            boolean equals = Objects.equals(temp.tl1ElementList, tl1ElementList);
            return equals;
        }
        return false;
    }

    /**
     * Method getTl1Element.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Tl1Element at the
     * given index
     */
    public Tl1Element getTl1Element(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.tl1ElementList.size()) {
            throw new IndexOutOfBoundsException("getTl1Element: Index value '" + index + "' not in range [0.." + (this.tl1ElementList.size() - 1) + "]");
        }
        
        return (Tl1Element) tl1ElementList.get(index);
    }

    /**
     * Method getTl1Element.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Tl1Element[] getTl1Element() {
        Tl1Element[] array = new Tl1Element[0];
        return (Tl1Element[]) this.tl1ElementList.toArray(array);
    }

    /**
     * Method getTl1ElementCollection.Returns a reference to 'tl1ElementList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Tl1Element> getTl1ElementCollection() {
        return this.tl1ElementList;
    }

    /**
     * Method getTl1ElementCount.
     * 
     * @return the size of this collection
     */
    public int getTl1ElementCount() {
        return this.tl1ElementList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            tl1ElementList);
        return hash;
    }

    /**
     * Method iterateTl1Element.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Tl1Element> iterateTl1Element() {
        return this.tl1ElementList.iterator();
    }

    /**
     */
    public void removeAllTl1Element() {
        this.tl1ElementList.clear();
    }

    /**
     * Method removeTl1Element.
     * 
     * @param vTl1Element
     * @return true if the object was removed from the collection.
     */
    public boolean removeTl1Element(final Tl1Element vTl1Element) {
        boolean removed = tl1ElementList.remove(vTl1Element);
        return removed;
    }

    /**
     * Method removeTl1ElementAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Tl1Element removeTl1ElementAt(final int index) {
        Object obj = this.tl1ElementList.remove(index);
        return (Tl1Element) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vTl1Element
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setTl1Element(final int index, final Tl1Element vTl1Element) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.tl1ElementList.size()) {
            throw new IndexOutOfBoundsException("setTl1Element: Index value '" + index + "' not in range [0.." + (this.tl1ElementList.size() - 1) + "]");
        }
        
        this.tl1ElementList.set(index, vTl1Element);
    }

    /**
     * 
     * 
     * @param vTl1ElementArray
     */
    public void setTl1Element(final Tl1Element[] vTl1ElementArray) {
        //-- copy array
        tl1ElementList.clear();
        
        for (int i = 0; i < vTl1ElementArray.length; i++) {
                this.tl1ElementList.add(vTl1ElementArray[i]);
        }
    }

    /**
     * Sets the value of 'tl1ElementList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vTl1ElementList the Vector to copy.
     */
    public void setTl1Element(final List<Tl1Element> vTl1ElementList) {
        // copy vector
        this.tl1ElementList.clear();
        
        this.tl1ElementList.addAll(vTl1ElementList);
    }

    /**
     * Sets the value of 'tl1ElementList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param tl1ElementList the Vector to set.
     */
    public void setTl1ElementCollection(final List<Tl1Element> tl1ElementList) {
        this.tl1ElementList = tl1ElementList;
    }

}
