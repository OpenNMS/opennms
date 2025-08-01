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
 * Class Categories.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "categories")
@XmlAccessorType(XmlAccessType.FIELD)
public class Categories implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "category")
    private java.util.List<Category> categoryList;

    public Categories() {
        this.categoryList = new java.util.ArrayList<>();
    }

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
    public java.util.Enumeration<Category> enumerateCategory() {
        return java.util.Collections.enumeration(this.categoryList);
    }

    /**
     * Method getCategory.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Category at the
     * given index
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
    public java.util.List<Category> getCategoryCollection() {
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
     * Method iterateCategory.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<Category> iterateCategory() {
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
     * @deprecated
     * @param index
     * @param vCategory
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    @Hidden
    public void setCategory(final int index, final Category vCategory) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.categoryList.size()) {
            throw new IndexOutOfBoundsException("setCategory: Index value '" + index + "' not in range [0.." + (this.categoryList.size() - 1) + "]");
        }
        
        this.categoryList.set(index, vCategory);
    }

    /**
     * 
     * @deprecated
     * @param vCategoryArray
     */
    @Hidden
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
    public void setCategory(final java.util.List<Category> vCategoryList) {
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
    @Hidden
    public void setCategoryCollection(final java.util.List<Category> categoryList) {
        this.categoryList = categoryList;
    }

}
