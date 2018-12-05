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
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyException;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("topologies")
public class TopologiesRestService {
    
    @Autowired
    private OnmsTopologyDao m_topologyDao;

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

    // FIXME enrich the tooltip with address speed and attributes
    private GraphmlType getGraphMl(String protocolSupported)
            throws InvalidGraphException, OnmsTopologyException {
        OnmsTopology topology = m_topologyDao.getTopology(protocolSupported);
        GraphML graphml = new GraphML();
        GraphMLGraph graph = new GraphMLGraph();
        graph.setProperty(OnmsTopology.NAMESPACE, protocolSupported);
        graph.setProperty(OnmsTopology.LABEL, protocolSupported + " Topology");
        graph.setId(protocolSupported);
        topology.getVertices().stream().forEach(vertex -> {
            GraphMLNode gnode = new GraphMLNode();
            gnode.setId(vertex.getId());
            gnode.setProperty(OnmsTopology.LABEL, vertex.getLabel());
            if (vertex.getNodeid() != null) {
                gnode.setProperty(OnmsTopology.NODE_ID, vertex.getNodeid());                               
            }
            if (vertex.getIconKey() != null) {
                gnode.setProperty(OnmsTopology.ICON_KEY, vertex.getIconKey());
            }
            if (vertex.getToolTipText() != null) {
                gnode.setProperty(OnmsTopology.TOOLTIP_TEXT, vertex.getToolTipText());
            }
            graph.addNode(gnode);
        });
        topology.getEdges().stream().forEach(shared -> {
            if (shared instanceof OnmsTopologyEdge) {
                OnmsTopologyEdge edge = (OnmsTopologyEdge) shared;
                GraphMLEdge gedge = new GraphMLEdge();
                gedge.setId(edge.getId());
                gedge.setSource(graph.getNodeById(edge.getSource().getVertex().getId()));
                gedge.setTarget(graph.getNodeById(edge.getTarget().getVertex().getId()));
                gedge.setProperty(OnmsTopology.TOOLTIP_TEXT, edge.getSource().getPort()
                        + edge.getTarget().getPort());
                gedge.setProperty(OnmsTopology.SOURCE_IFINDEX,
                                  edge.getSource().getIndex());
                gedge.setProperty(OnmsTopology.TARGET_IFINDEX,
                                  edge.getTarget().getIndex());
                graph.addEdge(gedge);
            } else {
                final GraphMLNode gcloud = new GraphMLNode();
                gcloud.setId(shared.getId());
                gcloud.setProperty(OnmsTopology.LABEL, shared.getId());
                gcloud.setProperty(OnmsTopology.NODE_ID, -1);
                gcloud.setProperty(OnmsTopology.ICON_KEY, "cloud");
                gcloud.setProperty(OnmsTopology.TOOLTIP_TEXT,
                                   "shared segment ->" + shared.getId());
                shared.getSources().stream().forEach(port -> {
                    GraphMLEdge gedge = new GraphMLEdge();
                    gedge.setId(gcloud.getId() + "|" + port.getId());
                    gedge.setSource(gcloud);
                    gedge.setTarget(graph.getNodeById(port.getVertex().getId()));
                    gedge.setProperty(OnmsTopology.TOOLTIP_TEXT,
                                      "connection to: " + port.getPort());
                    gedge.setProperty(OnmsTopology.SOURCE_IFINDEX, -1);
                    gedge.setProperty(OnmsTopology.TARGET_IFINDEX, port.getIndex());
                    graph.addEdge(gedge);
                });
            }
        });
        graphml.addGraph(graph);
        return GraphMLWriter.convert(graphml);
    }
}