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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class CalendarTable.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "calendarTable")
@XmlAccessorType(XmlAccessType.FIELD)
public class CalendarTable implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "month")
    private String month;

    @XmlElement(name = "daysOfWeek")
    private DaysOfWeek daysOfWeek;

    @XmlElement(name = "week", required = true)
    private java.util.List<Week> weekList;

    public CalendarTable() {
        this.weekList = new java.util.ArrayList<>();
    }

    /**
     * 
     * 
     * @param vWeek
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addWeek(final Week vWeek) throws IndexOutOfBoundsException {
        // check for the maximum size
        if (this.weekList.size() >= 6) {
            throw new IndexOutOfBoundsException("addWeek has a maximum of 6");
        }
        
        this.weekList.add(vWeek);
    }

    /**
     * 
     * 
     * @param index
     * @param vWeek
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addWeek(final int index, final Week vWeek) throws IndexOutOfBoundsException {
        // check for the maximum size
        if (this.weekList.size() >= 6) {
            throw new IndexOutOfBoundsException("addWeek has a maximum of 6");
        }
        
        this.weekList.add(index, vWeek);
    }

    /**
     * Method enumerateWeek.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<Week> enumerateWeek() {
        return java.util.Collections.enumeration(this.weekList);
    }

    /**
     * Returns the value of field 'daysOfWeek'.
     * 
     * @return the value of field 'DaysOfWeek'.
     */
    public DaysOfWeek getDaysOfWeek() {
        return this.daysOfWeek;
    }

    /**
     * Returns the value of field 'month'.
     * 
     * @return the value of field 'Month'.
     */
    public String getMonth() {
        return this.month;
    }

    /**
     * Method getWeek.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Week at the
     * given index
     */
    public Week getWeek(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.weekList.size()) {
            throw new IndexOutOfBoundsException("getWeek: Index value '" + index + "' not in range [0.." + (this.weekList.size() - 1) + "]");
        }
        
        return (Week) weekList.get(index);
    }

    /**
     * Method getWeek.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Week[] getWeek() {
        Week[] array = new Week[0];
        return (Week[]) this.weekList.toArray(array);
    }

    /**
     * Method getWeekCollection.Returns a reference to 'weekList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Week> getWeekCollection() {
        return this.weekList;
    }

    /**
     * Method getWeekCount.
     * 
     * @return the size of this collection
     */
    public int getWeekCount() {
        return this.weekList.size();
    }

    /**
     * Method iterateWeek.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<Week> iterateWeek() {
        return this.weekList.iterator();
    }

    /**
     */
    public void removeAllWeek() {
        this.weekList.clear();
    }

    /**
     * Method removeWeek.
     * 
     * @param vWeek
     * @return true if the object was removed from the collection.
     */
    public boolean removeWeek(final Week vWeek) {
        boolean removed = weekList.remove(vWeek);
        return removed;
    }

    /**
     * Method removeWeekAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Week removeWeekAt(final int index) {
        Object obj = this.weekList.remove(index);
        return (Week) obj;
    }

    /**
     * Sets the value of field 'daysOfWeek'.
     * 
     * @param daysOfWeek the value of field 'daysOfWeek'.
     */
    public void setDaysOfWeek(final DaysOfWeek daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    /**
     * Sets the value of field 'month'.
     * 
     * @param month the value of field 'month'.
     */
    public void setMonth(final String month) {
        this.month = month;
    }

    /**
     * 
     * 
     * @param index
     * @param vWeek
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setWeek(final int index, final Week vWeek) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.weekList.size()) {
            throw new IndexOutOfBoundsException("setWeek: Index value '" + index + "' not in range [0.." + (this.weekList.size() - 1) + "]");
        }
        
        this.weekList.set(index, vWeek);
    }

    /**
     * 
     * 
     * @param vWeekArray
     */
    public void setWeek(final Week[] vWeekArray) {
        //-- copy array
        weekList.clear();
        
        for (int i = 0; i < vWeekArray.length; i++) {
                this.weekList.add(vWeekArray[i]);
        }
    }

    /**
     * Sets the value of 'weekList' by copying the given Vector. All elements will
     * be checked for type safety.
     * 
     * @param vWeekList the Vector to copy.
     */
    public void setWeek(final java.util.List<Week> vWeekList) {
        // copy vector
        this.weekList.clear();
        
        this.weekList.addAll(vWeekList);
    }

    /**
     * Sets the value of 'weekList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param weekList the Vector to set.
     */
    public void setWeekCollection(final java.util.List<Week> weekList) {
        this.weekList = weekList;
    }

}
