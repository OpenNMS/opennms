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

package org.opennms.netmgt.config.snmpinterfacepoller;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the snmp-interface-poller-configuration.xml
 *  configuration file.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "snmp-interface-poller-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class SnmpInterfacePollerConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_SUPPRESS_ADMIN_DOWN_EVENT = "true";
    private static final String DEFAULT_USE_CRITERIA_FILTERS = "false";

    /**
     * Default Interval at which the interfaces are to be
     *  polled
     */
    @XmlAttribute(name = "interval")
    private Long interval;

    /**
     * The maximum number of threads used for
     *  snmp polling.
     */
    @XmlAttribute(name = "threads", required = true)
    private Integer threads;

    /**
     * The SNMP service string usually 'SNMP'.
     */
    @XmlAttribute(name = "service", required = true)
    private String service;

    /**
     * Flag which indicates to suppress Admin Status events at all.
     *  This is deprecated and will be ignored in the code!
     *  
     */
    @XmlAttribute(name = "suppressAdminDownEvent")
    private String suppressAdminDownEvent;

    /**
     * Flag which indicates if the filters defined on packages and interface
     *  criterias must be used to select the SNMP interfaces to be tracked by the
     * poller
     *  instead of do this selection through requisition policies.
     *  
     */
    @XmlAttribute(name = "useCriteriaFilters")
    private String useCriteriaFilters;

    /**
     * Configuration of node-outage
     *  functionality
     */
    @XmlElement(name = "node-outage", required = true)
    private org.opennms.netmgt.config.snmpinterfacepoller.NodeOutage nodeOutage;

    /**
     * Package encapsulating addresses, services to be
     *  polled for these addresses, etc..
     */
    @XmlElement(name = "package", required = true)
    private java.util.List<org.opennms.netmgt.config.snmpinterfacepoller.Package> _packageList = new java.util.ArrayList<>();

    /**
     * 
     * 
     * @param vPackage
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addPackage(final org.opennms.netmgt.config.snmpinterfacepoller.Package vPackage) throws IndexOutOfBoundsException {
        this._packageList.add(vPackage);
    }

    /**
     * 
     * 
     * @param index
     * @param vPackage
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addPackage(final int index, final org.opennms.netmgt.config.snmpinterfacepoller.Package vPackage) throws IndexOutOfBoundsException {
        this._packageList.add(index, vPackage);
    }

    /**
     */
    public void deleteInterval() {
        this.interval= null;
    }

    /**
     */
    public void deleteThreads() {
        this.threads= null;
    }

    /**
     * Method enumeratePackage.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.snmpinterfacepoller.Package> enumeratePackage() {
        return java.util.Collections.enumeration(this._packageList);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof SnmpInterfacePollerConfiguration) {
            SnmpInterfacePollerConfiguration temp = (SnmpInterfacePollerConfiguration)obj;
            boolean equals = Objects.equals(temp.interval, interval)
                && Objects.equals(temp.threads, threads)
                && Objects.equals(temp.service, service)
                && Objects.equals(temp.suppressAdminDownEvent, suppressAdminDownEvent)
                && Objects.equals(temp.useCriteriaFilters, useCriteriaFilters)
                && Objects.equals(temp.nodeOutage, nodeOutage)
                && Objects.equals(temp._packageList, _packageList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'interval'. The field 'interval' has the
     * following description: Default Interval at which the interfaces are to be
     *  polled
     * 
     * @return the value of field 'Interval'.
     */
    public Long getInterval() {
        return this.interval != null ? this.interval : Long.valueOf("300000");
    }

    /**
     * Returns the value of field 'nodeOutage'. The field 'nodeOutage' has the
     * following description: Configuration of node-outage
     *  functionality
     * 
     * @return the value of field 'NodeOutage'.
     */
    public org.opennms.netmgt.config.snmpinterfacepoller.NodeOutage getNodeOutage() {
        return this.nodeOutage;
    }

    /**
     * Method getPackage.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.snmpinterfacepoller.Package at the given index
     */
    public org.opennms.netmgt.config.snmpinterfacepoller.Package getPackage(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._packageList.size()) {
            throw new IndexOutOfBoundsException("getPackage: Index value '" + index + "' not in range [0.." + (this._packageList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.config.snmpinterfacepoller.Package) _packageList.get(index);
    }

    /**
     * Method getPackage.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.snmpinterfacepoller.Package[] getPackage() {
        org.opennms.netmgt.config.snmpinterfacepoller.Package[] array = new org.opennms.netmgt.config.snmpinterfacepoller.Package[0];
        return (org.opennms.netmgt.config.snmpinterfacepoller.Package[]) this._packageList.toArray(array);
    }

    /**
     * Method getPackageCollection.Returns a reference to '_packageList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.snmpinterfacepoller.Package> getPackageCollection() {
        return this._packageList;
    }

    /**
     * Method getPackageCount.
     * 
     * @return the size of this collection
     */
    public int getPackageCount() {
        return this._packageList.size();
    }

    /**
     * Returns the value of field 'service'. The field 'service' has the following
     * description: The SNMP service string usually 'SNMP'.
     * 
     * @return the value of field 'Service'.
     */
    public String getService() {
        return this.service;
    }

    /**
     * Returns the value of field 'suppressAdminDownEvent'. The field
     * 'suppressAdminDownEvent' has the following description: Flag which
     * indicates to suppress Admin Status events at all.
     *  This is deprecated and will be ignored in the code!
     *  
     * 
     * @return the value of field 'SuppressAdminDownEvent'.
     */
    public String getSuppressAdminDownEvent() {
        return this.suppressAdminDownEvent != null ? this.suppressAdminDownEvent : DEFAULT_SUPPRESS_ADMIN_DOWN_EVENT;
    }

    /**
     * Returns the value of field 'threads'. The field 'threads' has the following
     * description: The maximum number of threads used for
     *  snmp polling.
     * 
     * @return the value of field 'Threads'.
     */
    public Integer getThreads() {
        return this.threads;
    }

    /**
     * Returns the value of field 'useCriteriaFilters'. The field
     * 'useCriteriaFilters' has the following description: Flag which indicates if
     * the filters defined on packages and interface
     *  criterias must be used to select the SNMP interfaces to be tracked by the
     * poller
     *  instead of do this selection through requisition policies.
     *  
     * 
     * @return the value of field 'UseCriteriaFilters'.
     */
    public String getUseCriteriaFilters() {
        return this.useCriteriaFilters != null ? this.useCriteriaFilters : DEFAULT_USE_CRITERIA_FILTERS;
    }

    /**
     * Method hasInterval.
     * 
     * @return true if at least one Interval has been added
     */
    public boolean hasInterval() {
        return this.interval != null;
    }

    /**
     * Method hasThreads.
     * 
     * @return true if at least one Threads has been added
     */
    public boolean hasThreads() {
        return this.threads != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            interval, 
            threads, 
            service, 
            suppressAdminDownEvent, 
            useCriteriaFilters, 
            nodeOutage, 
            _packageList);
        return hash;
    }

    /**
     * Method iteratePackage.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.snmpinterfacepoller.Package> iteratePackage() {
        return this._packageList.iterator();
    }

    /**
     */
    public void removeAllPackage() {
        this._packageList.clear();
    }

    /**
     * Method removePackage.
     * 
     * @param vPackage
     * @return true if the object was removed from the collection.
     */
    public boolean removePackage(final org.opennms.netmgt.config.snmpinterfacepoller.Package vPackage) {
        boolean removed = _packageList.remove(vPackage);
        return removed;
    }

    /**
     * Method removePackageAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.snmpinterfacepoller.Package removePackageAt(final int index) {
        Object obj = this._packageList.remove(index);
        return (org.opennms.netmgt.config.snmpinterfacepoller.Package) obj;
    }

    /**
     * Sets the value of field 'interval'. The field 'interval' has the following
     * description: Default Interval at which the interfaces are to be
     *  polled
     * 
     * @param interval the value of field 'interval'.
     */
    public void setInterval(final Long interval) {
        this.interval = interval;
    }

    /**
     * Sets the value of field 'nodeOutage'. The field 'nodeOutage' has the
     * following description: Configuration of node-outage
     *  functionality
     * 
     * @param nodeOutage the value of field 'nodeOutage'.
     */
    public void setNodeOutage(final org.opennms.netmgt.config.snmpinterfacepoller.NodeOutage nodeOutage) {
        this.nodeOutage = nodeOutage;
    }

    /**
     * 
     * 
     * @param index
     * @param vPackage
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setPackage(final int index, final org.opennms.netmgt.config.snmpinterfacepoller.Package vPackage) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._packageList.size()) {
            throw new IndexOutOfBoundsException("setPackage: Index value '" + index + "' not in range [0.." + (this._packageList.size() - 1) + "]");
        }
        
        this._packageList.set(index, vPackage);
    }

    /**
     * 
     * 
     * @param vPackageArray
     */
    public void setPackage(final org.opennms.netmgt.config.snmpinterfacepoller.Package[] vPackageArray) {
        //-- copy array
        _packageList.clear();
        
        for (int i = 0; i < vPackageArray.length; i++) {
                this._packageList.add(vPackageArray[i]);
        }
    }

    /**
     * Sets the value of '_packageList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vPackageList the Vector to copy.
     */
    public void setPackage(final java.util.List<org.opennms.netmgt.config.snmpinterfacepoller.Package> vPackageList) {
        // copy vector
        this._packageList.clear();
        
        this._packageList.addAll(vPackageList);
    }

    /**
     * Sets the value of '_packageList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param _packageList the Vector to set.
     */
    public void setPackageCollection(final java.util.List<org.opennms.netmgt.config.snmpinterfacepoller.Package> _packageList) {
        this._packageList = _packageList;
    }

    /**
     * Sets the value of field 'service'. The field 'service' has the following
     * description: The SNMP service string usually 'SNMP'.
     * 
     * @param service the value of field 'service'.
     */
    public void setService(final String service) {
        this.service = service;
    }

    /**
     * Sets the value of field 'suppressAdminDownEvent'. The field
     * 'suppressAdminDownEvent' has the following description: Flag which
     * indicates to suppress Admin Status events at all.
     *  This is deprecated and will be ignored in the code!
     *  
     * 
     * @param suppressAdminDownEvent the value of field 'suppressAdminDownEvent'.
     */
    public void setSuppressAdminDownEvent(final String suppressAdminDownEvent) {
        this.suppressAdminDownEvent = suppressAdminDownEvent;
    }

    /**
     * Sets the value of field 'threads'. The field 'threads' has the following
     * description: The maximum number of threads used for
     *  snmp polling.
     * 
     * @param threads the value of field 'threads'.
     */
    public void setThreads(final Integer threads) {
        this.threads = threads;
    }

    /**
     * Sets the value of field 'useCriteriaFilters'. The field
     * 'useCriteriaFilters' has the following description: Flag which indicates if
     * the filters defined on packages and interface
     *  criterias must be used to select the SNMP interfaces to be tracked by the
     * poller
     *  instead of do this selection through requisition policies.
     *  
     * 
     * @param useCriteriaFilters the value of field 'useCriteriaFilters'.
     */
    public void setUseCriteriaFilters(final String useCriteriaFilters) {
        this.useCriteriaFilters = useCriteriaFilters;
    }

}
