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
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Synchronizes {@code $OPENNMS_HOME/etc} from the partner (ACTIVE) instance
 * using the HA sync endpoints ({@code /rest/ha/sync/manifest} and
 * {@code /rest/ha/sync/file}) — a binary-safe, manifest-based transfer:
 *
 * <ul>
 *   <li>the manifest lists every in-scope file with its sha256 and size;</li>
 *   <li>only files whose hash differs locally are fetched, as raw bytes
 *       (keystores such as {@code scv.jce}, {@code users.xml}, and TLS
 *       material all transfer intact);</li>
 *   <li>fetched files are staged and moved into place atomically;</li>
 *   <li>local files that are in scope but absent from the manifest are
 *       deleted — removals on the ACTIVE node propagate.</li>
 * </ul>
 *
 * <p>Exclusions (never overwritten or deleted) are shared with the serving
 * side via {@link HaSyncFiles}: {@code ha-configuration.xml} always, plus the
 * operator-extensible {@code <sync-excludes>} list.
 *
 * <p>The {@code hasync} service account credentials are resolved from the
 * Secure Credentials Vault at sync time, supporting an SCV expression of the
 * form {@code ${scv:alias:attribute}} in the {@code sync-password} field of
 * {@code ha-configuration.xml}.
 */
public class HaConfigSyncer {

    private static final Logger LOG = LoggerFactory.getLogger(HaConfigSyncer.class);

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
     * Performs one synchronization pass. Skipped entirely when this instance
     * is {@link HaInstanceState#ACTIVE}. Failures are logged but do not throw.
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
        List<String> excludes = config.getSyncExcludes();

        List<HaSyncFiles.Entry> manifest = fetchManifest(baseUrl, authHeader);
        if (manifest == null) {
            return; // already logged
        }
        if (manifest.isEmpty()) {
            LOG.warn("HA sync: manifest from {} was empty; skipping cycle (refusing to delete everything)", baseUrl);
            return;
        }

        Path etcRoot = HaSyncFiles.etcRoot();
        int fetched = 0;
        int failed = 0;
        Set<String> manifestPaths = new HashSet<>();

        for (HaSyncFiles.Entry entry : manifest) {
            manifestPaths.add(entry.relativePath());
            // The serving side applies exclusions too, but the local list may
            // legitimately be stricter — never let the partner overwrite an
            // excluded file.
            if (HaSyncFiles.isExcluded(entry.relativePath(), excludes)) {
                continue;
            }
            try {
                if (localMatches(etcRoot, entry)) {
                    continue;
                }
                fetchFile(baseUrl, authHeader, etcRoot, entry);
                fetched++;
            } catch (Exception e) {
                LOG.warn("HA sync: failed to sync {}: {}", entry.relativePath(), e.getMessage());
                failed++;
            }
        }

        int deleted = propagateDeletions(etcRoot, manifestPaths, excludes);

        if (fetched > 0 || failed > 0 || deleted > 0) {
            LOG.info("HA sync complete: {} files fetched, {} deleted, {} failed", fetched, deleted, failed);
        } else {
            LOG.debug("HA sync complete: no changes");
        }
    }

    // -------------------------------------------------------------------------
    // Manifest + transfer
    // -------------------------------------------------------------------------

    /** @return the parsed manifest, or null if it could not be fetched. */
    private List<HaSyncFiles.Entry> fetchManifest(String baseUrl, String authHeader) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/rest/ha/sync/manifest"))
                    .header("Authorization", authHeader)
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOG.warn("HA sync: manifest request returned HTTP {}", response.statusCode());
                return null;
            }
            return HaSyncFiles.parseManifestText(response.body());
        } catch (Exception e) {
            LOG.warn("HA sync: failed to fetch manifest from {}: {}", baseUrl, e.getMessage());
            return null;
        }
    }

    private boolean localMatches(Path etcRoot, HaSyncFiles.Entry entry) throws IOException {
        Path local = HaSyncFiles.resolveSafe(etcRoot, entry.relativePath());
        return Files.isRegularFile(local)
                && Files.size(local) == entry.size()
                && HaSyncFiles.sha256(local).equals(entry.sha256());
    }

    /** Downloads one file as raw bytes, stages it, verifies the hash, then
     * moves it into place atomically. */
    private void fetchFile(String baseUrl, String authHeader, Path etcRoot, HaSyncFiles.Entry entry)
            throws Exception {
        Path target = HaSyncFiles.resolveSafe(etcRoot, entry.relativePath());

        String encoded = URLEncoder.encode(entry.relativePath(), StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/rest/ha/sync/file?f=" + encoded))
                .header("Authorization", authHeader)
                .GET()
                .timeout(Duration.ofSeconds(120))
                .build();
        HttpResponse<byte[]> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " for " + entry.relativePath());
        }

        Files.createDirectories(target.getParent());
        Path staged = Files.createTempFile(target.getParent(), "." + target.getFileName(), ".sync");
        try {
            Files.write(staged, response.body());
            String stagedHash = HaSyncFiles.sha256(staged);
            if (!stagedHash.equals(entry.sha256())) {
                throw new IOException("hash mismatch for " + entry.relativePath()
                        + " (file changed on partner mid-transfer?)");
            }
            try {
                Files.move(staged, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Files.move(staged, target, StandardCopyOption.REPLACE_EXISTING);
            }
            LOG.debug("HA sync: wrote {}", entry.relativePath());
        } finally {
            Files.deleteIfExists(staged);
        }
    }

    /** Deletes local in-scope files that no longer exist on the partner. */
    private int propagateDeletions(Path etcRoot, Set<String> manifestPaths, List<String> excludes) {
        int deleted = 0;
        try {
            for (HaSyncFiles.Entry local : HaSyncFiles.buildManifest(etcRoot, excludes)) {
                if (manifestPaths.contains(local.relativePath())) {
                    continue;
                }
                Path p = HaSyncFiles.resolveSafe(etcRoot, local.relativePath());
                Files.deleteIfExists(p);
                deleted++;
                LOG.info("HA sync: deleted {} (removed on partner)", local.relativePath());
            }
        } catch (Exception e) {
            LOG.warn("HA sync: deletion propagation failed: {}", e.getMessage());
        }
        return deleted;
    }

    // -------------------------------------------------------------------------
    // SCV credential resolution
    // -------------------------------------------------------------------------

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
