/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.collectd;

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
 * Package encapsulating addresses eligible to have SNMP
 *  data collected from them.
 */

@XmlRootElement(name="package")
@XmlAccessorType(XmlAccessType.FIELD)
public class Package implements Serializable {
    private static final long serialVersionUID = 8238825426693556708L;

    private static final Service[] EMPTY_SERVICE_LIST = new Service[0];
    private static final IncludeRange[] EMPTY_INCLUDERANGE_LIST = new IncludeRange[0];
    private static final ExcludeRange[] EMPTY_EXCLUDERANGE_LIST = new ExcludeRange[0];
    private static final String[] EMPTY_STRING_LIST = new String[0];

    /**
     * The name or identifier for this
     *  package
     */
    @XmlAttribute(name="name")
    private String m_name;

    /**
     * A rule which addresses belonging to this package
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
     * Range of addresses in this package
     */
    @XmlElement(name="include-range")
    private List<IncludeRange> m_includeRanges = new ArrayList<IncludeRange>();

    /**
     * Range of addresses to be excluded from this
     *  package
     */
    @XmlElement(name="exclude-range")
    private List<ExcludeRange> m_excludeRanges = new ArrayList<ExcludeRange>();

    /**
     * A file URL holding specific addresses to be polled.
     *  Each line in the URL file can be one of:
     *  "<IP><space>#<comments>"; "<IP>";
     *  "#<comments>"; Lines starting with a '#' are ignored and so
     *  are characters after a '<space>#' in a line.
     */
    @XmlElement(name="include-url")
    private List<String> m_includeUrls = new ArrayList<String>();

    /**
     * Flag for storing collected data by domain/ifAlias.
     *  Defaults to false. Allowable values are true, false.
     *  
     */
    @XmlElement(name="storeByIfAlias")
    private String m_storeByIfAlias;

    /**
     * Flag for storing collected data by nodeid/interface name.
     *  Defaults to normal. Allowable values are true, false,
     * normal.
     *  
     */
    @XmlElement(name="storeByNodeID")
    private String m_storeByNodeID;

    /**
     * The name of the domain covered by this collection
     *  package. Defaults to package name.
     *  
     */
    @XmlElement(name="ifAliasDomain")
    private String m_ifAliasDomain;

    /**
     * Flag for controlling how interfaces are selected for
     *  data collection by domain/ifAlias. If true, storage will
     * occur for
     *  any interface on the node found to have an valid ifAlias.
     * Otherwise
     *  data will be stored only if the interface is configured for
     * data
     *  collection. Defaults to false.
     *  
     */
    @XmlElement(name="storFlagOverride")
    private String m_storFlagOverride;

    /**
     * A character or string for terminating ifAlias text.
     *  In effect, any text beginning with this character or string
     * becomes
     *  a comment and is not considered part of the ifAlias when
     * naming
     *  storage files and displaying data. Defaults to null.
     *  
     */
    @XmlElement(name="ifAliasComment")
    private String m_ifAliasComment;

    /**
     * Services for which data is to be collected in this
     *  package
     */
    @XmlElement(name="service")
    private List<Service> m_services = new ArrayList<Service>();

    /**
     * Scheduled outages - data collection is not
     *  performed during scheduled outages
     */
    @XmlElement(name="outage-calendar")
    private List<String> m_outageCalendar = new ArrayList<String>();

    public Package() {
        super();
    }

    /**
     * 
     * 
     * @param range
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addExcludeRange(final ExcludeRange range) throws IndexOutOfBoundsException {
        m_excludeRanges.add(range);
    }

    /**
     * 
     * 
     * @param index
     * @param range
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addExcludeRange(final int index, final ExcludeRange range) throws IndexOutOfBoundsException {
        m_excludeRanges.add(index, range);
    }

    /**
     * 
     * 
     * @param range
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeRange(final IncludeRange range) throws IndexOutOfBoundsException {
        m_includeRanges.add(range);
    }

    /**
     * 
     * 
     * @param index
     * @param range
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeRange(final int index, final IncludeRange range) throws IndexOutOfBoundsException {
        m_includeRanges.add(index, range);
    }

    /**
     * 
     * 
     * @param url
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeUrl(final String url) throws IndexOutOfBoundsException {
        m_includeUrls.add(url);
    }

    /**
     * 
     * 
     * @param index
     * @param url
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeUrl(final int index, final String url) throws IndexOutOfBoundsException {
        m_includeUrls.add(index, url);
    }

    /**
     * 
     * 
     * @param calendar
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addOutageCalendar(final String calendar) throws IndexOutOfBoundsException {
        m_outageCalendar.add(calendar);
    }

    /**
     * 
     * 
     * @param index
     * @param calendar
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addOutageCalendar(final int index, final String calendar) throws IndexOutOfBoundsException {
        m_outageCalendar.add(index, calendar);
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
        return Collections.enumeration(m_outageCalendar);
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
        return m_excludeRanges.toArray(EMPTY_EXCLUDERANGE_LIST);
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
     * Returns the value of field 'ifAliasComment'. The field
     * 'ifAliasComment' has the following description: A character
     * or string for terminating ifAlias text.
     *  In effect, any text beginning with this character or string
     * becomes
     *  a comment and is not considered part of the ifAlias when
     * naming
     *  storage files and displaying data. Defaults to null.
     *  
     * 
     * @return the value of field 'IfAliasComment'.
     */
    public String getIfAliasComment() {
        return m_ifAliasComment;
    }

    /**
     * Returns the value of field 'ifAliasDomain'. The field
     * 'ifAliasDomain' has the following description: The name of
     * the domain covered by this collection
     *  package. Defaults to package name.
     *  
     * 
     * @return the value of field 'IfAliasDomain'.
     */
    public String getIfAliasDomain() {
        return m_ifAliasDomain;
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
        return m_includeRanges.toArray(EMPTY_INCLUDERANGE_LIST);
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
     * following description: The name or identifier for this
     *  package
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
        return m_outageCalendar.get(index);
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
        return m_outageCalendar.toArray(EMPTY_STRING_LIST);
    }

    /**
     * Method getOutageCalendarCollection.Returns a reference to
     * 'm_outageCalendar'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getOutageCalendarCollection() {
        return new ArrayList<String>(m_outageCalendar);
    }

    /**
     * Method getOutageCalendarCount.
     * 
     * @return the size of this collection
     */
    public int getOutageCalendarCount() {
        return m_outageCalendar.size();
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
     * 'm_specifics'. No type checking is performed on any
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
     * Returns the value of field 'storFlagOverride'. The field
     * 'storFlagOverride' has the following description: Flag for
     * controlling how interfaces are selected for
     *  data collection by domain/ifAlias. If true, storage will
     * occur for
     *  any interface on the node found to have an valid ifAlias.
     * Otherwise
     *  data will be stored only if the interface is configured for
     * data
     *  collection. Defaults to false.
     *  
     * 
     * @return the value of field 'StorFlagOverride'.
     */
    public String getStorFlagOverride() {
        return m_storFlagOverride;
    }

    /**
     * Returns the value of field 'storeByIfAlias'. The field
     * 'storeByIfAlias' has the following description: Flag for
     * storing collected data by domain/ifAlias.
     *  Defaults to false. Allowable values are true, false.
     *  
     * 
     * @return the value of field 'StoreByIfAlias'.
     */
    public String getStoreByIfAlias() {
        return m_storeByIfAlias;
    }

    /**
     * Returns the value of field 'storeByNodeID'. The field
     * 'storeByNodeID' has the following description: Flag for
     * storing collected data by nodeid/interface name.
     *  Defaults to normal. Allowable values are true, false,
     * normal.
     *  
     * 
     * @return the value of field 'StoreByNodeID'.
     */
    public String getStoreByNodeID() {
        return m_storeByNodeID;
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
        return m_outageCalendar.iterator();
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
    public void marshal( final Writer out) throws MarshalException, ValidationException {
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
        m_outageCalendar.clear();
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
     * Method removeExcludeRange.
     * 
     * @param range
     * @return true if the object was removed from the collection.
     */
    public boolean removeExcludeRange(final ExcludeRange range) {
        return m_excludeRanges.remove(range);
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
     * @param range
     * @return true if the object was removed from the collection.
     */
    public boolean removeIncludeRange(final IncludeRange range) {
        return m_includeRanges.remove(range);
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
     * @param url
     * @return true if the object was removed from the collection.
     */
    public boolean removeIncludeUrl(final String url) {
        return m_includeUrls.remove(url);
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
     * @param calendar
     * @return true if the object was removed from the collection.
     */
    public boolean removeOutageCalendar(final String calendar) {
        return m_outageCalendar.remove(calendar);
    }

    /**
     * Method removeOutageCalendarAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeOutageCalendarAt(final int index) {
        return m_outageCalendar.remove(index);
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
     * @param range
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setExcludeRange(final int index, final ExcludeRange range) throws IndexOutOfBoundsException {
        m_excludeRanges.set(index, range);
    }

    /**
     * 
     * 
     * @param ranges
     */
    public void setExcludeRange(final ExcludeRange[] ranges) {
        m_excludeRanges.clear();
        for (final ExcludeRange range : ranges) {
            m_excludeRanges.add(range);
        }
    }

    /**
     * Sets the value of 'm_excludeRanges' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param ranges the Vector to copy.
     */
    public void setExcludeRange(final List<ExcludeRange> ranges) {
        if (ranges != m_excludeRanges) {
            m_excludeRanges.clear();
            m_excludeRanges.addAll(ranges);
        }
    }

    /**
     * Sets the value of 'm_excludeRanges' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param ranges the Vector to set.
     */
    public void setExcludeRangeCollection(final List<ExcludeRange> ranges) {
        m_excludeRanges = new ArrayList<ExcludeRange>(ranges);
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

    /**
     * Sets the value of field 'ifAliasComment'. The field
     * 'ifAliasComment' has the following description: A character
     * or string for terminating ifAlias text.
     *  In effect, any text beginning with this character or string
     * becomes
     *  a comment and is not considered part of the ifAlias when
     * naming
     *  storage files and displaying data. Defaults to null.
     *  
     * 
     * @param ifAliasComment the value of field 'ifAliasComment'.
     */
    public void setIfAliasComment(final String ifAliasComment) {
        m_ifAliasComment = ifAliasComment;
    }

    /**
     * Sets the value of field 'ifAliasDomain'. The field
     * 'ifAliasDomain' has the following description: The name of
     * the domain covered by this collection
     *  package. Defaults to package name.
     *  
     * 
     * @param ifAliasDomain the value of field 'ifAliasDomain'.
     */
    public void setIfAliasDomain(final String ifAliasDomain) {
        m_ifAliasDomain = ifAliasDomain;
    }

    /**
     * 
     * 
     * @param index
     * @param range
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setIncludeRange(final int index, final IncludeRange range) throws IndexOutOfBoundsException {
        m_includeRanges.set(index, range);
    }

    /**
     * 
     * 
     * @param ranges
     */
    public void setIncludeRange(final IncludeRange[] ranges) {
        m_includeRanges.clear();
        for (final IncludeRange range : ranges) {
            m_includeRanges.add(range);
        }
    }

    /**
     * Sets the value of 'm_includeRanges' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param ranges the Vector to copy.
     */
    public void setIncludeRange(final List<IncludeRange> ranges) {
        if (ranges != m_includeRanges) {
            m_includeRanges.clear();
            m_includeRanges.addAll(ranges);
        }
    }

    /**
     * Sets the value of 'm_includeRanges' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param ranges the Vector to set.
     */
    public void setIncludeRangeCollection(final List<IncludeRange> ranges) {
        m_includeRanges = new ArrayList<IncludeRange>(ranges);
    }

    /**
     * 
     * 
     * @param index
     * @param url
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setIncludeUrl(final int index, final String url) throws IndexOutOfBoundsException {
        m_includeUrls.set(index, url);
    }

    /**
     * 
     * 
     * @param urls
     */
    public void setIncludeUrl(final String[] urls) {
        m_includeUrls.clear();
        for (final String url : urls) {
            m_includeUrls.add(url);
        }
    }

    /**
     * Sets the value of 'm_includeUrls' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param urls the Vector to copy.
     */
    public void setIncludeUrl(final List<String> urls) {
        if (urls != m_includeUrls) {
            m_includeUrls.clear();
            m_includeUrls.addAll(urls);
        }
    }

    /**
     * Sets the value of 'm_includeUrls' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param urls the Vector to set.
     */
    public void setIncludeUrlCollection(final List<String> urls) {
        m_includeUrls = new ArrayList<String>(urls);
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the
     * following description: The name or identifier for this
     *  package
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
     * @param calendar
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setOutageCalendar(final int index, final String calendar) throws IndexOutOfBoundsException {
        m_outageCalendar.set(index, calendar);
    }

    /**
     * 
     * 
     * @param calendars
     */
    public void setOutageCalendar(final String[] calendars) {
        m_outageCalendar.clear();
        for (final String calendar : calendars) {
            m_outageCalendar.add(calendar);
        }
    }

    /**
     * Sets the value of 'm_outageCalendar' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param calendars the Vector to copy.
     */
    public void setOutageCalendar(final List<String> calendars) {
        if (calendars != m_outageCalendar) {
            m_outageCalendar.clear();
            m_outageCalendar.addAll(calendars);
        }
    }

    /**
     * Sets the value of 'm_outageCalendar' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param calendars the Vector to set.
     */
    public void setOutageCalendarCollection(final List<String> calendars) {
        m_outageCalendar = new ArrayList<String>(calendars);
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
     * Sets the value of 'm_specifics' by copying the given
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
     * Sets the value of 'm_specifics' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param specifics the Vector to set.
     */
    public void setSpecificCollection(final List<String> specifics) {
        m_specifics = new ArrayList<String>(specifics);
    }

    /**
     * Sets the value of field 'storFlagOverride'. The field
     * 'storFlagOverride' has the following description: Flag for
     * controlling how interfaces are selected for
     *  data collection by domain/ifAlias. If true, storage will
     * occur for
     *  any interface on the node found to have an valid ifAlias.
     * Otherwise
     *  data will be stored only if the interface is configured for
     * data
     *  collection. Defaults to false.
     *  
     * 
     * @param storFlagOverride the value of field 'storFlagOverride'
     */
    public void setStorFlagOverride(final String storFlagOverride) {
        m_storFlagOverride = storFlagOverride;
    }

    /**
     * Sets the value of field 'storeByIfAlias'. The field
     * 'storeByIfAlias' has the following description: Flag for
     * storing collected data by domain/ifAlias.
     *  Defaults to false. Allowable values are true, false.
     *  
     * 
     * @param storeByIfAlias the value of field 'storeByIfAlias'.
     */
    public void setStoreByIfAlias(final String storeByIfAlias) {
        m_storeByIfAlias = storeByIfAlias;
    }

    /**
     * Sets the value of field 'storeByNodeID'. The field
     * 'storeByNodeID' has the following description: Flag for
     * storing collected data by nodeid/interface name.
     *  Defaults to normal. Allowable values are true, false,
     * normal.
     *  
     * 
     * @param storeByNodeID the value of field 'storeByNodeID'.
     */
    public void setStoreByNodeID(final String storeByNodeID) {
        m_storeByNodeID = storeByNodeID;
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
    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

    @Override
    public int hashCode() {
        final int prime = 727;
        int result = 1;
        result = prime * result + ((m_excludeRanges == null) ? 0 : m_excludeRanges.hashCode());
        result = prime * result + ((m_filter == null) ? 0 : m_filter.hashCode());
        result = prime * result + ((m_ifAliasComment == null) ? 0 : m_ifAliasComment.hashCode());
        result = prime * result + ((m_ifAliasDomain == null) ? 0 : m_ifAliasDomain.hashCode());
        result = prime * result + ((m_includeRanges == null) ? 0 : m_includeRanges.hashCode());
        result = prime * result + ((m_includeUrls == null) ? 0 : m_includeUrls.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_outageCalendar == null) ? 0 : m_outageCalendar.hashCode());
        result = prime * result + ((m_services == null) ? 0 : m_services.hashCode());
        result = prime * result + ((m_specifics == null) ? 0 : m_specifics.hashCode());
        result = prime * result + ((m_storFlagOverride == null) ? 0 : m_storFlagOverride.hashCode());
        result = prime * result + ((m_storeByIfAlias == null) ? 0 : m_storeByIfAlias.hashCode());
        result = prime * result + ((m_storeByNodeID == null) ? 0 : m_storeByNodeID.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Package)) {
            return false;
        }
        final Package other = (Package) obj;
        if (m_excludeRanges == null) {
            if (other.m_excludeRanges != null) {
                return false;
            }
        } else if (!m_excludeRanges.equals(other.m_excludeRanges)) {
            return false;
        }
        if (m_filter == null) {
            if (other.m_filter != null) {
                return false;
            }
        } else if (!m_filter.equals(other.m_filter)) {
            return false;
        }
        if (m_ifAliasComment == null) {
            if (other.m_ifAliasComment != null) {
                return false;
            }
        } else if (!m_ifAliasComment.equals(other.m_ifAliasComment)) {
            return false;
        }
        if (m_ifAliasDomain == null) {
            if (other.m_ifAliasDomain != null) {
                return false;
            }
        } else if (!m_ifAliasDomain.equals(other.m_ifAliasDomain)) {
            return false;
        }
        if (m_includeRanges == null) {
            if (other.m_includeRanges != null) {
                return false;
            }
        } else if (!m_includeRanges.equals(other.m_includeRanges)) {
            return false;
        }
        if (m_includeUrls == null) {
            if (other.m_includeUrls != null) {
                return false;
            }
        } else if (!m_includeUrls.equals(other.m_includeUrls)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_outageCalendar == null) {
            if (other.m_outageCalendar != null) {
                return false;
            }
        } else if (!m_outageCalendar.equals(other.m_outageCalendar)) {
            return false;
        }
        if (m_services == null) {
            if (other.m_services != null) {
                return false;
            }
        } else if (!m_services.equals(other.m_services)) {
            return false;
        }
        if (m_specifics == null) {
            if (other.m_specifics != null) {
                return false;
            }
        } else if (!m_specifics.equals(other.m_specifics)) {
            return false;
        }
        if (m_storFlagOverride == null) {
            if (other.m_storFlagOverride != null) {
                return false;
            }
        } else if (!m_storFlagOverride.equals(other.m_storFlagOverride)) {
            return false;
        }
        if (m_storeByIfAlias == null) {
            if (other.m_storeByIfAlias != null) {
                return false;
            }
        } else if (!m_storeByIfAlias.equals(other.m_storeByIfAlias)) {
            return false;
        }
        if (m_storeByNodeID == null) {
            if (other.m_storeByNodeID != null) {
                return false;
            }
        } else if (!m_storeByNodeID.equals(other.m_storeByNodeID)) {
            return false;
        }
        return true;
    }

}
