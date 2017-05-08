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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The list of event mappings for this event. The first
 *  mapping that matches the event is used to translate the
 *  event into a new event.
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "mappings")
@XmlAccessorType(XmlAccessType.FIELD)
public class Mappings implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * A mapping for a given event. This translation is only
     *  applied if it is the first that matches
     *  
     */
    @XmlElement(name = "mapping")
    private List<Mapping> mappingList = new ArrayList<>();

    /**
     * 
     * 
     * @param vMapping
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addMapping(final Mapping vMapping) throws IndexOutOfBoundsException {
        this.mappingList.add(vMapping);
    }

    /**
     * 
     * 
     * @param index
     * @param vMapping
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addMapping(final int index, final Mapping vMapping) throws IndexOutOfBoundsException {
        this.mappingList.add(index, vMapping);
    }

    /**
     * Method enumerateMapping.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Mapping> enumerateMapping() {
        return Collections.enumeration(this.mappingList);
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
        
        if (obj instanceof Mappings) {
            Mappings temp = (Mappings)obj;
            boolean equals = Objects.equals(temp.mappingList, mappingList);
            return equals;
        }
        return false;
    }

    /**
     * Method getMapping.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Mapping at
     * the given index
     */
    public Mapping getMapping(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.mappingList.size()) {
            throw new IndexOutOfBoundsException("getMapping: Index value '" + index + "' not in range [0.." + (this.mappingList.size() - 1) + "]");
        }
        
        return (Mapping) mappingList.get(index);
    }

    /**
     * Method getMapping.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Mapping[] getMapping() {
        Mapping[] array = new Mapping[0];
        return (Mapping[]) this.mappingList.toArray(array);
    }

    /**
     * Method getMappingCollection.Returns a reference to 'mappingList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Mapping> getMappingCollection() {
        return this.mappingList;
    }

    /**
     * Method getMappingCount.
     * 
     * @return the size of this collection
     */
    public int getMappingCount() {
        return this.mappingList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            mappingList);
        return hash;
    }

    /**
     * Method iterateMapping.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Mapping> iterateMapping() {
        return this.mappingList.iterator();
    }

    /**
     */
    public void removeAllMapping() {
        this.mappingList.clear();
    }

    /**
     * Method removeMapping.
     * 
     * @param vMapping
     * @return true if the object was removed from the collection.
     */
    public boolean removeMapping(final Mapping vMapping) {
        boolean removed = mappingList.remove(vMapping);
        return removed;
    }

    /**
     * Method removeMappingAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Mapping removeMappingAt(final int index) {
        Object obj = this.mappingList.remove(index);
        return (Mapping) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vMapping
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setMapping(final int index, final Mapping vMapping) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.mappingList.size()) {
            throw new IndexOutOfBoundsException("setMapping: Index value '" + index + "' not in range [0.." + (this.mappingList.size() - 1) + "]");
        }
        
        this.mappingList.set(index, vMapping);
    }

    /**
     * 
     * 
     * @param vMappingArray
     */
    public void setMapping(final Mapping[] vMappingArray) {
        //-- copy array
        mappingList.clear();
        
        for (int i = 0; i < vMappingArray.length; i++) {
                this.mappingList.add(vMappingArray[i]);
        }
    }

    /**
     * Sets the value of 'mappingList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vMappingList the Vector to copy.
     */
    public void setMapping(final List<Mapping> vMappingList) {
        // copy vector
        this.mappingList.clear();
        
        this.mappingList.addAll(vMappingList);
    }

    /**
     * Sets the value of 'mappingList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param mappingList the Vector to set.
     */
    public void setMappingCollection(final List<Mapping> mappingList) {
        this.mappingList = mappingList;
    }

}
