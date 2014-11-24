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

@XmlRootElement(name="sendmail-protocol")
@XmlAccessorType(XmlAccessType.FIELD)
public class SendmailProtocol implements Serializable {
    private static final long serialVersionUID = -3167315498187497958L;

    /**
     * Field m_charSet.
     */
    @XmlAttribute(name="char-set")
    private String m_charSet;

    /**
     * Field m_mailer.
     */
    @XmlAttribute(name="mailer")
    private String m_mailer;

    /**
     * Field m_messageContentType.
     */
    @XmlAttribute(name="message-content-type")
    private String m_messageContentType;

    /**
     * Field m_messageEncoding.
     */
    @XmlAttribute(name="message-encoding")
    private String m_messageEncoding;

    /**
     * Field m_quitWait.
     */
    @XmlAttribute(name="quit-wait")
    private Boolean m_quitWait;

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

    public SendmailProtocol() {
        super();
    }

    public void deleteQuitWait() {
        m_quitWait = null;
    }

    public void deleteSslEnable() {
        m_sslEnable = null;
    }

    public void deleteStartTls() {
        m_startTls = null;
    }

    /**
     * Returns the value of field 'charSet'.
     * 
     * @return the value of field 'CharSet'.
     */
    public String getCharSet() {
        return m_charSet == null? "us-ascii" : m_charSet;
    }

    /**
     * Returns the value of field 'mailer'.
     * 
     * @return the value of field 'Mailer'.
     */
    public String getMailer() {
        return m_mailer == null? "smtpsend" : m_mailer;
    }

    /**
     * Returns the value of field 'messageContentType'.
     * 
     * @return the value of field 'MessageContentType'.
     */
    public String getMessageContentType() {
        return m_messageContentType == null? "text/plain" : m_messageContentType;
    }

    /**
     * Returns the value of field 'messageEncoding'.
     * 
     * @return the value of field 'MessageEncoding'.
     */
    public String getMessageEncoding() {
        return m_messageEncoding == null? "7-bit" : m_messageEncoding;
    }

    /**
     * Returns the value of field 'quitWait'.
     * 
     * @return the value of field 'QuitWait'.
     */
    public Boolean getQuitWait() {
        return m_quitWait == null? true : m_quitWait;
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
        return m_transport == null? "smtp" : m_transport;
    }

    /**
     * Method hasQuitWait.
     * 
     * @return true if at least one QuitWait has been added
     */
    public boolean hasQuitWait() {
        return m_quitWait != null;
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
     * Returns the value of field 'quitWait'.
     * 
     * @return the value of field 'QuitWait'.
     */
    public Boolean isQuitWait() {
        return m_quitWait == null? true : m_quitWait;
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
     * Sets the value of field 'charSet'.
     * 
     * @param charSet the value of field 'charSet'.
     */
    public void setCharSet(final String charSet) {
        m_charSet = charSet;
    }

    /**
     * Sets the value of field 'mailer'.
     * 
     * @param mailer the value of field 'mailer'.
     */
    public void setMailer(final String mailer) {
        m_mailer = mailer;
    }

    /**
     * Sets the value of field 'messageContentType'.
     * 
     * @param messageContentType the value of field
     * 'messageContentType'.
     */
    public void setMessageContentType(final String messageContentType) {
        m_messageContentType = messageContentType;
    }

    /**
     * Sets the value of field 'messageEncoding'.
     * 
     * @param messageEncoding the value of field 'messageEncoding'.
     */
    public void setMessageEncoding(final String messageEncoding) {
        m_messageEncoding = messageEncoding;
    }

    /**
     * Sets the value of field 'quitWait'.
     * 
     * @param quitWait the value of field 'quitWait'.
     */
    public void setQuitWait(final Boolean quitWait) {
        m_quitWait = quitWait;
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
     * SendmailProtocol
     */
    public static SendmailProtocol unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (SendmailProtocol) Unmarshaller.unmarshal(SendmailProtocol.class, reader);
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_charSet == null) ? 0 : m_charSet.hashCode());
        result = prime * result + ((m_mailer == null) ? 0 : m_mailer.hashCode());
        result = prime * result + ((m_messageContentType == null) ? 0 : m_messageContentType.hashCode());
        result = prime * result + ((m_messageEncoding == null) ? 0 : m_messageEncoding.hashCode());
        result = prime * result + ((m_quitWait == null) ? 0 : m_quitWait.hashCode());
        result = prime * result + ((m_sslEnable == null) ? 0 : m_sslEnable.hashCode());
        result = prime * result + ((m_startTls == null) ? 0 : m_startTls.hashCode());
        result = prime * result + ((m_transport == null) ? 0 : m_transport.hashCode());
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
        if (!(obj instanceof SendmailProtocol)) {
            return false;
        }
        final SendmailProtocol other = (SendmailProtocol) obj;
        if (m_charSet == null) {
            if (other.m_charSet != null) {
                return false;
            }
        } else if (!m_charSet.equals(other.m_charSet)) {
            return false;
        }
        if (m_mailer == null) {
            if (other.m_mailer != null) {
                return false;
            }
        } else if (!m_mailer.equals(other.m_mailer)) {
            return false;
        }
        if (m_messageContentType == null) {
            if (other.m_messageContentType != null) {
                return false;
            }
        } else if (!m_messageContentType.equals(other.m_messageContentType)) {
            return false;
        }
        if (m_messageEncoding == null) {
            if (other.m_messageEncoding != null) {
                return false;
            }
        } else if (!m_messageEncoding.equals(other.m_messageEncoding)) {
            return false;
        }
        if (m_quitWait == null) {
            if (other.m_quitWait != null) {
                return false;
            }
        } else if (!m_quitWait.equals(other.m_quitWait)) {
            return false;
        }
        if (m_sslEnable == null) {
            if (other.m_sslEnable != null) {
                return false;
            }
        } else if (!m_sslEnable.equals(other.m_sslEnable)) {
            return false;
        }
        if (m_startTls == null) {
            if (other.m_startTls != null) {
                return false;
            }
        } else if (!m_startTls.equals(other.m_startTls)) {
            return false;
        }
        if (m_transport == null) {
            if (other.m_transport != null) {
                return false;
            }
        } else if (!m_transport.equals(other.m_transport)) {
            return false;
        }
        return true;
    }

}
