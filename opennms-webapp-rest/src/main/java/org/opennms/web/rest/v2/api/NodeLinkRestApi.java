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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.opennms.web.rest.model.v2.BridgeLinkNodeDTO;
import org.opennms.web.rest.model.v2.CdpElementNodeDTO;
import org.opennms.web.rest.model.v2.CdpLinkNodeDTO;
import org.opennms.web.rest.model.v2.EnlinkdDTO;
import org.opennms.web.rest.model.v2.IsisElementNodeDTO;
import org.opennms.web.rest.model.v2.IsisLinkNodeDTO;
import org.opennms.web.rest.model.v2.LldpElementNodeDTO;
import org.opennms.web.rest.model.v2.LldpLinkNodeDTO;
import org.opennms.web.rest.model.v2.OspfElementNodeDTO;
import org.opennms.web.rest.model.v2.OspfLinkNodeDTO;

@Path("enlinkd")
public interface NodeLinkRestApi {

    @GET
    @Path("{nodeId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    EnlinkdDTO getEnlinkd(@PathParam("nodeId") String nodeId);

    @GET
    @Path("lldplinks/{nodeId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    List<LldpLinkNodeDTO> getLldpLinks(@PathParam("nodeId") String nodeId);

    @GET
    @Path("bridgelinks/{nodeId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    List<BridgeLinkNodeDTO> getBridgelinks(@PathParam("nodeId") String nodeId);

    @GET
    @Path("cdplinks/{nodeId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    List<CdpLinkNodeDTO> getCdpLinks(@PathParam("nodeId") String nodeId);

    @GET
    @Path("ospflinks/{nodeId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    List<OspfLinkNodeDTO> getOspfLinks(@PathParam("nodeId") String nodeId);

    @GET
    @Path("isislinks/{nodeId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    List<IsisLinkNodeDTO> getIsisLinks(@PathParam("nodeId") String nodeId);

    @GET
    @Path("lldpelems/{nodeId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    LldpElementNodeDTO getLldpelem(@PathParam("nodeId") String nodeId);

    @GET
    @Path("cdpelems/{nodeId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    CdpElementNodeDTO getCdpelem(@PathParam("nodeId") String nodeId);

    @GET
    @Path("ospfelems/{nodeId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    OspfElementNodeDTO getOspfelem(@PathParam("nodeId") String nodeId);

    @GET
    @Path("isiselems/{nodeId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    IsisElementNodeDTO getIsiselem(@PathParam("nodeId") String nodeId);
}
