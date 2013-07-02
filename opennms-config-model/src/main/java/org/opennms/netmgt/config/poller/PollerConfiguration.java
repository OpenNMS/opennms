/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.poller;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.opennms.core.xml.ValidateUsing;

/**
 * Top-level element for the poller-configuration.xml
 *  configuration file.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="poller-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("poller-configuration.xsd")
@SuppressWarnings("serial")
public class PollerConfiguration implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The maximum number of threads used for
     *  polling.
     */
	@XmlAttribute(name="threads", required=true)
    private int _threads;

    /**
     * SQL query for getting the next outage
     *  ID.
     */
	@XmlAttribute(name="nextOutageId")
    private java.lang.String _nextOutageId;

    /**
     * Enable/disable serviceUnresponsive
     *  behavior
     */
	@XmlAttribute(name="serviceUnresponsiveEnabled", required=true)
    private java.lang.String _serviceUnresponsiveEnabled;

    /**
     * Flag which indicates if an external XMLRPC server has
     *  to be notified with any event process errors
     */
	@XmlAttribute(name="xmlrpc")
    private java.lang.String _xmlrpc;

    /**
     * Flag which indicates if the optional path outage
     *  feature is enabled
     */
	@XmlAttribute(name="pathOutageEnabled")
    private java.lang.String _pathOutageEnabled;

    /**
     * Configuration of node-outage
     *  functionality
     */
	@XmlElement(name="node-outage", required=true)
    private org.opennms.netmgt.config.poller.NodeOutage _nodeOutage;

    /**
     * Package encapsulating addresses, services to be
     *  polled for these addresses, etc..
     */
	@XmlElement(name="package", required=true)
    private java.util.List<org.opennms.netmgt.config.poller.Package> _packageList;

    /**
     * Service monitors
     */
	@XmlElement(name="monitor", required=true)
    private java.util.List<org.opennms.netmgt.config.poller.Monitor> _monitorList;


      //----------------/
     //- Constructors -/
    //----------------/

    public PollerConfiguration() {
        super();
        this._packageList = new java.util.ArrayList<org.opennms.netmgt.config.poller.Package>();
        this._monitorList = new java.util.ArrayList<org.opennms.netmgt.config.poller.Monitor>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vMonitor
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMonitor(
            final org.opennms.netmgt.config.poller.Monitor vMonitor)
    throws java.lang.IndexOutOfBoundsException {
        this._monitorList.add(vMonitor);
    }

    /**
     * 
     * 
     * @param index
     * @param vMonitor
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMonitor(
            final int index,
            final org.opennms.netmgt.config.poller.Monitor vMonitor)
    throws java.lang.IndexOutOfBoundsException {
        this._monitorList.add(index, vMonitor);
    }

    /**
     * 
     * 
     * @param vPackage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPackage(
            final org.opennms.netmgt.config.poller.Package vPackage)
    throws java.lang.IndexOutOfBoundsException {
        this._packageList.add(vPackage);
    }

    /**
     * 
     * 
     * @param index
     * @param vPackage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPackage(
            final int index,
            final org.opennms.netmgt.config.poller.Package vPackage)
    throws java.lang.IndexOutOfBoundsException {
        this._packageList.add(index, vPackage);
    }

    /**
     * Method enumerateMonitor.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.poller.Monitor> enumerateMonitor(
    ) {
        return java.util.Collections.enumeration(this._monitorList);
    }

    /**
     * Method enumeratePackage.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.poller.Package> enumeratePackage(
    ) {
        return java.util.Collections.enumeration(this._packageList);
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(
            final java.lang.Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof PollerConfiguration) {
        
            PollerConfiguration temp = (PollerConfiguration)obj;
            if (this._threads != temp._threads)
                return false;
            if (this._nextOutageId != null) {
                if (temp._nextOutageId == null) return false;
                else if (!(this._nextOutageId.equals(temp._nextOutageId))) 
                    return false;
            }
            else if (temp._nextOutageId != null)
                return false;
            if (this._serviceUnresponsiveEnabled != null) {
                if (temp._serviceUnresponsiveEnabled == null) return false;
                else if (!(this._serviceUnresponsiveEnabled.equals(temp._serviceUnresponsiveEnabled))) 
                    return false;
            }
            else if (temp._serviceUnresponsiveEnabled != null)
                return false;
            if (this._xmlrpc != null) {
                if (temp._xmlrpc == null) return false;
                else if (!(this._xmlrpc.equals(temp._xmlrpc))) 
                    return false;
            }
            else if (temp._xmlrpc != null)
                return false;
            if (this._pathOutageEnabled != null) {
                if (temp._pathOutageEnabled == null) return false;
                else if (!(this._pathOutageEnabled.equals(temp._pathOutageEnabled))) 
                    return false;
            }
            else if (temp._pathOutageEnabled != null)
                return false;
            if (this._nodeOutage != null) {
                if (temp._nodeOutage == null) return false;
                else if (!(this._nodeOutage.equals(temp._nodeOutage))) 
                    return false;
            }
            else if (temp._nodeOutage != null)
                return false;
            if (this._packageList != null) {
                if (temp._packageList == null) return false;
                else if (!(this._packageList.equals(temp._packageList))) 
                    return false;
            }
            else if (temp._packageList != null)
                return false;
            if (this._monitorList != null) {
                if (temp._monitorList == null) return false;
                else if (!(this._monitorList.equals(temp._monitorList))) 
                    return false;
            }
            else if (temp._monitorList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getMonitor.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.poller.Monitor at the given index
     */
    public org.opennms.netmgt.config.poller.Monitor getMonitor(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._monitorList.size()) {
            throw new IndexOutOfBoundsException("getMonitor: Index value '" + index + "' not in range [0.." + (this._monitorList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.config.poller.Monitor) _monitorList.get(index);
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
    public org.opennms.netmgt.config.poller.Monitor[] getMonitor(
    ) {
        org.opennms.netmgt.config.poller.Monitor[] array = new org.opennms.netmgt.config.poller.Monitor[0];
        return (org.opennms.netmgt.config.poller.Monitor[]) this._monitorList.toArray(array);
    }

    /**
     * Method getMonitorCollection.Returns a reference to
     * '_monitorList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.poller.Monitor> getMonitorCollection(
    ) {
        return this._monitorList;
    }

    /**
     * Method getMonitorCount.
     * 
     * @return the size of this collection
     */
    public int getMonitorCount(
    ) {
        return this._monitorList.size();
    }

    /**
     * Returns the value of field 'nextOutageId'. The field
     * 'nextOutageId' has the following description: SQL query for
     * getting the next outage
     *  ID.
     * 
     * @return the value of field 'NextOutageId'.
     */
    public java.lang.String getNextOutageId(
    ) {
        return this._nextOutageId == null ? "SELECT nextval('outageNxtId')" : _nextOutageId;
    }

    /**
     * Returns the value of field 'nodeOutage'. The field
     * 'nodeOutage' has the following description: Configuration of
     * node-outage
     *  functionality
     * 
     * @return the value of field 'NodeOutage'.
     */
    public org.opennms.netmgt.config.poller.NodeOutage getNodeOutage(
    ) {
        return this._nodeOutage;
    }

    /**
     * Method getPackage.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.poller.Package at the given index
     */
    public org.opennms.netmgt.config.poller.Package getPackage(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._packageList.size()) {
            throw new IndexOutOfBoundsException("getPackage: Index value '" + index + "' not in range [0.." + (this._packageList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.config.poller.Package) _packageList.get(index);
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
    public org.opennms.netmgt.config.poller.Package[] getPackage(
    ) {
        org.opennms.netmgt.config.poller.Package[] array = new org.opennms.netmgt.config.poller.Package[0];
        return (org.opennms.netmgt.config.poller.Package[]) this._packageList.toArray(array);
    }

    /**
     * Method getPackageCollection.Returns a reference to
     * '_packageList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.poller.Package> getPackageCollection(
    ) {
        return this._packageList;
    }

    /**
     * Method getPackageCount.
     * 
     * @return the size of this collection
     */
    public int getPackageCount(
    ) {
        return this._packageList.size();
    }

    /**
     * Returns the value of field 'pathOutageEnabled'. The field
     * 'pathOutageEnabled' has the following description: Flag
     * which indicates if the optional path outage
     *  feature is enabled
     * 
     * @return the value of field 'PathOutageEnabled'.
     */
    public java.lang.String getPathOutageEnabled(
    ) {
        return this._pathOutageEnabled == null ? "false" : _pathOutageEnabled;
    }

    /**
     * Returns the value of field 'serviceUnresponsiveEnabled'. The
     * field 'serviceUnresponsiveEnabled' has the following
     * description: Enable/disable serviceUnresponsive
     *  behavior
     * 
     * @return the value of field 'ServiceUnresponsiveEnabled'.
     */
    public java.lang.String getServiceUnresponsiveEnabled(
    ) {
        return this._serviceUnresponsiveEnabled;
    }

    /**
     * Returns the value of field 'threads'. The field 'threads'
     * has the following description: The maximum number of threads
     * used for
     *  polling.
     * 
     * @return the value of field 'Threads'.
     */
    public int getThreads(
    ) {
        return this._threads;
    }

    /**
     * Returns the value of field 'xmlrpc'. The field 'xmlrpc' has
     * the following description: Flag which indicates if an
     * external XMLRPC server has
     *  to be notified with any event process errors
     * 
     * @return the value of field 'Xmlrpc'.
     */
    public java.lang.String getXmlrpc(
    ) {
        return this._xmlrpc == null ? "false" : _xmlrpc;
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode(
    ) {
        int result = 17;
        
        result = 37 * result + _threads;
        if (_nextOutageId != null) {
           result = 37 * result + _nextOutageId.hashCode();
        }
        if (_serviceUnresponsiveEnabled != null) {
           result = 37 * result + _serviceUnresponsiveEnabled.hashCode();
        }
        if (_xmlrpc != null) {
           result = 37 * result + _xmlrpc.hashCode();
        }
        if (_pathOutageEnabled != null) {
           result = 37 * result + _pathOutageEnabled.hashCode();
        }
        if (_nodeOutage != null) {
           result = 37 * result + _nodeOutage.hashCode();
        }
        if (_packageList != null) {
           result = 37 * result + _packageList.hashCode();
        }
        if (_monitorList != null) {
           result = 37 * result + _monitorList.hashCode();
        }
        
        return result;
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
        } catch (org.exolab.castor.xml.ValidationException vex) {
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
    public java.util.Iterator<org.opennms.netmgt.config.poller.Monitor> iterateMonitor(
    ) {
        return this._monitorList.iterator();
    }

    /**
     * Method iteratePackage.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.poller.Package> iteratePackage(
    ) {
        return this._packageList.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(
            final java.io.Writer out)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException if an IOException occurs during
     * marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(
            final org.xml.sax.ContentHandler handler)
    throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllMonitor(
    ) {
        this._monitorList.clear();
    }

    /**
     */
    public void removeAllPackage(
    ) {
        this._packageList.clear();
    }

    /**
     * Method removeMonitor.
     * 
     * @param vMonitor
     * @return true if the object was removed from the collection.
     */
    public boolean removeMonitor(
            final org.opennms.netmgt.config.poller.Monitor vMonitor) {
        boolean removed = _monitorList.remove(vMonitor);
        return removed;
    }

    /**
     * Method removeMonitorAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.poller.Monitor removeMonitorAt(
            final int index) {
        java.lang.Object obj = this._monitorList.remove(index);
        return (org.opennms.netmgt.config.poller.Monitor) obj;
    }

    /**
     * Method removePackage.
     * 
     * @param vPackage
     * @return true if the object was removed from the collection.
     */
    public boolean removePackage(
            final org.opennms.netmgt.config.poller.Package vPackage) {
        boolean removed = _packageList.remove(vPackage);
        return removed;
    }

    /**
     * Method removePackageAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.poller.Package removePackageAt(
            final int index) {
        java.lang.Object obj = this._packageList.remove(index);
        return (org.opennms.netmgt.config.poller.Package) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vMonitor
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setMonitor(
            final int index,
            final org.opennms.netmgt.config.poller.Monitor vMonitor)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._monitorList.size()) {
            throw new IndexOutOfBoundsException("setMonitor: Index value '" + index + "' not in range [0.." + (this._monitorList.size() - 1) + "]");
        }
        
        this._monitorList.set(index, vMonitor);
    }

    /**
     * 
     * 
     * @param vMonitorArray
     */
    public void setMonitor(
            final org.opennms.netmgt.config.poller.Monitor[] vMonitorArray) {
        //-- copy array
        _monitorList.clear();
        
        for (int i = 0; i < vMonitorArray.length; i++) {
                this._monitorList.add(vMonitorArray[i]);
        }
    }

    /**
     * Sets the value of '_monitorList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vMonitorList the Vector to copy.
     */
    public void setMonitor(
            final java.util.List<org.opennms.netmgt.config.poller.Monitor> vMonitorList) {
        // copy vector
        this._monitorList.clear();
        
        this._monitorList.addAll(vMonitorList);
    }

    /**
     * Sets the value of '_monitorList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param monitorList the Vector to set.
     */
    public void setMonitorCollection(
            final java.util.List<org.opennms.netmgt.config.poller.Monitor> monitorList) {
        this._monitorList = monitorList;
    }

    /**
     * Sets the value of field 'nextOutageId'. The field
     * 'nextOutageId' has the following description: SQL query for
     * getting the next outage
     *  ID.
     * 
     * @param nextOutageId the value of field 'nextOutageId'.
     */
    public void setNextOutageId(
            final java.lang.String nextOutageId) {
        this._nextOutageId = nextOutageId;
    }

    /**
     * Sets the value of field 'nodeOutage'. The field 'nodeOutage'
     * has the following description: Configuration of node-outage
     *  functionality
     * 
     * @param nodeOutage the value of field 'nodeOutage'.
     */
    public void setNodeOutage(
            final org.opennms.netmgt.config.poller.NodeOutage nodeOutage) {
        this._nodeOutage = nodeOutage;
    }

    /**
     * 
     * 
     * @param index
     * @param vPackage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setPackage(
            final int index,
            final org.opennms.netmgt.config.poller.Package vPackage)
    throws java.lang.IndexOutOfBoundsException {
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
    public void setPackage(
            final org.opennms.netmgt.config.poller.Package[] vPackageArray) {
        //-- copy array
        _packageList.clear();
        
        for (int i = 0; i < vPackageArray.length; i++) {
                this._packageList.add(vPackageArray[i]);
        }
    }

    /**
     * Sets the value of '_packageList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vPackageList the Vector to copy.
     */
    public void setPackage(
            final java.util.List<org.opennms.netmgt.config.poller.Package> vPackageList) {
        // copy vector
        this._packageList.clear();
        
        this._packageList.addAll(vPackageList);
    }

    /**
     * Sets the value of '_packageList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param _packageList the Vector to set.
     */
    public void setPackageCollection(
            final java.util.List<org.opennms.netmgt.config.poller.Package> _packageList) {
        this._packageList = _packageList;
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
    public void setPathOutageEnabled(
            final java.lang.String pathOutageEnabled) {
        this._pathOutageEnabled = pathOutageEnabled;
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
    public void setServiceUnresponsiveEnabled(
            final java.lang.String serviceUnresponsiveEnabled) {
        this._serviceUnresponsiveEnabled = serviceUnresponsiveEnabled;
    }

    /**
     * Sets the value of field 'threads'. The field 'threads' has
     * the following description: The maximum number of threads
     * used for
     *  polling.
     * 
     * @param threads the value of field 'threads'.
     */
    public void setThreads(
            final int threads) {
        this._threads = threads;
    }

    /**
     * Sets the value of field 'xmlrpc'. The field 'xmlrpc' has the
     * following description: Flag which indicates if an external
     * XMLRPC server has
     *  to be notified with any event process errors
     * 
     * @param xmlrpc the value of field 'xmlrpc'.
     */
    public void setXmlrpc(
            final java.lang.String xmlrpc) {
        this._xmlrpc = xmlrpc;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.opennms.netmgt.config.poller.PollerConfiguration
     */
    public static org.opennms.netmgt.config.poller.PollerConfiguration unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.config.poller.PollerConfiguration) Unmarshaller.unmarshal(org.opennms.netmgt.config.poller.PollerConfiguration.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate(
    )
    throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
