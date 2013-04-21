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

package org.opennms.netmgt.config.vmware.cim;

import org.apache.commons.lang.builder.EqualsBuilder;

import javax.xml.bind.annotation.*;

/**
 * A grouping of VMware related RRD parms and performance counter
 * groups
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "vmware-cim-collection")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class VmwareCimCollection implements java.io.Serializable {

    /**
     * Field _name.
     */
    @XmlAttribute(name = "name")
    private java.lang.String _name;

    /**
     * RRD parms
     */
    @XmlElement(name = "rrd")
    private org.opennms.netmgt.config.vmware.cim.Rrd _rrd;

    /**
     * VMware Cim object groups
     */
    @XmlElementWrapper(name = "vmware-cim-groups")
    @XmlElement(name = "vmware-cim-group")
    private java.util.List<org.opennms.netmgt.config.vmware.cim.VmwareCimGroup> _vmwareCimGroupList;

    public VmwareCimCollection() {
        super();
        this._vmwareCimGroupList = new java.util.ArrayList<org.opennms.netmgt.config.vmware.cim.VmwareCimGroup>();
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
        if (obj instanceof VmwareCimCollection) {
            VmwareCimCollection other = (VmwareCimCollection) obj;
            return new EqualsBuilder()
                    .append(getName(), other.getName())
                    .append(getRrd(), other.getRrd())
                    .append(getVmwareCimGroup(), other.getVmwareCimGroup())
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
    public org.opennms.netmgt.config.vmware.cim.Rrd getRrd(
    ) {
        return this._rrd;
    }

    /**
     * @param vVmwareCimGroup
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void addVmwareCimGroup(
            final org.opennms.netmgt.config.vmware.cim.VmwareCimGroup vVmwareCimGroup)
            throws java.lang.IndexOutOfBoundsException {
        this._vmwareCimGroupList.add(vVmwareCimGroup);
    }

    /**
     * @param index
     * @param vVmwareCimGroup
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void addVmwareCimGroup(
            final int index,
            final org.opennms.netmgt.config.vmware.cim.VmwareCimGroup vVmwareCimGroup)
            throws java.lang.IndexOutOfBoundsException {
        this._vmwareCimGroupList.add(index, vVmwareCimGroup);
    }

    /**
     * Method enumerateVmwareCimGroup.
     *
     * @return an Enumeration over all possible elements of this
     *         collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.vmware.cim.VmwareCimGroup> enumerateVmwareCimGroup(
    ) {
        return java.util.Collections.enumeration(this._vmwareCimGroupList);
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
            final org.opennms.netmgt.config.vmware.cim.Rrd rrd) {
        this._rrd = rrd;
    }

    /**
     * Method getVmwareCimGroup.
     *
     * @param index
     * @return the value of the
     *         org.opennms.netmgt.config.vmware.cim.VmwareCimGroup at the
     *         given index
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public org.opennms.netmgt.config.vmware.cim.VmwareCimGroup getVmwareCimGroup(
            final int index)
            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vmwareCimGroupList.size()) {
            throw new IndexOutOfBoundsException("getVmwareCimGroup: Index value '" + index + "' not in range [0.." + (this._vmwareCimGroupList.size() - 1) + "]");
        }

        return (org.opennms.netmgt.config.vmware.cim.VmwareCimGroup) _vmwareCimGroupList.get(index);
    }

    /**
     * Method getVmwareCimGroup.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     *
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.vmware.cim.VmwareCimGroup[] getVmwareCimGroup(
    ) {
        org.opennms.netmgt.config.vmware.cim.VmwareCimGroup[] array = new org.opennms.netmgt.config.vmware.cim.VmwareCimGroup[0];
        return (org.opennms.netmgt.config.vmware.cim.VmwareCimGroup[]) this._vmwareCimGroupList.toArray(array);
    }

    /**
     * Method getVmwareCimGroupCollection.Returns a reference to
     * '_vmwareCimGroupList'. No type checking is performed on any
     * modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.vmware.cim.VmwareCimGroup> getVmwareCimGroupCollection(
    ) {
        return this._vmwareCimGroupList;
    }

    /**
     * Method getVmwareCimGroupCount.
     *
     * @return the size of this collection
     */
    public int getVmwareCimGroupCount(
    ) {
        return this._vmwareCimGroupList.size();
    }

    /**
     * Method iterateVmwareCimGroup.
     *
     * @return an Iterator over all possible elements in this
     *         collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.vmware.cim.VmwareCimGroup> iterateVmwareCimGroup(
    ) {
        return this._vmwareCimGroupList.iterator();
    }

    /**
     */
    public void removeAllVmwareCimGroup(
    ) {
        this._vmwareCimGroupList.clear();
    }

    /**
     * Method removeVmwareCimGroup.
     *
     * @param vVmwareCimGroup
     * @return true if the object was removed from the collection.
     */
    public boolean removeVmwareCimGroup(
            final org.opennms.netmgt.config.vmware.cim.VmwareCimGroup vVmwareCimGroup) {
        boolean removed = _vmwareCimGroupList.remove(vVmwareCimGroup);
        return removed;
    }

    /**
     * Method removeVmwareCimGroupAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.vmware.cim.VmwareCimGroup removeVmwareCimGroupAt(
            final int index) {
        java.lang.Object obj = this._vmwareCimGroupList.remove(index);
        return (org.opennms.netmgt.config.vmware.cim.VmwareCimGroup) obj;
    }

    /**
     * @param index
     * @param vVmwareCimGroup
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void setVmwareCimGroup(
            final int index,
            final org.opennms.netmgt.config.vmware.cim.VmwareCimGroup vVmwareCimGroup)
            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vmwareCimGroupList.size()) {
            throw new IndexOutOfBoundsException("setVmwareCimGroup: Index value '" + index + "' not in range [0.." + (this._vmwareCimGroupList.size() - 1) + "]");
        }

        this._vmwareCimGroupList.set(index, vVmwareCimGroup);
    }

    /**
     * @param vVmwareCimGroupArray
     */
    public void setVmwareCimGroup(
            final org.opennms.netmgt.config.vmware.cim.VmwareCimGroup[] vVmwareCimGroupArray) {
        //-- copy array
        _vmwareCimGroupList.clear();

        for (int i = 0; i < vVmwareCimGroupArray.length; i++) {
            this._vmwareCimGroupList.add(vVmwareCimGroupArray[i]);
        }
    }

    /**
     * Sets the value of '_vmwareCimGroupList' by copying the given
     * Vector. All elements will be checked for type safety.
     *
     * @param vVmwareCimGroupList the Vector to copy.
     */
    public void setVmwareCimGroup(
            final java.util.List<org.opennms.netmgt.config.vmware.cim.VmwareCimGroup> vVmwareCimGroupList) {
        // copy vector
        this._vmwareCimGroupList.clear();

        this._vmwareCimGroupList.addAll(vVmwareCimGroupList);
    }
}
