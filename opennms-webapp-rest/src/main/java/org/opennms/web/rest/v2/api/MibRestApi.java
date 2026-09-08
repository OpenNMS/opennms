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
package org.opennms.web.rest.v2.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("mibs")
@Tag(name = "Mibs", description = "MIB Compiler API")
public interface MibRestApi {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "List MIB files",
            description = "List the MIB files in the pending and compiled directories.",
            operationId = "listMibFiles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MIB files retrieved successfully", content = @Content)
    })
    Response listMibFiles();

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Upload MIB files",
            description = "Upload one or more MIB files into the pending directory. "
                    + "Files whose name already exists in the pending or compiled directory are rejected per file.",
            operationId = "uploadMibFiles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Per-file success/error report", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    })
    Response uploadMibFiles(@Multipart("upload") List<Attachment> attachments);

    @GET
    @Path("/{dir}/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Get MIB file content",
            description = "Return the raw text content of a pending or compiled MIB file.",
            operationId = "getMibFileContent"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Raw MIB content", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid directory or file name", content = @Content),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content)
    })
    Response getMibFileContent(@PathParam("dir") String dir, @PathParam("name") String name);

    @PUT
    @Path("/pending/{name}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Update a pending MIB file",
            description = "Replace the raw text content of a pending MIB file. Compiled MIB files are read-only.",
            operationId = "updatePendingMibFile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File updated", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid file name or content too large", content = @Content),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content)
    })
    Response updatePendingMibFile(@PathParam("name") String name, String content);

    @DELETE
    @Path("/{dir}/{name}")
    @Operation(
            summary = "Delete a MIB file",
            description = "Delete a pending or compiled MIB file.",
            operationId = "deleteMibFile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "File deleted", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid directory or file name", content = @Content),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content)
    })
    Response deleteMibFile(@PathParam("dir") String dir, @PathParam("name") String name);

    @POST
    @Path("/pending/{name}/compile")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Compile a pending MIB file",
            description = "Parse the pending MIB against the compiled MIB directory. On success the file is moved "
                    + "to the compiled directory as <MibName>.mib. On parse failure the response carries the "
                    + "formatted errors and any missing dependencies.",
            operationId = "compileMibFile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compilation result (success or parse errors)", content = @Content),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "A compiled MIB with the same name exists and overwrite=false", content = @Content)
    })
    Response compileMibFile(@PathParam("name") String name,
                            @QueryParam("overwrite") @DefaultValue("false") boolean overwrite);

    @POST
    @Path("/compiled/{name}/events")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Generate event definitions from a compiled MIB",
            description = "Parse the compiled MIB and generate event definitions for its notifications/traps. "
                    + "Returns the generated events document as XML for review; nothing is persisted. "
                    + "Persist the (possibly edited) XML via POST /eventconf/upload.",
            operationId = "generateMibEvents"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Generated events (or parse errors)", content = @Content),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content)
    })
    Response generateEvents(@PathParam("name") String name, @QueryParam("ueiBase") String ueiBase);

    @POST
    @Path("/compiled/{name}/datacollection")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Generate a data collection group from a compiled MIB",
            description = "Parse the compiled MIB and generate a data collection group for its variables. "
                    + "Returns the generated datacollection-group as XML for review; nothing is persisted. "
                    + "Persist the (possibly edited) XML via POST /datacollectionconf/upload.",
            operationId = "generateMibDataCollection"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Generated data collection group (or parse errors)", content = @Content),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content)
    })
    Response generateDataCollection(@PathParam("name") String name);

    @POST
    @Path("/compiled/{name}/graph-templates")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Generate graph templates from a compiled MIB",
            description = "Parse the compiled MIB and generate prefab graph templates. With dryRun=true (default) "
                    + "the generated content is only returned for review; with dryRun=false it is also written to "
                    + "etc/snmp-graph.properties.d/<MibName>-graph.properties.",
            operationId = "generateMibGraphTemplates"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Generated graph templates (or parse errors)", content = @Content),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content)
    })
    Response generateGraphTemplates(@PathParam("name") String name,
                                    @QueryParam("dryRun") @DefaultValue("true") boolean dryRun);
}
