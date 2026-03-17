package org.opennms.features.mibcompiler.rest.api.impl;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.opennms.features.mibcompiler.rest.api.MibCompilerRestService;
import org.opennms.features.mibcompiler.rest.model.CompileMibRequest;
import org.opennms.features.mibcompiler.rest.model.CompileMibResult;
import org.opennms.features.mibcompiler.rest.service.MibCompilerFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.File;
import java.io.InputStream;
import java.util.*;


public class MibCompilerRestServiceImpl implements MibCompilerRestService {
    private static final Logger LOG = LoggerFactory.getLogger(MibCompilerRestServiceImpl.class);


    private final MibCompilerFileService mibCompilerFileService;

    public MibCompilerRestServiceImpl(MibCompilerFileService mibCompilerFileService) {
        this.mibCompilerFileService = mibCompilerFileService;
    }


    @Override
    public Response uploadMibFiles(final List<Attachment> attachments, final SecurityContext securityContext) {
        final Map<String, Attachment> fileMap = new LinkedHashMap<>();
        for (final Attachment attachment : attachments) {
            final String originalFilename = safeFilename(attachment);
            final String baseName = MibCompilerFileService.stripPathAndExtension(originalFilename);

            if (baseName == null || baseName.isBlank()) {
                LOG.warn("Skipping attachment with invalid filename: {}", originalFilename);
                continue;
            }

            if (fileMap.containsKey(baseName)) {
                final String existingFilename = safeFilename(fileMap.get(baseName));
                LOG.warn("Duplicate basename detected: '{}' and '{}' resolve to same name '{}'. Keeping first file.",
                        existingFilename, originalFilename, baseName);
                continue;
            }

            fileMap.put(baseName, attachment);
        }

        final List<Map<String, Object>> successList = new ArrayList<>();
        final List<Map<String, Object>> errorList = new ArrayList<>();

        for (final Map.Entry<String, Attachment> entry : fileMap.entrySet()) {
            final String baseName = entry.getKey();
            final Attachment attachment = entry.getValue();
            final String originalFilename = safeFilename(attachment);

            try {
                if (mibCompilerFileService.baseNameExistsInPendingOrCompiled(baseName)) {
                    errorList.add(Map.of(
                            "filename", originalFilename,
                            "basename", baseName,
                            "error", "A MIB with the same base name already exists in pending/ or compiled/."
                    ));
                    continue;
                }

                final String ext = MibCompilerFileService.normalizeExtension(
                        getExtensionOrDefault(originalFilename, MibCompilerFileService.DEFAULT_MIB_EXTENSION),
                        MibCompilerFileService.DEFAULT_MIB_EXTENSION
                );

                try (InputStream in = attachment.getObject(InputStream.class)) {
                    final File saved = mibCompilerFileService.saveToPending(baseName, ext, in);
                    successList.add(Map.of(
                            "filename", originalFilename,
                            "savedAs", saved.getName(),
                            "success", true
                    ));
                }
            } catch (Exception e) {
                String message = e.getMessage();
                if (message == null || message.isBlank()) {
                    message = "Unexpected error while processing MIB file.";
                }
                String detailedError = e.getClass().getSimpleName() + ": " + message;
                errorList.add(Map.of(
                        "filename", originalFilename,
                        "basename", baseName,
                        "error", detailedError,
                        "exception", e.getClass().getName()
                ));
            }
        }

        return Response.ok(Map.of("success", successList, "errors", errorList)).build();
    }

    @Override
    public Response compilePendingMib(CompileMibRequest request, SecurityContext securityContext) throws Exception {
        final String name = request != null ? request.getName() : null;

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

    private static String safeName(String name) {
        // keep response stable; avoid returning nulls when possible
        return name == null ? "" : name;
    }

    private static String fileNameOnly(File f) {
        return f == null ? null : f.getName();
    }

    private static List<String> emptyToNull(List<String> l) {
        return (l == null || l.isEmpty()) ? null : l;
    }


    private static String safeFilename(final Attachment attachment) {
        if (attachment == null || attachment.getContentDisposition() == null) {
            return null;
        }
        return attachment.getContentDisposition().getParameter("filename");
    }

    private static String stripPathAndExtension(final String filename) {
        if (filename == null) return null;

        // Strip any path
        String justName = filename;
        int slash = justName.lastIndexOf('/');
        int backslash = justName.lastIndexOf('\\');
        int idx = Math.max(slash, backslash);
        if (idx >= 0 && idx + 1 < justName.length()) {
            justName = justName.substring(idx + 1);
        }

        justName = justName.trim();
        if (justName.isEmpty()) return null;

        // Strip extension
        int dot = justName.lastIndexOf('.');
        if (dot > 0) {
            return justName.substring(0, dot);
        }
        return justName;
    }

    private static String getExtensionOrDefault(final String filename, final String defaultExt) {
        if (filename == null) return defaultExt;
        int dot = filename.lastIndexOf('.');
        if (dot > 0 && dot < filename.length() - 1) {
            return filename.substring(dot); // includes the "."
        }
        return defaultExt;
    }
}
