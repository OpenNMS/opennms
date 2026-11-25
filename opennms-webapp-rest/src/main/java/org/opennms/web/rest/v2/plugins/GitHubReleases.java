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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Lists the releases of a GitHub repository through the public REST API, keeping
 * each answer for a while and revalidating it with the ETag afterwards so the
 * unauthenticated rate limit is not spent on page reloads.
 */
public class GitHubReleases {
    private static final Logger LOG = LoggerFactory.getLogger(GitHubReleases.class);

    public static final String TOKEN_PROPERTY = "org.opennms.plugins.github.token";
    static final Pattern REPOSITORY = Pattern.compile("^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$");
    static final Duration CACHE_TTL = Duration.ofMinutes(15);
    static final int MAX_NOTES = 4000;
    static final int MAX_RELEASES = 30;
    static final String API_HOST = "api.github.com";
    static final int MAX_BODY_MB = 16;
    static final int MAX_REPOSITORIES = 64;
    private static final int MAX_BODY = MAX_BODY_MB * 1024 * 1024;

    public static class Asset {
        private String name;
        private long size;
        private String url;

        public Asset() {
        }

        public Asset(final String name, final long size, final String url) {
            this.name = name;
            this.size = size;
            this.url = url;
        }

        public String getName() { return name; }
        public void setName(final String name) { this.name = name; }
        public long getSize() { return size; }
        public void setSize(final long size) { this.size = size; }
        public String getUrl() { return url; }
        public void setUrl(final String url) { this.url = url; }
    }

    public static class Release {
        private String tag;
        private String name;
        private String publishedAt;
        private boolean prerelease;
        private String notes;
        private List<Asset> assets = new ArrayList<>();

        public String getTag() { return tag; }
        public void setTag(final String tag) { this.tag = tag; }
        public String getName() { return name; }
        public void setName(final String name) { this.name = name; }
        public String getPublishedAt() { return publishedAt; }
        public void setPublishedAt(final String publishedAt) { this.publishedAt = publishedAt; }
        public boolean isPrerelease() { return prerelease; }
        public void setPrerelease(final boolean prerelease) { this.prerelease = prerelease; }
        public String getNotes() { return notes; }
        public void setNotes(final String notes) { this.notes = notes; }
        public List<Asset> getAssets() { return assets; }
        public void setAssets(final List<Asset> assets) { this.assets = assets; }

        Release filtered(final Pattern assetPattern) {
            final Release copy = new Release();
            copy.tag = tag;
            copy.name = name;
            copy.publishedAt = publishedAt;
            copy.prerelease = prerelease;
            copy.notes = notes;
            for (final Asset asset : assets) {
                if (asset.getName() != null && assetPattern.matcher(asset.getName()).matches()) {
                    copy.assets.add(asset);
                }
            }
            return copy;
        }
    }

    public static class Result {
        private String repository;
        private List<Release> releases = new ArrayList<>();
        private String fetchedAt;
        private boolean cached;

        public String getRepository() { return repository; }
        public void setRepository(final String repository) { this.repository = repository; }
        public List<Release> getReleases() { return releases; }
        public void setReleases(final List<Release> releases) { this.releases = releases; }
        public String getFetchedAt() { return fetchedAt; }
        public void setFetchedAt(final String fetchedAt) { this.fetchedAt = fetchedAt; }
        public boolean isCached() { return cached; }
        public void setCached(final boolean cached) { this.cached = cached; }
    }

    private static final class CacheEntry {
        final List<Release> releases;
        final String etag;
        Instant fetchedAt;

        CacheEntry(final List<Release> releases, final String etag, final Instant fetchedAt) {
            this.releases = releases;
            this.etag = etag;
            this.fetchedAt = fetchedAt;
        }
    }

    private final HttpFetcher fetcher;
    private final Clock clock;
    private final Supplier<String> token;
    private final ObjectMapper mapper = new ObjectMapper();
    // Both maps are guarded by their own monitor and drop the least recently used repository past the bound.
    private final Map<String, CacheEntry> cache = new LruMap<>();
    private final Map<String, Object> locks = new LruMap<>();

    private static final class LruMap<V> extends LinkedHashMap<String, V> {
        private static final long serialVersionUID = 1L;

        LruMap() {
            super(16, 0.75f, true);
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<String, V> eldest) {
            return size() > MAX_REPOSITORIES;
        }
    }

    public GitHubReleases(final HttpFetcher fetcher) {
        this(fetcher, Clock.systemUTC(), () -> System.getProperty(TOKEN_PROPERTY));
    }

    GitHubReleases(final HttpFetcher fetcher, final Clock clock, final Supplier<String> token) {
        this.fetcher = fetcher;
        this.clock = clock;
        this.token = token;
    }

    public static boolean isValidRepository(final String repository) {
        return repository != null && REPOSITORY.matcher(repository).matches() && !repository.contains("..");
    }

    /**
     * Releases that carry at least one asset matching the pattern, newest first as
     * GitHub orders them. Lookups of one repository are serialised so a burst of
     * page loads costs one API request; other repositories are not held up.
     */
    public Result releases(final String repository, final Pattern assetPattern) throws PluginSourceException {
        if (!isValidRepository(repository)) {
            throw new PluginSourceException(400, "The repository must be given as owner/name.");
        }
        final Object lock;
        synchronized (locks) {
            lock = locks.computeIfAbsent(repository, r -> new Object());
        }
        synchronized (lock) {
            final Instant now = clock.instant();
            CacheEntry entry;
            synchronized (cache) {
                entry = cache.get(repository);
            }
            boolean cached = true;
            if (entry == null || entry.fetchedAt.plus(CACHE_TTL).isBefore(now)) {
                final CacheEntry fresh = fetch(repository, entry);
                cached = fresh == entry;
                if (fresh != entry) {
                    synchronized (cache) {
                        cache.put(repository, fresh);
                    }
                }
                entry = fresh;
            }
            final Result result = new Result();
            result.setRepository(repository);
            result.setFetchedAt(entry.fetchedAt.toString());
            result.setCached(cached);
            for (final Release release : entry.releases) {
                final Release filtered = release.filtered(assetPattern);
                if (!filtered.getAssets().isEmpty()) {
                    result.getReleases().add(filtered);
                }
            }
            return result;
        }
    }

    void clearCache() {
        synchronized (cache) {
            cache.clear();
        }
    }

    int cachedRepositories() {
        synchronized (cache) {
            return cache.size();
        }
    }

    private CacheEntry fetch(final String repository, final CacheEntry previous) throws PluginSourceException {
        final URI uri = URI.create("https://" + API_HOST + "/repos/" + repository + "/releases?per_page=" + MAX_RELEASES);
        final Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/vnd.github+json");
        headers.put("X-GitHub-Api-Version", "2022-11-28");
        final String bearer = token.get();
        if (bearer != null && !bearer.isBlank()) {
            headers.put("Authorization", "Bearer " + bearer.trim());
        }
        if (previous != null && previous.etag != null) {
            headers.put("If-None-Match", previous.etag);
        }
        final Instant now = clock.instant();
        final byte[] body;
        final String etag;
        try (HttpFetcher.Response response = fetcher.get(uri, headers)) {
            final int status = response.getStatus();
            if (status == 304 && previous != null) {
                previous.fetchedAt = now;
                return previous;
            }
            if (status != 200) {
                throw failure(repository, response, bearer != null && !bearer.isBlank());
            }
            body = response.bodyBytes(MAX_BODY + 1);
            etag = response.header("ETag");
        } catch (final IOException e) {
            LOG.warn("GitHub release lookup for {} failed: {}", repository, e.toString());
            throw PluginSourceException.transport(e, API_HOST, ApacheHttpFetcher.READ_TIMEOUT_MS);
        }
        if (body.length > MAX_BODY) {
            LOG.warn("GitHub release list for {} exceeds {} MB", repository, MAX_BODY_MB);
            throw new PluginSourceException(PluginSourceException.BAD_GATEWAY, "The release list for " + repository + " is larger than " + MAX_BODY_MB + " MB; this page cannot show it. Load the plugin from a file instead.");
        }
        try {
            return new CacheEntry(parse(new String(body, StandardCharsets.UTF_8)), etag, now);
        } catch (final IOException e) {
            LOG.warn("GitHub release list for {} is unusable: {}", repository, e.toString());
            throw new PluginSourceException(PluginSourceException.BAD_GATEWAY, "GitHub returned an unexpected answer for " + repository + " (" + PluginSourceException.describe(e) + "). Try again later, " + PluginSourceException.FROM_FILE);
        }
    }

    private static PluginSourceException failure(final String repository, final HttpFetcher.Response response, final boolean withToken) {
        final int status = response.getStatus();
        String body;
        try {
            body = response.bodyAsString(4096);
        } catch (final IOException e) {
            LOG.debug("Cannot read the body of the HTTP {} answer for {}: {}", status, repository, e.toString());
            body = "";
        }
        if ((status == 403 || status == 429) && isRateLimited(response, body)) {
            return PluginSourceException.rateLimited(response.header("X-RateLimit-Reset"));
        }
        if (status == 404) {
            return new PluginSourceException(404, "Repository " + repository + " was not found on GitHub.");
        }
        if (status == 401) {
            return new PluginSourceException(PluginSourceException.BAD_GATEWAY, withToken
                    ? "GitHub rejected the token configured in " + TOKEN_PROPERTY + " (HTTP 401). Fix or remove it, " + PluginSourceException.FROM_FILE
                    : "GitHub refused the request (HTTP 401). " + capitalize(PluginSourceException.FROM_FILE));
        }
        return new PluginSourceException(PluginSourceException.BAD_GATEWAY, "GitHub answered HTTP " + status + " for " + repository + ". Try again later, " + PluginSourceException.FROM_FILE);
    }

    private static boolean isRateLimited(final HttpFetcher.Response response, final String body) {
        final String remaining = response.header("X-RateLimit-Remaining");
        if (remaining != null && "0".equals(remaining.trim())) {
            return true;
        }
        return body != null && body.toLowerCase(Locale.ROOT).contains("rate limit");
    }

    private static String capitalize(final String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    List<Release> parse(final String json) throws IOException {
        final JsonNode root = mapper.readTree(json);
        final List<Release> releases = new ArrayList<>();
        if (root == null || !root.isArray()) {
            throw new IOException("GitHub returned something other than a release list");
        }
        for (final JsonNode node : root) {
            if (node.path("draft").asBoolean(false)) {
                continue;
            }
            final Release release = new Release();
            release.setTag(text(node, "tag_name"));
            release.setName(text(node, "name"));
            release.setPublishedAt(text(node, "published_at"));
            release.setPrerelease(node.path("prerelease").asBoolean(false));
            final String notes = text(node, "body");
            release.setNotes(notes != null && notes.length() > MAX_NOTES ? notes.substring(0, MAX_NOTES) + "…" : notes);
            for (final JsonNode assetNode : node.path("assets")) {
                final String name = text(assetNode, "name");
                final String url = text(assetNode, "browser_download_url");
                if (name == null || url == null) {
                    continue;
                }
                release.getAssets().add(new Asset(name, assetNode.path("size").asLong(-1), url));
            }
            if (release.getTag() != null) {
                releases.add(release);
            }
        }
        return releases;
    }

    private static String text(final JsonNode node, final String field) {
        final JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
