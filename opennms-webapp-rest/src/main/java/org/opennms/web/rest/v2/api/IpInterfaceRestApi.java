/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.web.rest.support.DateCollection;
import org.opennms.web.rest.support.FloatCollection;
import org.opennms.web.rest.support.IntegerCollection;
import org.opennms.web.rest.support.LongCollection;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchPropertyCollection;
import org.opennms.web.rest.support.StringCollection;

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

@Path("ipinterfaces")
public interface IpInterfaceRestApi {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get all IP Interfaces",
            description = "Get all IP Interfaces",
            tags = {"IP interfaces"},
            responses = {
                    @ApiResponse(
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = OnmsIpInterface.class ))),
                            responseCode = "200",
                            description = "Successful operation"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No IP interfaces found"
                    )
            }
    )
    Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext);

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Not implemented",
            description = "Method not implemented",
            tags = {"IP interfaces"},
            responses = {
                    @ApiResponse(responseCode = "501", description = "Not implemented method")
            }
    )
    Response updateMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext, final MultivaluedMapImpl params);

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Not implemented",
            description = "Method not implemented",
            tags = {"IP interfaces"},
            responses = {
                    @ApiResponse(responseCode = "501", description = "Not implemented method")
            }
    )
    Response create(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, OnmsIpInterface object);

    @DELETE
    @Operation(summary = "Not implemented",
            description = "Method not implemented",
            tags = {"IP interfaces"},
            responses = {
                    @ApiResponse(responseCode = "404", description = "No IP interfaces found"),
                    @ApiResponse(responseCode = "501", description = "Not implemented method")
            }
    )
    Response deleteMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext);

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get IP Interfaces specified by IpAddress",
            description = "Get IP Interfaces specified by IPAddress or by using FIQL query",
            tags = {"IP interfaces"},
            parameters = { @Parameter(required = true, name = "id", description = "IP interface id", in = ParameterIn.PATH)},
            responses = {
                    @ApiResponse(
                            content = @Content(schema = @Schema(implementation = OnmsIpInterface.class)),
                            responseCode = "200",
                            description = "Successful operation"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No IP interfaces found"
                    )
            }
    )
    Response get(@Context final UriInfo uriInfo, @PathParam("id") final String id);

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{id}")
    @Operation(summary = "Not implemented",
            description = "Method not implemented",
            tags = {"IP interfaces"},
            responses = {
                    @ApiResponse(responseCode = "501", description = "Not implemented method")
            }
    )
    Response update(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final Integer id, final OnmsIpInterface object);

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{id}")
    @Operation(summary = "Not implemented",
            description = "Method not implemented",
            tags = {"IP interfaces"},
            responses = {
                    @ApiResponse(responseCode = "501", description = "Not implemented method")
            }
    )
    Response updateProperties(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final String id, final MultivaluedMapImpl params);

    @POST
    @Path("{id}")
    @Operation(summary = "Not implemented",
            description = "Method not implemented",
            tags = {"IP interfaces"},
            responses = {
                    @ApiResponse(responseCode = "404", description = "No IP interfaces found")
            }
    )
    Response createSpecific();

    @DELETE
    @Path("{id}")
    @Operation(summary = "Not implemented",
            description = "Method not implemented",
            tags = {"IP interfaces"},
            parameters = {@Parameter(required = true, name = "id", description = "IP interface id")},
            responses = {
                    @ApiResponse(responseCode = "404", description = "No IP interfaces found"),
                    @ApiResponse(responseCode = "501", description = "Not implemented method")
            }
    )
    Response delete(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final String id);

    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get all search properties for IP interface",
            description = "Get all search properties for IP interface",
            tags = {"IP interfaces"},
            parameters = {@Parameter(name= "q", description = "query")},
            responses = {
                    @ApiResponse(
                            content = @Content(schema = @Schema(implementation = SearchPropertyCollection.class)),
                            responseCode = "200",
                            description = "Successful operation"
                    ),
                    @ApiResponse(responseCode = "204", description = "No content"),
                    @ApiResponse(responseCode = "404", description = "No property found for any IP interface")
            }
    )
    Response getProperties(@QueryParam("q") final String query);

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a property values",
            description = "Get a distinct list of values for the specified property in the IP interfaces",
            tags = {"IP interfaces"},
            parameters = {
                    @Parameter(required = true, name = "propertyId", description = "property id", in = ParameterIn.PATH),
                    @Parameter(name = "q", description = "query", in = ParameterIn.QUERY),
                    @Parameter(name = "limit", description = "limit results", in = ParameterIn.QUERY)
            },
            responses = {
                    @ApiResponse(
                            content = @Content(
                                    schema = @Schema(
                                            implementation = JaxbListWrapper.class,
                                            anyOf = {
                                                    IntegerCollection.class,
                                                    FloatCollection.class,
                                                    LongCollection.class,
                                                    StringCollection.class,
                                                    DateCollection.class
                                            })),
                            responseCode = "200",
                            description = "Successful operation"
                    ),
                    @ApiResponse(responseCode = "204", description = "No content"),
                    @ApiResponse(responseCode = "404", description = "No property found for any IP interface")
            }
    )
    Response getPropertyValues(@PathParam("propertyId") final String propertyId, @QueryParam("q") final String query, @QueryParam("limit") final Integer limit);

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(summary = "Total count of IP Interfaces",
            description = "Total count of IP Interfaces",
            tags = {"IP interfaces"},
            responses = {
                    @ApiResponse(
                            content = @Content(schema = @Schema(implementation = Integer.class)),
                            responseCode = "200",
                            description = "Successful operation"
                    )
            }
    )
    Response getCount(@Context final UriInfo uriInfo, @Context final SearchContext searchContext);

}
