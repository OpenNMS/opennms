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
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class ReadmailHost.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="readmail-host", namespace="http://xmlns.opennms.org/xsd/config/javamail-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReadmailHost implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The host. */
    @XmlAttribute(name="host")
    private String _host;

    /** The port. */
    @XmlAttribute(name="port")
    private Integer _port;

    /**
     * Basically any attributes that help setup the javamailer's confusing set of properties.
     */
    @XmlElement(name="readmail-protocol")
    private ReadmailProtocol _readmailProtocol;

    //----------------/
    //- Constructors -/
    //----------------/

    /**
     * Instantiates a new readmail host.
     */
    public ReadmailHost() {
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

        if (obj instanceof ReadmailHost) {
            final ReadmailHost temp = (ReadmailHost)obj;
            return Objects.equals(temp._host, _host)
                    && Objects.equals(temp._port, _port)
                    && Objects.equals(temp._readmailProtocol, _readmailProtocol);
        }
        return false;
    }

    /**
     * Returns the value of field 'host'.
     * 
     * @return the value of field 'Host'.
     */
    public String getHost() {
        return this._host == null ? "127.0.0.1" : this._host;
    }

    /**
     * Returns the value of field 'port'.
     * 
     * @return the value of field 'Port'.
     */
    public Integer getPort() {
        return this._port == null ? 110 : this._port;
    }

    /**
     * Returns the value of field 'readmailProtocol'. The field 'readmailProtocol' has the following description: Basically
     * any attributes that help setup the javamailer's confusing set of properties.
     *  
     * @return the value of field 'ReadmailProtocol'.
     */
    public ReadmailProtocol getReadmailProtocol() {
        return this._readmailProtocol;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override()
    public int hashCode() {
        return Objects.hash(_host, _port, _readmailProtocol);
    }

    /**
     * Sets the value of field 'host'.
     * 
     * @param host the value of field 'host'.
     */
    public void setHost(final String host) {
        this._host = host;
    }

    /**
     * Sets the value of field 'port'.
     * 
     * @param port the value of field 'port'.
     */
    public void setPort(final Integer port) {
        this._port = port;
    }

    /**
     * Sets the value of field 'readmailProtocol'. The field 'readmailProtocol' has the following description: Basically
     * any attributes that help setup the javamailer's confusing set of properties.
     *  
     * @param readmailProtocol the value of field 'readmailProtocol'
     */
    public void setReadmailProtocol(final ReadmailProtocol readmailProtocol) {
        this._readmailProtocol = readmailProtocol;
    }

}
