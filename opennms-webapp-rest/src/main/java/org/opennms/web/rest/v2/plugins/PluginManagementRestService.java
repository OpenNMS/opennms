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
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.plugins.KarInspection.Check;
import org.opennms.web.rest.v2.plugins.KarInspection.FeatureInfo;
import org.opennms.web.rest.v2.plugins.KarInspection.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Admin-only management of plugin KARs: check an upload, stage it into the
 * deploy directory, list what is deployed and unload a plugin. Every action is
 * written to the plugin-management log.
 */
@Component
@Path("plugin-management")
@Tag(name = "PluginManagement", description = "Upload, check, stage and unload plugin KARs")
public class PluginManagementRestService {
    private static final Logger LOG = LoggerFactory.getLogger(PluginManagementRestService.class);
    static final String AUDIT_LOGGER = "org.opennms.web.rest.v2.plugins.audit";
    private static final Logger AUDIT = LoggerFactory.getLogger(AUDIT_LOGGER);

    // Karaf's extender skips dot-files, so a name may not start with one.
    static final Pattern KAR_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");
    static final Pattern UPLOAD_TOKEN = Pattern.compile("[0-9a-f]{64}");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}]");
    static final String BOOT_SUFFIX = ".boot";
    static final String UPLOAD_NAME_SUFFIX = ".name";
    static final String WAIT_FOR_KAR = "wait-for-kar=";
    static final Duration UPLOAD_RETENTION = Duration.ofHours(1);
    static final String TOO_LARGE = "The file is larger than " + (KarInspector.MAX_SIZE_BYTES / (1024 * 1024)) + " MB";

    private final java.nio.file.Path opennmsHome;
    private final java.nio.file.Path deployDir;
    private final java.nio.file.Path bootDir;
    private final java.nio.file.Path uploadDir;
    private final KarafBridge bridge;
    private final PluginRegistry registry;
    private final KarInspector inspector;
    private final CompatibilityChecker checker;

    public PluginManagementRestService() {
        this(Paths.get(System.getProperty("opennms.home", ".")), new OsgiKarafBridge());
    }

    PluginManagementRestService(final java.nio.file.Path opennmsHome, final KarafBridge bridge) {
        this.opennmsHome = opennmsHome;
        this.deployDir = opennmsHome.resolve("deploy");
        this.bootDir = opennmsHome.resolve("etc").resolve("featuresBoot.d");
        this.uploadDir = opennmsHome.resolve("data").resolve("tmp").resolve("plugin-management");
        this.bridge = bridge;
        this.registry = new PluginRegistry(opennmsHome, bridge);
        this.inspector = new KarInspector(deployDir);
        this.checker = new CompatibilityChecker(bridge);
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
        if (request != null && request.getContentLengthLong() > KarInspector.MAX_SIZE_BYTES) {
            audit("check", user, remote, null, null, "rejected", "content length " + request.getContentLengthLong() + " exceeds the limit");
            throw badRequest(TOO_LARGE);
        }
        if (upload == null) {
            audit("check", user, remote, null, null, "rejected", "no upload part");
            throw badRequest("A multipart part named 'upload' carrying the KAR is required.");
        }
        final String fileName = originalFileName(upload);
        java.nio.file.Path staging = null;
        try {
            pruneUploads();
            Files.createDirectories(uploadDir);
            staging = Files.createTempFile(uploadDir, "upload-", ".part");
            try (InputStream in = upload.getObject(InputStream.class); OutputStream out = Files.newOutputStream(staging)) {
                if (!copyBounded(in, out, KarInspector.MAX_SIZE_BYTES)) {
                    audit("check", user, remote, null, null, "rejected", "upload exceeds the limit; file=" + fileName);
                    throw badRequest(TOO_LARGE);
                }
            }
            final KarInspection inspection = inspector.inspect(staging, fileName);
            checker.check(inspection);
            final java.nio.file.Path stored = uploadDir.resolve(inspection.getSha256() + PluginRegistry.KAR_SUFFIX);
            Files.move(staging, stored, StandardCopyOption.REPLACE_EXISTING);
            staging = null;
            Files.write(uploadDir.resolve(inspection.getSha256() + UPLOAD_NAME_SUFFIX), fileName.getBytes(StandardCharsets.UTF_8));
            inspection.setUploadToken(inspection.getSha256());
            audit("check", user, remote, inspection.getKarName(), inspection.getSha256(), "ok",
                    "fails=" + inspection.checksAt(Level.FAIL).size() + " warns=" + inspection.checksAt(Level.WARN).size());
            return inspection;
        } catch (final IOException e) {
            audit("check", user, remote, null, null, "error", "file=" + fileName + " " + e);
            throw serverError("Cannot store the upload: " + e.getMessage());
        } finally {
            if (staging != null) {
                try {
                    Files.deleteIfExists(staging);
                } catch (final IOException e) {
                    LOG.warn("Cannot delete staging file {}: {}", staging, e.toString());
                }
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
        final java.nio.file.Path stored = uploadDir.resolve(token + PluginRegistry.KAR_SUFFIX);
        if (!Files.isRegularFile(stored)) {
            audit("install", user, remote, null, token, "rejected", "unknown or expired upload token");
            throw notFound("The upload token is unknown or has expired; run the check again.");
        }
        final String fileName = storedFileName(token);
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
            audit("install", user, remote, karName, token, "error", e.toString());
            throw serverError("Cannot read the stored upload: " + e.getMessage());
        }
        final java.nio.file.Path target = deployDir.resolve(karName + PluginRegistry.KAR_SUFFIX);
        if (previous.isPresent() && !previous.get().isUnloaded() && inspection.getSha256().equals(previous.get().getSha256()) && Files.exists(target)) {
            audit("install", user, remote, karName, inspection.getSha256(), "rejected", "already loaded");
            throw conflict("Plugin '" + karName + "' with this checksum is already loaded.");
        }
        checker.check(inspection);
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

        final List<String> features = inspection.topLevelFeatures().stream().map(FeatureInfo::getName).collect(Collectors.toList());
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
            final PluginEntry recordedEntry = registry.recordInstall(karName, fileName, inspection.getSha256(), inspection.getSize(), user, features, opennmsHome.relativize(bootFile).toString(), autoStart);
            recorded = true;
            Files.copy(stored, part, StandardCopyOption.REPLACE_EXISTING);
            atomicMove(part, target);
            audit("install", user, remote, karName, inspection.getSha256(), "ok", "features=" + String.join(",", features) + " autoStart=" + autoStart + " warnsAcknowledged=" + warns.size());
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
        final String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return false;
        }
        final String wanted = WAIT_FOR_KAR + karName;
        for (final String token : trimmed.split("\\s+")) {
            if (wanted.equals(token)) {
                return true;
            }
        }
        return false;
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
            LOG.warn("Cannot roll back {}: {}", what, e.toString());
        }
    }

    private String storedFileName(final String token) {
        final java.nio.file.Path nameFile = uploadDir.resolve(token + UPLOAD_NAME_SUFFIX);
        try {
            if (Files.isRegularFile(nameFile)) {
                final String name = new String(Files.readAllBytes(nameFile), StandardCharsets.UTF_8).trim();
                if (!name.isEmpty()) {
                    return name;
                }
            }
        } catch (final IOException e) {
            LOG.warn("Cannot read {}: {}", nameFile, e.toString());
        }
        return token + PluginRegistry.KAR_SUFFIX;
    }

    private void pruneUploads() {
        if (!Files.isDirectory(uploadDir)) {
            return;
        }
        final long cutoff = System.currentTimeMillis() - UPLOAD_RETENTION.toMillis();
        try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(uploadDir)) {
            for (final java.nio.file.Path p : stream) {
                if (Files.isRegularFile(p) && Files.getLastModifiedTime(p).toMillis() < cutoff) {
                    Files.deleteIfExists(p);
                }
            }
        } catch (final IOException e) {
            LOG.warn("Cannot prune {}: {}", uploadDir, e.toString());
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

    private static void audit(final String action, final String user, final String remote, final String karName, final String sha256, final String outcome, final String detail) {
        if ("ok".equals(outcome)) {
            AUDIT.info("action={} user={} remote={} kar={} sha256={} outcome={} {}", action, clean(user), clean(remote), clean(karName), clean(sha256), outcome, clean(detail));
        } else {
            AUDIT.warn("action={} user={} remote={} kar={} sha256={} outcome={} {}", action, clean(user), clean(remote), clean(karName), clean(sha256), outcome, clean(detail));
        }
    }

    static String clean(final String value) {
        return value == null ? null : CONTROL_CHARS.matcher(value).replaceAll("");
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
}
