/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.xml.event;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Class Events.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="events")
@XmlAccessorType(XmlAccessType.FIELD)
// @ValidateUsing("event.xsd")
public class Events implements Serializable {
	private static final long serialVersionUID = -6993861737101274987L;

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

	/**
     * Field _eventList.
     */
	@XmlElement(name="event")
    private java.util.List<org.opennms.netmgt.xml.event.Event> _eventList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Events() {
        super();
        this._eventList = new java.util.ArrayList<org.opennms.netmgt.xml.event.Event>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vEvent
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEvent(
            final org.opennms.netmgt.xml.event.Event vEvent)
    throws java.lang.IndexOutOfBoundsException {
        this._eventList.add(vEvent);
    }

    /**
     * 
     * 
     * @param index
     * @param vEvent
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEvent(
            final int index,
            final org.opennms.netmgt.xml.event.Event vEvent)
    throws java.lang.IndexOutOfBoundsException {
        this._eventList.add(index, vEvent);
    }

    /**
     * Method enumerateEvent.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.xml.event.Event> enumerateEvent(
    ) {
        return java.util.Collections.enumeration(this._eventList);
    }

    /**
     * Method getEvent.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the org.opennms.netmgt.xml.event.Event
     * at the given index
     */
    public org.opennms.netmgt.xml.event.Event getEvent(final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._eventList.size()) {
            throw new IndexOutOfBoundsException("getEvent: Index value '" + index + "' not in range [0.." + (this._eventList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.xml.event.Event) _eventList.get(index);
    }

    /**
     * Method getEvent.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.xml.event.Event[] getEvent(
    ) {
        org.opennms.netmgt.xml.event.Event[] array = new org.opennms.netmgt.xml.event.Event[0];
        return (org.opennms.netmgt.xml.event.Event[]) this._eventList.toArray(array);
    }

    /**
     * Method getEventCollection.Returns a reference to
     * '_eventList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.xml.event.Event> getEventCollection(
    ) {
        return this._eventList;
    }

    /**
     * Method getEventCount.
     * 
     * @return the size of this collection
     */
    public int getEventCount(
    ) {
        return this._eventList.size();
    }

    /**
     * Method iterateEvent.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.xml.event.Event> iterateEvent(
    ) {
        return this._eventList.iterator();
    }

    /**
     */
    public void removeAllEvent(
    ) {
        this._eventList.clear();
    }

    /**
     * Method removeEvent.
     * 
     * @param vEvent
     * @return true if the object was removed from the collection.
     */
    public boolean removeEvent(
            final org.opennms.netmgt.xml.event.Event vEvent) {
        boolean removed = _eventList.remove(vEvent);
        return removed;
    }

    /**
     * Method removeEventAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.xml.event.Event removeEventAt(
            final int index) {
        java.lang.Object obj = this._eventList.remove(index);
        return (org.opennms.netmgt.xml.event.Event) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vEvent
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setEvent(
            final int index,
            final org.opennms.netmgt.xml.event.Event vEvent)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._eventList.size()) {
            throw new IndexOutOfBoundsException("setEvent: Index value '" + index + "' not in range [0.." + (this._eventList.size() - 1) + "]");
        }
        
        this._eventList.set(index, vEvent);
    }

    /**
     * 
     * 
     * @param vEventArray
     */
    public void setEvent(
            final org.opennms.netmgt.xml.event.Event[] vEventArray) {
        //-- copy array
        _eventList.clear();
        
        for (int i = 0; i < vEventArray.length; i++) {
                this._eventList.add(vEventArray[i]);
        }
    }

    /**
     * Sets the value of '_eventList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vEventList the Vector to copy.
     */
    public void setEvent(
            final java.util.List<org.opennms.netmgt.xml.event.Event> vEventList) {
        // copy vector
        this._eventList.clear();
        
        this._eventList.addAll(vEventList);
    }

    /**
     * Sets the value of '_eventList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param eventList the Vector to set.
     */
    public void setEventCollection(
            final java.util.List<org.opennms.netmgt.xml.event.Event> eventList) {
        this._eventList = eventList;
    }

        @Override
    public String toString() {
    	return new ToStringBuilder(this)
    		.append("event", _eventList)
    		.toString();
    }
}
