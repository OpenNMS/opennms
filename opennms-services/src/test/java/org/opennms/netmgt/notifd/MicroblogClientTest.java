/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.util.InMemoryResource;

public class MicroblogClientTest {
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
    @Ignore
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
}
