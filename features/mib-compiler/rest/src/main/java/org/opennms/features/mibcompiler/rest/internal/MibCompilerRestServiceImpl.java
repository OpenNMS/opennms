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
package org.opennms.features.mibcompiler.rest.internal;

import org.apache.commons.lang.StringUtils;
import org.opennms.features.mibcompiler.api.MibParser;
import org.opennms.features.mibcompiler.rest.MibCompilerRestService;
import org.opennms.features.mibcompiler.rest.model.MibCompilerFileText;
import org.opennms.features.mibcompiler.rest.model.MibCompilerGenerateEventsRequest;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.dao.support.EventConfServiceHelper;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.model.events.EventConfSourceMetadataDto;
import org.opennms.netmgt.xml.eventconf.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

import javax.annotation.PreDestroy;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class MibCompilerRestServiceImpl implements MibCompilerRestService {

    private static final Logger LOG = LoggerFactory.getLogger(MibCompilerRestServiceImpl.class);

    private static final String UNKNOWN_FILENAME = "unknown";
    private static final int MAX_FILENAME_LENGTH = 255;

    private final MibParser mibParser;
    private final EventConfSourceDao eventConfSourceDao;
    private final EventConfEventDao eventConfEventDao;
    private final EventConfDao eventConfDao;
    private final TransactionOperations operations;

    private final ExecutorService eventConfExecutor =
            EventConfServiceHelper.createEventConfExecutor("load-eventConf-%d");

    public MibCompilerRestServiceImpl(
            final MibParser mibParser, EventConfSourceDao eventConfSourceDao, EventConfEventDao eventConfEventDao, EventConfDao eventConfDao, TransactionOperations operations) {
        this.mibParser = Objects.requireNonNull(mibParser, "mibParser must not be null");
        this.eventConfSourceDao = eventConfSourceDao;
        this.eventConfEventDao = eventConfEventDao;
        this.eventConfDao = eventConfDao;
        this.operations = operations;

        this.mibParser.setMibDirectory(MibCompilerServiceUtil.getCompiledDir());
    }

    @Override
    public Response uploadMib(final byte[] mibContent, final String filename) throws Exception {
        final List<Map<String, Object>> successList = new ArrayList<>();
        final List<Map<String, Object>> errorList = new ArrayList<>();

        if (filename == null || filename.trim().isEmpty()) {
            final String originalFilename = safeFilename(filename);
            final String baseName = MibCompilerServiceUtil.stripPathAndExtension(originalFilename);

            LOG.warn("Skipping upload with missing/blank filename (rawFilename={})", filename);
            errorList.add(error(originalFilename, baseName, "filename must not be null/blank."));
            return buildResponse(Response.Status.BAD_REQUEST, successList, errorList);
        }

        final String originalFilename = safeFilename(filename);

        if (UNKNOWN_FILENAME.equals(originalFilename)) {
            final String baseName = MibCompilerServiceUtil.stripPathAndExtension(originalFilename);

            LOG.warn("Skipping upload with invalid filename: {}", filename);
            errorList.add(error(originalFilename, baseName, "Invalid filename."));
            return buildResponse(Response.Status.BAD_REQUEST, successList, errorList);
        }

        final String baseName = MibCompilerServiceUtil.stripPathAndExtension(originalFilename);

        if (isBlank(baseName)) {
            LOG.warn("Skipping upload with invalid filename: {}", originalFilename);

            errorList.add(error(originalFilename, baseName,
                    "Invalid filename; cannot derive base name."));

            return buildResponse(Response.Status.BAD_REQUEST, successList, errorList);
        }

        if (mibContent == null || mibContent.length == 0) {
            errorList.add(error(originalFilename, baseName, "Empty MIB content."));
            return buildResponse(Response.Status.BAD_REQUEST, successList, errorList);
        }

        if (MibCompilerServiceUtil.baseNameExistsInPendingOrCompiled(baseName)) {
            errorList.add(error(originalFilename, baseName,
                    "A MIB with the same base name already exists in pending/ or compiled/."));
            return buildResponse(Response.Status.CONFLICT, successList, errorList);
        }

        final String ext = MibCompilerServiceUtil.normalizeExtension(
                getExtensionOrDefault(originalFilename),
                MibCompilerServiceUtil.DEFAULT_MIB_EXTENSION
        );

        try (InputStream in = new ByteArrayInputStream(mibContent)) {
            final File saved = MibCompilerServiceUtil.saveToPending(baseName, ext, in);

            final Map<String, Object> success = new LinkedHashMap<>();
            success.put("filename", originalFilename);
            success.put("savedAs", saved.getName());
            success.put("success", Boolean.TRUE);
            successList.add(success);

            return buildResponse(Response.Status.CREATED, successList, errorList);

        } catch (Exception e) {
            LOG.warn("Failed to save uploaded MIB file '{}' (basename='{}') to pending.", originalFilename, baseName, e);

            errorList.add(errorWithException(originalFilename, baseName, e));
            return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, successList, errorList);
        }
    }

    @Override
    public Response compileMib(final String name) throws Exception {
        // 1) Validate request
        final String baseName = MibCompilerServiceUtil.stripPathAndExtension(safeName(name));
        if (isBlank(baseName)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "baseName must not be blank.");
            response.put("mibName", safeName(name));
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        final File pendingFile;
        try {
            pendingFile = MibCompilerServiceUtil.findPendingByBaseName(baseName);
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("mibName", safeName(name));
            return Response.status(Response.Status.CONFLICT).entity(response).build();
        }

        if (pendingFile == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "No pending file found with base name '" + baseName + "'.");
            response.put("mibName", safeName(name));
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }

        final boolean parsed = mibParser.parseMib(pendingFile);
        if (!parsed) {
            final var missingDeps = mibParser.getMissingDependencies();
            if (missingDeps != null && !missingDeps.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Missing dependencies: " + missingDeps);
                response.put("mibName", safeName(name));
                response.put("missingDependencies", missingDeps);
                return Response.status(Response.Status.CONFLICT).entity(response).build();
            }

            final String errors = mibParser.getFormattedErrors();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "MIB validation failed.");
            response.put("mibName", safeName(name));
            response.put("errors", errors);
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        final File compiledFile;
        try {
            compiledFile = MibCompilerServiceUtil.movePendingToCompiled(pendingFile, baseName);
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("mibName", safeName(name));
            return Response.status(Response.Status.CONFLICT).entity(response).build();
        }

        // 5) Success response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "MIB compiled successfully.");
        response.put("mibName", safeName(name));
        response.put("compiledFile", fileNameOnly(compiledFile));
        return Response.ok(response).build();
    }
    @Override
    public Response listPendingAndCompiledFiles() {
        LOG.debug("REST request: list mib compiler files");

        try {
            final var files = MibCompilerServiceUtil.listPendingAndCompiledFiles();
            return Response.ok(files).build();
        } catch (java.io.IOException e) {
            LOG.error("I/O error while listing mib compiler files", e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE) // 503
                    .entity("Unable to read mib directories.")
                    .build();
        } catch (Exception e) {
            LOG.error("Unexpected error while listing mib compiler files", e);
            return Response.serverError()
                    .entity("Unexpected error while listing mib compiler files.")
                    .build();
        }
    }

    @Override
    public Response deleteFile(String location, String fileName) {
        LOG.debug("REST request: delete mib compiler file: location={}, fileName={}", location, fileName);

        try {
            validateLocationAndFileName(location, fileName);
            final boolean deleted = MibCompilerServiceUtil.deleteFile(location, fileName);

            if (!deleted) {
                LOG.info("Mib compiler file not found for delete: location={}, fileName={}", location, fileName);
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            LOG.info("Mib compiler file deleted: location={}, fileName={}", location, fileName);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            LOG.warn("Delete mib compiler file failed (bad request): location={}, fileName={}, msg={}",
                    location, fileName, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (IllegalStateException e) {
            LOG.warn("Delete mib compiler file failed (conflict): location={}, fileName={}, msg={}",
                    location, fileName, e.getMessage(), e);
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        } catch (java.io.IOException e) {
            LOG.error("Delete mib compiler file failed (I/O): location={}, fileName={}", location, fileName, e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE) // 503
                    .entity("Unable to delete file.")
                    .build();
        } catch (Exception e) {
            LOG.error("Delete mib compiler file failed (unexpected): location={}, fileName={}", location, fileName, e);
            return Response.serverError()
                    .entity("Unexpected error while deleting file.")
                    .build();
        }
    }

    @Override
    public Response getFileText(String location, String fileName) throws Exception {
        try {
            validateLocationAndFileName(location, fileName);

            final String contents = MibCompilerServiceUtil.readTextFile(location, fileName);
            if (contents == null) {
                LOG.info("Mib compiler file not found for text read: location={}, fileName={}", location, fileName);
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            final var dto = new MibCompilerFileText(fileName, location.toLowerCase(), contents);
            return Response.ok(dto).build();
        } catch (IllegalArgumentException e) {
            LOG.warn("Get file text failed (bad request): location={}, fileName={}, msg={}", location, fileName, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (IllegalStateException e) {
            LOG.warn("Get file text failed (conflict): location={}, fileName={}, msg={}", location, fileName, e.getMessage(), e);
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        } catch (java.io.IOException e) {
            LOG.error("Get file text failed (I/O): location={}, fileName={}", location, fileName, e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Unable to read file.")
                    .build();
        } catch (Exception e) {
            LOG.error("Get file text failed (unexpected): location={}, fileName={}", location, fileName, e);
            return Response.serverError()
                    .entity("Unexpected error while reading file.")
                    .build();
        }
    }


    @Override
    public Response setFileText(final String fileName, final byte[] mibContent) {
        final String location = "pending";
        LOG.debug("REST request: set mib compiler file text: location={}, fileName={}", location, fileName);

        try {

            validateLocationAndFileName(location, fileName);

            if (mibContent == null) {
                LOG.warn("Set file text rejected: null mibContent (location={}, fileName={})", location, fileName);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("mibContent must not be null.")
                        .build();
            }

            MibCompilerServiceUtil.writeBinaryFile(location, fileName, mibContent);

            LOG.info("Set file text: updated successfully (location={}, fileName={})", location, fileName);
            return Response.ok("Text updated successfully").build();

        } catch (IllegalArgumentException e) {
            final String msg = e.getMessage();
            final boolean isNotFound = msg != null && msg.toLowerCase().contains("file does not");

            if (isNotFound) {
                LOG.warn("Set file text failed (not found): location={}, fileName={}, msg={}", location, fileName, msg);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(msg)
                        .build();
            }

            LOG.warn("Set file text failed (bad request): location={}, fileName={}, msg={}", location, fileName, msg);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(msg)
                    .build();

        } catch (IllegalStateException e) {
            LOG.warn("Set file text failed (conflict): location={}, fileName={}, msg={}", location, fileName, e.getMessage(), e);
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        } catch (java.io.IOException e) {
            LOG.error("Set file text failed (I/O): location={}, fileName={}", location, fileName, e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Unable to write file.")
                    .build();
        } catch (Exception e) {
            LOG.error("Set file text failed (unexpected): location={}, fileName={}", location, fileName, e);
            return Response.serverError()
                    .entity("Unexpected error while writing file.")
                    .build();
        }
    }
    @Override
    public Response generateEvents(final MibCompilerGenerateEventsRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Request must not be null.")
                    .build();
        }
        final String location = "compiled";
        String fileName = request.getName();
        LOG.debug("REST request: generate events: location={}, file={}", location, request != null ? request.getName() : null);

        try {

            if (fileName == null || fileName.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("mibFileName must not be null/empty.")
                        .build();
            }

            validateLocationAndFileName(location, fileName);

            final String ueiBase = request.getUeiBase();
            if (ueiBase == null || ueiBase.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("ueiBase must not be null/empty.")
                        .build();
            }
            final boolean exists = MibCompilerServiceUtil.exists(location, fileName);
            if (!exists) {
                LOG.info("Generate events: file not found (location={}, fileName={})", location, fileName);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("MIB file not found in compiled directory: " + fileName)
                        .build();
            }

            final File mibFile = MibCompilerServiceUtil.getFile(location, fileName);

            LOG.info("Parsing MIB before generating events (fileName={}, location={})", fileName, location);

            if (!mibParser.parseMib(mibFile)) {
                final List<String> dependencies = mibParser.getMissingDependencies();
                if (dependencies != null && !dependencies.isEmpty()) {
                    LOG.warn("Generate events rejected: missing dependencies (fileName={}, deps={})", fileName, dependencies);
                    return Response.status(Response.Status.CONFLICT)
                            .entity("Dependencies required: " + dependencies)
                            .build();
                }

                final String formattedErrors = mibParser.getFormattedErrors();
                LOG.warn("Generate events rejected: MIB parse errors (fileName={}, errors={})", fileName, formattedErrors);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(formattedErrors != null ? formattedErrors : "Problem found when compiling the MIB.")
                        .build();
            }

            final Events events = mibParser.getEvents(ueiBase);
            if (events == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("MIB parsed successfully but event generation returned null.")
                        .build();
            }

            return operations.execute(status -> {
                try {
                    final Date now = new Date();
                    final int maxFileOrder = Optional.ofNullable(eventConfSourceDao.findMaxFileOrder()).orElse(0);
                    final int nextFileOrder = maxFileOrder + 1;
                    final EventConfSourceMetadataDto meta =
                            buildMetadata(fileName, events, nextFileOrder, now);

                    final EventConfSource source = EventConfServiceHelper.createOrUpdateSource(eventConfSourceDao, meta);

                    eventConfEventDao.deleteBySourceId(source.getId());
                    EventConfServiceHelper.saveEvents(eventConfEventDao, source, events, meta.getUsername(), meta.getNow());
                    EventConfServiceHelper.reloadEventsFromDBAsync(eventConfEventDao, eventConfDao, eventConfExecutor);

                    final Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("success", true);
                    resp.put("message", "Events generated successfully.");
                    resp.put("mibFile", fileName);
                    resp.put("sourceId", source.getId());
                    return Response.ok(resp).build();
                } catch (Exception e) {
                    status.setRollbackOnly();
                    LOG.error("Generate events failed during DB transaction", e);
                    return Response.serverError()
                            .entity("Unexpected error while generating events.")
                            .build();
                }
            });

        } catch (IllegalArgumentException e) {
            LOG.warn("Generate events failed (bad request): location={}, fileName={}, msg={}", location, fileName, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error("Generate events failed (unexpected): location={}, fileName={}", location, fileName, e);
            return Response.serverError()
                    .entity("Unexpected error while generating events.")
                    .build();
        }
    }


    private static Response buildResponse(final Response.Status status,
                                          final List<Map<String, Object>> successList,
                                          final List<Map<String, Object>> errorList) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("success", successList);
        payload.put("errors", errorList);
        return Response.status(status).entity(payload).build();
    }

    private static Map<String, Object> error(final String filename, final String basename, final String message) {
        final Map<String, Object> e = new LinkedHashMap<>();
        e.put("filename", filename);
        e.put("basename", basename);
        e.put("error", message);
        return e;
    }

    private static Map<String, Object> errorWithException(final String filename, final String basename, final Exception ex) {
        final Map<String, Object> e = error(filename, basename, toDetailedErrorMessage(ex));
        e.put("exception", ex.getClass().getName());
        return e;
    }

    private static boolean isBlank(final String s) {
        if (s == null) return true;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static String safeFilename(final String filename) {
        if (filename == null) {
            return UNKNOWN_FILENAME;
        }

        String name = filename.trim();
        if (name.isEmpty()) {
            return UNKNOWN_FILENAME;
        }

        name = name.replace('\\', '/');
        final int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }

        name = name.trim();
        if (name.isEmpty() || ".".equals(name) || "..".equals(name)) {
            return UNKNOWN_FILENAME;
        }

        name = name.replaceAll("[\\p{Cntrl}\\\\/:*?\"<>|]+", "_");

        name = name.replaceAll("^[\\s.]+", "");
        name = name.replaceAll("[\\s.]+$", "");

        if (name.isEmpty()) {
            return UNKNOWN_FILENAME;
        }

        if (name.length() > MAX_FILENAME_LENGTH) {
            name = name.substring(0, MAX_FILENAME_LENGTH);
        }

        return name;
    }

    private static String getExtensionOrDefault(final String filename) {
        if (isBlank(filename)) {
            return MibCompilerServiceUtil.DEFAULT_MIB_EXTENSION;
        }

        final int dot = filename.lastIndexOf('.');
        if (dot > 0 && dot < filename.length() - 1) {
            return filename.substring(dot);
        }
        return MibCompilerServiceUtil.DEFAULT_MIB_EXTENSION;
    }

    private static String toDetailedErrorMessage(final Exception e) {
        String message = e.getMessage();
        if (isBlank(message)) {
            message = "Unexpected error while processing MIB file.";
        }
        return e.getClass().getSimpleName() + ": " + message;
    }

    private static String safeName(String name) {
        return name == null ? "" : name;
    }

    private static String fileNameOnly(File f) {
        return f == null ? null : f.getName();
    }

    private static void validateLocationAndFileName(final String location, final String fileName) {
        if (location == null || location.isBlank()) {
            LOG.warn("Request rejected: blank location (fileName={})", fileName);
            throw new IllegalArgumentException("location must not be blank.");
        }

        if (fileName == null || fileName.isBlank()) {
            LOG.warn("Request rejected: blank fileName (location={})", location);
            throw new IllegalArgumentException("fileName must not be blank.");
        }

        if (fileName.length() > MAX_FILENAME_LENGTH) {
            LOG.warn("Request rejected: oversized fileName length={} (location={})",
                    fileName.length(), location);
            throw new IllegalArgumentException("fileName too long (max " + MAX_FILENAME_LENGTH + ").");
        }

        if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) {
            LOG.warn("Request rejected: invalid fileName={} (location={})", fileName, location);
            throw new IllegalArgumentException("Invalid fileName.");
        }
    }

    private EventConfSourceMetadataDto buildMetadata(String fileName, Events events, int fileOrder,
                                                     Date now) {

        final String baseName = StringUtils.substringBeforeLast(fileName, ".");
        final String base = StringUtils.isBlank(baseName) ? fileName : baseName;

        final String eventsFileName = base + ".events";

        return new EventConfSourceMetadataDto.Builder()
                .filename(eventsFileName)
                .eventCount(events.getEvents().size())
                .fileOrder(fileOrder)
                .username("system-generated")
                .now(now)
                .vendor(base)
                .description("")
                .build();
    }

    @PreDestroy
    public void shutdown() {
        eventConfExecutor.shutdown();
    }

}