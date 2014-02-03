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
