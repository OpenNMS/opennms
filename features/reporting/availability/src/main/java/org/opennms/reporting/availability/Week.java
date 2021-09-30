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

package org.opennms.reporting.availability;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Week.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "week")
@XmlAccessorType(XmlAccessType.FIELD)
public class Week implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "day", required = true)
    private java.util.List<Day> dayList;

    public Week() {
        this.dayList = new java.util.ArrayList<>();
    }

    /**
     * 
     * 
     * @param vDay
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addDay(final Day vDay) throws IndexOutOfBoundsException {
        // check for the maximum size
        if (this.dayList.size() >= 7) {
            throw new IndexOutOfBoundsException("addDay has a maximum of 7");
        }
        
        this.dayList.add(vDay);
    }

    /**
     * 
     * 
     * @param index
     * @param vDay
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addDay(final int index, final Day vDay) throws IndexOutOfBoundsException {
        // check for the maximum size
        if (this.dayList.size() >= 7) {
            throw new IndexOutOfBoundsException("addDay has a maximum of 7");
        }
        
        this.dayList.add(index, vDay);
    }

    /**
     * Method enumerateDay.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<Day> enumerateDay() {
        return java.util.Collections.enumeration(this.dayList);
    }

    /**
     * Method getDay.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Day at the
     * given index
     */
    public Day getDay(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.dayList.size()) {
            throw new IndexOutOfBoundsException("getDay: Index value '" + index + "' not in range [0.." + (this.dayList.size() - 1) + "]");
        }
        
        return (Day) dayList.get(index);
    }

    /**
     * Method getDay.Returns the contents of the collection in an Array.  <p>Note:
     *  Just in case the collection contents are changing in another thread, we
     * pass a 0-length Array of the correct type into the API call.  This way we
     * <i>know</i> that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Day[] getDay() {
        Day[] array = new Day[0];
        return (Day[]) this.dayList.toArray(array);
    }

    /**
     * Method getDayCollection.Returns a reference to 'dayList'. No type checking
     * is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Day> getDayCollection() {
        return this.dayList;
    }

    /**
     * Method getDayCount.
     * 
     * @return the size of this collection
     */
    public int getDayCount() {
        return this.dayList.size();
    }

    /**
     * Method iterateDay.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<Day> iterateDay() {
        return this.dayList.iterator();
    }

    /**
     */
    public void removeAllDay() {
        this.dayList.clear();
    }

    /**
     * Method removeDay.
     * 
     * @param vDay
     * @return true if the object was removed from the collection.
     */
    public boolean removeDay(final Day vDay) {
        boolean removed = dayList.remove(vDay);
        return removed;
    }

    /**
     * Method removeDayAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Day removeDayAt(final int index) {
        Object obj = this.dayList.remove(index);
        return (Day) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vDay
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setDay(final int index, final Day vDay) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.dayList.size()) {
            throw new IndexOutOfBoundsException("setDay: Index value '" + index + "' not in range [0.." + (this.dayList.size() - 1) + "]");
        }
        
        this.dayList.set(index, vDay);
    }

    /**
     * 
     * 
     * @param vDayArray
     */
    public void setDay(final Day[] vDayArray) {
        //-- copy array
        dayList.clear();
        
        for (int i = 0; i < vDayArray.length; i++) {
                this.dayList.add(vDayArray[i]);
        }
    }

    /**
     * Sets the value of 'dayList' by copying the given Vector. All elements will
     * be checked for type safety.
     * 
     * @param vDayList the Vector to copy.
     */
    public void setDay(final java.util.List<Day> vDayList) {
        // copy vector
        this.dayList.clear();
        
        this.dayList.addAll(vDayList);
    }

    /**
     * Sets the value of 'dayList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param dayList the Vector to set.
     */
    public void setDayCollection(final java.util.List<Day> dayList) {
        this.dayList = dayList;
    }

}
