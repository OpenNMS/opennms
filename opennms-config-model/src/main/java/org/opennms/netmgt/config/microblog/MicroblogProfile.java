/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.microblog;


import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * This entity defines the parameters for a microblog service.
 */
@XmlRootElement(name = "microblog-profile")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("microblog-configuration.xsd")
public class MicroblogProfile implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "service-url", required = true)
    private String m_serviceUrl;

    @XmlAttribute(name = "authen-username")
    private String m_authenUsername;

    @XmlAttribute(name = "authen-password")
    private String m_authenPassword;

    @XmlAttribute(name = "oauth-consumer-key")
    private String m_oauthConsumerKey;

    @XmlAttribute(name = "oauth-consumer-secret")
    private String m_oauthConsumerSecret;

    @XmlAttribute(name = "oauth-access-token")
    private String m_oauthAccessToken;

    @XmlAttribute(name = "oauth-access-token-secret")
    private String m_oauthAccessTokenSecret;

    public MicroblogProfile() {
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getServiceUrl() {
        return m_serviceUrl;
    }

    public void setServiceUrl(final String serviceUrl) {
        m_serviceUrl = ConfigUtils.assertNotEmpty(serviceUrl, "service-url");
    }

    public Optional<String> getAuthenUsername() {
        return Optional.ofNullable(m_authenUsername);
    }

    public void setAuthenUsername(final String authenUsername) {
        m_authenUsername = authenUsername;
    }

    public Optional<String> getAuthenPassword() {
        return Optional.ofNullable(m_authenPassword);
    }

    public void setAuthenPassword(final String authenPassword) {
        m_authenPassword = authenPassword;
    }

    public Optional<String> getOauthConsumerKey() {
        return Optional.ofNullable(this.m_oauthConsumerKey);
    }

    public void setOauthConsumerKey(final String oauthConsumerKey) {
        m_oauthConsumerKey = oauthConsumerKey;
    }

    public Optional<String> getOauthConsumerSecret() {
        return Optional.ofNullable(m_oauthConsumerSecret);
    }

    public void setOauthConsumerSecret(final String oauthConsumerSecret) {
        m_oauthConsumerSecret = oauthConsumerSecret;
    }

    public Optional<String> getOauthAccessToken() {
        return Optional.ofNullable(m_oauthAccessToken);
    }

    public void setOauthAccessToken(final String oauthAccessToken) {
        m_oauthAccessToken = oauthAccessToken;
    }

    public Optional<String> getOauthAccessTokenSecret() {
        return Optional.ofNullable(m_oauthAccessTokenSecret);
    }

    public void setOauthAccessTokenSecret(final String oauthAccessTokenSecret) {
        m_oauthAccessTokenSecret = oauthAccessTokenSecret;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_name, 
                            m_serviceUrl, 
                            m_authenUsername, 
                            m_authenPassword, 
                            m_oauthConsumerKey, 
                            m_oauthConsumerSecret, 
                            m_oauthAccessToken, 
                            m_oauthAccessTokenSecret);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof MicroblogProfile) {
            final MicroblogProfile that = (MicroblogProfile)obj;
            return Objects.equals(that.m_name, m_name)
                    && Objects.equals(that.m_serviceUrl, m_serviceUrl)
                    && Objects.equals(that.m_authenUsername, m_authenUsername)
                    && Objects.equals(that.m_authenPassword, m_authenPassword)
                    && Objects.equals(that.m_oauthConsumerKey, m_oauthConsumerKey)
                    && Objects.equals(that.m_oauthConsumerSecret, m_oauthConsumerSecret)
                    && Objects.equals(that.m_oauthAccessToken, m_oauthAccessToken)
                    && Objects.equals(that.m_oauthAccessTokenSecret, m_oauthAccessTokenSecret);
        }
        return false;
    }

}
