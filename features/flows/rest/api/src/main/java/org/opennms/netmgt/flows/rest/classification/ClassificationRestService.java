/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.rest.classification;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("classifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ClassificationRestService {

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    Response getRules(@Context final UriInfo uriInfo);

    @GET
    @Path("{id}")
    Response getRule(@PathParam("id") int id);

    @POST
    Response saveRule(RuleDTO ruleDTO);

    @DELETE
    Response deleteRules(@Context final UriInfo uriInfo);

    @DELETE
    @Path("{id}")
    Response deleteRule(@PathParam("id") int id);

    @PUT
    @Path("{id}")
    Response updateRule(@PathParam("id") int id, RuleDTO newValue);

    @POST
    @Path("classify")
    Response classify(ClassificationRequestDTO classificationRequestDTO);

    @GET
    @Path("groups")
    Response getGroups(@Context final UriInfo uriInfo);

    @GET
    @Path("groups/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/comma-separated-values"})
    Response getGroup(@PathParam("id") int groupId,
                      @QueryParam("format") String format,
                      @QueryParam("filename") String requestedFilename,
                      @HeaderParam("Accept") String acceptHeader);

    @DELETE
    @Path("groups/{id}")
    Response deleteGroup(int groupId);

    @PUT
    @Path("groups/{id}")
    Response updateGroup(@PathParam("id") int id, GroupDTO newValue);

    @POST
    @Consumes("text/comma-separated-values")
    Response importRules(@Context UriInfo uriInfo, InputStream inputStream);

    @GET
    @Path("protocols")
    Response getProtocols();
}
