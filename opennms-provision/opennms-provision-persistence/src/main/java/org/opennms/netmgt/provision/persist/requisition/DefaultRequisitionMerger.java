/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist.requisition;

import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.RequisitionDao;
import org.opennms.netmgt.model.requisition.RequisitionEntity;
import org.opennms.netmgt.model.requisition.RequisitionInterfaceEntity;
import org.opennms.netmgt.model.requisition.RequisitionMonitoredServiceEntity;
import org.opennms.netmgt.model.requisition.RequisitionNodeEntity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Helper class to merge an incoming requisition objects to already persisted elements.
 * If an entity represented by the incoming object is not present in the database, it will be created.
 */
public class DefaultRequisitionMerger implements RequisitionMerger {

    @Autowired
    private RequisitionDao requisitionDao;

    @Override
    public RequisitionEntity mergeOrCreate(Requisition input) {
        final RequisitionEntity persistedRequisition = getOrCreateRequisition(input.getForeignSource());
        persistedRequisition.setForeignSource(input.getForeignSource());
        persistedRequisition.setLastUpdate(input.getDate());
        persistedRequisition.setLastImport(input.getLastImportAsDate());

        // Add or update nodes
        input.getNodes().forEach(inputNode -> mergeOrCreate(persistedRequisition, inputNode));

        // Remove not existing nodes
        persistedRequisition.getNodes().stream()
                .filter(n -> input.getNode(n.getForeignId()) == null)// does not exist anymore
                .collect(Collectors.toList())
                .forEach(n -> persistedRequisition.removeNode(n.getForeignId())); // so remove it

        return persistedRequisition;
    }

    @Override
    public RequisitionNodeEntity mergeOrCreate(RequisitionEntity parentRequisition, RequisitionNode inputNode) {
        Objects.requireNonNull(parentRequisition);

        final RequisitionNodeEntity persistedNode = getOrCreateNode(parentRequisition, inputNode.getForeignId());
        persistedNode.setCity(inputNode.getCity());
        persistedNode.setBuilding(inputNode.getBuilding());
        persistedNode.setForeignId(inputNode.getForeignId());
        persistedNode.setLocation(inputNode.getLocation());
        persistedNode.setNodeLabel(inputNode.getNodeLabel());
        persistedNode.setParentForeignId(inputNode.getParentForeignId());
        persistedNode.setParentForeignSource(inputNode.getParentForeignSource());
        persistedNode.setParentNodeLabel(inputNode.getParentNodeLabel());
        persistedNode.setCategories(inputNode.getCategories().stream().map(c -> c.getName()).collect(Collectors.toSet()));
        persistedNode.setAssets(inputNode.getAssets().stream().collect(Collectors.toMap(x -> x.getName(), x -> x.getValue())));

        // Add or update interfaces
        inputNode.getInterfaces().forEach(i -> mergeOrCreate(persistedNode, i));

        // Remove interfaces
        persistedNode.getInterfaces().stream()
                .filter(i -> inputNode.getInterface(i.getIpAddress()) == null) // does not exist anymore
                .collect(Collectors.toList())
                .forEach(i -> persistedNode.removeInterface(i)); // so remove it

        return persistedNode;
    }

    @Override
    public RequisitionInterfaceEntity mergeOrCreate(RequisitionNodeEntity parentNode, RequisitionInterface inputInterface) {
        Objects.requireNonNull(parentNode);

        final RequisitionInterfaceEntity persistedInterface = getOrCreateInterface(parentNode, inputInterface.getIpAddr());
        persistedInterface.setDescription(inputInterface.getDescr());
        persistedInterface.setIpAddress(inputInterface.getIpAddr());
        persistedInterface.setManaged(inputInterface.isManaged());
        persistedInterface.setSnmpPrimary(inputInterface.getSnmpPrimary());
        persistedInterface.setStatus(inputInterface.getStatus());
        persistedInterface.setCategories(inputInterface.getCategories().stream().map(c -> c.getName()).collect(Collectors.toSet()));

        // Add or update services
        inputInterface.getMonitoredServices().forEach(s -> mergeOrCreate(persistedInterface, s));

        // Remove services
        persistedInterface.getMonitoredServices().stream()
                .filter(s -> inputInterface.getMonitoredService(s.getServiceName()) == null) // Does not exist anymore
                .collect(Collectors.toList())
                .forEach(s -> persistedInterface.removeMonitoredService(s)); // so remove it

        return persistedInterface;
    }

    @Override
    public RequisitionMonitoredServiceEntity mergeOrCreate(RequisitionInterfaceEntity parentInterface, RequisitionMonitoredService inputService) {
        Objects.requireNonNull(parentInterface);

        RequisitionMonitoredServiceEntity persistedService = getOrCreateService(parentInterface, inputService.getServiceName());
        persistedService.setServiceName(inputService.getServiceName());
        persistedService.setCategories(inputService.getCategories().stream().map(c -> c.getName()).collect(Collectors.toSet()));

        return persistedService;

    }

    private RequisitionEntity getOrCreateRequisition(String foreignSource) {
        RequisitionEntity requisitionEntity = requisitionDao.get(foreignSource);
        if (requisitionEntity == null) {
            requisitionEntity = new RequisitionEntity();
        }
        return requisitionEntity;
    }

    private RequisitionNodeEntity getOrCreateNode(RequisitionEntity parentRequisition, String foreignId) {
        Objects.requireNonNull(parentRequisition);
        RequisitionNodeEntity nodeEntity = parentRequisition.getNode(foreignId);
        if (nodeEntity == null) {
            nodeEntity = new RequisitionNodeEntity();
            parentRequisition.addNode(nodeEntity);
        }
        return nodeEntity;
    }

    private RequisitionInterfaceEntity getOrCreateInterface(RequisitionNodeEntity parentNode, String ipAddress) {
        Objects.requireNonNull(parentNode);
        RequisitionInterfaceEntity interfaceEntity = parentNode.getInterface(ipAddress);
        if (interfaceEntity == null) {
            interfaceEntity = new RequisitionInterfaceEntity();
            parentNode.addInterface(interfaceEntity);
        }
        return interfaceEntity;
    }

    private RequisitionMonitoredServiceEntity getOrCreateService(RequisitionInterfaceEntity parentInterface, String serviceName) {
        Objects.requireNonNull(parentInterface);
        RequisitionMonitoredServiceEntity serviceEntity = parentInterface.getMonitoredService(serviceName);
        if (serviceEntity == null) {
            serviceEntity = new RequisitionMonitoredServiceEntity();
            parentInterface.addMonitoredService(serviceEntity);
        }
        return serviceEntity;
    }

}
