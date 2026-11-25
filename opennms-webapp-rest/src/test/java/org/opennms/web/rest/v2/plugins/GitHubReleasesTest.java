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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

public class GitHubReleasesTest {

    private static final String REPO = "OpenNMS-Plugins/alec";
    private static final String API = "https://api.github.com/repos/" + REPO + "/releases";
    private static final Pattern KARS = Pattern.compile(PluginCatalog.DEFAULT_ASSET_PATTERN);

    private static final String RELEASES_JSON = "[\n"
            + " {\"tag_name\":\"v3.0.4\",\"name\":\"ALEC 3.0.4\",\"published_at\":\"2026-08-01T10:00:00Z\",\"prerelease\":false,\"draft\":false,\"body\":\"Fixes.\",\n"
            + "  \"assets\":[{\"name\":\"opennms-alec-plugin.kar\",\"size\":97517568,\"browser_download_url\":\"https://github.com/OpenNMS-Plugins/alec/releases/download/v3.0.4/opennms-alec-plugin.kar\"},\n"
            + "             {\"name\":\"checksums.txt\",\"size\":120,\"browser_download_url\":\"https://github.com/OpenNMS-Plugins/alec/releases/download/v3.0.4/checksums.txt\"}]},\n"
            + " {\"tag_name\":\"v3.1.0-rc1\",\"name\":null,\"published_at\":\"2026-08-20T10:00:00Z\",\"prerelease\":true,\"draft\":false,\"body\":null,\n"
            + "  \"assets\":[{\"name\":\"opennms-alec-plugin-3.1.0-rc1.kar\",\"size\":5,\"browser_download_url\":\"https://github.com/OpenNMS-Plugins/alec/releases/download/v3.1.0-rc1/opennms-alec-plugin-3.1.0-rc1.kar\"}]},\n"
            + " {\"tag_name\":\"v3.0.3\",\"name\":\"no kar\",\"published_at\":\"2026-07-01T10:00:00Z\",\"prerelease\":false,\"draft\":false,\"body\":\"\",\n"
            + "  \"assets\":[{\"name\":\"source.zip\",\"size\":1,\"browser_download_url\":\"https://github.com/x/source.zip\"}]},\n"
            + " {\"tag_name\":\"v9.9.9\",\"draft\":true,\"assets\":[{\"name\":\"draft.kar\",\"size\":1,\"browser_download_url\":\"https://github.com/x/draft.kar\"}]}\n"
            + "]";

    private CannedFetcher fetcher;
    private MutableClock clock;
    private String token;
    private GitHubReleases releases;

    @Before
    public void setUp() {
        fetcher = new CannedFetcher();
        clock = new MutableClock(Instant.parse("2026-09-01T12:00:00Z"));
        token = null;
        releases = new GitHubReleases(fetcher, clock, () -> token);
    }

    @Test
    public void listsReleasesWithMatchingAssetsOnly() throws PluginSourceException {
        fetcher.on(API, () -> CannedFetcher.json(200, RELEASES_JSON, Map.of("ETag", "\"e1\"")));

        final GitHubReleases.Result result = releases.releases(REPO, KARS);

        assertEquals(REPO, result.getRepository());
        assertFalse(result.isCached());
        assertEquals("2026-09-01T12:00:00Z", result.getFetchedAt());
        assertEquals(2, result.getReleases().size());
        final GitHubReleases.Release latest = result.getReleases().get(0);
        assertEquals("v3.0.4", latest.getTag());
        assertEquals("ALEC 3.0.4", latest.getName());
        assertEquals("2026-08-01T10:00:00Z", latest.getPublishedAt());
        assertFalse(latest.isPrerelease());
        assertEquals("Fixes.", latest.getNotes());
        assertEquals(1, latest.getAssets().size());
        assertEquals("opennms-alec-plugin.kar", latest.getAssets().get(0).getName());
        assertEquals(97517568L, latest.getAssets().get(0).getSize());
        assertEquals("https://github.com/OpenNMS-Plugins/alec/releases/download/v3.0.4/opennms-alec-plugin.kar", latest.getAssets().get(0).getUrl());
        final GitHubReleases.Release rc = result.getReleases().get(1);
        assertEquals("v3.1.0-rc1", rc.getTag());
        assertNull(rc.getName());
        assertTrue(rc.isPrerelease());
        assertNull(rc.getNotes());

        final CannedFetcher.Request request = fetcher.requests.get(0);
        assertEquals(API + "?per_page=" + GitHubReleases.MAX_RELEASES, request.uri.toString());
        assertEquals("application/vnd.github+json", request.headers.get("Accept"));
        assertFalse(request.headers.containsKey("Authorization"));
        assertFalse(request.headers.containsKey("If-None-Match"));
    }

    @Test
    public void assetPatternNarrowsTheList() throws PluginSourceException {
        fetcher.on(API, () -> CannedFetcher.json(200, RELEASES_JSON, Map.of()));

        final GitHubReleases.Result result = releases.releases(REPO, Pattern.compile("opennms-alec-plugin\\.kar"));

        assertEquals(1, result.getReleases().size());
        assertEquals("v3.0.4", result.getReleases().get(0).getTag());
    }

    @Test
    public void notesAreCapped() throws Exception {
        final String longNotes = "x".repeat(GitHubReleases.MAX_NOTES + 100);
        final String json = "[{\"tag_name\":\"v1\",\"body\":\"" + longNotes + "\",\"assets\":[{\"name\":\"a.kar\",\"size\":1,\"browser_download_url\":\"https://github.com/a.kar\"}]}]";
        fetcher.on(API, () -> CannedFetcher.json(200, json, Map.of()));

        final String notes = releases.releases(REPO, KARS).getReleases().get(0).getNotes();

        assertEquals(GitHubReleases.MAX_NOTES + 1, notes.length());
        assertTrue(notes.endsWith("…"));
    }

    @Test
    public void tokenIsSentAsBearer() throws PluginSourceException {
        token = " ghp_secret ";
        fetcher.on(API, () -> CannedFetcher.json(200, "[]", Map.of()));

        releases.releases(REPO, KARS);

        assertEquals("Bearer ghp_secret", fetcher.requests.get(0).headers.get("Authorization"));
    }

    @Test
    public void secondCallWithinTheTtlIsServedFromMemory() throws PluginSourceException {
        fetcher.on(API, () -> CannedFetcher.json(200, RELEASES_JSON, Map.of("ETag", "\"e1\"")));
        releases.releases(REPO, KARS);
        clock.advance(Duration.ofMinutes(14));

        final GitHubReleases.Result result = releases.releases(REPO, KARS);

        assertTrue(result.isCached());
        assertEquals(1, fetcher.requests.size());
        assertEquals(2, result.getReleases().size());
        assertEquals("2026-09-01T12:00:00Z", result.getFetchedAt());
    }

    @Test
    public void expiredEntryIsRevalidatedWithTheEtag() throws PluginSourceException {
        fetcher.on(API, () -> CannedFetcher.json(200, RELEASES_JSON, Map.of("ETag", "\"e1\"")));
        releases.releases(REPO, KARS);
        clock.advance(Duration.ofMinutes(16));
        fetcher.on(API, () -> CannedFetcher.json(304, "", Map.of("ETag", "\"e1\"")));

        final GitHubReleases.Result result = releases.releases(REPO, KARS);

        assertEquals(2, fetcher.requests.size());
        assertEquals("\"e1\"", fetcher.requests.get(1).headers.get("If-None-Match"));
        assertTrue(result.isCached());
        assertEquals(2, result.getReleases().size());
        assertEquals("2026-09-01T12:16:00Z", result.getFetchedAt());

        clock.advance(Duration.ofMinutes(16));
        fetcher.on(API, () -> CannedFetcher.json(200, "[]", Map.of("ETag", "\"e2\"")));
        final GitHubReleases.Result changed = releases.releases(REPO, KARS);
        assertFalse(changed.isCached());
        assertTrue(changed.getReleases().isEmpty());
    }

    @Test
    public void rateLimitIsExplained() {
        fetcher.on(API, () -> CannedFetcher.json(403, "{\"message\":\"API rate limit exceeded\"}",
                Map.of("X-RateLimit-Remaining", "0", "X-RateLimit-Reset", "1756728000")));

        final PluginSourceException e = expect(REPO);

        assertEquals(502, e.getStatus());
        assertTrue(e.getMessage(), e.getMessage().startsWith("GitHub rate limit reached; resets at "));
        assertTrue(e.getMessage(), e.getMessage().contains("Set " + GitHubReleases.TOKEN_PROPERTY + " in opennms.properties to raise it, or load from a file."));
    }

    @Test
    public void unknownRepositoryIs404() {
        fetcher.on(API, () -> CannedFetcher.json(404, "{\"message\":\"Not Found\"}", Map.of()));

        final PluginSourceException e = expect(REPO);

        assertEquals(404, e.getStatus());
        assertEquals("Repository " + REPO + " was not found on GitHub.", e.getMessage());
    }

    @Test
    public void otherStatusesAreBadGateway() {
        fetcher.on(API, () -> CannedFetcher.json(503, "", Map.of()));

        final PluginSourceException e = expect(REPO);

        assertEquals(502, e.getStatus());
        assertEquals("GitHub answered HTTP 503 for " + REPO + ". Try again later, or load the plugin from a file instead.", e.getMessage());
    }

    @Test
    public void connectFailureIsExplained() {
        fetcher.failing(API, new UnknownHostException("api.github.com"));

        final PluginSourceException e = expect(REPO);

        assertEquals(502, e.getStatus());
        assertEquals("Could not reach api.github.com from this server: the name api.github.com cannot be resolved. "
                + "Check the server's network access or proxy settings, or load the plugin from a file instead.", e.getMessage());
    }

    @Test
    public void timeoutIsGatewayTimeout() {
        fetcher.failing(API, new SocketTimeoutException("Read timed out"));

        final PluginSourceException e = expect(REPO);

        assertEquals(504, e.getStatus());
        assertEquals("GitHub did not answer within 30 s. Try again later, or load the plugin from a file instead.", e.getMessage());
    }

    @Test
    public void malformedRepositoryNeverReachesTheNetwork() {
        for (final String bad : new String[] { null, "", "alec", "a/b/c", "../x/y", "OpenNMS Plugins/alec", "a/b?x=1" }) {
            assertFalse(String.valueOf(bad), GitHubReleases.isValidRepository(bad));
            assertEquals(400, expect(bad).getStatus());
        }
        assertTrue(fetcher.requests.isEmpty());
        assertTrue(GitHubReleases.isValidRepository("OpenNMS-Plugins/opennms-pagerduty-plugin"));
        assertTrue(GitHubReleases.isValidRepository("a.b_c/d-e.f"));
    }

    @Test
    public void nonArrayBodyIsReportedNotThrownRaw() {
        fetcher.on(API, () -> CannedFetcher.json(200, "{\"message\":\"weird\"}", Map.of()));

        final PluginSourceException e = expect(REPO);

        assertEquals(502, e.getStatus());
        assertEquals("GitHub returned an unexpected answer for " + REPO + " (GitHub returned something other than a release list). Try again later, or load the plugin from a file instead.", e.getMessage());
    }

    @Test
    public void oversizedReleaseListIsRefusedWithoutParsing() {
        final byte[] huge = new byte[GitHubReleases.MAX_BODY_MB * 1024 * 1024 + 1];
        java.util.Arrays.fill(huge, (byte) '[');
        fetcher.on(API, () -> CannedFetcher.bytes(200, huge, Map.of()));

        final PluginSourceException e = expect(REPO);

        assertEquals(502, e.getStatus());
        assertEquals("The release list for " + REPO + " is larger than 16 MB; this page cannot show it. Load the plugin from a file instead.", e.getMessage());
    }

    @Test
    public void unreadableErrorBodyStillReportsTheStatus() {
        fetcher.on(API, () -> new HttpFetcher.Response(503, Map.of(), new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("connection reset");
            }
        }, null));

        final PluginSourceException e = expect(REPO);

        assertEquals(502, e.getStatus());
        assertEquals("GitHub answered HTTP 503 for " + REPO + ". Try again later, or load the plugin from a file instead.", e.getMessage());
    }

    @Test(timeout = 10_000)
    public void aStalledRepositoryDoesNotBlockOthers() throws Exception {
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        fetcher.on(API, () -> {
            entered.countDown();
            try {
                release.await();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return CannedFetcher.json(200, RELEASES_JSON, Map.of());
        });
        fetcher.on("https://api.github.com/repos/me/other/releases", () -> CannedFetcher.json(200, "[]", Map.of()));
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        final Thread stalled = new Thread(() -> {
            try {
                releases.releases(REPO, KARS);
            } catch (final Throwable t) {
                failure.set(t);
            }
        });
        stalled.start();
        assertTrue(entered.await(5, TimeUnit.SECONDS));

        final GitHubReleases.Result other = releases.releases("me/other", KARS);

        assertEquals("me/other", other.getRepository());
        assertTrue(stalled.isAlive());
        release.countDown();
        stalled.join();
        assertNull(failure.get());
        assertEquals(2, releases.cachedRepositories());
    }

    @Test
    public void cacheForgetsTheLeastRecentlyUsedRepositoryPastTheBound() throws PluginSourceException {
        fetcher.on("https://api.github.com/repos/", () -> CannedFetcher.json(200, "[]", Map.of()));
        for (int i = 0; i <= GitHubReleases.MAX_REPOSITORIES; i++) {
            releases.releases("owner/repo" + i, KARS);
        }
        assertEquals(GitHubReleases.MAX_REPOSITORIES, releases.cachedRepositories());
        assertEquals(GitHubReleases.MAX_REPOSITORIES + 1, fetcher.requests.size());

        assertTrue(releases.releases("owner/repo1", KARS).isCached());
        assertFalse(releases.releases("owner/repo0", KARS).isCached());
        assertEquals(GitHubReleases.MAX_REPOSITORIES + 2, fetcher.requests.size());
    }

    private PluginSourceException expect(final String repository) {
        try {
            releases.releases(repository, KARS);
            fail("expected a PluginSourceException");
            return null;
        } catch (final PluginSourceException e) {
            return e;
        }
    }

    static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(final Instant now) {
            this.now = now;
        }

        void advance(final Duration by) {
            now = now.plus(by);
        }

        void set(final Instant instant) {
            now = instant;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(final java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
