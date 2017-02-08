/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.vmware.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.simple.SimpleGraphProvider;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class VmwareTopologyProvider extends SimpleGraphProvider implements GraphProvider, SearchProvider {
    public static final String TOPOLOGY_NAMESPACE_VMWARE = "vmware";
    private static final Logger LOG = LoggerFactory.getLogger(VmwareTopologyProvider.class);

    private static final String SPLIT_REGEXP = " *, *";
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

    public void debug(Vertex vmwareVertex) {
        LOG.debug("-+- id: {}", vmwareVertex.getId());
        LOG.debug(" |- hashCode: {}", vmwareVertex.hashCode());
        LOG.debug(" |- label: {}", vmwareVertex.getLabel());
        LOG.debug(" |- ip: {}", vmwareVertex.getIpAddress());
        LOG.debug(" |- iconKey: {}", vmwareVertex.getIconKey());
        LOG.debug(" |- nodeId: {}", vmwareVertex.getNodeID());

        for (EdgeRef edge : getEdgeIdsForVertex(vmwareVertex)) {
            Edge vmwareEdge = getEdge(edge);
            VertexRef edgeTo = vmwareEdge.getTarget().getVertex();
            if (vmwareVertex.equals(edgeTo)) {
                edgeTo = vmwareEdge.getSource().getVertex();
            }
            LOG.debug(" |- edgeTo: {}", edgeTo);
        }
        LOG.debug(" '- parent: {}", (vmwareVertex.getParent() == null ? null : vmwareVertex.getParent().getId()));
    }

    public void debugAll() {
        for (Vertex id : getVertices()) {
            debug(id);
        }
    }

    private AbstractVertex addDatacenterGroup(String vertexId, String groupName) {
        if (containsVertexId(vertexId)) {
            return (AbstractVertex) getVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId);
        }
        return addGroup(vertexId, "vmware.DATACENTER_ICON", groupName);
    }

    private AbstractVertex addNetworkVertex(String vertexId, String vertexName) {
        if (containsVertexId(vertexId)) {
            return (AbstractVertex) getVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId);
        }
        AbstractVertex vertex = addVertex(vertexId, 50, 50);
        vertex.setIconKey("vmware.NETWORK_ICON");
        vertex.setLabel(vertexName);
        return vertex;
    }

    private AbstractVertex addDatastoreVertex(String vertexId, String vertexName) {
        if (containsVertexId(vertexId)) {
            return (AbstractVertex) getVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId);
        }
        AbstractVertex vertex = addVertex(vertexId, 50, 50);
        vertex.setIconKey("vmware.DATASTORE_ICON");
        vertex.setLabel(vertexName);
        return vertex;
    }

    private AbstractVertex addVirtualMachineVertex(String vertexId, String vertexName, String primaryInterface, int id, String powerState) {
        if (containsVertexId(vertexId)) {
            return (AbstractVertex) getVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId);
        }

        String icon = "vmware.VIRTUALMACHINE_ICON_UNKNOWN";

        if ("poweredOn".equals(powerState)) {
            icon = "vmware.VIRTUALMACHINE_ICON_ON";
        } else if ("poweredOff".equals(powerState)) {
            icon = "vmware.VIRTUALMACHINE_ICON_OFF";
        } else if ("suspended".equals(powerState)) {
            icon = "vmware.VIRTUALMACHINE_ICON_SUSPENDED";
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

        String icon = "vmware.HOSTSYSTEM_ICON_UNKNOWN";

        if ("poweredOn".equals(powerState)) {
            icon = "vmware.HOSTSYSTEM_ICON_ON";
        } else if ("poweredOff".equals(powerState)) {
            icon = "vmware.HOSTSYSTEM_ICON_OFF";
        } else if ("standBy".equals(powerState)) {
            icon = "vmware.HOSTSYSTEM_ICON_STANDBY";
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

        String[] entities = vmwareTopologyInfo.split(SPLIT_REGEXP);

        for (String entityAndName : entities) {
            String[] splitBySlash = entityAndName.split("/");
            String entityId = splitBySlash[0];

            String entityName = "unknown";

            if (splitBySlash.length > 1) {
                try {
                    entityName = new String(URLDecoder.decode(splitBySlash[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    LOG.error(e.getMessage(), e);
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
        // hostSystemVertex.setParent(datacenterVertex);
        if (!hostSystemVertex.equals(datacenterVertex)) setParent(hostSystemVertex, datacenterVertex);

        for (String network : networks) {
            AbstractVertex networkVertex = addNetworkVertex(vmwareManagementServer + "/" + network, moIdToName.get(network));
            // networkVertex.setParent(datacenterVertex);
            if (!networkVertex.equals(datacenterVertex)) setParent(networkVertex, datacenterVertex);
            connectVertices(vmwareManagementServer + "/" + vmwareManagedObjectId + "->" + network, hostSystemVertex, networkVertex, getEdgeNamespace());
        }
        for (String datastore : datastores) {
            AbstractVertex datastoreVertex = addDatastoreVertex(vmwareManagementServer + "/" + datastore, moIdToName.get(datastore));
            // datastoreVertex.setParent(datacenterVertex);
            if (!datastoreVertex.equals(datacenterVertex)) setParent(datastoreVertex, datacenterVertex);
            connectVertices(vmwareManagementServer + "/" + vmwareManagedObjectId + "->" + datastore, hostSystemVertex, datastoreVertex, getEdgeNamespace());
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

        String[] entities = vmwareTopologyInfo.split(SPLIT_REGEXP);

        for (String entityAndName : entities) {
            String[] splitBySlash = entityAndName.split("/");
            String entityId = splitBySlash[0];

            String entityName = "unknown";

            if (splitBySlash.length > 1) {
                try {
                    entityName = new String(URLDecoder.decode(splitBySlash[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    LOG.error(e.getMessage(), e);
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
            LOG.warn("Cannot find host system id for virtual machine {}/{}", vmwareManagementServer, vmwareManagedObjectId);
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
            // virtualMachineVertex.setParent(datacenterVertex);
            if (!virtualMachineVertex.equals(datacenterVertex)) setParent(virtualMachineVertex, datacenterVertex);
        } else {
            addHostSystemVertex(vmwareManagementServer + "/" + vmwareHostSystemId, moIdToName.get(vmwareHostSystemId) + " (not in database)", "", -1, "unknown");
        }

        // connect the virtual machine to the host system
        connectVertices(vmwareManagementServer + "/" + vmwareManagedObjectId + "->" + vmwareManagementServer + "/" + vmwareHostSystemId, virtualMachineVertex, getVertex(getVertexNamespace(), vmwareManagementServer + "/" + vmwareHostSystemId), getEdgeNamespace());
    }

    @Override
    public void refresh() {
        m_generated = true;
        resetContainer();

        // get all host systems
        List<OnmsNode> hostSystems = m_nodeDao.findAllByVarCharAssetColumn("vmwareManagedEntityType", "HostSystem");
        if (hostSystems.isEmpty()) {
            LOG.info("refresh: No host systems with defined VMware assets fields found!");
        } else {
            for (OnmsNode hostSystem : hostSystems) {
                addHostSystem(hostSystem);
            }
        }

        // get all virtual machines
        List<OnmsNode> virtualMachines = m_nodeDao.findAllByVarCharAssetColumn("vmwareManagedEntityType", "VirtualMachine");
        if (virtualMachines.isEmpty()) {
            LOG.info("refresh: No virtual machines with defined VMware assets fields found!");
        } else {
            for (OnmsNode virtualMachine : virtualMachines) {
                addVirtualMachine(virtualMachine);
            }
        }
        debugAll();
    }

    @Override
    public void onFocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
        GraphContainer m_graphContainer = operationContext.getGraphContainer();
        VertexRef vertexRef = getVertex(searchResult.getNamespace(), searchResult.getId());
        m_graphContainer.getSelectionManager().setSelectedVertexRefs(Lists.newArrayList(vertexRef));
    }

    @Override
    public void onDefocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
        GraphContainer graphContainer = operationContext.getGraphContainer();
        VertexRef vertexRef = getVertex(searchResult.getNamespace(), searchResult.getId());
        graphContainer.getSelectionManager().deselectVertexRefs(Lists.newArrayList(vertexRef));
    }

    @Override
    public void onCenterSearchResult(final SearchResult searchResult, final GraphContainer graphContainer) {
        // TODO: implement?
    }

    @Override
    public void onToggleCollapse(final SearchResult searchResult, final GraphContainer graphContainer) {
        // TODO: implement?
    }

    @Override
    public String getSearchProviderNamespace() {
        return "vmware";
    }

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        return searchPrefix.contains("nodes=");
    }

    //FIXME: This should return the list of vertexrefs for the "zoom to focus" operation
    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
        return Collections.emptySet();
    }

    @Override
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        DefaultVertexHopCriteria criteria = new DefaultVertexHopCriteria(
                new DefaultVertexRef(
                        searchResult.getNamespace(),
                        searchResult.getId(),
                        searchResult.getLabel()));
        container.addCriteria(criteria);
    }

    @Override
    public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        DefaultVertexHopCriteria criteria = new DefaultVertexHopCriteria(
                new DefaultVertexRef(
                        searchResult.getNamespace(),
                        searchResult.getId(),
                        searchResult.getLabel()));
        container.removeCriteria(criteria);
    }

    @Override
    public List<SearchResult> query(SearchQuery searchQuery, GraphContainer graphContainer) {
        List<Vertex> vertices = m_vertexProvider.getVertices();
        List<SearchResult> searchResults = Lists.newArrayList();

        for(Vertex vertex : vertices){
            if(searchQuery.matches(vertex.getLabel())) {
                searchResults.add(new SearchResult(vertex));
            }
        }

        return searchResults;
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
        return getSelection(TOPOLOGY_NAMESPACE_VMWARE, selectedVertices, type);
    }
}
