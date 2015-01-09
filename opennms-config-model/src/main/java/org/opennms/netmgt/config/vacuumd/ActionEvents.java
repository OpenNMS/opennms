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
 * Class ActionEvents.
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "action-events")
@XmlAccessorType(XmlAccessType.FIELD)
public class ActionEvents implements Serializable {
    private static final long serialVersionUID = 7842412566621127116L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Field _actionEventList.
     */
    @XmlElement(name="action-event")
    private List<ActionEvent> _actionEventList = new ArrayList<ActionEvent>(0);

    // ----------------/
    // - Constructors -/
    // ----------------/

    public ActionEvents() {
        super();
    }

    public ActionEvents(final List<ActionEvent> actionEvents) {
        super();
        setActionEvent(actionEvents);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     *
     *
     * @param vActionEvent
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addActionEvent(final ActionEvent vActionEvent)
            throws IndexOutOfBoundsException {
        this._actionEventList.add(vActionEvent);
    }

    /**
     *
     *
     * @param index
     * @param vActionEvent
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addActionEvent(final int index, final ActionEvent vActionEvent)
            throws IndexOutOfBoundsException {
        this._actionEventList.add(index, vActionEvent);
    }

    /**
     * Method enumerateActionEvent.
     *
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<ActionEvent> enumerateActionEvent() {
        return Collections.enumeration(this._actionEventList);
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

        if (obj instanceof ActionEvents) {

            ActionEvents temp = (ActionEvents) obj;
            if (this._actionEventList != null) {
                if (temp._actionEventList == null)
                    return false;
                else if (!(this._actionEventList.equals(temp._actionEventList)))
                    return false;
            } else if (temp._actionEventList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getActionEvent.
     *
     * @param index
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the ActionEvent at the given index
     */
    public ActionEvent getActionEvent(final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._actionEventList.size()) {
            throw new IndexOutOfBoundsException(
                                                "getActionEvent: Index value '"
                                                        + index
                                                        + "' not in range [0.."
                                                        + (this._actionEventList.size() - 1)
                                                        + "]");
        }

        return (ActionEvent) _actionEventList.get(index);
    }

    /**
     * Method getActionEvent.Returns the contents of the collection in an
     * Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     *
     * @return this collection as an Array
     */
    public ActionEvent[] getActionEvent() {
        ActionEvent[] array = new ActionEvent[0];
        return (ActionEvent[]) this._actionEventList.toArray(array);
    }

    /**
     * Method getActionEventCollection.Returns a reference to
     * '_actionEventList'. No type checking is performed on any modifications
     * to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public List<ActionEvent> getActionEventCollection() {
        return this._actionEventList;
    }

    /**
     * Method getActionEventCount.
     *
     * @return the size of this collection
     */
    public int getActionEventCount() {
        return this._actionEventList.size();
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

        if (_actionEventList != null) {
            result = 37 * result + _actionEventList.hashCode();
        }

        return result;
    }

    /**
     * Method iterateActionEvent.
     *
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<ActionEvent> iterateActionEvent() {
        return this._actionEventList.iterator();
    }

    /**
     * Method removeActionEvent.
     *
     * @param vActionEvent
     * @return true if the object was removed from the collection.
     */
    public boolean removeActionEvent(final ActionEvent vActionEvent) {
    	return _actionEventList.remove(vActionEvent);
    }

    /**
     * Method removeActionEventAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public ActionEvent removeActionEventAt(final int index) {
    	return (ActionEvent) this._actionEventList.remove(index);
    }

    /**
     */
    public void removeAllActionEvent() {
        this._actionEventList.clear();
    }

    /**
     *
     *
     * @param index
     * @param vActionEvent
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setActionEvent(final int index, final ActionEvent vActionEvent)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._actionEventList.size()) {
            throw new IndexOutOfBoundsException(
                                                "setActionEvent: Index value '"
                                                        + index
                                                        + "' not in range [0.."
                                                        + (this._actionEventList.size() - 1)
                                                        + "]");
        }

        this._actionEventList.set(index, vActionEvent);
    }

    /**
     *
     *
     * @param vActionEventArray
     */
    public void setActionEvent(final ActionEvent[] vActionEventArray) {
        // -- copy array
        _actionEventList.clear();

        for (int i = 0; i < vActionEventArray.length; i++) {
            this._actionEventList.add(vActionEventArray[i]);
        }
    }

    /**
     * Sets the value of '_actionEventList' by copying the given Vector. All
     * elements will be checked for type safety.
     *
     * @param vActionEventList
     *            the Vector to copy.
     */
    public void setActionEvent(final List<ActionEvent> vActionEventList) {
        // copy vector
        this._actionEventList.clear();

        this._actionEventList.addAll(vActionEventList);
    }
}
