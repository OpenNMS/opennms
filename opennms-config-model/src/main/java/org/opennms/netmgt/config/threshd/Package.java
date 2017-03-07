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

package org.opennms.netmgt.config.threshd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Package encapsulating addresses eligible for
 *  thresholding.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "package")
@XmlAccessorType(XmlAccessType.FIELD)
public class Package implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Name or identifier for this package
     */
    @XmlAttribute(name = "name", required = true)
    private String name;

    /**
     * A rule which adresses belonging to this package
     *  must pass. This package is applied only to addresses that pass
     *  this filter
     */
    @XmlElement(name = "filter", required = true)
    private Filter filter;

    /**
     * Adresses in this package
     */
    @XmlElement(name = "specific")
    private List<String> specificList = new ArrayList<>();

    /**
     * Range of adresses in this package
     */
    @XmlElement(name = "include-range")
    private List<IncludeRange> includeRangeList = new ArrayList<>();

    /**
     * Range of adresses to be excluded from this
     *  package
     */
    @XmlElement(name = "exclude-range")
    private List<ExcludeRange> excludeRangeList = new ArrayList<>();

    /**
     * A file URL holding specific addresses to be polled.
     *  Each line in the URL file can be one of:
     *  <IP><space>#<comments>, or <IP>, or
     *  #<comments>. Lines starting with a '#' are ignored and so
     *  are characters after a '<space>#' in a line.
     */
    @XmlElement(name = "include-url")
    private List<String> includeUrlList = new ArrayList<>();

    /**
     * Services for which thresholding is to occur in this
     *  package
     */
    @XmlElement(name = "service")
    private List<Service> serviceList = new ArrayList<>();

    /**
     * Scheduled outages. Thresholding is not performed
     *  during scheduled outages.
     */
    @XmlElement(name = "outage-calendar")
    private List<String> outageCalendarList = new ArrayList<>();

    public Package() { }

    /**
     * 
     * 
     * @param vExcludeRange
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addExcludeRange(final ExcludeRange vExcludeRange) throws IndexOutOfBoundsException {
        this.excludeRangeList.add(vExcludeRange);
    }

    /**
     * 
     * 
     * @param index
     * @param vExcludeRange
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addExcludeRange(final int index, final ExcludeRange vExcludeRange) throws IndexOutOfBoundsException {
        this.excludeRangeList.add(index, vExcludeRange);
    }

    /**
     * 
     * 
     * @param vIncludeRange
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addIncludeRange(final IncludeRange vIncludeRange) throws IndexOutOfBoundsException {
        this.includeRangeList.add(vIncludeRange);
    }

    /**
     * 
     * 
     * @param index
     * @param vIncludeRange
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addIncludeRange(final int index, final IncludeRange vIncludeRange) throws IndexOutOfBoundsException {
        this.includeRangeList.add(index, vIncludeRange);
    }

    /**
     * 
     * 
     * @param vIncludeUrl
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addIncludeUrl(final String vIncludeUrl) throws IndexOutOfBoundsException {
        this.includeUrlList.add(vIncludeUrl);
    }

    /**
     * 
     * 
     * @param index
     * @param vIncludeUrl
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addIncludeUrl(final int index, final String vIncludeUrl) throws IndexOutOfBoundsException {
        this.includeUrlList.add(index, vIncludeUrl);
    }

    /**
     * 
     * 
     * @param vOutageCalendar
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addOutageCalendar(final String vOutageCalendar) throws IndexOutOfBoundsException {
        this.outageCalendarList.add(vOutageCalendar);
    }

    /**
     * 
     * 
     * @param index
     * @param vOutageCalendar
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addOutageCalendar(final int index, final String vOutageCalendar) throws IndexOutOfBoundsException {
        this.outageCalendarList.add(index, vOutageCalendar);
    }

    /**
     * 
     * 
     * @param vService
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addService(final Service vService) throws IndexOutOfBoundsException {
        this.serviceList.add(vService);
    }

    /**
     * 
     * 
     * @param index
     * @param vService
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addService(final int index, final Service vService) throws IndexOutOfBoundsException {
        this.serviceList.add(index, vService);
    }

    /**
     * 
     * 
     * @param vSpecific
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addSpecific(final String vSpecific) throws IndexOutOfBoundsException {
        this.specificList.add(vSpecific);
    }

    /**
     * 
     * 
     * @param index
     * @param vSpecific
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addSpecific(final int index, final String vSpecific) throws IndexOutOfBoundsException {
        this.specificList.add(index, vSpecific);
    }

    /**
     * Method enumerateExcludeRange.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<ExcludeRange> enumerateExcludeRange() {
        return Collections.enumeration(this.excludeRangeList);
    }

    /**
     * Method enumerateIncludeRange.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<IncludeRange> enumerateIncludeRange() {
        return Collections.enumeration(this.includeRangeList);
    }

    /**
     * Method enumerateIncludeUrl.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<String> enumerateIncludeUrl() {
        return Collections.enumeration(this.includeUrlList);
    }

    /**
     * Method enumerateOutageCalendar.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<String> enumerateOutageCalendar() {
        return Collections.enumeration(this.outageCalendarList);
    }

    /**
     * Method enumerateService.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Service> enumerateService() {
        return Collections.enumeration(this.serviceList);
    }

    /**
     * Method enumerateSpecific.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<String> enumerateSpecific() {
        return Collections.enumeration(this.specificList);
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
        
        if (obj instanceof Package) {
            Package temp = (Package)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.filter, filter)
                && Objects.equals(temp.specificList, specificList)
                && Objects.equals(temp.includeRangeList, includeRangeList)
                && Objects.equals(temp.excludeRangeList, excludeRangeList)
                && Objects.equals(temp.includeUrlList, includeUrlList)
                && Objects.equals(temp.serviceList, serviceList)
                && Objects.equals(temp.outageCalendarList, outageCalendarList);
            return equals;
        }
        return false;
    }

    /**
     * Method getExcludeRange.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the ExcludeRange at
     * the given index
     */
    public ExcludeRange getExcludeRange(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.excludeRangeList.size()) {
            throw new IndexOutOfBoundsException("getExcludeRange: Index value '" + index + "' not in range [0.." + (this.excludeRangeList.size() - 1) + "]");
        }
        
        return (ExcludeRange) excludeRangeList.get(index);
    }

    /**
     * Method getExcludeRange.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public ExcludeRange[] getExcludeRange() {
        ExcludeRange[] array = new ExcludeRange[0];
        return (ExcludeRange[]) this.excludeRangeList.toArray(array);
    }

    /**
     * Method getExcludeRangeCollection.Returns a reference to 'excludeRangeList'.
     * No type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<ExcludeRange> getExcludeRangeCollection() {
        return this.excludeRangeList;
    }

    /**
     * Method getExcludeRangeCount.
     * 
     * @return the size of this collection
     */
    public int getExcludeRangeCount() {
        return this.excludeRangeList.size();
    }

    /**
     * Returns the value of field 'filter'. The field 'filter' has the following
     * description: A rule which adresses belonging to this package
     *  must pass. This package is applied only to addresses that pass
     *  this filter
     * 
     * @return the value of field 'Filter'.
     */
    public Filter getFilter() {
        return this.filter;
    }

    /**
     * Method getIncludeRange.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the IncludeRange at
     * the given index
     */
    public IncludeRange getIncludeRange(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.includeRangeList.size()) {
            throw new IndexOutOfBoundsException("getIncludeRange: Index value '" + index + "' not in range [0.." + (this.includeRangeList.size() - 1) + "]");
        }
        
        return (IncludeRange) includeRangeList.get(index);
    }

    /**
     * Method getIncludeRange.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public IncludeRange[] getIncludeRange() {
        IncludeRange[] array = new IncludeRange[0];
        return (IncludeRange[]) this.includeRangeList.toArray(array);
    }

    /**
     * Method getIncludeRangeCollection.Returns a reference to 'includeRangeList'.
     * No type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<IncludeRange> getIncludeRangeCollection() {
        return this.includeRangeList;
    }

    /**
     * Method getIncludeRangeCount.
     * 
     * @return the size of this collection
     */
    public int getIncludeRangeCount() {
        return this.includeRangeList.size();
    }

    /**
     * Method getIncludeUrl.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getIncludeUrl(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.includeUrlList.size()) {
            throw new IndexOutOfBoundsException("getIncludeUrl: Index value '" + index + "' not in range [0.." + (this.includeUrlList.size() - 1) + "]");
        }
        
        return (String) includeUrlList.get(index);
    }

    /**
     * Method getIncludeUrl.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public String[] getIncludeUrl() {
        String[] array = new String[0];
        return (String[]) this.includeUrlList.toArray(array);
    }

    /**
     * Method getIncludeUrlCollection.Returns a reference to 'includeUrlList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getIncludeUrlCollection() {
        return this.includeUrlList;
    }

    /**
     * Method getIncludeUrlCount.
     * 
     * @return the size of this collection
     */
    public int getIncludeUrlCount() {
        return this.includeUrlList.size();
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the following
     * description: Name or identifier for this package
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Method getOutageCalendar.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getOutageCalendar(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.outageCalendarList.size()) {
            throw new IndexOutOfBoundsException("getOutageCalendar: Index value '" + index + "' not in range [0.." + (this.outageCalendarList.size() - 1) + "]");
        }
        
        return (String) outageCalendarList.get(index);
    }

    /**
     * Method getOutageCalendar.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are changing in
     * another thread, we pass a 0-length Array of the correct type into the API
     * call.  This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public String[] getOutageCalendar() {
        String[] array = new String[0];
        return (String[]) this.outageCalendarList.toArray(array);
    }

    /**
     * Method getOutageCalendarCollection.Returns a reference to
     * 'outageCalendarList'. No type checking is performed on any modifications to
     * the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getOutageCalendarCollection() {
        return this.outageCalendarList;
    }

    /**
     * Method getOutageCalendarCount.
     * 
     * @return the size of this collection
     */
    public int getOutageCalendarCount() {
        return this.outageCalendarList.size();
    }

    /**
     * Method getService.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Service at the
     * given index
     */
    public Service getService(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.serviceList.size()) {
            throw new IndexOutOfBoundsException("getService: Index value '" + index + "' not in range [0.." + (this.serviceList.size() - 1) + "]");
        }
        
        return (Service) serviceList.get(index);
    }

    /**
     * Method getService.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Service[] getService() {
        Service[] array = new Service[0];
        return (Service[]) this.serviceList.toArray(array);
    }

    /**
     * Method getServiceCollection.Returns a reference to 'serviceList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Service> getServiceCollection() {
        return this.serviceList;
    }

    /**
     * Method getServiceCount.
     * 
     * @return the size of this collection
     */
    public int getServiceCount() {
        return this.serviceList.size();
    }

    /**
     * Method getSpecific.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getSpecific(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.specificList.size()) {
            throw new IndexOutOfBoundsException("getSpecific: Index value '" + index + "' not in range [0.." + (this.specificList.size() - 1) + "]");
        }
        
        return (String) specificList.get(index);
    }

    /**
     * Method getSpecific.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public String[] getSpecific() {
        String[] array = new String[0];
        return (String[]) this.specificList.toArray(array);
    }

    /**
     * Method getSpecificCollection.Returns a reference to 'specificList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getSpecificCollection() {
        return this.specificList;
    }

    /**
     * Method getSpecificCount.
     * 
     * @return the size of this collection
     */
    public int getSpecificCount() {
        return this.specificList.size();
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
            filter, 
            specificList, 
            includeRangeList, 
            excludeRangeList, 
            includeUrlList, 
            serviceList, 
            outageCalendarList);
        return hash;
    }

    /**
     * Method iterateExcludeRange.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<ExcludeRange> iterateExcludeRange() {
        return this.excludeRangeList.iterator();
    }

    /**
     * Method iterateIncludeRange.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<IncludeRange> iterateIncludeRange() {
        return this.includeRangeList.iterator();
    }

    /**
     * Method iterateIncludeUrl.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<String> iterateIncludeUrl() {
        return this.includeUrlList.iterator();
    }

    /**
     * Method iterateOutageCalendar.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<String> iterateOutageCalendar() {
        return this.outageCalendarList.iterator();
    }

    /**
     * Method iterateService.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Service> iterateService() {
        return this.serviceList.iterator();
    }

    /**
     * Method iterateSpecific.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<String> iterateSpecific() {
        return this.specificList.iterator();
    }

    /**
     */
    public void removeAllExcludeRange() {
        this.excludeRangeList.clear();
    }

    /**
     */
    public void removeAllIncludeRange() {
        this.includeRangeList.clear();
    }

    /**
     */
    public void removeAllIncludeUrl() {
        this.includeUrlList.clear();
    }

    /**
     */
    public void removeAllOutageCalendar() {
        this.outageCalendarList.clear();
    }

    /**
     */
    public void removeAllService() {
        this.serviceList.clear();
    }

    /**
     */
    public void removeAllSpecific() {
        this.specificList.clear();
    }

    /**
     * Method removeExcludeRange.
     * 
     * @param vExcludeRange
     * @return true if the object was removed from the collection.
     */
    public boolean removeExcludeRange(final ExcludeRange vExcludeRange) {
        boolean removed = excludeRangeList.remove(vExcludeRange);
        return removed;
    }

    /**
     * Method removeExcludeRangeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public ExcludeRange removeExcludeRangeAt(final int index) {
        Object obj = this.excludeRangeList.remove(index);
        return (ExcludeRange) obj;
    }

    /**
     * Method removeIncludeRange.
     * 
     * @param vIncludeRange
     * @return true if the object was removed from the collection.
     */
    public boolean removeIncludeRange(final IncludeRange vIncludeRange) {
        boolean removed = includeRangeList.remove(vIncludeRange);
        return removed;
    }

    /**
     * Method removeIncludeRangeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public IncludeRange removeIncludeRangeAt(final int index) {
        Object obj = this.includeRangeList.remove(index);
        return (IncludeRange) obj;
    }

    /**
     * Method removeIncludeUrl.
     * 
     * @param vIncludeUrl
     * @return true if the object was removed from the collection.
     */
    public boolean removeIncludeUrl(final String vIncludeUrl) {
        boolean removed = includeUrlList.remove(vIncludeUrl);
        return removed;
    }

    /**
     * Method removeIncludeUrlAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeIncludeUrlAt(final int index) {
        Object obj = this.includeUrlList.remove(index);
        return (String) obj;
    }

    /**
     * Method removeOutageCalendar.
     * 
     * @param vOutageCalendar
     * @return true if the object was removed from the collection.
     */
    public boolean removeOutageCalendar(final String vOutageCalendar) {
        boolean removed = outageCalendarList.remove(vOutageCalendar);
        return removed;
    }

    /**
     * Method removeOutageCalendarAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeOutageCalendarAt(final int index) {
        Object obj = this.outageCalendarList.remove(index);
        return (String) obj;
    }

    /**
     * Method removeService.
     * 
     * @param vService
     * @return true if the object was removed from the collection.
     */
    public boolean removeService(final Service vService) {
        boolean removed = serviceList.remove(vService);
        return removed;
    }

    /**
     * Method removeServiceAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Service removeServiceAt(final int index) {
        Object obj = this.serviceList.remove(index);
        return (Service) obj;
    }

    /**
     * Method removeSpecific.
     * 
     * @param vSpecific
     * @return true if the object was removed from the collection.
     */
    public boolean removeSpecific(final String vSpecific) {
        boolean removed = specificList.remove(vSpecific);
        return removed;
    }

    /**
     * Method removeSpecificAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeSpecificAt(final int index) {
        Object obj = this.specificList.remove(index);
        return (String) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vExcludeRange
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setExcludeRange(final int index, final ExcludeRange vExcludeRange) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.excludeRangeList.size()) {
            throw new IndexOutOfBoundsException("setExcludeRange: Index value '" + index + "' not in range [0.." + (this.excludeRangeList.size() - 1) + "]");
        }
        
        this.excludeRangeList.set(index, vExcludeRange);
    }

    /**
     * 
     * 
     * @param vExcludeRangeArray
     */
    public void setExcludeRange(final ExcludeRange[] vExcludeRangeArray) {
        //-- copy array
        excludeRangeList.clear();
        
        for (int i = 0; i < vExcludeRangeArray.length; i++) {
                this.excludeRangeList.add(vExcludeRangeArray[i]);
        }
    }

    /**
     * Sets the value of 'excludeRangeList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vExcludeRangeList the Vector to copy.
     */
    public void setExcludeRange(final List<ExcludeRange> vExcludeRangeList) {
        // copy vector
        this.excludeRangeList.clear();
        
        this.excludeRangeList.addAll(vExcludeRangeList);
    }

    /**
     * Sets the value of 'excludeRangeList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param excludeRangeList the Vector to set.
     */
    public void setExcludeRangeCollection(final List<ExcludeRange> excludeRangeList) {
        this.excludeRangeList = excludeRangeList;
    }

    /**
     * Sets the value of field 'filter'. The field 'filter' has the following
     * description: A rule which adresses belonging to this package
     *  must pass. This package is applied only to addresses that pass
     *  this filter
     * 
     * @param filter the value of field 'filter'.
     */
    public void setFilter(final Filter filter) {
        this.filter = filter;
    }

    /**
     * 
     * 
     * @param index
     * @param vIncludeRange
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setIncludeRange(final int index, final IncludeRange vIncludeRange) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.includeRangeList.size()) {
            throw new IndexOutOfBoundsException("setIncludeRange: Index value '" + index + "' not in range [0.." + (this.includeRangeList.size() - 1) + "]");
        }
        
        this.includeRangeList.set(index, vIncludeRange);
    }

    /**
     * 
     * 
     * @param vIncludeRangeArray
     */
    public void setIncludeRange(final IncludeRange[] vIncludeRangeArray) {
        //-- copy array
        includeRangeList.clear();
        
        for (int i = 0; i < vIncludeRangeArray.length; i++) {
                this.includeRangeList.add(vIncludeRangeArray[i]);
        }
    }

    /**
     * Sets the value of 'includeRangeList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vIncludeRangeList the Vector to copy.
     */
    public void setIncludeRange(final List<IncludeRange> vIncludeRangeList) {
        // copy vector
        this.includeRangeList.clear();
        
        this.includeRangeList.addAll(vIncludeRangeList);
    }

    /**
     * Sets the value of 'includeRangeList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param includeRangeList the Vector to set.
     */
    public void setIncludeRangeCollection(final List<IncludeRange> includeRangeList) {
        this.includeRangeList = includeRangeList;
    }

    /**
     * 
     * 
     * @param index
     * @param vIncludeUrl
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setIncludeUrl(final int index, final String vIncludeUrl) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.includeUrlList.size()) {
            throw new IndexOutOfBoundsException("setIncludeUrl: Index value '" + index + "' not in range [0.." + (this.includeUrlList.size() - 1) + "]");
        }
        
        this.includeUrlList.set(index, vIncludeUrl);
    }

    /**
     * 
     * 
     * @param vIncludeUrlArray
     */
    public void setIncludeUrl(final String[] vIncludeUrlArray) {
        //-- copy array
        includeUrlList.clear();
        
        for (int i = 0; i < vIncludeUrlArray.length; i++) {
                this.includeUrlList.add(vIncludeUrlArray[i]);
        }
    }

    /**
     * Sets the value of 'includeUrlList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vIncludeUrlList the Vector to copy.
     */
    public void setIncludeUrl(final List<String> vIncludeUrlList) {
        // copy vector
        this.includeUrlList.clear();
        
        this.includeUrlList.addAll(vIncludeUrlList);
    }

    /**
     * Sets the value of 'includeUrlList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param includeUrlList the Vector to set.
     */
    public void setIncludeUrlCollection(final List<String> includeUrlList) {
        this.includeUrlList = includeUrlList;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the following
     * description: Name or identifier for this package
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * 
     * 
     * @param index
     * @param vOutageCalendar
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setOutageCalendar(final int index, final String vOutageCalendar) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.outageCalendarList.size()) {
            throw new IndexOutOfBoundsException("setOutageCalendar: Index value '" + index + "' not in range [0.." + (this.outageCalendarList.size() - 1) + "]");
        }
        
        this.outageCalendarList.set(index, vOutageCalendar);
    }

    /**
     * 
     * 
     * @param vOutageCalendarArray
     */
    public void setOutageCalendar(final String[] vOutageCalendarArray) {
        //-- copy array
        outageCalendarList.clear();
        
        for (int i = 0; i < vOutageCalendarArray.length; i++) {
                this.outageCalendarList.add(vOutageCalendarArray[i]);
        }
    }

    /**
     * Sets the value of 'outageCalendarList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vOutageCalendarList the Vector to copy.
     */
    public void setOutageCalendar(final List<String> vOutageCalendarList) {
        // copy vector
        this.outageCalendarList.clear();
        
        this.outageCalendarList.addAll(vOutageCalendarList);
    }

    /**
     * Sets the value of 'outageCalendarList' by setting it to the given Vector.
     * No type checking is performed.
     * @deprecated
     * 
     * @param outageCalendarList the Vector to set.
     */
    public void setOutageCalendarCollection(final List<String> outageCalendarList) {
        this.outageCalendarList = outageCalendarList;
    }

    /**
     * 
     * 
     * @param index
     * @param vService
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setService(final int index, final Service vService) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.serviceList.size()) {
            throw new IndexOutOfBoundsException("setService: Index value '" + index + "' not in range [0.." + (this.serviceList.size() - 1) + "]");
        }
        
        this.serviceList.set(index, vService);
    }

    /**
     * 
     * 
     * @param vServiceArray
     */
    public void setService(final Service[] vServiceArray) {
        //-- copy array
        serviceList.clear();
        
        for (int i = 0; i < vServiceArray.length; i++) {
                this.serviceList.add(vServiceArray[i]);
        }
    }

    /**
     * Sets the value of 'serviceList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vServiceList the Vector to copy.
     */
    public void setService(final List<Service> vServiceList) {
        // copy vector
        this.serviceList.clear();
        
        this.serviceList.addAll(vServiceList);
    }

    /**
     * Sets the value of 'serviceList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param serviceList the Vector to set.
     */
    public void setServiceCollection(final List<Service> serviceList) {
        this.serviceList = serviceList;
    }

    /**
     * 
     * 
     * @param index
     * @param vSpecific
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setSpecific(final int index, final String vSpecific) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.specificList.size()) {
            throw new IndexOutOfBoundsException("setSpecific: Index value '" + index + "' not in range [0.." + (this.specificList.size() - 1) + "]");
        }
        
        this.specificList.set(index, vSpecific);
    }

    /**
     * 
     * 
     * @param vSpecificArray
     */
    public void setSpecific(final String[] vSpecificArray) {
        //-- copy array
        specificList.clear();
        
        for (int i = 0; i < vSpecificArray.length; i++) {
                this.specificList.add(vSpecificArray[i]);
        }
    }

    /**
     * Sets the value of 'specificList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vSpecificList the Vector to copy.
     */
    public void setSpecific(final List<String> vSpecificList) {
        // copy vector
        this.specificList.clear();
        
        this.specificList.addAll(vSpecificList);
    }

    /**
     * Sets the value of 'specificList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param specificList the Vector to set.
     */
    public void setSpecificCollection(final List<String> specificList) {
        this.specificList = specificList;
    }

}
