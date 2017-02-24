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

package org.opennms.netmgt.config.rancid.adapter;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class BasicSchedule.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "basicSchedule")
@XmlAccessorType(XmlAccessType.FIELD)
public class BasicSchedule implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * outage name
     */
    @XmlAttribute(name = "name", required = true)
    private String name;

    /**
     * outage type
     */
    @XmlAttribute(name = "type", required = true)
    private String type;

    /**
     * defines start/end time for the outage
     */
    @XmlElement(name = "time", required = true)
    private java.util.List<Time> timeList;

    public BasicSchedule() {
        this.timeList = new java.util.ArrayList<Time>();
    }

    /**
     * 
     * 
     * @param vTime
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addTime(final Time vTime) throws IndexOutOfBoundsException {
        this.timeList.add(vTime);
    }

    /**
     * 
     * 
     * @param index
     * @param vTime
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addTime(final int index, final Time vTime) throws IndexOutOfBoundsException {
        this.timeList.add(index, vTime);
    }

    /**
     * Method enumerateTime.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<Time> enumerateTime() {
        return java.util.Collections.enumeration(this.timeList);
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
        
        if (obj instanceof BasicSchedule) {
            BasicSchedule temp = (BasicSchedule)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.type, type)
                && Objects.equals(temp.timeList, timeList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the following
     * description: outage name
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Method getTime.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Time at
     * the given index
     */
    public Time getTime(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.timeList.size()) {
            throw new IndexOutOfBoundsException("getTime: Index value '" + index + "' not in range [0.." + (this.timeList.size() - 1) + "]");
        }
        
        return (Time) timeList.get(index);
    }

    /**
     * Method getTime.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Time[] getTime() {
        Time[] array = new Time[0];
        return (Time[]) this.timeList.toArray(array);
    }

    /**
     * Method getTimeCollection.Returns a reference to 'timeList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Time> getTimeCollection() {
        return this.timeList;
    }

    /**
     * Method getTimeCount.
     * 
     * @return the size of this collection
     */
    public int getTimeCount() {
        return this.timeList.size();
    }

    /**
     * Returns the value of field 'type'. The field 'type' has the following
     * description: outage type
     * 
     * @return the value of field 'Type'.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            name, 
            type, 
            timeList);
        return hash;
    }

    /**
     * Method iterateTime.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<Time> iterateTime() {
        return this.timeList.iterator();
    }

    /**
     */
    public void removeAllTime() {
        this.timeList.clear();
    }

    /**
     * Method removeTime.
     * 
     * @param vTime
     * @return true if the object was removed from the collection.
     */
    public boolean removeTime(final Time vTime) {
        boolean removed = timeList.remove(vTime);
        return removed;
    }

    /**
     * Method removeTimeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Time removeTimeAt(final int index) {
        Object obj = this.timeList.remove(index);
        return (Time) obj;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the following
     * description: outage name
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * 
     * 
     * @param index
     * @param vTime
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setTime(final int index, final Time vTime) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.timeList.size()) {
            throw new IndexOutOfBoundsException("setTime: Index value '" + index + "' not in range [0.." + (this.timeList.size() - 1) + "]");
        }
        
        this.timeList.set(index, vTime);
    }

    /**
     * 
     * 
     * @param vTimeArray
     */
    public void setTime(final Time[] vTimeArray) {
        //-- copy array
        timeList.clear();
        
        for (int i = 0; i < vTimeArray.length; i++) {
                this.timeList.add(vTimeArray[i]);
        }
    }

    /**
     * Sets the value of 'timeList' by copying the given Vector. All elements will
     * be checked for type safety.
     * 
     * @param vTimeList the Vector to copy.
     */
    public void setTime(final java.util.List<Time> vTimeList) {
        // copy vector
        this.timeList.clear();
        
        this.timeList.addAll(vTimeList);
    }

    /**
     * Sets the value of 'timeList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param timeList the Vector to set.
     */
    public void setTimeCollection(final java.util.List<Time> timeList) {
        this.timeList = timeList;
    }

    /**
     * Sets the value of field 'type'. The field 'type' has the following
     * description: outage type
     * 
     * @param type the value of field 'type'.
     */
    public void setType(final String type) {
        this.type = type;
    }

}
