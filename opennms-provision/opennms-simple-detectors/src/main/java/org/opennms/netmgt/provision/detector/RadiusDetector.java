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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.jradius.client.RadiusClient;
import net.sourceforge.jradiusclient.RadiusAttribute;
import net.sourceforge.jradiusclient.RadiusAttributeValues;
import net.sourceforge.jradiusclient.RadiusPacket;
import net.sourceforge.jradiusclient.exception.InvalidParameterException;
import net.sourceforge.jradiusclient.exception.RadiusException;
import net.sourceforge.jradiusclient.util.ChapUtil;

import org.opennms.netmgt.provision.DetectorMonitor;

/**
 * @author Donald Desloge
 *
 */
public class RadiusDetector extends AbstractDetector {
    
    public static interface RadiusExchange{
        public boolean processResponse(RadiusPacket packet);
        public boolean sendRequest();
    }
    
    public static class RadiusExchangeImpl implements RadiusExchange{
        private ResponseHandler m_responseHandler;
        private RequestHandler m_requestHandler;
        
        /**
         * @param responseHandler
         * @param requestHandler
         */
        public RadiusExchangeImpl(ResponseHandler responseHandler, RequestHandler requestHandler) {
            m_responseHandler = responseHandler;
            m_requestHandler = requestHandler;
        }

        public boolean processResponse(RadiusPacket packet) {
            if(m_responseHandler != null) {
                return m_responseHandler.matches(packet);
            }
            return false;
        }

        public boolean sendRequest() {
            if(m_requestHandler != null) {
                return m_requestHandler.doRequest();
            }
            return false;
        }
        
    }
    
    public static interface ResponseHandler{
        public boolean matches(RadiusPacket packet);
    }
    
    public static interface RequestHandler{
        public boolean doRequest();
    }
    
    /**
     * Number of milliseconds to wait before timing out a radius AUTH request
     */
    public static final int DEFAULT_TIMEOUT = 5000;

    /**
     * Default number of times to retry a test
     */
    public static final int DEFAULT_RETRY = 0;

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
    
    private int m_authport;
    private int m_acctport;
    private String m_authType;
    private String m_secret;
    private String m_nasid;
    private String m_user;
    private String m_password;
    private RadiusClient m_radiusClient;
    private List<RadiusExchange> m_conversation = new ArrayList<RadiusExchange>();
    
    public RadiusDetector(){
        setRetries(DEFAULT_RETRY);
        setTimeout(DEFAULT_TIMEOUT);
        setAuthport(DEFAULT_AUTH_PORT);
        setAcctPort(DEFAULT_ACCT_PORT);
        setAuthType(DEFAULT_AUTH_TYPE);
        setSecret(DEFAULT_SECRET);
        setNasID(DEFAULT_NAS_ID);
        setUser(DEFAULT_USER);
        setPassword(DEFAULT_PASSWORD);
    }
    
    @Override
    public void init()  {
        

    }

    public void onInit() {
       addResponseHandler(acceptablePacketRange(RadiusPacket.ACCESS_ACCEPT, RadiusPacket.ACCESS_CHALLENGE, RadiusPacket.ACCESS_REJECT), null); 
    }

    
    /**
     * @param accessAccept
     * @param accessChallenge
     * @param accessReject
     * @return
     */
    private ResponseHandler acceptablePacketRange(final int accessAccept, final int accessChallenge, final int accessReject) {
        
        return new ResponseHandler() {

            public boolean matches(RadiusPacket packet) {
                return (packet.getPacketType() == accessAccept) | (packet.getPacketType() == accessChallenge) | (packet.getPacketType() == accessReject);
            }
            
        };
    }

    /**
     * 
     */
    private void addResponseHandler(ResponseHandler responseHandler, RequestHandler requestHandler) {
        addExchange(new RadiusExchangeImpl(responseHandler, requestHandler));
    }

    /**
     * @param radiusExchangeImpl
     */
    private void addExchange(RadiusExchange radiusExchange) {
        m_conversation.add(radiusExchange);
        
    }

    @Override
    public boolean isServiceDetected(InetAddress address, DetectorMonitor detectMonitor) {
        
//        try {
//            //m_radiusClient = new RadiusClient(address.getCanonicalHostName(), getAuthport() ,getAcctPort(), getSecret(), getTimeout());
//        } catch(RadiusException rex) {
//            //log.info(getClass().getName() + ": Radius Exception: " + rex.getMessage());
//            return false;
//        } catch(InvalidParameterException ivpex) {
//            //log.error(getClass().getName() + ": Radius parameter exception: " + ivpex.getMessage());
//            return false;
//        }
//        
//        for (int attempts = 0; attempts <= getRetries(); attempts++) {
//            try {    
//                if(attemptConversation()) { return true; }
//            } catch (InvalidParameterException ivpex){
//                //log.error(getClass().getName() + ": Invalid Radius Parameter: " + ivpex);
//            } catch (RadiusException radex){
//                //log.info(getClass().getName() + ": Radius Exception : " + radex);
//            }
//        }
        
        return false;
    }
    
    

    /**
     * @return
     * @throws InvalidParameterException 
     */
    private boolean attemptConversation() throws InvalidParameterException, RadiusException {
        
        ChapUtil chapUtil = new ChapUtil();
        RadiusPacket accessRequest = new RadiusPacket(RadiusPacket.ACCESS_REQUEST);
        RadiusAttribute userNameAttribute;
        RadiusAttribute nasIdAttribute;
        nasIdAttribute = new RadiusAttribute(RadiusAttributeValues.NAS_IDENTIFIER, getNasID().getBytes());
        userNameAttribute = new RadiusAttribute(RadiusAttributeValues.USER_NAME, getUser().getBytes());
        accessRequest.setAttribute(userNameAttribute);
        accessRequest.setAttribute(nasIdAttribute);
        if(getAuthType().equalsIgnoreCase("chap")){
            byte[] chapChallenge = chapUtil.getNextChapChallenge(16);
            accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.CHAP_PASSWORD, chapEncrypt(getPassword(), chapChallenge, chapUtil)));
            accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.CHAP_CHALLENGE, chapChallenge));
        }else{
            accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.USER_PASSWORD,getPassword().getBytes()));
        }
        
        for(Iterator<RadiusExchange> it = m_conversation.iterator(); it.hasNext();) {
            RadiusExchange ex = it.next();
            
            if(!ex.processResponse(accessRequest)) {
               return false; 
            }
            //System.out.println("processed response successfully");
            
            if(!ex.sendRequest()) {
                return false;
            }
        }
        return true;
    }

    public void setAuthport(int authport) {
        m_authport = authport;
    }

    public int getAuthport() {
        return m_authport;
    }

    public void setAcctPort(int acctport) {
        m_acctport = acctport;
    }

    public int getAcctPort() {
        return m_acctport;
    }

    public void setAuthType(String authType) {
        m_authType = authType;
    }

    public String getAuthType() {
        return m_authType;
    }

    public void setSecret(String secret) {
        m_secret = secret;
    }

    public String getSecret() {
        return m_secret;
    }

    public void setNasID(String nasid) {
        m_nasid = nasid;
    }

    public String getNasID() {
        return m_nasid;
    }

    public void setUser(String user) {
        m_user = user;
    }

    public String getUser() {
        return m_user;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    public String getPassword() {
        return m_password;
    }
    
    /**
     * @param password
     * @param chapChallenge
     * @param chapUtil
     * @return
     */
    private static byte[] chapEncrypt(final String plainText, final byte[] chapChallenge, final ChapUtil chapUtil){
        
        byte chapIdentifier = chapUtil.getNextChapIdentifier();
        byte[] chapPassword = new byte[17];
        chapPassword[0] = chapIdentifier;
        System.arraycopy(ChapUtil.chapEncrypt(chapIdentifier, plainText.getBytes(),chapChallenge),
        0, chapPassword, 1, 16);
        return chapPassword;
    }

}
