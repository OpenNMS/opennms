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
package org.opennms.web.rest.v2.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Downloads a release asset into the temp area under the same limits as an
 * upload: https only, hosts from a short allowlist re-checked on every redirect,
 * the KAR size cap and the temp-area space cap.
 */
public class KarDownloader {
    private static final Logger LOG = LoggerFactory.getLogger(KarDownloader.class);

    public static final String HOSTS_PROPERTY = "org.opennms.plugins.download.hosts";
    static final Set<String> BUILT_IN_HOSTS = Set.of("github.com", "objects.githubusercontent.com", "release-assets.githubusercontent.com");
    static final int MAX_REDIRECTS = 5;
    private static final int BUFFER = 64 * 1024;

    public static final class Downloaded {
        private final Path part;
        private final String sha256;
        private final long size;
        private final URI finalUrl;

        Downloaded(final Path part, final String sha256, final long size, final URI finalUrl) {
            this.part = part;
            this.sha256 = sha256;
            this.size = size;
            this.finalUrl = finalUrl;
        }

        /** The downloaded bytes as a {@code .part} file; the caller commits or deletes it. */
        public Path getPart() { return part; }
        public String getSha256() { return sha256; }
        public long getSize() { return size; }
        public URI getFinalUrl() { return finalUrl; }
    }

    private final HttpFetcher fetcher;
    private final TempArea tempArea;
    private final Supplier<String> extraHosts;
    private final boolean requireHttps;

    public KarDownloader(final HttpFetcher fetcher, final TempArea tempArea) {
        this(fetcher, tempArea, () -> System.getProperty(HOSTS_PROPERTY, ""), true);
    }

    KarDownloader(final HttpFetcher fetcher, final TempArea tempArea, final Supplier<String> extraHosts, final boolean requireHttps) {
        this.fetcher = fetcher;
        this.tempArea = tempArea;
        this.extraHosts = extraHosts;
        this.requireHttps = requireHttps;
    }

    /** @param expectedSize the asset size GitHub reported, or -1 when unknown. */
    public Downloaded download(final URI url, final String assetName, final long expectedSize) throws PluginSourceException {
        if (expectedSize > KarInspector.MAX_SIZE_BYTES) {
            throw new PluginSourceException(400, PluginManagementRestService.TOO_LARGE);
        }
        final Path part;
        try {
            part = tempArea.newPartFile("download-");
        } catch (final IOException e) {
            throw diskFailure(e);
        }
        boolean keep = false;
        try {
            tempArea.ensureSpace(part, expectedSize);
            final Downloaded downloaded = follow(url, part, assetName, expectedSize);
            keep = true;
            return downloaded;
        } finally {
            if (!keep) {
                tempArea.abort(part);
            }
        }
    }

    private Downloaded follow(final URI url, final Path part, final String assetName, final long expectedSize) throws PluginSourceException {
        URI current = url;
        for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
            validate(current);
            HttpFetcher.Response response = null;
            try {
                try {
                    response = fetcher.get(current, Collections.emptyMap());
                } catch (final IOException e) {
                    LOG.warn("Cannot download {}: {}", current, e.toString());
                    throw PluginSourceException.transport(e, current.getHost(), ApacheHttpFetcher.READ_TIMEOUT_MS);
                }
                final int status = response.getStatus();
                if (status == 301 || status == 302 || status == 303 || status == 307 || status == 308) {
                    final String location = response.header("Location");
                    if (location == null) {
                        throw new PluginSourceException(PluginSourceException.BAD_GATEWAY, "The download of " + assetName + " redirected without a Location header. Try again later, " + PluginSourceException.FROM_FILE);
                    }
                    if (hop == MAX_REDIRECTS) {
                        throw new PluginSourceException(PluginSourceException.BAD_GATEWAY, "The download of " + assetName + " redirected more than " + MAX_REDIRECTS + " times. Try again later, " + PluginSourceException.FROM_FILE);
                    }
                    current = resolve(current, location, assetName);
                    continue;
                }
                if (status == 404) {
                    throw new PluginSourceException(404, assetName + " was not found at " + current + ".");
                }
                if (status != 200) {
                    throw new PluginSourceException(PluginSourceException.BAD_GATEWAY, PluginSourceException.label(current.getHost()) + " answered HTTP " + status + " for " + assetName + ". Try again later, " + PluginSourceException.FROM_FILE);
                }
                return save(response, part, current, assetName, expectedSize);
            } finally {
                if (response != null) {
                    try {
                        response.close();
                    } catch (final IOException e) {
                        LOG.debug("Cannot close the response for {}: {}", current, e.toString());
                    }
                }
            }
        }
        throw new PluginSourceException(PluginSourceException.BAD_GATEWAY, "The download of " + assetName + " redirected more than " + MAX_REDIRECTS + " times.");
    }

    /** Streams the body into the part file; the caller closes the response, which on failure aborts the connection instead of draining it. */
    private Downloaded save(final HttpFetcher.Response response, final Path part, final URI finalUrl, final String assetName, final long expectedSize) throws PluginSourceException {
        final long declared = response.contentLength();
        if (declared > KarInspector.MAX_SIZE_BYTES) {
            throw new PluginSourceException(400, PluginManagementRestService.TOO_LARGE);
        }
        if (declared > expectedSize) {
            tempArea.ensureSpace(part, declared);
        }
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException e) {
            throw new PluginSourceException(500, "SHA-256 is not available in this JVM.");
        }
        final InputStream in = response.getBody();
        long total = 0;
        try (OutputStream out = Files.newOutputStream(part)) {
            final byte[] buffer = new byte[BUFFER];
            while (true) {
                final int read;
                try {
                    read = in.read(buffer);
                } catch (final IOException e) {
                    throw interrupted(assetName, total, e);
                }
                if (read < 0) {
                    break;
                }
                total += read;
                if (total > KarInspector.MAX_SIZE_BYTES) {
                    throw new PluginSourceException(400, PluginManagementRestService.TOO_LARGE);
                }
                try {
                    out.write(buffer, 0, read);
                } catch (final IOException e) {
                    throw diskFailure(e);
                }
                digest.update(buffer, 0, read);
            }
        } catch (final IOException e) {
            throw diskFailure(e);
        }
        if (declared >= 0 && declared != total) {
            throw interrupted(assetName, total, null);
        }
        return new Downloaded(part, hex(digest.digest()), total, finalUrl);
    }

    void validate(final URI uri) throws PluginSourceException {
        final String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"https".equals(scheme) && (requireHttps || !"http".equals(scheme))) {
            throw new PluginSourceException(400, "Only https downloads are allowed; refusing " + uri + ".");
        }
        final String host = uri.getHost();
        if (host == null || uri.getRawUserInfo() != null) {
            throw new PluginSourceException(400, "The download URL has no usable host: " + uri + ".");
        }
        if (!allowedHosts(extraHosts.get()).contains(host.toLowerCase(Locale.ROOT))) {
            throw new PluginSourceException(400, "Downloads from " + host + " are not allowed. Add the host to " + HOSTS_PROPERTY + " in opennms.properties if it is trusted, " + PluginSourceException.FROM_FILE);
        }
    }

    static Set<String> allowedHosts(final String property) {
        final Set<String> hosts = new LinkedHashSet<>(BUILT_IN_HOSTS);
        if (property != null) {
            for (final String host : property.split(",")) {
                if (!host.isBlank()) {
                    hosts.add(host.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        return hosts;
    }

    private static URI resolve(final URI base, final String location, final String assetName) throws PluginSourceException {
        try {
            return base.resolve(new URI(location));
        } catch (final URISyntaxException | IllegalArgumentException e) {
            throw new PluginSourceException(PluginSourceException.BAD_GATEWAY, "The download of " + assetName + " redirected to an unparseable location. Try again later, " + PluginSourceException.FROM_FILE);
        }
    }

    private static PluginSourceException interrupted(final String assetName, final long received, final IOException cause) {
        final String reason = cause == null ? "the connection closed early" : PluginSourceException.describe(cause);
        return new PluginSourceException(PluginSourceException.BAD_GATEWAY, "The download of " + assetName + " was interrupted after " + TempArea.megabytes(received) + " MB (" + reason + "). Try again, " + PluginSourceException.FROM_FILE);
    }

    private PluginSourceException diskFailure(final IOException e) {
        return new PluginSourceException(PluginSourceException.INSUFFICIENT_STORAGE, "Cannot write to " + tempArea.getDir() + ": " + PluginSourceException.describe(e) + ". Free disk space under that directory and try again.");
    }

    static String hex(final byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }
}
