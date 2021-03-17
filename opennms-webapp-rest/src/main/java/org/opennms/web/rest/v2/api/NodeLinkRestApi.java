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

import org.opennms.web.rest.model.v2.BridgeElementNodeDTO;
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
    @Path("{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    EnlinkdDTO getEnlinkd(@PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("lldp_links/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    List<LldpLinkNodeDTO> getLldpLinks(@PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("bridge_links/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    List<BridgeLinkNodeDTO> getBridgeLinks(@PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("cdp_links/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    List<CdpLinkNodeDTO> getCdpLinks(@PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("ospf_links/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    List<OspfLinkNodeDTO> getOspfLinks(@PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("isis_links/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    List<IsisLinkNodeDTO> getIsisLinks(@PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("lldp_elems/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    LldpElementNodeDTO getLldpElem(@PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("bridge_elems/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    List<BridgeElementNodeDTO> getBridgeElem(@PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("cdp_elems/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    CdpElementNodeDTO getCdpElem(@PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("ospf_elems/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    OspfElementNodeDTO getOspfElem(@PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("isis_elems/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    IsisElementNodeDTO getIsisElem(@PathParam("node_criteria") String nodeCriteria);
}
