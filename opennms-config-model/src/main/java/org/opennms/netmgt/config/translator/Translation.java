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
 * List of OpenNMS events for which the Event Translator 
 *  will subscribe for translation.
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "translation")
@XmlAccessorType(XmlAccessType.FIELD)
public class Translation implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * This defines the allowable translations for a given
     *  event uei
     *  
     */
    @XmlElement(name = "event-translation-spec", required = true)
    private List<EventTranslationSpec> eventTranslationSpecList;

    public Translation() {
        this.eventTranslationSpecList = new ArrayList<EventTranslationSpec>();
    }

    /**
     * 
     * 
     * @param vEventTranslationSpec
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addEventTranslationSpec(final EventTranslationSpec vEventTranslationSpec) throws IndexOutOfBoundsException {
        this.eventTranslationSpecList.add(vEventTranslationSpec);
    }

    /**
     * 
     * 
     * @param index
     * @param vEventTranslationSpec
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addEventTranslationSpec(final int index, final EventTranslationSpec vEventTranslationSpec) throws IndexOutOfBoundsException {
        this.eventTranslationSpecList.add(index, vEventTranslationSpec);
    }

    /**
     * Method enumerateEventTranslationSpec.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<EventTranslationSpec> enumerateEventTranslationSpec() {
        return Collections.enumeration(this.eventTranslationSpecList);
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
        
        if (obj instanceof Translation) {
            Translation temp = (Translation)obj;
            boolean equals = Objects.equals(temp.eventTranslationSpecList, eventTranslationSpecList);
            return equals;
        }
        return false;
    }

    /**
     * Method getEventTranslationSpec.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the
     * EventTranslationSpec at the given index
     */
    public EventTranslationSpec getEventTranslationSpec(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.eventTranslationSpecList.size()) {
            throw new IndexOutOfBoundsException("getEventTranslationSpec: Index value '" + index + "' not in range [0.." + (this.eventTranslationSpecList.size() - 1) + "]");
        }
        
        return (EventTranslationSpec) eventTranslationSpecList.get(index);
    }

    /**
     * Method getEventTranslationSpec.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are changing in
     * another thread, we pass a 0-length Array of the correct type into the API
     * call.  This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public EventTranslationSpec[] getEventTranslationSpec() {
        EventTranslationSpec[] array = new EventTranslationSpec[0];
        return (EventTranslationSpec[]) this.eventTranslationSpecList.toArray(array);
    }

    /**
     * Method getEventTranslationSpecCollection.Returns a reference to
     * 'eventTranslationSpecList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<EventTranslationSpec> getEventTranslationSpecCollection() {
        return this.eventTranslationSpecList;
    }

    /**
     * Method getEventTranslationSpecCount.
     * 
     * @return the size of this collection
     */
    public int getEventTranslationSpecCount() {
        return this.eventTranslationSpecList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            eventTranslationSpecList);
        return hash;
    }

    /**
     * Method iterateEventTranslationSpec.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<EventTranslationSpec> iterateEventTranslationSpec() {
        return this.eventTranslationSpecList.iterator();
    }

    /**
     */
    public void removeAllEventTranslationSpec() {
        this.eventTranslationSpecList.clear();
    }

    /**
     * Method removeEventTranslationSpec.
     * 
     * @param vEventTranslationSpec
     * @return true if the object was removed from the collection.
     */
    public boolean removeEventTranslationSpec(final EventTranslationSpec vEventTranslationSpec) {
        boolean removed = eventTranslationSpecList.remove(vEventTranslationSpec);
        return removed;
    }

    /**
     * Method removeEventTranslationSpecAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public EventTranslationSpec removeEventTranslationSpecAt(final int index) {
        Object obj = this.eventTranslationSpecList.remove(index);
        return (EventTranslationSpec) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vEventTranslationSpec
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setEventTranslationSpec(final int index, final EventTranslationSpec vEventTranslationSpec) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.eventTranslationSpecList.size()) {
            throw new IndexOutOfBoundsException("setEventTranslationSpec: Index value '" + index + "' not in range [0.." + (this.eventTranslationSpecList.size() - 1) + "]");
        }
        
        this.eventTranslationSpecList.set(index, vEventTranslationSpec);
    }

    /**
     * 
     * 
     * @param vEventTranslationSpecArray
     */
    public void setEventTranslationSpec(final EventTranslationSpec[] vEventTranslationSpecArray) {
        //-- copy array
        eventTranslationSpecList.clear();
        
        for (int i = 0; i < vEventTranslationSpecArray.length; i++) {
                this.eventTranslationSpecList.add(vEventTranslationSpecArray[i]);
        }
    }

    /**
     * Sets the value of 'eventTranslationSpecList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vEventTranslationSpecList the Vector to copy.
     */
    public void setEventTranslationSpec(final List<EventTranslationSpec> vEventTranslationSpecList) {
        // copy vector
        this.eventTranslationSpecList.clear();
        
        this.eventTranslationSpecList.addAll(vEventTranslationSpecList);
    }

    /**
     * Sets the value of 'eventTranslationSpecList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param eventTranslationSpecList the Vector to set.
     */
    public void setEventTranslationSpecCollection(final List<EventTranslationSpec> eventTranslationSpecList) {
        this.eventTranslationSpecList = eventTranslationSpecList;
    }

}
