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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.topology.assets.TopologyAsset;
import org.opennms.netmgt.topology.assets.TopologyAssetDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Upload, serve, list, and delete the images that custom topology views
 * reference: view backgrounds (floor plans, rack diagrams) and custom node
 * icons. Metadata travels as JSON; the bytes are posted/served raw under the
 * image's own content type — no multipart.
 *
 * <p>Reads are open to any authenticated user; writes (POST/DELETE) require
 * {@code ROLE_REST} or {@code ROLE_ADMIN} — enforced by the default
 * {@code /api/v2/**} rules in the Spring Security configuration, so the
 * resource carries no role annotations (same model as the topology views).
 *
 * <p>Only raster image types are accepted. SVG is deliberately excluded for
 * now: it can carry active content, and these assets are served from the
 * application origin; revisit with a sandboxed serving path if needed. Each
 * {@code kind} has its own size cap — icons are reused per node and should be
 * small, backgrounds are floor-plan-sized.
 */
@Component
@Path("topology/assets")
@Tag(name = "TopologyAssets", description = "Topology view image assets API")
public class TopologyAssetRestService {

    private static final Map<String, Long> MAX_SIZE_BY_KIND = ImmutableMap.of(
            TopologyAsset.KIND_BACKGROUND, 10L * 1024 * 1024,
            TopologyAsset.KIND_ICON, 512L * 1024);

    static final String[] ALLOWED_MIME_TYPES = {"image/png", "image/jpeg", "image/gif", "image/webp"};

    private static final String ASSET_ID_DESC =
            "The asset's generated id, as returned by the upload or the listing (a UUID on this build).";

    private static final String ASSET_STORE_UNAVAILABLE =
            "The asset store bean is not present in this deployment.";

    @Autowired(required = false)
    private TopologyAssetDao m_dao;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List topology image assets",
            description = """
        Metadata for every stored asset. The image bytes are not included; they are served from
        `/topology/assets/{id}`. `created` and `lastModified` are epoch milliseconds.""",
            operationId = "listTopologyAssets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asset metadata, empty when nothing matches.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = TopologyAssetDTO.class)),
                            examples = @ExampleObject(value = """
                    [
                      {
                        "id": "defb7a91-9287-459a-ba1d-6879fee1cbfd",
                        "name": "rack-elevation",
                        "kind": "background",
                        "mimeType": "image/png",
                        "sizeBytes": 36667,
                        "owner": "admin",
                        "created": 1787727367630,
                        "lastModified": 1787727367630
                      }
                    ]"""))),
            @ApiResponse(responseCode = "503", description = ASSET_STORE_UNAVAILABLE,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "The topology asset store is not available")))
    })
    public List<TopologyAssetDTO> list(@Parameter(description = """
                    Restrict the listing to one kind. An unrecognised value matches nothing and
                    yields an empty array.""",
                    example = "icon",
                    schema = @Schema(allowableValues = {"background", "icon"}))
                                       @QueryParam("kind") final String kind) {
        return getDao().findAll().stream()
                .filter(asset -> kind == null || kind.equals(asset.getKind()))
                .map(TopologyAssetRestService::toDto)
                .collect(Collectors.toList());
    }

    @POST
    @Consumes({"image/png", "image/jpeg", "image/gif", "image/webp"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Upload a topology image asset",
            description = """
        The request body is the raw image, not a multipart form; the `Content-Type` header records the
        stored MIME type. `name` and `kind` travel as query parameters.

        Size caps are per kind: 10485760 bytes for a `background`, 524288 for an `icon`. Only
        `image/png`, `image/jpeg`, `image/gif` and `image/webp` are accepted; `image/svg+xml` is
        rejected at the media-type layer. The `owner` recorded on the asset is the authenticated
        principal.

        Names carry no uniqueness constraint: uploading the same name twice stores two assets with
        different ids.
        The 201 carries a `Location` header pointing at the new asset's byte URL.""",
            operationId = "uploadTopologyAsset")
    @RequestBody(required = true, description = "The raw image bytes.",
            content = {
                    @Content(mediaType = "image/png", schema = @Schema(type = "string", format = "binary")),
                    @Content(mediaType = "image/jpeg", schema = @Schema(type = "string", format = "binary")),
                    @Content(mediaType = "image/gif", schema = @Schema(type = "string", format = "binary")),
                    @Content(mediaType = "image/webp", schema = @Schema(type = "string", format = "binary"))
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Asset stored. `Location` holds its byte URL.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TopologyAssetDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "id": "defb7a91-9287-459a-ba1d-6879fee1cbfd",
                      "name": "core-switch",
                      "kind": "icon",
                      "mimeType": "image/png",
                      "sizeBytes": 67,
                      "owner": "admin",
                      "created": 1787727367630,
                      "lastModified": 1787727367630
                    }"""))),
            @ApiResponse(responseCode = "400", description = "`name` is missing or blank, `kind` is missing or unrecognised, or the body is empty.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "An asset kind is required (?kind=): one of [background, icon]"))),
            @ApiResponse(responseCode = "413", description = "The body exceeds the cap for this kind.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "A icon asset may be at most 524288 bytes"))),
            @ApiResponse(responseCode = "415", description = "The `Content-Type` is absent or is not one of the four accepted image types."),
            @ApiResponse(responseCode = "503", description = ASSET_STORE_UNAVAILABLE,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "The topology asset store is not available")))
    })
    public Response upload(@Context final UriInfo uriInfo,
                           @Context final SecurityContext securityContext,
                           @Context final javax.ws.rs.core.HttpHeaders headers,
                           @Parameter(description = "Display name for the asset. Trimmed; must not be blank.",
                                   required = true, example = "core-switch")
                           @QueryParam("name") final String name,
                           @Parameter(description = "Which cap and role the asset takes on.", required = true,
                                   example = "icon", schema = @Schema(allowableValues = {"background", "icon"}))
                           @QueryParam("kind") final String kind,
                           final byte[] bytes) {
        if (name == null || name.trim().isEmpty()) {
            throw webException(Response.Status.BAD_REQUEST, "An asset name is required (?name=)");
        }
        final Long maxSize = kind == null ? null : MAX_SIZE_BY_KIND.get(kind);
        if (maxSize == null) {
            throw webException(Response.Status.BAD_REQUEST,
                    "An asset kind is required (?kind=): one of " + MAX_SIZE_BY_KIND.keySet());
        }
        if (bytes == null || bytes.length == 0) {
            throw webException(Response.Status.BAD_REQUEST, "The request body must be the image bytes");
        }
        if (bytes.length > maxSize) {
            throw webException(Response.Status.REQUEST_ENTITY_TOO_LARGE,
                    "A " + kind + " asset may be at most " + maxSize + " bytes");
        }
        // @Consumes already gates the type; record it without any parameters.
        // A request with no Content-Type at all can still reach here, though --
        // answer 415 rather than NPE into a 500.
        final MediaType contentType = headers.getMediaType();
        if (contentType == null) {
            throw webException(Response.Status.UNSUPPORTED_MEDIA_TYPE,
                    "A Content-Type header with the image type is required");
        }
        final String mimeType = contentType.getType() + "/" + contentType.getSubtype();

        final TopologyAsset asset = new TopologyAsset();
        asset.setName(name.trim());
        asset.setKind(kind);
        asset.setMimeType(mimeType);
        asset.setOwner(securityContext.getUserPrincipal() == null ? null : securityContext.getUserPrincipal().getName());

        final TopologyAsset saved = getDao().save(asset, bytes);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(saved.getId()).build())
                .entity(toDto(saved))
                .build();
    }

    /**
     * The image bytes, under the asset's own content type. ETag'd by the
     * asset's last-modified time so browsers can revalidate cheaply — icons
     * especially are reused by many nodes in a view.
     */
    @GET
    @Path("{id}")
    @Operation(summary = "Get a topology asset's image bytes",
            description = """
        Serves the stored bytes under the asset's own `Content-Type`, with `Cache-Control: max-age=3600`
        and an `ETag` derived from the asset's last-modified time. A conditional request carrying a
        matching `If-None-Match` answers 304 with no body.""",
            operationId = "getTopologyAssetBytes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The image, under its stored MIME type.",
                    content = @Content(mediaType = "image/png", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "304", description = "`If-None-Match` matched the current ETag."),
            @ApiResponse(responseCode = "404", description = "No such asset, or the metadata row has no bytes behind it.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No asset with id \'defb7a91-9287-459a-ba1d-6879fee1cbfd\'"))),
            @ApiResponse(responseCode = "503", description = ASSET_STORE_UNAVAILABLE,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response getBytes(@Context final Request request,
                             @Parameter(description = ASSET_ID_DESC, required = true,
                                     example = "defb7a91-9287-459a-ba1d-6879fee1cbfd")
                             @PathParam("id") final String id) {
        final TopologyAsset asset = require(id);
        final EntityTag etag = new EntityTag(String.valueOf(asset.getLastModified() == null ? 0L : asset.getLastModified().getTime()));

        final CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge(3600);

        final Response.ResponseBuilder notModified = request.evaluatePreconditions(etag);
        if (notModified != null) {
            return notModified.cacheControl(cacheControl).tag(etag).build();
        }

        final Optional<byte[]> bytes = getDao().getBytes(id);
        if (!bytes.isPresent()) {
            throw webException(Response.Status.NOT_FOUND, "No bytes stored for asset '" + id + "'");
        }
        return Response.ok(bytes.get(), asset.getMimeType())
                .cacheControl(cacheControl)
                .tag(etag)
                .build();
    }

    @GET
    @Path("{id}/meta")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a topology asset's metadata",
            description = "One asset's metadata without transferring the image. `created` and `lastModified` are epoch milliseconds.",
            operationId = "getTopologyAssetMeta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The asset's metadata.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TopologyAssetDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "id": "defb7a91-9287-459a-ba1d-6879fee1cbfd",
                      "name": "core-switch",
                      "kind": "icon",
                      "mimeType": "image/png",
                      "sizeBytes": 67,
                      "owner": "admin",
                      "created": 1787727367630,
                      "lastModified": 1787727367630
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No asset with that id.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No asset with id \'defb7a91-9287-459a-ba1d-6879fee1cbfd\'"))),
            @ApiResponse(responseCode = "503", description = ASSET_STORE_UNAVAILABLE,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public TopologyAssetDTO getMeta(@Parameter(description = ASSET_ID_DESC, required = true,
                                            example = "defb7a91-9287-459a-ba1d-6879fee1cbfd")
                                    @PathParam("id") final String id) {
        return toDto(require(id));
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Delete a topology asset",
            description = """
        Removes the metadata and the bytes together. Views that referenced the asset are not rewritten,
        so a background or icon reference can be left dangling.""",
            operationId = "deleteTopologyAsset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Asset deleted."),
            @ApiResponse(responseCode = "404", description = "No asset with that id.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No asset with id \'defb7a91-9287-459a-ba1d-6879fee1cbfd\'"))),
            @ApiResponse(responseCode = "503", description = ASSET_STORE_UNAVAILABLE,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response delete(@Parameter(description = ASSET_ID_DESC, required = true,
                                   example = "defb7a91-9287-459a-ba1d-6879fee1cbfd")
                           @PathParam("id") final String id) {
        if (!getDao().delete(id)) {
            throw webException(Response.Status.NOT_FOUND, "No asset with id '" + id + "'");
        }
        return Response.noContent().build();
    }

    private TopologyAsset require(final String id) {
        return getDao().get(id)
                .orElseThrow(() -> webException(Response.Status.NOT_FOUND, "No asset with id '" + id + "'"));
    }

    private TopologyAssetDao getDao() {
        if (m_dao == null) {
            throw webException(Response.Status.SERVICE_UNAVAILABLE, "The topology asset store is not available");
        }
        return m_dao;
    }

    private static TopologyAssetDTO toDto(final TopologyAsset asset) {
        final TopologyAssetDTO dto = new TopologyAssetDTO();
        dto.setId(asset.getId());
        dto.setName(asset.getName());
        dto.setKind(asset.getKind());
        dto.setMimeType(asset.getMimeType());
        dto.setSizeBytes(asset.getSizeBytes());
        dto.setOwner(asset.getOwner());
        dto.setCreated(asset.getCreated());
        dto.setLastModified(asset.getLastModified());
        return dto;
    }

    private static WebApplicationException webException(final Response.Status status, final String message) {
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(message).build());
    }
}
