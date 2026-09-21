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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.tokenauth.BasicAuth;
import org.opennms.netmgt.config.tokenauth.TokenAuth;
import org.opennms.netmgt.config.tokenauth.TokenAuthConfiguration;
import org.opennms.netmgt.config.tokenauth.TokenFrom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

/**
 * Singleton holding the parsed {@code token-auth-configuration.xml}.
 * Modeled on {@link VacuumdConfigFactory}. Performs strict load-time
 * validation: each {@code <token-auth>} block must have a name, URL, and
 * exactly one of jsonpath / header / body-as-token in its
 * {@code <token-from>}.
 */
public final class TokenAuthConfigFactory {

    private static final Logger LOG = LoggerFactory.getLogger(TokenAuthConfigFactory.class);

    private static TokenAuthConfigFactory m_singleton;

    private final TokenAuthConfiguration m_config;

    public TokenAuthConfigFactory(final InputStream stream) {
        this(JaxbUtils.unmarshal(TokenAuthConfiguration.class,
                new InputStreamReader(stream, StandardCharsets.UTF_8)));
    }

    /**
     * Loads {@code etc/token-auth-configuration.xml} into the singleton.
     * If {@code opennms.home} or the file itself is missing (typical in
     * test contexts that don't stage a real OpenNMS install), the
     * singleton is initialised with an empty configuration so
     * Spring context startup does not fail.
     */
    public static synchronized void init() throws IOException {
        if (m_singleton != null) {
            return;
        }
        final File file = locateConfigFile();
        if (file == null) {
            setInstance(new TokenAuthConfigFactory(new TokenAuthConfiguration()));
            return;
        }
        try (InputStream is = new FileInputStream(file)) {
            setInstance(new TokenAuthConfigFactory(is));
        }
    }

    /**
     * Reloads the config from disk. The new factory is parsed and
     * validated first; only on success does the singleton swap, so a
     * failed reload leaves the previous configuration in effect.
     */
    public static synchronized void reload() throws IOException {
        final File file = locateConfigFile();
        if (file == null) {
            setInstance(new TokenAuthConfigFactory(new TokenAuthConfiguration()));
            return;
        }
        try (InputStream is = new FileInputStream(file)) {
            setInstance(new TokenAuthConfigFactory(is));
        }
    }

    private static File locateConfigFile() throws IOException {
        try {
            return ConfigFileConstants.getFile(ConfigFileConstants.TOKEN_AUTH_CONFIG_FILE_NAME);
        } catch (final FileNotFoundException e) {
            LOG.info("token-auth-configuration.xml not available ({}); starting with empty token-auth config", e.getMessage());
            return null;
        }
    }

    public static synchronized TokenAuthConfigFactory getInstance() {
        if (m_singleton == null) {
            throw new IllegalStateException("TokenAuthConfigFactory.init() has not been called");
        }
        return m_singleton;
    }

    public synchronized Optional<TokenAuth> getAuth(final String name) {
        return m_config.getAuth(name);
    }

    public synchronized List<TokenAuth> getAuths() {
        return m_config.getAuths();
    }

    public synchronized TokenAuthConfiguration getConfig() {
        return m_config;
    }

    static void validate(final TokenAuthConfiguration config) {
        final Set<String> seen = new HashSet<>();
        for (final TokenAuth auth : config.getAuths()) {
            validate(auth);
            if (!seen.add(auth.getName())) {
                throw new IllegalArgumentException(
                        "duplicate auth definition name: '" + auth.getName() + "'");
            }
        }
    }

    static void validate(final TokenAuth auth) {
        if (auth.getName() == null || auth.getName().isEmpty()) {
            throw new IllegalArgumentException("auth definition is missing a name attribute");
        }
        if (auth.getUrl() == null || auth.getUrl().isEmpty()) {
            throw new IllegalArgumentException(
                    "auth definition '" + auth.getName() + "' has no <url>");
        }
        validateUrlScheme(auth.getName(), auth.getUrl());
        if (auth.getTokenFrom() == null) {
            throw new IllegalArgumentException(
                    "auth definition '" + auth.getName() + "' has no <token-from>");
        }
        validateTokenFrom(auth);
        if (auth.getBasicAuth() != null) {
            final BasicAuth ba = auth.getBasicAuth();
            if (ba.getUsername() == null || ba.getPassword() == null) {
                throw new IllegalArgumentException(
                        "auth definition '" + auth.getName()
                                + "' has <basic-auth> without both username and password");
            }
        }
        if (auth.getTtlSeconds() != null && auth.getTtlSeconds() < 0) {
            throw new IllegalArgumentException(
                    "auth definition '" + auth.getName()
                            + "' has a negative <ttl-seconds>; use a positive value or omit the element");
        }
    }

    private static void validateUrlScheme(final String name, final String url) {
        // Skip when the URL is a Mate placeholder; it resolves at acquire time.
        if (url.startsWith("${")) {
            return;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return;
        }
        throw new IllegalArgumentException(
                "auth definition '" + name
                        + "' has a <url> that does not start with http:// or https://: "
                        + url);
    }

    private static void validateTokenFrom(final TokenAuth auth) {
        final TokenFrom tf = auth.getTokenFrom();
        int specified = 0;
        if (tf.getJsonpath() != null && !tf.getJsonpath().isEmpty()) {
            specified++;
        }
        if (tf.getHeader() != null && !tf.getHeader().isEmpty()) {
            specified++;
        }
        if (tf.isBodyAsToken()) {
            specified++;
        }
        if (specified != 1) {
            throw new IllegalArgumentException(
                    "auth definition '" + auth.getName()
                            + "' must specify exactly one of jsonpath, header, or body-as-token=\"true\""
                            + " on its <token-from>; got " + specified);
        }
    }

    @VisibleForTesting
    public TokenAuthConfigFactory(final TokenAuthConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        validate(config);
        m_config = config;
    }

    @VisibleForTesting
    static synchronized void setInstance(final TokenAuthConfigFactory factory) {
        m_singleton = factory;
    }

    @VisibleForTesting
    public static synchronized void resetForTesting() {
        m_singleton = null;
    }
}
