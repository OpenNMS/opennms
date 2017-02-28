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

package org.opennms.netmgt.config.threshd;

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
 * Top-level element for the threshd-configuration.xml
 *  configuration file.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "threshd-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class ThreshdConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Maximum number of threads used for
     *  thresholding.
     */
    @XmlAttribute(name = "threads", required = true)
    private Integer threads;

    /**
     * Package encapsulating addresses eligible for
     *  thresholding.
     */
    @XmlElement(name = "package", required = true)
    private List<Package> _packageList = new ArrayList<>();

    /**
     * Service thresholders
     */
    @XmlElement(name = "thresholder")
    private List<Thresholder> thresholderList = new ArrayList<>();

    public ThreshdConfiguration() { }

    /**
     * 
     * 
     * @param vPackage
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addPackage(final Package vPackage) throws IndexOutOfBoundsException {
        this._packageList.add(vPackage);
    }

    /**
     * 
     * 
     * @param index
     * @param vPackage
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addPackage(final int index, final Package vPackage) throws IndexOutOfBoundsException {
        this._packageList.add(index, vPackage);
    }

    /**
     * 
     * 
     * @param vThresholder
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addThresholder(final Thresholder vThresholder) throws IndexOutOfBoundsException {
        this.thresholderList.add(vThresholder);
    }

    /**
     * 
     * 
     * @param index
     * @param vThresholder
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addThresholder(final int index, final Thresholder vThresholder) throws IndexOutOfBoundsException {
        this.thresholderList.add(index, vThresholder);
    }

    /**
     */
    public void deleteThreads() {
        this.threads= null;
    }

    /**
     * Method enumeratePackage.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Package> enumeratePackage() {
        return Collections.enumeration(this._packageList);
    }

    /**
     * Method enumerateThresholder.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Thresholder> enumerateThresholder() {
        return Collections.enumeration(this.thresholderList);
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
        
        if (obj instanceof ThreshdConfiguration) {
            ThreshdConfiguration temp = (ThreshdConfiguration)obj;
            boolean equals = Objects.equals(temp.threads, threads)
                && Objects.equals(temp._packageList, _packageList)
                && Objects.equals(temp.thresholderList, thresholderList);
            return equals;
        }
        return false;
    }

    /**
     * Method getPackage.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Package at the
     * given index
     */
    public Package getPackage(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._packageList.size()) {
            throw new IndexOutOfBoundsException("getPackage: Index value '" + index + "' not in range [0.." + (this._packageList.size() - 1) + "]");
        }
        
        return (Package) _packageList.get(index);
    }

    /**
     * Method getPackage.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Package[] getPackage() {
        Package[] array = new Package[0];
        return (Package[]) this._packageList.toArray(array);
    }

    /**
     * Method getPackageCollection.Returns a reference to '_packageList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Package> getPackageCollection() {
        return this._packageList;
    }

    /**
     * Method getPackageCount.
     * 
     * @return the size of this collection
     */
    public int getPackageCount() {
        return this._packageList.size();
    }

    /**
     * Returns the value of field 'threads'. The field 'threads' has the following
     * description: Maximum number of threads used for
     *  thresholding.
     * 
     * @return the value of field 'Threads'.
     */
    public Integer getThreads() {
        return this.threads;
    }

    /**
     * Method getThresholder.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Thresholder at
     * the given index
     */
    public Thresholder getThresholder(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.thresholderList.size()) {
            throw new IndexOutOfBoundsException("getThresholder: Index value '" + index + "' not in range [0.." + (this.thresholderList.size() - 1) + "]");
        }
        
        return (Thresholder) thresholderList.get(index);
    }

    /**
     * Method getThresholder.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Thresholder[] getThresholder() {
        Thresholder[] array = new Thresholder[0];
        return (Thresholder[]) this.thresholderList.toArray(array);
    }

    /**
     * Method getThresholderCollection.Returns a reference to 'thresholderList'.
     * No type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Thresholder> getThresholderCollection() {
        return this.thresholderList;
    }

    /**
     * Method getThresholderCount.
     * 
     * @return the size of this collection
     */
    public int getThresholderCount() {
        return this.thresholderList.size();
    }

    /**
     * Method hasThreads.
     * 
     * @return true if at least one Threads has been added
     */
    public boolean hasThreads() {
        return this.threads != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            threads, 
            _packageList, 
            thresholderList);
        return hash;
    }

    /**
     * Method iteratePackage.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Package> iteratePackage() {
        return this._packageList.iterator();
    }

    /**
     * Method iterateThresholder.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Thresholder> iterateThresholder() {
        return this.thresholderList.iterator();
    }

    /**
     */
    public void removeAllPackage() {
        this._packageList.clear();
    }

    /**
     */
    public void removeAllThresholder() {
        this.thresholderList.clear();
    }

    /**
     * Method removePackage.
     * 
     * @param vPackage
     * @return true if the object was removed from the collection.
     */
    public boolean removePackage(final Package vPackage) {
        boolean removed = _packageList.remove(vPackage);
        return removed;
    }

    /**
     * Method removePackageAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Package removePackageAt(final int index) {
        Object obj = this._packageList.remove(index);
        return (Package) obj;
    }

    /**
     * Method removeThresholder.
     * 
     * @param vThresholder
     * @return true if the object was removed from the collection.
     */
    public boolean removeThresholder(final Thresholder vThresholder) {
        boolean removed = thresholderList.remove(vThresholder);
        return removed;
    }

    /**
     * Method removeThresholderAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Thresholder removeThresholderAt(final int index) {
        Object obj = this.thresholderList.remove(index);
        return (Thresholder) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vPackage
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setPackage(final int index, final Package vPackage) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._packageList.size()) {
            throw new IndexOutOfBoundsException("setPackage: Index value '" + index + "' not in range [0.." + (this._packageList.size() - 1) + "]");
        }
        
        this._packageList.set(index, vPackage);
    }

    /**
     * 
     * 
     * @param vPackageArray
     */
    public void setPackage(final Package[] vPackageArray) {
        //-- copy array
        _packageList.clear();
        
        for (int i = 0; i < vPackageArray.length; i++) {
                this._packageList.add(vPackageArray[i]);
        }
    }

    /**
     * Sets the value of '_packageList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vPackageList the Vector to copy.
     */
    public void setPackage(final List<Package> vPackageList) {
        // copy vector
        this._packageList.clear();
        
        this._packageList.addAll(vPackageList);
    }

    /**
     * Sets the value of '_packageList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param _packageList the Vector to set.
     */
    public void setPackageCollection(final List<Package> _packageList) {
        this._packageList = _packageList;
    }

    /**
     * Sets the value of field 'threads'. The field 'threads' has the following
     * description: Maximum number of threads used for
     *  thresholding.
     * 
     * @param threads the value of field 'threads'.
     */
    public void setThreads(final Integer threads) {
        this.threads = threads;
    }

    /**
     * 
     * 
     * @param index
     * @param vThresholder
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setThresholder(final int index, final Thresholder vThresholder) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.thresholderList.size()) {
            throw new IndexOutOfBoundsException("setThresholder: Index value '" + index + "' not in range [0.." + (this.thresholderList.size() - 1) + "]");
        }
        
        this.thresholderList.set(index, vThresholder);
    }

    /**
     * 
     * 
     * @param vThresholderArray
     */
    public void setThresholder(final Thresholder[] vThresholderArray) {
        //-- copy array
        thresholderList.clear();
        
        for (int i = 0; i < vThresholderArray.length; i++) {
                this.thresholderList.add(vThresholderArray[i]);
        }
    }

    /**
     * Sets the value of 'thresholderList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vThresholderList the Vector to copy.
     */
    public void setThresholder(final List<Thresholder> vThresholderList) {
        // copy vector
        this.thresholderList.clear();
        
        this.thresholderList.addAll(vThresholderList);
    }

    /**
     * Sets the value of 'thresholderList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param thresholderList the Vector to set.
     */
    public void setThresholderCollection(final List<Thresholder> thresholderList) {
        this.thresholderList = thresholderList;
    }

}
