/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v2.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("minions")
public interface MinionRestApi {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get all minions", description = "Get all minions", tags = {"Minions"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Minion not found",
                    content = @Content)
    })
    Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(summary = "Get minions count", description = "Get minions count", tags = {"Minions"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Minion not found",
                    content = @Content)
    })
    Response getCount(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get the properties", description = "Get all types of properties", tags = {"Minions"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Minions properties not found",
                    content = @Content)
    })
    Response getProperties(@QueryParam("q") final String query) ;

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a properties ", description = "Get a properties  for a specific propertyId", tags = {"Minions"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Minions property not found",
                    content = @Content)
    })
    Response getPropertyValues(@PathParam("propertyId") final String propertyId, @QueryParam("q") final String query, @QueryParam("limit") final Integer limit) ;

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get minion by id", description = "Get minion of id", tags = {"Minions"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Minion id not found",
                    content = @Content)
    })
    Response get(@Context final UriInfo uriInfo, @PathParam("id") final String id) ;

    @POST
    @Path("{id}")
    @Operation(summary = "Create a minion", description = "Create a minion by id", tags = {"Minions"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "Bad input request",
                    content = @Content)
    })
    Response createSpecific() ;

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Create a minion", description = "Create a minion", tags = {"Minions"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "Bad input request",
                    content = @Content)
    })
    Response create(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, OnmsMinion object) ;

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Create minions", description = "Create minions", tags = {"Minions"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "Bad input request",
                    content = @Content)
    })
    Response updateMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext, final MultivaluedMapImpl params) ;

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{id}")
    @Operation(summary = "Update a minion", description = "Update a minion by id", tags = {"Minions"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Minion not found",
                    content = @Content)
    })
    Response update(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final String id, final OnmsMinion object);

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{id}")
    @Operation(summary = "Update a minion properties", description = "Update a minion properties by id", tags = {"Minions"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Minion not found",
                    content = @Content)
    })
    Response updateProperties(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final String id, final MultivaluedMapImpl params) ;

    @DELETE
    @Operation(summary = " Delete minions", description = "Delete minions", tags = {"Minions"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Minions not found",
                    content = @Content)
    })
    Response deleteMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @DELETE
    @Path("{id}")
    @Operation(summary = "Delete a minion by id", description = "Delete a minion by id", tags = {"Minions"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Minion not found",
                    content = @Content)
    })
    Response delete(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final String id) ;


}
