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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

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
 * This entity defines the test for sending mail. Attributes are
 * used to
 *  derive values of java mail properties, or, they can be
 * specified directly
 *  as key value pairs. Attributes will are easier to read but
 * there isn't 
 *  an attribute for every javamail property possible (some are
 * fairly obscure).
 */

@XmlRootElement(name="sendmail-test")
@XmlAccessorType(XmlAccessType.FIELD)
public class SendmailTest implements Serializable {
    private static final long serialVersionUID = 8076651227806492951L;

    private static final JavamailProperty[] EMPTY_LIST_OF_JAVAMAIL_PROPERTIES = new JavamailProperty[0];

    /**
     * Field m_debug.
     */
    @XmlAttribute(name="debug")
    private Boolean m_debug;

    /**
     * Field m_useAuthentication.
     */
    @XmlAttribute(name="use-authentication")
    private Boolean m_useAuthentication;

    /**
     * Field m_useJmta.
     */
    @XmlAttribute(name="use-jmta")
    private Boolean m_useJmta;

    /**
     * Field m_attemptInterval.
     */
    @XmlAttribute(name="attempt-interval")
    private Long m_attemptInterval;

    /**
     * Use these name value pairs to configure freeform properties
     * from the JavaMail class.
     *  
     *  
     */
    @XmlElement(name="javamail-property")
    private List<JavamailProperty> m_javamailProperties = new ArrayList<JavamailProperty>();

    /**
     * Define the host and port of the sendmail server. If you
     * don't, defaults will be used and
     *  ${ipaddr} is replaced with the IP address of the service.
     *  
     *  
     */
    @XmlElement(name="sendmail-host")
    private SendmailHost m_sendmailHost;

    /**
     * Basically attributes that help setup the javamailer's
     * confusion set of properties.
     *  
     *  
     */
    @XmlElement(name="sendmail-protocol")
    private SendmailProtocol m_sendmailProtocol;

    /**
     * Define the to, from, subject, and body of a message. If not
     * defined, one will be defined
     *  for your benefit (or confusion ;-)
     *  
     *  
     */
    @XmlElement(name="sendmail-message")
    private SendmailMessage m_sendmailMessage;

    /**
     * Configure user based authentication.
     *  
     *  
     */
    @XmlElement(name="user-auth")
    private UserAuth m_userAuth;

    public SendmailTest() {
        super();
    }

    public SendmailTest(final Long attemptInternal, final Boolean debug, final Boolean useAuthentication, final Boolean useJmta) {
        super();
        m_attemptInterval = attemptInternal;
        m_debug = debug;
        m_useAuthentication = useAuthentication;
        m_useJmta = useJmta;
    }

    /**
     * 
     * 
     * @param javamailProperty
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addJavamailProperty(final JavamailProperty javamailProperty) throws IndexOutOfBoundsException {
        m_javamailProperties.add(javamailProperty);
    }

    /**
     * 
     * 
     * @param index
     * @param javamailProperty
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addJavamailProperty(final int index, final JavamailProperty javamailProperty) throws IndexOutOfBoundsException {
        m_javamailProperties.add(index, javamailProperty);
    }

    public void addJavamailProperty(final String name, final String value) {
        m_javamailProperties.add(new JavamailProperty(name, value));
    }

    public void deleteAttemptInterval() {
        m_attemptInterval = null;
    }

    public void deleteDebug() {
        m_debug = null;
    }

    public void deleteUseAuthentication() {
        m_userAuth = null;
    }

    public void deleteUseJmta() {
        m_useJmta = null;
    }

    /**
     * Method enumerateJavamailProperty.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<JavamailProperty> enumerateJavamailProperty() {
        return Collections.enumeration(m_javamailProperties);
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
        
        if (obj instanceof SendmailTest) {
            final SendmailTest temp = (SendmailTest)obj;

            if (m_debug != null) {
                if (temp.m_debug == null) {
                    return false;
                } else if (!(m_debug.equals(temp.m_debug))) {
                    return false;
                }
            } else if (temp.m_debug != null) {
                return false;
            }
            if (m_useAuthentication != null) {
                if (temp.m_useAuthentication == null) {
                    return false;
                } else if (!(m_useAuthentication.equals(temp.m_useAuthentication))) {
                    return false;
                }
            } else if (temp.m_useAuthentication != null) {
                return false;
            }
            if (m_useJmta != null) {
                if (temp.m_useJmta == null) {
                    return false;
                } else if (!(m_useJmta.equals(temp.m_useJmta))) {
                    return false;
                }
            } else if (temp.m_useJmta != null) {
                return false;
            }
            if (m_attemptInterval != null) {
                if (temp.m_attemptInterval == null) {
                    return false;
                } else if (!(m_attemptInterval.equals(temp.m_attemptInterval))) {
                    return false;
                }
            } else if (temp.m_attemptInterval != null) {
                return false;
            }
            if (m_javamailProperties != null) {
                if (temp.m_javamailProperties == null) {
                    return false;
                } else if (!(m_javamailProperties.equals(temp.m_javamailProperties))) {
                    return false;
                }
            } else if (temp.m_javamailProperties != null) {
                return false;
            }
            if (m_sendmailHost != null) {
                if (temp.m_sendmailHost == null) {
                    return false;
                } else if (!(m_sendmailHost.equals(temp.m_sendmailHost))) {
                    return false;
                }
            } else if (temp.m_sendmailHost != null) {
                return false;
            }
            if (m_sendmailProtocol != null) {
                if (temp.m_sendmailProtocol == null) {
                    return false;
                } else if (!(m_sendmailProtocol.equals(temp.m_sendmailProtocol))) {
                    return false;
                }
            } else if (temp.m_sendmailProtocol != null) {
                return false;
            }
            if (m_sendmailMessage != null) {
                if (temp.m_sendmailMessage == null) {
                    return false;
                } else if (!(m_sendmailMessage.equals(temp.m_sendmailMessage))) {
                    return false;
                }
            } else if (temp.m_sendmailMessage != null) {
                return false;
            }
            if (m_userAuth != null) {
                if (temp.m_userAuth == null) {
                    return false;
                } else if (!(m_userAuth.equals(temp.m_userAuth))) {
                    return false;
                }
            } else if (temp.m_userAuth != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'attemptInterval'.
     * 
     * @return the value of field 'AttemptInterval'.
     */
    public Long getAttemptInterval() {
        return m_attemptInterval == null? 3000L : m_attemptInterval;
    }

    /**
     * Returns the value of field 'debug'.
     * 
     * @return the value of field 'Debug'.
     */
    public Boolean getDebug() {
        return m_debug == null? true : m_debug;
    }

    /**
     * Method getJavamailProperty.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * JavamailProperty
     * at the given index
     */
    public JavamailProperty getJavamailProperty(final int index) throws IndexOutOfBoundsException {
        return m_javamailProperties.get(index);
    }

    /**
     * Method getJavamailProperty.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public JavamailProperty[] getJavamailProperty() {
        return m_javamailProperties.toArray(EMPTY_LIST_OF_JAVAMAIL_PROPERTIES);
    }

    /**
     * Method getJavamailPropertyCollection.Returns a reference to
     * 'm_javamailProperties'. No type checking is performed on
     * any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<JavamailProperty> getJavamailPropertyCollection() {
        return new ArrayList<JavamailProperty>(m_javamailProperties);
    }

    /**
     * Method getJavamailPropertyCount.
     * 
     * @return the size of this collection
     */
    public int getJavamailPropertyCount() {
        return m_javamailProperties.size();
    }

    /**
     * Returns the value of field 'sendmailHost'. The field
     * 'sendmailHost' has the following description: Define the
     * host and port of the sendmail server. If you don't, defaults
     * will be used and
     *  ${ipaddr} is replaced with the IP address of the service.
     *  
     *  
     * 
     * @return the value of field 'SendmailHost'.
     */
    public SendmailHost getSendmailHost() {
        return m_sendmailHost;
    }

    /**
     * Returns the value of field 'sendmailMessage'. The field
     * 'sendmailMessage' has the following description: Define the
     * to, from, subject, and body of a message. If not defined,
     * one will be defined
     *  for your benefit (or confusion ;-)
     *  
     *  
     * 
     * @return the value of field 'SendmailMessage'.
     */
    public SendmailMessage getSendmailMessage() {
        return m_sendmailMessage;
    }

    /**
     * Returns the value of field 'sendmailProtocol'. The field
     * 'sendmailProtocol' has the following description: Basically
     * attributes that help setup the javamailer's confusion set of
     * properties.
     *  
     *  
     * 
     * @return the value of field 'SendmailProtocol'.
     */
    public SendmailProtocol getSendmailProtocol() {
        return m_sendmailProtocol;
    }

    /**
     * Returns the value of field 'useAuthentication'.
     * 
     * @return the value of field 'UseAuthentication'.
     */
    public Boolean getUseAuthentication() {
        return m_useAuthentication == null? false : m_useAuthentication;
    }

    /**
     * Returns the value of field 'useJmta'.
     * 
     * @return the value of field 'UseJmta'.
     */
    public Boolean getUseJmta() {
        return m_useJmta == null? true : m_useJmta;
    }

    /**
     * Returns the value of field 'userAuth'. The field 'userAuth'
     * has the following description: Configure user based
     * authentication.
     *  
     *  
     * 
     * @return the value of field 'UserAuth'.
     */
    public UserAuth getUserAuth() {
        return m_userAuth;
    }

    /**
     * Method hasAttemptInterval.
     * 
     * @return true if at least one AttemptInterval has been added
     */
    public boolean hasAttemptInterval() {
        return m_attemptInterval != null;
    }

    /**
     * Method hasDebug.
     * 
     * @return true if at least one Debug has been added
     */
    public boolean hasDebug() {
        return m_debug != null;
    }

    /**
     * Method hasUseAuthentication.
     * 
     * @return true if at least one UseAuthentication has been added
     */
    public boolean hasUseAuthentication() {
        return m_useAuthentication != null;
    }

    /**
     * Method hasUseJmta.
     * 
     * @return true if at least one UseJmta has been added
     */
    public boolean hasUseJmta() {
        return m_useJmta != null;
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
        
        if (m_debug != null) {
            result = 37 * result + m_debug.hashCode();
         }
        if (m_useAuthentication != null) {
            result = 37 * result + m_useAuthentication.hashCode();
         }
        if (m_useJmta != null) {
            result = 37 * result + m_useJmta.hashCode();
         }
        if (m_attemptInterval != null) {
            result = 37 * result + m_attemptInterval.hashCode();
         }
        if (m_javamailProperties != null) {
           result = 37 * result + m_javamailProperties.hashCode();
        }
        if (m_sendmailHost != null) {
           result = 37 * result + m_sendmailHost.hashCode();
        }
        if (m_sendmailProtocol != null) {
           result = 37 * result + m_sendmailProtocol.hashCode();
        }
        if (m_sendmailMessage != null) {
           result = 37 * result + m_sendmailMessage.hashCode();
        }
        if (m_userAuth != null) {
           result = 37 * result + m_userAuth.hashCode();
        }
        
        return result;
    }

    /**
     * Returns the value of field 'debug'.
     * 
     * @return the value of field 'Debug'.
     */
    public Boolean isDebug() {
        return m_debug == null? true : m_debug;
    }

    /**
     * Returns the value of field 'useAuthentication'.
     * 
     * @return the value of field 'UseAuthentication'.
     */
    public Boolean isUseAuthentication() {
        return m_useAuthentication == null? false : m_useAuthentication;
    }

    /**
     * Returns the value of field 'useJmta'.
     * 
     * @return the value of field 'UseJmta'.
     */
    public Boolean isUseJmta() {
        return m_useJmta == null? true : m_useJmta;
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
     * Method iterateJavamailProperty.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<JavamailProperty> iterateJavamailProperty() {
        return m_javamailProperties.iterator();
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
     */
    public void removeAllJavamailProperty() {
        m_javamailProperties.clear();
    }

    /**
     * Method removeJavamailProperty.
     * 
     * @param javamailProperty
     * @return true if the object was removed from the collection.
     */
    public boolean removeJavamailProperty(final JavamailProperty javamailProperty) {
        return m_javamailProperties.remove(javamailProperty);
    }

    /**
     * Method removeJavamailPropertyAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public JavamailProperty removeJavamailPropertyAt(final int index) {
        return m_javamailProperties.remove(index);
    }

    /**
     * Sets the value of field 'attemptInterval'.
     * 
     * @param attemptInterval the value of field 'attemptInterval'.
     */
    public void setAttemptInterval(final Long attemptInterval) {
        m_attemptInterval = attemptInterval;
    }

    /**
     * Sets the value of field 'debug'.
     * 
     * @param debug the value of field 'debug'.
     */
    public void setDebug(final Boolean debug) {
        m_debug = debug;
    }

    /**
     * 
     * 
     * @param index
     * @param javamailProperty
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setJavamailProperty(final int index, final JavamailProperty javamailProperty) throws IndexOutOfBoundsException {
        m_javamailProperties.set(index, javamailProperty);
    }

    /**
     * 
     * 
     * @param javamailProperties
     */
    public void setJavamailProperty(final JavamailProperty[] javamailProperties) {
        m_javamailProperties.clear();
        for (final JavamailProperty prop : javamailProperties) {
            m_javamailProperties.add(prop);
        }
    }

    /**
     * Sets the value of 'm_javamailProperties' by copying the
     * given Vector. All elements will be checked for type safety.
     * 
     * @param javamailProperties the Vector to copy.
     */
    public void setJavamailProperty(final List<JavamailProperty> javamailProperties) {
        if (javamailProperties != m_javamailProperties) {
            m_javamailProperties.clear();
            m_javamailProperties.addAll(javamailProperties);
        }
    }

    /**
     * Sets the value of 'm_javamailProperties' by setting it to
     * the given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param javamailProperties the Vector to set.
     */
    public void setJavamailPropertyCollection(final List<JavamailProperty> javamailProperties) {
        m_javamailProperties = new ArrayList<JavamailProperty>(javamailProperties);
    }

    /**
     * Sets the value of field 'sendmailHost'. The field
     * 'sendmailHost' has the following description: Define the
     * host and port of the sendmail server. If you don't, defaults
     * will be used and
     *  ${ipaddr} is replaced with the IP address of the service.
     *  
     *  
     * 
     * @param sendmailHost the value of field 'sendmailHost'.
     */
    public void setSendmailHost(final SendmailHost sendmailHost) {
        m_sendmailHost = sendmailHost;
    }

    public void setSendmailHost(final String host, final Long port) {
        m_sendmailHost = new SendmailHost(host, port);
    }

    /**
     * Sets the value of field 'sendmailMessage'. The field
     * 'sendmailMessage' has the following description: Define the
     * to, from, subject, and body of a message. If not defined,
     * one will be defined
     *  for your benefit (or confusion ;-)
     *  
     *  
     * 
     * @param sendmailMessage the value of field 'sendmailMessage'.
     */
    public void setSendmailMessage(final SendmailMessage sendmailMessage) {
        m_sendmailMessage = sendmailMessage;
    }

    /**
     * Sets the value of field 'sendmailProtocol'. The field
     * 'sendmailProtocol' has the following description: Basically
     * attributes that help setup the javamailer's confusion set of
     * properties.
     *  
     *  
     * 
     * @param sendmailProtocol the value of field 'sendmailProtocol'
     */
    public void setSendmailProtocol(final SendmailProtocol sendmailProtocol) {
        m_sendmailProtocol = sendmailProtocol;
    }

    /**
     * Sets the value of field 'useAuthentication'.
     * 
     * @param useAuthentication the value of field
     * 'useAuthentication'.
     */
    public void setUseAuthentication(final Boolean useAuthentication) {
        m_useAuthentication = useAuthentication;
    }

    /**
     * Sets the value of field 'useJmta'.
     * 
     * @param useJmta the value of field 'useJmta'.
     */
    public void setUseJmta(final Boolean useJmta) {
        m_useJmta = useJmta;
    }

    /**
     * Sets the value of field 'userAuth'. The field 'userAuth' has
     * the following description: Configure user based
     * authentication.
     *  
     *  
     * 
     * @param userAuth the value of field 'userAuth'.
     */
    public void setUserAuth(final UserAuth userAuth) {
        m_userAuth = userAuth;
    }

    public void setUserAuth(final String username, final String password) {
        m_userAuth = new UserAuth(username, password);
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
     * SendmailTest
     */
    public static SendmailTest unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (SendmailTest) Unmarshaller.unmarshal(SendmailTest.class, reader);
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
