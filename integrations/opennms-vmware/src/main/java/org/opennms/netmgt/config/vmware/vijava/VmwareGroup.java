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
package org.opennms.netmgt.config.vmware.vijava;

import org.apache.commons.lang.builder.EqualsBuilder;

import javax.xml.bind.annotation.*;

/**
 * An VMware Object Group
 */
@XmlRootElement(name = "vmware-group")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class VmwareGroup implements java.io.Serializable {

    /**
     * The name of this group, for user id purposes
     */
    @XmlAttribute(name = "name")
    private java.lang.String _name;

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
     * An VMware Object
     */
    @XmlElement(name = "attrib")
    private java.util.List<org.opennms.netmgt.config.vmware.vijava.Attrib> _attribList;

    public VmwareGroup() {
        super();
        this._attribList = new java.util.ArrayList<>();
    }

    /**
     * @param vAttrib
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void addAttrib(
            final org.opennms.netmgt.config.vmware.vijava.Attrib vAttrib)
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
            final org.opennms.netmgt.config.vmware.vijava.Attrib vAttrib)
            throws java.lang.IndexOutOfBoundsException {
        this._attribList.add(index, vAttrib);
    }

    /**
     * Method enumerateAttrib.
     *
     * @return an Enumeration over all possible elements of this
     *         collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.vmware.vijava.Attrib> enumerateAttrib(
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
        if (obj instanceof VmwareGroup) {
            VmwareGroup other = (VmwareGroup) obj;
            return new EqualsBuilder()
                    .append(getName(), other.getName())
                    .append(getResourceType(), other.getResourceType())
                    .append(getAttrib(), other.getAttrib())
                    .isEquals();
        }
        return false;
    }

    /**
     * Method getAttrib.
     *
     * @param index
     * @return the value of the
     *         org.opennms.netmgt.config.vmware.vijava.Attrib at the given
     *         index
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public org.opennms.netmgt.config.vmware.vijava.Attrib getAttrib(
            final int index)
            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._attribList.size()) {
            throw new IndexOutOfBoundsException("getAttrib: Index value '" + index + "' not in range [0.." + (this._attribList.size() - 1) + "]");
        }

        return (org.opennms.netmgt.config.vmware.vijava.Attrib) _attribList.get(index);
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
    public org.opennms.netmgt.config.vmware.vijava.Attrib[] getAttrib(
    ) {
        org.opennms.netmgt.config.vmware.vijava.Attrib[] array = new org.opennms.netmgt.config.vmware.vijava.Attrib[0];
        return (org.opennms.netmgt.config.vmware.vijava.Attrib[]) this._attribList.toArray(array);
    }

    /**
     * Method getAttribCollection.Returns a reference to
     * '_attribList'. No type checking is performed on any
     * modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.vmware.vijava.Attrib> getAttribCollection(
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
     * Method iterateAttrib.
     *
     * @return an Iterator over all possible elements in this
     *         collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.vmware.vijava.Attrib> iterateAttrib(
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
            final org.opennms.netmgt.config.vmware.vijava.Attrib vAttrib) {
        boolean removed = _attribList.remove(vAttrib);
        return removed;
    }

    /**
     * Method removeAttribAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.vmware.vijava.Attrib removeAttribAt(
            final int index) {
        java.lang.Object obj = this._attribList.remove(index);
        return (org.opennms.netmgt.config.vmware.vijava.Attrib) obj;
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
            final org.opennms.netmgt.config.vmware.vijava.Attrib vAttrib)
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
            final org.opennms.netmgt.config.vmware.vijava.Attrib[] vAttribArray) {
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
            final java.util.List<org.opennms.netmgt.config.vmware.vijava.Attrib> vAttribList) {
        // copy vector
        this._attribList.clear();

        this._attribList.addAll(vAttribList);
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
}
