/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.radius.detector.client;

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
    
    @Override
    public void connect(final InetAddress address, final int port, final int timeout) throws IOException, Exception {
        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");
    	m_radiusClient = new RadiusClient(address, getSecret(), getAuthPort(), getAcctPort(), convertTimeout(timeout));
    }

    private int convertTimeout(int timeout) {
		
		return timeout/1000 > 0 ? timeout/1000 : 1;
	}

    @Override
	public void close() {
    	m_radiusClient.close();
    }

    @Override
    public RadiusPacket receiveBanner() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
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
