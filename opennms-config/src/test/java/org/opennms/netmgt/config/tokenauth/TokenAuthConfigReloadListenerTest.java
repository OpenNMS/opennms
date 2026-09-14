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
package org.opennms.netmgt.config.tokenauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.config.TokenAuthConfigFactory;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

public class TokenAuthConfigReloadListenerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private String previousOpennmsHome;
    private File etcDir;
    private EventIpcManager eventIpcManager;
    private TokenCache tokenCache;
    private TokenAuthConfigReloadListener listener;
    private AtomicInteger acquireCount;

    @Before
    public void setUp() throws Exception {
        previousOpennmsHome = System.getProperty("opennms.home");
        System.setProperty("opennms.home", tempFolder.getRoot().getAbsolutePath());

        etcDir = tempFolder.newFolder("etc");

        // The factory is a static singleton; reset it so reload() picks
        // up the temp file rather than whatever the previous test left.
        TokenAuthConfigFactory.resetForTesting();
        writeConfig(authBlock("auth-one"));

        acquireCount = new AtomicInteger();
        tokenCache = new TokenCache(new TokenAcquirer() {
            @Override
            public CachedToken acquire(final TokenAuth auth) {
                final int n = acquireCount.incrementAndGet();
                return new CachedToken("tok-" + n, null);
            }
        });
        eventIpcManager = mock(EventIpcManager.class);
        listener = new TokenAuthConfigReloadListener(tokenCache, eventIpcManager);
    }

    @After
    public void tearDown() {
        TokenAuthConfigFactory.resetForTesting();
        if (previousOpennmsHome == null) {
            System.clearProperty("opennms.home");
        } else {
            System.setProperty("opennms.home", previousOpennmsHome);
        }
    }

    @Test
    public void reloadsConfigAndFlushesCacheOnMatchingDaemonName() throws IOException {
        // Prime the cache by acquiring a token for the original auth
        // definition. After reload it should be evicted.
        TokenAuthConfigFactory.init();
        final TokenAuth original = TokenAuthConfigFactory.getInstance().getAuth("auth-one").orElseThrow();
        tokenCache.getToken(original);
        assertTrue("primed cache should report 'auth-one' as cached",
                tokenCache.isCached("auth-one"));
        assertEquals(1, acquireCount.get());

        // Rewrite the on-disk config with a renamed auth before firing
        // the reload event.
        writeConfig(authBlock("auth-two"));

        listener.onEvent(reloadEventFor("TokenAuth"));

        // After reload, the new config is loaded ("auth-two" exists,
        // "auth-one" does not) and the cache has been flushed.
        assertTrue(TokenAuthConfigFactory.getInstance().getAuth("auth-two").isPresent());
        assertFalse(TokenAuthConfigFactory.getInstance().getAuth("auth-one").isPresent());
        assertFalse("cache must be flushed on reload",
                tokenCache.isCached("auth-one"));

        final ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventIpcManager).sendNow(captor.capture());
        assertEquals(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI,
                captor.getValue().getUei());
    }

    @Test
    public void ignoresEventForOtherDaemons() throws IOException {
        TokenAuthConfigFactory.init();
        tokenCache.getToken(TokenAuthConfigFactory.getInstance().getAuth("auth-one").orElseThrow());

        listener.onEvent(reloadEventFor("SomethingElse"));

        verify(eventIpcManager, never()).sendNow(any(Event.class));
        assertTrue("cache must be untouched", tokenCache.isCached("auth-one"));
    }

    @Test
    public void ignoresEventWithoutDaemonNameParameter() throws IOException {
        TokenAuthConfigFactory.init();
        tokenCache.getToken(TokenAuthConfigFactory.getInstance().getAuth("auth-one").orElseThrow());

        final IEvent bareEvent = ImmutableMapper.fromMutableEvent(
                new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test").getEvent());
        listener.onEvent(bareEvent);

        verify(eventIpcManager, never()).sendNow(any(Event.class));
        assertTrue(tokenCache.isCached("auth-one"));
    }

    @Test
    public void selfRegistersOnContextRefreshOnceFactoryIsReady() {
        // The production constructor takes only the cache. Before
        // EventIpcManagerFactory has a manager, a ContextRefreshedEvent
        // is a no-op. Once the factory has a manager, the next refresh
        // drives a one-shot registration.
        org.opennms.netmgt.events.api.EventIpcManagerFactory.reset();
        final TokenCache cache = new TokenCache(new TokenAcquirer() {
            @Override
            public CachedToken acquire(final TokenAuth auth) { return new CachedToken("t", null); }
        });
        final TokenAuthConfigReloadListener l =
                new TokenAuthConfigReloadListener(cache);
        final org.springframework.context.event.ContextRefreshedEvent stubEvent =
                new org.springframework.context.event.ContextRefreshedEvent(
                        mock(org.springframework.context.ApplicationContext.class));

        // Factory not yet ready -- nothing registers.
        l.onApplicationEvent(stubEvent);

        final EventIpcManager mgr = mock(EventIpcManager.class);
        org.opennms.netmgt.events.api.EventIpcManagerFactory.setIpcManager(mgr);

        // Now ready; this refresh drives a single addEventListener.
        l.onApplicationEvent(stubEvent);
        verify(mgr).addEventListener(l, EventConstants.RELOAD_DAEMON_CONFIG_UEI);

        // A second refresh must not double-register.
        l.onApplicationEvent(stubEvent);
        verify(mgr).addEventListener(l, EventConstants.RELOAD_DAEMON_CONFIG_UEI);

        org.opennms.netmgt.events.api.EventIpcManagerFactory.reset();
    }

    @Test
    public void emitsFailureEventWhenReloadThrows() throws IOException {
        // Replace the on-disk config with malformed XML so reload() fails.
        Files.writeString(etcDir.toPath().resolve("token-auth-configuration.xml"),
                "<not-valid-xml", StandardCharsets.UTF_8);
        TokenAuthConfigFactory.resetForTesting();

        listener.onEvent(reloadEventFor("TokenAuth"));

        final ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventIpcManager).sendNow(captor.capture());
        assertEquals(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI,
                captor.getValue().getUei());
    }

    private void writeConfig(final String authBlocks) throws IOException {
        final String xml = "<?xml version=\"1.0\"?>"
                + "<token-auth-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/token-auth\">"
                + authBlocks
                + "</token-auth-configuration>";
        Files.writeString(etcDir.toPath().resolve("token-auth-configuration.xml"),
                xml, StandardCharsets.UTF_8);
    }

    private static String authBlock(final String name) {
        return "<token-auth name=\"" + name + "\">"
                + "<url>http://localhost/x</url>"
                + "<method>POST</method>"
                + "<token-from body-as-token=\"true\"/>"
                + "</token-auth>";
    }

    private static IEvent reloadEventFor(final String daemonName) {
        final EventBuilder b = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test");
        b.addParam(EventConstants.PARM_DAEMON_NAME, daemonName);
        return ImmutableMapper.fromMutableEvent(b.getEvent());
    }

}
