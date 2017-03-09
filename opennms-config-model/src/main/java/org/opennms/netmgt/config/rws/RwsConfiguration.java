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

package org.opennms.netmgt.config.rws;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the rws-configuration.xml configuration file.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "rws-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class RwsConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Base Url(s) for Rancid Server.
     */
    @XmlElement(name = "base-url", required = true)
    private BaseUrl baseUrl;

    /**
     * Stand By Url(s) for Rancid Servers.
     */
    @XmlElement(name = "standby-url")
    private java.util.List<StandbyUrl> standbyUrlList;

    public RwsConfiguration() {
        this.standbyUrlList = new java.util.ArrayList<StandbyUrl>();
    }

    /**
     * 
     * 
     * @param vStandbyUrl
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addStandbyUrl(final StandbyUrl vStandbyUrl) throws IndexOutOfBoundsException {
        this.standbyUrlList.add(vStandbyUrl);
    }

    /**
     * 
     * 
     * @param index
     * @param vStandbyUrl
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addStandbyUrl(final int index, final StandbyUrl vStandbyUrl) throws IndexOutOfBoundsException {
        this.standbyUrlList.add(index, vStandbyUrl);
    }

    /**
     * Method enumerateStandbyUrl.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<StandbyUrl> enumerateStandbyUrl() {
        return java.util.Collections.enumeration(this.standbyUrlList);
    }

    /**
     * Returns the value of field 'baseUrl'. The field 'baseUrl' has the following
     * description: Base Url(s) for Rancid Server.
     * 
     * @return the value of field 'BaseUrl'.
     */
    public BaseUrl getBaseUrl() {
        return this.baseUrl;
    }

    /**
     * Method getStandbyUrl.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the StandbyUrl at the
     * given index
     */
    public StandbyUrl getStandbyUrl(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.standbyUrlList.size()) {
            throw new IndexOutOfBoundsException("getStandbyUrl: Index value '" + index + "' not in range [0.." + (this.standbyUrlList.size() - 1) + "]");
        }
        
        return (StandbyUrl) standbyUrlList.get(index);
    }

    /**
     * Method getStandbyUrl.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public StandbyUrl[] getStandbyUrl() {
        StandbyUrl[] array = new StandbyUrl[0];
        return (StandbyUrl[]) this.standbyUrlList.toArray(array);
    }

    /**
     * Method getStandbyUrlCollection.Returns a reference to 'standbyUrlList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<StandbyUrl> getStandbyUrlCollection() {
        return this.standbyUrlList;
    }

    /**
     * Method getStandbyUrlCount.
     * 
     * @return the size of this collection
     */
    public int getStandbyUrlCount() {
        return this.standbyUrlList.size();
    }

    /**
     * Method iterateStandbyUrl.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<StandbyUrl> iterateStandbyUrl() {
        return this.standbyUrlList.iterator();
    }

    /**
     */
    public void removeAllStandbyUrl() {
        this.standbyUrlList.clear();
    }

    /**
     * Method removeStandbyUrl.
     * 
     * @param vStandbyUrl
     * @return true if the object was removed from the collection.
     */
    public boolean removeStandbyUrl(final StandbyUrl vStandbyUrl) {
        boolean removed = standbyUrlList.remove(vStandbyUrl);
        return removed;
    }

    /**
     * Method removeStandbyUrlAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public StandbyUrl removeStandbyUrlAt(final int index) {
        Object obj = this.standbyUrlList.remove(index);
        return (StandbyUrl) obj;
    }

    /**
     * Sets the value of field 'baseUrl'. The field 'baseUrl' has the following
     * description: Base Url(s) for Rancid Server.
     * 
     * @param baseUrl the value of field 'baseUrl'.
     */
    public void setBaseUrl(final BaseUrl baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 
     * 
     * @param index
     * @param vStandbyUrl
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setStandbyUrl(final int index, final StandbyUrl vStandbyUrl) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.standbyUrlList.size()) {
            throw new IndexOutOfBoundsException("setStandbyUrl: Index value '" + index + "' not in range [0.." + (this.standbyUrlList.size() - 1) + "]");
        }
        
        this.standbyUrlList.set(index, vStandbyUrl);
    }

    /**
     * 
     * 
     * @param vStandbyUrlArray
     */
    public void setStandbyUrl(final StandbyUrl[] vStandbyUrlArray) {
        //-- copy array
        standbyUrlList.clear();
        
        for (int i = 0; i < vStandbyUrlArray.length; i++) {
                this.standbyUrlList.add(vStandbyUrlArray[i]);
        }
    }

    /**
     * Sets the value of 'standbyUrlList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vStandbyUrlList the Vector to copy.
     */
    public void setStandbyUrl(final java.util.List<StandbyUrl> vStandbyUrlList) {
        // copy vector
        this.standbyUrlList.clear();
        
        this.standbyUrlList.addAll(vStandbyUrlList);
    }

    /**
     * Sets the value of 'standbyUrlList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param standbyUrlList the Vector to set.
     */
    public void setStandbyUrlCollection(final java.util.List<StandbyUrl> standbyUrlList) {
        this.standbyUrlList = standbyUrlList;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof RwsConfiguration)) {
            return false;
        }
        RwsConfiguration castOther = (RwsConfiguration) other;
        return Objects.equals(baseUrl, castOther.baseUrl) && Objects.equals(standbyUrlList, castOther.standbyUrlList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUrl, standbyUrlList);
    }

}
