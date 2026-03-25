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

import org.opennms.features.mibcompiler.rest.MibCompilerRestService;
import org.opennms.features.mibcompiler.rest.model.CompileMibResult;
import org.opennms.features.mibcompiler.rest.model.MibCompilerFileText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class MibCompilerRestServiceImpl implements MibCompilerRestService {

    private static final Logger LOG = LoggerFactory.getLogger(MibCompilerRestServiceImpl.class);

    private static final String UNKNOWN_FILENAME = "unknown";
    private static final int MAX_FILENAME_LENGTH = 255;

    private final MibCompilerFileService mibCompilerFileService;

    public MibCompilerRestServiceImpl(final MibCompilerFileService mibCompilerFileService) {
        this.mibCompilerFileService = Objects.requireNonNull(mibCompilerFileService, "mibCompilerFileService must not be null");
    }

    @Override
    public Response uploadMib(final byte[] mibContent, final String filename) throws Exception {
        final List<Map<String, Object>> successList = new ArrayList<>();
        final List<Map<String, Object>> errorList = new ArrayList<>();

        final String originalFilename = safeFilename(filename);
        final String baseName = MibCompilerFileService.stripPathAndExtension(originalFilename);

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

        if (mibCompilerFileService.baseNameExistsInPendingOrCompiled(baseName)) {
            errorList.add(error(originalFilename, baseName,
                    "A MIB with the same base name already exists in pending/ or compiled/."));
            return buildResponse(Response.Status.CONFLICT, successList, errorList);
        }

        final String ext = MibCompilerFileService.normalizeExtension(
                getExtensionOrDefault(originalFilename),
                MibCompilerFileService.DEFAULT_MIB_EXTENSION
        );

        try (InputStream in = new ByteArrayInputStream(mibContent)) {
            final File saved = mibCompilerFileService.saveToPending(baseName, ext, in);

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

        final CompileMibResult result = mibCompilerFileService.compilePendingByBaseName(name);

        switch (result.getStatus()) {

            case SUCCESS: {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", result.getMessage());
                response.put("mibName", safeName(name));
                response.put("compiledFile", fileNameOnly(result.getCompiledFile()));

                return Response.ok(response).build();
            }

            case NOT_FOUND: {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", result.getMessage());
                response.put("mibName", safeName(name));

                return Response.status(Response.Status.NOT_FOUND)
                        .entity(response)
                        .build();
            }

            case INVALID_REQUEST: {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", result.getMessage());
                response.put("mibName", safeName(name));

                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(response)
                        .build();
            }

            case MISSING_DEPENDENCIES:
            case CONFLICT: {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", result.getMessage());
                response.put("mibName", safeName(name));
                response.put("missingDependencies", emptyToNull(result.getMissingDependencies()));

                return Response.status(Response.Status.CONFLICT)
                        .entity(response)
                        .build();
            }

            case VALIDATION_FAILED: {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", result.getMessage());
                response.put("mibName", safeName(name));
                response.put("errors", result.getFormattedErrors());

                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(response)
                        .build();
            }

            default: {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Unexpected status: " + result.getStatus());
                response.put("mibName", safeName(name));

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(response)
                        .build();
            }
        }
    }

    @Override
    public Response listPendingAndCompiledFiles() {
        LOG.debug("REST request: list mib compiler files");

        try {
            final var files = mibCompilerFileService.listPendingAndCompiledFiles();
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

        validateFileNameAndLocation(fileName, location);

        try {
            final boolean deleted = mibCompilerFileService.deleteFile(location, fileName);

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
            validateFileNameAndLocation(fileName, location);

            final String contents = mibCompilerFileService.readTextFile(location, fileName);
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
    public Response setFileText(final String fileName, final MibCompilerFileText body) {
        LOG.debug("REST request: set mib compiler file text: location={}, fileName={}", body.getLocation(), fileName);

        validateFileNameAndLocation(body.getLocation(), fileName);

        if (!"pending".equalsIgnoreCase(body.getLocation())) {
            LOG.warn("Set file text rejected: location must be 'pending' (location={}, fileName={})", body.getLocation(), fileName);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Setting file contents is only allowed for location 'pending'.")
                    .build();
        }

        if (body.getContents() == null) {
            LOG.warn("Set file text rejected: null contents (location={}, fileName={})", body.getLocation(), fileName);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("contents must not be null.")
                    .build();
        }

        if (body.getName() != null && !fileName.equals(body.getName())) {
            LOG.warn("Set file text rejected: body.name mismatch (pathFileName={}, bodyName={})", fileName, body.getName());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Body 'name' must match path fileName.")
                    .build();
        }

        try {
            final boolean updated = mibCompilerFileService.writeTextFile(body.getLocation(), fileName, body.getContents());
            if (!updated) {
                LOG.info("Set file text: file not found (location={}, fileName={})", body.getLocation(), fileName);
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            LOG.info("Set file text: updated successfully (location={}, fileName={})", body.getLocation(), fileName);

            final MibCompilerFileText response = new MibCompilerFileText(fileName, "pending", body.getContents());
            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            LOG.warn("Set file text failed (bad request): location={}, fileName={}, msg={}", body.getLocation(), fileName, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (IllegalStateException e) {
            LOG.warn("Set file text failed (conflict): location={}, fileName={}, msg={}", body.getLocation(), fileName, e.getMessage(), e);
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        } catch (java.io.IOException e) {
            LOG.error("Set file text failed (I/O): location={}, fileName={}", body.getLocation(), fileName, e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Unable to write file.")
                    .build();
        } catch (Exception e) {
            LOG.error("Set file text failed (unexpected): location={}, fileName={}", body.getLocation(), fileName, e);
            return Response.serverError()
                    .entity("Unexpected error while writing file.")
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
            return MibCompilerFileService.DEFAULT_MIB_EXTENSION;
        }

        final int dot = filename.lastIndexOf('.');
        if (dot > 0 && dot < filename.length() - 1) {
            return filename.substring(dot);
        }
        return MibCompilerFileService.DEFAULT_MIB_EXTENSION;
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

    private static List<String> emptyToNull(List<String> l) {
        return (l == null || l.isEmpty()) ? null : l;
    }

    private static Response validateFileNameAndLocation(final String location, final String fileName) {
        if (location == null || location.isBlank()) {
            LOG.warn("Request rejected: blank location (fileName={})", fileName);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("location must not be blank.")
                    .build();
        }

        if (fileName == null || fileName.isBlank()) {
            LOG.warn("Request rejected: blank fileName (location={})", location);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("fileName must not be blank.")
                    .build();
        }

        if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) {
            LOG.warn("Request rejected: invalid fileName={} (location={})", fileName, location);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid fileName.")
                    .build();
        }

        return null;
    }
}