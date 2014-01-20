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
import org.opennms.core.xml.ValidateUsing;

/**
 * Top-level element for the collectd-configuration.xml
 *  configuration file.
 */

@XmlRootElement(name="collectd-configuration")
@ValidateUsing("collectd-configuration.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectdConfiguration implements Serializable {
    private static final long serialVersionUID = -5839903699762236380L;

    private static final Collector[] EMPTY_LIST_OF_COLLECTORS = new Collector[0];
    private static final Package[] EMPTY_LIST_OF_PACKAGES = new Package[0];

    /**
     * The maximum number of threads used for data
     *  collection.
     */
    @XmlAttribute(name="threads")
    private Integer m_threads;

    /**
     * Package encapsulating addresses eligible to have
     *  SNMP data collected from them.
     */
    @XmlElement(name="package")
    private List<Package> m_packages = new ArrayList<Package>();

    /**
     * Service collectors
     */
    @XmlElement(name="collector")
    private List<Collector> m_collectors = new ArrayList<Collector>();

    public CollectdConfiguration() {
        super();
    }

    /**
     * 
     * 
     * @param collector
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCollector(final Collector collector) throws IndexOutOfBoundsException {
        m_collectors.add(collector);
    }

    /**
     * 
     * 
     * @param index
     * @param collector
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCollector(final int index, final Collector collector) throws IndexOutOfBoundsException {
        m_collectors.add(index, collector);
    }

    public void addCollector(final String service, final String className) {
        m_collectors.add(new Collector(service, className));
    }

    /**
     * 
     * 
     * @param p
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPackage(final Package p) throws IndexOutOfBoundsException {
        m_packages.add(p);
    }

    /**
     * 
     * 
     * @param index
     * @param p
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPackage(final int index, final Package p) throws IndexOutOfBoundsException {
        m_packages.add(index, p);
    }

    /**
     */
    public void deleteThreads() {
        m_threads = null;
    }

    /**
     * Method enumerateCollector.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Collector> enumerateCollector() {
        return Collections.enumeration(m_collectors);
    }

    /**
     * Method enumeratePackage.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Package> enumeratePackage() {
        return Collections.enumeration(m_packages);
    }

    /**
     * Method getCollector.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Collector at the given
     * index
     */
    public Collector getCollector(final int index) throws IndexOutOfBoundsException {
        return m_collectors.get(index);
    }

    /**
     * Method getCollector.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Collector[] getCollector() {
        return m_collectors.toArray(EMPTY_LIST_OF_COLLECTORS);
    }

    /**
     * Method getCollectorCollection.Returns a reference to
     * 'm_collectors'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Collector> getCollectorCollection() {
        return new ArrayList<Collector>(m_collectors);
    }

    /**
     * Method getCollectorCount.
     * 
     * @return the size of this collection
     */
    public int getCollectorCount() {
        return m_collectors.size();
    }

    /**
     * Method getPackage.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Package at the given index
     */
    public Package getPackage(final int index) throws IndexOutOfBoundsException {
        return m_packages.get(index);
    }

    /**
     * Method getPackage.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Package[] getPackage() {
        return m_packages.toArray(EMPTY_LIST_OF_PACKAGES);
    }

    /**
     * Method getPackageCollection.Returns a reference to
     * 'm_packages'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Package> getPackageCollection() {
        return new ArrayList<Package>(m_packages);
    }

    /**
     * Method getPackageCount.
     * 
     * @return the size of this collection
     */
    public int getPackageCount() {
        return m_packages.size();
    }

    /**
     * Returns the value of field 'threads'. The field 'threads'
     * has the following description: The maximum number of threads
     * used for data
     *  collection.
     * 
     * @return the value of field 'Threads'.
     */
    public Integer getThreads() {
        return m_threads == null? 0 : m_threads;
    }

    /**
     * Method hasThreads.
     * 
     * @return true if at least one Threads has been added
     */
    public boolean hasThreads() {
        return m_threads != null;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid(
            ) {
        try {
            validate();
        } catch (ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateCollector.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Collector> iterateCollector() {
        return m_collectors.iterator();
    }

    /**
     * Method iteratePackage.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Package> iteratePackage() {
        return m_packages.iterator();
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
    public void marshal(
            final Writer out)
                    throws MarshalException, ValidationException {
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
    public void marshal(
            final org.xml.sax.ContentHandler handler)
                    throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllCollector() {
        m_collectors.clear();
    }

    /**
     */
    public void removeAllPackage() {
        m_packages.clear();
    }

    /**
     * Method removeCollector.
     * 
     * @param collector
     * @return true if the object was removed from the collection.
     */
    public boolean removeCollector(final Collector collector) {
        return m_collectors.remove(collector);
    }

    /**
     * Method removeCollectorAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Collector removeCollectorAt(final int index) {
        return m_collectors.remove(index);
    }

    /**
     * Method removePackage.
     * 
     * @param p
     * @return true if the object was removed from the collection.
     */
    public boolean removePackage(final Package p) {
        return m_packages.remove(p);
    }

    /**
     * Method removePackageAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Package removePackageAt(final int index) {
        return m_packages.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param collector
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setCollector(final int index, final Collector collector) throws IndexOutOfBoundsException {
        m_collectors.set(index, collector);
    }

    /**
     * 
     * 
     * @param collectors
     */
    public void setCollector(final Collector[] collectors) {
        m_collectors.clear();
        for (final Collector collector : collectors) {
            m_collectors.add(collector);
        }
    }

    /**
     * Sets the value of 'm_collectors' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param collectors the Vector to copy.
     */
    public void setCollector(final List<Collector> collectors) {
        if (collectors != m_collectors) {
            m_collectors.clear();
            m_collectors.addAll(collectors);
        }
    }

    /**
     * Sets the value of 'm_collectors' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param collectors the Vector to set.
     */
    public void setCollectorCollection(final List<Collector> collectors) {
        m_collectors = new ArrayList<Collector>(collectors);
    }

    /**
     * 
     * 
     * @param index
     * @param p
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setPackage(final int index, final Package p) throws IndexOutOfBoundsException {
        m_packages.set(index, p);
    }

    /**
     * 
     * 
     * @param packages
     */
    public void setPackage(final Package[] packages) {
        m_packages.clear();
        for (final Package p : packages) {
            m_packages.add(p);
        }
    }

    /**
     * Sets the value of 'm_packages' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param packages the Vector to copy.
     */
    public void setPackage(final List<Package> packages) {
        if (packages != m_packages) {
            m_packages.clear();
            m_packages.addAll(packages);
        }
    }

    /**
     * Sets the value of 'm_packages' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param m_packages the Vector to set.
     */
    public void setPackageCollection(final List<Package> packages) {
        m_packages = new ArrayList<Package>(packages);
    }

    /**
     * Sets the value of field 'threads'. The field 'threads' has
     * the following description: The maximum number of threads
     * used for data
     *  collection.
     * 
     * @param threads the value of field 'threads'.
     */
    public void setThreads(final Integer threads) {
        m_threads = threads;
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
     * CollectdConfiguration
     */
    public static CollectdConfiguration unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (CollectdConfiguration) Unmarshaller.unmarshal(CollectdConfiguration.class, reader);
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
        final int prime = 631;
        int result = 1;
        result = prime * result + ((m_collectors == null) ? 0 : m_collectors.hashCode());
        result = prime * result + ((m_packages == null) ? 0 : m_packages.hashCode());
        result = prime * result + ((m_threads == null) ? 0 : m_threads.hashCode());
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
        if (!(obj instanceof CollectdConfiguration)) {
            return false;
        }
        final CollectdConfiguration other = (CollectdConfiguration) obj;
        if (m_collectors == null) {
            if (other.m_collectors != null) {
                return false;
            }
        } else if (!m_collectors.equals(other.m_collectors)) {
            return false;
        }
        if (m_packages == null) {
            if (other.m_packages != null) {
                return false;
            }
        } else if (!m_packages.equals(other.m_packages)) {
            return false;
        }
        if (m_threads == null) {
            if (other.m_threads != null) {
                return false;
            }
        } else if (!m_threads.equals(other.m_threads)) {
            return false;
        }
        return true;
    }

}
