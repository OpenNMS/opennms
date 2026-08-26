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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "FileSystem", description = """
        File System API: read and write the configuration files under the OpenNMS `etc` directory, addressed
        by their path relative to that directory.

        Every operation requires `ROLE_FILESYSTEM_EDITOR`. That is enforced twice, by the servlet security
        configuration and again in each handler, so a caller without the role gets 403 from the container
        before the handler runs. `ROLE_ADMIN` on its own is not enough. `users.xml` is further restricted and
        needs `ROLE_ADMIN` as well.

        Only files whose extension is one of the supported set are reachable, and only inside `etc`, up to
        four levels deep. A name that resolves outside `etc`, or whose extension is not supported, is
        rejected with 400.

        Writing is a whole-file replace, not a patch, and an existing file is overwritten without a version
        check. `.xml` files are checked for well-formedness before the replace, which catches a truncated
        upload but not a schema violation: a well-formed file that the daemon cannot load is still written.
        Writes do not reload anything; the affected daemon has to be told separately.""")
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
    @Operation(
            summary = "List editable configuration files",
            description = """
        List the configuration files under `etc` that this API can read or write, as paths relative to `etc`,
        sorted. The walk goes four levels deep and follows symbolic links, and only files with a supported
        extension are listed.

        `users.xml` is listed only for callers that also hold `ROLE_ADMIN`.

        With `changedFilesOnly=true` the list is narrowed to files that differ from the pristine copy shipped
        in `share/etc-pristine`, ignoring line-ending differences. A file with no pristine counterpart counts
        as changed, so locally added files are included.""",
            operationId = "getEditableFiles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The file paths, relative to `etc`.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(type = "string")),
                            examples = @ExampleObject(value = """
                    [
                      "collectd-configuration.xml",
                      "discovery-configuration.xml",
                      "opennms.properties",
                      "poller-configuration.xml"
                    ]"""))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_FILESYSTEM_EDITOR`.")
    })
    public List<String> getFiles(
            @Parameter(description = "When true, list only files that differ from their `share/etc-pristine` counterpart, or that have none.",
                    example = "false")
            @QueryParam("changedFilesOnly") boolean changedFilesOnly, @Context SecurityContext securityContext) {
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
    @Operation(
            summary = "Get the help text for a configuration file",
            description = """
        Return the bundled Markdown help for one configuration file, if the distribution ships any. Help is
        looked up by file name, so a file with no help document produces an empty response body rather than a
        404.

        The file name still has to pass the same checks as the other operations, so an unsupported extension
        or a path outside `etc` is rejected with 400 even though nothing is read from `etc`.""",
            operationId = "getFileHelp"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The Markdown help document, or an empty body when none is bundled.",
                    content = @Content(mediaType = "text/markdown",
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "The name resolves outside `etc`, or its extension is not supported.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unsupported file extension: passwd"))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_FILESYSTEM_EDITOR`, or the file is `users.xml` and the caller is not an admin.")
    })
    public InputStream getFileHelp(
            @Parameter(description = "File path relative to `etc`, as listed by `GET /filesystem`.",
                    required = true, example = "discovery-configuration.xml")
            @QueryParam("f") String fileName, @Context SecurityContext securityContext) {
        if (!securityContext.isUserInRole(Authentication.ROLE_FILESYSTEM_EDITOR)) {
            throw new ForbiddenException("FILESYSTEM EDITOR role is required for retrieving help.");
        }
        ensureFileIsAllowed(fileName, securityContext);
        return this.getClass().getResourceAsStream("/help/" + fileName + ".md");
    }

    @GET
    @Path("/extensions")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "List the file extensions this API will handle",
            description = """
        List the file extensions the other operations accept, sorted. The set is fixed in the code rather
        than configurable, and a file whose extension is not in it cannot be read or written here whatever
        its location.""",
            operationId = "getSupportedFileExtensions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The supported extensions.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(type = "string")),
                            examples = @ExampleObject(value = """
                    [
                      "boot",
                      "bsh",
                      "cfg",
                      "dcb",
                      "drl",
                      "groovy",
                      "properties",
                      "xml"
                    ]"""))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_FILESYSTEM_EDITOR`.")
    })
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
    @Operation(
            summary = "Read a configuration file",
            description = """
        Return the contents of one configuration file as text, read as UTF-8. The response media type is
        probed from the file rather than fixed, and `Content-Disposition` carries the bare file name while
        `Last-Modified` carries the file's timestamp.

        A name that passes the checks but does not exist yields 204 with no body, so 204 is how a missing file
        is reported rather than 404. Read the file before writing it back: the write is a whole-file replace
        with no version check.""",
            operationId = "getFileContents"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The file contents. The media type is probed from the file.",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "204", description = "The file does not exist. No body."),
            @ApiResponse(responseCode = "400", description = "The name resolves outside `etc`, or its extension is not supported.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot access files outside of folder! Filename given: ../../passwd"))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_FILESYSTEM_EDITOR`, or the file is `users.xml` and the caller is not an admin.")
    })
    public Response getFileContents(
            @Parameter(description = "File path relative to `etc`, as listed by `GET /filesystem`.",
                    required = true, example = "discovery-configuration.xml")
            @QueryParam("f") String fileName, @Context SecurityContext securityContext) {
        if (!securityContext.isUserInRole(Authentication.ROLE_FILESYSTEM_EDITOR)) {
            throw new ForbiddenException("FILESYSTEM EDITOR role is required for reading files.");
        }
        return fileContents(ensureFileIsAllowed(fileName, securityContext));
    }

    @POST
    @Path("/contents")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(
            summary = "Write a configuration file",
            description = """
        Replace the contents of one configuration file with the uploaded part. The part must be named
        `upload`. The target file is named by the `f` query parameter, not by the part's own filename.

        This is a whole-file replace with no version check: an existing file is overwritten, and a file that
        does not exist yet is created. Read the current contents first if they matter.

        A `.xml` upload is parsed for well-formedness before the replace, and a parse failure is reported as
        400 with the target left untouched. The parse is non-validating and external entities are disabled, so
        a well-formed file that violates its schema is still written.

        Nothing is reloaded. Whatever daemon reads the file has to be told separately, for instance through
        its own reload endpoint or a `reloadDaemonConfig` event.""",
            operationId = "uploadFileContents"
    )
    @RequestBody(
            required = true,
            description = "Multipart form with a single part named `upload` carrying the new file contents.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
                    schema = @Schema(type = "string", format = "binary"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The file was written. The body is a plain confirmation, despite the `text/html` media type.",
                    content = @Content(mediaType = MediaType.TEXT_HTML,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Successfully wrote to '/opt/opennms/etc/example.xml'."))),
            @ApiResponse(responseCode = "400", description = "The name resolves outside `etc`, its extension is not supported, or an `.xml` upload is not well formed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Validation failed: XML document structures must start and end within the same entity."))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_FILESYSTEM_EDITOR`, or the file is `users.xml` and the caller is not an admin.")
    })
    public String uploadFile(
            @Parameter(description = "File path relative to `etc` to write. Created if it does not exist, overwritten if it does.",
                    required = true, example = "ApiDoc-probe.xml")
            @QueryParam("f") String fileName,
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
    @Operation(
            summary = "Delete a configuration file",
            description = """
        Delete one configuration file from `etc`. There is no confirmation step and no backup, and deleting a
        file the running instance depends on will break it at the next reload or restart.

        A name that passes the checks but does not exist fails with 500 from the underlying delete, not with
        404.""",
            operationId = "deleteFileContents"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The file was deleted. The body is a plain confirmation, despite the `text/html` media type.",
                    content = @Content(mediaType = MediaType.TEXT_HTML,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Successfully deleted to '/opt/opennms/etc/example.xml'."))),
            @ApiResponse(responseCode = "400", description = "The name resolves outside `etc`, or its extension is not supported.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unsupported file extension: passwd"))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_FILESYSTEM_EDITOR`, or the file is `users.xml` and the caller is not an admin."),
            @ApiResponse(responseCode = "500", description = "The file does not exist, or could not be removed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string")))
    })
    public String deleteFile(
            @Parameter(description = "File path relative to `etc` to delete.", required = true, example = "ApiDoc-probe.xml")
            @QueryParam("f") String fileName,
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
