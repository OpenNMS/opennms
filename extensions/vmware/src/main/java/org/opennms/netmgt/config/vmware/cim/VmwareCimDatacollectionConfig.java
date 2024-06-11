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
package org.opennms.netmgt.config.vmware.cim;

import org.apache.commons.lang.builder.EqualsBuilder;

import javax.xml.bind.annotation.*;

/**
 * Class VmwareCimDatacollectionConfig.
 */
@XmlRootElement(name = "vmware-cim-datacollection-config")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class VmwareCimDatacollectionConfig implements java.io.Serializable {

    /**
     * Field _rrdRepository.
     */
    @XmlAttribute(name = "rrdRepository")
    private java.lang.String _rrdRepository;

    /**
     * A grouping of VMware related RRD parms and performance
     * counter groups
     */
    @XmlElement(name = "vmware-cim-collection")
    private java.util.List<org.opennms.netmgt.config.vmware.cim.VmwareCimCollection> _vmwareCimCollectionList;

    public VmwareCimDatacollectionConfig() {
        super();
        this._vmwareCimCollectionList = new java.util.ArrayList<>();
    }

    /**
     * @param vVmwareCimCollection
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void addVmwareCimCollection(
            final org.opennms.netmgt.config.vmware.cim.VmwareCimCollection vVmwareCimCollection)
            throws java.lang.IndexOutOfBoundsException {
        this._vmwareCimCollectionList.add(vVmwareCimCollection);
    }

    /**
     * @param index
     * @param vVmwareCimCollection
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void addVmwareCimCollection(
            final int index,
            final org.opennms.netmgt.config.vmware.cim.VmwareCimCollection vVmwareCimCollection)
            throws java.lang.IndexOutOfBoundsException {
        this._vmwareCimCollectionList.add(index, vVmwareCimCollection);
    }

    /**
     * Method enumerateVmwareCimCollection.
     *
     * @return an Enumeration over all possible elements of this
     *         collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.vmware.cim.VmwareCimCollection> enumerateVmwareCimCollection(
    ) {
        return java.util.Collections.enumeration(this._vmwareCimCollectionList);
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
        if (obj instanceof VmwareCimDatacollectionConfig) {
            VmwareCimDatacollectionConfig other = (VmwareCimDatacollectionConfig) obj;
            return new EqualsBuilder()
                    .append(getRrdRepository(), other.getRrdRepository())
                    .append(getVmwareCimCollection(), other.getVmwareCimCollection())
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
     * Method getVmwareCimCollection.
     *
     * @param index
     * @return the value of the
     *         org.opennms.netmgt.config.vmware.cim.VmwareCimCollection at
     *         the given index
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public org.opennms.netmgt.config.vmware.cim.VmwareCimCollection getVmwareCimCollection(
            final int index)
            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vmwareCimCollectionList.size()) {
            throw new IndexOutOfBoundsException("getVmwareCimCollection: Index value '" + index + "' not in range [0.." + (this._vmwareCimCollectionList.size() - 1) + "]");
        }

        return (org.opennms.netmgt.config.vmware.cim.VmwareCimCollection) _vmwareCimCollectionList.get(index);
    }

    /**
     * Method getVmwareCimCollection.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     *
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.vmware.cim.VmwareCimCollection[] getVmwareCimCollection(
    ) {
        org.opennms.netmgt.config.vmware.cim.VmwareCimCollection[] array = new org.opennms.netmgt.config.vmware.cim.VmwareCimCollection[0];
        return (org.opennms.netmgt.config.vmware.cim.VmwareCimCollection[]) this._vmwareCimCollectionList.toArray(array);
    }

    /**
     * Method getVmwareCimCollectionCollection.Returns a reference
     * to '_vmwareCimCollectionList'. No type checking is performed
     * on any modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.vmware.cim.VmwareCimCollection> getVmwareCimCollectionCollection(
    ) {
        return this._vmwareCimCollectionList;
    }

    /**
     * Method getVmwareCimCollectionCount.
     *
     * @return the size of this collection
     */
    public int getVmwareCimCollectionCount(
    ) {
        return this._vmwareCimCollectionList.size();
    }

    /**
     * Method iterateVmwareCimCollection.
     *
     * @return an Iterator over all possible elements in this
     *         collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.vmware.cim.VmwareCimCollection> iterateVmwareCimCollection(
    ) {
        return this._vmwareCimCollectionList.iterator();
    }

    /**
     */
    public void removeAllVmwareCimCollection(
    ) {
        this._vmwareCimCollectionList.clear();
    }

    /**
     * Method removeVmwareCimCollection.
     *
     * @param vVmwareCimCollection
     * @return true if the object was removed from the collection.
     */
    public boolean removeVmwareCimCollection(
            final org.opennms.netmgt.config.vmware.cim.VmwareCimCollection vVmwareCimCollection) {
        boolean removed = _vmwareCimCollectionList.remove(vVmwareCimCollection);
        return removed;
    }

    /**
     * Method removeVmwareCimCollectionAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.vmware.cim.VmwareCimCollection removeVmwareCimCollectionAt(
            final int index) {
        java.lang.Object obj = this._vmwareCimCollectionList.remove(index);
        return (org.opennms.netmgt.config.vmware.cim.VmwareCimCollection) obj;
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
     * @param vVmwareCimCollection
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void setVmwareCimCollection(
            final int index,
            final org.opennms.netmgt.config.vmware.cim.VmwareCimCollection vVmwareCimCollection)
            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vmwareCimCollectionList.size()) {
            throw new IndexOutOfBoundsException("setVmwareCimCollection: Index value '" + index + "' not in range [0.." + (this._vmwareCimCollectionList.size() - 1) + "]");
        }

        this._vmwareCimCollectionList.set(index, vVmwareCimCollection);
    }

    /**
     * @param vVmwareCimCollectionArray
     */
    public void setVmwareCimCollection(
            final org.opennms.netmgt.config.vmware.cim.VmwareCimCollection[] vVmwareCimCollectionArray) {
        //-- copy array
        _vmwareCimCollectionList.clear();

        for (int i = 0; i < vVmwareCimCollectionArray.length; i++) {
            this._vmwareCimCollectionList.add(vVmwareCimCollectionArray[i]);
        }
    }

    /**
     * Sets the value of '_vmwareCimCollectionList' by copying the
     * given Vector. All elements will be checked for type safety.
     *
     * @param vVmwareCimCollectionList the Vector to copy.
     */
    public void setVmwareCimCollection(
            final java.util.List<org.opennms.netmgt.config.vmware.cim.VmwareCimCollection> vVmwareCimCollectionList) {
        // copy vector
        this._vmwareCimCollectionList.clear();

        this._vmwareCimCollectionList.addAll(vVmwareCimCollectionList);
    }
}
