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
 * Define the to, from, subject, and body of a message. If not
 * defined, one will be defined
 *  for your benefit (or confusion ;-)
 */

@XmlRootElement(name="sendmail-message")
@XmlAccessorType(XmlAccessType.FIELD)
public class SendmailMessage implements Serializable {
    private static final long serialVersionUID = -7401307903017394619L;

    /**
     * Field m_to.
     */
    @XmlAttribute(name="to")
    private String m_to;

    /**
     * Field m_from.
     */
    @XmlAttribute(name="from")
    private String m_from;

    /**
     * Field m_subject.
     */
    @XmlAttribute(name="subject")
    private String m_subject;

    /**
     * Field m_body.
     */
    @XmlAttribute(name="body")
    private String m_body;

    public SendmailMessage() {
        super();
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
        
        if (obj instanceof SendmailMessage) {
            final SendmailMessage temp = (SendmailMessage)obj;
            if (m_to != null) {
                if (temp.m_to == null) {
                    return false;
                } else if (!(m_to.equals(temp.m_to))) {
                    return false;
                }
            } else if (temp.m_to != null) {
                return false;
            }
            if (m_from != null) {
                if (temp.m_from == null) {
                    return false;
                } else if (!(m_from.equals(temp.m_from))) {
                    return false;
                }
            } else if (temp.m_from != null) {
                return false;
            }
            if (m_subject != null) {
                if (temp.m_subject == null) {
                    return false;
                } else if (!(m_subject.equals(temp.m_subject))) {
                    return false;
                }
            } else if (temp.m_subject != null) {
                return false;
            }
            if (m_body != null) {
                if (temp.m_body == null) {
                    return false;
                } else if (!(m_body.equals(temp.m_body))) {
                    return false;
                }
            } else if (temp.m_body != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'body'.
     * 
     * @return the value of field 'Body'.
     */
    public String getBody() {
        return m_body == null? "This is an OpenNMS test message." : m_body;
    }

    /**
     * Returns the value of field 'from'.
     * 
     * @return the value of field 'From'.
     */
    public String getFrom() {
        return m_from == null? "root@[127.0.0.1]" : m_from;
    }

    /**
     * Returns the value of field 'subject'.
     * 
     * @return the value of field 'Subject'.
     */
    public String getSubject() {
        return m_subject == null? "OpenNMS Test Message" : m_subject;
    }

    /**
     * Returns the value of field 'to'.
     * 
     * @return the value of field 'To'.
     */
    public String getTo() {
        return m_to == null? "root@localhost" : m_to;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;
        
        if (m_to != null) {
           result = 37 * result + m_to.hashCode();
        }
        if (m_from != null) {
           result = 37 * result + m_from.hashCode();
        }
        if (m_subject != null) {
           result = 37 * result + m_subject.hashCode();
        }
        if (m_body != null) {
           result = 37 * result + m_body.hashCode();
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
     * Sets the value of field 'body'.
     * 
     * @param body the value of field 'body'.
     */
    public void setBody(final String body) {
        m_body = body;
    }

    /**
     * Sets the value of field 'from'.
     * 
     * @param from the value of field 'from'.
     */
    public void setFrom(final String from) {
        m_from = from;
    }

    /**
     * Sets the value of field 'subject'.
     * 
     * @param subject the value of field 'subject'.
     */
    public void setSubject(final String subject) {
        m_subject = subject;
    }

    /**
     * Sets the value of field 'to'.
     * 
     * @param to the value of field 'to'.
     */
    public void setTo(final String to) {
        m_to = to;
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
     * SendmailMessage
     */
    public static SendmailMessage unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (SendmailMessage) Unmarshaller.unmarshal(SendmailMessage.class, reader);
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
