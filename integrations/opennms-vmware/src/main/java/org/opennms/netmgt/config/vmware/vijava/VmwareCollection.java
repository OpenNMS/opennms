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
 * A grouping of VMware related RRD parms and performance counter
 * groups
 */
@XmlRootElement(name = "vmware-cim-collection")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class VmwareCollection implements java.io.Serializable {

    /**
     * Field _name.
     */
    @XmlAttribute(name = "name")
    private java.lang.String _name;

    /**
     * RRD parms
     */
    @XmlElement(name = "rrd")
    private org.opennms.netmgt.config.vmware.vijava.Rrd _rrd;

    /**
     * VMware object groups
     */
    @XmlElementWrapper(name = "vmware-groups")
    @XmlElement(name = "vmware-group")
    private java.util.List<org.opennms.netmgt.config.vmware.vijava.VmwareGroup> _vmwareGroupList;

    public VmwareCollection() {
        super();
        this._vmwareGroupList = new java.util.ArrayList<org.opennms.netmgt.config.vmware.vijava.VmwareGroup>();
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
        if (obj instanceof VmwareCollection) {
            VmwareCollection other = (VmwareCollection) obj;
            return new EqualsBuilder()
                    .append(getName(), other.getName())
                    .append(getRrd(), other.getRrd())
                    .append(getVmwareGroup(), other.getVmwareGroup())
                    .isEquals();
        }
        return false;
    }


    /**
     * Returns the value of field 'name'.
     *
     * @return the value of field 'Name'.
     */
    public java.lang.String getName(
    ) {
        return this._name == null ? "" : this._name;
    }

    /**
     * Returns the value of field 'rrd'. The field 'rrd' has the
     * following description: RRD parms
     *
     * @return the value of field 'Rrd'.
     */
    public org.opennms.netmgt.config.vmware.vijava.Rrd getRrd(
    ) {
        return this._rrd;
    }

    /**
     * @param vVmwareGroup
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void addVmwareGroup(
            final org.opennms.netmgt.config.vmware.vijava.VmwareGroup vVmwareGroup)
            throws java.lang.IndexOutOfBoundsException {
        this._vmwareGroupList.add(vVmwareGroup);
    }

    /**
     * @param index
     * @param vVmwareGroup
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void addVmwareGroup(
            final int index,
            final org.opennms.netmgt.config.vmware.vijava.VmwareGroup vVmwareGroup)
            throws java.lang.IndexOutOfBoundsException {
        this._vmwareGroupList.add(index, vVmwareGroup);
    }

    /**
     * Method enumerateVmwareGroup.
     *
     * @return an Enumeration over all possible elements of this
     *         collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.vmware.vijava.VmwareGroup> enumerateVmwareGroup(
    ) {
        return java.util.Collections.enumeration(this._vmwareGroupList);
    }

    /**
     * Method getVmwareGroup.
     *
     * @param index
     * @return the value of the
     *         org.opennms.netmgt.config.vmware.vijava.VmwareGroup at the
     *         given index
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public org.opennms.netmgt.config.vmware.vijava.VmwareGroup getVmwareGroup(
            final int index)
            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vmwareGroupList.size()) {
            throw new IndexOutOfBoundsException("getVmwareGroup: Index value '" + index + "' not in range [0.." + (this._vmwareGroupList.size() - 1) + "]");
        }

        return (org.opennms.netmgt.config.vmware.vijava.VmwareGroup) _vmwareGroupList.get(index);
    }

    /**
     * Method getVmwareGroup.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     *
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.vmware.vijava.VmwareGroup[] getVmwareGroup(
    ) {
        org.opennms.netmgt.config.vmware.vijava.VmwareGroup[] array = new org.opennms.netmgt.config.vmware.vijava.VmwareGroup[0];
        return (org.opennms.netmgt.config.vmware.vijava.VmwareGroup[]) this._vmwareGroupList.toArray(array);
    }

    /**
     * Method getVmwareGroupCollection.Returns a reference to
     * '_vmwareGroupList'. No type checking is performed on any
     * modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.vmware.vijava.VmwareGroup> getVmwareGroupCollection(
    ) {
        return this._vmwareGroupList;
    }

    /**
     * Method getVmwareGroupCount.
     *
     * @return the size of this collection
     */
    public int getVmwareGroupCount(
    ) {
        return this._vmwareGroupList.size();
    }

    /**
     * Method iterateVmwareGroup.
     *
     * @return an Iterator over all possible elements in this
     *         collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.vmware.vijava.VmwareGroup> iterateVmwareGroup(
    ) {
        return this._vmwareGroupList.iterator();
    }

    /**
     */
    public void removeAllVmwareGroup(
    ) {
        this._vmwareGroupList.clear();
    }

    /**
     * Method removeVmwareGroup.
     *
     * @param vVmwareGroup
     * @return true if the object was removed from the collection.
     */
    public boolean removeVmwareGroup(
            final org.opennms.netmgt.config.vmware.vijava.VmwareGroup vVmwareGroup) {
        boolean removed = _vmwareGroupList.remove(vVmwareGroup);
        return removed;
    }

    /**
     * Method removeVmwareGroupAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.vmware.vijava.VmwareGroup removeVmwareGroupAt(
            final int index) {
        java.lang.Object obj = this._vmwareGroupList.remove(index);
        return (org.opennms.netmgt.config.vmware.vijava.VmwareGroup) obj;
    }

    /**
     * @param index
     * @param vVmwareGroup
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void setVmwareGroup(
            final int index,
            final org.opennms.netmgt.config.vmware.vijava.VmwareGroup vVmwareGroup)
            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vmwareGroupList.size()) {
            throw new IndexOutOfBoundsException("setVmwareGroup: Index value '" + index + "' not in range [0.." + (this._vmwareGroupList.size() - 1) + "]");
        }

        this._vmwareGroupList.set(index, vVmwareGroup);
    }

    /**
     * @param vVmwareGroupArray
     */
    public void setVmwareGroup(
            final org.opennms.netmgt.config.vmware.vijava.VmwareGroup[] vVmwareGroupArray) {
        //-- copy array
        _vmwareGroupList.clear();

        for (int i = 0; i < vVmwareGroupArray.length; i++) {
            this._vmwareGroupList.add(vVmwareGroupArray[i]);
        }
    }

    /**
     * Sets the value of '_vmwareGroupList' by copying the given
     * Vector. All elements will be checked for type safety.
     *
     * @param vVmwareGroupList the Vector to copy.
     */
    public void setVmwareGroup(
            final java.util.List<org.opennms.netmgt.config.vmware.vijava.VmwareGroup> vVmwareGroupList) {
        // copy vector
        this._vmwareGroupList.clear();

        this._vmwareGroupList.addAll(vVmwareGroupList);
    }

    /**
     * Sets the value of field 'name'.
     *
     * @param name the value of field 'name'.
     */
    public void setName(
            final java.lang.String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'rrd'. The field 'rrd' has the
     * following description: RRD parms
     *
     * @param rrd the value of field 'rrd'.
     */
    public void setRrd(
            final org.opennms.netmgt.config.vmware.vijava.Rrd rrd) {
        this._rrd = rrd;
    }
}
