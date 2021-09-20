/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2021 The OpenNMS Group, Inc.
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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.SearchContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("ipinterfaces")
public interface IpInterfaceRestApi  {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get all IP Interfaces", description = "Get all IP Interfaces", tags = {"IP interfaces"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                        content = @Content),
            @ApiResponse(responseCode = "204", description = "No IP interfaces found",
                        content = @Content)
    })
    Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get IP Interfaces specified by IpAddress", description = "Get IP Interfaces specified by IPAddress or by using FIQL query", tags = {"IP interfaces"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "204", description = "No IP interfaces found",
                    content = @Content)
    })
    Response get(@Context final UriInfo uriInfo, @PathParam("id") final String id) ;

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(summary = "Get total count of IP Interfaces", description = "Get total count of IP Interfaces", tags = {"IP interfaces"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content)
    })
    Response getCount(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get IP Interfaces properties", description = "Get IP Interfaces properties")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "204", description = "No IP interface properties found",
                    content = @Content)
    })
    Response getProperties(@QueryParam("q") final String query) ;

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get IP Interfaces properties specified by query and propertyId", description = "Get IP Interfaces properties specified by query and propertyId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No IP interface properties found",
                    content = @Content)
    })
    Response getPropertyValues(@PathParam("propertyId") final String propertyId, @QueryParam("q") final String query, @QueryParam("limit") final Integer limit) ;
}
