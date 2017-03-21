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

package org.opennms.netmgt.config.threshd;

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
 * Top-level element for the thresholds.xml configuration file.
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "thresholding-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class ThresholdingConfig implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Thresholding group element
     */
    @XmlElement(name = "group")
    private List<Group> groupList = new ArrayList<>();

    public ThresholdingConfig() { }

    /**
     * 
     * 
     * @param vGroup
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addGroup(final Group vGroup) throws IndexOutOfBoundsException {
        this.groupList.add(vGroup);
    }

    /**
     * 
     * 
     * @param index
     * @param vGroup
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addGroup(final int index, final Group vGroup) throws IndexOutOfBoundsException {
        this.groupList.add(index, vGroup);
    }

    /**
     * Method enumerateGroup.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Group> enumerateGroup() {
        return Collections.enumeration(this.groupList);
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
        
        if (obj instanceof ThresholdingConfig) {
            ThresholdingConfig temp = (ThresholdingConfig)obj;
            boolean equals = Objects.equals(temp.groupList, groupList);
            return equals;
        }
        return false;
    }

    /**
     * Method getGroup.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Group at the
     * given index
     */
    public Group getGroup(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.groupList.size()) {
            throw new IndexOutOfBoundsException("getGroup: Index value '" + index + "' not in range [0.." + (this.groupList.size() - 1) + "]");
        }
        
        return (Group) groupList.get(index);
    }

    /**
     * Method getGroup.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Group[] getGroup() {
        Group[] array = new Group[0];
        return (Group[]) this.groupList.toArray(array);
    }

    /**
     * Method getGroupCollection.Returns a reference to 'groupList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Group> getGroupCollection() {
        return this.groupList;
    }

    /**
     * Method getGroupCount.
     * 
     * @return the size of this collection
     */
    public int getGroupCount() {
        return this.groupList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            groupList);
        return hash;
    }

    /**
     * Method iterateGroup.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Group> iterateGroup() {
        return this.groupList.iterator();
    }

    /**
     */
    public void removeAllGroup() {
        this.groupList.clear();
    }

    /**
     * Method removeGroup.
     * 
     * @param vGroup
     * @return true if the object was removed from the collection.
     */
    public boolean removeGroup(final Group vGroup) {
        boolean removed = groupList.remove(vGroup);
        return removed;
    }

    /**
     * Method removeGroupAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Group removeGroupAt(final int index) {
        Object obj = this.groupList.remove(index);
        return (Group) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vGroup
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setGroup(final int index, final Group vGroup) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.groupList.size()) {
            throw new IndexOutOfBoundsException("setGroup: Index value '" + index + "' not in range [0.." + (this.groupList.size() - 1) + "]");
        }
        
        this.groupList.set(index, vGroup);
    }

    /**
     * 
     * 
     * @param vGroupArray
     */
    public void setGroup(final Group[] vGroupArray) {
        //-- copy array
        groupList.clear();
        
        for (int i = 0; i < vGroupArray.length; i++) {
                this.groupList.add(vGroupArray[i]);
        }
    }

    /**
     * Sets the value of 'groupList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vGroupList the Vector to copy.
     */
    public void setGroup(final List<Group> vGroupList) {
        // copy vector
        this.groupList.clear();
        
        this.groupList.addAll(vGroupList);
    }

    /**
     * Sets the value of 'groupList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param groupList the Vector to set.
     */
    public void setGroupCollection(final List<Group> groupList) {
        this.groupList = groupList;
    }

}
