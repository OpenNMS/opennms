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

package org.opennms.web.rest.v2;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.web.enlinkd.BridgeLinkNode;
import org.opennms.web.enlinkd.CdpElementNode;
import org.opennms.web.enlinkd.CdpLinkNode;
import org.opennms.web.enlinkd.EnLinkdElementFactoryInterface;
import org.opennms.web.enlinkd.IsisElementNode;
import org.opennms.web.enlinkd.IsisLinkNode;
import org.opennms.web.enlinkd.LldpElementNode;
import org.opennms.web.enlinkd.LldpLinkNode;
import org.opennms.web.enlinkd.OspfElementNode;
import org.opennms.web.enlinkd.OspfLinkNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("enlinkd")
@Transactional
public class EnhanceLinkdRestService {
    @Autowired
    private EnLinkdElementFactoryInterface enLinkdElementFactory;

    @Autowired
    private NodeDao m_nodeDao;

    @GET
    @Path("lldplinks/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getLldpLinks(@PathParam("nodeId") int nodeId) {
        checkNodeInDB(nodeId);
        Collection<LldpLinkNode> lldpLinks = enLinkdElementFactory.getLldpLinks(nodeId);
        return Response.ok(lldpLinks).build();
    }

    @GET
    @Path("bridgelinks/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getBridgelinks(@PathParam("nodeId") int nodeId) {
        checkNodeInDB(nodeId);
        Collection<BridgeLinkNode> bridgelinks = enLinkdElementFactory.getBridgeLinks(nodeId);
        return Response.ok(bridgelinks).build();
    }

    @GET
    @Path("cdplinks/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCdpLinks(@PathParam("nodeId") int nodeId) {
        checkNodeInDB(nodeId);
        Collection<CdpLinkNode> cdpLinks = enLinkdElementFactory.getCdpLinks(nodeId);
        return Response.ok(cdpLinks).build();
    }

    @GET
    @Path("ospflinks/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getOspfLinks(@PathParam("nodeId") int nodeId) {
        checkNodeInDB(nodeId);
        Collection<OspfLinkNode> ospfLinks = enLinkdElementFactory.getOspfLinks(nodeId);
        return Response.ok(ospfLinks).build();
    }

    @GET
    @Path("isislinks/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getIsisLinks(@PathParam("nodeId") int nodeId) {
        checkNodeInDB(nodeId);
        Collection<IsisLinkNode> isisLinks = enLinkdElementFactory.getIsisLinks(nodeId);
        return Response.ok(isisLinks).build();
    }

    @GET
    @Path("lldpelem/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getLldpelem(@PathParam("nodeId") int nodeId) {
        checkNodeInDB(nodeId);
        LldpElementNode lldpelem = enLinkdElementFactory.getLldpElement(nodeId);
        return Response.ok(lldpelem).build();
    }

    @GET
    @Path("cdpelem/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCdpelem(@PathParam("nodeId") int nodeId) {
        checkNodeInDB(nodeId);
        CdpElementNode cdpelem = enLinkdElementFactory.getCdpElement(nodeId);
        return Response.ok(cdpelem).build();
    }

    @GET
    @Path("ospfelem/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getOspfelem(@PathParam("nodeId") int nodeId) {
        checkNodeInDB(nodeId);
        OspfElementNode ospfelem = enLinkdElementFactory.getOspfElement(nodeId);
        return Response.ok(ospfelem).build();
    }

    @GET
    @Path("isiselem/{nodeId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getIsiselem(@PathParam("nodeId") int nodeId) {
        checkNodeInDB(nodeId);
        IsisElementNode isiselem = enLinkdElementFactory.getIsisElement(nodeId);
        return Response.ok(isiselem).build();
    }

    private void checkNodeInDB(int nodeId) {
        if (m_nodeDao.get(nodeId) == null){
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("No such node in database").build());
        }
    }
}
