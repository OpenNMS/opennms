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

package org.opennms.report.inventory;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 * Class Nbisinglenode.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "nbisinglenode")
@XmlAccessorType(XmlAccessType.FIELD)
public class Nbisinglenode implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "groupname")
    private String groupname;

    @XmlElement(name = "devicename")
    private String devicename;

    @XmlElement(name = "configurationurl")
    private String configurationurl;

    @XmlElement(name = "creationdate")
    private java.util.Date creationdate;

    @XmlElement(name = "status")
    private String status;

    @XmlElement(name = "swconfigurationurl")
    private String swconfigurationurl;

    @XmlElement(name = "version")
    private String version;

    @XmlElement(name = "comment")
    private String comment;

    @XmlElement(name = "inventoryElement2RP")
    private java.util.List<InventoryElement2RP> inventoryElement2RPList;

    public Nbisinglenode() {
        this.inventoryElement2RPList = new java.util.ArrayList<>();
    }

    /**
     * 
     * 
     * @param vInventoryElement2RP
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addInventoryElement2RP(final InventoryElement2RP vInventoryElement2RP) throws IndexOutOfBoundsException {
        this.inventoryElement2RPList.add(vInventoryElement2RP);
    }

    /**
     * 
     * 
     * @param index
     * @param vInventoryElement2RP
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addInventoryElement2RP(final int index, final InventoryElement2RP vInventoryElement2RP) throws IndexOutOfBoundsException {
        this.inventoryElement2RPList.add(index, vInventoryElement2RP);
    }

    /**
     * Method enumerateInventoryElement2RP.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<InventoryElement2RP> enumerateInventoryElement2RP() {
        return java.util.Collections.enumeration(this.inventoryElement2RPList);
    }

    /**
     * Returns the value of field 'comment'.
     * 
     * @return the value of field 'Comment'.
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Returns the value of field 'configurationurl'.
     * 
     * @return the value of field 'Configurationurl'.
     */
    public String getConfigurationurl() {
        return this.configurationurl;
    }

    /**
     * Returns the value of field 'creationdate'.
     * 
     * @return the value of field 'Creationdate'.
     */
    public java.util.Date getCreationdate() {
        return this.creationdate;
    }

    /**
     * Returns the value of field 'devicename'.
     * 
     * @return the value of field 'Devicename'.
     */
    public String getDevicename() {
        return this.devicename;
    }

    /**
     * Returns the value of field 'groupname'.
     * 
     * @return the value of field 'Groupname'.
     */
    public String getGroupname() {
        return this.groupname;
    }

    /**
     * Method getInventoryElement2RP.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the InventoryElement2RP
     * at the given index
     */
    public InventoryElement2RP getInventoryElement2RP(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.inventoryElement2RPList.size()) {
            throw new IndexOutOfBoundsException("getInventoryElement2RP: Index value '" + index + "' not in range [0.." + (this.inventoryElement2RPList.size() - 1) + "]");
        }
        
        return (InventoryElement2RP) inventoryElement2RPList.get(index);
    }

    /**
     * Method getInventoryElement2RP.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are changing in
     * another thread, we pass a 0-length Array of the correct type into the API
     * call.  This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public InventoryElement2RP[] getInventoryElement2RP() {
        InventoryElement2RP[] array = new InventoryElement2RP[0];
        return (InventoryElement2RP[]) this.inventoryElement2RPList.toArray(array);
    }

    /**
     * Method getInventoryElement2RPCollection.Returns a reference to
     * 'inventoryElement2RPList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<InventoryElement2RP> getInventoryElement2RPCollection() {
        return this.inventoryElement2RPList;
    }

    /**
     * Method getInventoryElement2RPCount.
     * 
     * @return the size of this collection
     */
    public int getInventoryElement2RPCount() {
        return this.inventoryElement2RPList.size();
    }

    /**
     * Returns the value of field 'status'.
     * 
     * @return the value of field 'Status'.
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * Returns the value of field 'swconfigurationurl'.
     * 
     * @return the value of field 'Swconfigurationurl'.
     */
    public String getSwconfigurationurl() {
        return this.swconfigurationurl;
    }

    /**
     * Returns the value of field 'version'.
     * 
     * @return the value of field 'Version'.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Method iterateInventoryElement2RP.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<InventoryElement2RP> iterateInventoryElement2RP() {
        return this.inventoryElement2RPList.iterator();
    }

    /**
     */
    public void removeAllInventoryElement2RP() {
        this.inventoryElement2RPList.clear();
    }

    /**
     * Method removeInventoryElement2RP.
     * 
     * @param vInventoryElement2RP
     * @return true if the object was removed from the collection.
     */
    public boolean removeInventoryElement2RP(final InventoryElement2RP vInventoryElement2RP) {
        boolean removed = inventoryElement2RPList.remove(vInventoryElement2RP);
        return removed;
    }

    /**
     * Method removeInventoryElement2RPAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public InventoryElement2RP removeInventoryElement2RPAt(final int index) {
        Object obj = this.inventoryElement2RPList.remove(index);
        return (InventoryElement2RP) obj;
    }

    /**
     * Sets the value of field 'comment'.
     * 
     * @param comment the value of field 'comment'.
     */
    public void setComment(final String comment) {
        this.comment = comment;
    }

    /**
     * Sets the value of field 'configurationurl'.
     * 
     * @param configurationurl the value of field 'configurationurl'.
     */
    public void setConfigurationurl(final String configurationurl) {
        this.configurationurl = configurationurl;
    }

    /**
     * Sets the value of field 'creationdate'.
     * 
     * @param creationdate the value of field 'creationdate'.
     */
    public void setCreationdate(final java.util.Date creationdate) {
        this.creationdate = creationdate;
    }

    /**
     * Sets the value of field 'devicename'.
     * 
     * @param devicename the value of field 'devicename'.
     */
    public void setDevicename(final String devicename) {
        this.devicename = devicename;
    }

    /**
     * Sets the value of field 'groupname'.
     * 
     * @param groupname the value of field 'groupname'.
     */
    public void setGroupname(final String groupname) {
        this.groupname = groupname;
    }

    /**
     * 
     * 
     * @param index
     * @param vInventoryElement2RP
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setInventoryElement2RP(final int index, final InventoryElement2RP vInventoryElement2RP) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.inventoryElement2RPList.size()) {
            throw new IndexOutOfBoundsException("setInventoryElement2RP: Index value '" + index + "' not in range [0.." + (this.inventoryElement2RPList.size() - 1) + "]");
        }
        
        this.inventoryElement2RPList.set(index, vInventoryElement2RP);
    }

    /**
     * 
     * 
     * @param vInventoryElement2RPArray
     */
    public void setInventoryElement2RP(final InventoryElement2RP[] vInventoryElement2RPArray) {
        //-- copy array
        inventoryElement2RPList.clear();
        
        for (int i = 0; i < vInventoryElement2RPArray.length; i++) {
                this.inventoryElement2RPList.add(vInventoryElement2RPArray[i]);
        }
    }

    /**
     * Sets the value of 'inventoryElement2RPList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vInventoryElement2RPList the Vector to copy.
     */
    public void setInventoryElement2RP(final java.util.List<InventoryElement2RP> vInventoryElement2RPList) {
        // copy vector
        this.inventoryElement2RPList.clear();
        
        this.inventoryElement2RPList.addAll(vInventoryElement2RPList);
    }

    /**
     * Sets the value of 'inventoryElement2RPList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param inventoryElement2RPList the Vector to set.
     */
    public void setInventoryElement2RPCollection(final java.util.List<InventoryElement2RP> inventoryElement2RPList) {
        this.inventoryElement2RPList = inventoryElement2RPList;
    }

    /**
     * Sets the value of field 'status'.
     * 
     * @param status the value of field 'status'.
     */
    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * Sets the value of field 'swconfigurationurl'.
     * 
     * @param swconfigurationurl the value of field 'swconfigurationurl'.
     */
    public void setSwconfigurationurl(final String swconfigurationurl) {
        this.swconfigurationurl = swconfigurationurl;
    }

    /**
     * Sets the value of field 'version'.
     * 
     * @param version the value of field 'version'.
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Nbisinglenode)) {
            return false;
        }
        Nbisinglenode castOther = (Nbisinglenode) other;
        return Objects.equals(groupname, castOther.groupname) && Objects.equals(devicename, castOther.devicename)
                && Objects.equals(configurationurl, castOther.configurationurl)
                && Objects.equals(creationdate, castOther.creationdate) && Objects.equals(status, castOther.status)
                && Objects.equals(swconfigurationurl, castOther.swconfigurationurl)
                && Objects.equals(version, castOther.version) && Objects.equals(comment, castOther.comment)
                && Objects.equals(inventoryElement2RPList, castOther.inventoryElement2RPList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupname, devicename, configurationurl, creationdate, status, swconfigurationurl, version,
                comment, inventoryElement2RPList);
    }

}
