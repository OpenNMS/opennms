/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.vmware.vijava;

import org.apache.commons.lang.builder.EqualsBuilder;

import javax.xml.bind.annotation.*;

/**
 * Class VmwareDatacollectionConfig.
 */
@XmlRootElement(name = "vmware-datacollection-config")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class VmwareDatacollectionConfig implements java.io.Serializable {

    /**
     * Field _rrdRepository.
     */
    @XmlAttribute(name = "rrdRepository")
    private java.lang.String _rrdRepository;

    /**
     * A grouping of VMware related RRD parms and performance
     * counter groups
     */
    @XmlElement(name = "vmware-collection")
    private java.util.List<org.opennms.netmgt.config.vmware.vijava.VmwareCollection> _vmwareCollectionList;

    public VmwareDatacollectionConfig() {
        super();
        this._vmwareCollectionList = new java.util.ArrayList<org.opennms.netmgt.config.vmware.vijava.VmwareCollection>();
    }

    /**
     * @param vVmwareCollection
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void addVmwareCollection(
            final org.opennms.netmgt.config.vmware.vijava.VmwareCollection vVmwareCollection)
            throws java.lang.IndexOutOfBoundsException {
        this._vmwareCollectionList.add(vVmwareCollection);
    }

    /**
     * @param index
     * @param vVmwareCollection
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void addVmwareCollection(
            final int index,
            final org.opennms.netmgt.config.vmware.vijava.VmwareCollection vVmwareCollection)
            throws java.lang.IndexOutOfBoundsException {
        this._vmwareCollectionList.add(index, vVmwareCollection);
    }

    /**
     * Method enumerateVmwareCollection.
     *
     * @return an Enumeration over all possible elements of this
     *         collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.vmware.vijava.VmwareCollection> enumerateVmwareCollection(
    ) {
        return java.util.Collections.enumeration(this._vmwareCollectionList);
    }

    /**
     * Overrides the java.lang.Object.equals method.
     *
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(
            final java.lang.Object obj) {
        if (obj instanceof VmwareDatacollectionConfig) {
            VmwareDatacollectionConfig other = (VmwareDatacollectionConfig) obj;
            return new EqualsBuilder()
                    .append(getRrdRepository(), other.getRrdRepository())
                    .append(getVmwareCollection(), other.getVmwareCollection())
                    .isEquals();
        }
        return false;
    }

    /**
     * Returns the value of field 'rrdRepository'.
     *
     * @return the value of field 'RrdRepository'.
     */
    public java.lang.String getRrdRepository(
    ) {
        return this._rrdRepository == null ? "" : this._rrdRepository;
    }

    /**
     * Method getVmwareCollection.
     *
     * @param index
     * @return the value of the
     *         org.opennms.netmgt.config.vmware.vijava.VmwareCollection at
     *         the given index
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public org.opennms.netmgt.config.vmware.vijava.VmwareCollection getVmwareCollection(
            final int index)
            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vmwareCollectionList.size()) {
            throw new IndexOutOfBoundsException("getVmwareCollection: Index value '" + index + "' not in range [0.." + (this._vmwareCollectionList.size() - 1) + "]");
        }

        return (org.opennms.netmgt.config.vmware.vijava.VmwareCollection) _vmwareCollectionList.get(index);
    }

    /**
     * Method getVmwareCollection.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     *
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.vmware.vijava.VmwareCollection[] getVmwareCollection(
    ) {
        org.opennms.netmgt.config.vmware.vijava.VmwareCollection[] array = new org.opennms.netmgt.config.vmware.vijava.VmwareCollection[0];
        return (org.opennms.netmgt.config.vmware.vijava.VmwareCollection[]) this._vmwareCollectionList.toArray(array);
    }

    /**
     * Method getVmwareCollectionCollection.Returns a reference to
     * '_vmwareCollectionList'. No type checking is performed on
     * any modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.vmware.vijava.VmwareCollection> getVmwareCollectionCollection(
    ) {
        return this._vmwareCollectionList;
    }

    /**
     * Method getVmwareCollectionCount.
     *
     * @return the size of this collection
     */
    public int getVmwareCollectionCount(
    ) {
        return this._vmwareCollectionList.size();
    }

    /**
     * Method iterateVmwareCollection.
     *
     * @return an Iterator over all possible elements in this
     *         collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.vmware.vijava.VmwareCollection> iterateVmwareCollection(
    ) {
        return this._vmwareCollectionList.iterator();
    }

    /**
     */
    public void removeAllVmwareCollection(
    ) {
        this._vmwareCollectionList.clear();
    }

    /**
     * Method removeVmwareCollection.
     *
     * @param vVmwareCollection
     * @return true if the object was removed from the collection.
     */
    public boolean removeVmwareCollection(
            final org.opennms.netmgt.config.vmware.vijava.VmwareCollection vVmwareCollection) {
        boolean removed = _vmwareCollectionList.remove(vVmwareCollection);
        return removed;
    }

    /**
     * Method removeVmwareCollectionAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.vmware.vijava.VmwareCollection removeVmwareCollectionAt(
            final int index) {
        java.lang.Object obj = this._vmwareCollectionList.remove(index);
        return (org.opennms.netmgt.config.vmware.vijava.VmwareCollection) obj;
    }

    /**
     * Sets the value of field 'rrdRepository'.
     *
     * @param rrdRepository the value of field 'rrdRepository'.
     */
    public void setRrdRepository(
            final java.lang.String rrdRepository) {
        this._rrdRepository = rrdRepository;
    }

    /**
     * @param index
     * @param vVmwareCollection
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void setVmwareCollection(
            final int index,
            final org.opennms.netmgt.config.vmware.vijava.VmwareCollection vVmwareCollection)
            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vmwareCollectionList.size()) {
            throw new IndexOutOfBoundsException("setVmwareCollection: Index value '" + index + "' not in range [0.." + (this._vmwareCollectionList.size() - 1) + "]");
        }

        this._vmwareCollectionList.set(index, vVmwareCollection);
    }

    /**
     * @param vVmwareCollectionArray
     */
    public void setVmwareCollection(
            final org.opennms.netmgt.config.vmware.vijava.VmwareCollection[] vVmwareCollectionArray) {
        //-- copy array
        _vmwareCollectionList.clear();

        for (int i = 0; i < vVmwareCollectionArray.length; i++) {
            this._vmwareCollectionList.add(vVmwareCollectionArray[i]);
        }
    }

    /**
     * Sets the value of '_vmwareCollectionList' by copying the
     * given Vector. All elements will be checked for type safety.
     *
     * @param vVmwareCollectionList the Vector to copy.
     */
    public void setVmwareCollection(
            final java.util.List<org.opennms.netmgt.config.vmware.vijava.VmwareCollection> vVmwareCollectionList) {
        // copy vector
        this._vmwareCollectionList.clear();

        this._vmwareCollectionList.addAll(vVmwareCollectionList);
    }
}
