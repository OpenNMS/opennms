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

@XmlRootElement(name="readmail-test")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("mail-transport-test.xsd")
public class ReadmailTest implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="debug")
    private Boolean m_debug;

    @XmlAttribute(name="mail-folder")
    private String m_mailFolder;

    @XmlAttribute(name="subject-match")
    private String m_subjectMatch;

    @XmlAttribute(name="attempt-interval")
    private Long m_attemptInterval;

    @XmlAttribute(name="delete-all-mail")
    private Boolean m_deleteAllMail;

    /**
     * Use these name value pairs to configure freeform properties
     * from the JavaMail class.
     */
    @XmlElement(name="javamail-property")
    private List<JavamailProperty> m_javamailProperties = new ArrayList<JavamailProperty>();

    /**
     * Define the host and port of the sendmail server. If you
     * don't, defaults will be used and ${ipaddr} is replaced with the IP address of the service.
     */
    @XmlElement(name="readmail-host", required=true)
    private ReadmailHost m_readmailHost;

    /**
     * Configure user based authentication.
     */
    @XmlElement(name="user-auth")
    private UserAuth m_userAuth;

    public ReadmailTest() {
    }

    public ReadmailTest(final Long attemptInterval, final Boolean debug, final String mailFolder, final String subjectMatch, final Boolean deleteAllMail) {
        setAttemptInterval(attemptInterval);
        setDebug(debug);
        setMailFolder(mailFolder);
        setSubjectMatch(subjectMatch);
        setDeleteAllMail(deleteAllMail);
    }

    public Boolean getDebug() {
        return m_debug == null? Boolean.TRUE : m_debug;
    }

    public void setDebug(final Boolean debug) {
        m_debug = debug;
    }

    public String getMailFolder() {
        return m_mailFolder == null? "INBOX" : m_mailFolder;
    }

    public void setMailFolder(final String mailFolder) {
        m_mailFolder = ConfigUtils.normalizeString(mailFolder);
    }

    public String getSubjectMatch() {
        return m_subjectMatch;
    }

    public void setSubjectMatch(final String subjectMatch) {
        m_subjectMatch = ConfigUtils.normalizeString(subjectMatch);
    }

    public Long getAttemptInterval() {
        return m_attemptInterval == null? 1000L : m_attemptInterval;
    }

    public void setAttemptInterval(final Long attemptInterval) {
        m_attemptInterval = attemptInterval;
    }

    public Boolean getDeleteAllMail() {
        return m_deleteAllMail == null? Boolean.FALSE : m_deleteAllMail;
    }

    public void setDeleteAllMail(final Boolean deleteAllMail) {
        m_deleteAllMail = deleteAllMail;
    }

    public List<JavamailProperty> getJavamailProperties() {
        return m_javamailProperties;
    }

    public void setJavamailProperties(final List<JavamailProperty> javamailProperties) {
        if (javamailProperties == m_javamailProperties) return;
        m_javamailProperties.clear();
        if (javamailProperties != null) m_javamailProperties.addAll(javamailProperties);
    }

    public void addJavamailProperty(final JavamailProperty javamailProperty) {
        m_javamailProperties.add(javamailProperty);
    }

    public void addJavamailProperty(final String name, final String value) {
        m_javamailProperties.add(new JavamailProperty(name, value));
    }

    public ReadmailHost getReadmailHost() {
        return m_readmailHost;
    }

    public void setReadmailHost(final ReadmailHost readmailHost) {
        m_readmailHost = ConfigUtils.assertNotEmpty(readmailHost, "readmail-host");
    }

    public Optional<UserAuth> getUserAuth() {
        return Optional.ofNullable(m_userAuth);
    }

    public void setUserAuth(final UserAuth userAuth) {
        m_userAuth = userAuth;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_debug, m_mailFolder, m_subjectMatch, m_attemptInterval, m_deleteAllMail, m_javamailProperties, m_readmailHost, m_userAuth);
    }

    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;

        if (obj instanceof ReadmailTest) {
            final ReadmailTest that = (ReadmailTest)obj;
            return Objects.equals(this.m_debug, that.m_debug) &&
                    Objects.equals(this.m_mailFolder, that.m_mailFolder) &&
                    Objects.equals(this.m_subjectMatch, that.m_subjectMatch) &&
                    Objects.equals(this.m_attemptInterval, that.m_attemptInterval) &&
                    Objects.equals(this.m_deleteAllMail, that.m_deleteAllMail) &&
                    Objects.equals(this.m_javamailProperties, that.m_javamailProperties) &&
                    Objects.equals(this.m_readmailHost, that.m_readmailHost) &&
                    Objects.equals(this.m_userAuth, that.m_userAuth);
        }
        return false;
    }

    public void setUserAuth(final String username, final String password) {
        m_userAuth = new UserAuth(username, password);
    }

}
