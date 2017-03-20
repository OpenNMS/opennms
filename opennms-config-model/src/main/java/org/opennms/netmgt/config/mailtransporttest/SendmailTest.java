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

package org.opennms.netmgt.config.mailtransporttest;

  import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
    private static final long serialVersionUID = 1L;

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
    @XmlElement(name="sendmail-host", required = true)
    private SendmailHost m_sendmailHost;

    /**
     * Basically attributes that help setup the javamailer's
     * confusion set of properties.
     *  
     *  
     */
    @XmlElement(name="sendmail-protocol", required = true)
    private SendmailProtocol m_sendmailProtocol;

    /**
     * Define the to, from, subject, and body of a message. If not
     * defined, one will be defined
     *  for your benefit (or confusion ;-)
     *  
     *  
     */
    @XmlElement(name="sendmail-message", required = true)
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

    public SendmailTest(final Long attemptInterval, final Boolean debug, final Boolean useAuthentication, final Boolean useJmta) {
        super();
        m_attemptInterval = attemptInterval;
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
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;
        
        if (obj instanceof SendmailTest) {
            final SendmailTest that = (SendmailTest)obj;
            return Objects.equals(this.m_debug, that.m_debug) &&
                    Objects.equals(this.m_useAuthentication, that.m_useAuthentication) &&
                    Objects.equals(this.m_useJmta, that.m_useJmta) &&
                    Objects.equals(this.m_attemptInterval, that.m_attemptInterval) &&
                    Objects.equals(this.m_javamailProperties, that.m_javamailProperties) &&
                    Objects.equals(this.m_sendmailHost, that.m_sendmailHost) &&
                    Objects.equals(this.m_sendmailProtocol, that.m_sendmailProtocol) &&
                    Objects.equals(this.m_sendmailMessage, that.m_sendmailMessage) &&
                    Objects.equals(this.m_userAuth, that.m_userAuth);
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
     * Method getJavamailPropertyCollection.Returns a reference to
     * 'm_javamailProperties'. No type checking is performed on
     * any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<JavamailProperty> getJavamailPropertyCollection() {
        return new ArrayList<>(m_javamailProperties);
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
    public Optional<UserAuth> getUserAuth() {
        return Optional.ofNullable(m_userAuth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_debug, m_useAuthentication, m_useJmta, m_attemptInterval, m_javamailProperties, m_sendmailHost, m_sendmailProtocol, m_sendmailMessage, m_userAuth);
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
     * Sets the value of 'm_javamailProperties' by copying the
     * given Vector. All elements will be checked for type safety.
     * 
     * @param javamailProperties the Vector to copy.
     */
    public void setJavamailProperties(final List<JavamailProperty> javamailProperties) {
        if (javamailProperties == null) {
            m_javamailProperties.clear();
        } else if (javamailProperties != m_javamailProperties) {
            m_javamailProperties.clear();
            m_javamailProperties.addAll(javamailProperties);
        }
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
        if (sendmailHost == null) {
            throw new IllegalArgumentException("'sendmail-host' is a required element!");
        }
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
        if (sendmailMessage == null) {
            throw new IllegalArgumentException("'sendmail-message' is a required element!");
        }
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
        if (sendmailProtocol == null) {
            throw new IllegalArgumentException("'sendmail-protocol' is a required element!");
        }
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

}
