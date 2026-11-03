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
package org.opennms.javamail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.EmptyScope;
import org.opennms.core.mate.api.Scope;

/**
 * ${token:<name>} interpolation in mail credentials: the token must resolve
 * through the token scope, and it must be fetched on every interpolation so
 * long-running daemons never hold a stale OAuth access token.
 */
public class JavaMailerConfigTokenTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private final AtomicInteger fetches = new AtomicInteger();

    private final Scope countingTokenScope = new Scope() {
        @Override
        public Optional<ScopeValue> get(final ContextKey contextKey) {
            if (!"token".equals(contextKey.context)) {
                return Optional.empty();
            }
            fetches.incrementAndGet();
            if ("m365".equals(contextKey.key)) {
                return Optional.of(new ScopeValue(ScopeName.GLOBAL, "token-" + fetches.get()));
            }
            return Optional.empty();
        }

        @Override
        public Set<ContextKey> keys() {
            return Set.of();
        }
    };

    private String previousOpennmsHome;

    @Before
    public void setUp() throws Exception {
        previousOpennmsHome = System.getProperty("opennms.home");
        final File etc = tempFolder.newFolder("etc");
        Files.write(new File(etc, "javamail-configuration.properties").toPath(),
                ("org.opennms.core.utils.authenticateUser=svc@example.com\n"
                        + "org.opennms.core.utils.authenticatePassword=${token:m365}\n").getBytes(StandardCharsets.UTF_8));
        System.setProperty("opennms.home", tempFolder.getRoot().getAbsolutePath());

        JavaMailerConfig.setSecureCredentialsVaultScope(EmptyScope.EMPTY);
        JavaMailerConfig.setTokenScope(countingTokenScope);
    }

    @After
    public void tearDown() {
        JavaMailerConfig.setSecureCredentialsVaultScope(EmptyScope.EMPTY);
        JavaMailerConfig.setTokenScope(EmptyScope.EMPTY);
        if (previousOpennmsHome != null) {
            System.setProperty("opennms.home", previousOpennmsHome);
        } else {
            System.clearProperty("opennms.home");
        }
    }

    @Test
    public void resolvesTokenInAuthenticatePassword() throws Exception {
        final Properties props = JavaMailerConfig.getProperties();
        assertEquals("svc@example.com", props.getProperty("org.opennms.core.utils.authenticateUser"));
        assertTrue(props.getProperty("org.opennms.core.utils.authenticatePassword").startsWith("token-"));
    }

    @Test
    public void fetchesTokenOnEveryInterpolation() throws Exception {
        final String first = JavaMailerConfig.getProperties().getProperty("org.opennms.core.utils.authenticatePassword");
        final String second = JavaMailerConfig.getProperties().getProperty("org.opennms.core.utils.authenticatePassword");
        assertTrue(fetches.get() >= 2);
        assertEquals("token-1", first);
        assertEquals("token-2", second);
    }

    @Test
    public void resolvesTokenInStringInterpolation() {
        final String interpolated = JavaMailerConfig.interpolate("${token:m365}");
        assertTrue(interpolated.startsWith("token-"));
    }

    @Test
    public void unknownTokenFallsBackToDefault() {
        assertEquals("fallback", JavaMailerConfig.interpolate("${token:missing|fallback}"));
    }
}
