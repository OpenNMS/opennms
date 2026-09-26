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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.plugins.KarInspection.Check;
import org.opennms.web.rest.v2.plugins.KarInspection.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Admin-only management of plugin KARs: fetch one from its repository or check
 * an upload, stage it into the deploy directory, list what is deployed and
 * unload a plugin. Every action is written to the plugin-management log.
 */
@Component
@Path("plugin-management")
@Tag(name = "PluginManagement", description = "Fetch, upload, check, stage and unload plugin KARs")
public class PluginManagementRestService {
    private static final Logger LOG = LoggerFactory.getLogger(PluginManagementRestService.class);
    static final String AUDIT_LOGGER = "org.opennms.web.rest.v2.plugins.audit";
    private static final Logger AUDIT = LoggerFactory.getLogger(AUDIT_LOGGER);

    // Karaf's extender skips dot-files, so a name may not start with one.
    static final Pattern KAR_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");
    static final Pattern UPLOAD_TOKEN = Pattern.compile("[0-9a-f]{64}");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}]");
    private static final Pattern URL_USERINFO = Pattern.compile("(?i)(https?://)[^/\\s@]*@");
    static final String BOOT_SUFFIX = ".boot";
    static final String WAIT_FOR_KAR = PluginRegistry.WAIT_FOR_KAR;
    static final String CHOOSE_FEATURES = "Choose at least one feature to start";
    static final String TOO_LARGE = "The file is larger than " + (KarInspector.MAX_SIZE_BYTES / (1024 * 1024)) + " MB";

    private final java.nio.file.Path opennmsHome;
    private final java.nio.file.Path deployDir;
    private final java.nio.file.Path bootDir;
    private final KarafBridge bridge;
    private final PluginRegistry registry;
    private final KarInspector inspector;
    private final CompatibilityChecker checker;
    private final TempArea tempArea;
    private final PluginCatalog catalog;
    private final HttpFetcher fetcher;
    private final GitHubReleases releases;
    private final KarDownloader downloader;

    public PluginManagementRestService() {
        this(Paths.get(System.getProperty("opennms.home", ".")));
    }

    private PluginManagementRestService(final java.nio.file.Path opennmsHome) {
        this(opennmsHome, new OsgiKarafBridge(opennmsHome.resolve("etc")));
    }

    PluginManagementRestService(final java.nio.file.Path opennmsHome, final KarafBridge bridge) {
        this(opennmsHome, bridge, new LazyFetcher(), Clock.systemUTC());
    }

    PluginManagementRestService(final java.nio.file.Path opennmsHome, final KarafBridge bridge, final HttpFetcher fetcher, final Clock clock) {
        this.opennmsHome = opennmsHome;
        this.deployDir = opennmsHome.resolve("deploy");
        this.bootDir = opennmsHome.resolve("etc").resolve("featuresBoot.d");
        this.bridge = bridge;
        this.registry = new PluginRegistry(opennmsHome, bridge);
        this.inspector = new KarInspector(deployDir);
        this.checker = new CompatibilityChecker(bridge);
        this.tempArea = new TempArea(opennmsHome.resolve("data").resolve("tmp").resolve("plugin-management"), clock, registry::deployedInstalls, TempArea.CAP_BYTES);
        this.catalog = new PluginCatalog(opennmsHome.resolve("etc").resolve(PluginCatalog.FILE_NAME));
        this.fetcher = fetcher;
        this.releases = new GitHubReleases(fetcher, clock, () -> System.getProperty(GitHubReleases.TOKEN_PROPERTY));
        this.downloader = new KarDownloader(fetcher, tempArea);
        tempArea.ensureScheduled();
    }

    @PreDestroy
    public void shutdown() {
        tempArea.shutdown();
        try {
            fetcher.close();
        } catch (final IOException e) {
            LOG.warn("Cannot close the HTTP client: {}", e.toString());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PluginManagementStatus status(@Context final SecurityContext securityContext) {
        requireAdmin(securityContext);
        final PluginManagementStatus status = new PluginManagementStatus();
        status.setOpennmsHome(opennmsHome.toString());
        status.setDeployDir(deployDir.toString());
        status.setContainerAvailable(safely(bridge::isAvailable, false));
        status.setJavaVersion(safely(bridge::javaVersion, null));
        status.setOiaVersion(safely(bridge::oiaVersion, null));
        final List<InstalledFeature> pluginFeatures = safely(bridge::pluginFeatures, Collections.emptyList());
        status.setPluginFeatures(pluginFeatures);
        status.setRestartInstructions(RestartInstructions.current());
        final TempArea.Usage usage = tempArea.usage();
        status.setTempDir(tempArea.getDir().toString());
        status.setTempBytes(usage.getBytes());
        status.setTempFiles(usage.getFiles());
        try {
            final List<PluginEntry> plugins = registry.list();
            status.setPlugins(plugins);
            status.setRestartRequired(plugins.stream().anyMatch(PluginEntry::isPendingRestart));
        } catch (final IOException e) {
            throw serverError("Cannot read " + registry.getRegistryFile() + ": " + e.getMessage());
        }
        return status;
    }

    @GET
    @Path("restart-instructions")
    @Produces(MediaType.APPLICATION_JSON)
    public RestartInstructions restartInstructions(@Context final SecurityContext securityContext) {
        requireAdmin(securityContext);
        return RestartInstructions.current();
    }

    // --- repository -----------------------------------------------------------

    @GET
    @Path("catalog")
    @Produces(MediaType.APPLICATION_JSON)
    public PluginCatalogResponse catalog(@Context final SecurityContext securityContext,
                                         @Context final HttpServletRequest request) {
        requireAdmin(securityContext);
        final PluginCatalogResponse response = new PluginCatalogResponse();
        response.setEntries(catalog.entries());
        response.setCustomAllowed(true);
        audit("catalog", user(securityContext), remote(request), null, null, "ok", "entries=" + response.getEntries().size() + " file=" + catalog.getFile());
        return response;
    }

    @GET
    @Path("catalog/{id}/releases")
    @Produces(MediaType.APPLICATION_JSON)
    public GitHubReleases.Result catalogReleases(@PathParam("id") final String id,
                                                 @Context final SecurityContext securityContext,
                                                 @Context final HttpServletRequest request) {
        requireAdmin(securityContext);
        final String user = user(securityContext);
        final String remote = remote(request);
        final PluginCatalog.Entry entry = catalog.find(id).orElse(null);
        if (entry == null) {
            audit("releases", user, remote, null, null, "rejected", "unknown catalog entry " + id);
            throw notFound("No catalog entry with id '" + clean(id) + "'.");
        }
        return lookupReleases(entry.getRepository(), entry.assetPattern(), user, remote);
    }

    @GET
    @Path("releases")
    @Produces(MediaType.APPLICATION_JSON)
    public GitHubReleases.Result releases(@QueryParam("repository") final String repository,
                                          @Context final SecurityContext securityContext,
                                          @Context final HttpServletRequest request) {
        requireAdmin(securityContext);
        final String user = user(securityContext);
        final String remote = remote(request);
        if (!GitHubReleases.isValidRepository(repository)) {
            audit("releases", user, remote, null, null, "rejected", "malformed repository " + repository);
            throw badRequest("The repository must be given as owner/name.");
        }
        return lookupReleases(repository, Pattern.compile(PluginCatalog.DEFAULT_ASSET_PATTERN), user, remote);
    }

    private GitHubReleases.Result lookupReleases(final String repository, final Pattern assetPattern, final String user, final String remote) {
        try {
            final GitHubReleases.Result result = releases.releases(repository, assetPattern);
            audit("releases", user, remote, null, null, "ok", "repository=" + repository + " releases=" + result.getReleases().size() + " cached=" + result.isCached());
            return result;
        } catch (final PluginSourceException e) {
            audit("releases", user, remote, null, null, "error", "repository=" + repository + " reason=" + e.getMessage());
            throw sourceError(e);
        }
    }

    @POST
    @Path("fetch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public KarInspection fetch(final FetchRequest fetchRequest,
                               @Context final SecurityContext securityContext,
                               @Context final HttpServletRequest request) {
        requireAdmin(securityContext);
        final String user = user(securityContext);
        final String remote = remote(request);
        if (fetchRequest == null || isBlank(fetchRequest.getTag()) || isBlank(fetchRequest.getAssetName())) {
            audit("fetch", user, remote, null, null, "rejected", "tag and assetName are required");
            throw badRequest("A release tag and an asset name are required.");
        }
        final String repository;
        final Pattern assetPattern;
        final List<String> bootFeatures;
        if (!isBlank(fetchRequest.getCatalogId())) {
            final PluginCatalog.Entry entry = catalog.find(fetchRequest.getCatalogId()).orElse(null);
            if (entry == null) {
                audit("fetch", user, remote, null, null, "rejected", "unknown catalog entry " + fetchRequest.getCatalogId());
                throw notFound("No catalog entry with id '" + clean(fetchRequest.getCatalogId()) + "'.");
            }
            repository = entry.getRepository();
            assetPattern = entry.assetPattern();
            bootFeatures = entry.getBootFeatures();
        } else if (GitHubReleases.isValidRepository(fetchRequest.getRepository())) {
            repository = fetchRequest.getRepository();
            assetPattern = Pattern.compile(PluginCatalog.DEFAULT_ASSET_PATTERN);
            bootFeatures = catalog.bootFeaturesFor(repository);
        } else {
            audit("fetch", user, remote, null, null, "rejected", "neither a catalog id nor an owner/name repository");
            throw badRequest("Either a catalogId or a repository given as owner/name is required.");
        }
        final String tag = fetchRequest.getTag().trim();
        final String assetName = fetchRequest.getAssetName().trim();
        final String where = "repository=" + repository + " tag=" + tag + " asset=" + assetName;

        final GitHubReleases.Asset asset;
        try {
            final GitHubReleases.Result result = releases.releases(repository, assetPattern);
            final GitHubReleases.Release release = result.getReleases().stream().filter(r -> tag.equals(r.getTag())).findFirst().orElse(null);
            if (release == null) {
                audit("fetch", user, remote, null, null, "rejected", where + " reason=release not found");
                throw notFound("Release " + clean(tag) + " of " + repository + " was not found, or it carries no plugin file.");
            }
            asset = release.getAssets().stream().filter(a -> assetName.equals(a.getName())).findFirst().orElse(null);
            if (asset == null) {
                audit("fetch", user, remote, null, null, "rejected", where + " reason=asset not found");
                throw notFound("Release " + clean(tag) + " of " + repository + " has no plugin file named " + clean(assetName) + ".");
            }
        } catch (final PluginSourceException e) {
            audit("fetch", user, remote, null, null, "error", where + " reason=" + e.getMessage());
            throw sourceError(e);
        }

        final URI url;
        try {
            url = new URI(asset.getUrl());
        } catch (final Exception e) {
            audit("fetch", user, remote, null, null, "error", where + " url=" + asset.getUrl() + " reason=unparseable asset url");
            throw sourceError(new PluginSourceException(PluginSourceException.BAD_GATEWAY, "GitHub returned an unusable download link for " + clean(assetName) + "."));
        }
        final KarDownloader.Downloaded downloaded;
        try {
            downloaded = downloader.download(url, assetName, asset.getSize());
        } catch (final PluginSourceException e) {
            audit("fetch", user, remote, null, null, "error", where + " url=" + url + " reason=" + e.getMessage());
            throw sourceError(e);
        }
        final KarInspection.Source source = new KarInspection.Source(repository, tag, assetName, url.toString());
        try {
            final KarInspection inspection = inspector.inspect(downloaded.getPart(), assetName);
            checker.check(inspection);
            FeatureSelection.suggest(inspection, bootFeatures);
            tempArea.commit(downloaded.getPart(), inspection.getSha256(), assetName, source.toRegistryString());
            inspection.setUploadToken(inspection.getSha256());
            inspection.setSource(source);
            audit("fetch", user, remote, inspection.getKarName(), inspection.getSha256(), "ok",
                    where + " url=" + url + " size=" + downloaded.getSize() + " fails=" + inspection.checksAt(Level.FAIL).size() + " warns=" + inspection.checksAt(Level.WARN).size()
                    + " suggestedFeatures=" + String.join(",", inspection.getSuggestedFeatures()));
            return inspection;
        } catch (final IOException e) {
            tempArea.abort(downloaded.getPart());
            audit("fetch", user, remote, null, downloaded.getSha256(), "error", where + " url=" + url + " reason=" + e);
            throw sourceError(new PluginSourceException(PluginSourceException.INSUFFICIENT_STORAGE, "Cannot store the downloaded file: " + e.getMessage()));
        }
    }

    // --- upload ---------------------------------------------------------------

    @POST
    @Path("check")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public KarInspection check(@Multipart("upload") final Attachment upload,
                               @Context final SecurityContext securityContext,
                               @Context final HttpServletRequest request) {
        requireAdmin(securityContext);
        final String user = user(securityContext);
        final String remote = remote(request);
        final long contentLength = request == null ? -1 : request.getContentLengthLong();
        if (contentLength > KarInspector.MAX_SIZE_BYTES) {
            audit("check", user, remote, null, null, "rejected", "content length " + contentLength + " exceeds the limit");
            throw badRequest(TOO_LARGE);
        }
        if (upload == null) {
            audit("check", user, remote, null, null, "rejected", "no upload part");
            throw badRequest("A multipart part named 'upload' carrying the KAR is required.");
        }
        final String fileName = originalFileName(upload);
        java.nio.file.Path staging = null;
        try {
            staging = tempArea.newPartFile("upload-");
            try {
                tempArea.ensureSpace(staging, contentLength);
            } catch (final PluginSourceException e) {
                audit("check", user, remote, null, null, "error", "file=" + fileName + " reason=" + e.getMessage());
                throw sourceError(e);
            }
            try (InputStream in = upload.getObject(InputStream.class); OutputStream out = Files.newOutputStream(staging)) {
                if (!copyBounded(in, out, KarInspector.MAX_SIZE_BYTES)) {
                    audit("check", user, remote, null, null, "rejected", "upload exceeds the limit; file=" + fileName);
                    throw badRequest(TOO_LARGE);
                }
            }
            final KarInspection inspection = inspector.inspect(staging, fileName);
            checker.check(inspection);
            FeatureSelection.suggest(inspection, Collections.emptyList());
            tempArea.commit(staging, inspection.getSha256(), fileName, PluginRegistry.SOURCE_UPLOAD);
            staging = null;
            inspection.setUploadToken(inspection.getSha256());
            audit("check", user, remote, inspection.getKarName(), inspection.getSha256(), "ok",
                    "file=" + fileName + " fails=" + inspection.checksAt(Level.FAIL).size() + " warns=" + inspection.checksAt(Level.WARN).size());
            return inspection;
        } catch (final IOException e) {
            audit("check", user, remote, null, null, "error", "file=" + fileName + " reason=" + e);
            throw serverError("Cannot store the upload: " + e.getMessage());
        } finally {
            if (staging != null) {
                tempArea.abort(staging);
            }
        }
    }

    @POST
    @Path("install")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PluginActionResult install(final InstallRequest installRequest,
                                      @Context final SecurityContext securityContext,
                                      @Context final HttpServletRequest request) {
        requireAdmin(securityContext);
        final String user = user(securityContext);
        final String remote = remote(request);
        if (installRequest == null || installRequest.getUploadToken() == null || !UPLOAD_TOKEN.matcher(installRequest.getUploadToken()).matches()) {
            audit("install", user, remote, null, null, "rejected", "missing or malformed upload token");
            throw badRequest("An uploadToken from a previous check is required.");
        }
        final String token = installRequest.getUploadToken();
        final java.nio.file.Path stored = tempArea.karFile(token);
        if (!Files.isRegularFile(stored)) {
            audit("install", user, remote, null, token, "rejected", "unknown or expired upload token");
            throw expiredToken();
        }
        final String fileName = tempArea.fileName(token).orElse(token + PluginRegistry.KAR_SUFFIX);
        final String source = tempArea.source(token).orElse(PluginRegistry.SOURCE_UPLOAD);
        final String karName;
        if (installRequest.getKarName() != null && !installRequest.getKarName().isEmpty()) {
            if (!KAR_NAME.matcher(installRequest.getKarName()).matches()) {
                audit("install", user, remote, null, token, "rejected", "invalid kar name");
                throw badRequest("The KAR name must start with a letter or digit and may only contain letters, digits, '.', '_' and '-'.");
            }
            karName = installRequest.getKarName();
        } else {
            karName = KarInspector.sanitizeKarName(fileName);
        }

        final KarInspection inspection;
        final Optional<PluginRegistry.Record> previous;
        try {
            inspection = inspector.inspect(stored, fileName, karName);
            previous = registry.record(karName);
        } catch (final IOException e) {
            if (vanished(e)) {
                audit("install", user, remote, karName, token, "rejected", "upload expired during install");
                throw expiredToken();
            }
            audit("install", user, remote, karName, token, "error", e.toString());
            throw serverError("Cannot read the stored upload: " + e.getMessage());
        }
        final java.nio.file.Path target = deployDir.resolve(karName + PluginRegistry.KAR_SUFFIX);
        if (previous.isPresent() && !previous.get().isUnloaded() && inspection.getSha256().equals(previous.get().getSha256()) && Files.exists(target)) {
            audit("install", user, remote, karName, inspection.getSha256(), "rejected", "already loaded");
            throw conflict("Plugin '" + karName + "' with this checksum is already loaded.");
        }
        checker.check(inspection);
        FeatureSelection.suggest(inspection, catalog.bootFeaturesFor(repositoryOf(source)));
        final List<Check> fails = inspection.checksAt(Level.FAIL);
        if (!fails.isEmpty()) {
            audit("install", user, remote, karName, token, "refused", "failed checks: " + describe(fails));
            throw conflict("The plugin failed these checks: " + describe(fails));
        }
        final List<Check> warns = inspection.checksAt(Level.WARN);
        if (!warns.isEmpty() && !installRequest.isAcknowledgeWarnings()) {
            audit("install", user, remote, karName, token, "refused", "unacknowledged warnings: " + describe(warns));
            throw conflict("These warnings must be acknowledged before installing: " + describe(warns));
        }

        final List<String> topLevel = inspection.topLevelFeatureNames();
        final List<String> features = new ArrayList<>();
        for (final String requested : installRequest.getFeatures()) {
            if (requested == null || requested.isBlank()) {
                continue;
            }
            final String name = requested.trim();
            if (!topLevel.contains(name)) {
                audit("install", user, remote, karName, token, "rejected", "feature " + name + " is not a top-level feature of the KAR");
                throw badRequest("'" + clean(name) + "' is not a top-level feature of this KAR; choose from: " + String.join(", ", topLevel));
            }
            if (!features.contains(name)) {
                features.add(name);
            }
        }
        if (features.isEmpty()) {
            features.addAll(inspection.getSuggestedFeatures());
        }
        if (features.isEmpty()) {
            audit("install", user, remote, karName, token, "rejected", "no feature chosen; top-level features: " + String.join(",", topLevel));
            throw badRequest(CHOOSE_FEATURES);
        }
        final boolean autoStart = inspection.getFeatureStart() == null || !"false".equalsIgnoreCase(inspection.getFeatureStart().trim());
        final java.nio.file.Path bootFile = bootDir.resolve(karName + BOOT_SUFFIX);
        // The .part name keeps Felix FileInstall from picking the file up before the move completes.
        final java.nio.file.Path part = deployDir.resolve("." + karName + PluginRegistry.KAR_SUFFIX + ".part");
        boolean bootWritten = false;
        boolean recorded = false;
        try {
            Files.createDirectories(deployDir);
            Files.createDirectories(bootDir);
            // Boot file and record first: once the KAR lands in deploy/ Karaf may start it within seconds.
            writeBootFile(bootFile, karName, features);
            bootWritten = true;
            final PluginEntry recordedEntry = registry.recordInstall(karName, fileName, inspection.getSha256(), inspection.getSize(), source, user, features, opennmsHome.relativize(bootFile).toString(), autoStart);
            recorded = true;
            Files.copy(stored, part, StandardCopyOption.REPLACE_EXISTING);
            atomicMove(part, target);
            audit("install", user, remote, karName, inspection.getSha256(), "ok", "source=" + source + " features=" + String.join(",", features) + " autoStart=" + autoStart + " warnsAcknowledged=" + warns.size());
            final PluginActionResult result = new PluginActionResult();
            result.setPlugin(registry.find(karName).orElse(recordedEntry));
            result.setRestartRequired(!autoStart);
            result.setRestartInstructions(RestartInstructions.current());
            result.setChecks(inspection.getChecks());
            return result;
        } catch (final IOException e) {
            quietly(() -> Files.deleteIfExists(part), part);
            if (bootWritten) {
                quietly(() -> Files.deleteIfExists(bootFile), bootFile);
            }
            if (recorded) {
                quietly(() -> registry.revertInstall(karName, previous.orElse(null)), registry.getRegistryFile());
            }
            if (vanished(e) && !Files.isRegularFile(stored)) {
                audit("install", user, remote, karName, inspection.getSha256(), "rejected", "upload expired during install");
                throw expiredToken();
            }
            audit("install", user, remote, karName, inspection.getSha256(), "error", e.toString());
            throw serverError("Cannot stage the plugin: " + e.getMessage());
        }
    }

    @DELETE
    @Path("{karName}")
    @Produces(MediaType.APPLICATION_JSON)
    public PluginActionResult unload(@PathParam("karName") final String karName,
                                     @Context final SecurityContext securityContext,
                                     @Context final HttpServletRequest request) {
        requireAdmin(securityContext);
        final String user = user(securityContext);
        final String remote = remote(request);
        if (karName == null || !KAR_NAME.matcher(karName).matches()) {
            audit("unload", user, remote, null, null, "rejected", "invalid kar name");
            throw badRequest("The KAR name must start with a letter or digit and may only contain letters, digits, '.', '_' and '-'.");
        }
        final java.nio.file.Path kar = deployDir.resolve(karName + PluginRegistry.KAR_SUFFIX);
        final java.nio.file.Path bootFile = bootDir.resolve(karName + BOOT_SUFFIX);
        try {
            final Optional<PluginEntry> existing = registry.find(karName);
            if (existing.isEmpty() && !Files.exists(kar)) {
                audit("unload", user, remote, karName, null, "rejected", "unknown plugin");
                throw notFound("No plugin named '" + karName + "' is deployed or managed.");
            }
            final boolean removedKar = Files.deleteIfExists(kar);
            final List<String> bootFilesRemoved = new ArrayList<>();
            if (Files.deleteIfExists(bootFile)) {
                bootFilesRemoved.add(relativeToHome(bootFile));
            }
            bootFilesRemoved.addAll(removeBootReferences(karName));
            final String fileName = existing.map(PluginEntry::getFileName).orElse(kar.getFileName().toString());
            final PluginEntry entry = registry.recordUnload(karName, user, fileName);
            audit("unload", user, remote, karName, existing.map(PluginEntry::getSha256).orElse(null), "ok",
                    "removedKar=" + removedKar + " bootFilesRemoved=" + String.join(",", bootFilesRemoved));
            final PluginActionResult result = new PluginActionResult();
            result.setPlugin(entry);
            result.setRestartRequired(true);
            result.setRestartInstructions(RestartInstructions.current());
            result.setBootFilesRemoved(bootFilesRemoved);
            return result;
        } catch (final IOException e) {
            audit("unload", user, remote, karName, null, "error", e.toString());
            throw serverError("Cannot unload the plugin: " + e.getMessage());
        }
    }

    // --- helpers ---------------------------------------------------------------

    static void writeBootFile(final java.nio.file.Path bootFile, final String karName, final List<String> features) throws IOException {
        final StringBuilder content = new StringBuilder();
        content.append("# Managed by the Plugin Management page; remove together with deploy/").append(karName).append(PluginRegistry.KAR_SUFFIX).append('\n');
        for (final String feature : features) {
            content.append(feature).append(' ').append(WAIT_FOR_KAR).append(karName).append('\n');
        }
        final java.nio.file.Path tmp = bootFile.resolveSibling(bootFile.getFileName() + ".tmp");
        Files.write(tmp, content.toString().getBytes(StandardCharsets.UTF_8));
        atomicMove(tmp, bootFile);
    }

    /**
     * Drops every line in featuresBoot.d that waits for the KAR, whichever file it
     * lives in, and deletes files that keep no feature line. Returns the touched
     * files relative to OPENNMS_HOME.
     */
    List<String> removeBootReferences(final String karName) throws IOException {
        final List<String> touched = new ArrayList<>();
        if (!Files.isDirectory(bootDir)) {
            return touched;
        }
        final List<java.nio.file.Path> files = new ArrayList<>();
        try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(bootDir)) {
            for (final java.nio.file.Path p : stream) {
                if (Files.isRegularFile(p)) {
                    files.add(p);
                }
            }
        }
        files.sort(null);
        for (final java.nio.file.Path file : files) {
            final List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            final List<String> kept = new ArrayList<>();
            boolean featureLines = false;
            for (final String line : lines) {
                if (waitsForKar(line, karName)) {
                    continue;
                }
                kept.add(line);
                final String trimmed = line.trim();
                featureLines |= !trimmed.isEmpty() && !trimmed.startsWith("#");
            }
            if (kept.size() == lines.size()) {
                continue;
            }
            if (featureLines) {
                final java.nio.file.Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
                Files.write(tmp, (String.join("\n", kept) + "\n").getBytes(StandardCharsets.UTF_8));
                atomicMove(tmp, file);
            } else {
                Files.delete(file);
            }
            touched.add(relativeToHome(file));
        }
        return touched;
    }

    static boolean waitsForKar(final String line, final String karName) {
        return PluginRegistry.waitsForKar(line, karName);
    }

    /** owner/name out of a registry source such as {@code github:owner/name@tag}; null for uploads. */
    static String repositoryOf(final String source) {
        if (source == null || !source.startsWith("github:")) {
            return null;
        }
        final int at = source.indexOf('@');
        return at < 0 ? source.substring("github:".length()) : source.substring("github:".length(), at);
    }

    /** Copies at most {@code limit} bytes; false when the stream holds more. */
    static boolean copyBounded(final InputStream in, final OutputStream out, final long limit) throws IOException {
        final byte[] buffer = new byte[64 * 1024];
        long total = 0;
        int read;
        while ((read = in.read(buffer)) >= 0) {
            total += read;
            if (total > limit) {
                return false;
            }
            out.write(buffer, 0, read);
        }
        return true;
    }

    private static void atomicMove(final java.nio.file.Path from, final java.nio.file.Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (final AtomicMoveNotSupportedException e) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String relativeToHome(final java.nio.file.Path path) {
        try {
            return opennmsHome.relativize(path).toString();
        } catch (final IllegalArgumentException e) {
            return path.toString();
        }
    }

    private interface IoAction {
        void run() throws IOException;
    }

    private static void quietly(final IoAction action, final Object what) {
        try {
            action.run();
        } catch (final IOException e) {
            LOG.warn("Cannot clean up {}: {}", what, e.toString());
        }
    }

    static String originalFileName(final Attachment upload) {
        String name = null;
        final ContentDisposition disposition = upload.getContentDisposition();
        if (disposition != null) {
            name = disposition.getParameter("filename");
        }
        if ((name == null || name.isEmpty()) && upload.getDataHandler() != null) {
            name = upload.getDataHandler().getName();
        }
        if (name == null || name.isEmpty()) {
            return "plugin.kar";
        }
        final int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        return slash >= 0 ? name.substring(slash + 1) : name;
    }

    private static String describe(final List<Check> checks) {
        return checks.stream().map(c -> c.getId() + ": " + c.getMessage()).collect(Collectors.joining("; "));
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

    private static void audit(final String action, final String user, final String remote, final String karName, final String sha256, final String outcome, final String detail) {
        if ("ok".equals(outcome)) {
            AUDIT.info("action={} user={} remote={} kar={} sha256={} outcome={} {}", action, clean(user), clean(remote), clean(karName), clean(sha256), outcome, redact(clean(detail)));
        } else {
            AUDIT.warn("action={} user={} remote={} kar={} sha256={} outcome={} {}", action, clean(user), clean(remote), clean(karName), clean(sha256), outcome, redact(clean(detail)));
        }
    }

    static String clean(final String value) {
        return value == null ? null : CONTROL_CHARS.matcher(value).replaceAll("");
    }

    /** Strips the userinfo out of every URL so credentials never reach the log. */
    static String redact(final String value) {
        return value == null ? null : URL_USERINFO.matcher(value).replaceAll("$1");
    }

    private static boolean vanished(final IOException e) {
        return e instanceof NoSuchFileException || e instanceof FileNotFoundException;
    }

    private static WebApplicationException expiredToken() {
        return notFound("The upload token is unknown or has expired; run the check again.");
    }

    private interface Unsafe<T> {
        T get() throws Exception;
    }

    private static <T> T safely(final Unsafe<T> call, final T fallback) {
        try {
            return call.get();
        } catch (final Throwable t) {
            LOG.warn("Karaf bridge call failed: {}", t.toString());
            return fallback;
        }
    }

    private static String user(final SecurityContext securityContext) {
        return securityContext.getUserPrincipal() == null ? "unknown" : securityContext.getUserPrincipal().getName();
    }

    private static String remote(final HttpServletRequest request) {
        return request == null ? "unknown" : request.getRemoteAddr();
    }

    private static void requireAdmin(final SecurityContext securityContext) {
        if (securityContext == null || !securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            throw new WebApplicationException(Response.status(Status.FORBIDDEN).build());
        }
    }

    private static WebApplicationException sourceError(final PluginSourceException e) {
        return new WebApplicationException(Response.status(e.getStatus()).type(MediaType.TEXT_PLAIN).entity(e.getMessage()).build());
    }

    private static WebApplicationException badRequest(final String message) {
        return new WebApplicationException(Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(message).build());
    }

    private static WebApplicationException notFound(final String message) {
        return new WebApplicationException(Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity(message).build());
    }

    private static WebApplicationException conflict(final String message) {
        return new WebApplicationException(Response.status(Status.CONFLICT).type(MediaType.TEXT_PLAIN).entity(message).build());
    }

    private static WebApplicationException serverError(final String message) {
        return new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(message).build());
    }

    /** Builds the HTTP client on first use so the REST class loads without it. */
    private static final class LazyFetcher implements HttpFetcher {
        private volatile HttpFetcher delegate;

        @Override
        public Response get(final URI uri, final Map<String, String> headers) throws IOException {
            HttpFetcher fetcher = delegate;
            if (fetcher == null) {
                synchronized (this) {
                    if (delegate == null) {
                        final String version = new SystemInfoUtils().getDisplayVersion();
                        delegate = new ApacheHttpFetcher("OpenNMS/" + (version == null || version.isBlank() ? "unknown" : version));
                    }
                    fetcher = delegate;
                }
            }
            return fetcher.get(uri, headers);
        }

        @Override
        public void close() throws IOException {
            final HttpFetcher fetcher = delegate;
            if (fetcher != null) {
                fetcher.close();
            }
        }
    }
}
