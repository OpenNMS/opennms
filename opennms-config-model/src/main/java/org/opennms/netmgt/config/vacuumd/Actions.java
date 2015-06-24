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
 * A collection of actions
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "actions")
@XmlAccessorType(XmlAccessType.FIELD)
public class Actions implements Serializable {
    private static final long serialVersionUID = -8231751311732262169L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * actions modify the database based on results of a trigger
     */
    @XmlElement(name="action")
    private List<Action> _actionList = new ArrayList<Action>(0);

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Actions() {
        super();
    }

    public Actions(final List<Action> actions) {
        super();
        setAction(actions);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     *
     *
     * @param vAction
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addAction(final Action vAction)
            throws IndexOutOfBoundsException {
        this._actionList.add(vAction);
    }

    /**
     *
     *
     * @param index
     * @param vAction
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addAction(final int index, final Action vAction)
            throws IndexOutOfBoundsException {
        this._actionList.add(index, vAction);
    }

    /**
     * Method enumerateAction.
     *
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Action> enumerateAction() {
        return Collections.enumeration(this._actionList);
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

        if (obj instanceof Actions) {

            Actions temp = (Actions) obj;
            if (this._actionList != null) {
                if (temp._actionList == null)
                    return false;
                else if (!(this._actionList.equals(temp._actionList)))
                    return false;
            } else if (temp._actionList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getAction.
     *
     * @param index
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the Action at the given index
     */
    public Action getAction(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._actionList.size()) {
            throw new IndexOutOfBoundsException("getAction: Index value '"
                    + index + "' not in range [0.."
                    + (this._actionList.size() - 1) + "]");
        }

        return (Action) _actionList.get(index);
    }

    /**
     * Method getAction.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     *
     * @return this collection as an Array
     */
    public Action[] getAction() {
        Action[] array = new Action[0];
        return (Action[]) this._actionList.toArray(array);
    }

    /**
     * Method getActionCollection.Returns a reference to '_actionList'. No
     * type checking is performed on any modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public List<Action> getActionCollection() {
        return this._actionList;
    }

    /**
     * Method getActionCount.
     *
     * @return the size of this collection
     */
    public int getActionCount() {
        return this._actionList.size();
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

        if (_actionList != null) {
            result = 37 * result + _actionList.hashCode();
        }

        return result;
    }

    /**
     * Method iterateAction.
     *
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Action> iterateAction() {
        return this._actionList.iterator();
    }

    /**
     * Method removeAction.
     *
     * @param vAction
     * @return true if the object was removed from the collection.
     */
    public boolean removeAction(final Action vAction) {
    	return _actionList.remove(vAction);
    }

    /**
     * Method removeActionAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public Action removeActionAt(final int index) {
    	return (Action) this._actionList.remove(index);
    }

    /**
     */
    public void removeAllAction() {
        this._actionList.clear();
    }

    /**
     *
     *
     * @param index
     * @param vAction
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setAction(final int index, final Action vAction)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._actionList.size()) {
            throw new IndexOutOfBoundsException("setAction: Index value '"
                    + index + "' not in range [0.."
                    + (this._actionList.size() - 1) + "]");
        }

        this._actionList.set(index, vAction);
    }

    /**
     *
     *
     * @param vActionArray
     */
    public void setAction(final Action[] vActionArray) {
        // -- copy array
        _actionList.clear();

        for (int i = 0; i < vActionArray.length; i++) {
            this._actionList.add(vActionArray[i]);
        }
    }

    /**
     * Sets the value of '_actionList' by copying the given Vector. All
     * elements will be checked for type safety.
     *
     * @param vActionList
     *            the Vector to copy.
     */
    public void setAction(final List<Action> vActionList) {
        // copy vector
        this._actionList.clear();

        this._actionList.addAll(vActionList);
    }
}
