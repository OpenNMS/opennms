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
 * An VMware Cim Object Group
 */

@XmlRootElement(name = "vmware-cim-group")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class VmwareCimGroup implements java.io.Serializable {

    /**
     * The name of the Cmi class to query
     */
    @XmlAttribute(name = "cimClass")
    private java.lang.String _cimClass;

    /**
     * The name of this group, for user id purposes
     */
    @XmlAttribute(name = "name")
    private java.lang.String _name;

    /**
     * The attibute with name Key will be checked against Value
     */
    @XmlAttribute(name = "key")
    private java.lang.String _key;

    /**
     * The attibute with name Key will be checked against Value
     */
    @XmlAttribute(name = "value")
    private java.lang.String _value;

    /**
     * The instance attribute of this group
     */
    @XmlAttribute(name = "instance")
    private java.lang.String _instance;

    /**
     * Specifies the name of the resource type that pertains to the
     * attributes
     * in this group. For scalar attributes (those occurring once
     * per node,
     * such as available system memory) this should be "node". For
     * multi-instanced attributes, this should be the name of a
     * custom
     * resource type declared in datacollection-config.xml.
     */
    @XmlAttribute(name = "resourceType")
    private java.lang.String _resourceType;

    /**
     * An VMware Cim Object
     */
    @XmlElement(name = "attrib")
    private java.util.List<org.opennms.netmgt.config.vmware.cim.Attrib> _attribList;

    public VmwareCimGroup() {
        super();
        this._attribList = new java.util.ArrayList<org.opennms.netmgt.config.vmware.cim.Attrib>();
    }

    /**
     * @param vAttrib
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void addAttrib(
            final org.opennms.netmgt.config.vmware.cim.Attrib vAttrib)
            throws java.lang.IndexOutOfBoundsException {
        this._attribList.add(vAttrib);
    }

    /**
     * @param index
     * @param vAttrib
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void addAttrib(
            final int index,
            final org.opennms.netmgt.config.vmware.cim.Attrib vAttrib)
            throws java.lang.IndexOutOfBoundsException {
        this._attribList.add(index, vAttrib);
    }

    /**
     * Method enumerateAttrib.
     *
     * @return an Enumeration over all possible elements of this
     *         collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.vmware.cim.Attrib> enumerateAttrib(
    ) {
        return java.util.Collections.enumeration(this._attribList);
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
        if (obj instanceof VmwareCimGroup) {
            VmwareCimGroup other = (VmwareCimGroup) obj;
            return new EqualsBuilder()
                    .append(getName(), other.getName())
                    .append(getAttrib(), other.getAttrib())
                    .append(getResourceType(), other.getResourceType())
                    .append(getCimClass(), other.getCimClass())
                    .append(getInstance(), other.getInstance())
                    .append(getKey(), other.getKey())
                    .append(getValue(), other.getValue())
                    .isEquals();
        }
        return false;
    }

    /**
     * Method getAttrib.
     *
     * @param index
     * @return the value of the
     *         org.opennms.netmgt.config.vmware.cim.Attrib at the given inde
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public org.opennms.netmgt.config.vmware.cim.Attrib getAttrib(
            final int index)
            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._attribList.size()) {
            throw new IndexOutOfBoundsException("getAttrib: Index value '" + index + "' not in range [0.." + (this._attribList.size() - 1) + "]");
        }

        return (org.opennms.netmgt.config.vmware.cim.Attrib) _attribList.get(index);
    }

    /**
     * Method getAttrib.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     *
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.vmware.cim.Attrib[] getAttrib(
    ) {
        org.opennms.netmgt.config.vmware.cim.Attrib[] array = new org.opennms.netmgt.config.vmware.cim.Attrib[0];
        return (org.opennms.netmgt.config.vmware.cim.Attrib[]) this._attribList.toArray(array);
    }

    /**
     * Method getAttribCollection.Returns a reference to
     * '_attribList'. No type checking is performed on any
     * modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.vmware.cim.Attrib> getAttribCollection(
    ) {
        return this._attribList;
    }

    /**
     * Method getAttribCount.
     *
     * @return the size of this collection
     */
    public int getAttribCount(
    ) {
        return this._attribList.size();
    }

    /**
     * Returns the value of field 'cimClass'. The field 'cimClass'
     * has the following description: The name of the Cmi class to
     * query
     *
     * @return the value of field 'CimClass'.
     */
    public java.lang.String getCimClass(
    ) {
        return this._cimClass == null ? "" : this._cimClass;
    }

    /**
     * Returns the value of field 'instance'. The field 'instance'
     * has the following description: The instance attribute of
     * this group
     *
     * @return the value of field 'Instance'.
     */
    public java.lang.String getInstance(
    ) {
        return this._instance == null ? "" : this._instance;
    }

    /**
     * Returns the value of field 'key'. The field 'key' has the
     * following description: The attibute with name Key will be
     * checked against Value
     *
     * @return the value of field 'Key'.
     */
    public java.lang.String getKey(
    ) {
        return this._key == null ? "" : this._key;
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the
     * following description: The name of this group, for user id
     * purposes
     *
     * @return the value of field 'Name'.
     */
    public java.lang.String getName(
    ) {
        return this._name == null ? "" : this._name;
    }

    /**
     * Returns the value of field 'resourceType'. The field
     * 'resourceType' has the following description: Specifies the
     * name of the resource type that pertains to the attributes
     * in this group. For scalar attributes (those occurring once
     * per node,
     * such as available system memory) this should be "node". For
     * multi-instanced attributes, this should be the name of a
     * custom
     * resource type declared in datacollection-config.xml.
     *
     * @return the value of field 'ResourceType'.
     */
    public java.lang.String getResourceType(
    ) {
        return this._resourceType == null ? "" : this._resourceType;
    }

    /**
     * Returns the value of field 'value'. The field 'value' has
     * the following description: The attibute with name Key will
     * be checked against Value
     *
     * @return the value of field 'Value'.
     */
    public java.lang.String getValue(
    ) {
        return this._value == null ? "" : this._value;
    }

    /**
     * Method iterateAttrib.
     *
     * @return an Iterator over all possible elements in this
     *         collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.vmware.cim.Attrib> iterateAttrib(
    ) {
        return this._attribList.iterator();
    }

    /**
     */
    public void removeAllAttrib(
    ) {
        this._attribList.clear();
    }

    /**
     * Method removeAttrib.
     *
     * @param vAttrib
     * @return true if the object was removed from the collection.
     */
    public boolean removeAttrib(
            final org.opennms.netmgt.config.vmware.cim.Attrib vAttrib) {
        boolean removed = _attribList.remove(vAttrib);
        return removed;
    }

    /**
     * Method removeAttribAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.vmware.cim.Attrib removeAttribAt(
            final int index) {
        java.lang.Object obj = this._attribList.remove(index);
        return (org.opennms.netmgt.config.vmware.cim.Attrib) obj;
    }

    /**
     * @param index
     * @param vAttrib
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void setAttrib(
            final int index,
            final org.opennms.netmgt.config.vmware.cim.Attrib vAttrib)
            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._attribList.size()) {
            throw new IndexOutOfBoundsException("setAttrib: Index value '" + index + "' not in range [0.." + (this._attribList.size() - 1) + "]");
        }

        this._attribList.set(index, vAttrib);
    }

    /**
     * @param vAttribArray
     */
    public void setAttrib(
            final org.opennms.netmgt.config.vmware.cim.Attrib[] vAttribArray) {
        //-- copy array
        _attribList.clear();

        for (int i = 0; i < vAttribArray.length; i++) {
            this._attribList.add(vAttribArray[i]);
        }
    }

    /**
     * Sets the value of '_attribList' by copying the given Vector.
     * All elements will be checked for type safety.
     *
     * @param vAttribList the Vector to copy.
     */
    public void setAttrib(
            final java.util.List<org.opennms.netmgt.config.vmware.cim.Attrib> vAttribList) {
        // copy vector
        this._attribList.clear();

        this._attribList.addAll(vAttribList);
    }

    /**
     * Sets the value of field 'cimClass'. The field 'cimClass' has
     * the following description: The name of the Cmi class to
     * query
     *
     * @param cimClass the value of field 'cimClass'.
     */
    public void setCimClass(
            final java.lang.String cimClass) {
        this._cimClass = cimClass;
    }

    /**
     * Sets the value of field 'instance'. The field 'instance' has
     * the following description: The instance attribute of this
     * group
     *
     * @param instance the value of field 'instance'.
     */
    public void setInstance(
            final java.lang.String instance) {
        this._instance = instance;
    }

    /**
     * Sets the value of field 'key'. The field 'key' has the
     * following description: The attibute with name Key will be
     * checked against Value
     *
     * @param key the value of field 'key'.
     */
    public void setKey(
            final java.lang.String key) {
        this._key = key;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the
     * following description: The name of this group, for user id
     * purposes
     *
     * @param name the value of field 'name'.
     */
    public void setName(
            final java.lang.String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'resourceType'. The field
     * 'resourceType' has the following description: Specifies the
     * name of the resource type that pertains to the attributes
     * in this group. For scalar attributes (those occurring once
     * per node,
     * such as available system memory) this should be "node". For
     * multi-instanced attributes, this should be the name of a
     * custom
     * resource type declared in datacollection-config.xml.
     *
     * @param resourceType the value of field 'resourceType'.
     */
    public void setResourceType(
            final java.lang.String resourceType) {
        this._resourceType = resourceType;
    }

    /**
     * Sets the value of field 'value'. The field 'value' has the
     * following description: The attibute with name Key will be
     * checked against Value
     *
     * @param value the value of field 'value'.
     */
    public void setValue(
            final java.lang.String value) {
        this._value = value;
    }
}
