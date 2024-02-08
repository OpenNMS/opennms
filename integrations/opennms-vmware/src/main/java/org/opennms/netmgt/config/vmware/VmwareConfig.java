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
package org.opennms.netmgt.config.vmware;

import org.apache.commons.lang.builder.EqualsBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is the top-level element for vmware-config.xml
 */
@XmlRootElement(name = "vmware-config")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class VmwareConfig implements java.io.Serializable {

    /**
     * A VMware Server entry
     */
    @XmlElement(name = "vmware-server")
    private java.util.List<org.opennms.netmgt.config.vmware.VmwareServer> _vmwareServerList;

    public VmwareConfig() {
        super();
        this._vmwareServerList = new java.util.ArrayList<>();
    }

    /**
     * @param vVmwareServer
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void addVmwareServer(
            final org.opennms.netmgt.config.vmware.VmwareServer vVmwareServer)
            throws java.lang.IndexOutOfBoundsException {
        this._vmwareServerList.add(vVmwareServer);
    }

    /**
     * @param index
     * @param vVmwareServer
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void addVmwareServer(
            final int index,
            final org.opennms.netmgt.config.vmware.VmwareServer vVmwareServer)
            throws java.lang.IndexOutOfBoundsException {
        this._vmwareServerList.add(index, vVmwareServer);
    }

    /**
     * Method enumerateVmwareServer.
     *
     * @return an Enumeration over all possible elements of this
     *         collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.vmware.VmwareServer> enumerateVmwareServer(
    ) {
        return java.util.Collections.enumeration(this._vmwareServerList);
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
        if (obj instanceof VmwareConfig) {
            VmwareConfig other = (VmwareConfig) obj;
            return new EqualsBuilder()
                    .append(getVmwareServer(), other.getVmwareServer())
                    .isEquals();
        }
        return false;
    }

    /**
     * Method getVmwareServer.
     *
     * @param index
     * @return the value of the
     *         org.opennms.netmgt.config.vmware.VmwareServer at the given
     *         index
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public org.opennms.netmgt.config.vmware.VmwareServer getVmwareServer(
            final int index)
            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vmwareServerList.size()) {
            throw new IndexOutOfBoundsException("getVmwareServer: Index value '" + index + "' not in range [0.." + (this._vmwareServerList.size() - 1) + "]");
        }

        return (org.opennms.netmgt.config.vmware.VmwareServer) _vmwareServerList.get(index);
    }

    /**
     * Method getVmwareServer.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     *
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.vmware.VmwareServer[] getVmwareServer(
    ) {
        org.opennms.netmgt.config.vmware.VmwareServer[] array = new org.opennms.netmgt.config.vmware.VmwareServer[0];
        return (org.opennms.netmgt.config.vmware.VmwareServer[]) this._vmwareServerList.toArray(array);
    }

    /**
     * Method getVmwareServerCollection.Returns a reference to
     * '_vmwareServerList'. No type checking is performed on any
     * modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.vmware.VmwareServer> getVmwareServerCollection(
    ) {
        return this._vmwareServerList;
    }

    /**
     * Method getVmwareServerCount.
     *
     * @return the size of this collection
     */
    public int getVmwareServerCount(
    ) {
        return this._vmwareServerList.size();
    }

    /**
     * Method iterateVmwareServer.
     *
     * @return an Iterator over all possible elements in this
     *         collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.vmware.VmwareServer> iterateVmwareServer(
    ) {
        return this._vmwareServerList.iterator();
    }

    /**
     */
    public void removeAllVmwareServer(
    ) {
        this._vmwareServerList.clear();
    }

    /**
     * Method removeVmwareServer.
     *
     * @param vVmwareServer
     * @return true if the object was removed from the collection.
     */
    public boolean removeVmwareServer(
            final org.opennms.netmgt.config.vmware.VmwareServer vVmwareServer) {
        boolean removed = _vmwareServerList.remove(vVmwareServer);
        return removed;
    }

    /**
     * Method removeVmwareServerAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.vmware.VmwareServer removeVmwareServerAt(
            final int index) {
        java.lang.Object obj = this._vmwareServerList.remove(index);
        return (org.opennms.netmgt.config.vmware.VmwareServer) obj;
    }

    /**
     * @param index
     * @param vVmwareServer
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void setVmwareServer(
            final int index,
            final org.opennms.netmgt.config.vmware.VmwareServer vVmwareServer)
            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vmwareServerList.size()) {
            throw new IndexOutOfBoundsException("setVmwareServer: Index value '" + index + "' not in range [0.." + (this._vmwareServerList.size() - 1) + "]");
        }

        this._vmwareServerList.set(index, vVmwareServer);
    }

    /**
     * @param vVmwareServerArray
     */
    public void setVmwareServer(
            final org.opennms.netmgt.config.vmware.VmwareServer[] vVmwareServerArray) {
        //-- copy array
        _vmwareServerList.clear();

        for (int i = 0; i < vVmwareServerArray.length; i++) {
            this._vmwareServerList.add(vVmwareServerArray[i]);
        }
    }

    /**
     * Sets the value of '_vmwareServerList' by copying the given
     * Vector. All elements will be checked for type safety.
     *
     * @param vVmwareServerList the Vector to copy.
     */
    public void setVmwareServer(
            final java.util.List<org.opennms.netmgt.config.vmware.VmwareServer> vVmwareServerList) {
        // copy vector
        this._vmwareServerList.clear();

        this._vmwareServerList.addAll(vVmwareServerList);
    }
}
