/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

/**
 * This class was original generated with Castor, but is no longer.
 */
package org.opennms.netmgt.config.service;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the service-configuration.xml configuration file.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "service-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceConfiguration implements Serializable {
    private static final long serialVersionUID = 1477638002034420045L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Service to be launched by the manager.
     */
    @XmlElement(name = "service")
    private List<Service> _serviceList = new ArrayList<Service>(0);;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public ServiceConfiguration() {
        super();
    }

    public ServiceConfiguration(final List<Service> serviceList) {
        super();
        setService(serviceList);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * 
     * 
     * @param vService
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addService(final Service vService)
            throws IndexOutOfBoundsException {
        this._serviceList.add(vService);
    }

    /**
     * 
     * 
     * @param index
     * @param vService
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addService(final int index, final Service vService)
            throws IndexOutOfBoundsException {
        this._serviceList.add(index, vService);
    }

    /**
     * Method enumerateService.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<Service> enumerateService() {
        return Collections.enumeration(this._serviceList);
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof ServiceConfiguration) {

            ServiceConfiguration temp = (ServiceConfiguration) obj;
            if (this._serviceList != null) {
                if (temp._serviceList == null)
                    return false;
                else if (!(this._serviceList.equals(temp._serviceList)))
                    return false;
            } else if (temp._serviceList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getService.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the org.opennms.netmgt.config.service.Service at
     *         the given index
     */
    public Service getService(final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._serviceList.size()) {
            throw new IndexOutOfBoundsException("getService: Index value '"
                    + index + "' not in range [0.."
                    + (this._serviceList.size() - 1) + "]");
        }

        return (Service) _serviceList.get(index);
    }

    /**
     * Method getService.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public Service[] getService() {
        Service[] array = new Service[0];
        return (Service[]) this._serviceList.toArray(array);
    }

    /**
     * Method getServiceCollection.Returns a reference to '_serviceList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Service> getServiceCollection() {
        return this._serviceList;
    }

    /**
     * Method getServiceCount.
     * 
     * @return the size of this collection
     */
    public int getServiceCount() {
        return this._serviceList.size();
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;

        if (_serviceList != null) {
            result = 37 * result + _serviceList.hashCode();
        }

        return result;
    }

    /**
     * Method iterateService.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Service> iterateService() {
        return this._serviceList.iterator();
    }

    /**
     */
    public void removeAllService() {
        this._serviceList.clear();
    }

    /**
     * Method removeService.
     * 
     * @param vService
     * @return true if the object was removed from the collection.
     */
    public boolean removeService(final Service vService) {
        boolean removed = _serviceList.remove(vService);
        return removed;
    }

    /**
     * Method removeServiceAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Service removeServiceAt(final int index) {
        Object obj = this._serviceList.remove(index);
        return (Service) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vService
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setService(final int index, final Service vService)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._serviceList.size()) {
            throw new IndexOutOfBoundsException("setService: Index value '"
                    + index + "' not in range [0.."
                    + (this._serviceList.size() - 1) + "]");
        }

        this._serviceList.set(index, vService);
    }

    /**
     * 
     * 
     * @param vServiceArray
     */
    public void setService(final Service[] vServiceArray) {
        // -- copy array
        _serviceList.clear();

        for (int i = 0; i < vServiceArray.length; i++) {
            this._serviceList.add(vServiceArray[i]);
        }
    }

    /**
     * Sets the value of '_serviceList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vServiceList
     *            the Vector to copy.
     */
    public void setService(final java.util.List<Service> vServiceList) {
        // copy vector
        this._serviceList.clear();

        this._serviceList.addAll(vServiceList);
    }
}
