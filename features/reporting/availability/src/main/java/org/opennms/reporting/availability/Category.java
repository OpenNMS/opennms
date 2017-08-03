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
 * Class Category.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "category")
@XmlAccessorType(XmlAccessType.FIELD)
public class Category implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "catName")
    private String catName;

    @XmlElement(name = "catSections")
    private java.util.List<CatSections> catSectionsList;

    @XmlElement(name = "catComments")
    private String catComments;

    @XmlElement(name = "warning")
    private Double warning;

    @XmlElement(name = "normal")
    private Double normal;

    @XmlElement(name = "catIndex")
    private Integer catIndex;

    @XmlElement(name = "nodeCount")
    private Integer nodeCount;

    @XmlElement(name = "ipaddrCount")
    private Integer ipaddrCount;

    @XmlElement(name = "serviceCount")
    private Integer serviceCount;

    public Category() {
        this.catSectionsList = new java.util.ArrayList<>();
    }

    /**
     * 
     * 
     * @param vCatSections
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addCatSections(final CatSections vCatSections) throws IndexOutOfBoundsException {
        this.catSectionsList.add(vCatSections);
    }

    /**
     * 
     * 
     * @param index
     * @param vCatSections
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addCatSections(final int index, final CatSections vCatSections) throws IndexOutOfBoundsException {
        this.catSectionsList.add(index, vCatSections);
    }

    /**
     */
    public void deleteCatIndex() {
        this.catIndex= null;
    }

    /**
     */
    public void deleteIpaddrCount() {
        this.ipaddrCount= null;
    }

    /**
     */
    public void deleteNodeCount() {
        this.nodeCount= null;
    }

    /**
     */
    public void deleteNormal() {
        this.normal= null;
    }

    /**
     */
    public void deleteServiceCount() {
        this.serviceCount= null;
    }

    /**
     */
    public void deleteWarning() {
        this.warning= null;
    }

    /**
     * Method enumerateCatSections.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<CatSections> enumerateCatSections() {
        return java.util.Collections.enumeration(this.catSectionsList);
    }

    /**
     * Returns the value of field 'catComments'.
     * 
     * @return the value of field 'CatComments'.
     */
    public String getCatComments() {
        return this.catComments;
    }

    /**
     * Returns the value of field 'catIndex'.
     * 
     * @return the value of field 'CatIndex'.
     */
    public Integer getCatIndex() {
        return this.catIndex;
    }

    /**
     * Returns the value of field 'catName'.
     * 
     * @return the value of field 'CatName'.
     */
    public String getCatName() {
        return this.catName;
    }

    /**
     * Method getCatSections.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the CatSections at
     * the given index
     */
    public CatSections getCatSections(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.catSectionsList.size()) {
            throw new IndexOutOfBoundsException("getCatSections: Index value '" + index + "' not in range [0.." + (this.catSectionsList.size() - 1) + "]");
        }
        
        return (CatSections) catSectionsList.get(index);
    }

    /**
     * Method getCatSections.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public CatSections[] getCatSections() {
        CatSections[] array = new CatSections[0];
        return (CatSections[]) this.catSectionsList.toArray(array);
    }

    /**
     * Method getCatSectionsCollection.Returns a reference to 'catSectionsList'.
     * No type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<CatSections> getCatSectionsCollection() {
        return this.catSectionsList;
    }

    /**
     * Method getCatSectionsCount.
     * 
     * @return the size of this collection
     */
    public int getCatSectionsCount() {
        return this.catSectionsList.size();
    }

    /**
     * Returns the value of field 'ipaddrCount'.
     * 
     * @return the value of field 'IpaddrCount'.
     */
    public Integer getIpaddrCount() {
        return this.ipaddrCount;
    }

    /**
     * Returns the value of field 'nodeCount'.
     * 
     * @return the value of field 'NodeCount'.
     */
    public Integer getNodeCount() {
        return this.nodeCount;
    }

    /**
     * Returns the value of field 'normal'.
     * 
     * @return the value of field 'Normal'.
     */
    public Double getNormal() {
        return this.normal;
    }

    /**
     * Returns the value of field 'serviceCount'.
     * 
     * @return the value of field 'ServiceCount'.
     */
    public Integer getServiceCount() {
        return this.serviceCount;
    }

    /**
     * Returns the value of field 'warning'.
     * 
     * @return the value of field 'Warning'.
     */
    public Double getWarning() {
        return this.warning;
    }

    /**
     * Method hasCatIndex.
     * 
     * @return true if at least one CatIndex has been added
     */
    public boolean hasCatIndex() {
        return this.catIndex != null;
    }

    /**
     * Method hasIpaddrCount.
     * 
     * @return true if at least one IpaddrCount has been added
     */
    public boolean hasIpaddrCount() {
        return this.ipaddrCount != null;
    }

    /**
     * Method hasNodeCount.
     * 
     * @return true if at least one NodeCount has been added
     */
    public boolean hasNodeCount() {
        return this.nodeCount != null;
    }

    /**
     * Method hasNormal.
     * 
     * @return true if at least one Normal has been added
     */
    public boolean hasNormal() {
        return this.normal != null;
    }

    /**
     * Method hasServiceCount.
     * 
     * @return true if at least one ServiceCount has been added
     */
    public boolean hasServiceCount() {
        return this.serviceCount != null;
    }

    /**
     * Method hasWarning.
     * 
     * @return true if at least one Warning has been added
     */
    public boolean hasWarning() {
        return this.warning != null;
    }

    /**
     * Method iterateCatSections.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<CatSections> iterateCatSections() {
        return this.catSectionsList.iterator();
    }

    /**
     */
    public void removeAllCatSections() {
        this.catSectionsList.clear();
    }

    /**
     * Method removeCatSections.
     * 
     * @param vCatSections
     * @return true if the object was removed from the collection.
     */
    public boolean removeCatSections(final CatSections vCatSections) {
        boolean removed = catSectionsList.remove(vCatSections);
        return removed;
    }

    /**
     * Method removeCatSectionsAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public CatSections removeCatSectionsAt(final int index) {
        Object obj = this.catSectionsList.remove(index);
        return (CatSections) obj;
    }

    /**
     * Sets the value of field 'catComments'.
     * 
     * @param catComments the value of field 'catComments'.
     */
    public void setCatComments(final String catComments) {
        this.catComments = catComments;
    }

    /**
     * Sets the value of field 'catIndex'.
     * 
     * @param catIndex the value of field 'catIndex'.
     */
    public void setCatIndex(final Integer catIndex) {
        this.catIndex = catIndex;
    }

    /**
     * Sets the value of field 'catName'.
     * 
     * @param catName the value of field 'catName'.
     */
    public void setCatName(final String catName) {
        this.catName = catName;
    }

    /**
     * 
     * 
     * @param index
     * @param vCatSections
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setCatSections(final int index, final CatSections vCatSections) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.catSectionsList.size()) {
            throw new IndexOutOfBoundsException("setCatSections: Index value '" + index + "' not in range [0.." + (this.catSectionsList.size() - 1) + "]");
        }
        
        this.catSectionsList.set(index, vCatSections);
    }

    /**
     * 
     * 
     * @param vCatSectionsArray
     */
    public void setCatSections(final CatSections[] vCatSectionsArray) {
        //-- copy array
        catSectionsList.clear();
        
        for (int i = 0; i < vCatSectionsArray.length; i++) {
                this.catSectionsList.add(vCatSectionsArray[i]);
        }
    }

    /**
     * Sets the value of 'catSectionsList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vCatSectionsList the Vector to copy.
     */
    public void setCatSections(final java.util.List<CatSections> vCatSectionsList) {
        // copy vector
        this.catSectionsList.clear();
        
        this.catSectionsList.addAll(vCatSectionsList);
    }

    /**
     * Sets the value of 'catSectionsList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param catSectionsList the Vector to set.
     */
    public void setCatSectionsCollection(final java.util.List<CatSections> catSectionsList) {
        this.catSectionsList = catSectionsList;
    }

    /**
     * Sets the value of field 'ipaddrCount'.
     * 
     * @param ipaddrCount the value of field 'ipaddrCount'.
     */
    public void setIpaddrCount(final Integer ipaddrCount) {
        this.ipaddrCount = ipaddrCount;
    }

    /**
     * Sets the value of field 'nodeCount'.
     * 
     * @param nodeCount the value of field 'nodeCount'.
     */
    public void setNodeCount(final Integer nodeCount) {
        this.nodeCount = nodeCount;
    }

    /**
     * Sets the value of field 'normal'.
     * 
     * @param normal the value of field 'normal'.
     */
    public void setNormal(final Double normal) {
        this.normal = normal;
    }

    /**
     * Sets the value of field 'serviceCount'.
     * 
     * @param serviceCount the value of field 'serviceCount'.
     */
    public void setServiceCount(final Integer serviceCount) {
        this.serviceCount = serviceCount;
    }

    /**
     * Sets the value of field 'warning'.
     * 
     * @param warning the value of field 'warning'.
     */
    public void setWarning(final Double warning) {
        this.warning = warning;
    }

}
