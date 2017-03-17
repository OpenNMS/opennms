/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.javamail;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

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
    private String _transport;

    /** The SSL enable flag. */
    @XmlAttribute(name="ssl-enable")
    private Boolean _sslEnable;

    /** The start TLS flag. */
    @XmlAttribute(name="start-tls")
    private Boolean _startTls;

    //----------------/
    //- Constructors -/
    //----------------/

    /**
     * Instantiates a new readmail protocol.
     */
    public ReadmailProtocol() {
        super();
    }

    //-----------/
    //- Methods -/
    //-----------/

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ReadmailProtocol) {
            final ReadmailProtocol temp = (ReadmailProtocol)obj;
            return Objects.equals(temp._transport, _transport)
                    && Objects.equals(temp._sslEnable, _sslEnable)
                    && Objects.equals(temp._startTls, _startTls);
        }
        return false;
    }

    /**
     * Returns the value of field 'sslEnable'.
     * 
     * @return the value of field 'SslEnable'.
     */
    public Boolean isSslEnable() {
        return this._sslEnable == null ? Boolean.FALSE : this._sslEnable;
    }

    /**
     * Returns the value of field 'startTls'.
     * 
     * @return the value of field 'StartTls'.
     */
    public Boolean isStartTls() {
        return this._startTls == null ? Boolean.FALSE : this._startTls;
    }

    /**
     * Returns the value of field 'transport'.
     * 
     * @return the value of field 'Transport'.
     */
    public String getTransport() {
        return this._transport == null ? "pop3" : this._transport;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override()
    public int hashCode() {
        return Objects.hash(_transport, _sslEnable, _startTls);
    }

    /**
     * Sets the value of field 'sslEnable'.
     * 
     * @param sslEnable the value of field 'sslEnable'.
     */
    public void setSslEnable(final Boolean sslEnable) {
        this._sslEnable = sslEnable;
    }

    /**
     * Sets the value of field 'startTls'.
     * 
     * @param startTls the value of field 'startTls'.
     */
    public void setStartTls(final Boolean startTls) {
        this._startTls = startTls;
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
