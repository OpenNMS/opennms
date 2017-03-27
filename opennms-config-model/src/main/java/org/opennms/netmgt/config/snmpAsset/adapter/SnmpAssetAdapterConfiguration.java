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

package org.opennms.netmgt.config.snmpAsset.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class SnmpAssetAdapterConfiguration.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "snmp-asset-adapter-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class SnmpAssetAdapterConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "package", required = true)
    private List<Package> _packageList = new ArrayList<>();

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
     * Method enumeratePackage.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Package> enumeratePackage() {
        return Collections.enumeration(this._packageList);
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
        
        if (obj instanceof SnmpAssetAdapterConfiguration) {
            SnmpAssetAdapterConfiguration temp = (SnmpAssetAdapterConfiguration)obj;
            boolean equals = Objects.equals(temp._packageList, _packageList);
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
     * @return the value of the
     * Package at the given index
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
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            _packageList);
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
     */
    public void removeAllPackage() {
        this._packageList.clear();
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

}
