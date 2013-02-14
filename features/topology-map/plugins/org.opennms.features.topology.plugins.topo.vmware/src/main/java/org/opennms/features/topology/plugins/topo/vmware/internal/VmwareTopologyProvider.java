/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.vmware.internal;

import org.opennms.features.topology.api.SimpleGroup;
import org.opennms.features.topology.api.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.*;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VmwareTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    public static final String TOPOLOGY_NAMESPACE_VMWARE = "vmware";

    private final String SPLIT_REGEXP = " *, *";

    private static final Logger m_vmwareLog = LoggerFactory.getLogger(VmwareTopologyProvider.class);

    private NodeDao m_nodeDao;
    private IpInterfaceDao m_ipInterfaceDao;

    private boolean m_generated = false;

    public VmwareTopologyProvider() {
        super(TOPOLOGY_NAMESPACE_VMWARE);
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    public boolean isGenerated() {
        return m_generated;
    }

    public void initialize() {
        generate();
    }

    public void debug(Vertex vmwareVertex) {
        System.err.println("-+- id: " + vmwareVertex.getId());
        System.err.println(" |- hashCode: " + vmwareVertex.hashCode());
        System.err.println(" |- label: " + vmwareVertex.getLabel());
        System.err.println(" |- ip: " + vmwareVertex.getIpAddress());
        System.err.println(" |- iconKey: " + vmwareVertex.getIconKey());
        System.err.println(" |- nodeId: " + vmwareVertex.getNodeID());

        for (EdgeRef edge : getEdgeIdsForVertex(vmwareVertex)) {
            Edge vmwareEdge = getEdge(edge);
            VertexRef edgeTo = vmwareEdge.getTarget().getVertex();
            if (vmwareVertex.equals(edgeTo)) {
                edgeTo = vmwareEdge.getSource().getVertex();
            }
            System.err.println(" |- edgeTo: " + edgeTo);
        }
        System.err.println(" '- parent: " + (vmwareVertex.getParent() == null ? null : vmwareVertex.getParent().getId()));
    }

    public void debugAll() {
        for (Vertex id : getVertices()) {
            debug(id);
        }
    }

    private AbstractVertex addDatacenterGroup(String groupId, String groupName) {
        if (containsVertexId(groupId)) {
            return (AbstractVertex) getVertex(TOPOLOGY_NAMESPACE_VMWARE, groupId);
        }
        return addGroup(groupId, "DATACENTER_ICON", groupName);
    }

    private AbstractVertex addNetworkVertex(String vertexId, String vertexName) {
        if (containsVertexId(vertexId)) {
            return (AbstractVertex) getVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId);
        }
        AbstractVertex vertex = addVertex(vertexId, 50, 50);
        vertex.setIconKey("NETWORK_ICON");
        vertex.setLabel(vertexName);
        return vertex;
    }

    private AbstractVertex addDatastoreVertex(String vertexId, String vertexName) {
        if (containsVertexId(vertexId)) {
            return (AbstractVertex) getVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId);
        }
        AbstractVertex vertex = addVertex(vertexId, 50, 50);
        vertex.setIconKey("DATASTORE_ICON");
        vertex.setLabel(vertexName);
        return vertex;
    }

    private AbstractVertex addVirtualMachineVertex(String vertexId, String vertexName, String primaryInterface, int id, String powerState) {
        String icon = "VIRTUALMACHINE_ICON_UNKNOWN";

        if ("poweredOn".equals(powerState)) {
            icon = "VIRTUALMACHINE_ICON_ON";
        } else if ("poweredOff".equals(powerState)) {
            icon = "VIRTUALMACHINE_ICON_OFF";
        } else if ("suspended".equals(powerState)) {
            icon = "VIRTUALMACHINE_ICON_SUSPENDED";
        }

        AbstractVertex vertex = addVertex(vertexId, 50, 50);
        vertex.setIconKey(icon);
        vertex.setLabel(vertexName);
        vertex.setIpAddress(primaryInterface);
        vertex.setNodeID(id);
        return vertex;
    }

    private AbstractVertex addHostSystemVertex(String vertexId, String vertexName, String primaryInterface, int id, String powerState) {
        if (containsVertexId(vertexId)) {
            return (AbstractVertex) getVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId);
        }

        String icon = "HOSTSYSTEM_ICON_UNKNOWN";

        if ("poweredOn".equals(powerState)) {
            icon = "HOSTSYSTEM_ICON_ON";
        } else if ("poweredOff".equals(powerState)) {
            icon = "HOSTSYSTEM_ICON_OFF";
        } else if ("standBy".equals(powerState)) {
            icon = "HOSTSYSTEM_ICON_STANDBY";
        }

        AbstractVertex vertex = addVertex(vertexId, 50, 50);
        vertex.setIconKey(icon);
        vertex.setLabel(vertexName);
        vertex.setIpAddress(primaryInterface);
        vertex.setNodeID(id);
        return vertex;
    }


    private void addHostSystem(OnmsNode hostSystem) {
        // getting data for nodes

        String vmwareManagementServer = hostSystem.getAssetRecord().getVmwareManagementServer().trim();
        String vmwareManagedObjectId = hostSystem.getAssetRecord().getVmwareManagedObjectId().trim();
        String vmwareTopologyInfo = hostSystem.getAssetRecord().getVmwareTopologyInfo().trim();
        String vmwareState = hostSystem.getAssetRecord().getVmwareState().trim();

        String datacenterMoId = null;
        String datacenterName = "Datacenter (" + vmwareManagementServer + ")";

        ArrayList<String> networks = new ArrayList<String>();
        ArrayList<String> datastores = new ArrayList<String>();

        HashMap<String, String> moIdToName = new HashMap<String, String>();

        String entities[] = vmwareTopologyInfo.split(SPLIT_REGEXP);

        for (String entityAndName : entities) {
            String splitBySlash[] = entityAndName.split("/");
            String entityId = splitBySlash[0];

            String entityName = "unknown";

            if (splitBySlash.length > 1) {
                try {
                    entityName = new String(URLDecoder.decode(splitBySlash[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            String entityType = entityId.split("-")[0];

            if ("network".equals(entityType)) {
                networks.add(entityId);
            }

            if ("datastore".equals(entityType)) {
                datastores.add(entityId);
            }

            if ("datacenter".equals(entityType)) {
                datacenterMoId = entityId;
            }

            moIdToName.put(entityId, entityName);
        }

        if (datacenterMoId != null) {
            datacenterName = moIdToName.get(datacenterMoId) + " (" + vmwareManagementServer + ")";
        }

        AbstractVertex datacenterVertex = addDatacenterGroup(vmwareManagementServer, datacenterName);

        String primaryInterface = "unknown";

        // get the primary interface ip address
        OnmsIpInterface ipInterface = m_ipInterfaceDao.findPrimaryInterfaceByNodeId(hostSystem.getId());

        if (ipInterface != null) {
            primaryInterface = ipInterface.getIpHostName();
        }

        AbstractVertex hostSystemVertex = addHostSystemVertex(vmwareManagementServer + "/" + vmwareManagedObjectId, hostSystem.getLabel(), primaryInterface, hostSystem.getId(), vmwareState);

        // set the parent vertex
        hostSystemVertex.setParent(datacenterVertex);

        for (String network : networks) {
            AbstractVertex networkVertex = addNetworkVertex(vmwareManagementServer + "/" + network, moIdToName.get(network));
            networkVertex.setParent(datacenterVertex);
            connectVertices(vmwareManagementServer + "/" + vmwareManagedObjectId + "->" + network, hostSystemVertex, networkVertex);
        }
        for (String datastore : datastores) {
            AbstractVertex datastoreVertex = addDatastoreVertex(vmwareManagementServer + "/" + datastore, moIdToName.get(datastore));
            datastoreVertex.setParent(datacenterVertex);
            connectVertices(vmwareManagementServer + "/" + vmwareManagedObjectId + "->" + datastore, hostSystemVertex, datastoreVertex);
        }
    }

    private void addVirtualMachine(OnmsNode virtualMachine) {
        // getting data for nodes

        String vmwareManagementServer = virtualMachine.getAssetRecord().getVmwareManagementServer().trim();
        String vmwareManagedObjectId = virtualMachine.getAssetRecord().getVmwareManagedObjectId().trim();
        String vmwareTopologyInfo = virtualMachine.getAssetRecord().getVmwareTopologyInfo().trim();
        String vmwareState = virtualMachine.getAssetRecord().getVmwareState().trim();

        String datacenterMoId = null;
        String datacenterName = "Datacenter (" + vmwareManagementServer + ")";

        String vmwareHostSystemId = null;

        ArrayList<String> networks = new ArrayList<String>();
        ArrayList<String> datastores = new ArrayList<String>();

        HashMap<String, String> moIdToName = new HashMap<String, String>();

        String entities[] = vmwareTopologyInfo.split(SPLIT_REGEXP);

        for (String entityAndName : entities) {
            String splitBySlash[] = entityAndName.split("/");
            String entityId = splitBySlash[0];

            String entityName = "unknown";

            if (splitBySlash.length > 1) {
                try {
                    entityName = new String(URLDecoder.decode(splitBySlash[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            String entityType = entityId.split("-")[0];

            if ("network".equals(entityType)) {
                networks.add(entityId);
            }

            if ("datastore".equals(entityType)) {
                datastores.add(entityId);
            }

            if ("datacenter".equals(entityType)) {
                datacenterMoId = entityId;
            }

            if ("host".equals(entityType)) {
                vmwareHostSystemId = entityId;
            }

            moIdToName.put(entityId, entityName);
        }

        if (datacenterMoId != null) {
            datacenterName = moIdToName.get(datacenterMoId) + " (" + vmwareManagementServer + ")";
        }

        if (vmwareHostSystemId == null) {
            System.err.println("Cannot find host system id for virtual machine " + vmwareManagementServer + "/" + vmwareManagedObjectId);
        }

        AbstractVertex datacenterVertex = addDatacenterGroup(vmwareManagementServer, datacenterName);

        String primaryInterface = "unknown";

        // get the primary interface ip address

        OnmsIpInterface ipInterface = m_ipInterfaceDao.findPrimaryInterfaceByNodeId(virtualMachine.getId());

        if (ipInterface != null) {
            primaryInterface = ipInterface.getIpHostName();
        }

        // add a vertex for the virtual machine
        AbstractVertex virtualMachineVertex = addVirtualMachineVertex(vmwareManagementServer + "/" + vmwareManagedObjectId, virtualMachine.getLabel(), primaryInterface, virtualMachine.getId(), vmwareState);

        if (containsVertexId(vmwareManagementServer + "/" + vmwareHostSystemId)) {
            // and set the parent vertex
            virtualMachineVertex.setParent(datacenterVertex);
        } else {
            addHostSystemVertex(vmwareManagementServer + "/" + vmwareHostSystemId, moIdToName.get(vmwareHostSystemId) + " (not in database)", "", -1, "unknown");
        }

        // connect the virtual machine to the host system
        connectVertices(vmwareManagementServer + "/" + vmwareManagedObjectId + "->" + vmwareManagementServer + "/" + vmwareHostSystemId, virtualMachineVertex, getVertex(getVertexNamespace(), vmwareManagementServer + "/" + vmwareHostSystemId));
    }

    public void generate() {
        m_generated = true;

        // reset container
        resetContainer();

        // get all host systems
        List<OnmsNode> hostSystems = m_nodeDao.findAllByVarCharAssetColumn("vmwareManagedEntityType", "HostSystem");

        if (hostSystems.size() == 0) {
            System.err.println("No host systems with defined VMware assets fields found!");
        } else {
            for (OnmsNode hostSystem : hostSystems) {
                addHostSystem(hostSystem);
            }
        }

        // get all virtual machines
        List<OnmsNode> virtualMachines = m_nodeDao.findAllByVarCharAssetColumn("vmwareManagedEntityType", "VirtualMachine");

        if (virtualMachines.size() == 0) {
            System.err.println("No virtual machines with defined VMware assets fields found!");
        } else {
            for (OnmsNode virtualMachine : virtualMachines) {
                addVirtualMachine(virtualMachine);
            }
        }

        debugAll();
    }

    public void save(String filename) {
        List<WrappedVertex> vertices = new ArrayList<WrappedVertex>();
        for (Vertex vertex : getVertices()) {
            if (vertex.isGroup()) {
                vertices.add(new WrappedGroup(vertex));
            } else {
                vertices.add(new WrappedLeafVertex(vertex));
            }
        }
        List<WrappedEdge> edges = new ArrayList<WrappedEdge>();
        for (Edge edge : getEdges()) {
            WrappedEdge newEdge = new WrappedEdge(edge, new WrappedLeafVertex(m_vertexProvider.getVertex(edge.getSource().getVertex())), new WrappedLeafVertex(m_vertexProvider.getVertex(edge.getTarget().getVertex())));
            edges.add(newEdge);
        }

        WrappedGraph graph = new WrappedGraph(getVertexNamespace(), vertices, edges);

        try {
            JAXBContext jc = JAXBContext.newInstance(WrappedGraph.class, WrappedLeafVertex.class, WrappedGroup.class, WrappedEdge.class);
            Marshaller u = jc.createMarshaller();
            u.marshal(graph, new File(filename));
        } catch (JAXBException e) {
            m_vmwareLog.error(e.getMessage(), e);
        }
    }

    private void load(final URI source, final WrappedGraph graph) {
        String namespace = graph.m_namespace == null ? TOPOLOGY_NAMESPACE_VMWARE : graph.m_namespace;
        if (getVertexNamespace() != namespace) {
            m_vmwareLog.info("Creating new vertex provider with namespace {}", namespace);
            m_vertexProvider = new SimpleVertexProvider(namespace);
        }
        if (getEdgeNamespace() != namespace) {
            m_vmwareLog.info("Creating new edge provider with namespace {}", namespace);
            m_edgeProvider = new SimpleEdgeProvider(namespace);
        }

        clearVertices();
        for (WrappedVertex vertex : graph.m_vertices) {
            if (vertex.namespace == null) {
                vertex.namespace = getVertexNamespace();
                m_vmwareLog.warn("Setting namespace on vertex to default: {}", vertex);
            }

            if (vertex.id == null) {
                m_vmwareLog.warn("Invalid vertex unmarshalled from {}: {}", source.toString(), vertex);
            } else if (vertex.id.startsWith(SIMPLE_GROUP_ID_PREFIX)) {
                try {
                    // Find the highest index group number and start the index for new groups above it
                    int groupNumber = Integer.parseInt(vertex.id.substring(SIMPLE_GROUP_ID_PREFIX.length()));
                    if (m_groupCounter <= groupNumber) {
                        m_groupCounter = groupNumber + 1;
                    }
                } catch (NumberFormatException e) {
                    // Ignore this group ID since it doesn't conform to our pattern for auto-generated IDs
                }
            }
            AbstractVertex newVertex;
            if (vertex.group) {
                newVertex = new SimpleGroup(vertex.namespace, vertex.id);
                if (vertex.x != null) {
                    newVertex.setX(vertex.x);
                }
                if (vertex.y != null) {
                    newVertex.setY(vertex.y);
                }
            } else {
                newVertex = new SimpleLeafVertex(vertex.namespace, vertex.id, vertex.x, vertex.y);
            }
            newVertex.setIconKey(vertex.iconKey);
            newVertex.setIpAddress(vertex.ipAddr);
            newVertex.setLabel(vertex.label);
            newVertex.setLocked(vertex.locked);
            if (vertex.nodeID != null) {
                newVertex.setNodeID(vertex.nodeID);
            }
            newVertex.setParent(vertex.parent);
            newVertex.setSelected(vertex.selected);
            newVertex.setStyleName(vertex.styleName);
            newVertex.setTooltipText(vertex.tooltipText);
            addVertices(newVertex);
        }

        clearEdges();
        for (WrappedEdge edge : graph.m_edges) {
            if (edge.namespace == null) {
                edge.namespace = getEdgeNamespace();
                m_vmwareLog.warn("Setting namespace on edge to default: {}", edge);
            }

            if (edge.id == null) {
                m_vmwareLog.warn("Invalid edge unmarshalled from {}: {}", source.toString(), edge);
            } else if (edge.id.startsWith(SIMPLE_EDGE_ID_PREFIX)) {
                try {
                    /*
                     * This code will be necessary if we allow edges to be created

                    // Find the highest index group number and start the index for new groups above it
                    int edgeNumber = Integer.parseInt(edge.getId().substring(SIMPLE_EDGE_ID_PREFIX.length()));

                    if (m_edgeCounter <= edgeNumber) {
                        m_edgeCounter = edgeNumber + 1;
                    }
                    */
                } catch (NumberFormatException e) {
                    // Ignore this edge ID since it doesn't conform to our pattern for auto-generated IDs
                }
            }
            AbstractEdge newEdge = connectVertices(edge.id, edge.source, edge.target);
            newEdge.setLabel(edge.label);
            newEdge.setTooltipText(edge.tooltipText);
            //addEdges(newEdge);
        }

        for (WrappedVertex vertex : graph.m_vertices) {
            if (vertex.parent != null) {
                m_vmwareLog.debug("Setting parent of " + vertex + " to " + vertex.parent);
                setParent(vertex, vertex.parent);
            }
        }
    }

    void load(URI url) throws JAXBException, MalformedURLException {
        JAXBContext jc = JAXBContext.newInstance(WrappedGraph.class);
        Unmarshaller u = jc.createUnmarshaller();
        WrappedGraph graph = (WrappedGraph) u.unmarshal(url.toURL());
        load(url, graph);
    }

    public void load(String filename) throws MalformedURLException, JAXBException {
        load(new File(filename).toURI());
    }
}
