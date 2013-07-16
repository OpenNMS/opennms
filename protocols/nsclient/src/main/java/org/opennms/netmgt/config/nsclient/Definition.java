/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.nsclient;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Provides a mechanism for associating one or more specific
 *  IP addresses and/or IP address ranges with a set of NSClient
 * parms which
 *  will be used in place of the default values during NSClient
 * data
 *  collection.
 * 
 * @version $Revision$ $Date$
 */

@SuppressWarnings("all") public class Definition implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * If set, overrides TCP port 1248 as the port where NSClient
     *  requests are sent.
     */
    private int _port;

    /**
     * keeps track of state for field: _port
     */
    private boolean _has_port;

    /**
     * number of retries
     */
    private int _retry;

    /**
     * keeps track of state for field: _retry
     */
    private boolean _has_retry;

    /**
     * timeout (in milliseconds)
     */
    private int _timeout;

    /**
     * keeps track of state for field: _timeout
     */
    private boolean _has_timeout;

    /**
     * Password for this definition
     */
    private java.lang.String _password;

    /**
     * IP address range to which this definition
     *  applies.
     */
    private java.util.List<org.opennms.netmgt.config.nsclient.Range> _rangeList;

    /**
     * Specific IP address to which this definition
     *  applies.
     */
    private java.util.List<java.lang.String> _specificList;

    /**
     * Match Octets (as in IPLIKE)
     */
    private java.util.List<java.lang.String> _ipMatchList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Definition() {
        super();
        this._rangeList = new java.util.ArrayList<org.opennms.netmgt.config.nsclient.Range>();
        this._specificList = new java.util.ArrayList<java.lang.String>();
        this._ipMatchList = new java.util.ArrayList<java.lang.String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vIpMatch
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIpMatch(
            final java.lang.String vIpMatch)
    throws java.lang.IndexOutOfBoundsException {
        this._ipMatchList.add(vIpMatch);
    }

    /**
     * 
     * 
     * @param index
     * @param vIpMatch
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIpMatch(
            final int index,
            final java.lang.String vIpMatch)
    throws java.lang.IndexOutOfBoundsException {
        this._ipMatchList.add(index, vIpMatch);
    }

    /**
     * 
     * 
     * @param vRange
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRange(
            final org.opennms.netmgt.config.nsclient.Range vRange)
    throws java.lang.IndexOutOfBoundsException {
        this._rangeList.add(vRange);
    }

    /**
     * 
     * 
     * @param index
     * @param vRange
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRange(
            final int index,
            final org.opennms.netmgt.config.nsclient.Range vRange)
    throws java.lang.IndexOutOfBoundsException {
        this._rangeList.add(index, vRange);
    }

    /**
     * 
     * 
     * @param vSpecific
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSpecific(
            final java.lang.String vSpecific)
    throws java.lang.IndexOutOfBoundsException {
        this._specificList.add(vSpecific);
    }

    /**
     * 
     * 
     * @param index
     * @param vSpecific
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSpecific(
            final int index,
            final java.lang.String vSpecific)
    throws java.lang.IndexOutOfBoundsException {
        this._specificList.add(index, vSpecific);
    }

    /**
     */
    public void deletePort(
    ) {
        this._has_port= false;
    }

    /**
     */
    public void deleteRetry(
    ) {
        this._has_retry= false;
    }

    /**
     */
    public void deleteTimeout(
    ) {
        this._has_timeout= false;
    }

    /**
     * Method enumerateIpMatch.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<java.lang.String> enumerateIpMatch(
    ) {
        return java.util.Collections.enumeration(this._ipMatchList);
    }

    /**
     * Method enumerateRange.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.nsclient.Range> enumerateRange(
    ) {
        return java.util.Collections.enumeration(this._rangeList);
    }

    /**
     * Method enumerateSpecific.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<java.lang.String> enumerateSpecific(
    ) {
        return java.util.Collections.enumeration(this._specificList);
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
        
        if (obj instanceof Definition) {
        
            Definition temp = (Definition)obj;
            if (this._port != temp._port)
                return false;
            if (this._has_port != temp._has_port)
                return false;
            if (this._retry != temp._retry)
                return false;
            if (this._has_retry != temp._has_retry)
                return false;
            if (this._timeout != temp._timeout)
                return false;
            if (this._has_timeout != temp._has_timeout)
                return false;
            if (this._password != null) {
                if (temp._password == null) return false;
                else if (!(this._password.equals(temp._password))) 
                    return false;
            }
            else if (temp._password != null)
                return false;
            if (this._rangeList != null) {
                if (temp._rangeList == null) return false;
                else if (!(this._rangeList.equals(temp._rangeList))) 
                    return false;
            }
            else if (temp._rangeList != null)
                return false;
            if (this._specificList != null) {
                if (temp._specificList == null) return false;
                else if (!(this._specificList.equals(temp._specificList))) 
                    return false;
            }
            else if (temp._specificList != null)
                return false;
            if (this._ipMatchList != null) {
                if (temp._ipMatchList == null) return false;
                else if (!(this._ipMatchList.equals(temp._ipMatchList))) 
                    return false;
            }
            else if (temp._ipMatchList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getIpMatch.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getIpMatch(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._ipMatchList.size()) {
            throw new IndexOutOfBoundsException("getIpMatch: Index value '" + index + "' not in range [0.." + (this._ipMatchList.size() - 1) + "]");
        }
        
        return (java.lang.String) _ipMatchList.get(index);
    }

    /**
     * Method getIpMatch.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public java.lang.String[] getIpMatch(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._ipMatchList.toArray(array);
    }

    /**
     * Method getIpMatchCollection.Returns a reference to
     * '_ipMatchList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getIpMatchCollection(
    ) {
        return this._ipMatchList;
    }

    /**
     * Method getIpMatchCount.
     * 
     * @return the size of this collection
     */
    public int getIpMatchCount(
    ) {
        return this._ipMatchList.size();
    }

    /**
     * Returns the value of field 'password'. The field 'password'
     * has the following description: Password for this definition
     * 
     * @return the value of field 'Password'.
     */
    public java.lang.String getPassword(
    ) {
        return this._password;
    }

    /**
     * Returns the value of field 'port'. The field 'port' has the
     * following description: If set, overrides TCP port 1248 as
     * the port where NSClient
     *  requests are sent.
     * 
     * @return the value of field 'Port'.
     */
    public int getPort(
    ) {
        return this._port;
    }

    /**
     * Method getRange.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.nsclient.Range at the given index
     */
    public org.opennms.netmgt.config.nsclient.Range getRange(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._rangeList.size()) {
            throw new IndexOutOfBoundsException("getRange: Index value '" + index + "' not in range [0.." + (this._rangeList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.config.nsclient.Range) _rangeList.get(index);
    }

    /**
     * Method getRange.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.nsclient.Range[] getRange(
    ) {
        org.opennms.netmgt.config.nsclient.Range[] array = new org.opennms.netmgt.config.nsclient.Range[0];
        return (org.opennms.netmgt.config.nsclient.Range[]) this._rangeList.toArray(array);
    }

    /**
     * Method getRangeCollection.Returns a reference to
     * '_rangeList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.nsclient.Range> getRangeCollection(
    ) {
        return this._rangeList;
    }

    /**
     * Method getRangeCount.
     * 
     * @return the size of this collection
     */
    public int getRangeCount(
    ) {
        return this._rangeList.size();
    }

    /**
     * Returns the value of field 'retry'. The field 'retry' has
     * the following description: number of retries
     * 
     * @return the value of field 'Retry'.
     */
    public int getRetry(
    ) {
        return this._retry;
    }

    /**
     * Method getSpecific.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getSpecific(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._specificList.size()) {
            throw new IndexOutOfBoundsException("getSpecific: Index value '" + index + "' not in range [0.." + (this._specificList.size() - 1) + "]");
        }
        
        return (java.lang.String) _specificList.get(index);
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
    public java.lang.String[] getSpecific(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._specificList.toArray(array);
    }

    /**
     * Method getSpecificCollection.Returns a reference to
     * '_specificList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getSpecificCollection(
    ) {
        return this._specificList;
    }

    /**
     * Method getSpecificCount.
     * 
     * @return the size of this collection
     */
    public int getSpecificCount(
    ) {
        return this._specificList.size();
    }

    /**
     * Returns the value of field 'timeout'. The field 'timeout'
     * has the following description: timeout (in milliseconds)
     * 
     * @return the value of field 'Timeout'.
     */
    public int getTimeout(
    ) {
        return this._timeout;
    }

    /**
     * Method hasPort.
     * 
     * @return true if at least one Port has been added
     */
    public boolean hasPort(
    ) {
        return this._has_port;
    }

    /**
     * Method hasRetry.
     * 
     * @return true if at least one Retry has been added
     */
    public boolean hasRetry(
    ) {
        return this._has_retry;
    }

    /**
     * Method hasTimeout.
     * 
     * @return true if at least one Timeout has been added
     */
    public boolean hasTimeout(
    ) {
        return this._has_timeout;
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode(
    ) {
        int result = 17;
        
        long tmp;
        result = 37 * result + _port;
        result = 37 * result + _retry;
        result = 37 * result + _timeout;
        if (_password != null) {
           result = 37 * result + _password.hashCode();
        }
        if (_rangeList != null) {
           result = 37 * result + _rangeList.hashCode();
        }
        if (_specificList != null) {
           result = 37 * result + _specificList.hashCode();
        }
        if (_ipMatchList != null) {
           result = 37 * result + _ipMatchList.hashCode();
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
     * Method iterateIpMatch.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<java.lang.String> iterateIpMatch(
    ) {
        return this._ipMatchList.iterator();
    }

    /**
     * Method iterateRange.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.nsclient.Range> iterateRange(
    ) {
        return this._rangeList.iterator();
    }

    /**
     * Method iterateSpecific.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<java.lang.String> iterateSpecific(
    ) {
        return this._specificList.iterator();
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
    public void removeAllIpMatch(
    ) {
        this._ipMatchList.clear();
    }

    /**
     */
    public void removeAllRange(
    ) {
        this._rangeList.clear();
    }

    /**
     */
    public void removeAllSpecific(
    ) {
        this._specificList.clear();
    }

    /**
     * Method removeIpMatch.
     * 
     * @param vIpMatch
     * @return true if the object was removed from the collection.
     */
    public boolean removeIpMatch(
            final java.lang.String vIpMatch) {
        boolean removed = _ipMatchList.remove(vIpMatch);
        return removed;
    }

    /**
     * Method removeIpMatchAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeIpMatchAt(
            final int index) {
        java.lang.Object obj = this._ipMatchList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * Method removeRange.
     * 
     * @param vRange
     * @return true if the object was removed from the collection.
     */
    public boolean removeRange(
            final org.opennms.netmgt.config.nsclient.Range vRange) {
        boolean removed = _rangeList.remove(vRange);
        return removed;
    }

    /**
     * Method removeRangeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.nsclient.Range removeRangeAt(
            final int index) {
        java.lang.Object obj = this._rangeList.remove(index);
        return (org.opennms.netmgt.config.nsclient.Range) obj;
    }

    /**
     * Method removeSpecific.
     * 
     * @param vSpecific
     * @return true if the object was removed from the collection.
     */
    public boolean removeSpecific(
            final java.lang.String vSpecific) {
        boolean removed = _specificList.remove(vSpecific);
        return removed;
    }

    /**
     * Method removeSpecificAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeSpecificAt(
            final int index) {
        java.lang.Object obj = this._specificList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vIpMatch
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setIpMatch(
            final int index,
            final java.lang.String vIpMatch)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._ipMatchList.size()) {
            throw new IndexOutOfBoundsException("setIpMatch: Index value '" + index + "' not in range [0.." + (this._ipMatchList.size() - 1) + "]");
        }
        
        this._ipMatchList.set(index, vIpMatch);
    }

    /**
     * 
     * 
     * @param vIpMatchArray
     */
    public void setIpMatch(
            final java.lang.String[] vIpMatchArray) {
        //-- copy array
        _ipMatchList.clear();
        
        for (int i = 0; i < vIpMatchArray.length; i++) {
                this._ipMatchList.add(vIpMatchArray[i]);
        }
    }

    /**
     * Sets the value of '_ipMatchList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vIpMatchList the Vector to copy.
     */
    public void setIpMatch(
            final java.util.List<java.lang.String> vIpMatchList) {
        // copy vector
        this._ipMatchList.clear();
        
        this._ipMatchList.addAll(vIpMatchList);
    }

    /**
     * Sets the value of '_ipMatchList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param ipMatchList the Vector to set.
     */
    public void setIpMatchCollection(
            final java.util.List<java.lang.String> ipMatchList) {
        this._ipMatchList = ipMatchList;
    }

    /**
     * Sets the value of field 'password'. The field 'password' has
     * the following description: Password for this definition
     * 
     * @param password the value of field 'password'.
     */
    public void setPassword(
            final java.lang.String password) {
        this._password = password;
    }

    /**
     * Sets the value of field 'port'. The field 'port' has the
     * following description: If set, overrides TCP port 1248 as
     * the port where NSClient
     *  requests are sent.
     * 
     * @param port the value of field 'port'.
     */
    public void setPort(
            final int port) {
        this._port = port;
        this._has_port = true;
    }

    /**
     * 
     * 
     * @param index
     * @param vRange
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setRange(
            final int index,
            final org.opennms.netmgt.config.nsclient.Range vRange)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._rangeList.size()) {
            throw new IndexOutOfBoundsException("setRange: Index value '" + index + "' not in range [0.." + (this._rangeList.size() - 1) + "]");
        }
        
        this._rangeList.set(index, vRange);
    }

    /**
     * 
     * 
     * @param vRangeArray
     */
    public void setRange(
            final org.opennms.netmgt.config.nsclient.Range[] vRangeArray) {
        //-- copy array
        _rangeList.clear();
        
        for (int i = 0; i < vRangeArray.length; i++) {
                this._rangeList.add(vRangeArray[i]);
        }
    }

    /**
     * Sets the value of '_rangeList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vRangeList the Vector to copy.
     */
    public void setRange(
            final java.util.List<org.opennms.netmgt.config.nsclient.Range> vRangeList) {
        // copy vector
        this._rangeList.clear();
        
        this._rangeList.addAll(vRangeList);
    }

    /**
     * Sets the value of '_rangeList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param rangeList the Vector to set.
     */
    public void setRangeCollection(
            final java.util.List<org.opennms.netmgt.config.nsclient.Range> rangeList) {
        this._rangeList = rangeList;
    }

    /**
     * Sets the value of field 'retry'. The field 'retry' has the
     * following description: number of retries
     * 
     * @param retry the value of field 'retry'.
     */
    public void setRetry(
            final int retry) {
        this._retry = retry;
        this._has_retry = true;
    }

    /**
     * 
     * 
     * @param index
     * @param vSpecific
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setSpecific(
            final int index,
            final java.lang.String vSpecific)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._specificList.size()) {
            throw new IndexOutOfBoundsException("setSpecific: Index value '" + index + "' not in range [0.." + (this._specificList.size() - 1) + "]");
        }
        
        this._specificList.set(index, vSpecific);
    }

    /**
     * 
     * 
     * @param vSpecificArray
     */
    public void setSpecific(
            final java.lang.String[] vSpecificArray) {
        //-- copy array
        _specificList.clear();
        
        for (int i = 0; i < vSpecificArray.length; i++) {
                this._specificList.add(vSpecificArray[i]);
        }
    }

    /**
     * Sets the value of '_specificList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vSpecificList the Vector to copy.
     */
    public void setSpecific(
            final java.util.List<java.lang.String> vSpecificList) {
        // copy vector
        this._specificList.clear();
        
        this._specificList.addAll(vSpecificList);
    }

    /**
     * Sets the value of '_specificList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param specificList the Vector to set.
     */
    public void setSpecificCollection(
            final java.util.List<java.lang.String> specificList) {
        this._specificList = specificList;
    }

    /**
     * Sets the value of field 'timeout'. The field 'timeout' has
     * the following description: timeout (in milliseconds)
     * 
     * @param timeout the value of field 'timeout'.
     */
    public void setTimeout(
            final int timeout) {
        this._timeout = timeout;
        this._has_timeout = true;
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
     * org.opennms.netmgt.config.nsclient.Definition
     */
    public static org.opennms.netmgt.config.nsclient.Definition unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.config.nsclient.Definition) Unmarshaller.unmarshal(org.opennms.netmgt.config.nsclient.Definition.class, reader);
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
