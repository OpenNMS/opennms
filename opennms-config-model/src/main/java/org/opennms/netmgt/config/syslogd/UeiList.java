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

package org.opennms.netmgt.config.syslogd;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class UeiList.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "ueiList")
@XmlAccessorType(XmlAccessType.FIELD)
public class UeiList implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * List of Strings to UEI matches
     */
    @XmlElement(name = "ueiMatch")
    private java.util.List<UeiMatch> ueiMatchList;

    public UeiList() {
        this.ueiMatchList = new java.util.ArrayList<UeiMatch>();
    }

    /**
     * 
     * 
     * @param vUeiMatch
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addUeiMatch(final UeiMatch vUeiMatch) throws IndexOutOfBoundsException {
        this.ueiMatchList.add(vUeiMatch);
    }

    /**
     * 
     * 
     * @param index
     * @param vUeiMatch
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addUeiMatch(final int index, final UeiMatch vUeiMatch) throws IndexOutOfBoundsException {
        this.ueiMatchList.add(index, vUeiMatch);
    }

    /**
     * Method enumerateUeiMatch.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<UeiMatch> enumerateUeiMatch() {
        return java.util.Collections.enumeration(this.ueiMatchList);
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
        
        if (obj instanceof UeiList) {
            UeiList temp = (UeiList)obj;
            boolean equals = Objects.equals(temp.ueiMatchList, ueiMatchList);
            return equals;
        }
        return false;
    }

    /**
     * Method getUeiMatch.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the types.UeiMatch
     * at the given index
     */
    public UeiMatch getUeiMatch(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.ueiMatchList.size()) {
            throw new IndexOutOfBoundsException("getUeiMatch: Index value '" + index + "' not in range [0.." + (this.ueiMatchList.size() - 1) + "]");
        }
        
        return (UeiMatch) ueiMatchList.get(index);
    }

    /**
     * Method getUeiMatch.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public UeiMatch[] getUeiMatch() {
        UeiMatch[] array = new UeiMatch[0];
        return (UeiMatch[]) this.ueiMatchList.toArray(array);
    }

    /**
     * Method getUeiMatchCollection.Returns a reference to 'ueiMatchList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<UeiMatch> getUeiMatchCollection() {
        return this.ueiMatchList;
    }

    /**
     * Method getUeiMatchCount.
     * 
     * @return the size of this collection
     */
    public int getUeiMatchCount() {
        return this.ueiMatchList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            ueiMatchList);
        return hash;
    }

    /**
     * Method iterateUeiMatch.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<UeiMatch> iterateUeiMatch() {
        return this.ueiMatchList.iterator();
    }

    /**
     */
    public void removeAllUeiMatch() {
        this.ueiMatchList.clear();
    }

    /**
     * Method removeUeiMatch.
     * 
     * @param vUeiMatch
     * @return true if the object was removed from the collection.
     */
    public boolean removeUeiMatch(final UeiMatch vUeiMatch) {
        boolean removed = ueiMatchList.remove(vUeiMatch);
        return removed;
    }

    /**
     * Method removeUeiMatchAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public UeiMatch removeUeiMatchAt(final int index) {
        Object obj = this.ueiMatchList.remove(index);
        return (UeiMatch) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vUeiMatch
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setUeiMatch(final int index, final UeiMatch vUeiMatch) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.ueiMatchList.size()) {
            throw new IndexOutOfBoundsException("setUeiMatch: Index value '" + index + "' not in range [0.." + (this.ueiMatchList.size() - 1) + "]");
        }
        
        this.ueiMatchList.set(index, vUeiMatch);
    }

    /**
     * 
     * 
     * @param vUeiMatchArray
     */
    public void setUeiMatch(final UeiMatch[] vUeiMatchArray) {
        //-- copy array
        ueiMatchList.clear();
        
        for (int i = 0; i < vUeiMatchArray.length; i++) {
                this.ueiMatchList.add(vUeiMatchArray[i]);
        }
    }

    /**
     * Sets the value of 'ueiMatchList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vUeiMatchList the Vector to copy.
     */
    public void setUeiMatch(final java.util.List<UeiMatch> vUeiMatchList) {
        // copy vector
        this.ueiMatchList.clear();
        
        this.ueiMatchList.addAll(vUeiMatchList);
    }

    /**
     * Sets the value of 'ueiMatchList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param ueiMatchList the Vector to set.
     */
    public void setUeiMatchCollection(final java.util.List<UeiMatch> ueiMatchList) {
        this.ueiMatchList = ueiMatchList;
    }

}
