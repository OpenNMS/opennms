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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * Use this element to define a mail test with the
 * MailTransportMonitor. Supported
 *  use cases for the sequence are:
 *  
 *  a) Class will test that it can successfully send an email.
 *  b) Class will test that it can successfully connect to a mail
 * server and get mailbox contents.
 *  c) Class will test that it can successfully read a new email
 * message from a mail server.
 *  d) Class will test that it can send an email and read that same
 * email from a mail server.
 *  
 *  The sequence support a max of one send and one receive server.
 * If each are specified, the complete
 *  sequence is delivery of a message from one mail host to
 * another.
 */

@XmlRootElement(name="mail-transport-test")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("mail-transport-test.xsd")
public class MailTransportTest implements Serializable {
    private static final long serialVersionUID = -1213778109324747604L;

    /**
     * Defines a use case for testing mail. If both a send and a
     * read test are
     *  configured, then the use case (d) will be executed.
     *  
     */
    @XmlElement(name="mail-test")
    private MailTest m_mailTest;


      //----------------/
     //- Constructors -/
    //----------------/

    public MailTransportTest() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;
        
        if (obj instanceof MailTransportTest) {
            final MailTransportTest temp = (MailTransportTest)obj;
            if (m_mailTest != null) {
                if (temp.m_mailTest == null) {
                    return false;
                } else if (!(m_mailTest.equals(temp.m_mailTest))) {
                    return false;
                }
            } else if (temp.m_mailTest != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'mailTest'. The field 'mailTest'
     * has the following description: Defines a use case for
     * testing mail. If both a send and a read test are
     *  configured, then the use case (d) will be executed.
     *  
     * 
     * @return the value of field 'MailTest'.
     */
    public MailTest getMailTest() {
        return m_mailTest;
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
        
        if (m_mailTest != null) {
           result = 37 * result + m_mailTest.hashCode();
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
     * Sets the value of field 'mailTest'. The field 'mailTest' has
     * the following description: Defines a use case for testing
     * mail. If both a send and a read test are
     *  configured, then the use case (d) will be executed.
     *  
     * 
     * @param mailTest the value of field 'mailTest'.
     */
    public void setMailTest(final MailTest mailTest) {
        m_mailTest = mailTest;
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
     * MailTransportTest
     */
    public static MailTransportTest unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (MailTransportTest) Unmarshaller.unmarshal(MailTransportTest.class, reader);
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
