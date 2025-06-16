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
package org.opennms.web.rest.v1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.web.api.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.common.collect.ImmutableSet;

@Component
@Path("filesystem")
@Tag(name = "FileSystem", description = "File System API")
public class FilesystemRestService {
    private static final Logger LOG = LoggerFactory.getLogger(FilesystemRestService.class);

    private static final Set<String> SUPPORTED_FILE_EXTENSIONS = ImmutableSet.of("xml",
            "properties",
            "boot",
            "cfg",
            "drl",
            "groovy",
            "bsh",
            "dcb");
    private final java.nio.file.Path usersXml;

    public FilesystemRestService() {
        try {
            this.usersXml = ConfigFileConstants.getFile(ConfigFileConstants.USERS_CONF_FILE_NAME).toPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    FilesystemRestService(final java.nio.file.Path usersXml) {
        this.usersXml = usersXml;
    }

    private final java.nio.file.Path etcFolder = Paths.get(System.getProperty("opennms.home"), "etc");
    private final java.nio.file.Path etcPristineFolder = Paths.get(System.getProperty("opennms.home"), "share", "etc-pristine");

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getFiles(@QueryParam("changedFilesOnly") boolean changedFilesOnly, @Context SecurityContext securityContext) {
        if (!securityContext.isUserInRole(Authentication.ROLE_FILESYSTEM_EDITOR)) {
            throw new ForbiddenException("FILESYSTEM EDITOR role is required for enumerating files.");
        }

        try {
            return Files.find(etcFolder, 4, (path, basicFileAttributes) -> isSupportedExtension(path), FileVisitOption.FOLLOW_LINKS)
                    .filter(p -> !p.equals(usersXml) || securityContext.isUserInRole(Authentication.ROLE_ADMIN))
                    .map(p -> etcFolder.relativize(p).toString())
                    .filter(p -> !changedFilesOnly || !doesFileExistAndMatchContentsWithEtcPristine(p, securityContext))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to enumerate files in path: " + etcFolder, e);
        }
    }

    public boolean doesFileExistAndMatchContentsWithEtcPristine(String file, final SecurityContext securityContext) {
        final java.nio.file.Path etcPath = ensureFileIsAllowed(file, securityContext);
        final java.nio.file.Path etcPristinePath = etcPristineFolder.resolve(file);
        if (!Files.exists(etcPristinePath)) {
            return false;
        }

        try (Reader pathReader = Files.newBufferedReader(etcPath);
             Reader etcPristineReader = Files.newBufferedReader(etcPristinePath)) {
            return IOUtils.contentEqualsIgnoreEOL(pathReader, etcPristineReader);
        } catch (IOException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @GET
    @Path("/help")
    @Produces("text/markdown")
    public InputStream getFileHelp(@QueryParam("f") String fileName, @Context SecurityContext securityContext) {
        if (!securityContext.isUserInRole(Authentication.ROLE_FILESYSTEM_EDITOR)) {
            throw new ForbiddenException("FILESYSTEM EDITOR role is required for retrieving help.");
        }
        ensureFileIsAllowed(fileName, securityContext);
        return this.getClass().getResourceAsStream("/help/" + fileName + ".md");
    }

    @GET
    @Path("/extensions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getSupportedExtensions(@Context SecurityContext securityContext) {
        if (!securityContext.isUserInRole(Authentication.ROLE_FILESYSTEM_EDITOR)) {
            throw new ForbiddenException("FILESYSTEM EDITOR role is required for retrieving supported extensions.");
        }
        return SUPPORTED_FILE_EXTENSIONS.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    @GET
    @Path("/contents")
    public Response getFileContents(@QueryParam("f") String fileName, @Context SecurityContext securityContext) {
        if (!securityContext.isUserInRole(Authentication.ROLE_FILESYSTEM_EDITOR)) {
            throw new ForbiddenException("FILESYSTEM EDITOR role is required for reading files.");
        }
        return fileContents(ensureFileIsAllowed(fileName, securityContext));
    }

    @POST
    @Path("/contents")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String uploadFile(@QueryParam("f") String fileName,
                             @Multipart("upload") Attachment attachment,
                             @Context SecurityContext securityContext) throws IOException {
        if (!securityContext.isUserInRole(Authentication.ROLE_FILESYSTEM_EDITOR)) {
            throw new ForbiddenException("FILESYSTEM EDITOR role is required for uploading file contents.");
        }
        final java.nio.file.Path targetPath = ensureFileIsAllowed(fileName, securityContext);

        // Write the contents a temporary file
        final File tempFile = File.createTempFile("upload-", targetPath.getFileName().toString());
        try {
            tempFile.deleteOnExit();
            final InputStream in = attachment.getObject(InputStream.class);
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Validate it
            maybeValidateXml(tempFile);

            // Copy it to the right place
            Files.copy(tempFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return String.format("Successfully wrote to '%s'.", targetPath);
        } finally {
            // Delete the temporary file
            if (!tempFile.delete()) {
                LOG.warn("Failed to delete temporary file '{}' when uploading contents for '{}'.", tempFile, targetPath);
            }
        }
    }

    @DELETE
    @Path("/contents")
    @Produces(MediaType.TEXT_HTML)
    public String deleteFile(@QueryParam("f") String fileName,
                             @Context SecurityContext securityContext) throws IOException {
        if (!securityContext.isUserInRole(Authentication.ROLE_FILESYSTEM_EDITOR)) {
            throw new ForbiddenException("FILESYSTEM EDITOR role is required for deleting file contents.");
        }
        final java.nio.file.Path targetPath = ensureFileIsAllowed(fileName, securityContext);
        Files.delete(targetPath);
        return String.format("Successfully deleted to '%s'.", targetPath);
    }

    public static Response fileContents(final java.nio.file.Path path) {
        if (!Files.exists(path)) {
            return Response.noContent().build();
        }
        try {
            final String mimeType = Files.probeContentType(path);
            return Response.ok(Files.readString(path, StandardCharsets.UTF_8))
                    .type(mimeType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, path.getFileName().toString())
                    .header(HttpHeaders.LAST_MODIFIED, new Date(Files.getLastModifiedTime(path).toMillis()))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isSupportedExtension(java.nio.file.Path path) {
        return SUPPORTED_FILE_EXTENSIONS.contains(FilenameUtils.getExtension(path.getFileName().toString()));
    }

    private java.nio.file.Path ensureFileIsAllowed(String fileName, SecurityContext securityContext) {
        final java.nio.file.Path etcFolderNormalized = etcFolder.normalize();
        final java.nio.file.Path fileNormalized = etcFolder.resolve(fileName).normalize();

        if (fileNormalized.equals(usersXml) && !securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            throw new ForbiddenException("ADMIN role is required for accessing users.xml file contents.");
        }

        if (!(fileNormalized.getNameCount() > etcFolderNormalized.getNameCount() && fileNormalized.startsWith(etcFolderNormalized))) {
            throw new BadRequestException("Cannot access files outside of folder! Filename given: " + fileName);
        }
        if (!SUPPORTED_FILE_EXTENSIONS.contains(FilenameUtils.getExtension(fileNormalized.getFileName().toString()))) {
            throw new BadRequestException("Unsupported file extension: " + fileName);
        }
        return fileNormalized;
    }

    void maybeValidateXml(File file) {
        if (!file.getName().endsWith(".xml")) {
            return;
        }

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        try {
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException e) {
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error configuring parser factory: " + e.getMessage()).build());
        }

        final CapturingErrorHandler errorHandler = new CapturingErrorHandler();
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(errorHandler);
            builder.parse(file);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Validation failed: " + e.getMessage()).build());
        }
    }

    public static class CapturingErrorHandler implements ErrorHandler {
        final StringBuilder sb = new StringBuilder();

        public void warning(SAXParseException e) {
            sb.append("WARNING: ");
            sb.append(e.getMessage());
            sb.append("\n");
        }

        public void error(SAXParseException e) {
            sb.append("ERROR: ");
            sb.append(e.getMessage());
            sb.append("\n");
        }

        public void fatalError(SAXParseException e) {
            sb.append("FATAL ERROR: ");
            sb.append(e.getMessage());
            sb.append("\n");
        }

        public String toString() {
            return sb.toString();
        }
    }
}
