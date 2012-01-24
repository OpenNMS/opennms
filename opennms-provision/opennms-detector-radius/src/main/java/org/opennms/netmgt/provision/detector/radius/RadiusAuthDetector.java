/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.radius;

import net.jradius.client.auth.CHAPAuthenticator;
import net.jradius.client.auth.EAPMD5Authenticator;
import net.jradius.client.auth.EAPMSCHAPv2Authenticator;
import net.jradius.client.auth.MSCHAPv1Authenticator;
import net.jradius.client.auth.MSCHAPv2Authenticator;
import net.jradius.client.auth.PAPAuthenticator;
import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.dictionary.Attr_NASIdentifier;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.AccessChallenge;
import net.jradius.packet.AccessReject;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeList;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ClientConversation.RequestBuilder;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
import org.opennms.netmgt.provision.detector.radius.client.RadiusDetectorClient;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>RadiusAuthDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
public class RadiusAuthDetector extends BasicDetector<AttributeList, RadiusPacket>{
    
    private static final String DEFAULT_SERVICE_NAME = "RadiusAuth";

    /**
     * Default radius authentication port
     */
    public static final int DEFAULT_AUTH_PORT = 1812;

    /**
     * Default radius accounting port
     */
    public static final int DEFAULT_ACCT_PORT = 1813;

    /**
     * Default radius authentication type
     */
    public static final String DEFAULT_AUTH_TYPE = "pap";

    /**
     * Default user
     */
    public static final String DEFAULT_USER = "OpenNMS";

    /**
     * Default password
     */
    public static final String DEFAULT_PASSWORD = "OpenNMS";

    /**
     * Default secret
     */
    public static final String DEFAULT_SECRET = "secret";

    /**
     * 
     * Default NAS_ID
     */
    public static final String DEFAULT_NAS_ID = "opennms";
    
    private int m_authport = DEFAULT_AUTH_PORT;
    private int m_acctport = DEFAULT_ACCT_PORT;
    private String m_secret = DEFAULT_SECRET;
    private String m_authType = DEFAULT_AUTH_TYPE;
    private String m_nasid = DEFAULT_NAS_ID;
    private String m_user = DEFAULT_USER;
    private String m_password = DEFAULT_PASSWORD;
    
    /**
     * Default constructor
     */
    protected RadiusAuthDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_AUTH_PORT);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    protected RadiusAuthDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }

    /** {@inheritDoc} */
    @Override
    public void onInit() {
        send(request(getNasID(), getUser(), getPassword()), expectValidResponse(AccessAccept.class, AccessChallenge.class, AccessReject.class));
    }
    
    /**
     * @return
     */
    private ResponseValidator<RadiusPacket> expectValidResponse(final Class<?> accept, final Class<?> challenge, final Class<?> reject) {
        
        return new ResponseValidator<RadiusPacket>() {

            public boolean validate(final RadiusPacket response) {
            	return (accept.isInstance(response) || challenge.isInstance(response) || reject.isInstance(response));
            }
            
        };
    }

    private RequestBuilder<AttributeList> request(final String nasID, final String user, final String password) {
    	LogUtils.debugf(this, "request: nasID = %s, user = %s, password = %s", nasID, user, password);
    	
        return new RequestBuilder<AttributeList>() {

            public AttributeList getRequest() throws Exception {
    	    	final AttributeList attributes = new AttributeList();
    	    	attributes.add(new Attr_UserName(user));
    	    	attributes.add(new Attr_NASIdentifier(nasID));
    	    	attributes.add(new Attr_UserPassword(password));
    	    	return attributes;
            }
            
        };
    }
    
    /** {@inheritDoc} */
    @Override
    protected Client<AttributeList, RadiusPacket> getClient() {
    	final RadiusDetectorClient rdc = new RadiusDetectorClient();
        rdc.setAuthport(getAuthPort());
        rdc.setAcctPort(getAcctPort());
        rdc.setSecret(getSecret());
        rdc.setAuthenticator(getAuthenticator());
        return rdc;
    }
    
    /**
     * <p>setAuthPort</p>
     *
     * @param authport a int.
     */
    public void setAuthPort(int authport) {
        m_authport = authport;
    }

    /**
     * <p>getAuthPort</p>
     *
     * @return a int.
     */
    public int getAuthPort() {
        return m_authport;
    }

    /**
     * <p>setAcctPort</p>
     *
     * @param acctport a int.
     */
    public void setAcctPort(int acctport) {
        m_acctport = acctport;
    }

    /**
     * <p>getAcctPort</p>
     *
     * @return a int.
     */
    public int getAcctPort() {
        return m_acctport;
    }

    /**
     * <p>setSecret</p>
     *
     * @param secret a {@link java.lang.String} object.
     */
    public void setSecret(String secret) {
        m_secret = secret;
    }

    /**
     * <p>getSecret</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSecret() {
        return m_secret;
    }

    /**
     * <p>setAuthType</p>
     *
     * @param authType a {@link java.lang.String} object.
     */
    public void setAuthType(String authType) {
        m_authType = authType;
    }

    /**
     * <p>getAuthType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAuthType() {
        return m_authType;
    }

    public RadiusAuthenticator getAuthenticator() {
    	final RadiusAuthenticator auth;
    	final String authType = getAuthType();
    	if (authType.equalsIgnoreCase("chap")) {
    		auth = new CHAPAuthenticator();
    	} else if (authType.equalsIgnoreCase("pap")) {
    		auth = new PAPAuthenticator();
    	} else if (authType.equalsIgnoreCase("mschapv1")) {
    		auth = new MSCHAPv1Authenticator();
    	} else if (authType.equalsIgnoreCase("mschapv2")) {
    		auth = new MSCHAPv2Authenticator();
    	} else if (authType.equalsIgnoreCase("eapmd5") || authType.equalsIgnoreCase("eap-md5")) {
    		auth = new EAPMD5Authenticator();
    	} else if (authType.equalsIgnoreCase("eapmschapv2") || authType.equalsIgnoreCase("eap-mschapv2")) {
    		auth = new EAPMSCHAPv2Authenticator();
    	} else {
    		auth = null;
    	}
    	return auth;
    }

    /**
     * <p>setNasID</p>
     *
     * @param nasid a {@link java.lang.String} object.
     */
    public void setNasID(String nasid) {
        m_nasid = nasid;
    }

    /**
     * <p>getNasID</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNasID() {
        return m_nasid;
    }

    /**
     * <p>setUser</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public void setUser(final String user) {
        m_user = user;
    }

    /**
     * <p>getUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUser() {
        return m_user;
    }

    /**
     * <p>setPassword</p>
     *
     * @param password a {@link java.lang.String} object.
     */
    public void setPassword(final String password) {
        m_password = password;
    }

    /**
     * <p>getPassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPassword() {
        return m_password;
    }
	
}
