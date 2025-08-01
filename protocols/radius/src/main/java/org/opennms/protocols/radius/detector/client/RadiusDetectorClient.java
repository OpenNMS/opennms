/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.protocols.radius.detector.client;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.netmgt.provision.support.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jradius.client.RadiusClient;
import net.jradius.client.auth.MSCHAPv2Authenticator;
import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;




/**
 * <p>RadiusDetectorClient class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public class RadiusDetectorClient implements Client<CompositeAttributeLists, RadiusPacket> {
	
	private static final Logger LOG = LoggerFactory.getLogger(RadiusDetectorClient.class);
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
    public RadiusPacket sendRequest(final CompositeAttributeLists parameters) throws Exception {
    	AccessRequest request = parameters.createRadiusRequest(getAuthenticator());
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
