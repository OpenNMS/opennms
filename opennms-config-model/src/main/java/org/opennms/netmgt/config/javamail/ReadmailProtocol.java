/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.javamail;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The Class ReadmailProtocol.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="readmail-protocol", namespace="http://xmlns.opennms.org/xsd/config/javamail-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReadmailProtocol implements Serializable {

    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8487366539846091236L;

    /** The transport. */
    @XmlAttribute(name="transport")
    private String _transport = "pop3";

    /** The SSL enable flag. */
    @XmlAttribute(name="ssl-enable")
    private boolean _sslEnable = false;

    /** keeps track of state for field: _sslEnable. */
    @XmlTransient
    private boolean _has_sslEnable;

    /** The start TLS flag. */
    @XmlAttribute(name="start-tls")
    private boolean _startTls = false;

    /** keeps track of state for field: _startTls. */
    @XmlTransient
    private boolean _has_startTls;

    //----------------/
    //- Constructors -/
    //----------------/

    /**
     * Instantiates a new readmail protocol.
     */
    public ReadmailProtocol() {
        super();
        setTransport("pop3");
    }

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Delete SSL enable.
     */
    public void deleteSslEnable() {
        this._has_sslEnable= false;
    }

    /**
     * Delete start TLS.
     */
    public void deleteStartTls() {
        this._has_startTls= false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;
        if (obj instanceof ReadmailProtocol) {
            ReadmailProtocol temp = (ReadmailProtocol)obj;
            if (this._transport != null) {
                if (temp._transport == null) return false;
                else if (!(this._transport.equals(temp._transport))) 
                    return false;
            }
            else if (temp._transport != null)
                return false;
            if (this._sslEnable != temp._sslEnable)
                return false;
            if (this._has_sslEnable != temp._has_sslEnable)
                return false;
            if (this._startTls != temp._startTls)
                return false;
            if (this._has_startTls != temp._has_startTls)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'sslEnable'.
     * 
     * @return the value of field 'SslEnable'.
     */
    public boolean getSslEnable() {
        return this._sslEnable;
    }

    /**
     * Returns the value of field 'startTls'.
     * 
     * @return the value of field 'StartTls'.
     */
    public boolean getStartTls() {
        return this._startTls;
    }

    /**
     * Returns the value of field 'transport'.
     * 
     * @return the value of field 'Transport'.
     */
    public String getTransport() {
        return this._transport;
    }

    /**
     * Method hasSslEnable.
     * 
     * @return true if at least one SslEnable has been added
     */
    public boolean hasSslEnable() {
        return this._has_sslEnable;
    }

    /**
     * Method hasStartTls.
     * 
     * @return true if at least one StartTls has been added
     */
    public boolean hasStartTls() {
        return this._has_startTls;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override()
    public int hashCode() {
        int result = 17;
        if (_transport != null) {
            result = 37 * result + _transport.hashCode();
        }
        result = 37 * result + (_sslEnable?0:1);
        result = 37 * result + (_startTls?0:1);
        return result;
    }

    /**
     * Returns the value of field 'sslEnable'.
     * 
     * @return the value of field 'SslEnable'.
     */
    public boolean isSslEnable() {
        return this._sslEnable;
    }

    /**
     * Returns the value of field 'startTls'.
     * 
     * @return the value of field 'StartTls'.
     */
    public boolean isStartTls() {
        return this._startTls;
    }

    /**
     * Sets the value of field 'sslEnable'.
     * 
     * @param sslEnable the value of field 'sslEnable'.
     */
    public void setSslEnable(final boolean sslEnable) {
        this._sslEnable = sslEnable;
        this._has_sslEnable = true;
    }

    /**
     * Sets the value of field 'startTls'.
     * 
     * @param startTls the value of field 'startTls'.
     */
    public void setStartTls(final boolean startTls) {
        this._startTls = startTls;
        this._has_startTls = true;
    }

    /**
     * Sets the value of field 'transport'.
     * 
     * @param transport the value of field 'transport'.
     */
    public void setTransport(final String transport) {
        this._transport = transport;
    }

}
