/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2017 The OpenNMS Group, Inc.
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

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * This entity defines the test for sending mail. Attributes are used to
 * derive values of java mail properties, or, they can be specified directly
 * as key value pairs. Attributes will are easier to read but there isn't 
 * an attribute for every javamail property possible (some are fairly obscure).
 */

@XmlRootElement(name="sendmail-test")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("mail-transport-test.xsd")
public class SendmailTest implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="debug")
    private Boolean m_debug;

    @XmlAttribute(name="use-authentication")
    private Boolean m_useAuthentication;

    @XmlAttribute(name="use-jmta")
    private Boolean m_useJmta;

    @XmlAttribute(name="attempt-interval")
    private Long m_attemptInterval;

    /**
     * Use these name value pairs to configure freeform properties
     * from the JavaMail class.
     */
    @XmlElement(name="javamail-property")
    private List<JavamailProperty> m_javamailProperties = new ArrayList<>();

    /**
     * Define the host and port of the sendmail server. If you
     * don't, defaults will be used and
     *  ${ipaddr} is replaced with the IP address of the service.
     */
    @XmlElement(name="sendmail-host", required = true)
    private SendmailHost m_sendmailHost;

    /**
     * Basically attributes that help setup the javamailer's
     * confusion set of properties.
     */
    @XmlElement(name="sendmail-protocol", required = true)
    private SendmailProtocol m_sendmailProtocol;

    /**
     * Define the to, from, subject, and body of a message. If not
     * defined, one will be defined
     *  for your benefit (or confusion ;-)
     */
    @XmlElement(name="sendmail-message", required = true)
    private SendmailMessage m_sendmailMessage;

    /**
     * Configure user based authentication.
     */
    @XmlElement(name="user-auth")
    private UserAuth m_userAuth;

    public SendmailTest() {
    }

    public SendmailTest(final Long attemptInterval, final Boolean debug, final Boolean useAuthentication, final Boolean useJmta) {
        setAttemptInterval(attemptInterval);
        setDebug(debug);
        setUseAuthentication(useAuthentication);
        setUseJmta(useJmta);
    }

    public Boolean getDebug() {
        return m_debug == null? true : m_debug;
    }

    public void setDebug(final Boolean debug) {
        m_debug = debug;
    }

    public Boolean getUseAuthentication() {
        return m_useAuthentication == null? Boolean.FALSE : m_useAuthentication;
    }

    public void setUseAuthentication(final Boolean useAuthentication) {
        m_useAuthentication = useAuthentication;
    }

    public Boolean getUseJmta() {
        return m_useJmta == null? Boolean.TRUE : m_useJmta;
    }

    public void setUseJmta(final Boolean useJmta) {
        m_useJmta = useJmta;
    }

    public Long getAttemptInterval() {
        return m_attemptInterval == null? 3000L : m_attemptInterval;
    }

    public void setAttemptInterval(final Long attemptInterval) {
        m_attemptInterval = attemptInterval;
    }

    public List<JavamailProperty> getJavamailProperties() {
        return m_javamailProperties;
    }

    public void setJavamailProperties(final List<JavamailProperty> javamailProperties) {
        if (javamailProperties == m_javamailProperties) return;
        m_javamailProperties.clear();
        if (javamailProperties != null) m_javamailProperties.addAll(javamailProperties);
    }

    public void addJavamailProperty(final JavamailProperty property) {
        m_javamailProperties.add(property);
    }

    public void addJavamailProperty(final String name, final String value) {
        m_javamailProperties.add(new JavamailProperty(name, value));
    }

    public boolean removeJavamailProperty(final JavamailProperty javamailProperty) {
        return m_javamailProperties.remove(javamailProperty);
    }

    public SendmailHost getSendmailHost() {
        return m_sendmailHost;
    }

    public void setSendmailHost(final SendmailHost sendmailHost) {
        m_sendmailHost = ConfigUtils.assertNotNull(sendmailHost, "sendmail-host");
    }

    public void setSendmailHost(final String host, final Long port) {
        m_sendmailHost = new SendmailHost(host, port);
    }

    public SendmailProtocol getSendmailProtocol() {
        return m_sendmailProtocol;
    }

    public void setSendmailProtocol(final SendmailProtocol sendmailProtocol) {
        m_sendmailProtocol = ConfigUtils.assertNotNull(sendmailProtocol, "sendmail-protocol");
    }

    public SendmailMessage getSendmailMessage() {
        return m_sendmailMessage;
    }

    public void setSendmailMessage(final SendmailMessage sendmailMessage) {
        m_sendmailMessage = ConfigUtils.assertNotNull(sendmailMessage, "sendmail-message");
    }

    public Optional<UserAuth> getUserAuth() {
        return Optional.ofNullable(m_userAuth);
    }

    public void setUserAuth(final UserAuth userAuth) {
        m_userAuth = userAuth;
    }

    public void setUserAuth(final String username, final String password) {
        m_userAuth = new UserAuth(username, password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_debug, m_useAuthentication, m_useJmta, m_attemptInterval, m_javamailProperties, m_sendmailHost, m_sendmailProtocol, m_sendmailMessage, m_userAuth);
    }

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

}
