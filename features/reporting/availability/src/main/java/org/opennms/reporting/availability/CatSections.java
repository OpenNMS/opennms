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
 * Class CatSections.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "catSections")
@XmlAccessorType(XmlAccessType.FIELD)
public class CatSections implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "section")
    private java.util.List<Section> sectionList;

    public CatSections() {
        this.sectionList = new java.util.ArrayList<>();
    }

    /**
     * 
     * 
     * @param vSection
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addSection(final Section vSection) throws IndexOutOfBoundsException {
        this.sectionList.add(vSection);
    }

    /**
     * 
     * 
     * @param index
     * @param vSection
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addSection(final int index, final Section vSection) throws IndexOutOfBoundsException {
        this.sectionList.add(index, vSection);
    }

    /**
     * Method enumerateSection.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<Section> enumerateSection() {
        return java.util.Collections.enumeration(this.sectionList);
    }

    /**
     * Method getSection.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Section at the
     * given index
     */
    public Section getSection(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.sectionList.size()) {
            throw new IndexOutOfBoundsException("getSection: Index value '" + index + "' not in range [0.." + (this.sectionList.size() - 1) + "]");
        }
        
        return (Section) sectionList.get(index);
    }

    /**
     * Method getSection.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Section[] getSection() {
        Section[] array = new Section[0];
        return (Section[]) this.sectionList.toArray(array);
    }

    /**
     * Method getSectionCollection.Returns a reference to 'sectionList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Section> getSectionCollection() {
        return this.sectionList;
    }

    /**
     * Method getSectionCount.
     * 
     * @return the size of this collection
     */
    public int getSectionCount() {
        return this.sectionList.size();
    }

    /**
     * Method iterateSection.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<Section> iterateSection() {
        return this.sectionList.iterator();
    }

    /**
     */
    public void removeAllSection() {
        this.sectionList.clear();
    }

    /**
     * Method removeSection.
     * 
     * @param vSection
     * @return true if the object was removed from the collection.
     */
    public boolean removeSection(final Section vSection) {
        boolean removed = sectionList.remove(vSection);
        return removed;
    }

    /**
     * Method removeSectionAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Section removeSectionAt(final int index) {
        Object obj = this.sectionList.remove(index);
        return (Section) obj;
    }

    /**
     * 
     * @deprecated
     * @param index
     * @param vSection
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    @Hidden
    public void setSection(final int index, final Section vSection) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.sectionList.size()) {
            throw new IndexOutOfBoundsException("setSection: Index value '" + index + "' not in range [0.." + (this.sectionList.size() - 1) + "]");
        }
        
        this.sectionList.set(index, vSection);
    }

    /**
     * 
     * @deprecated
     * @param vSectionArray
     */
    @Hidden
    public void setSection(final Section[] vSectionArray) {
        //-- copy array
        sectionList.clear();
        
        for (int i = 0; i < vSectionArray.length; i++) {
                this.sectionList.add(vSectionArray[i]);
        }
    }

    /**
     * Sets the value of 'sectionList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vSectionList the Vector to copy.
     */
    public void setSection(final java.util.List<Section> vSectionList) {
        // copy vector
        this.sectionList.clear();
        
        this.sectionList.addAll(vSectionList);
    }

    /**
     * Sets the value of 'sectionList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param sectionList the Vector to set.
     */
    @Hidden
    public void setSectionCollection(final java.util.List<Section> sectionList) {
        this.sectionList = sectionList;
    }

}
