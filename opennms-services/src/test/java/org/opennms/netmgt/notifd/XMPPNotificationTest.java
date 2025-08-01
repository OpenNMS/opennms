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
package org.opennms.netmgt.notifd;

import static junit.framework.TestCase.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;

public class XMPPNotificationTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private XMPPNotificationManager m_xmppManager;

    @Before
    public void setUp() {
        final File keystoreFile = new File(tempFolder.getRoot(), "scv.jce");
        final SecureCredentialsVault secureCredentialsVault = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "notRealPassword");
        secureCredentialsVault.setCredentials("xmpp", new Credentials("john", "doe"));
        System.setProperty("useSystemXMPPConfig", "true");
        System.setProperty("xmpp.server", "jabber.example.com");
        System.setProperty("xmpp.port", "5222");
        System.setProperty("xmpp.TLSEnabled", "true");
        System.setProperty("xmpp.selfSignedCertificateEnabled", "true");
        System.setProperty("xmpp.user", "${scv:xmpp:username|test}");
        System.setProperty("xmpp.pass", "${scv:xmpp:password|testpass}");
        m_xmppManager = XMPPNotificationManager.getInstance(secureCredentialsVault);
    }

    @Test
    @Ignore("requires a working test jabber server")
    public void testNotification() {
        m_xmppManager.sendMessage("test@jabber.example.com", "This is a single-user test.");
    }
    
    @Test
    @Ignore("requires a working test jabber server")
    public void testGroupNotification() {
        m_xmppManager.sendGroupChat("test@conference.jabber.example.com", "This is a conference test.");
    }

    @Test
    public void testMetadata() {
        assertEquals("john", m_xmppManager.getXmppUser());
        assertEquals("doe", m_xmppManager.getXmppPassword());
    }
}
