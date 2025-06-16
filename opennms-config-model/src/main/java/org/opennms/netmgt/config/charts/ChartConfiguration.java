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
package org.opennms.netmgt.config.charts;


import java.util.ArrayList;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.Hidden;

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
     * @deprecated
     * @param index
     * @param vBarChart
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    @Hidden
    public void setBarChart(final int index, final BarChart vBarChart) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.barChartList.size()) {
            throw new IndexOutOfBoundsException("setBarChart: Index value '" + index + "' not in range [0.." + (this.barChartList.size() - 1) + "]");
        }
        
        this.barChartList.set(index, vBarChart);
    }

    /**
     * 
     * @deprecated
     * @param vBarChartArray
     */
    @Hidden
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
    @Hidden
    public void setBarChartCollection(final java.util.List<BarChart> barChartList) {
        this.barChartList = barChartList == null? new ArrayList<>() : barChartList;
    }

}
