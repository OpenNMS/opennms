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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.opennms.netmgt.config.tokenauth.TokenAuth;
import org.opennms.netmgt.config.tokenauth.TokenAuthConfiguration;
import org.opennms.netmgt.config.tokenauth.BasicAuth;
import org.opennms.netmgt.config.tokenauth.TokenFrom;

public class TokenAuthConfigFactoryTest {

    @After
    public void tearDown() {
        TokenAuthConfigFactory.resetForTesting();
    }

    private static String validXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<token-auth-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/token-auth\">"
                + "  <token-auth name=\"catalyst-prod\">"
                + "    <url>https://example.com/auth/token</url>"
                + "    <method>POST</method>"
                + "    <basic-auth username=\"u\" password=\"p\"/>"
                + "    <token-from jsonpath=\"Token\"/>"
                + "    <ttl-seconds>3300</ttl-seconds>"
                + "  </token-auth>"
                + "</token-auth-configuration>";
    }

    @Test
    public void parsesValidConfig() {
        final TokenAuthConfigFactory factory = new TokenAuthConfigFactory(
                new ByteArrayInputStream(validXml().getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, factory.getAuths().size());
        final TokenAuth auth = factory.getAuth("catalyst-prod").orElseThrow();
        assertEquals("https://example.com/auth/token", auth.getUrl());
        assertEquals("Token", auth.getTokenFrom().getJsonpath());
        assertEquals(Long.valueOf(3300L), auth.getTtlSeconds());
    }

    @Test
    public void getAuthReturnsEmptyForUnknownName() {
        final TokenAuthConfigFactory factory = new TokenAuthConfigFactory(
                new ByteArrayInputStream(validXml().getBytes(StandardCharsets.UTF_8)));
        assertFalse(factory.getAuth("nonexistent").isPresent());
    }

    @Test
    public void singletonInitAndGet() {
        TokenAuthConfigFactory.setInstance(new TokenAuthConfigFactory(
                new ByteArrayInputStream(validXml().getBytes(StandardCharsets.UTF_8))));
        assertTrue(TokenAuthConfigFactory.getInstance().getAuth("catalyst-prod").isPresent());
    }

    @Test
    public void getInstanceBeforeInitFails() {
        // Sanity: not set up
        assertThrows(IllegalStateException.class, TokenAuthConfigFactory::getInstance);
    }

    @Test
    public void rejectsAuthWithoutUrl() {
        final TokenAuthConfiguration cfg = new TokenAuthConfiguration();
        final TokenAuth auth = new TokenAuth();
        auth.setName("bad");
        final TokenFrom tf = new TokenFrom();
        tf.setJsonpath("Token");
        auth.setTokenFrom(tf);
        cfg.setAuths(List.of(auth));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new TokenAuthConfigFactory(cfg));
        assertTrue(ex.getMessage().contains("<url>"));
    }

    @Test
    public void rejectsAuthWithoutTokenFrom() {
        final TokenAuthConfiguration cfg = new TokenAuthConfiguration();
        final TokenAuth auth = new TokenAuth();
        auth.setName("bad");
        auth.setUrl("https://example.com/x");
        cfg.setAuths(List.of(auth));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new TokenAuthConfigFactory(cfg));
        assertTrue(ex.getMessage().contains("<token-from>"));
    }

    @Test
    public void rejectsTokenFromWithMultipleStrategies() {
        final TokenAuthConfiguration cfg = new TokenAuthConfiguration();
        final TokenAuth auth = new TokenAuth();
        auth.setName("bad");
        auth.setUrl("https://example.com/x");
        final TokenFrom tf = new TokenFrom();
        tf.setJsonpath("Token");
        tf.setHeader("X-Token");  // both set -- forbidden
        auth.setTokenFrom(tf);
        cfg.setAuths(List.of(auth));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new TokenAuthConfigFactory(cfg));
        assertTrue(ex.getMessage().contains("exactly one"));
    }

    @Test
    public void rejectsTokenFromWithNoStrategy() {
        final TokenAuthConfiguration cfg = new TokenAuthConfiguration();
        final TokenAuth auth = new TokenAuth();
        auth.setName("bad");
        auth.setUrl("https://example.com/x");
        auth.setTokenFrom(new TokenFrom());  // empty
        cfg.setAuths(List.of(auth));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new TokenAuthConfigFactory(cfg));
        assertTrue(ex.getMessage().contains("exactly one"));
    }

    @Test
    public void rejectsDuplicateAuthNames() {
        final TokenAuthConfiguration cfg = new TokenAuthConfiguration();
        final TokenAuth first = new TokenAuth();
        first.setName("dup");
        first.setUrl("https://example.com/x");
        final TokenFrom tf1 = new TokenFrom();
        tf1.setJsonpath("Token");
        first.setTokenFrom(tf1);

        final TokenAuth second = new TokenAuth();
        second.setName("dup");  // same name -- forbidden
        second.setUrl("https://example.com/y");
        final TokenFrom tf2 = new TokenFrom();
        tf2.setJsonpath("Token");
        second.setTokenFrom(tf2);

        cfg.setAuths(List.of(first, second));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new TokenAuthConfigFactory(cfg));
        assertTrue(ex.getMessage().contains("duplicate"));
        assertTrue(ex.getMessage().contains("dup"));
    }

    @Test
    public void rejectsNegativeTtl() {
        final TokenAuthConfiguration cfg = new TokenAuthConfiguration();
        final TokenAuth auth = new TokenAuth();
        auth.setName("bad-ttl");
        auth.setUrl("https://example.com/x");
        final TokenFrom tf = new TokenFrom();
        tf.setJsonpath("Token");
        auth.setTokenFrom(tf);
        auth.setTtlSeconds(-1L);
        cfg.setAuths(List.of(auth));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new TokenAuthConfigFactory(cfg));
        assertTrue(ex.getMessage().contains("negative"));
    }

    @Test
    public void rejectsBasicAuthMissingPassword() {
        final TokenAuthConfiguration cfg = new TokenAuthConfiguration();
        final TokenAuth auth = new TokenAuth();
        auth.setName("bad");
        auth.setUrl("https://example.com/x");
        final BasicAuth ba = new BasicAuth();
        ba.setUsername("u");
        // password is null
        auth.setBasicAuth(ba);
        final TokenFrom tf = new TokenFrom();
        tf.setJsonpath("Token");
        auth.setTokenFrom(tf);
        cfg.setAuths(List.of(auth));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new TokenAuthConfigFactory(cfg));
        assertTrue(ex.getMessage().contains("basic-auth"));
    }
}
