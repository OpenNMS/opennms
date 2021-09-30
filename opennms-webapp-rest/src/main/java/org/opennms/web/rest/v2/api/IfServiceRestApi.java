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
import javax.ws.rs.GET;
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
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.web.rest.support.MultivaluedMapImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("ifservices")
public interface IfServiceRestApi {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get all If Services", description = "Get all If Services", tags = {"If Services"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "204", description = "No If services found",
                    content = @Content)
    })
    Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(summary = "Get total count of If Services", description = "Get total count of If Services", tags = {"If Services"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content)
    })
    Response getCount(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get all If Services properties", description = "Get all If Services properties")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "204", description = "No If service properties found",
                    content = @Content)
    })
    Response getProperties(@QueryParam("q") final String query) ;

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get If Services properties specified by query and propertyId", description = "Get the If Services properties with a given query and propertyId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No If service properties found",
                    content = @Content)
    })
    Response getPropertyValues(@PathParam("propertyId") final String propertyId, @QueryParam("q") final String query, @QueryParam("limit") final Integer limit) ;

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Create many If services", description = "Create many If services")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad input request",
                    content = @Content)
    })
    Response updateMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext, final MultivaluedMapImpl params) ;

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{id}")
    @Operation(summary = "Update  an If service", description = "Update  an If service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No If services found",
                    content = @Content)
    })
    Response update(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final Integer id, final OnmsMonitoredService object);
}
