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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class KarDownloaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private HttpServer server;
    private String base;
    private final byte[] payload = new byte[300_000];
    private final AtomicInteger okHits = new AtomicInteger();
    private final AtomicLong endlessBytes = new AtomicLong();
    private final CountDownLatch endlessStopped = new CountDownLatch(1);
    private Path tempDir;
    private TempArea tempArea;
    private KarDownloader downloader;

    @Before
    public void setUp() throws IOException {
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (i * 31);
        }
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        base = "http://127.0.0.1:" + server.getAddress().getPort();
        server.createContext("/ok.kar", exchange -> {
            okHits.incrementAndGet();
            send(exchange, 200, payload);
        });
        server.createContext("/redirect", exchange -> redirect(exchange, base + "/ok.kar"));
        server.createContext("/relative", exchange -> redirect(exchange, "/ok.kar"));
        server.createContext("/elsewhere", exchange -> redirect(exchange, "http://localhost:" + server.getAddress().getPort() + "/ok.kar"));
        server.createContext("/loop", exchange -> redirect(exchange, base + "/loop"));
        server.createContext("/missing", exchange -> send(exchange, 404, new byte[0]));
        server.createContext("/broken", exchange -> send(exchange, 500, new byte[0]));
        server.createContext("/huge", exchange -> {
            exchange.sendResponseHeaders(200, KarInspector.MAX_SIZE_BYTES + 1);
            exchange.close();
        });
        server.createContext("/short", exchange -> {
            exchange.sendResponseHeaders(200, 1000);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(payload, 0, 400);
            }
        });
        server.createContext("/endless", exchange -> {
            exchange.sendResponseHeaders(200, 0);
            final byte[] chunk = new byte[64 * 1024];
            try (OutputStream out = exchange.getResponseBody()) {
                while (true) {
                    out.write(chunk);
                    endlessBytes.addAndGet(chunk.length);
                }
            } catch (final IOException e) {
                endlessStopped.countDown();
            }
        });
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        tempDir = folder.getRoot().toPath().resolve("tmp");
        tempArea = new TempArea(tempDir, Clock.systemUTC(), Map::of, TempArea.CAP_BYTES);
        downloader = new KarDownloader(new ApacheHttpFetcher("OpenNMS/test"), tempArea, () -> "127.0.0.1", false);
    }

    @After
    public void tearDown() {
        server.stop(0);
        ((ExecutorService) server.getExecutor()).shutdownNow();
    }

    @Test
    public void downloadsIntoAPartFileWithItsSha256() throws Exception {
        final KarDownloader.Downloaded downloaded = downloader.download(URI.create(base + "/ok.kar"), "ok.kar", payload.length);

        assertTrue(downloaded.getPart().getFileName().toString().endsWith(".part"));
        assertEquals(tempDir, downloaded.getPart().getParent());
        assertEquals(payload.length, downloaded.getSize());
        assertEquals(KarInspector.sha256(downloaded.getPart()), downloaded.getSha256());
        assertEquals(base + "/ok.kar", downloaded.getFinalUrl().toString());

        tempArea.commit(downloaded.getPart(), downloaded.getSha256(), "ok.kar", "github:me/ok@v1");
        assertEquals(downloaded.getSha256(), KarInspector.sha256(tempArea.karFile(downloaded.getSha256())));
        assertEquals("ok.kar", tempArea.fileName(downloaded.getSha256()).orElseThrow());
        assertEquals("github:me/ok@v1", tempArea.source(downloaded.getSha256()).orElseThrow());
        assertEquals(3, tempArea.usage().getFiles());
    }

    @Test
    public void followsRedirectsToAllowedHosts() throws Exception {
        final KarDownloader.Downloaded downloaded = downloader.download(URI.create(base + "/redirect"), "ok.kar", -1);
        assertEquals(base + "/ok.kar", downloaded.getFinalUrl().toString());
        assertEquals(payload.length, downloaded.getSize());

        final KarDownloader.Downloaded relative = downloader.download(URI.create(base + "/relative"), "ok.kar", -1);
        assertEquals(base + "/ok.kar", relative.getFinalUrl().toString());
        assertEquals(2, okHits.get());
    }

    @Test
    public void redirectToADisallowedHostIsRefusedBeforeItIsFollowed() {
        final PluginSourceException e = expect(base + "/elsewhere", "ok.kar", -1);

        assertEquals(400, e.getStatus());
        assertTrue(e.getMessage(), e.getMessage().startsWith("Downloads from localhost are not allowed. Add the host to " + KarDownloader.HOSTS_PROPERTY));
        assertEquals(0, okHits.get());
        assertTrue(partFiles().isEmpty());
    }

    @Test
    public void redirectLoopsGiveUp() {
        final PluginSourceException e = expect(base + "/loop", "ok.kar", -1);

        assertEquals(502, e.getStatus());
        assertTrue(e.getMessage(), e.getMessage().contains("redirected more than " + KarDownloader.MAX_REDIRECTS + " times"));
    }

    @Test
    public void httpsIsRequiredInProduction() {
        final KarDownloader strict = new KarDownloader(new ApacheHttpFetcher("OpenNMS/test"), tempArea, () -> "127.0.0.1", true);

        try {
            strict.download(URI.create(base + "/ok.kar"), "ok.kar", -1);
            fail("expected 400");
        } catch (final PluginSourceException e) {
            assertEquals(400, e.getStatus());
            assertTrue(e.getMessage(), e.getMessage().startsWith("Only https downloads are allowed"));
        }
        assertEquals(0, okHits.get());
    }

    @Test
    public void hostsOutsideTheAllowlistAreRefused() {
        final PluginSourceException e = expect("https://example.com/plugin.kar", "plugin.kar", -1);

        assertEquals(400, e.getStatus());
        assertEquals("Downloads from example.com are not allowed. Add the host to " + KarDownloader.HOSTS_PROPERTY
                + " in opennms.properties if it is trusted, or load the plugin from a file instead.", e.getMessage());
        assertEquals(400, expect("https://user@github.com/plugin.kar", "plugin.kar", -1).getStatus());
    }

    @Test
    public void allowlistCombinesBuiltInAndConfiguredHosts() {
        assertEquals(Set.of("github.com", "objects.githubusercontent.com", "release-assets.githubusercontent.com"), KarDownloader.allowedHosts(null));
        assertEquals(Set.of("github.com", "objects.githubusercontent.com", "release-assets.githubusercontent.com", "mirror.example.org", "other.example.org"),
                KarDownloader.allowedHosts(" Mirror.example.org, ,other.example.org "));
    }

    @Test
    public void gitHubRedirectToItsReleaseAssetHostIsFollowed() throws Exception {
        final String start = "https://github.com/OpenNMS-Plugins/alec/releases/download/v1/ok.kar";
        final String asset = "https://release-assets.githubusercontent.com/github-production-release-asset/1/2?sig=abc";
        final CannedFetcher canned = new CannedFetcher()
                .on(start, () -> CannedFetcher.bytes(302, new byte[0], Map.of("Location", asset)))
                .on(asset, () -> CannedFetcher.bytes(200, payload, Map.of("Content-Length", String.valueOf(payload.length))));
        final KarDownloader production = new KarDownloader(canned, tempArea, () -> "", true);

        final KarDownloader.Downloaded downloaded = production.download(URI.create(start), "ok.kar", payload.length);

        assertEquals(asset, downloaded.getFinalUrl().toString());
        assertEquals(payload.length, downloaded.getSize());
        assertEquals(KarInspector.sha256(downloaded.getPart()), downloaded.getSha256());
        assertEquals(2, canned.requests.size());
    }

    @Test
    public void gitHubRedirectToAnUnlistedHostIsRefused() {
        final String start = "https://github.com/OpenNMS-Plugins/alec/releases/download/v1/ok.kar";
        final CannedFetcher canned = new CannedFetcher()
                .on(start, () -> CannedFetcher.bytes(302, new byte[0], Map.of("Location", "https://cdn.example.net/ok.kar")));
        final KarDownloader production = new KarDownloader(canned, tempArea, () -> "", true);

        try {
            production.download(URI.create(start), "ok.kar", payload.length);
            fail("expected 400");
        } catch (final PluginSourceException e) {
            assertEquals(400, e.getStatus());
            assertTrue(e.getMessage(), e.getMessage().startsWith("Downloads from cdn.example.net are not allowed."));
        }
        assertEquals(1, canned.requests.size());
        assertTrue(partFiles().isEmpty());
        assertEquals(0, tempArea.reservedBytes());
    }

    @Test(timeout = 120_000)
    public void bodyThatNeverEndsStopsAtTheCapWithoutDrainingTheConnection() throws Exception {
        final PluginSourceException e = expect(base + "/endless", "endless.kar", -1);

        assertEquals(400, e.getStatus());
        assertEquals(PluginManagementRestService.TOO_LARGE, e.getMessage());
        assertTrue("the server noticed the closed connection", endlessStopped.await(30, TimeUnit.SECONDS));
        assertTrue("sent " + endlessBytes.get(), endlessBytes.get() < KarInspector.MAX_SIZE_BYTES + 64L * 1024 * 1024);
        assertTrue(partFiles().isEmpty());
        assertEquals(0, tempArea.reservedBytes());
    }

    @Test
    public void connectionIsClosedBeforeTheBodyWhenADownloadFails() throws Exception {
        final AtomicBoolean resourceClosed = new AtomicBoolean();
        final AtomicBoolean bodyClosedAfterResource = new AtomicBoolean();
        final InputStream failing = new InputStream() {
            private int sent;

            @Override
            public int read() throws IOException {
                if (sent++ < 100) {
                    return 'x';
                }
                throw new IOException("connection reset");
            }

            @Override
            public void close() {
                bodyClosedAfterResource.set(resourceClosed.get());
            }
        };
        final CannedFetcher canned = new CannedFetcher()
                .on("https://github.com/", () -> new HttpFetcher.Response(200, Map.of("Content-Length", "1000"), failing, () -> resourceClosed.set(true)));
        final KarDownloader production = new KarDownloader(canned, tempArea, () -> "", true);

        try {
            production.download(URI.create("https://github.com/x/y.kar"), "y.kar", 1000);
            fail("expected 502");
        } catch (final PluginSourceException e) {
            assertEquals(502, e.getStatus());
            assertTrue(e.getMessage(), e.getMessage().startsWith("The download of y.kar was interrupted after 0.0 MB (connection reset)"));
        }
        assertTrue(resourceClosed.get());
        assertTrue(bodyClosedAfterResource.get());
        assertTrue(partFiles().isEmpty());
    }

    @Test
    public void declaredSizeAboveTheCapIsRefusedWithoutDownloading() {
        assertEquals(400, expect(base + "/ok.kar", "ok.kar", KarInspector.MAX_SIZE_BYTES + 1).getStatus());
        assertEquals(0, okHits.get());

        final PluginSourceException e = expect(base + "/huge", "huge.kar", -1);
        assertEquals(400, e.getStatus());
        assertEquals(PluginManagementRestService.TOO_LARGE, e.getMessage());
        assertTrue(partFiles().isEmpty());
    }

    @Test
    public void truncatedBodyIsReportedAsInterrupted() {
        final PluginSourceException e = expect(base + "/short", "short.kar", 1000);

        assertEquals(502, e.getStatus());
        assertTrue(e.getMessage(), e.getMessage().startsWith("The download of short.kar was interrupted after 0.0 MB ("));
        assertTrue(e.getMessage(), e.getMessage().endsWith("Try again, or load the plugin from a file instead."));
        assertTrue(partFiles().isEmpty());
    }

    @Test
    public void missingAssetIs404AndServerErrorsAreBadGateway() {
        final PluginSourceException missing = expect(base + "/missing", "gone.kar", -1);
        assertEquals(404, missing.getStatus());
        assertEquals("gone.kar was not found at " + base + "/missing.", missing.getMessage());

        final PluginSourceException broken = expect(base + "/broken", "broken.kar", -1);
        assertEquals(502, broken.getStatus());
        assertEquals("127.0.0.1 answered HTTP 500 for broken.kar. Try again later, or load the plugin from a file instead.", broken.getMessage());
    }

    @Test
    public void connectionRefusedIsExplained() throws IOException {
        final int port;
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            port = socket.getLocalPort();
        }

        final PluginSourceException e = expect("http://127.0.0.1:" + port + "/ok.kar", "ok.kar", -1);

        assertEquals(502, e.getStatus());
        assertTrue(e.getMessage(), e.getMessage().startsWith("Could not reach 127.0.0.1 from this server: "));
        assertTrue(e.getMessage(), e.getMessage().endsWith("Check the server's network access or proxy settings, or load the plugin from a file instead."));
    }

    @Test
    public void noTemporarySpaceIs507() {
        final TempArea tiny = new TempArea(tempDir, Clock.systemUTC(), Map::of, 1000);
        final KarDownloader small = new KarDownloader(new ApacheHttpFetcher("OpenNMS/test"), tiny, () -> "127.0.0.1", false);

        try {
            small.download(URI.create(base + "/ok.kar"), "ok.kar", payload.length);
            fail("expected 507");
        } catch (final PluginSourceException e) {
            assertEquals(507, e.getStatus());
            assertTrue(e.getMessage(), e.getMessage().startsWith(TempArea.NO_SPACE));
        }
        assertEquals(0, okHits.get());
    }

    private PluginSourceException expect(final String url, final String assetName, final long expectedSize) {
        try {
            downloader.download(URI.create(url), assetName, expectedSize);
            fail("expected a PluginSourceException for " + url);
            return null;
        } catch (final PluginSourceException e) {
            return e;
        }
    }

    private List<Path> partFiles() {
        final List<Path> parts = new ArrayList<>();
        if (!Files.isDirectory(tempDir)) {
            return parts;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempDir, "*.part")) {
            stream.forEach(parts::add);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
        return parts;
    }

    private static void send(final HttpExchange exchange, final int status, final byte[] body) throws IOException {
        exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private static void redirect(final HttpExchange exchange, final String location) throws IOException {
        exchange.getResponseHeaders().add("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

}
