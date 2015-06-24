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
 * Class Automations.
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "automations")
@XmlAccessorType(XmlAccessType.FIELD)
public class Automations implements Serializable {
    private static final long serialVersionUID = 7676358751414193033L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Triggers and Actions hooked up and run by the Vacuumd schedule using
     * interval for frequency
     */
    @XmlElement(name = "automation")
    private List<Automation> _automationList = new ArrayList<Automation>(0);

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Automations() {
        super();
    }

    public Automations(List<Automation> automations) {
        super();
        _automationList = automations;
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     *
     *
     * @param vAutomation
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addAutomation(final Automation vAutomation)
            throws IndexOutOfBoundsException {
        this._automationList.add(vAutomation);
    }

    /**
     *
     *
     * @param index
     * @param vAutomation
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addAutomation(final int index, final Automation vAutomation)
            throws IndexOutOfBoundsException {
        this._automationList.add(index, vAutomation);
    }

    /**
     * Method enumerateAutomation.
     *
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Automation> enumerateAutomation() {
        return Collections.enumeration(this._automationList);
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

        if (obj instanceof Automations) {

            Automations temp = (Automations) obj;
            if (this._automationList != null) {
                if (temp._automationList == null)
                    return false;
                else if (!(this._automationList.equals(temp._automationList)))
                    return false;
            } else if (temp._automationList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getAutomation.
     *
     * @param index
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the Automation at the given index
     */
    public Automation getAutomation(final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._automationList.size()) {
            throw new IndexOutOfBoundsException(
                                                "getAutomation: Index value '"
                                                        + index
                                                        + "' not in range [0.."
                                                        + (this._automationList.size() - 1)
                                                        + "]");
        }

        return (Automation) _automationList.get(index);
    }

    /**
     * Method getAutomation.Returns the contents of the collection in an
     * Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     *
     * @return this collection as an Array
     */
    public Automation[] getAutomation() {
        Automation[] array = new Automation[0];
        return (Automation[]) this._automationList.toArray(array);
    }

    /**
     * Method getAutomationCollection.Returns a reference to
     * '_automationList'. No type checking is performed on any modifications
     * to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public List<Automation> getAutomationCollection() {
        return this._automationList;
    }

    /**
     * Method getAutomationCount.
     *
     * @return the size of this collection
     */
    public int getAutomationCount() {
        return this._automationList.size();
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

        if (_automationList != null) {
            result = 37 * result + _automationList.hashCode();
        }

        return result;
    }

    /**
     * Method iterateAutomation.
     *
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Automation> iterateAutomation() {
        return this._automationList.iterator();
    }

    /**
     */
    public void removeAllAutomation() {
        this._automationList.clear();
    }

    /**
     * Method removeAutomation.
     *
     * @param vAutomation
     * @return true if the object was removed from the collection.
     */
    public boolean removeAutomation(final Automation vAutomation) {
    	return _automationList.remove(vAutomation);
    }

    /**
     * Method removeAutomationAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public Automation removeAutomationAt(final int index) {
    	return (Automation) this._automationList.remove(index);
    }

    /**
     *
     *
     * @param index
     * @param vAutomation
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setAutomation(final int index, final Automation vAutomation)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._automationList.size()) {
            throw new IndexOutOfBoundsException(
                                                "setAutomation: Index value '"
                                                        + index
                                                        + "' not in range [0.."
                                                        + (this._automationList.size() - 1)
                                                        + "]");
        }

        this._automationList.set(index, vAutomation);
    }

    /**
     *
     *
     * @param vAutomationArray
     */
    public void setAutomation(final Automation[] vAutomationArray) {
        // -- copy array
        _automationList.clear();

        for (int i = 0; i < vAutomationArray.length; i++) {
            this._automationList.add(vAutomationArray[i]);
        }
    }

    /**
     * Sets the value of '_automationList' by copying the given Vector. All
     * elements will be checked for type safety.
     *
     * @param vAutomationList
     *            the Vector to copy.
     */
    public void setAutomation(final List<Automation> vAutomationList) {
        // copy vector
        this._automationList.clear();

        this._automationList.addAll(vAutomationList);
    }
}
