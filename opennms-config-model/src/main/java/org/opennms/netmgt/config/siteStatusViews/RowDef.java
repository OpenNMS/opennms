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

package org.opennms.netmgt.config.siteStatusViews;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class RowDef.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "row-def")
@XmlAccessorType(XmlAccessType.FIELD)
public class RowDef implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_REPORT_CATEGORY = "Network Interfaces";

    @XmlAttribute(name = "label", required = true)
    private String label;

    @XmlAttribute(name = "report-category")
    private String reportCategory;

    /**
     * This element is used to specify OpenNMS specific categories. Note:
     * currently, these categories are defined in a separate configuration file
     * and are
     *  related directly to monitored services. I have separated out this element
     * so that it can be refereneced by other entities (nodes, interfaces, etc.)
     *  however, they will be ignored until the domain model is changed and the
     * service layer is adapted for this behavior.
     *  
     */
    @XmlElement(name = "category", required = true)
    private List<Category> categoryList = new ArrayList<>();

    /**
     * 
     * 
     * @param vCategory
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addCategory(final Category vCategory) throws IndexOutOfBoundsException {
        this.categoryList.add(vCategory);
    }

    /**
     * 
     * 
     * @param index
     * @param vCategory
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addCategory(final int index, final Category vCategory) throws IndexOutOfBoundsException {
        this.categoryList.add(index, vCategory);
    }

    /**
     * Method enumerateCategory.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Category> enumerateCategory() {
        return Collections.enumeration(this.categoryList);
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
        
        if (obj instanceof RowDef) {
            RowDef temp = (RowDef)obj;
            boolean equals = Objects.equals(temp.label, label)
                && Objects.equals(temp.reportCategory, reportCategory)
                && Objects.equals(temp.categoryList, categoryList);
            return equals;
        }
        return false;
    }

    /**
     * Method getCategory.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Category
     * at the given index
     */
    public Category getCategory(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.categoryList.size()) {
            throw new IndexOutOfBoundsException("getCategory: Index value '" + index + "' not in range [0.." + (this.categoryList.size() - 1) + "]");
        }
        
        return (Category) categoryList.get(index);
    }

    /**
     * Method getCategory.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Category[] getCategory() {
        Category[] array = new Category[0];
        return (Category[]) this.categoryList.toArray(array);
    }

    /**
     * Method getCategoryCollection.Returns a reference to 'categoryList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Category> getCategoryCollection() {
        return this.categoryList;
    }

    /**
     * Method getCategoryCount.
     * 
     * @return the size of this collection
     */
    public int getCategoryCount() {
        return this.categoryList.size();
    }

    /**
     * Returns the value of field 'label'.
     * 
     * @return the value of field 'Label'.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Returns the value of field 'reportCategory'.
     * 
     * @return the value of field 'ReportCategory'.
     */
    public String getReportCategory() {
        return this.reportCategory != null ? this.reportCategory : DEFAULT_REPORT_CATEGORY;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            label, 
            reportCategory, 
            categoryList);
        return hash;
    }

    /**
     * Method iterateCategory.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Category> iterateCategory() {
        return this.categoryList.iterator();
    }

    /**
     */
    public void removeAllCategory() {
        this.categoryList.clear();
    }

    /**
     * Method removeCategory.
     * 
     * @param vCategory
     * @return true if the object was removed from the collection.
     */
    public boolean removeCategory(final Category vCategory) {
        boolean removed = categoryList.remove(vCategory);
        return removed;
    }

    /**
     * Method removeCategoryAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Category removeCategoryAt(final int index) {
        Object obj = this.categoryList.remove(index);
        return (Category) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vCategory
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setCategory(final int index, final Category vCategory) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.categoryList.size()) {
            throw new IndexOutOfBoundsException("setCategory: Index value '" + index + "' not in range [0.." + (this.categoryList.size() - 1) + "]");
        }
        
        this.categoryList.set(index, vCategory);
    }

    /**
     * 
     * 
     * @param vCategoryArray
     */
    public void setCategory(final Category[] vCategoryArray) {
        //-- copy array
        categoryList.clear();
        
        for (int i = 0; i < vCategoryArray.length; i++) {
                this.categoryList.add(vCategoryArray[i]);
        }
    }

    /**
     * Sets the value of 'categoryList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vCategoryList the Vector to copy.
     */
    public void setCategory(final List<Category> vCategoryList) {
        // copy vector
        this.categoryList.clear();
        
        this.categoryList.addAll(vCategoryList);
    }

    /**
     * Sets the value of 'categoryList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param categoryList the Vector to set.
     */
    public void setCategoryCollection(final List<Category> categoryList) {
        this.categoryList = categoryList;
    }

    /**
     * Sets the value of field 'label'.
     * 
     * @param label the value of field 'label'.
     */
    public void setLabel(final String label) {
        this.label = label;
    }

    /**
     * Sets the value of field 'reportCategory'.
     * 
     * @param reportCategory the value of field 'reportCategory'.
     */
    public void setReportCategory(final String reportCategory) {
        this.reportCategory = reportCategory;
    }

}
