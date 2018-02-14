/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.javamail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * The Class ReadmailConfig.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="readmail-config", namespace="http://xmlns.opennms.org/xsd/config/javamail-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("javamail-configuration.xsd")
public class ReadmailConfig implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="debug")
    private Boolean m_debug;

    @XmlAttribute(name="mail-folder")
    private String m_mailFolder;

    @XmlAttribute(name="attempt-interval")
    private Long m_attemptInterval;

    @XmlAttribute(name="delete-all-mail")
    private Boolean m_deleteAllMail;

    @XmlAttribute(name="name", required=true)
    private String m_name;

    /**
     * Use these name value pairs to configure free-form properties from the JavaMail class.
     */
    @XmlElement(name="javamail-property")
    private List<JavamailProperty> m_javamailProperties = new ArrayList<>();

    /**
     * Define the host and port of a service for reading email.
     */
    @XmlElement(name="readmail-host", required=true)
    private ReadmailHost m_readmailHost;

    /**
     * Configure user based authentication.
     */
    @XmlElement(name="user-auth", required=true)
    private UserAuth m_userAuth;

    public ReadmailConfig() {
    }

    public Boolean isDebug() {
        return m_debug == null ? Boolean.TRUE : m_debug;
    }

    public void setDebug(final Boolean debug) {
        m_debug = debug;
    }

    public String getMailFolder() {
        return m_mailFolder == null ? "INBOX" : m_mailFolder;
    }

    public void setMailFolder(final String mailFolder) {
        m_mailFolder = mailFolder;
    }

    public Long getAttemptInterval() {
        return m_attemptInterval == null ? 1000l : m_attemptInterval;
    }

    public void setAttemptInterval(final Long attemptInterval) {
        m_attemptInterval = attemptInterval;
    }

    public Boolean isDeleteAllMail() {
        return m_deleteAllMail ? Boolean.FALSE : m_deleteAllMail;
    }

    public void setDeleteAllMail(final Boolean deleteAllMail) {
        m_deleteAllMail = deleteAllMail;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public List<JavamailProperty> getJavamailProperties() {
        return m_javamailProperties;
    }

    public void setJavamailProperties(final List<JavamailProperty> properties) {
        if (properties == m_javamailProperties) return;
        m_javamailProperties.clear();
        if (properties != null) m_javamailProperties.addAll(properties);
    }

    public ReadmailHost getReadmailHost() {
        return m_readmailHost;
    }

    public void setReadmailHost(final ReadmailHost readmailHost) {
        m_readmailHost = ConfigUtils.assertNotNull(readmailHost, "readmail-host");
    }

    public UserAuth getUserAuth() {
        return m_userAuth;
    }

    public void setUserAuth(final UserAuth userAuth) {
        m_userAuth = ConfigUtils.assertNotNull(userAuth, "user-auth");
    }

    @Override()
    public int hashCode() {
        return Objects.hash(m_debug,
                            m_mailFolder,
                            m_attemptInterval,
                            m_deleteAllMail,
                            m_name,
                            m_javamailProperties,
                            m_readmailHost,
                            m_userAuth);
    }

    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ReadmailConfig) {
            final ReadmailConfig that = (ReadmailConfig)obj;
            return Objects.equals(this.m_debug, that.m_debug)
                    && Objects.equals(this.m_mailFolder, that.m_mailFolder)
                    && Objects.equals(this.m_attemptInterval, that.m_attemptInterval)
                    && Objects.equals(this.m_deleteAllMail, that.m_deleteAllMail)
                    && Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_javamailProperties, that.m_javamailProperties)
                    && Objects.equals(this.m_readmailHost, that.m_readmailHost)
                    && Objects.equals(this.m_userAuth, that.m_userAuth);
        }
        return false;
    }

}
