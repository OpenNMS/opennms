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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;
import org.springframework.security.util.InMemoryResource;

import twitter4j.TwitterException;

public class MicroblogClientTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private final String m_consumerKey = "";
    private final String m_consumerSecret = "";

    @Test
    public void testOAuthConfig() {
        final InMemoryResource configResource = new InMemoryResource(
                "<?xml version=\"1.0\"?>\n" + 
                "<microblog-configuration default-microblog-profile-name=\"twitter\">\n" + 
                "    <microblog-profile\n" + 
                "        name=\"twitter\"\n" + 
                "        service-url=\"https://twitter.com/\"\n" + 
                "        oauth-consumer-key=\"ABC\"\n" + 
                "        oauth-consumer-secret=\"DEF\"\n" + 
                "    />\n" + 
                "</microblog-configuration>\n");
        final MicroblogClient client = new MicroblogClient(configResource);

        final String profile = "twitter";
        assertNotNull(client.getProfile(profile));
        assertTrue(client.hasOAuth(profile));
        assertFalse(client.hasBasicAuth(profile));
        assertFalse(client.hasOAuthAccessToken(profile));
    }

    @Test
    public void testPasswordConfig() {
        final InMemoryResource configResource = new InMemoryResource(
                "<?xml version=\"1.0\"?>\n" + 
                "<microblog-configuration default-microblog-profile-name=\"twitter\">\n" + 
                "    <microblog-profile\n" + 
                "        name=\"twitter\"\n" + 
                "        authen-username=\"thisIsBogus\"\n" +
                "        authen-password=\"thisIsAlsoBogus\"\n" +
                "        service-url=\"https://twitter.com/\"\n" + 
                "    />\n" + 
                "</microblog-configuration>\n");
        final MicroblogClient client = new MicroblogClient(configResource);

        final String profile = "twitter";
        assertNotNull(client.getProfile(profile));
        assertFalse(client.hasOAuth(profile));
        assertTrue(client.hasBasicAuth(profile));
        assertFalse(client.hasOAuthAccessToken(profile));
    }

    @Test
    @Ignore("manual test requires oauth token")
    public void testOAuthRegistration() throws Exception {
        final InMemoryResource configResource = new InMemoryResource(
                 "<?xml version=\"1.0\"?>\n" + 
                 "<microblog-configuration default-microblog-profile-name=\"twitter\">\n" + 
                 "    <microblog-profile\n" + 
                 "        name=\"twitter\"\n" + 
                 "        service-url=\"https://twitter.com/\"\n" + 
                 "        oauth-consumer-key=\"" + m_consumerKey + "\"\n" + 
                 "        oauth-consumer-secret=\"" + m_consumerSecret + "\"\n" + 
                 "    />\n" + 
                 "</microblog-configuration>\n");
         final MicroblogClient client = new MicroblogClient(configResource);

         final String profile = "twitter";
         assertNotNull(client.getProfile(profile));
         assertTrue(client.hasOAuth(profile));
         assertFalse(client.hasBasicAuth(profile));
         assertFalse(client.hasOAuthAccessToken(profile));

         final MicroblogAuthorization auth = client.requestAuthorization(profile);
         assertNotNull(auth);
         assertNotNull(auth.getUrl());
         assertTrue(auth.getUrl() + " should contain twitter.com/", auth.getUrl().contains("twitter.com/"));
    }

    @Test
    public void testMetadata() throws TwitterException {
        final File keystoreFile = new File(tempFolder.getRoot(), "scv.jce");
        final SecureCredentialsVault secureCredentialsVault = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "notRealPassword");
        secureCredentialsVault.setCredentials("authen", new Credentials("john", "doe"));
        secureCredentialsVault.setCredentials("oauth", new Credentials("foo", "bar"));
        final InMemoryResource configResource = new InMemoryResource(
                "<?xml version=\"1.0\"?>\n" +
                        "<microblog-configuration default-microblog-profile-name=\"twitter\">\n" +
                        "    <microblog-profile\n" +
                        "        name=\"twitter\"\n" +
                        "        service-url=\"https://twitter.com/\"\n" +
                        "        authen-username=\"${scv:authen:username|ABC}\"\n" +
                        "        authen-password=\"${scv:authen:password|DEF}\"\n" +
                        "        oauth-consumer-key=\"${scv:oauth:username|ABC}\"\n" +
                        "        oauth-consumer-secret=\"${scv:oauth:password|DEF}\"\n" +
                        "    />\n" +
                        "</microblog-configuration>\n");
        final MicroblogClient client = new MicroblogClient(configResource, secureCredentialsVault);
        assertEquals("john", client.getTwitter("twitter").getConfiguration().getUser());
        assertEquals("doe", client.getTwitter("twitter").getConfiguration().getPassword());
        assertEquals("foo", client.getTwitter("twitter").getConfiguration().getOAuthConsumerKey());
        assertEquals("bar", client.getTwitter("twitter").getConfiguration().getOAuthConsumerSecret());
    }
}
