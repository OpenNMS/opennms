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
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.netmgt.model.OnmsMetaDataList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.v2.NodeCategoriesRestService;
import org.opennms.web.rest.v2.NodeHardwareInventoryRestService;
import org.opennms.web.rest.v2.NodeIpInterfacesRestService;
import org.opennms.web.rest.v2.NodeSnmpInterfacesRestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("nodes")
public interface NodeRestApi {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get all nodes", description = "Get all nodes", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "204", description = "No nodes found",
                    content = @Content)
    })
    Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get the nodes specified by the given ID", description = "Get the nodes specified by the given ID", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "204", description = "No nodes found",
                    content = @Content)
    })
    Response get(@Context final UriInfo uriInfo, @PathParam("id") final String id) ;

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(summary = "Get total count of nodes", description = "Get total count of nodes", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content)
    })
    Response getCount(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get all nodes properties", description = "Get all nodes properties", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "204", description = "No nodes properties found",
                    content = @Content)
    })
    Response getProperties(@QueryParam("q") final String query) ;

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get the nodes properties specified by the given ID", description = "Get the nodes properties specified by the given ID", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No nodes properties found",
                    content = @Content)
    })
    Response getPropertyValues(@PathParam("propertyId") final String propertyId, @QueryParam("q") final String query, @QueryParam("limit") final Integer limit) ;

    @Path("{nodeCriteria}/ipinterfaces")
    @Operation(summary = "Get all IP interfaces associated by the given node", description = "Get all IP interfaces associated by the given node", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "204", description = "No IP interfaces found",
                    content = @Content)
    })
    public NodeIpInterfacesRestService getIpInterfaceResource(@Context final ResourceContext context) ;

    @Path("{nodeCriteria}/snmpinterfaces")
    @Operation(summary = "Get all SNMP interfaces associated by the given node", description = "Get all SNMP interfaces associated by the given node" , tags = {"Nodes"})
    public NodeSnmpInterfacesRestService getSnmpInterfaceResource(@Context final ResourceContext context) ;

    @Path("{nodeCriteria}/hardwareInventory")
    @Operation(summary = "Get all hardware inventory associated by the given node", description = "Get all hardware inventory associated by the given node" , tags = {"Nodes"})
    public NodeHardwareInventoryRestService getHardwareInventoryResource(@Context final ResourceContext context) ;

    @Path("{nodeCriteria}/categories")
    @Operation(summary = "Get all categories by the given node", description = "Get all categories by the given node", tags = {"Nodes"})
    public NodeCategoriesRestService getCategoriesResource(@Context final ResourceContext context);

    @GET
    @Path("{nodeCriteria}/metadata")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get all metadata by the given node", description = "Get all metadata by the given node", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Node is null",
                    content = @Content)
    })
    OnmsMetaDataList getMetaData(@PathParam("nodeCriteria") String nodeCriteria);

    @GET
    @Path("{nodeCriteria}/metadata/{context}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get the metadata specified by the given node context", description = "Get the metadata specified by the given node context", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Node is null",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No metadata found",
                    content = @Content)
    })
    OnmsMetaDataList getMetaData(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("context") String context);

    @GET
    @Path("{nodeCriteria}/metadata/{context}/{key}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get the metadata specified by the given node context key", description = "Get the metadata specified by the given node context key", tags = {"Nodes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Node is null",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No metadata found",
                    content = @Content)
    })
    OnmsMetaDataList getMetaData(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("context") String context, @PathParam("key") String key);

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Add a node", description = "Add a node")
    @ApiResponses(value = {

            @ApiResponse(responseCode = "201", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Node is null",
                    content = @Content)
    })
    Response create(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, OnmsNode object) ;

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Modify nodes", description = "Modify nodes", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "204", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No nodes found",
                    content = @Content)
    })
    Response updateMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext, final MultivaluedMapImpl params) ;

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{id}")
    @Operation(summary = "Modify the node specified by the given ID", description = "Modify the node specified by the given ID", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "204", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No nodes found",
                    content = @Content)
    })
    Response updateProperties(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final String id, final MultivaluedMapImpl params) ;

    @DELETE
    @Operation(summary = " Delete nodes", description = "Delete nodes", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "204", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No nodes found",
                    content = @Content)
    })
    Response deleteMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @DELETE
    @Path("{id}")
    @Operation(summary = "Delete the node specified by the given ID", description = "Delete the node specified by the given ID", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "204", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No nodes found",
                    content = @Content)
    })
    Response delete(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final String id) ;
}
