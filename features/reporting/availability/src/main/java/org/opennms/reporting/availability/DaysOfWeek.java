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

/**
 * Class DaysOfWeek.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "daysOfWeek")
@XmlAccessorType(XmlAccessType.FIELD)
public class DaysOfWeek implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "dayName", required = true)
    private java.util.List<String> dayNameList;

    public DaysOfWeek() {
        this.dayNameList = new java.util.ArrayList<>();
    }

    /**
     * 
     * 
     * @param vDayName
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addDayName(final String vDayName) throws IndexOutOfBoundsException {
        // check for the maximum size
        if (this.dayNameList.size() >= 7) {
            throw new IndexOutOfBoundsException("addDayName has a maximum of 7");
        }
        
        this.dayNameList.add(vDayName);
    }

    /**
     * 
     * 
     * @param index
     * @param vDayName
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addDayName(final int index, final String vDayName) throws IndexOutOfBoundsException {
        // check for the maximum size
        if (this.dayNameList.size() >= 7) {
            throw new IndexOutOfBoundsException("addDayName has a maximum of 7");
        }
        
        this.dayNameList.add(index, vDayName);
    }

    /**
     * Method enumerateDayName.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<String> enumerateDayName() {
        return java.util.Collections.enumeration(this.dayNameList);
    }

    /**
     * Method getDayName.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getDayName(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.dayNameList.size()) {
            throw new IndexOutOfBoundsException("getDayName: Index value '" + index + "' not in range [0.." + (this.dayNameList.size() - 1) + "]");
        }
        
        return (String) dayNameList.get(index);
    }

    /**
     * Method getDayName.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public String[] getDayName() {
        String[] array = new String[0];
        return (String[]) this.dayNameList.toArray(array);
    }

    /**
     * Method getDayNameCollection.Returns a reference to 'dayNameList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<String> getDayNameCollection() {
        return this.dayNameList;
    }

    /**
     * Method getDayNameCount.
     * 
     * @return the size of this collection
     */
    public int getDayNameCount() {
        return this.dayNameList.size();
    }

    /**
     * Method iterateDayName.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<String> iterateDayName() {
        return this.dayNameList.iterator();
    }

    /**
     */
    public void removeAllDayName() {
        this.dayNameList.clear();
    }

    /**
     * Method removeDayName.
     * 
     * @param vDayName
     * @return true if the object was removed from the collection.
     */
    public boolean removeDayName(final String vDayName) {
        boolean removed = dayNameList.remove(vDayName);
        return removed;
    }

    /**
     * Method removeDayNameAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeDayNameAt(final int index) {
        Object obj = this.dayNameList.remove(index);
        return (String) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vDayName
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setDayName(final int index, final String vDayName) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.dayNameList.size()) {
            throw new IndexOutOfBoundsException("setDayName: Index value '" + index + "' not in range [0.." + (this.dayNameList.size() - 1) + "]");
        }
        
        this.dayNameList.set(index, vDayName);
    }

    /**
     * 
     * 
     * @param vDayNameArray
     */
    public void setDayName(final String[] vDayNameArray) {
        //-- copy array
        dayNameList.clear();
        
        for (int i = 0; i < vDayNameArray.length; i++) {
                this.dayNameList.add(vDayNameArray[i]);
        }
    }

    /**
     * Sets the value of 'dayNameList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vDayNameList the Vector to copy.
     */
    public void setDayName(final java.util.List<String> vDayNameList) {
        // copy vector
        this.dayNameList.clear();
        
        this.dayNameList.addAll(vDayNameList);
    }

    /**
     * Sets the value of 'dayNameList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param dayNameList the Vector to set.
     */
    public void setDayNameCollection(final java.util.List<String> dayNameList) {
        this.dayNameList = dayNameList;
    }

}
