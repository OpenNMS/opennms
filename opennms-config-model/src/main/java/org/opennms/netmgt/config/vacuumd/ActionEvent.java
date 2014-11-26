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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class ActionEvent.
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "action-event")
@XmlAccessorType(XmlAccessType.FIELD)
public class ActionEvent implements Serializable {
    private static final long serialVersionUID = 1286974132304106079L;

    private static final boolean DEFAULT_FOR_EACH_RESULT_FLAG = false;

    private static final boolean DEFAULT_ADD_ALL_PARMS_FLAG = false;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Field _name.
     */
    @XmlAttribute(name = "name")
    private String _name;

    /**
     * Field _forEachResult.
     */
    @XmlAttribute(name = "for-each-result")
    private Boolean _forEachResult;

    /**
     * Field _addAllParms.
     */
    @XmlAttribute(name = "add-all-parms")
    private Boolean _addAllParms;

    /**
     * Field _assignmentList.
     */
    @XmlElement(name = "assignment")
    private List<Assignment> _assignmentList = new ArrayList<Assignment>(0);

    // ----------------/
    // - Constructors -/
    // ----------------/

    public ActionEvent() {
        super();
    }

    public ActionEvent(final String name, final boolean forEachResult,
            final boolean addAllParms, final List<Assignment> assignments) {
        super();
        setName(name);
        setForEachResult(forEachResult);
        setAddAllParms(addAllParms);
        setAssignment(assignments);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     *
     *
     * @param vAssignment
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addAssignment(final Assignment vAssignment)
            throws IndexOutOfBoundsException {
        this._assignmentList.add(vAssignment);
    }

    /**
     *
     *
     * @param index
     * @param vAssignment
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addAssignment(final int index, final Assignment vAssignment)
            throws IndexOutOfBoundsException {
        this._assignmentList.add(index, vAssignment);
    }

    /**
     * Method enumerateAssignment.
     *
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Assignment> enumerateAssignment() {
        return Collections.enumeration(this._assignmentList);
    }

    /**
     * Overrides the Object.equals method.
     *
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ActionEvent other = (ActionEvent) obj;
        if (_addAllParms == null) {
            if (other._addAllParms != null)
                return false;
        } else if (!_addAllParms.equals(other._addAllParms))
            return false;
        if (_assignmentList == null) {
            if (other._assignmentList != null)
                return false;
        } else if (!_assignmentList.equals(other._assignmentList))
            return false;
        if (_forEachResult == null) {
            if (other._forEachResult != null)
                return false;
        } else if (!_forEachResult.equals(other._forEachResult))
            return false;
        if (_name == null) {
            if (other._name != null)
                return false;
        } else if (!_name.equals(other._name))
            return false;
        return true;
    }

    /**
     * Returns the value of field 'addAllParms'.
     *
     * @return the value of field 'AddAllParms'.
     */
    public boolean getAddAllParms() {
        return _addAllParms == null ? DEFAULT_ADD_ALL_PARMS_FLAG
                                   : _addAllParms;
    }

    /**
     * Method getAssignment.
     *
     * @param index
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the Assignment at the given index
     */
    public Assignment getAssignment(final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._assignmentList.size()) {
            throw new IndexOutOfBoundsException(
                                                "getAssignment: Index value '"
                                                        + index
                                                        + "' not in range [0.."
                                                        + (this._assignmentList.size() - 1)
                                                        + "]");
        }

        return (Assignment) _assignmentList.get(index);
    }

    /**
     * Method getAssignment.Returns the contents of the collection in an
     * Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     *
     * @return this collection as an Array
     */
    public Assignment[] getAssignment() {
        Assignment[] array = new Assignment[0];
        return (Assignment[]) this._assignmentList.toArray(array);
    }

    /**
     * Method getAssignmentCollection.Returns a reference to
     * '_assignmentList'. No type checking is performed on any modifications
     * to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public List<Assignment> getAssignmentCollection() {
        return this._assignmentList;
    }

    /**
     * Method getAssignmentCount.
     *
     * @return the size of this collection
     */
    public int getAssignmentCount() {
        return this._assignmentList.size();
    }

    /**
     * Returns the value of field 'forEachResult'.
     *
     * @return the value of field 'ForEachResult'.
     */
    public boolean getForEachResult() {
        return _forEachResult == null ? DEFAULT_FOR_EACH_RESULT_FLAG
                                     : _forEachResult;
    }

    /**
     * Returns the value of field 'name'.
     *
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this._name;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     *
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_addAllParms == null) ? 0 : _addAllParms.hashCode());
        result = prime
                * result
                + ((_assignmentList == null) ? 0 : _assignmentList.hashCode());
        result = prime * result
                + ((_forEachResult == null) ? 0 : _forEachResult.hashCode());
        result = prime * result + ((_name == null) ? 0 : _name.hashCode());
        return result;
    }

    /**
     * Returns the value of field 'addAllParms'.
     *
     * @return the value of field 'AddAllParms'.
     */
    public boolean isAddAllParms() {
        return _addAllParms == null ? DEFAULT_ADD_ALL_PARMS_FLAG
                                   : _addAllParms;
    }

    /**
     * Returns the value of field 'forEachResult'.
     *
     * @return the value of field 'ForEachResult'.
     */
    public boolean isForEachResult() {
        return _forEachResult == null ? DEFAULT_FOR_EACH_RESULT_FLAG
                                     : _forEachResult;
    }

    /**
     * Method iterateAssignment.
     *
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Assignment> iterateAssignment() {
        return this._assignmentList.iterator();
    }

    /**
     */
    public void removeAllAssignment() {
        this._assignmentList.clear();
    }

    /**
     * Method removeAssignment.
     *
     * @param vAssignment
     * @return true if the object was removed from the collection.
     */
    public boolean removeAssignment(final Assignment vAssignment) {
        return _assignmentList.remove(vAssignment);
    }

    /**
     * Method removeAssignmentAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public Assignment removeAssignmentAt(final int index) {
        return this._assignmentList.remove(index);
    }

    /**
     * Sets the value of field 'addAllParms'.
     *
     * @param addAllParms
     *            the value of field 'addAllParms'.
     */
    public void setAddAllParms(final boolean addAllParms) {
        this._addAllParms = addAllParms;
    }

    /**
     *
     *
     * @param index
     * @param vAssignment
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setAssignment(final int index, final Assignment vAssignment)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._assignmentList.size()) {
            throw new IndexOutOfBoundsException(
                                                "setAssignment: Index value '"
                                                        + index
                                                        + "' not in range [0.."
                                                        + (this._assignmentList.size() - 1)
                                                        + "]");
        }

        this._assignmentList.set(index, vAssignment);
    }

    /**
     *
     *
     * @param vAssignmentArray
     */
    public void setAssignment(final Assignment[] vAssignmentArray) {
        // -- copy array
        _assignmentList.clear();

        for (int i = 0; i < vAssignmentArray.length; i++) {
            this._assignmentList.add(vAssignmentArray[i]);
        }
    }

    /**
     * Sets the value of '_assignmentList' by copying the given Vector. All
     * elements will be checked for type safety.
     *
     * @param vAssignmentList
     *            the Vector to copy.
     */
    public void setAssignment(final List<Assignment> vAssignmentList) {
        // copy vector
        this._assignmentList.clear();

        this._assignmentList.addAll(vAssignmentList);
    }

    /**
     * Sets the value of field 'forEachResult'.
     *
     * @param forEachResult
     *            the value of field 'forEachResult'.
     */
    public void setForEachResult(final boolean forEachResult) {
        this._forEachResult = forEachResult;
    }

    /**
     * Sets the value of field 'name'.
     *
     * @param name
     *            the value of field 'name'.
     */
    public void setName(final String name) {
        this._name = name;
    }
}
