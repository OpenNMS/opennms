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

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

public class VmwareTopologyProvider implements TopologyProvider {

    private final String SPLIT_REGEXP = " *, *";

    private NodeDao m_nodeDao;
    private IpInterfaceDao m_ipInterfaceDao;

    private VmwareVertexContainer m_vertexContainer;
    private BeanContainer<String, VmwareEdge> m_edgeContainer;

    private int m_groupCounter = 0;
    private boolean m_generated = false;

    public VmwareTopologyProvider() {
        m_vertexContainer = new VmwareVertexContainer();
        m_edgeContainer = new BeanContainer<String, VmwareEdge>(VmwareEdge.class);
        m_edgeContainer.setBeanIdProperty("id");

        resetContainer();
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

    public void debug(String id) {
        VmwareVertex vmwareVertex = getRequiredVertex(id);

        System.err.println("-+- id: " + vmwareVertex.getId());
        System.err.println(" |- hashCode: " + vmwareVertex.hashCode());
        System.err.println(" |- label: " + vmwareVertex.getLabel());
        System.err.println(" |- ip: " + vmwareVertex.getIpAddr());
        System.err.println(" |- iconKey: " + vmwareVertex.getIconKey());
        System.err.println(" |- nodeId: " + vmwareVertex.getNodeID());

        for (VmwareEdge vmwareEdge : vmwareVertex.getEdges()) {
            String edgeTo = vmwareEdge.getTarget().getId();
            if (id.equals(edgeTo)) {
                edgeTo = vmwareEdge.getSource().getId();
            }
            System.err.println(" |- edgeTo: " + edgeTo);
        }
        System.err.println(" '- parent: " + (vmwareVertex.getParent() == null ? null : vmwareVertex.getParent().getId()));
    }

    public void debugAll() {
        for (String id : m_vertexContainer.getItemIds()) {
            debug(id);
        }
    }

    public VmwareGroup addDatacenterGroup(String groupId, String groupName) {
        if (!m_vertexContainer.containsId(groupId)) {
            addGroup(groupId, "DATACENTER_ICON", groupName);
        }
        return (VmwareGroup) getRequiredVertex(groupId);
    }

    public VmwareVertex addNetworkVertex(String vertexId, String vertexName) {
        if (!m_vertexContainer.containsId(vertexId)) {
            addVertex(vertexId, 50, 50, "NETWORK_ICON", vertexName, "", -1);
        }
        return getRequiredVertex(vertexId);
    }

    public VmwareVertex addDatastoreVertex(String vertexId, String vertexName) {
        if (!m_vertexContainer.containsId(vertexId)) {
            addVertex(vertexId, 50, 50, "DATASTORE_ICON", vertexName, "", -1);
        }
        return getRequiredVertex(vertexId);
    }

    public VmwareVertex addVirtualMachineVertex(String vertexId, String vertexName, String primaryInterface, int id, String powerState) {
        if (!m_vertexContainer.containsId(vertexId)) {
            String icon = "VIRTUALMACHINE_ICON_UNKNOWN";

            if ("poweredOn".equals(powerState)) {
                icon = "VIRTUALMACHINE_ICON_ON";
            }
            if ("poweredOff".equals(powerState)) {
                icon = "VIRTUALMACHINE_ICON_OFF";
            }
            if ("suspended".equals(powerState)) {
                icon = "VIRTUALMACHINE_ICON_SUSPENDED";
            }

            addVertex(vertexId, 50, 50, icon, vertexName, primaryInterface, id);
        }
        return getRequiredVertex(vertexId);
    }

    public VmwareVertex addHostSystemVertex(String vertexId, String vertexName, String primaryInterface, int id, String powerState) {
        if (!m_vertexContainer.containsId(vertexId)) {
            String icon = "HOSTSYSTEM_ICON_UNKNOWN";

            if ("poweredOn".equals(powerState)) {
                icon = "HOSTSYSTEM_ICON_ON";
            }
            if ("poweredOff".equals(powerState)) {
                icon = "HOSTSYSTEM_ICON_OFF";
            }
            if ("standBy".equals(powerState)) {
                icon = "HOSTSYSTEM_ICON_STANDBY";
            }

            addVertex(vertexId, 50, 50, icon, vertexName, primaryInterface, id);
        }
        return getRequiredVertex(vertexId);
    }


    public void addHostSystem(OnmsNode hostSystem) {
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

        VmwareGroup datacenterVertex = addDatacenterGroup(vmwareManagementServer, datacenterName);

        String primaryInterface = "unknown";

        // get the primary interface ip address
        OnmsIpInterface ipInterface = m_ipInterfaceDao.findPrimaryInterfaceByNodeId(hostSystem.getId());

        if (ipInterface != null) {
            primaryInterface = ipInterface.getIpHostName();
        }

        VmwareVertex hostSystemVertex = addHostSystemVertex(vmwareManagementServer + "/" + vmwareManagedObjectId, hostSystem.getLabel(), primaryInterface, hostSystem.getId(), vmwareState);

        // set the parent vertex
        hostSystemVertex.setParent(datacenterVertex);

        for (String network : networks) {
            VmwareVertex networkVertex = addNetworkVertex(vmwareManagementServer + "/" + network, moIdToName.get(network));
            networkVertex.setParent(datacenterVertex);
            connectVertices(vmwareManagementServer + "/" + vmwareManagedObjectId + "->" + network, vmwareManagementServer + "/" + vmwareManagedObjectId, vmwareManagementServer + "/" + network);
        }
        for (String datastore : datastores) {
            VmwareVertex datastoreVertex = addDatastoreVertex(vmwareManagementServer + "/" + datastore, moIdToName.get(datastore));
            datastoreVertex.setParent(datacenterVertex);
            connectVertices(vmwareManagementServer + "/" + vmwareManagedObjectId + "->" + datastore, vmwareManagementServer + "/" + vmwareManagedObjectId, vmwareManagementServer + "/" + datastore);
        }
    }

    public void addVirtualMachine(OnmsNode virtualMachine) {
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

        VmwareGroup datacenterVertex = addDatacenterGroup(vmwareManagementServer, datacenterName);

        String primaryInterface = "unknown";

        // get the primary interface ip address

        OnmsIpInterface ipInterface = m_ipInterfaceDao.findPrimaryInterfaceByNodeId(virtualMachine.getId());

        if (ipInterface != null) {
            primaryInterface = ipInterface.getIpHostName();
        }

        // add a vertex for the virtual machine
        VmwareVertex virtualMachineVertex = addVirtualMachineVertex(vmwareManagementServer + "/" + vmwareManagedObjectId, virtualMachine.getLabel(), primaryInterface, virtualMachine.getId(), vmwareState);

        if (m_vertexContainer.containsId(vmwareManagementServer + "/" + vmwareHostSystemId)) {
            // and set the parent vertex
            virtualMachineVertex.setParent(datacenterVertex);
        } else {
            VmwareVertex hostSystemVertex = addHostSystemVertex(vmwareManagementServer + "/" + vmwareHostSystemId, moIdToName.get(vmwareHostSystemId) + " (not in database)", "", -1, "unknown");
        }

        // connect the virtual machine to the host system
        connectVertices(vmwareManagementServer + "/" + vmwareManagedObjectId + "->" + vmwareManagementServer + "/" + vmwareHostSystemId, vmwareManagementServer + "/" + vmwareManagedObjectId, vmwareManagementServer + "/" + vmwareHostSystemId);
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

    public VmwareVertexContainer getVertexContainer() {
        return m_vertexContainer;
    }

    public BeanContainer<String, VmwareEdge> getEdgeContainer() {
        return m_edgeContainer;
    }

    public Collection<?> getVertexIds() {
        return m_vertexContainer.getItemIds();
    }

    public Collection<?> getEdgeIds() {
        return m_edgeContainer.getItemIds();
    }

    public Item getVertexItem(Object vertexId) {
        return m_vertexContainer.getItem(vertexId);
    }

    public Item getEdgeItem(Object edgeId) {
        return m_edgeContainer.getItem(edgeId);
    }

    public Collection<?> getEndPointIdsForEdge(Object edgeId) {
        VmwareEdge edge = getRequiredEdge(edgeId);

        List<Object> endPoints = new ArrayList<Object>(2);

        endPoints.add(edge.getSource().getId());
        endPoints.add(edge.getTarget().getId());

        return endPoints;
    }

    @Override
    public String getNamespace() {
        return "vmware";
    }

    public Collection<?> getEdgeIdsForVertex(Object vertexId) {
        VmwareVertex vertex = getRequiredVertex(vertexId);

        List<Object> edges = new ArrayList<Object>(vertex.getEdges().size());

        for (VmwareEdge e : vertex.getEdges()) {

            Object edgeId = e.getId();

            edges.add(edgeId);
        }

        return edges;
    }

    private Item addVertex(String id, int x, int y, String icon, String label, String ipAddr, int nodeID) {
        if (m_vertexContainer.containsId(id)) {
            throw new IllegalArgumentException("A vertex or group with id " + id + " already exists!");
        }
        System.err.println("Adding a vertex: " + id);
        VmwareVertex vertex = new VmwareLeafVertex(id, x, y);

        vertex.setIconKey(icon);
        //vertex.setIcon(VmwareConstants.ICONS.get(icon));

        vertex.setLabel(label);
        vertex.setIpAddr(ipAddr);
        vertex.setNodeID(nodeID);

        return m_vertexContainer.addBean(vertex);
    }

    private Item addGroup(String groupId, String icon, String label) {
        if (m_vertexContainer.containsId(groupId)) {
            throw new IllegalArgumentException("A vertex or group with id " + groupId + " already exists!");
        }
        System.err.println("Adding a group: " + groupId);
        VmwareVertex vertex = new VmwareGroup(groupId);

        vertex.setIconKey(icon);

        vertex.setLabel(label);

        return m_vertexContainer.addBean(vertex);
    }

    private void connectVertices(String id, Object sourceVertextId, Object targetVertextId) {
        VmwareVertex source = getRequiredVertex(sourceVertextId);
        VmwareVertex target = getRequiredVertex(targetVertextId);

        VmwareEdge edge = new VmwareEdge(id, source, target);

        m_edgeContainer.addBean(edge);
    }

    public void removeVertex(Object vertexId) {
        VmwareVertex vertex = getVertex(vertexId, false);

        if (vertex == null) {
            return;
        }

        m_vertexContainer.removeItem(vertexId);

        for (VmwareEdge e : vertex.getEdges()) {
            m_edgeContainer.removeItem(e.getId());
        }
    }

    public VmwareVertex getRequiredVertex(Object vertexId) {
        return getVertex(vertexId, true);
    }

    public VmwareVertex getVertex(Object vertexId, boolean required) {
        BeanItem<VmwareVertex> item = m_vertexContainer.getItem(vertexId);
        if (required && item == null) {
            System.out.println("Error: required vertex '" + vertexId + "' not found");
            debugAll();
//            throw new IllegalArgumentException("required vertex " + vertexId + " not found.");
        }

        return item == null ? null : item.getBean();
    }

    private VmwareEdge getRequiredEdge(Object edgeId) {
        return getEdge(edgeId, true);
    }

    private VmwareEdge getEdge(Object edgeId, boolean required) {
        BeanItem<VmwareEdge> item = m_edgeContainer.getItem(edgeId);
        if (required && item == null) {
            System.out.println("Error: required edge '" + edgeId + "' not found");
            debugAll();
            //throw new IllegalArgumentException("required edge " + edgeId + " not found.");
        }

        return item == null ? null : item.getBean();
    }


    @XmlRootElement(name = "graph")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class SimpleGraph {
        @XmlElements({
                @XmlElement(name = "vertex", type = VmwareLeafVertex.class),
                @XmlElement(name = "group", type = VmwareGroup.class)
        })
        List<VmwareVertex> m_vertices = new ArrayList<VmwareVertex>();

        @XmlElement(name = "edge")
        List<VmwareEdge> m_edges = new ArrayList<VmwareEdge>();

        @SuppressWarnings("unused")
        public SimpleGraph() {
        }

        public SimpleGraph(List<VmwareVertex> vertices, List<VmwareEdge> edges) {
            m_vertices = vertices;
            m_edges = edges;
        }
    }

    public void save(String filename) {
        List<VmwareVertex> vertices = getBeans(m_vertexContainer);
        List<VmwareEdge> edges = getBeans(m_edgeContainer);

        SimpleGraph graph = new SimpleGraph(vertices, edges);

        JAXB.marshal(graph, new File(filename));
    }

    public void load(String filename) {
        SimpleGraph graph = JAXB.unmarshal(new File(filename), SimpleGraph.class);

        m_vertexContainer.removeAllItems();
        m_vertexContainer.addAll(graph.m_vertices);

        m_edgeContainer.removeAllItems();
        m_edgeContainer.addAll(graph.m_edges);
    }

    private <T> List<T> getBeans(BeanContainer<?, T> container) {
        Collection<?> itemIds = container.getItemIds();
        List<T> beans = new ArrayList<T>(itemIds.size());

        for (Object itemId : itemIds) {
            beans.add(container.getItem(itemId).getBean());
        }

        return beans;
    }

    public String getNextGroupId() {
        return "g" + m_groupCounter++;
    }

    public void resetContainer() {
        getVertexContainer().removeAllItems();
        getEdgeContainer().removeAllItems();
    }

    public Collection<?> getPropertyIds() {
        return Collections.EMPTY_LIST;
    }

    public Property getProperty(String propertyId) {
        return null;
    }


    //@Override
    public void setParent(Object vertexId, Object parentId) {
        m_vertexContainer.setParent(vertexId, parentId);
    }

    //@Override
    public boolean containsVertexId(Object vertexId) {
        return m_vertexContainer.containsId(vertexId);
    }

    @Override
    public Object addGroup(String groupName, String groupIcon) {
        String nextGroupId = getNextGroupId();
        addGroup(nextGroupId, groupIcon, groupName);
        return nextGroupId;
    }

}