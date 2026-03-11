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
package org.opennms.web.rest.v2;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.opennms.web.rest.v2.api.MibCompilerRestApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class MibCompilerRestService implements MibCompilerRestApi {
    private static final Logger LOG = LoggerFactory.getLogger(MibCompilerRestService.class);

    @Autowired
    private MibCompilerFileService mibCompilerFileService;

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
                errorList.add(Map.of(
                        "filename", originalFilename,
                        "basename", baseName,
                        "error", e.getMessage()
                ));
            }
        }

        return Response.ok(Map.of("success", successList, "errors", errorList)).build();
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

    /**
     * Checks if any file in dir has the given basename (basename comparison, not full filename).
     * Requirement: "Base file names (without extension) must not already exist in either compiled or pending."
     */
    private static boolean baseNameExists(final File dir, final String baseName) throws Exception {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return false;
        }
        try (var stream = Files.list(dir.toPath())) {
            return stream.anyMatch(p -> {
                String name = p.getFileName().toString();
                String otherBase = stripPathAndExtension(name);
                return baseName.equals(otherBase);
            });
        }
    }
}
