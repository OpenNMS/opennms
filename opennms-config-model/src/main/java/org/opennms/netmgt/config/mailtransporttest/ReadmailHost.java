/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.mailtransporttest;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

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
 * Define the host and port of the sendmail server. If you don't,
 * defaults will be used and
 *  ${ipaddr} is replaced with the IP address of the service.
 */

@XmlRootElement(name="readmail-host")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReadmailHost implements Serializable {
    private static final long serialVersionUID = 1723200582958011828L;

    /**
     * Field m_host.
     */
    @XmlAttribute(name="host")
    private String m_host;

    /**
     * Field m_port.
     */
    @XmlAttribute(name="port")
    private Long m_port;

    /**
     * Basically attributes that help setup the javamailer's
     * confusion set of properties.
     */
    @XmlElement(name="readmail-protocol")
    private ReadmailProtocol m_readmailProtocol;

    public ReadmailHost() {
        super();
    }

    public ReadmailHost(final String host, final Long port) {
        super();
        m_host = host;
        m_port = port;
    }

    public void deletePort() {
        m_port = null;
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;
        
        if (obj instanceof ReadmailHost) {
            final ReadmailHost temp = (ReadmailHost)obj;
            if (m_host != null) {
                if (temp.m_host == null) {
                    return false;
                } else if (!(m_host.equals(temp.m_host))) {
                    return false;
                }
            } else if (temp.m_host != null) {
                return false;
            }
            if (m_port != null) {
                if (temp.m_port == null) {
                    return false;
                } else if (!(m_port.equals(temp.m_port))) {
                    return false;
                }
            } else if (temp.m_port != null) {
                return false;
            }
            if (m_readmailProtocol != null) {
                if (temp.m_readmailProtocol == null) {
                    return false;
                } else if (!(m_readmailProtocol.equals(temp.m_readmailProtocol))) {
                    return false;
                }
            } else if (temp.m_readmailProtocol != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'host'.
     * 
     * @return the value of field 'Host'.
     */
    public String getHost() {
        return m_host == null? "${ipaddr}" : m_host;
    }

    /**
     * Returns the value of field 'port'.
     * 
     * @return the value of field 'Port'.
     */
    public Long getPort() {
        return m_port == null? 110 : m_port;
    }

    /**
     * Returns the value of field 'readmailProtocol'. The field
     * 'readmailProtocol' has the following description: Basically
     * attributes that help setup the javamailer's confusion set of
     * properties.
     *  
     *  
     * 
     * @return the value of field 'ReadmailProtocol'.
     */
    public ReadmailProtocol getReadmailProtocol() {
        return m_readmailProtocol;
    }

    /**
     * Method hasPort.
     * 
     * @return true if at least one Port has been added
     */
    public boolean hasPort() {
        return m_port != null;
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
        
        if (m_host != null) {
           result = 37 * result + m_host.hashCode();
        }
        if (m_port != null) {
            result = 37 * result + m_port.hashCode();
        }
        if (m_readmailProtocol != null) {
           result = 37 * result + m_readmailProtocol.hashCode();
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
     * Sets the value of field 'host'.
     * 
     * @param host the value of field 'host'.
     */
    public void setHost(final String host) {
        m_host = host;
    }

    /**
     * Sets the value of field 'port'.
     * 
     * @param port the value of field 'port'.
     */
    public void setPort(final Long port) {
        m_port = port;
    }

    /**
     * Sets the value of field 'readmailProtocol'. The field
     * 'readmailProtocol' has the following description: Basically
     * attributes that help setup the javamailer's confusion set of
     * properties.
     *  
     *  
     * 
     * @param readmailProtocol the value of field 'readmailProtocol'
     */
    public void setReadmailProtocol(final ReadmailProtocol readmailProtocol) {
        m_readmailProtocol = readmailProtocol;
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
     * ReadmailHost
     */
    public static ReadmailHost unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (ReadmailHost) Unmarshaller.unmarshal(ReadmailHost.class, reader);
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

}
