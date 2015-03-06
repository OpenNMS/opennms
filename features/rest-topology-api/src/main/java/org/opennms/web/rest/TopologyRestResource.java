package org.opennms.web.rest;

import org.opennms.netmgt.dao.api.*;
import org.opennms.netmgt.model.topology.IsIsTopologyLink;
import org.opennms.netmgt.model.topology.LldpTopologyLink;
import org.opennms.netmgt.model.topology.OspfTopologyLink;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Scope("prototype")
@Path("topologyAPI")
public class TopologyRestResource {

    LldpLinkDao m_lldpLinkDao;
    OspfLinkDao m_ospfLinkDao;
    CdpLinkDao m_cdpLinkDao;
    IsIsLinkDao m_isIsLinkDao;
    BridgeMacLinkDao m_bridgeMacLinkDao;

    @GET
    @Path("{nodeId}/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public TopologyResponse getTopology(@PathParam("nodeId") final int nodeId){
        List<LldpTopologyLink> lldpLinks = m_lldpLinkDao.findAllTopologyLinks();
        List<OspfTopologyLink> ospfLinks = m_ospfLinkDao.findAllTopologyLinks();
        List<IsIsTopologyLink> isisLinks = m_isIsLinkDao.getLinksForTopology();
        return new TopologyResponse("testing");
    }


}
