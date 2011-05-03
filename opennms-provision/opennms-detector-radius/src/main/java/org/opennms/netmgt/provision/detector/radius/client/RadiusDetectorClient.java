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
package org.opennms.netmgt.provision.detector.radius.client;

import java.io.IOException;
import java.net.InetAddress;

import net.jradius.client.RadiusClient;
import net.jradius.client.auth.MSCHAPv2Authenticator;
import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;

import org.opennms.netmgt.provision.support.Client;

/**
 * <p>RadiusDetectorClient class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public class RadiusDetectorClient implements Client<AttributeList, RadiusPacket> {
    /**
     * Default radius authentication port
     */
    public static final int DEFAULT_AUTH_PORT = 1812;

    /**
     * Default radius accounting port
     */
    public static final int DEFAULT_ACCT_PORT = 1813;
    
    /**
     * Default secret
     */
    public static final String DEFAULT_SECRET = "secret123"; //"secret";
    
    private RadiusClient m_radiusClient;
    private int m_authport = DEFAULT_AUTH_PORT;
    private int m_acctport = DEFAULT_ACCT_PORT;
    private String m_secret = DEFAULT_SECRET;
    private RadiusAuthenticator m_authenticator = new MSCHAPv2Authenticator();
    
    public void connect(final InetAddress address, final int port, final int timeout) throws IOException, Exception {
        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");
    	m_radiusClient = new RadiusClient(address, getSecret(), getAuthPort(), getAcctPort(), timeout);
    }

    public void close() {
    	m_radiusClient.close();
    }

    public RadiusPacket receiveBanner() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public RadiusPacket sendRequest(final AttributeList attributes) throws Exception {
    	final AccessRequest request = new AccessRequest(m_radiusClient, attributes);
    	return m_radiusClient.authenticate(request, getAuthenticator(), 0);
    }

    /**
     * <p>setAuthport</p>
     *
     * @param authport a int.
     */
    public void setAuthport(final int authport) {
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
    public void setAcctPort(final int acctport) {
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
    public void setSecret(final String secret) {
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

    public void setAuthenticator(final RadiusAuthenticator authenticator) {
    	m_authenticator = authenticator;
    }
    
    public RadiusAuthenticator getAuthenticator() {
    	return m_authenticator;
    }
}
