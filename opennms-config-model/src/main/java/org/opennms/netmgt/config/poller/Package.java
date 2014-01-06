/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.poller;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.xml.sax.ContentHandler;

/**
 * Package encapsulating addresses, services to be polled
 *  for these addresses, etc..
 */

@XmlRootElement(name="package")
@XmlAccessorType(XmlAccessType.FIELD)
public class Package implements Serializable {
    private static final long serialVersionUID = -2212828908084375720L;

    private static final Downtime[] EMPTY_DOWNTIME_LIST = new Downtime[0];
    private static final Service[] EMPTY_SERVICE_LIST = new Service[0];
    private static final ExcludeRange[] EMPTY_EXCLUDE_RANGE_LIST = new ExcludeRange[0];
    private static final IncludeRange[] EMPTY_INCLUDE_RANGE_LIST = new IncludeRange[0];
    private static final String[] EMPTY_STRING_LIST = new String[0];

    /**
     * Name or identifier for this package.
     */
    @XmlAttribute(name="name")
    private String m_name;

    /**
     * Boolean representing whether this is a package for a remote
     * location monitor.
     *  If true, this package will be ignored by the OpenNMS daemon
     * poller.
     *  
     */
    @XmlAttribute(name="remote")
    private Boolean m_remote;

    /**
     * A rule which adresses belonging to this package
     *  must pass. This package is applied only to addresses that
     * pass
     *  this filter.
     */
    @XmlElement(name="filter")
    private Filter m_filter;

    /**
     * Addresses in this package
     */
    @XmlElement(name="specific")
    private List<String> m_specifics = new ArrayList<String>();

    /**
     * Range of addresses in this package.
     */
    @XmlElement(name="include-range")
    private List<IncludeRange> m_includeRanges = new ArrayList<IncludeRange>();

    /**
     * Range of addresses to be excluded from this
     *  package.
     */
    @XmlElement(name="exclude-range")
    private List<ExcludeRange> m_excludeRanges = new ArrayList<ExcludeRange>();

    /**
     * A file URL holding specific addresses to be polled.
     *  Each line in the URL file can be one of:
     *  <IP><space>#<comments> or <IP> or
     *  #<comments>. Lines starting with a '#' are ignored and so
     *  are characters after a '<space>#' in a line.
     */
    @XmlElement(name="include-url")
    private List<String> m_includeUrls = new ArrayList<String>();

    /**
     * RRD parameters for response time
     *  data.
     */
    @XmlElement(name="rrd")
    private Rrd m_rrd;

    /**
     * Services to be polled for addresses belonging to
     *  this package.
     */
    @XmlElement(name="service")
    private List<Service> m_services = new ArrayList<Service>();

    /**
     * Scheduled outages. If a service is found down
     *  during this period, it is not reported as down.
     */
    @XmlElement(name="outage-calendar")
    private List<String> m_outageCalendars = new ArrayList<String>();

    /**
     * Downtime model. Determines the rate at which
     *  addresses are to be polled when they remain down for
     * extended
     *  periods.
     */
    @XmlElement(name="downtime")
    private List<Downtime> m_downtimes = new ArrayList<Downtime>();


      //----------------/
     //- Constructors -/
    //----------------/

    public Package() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    public Package(final String name) {
        this();
        setName(name);
    }


    /**
     * 
     * 
     * @param downtime
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addDowntime(final Downtime downtime) throws IndexOutOfBoundsException {
        m_downtimes.add(downtime);
    }

    /**
     * 
     * 
     * @param index
     * @param downtime
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addDowntime(final int index, final Downtime downtime) throws IndexOutOfBoundsException {
        m_downtimes.add(index, downtime);
    }

    /**
     * 
     * 
     * @param excludeRange
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addExcludeRange(final ExcludeRange excludeRange) throws IndexOutOfBoundsException {
        m_excludeRanges.add(excludeRange);
    }

    /**
     * 
     * 
     * @param index
     * @param excludeRange
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addExcludeRange(final int index, final ExcludeRange excludeRange) throws IndexOutOfBoundsException {
        m_excludeRanges.add(index, excludeRange);
    }

    /**
     * 
     * 
     * @param includeRange
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeRange(final IncludeRange includeRange) throws IndexOutOfBoundsException {
        m_includeRanges.add(includeRange);
    }

    /**
     * 
     * 
     * @param index
     * @param includeRange
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeRange(final int index, final IncludeRange includeRange) throws IndexOutOfBoundsException {
        m_includeRanges.add(index, includeRange);
    }

    public void addIncludeRange(final String begin, final String end) {
        addIncludeRange(new IncludeRange(begin, end));
    }

    /**
     * 
     * 
     * @param includeUrl
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeUrl(final String includeUrl) throws IndexOutOfBoundsException {
        m_includeUrls.add(includeUrl);
    }

    /**
     * 
     * 
     * @param index
     * @param includeUrl
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeUrl(final int index, final String includeUrl) throws IndexOutOfBoundsException {
        m_includeUrls.add(index, includeUrl);
    }

    /**
     * 
     * 
     * @param outageCalendar
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addOutageCalendar(final String outageCalendar) throws IndexOutOfBoundsException {
        m_outageCalendars.add(outageCalendar);
    }

    /**
     * 
     * 
     * @param index
     * @param outageCalendar
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addOutageCalendar(final int index, final String outageCalendar) throws IndexOutOfBoundsException {
        m_outageCalendars.add(index, outageCalendar);
    }

    /**
     * 
     * 
     * @param service
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addService(final Service service) throws IndexOutOfBoundsException {
        m_services.add(service);
    }

    /**
     * 
     * 
     * @param index
     * @param service
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addService(final int index, final Service service) throws IndexOutOfBoundsException {
        m_services.add(index, service);
    }

    /**
     * 
     * 
     * @param specific
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSpecific(final String specific) throws IndexOutOfBoundsException {
        m_specifics.add(specific);
    }

    /**
     * 
     * 
     * @param index
     * @param specific
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSpecific(final int index, final String specific) throws IndexOutOfBoundsException {
        m_specifics.add(index, specific);
    }

    /**
     */
    public void deleteRemote() {
        m_remote = null;
    }

    /**
     * Method enumerateDowntime.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Downtime> enumerateDowntime() {
        return Collections.enumeration(m_downtimes);
    }

    /**
     * Method enumerateExcludeRange.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<ExcludeRange> enumerateExcludeRange() {
        return Collections.enumeration(m_excludeRanges);
    }

    /**
     * Method enumerateIncludeRange.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<IncludeRange> enumerateIncludeRange() {
        return Collections.enumeration(m_includeRanges);
    }

    /**
     * Method enumerateIncludeUrl.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateIncludeUrl() {
        return Collections.enumeration(m_includeUrls);
    }

    /**
     * Method enumerateOutageCalendar.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateOutageCalendar() {
        return Collections.enumeration(m_outageCalendars);
    }

    /**
     * Method enumerateService.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Service> enumerateService() {
        return Collections.enumeration(m_services);
    }

    /**
     * Method enumerateSpecific.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateSpecific() {
        return Collections.enumeration(m_specifics);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(
            final Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof Package) {
        
            Package temp = (Package)obj;
            if (m_name != null) {
                if (temp.m_name == null) {
                    return false;
                } else if (!(m_name.equals(temp.m_name))) {
                    return false;
                }
            } else if (temp.m_name != null) {
                return false;
            }
            if (m_remote != null) {
                if (temp.m_remote == null) {
                    return false;
                } else if (!(m_remote.equals(temp.m_remote))) {
                    return false;
                }
            } else if (temp.m_remote != null) {
                return false;
            }
            if (m_filter != null) {
                if (temp.m_filter == null) {
                    return false;
                } else if (!(m_filter.equals(temp.m_filter))) {
                    return false;
                }
            } else if (temp.m_filter != null) {
                return false;
            }
            if (m_specifics != null) {
                if (temp.m_specifics == null) {
                    return false;
                } else if (!(m_specifics.equals(temp.m_specifics))) {
                    return false;
                }
            } else if (temp.m_specifics != null) {
                return false;
            }
            if (m_includeRanges != null) {
                if (temp.m_includeRanges == null) {
                    return false;
                } else if (!(m_includeRanges.equals(temp.m_includeRanges))) {
                    return false;
                }
            } else if (temp.m_includeRanges != null) {
                return false;
            }
            if (m_excludeRanges != null) {
                if (temp.m_excludeRanges == null) {
                    return false;
                } else if (!(m_excludeRanges.equals(temp.m_excludeRanges))) {
                    return false;
                }
            } else if (temp.m_excludeRanges != null) {
                return false;
            }
            if (m_includeUrls != null) {
                if (temp.m_includeUrls == null) {
                    return false;
                } else if (!(m_includeUrls.equals(temp.m_includeUrls))) {
                    return false;
                }
            } else if (temp.m_includeUrls != null) {
                return false;
            }
            if (m_rrd != null) {
                if (temp.m_rrd == null) {
                    return false;
                } else if (!(m_rrd.equals(temp.m_rrd))) {
                    return false;
                }
            } else if (temp.m_rrd != null) {
                return false;
            }
            if (m_services != null) {
                if (temp.m_services == null) {
                    return false;
                } else if (!(m_services.equals(temp.m_services))) {
                    return false;
                }
            } else if (temp.m_services != null) {
                return false;
            }
            if (m_outageCalendars != null) {
                if (temp.m_outageCalendars == null) {
                    return false;
                } else if (!(m_outageCalendars.equals(temp.m_outageCalendars))) {
                    return false;
                }
            } else if (temp.m_outageCalendars != null) {
                return false;
            }
            if (m_downtimes != null) {
                if (temp.m_downtimes == null) {
                    return false;
                } else if (!(m_downtimes.equals(temp.m_downtimes))) {
                    return false;
                }
            } else if (temp.m_downtimes != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Method getDowntime.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Downtime at the given index
     */
    public Downtime getDowntime(final int index) throws IndexOutOfBoundsException {
        return m_downtimes.get(index);
    }

    /**
     * Method getDowntime.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Downtime[] getDowntime() {
        return m_downtimes.toArray(EMPTY_DOWNTIME_LIST);
    }

    /**
     * Method getDowntimeCollection.Returns a reference to
     * 'm_downtimes'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Downtime> getDowntimeCollection() {
        return new ArrayList<Downtime>(m_downtimes);
    }

    /**
     * Method getDowntimeCount.
     * 
     * @return the size of this collection
     */
    public int getDowntimeCount() {
        return m_downtimes.size();
    }

    /**
     * Method getExcludeRange.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * ExcludeRange at the given
     * index
     */
    public ExcludeRange getExcludeRange(final int index) throws IndexOutOfBoundsException {
        return m_excludeRanges.get(index);
    }

    /**
     * Method getExcludeRange.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public ExcludeRange[] getExcludeRange() {
        return m_excludeRanges.toArray(EMPTY_EXCLUDE_RANGE_LIST);
    }

    /**
     * Method getExcludeRangeCollection.Returns a reference to
     * 'm_excludeRanges'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<ExcludeRange> getExcludeRangeCollection() {
        return new ArrayList<ExcludeRange>(m_excludeRanges);
    }

    /**
     * Method getExcludeRangeCount.
     * 
     * @return the size of this collection
     */
    public int getExcludeRangeCount() {
        return m_excludeRanges.size();
    }

    /**
     * Returns the value of field 'filter'. The field 'filter' has
     * the following description: A rule which adresses belonging
     * to this package
     *  must pass. This package is applied only to addresses that
     * pass
     *  this filter.
     * 
     * @return the value of field 'Filter'.
     */
    public Filter getFilter() {
        return m_filter;
    }

    /**
     * Method getIncludeRange.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * IncludeRange at the given
     * index
     */
    public IncludeRange getIncludeRange(final int index) throws IndexOutOfBoundsException {
        return m_includeRanges.get(index);
    }

    /**
     * Method getIncludeRange.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public IncludeRange[] getIncludeRange() {
        return m_includeRanges.toArray(EMPTY_INCLUDE_RANGE_LIST);
    }

    /**
     * Method getIncludeRangeCollection.Returns a reference to
     * 'm_includeRanges'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<IncludeRange> getIncludeRangeCollection() {
        return new ArrayList<IncludeRange>(m_includeRanges);
    }

    /**
     * Method getIncludeRangeCount.
     * 
     * @return the size of this collection
     */
    public int getIncludeRangeCount() {
        return m_includeRanges.size();
    }

    /**
     * Method getIncludeUrl.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getIncludeUrl(final int index) throws IndexOutOfBoundsException {
        return m_includeUrls.get(index);
    }

    /**
     * Method getIncludeUrl.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public String[] getIncludeUrl() {
        return m_includeUrls.toArray(EMPTY_STRING_LIST);
    }

    /**
     * Method getIncludeUrlCollection.Returns a reference to
     * 'm_includeUrls'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getIncludeUrlCollection() {
        return new ArrayList<String>(m_includeUrls);
    }

    /**
     * Method getIncludeUrlCount.
     * 
     * @return the size of this collection
     */
    public int getIncludeUrlCount() {
        return m_includeUrls.size();
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the
     * following description: Name or identifier for this package.
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return m_name;
    }

    /**
     * Method getOutageCalendar.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getOutageCalendar(final int index) throws IndexOutOfBoundsException {
        return m_outageCalendars.get(index);
    }

    /**
     * Method getOutageCalendar.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public String[] getOutageCalendar() {
        return m_outageCalendars.toArray(EMPTY_STRING_LIST);
    }

    /**
     * Method getOutageCalendarCollection.Returns a reference to
     * 'm_outageCalendars'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getOutageCalendarCollection() {
        return new ArrayList<String>(m_outageCalendars);
    }

    /**
     * Method getOutageCalendarCount.
     * 
     * @return the size of this collection
     */
    public int getOutageCalendarCount() {
        return m_outageCalendars.size();
    }

    /**
     * Returns the value of field 'remote'. The field 'remote' has
     * the following description: Boolean represnting whether this
     * is a package for a remote location montior.
     *  If true, this package will be ignored by the OpenNMS daemon
     * poller.
     *  
     * 
     * @return the value of field 'Remote'.
     */
    public boolean getRemote() {
        return m_remote == null? false : m_remote;
    }

    /**
     * Returns the value of field 'rrd'. The field 'rrd' has the
     * following description: RRD parameters for response time
     *  data.
     * 
     * @return the value of field 'Rrd'.
     */
    public Rrd getRrd() {
        return m_rrd;
    }

    /**
     * Method getService.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Service at the given index
     */
    public Service getService(final int index) throws IndexOutOfBoundsException {
        return m_services.get(index);
    }

    /**
     * Method getService.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Service[] getService() {
        return m_services.toArray(EMPTY_SERVICE_LIST);
    }

    /**
     * Method getServiceCollection.Returns a reference to
     * 'm_services'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Service> getServiceCollection() {
        return new ArrayList<Service>(m_services);
    }

    /**
     * Method getServiceCount.
     * 
     * @return the size of this collection
     */
    public int getServiceCount() {
        return m_services.size();
    }

    /**
     * Method getSpecific.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getSpecific(final int index) throws IndexOutOfBoundsException {
        return m_specifics.get(index);
    }

    /**
     * Method getSpecific.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public String[] getSpecific() {
        return m_specifics.toArray(EMPTY_STRING_LIST);
    }

    /**
     * Method getSpecificCollection.Returns a reference to
     * '_specificList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getSpecificCollection() {
        return new ArrayList<String>(m_specifics);
    }

    /**
     * Method getSpecificCount.
     * 
     * @return the size of this collection
     */
    public int getSpecificCount() {
        return m_specifics.size();
    }

    /**
     * Method hasRemote.
     * 
     * @return true if at least one Remote has been added
     */
    public boolean hasRemote() {
        return m_remote != null;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode(
    ) {
        int result = 17;
        
        if (m_name != null) {
           result = 37 * result + m_name.hashCode();
        }
        if (m_remote != null) {
            result = 37 * result + (m_remote?0:1);
        }
        if (m_filter != null) {
           result = 37 * result + m_filter.hashCode();
        }
        if (m_specifics != null) {
           result = 37 * result + m_specifics.hashCode();
        }
        if (m_includeRanges != null) {
           result = 37 * result + m_includeRanges.hashCode();
        }
        if (m_excludeRanges != null) {
           result = 37 * result + m_excludeRanges.hashCode();
        }
        if (m_includeUrls != null) {
           result = 37 * result + m_includeUrls.hashCode();
        }
        if (m_rrd != null) {
           result = 37 * result + m_rrd.hashCode();
        }
        if (m_services != null) {
           result = 37 * result + m_services.hashCode();
        }
        if (m_outageCalendars != null) {
           result = 37 * result + m_outageCalendars.hashCode();
        }
        if (m_downtimes != null) {
           result = 37 * result + m_downtimes.hashCode();
        }
        
        return result;
    }

    /**
     * Returns the value of field 'remote'. The field 'remote' has
     * the following description: Boolean represnting whether this
     * is a package for a remote location montior.
     *  If true, this package will be ignored by the OpenNMS daemon
     * poller.
     *  
     * 
     * @return the value of field 'Remote'.
     */
    public boolean isRemote() {
        return m_remote == null? false : m_remote;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid() {
        try {
            validate();
        } catch (final ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateDowntime.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Downtime> iterateDowntime() {
        return m_downtimes.iterator();
    }

    /**
     * Method iterateExcludeRange.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<ExcludeRange> iterateExcludeRange() {
        return m_excludeRanges.iterator();
    }

    /**
     * Method iterateIncludeRange.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<IncludeRange> iterateIncludeRange() {
        return m_includeRanges.iterator();
    }

    /**
     * Method iterateIncludeUrl.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateIncludeUrl() {
        return m_includeUrls.iterator();
    }

    /**
     * Method iterateOutageCalendar.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateOutageCalendar() {
        return m_outageCalendars.iterator();
    }

    /**
     * Method iterateService.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Service> iterateService() {
        return m_services.iterator();
    }

    /**
     * Method iterateSpecific.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateSpecific() {
        return m_specifics.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws IOException if an IOException occurs during
     * marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllDowntime() {
        m_downtimes.clear();
    }

    /**
     */
    public void removeAllExcludeRange() {
        m_excludeRanges.clear();
    }

    /**
     */
    public void removeAllIncludeRange() {
        m_includeRanges.clear();
    }

    /**
     */
    public void removeAllIncludeUrl() {
        m_includeUrls.clear();
    }

    /**
     */
    public void removeAllOutageCalendar() {
        m_outageCalendars.clear();
    }

    /**
     */
    public void removeAllService() {
        m_services.clear();
    }

    /**
     */
    public void removeAllSpecific() {
        m_specifics.clear();
    }

    /**
     * Method removeDowntime.
     * 
     * @param downtime
     * @return true if the object was removed from the collection.
     */
    public boolean removeDowntime(final Downtime downtime) {
        return m_downtimes.remove(downtime);
    }

    /**
     * Method removeDowntimeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Downtime removeDowntimeAt(final int index) {
        return m_downtimes.remove(index);
    }

    /**
     * Method removeExcludeRange.
     * 
     * @param excludeRange
     * @return true if the object was removed from the collection.
     */
    public boolean removeExcludeRange(final ExcludeRange excludeRange) {
        return m_excludeRanges.remove(excludeRange);
    }

    /**
     * Method removeExcludeRangeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public ExcludeRange removeExcludeRangeAt(final int index) {
        return m_excludeRanges.remove(index);
    }

    /**
     * Method removeIncludeRange.
     * 
     * @param includeRange
     * @return true if the object was removed from the collection.
     */
    public boolean removeIncludeRange(final IncludeRange includeRange) {
        return m_includeRanges.remove(includeRange);
    }

    /**
     * Method removeIncludeRangeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public IncludeRange removeIncludeRangeAt(final int index) {
        return m_includeRanges.remove(index);
    }

    /**
     * Method removeIncludeUrl.
     * 
     * @param includeUrl
     * @return true if the object was removed from the collection.
     */
    public boolean removeIncludeUrl(final String includeUrl) {
        return m_includeUrls.remove(includeUrl);
    }

    /**
     * Method removeIncludeUrlAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeIncludeUrlAt(final int index) {
        return m_includeUrls.remove(index);
    }

    /**
     * Method removeOutageCalendar.
     * 
     * @param outageCalendar
     * @return true if the object was removed from the collection.
     */
    public boolean removeOutageCalendar(final String outageCalendar) {
        return m_outageCalendars.remove(outageCalendar);
    }

    /**
     * Method removeOutageCalendarAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeOutageCalendarAt(final int index) {
        return m_outageCalendars.remove(index);
    }

    /**
     * Method removeService.
     * 
     * @param service
     * @return true if the object was removed from the collection.
     */
    public boolean removeService(final Service service) {
        return m_services.remove(service);
    }

    /**
     * Method removeServiceAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Service removeServiceAt(final int index) {
        return m_services.remove(index);
    }

    /**
     * Method removeSpecific.
     * 
     * @param specific
     * @return true if the object was removed from the collection.
     */
    public boolean removeSpecific(final String specific) {
        return m_specifics.remove(specific);
    }

    /**
     * Method removeSpecificAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeSpecificAt(final int index) {
        return m_specifics.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param downtime
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setDowntime(final int index, final Downtime downtime) throws IndexOutOfBoundsException {
        m_downtimes.set(index, downtime);
    }

    /**
     * 
     * 
     * @param downtimes
     */
    public void setDowntime(final Downtime[] downtimes) {
        m_downtimes.clear();
        for (final Downtime downtime : downtimes) {
            m_downtimes.add(downtime);
        }
    }

    /**
     * Sets the value of 'm_downtimes' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param downtimes the Vector to copy.
     */
    public void setDowntime(final List<Downtime> downtimes) {
        if (downtimes != m_downtimes) {
            m_downtimes.clear();
            m_downtimes.addAll(downtimes);
        }
    }

    /**
     * Sets the value of 'm_downtimes' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param downtimes the Vector to set.
     */
    public void setDowntimeCollection(final List<Downtime> downtimes) {
        m_downtimes = new ArrayList<Downtime>(downtimes);
    }

    /**
     * 
     * 
     * @param index
     * @param excludeRange
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setExcludeRange(final int index, final ExcludeRange excludeRange) throws IndexOutOfBoundsException {
        m_excludeRanges.set(index, excludeRange);
    }

    /**
     * 
     * 
     * @param excludeRanges
     */
    public void setExcludeRange(final ExcludeRange[] excludeRanges) {
        m_excludeRanges.clear();
        for (final ExcludeRange excludeRange : excludeRanges) {
            m_excludeRanges.add(excludeRange);
        }
    }

    /**
     * Sets the value of 'm_excludeRanges' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param excludeRanges the Vector to copy.
     */
    public void setExcludeRange(final List<ExcludeRange> excludeRanges) {
        if (excludeRanges != m_excludeRanges) {
            m_excludeRanges.clear();
            m_excludeRanges.addAll(excludeRanges);
        }
    }

    /**
     * Sets the value of 'm_excludeRanges' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param excludeRanges the Vector to set.
     */
    public void setExcludeRangeCollection(final List<ExcludeRange> excludeRanges) {
        m_excludeRanges = new ArrayList<ExcludeRange>(excludeRanges);
    }

    /**
     * Sets the value of field 'filter'. The field 'filter' has the
     * following description: A rule which adresses belonging to
     * this package
     *  must pass. This package is applied only to addresses that
     * pass
     *  this filter.
     * 
     * @param filter the value of field 'filter'.
     */
    public void setFilter(final Filter filter) {
        m_filter = filter;
    }

    public void setFilter(final String filter) {
        setFilter(new Filter(filter));
    }

    /**
     * 
     * 
     * @param index
     * @param includeRange
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setIncludeRange(final int index, final IncludeRange includeRange) throws IndexOutOfBoundsException {
        m_includeRanges.set(index, includeRange);
    }

    /**
     * 
     * 
     * @param includeRanges
     */
    public void setIncludeRange(final IncludeRange[] includeRanges) {
        m_includeRanges.clear();
        for (final IncludeRange includeRange : includeRanges) {
            m_includeRanges.add(includeRange);
        }
    }

    /**
     * Sets the value of 'm_includeRanges' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param includeRanges the Vector to copy.
     */
    public void setIncludeRange(final List<IncludeRange> includeRanges) {
        if (includeRanges != m_includeRanges) {
            m_includeRanges.clear();
            m_includeRanges.addAll(includeRanges);
        }
    }

    /**
     * Sets the value of 'm_includeRanges' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param includeRanges the Vector to set.
     */
    public void setIncludeRangeCollection(final List<IncludeRange> includeRanges) {
        setIncludeRange(includeRanges);
    }

    /**
     * 
     * 
     * @param index
     * @param includeUrl
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setIncludeUrl(final int index, final String includeUrl) throws IndexOutOfBoundsException {
        m_includeUrls.set(index, includeUrl);
    }

    /**
     * 
     * 
     * @param includeUrls
     */
    public void setIncludeUrl(final String[] includeUrls) {
        m_includeUrls.clear();
        for (final String includeUrl : includeUrls) {
            m_includeUrls.add(includeUrl);
        }
    }

    /**
     * Sets the value of 'm_includeUrls' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param includeUrls the Vector to copy.
     */
    public void setIncludeUrl(final List<String> includeUrls) {
        if (includeUrls != m_includeUrls) {
            m_includeUrls.clear();
            m_includeUrls.addAll(includeUrls);
        }
    }

    /**
     * Sets the value of 'm_includeUrls' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param includeUrls the Vector to set.
     */
    public void setIncludeUrlCollection(final List<String> includeUrls) {
        m_includeUrls = new ArrayList<String>(includeUrls);
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the
     * following description: Name or identifier for this package.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        m_name = name;
    }

    /**
     * 
     * 
     * @param index
     * @param outageCalendar
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setOutageCalendar(final int index, final String outageCalendar) throws IndexOutOfBoundsException {
        m_outageCalendars.set(index, outageCalendar);
    }

    /**
     * 
     * 
     * @param outageCalendars
     */
    public void setOutageCalendar(final String[] outageCalendars) {
        m_outageCalendars.clear();
        for (final String outageCalendar : outageCalendars) {
            m_outageCalendars.add(outageCalendar);
        }
    }

    /**
     * Sets the value of 'm_outageCalendars' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param outageCalendars the Vector to copy.
     */
    public void setOutageCalendar(final List<String> outageCalendars) {
        if (outageCalendars != m_outageCalendars) {
            m_outageCalendars.clear();
            m_outageCalendars.addAll(outageCalendars);
        }
    }

    /**
     * Sets the value of 'm_outageCalendars' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param outageCalendars the Vector to set.
     */
    public void setOutageCalendarCollection(final List<String> outageCalendars) {
        m_outageCalendars = new ArrayList<String>(outageCalendars);
    }

    /**
     * Sets the value of field 'remote'. The field 'remote' has the
     * following description: Boolean represnting whether this is a
     * package for a remote location montior.
     *  If true, this package will be ignored by the OpenNMS daemon
     * poller.
     *  
     * 
     * @param remote the value of field 'remote'.
     */
    public void setRemote(final Boolean remote) {
        m_remote = remote;
    }

    /**
     * Sets the value of field 'rrd'. The field 'rrd' has the
     * following description: RRD parameters for response time
     *  data.
     * 
     * @param rrd the value of field 'rrd'.
     */
    public void setRrd(final Rrd rrd) {
        m_rrd = rrd;
    }

    /**
     * 
     * 
     * @param index
     * @param service
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setService(final int index, final Service service) throws IndexOutOfBoundsException {
        m_services.set(index, service);
    }

    /**
     * 
     * 
     * @param services
     */
    public void setService(final Service[] services) {
        m_services.clear();
        for (final Service service : services) {
            m_services.add(service);
        }
    }

    /**
     * Sets the value of 'm_services' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param services the Vector to copy.
     */
    public void setService(final List<Service> services) {
        if (services != m_services) {
            m_services.clear();
            m_services.addAll(services);
        }
    }

    /**
     * Sets the value of 'm_services' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param services the Vector to set.
     */
    public void setServiceCollection(final List<Service> services) {
        m_services = new ArrayList<Service>(services);
    }

    /**
     * 
     * 
     * @param index
     * @param specific
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setSpecific(final int index, final String specific) throws IndexOutOfBoundsException {
        m_specifics.set(index, specific);
    }

    /**
     * 
     * 
     * @param specifics
     */
    public void setSpecific(final String[] specifics) {
        m_specifics.clear();
        for (final String specific : specifics) {
            m_specifics.add(specific);
        }
    }

    /**
     * Sets the value of '_specificList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param specifics the Vector to copy.
     */
    public void setSpecific(final List<String> specifics) {
        if (specifics != m_specifics) {
            m_specifics.clear();
            m_specifics.addAll(specifics);
        }
    }

    /**
     * Sets the value of '_specificList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param specifics the Vector to set.
     */
    public void setSpecificCollection(final List<String> specifics) {
        m_specifics = new ArrayList<String>(specifics);
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * Package
     */
    public static Package unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Package) Unmarshaller.unmarshal(Package.class, reader);
    }

    /**
     * 
     * 
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate()
    throws ValidationException {
        new Validator().validate(this);
    }

    @Override
    public String toString() {
        return "Package[name=" + m_name +
                ",remote=" + m_remote +
                ",filter=" + m_filter +
                ",specifics=" + m_specifics +
                ",includeRanges=" + m_includeRanges +
                ",excludeRanges=" + m_excludeRanges +
                ",includeUrls=" + m_includeUrls +
                ",rrd=" + m_rrd +
                ",services=" + m_services +
                ",outageCalendars=" + m_outageCalendars +
                ",downtimes=" + m_downtimes +
                "]";
    }
}
