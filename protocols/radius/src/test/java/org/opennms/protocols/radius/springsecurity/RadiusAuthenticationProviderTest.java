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
package org.opennms.protocols.radius.springsecurity;

import java.io.IOException;

import net.jradius.client.auth.CHAPAuthenticator;
import net.jradius.client.auth.EAPMD5Authenticator;
import net.jradius.client.auth.EAPMSCHAPv2Authenticator;
import net.jradius.client.auth.MSCHAPv1Authenticator;
import net.jradius.client.auth.MSCHAPv2Authenticator;
import net.jradius.client.auth.PAPAuthenticator;
import net.jradius.client.auth.RadiusAuthenticator;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 */
public class RadiusAuthenticationProviderTest {

	private String m_radiusServer = "127.0.0.1";
	private String m_sharedSecret = "testing123";
	private Object m_principal = "test";
	private final String m_username = "test";
	private Object m_credentials = "opennms";

	@Test
	@Ignore("Need to have a RADIUS server running on localhost")
	public void testRetrieveUserDefault() throws IOException {
		RadiusAuthenticationProvider provider = new RadiusAuthenticationProvider(m_radiusServer, m_sharedSecret);
		provider.setAuthTypeClass(null);
		provider.setRolesAttribute("Unknown-VSAttribute(5813:1)");

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(m_principal, m_credentials);
		provider.retrieveUser(m_username, token);
	}

	@Test
	@Ignore("Need to have a RADIUS server running on localhost")
	public void testRetrieveUserPap() throws IOException {
		RadiusAuthenticationProvider provider = new RadiusAuthenticationProvider(m_radiusServer, m_sharedSecret);
		provider.setAuthTypeClass(PAPAuthenticator.class);
		provider.setRolesAttribute("Unknown-VSAttribute(5813:1)");

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(m_principal, m_credentials);
		provider.retrieveUser(m_username, token);
	}

	@Test
	@Ignore("Need to have a RADIUS server running on localhost")
	public void testRetrieveUserChap() throws IOException {
		RadiusAuthenticationProvider provider = new RadiusAuthenticationProvider(m_radiusServer, m_sharedSecret);
		provider.setAuthTypeClass(CHAPAuthenticator.class);
		provider.setRolesAttribute("Unknown-VSAttribute(5813:1)");

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(m_principal, m_credentials);
		provider.retrieveUser(m_username, token);
	}

	/**
	 * This test will use null as the authenticator value, which will default to using
	 * PAP authentication.
	 */
	@Test
	@Ignore("Need to have a RADIUS server running on localhost")
	public void testRetrieveUserMultipleTimesDefault() throws IOException {
		doTestRetrieveUserMultipleTimes(null);
	}

	@Test
	@Ignore("Need to have a RADIUS server running on localhost")
	public void testRetrieveUserMultipleTimesPAP() throws IOException {
		doTestRetrieveUserMultipleTimes(new PAPAuthenticator());
	}

	@Test
	@Ignore("Need to have a RADIUS server running on localhost")
	public void testRetrieveUserMultipleTimesCHAP() throws IOException {
		doTestRetrieveUserMultipleTimes(new CHAPAuthenticator());
	}

	@Test
	@Ignore("Need to have a RADIUS server running on localhost")
	public void testRetrieveUserMultipleTimesEAPMD5() throws IOException {
		doTestRetrieveUserMultipleTimes(new EAPMD5Authenticator());
	}

	@Test
	@Ignore("Need to have a RADIUS server running on localhost")
	public void testRetrieveUserMultipleTimesEAPMSCHAPv2() throws IOException {
		doTestRetrieveUserMultipleTimes(new EAPMSCHAPv2Authenticator());
	}

	@Test
	@Ignore("Need to have a RADIUS server running on localhost")
	public void testRetrieveUserMultipleTimesMSCHAPv1() throws IOException {
		doTestRetrieveUserMultipleTimes(new MSCHAPv1Authenticator());
	}

	@Test
	@Ignore("Need to have a RADIUS server running on localhost")
	public void testRetrieveUserMultipleTimesMSCHAPv2() throws IOException {
		doTestRetrieveUserMultipleTimes(new MSCHAPv2Authenticator());
	}

	public void doTestRetrieveUserMultipleTimes(RadiusAuthenticator authenticator) {
		RadiusAuthenticationProvider provider = new RadiusAuthenticationProvider(m_radiusServer, m_sharedSecret);
		provider.setAuthTypeClass(authenticator.getClass());
		provider.setRolesAttribute("Unknown-VSAttribute(5813:1)");

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(m_principal, m_credentials);
		provider.retrieveUser(m_username, token);
		provider.retrieveUser(m_username, token);
		provider.retrieveUser(m_username, token);
		provider.retrieveUser(m_username, token);
		provider.retrieveUser(m_username, token);
		provider.retrieveUser(m_username, token);
	}
}
