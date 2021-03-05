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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("enlinkd")
public interface EnhanceLinkdRestApi {

    @GET
    @Path("lldplinks/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    Response getLldpLinks(@PathParam("nodeId") int nodeId);

    @GET
    @Path("bridgelinks/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    Response getBridgelinks(@PathParam("nodeId") int nodeId);

    @GET
    @Path("cdplinks/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    Response getCdpLinks(@PathParam("nodeId") int nodeId);

    @GET
    @Path("ospflinks/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    Response getOspfLinks(@PathParam("nodeId") int nodeId);

    @GET
    @Path("isislinks/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    Response getIsisLinks(@PathParam("nodeId") int nodeId);

    @GET
    @Path("lldpelem/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    Response getLldpelem(@PathParam("nodeId") int nodeId);

    @GET
    @Path("cdpelem/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    Response getCdpelem(@PathParam("nodeId") int nodeId);

    @GET
    @Path("ospfelem/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    Response getOspfelem(@PathParam("nodeId") int nodeId);

    @GET
    @Path("isiselem/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    Response getIsiselem(@PathParam("nodeId") int nodeId);
}
