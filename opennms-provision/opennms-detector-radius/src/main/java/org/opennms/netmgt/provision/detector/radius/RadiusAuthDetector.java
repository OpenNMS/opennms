/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 */
package org.opennms.netmgt.provision.detector.radius;

import net.sourceforge.jradiusclient.RadiusAttribute;
import net.sourceforge.jradiusclient.RadiusAttributeValues;
import net.sourceforge.jradiusclient.RadiusPacket;
import net.sourceforge.jradiusclient.util.ChapUtil;

import org.opennms.netmgt.provision.detector.radius.client.RadiusDetectorClient;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ClientConversation.RequestBuilder;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
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
public class RadiusAuthDetector extends BasicDetector<RadiusPacket, RadiusPacket>{
    
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
    protected RadiusAuthDetector(String serviceName, int port) {
        super(serviceName, port);
    }

    /** {@inheritDoc} */
    @Override
    public void onInit() {
        send(request(getNasID(), getUser(), getPassword()), expectValidResponse(RadiusPacket.ACCESS_ACCEPT, RadiusPacket.ACCESS_CHALLENGE, RadiusPacket.ACCESS_REJECT));
    }
    
    /**
     * @return
     */
    private ResponseValidator<RadiusPacket> expectValidResponse(final int accept, final int challenge, final int reject) {
        
        return new ResponseValidator<RadiusPacket>() {

            public boolean validate(RadiusPacket response) {
                
                return (response.getPacketType() == accept || response.getPacketType() == challenge || response.getPacketType() == reject);
            }
            
        };
    }

    private RequestBuilder<RadiusPacket> request(final String nasID, final String user, final String password){
        return new RequestBuilder<RadiusPacket>() {

            public RadiusPacket getRequest() throws Exception {
                ChapUtil chapUtil = new ChapUtil();
                RadiusPacket accessRequest = new RadiusPacket(RadiusPacket.ACCESS_REQUEST);
                RadiusAttribute userNameAttribute;
                RadiusAttribute nasIdAttribute;
                nasIdAttribute = new RadiusAttribute(RadiusAttributeValues.NAS_IDENTIFIER,nasID.getBytes());
                userNameAttribute = new RadiusAttribute(RadiusAttributeValues.USER_NAME,user.getBytes());
                accessRequest.setAttribute(userNameAttribute);
                accessRequest.setAttribute(nasIdAttribute);
                if(getAuthType().equalsIgnoreCase("chap")){
                    byte[] chapChallenge = chapUtil.getNextChapChallenge(16);
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.CHAP_PASSWORD, chapEncrypt(password, chapChallenge, chapUtil)));
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.CHAP_CHALLENGE, chapChallenge));
                }else{
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.USER_PASSWORD,password.getBytes()));
                }
                return accessRequest;
            }
            
        };
    }
    
    /** {@inheritDoc} */
    @Override
    protected Client<RadiusPacket, RadiusPacket> getClient() {
        RadiusDetectorClient rdc = new RadiusDetectorClient();
        rdc.setAuthport(getAuthPort());
        rdc.setAcctPort(getAcctPort());
        rdc.setSecret(getSecret());
        return rdc;
    }
    
    private static byte[] chapEncrypt(final String plainText, final byte[] chapChallenge, final ChapUtil chapUtil){
        byte chapIdentifier = chapUtil.getNextChapIdentifier();
        byte[] chapPassword = new byte[17];
        chapPassword[0] = chapIdentifier;
        System.arraycopy(ChapUtil.chapEncrypt(chapIdentifier, plainText.getBytes(),chapChallenge), 0, chapPassword, 1, 16);
        return chapPassword;
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
    public void setUser(String user) {
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
    public void setPassword(String password) {
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
