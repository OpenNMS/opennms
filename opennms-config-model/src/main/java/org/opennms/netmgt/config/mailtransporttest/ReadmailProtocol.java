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
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.xml.sax.ContentHandler;

/**
 * Basically attributes that help setup the javamailer's confusion
 * set of properties.
 */

@XmlRootElement(name="readmail-protocol")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReadmailProtocol implements Serializable {
    private static final long serialVersionUID = 8023143379589483011L;

    /**
     * Field m_transport.
     */
    @XmlAttribute(name="transport")
    private String m_transport;

    /**
     * Field m_sslEnable.
     */
    @XmlAttribute(name="ssl-enable")
    private Boolean m_sslEnable;

    /**
     * Field m_startTls.
     */
    @XmlAttribute(name="start-tls")
    private Boolean m_startTls;

    public ReadmailProtocol() {
        super();
    }

    public ReadmailProtocol(final String transport, final Boolean sslEnable, final Boolean startTls) {
        super();
        m_transport = transport;
        m_sslEnable = sslEnable;
        m_startTls = startTls;
    }

    public void deleteSslEnable() {
        m_sslEnable = null;
    }

    public void deleteStartTls() {
        m_startTls = null;
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
        
        if (obj instanceof ReadmailProtocol) {
            final ReadmailProtocol temp = (ReadmailProtocol)obj;
            if (m_transport != null) {
                if (temp.m_transport == null) {
                    return false;
                } else if (!(m_transport.equals(temp.m_transport))) {
                    return false;
                }
            } else if (temp.m_transport != null) {
                return false;
            }
            if (m_sslEnable != null) {
                if (temp.m_sslEnable == null) {
                    return false;
                } else if (!(m_sslEnable.equals(temp.m_sslEnable))) {
                    return false;
                }
            } else if (temp.m_sslEnable != null) {
                return false;
            }
            if (m_startTls != null) {
                if (temp.m_startTls == null) {
                    return false;
                } else if (!(m_startTls.equals(temp.m_startTls))) {
                    return false;
                }
            } else if (temp.m_startTls != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'sslEnable'.
     * 
     * @return the value of field 'SslEnable'.
     */
    public Boolean getSslEnable() {
        return m_sslEnable == null? false : m_sslEnable;
    }

    /**
     * Returns the value of field 'startTls'.
     * 
     * @return the value of field 'StartTls'.
     */
    public Boolean getStartTls() {
        return m_startTls == null? false : m_startTls;
    }

    /**
     * Returns the value of field 'transport'.
     * 
     * @return the value of field 'Transport'.
     */
    public String getTransport() {
        return m_transport == null? "pop3" : m_transport;
    }

    /**
     * Method hasSslEnable.
     * 
     * @return true if at least one SslEnable has been added
     */
    public boolean hasSslEnable() {
        return m_sslEnable != null;
    }

    /**
     * Method hasStartTls.
     * 
     * @return true if at least one StartTls has been added
     */
    public boolean hasStartTls() {
        return m_startTls != null;
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
        
        if (m_transport != null) {
            result = 37 * result + m_transport.hashCode();
         }
        if (m_sslEnable != null) {
            result = 37 * result + m_sslEnable.hashCode();
         }
        if (m_startTls != null) {
            result = 37 * result + m_startTls.hashCode();
         }
        
        return result;
    }

    /**
     * Returns the value of field 'sslEnable'.
     * 
     * @return the value of field 'SslEnable'.
     */
    public Boolean isSslEnable() {
        return m_sslEnable == null? false : m_sslEnable;
    }

    /**
     * Returns the value of field 'startTls'.
     * 
     * @return the value of field 'StartTls'.
     */
    public Boolean isStartTls() {
        return m_startTls == null? false : m_startTls;
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
     * Sets the value of field 'sslEnable'.
     * 
     * @param sslEnable the value of field 'sslEnable'.
     */
    public void setSslEnable(final Boolean sslEnable) {
        m_sslEnable = sslEnable;
    }

    /**
     * Sets the value of field 'startTls'.
     * 
     * @param startTls the value of field 'startTls'.
     */
    public void setStartTls(final Boolean startTls) {
        m_startTls = startTls;
    }

    /**
     * Sets the value of field 'transport'.
     * 
     * @param transport the value of field 'transport'.
     */
    public void setTransport(final String transport) {
        m_transport = transport;
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
     * ReadmailProtocol
     */
    public static ReadmailProtocol unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (ReadmailProtocol) Unmarshaller.unmarshal(ReadmailProtocol.class, reader);
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
