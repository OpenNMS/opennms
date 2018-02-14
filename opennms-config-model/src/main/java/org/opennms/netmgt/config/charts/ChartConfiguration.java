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

package org.opennms.netmgt.config.charts;


import java.util.ArrayList;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "chart-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChartConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "bar-chart")
    private java.util.List<BarChart> barChartList = new ArrayList<>();

    public ChartConfiguration() {
    }

    /**
     * 
     * 
     * @param vBarChart
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addBarChart(final BarChart vBarChart) throws IndexOutOfBoundsException {
        this.barChartList.add(vBarChart);
    }

    /**
     * 
     * 
     * @param index
     * @param vBarChart
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addBarChart(final int index, final BarChart vBarChart) throws IndexOutOfBoundsException {
        this.barChartList.add(index, vBarChart);
    }

    /**
     * Method enumerateBarChart.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<BarChart> enumerateBarChart() {
        return java.util.Collections.enumeration(this.barChartList);
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
        
        if (obj instanceof ChartConfiguration) {
            ChartConfiguration temp = (ChartConfiguration)obj;
            boolean equals = Objects.equals(temp.barChartList, barChartList);
            return equals;
        }
        return false;
    }

    /**
     * Method getBarChart.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the BarChart at the
     * given index
     */
    public BarChart getBarChart(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.barChartList.size()) {
            throw new IndexOutOfBoundsException("getBarChart: Index value '" + index + "' not in range [0.." + (this.barChartList.size() - 1) + "]");
        }
        
        return (BarChart) barChartList.get(index);
    }

    /**
     * Method getBarChart.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public BarChart[] getBarChart() {
        BarChart[] array = new BarChart[0];
        return (BarChart[]) this.barChartList.toArray(array);
    }

    /**
     * Method getBarChartCollection.Returns a reference to 'barChartList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<BarChart> getBarChartCollection() {
        return this.barChartList;
    }

    /**
     * Method getBarChartCount.
     * 
     * @return the size of this collection
     */
    public int getBarChartCount() {
        return this.barChartList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            barChartList);
        return hash;
    }

    /**
     * Method iterateBarChart.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<BarChart> iterateBarChart() {
        return this.barChartList.iterator();
    }

    /**
     */
    public void removeAllBarChart() {
        this.barChartList.clear();
    }

    /**
     * Method removeBarChart.
     * 
     * @param vBarChart
     * @return true if the object was removed from the collection.
     */
    public boolean removeBarChart(final BarChart vBarChart) {
        boolean removed = barChartList.remove(vBarChart);
        return removed;
    }

    /**
     * Method removeBarChartAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public BarChart removeBarChartAt(final int index) {
        Object obj = this.barChartList.remove(index);
        return (BarChart) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vBarChart
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setBarChart(final int index, final BarChart vBarChart) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.barChartList.size()) {
            throw new IndexOutOfBoundsException("setBarChart: Index value '" + index + "' not in range [0.." + (this.barChartList.size() - 1) + "]");
        }
        
        this.barChartList.set(index, vBarChart);
    }

    /**
     * 
     * 
     * @param vBarChartArray
     */
    public void setBarChart(final BarChart[] vBarChartArray) {
        //-- copy array
        barChartList.clear();
        
        for (int i = 0; i < vBarChartArray.length; i++) {
                this.barChartList.add(vBarChartArray[i]);
        }
    }

    /**
     * Sets the value of 'barChartList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vBarChartList the Vector to copy.
     */
    public void setBarChart(final java.util.List<BarChart> vBarChartList) {
        // copy vector
        this.barChartList.clear();
        
        this.barChartList.addAll(vBarChartList);
    }

    /**
     * Sets the value of 'barChartList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param barChartList the Vector to set.
     */
    public void setBarChartCollection(final java.util.List<BarChart> barChartList) {
        this.barChartList = barChartList == null? new ArrayList<>() : barChartList;
    }

}
