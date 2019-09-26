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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class VmwareTopologyProvider extends AbstractTopologyProvider {
    public static final String TOPOLOGY_NAMESPACE_VMWARE = "vmware";
    private static final Logger LOG = LoggerFactory.getLogger(VmwareTopologyProvider.class);
    private static final String SPLIT_REGEXP = " *, *";
    private final NodeDao m_nodeDao;
    private final IpInterfaceDao m_ipInterfaceDao;

    private interface Icons {
        String DATACENTER = "vmware.DATACENTER_ICON";
        String DATASTORE = "vmware.DATASTORE_ICON";
        String NETWORK = "vmware.NETWORK_ICON";
        String VIRTUAL_MACHINE_UNKNOWN = "vmware.VIRTUALMACHINE_ICON_UNKNOWN";
        String VIRTUAL_MACHINE_ON = "vmware.VIRTUALMACHINE_ICON_ON";
        String VIRTUAL_MACHINE_OFF = "vmware.VIRTUALMACHINE_ICON_OFF";
        String VIRTUAL_MACHINE_SUSPENDED = "vmware.VIRTUALMACHINE_ICON_SUSPENDED";
        String HOSTSYSTEM_UNKNOWN = "vmware.HOSTSYSTEM_ICON_UNKNOWN";
        String HOSTSYSTEM_ON = "vmware.HOSTSYSTEM_ICON_ON";
        String HOSTSYSTEM_OFF = "vmware.HOSTSYSTEM_ICON_OFF";
        String HOSTSYSTEM_STANDBY = "vmware.HOSTSYSTEM_ICON_STANDBY";
    }

    private class ParsedEntity {
        private String entityId;
        private String entityName;
        private String entityType;

        public ParsedEntity(String string) {
            String[] splitBySlash = string.split("/");
            entityId = splitBySlash[0];
            entityType = entityId.split("-")[0];
            entityName = "unknown";

            if (splitBySlash.length > 1) {
                try {
                    entityName = new String(URLDecoder.decode(splitBySlash[1], StandardCharsets.UTF_8.name()));
                } catch (UnsupportedEncodingException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }

        public String getEntityId() {
            return entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        public String getEntityName() {
            return entityName;
        }

        public void setEntityName(String entityName) {
            this.entityName = entityName;
        }

        public String getEntityType() {
            return entityType;
        }

        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }
    }

    public VmwareTopologyProvider(NodeDao nodeDao, IpInterfaceDao ipInterfaceDao) {
        super(TOPOLOGY_NAMESPACE_VMWARE);
        m_nodeDao = Objects.requireNonNull(nodeDao);
        m_ipInterfaceDao = Objects.requireNonNull(ipInterfaceDao);
    }

    @Override
    public void refresh() {
        resetContainer();

        getEntities("HostSystem").stream().forEach(this::addHostSystem);
        getEntities("VirtualMachine").stream().forEach(this::addVirtualMachine);
    }

    private List<OnmsNode> getEntities(String entityType) {
        List<OnmsNode> entities = m_nodeDao.findAllByVarCharAssetColumn("vmwareManagedEntityType", entityType);
        if (entities.isEmpty()) {
            LOG.info("No entities of type '{}' with defined VMware assets fields found!", entityType);
        }
        return entities;
    }

    @Override
    public Defaults getDefaults() {
        return new Defaults()
                .withCriteria(() -> {
                    if (getVertices().isEmpty()) {
                        return Lists.newArrayList();
                    }
                    return getVertices().stream().filter(e -> Icons.DATACENTER.equals(e.getIconKey())).map(DefaultVertexHopCriteria::new).collect(Collectors.toList());
                });
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
        return getSelection(TOPOLOGY_NAMESPACE_VMWARE, selectedVertices, type);
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return Sets.newHashSet(ContentType.Alarm, ContentType.Node).contains(type);
    }

    private AbstractVertex createEntityVertex(String vertexId, String vertexName, String iconKey) {
        if (containsVertexId(vertexId)) {
            return (AbstractVertex) getVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId);
        }
        AbstractVertex vertex = new AbstractVertex(TOPOLOGY_NAMESPACE_VMWARE, vertexId, vertexName);
        vertex.setIconKey(iconKey);
        return vertex;

    }

    private AbstractVertex createDatacenterVertex(String vertexId, String vertexName) {
        return createEntityVertex(vertexId, vertexName, Icons.DATACENTER);
    }

    private AbstractVertex createNetworkVertex(String vertexId, String vertexName) {
        return createEntityVertex(vertexId, vertexName, Icons.NETWORK);
    }

    private AbstractVertex createDatastoreVertex(String vertexId, String vertexName) {
        return createEntityVertex(vertexId, vertexName, Icons.DATASTORE);
    }

    private AbstractVertex createVirtualMachineVertex(String vertexId, String vertexName, String primaryInterface, int id, String powerState) {
        AbstractVertex vertex;
        if ("poweredOn".equals(powerState)) {
            vertex = createEntityVertex(vertexId, vertexName, Icons.VIRTUAL_MACHINE_ON);
        } else if ("poweredOff".equals(powerState)) {
            vertex = createEntityVertex(vertexId, vertexName, Icons.VIRTUAL_MACHINE_OFF);
        } else if ("suspended".equals(powerState)) {
            vertex = createEntityVertex(vertexId, vertexName, Icons.VIRTUAL_MACHINE_SUSPENDED);
        } else {
            vertex = createEntityVertex(vertexId, vertexName, Icons.VIRTUAL_MACHINE_UNKNOWN);
        }
        vertex.setIpAddress(primaryInterface);
        vertex.setNodeID(id);
        return vertex;
    }

    private AbstractVertex createHostSystemVertex(String vertexId, String vertexName, String primaryInterface, int id, String powerState) {
        AbstractVertex vertex;
        if ("poweredOn".equals(powerState)) {
            vertex = createEntityVertex(vertexId, vertexName, Icons.HOSTSYSTEM_ON);
        } else if ("poweredOff".equals(powerState)) {
            vertex = createEntityVertex(vertexId, vertexName, Icons.HOSTSYSTEM_OFF);
        } else if ("standBy".equals(powerState)) {
            vertex = createEntityVertex(vertexId, vertexName, Icons.HOSTSYSTEM_STANDBY);
        } else {
            vertex = createEntityVertex(vertexId, vertexName, Icons.HOSTSYSTEM_UNKNOWN);
        }
        vertex.setIpAddress(primaryInterface);
        vertex.setNodeID(id);
        return vertex;
    }

    private Map<String, ParsedEntity> parseNodeAssets(OnmsNode onmsNode) {
        String vmwareTopologyInfo = onmsNode.getAssetRecord().getVmwareTopologyInfo().trim();

        return Arrays.stream(vmwareTopologyInfo.split(SPLIT_REGEXP))
                .map(ParsedEntity::new)
                .collect(Collectors.toMap(ParsedEntity::getEntityId, Function.identity()));
    }

    private void addHostSystem(OnmsNode hostSystem) {
        String vmwareManagementServer = hostSystem.getAssetRecord().getVmwareManagementServer().trim();
        String vmwareManagedObjectId = hostSystem.getAssetRecord().getVmwareManagedObjectId().trim();
        String vmwareState = hostSystem.getAssetRecord().getVmwareState().trim();
        String primaryInterface = "unknown";
        OnmsIpInterface ipInterface = m_ipInterfaceDao.findPrimaryInterfaceByNodeId(hostSystem.getId());

        if (ipInterface != null) {
            primaryInterface = ipInterface.getIpHostName();
        }

        AbstractVertex hostSystemVertex = createHostSystemVertex(
                vmwareManagementServer + "/" + vmwareManagedObjectId,
                hostSystem.getLabel(),
                primaryInterface,
                hostSystem.getId(),
                vmwareState
        );

        addVertices(hostSystemVertex);

        Map<String, ParsedEntity> parsedEntities = parseNodeAssets(hostSystem);

        String datacenterName = parsedEntities.values().stream().filter(e -> "datacenter".equals(e.getEntityType())).findFirst().map(e -> parsedEntities.get(e.getEntityId()).getEntityName() + " (" + vmwareManagementServer + ")").orElse("Datacenter (" + vmwareManagementServer + ")");

        AbstractVertex datacenterVertex = createDatacenterVertex(vmwareManagementServer, datacenterName);
        addVertices(datacenterVertex);

        if (!hostSystemVertex.equals(datacenterVertex)) {
            connectVertices(hostSystemVertex, datacenterVertex);
        }

        parsedEntities.values().stream().filter(e -> "network".equals(e.getEntityType())).forEach(
                e -> {
                    AbstractVertex networkVertex = createNetworkVertex(vmwareManagementServer + "/" + e.getEntityId(), parsedEntities.get(e.getEntityId()).getEntityName());
                    addVertices(networkVertex);

                    connectVertices(
                            vmwareManagementServer + "/" + vmwareManagedObjectId + "->" + e.getEntityId(),
                            hostSystemVertex,
                            networkVertex,
                            getNamespace()
                    );
                }
        );

        parsedEntities.values().stream().filter(e -> "datastore".equals(e.getEntityType())).forEach(
                e -> {
                    AbstractVertex datastoreVertex = createDatastoreVertex(vmwareManagementServer + "/" + e.getEntityId(), parsedEntities.get(e.getEntityId()).getEntityName());
                    addVertices(datastoreVertex);

                    connectVertices(
                            vmwareManagementServer + "/" + vmwareManagedObjectId + "->" + e.getEntityId(),
                            hostSystemVertex,
                            datastoreVertex,
                            getNamespace()
                    );
                }
        );
    }

    private void addVirtualMachine(OnmsNode virtualMachine) {
        String vmwareManagementServer = virtualMachine.getAssetRecord().getVmwareManagementServer().trim();
        String vmwareManagedObjectId = virtualMachine.getAssetRecord().getVmwareManagedObjectId().trim();
        String vmwareState = virtualMachine.getAssetRecord().getVmwareState().trim();
        String primaryInterface = "unknown";
        OnmsIpInterface ipInterface = m_ipInterfaceDao.findPrimaryInterfaceByNodeId(virtualMachine.getId());

        if (ipInterface != null) {
            primaryInterface = ipInterface.getIpHostName();
        }

        Map<String, ParsedEntity> parsedEntities = parseNodeAssets(virtualMachine);

        String vmwareHostSystemId = parsedEntities.values().stream().filter(e -> "host".equals(e.getEntityType())).findFirst().map(ParsedEntity::getEntityId).orElse(null);

        if (vmwareHostSystemId == null) {
            LOG.warn("Cannot find host system id for virtual machine {}/{}", vmwareManagementServer, vmwareManagedObjectId);
        }

        AbstractVertex virtualMachineVertex = createVirtualMachineVertex(vmwareManagementServer + "/" + vmwareManagedObjectId, virtualMachine.getLabel(), primaryInterface, virtualMachine.getId(), vmwareState);
        addVertices(virtualMachineVertex);

        if (!containsVertexId(vmwareManagementServer + "/" + vmwareHostSystemId)) {
            LOG.warn("Cannot find associated vertex for host system {}/{}", vmwareManagementServer, vmwareHostSystemId);
        }

        connectVertices(
                vmwareManagementServer + "/" + vmwareManagedObjectId + "->" + vmwareManagementServer + "/" + vmwareHostSystemId,
                virtualMachineVertex,
                getVertex(getNamespace(), vmwareManagementServer + "/" + vmwareHostSystemId),
                getNamespace()
        );
    }
}
