/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.reporting.availability;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.Hidden;

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
     * @deprecated
     * @param index
     * @param vDay
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    @Hidden
    public void setDay(final int index, final Day vDay) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.dayList.size()) {
            throw new IndexOutOfBoundsException("setDay: Index value '" + index + "' not in range [0.." + (this.dayList.size() - 1) + "]");
        }
        
        this.dayList.set(index, vDay);
    }

    /**
     * 
     * @deprecated
     * @param vDayArray
     */
    @Hidden
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
    @Hidden
    public void setDayCollection(final java.util.List<Day> dayList) {
        this.dayList = dayList;
    }

}
