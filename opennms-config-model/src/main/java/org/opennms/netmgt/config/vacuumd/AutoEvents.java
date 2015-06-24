/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

/**
 * This class was original generated with Castor, but is no longer.
 */
package org.opennms.netmgt.config.vacuumd;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class AutoEvents.
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "auto-events")
@XmlAccessorType(XmlAccessType.FIELD)
public class AutoEvents implements Serializable {
    private static final long serialVersionUID = 8553439381132405258L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * (THIS IS BEING DEPRECATED) actions modify the database based on results
     * of a trigger
     */
    @XmlElement(name = "auto-event")
    private List<AutoEvent> _autoEventList = new ArrayList<AutoEvent>();

    // ----------------/
    // - Constructors -/
    // ----------------/

    public AutoEvents() {
        super();
    }

    public AutoEvents(final List<AutoEvent> autoEvents) {
        super();
        setAutoEvent(autoEvents);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     *
     *
     * @param vAutoEvent
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addAutoEvent(final AutoEvent vAutoEvent)
            throws IndexOutOfBoundsException {
        this._autoEventList.add(vAutoEvent);
    }

    /**
     *
     *
     * @param index
     * @param vAutoEvent
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addAutoEvent(final int index, final AutoEvent vAutoEvent)
            throws IndexOutOfBoundsException {
        this._autoEventList.add(index, vAutoEvent);
    }

    /**
     * Method enumerateAutoEvent.
     *
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<AutoEvent> enumerateAutoEvent() {
        return Collections.enumeration(this._autoEventList);
    }

    /**
     * Overrides the Object.equals method.
     *
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof AutoEvents) {

            AutoEvents temp = (AutoEvents) obj;
            if (this._autoEventList != null) {
                if (temp._autoEventList == null)
                    return false;
                else if (!(this._autoEventList.equals(temp._autoEventList)))
                    return false;
            } else if (temp._autoEventList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getAutoEvent.
     *
     * @param index
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the AutoEvent at the given inde
     */
    public AutoEvent getAutoEvent(final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._autoEventList.size()) {
            throw new IndexOutOfBoundsException("getAutoEvent: Index value '"
                    + index + "' not in range [0.."
                    + (this._autoEventList.size() - 1) + "]");
        }

        return (AutoEvent) _autoEventList.get(index);
    }

    /**
     * Method getAutoEvent.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     *
     * @return this collection as an Array
     */
    public AutoEvent[] getAutoEvent() {
        AutoEvent[] array = new AutoEvent[0];
        return (AutoEvent[]) this._autoEventList.toArray(array);
    }

    /**
     * Method getAutoEventCollection.Returns a reference to '_autoEventList'.
     * No type checking is performed on any modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public List<AutoEvent> getAutoEventCollection() {
        return this._autoEventList;
    }

    /**
     * Method getAutoEventCount.
     *
     * @return the size of this collection
     */
    public int getAutoEventCount() {
        return this._autoEventList.size();
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     *
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;

        if (_autoEventList != null) {
            result = 37 * result + _autoEventList.hashCode();
        }

        return result;
    }

    /**
     * Method iterateAutoEvent.
     *
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<AutoEvent> iterateAutoEvent() {
        return this._autoEventList.iterator();
    }

    /**
     */
    public void removeAllAutoEvent() {
        this._autoEventList.clear();
    }

    /**
     * Method removeAutoEvent.
     *
     * @param vAutoEvent
     * @return true if the object was removed from the collection.
     */
    public boolean removeAutoEvent(final AutoEvent vAutoEvent) {
    	return _autoEventList.remove(vAutoEvent);
    }

    /**
     * Method removeAutoEventAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public AutoEvent removeAutoEventAt(final int index) {
    	return (AutoEvent) this._autoEventList.remove(index);
    }

    /**
     *
     *
     * @param index
     * @param vAutoEvent
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setAutoEvent(final int index, final AutoEvent vAutoEvent)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._autoEventList.size()) {
            throw new IndexOutOfBoundsException("setAutoEvent: Index value '"
                    + index + "' not in range [0.."
                    + (this._autoEventList.size() - 1) + "]");
        }

        this._autoEventList.set(index, vAutoEvent);
    }

    /**
     *
     *
     * @param vAutoEventArray
     */
    public void setAutoEvent(final AutoEvent[] vAutoEventArray) {
        // -- copy array
        _autoEventList.clear();

        for (int i = 0; i < vAutoEventArray.length; i++) {
            this._autoEventList.add(vAutoEventArray[i]);
        }
    }

    /**
     * Sets the value of '_autoEventList' by copying the given Vector. All
     * elements will be checked for type safety.
     *
     * @param vAutoEventList
     *            the Vector to copy.
     */
    public void setAutoEvent(final List<AutoEvent> vAutoEventList) {
        // copy vector
        this._autoEventList.clear();

        this._autoEventList.addAll(vAutoEventList);
    }
}
