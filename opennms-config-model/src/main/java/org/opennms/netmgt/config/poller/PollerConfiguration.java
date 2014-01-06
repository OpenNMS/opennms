/*
 * This class was converted to JAXB from Castor.
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
import javax.xml.bind.annotation.XmlSeeAlso;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.pagesequence.PageSequence;
import org.xml.sax.ContentHandler;

/**
 * Top-level element for the poller-configuration.xml
 *  configuration file.
 */

@XmlRootElement(name="poller-configuration")
@ValidateUsing("poller-configuration.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({PageSequence.class})
public class PollerConfiguration implements Serializable {
    private static final long serialVersionUID = 9063220520291464260L;

    private static final Monitor[] EMPTY_MONITOR_LIST = new Monitor[0];
    private static final Package[] EMPTY_PACKAGE_LIST = new Package[0];

    /**
     * The maximum number of threads used for
     *  polling.
     */
    @XmlAttribute(name="threads")
    private Integer m_threads = 30;

    /**
     * SQL query for getting the next outage
     *  ID.
     */
    @XmlAttribute(name="nextOutageId")
    private String m_nextOutageId = "SELECT nextval('outageNxtId')";

    /**
     * Enable/disable serviceUnresponsive
     *  behavior
     */
    @XmlAttribute(name="serviceUnresponsiveEnabled")
    private String m_serviceUnresponsiveEnabled = "false";

    /**
     * Flag which indicates if an external XMLRPC server has
     *  to be notified with any event process errors
     */
    @XmlAttribute(name="xmlrpc")
    private String m_xmlrpc = "false";

    /**
     * Flag which indicates if the optional path outage
     *  feature is enabled
     */
    @XmlAttribute(name="pathOutageEnabled")
    private String m_pathOutageEnabled = "false";

    /**
     * Configuration of node-outage
     *  functionality
     */
    @XmlElement(name="node-outage")
    private NodeOutage m_nodeOutage;

    /**
     * Package encapsulating addresses, services to be
     *  polled for these addresses, etc..
     */
    @XmlElement(name="package")
    private List<Package> m_packages = new ArrayList<Package>();

    /**
     * Service monitors
     */
    @XmlElement(name="monitor")
    private List<Monitor> m_monitors = new ArrayList<Monitor>();


    /**
     * 
     * 
     * @param monitor
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMonitor(final Monitor monitor) throws IndexOutOfBoundsException {
        m_monitors.add(monitor);
    }

    /**
     * 
     * 
     * @param index
     * @param monitor
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMonitor(final int index, final Monitor monitor) throws IndexOutOfBoundsException {
        m_monitors.add(index, monitor);
    }

    public void addMonitor(final String service, final String className) {
        addMonitor(new Monitor(service, className));
    }

    /**
     * 
     * 
     * @param pack
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPackage(final Package pack) throws IndexOutOfBoundsException {
        m_packages.add(pack);
    }

    /**
     * 
     * 
     * @param index
     * @param pack
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPackage(final int index, final Package pack) throws IndexOutOfBoundsException {
        m_packages.add(index, pack);
    }

    /**
     */
    public void deleteThreads() {
        m_threads = null;
    }

    /**
     * Method enumerateMonitor.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Monitor> enumerateMonitor() {
        return Collections.enumeration(m_monitors);
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
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;

        if (obj instanceof PollerConfiguration) {
            final PollerConfiguration temp = (PollerConfiguration)obj;
            if (m_threads != null) {
                if (temp.m_threads == null) {
                    return false;
                } else if (!(m_threads.equals(temp.m_threads))) {
                    return false;
                }
            } else if (temp.m_threads != null) {
                return false;
            }
            if (m_nextOutageId != null) {
                if (temp.m_nextOutageId == null) {
                    return false;
                } else if (!(m_nextOutageId.equals(temp.m_nextOutageId))) {
                    return false;
                }
            } else if (temp.m_nextOutageId != null) {
                return false;
            }
            if (m_serviceUnresponsiveEnabled != null) {
                if (temp.m_serviceUnresponsiveEnabled == null) {
                    return false;
                } else if (!(m_serviceUnresponsiveEnabled.equals(temp.m_serviceUnresponsiveEnabled))) {
                    return false;
                }
            }
            else if (temp.m_serviceUnresponsiveEnabled != null) {
                return false;
            }
            if (m_xmlrpc != null) {
                if (temp.m_xmlrpc == null) {
                    return false;
                } else if (!(m_xmlrpc.equals(temp.m_xmlrpc))) {
                    return false;
                }
            } else if (temp.m_xmlrpc != null) {
                return false;
            }
            if (m_pathOutageEnabled != null) {
                if (temp.m_pathOutageEnabled == null) {
                    return false;
                } else if (!(m_pathOutageEnabled.equals(temp.m_pathOutageEnabled))) {
                    return false;
                }
            } else if (temp.m_pathOutageEnabled != null) {
                return false;
            }
            if (m_nodeOutage != null) {
                if (temp.m_nodeOutage == null) {
                    return false;
                } else if (!(m_nodeOutage.equals(temp.m_nodeOutage))) {
                    return false;
                }
            } else if (temp.m_nodeOutage != null) {
                return false;
            }
            if (m_packages != null) {
                if (temp.m_packages == null) {
                    return false;
                } else if (!(m_packages.equals(temp.m_packages))) {
                    return false;
                }
            } else if (temp.m_packages != null) {
                return false;
            }
            if (m_monitors != null) {
                if (temp.m_monitors == null) {
                    return false;
                } else if (!(m_monitors.equals(temp.m_monitors))) {
                    return false;
                }
            } else if (temp.m_monitors != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Method getMonitor.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Monitor at the given index
     */
    public Monitor getMonitor(final int index) throws IndexOutOfBoundsException {
        return m_monitors.get(index);
    }

    /**
     * Method getMonitor.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Monitor[] getMonitor() {
        return m_monitors.toArray(EMPTY_MONITOR_LIST);
    }

    /**
     * Method getMonitorCollection.Returns a reference to
     * 'm_monitors'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Monitor> getMonitorCollection() {
        return new ArrayList<Monitor>(m_monitors);
    }

    /**
     * Method getMonitorCount.
     * 
     * @return the size of this collection
     */
    public int getMonitorCount() {
        return m_monitors.size();
    }

    /**
     * Returns the value of field 'nextOutageId'. The field
     * 'nextOutageId' has the following description: SQL query for
     * getting the next outage
     *  ID.
     * 
     * @return the value of field 'NextOutageId'.
     */
    public String getNextOutageId() {
        return m_nextOutageId == null? "SELECT nextval('outageNxtId')" : m_nextOutageId;
    }

    /**
     * Returns the value of field 'nodeOutage'. The field
     * 'nodeOutage' has the following description: Configuration of
     * node-outage
     *  functionality
     * 
     * @return the value of field 'NodeOutage'.
     */
    public NodeOutage getNodeOutage() {
        return m_nodeOutage;
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
        return m_packages.toArray(EMPTY_PACKAGE_LIST);
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
     * Returns the value of field 'pathOutageEnabled'. The field
     * 'pathOutageEnabled' has the following description: Flag
     * which indicates if the optional path outage
     *  feature is enabled
     * 
     * @return the value of field 'PathOutageEnabled'.
     */
    public String getPathOutageEnabled() {
        return m_pathOutageEnabled == null? "false" : m_pathOutageEnabled;
    }

    /**
     * Returns the value of field 'serviceUnresponsiveEnabled'. The
     * field 'serviceUnresponsiveEnabled' has the following
     * description: Enable/disable serviceUnresponsive
     *  behavior
     * 
     * @return the value of field 'ServiceUnresponsiveEnabled'.
     */
    public String getServiceUnresponsiveEnabled() {
        return m_serviceUnresponsiveEnabled;
    }

    /**
     * Returns the value of field 'threads'. The field 'threads'
     * has the following description: The maximum number of threads
     * used for
     *  polling.
     * 
     * @return the value of field 'Threads'.
     */
    public Integer getThreads() {
        return m_threads == null? 0 : m_threads;
    }

    /**
     * Returns the value of field 'xmlrpc'. The field 'xmlrpc' has
     * the following description: Flag which indicates if an
     * external XMLRPC server has
     *  to be notified with any event process errors
     * 
     * @return the value of field 'Xmlrpc'.
     */
    public String getXmlrpc() {
        return m_xmlrpc == null? "false" : m_xmlrpc;
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
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;

        if (m_threads != null) {
            result = 37 * result + m_threads.hashCode();
        }
        if (m_nextOutageId != null) {
            result = 37 * result + m_nextOutageId.hashCode();
        }
        if (m_serviceUnresponsiveEnabled != null) {
            result = 37 * result + m_serviceUnresponsiveEnabled.hashCode();
        }
        if (m_xmlrpc != null) {
            result = 37 * result + m_xmlrpc.hashCode();
        }
        if (m_pathOutageEnabled != null) {
            result = 37 * result + m_pathOutageEnabled.hashCode();
        }
        if (m_nodeOutage != null) {
            result = 37 * result + m_nodeOutage.hashCode();
        }
        if (m_packages != null) {
            result = 37 * result + m_packages.hashCode();
        }
        if (m_monitors != null) {
            result = 37 * result + m_monitors.hashCode();
        }

        return result;
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
     * Method iterateMonitor.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Monitor> iterateMonitor() {
        return m_monitors.iterator();
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
    public void removeAllMonitor() {
        m_monitors.clear();
    }

    /**
     */
    public void removeAllPackage() {
        m_packages.clear();
    }

    /**
     * Method removeMonitor.
     * 
     * @param monitor
     * @return true if the object was removed from the collection.
     */
    public boolean removeMonitor(final Monitor monitor) {
        return m_monitors.remove(monitor);
    }

    /**
     * Method removeMonitorAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Monitor removeMonitorAt(final int index) {
        return m_monitors.remove(index);
    }

    /**
     * Method removePackage.
     * 
     * @param pack
     * @return true if the object was removed from the collection.
     */
    public boolean removePackage(final Package pack) {
        return m_packages.remove(pack);
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
     * @param monitor
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setMonitor(final int index, final Monitor monitor) throws IndexOutOfBoundsException {
        m_monitors.set(index, monitor);
    }

    /**
     * 
     * 
     * @param monitors
     */
    public void setMonitor(final Monitor[] monitors) {
        m_monitors.clear();
        for (final Monitor monitor : monitors) {
            m_monitors.add(monitor);
        }
    }

    /**
     * Sets the value of 'm_monitors' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param monitors the Vector to copy.
     */
    public void setMonitor(final List<Monitor> monitors) {
        if (monitors != m_monitors) {
            m_monitors.clear();
            m_monitors.addAll(monitors);
        }
    }

    /**
     * Sets the value of 'm_monitors' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param monitors the Vector to set.
     */
    public void setMonitorCollection(final List<Monitor> monitors) {
        m_monitors = new ArrayList<Monitor>(monitors);
    }

    /**
     * Sets the value of field 'nextOutageId'. The field
     * 'nextOutageId' has the following description: SQL query for
     * getting the next outage
     *  ID.
     * 
     * @param nextOutageId the value of field 'nextOutageId'.
     */
    public void setNextOutageId(final String nextOutageId) {
        m_nextOutageId = nextOutageId;
    }

    /**
     * Sets the value of field 'nodeOutage'. The field 'nodeOutage'
     * has the following description: Configuration of node-outage
     *  functionality
     * 
     * @param nodeOutage the value of field 'nodeOutage'.
     */
    public void setNodeOutage(final NodeOutage nodeOutage) {
        m_nodeOutage = nodeOutage;
    }

    /**
     * 
     * 
     * @param index
     * @param vPackage
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setPackage(final int index, final Package vPackage) throws IndexOutOfBoundsException {
        m_packages.set(index, vPackage);
    }

    /**
     * 
     * 
     * @param packages
     */
    public void setPackage(final Package[] packages) {
        m_packages.clear();
        for (final Package pack : packages) {
            m_packages.add(pack);
        }
    }

    /**
     * Sets the value of 'm_packages' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param packages the Vector to copy.
     */
    public void setPackage(final List<Package> packages) {
        if (m_packages != packages) {
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
    public void setPackageCollection(final List<Package> packageList) {
        m_packages = new ArrayList<Package>(packageList);
    }

    /**
     * Sets the value of field 'pathOutageEnabled'. The field
     * 'pathOutageEnabled' has the following description: Flag
     * which indicates if the optional path outage
     *  feature is enabled
     * 
     * @param pathOutageEnabled the value of field
     * 'pathOutageEnabled'.
     */
    public void setPathOutageEnabled(final String pathOutageEnabled) {
        m_pathOutageEnabled = pathOutageEnabled;
    }

    /**
     * Sets the value of field 'serviceUnresponsiveEnabled'. The
     * field 'serviceUnresponsiveEnabled' has the following
     * description: Enable/disable serviceUnresponsive
     *  behavior
     * 
     * @param serviceUnresponsiveEnabled the value of field
     * 'serviceUnresponsiveEnabled'.
     */
    public void setServiceUnresponsiveEnabled(final String serviceUnresponsiveEnabled) {
        m_serviceUnresponsiveEnabled = serviceUnresponsiveEnabled;
    }

    /**
     * Sets the value of field 'threads'. The field 'threads' has
     * the following description: The maximum number of threads
     * used for
     *  polling.
     * 
     * @param threads the value of field 'threads'.
     */
    public void setThreads(final Integer threads) {
        m_threads = threads;
    }

    /**
     * Sets the value of field 'xmlrpc'. The field 'xmlrpc' has the
     * following description: Flag which indicates if an external
     * XMLRPC server has
     *  to be notified with any event process errors
     * 
     * @param xmlrpc the value of field 'xmlrpc'.
     */
    public void setXmlrpc(final String xmlrpc) {
        m_xmlrpc = xmlrpc;
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
     * PollerConfiguration
     */
    public static PollerConfiguration unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (PollerConfiguration)Unmarshaller.unmarshal(PollerConfiguration.class, reader);
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
    public String toString() {
        return "PollerConfiguration[" +
                "threads=" + m_threads +
                ",nextOutageId=" + m_nextOutageId +
                ",serviceUnresponsiveEnabled=" + m_serviceUnresponsiveEnabled +
                ",xmlrpc=" + m_xmlrpc +
                ",pathOutageEnabled=" + m_pathOutageEnabled +
                ",nodeOutage=" + m_nodeOutage +
                ",packages=" + m_packages +
                ",monitors=" + m_monitors +
                "]";
    }
}
