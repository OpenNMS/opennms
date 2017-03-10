/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This entity defines the parameters for a microblog service.
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "microblog-profile")
@XmlAccessorType(XmlAccessType.FIELD)
public class MicroblogProfile implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "service-url", required = true)
    private String serviceUrl;

    @XmlAttribute(name = "authen-username")
    private String authenUsername;

    @XmlAttribute(name = "authen-password")
    private String authenPassword;

    @XmlAttribute(name = "oauth-consumer-key")
    private String oauthConsumerKey;

    @XmlAttribute(name = "oauth-consumer-secret")
    private String oauthConsumerSecret;

    @XmlAttribute(name = "oauth-access-token")
    private String oauthAccessToken;

    @XmlAttribute(name = "oauth-access-token-secret")
    private String oauthAccessTokenSecret;

    public MicroblogProfile() {
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof MicroblogProfile) {
            MicroblogProfile temp = (MicroblogProfile)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.serviceUrl, serviceUrl)
                && Objects.equals(temp.authenUsername, authenUsername)
                && Objects.equals(temp.authenPassword, authenPassword)
                && Objects.equals(temp.oauthConsumerKey, oauthConsumerKey)
                && Objects.equals(temp.oauthConsumerSecret, oauthConsumerSecret)
                && Objects.equals(temp.oauthAccessToken, oauthAccessToken)
                && Objects.equals(temp.oauthAccessTokenSecret, oauthAccessTokenSecret);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'authenPassword'.
     * 
     * @return the value of field 'AuthenPassword'.
     */
    public String getAuthenPassword() {
        return this.authenPassword;
    }

    /**
     * Returns the value of field 'authenUsername'.
     * 
     * @return the value of field 'AuthenUsername'.
     */
    public String getAuthenUsername() {
        return this.authenUsername;
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of field 'oauthAccessToken'.
     * 
     * @return the value of field 'OauthAccessToken'.
     */
    public String getOauthAccessToken() {
        return this.oauthAccessToken;
    }

    /**
     * Returns the value of field 'oauthAccessTokenSecret'.
     * 
     * @return the value of field 'OauthAccessTokenSecret'.
     */
    public String getOauthAccessTokenSecret() {
        return this.oauthAccessTokenSecret;
    }

    /**
     * Returns the value of field 'oauthConsumerKey'.
     * 
     * @return the value of field 'OauthConsumerKey'.
     */
    public String getOauthConsumerKey() {
        return this.oauthConsumerKey;
    }

    /**
     * Returns the value of field 'oauthConsumerSecret'.
     * 
     * @return the value of field 'OauthConsumerSecret'.
     */
    public String getOauthConsumerSecret() {
        return this.oauthConsumerSecret;
    }

    /**
     * Returns the value of field 'serviceUrl'.
     * 
     * @return the value of field 'ServiceUrl'.
     */
    public String getServiceUrl() {
        return this.serviceUrl;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            name, 
            serviceUrl, 
            authenUsername, 
            authenPassword, 
            oauthConsumerKey, 
            oauthConsumerSecret, 
            oauthAccessToken, 
            oauthAccessTokenSecret);
        return hash;
    }

    /**
     * Sets the value of field 'authenPassword'.
     * 
     * @param authenPassword the value of field 'authenPassword'.
     */
    public void setAuthenPassword(final String authenPassword) {
        this.authenPassword = authenPassword;
    }

    /**
     * Sets the value of field 'authenUsername'.
     * 
     * @param authenUsername the value of field 'authenUsername'.
     */
    public void setAuthenUsername(final String authenUsername) {
        this.authenUsername = authenUsername;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the value of field 'oauthAccessToken'.
     * 
     * @param oauthAccessToken the value of field 'oauthAccessToken'.
     */
    public void setOauthAccessToken(final String oauthAccessToken) {
        this.oauthAccessToken = oauthAccessToken;
    }

    /**
     * Sets the value of field 'oauthAccessTokenSecret'.
     * 
     * @param oauthAccessTokenSecret the value of field 'oauthAccessTokenSecret'.
     */
    public void setOauthAccessTokenSecret(final String oauthAccessTokenSecret) {
        this.oauthAccessTokenSecret = oauthAccessTokenSecret;
    }

    /**
     * Sets the value of field 'oauthConsumerKey'.
     * 
     * @param oauthConsumerKey the value of field 'oauthConsumerKey'.
     */
    public void setOauthConsumerKey(final String oauthConsumerKey) {
        this.oauthConsumerKey = oauthConsumerKey;
    }

    /**
     * Sets the value of field 'oauthConsumerSecret'.
     * 
     * @param oauthConsumerSecret the value of field 'oauthConsumerSecret'.
     */
    public void setOauthConsumerSecret(final String oauthConsumerSecret) {
        this.oauthConsumerSecret = oauthConsumerSecret;
    }

    /**
     * Sets the value of field 'serviceUrl'.
     * 
     * @param serviceUrl the value of field 'serviceUrl'.
     */
    public void setServiceUrl(final String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

}
