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

package org.opennms.netmgt.config.server;

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
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * Top-level element for the opennms-server.xml
 *  configuration file.
 */

@XmlRootElement(name="local-server")
@ValidateUsing("opennms-server.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalServer implements Serializable {
    private static final long serialVersionUID = -8351197310769498904L;

    /**
     * Field m_serverName.
     */
    @XmlAttribute(name="server-name")
    private String m_serverName;

    /**
     * Field m_defaultCriticalPathIp.
     */
    @XmlAttribute(name="defaultCriticalPathIp")
    private String m_defaultCriticalPathIp;

    /**
     * Field m_defaultCriticalPathService.
     */
    @XmlAttribute(name="defaultCriticalPathService")
    private String m_defaultCriticalPathService;

    /**
     * Field m_defaultCriticalPathTimeout.
     */
    @XmlAttribute(name="defaultCriticalPathTimeout")
    private Integer m_defaultCriticalPathTimeout;

    /**
     * Field m_defaultCriticalPathRetries.
     */
    @XmlAttribute(name="defaultCriticalPathRetries")
    private Integer m_defaultCriticalPathRetries;

    /**
     * A flag to indicate if poller has to identify the nms
     *  server to restrict services to poll.
     */
    @XmlAttribute(name="verify-server")
    private String m_verifyServer;

    public LocalServer() {
        super();
    }

    /**
     */
    public void deleteDefaultCriticalPathRetries() {
        m_defaultCriticalPathRetries = null;
    }

    /**
     */
    public void deleteDefaultCriticalPathTimeout() {
        m_defaultCriticalPathTimeout = null;
    }

    /**
     * Returns the value of field 'defaultCriticalPathIp'.
     * 
     * @return the value of field 'DefaultCriticalPathIp'.
     */
    public String getDefaultCriticalPathIp() {
        return m_defaultCriticalPathIp == null? "" : m_defaultCriticalPathIp;
    }

    /**
     * Returns the value of field 'defaultCriticalPathRetries'.
     * 
     * @return the value of field 'DefaultCriticalPathRetries'.
     */
    public int getDefaultCriticalPathRetries() {
        return m_defaultCriticalPathRetries == null? 0 : m_defaultCriticalPathRetries;
    }

    /**
     * Returns the value of field 'defaultCriticalPathService'.
     * 
     * @return the value of field 'DefaultCriticalPathService'.
     */
    public String getDefaultCriticalPathService() {
        return m_defaultCriticalPathService == null? "" : m_defaultCriticalPathService;
    }

    /**
     * Returns the value of field 'defaultCriticalPathTimeout'.
     * 
     * @return the value of field 'DefaultCriticalPathTimeout'.
     */
    public Integer getDefaultCriticalPathTimeout() {
        return m_defaultCriticalPathTimeout == null? 1500 : m_defaultCriticalPathTimeout;
    }

    /**
     * Returns the value of field 'serverName'.
     * 
     * @return the value of field 'ServerName'.
     */
    public String getServerName() {
        return m_serverName == null? "localhost" : m_serverName;
    }

    /**
     * Returns the value of field 'verifyServer'. The field
     * 'verifyServer' has the following description: A flag to
     * indicate if poller has to identify the nms
     *  server to restrict services to poll.
     * 
     * @return the value of field 'VerifyServer'.
     */
    public String getVerifyServer() {
        return m_verifyServer == null? "false" : m_verifyServer;
    }

    /**
     * Method hasDefaultCriticalPathRetries.
     * 
     * @return true if at least one DefaultCriticalPathRetries has
     * been added
     */
    public boolean hasDefaultCriticalPathRetries() {
        return m_defaultCriticalPathRetries != null;
    }

    /**
     * Method hasDefaultCriticalPathTimeout.
     * 
     * @return true if at least one DefaultCriticalPathTimeout has
     * been added
     */
    public boolean hasDefaultCriticalPathTimeout() {
        return m_defaultCriticalPathTimeout != null;
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
     * Sets the value of field 'defaultCriticalPathIp'.
     * 
     * @param ip the value of field
     * 'defaultCriticalPathIp'.
     */
    public void setDefaultCriticalPathIp(final String ip) {
        m_defaultCriticalPathIp = ip;
    }

    /**
     * Sets the value of field 'defaultCriticalPathRetries'.
     * 
     * @param defaultCriticalPathRetries the value of field
     * 'defaultCriticalPathRetries'.
     */
    public void setDefaultCriticalPathRetries(final Integer retries) {
        m_defaultCriticalPathRetries = retries;
    }

    /**
     * Sets the value of field 'defaultCriticalPathService'.
     * 
     * @param defaultCriticalPathService the value of field
     * 'defaultCriticalPathService'.
     */
    public void setDefaultCriticalPathService(final String defaultCriticalPathService) {
        m_defaultCriticalPathService = defaultCriticalPathService;
    }

    /**
     * Sets the value of field 'defaultCriticalPathTimeout'.
     * 
     * @param defaultCriticalPathTimeout the value of field
     * 'defaultCriticalPathTimeout'.
     */
    public void setDefaultCriticalPathTimeout(final Integer timeout) {
        m_defaultCriticalPathTimeout = timeout;
    }

    /**
     * Sets the value of field 'serverName'.
     * 
     * @param serverName the value of field 'serverName'.
     */
    public void setServerName(final String serverName) {
        m_serverName = serverName;
    }

    /**
     * Sets the value of field 'verifyServer'. The field
     * 'verifyServer' has the following description: A flag to
     * indicate if poller has to identify the nms
     *  server to restrict services to poll.
     * 
     * @param verifyServer the value of field 'verifyServer'.
     */
    public void setVerifyServer(final String verifyServer) {
        m_verifyServer = verifyServer;
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
     * org.opennms.netmgt.config.server.LocalServer
     */
    public static LocalServer unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (LocalServer) Unmarshaller.unmarshal(org.opennms.netmgt.config.server.LocalServer.class, reader);
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
        final int prime = 59;
        int result = 1;
        result = prime * result + ((m_defaultCriticalPathIp == null) ? 0 : m_defaultCriticalPathIp.hashCode());
        result = prime * result + ((m_defaultCriticalPathRetries == null) ? 0 : m_defaultCriticalPathRetries.hashCode());
        result = prime * result + ((m_defaultCriticalPathService == null) ? 0 : m_defaultCriticalPathService.hashCode());
        result = prime * result + ((m_defaultCriticalPathTimeout == null) ? 0 : m_defaultCriticalPathTimeout.hashCode());
        result = prime * result + ((m_serverName == null) ? 0 : m_serverName.hashCode());
        result = prime * result + ((m_verifyServer == null) ? 0 : m_verifyServer.hashCode());
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
        if (!(obj instanceof LocalServer)) {
            return false;
        }
        final LocalServer other = (LocalServer) obj;
        if (m_defaultCriticalPathIp == null) {
            if (other.m_defaultCriticalPathIp != null) {
                return false;
            }
        } else if (!m_defaultCriticalPathIp.equals(other.m_defaultCriticalPathIp)) {
            return false;
        }
        if (m_defaultCriticalPathRetries == null) {
            if (other.m_defaultCriticalPathRetries != null) {
                return false;
            }
        } else if (!m_defaultCriticalPathRetries.equals(other.m_defaultCriticalPathRetries)) {
            return false;
        }
        if (m_defaultCriticalPathService == null) {
            if (other.m_defaultCriticalPathService != null) {
                return false;
            }
        } else if (!m_defaultCriticalPathService.equals(other.m_defaultCriticalPathService)) {
            return false;
        }
        if (m_defaultCriticalPathTimeout == null) {
            if (other.m_defaultCriticalPathTimeout != null) {
                return false;
            }
        } else if (!m_defaultCriticalPathTimeout.equals(other.m_defaultCriticalPathTimeout)) {
            return false;
        }
        if (m_serverName == null) {
            if (other.m_serverName != null) {
                return false;
            }
        } else if (!m_serverName.equals(other.m_serverName)) {
            return false;
        }
        if (m_verifyServer == null) {
            if (other.m_verifyServer != null) {
                return false;
            }
        } else if (!m_verifyServer.equals(other.m_verifyServer)) {
            return false;
        }
        return true;
    }

}
