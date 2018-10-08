/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.graphdrawing.graphml.GraphmlType;
import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLEdge;
import org.opennms.features.graphml.model.GraphMLGraph;
import org.opennms.features.graphml.model.GraphMLNode;
import org.opennms.features.graphml.model.GraphMLWriter;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.netmgt.model.OnmsTopology;
import org.opennms.netmgt.model.OnmsTopologyException;
import org.opennms.netmgt.topologies.service.api.TopologyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("topologies")
public class TopologyRestService {

    private final static String NAMESPACE = "namespace";
    private final static String ICON_KEY = "iconKey";
    private final static String LABEL = "label";
    private final static String NODE_ID = "nodeID";
    private final static String TOOLTIP_TEXT = "tooltipText";
//FIXME support port
//    private final static String SOURCE_PORT= "sourceport";
//    private final static String TARGET_PORT= "targetport";
    private final static String SOURCE_IFINDEX= "sourceifindex";
    private final static String TARGET_IFINDEX= "targetifindex";
    
    @Autowired
    private TopologyDao m_topologyDao;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getProtocolSupportedString() {
        return m_topologyDao.getSupportedProtocols().toString();
    }
    
    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCount() {
        return Integer.toString(m_topologyDao.getSupportedProtocols().size());
    }
    
    @GET
    @Path("{supported-protocol}")
    @Produces({MediaType.APPLICATION_XML})
    public Response getGraph(@PathParam("supported-protocol") String supportedProtocol) throws InvalidGraphException,OnmsTopologyException {
        if (!m_topologyDao.getSupportedProtocols().contains(supportedProtocol)) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();                
        }
        return Response.ok(getGraphMl(supportedProtocol)).build();
    }

    private GraphmlType getGraphMl(String protocolSupported) throws InvalidGraphException, OnmsTopologyException {
        OnmsTopology topology = m_topologyDao.getTopology(protocolSupported);
        GraphML graphml = new GraphML();
        GraphMLGraph graph = new GraphMLGraph();
        graph.setProperty(NAMESPACE,protocolSupported);
        graph.setProperty(LABEL,protocolSupported + " Topology");
        graph.setId(protocolSupported);
        topology.getVertices().stream().forEach(vertex -> {
            GraphMLNode gnode = new GraphMLNode();
            gnode.setId(vertex.getId());
            gnode.setProperty(LABEL, vertex.getNode().getLabel());
            gnode.setProperty(NODE_ID, vertex.getNode().getId());
            gnode.setProperty(ICON_KEY, vertex.getIconKey());
            gnode.setProperty(TOOLTIP_TEXT, vertex.getTooltipText());
            graph.addNode(gnode);
        });
        topology.getEdges().stream().forEach(edge -> {
            GraphMLEdge gedge = new GraphMLEdge();
            gedge.setId(edge.getId());
            gedge.setSource(graph.getNodeById(edge.getSource().getId()));
            gedge.setTarget(graph.getNodeById(edge.getTarget().getId()));
            gedge.setProperty(TOOLTIP_TEXT, edge.getTooltipText());
            gedge.setProperty(SOURCE_IFINDEX, edge.getSourceIfIndex());
            gedge.setProperty(TARGET_IFINDEX, edge.getTargetIfIndex());
            graph.addEdge(gedge);
        });
        graphml.addGraph(graph);
        return GraphMLWriter.convert(graphml);
   }
}