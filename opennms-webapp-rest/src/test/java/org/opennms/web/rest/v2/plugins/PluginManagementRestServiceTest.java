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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.time.Clock;
import java.util.Arrays;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.SecurityContext;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.web.api.Authentication;

public class PluginManagementRestServiceTest {

    private static final String FEATURES_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<features xmlns=\"http://karaf.apache.org/xmlns/features/v1.4.0\" name=\"alec\">\n"
            + "  <feature name=\"alec\" version=\"1.0.0\"><bundle>mvn:org.example/alec/1.0.0</bundle></feature>\n"
            + "</features>\n";

    private static final String MULTI_FEATURES_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<features xmlns=\"http://karaf.apache.org/xmlns/features/v1.4.0\" name=\"alec\">\n"
            + "  <feature name=\"alec-api\" version=\"3.0.5\"><bundle>mvn:org.example/alec-api/3.0.5</bundle></feature>\n"
            + "  <feature name=\"alec-opennms-standalone\" version=\"3.0.5\" description=\"Everything on the core\"><feature>alec-api</feature></feature>\n"
            + "  <feature name=\"alec-opennms-distributed\" version=\"3.0.5\" description=\"Core side of a Sentinel deployment\"><feature>alec-api</feature></feature>\n"
            + "  <feature name=\"alec-sentinel-distributed\" version=\"3.0.5\"><feature>alec-api</feature></feature>\n"
            + "</features>\n";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path home;
    private Path deployDir;
    private Path bootDir;
    private Path uploadDir;
    private FakeKarafBridge bridge;
    private CannedFetcher fetcher;
    private PluginManagementRestService service;

    @Before
    public void setUp() throws IOException {
        home = folder.getRoot().toPath();
        deployDir = home.resolve("deploy");
        bootDir = home.resolve("etc").resolve("featuresBoot.d");
        uploadDir = home.resolve("data").resolve("tmp").resolve("plugin-management");
        Files.createDirectories(bootDir);
        bridge = new FakeKarafBridge();
        fetcher = new CannedFetcher();
        service = new PluginManagementRestService(home, bridge, fetcher, Clock.systemUTC());
    }

    @After
    public void tearDown() {
        service.shutdown();
    }

    @Test
    public void karNamesStartWithALetterOrDigit() {
        for (final String ok : new String[] { "alec", "opennms-alec-plugin", "a.b-c_d", "1st", "A" }) {
            assertTrue(ok, PluginManagementRestService.KAR_NAME.matcher(ok).matches());
        }
        for (final String bad : new String[] { "", ".hidden", "-x", "_x", "a b", "a/b", "a\nb", "..", "a\0" }) {
            assertFalse(bad, PluginManagementRestService.KAR_NAME.matcher(bad).matches());
        }
    }

    @Test
    public void auditFieldsLoseControlCharacters() {
        assertEquals("abc", PluginManagementRestService.clean("a\nb\rc"));
        assertEquals("action=x[0m user=y", PluginManagementRestService.clean("action=x\u001b[0m user=y"));
        assertNull(PluginManagementRestService.clean(null));
    }

    @Test
    public void bootLinesMatchTheExactWaitForKarToken() {
        assertTrue(PluginManagementRestService.waitsForKar("opennms-alec wait-for-kar=alec", "alec"));
        assertTrue(PluginManagementRestService.waitsForKar("  wait-for-kar=alec\topennms-alec ", "alec"));
        assertFalse(PluginManagementRestService.waitsForKar("opennms-alec wait-for-kar=alec-ui", "alec"));
        assertFalse(PluginManagementRestService.waitsForKar("opennms-alec wait-for-kar=alec", "ale"));
        assertFalse(PluginManagementRestService.waitsForKar("# opennms-alec wait-for-kar=alec", "alec"));
        assertFalse(PluginManagementRestService.waitsForKar("opennms-alec", "alec"));
        assertFalse(PluginManagementRestService.waitsForKar("", "alec"));
    }

    @Test
    public void unloadCleansEveryBootFileThatWaitsForTheKar() throws IOException {
        Files.createDirectories(deployDir);
        Files.write(deployDir.resolve("alec.kar"), new byte[] { 1 });
        Files.write(bootDir.resolve("shared.boot"), "# two plugins\nopennms-other wait-for-kar=other\nopennms-alec wait-for-kar=alec\n".getBytes(StandardCharsets.UTF_8));
        Files.write(bootDir.resolve("only.boot"), "# just alec\nalec-ui wait-for-kar=alec\n\n".getBytes(StandardCharsets.UTF_8));
        Files.write(bootDir.resolve("untouched.boot"), "opennms-other wait-for-kar=other\n".getBytes(StandardCharsets.UTF_8));
        Files.write(bootDir.resolve("alec.boot"), "alec wait-for-kar=alec\n".getBytes(StandardCharsets.UTF_8));

        final PluginActionResult result = service.unload("alec", admin(), null);

        assertEquals(Arrays.asList("etc/featuresBoot.d/alec.boot", "etc/featuresBoot.d/only.boot", "etc/featuresBoot.d/shared.boot"), result.getBootFilesRemoved());
        assertFalse(Files.exists(deployDir.resolve("alec.kar")));
        assertFalse(Files.exists(bootDir.resolve("alec.boot")));
        assertFalse(Files.exists(bootDir.resolve("only.boot")));
        assertEquals("# two plugins\nopennms-other wait-for-kar=other\n", new String(Files.readAllBytes(bootDir.resolve("shared.boot")), StandardCharsets.UTF_8));
        assertEquals("opennms-other wait-for-kar=other\n", new String(Files.readAllBytes(bootDir.resolve("untouched.boot")), StandardCharsets.UTF_8));
        assertTrue(result.isRestartRequired());
        assertEquals(PluginEntry.STATUS_UNLOADED, result.getPlugin().getStatus());
        assertEquals("alec.kar", result.getPlugin().getFileName());
        assertTrue(result.getPlugin().isPendingRestart());
    }

    @Test
    public void unloadOnlyKnowsExactKarSuffix() throws IOException {
        Files.createDirectories(deployDir);
        Files.write(deployDir.resolve("alec.KAR"), new byte[] { 1 });
        Assume.assumeFalse("case-insensitive file system", Files.exists(deployDir.resolve("alec.kar")));

        try {
            service.unload("alec", admin(), null);
            fail("expected 404");
        } catch (final WebApplicationException e) {
            assertEquals(404, e.getResponse().getStatus());
        }
        assertTrue(Files.exists(deployDir.resolve("alec.KAR")));
    }

    @Test
    public void unloadRejectsNamesTheExtenderWouldSkip() {
        try {
            service.unload(".alec", admin(), null);
            fail("expected 400");
        } catch (final WebApplicationException e) {
            assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void installWritesBootFileAndRecordBeforeTheKarAndKeepsTheUpload() throws IOException {
        final String token = storeUpload("alec-1.0.kar", true);

        final PluginActionResult result = service.install(request(token, "alec"), admin(), null);

        assertTrue(Files.isRegularFile(deployDir.resolve("alec.kar")));
        assertEquals("# Managed by the Plugin Management page; remove together with deploy/alec.kar\nalec wait-for-kar=alec\n",
                new String(Files.readAllBytes(bootDir.resolve("alec.boot")), StandardCharsets.UTF_8));
        assertTrue(Files.isRegularFile(uploadDir.resolve(token + ".kar")));
        assertTrue(Files.isRegularFile(uploadDir.resolve(token + ".name")));
        assertFalse(result.isRestartRequired());
        assertTrue(result.getPlugin().isAutoStart());
        assertFalse(result.getPlugin().isPendingRestart());
        assertEquals("etc/featuresBoot.d/alec.boot", result.getPlugin().getBootFile());
        assertEquals("alec-1.0.kar", result.getPlugin().getFileName());
        assertEquals(PluginEntry.STATUS_STAGED, result.getPlugin().getStatus());
        assertFalse(service.status(admin()).isRestartRequired());

        try {
            service.install(request(token, "alec"), admin(), null);
            fail("expected 409");
        } catch (final WebApplicationException e) {
            assertEquals(409, e.getResponse().getStatus());
            assertTrue(String.valueOf(e.getResponse().getEntity()), String.valueOf(e.getResponse().getEntity()).contains("already loaded"));
        }
    }

    @Test
    public void installWithoutAutoStartFlagsARestart() throws IOException {
        final String token = storeUpload("alec-1.0.kar", false);

        final PluginActionResult result = service.install(request(token, "alec"), admin(), null);

        assertTrue(result.isRestartRequired());
        assertFalse(result.getPlugin().isAutoStart());
        assertTrue(result.getPlugin().isPendingRestart());
        assertTrue(service.status(admin()).isRestartRequired());
    }

    @Test
    public void failedKarMoveLeavesNoBootFileOrRecord() throws IOException {
        final String token = storeUpload("alec-1.0.kar", true);
        Files.createDirectories(deployDir.resolve("alec.kar"));
        Files.write(deployDir.resolve("alec.kar").resolve("blocker"), new byte[] { 1 });

        try {
            service.install(request(token, "alec"), admin(), null);
            fail("expected 500");
        } catch (final WebApplicationException e) {
            assertEquals(500, e.getResponse().getStatus());
        }

        assertFalse(Files.exists(bootDir.resolve("alec.boot")));
        assertFalse(Files.exists(deployDir.resolve(".alec.kar.part")));
        assertTrue(service.status(admin()).getPlugins().stream().noneMatch(PluginEntry::isManaged));
        assertTrue(Files.isRegularFile(uploadDir.resolve(token + ".kar")));
    }

    @Test
    public void catalogIsServedLocally() {
        final PluginCatalogResponse catalog = service.catalog(admin(), null);

        assertEquals(3, catalog.getEntries().size());
        assertEquals("alec", catalog.getEntries().get(0).getId());
        assertEquals(Arrays.asList("alec-opennms-standalone"), catalog.getEntries().get(0).getBootFeatures());
        assertTrue(catalog.isCustomAllowed());
        assertTrue(fetcher.requests.isEmpty());
    }

    @Test
    public void installWritesOnlyTheChosenTopLevelFeatures() throws IOException {
        final String token = storeUpload("opennms-alec-plugin.kar", true, MULTI_FEATURES_XML);
        final InstallRequest request = request(token, "opennms-alec-plugin");
        request.setFeatures(Arrays.asList(" alec-opennms-standalone ", "alec-opennms-standalone", null, ""));

        final PluginActionResult result = service.install(request, admin(), null);

        assertEquals("# Managed by the Plugin Management page; remove together with deploy/opennms-alec-plugin.kar\nalec-opennms-standalone wait-for-kar=opennms-alec-plugin\n",
                new String(Files.readAllBytes(bootDir.resolve("opennms-alec-plugin.boot")), StandardCharsets.UTF_8));
        assertEquals(Arrays.asList("alec-opennms-standalone"), result.getPlugin().getFeatures());
        assertEquals(Arrays.asList("alec-opennms-standalone"), service.status(admin()).getPlugins().get(0).getFeatures());
    }

    @Test
    public void installRejectsFeaturesTheKarDoesNotOfferAtTheTopLevel() throws IOException {
        final String token = storeUpload("opennms-alec-plugin.kar", true, MULTI_FEATURES_XML);
        for (final String feature : new String[] { "alec-api", "nope" }) {
            final InstallRequest request = request(token, "opennms-alec-plugin");
            request.setFeatures(Arrays.asList("alec-opennms-standalone", feature));
            final WebApplicationException e = failure(() -> service.install(request, admin(), null));
            assertEquals(feature, 400, e.getResponse().getStatus());
            assertEquals("'" + feature + "' is not a top-level feature of this KAR; choose from: alec-opennms-standalone, alec-opennms-distributed, alec-sentinel-distributed", e.getResponse().getEntity());
        }
        assertFalse(Files.exists(deployDir.resolve("opennms-alec-plugin.kar")));
        assertFalse(Files.exists(bootDir.resolve("opennms-alec-plugin.boot")));
    }

    @Test
    public void installWithoutAChoiceNeedsASuggestion() throws IOException {
        final String token = storeUpload("opennms-alec-plugin.kar", true, MULTI_FEATURES_XML);

        final WebApplicationException e = failure(() -> service.install(request(token, "opennms-alec-plugin"), admin(), null));

        assertEquals(400, e.getResponse().getStatus());
        assertEquals(PluginManagementRestService.CHOOSE_FEATURES, e.getResponse().getEntity());
        assertFalse(Files.exists(deployDir.resolve("opennms-alec-plugin.kar")));
        assertTrue(service.status(admin()).getPlugins().isEmpty());
    }

    @Test
    public void fetchFromTheCatalogSuggestsItsBootFeaturesAndInstallDefaultsToThem() throws IOException {
        final byte[] kar = karBytes(true, MULTI_FEATURES_XML);
        final String url = "https://github.com/OpenNMS-Plugins/alec/releases/download/v3.0.5/opennms-alec-plugin.kar";
        fetcher.on("https://api.github.com/repos/OpenNMS-Plugins/alec/releases", () -> CannedFetcher.json(200,
                "[{\"tag_name\":\"v3.0.5\",\"assets\":[{\"name\":\"opennms-alec-plugin.kar\",\"size\":" + kar.length + ",\"browser_download_url\":\"" + url + "\"}]}]", Map.of()));
        fetcher.on(url, () -> CannedFetcher.bytes(200, kar, Map.of("Content-Length", String.valueOf(kar.length))));
        final FetchRequest fetch = new FetchRequest();
        fetch.setCatalogId("alec");
        fetch.setTag("v3.0.5");
        fetch.setAssetName("opennms-alec-plugin.kar");

        final KarInspection inspection = service.fetch(fetch, admin(), null);

        assertEquals(Arrays.asList("alec-opennms-standalone"), inspection.getSuggestedFeatures());
        assertEquals(Arrays.asList("alec-opennms-standalone", "alec-opennms-distributed", "alec-sentinel-distributed"), inspection.topLevelFeatureNames());
        assertTrue(inspection.getChecks().stream().noneMatch(c -> FeatureSelection.CHECK_BOOT_FEATURES.equals(c.getId())));
        assertEquals("Everything on the core", inspection.getFeatures().get(1).getDescription());
        assertFalse(inspection.getFeatures().get(0).isTopLevel());
        assertTrue(inspection.getFeatures().get(1).isTopLevel());

        final PluginActionResult result = service.install(request(inspection.getUploadToken(), "opennms-alec-plugin"), admin(), null);

        assertEquals(Arrays.asList("alec-opennms-standalone"), result.getPlugin().getFeatures());
        assertEquals("# Managed by the Plugin Management page; remove together with deploy/opennms-alec-plugin.kar\nalec-opennms-standalone wait-for-kar=opennms-alec-plugin\n",
                new String(Files.readAllBytes(bootDir.resolve("opennms-alec-plugin.boot")), StandardCharsets.UTF_8));
    }

    @Test
    public void uploadedKarSuggestsTheFeatureNamedAfterIt() throws IOException {
        final String token = storeUpload("alec-opennms-standalone-3.0.5.kar", true, MULTI_FEATURES_XML);

        final PluginActionResult result = service.install(request(token, "alec-opennms-standalone"), admin(), null);

        assertEquals(Arrays.asList("alec-opennms-standalone"), result.getPlugin().getFeatures());
    }

    @Test
    public void auditDropsCredentialsFromUrls() {
        assertEquals("url=https://host/a.kar?x=1 reason=y", PluginManagementRestService.redact("url=https://user:p%40ss@host/a.kar?x=1 reason=y"));
        assertEquals("http://h/ and HTTPS://h2/", PluginManagementRestService.redact("http://u@h/ and HTTPS://u:p@h2/"));
        assertEquals("url=https://host/a@b", PluginManagementRestService.redact("url=https://host/a@b"));
        assertNull(PluginManagementRestService.redact(null));
    }

    @Test
    public void installOfAnUploadEvictedMidwayIs404AndLeavesNothingBehind() throws IOException {
        final String token = storeUpload("alec-1.0.kar", true);
        bridge.beforeExports = () -> {
            try {
                Files.delete(uploadDir.resolve(token + ".kar"));
            } catch (final IOException e) {
                throw new AssertionError(e);
            }
        };

        final WebApplicationException e = failure(() -> service.install(request(token, "alec"), admin(), null));

        assertEquals(404, e.getResponse().getStatus());
        assertEquals("The upload token is unknown or has expired; run the check again.", e.getResponse().getEntity());
        assertFalse(Files.exists(bootDir.resolve("alec.boot")));
        assertFalse(Files.exists(deployDir.resolve("alec.kar")));
        assertFalse(Files.exists(deployDir.resolve(".alec.kar.part")));
        bridge.beforeExports = null;
        assertTrue(service.status(admin()).getPlugins().stream().noneMatch(PluginEntry::isManaged));
        assertEquals(404, failure(() -> service.install(request(token, "alec"), admin(), null)).getResponse().getStatus());
    }

    @Test
    public void statusReportsTheTemporaryArea() throws IOException {
        final String token = storeUpload("alec-1.0.kar", true);

        final PluginManagementStatus status = service.status(admin());

        assertEquals(uploadDir.toString(), status.getTempDir());
        assertEquals(2, status.getTempFiles());
        assertEquals(Files.size(uploadDir.resolve(token + ".kar")) + "alec-1.0.kar".length(), status.getTempBytes());
    }

    @Test
    public void fetchStoresTheAssetLikeAnUploadAndInstallRecordsTheSource() throws IOException {
        final byte[] kar = karBytes(true);
        final String url = "https://github.com/OpenNMS-Plugins/alec/releases/download/v1.0.0/opennms-alec-plugin.kar";
        fetcher.on("https://api.github.com/repos/OpenNMS-Plugins/alec/releases", () -> CannedFetcher.json(200,
                "[{\"tag_name\":\"v1.0.0\",\"name\":\"1.0.0\",\"published_at\":\"2026-08-01T00:00:00Z\",\"prerelease\":false,\"body\":\"n\","
                + "\"assets\":[{\"name\":\"opennms-alec-plugin.kar\",\"size\":" + kar.length + ",\"browser_download_url\":\"" + url + "\"}]}]", Map.of()));
        fetcher.on(url, () -> CannedFetcher.bytes(200, kar, Map.of("Content-Length", String.valueOf(kar.length))));
        final FetchRequest fetch = new FetchRequest();
        fetch.setCatalogId("alec");
        fetch.setTag("v1.0.0");
        fetch.setAssetName("opennms-alec-plugin.kar");

        final KarInspection inspection = service.fetch(fetch, admin(), null);

        final String token = inspection.getUploadToken();
        assertTrue(token, PluginManagementRestService.UPLOAD_TOKEN.matcher(token).matches());
        assertEquals(inspection.getSha256(), token);
        assertEquals("opennms-alec-plugin.kar", inspection.getFileName());
        assertEquals("opennms-alec-plugin", inspection.getKarName());
        assertEquals(kar.length, inspection.getSize());
        assertEquals("OpenNMS-Plugins/alec", inspection.getSource().getRepository());
        assertEquals("v1.0.0", inspection.getSource().getTag());
        assertEquals("opennms-alec-plugin.kar", inspection.getSource().getAssetName());
        assertEquals(url, inspection.getSource().getUrl());
        assertTrue(inspection.checksAt(KarInspection.Level.FAIL).isEmpty());
        // the catalog expects alec-opennms-standalone; this KAR only has alec, so the page warns and falls back to the single top-level feature
        final KarInspection.Check bootFeatures = inspection.getChecks().stream().filter(c -> FeatureSelection.CHECK_BOOT_FEATURES.equals(c.getId())).findFirst().orElseThrow();
        assertEquals(KarInspection.Level.WARN, bootFeatures.getLevel());
        assertTrue(bootFeatures.getMessage(), bootFeatures.getMessage().contains("alec-opennms-standalone"));
        assertEquals(Arrays.asList("alec"), inspection.getSuggestedFeatures());
        assertEquals(token, KarInspector.sha256(uploadDir.resolve(token + ".kar")));
        assertEquals("opennms-alec-plugin.kar", new String(Files.readAllBytes(uploadDir.resolve(token + ".name")), StandardCharsets.UTF_8));
        assertEquals("github:OpenNMS-Plugins/alec@v1.0.0", new String(Files.readAllBytes(uploadDir.resolve(token + ".source")), StandardCharsets.UTF_8));
        assertEquals(2, fetcher.requests.size());

        final PluginActionResult result = service.install(request(token, "alec"), admin(), null);

        assertEquals("github:OpenNMS-Plugins/alec@v1.0.0", result.getPlugin().getSource());
        assertEquals("opennms-alec-plugin.kar", result.getPlugin().getFileName());
        assertTrue(Files.isRegularFile(deployDir.resolve("alec.kar")));
        assertEquals("github:OpenNMS-Plugins/alec@v1.0.0", service.status(admin()).getPlugins().get(0).getSource());

        final String uploadToken = storeUpload("other.kar", false);
        assertEquals("upload", service.install(request(uploadToken, "other"), admin(), null).getPlugin().getSource());
    }

    @Test
    public void fetchOfAnUnknownTagOrAssetIs404() {
        fetcher.on("https://api.github.com/repos/OpenNMS-Plugins/alec/releases", () -> CannedFetcher.json(200,
                "[{\"tag_name\":\"v1.0.0\",\"assets\":[{\"name\":\"a.kar\",\"size\":1,\"browser_download_url\":\"https://github.com/a.kar\"}]}]", Map.of()));
        final FetchRequest fetch = new FetchRequest();
        fetch.setRepository("OpenNMS-Plugins/alec");
        fetch.setTag("v2.0.0");
        fetch.setAssetName("a.kar");

        assertEquals(404, failure(() -> service.fetch(fetch, admin(), null)).getResponse().getStatus());
        fetch.setTag("v1.0.0");
        fetch.setAssetName("b.kar");
        assertEquals(404, failure(() -> service.fetch(fetch, admin(), null)).getResponse().getStatus());
        fetch.setCatalogId("nope");
        assertEquals(404, failure(() -> service.fetch(fetch, admin(), null)).getResponse().getStatus());
        assertEquals(404, failure(() -> service.catalogReleases("nope", admin(), null)).getResponse().getStatus());
    }

    @Test
    public void fetchRejectsIncompleteRequestsBeforeTouchingTheNetwork() {
        final FetchRequest fetch = new FetchRequest();
        assertEquals(400, failure(() -> service.fetch(fetch, admin(), null)).getResponse().getStatus());
        fetch.setTag("v1");
        fetch.setAssetName("a.kar");
        assertEquals(400, failure(() -> service.fetch(fetch, admin(), null)).getResponse().getStatus());
        fetch.setRepository("not-a-repo");
        assertEquals(400, failure(() -> service.fetch(fetch, admin(), null)).getResponse().getStatus());
        assertEquals(400, failure(() -> service.releases("not-a-repo", admin(), null)).getResponse().getStatus());
        assertEquals(400, failure(() -> service.fetch(null, admin(), null)).getResponse().getStatus());
        assertTrue(fetcher.requests.isEmpty());
    }

    @Test
    public void offlineServerAnswersWithAPlainExplanation() {
        fetcher.failing("https://api.github.com/", new UnknownHostException("api.github.com"));
        final FetchRequest fetch = new FetchRequest();
        fetch.setCatalogId("alec");
        fetch.setTag("v1.0.0");
        fetch.setAssetName("a.kar");

        final WebApplicationException e = failure(() -> service.fetch(fetch, admin(), null));

        assertEquals(502, e.getResponse().getStatus());
        assertEquals("text/plain", e.getResponse().getMediaType().toString());
        assertEquals("Could not reach api.github.com from this server: the name api.github.com cannot be resolved. "
                + "Check the server's network access or proxy settings, or load the plugin from a file instead.", e.getResponse().getEntity());
        assertEquals(502, failure(() -> service.catalogReleases("alec", admin(), null)).getResponse().getStatus());
        assertEquals(502, failure(() -> service.releases("OpenNMS-Plugins/alec", admin(), null)).getResponse().getStatus());
        assertEquals(3, service.catalog(admin(), null).getEntries().size());
    }

    @Test
    public void fetchedFileThatIsNotAKarStillReturnsItsChecks() {
        final byte[] junk = "not a zip".getBytes(StandardCharsets.UTF_8);
        final String url = "https://objects.githubusercontent.com/junk.kar";
        fetcher.on("https://api.github.com/repos/me/mine/releases", () -> CannedFetcher.json(200,
                "[{\"tag_name\":\"v1\",\"assets\":[{\"name\":\"junk.kar\",\"size\":" + junk.length + ",\"browser_download_url\":\"" + url + "\"}]}]", Map.of()));
        fetcher.on(url, () -> CannedFetcher.bytes(200, junk, Map.of()));
        final FetchRequest fetch = new FetchRequest();
        fetch.setRepository("me/mine");
        fetch.setTag("v1");
        fetch.setAssetName("junk.kar");

        final KarInspection inspection = service.fetch(fetch, admin(), null);

        assertFalse(inspection.checksAt(KarInspection.Level.FAIL).isEmpty());
        assertEquals(409, failure(() -> service.install(request(inspection.getUploadToken(), "junk"), admin(), null)).getResponse().getStatus());
    }

    @Test
    public void boundedCopyStopsPastTheLimit() throws IOException {
        final byte[] data = new byte[1000];
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertTrue(PluginManagementRestService.copyBounded(new ByteArrayInputStream(data), out, 1000));
        assertEquals(1000, out.size());
        assertFalse(PluginManagementRestService.copyBounded(new ByteArrayInputStream(data), new ByteArrayOutputStream(), 999));
    }

    // --- fixtures --------------------------------------------------------------

    private static byte[] karBytes(final boolean autoStart) throws IOException {
        return karBytes(autoStart, FEATURES_XML);
    }

    private static byte[] karBytes(final boolean autoStart, final String featuresXml) throws IOException {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Karaf-Feature-Start", String.valueOf(autoStart));
        final ByteArrayOutputStream manifestBytes = new ByteArrayOutputStream();
        manifest.write(manifestBytes);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            zip.write(manifestBytes.toByteArray());
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("repository/org/example/alec/1.0.0/alec-1.0.0-features.xml"));
            zip.write(featuresXml.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return out.toByteArray();
    }

    private String storeUpload(final String fileName, final boolean autoStart) throws IOException {
        return storeUpload(fileName, autoStart, FEATURES_XML);
    }

    private String storeUpload(final String fileName, final boolean autoStart, final String featuresXml) throws IOException {
        final Path kar = folder.getRoot().toPath().resolve("upload-" + fileName);
        Files.write(kar, karBytes(autoStart, featuresXml));
        final String token = KarInspector.sha256(kar);
        Files.createDirectories(uploadDir);
        Files.move(kar, uploadDir.resolve(token + ".kar"));
        Files.write(uploadDir.resolve(token + ".name"), fileName.getBytes(StandardCharsets.UTF_8));
        return token;
    }

    private static WebApplicationException failure(final Runnable call) {
        try {
            call.run();
        } catch (final WebApplicationException e) {
            return e;
        }
        fail("expected a WebApplicationException");
        return null;
    }

    private static InstallRequest request(final String token, final String karName) {
        final InstallRequest request = new InstallRequest();
        request.setUploadToken(token);
        request.setKarName(karName);
        request.setAcknowledgeWarnings(true);
        return request;
    }

    private static SecurityContext admin() {
        return new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> "admin";
            }

            @Override
            public boolean isUserInRole(final String role) {
                return Authentication.ROLE_ADMIN.equals(role);
            }

            @Override
            public boolean isSecure() {
                return true;
            }

            @Override
            public String getAuthenticationScheme() {
                return BASIC_AUTH;
            }
        };
    }
}
