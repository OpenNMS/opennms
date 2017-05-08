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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A mapping for a given event. This translation is only
 *  applied if it is the first that matches
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "mapping")
@XmlAccessorType(XmlAccessType.FIELD)
public class Mapping implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "preserve-snmp-data")
    private Boolean preserveSnmpData;

    /**
     * An element representing an assignement to an attribute of the event
     *  
     */
    @XmlElement(name = "assignment")
    private List<Assignment> assignmentList = new ArrayList<>();

    /**
     * 
     * 
     * @param vAssignment
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addAssignment(final Assignment vAssignment) throws IndexOutOfBoundsException {
        this.assignmentList.add(vAssignment);
    }

    /**
     * 
     * 
     * @param index
     * @param vAssignment
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addAssignment(final int index, final Assignment vAssignment) throws IndexOutOfBoundsException {
        this.assignmentList.add(index, vAssignment);
    }

    /**
     */
    public void deletePreserveSnmpData() {
        this.preserveSnmpData= null;
    }

    /**
     * Method enumerateAssignment.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Assignment> enumerateAssignment() {
        return Collections.enumeration(this.assignmentList);
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
        
        if (obj instanceof Mapping) {
            Mapping temp = (Mapping)obj;
            boolean equals = Objects.equals(temp.preserveSnmpData, preserveSnmpData)
                && Objects.equals(temp.assignmentList, assignmentList);
            return equals;
        }
        return false;
    }

    /**
     * Method getAssignment.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Assignment at
     * the given index
     */
    public Assignment getAssignment(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.assignmentList.size()) {
            throw new IndexOutOfBoundsException("getAssignment: Index value '" + index + "' not in range [0.." + (this.assignmentList.size() - 1) + "]");
        }
        
        return (Assignment) assignmentList.get(index);
    }

    /**
     * Method getAssignment.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Assignment[] getAssignment() {
        Assignment[] array = new Assignment[0];
        return (Assignment[]) this.assignmentList.toArray(array);
    }

    /**
     * Method getAssignmentCollection.Returns a reference to 'assignmentList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Assignment> getAssignmentCollection() {
        return this.assignmentList;
    }

    /**
     * Method getAssignmentCount.
     * 
     * @return the size of this collection
     */
    public int getAssignmentCount() {
        return this.assignmentList.size();
    }

    /**
     * Returns the value of field 'preserveSnmpData'.
     * 
     * @return the value of field 'PreserveSnmpData'.
     */
    public Boolean getPreserveSnmpData() {
        return this.preserveSnmpData != null ? this.preserveSnmpData : Boolean.valueOf("false");
    }

    /**
     * Method hasPreserveSnmpData.
     * 
     * @return true if at least one PreserveSnmpData has been added
     */
    public boolean hasPreserveSnmpData() {
        return this.preserveSnmpData != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            preserveSnmpData, 
            assignmentList);
        return hash;
    }

    /**
     * Returns the value of field 'preserveSnmpData'.
     * 
     * @return the value of field 'PreserveSnmpData'.
     */
    public Boolean isPreserveSnmpData() {
        return this.preserveSnmpData != null ? this.preserveSnmpData : Boolean.valueOf("false");
    }

    /**
     * Method iterateAssignment.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Assignment> iterateAssignment() {
        return this.assignmentList.iterator();
    }

    /**
     */
    public void removeAllAssignment() {
        this.assignmentList.clear();
    }

    /**
     * Method removeAssignment.
     * 
     * @param vAssignment
     * @return true if the object was removed from the collection.
     */
    public boolean removeAssignment(final Assignment vAssignment) {
        boolean removed = assignmentList.remove(vAssignment);
        return removed;
    }

    /**
     * Method removeAssignmentAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Assignment removeAssignmentAt(final int index) {
        Object obj = this.assignmentList.remove(index);
        return (Assignment) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vAssignment
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setAssignment(final int index, final Assignment vAssignment) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.assignmentList.size()) {
            throw new IndexOutOfBoundsException("setAssignment: Index value '" + index + "' not in range [0.." + (this.assignmentList.size() - 1) + "]");
        }
        
        this.assignmentList.set(index, vAssignment);
    }

    /**
     * 
     * 
     * @param vAssignmentArray
     */
    public void setAssignment(final Assignment[] vAssignmentArray) {
        //-- copy array
        assignmentList.clear();
        
        for (int i = 0; i < vAssignmentArray.length; i++) {
                this.assignmentList.add(vAssignmentArray[i]);
        }
    }

    /**
     * Sets the value of 'assignmentList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vAssignmentList the Vector to copy.
     */
    public void setAssignment(final List<Assignment> vAssignmentList) {
        // copy vector
        this.assignmentList.clear();
        
        this.assignmentList.addAll(vAssignmentList);
    }

    /**
     * Sets the value of 'assignmentList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param assignmentList the Vector to set.
     */
    public void setAssignmentCollection(final List<Assignment> assignmentList) {
        this.assignmentList = assignmentList;
    }

    /**
     * Sets the value of field 'preserveSnmpData'.
     * 
     * @param preserveSnmpData the value of field 'preserveSnmpData'.
     */
    public void setPreserveSnmpData(final Boolean preserveSnmpData) {
        this.preserveSnmpData = preserveSnmpData;
    }

}
