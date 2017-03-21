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

package org.opennms.netmgt.config.rancid.adapter;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This represents a policy to manage a provisioned node
 *  if matched a node will be added updated or deleted using
 *  the element attribute definitions .
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "policy-manage")
@XmlAccessorType(XmlAccessType.FIELD)
public class PolicyManage implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The name of the policy
     *  
     */
    @XmlAttribute(name = "name", required = true)
    private String name;

    /**
     * The time in sec to wait before trying
     *  to set the download flag to up in router.db.
     *  If schedule is there then it is verified if you are able
     *  to write to router.db in rancid.
     *  Otherwise you wait until schedule let you write on rancid.
     *  
     */
    @XmlAttribute(name = "delay")
    private Long delay;

    /**
     * The maximum number of retry before
     *  sending a failure.
     */
    @XmlAttribute(name = "retries")
    private Integer retries;

    /**
     * If you want to use opennms categories
     *  to match rancid device type.
     */
    @XmlAttribute(name = "useCategories")
    private Boolean useCategories;

    /**
     * The Default Rancid type, it is used when no device type
     *  for provisioned node is found.
     *  
     */
    @XmlAttribute(name = "default-type")
    private String defaultType;

    /**
     * Package encapsulating addresses, services to be polled
     *  for these addresses, etc..
     */
    @XmlElement(name = "package", required = true)
    private Package _package;

    /**
     * This is a time when you can schedule set up/down
     *  to rancid
     */
    @XmlElement(name = "schedule")
    private java.util.List<Schedule> scheduleList;

    public PolicyManage() {
        this.scheduleList = new java.util.ArrayList<Schedule>();
    }

    /**
     * 
     * 
     * @param vSchedule
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addSchedule(final Schedule vSchedule) throws IndexOutOfBoundsException {
        this.scheduleList.add(vSchedule);
    }

    /**
     * 
     * 
     * @param index
     * @param vSchedule
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addSchedule(final int index, final Schedule vSchedule) throws IndexOutOfBoundsException {
        this.scheduleList.add(index, vSchedule);
    }

    /**
     */
    public void deleteDelay() {
        this.delay= null;
    }

    /**
     */
    public void deleteRetries() {
        this.retries= null;
    }

    /**
     */
    public void deleteUseCategories() {
        this.useCategories= null;
    }

    /**
     * Method enumerateSchedule.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<Schedule> enumerateSchedule() {
        return java.util.Collections.enumeration(this.scheduleList);
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
        
        if (obj instanceof PolicyManage) {
            PolicyManage temp = (PolicyManage)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.delay, delay)
                && Objects.equals(temp.retries, retries)
                && Objects.equals(temp.useCategories, useCategories)
                && Objects.equals(temp.defaultType, defaultType)
                && Objects.equals(temp._package, _package)
                && Objects.equals(temp.scheduleList, scheduleList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'defaultType'. The field 'defaultType' has the
     * following description: The Default Rancid type, it is used when no device
     * type
     *  for provisioned node is found.
     *  
     * 
     * @return the value of field 'DefaultType'.
     */
    public String getDefaultType() {
        return this.defaultType;
    }

    /**
     * Returns the value of field 'delay'. The field 'delay' has the following
     * description: The time in sec to wait before trying
     *  to set the download flag to up in router.db.
     *  If schedule is there then it is verified if you are able
     *  to write to router.db in rancid.
     *  Otherwise you wait until schedule let you write on rancid.
     *  
     * 
     * @return the value of field 'Delay'.
     */
    public Long getDelay() {
        return this.delay;
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the following
     * description: The name of the policy
     *  
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of field 'package'. The field 'package' has the following
     * description: Package encapsulating addresses, services to be polled
     *  for these addresses, etc..
     * 
     * @return the value of field 'Package'.
     */
    public Package getPackage() {
        return this._package;
    }

    /**
     * Returns the value of field 'retries'. The field 'retries' has the following
     * description: The maximum number of retry before
     *  sending a failure.
     * 
     * @return the value of field 'Retries'.
     */
    public Integer getRetries() {
        return this.retries;
    }

    /**
     * Method getSchedule.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Schedule
     * at the given index
     */
    public Schedule getSchedule(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.scheduleList.size()) {
            throw new IndexOutOfBoundsException("getSchedule: Index value '" + index + "' not in range [0.." + (this.scheduleList.size() - 1) + "]");
        }
        
        return (Schedule) scheduleList.get(index);
    }

    /**
     * Method getSchedule.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Schedule[] getSchedule() {
        Schedule[] array = new Schedule[0];
        return (Schedule[]) this.scheduleList.toArray(array);
    }

    /**
     * Method getScheduleCollection.Returns a reference to 'scheduleList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Schedule> getScheduleCollection() {
        return this.scheduleList;
    }

    /**
     * Method getScheduleCount.
     * 
     * @return the size of this collection
     */
    public int getScheduleCount() {
        return this.scheduleList.size();
    }

    /**
     * Returns the value of field 'useCategories'. The field 'useCategories' has
     * the following description: If you want to use opennms categories
     *  to match rancid device type.
     * 
     * @return the value of field 'UseCategories'.
     */
    public Boolean getUseCategories() {
        return this.useCategories;
    }

    /**
     * Method hasDelay.
     * 
     * @return true if at least one Delay has been added
     */
    public boolean hasDelay() {
        return this.delay != null;
    }

    /**
     * Method hasRetries.
     * 
     * @return true if at least one Retries has been added
     */
    public boolean hasRetries() {
        return this.retries != null;
    }

    /**
     * Method hasUseCategories.
     * 
     * @return true if at least one UseCategories has been added
     */
    public boolean hasUseCategories() {
        return this.useCategories != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            name, 
            delay, 
            retries, 
            useCategories, 
            defaultType, 
            _package, 
            scheduleList);
        return hash;
    }

    /**
     * Returns the value of field 'useCategories'. The field 'useCategories' has
     * the following description: If you want to use opennms categories
     *  to match rancid device type.
     * 
     * @return the value of field 'UseCategories'.
     */
    public Boolean isUseCategories() {
        return this.useCategories;
    }

    /**
     * Method iterateSchedule.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<Schedule> iterateSchedule() {
        return this.scheduleList.iterator();
    }

    /**
     */
    public void removeAllSchedule() {
        this.scheduleList.clear();
    }

    /**
     * Method removeSchedule.
     * 
     * @param vSchedule
     * @return true if the object was removed from the collection.
     */
    public boolean removeSchedule(final Schedule vSchedule) {
        boolean removed = scheduleList.remove(vSchedule);
        return removed;
    }

    /**
     * Method removeScheduleAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Schedule removeScheduleAt(final int index) {
        Object obj = this.scheduleList.remove(index);
        return (Schedule) obj;
    }

    /**
     * Sets the value of field 'defaultType'. The field 'defaultType' has the
     * following description: The Default Rancid type, it is used when no device
     * type
     *  for provisioned node is found.
     *  
     * 
     * @param defaultType the value of field 'defaultType'.
     */
    public void setDefaultType(final String defaultType) {
        this.defaultType = defaultType;
    }

    /**
     * Sets the value of field 'delay'. The field 'delay' has the following
     * description: The time in sec to wait before trying
     *  to set the download flag to up in router.db.
     *  If schedule is there then it is verified if you are able
     *  to write to router.db in rancid.
     *  Otherwise you wait until schedule let you write on rancid.
     *  
     * 
     * @param delay the value of field 'delay'.
     */
    public void setDelay(final Long delay) {
        this.delay = delay;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the following
     * description: The name of the policy
     *  
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the value of field 'package'. The field 'package' has the following
     * description: Package encapsulating addresses, services to be polled
     *  for these addresses, etc..
     * 
     * @param _package
     * @param package the value of field 'package'.
     */
    public void setPackage(final Package _package) {
        this._package = _package;
    }

    /**
     * Sets the value of field 'retries'. The field 'retries' has the following
     * description: The maximum number of retry before
     *  sending a failure.
     * 
     * @param retries the value of field 'retries'.
     */
    public void setRetries(final Integer retries) {
        this.retries = retries;
    }

    /**
     * 
     * 
     * @param index
     * @param vSchedule
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setSchedule(final int index, final Schedule vSchedule) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.scheduleList.size()) {
            throw new IndexOutOfBoundsException("setSchedule: Index value '" + index + "' not in range [0.." + (this.scheduleList.size() - 1) + "]");
        }
        
        this.scheduleList.set(index, vSchedule);
    }

    /**
     * 
     * 
     * @param vScheduleArray
     */
    public void setSchedule(final Schedule[] vScheduleArray) {
        //-- copy array
        scheduleList.clear();
        
        for (int i = 0; i < vScheduleArray.length; i++) {
                this.scheduleList.add(vScheduleArray[i]);
        }
    }

    /**
     * Sets the value of 'scheduleList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vScheduleList the Vector to copy.
     */
    public void setSchedule(final java.util.List<Schedule> vScheduleList) {
        // copy vector
        this.scheduleList.clear();
        
        this.scheduleList.addAll(vScheduleList);
    }

    /**
     * Sets the value of 'scheduleList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param scheduleList the Vector to set.
     */
    public void setScheduleCollection(final java.util.List<Schedule> scheduleList) {
        this.scheduleList = scheduleList;
    }

    /**
     * Sets the value of field 'useCategories'. The field 'useCategories' has the
     * following description: If you want to use opennms categories
     *  to match rancid device type.
     * 
     * @param useCategories the value of field 'useCategories'.
     */
    public void setUseCategories(final Boolean useCategories) {
        this.useCategories = useCategories;
    }

}
