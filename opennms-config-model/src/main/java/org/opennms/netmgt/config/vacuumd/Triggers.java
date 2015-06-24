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
 * A collection of triggers
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "triggers")
@XmlAccessorType(XmlAccessType.FIELD)
public class Triggers implements Serializable {
    private static final long serialVersionUID = 2867131390371677435L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * A query to the database with a result set used for actions
     */
    @XmlElement(name = "trigger")
    private List<Trigger> _triggerList = new ArrayList<Trigger>(0);

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Triggers() {
        super();
    }

    public Triggers(final List<Trigger> triggers) {
        super();
        setTrigger(triggers);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     *
     *
     * @param vTrigger
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addTrigger(final Trigger vTrigger)
            throws IndexOutOfBoundsException {
        this._triggerList.add(vTrigger);
    }

    /**
     *
     *
     * @param index
     * @param vTrigger
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addTrigger(final int index, final Trigger vTrigger)
            throws IndexOutOfBoundsException {
        this._triggerList.add(index, vTrigger);
    }

    /**
     * Method enumerateTrigger.
     *
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Trigger> enumerateTrigger() {
        return Collections.enumeration(this._triggerList);
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

        if (obj instanceof Triggers) {

            Triggers temp = (Triggers) obj;
            if (this._triggerList != null) {
                if (temp._triggerList == null)
                    return false;
                else if (!(this._triggerList.equals(temp._triggerList)))
                    return false;
            } else if (temp._triggerList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getTrigger.
     *
     * @param index
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the Trigger at the given index
     */
    public Trigger getTrigger(final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._triggerList.size()) {
            throw new IndexOutOfBoundsException("getTrigger: Index value '"
                    + index + "' not in range [0.."
                    + (this._triggerList.size() - 1) + "]");
        }

        return (Trigger) _triggerList.get(index);
    }

    /**
     * Method getTrigger.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     *
     * @return this collection as an Array
     */
    public Trigger[] getTrigger() {
        Trigger[] array = new Trigger[0];
        return (Trigger[]) this._triggerList.toArray(array);
    }

    /**
     * Method getTriggerCollection.Returns a reference to '_triggerList'. No
     * type checking is performed on any modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public List<Trigger> getTriggerCollection() {
        return this._triggerList;
    }

    /**
     * Method getTriggerCount.
     *
     * @return the size of this collection
     */
    public int getTriggerCount() {
        return this._triggerList.size();
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

        if (_triggerList != null) {
            result = 37 * result + _triggerList.hashCode();
        }

        return result;
    }

    /**
     * Method iterateTrigger.
     *
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Trigger> iterateTrigger() {
        return this._triggerList.iterator();
    }

    /**
     */
    public void removeAllTrigger() {
        this._triggerList.clear();
    }

    /**
     * Method removeTrigger.
     *
     * @param vTrigger
     * @return true if the object was removed from the collection.
     */
    public boolean removeTrigger(final Trigger vTrigger) {
    	return _triggerList.remove(vTrigger);
    }

    /**
     * Method removeTriggerAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public Trigger removeTriggerAt(final int index) {
    	return (Trigger) this._triggerList.remove(index);
    }

    /**
     *
     *
     * @param index
     * @param vTrigger
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setTrigger(final int index, final Trigger vTrigger)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._triggerList.size()) {
            throw new IndexOutOfBoundsException("setTrigger: Index value '"
                    + index + "' not in range [0.."
                    + (this._triggerList.size() - 1) + "]");
        }

        this._triggerList.set(index, vTrigger);
    }

    /**
     *
     *
     * @param vTriggerArray
     */
    public void setTrigger(final Trigger[] vTriggerArray) {
        // -- copy array
        _triggerList.clear();

        for (int i = 0; i < vTriggerArray.length; i++) {
            this._triggerList.add(vTriggerArray[i]);
        }
    }

    /**
     * Sets the value of '_triggerList' by copying the given Vector. All
     * elements will be checked for type safety.
     *
     * @param vTriggerList
     *            the Vector to copy.
     */
    public void setTrigger(final List<Trigger> vTriggerList) {
        // copy vector
        this._triggerList.clear();

        this._triggerList.addAll(vTriggerList);
    }
}
