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

package org.opennms.netmgt.config.xmpConfig;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Top-level element for the xmp-config.xml configuration
 *  file.
 * 
 * @version $Revision$ $Date$
 */

@SuppressWarnings("all") public class XmpConfig implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * If set, overrides TCP port 5270 as the port
     *  where XMP documents (queries) are sent.
     */
    private int _port;

    /**
     * keeps track of state for field: _port
     */
    private boolean _has_port;

    /**
     * Default number of retries
     */
    private int _retry;

    /**
     * keeps track of state for field: _retry
     */
    private boolean _has_retry;

    /**
     * Default timeout (in milliseconds)
     */
    private int _timeout;

    /**
     * keeps track of state for field: _timeout
     */
    private boolean _has_timeout;

    /**
     * Default XMP user/profile
     */
    private java.lang.String _authenUser;


      //----------------/
     //- Constructors -/
    //----------------/

    public XmpConfig() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

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
        
        if (obj instanceof XmpConfig) {
        
            XmpConfig temp = (XmpConfig)obj;
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
            if (this._authenUser != null) {
                if (temp._authenUser == null) return false;
                else if (!(this._authenUser.equals(temp._authenUser))) 
                    return false;
            }
            else if (temp._authenUser != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'authenUser'. The field
     * 'authenUser' has the following description: Default XMP
     * user/profile
     * 
     * @return the value of field 'AuthenUser'.
     */
    public java.lang.String getAuthenUser(
    ) {
        return this._authenUser;
    }

    /**
     * Returns the value of field 'port'. The field 'port' has the
     * following description: If set, overrides TCP port 5270 as
     * the port
     *  where XMP documents (queries) are sent.
     * 
     * @return the value of field 'Port'.
     */
    public int getPort(
    ) {
        return this._port;
    }

    /**
     * Returns the value of field 'retry'. The field 'retry' has
     * the following description: Default number of retries
     * 
     * @return the value of field 'Retry'.
     */
    public int getRetry(
    ) {
        return this._retry;
    }

    /**
     * Returns the value of field 'timeout'. The field 'timeout'
     * has the following description: Default timeout (in
     * milliseconds)
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
        if (_authenUser != null) {
           result = 37 * result + _authenUser.hashCode();
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
     * Sets the value of field 'authenUser'. The field 'authenUser'
     * has the following description: Default XMP user/profile
     * 
     * @param authenUser the value of field 'authenUser'.
     */
    public void setAuthenUser(
            final java.lang.String authenUser) {
        this._authenUser = authenUser;
    }

    /**
     * Sets the value of field 'port'. The field 'port' has the
     * following description: If set, overrides TCP port 5270 as
     * the port
     *  where XMP documents (queries) are sent.
     * 
     * @param port the value of field 'port'.
     */
    public void setPort(
            final int port) {
        this._port = port;
        this._has_port = true;
    }

    /**
     * Sets the value of field 'retry'. The field 'retry' has the
     * following description: Default number of retries
     * 
     * @param retry the value of field 'retry'.
     */
    public void setRetry(
            final int retry) {
        this._retry = retry;
        this._has_retry = true;
    }

    /**
     * Sets the value of field 'timeout'. The field 'timeout' has
     * the following description: Default timeout (in milliseconds)
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
     * org.opennms.netmgt.config.xmpConfig.XmpConfig
     */
    public static org.opennms.netmgt.config.xmpConfig.XmpConfig unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.config.xmpConfig.XmpConfig) Unmarshaller.unmarshal(org.opennms.netmgt.config.xmpConfig.XmpConfig.class, reader);
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
