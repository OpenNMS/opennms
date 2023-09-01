/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
