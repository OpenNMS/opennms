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

import net.sourceforge.jradiusclient.RadiusClient;
import net.sourceforge.jradiusclient.RadiusPacket;

import org.opennms.netmgt.provision.support.Client;

/**
 * @author Donald Desloge
 *
 */
public class RadiusDetectorClient implements Client<RadiusPacket, RadiusPacket> {
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
    //private String m_authType;
    //private String m_nasid;
    //private String m_user;
    //private String m_password;
    
    public void close() {
        // TODO Auto-generated method stub
        
    }

    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        m_radiusClient = new RadiusClient(address.getCanonicalHostName(), getAuthPort() ,getAcctPort(), getSecret(), timeout);
    }

    public RadiusPacket receiveBanner() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public RadiusPacket sendRequest(RadiusPacket request) throws Exception {
        return m_radiusClient.authenticate(request);
    }

    public void setAuthport(int authport) {
        m_authport = authport;
    }

    public int getAuthPort() {
        return m_authport;
    }

    public void setAcctPort(int acctport) {
        m_acctport = acctport;
    }

    public int getAcctPort() {
        return m_acctport;
    }

    public void setSecret(String secret) {
        m_secret = secret;
    }

    public String getSecret() {
        return m_secret;
    }

}
