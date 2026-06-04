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
package org.opennms.netmgt.ha;

import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches configuration files from the partner (ACTIVE) instance's
 * {@code /rest/filesystem} API and writes them to the local
 * {@code $OPENNMS_HOME/etc} directory.
 *
 * <p>The {@code hasync} service account credentials are resolved from the
 * Secure Credentials Vault at sync time, supporting an SCV expression of the
 * form {@code ${scv:alias:attribute}} in the {@code sync-password} field of
 * {@code ha-configuration.xml}.
 *
 * <p>The file {@code ha-configuration.xml} is always excluded from sync since
 * each node has a distinct role assignment.
 */
public class HaConfigSyncer {

    private static final Logger LOG = LoggerFactory.getLogger(HaConfigSyncer.class);

    /** Files that must never be overwritten by sync, regardless of what the partner returns. vault extensions are already excluded by the filesystem API */
    private static final List<String> SYNC_EXCLUSIONS = List.of("ha-configuration.xml", "examples/");

    private static final Pattern SCV_PATTERN =
            Pattern.compile("^\\$\\{scv:([^:}]+):([^}]+)\\}$");

    private final Supplier<HaConfiguration> configSupplier;
    private final Supplier<HaInstanceState> stateSupplier;
    private final HttpClient httpClient;

    /** Constructor used in tests and when no state tracking is needed (always treats self as STANDBY). */
    public HaConfigSyncer(HaConfiguration config) {
        this(() -> config, () -> HaInstanceState.STANDBY);
    }

    public HaConfigSyncer(HaConfiguration config, Supplier<HaInstanceState> stateSupplier) {
        this(() -> config, stateSupplier);
    }

    /**
     * Preferred constructor: {@code configSupplier} is invoked at the start of every
     * {@link #sync()} cycle, so live configuration changes (e.g. {@code sync-enabled}
     * toggled off, credentials rotated, partner URL updated) take effect on the next
     * cycle without restarting the syncer.
     */
    public HaConfigSyncer(Supplier<HaConfiguration> configSupplier, Supplier<HaInstanceState> stateSupplier) {
        this.configSupplier = configSupplier;
        this.stateSupplier = stateSupplier;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Performs one synchronization pass: fetches the file list from the partner
     * and overwrites each local file whose content has changed. Skipped entirely
     * when this instance is {@link HaInstanceState#ACTIVE}. Failures are logged
     * but do not throw.
     */
    public void sync() {
        HaConfiguration config = configSupplier.get(); // snapshot for this cycle

        if (!config.isSyncEnabled()) {
            return;
        }

        HaInstanceState state = stateSupplier.get();
        if (state == HaInstanceState.ACTIVE) {
            LOG.debug("HA sync: this instance is ACTIVE; skipping config sync");
            return;
        }

        if (config.getPartnerRestUrl() == null || config.getPartnerRestUrl().isBlank()) {
            LOG.warn("HA sync: partner-rest-url is not configured; skipping config sync");
            return;
        }

        String resolvedPassword = resolveScvExpression(config.getSyncPassword());
        if (config.getSyncUsername() == null || resolvedPassword == null) {
            LOG.warn("HA sync: credentials not available (check SCV entry '{}'); skipping sync",
                    extractScvAlias(config.getSyncPassword()));
            return;
        }

        String authHeader = basicAuthHeader(config.getSyncUsername(), resolvedPassword);
        String baseUrl = config.getPartnerRestUrl().replaceAll("/$", "");

        List<String> files = fetchFileList(baseUrl, authHeader);
        if (files.isEmpty()) {
            LOG.warn("HA sync: file list from {} was empty or unavailable", baseUrl);
            return;
        }

        int synced = 0;
        int failed = 0;
        for (String filename : files) {
            if (SYNC_EXCLUSIONS.contains(filename)) {
                LOG.debug("HA sync: skipping excluded file {}", filename);
                continue;
            }
            try {
                syncFile(baseUrl, authHeader, filename);
                synced++;
            } catch (Exception e) {
                LOG.warn("HA sync: failed to sync file {}: {}", filename, e.getMessage());
                failed++;
            }
        }
        LOG.info("HA sync complete: {} files synced, {} failed", synced, failed);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private List<String> fetchFileList(String baseUrl, String authHeader) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/rest/filesystem/"))
                    .header("Authorization", authHeader)
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseJsonStringArray(response.body());
            } else {
                LOG.warn("HA sync: file list request returned HTTP {}", response.statusCode());
                return List.of();
            }
        } catch (Exception e) {
            LOG.warn("HA sync: failed to fetch file list from {}: {}", baseUrl, e.getMessage());
            return List.of();
        }
    }

    private void syncFile(String baseUrl, String authHeader, String filename) throws Exception {
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/rest/filesystem/contents?f=" + encodedFilename))
                .header("Authorization", authHeader)
                .GET()
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            writeLocalFile(filename, response.body());
        } else if (response.statusCode() == 204) {
            LOG.debug("HA sync: file {} is empty on partner, skipping", filename);
        } else {
            throw new IOException("HTTP " + response.statusCode() + " for file " + filename);
        }
    }

    private void writeLocalFile(String filename, String content) throws IOException {
        String opennmsHome = System.getProperty("opennms.home", ".");
        Path target = Paths.get(opennmsHome, "etc", filename).normalize();
        Path etcDir = Paths.get(opennmsHome, "etc").toAbsolutePath();

        // Guard against path traversal
        if (!target.toAbsolutePath().startsWith(etcDir)) {
            throw new IOException("Sync rejected: " + filename + " resolves outside etc directory");
        }

        if (Files.exists(target)) {
            String existing = Files.readString(target, StandardCharsets.UTF_8);
            if (existing.equals(content)) {
                LOG.debug("HA sync: {} unchanged, skipping write", filename);
                return;
            }
        }

        Files.createDirectories(target.getParent());
        Files.writeString(target, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        LOG.debug("HA sync: wrote {}", filename);
    }

    /**
     * Resolves an SCV expression {@code ${scv:alias:attribute}} by reading the
     * local JCEKS keystore directly. Returns the raw string unchanged if it is
     * not an SCV expression.
     */
    String resolveScvExpression(String expression) {
        if (expression == null) return null;

        Matcher m = SCV_PATTERN.matcher(expression.trim());
        if (!m.matches()) {
            return expression; // treat as literal password
        }

        String alias = m.group(1);
        String attribute = m.group(2);

        try {
            SecureCredentialsVault scv = loadScv();
            Credentials creds = scv.getCredentials(alias);
            if (creds == null) {
                LOG.warn("HA sync: no SCV entry found for alias '{}'", alias);
                return null;
            }
            if ("username".equals(attribute)) return creds.getUsername();
            if ("password".equals(attribute)) return creds.getPassword();
            String attr = creds.getAttribute(attribute);
            if (attr == null) {
                LOG.warn("HA sync: SCV alias '{}' has no attribute '{}'", alias, attribute);
            }
            return attr;
        } catch (Exception e) {
            LOG.error("HA sync: failed to resolve SCV expression for alias '{}'", alias, e);
            return null;
        }
    }

    private SecureCredentialsVault loadScv() {
        String opennmsHome = System.getProperty("opennms.home", ".");
        String keystorePath = opennmsHome + File.separator + "etc" + File.separator + "scv.jce";
        String keystorePassword = System.getProperty(
                JCEKSSecureCredentialsVault.KEYSTORE_KEY_PROPERTY,
                JCEKSSecureCredentialsVault.DEFAULT_KEYSTORE_KEY);
        return new JCEKSSecureCredentialsVault(keystorePath, keystorePassword);
    }

    /** Parses a simple JSON string array: {@code ["a","b","c"]}. */
    static List<String> parseJsonStringArray(String json) {
        List<String> result = new ArrayList<>();
        if (json == null) return result;
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) return result;
        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return result;

        // Simple tokeniser: split on "," boundaries that are outside quotes
        boolean inQuote = false;
        int start = 0;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
            } else if (c == ',' && !inQuote) {
                addToken(json.substring(start, i), result);
                start = i + 1;
            }
        }
        addToken(json.substring(start), result);
        return result;
    }

    private static void addToken(String token, List<String> result) {
        token = token.trim();
        if (token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2) {
            String value = token.substring(1, token.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
            if (!value.isEmpty()) result.add(value);
        }
    }

    private static String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private static String extractScvAlias(String expression) {
        if (expression == null) return "(none)";
        Matcher m = SCV_PATTERN.matcher(expression.trim());
        return m.matches() ? m.group(1) : "(literal)";
    }
}
