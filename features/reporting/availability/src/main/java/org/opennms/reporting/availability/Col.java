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
 * Class Col.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "col")
@XmlAccessorType(XmlAccessType.FIELD)
public class Col implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "colTitle")
    private java.util.List<String> colTitleList;

    public Col() {
        this.colTitleList = new java.util.ArrayList<>();
    }

    /**
     * 
     * 
     * @param vColTitle
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addColTitle(final String vColTitle) throws IndexOutOfBoundsException {
        this.colTitleList.add(vColTitle);
    }

    /**
     * 
     * 
     * @param index
     * @param vColTitle
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addColTitle(final int index, final String vColTitle) throws IndexOutOfBoundsException {
        this.colTitleList.add(index, vColTitle);
    }

    /**
     * Method enumerateColTitle.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<String> enumerateColTitle() {
        return java.util.Collections.enumeration(this.colTitleList);
    }

    /**
     * Method getColTitle.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getColTitle(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.colTitleList.size()) {
            throw new IndexOutOfBoundsException("getColTitle: Index value '" + index + "' not in range [0.." + (this.colTitleList.size() - 1) + "]");
        }
        
        return (String) colTitleList.get(index);
    }

    /**
     * Method getColTitle.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public String[] getColTitle() {
        String[] array = new String[0];
        return (String[]) this.colTitleList.toArray(array);
    }

    /**
     * Method getColTitleCollection.Returns a reference to 'colTitleList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<String> getColTitleCollection() {
        return this.colTitleList;
    }

    /**
     * Method getColTitleCount.
     * 
     * @return the size of this collection
     */
    public int getColTitleCount() {
        return this.colTitleList.size();
    }

    /**
     * Method iterateColTitle.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<String> iterateColTitle() {
        return this.colTitleList.iterator();
    }

    /**
     */
    public void removeAllColTitle() {
        this.colTitleList.clear();
    }

    /**
     * Method removeColTitle.
     * 
     * @param vColTitle
     * @return true if the object was removed from the collection.
     */
    public boolean removeColTitle(final String vColTitle) {
        boolean removed = colTitleList.remove(vColTitle);
        return removed;
    }

    /**
     * Method removeColTitleAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeColTitleAt(final int index) {
        Object obj = this.colTitleList.remove(index);
        return (String) obj;
    }

    /**
     * 
     * @deprecated
     * @param index
     * @param vColTitle
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    @Hidden
    public void setColTitle(final int index, final String vColTitle) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.colTitleList.size()) {
            throw new IndexOutOfBoundsException("setColTitle: Index value '" + index + "' not in range [0.." + (this.colTitleList.size() - 1) + "]");
        }
        
        this.colTitleList.set(index, vColTitle);
    }

    /**
     * 
     * @deprecated
     * @param vColTitleArray
     */
    @Hidden
    public void setColTitle(final String[] vColTitleArray) {
        //-- copy array
        colTitleList.clear();
        
        for (int i = 0; i < vColTitleArray.length; i++) {
                this.colTitleList.add(vColTitleArray[i]);
        }
    }

    /**
     * Sets the value of 'colTitleList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vColTitleList the Vector to copy.
     */
    public void setColTitle(final java.util.List<String> vColTitleList) {
        // copy vector
        this.colTitleList.clear();
        
        this.colTitleList.addAll(vColTitleList);
    }

    /**
     * Sets the value of 'colTitleList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param colTitleList the Vector to set.
     */
    @Hidden
    public void setColTitleCollection(final java.util.List<String> colTitleList) {
        this.colTitleList = colTitleList;
    }

}
