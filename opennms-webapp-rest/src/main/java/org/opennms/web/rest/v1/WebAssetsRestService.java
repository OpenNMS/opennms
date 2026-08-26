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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.IOUtils;
import org.opennms.web.utils.assets.AssetLocator;
import org.opennms.web.utils.assets.AssetResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

@Component("webAssetsRestService")
@Path("web-assets")
@Tag(name = "Web-Assets", description = """
        Web Assets API: the bundled front-end assets (scripts, stylesheets, fonts, images) that the OpenNMS
        web UI loads, addressed by the logical asset name the build assigned them.

        An asset name usually maps to more than one file, one per type: `opennms` covers both `opennms.css`
        and `opennms.min.js`. The listing operations report that mapping; the third operation serves the file
        itself.

        These endpoints exist for the UI's own use, and the asset names are a build artefact rather than a
        stable interface.""")
public class WebAssetsRestService extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(WebAssetsRestService.class);

    @Autowired
    private AssetLocator m_assetLocator;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/")
    @Operation(
            summary = "List asset names",
            description = """
        List every known asset name. The list is unsorted and, on the builds checked, includes one empty
        string, so callers should not assume every entry addresses a real asset.""",
            operationId = "listWebAssets"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The asset names.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(type = "string")),
                            examples = @ExampleObject(value = """
                    [
                      "onms-assets",
                      "opennms",
                      "font-awesome",
                      "global"
                    ]""")))
    })
    public List<String> listAssets() {
        return new ArrayList<>(m_assetLocator.getAssets());
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("{assetName}")
    @Operation(
            summary = "List the files that make up one asset",
            description = """
        List the files registered under an asset name. Each entry gives the asset name, its type and the path
        the file is served from, which is what `GET /web-assets/{assetName}.{type}` addresses.

        A few assets carry inline content rather than a file: for those the `type` is `text` and `path` holds
        the content itself rather than a filename.""",
            operationId = "getWebAssetResources"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The files registered under the asset name.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = AssetResource.class)),
                            examples = @ExampleObject(value = """
                    [
                      {
                        "asset": "opennms",
                        "type": "css",
                        "path": "opennms.css"
                      },
                      {
                        "asset": "opennms",
                        "type": "js",
                        "path": "opennms.min.js"
                      }
                    ]"""))),
            @ApiResponse(responseCode = "404", description = "No asset with that name is registered. The body is empty.")
    })
    public List<AssetResource> getResources(
            @Parameter(description = "Asset name, as listed by `GET /web-assets`.", required = true, example = "opennms")
            @PathParam("assetName") final String assetName) {
        final Optional<Collection<AssetResource>> resources = m_assetLocator.getResources(assetName);
        if (!resources.isPresent()) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        return new ArrayList<>(resources.get());
    }

    @GET
    @Path("{assetName}.{type}")
    @Operation(
            summary = "Fetch an asset file",
            description = """
        Serve the file registered for an asset name and type. The response media type is chosen from the
        `type` segment: `js` becomes `application/javascript`, `css` becomes `text/css`, image and font types
        get their own, `md`, `sh`, `txt`, `yml` and `jsp` come back as `text/plain`, `xml` as `text/xml`, and
        anything unrecognised as `application/octet-stream`.

        When the exact name is not registered, one fallback is tried: the last `-`-separated component of the
        name is dropped and, if that shorter name registers a file whose path is the originally requested
        one, that file is served.

        This operation depends on the built asset files being present on the classpath. Where they are not,
        the request fails with 500 even though the asset is listed.""",
            operationId = "getWebAssetFile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The asset file. The media type follows the `type` segment.",
                    content = @Content(schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "No file is registered for that asset name and type. The body is empty."),
            @ApiResponse(responseCode = "500", description = "The file is registered but could not be read.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "java.io.FileNotFoundException: class path resource [opennms.css] cannot be opened because it does not exist")))
    })
    public Response getResource(
            @Parameter(description = "Asset name, as listed by `GET /web-assets`.", required = true, example = "opennms")
            @PathParam("assetName") final String assetName,
            @Parameter(description = "File type, as reported by `GET /web-assets/{assetName}`.", required = true, example = "css")
            @PathParam("type") final String type) {
        InputStream is = null;

        try {
            Optional<InputStream> resourceInputStream = m_assetLocator.open(assetName, type);
            if (!resourceInputStream.isPresent()) {

                final List<String> split = Arrays.asList(assetName.split("-"));
                if (split.size() > 1) {
                    split.remove(split.size() - 1);
                    final String newAssetName = String.join("-", split);
                    final Optional<AssetResource> newResource = m_assetLocator.getResource(newAssetName, type);
                    LOG.debug("{}.{} not found, found {} instead", assetName, type, newResource);
                    if (newResource.isPresent() && newResource.get().getPath().equals(assetName + "." + type)) {
                        resourceInputStream = m_assetLocator.open(newAssetName, type);
                    }
                }
            }

            if (!resourceInputStream.isPresent()) {
                return Response.status(Status.NOT_FOUND).build();
            }

            is = resourceInputStream.get();
            final byte[] bytes = FileCopyUtils.copyToByteArray(is);
            switch(type) {
            // javascript
            case "js":
                return Response.ok(new String(bytes, StandardCharsets.UTF_8)).type("application/javascript").build();
            case "map":
            case "json":
                return streamResponse(bytes, "application/json");
            // styles
            case "css":
                return Response.ok(new String(bytes, StandardCharsets.UTF_8)).type("text/css").build();
            // images
            case "gif":
                return streamResponse(bytes, "image/gif");
            case "ico":
                return streamResponse(bytes, "image/x-icon");
            case "jpg":
            case "jpeg":
                return streamResponse(bytes, "image/jpeg");
            case "png":
                return streamResponse(bytes, "image/png");
            case "svg":
                return streamResponse(bytes, "image/svg+xml");
            // fonts
            case "eot":
                return streamResponse(bytes, "application/vnd.ms-fontobject");
            case "otf":
            case "ttf":
                return streamResponse(bytes, "application/octet-stream");
            case "woff":
                return streamResponse(bytes, "font/woff");
            case "woff2":
                return streamResponse(bytes, "font/woff2");
            // text
            case "html":
                return streamResponse(bytes, "text/html");
            case "jsp":
            case "md":
            case "sh":
            case "txt":
            case "yml":
                return Response.ok(new String(bytes, StandardCharsets.UTF_8)).type("text/plain").build();
            case "xml":
                return Response.ok(new String(bytes, StandardCharsets.UTF_8)).type("text/xml").build();
            // miscellaneous
            case "hbs":
                return streamResponse(bytes, "text/x-handlebars-template");
            default:
                LOG.warn("Unhandled type, returning as application/octet-stream: {}", type);
                return streamResponse(bytes, "application/octet-stream");
            }
        } catch (final IOException e) {
            LOG.debug("I/O error while reading {}.{}", assetName, type, e);
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity("Resource " + assetName + "/" + type + " exists, but could not be read.\n" + e.getMessage() + "\n" + e.getCause()).build());
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private Response streamResponse(final byte[] bytes, final String mimeType) {
        return Response.status(Status.OK).type(mimeType).entity(new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException, WebApplicationException {
                output.write(bytes);
                output.flush();
            }
        }).build();
    }
}
