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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.SimpleGraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class VmwareTopologyProvider extends SimpleGraphProvider {

    private interface Icons {
        String DATACENTER = "vmware.DATACENTER_ICON";
        String DATASTORE = "vmware.DATASTORE_ICON";
        String NETWORK = "vmware.NETWORK_ICON";
    }

    public static final String TOPOLOGY_NAMESPACE_VMWARE = "vmware";

    private static final Logger LOG = LoggerFactory.getLogger(VmwareTopologyProvider.class);
    private static final String SPLIT_REGEXP = " *, *";

    private NodeDao m_nodeDao;
    private IpInterfaceDao m_ipInterfaceDao;

    public VmwareTopologyProvider(NodeDao nodeDao, IpInterfaceDao ipInterfaceDao) {
        super(TOPOLOGY_NAMESPACE_VMWARE);
        m_nodeDao = Objects.requireNonNull(nodeDao);
        m_ipInterfaceDao = Objects.requireNonNull(ipInterfaceDao);
    }

    @Override
    public void refresh() {
        resetContainer();

        // get all host systems
        List<OnmsNode> hostSystems = m_nodeDao.findAllByVarCharAssetColumn("vmwareManagedEntityType", "HostSystem");
        if (hostSystems.isEmpty()) {
            LOG.info("refresh: No host systems with defined VMware assets fields found!");
        }
        for (OnmsNode hostSystem : hostSystems) {
            addHostSystem(hostSystem);
        }

        // get all virtual machines
        List<OnmsNode> virtualMachines = m_nodeDao.findAllByVarCharAssetColumn("vmwareManagedEntityType", "VirtualMachine");
        if (virtualMachines.isEmpty()) {
            LOG.info("refresh: No virtual machines with defined VMware assets fields found!");
        }
        for (OnmsNode virtualMachine : virtualMachines) {
            addVirtualMachine(virtualMachine);
        }
    }

    @Override
    public Defaults getDefaults() {
        return new Defaults()
                .withCriteria(() -> {
                    if (getVertices().isEmpty()) {
                        return Lists.newArrayList();
                    }
                    return Lists.newArrayList(new DefaultVertexHopCriteria(getVertices().get(0)));
                });
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
        return getSelection(TOPOLOGY_NAMESPACE_VMWARE, selectedVertices, type);
    }

    private AbstractVertex createDatacenterVertex(String vertexId, String vertexName) {
        if (containsVertexId(vertexId)) {
            return (AbstractVertex) getVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId);
        }
        AbstractVertex datacenterVertex = new AbstractVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId, vertexName);
        datacenterVertex.setIconKey(Icons.DATACENTER);
        return datacenterVertex;
    }

    private AbstractVertex createNetworkVertex(String vertexId, String vertexName) {
        if (containsVertexId(vertexId)) {
            return (AbstractVertex) getVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId);
        }
        AbstractVertex vertex = new AbstractVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId, vertexName);
        vertex.setIconKey(Icons.NETWORK);
        vertex.setLabel(vertexName);
        return vertex;
    }

    private AbstractVertex createDatastoreVertex(String vertexId, String vertexName) {
        if (containsVertexId(vertexId)) {
            return (AbstractVertex) getVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId);
        }
        AbstractVertex vertex = new AbstractVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId, vertexName);
        vertex.setIconKey(Icons.DATASTORE);
        return vertex;
    }

    private AbstractVertex createVirtualMachineVertex(String vertexId, String vertexName, String primaryInterface, int id, String powerState) {
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

        AbstractVertex vertex = new AbstractVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId, vertexName);
        vertex.setIconKey(icon);
        vertex.setIpAddress(primaryInterface);
        vertex.setNodeID(id);
        return vertex;
    }

    private AbstractVertex createHostSystemVertex(String vertexId, String vertexName, String primaryInterface, int id, String powerState) {
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

        AbstractVertex vertex = new AbstractVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId, vertexName);
        vertex.setIconKey(icon);
        vertex.setIpAddress(primaryInterface);
        vertex.setNodeID(id);
        return vertex;
    }


    private void addHostSystem(OnmsNode hostSystem) {
        String vmwareManagementServer = hostSystem.getAssetRecord().getVmwareManagementServer().trim();
        String vmwareManagedObjectId = hostSystem.getAssetRecord().getVmwareManagedObjectId().trim();
        String vmwareTopologyInfo = hostSystem.getAssetRecord().getVmwareTopologyInfo().trim();
        String vmwareState = hostSystem.getAssetRecord().getVmwareState().trim();
        String datacenterName = "Datacenter (" + vmwareManagementServer + ")";
        String datacenterMoId = null;

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

        AbstractVertex datacenterVertex = createDatacenterVertex(vmwareManagementServer, datacenterName);
        addVertices(datacenterVertex);

        String primaryInterface = "unknown";

        // get the primary interface ip address
        OnmsIpInterface ipInterface = m_ipInterfaceDao.findPrimaryInterfaceByNodeId(hostSystem.getId());

        if (ipInterface != null) {
            primaryInterface = ipInterface.getIpHostName();
        }

        AbstractVertex hostSystemVertex = createHostSystemVertex(vmwareManagementServer + "/" + vmwareManagedObjectId, hostSystem.getLabel(), primaryInterface, hostSystem.getId(), vmwareState);
        addVertices(hostSystemVertex);

        // set the parent vertex
        if (!hostSystemVertex.equals(datacenterVertex)) {
            connectVertices(hostSystemVertex, datacenterVertex);
        }

        for (String network : networks) {
            AbstractVertex networkVertex = createNetworkVertex(vmwareManagementServer + "/" + network, moIdToName.get(network));
            addVertices(networkVertex);
            connectVertices(vmwareManagementServer + "/" + vmwareManagedObjectId + "->" + network, hostSystemVertex, networkVertex, getEdgeNamespace());
        }
        for (String datastore : datastores) {
            AbstractVertex datastoreVertex = createDatastoreVertex(vmwareManagementServer + "/" + datastore, moIdToName.get(datastore));
            addVertices(datastoreVertex);
            connectVertices(vmwareManagementServer + "/" + vmwareManagedObjectId + "->" + datastore, hostSystemVertex, datastoreVertex, getEdgeNamespace());
        }
    }

    private void addVirtualMachine(OnmsNode virtualMachine) {
        String vmwareManagementServer = virtualMachine.getAssetRecord().getVmwareManagementServer().trim();
        String vmwareManagedObjectId = virtualMachine.getAssetRecord().getVmwareManagedObjectId().trim();
        String vmwareTopologyInfo = virtualMachine.getAssetRecord().getVmwareTopologyInfo().trim();
        String vmwareState = virtualMachine.getAssetRecord().getVmwareState().trim();
        String datacenterName = "Datacenter (" + vmwareManagementServer + ")";

        String datacenterMoId = null;
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

        AbstractVertex datacenterVertex = createDatacenterVertex(vmwareManagementServer, datacenterName);
        addVertices(datacenterVertex);

        String primaryInterface = "unknown";
        OnmsIpInterface ipInterface = m_ipInterfaceDao.findPrimaryInterfaceByNodeId(virtualMachine.getId());
        if (ipInterface != null) {
            primaryInterface = ipInterface.getIpHostName();
        }

        AbstractVertex virtualMachineVertex = createVirtualMachineVertex(vmwareManagementServer + "/" + vmwareManagedObjectId, virtualMachine.getLabel(), primaryInterface, virtualMachine.getId(), vmwareState);
        addVertices(virtualMachineVertex);

        AbstractVertex hostSystemVertex = createHostSystemVertex(vmwareManagementServer + "/" + vmwareHostSystemId, moIdToName.get(vmwareHostSystemId) + " (not in database)", "", -1, "hostSystemVertex");
        addVertices(hostSystemVertex);

        connectVertices(vmwareManagementServer + "/" + vmwareManagedObjectId + "->" + vmwareManagementServer + "/" + vmwareHostSystemId, virtualMachineVertex, getVertex(getVertexNamespace(), vmwareManagementServer + "/" + vmwareHostSystemId), getEdgeNamespace());
    }
}
