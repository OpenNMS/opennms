package org.opennms.netmgt.provision.persist.requisition;

import java.util.stream.Collectors;

import org.opennms.netmgt.model.requisition.RequisitionEntity;
import org.opennms.netmgt.model.requisition.RequisitionInterfaceEntity;
import org.opennms.netmgt.model.requisition.RequisitionMonitoredServiceEntity;
import org.opennms.netmgt.model.requisition.RequisitionNodeEntity;

/**
 * Simply converts between types. Does not merge with persisted entities.
 *
 * @see RequisitionMerger
 */
public class RequisitionMapper {

    public static RequisitionNode toRestModel(RequisitionNodeEntity input) {
        if (input == null) {
            return null;
        }
        RequisitionNode node = new RequisitionNode();
        node.setCity(input.getCity());
        node.setBuilding(input.getBuilding());
        node.setForeignId(input.getForeignId());
        node.setLocation(input.getLocation());
        node.setNodeLabel(input.getNodeLabel());
        node.setParentForeignId(input.getParentForeignId());
        node.setParentForeignSource(input.getParentForeignSource());
        node.setParentNodeLabel(input.getParentNodeLabel());
        node.setAssets(input.getAssets().entrySet().stream().map(e -> new RequisitionAsset(e.getKey(), e.getValue())).collect(Collectors.toList()));
        node.setCategories(input.getCategories().stream().map(category -> new RequisitionCategory(category)).collect(Collectors.toList()));
        node.setInterfaces(input.getInterfaces().stream().map(it -> toRestModel(it)).collect(Collectors.toList()));
        return node;
    }

    public static Requisition toRestModel(RequisitionEntity input) {
        if (input == null) {
            return null;
        }
        Requisition requisition = new Requisition();
        if (requisition.getDate() != null) {
            requisition.setDate(input.getLastUpdate());
        }
        if (input.getLastImport() != null) {
            requisition.setLastImport(input.getLastImport());
        }
        requisition.setForeignSource(input.getForeignSource());
        requisition.setNodes(input.getNodes().stream().map(n -> toRestModel(n)).collect(Collectors.toList()));
        return requisition;
    }

    public static RequisitionInterface toRestModel(RequisitionInterfaceEntity input) {
        if (input == null) {
            return null;
        }
        RequisitionInterface output = new RequisitionInterface();
        output.setDescr(input.getDescription());
        output.setIpAddr(input.getIpAddress());
        output.setManaged(input.isManaged());
        output.setStatus(input.getStatus());
        output.setSnmpPrimary(input.getSnmpPrimary());
        output.setCategories(input.getCategories().stream().map(category -> new RequisitionCategory(category)).collect(Collectors.toList()));
        output.setMonitoredServices(input.getMonitoredServices().stream().map(service -> toRestModel(service)).collect(Collectors.toList()));
        return output;
    }

    public static RequisitionMonitoredService toRestModel(RequisitionMonitoredServiceEntity input) {
        if (input == null) {
            return null;
        }
        RequisitionMonitoredService output = new RequisitionMonitoredService();
        output.setServiceName(input.getServiceName());
        output.setCategories(input.getCategories().stream().map(category -> new RequisitionCategory(category)).collect(Collectors.toList()));
        return output;
    }

    public static RequisitionEntity toPersistenceModel(Requisition input) {
        if (input == null) {
            return null;
        }
        RequisitionEntity output = new RequisitionEntity();
        output.setForeignSource(input.getForeignSource());
        output.setLastUpdate(input.getDate());
        output.setLastImport(input.getLastImportAsDate());

        input.getNodes().stream().map(n -> toPersistenceModel(n)).forEach(n -> output.addNode(n));
        return output;
    }

    private static RequisitionNodeEntity toPersistenceModel(RequisitionNode input) {
        if (input == null) {
            return null;
        }
        RequisitionNodeEntity output = new RequisitionNodeEntity();
        output.setCity(input.getCity());
        output.setBuilding(input.getBuilding());
        output.setForeignId(input.getForeignId());
        output.setLocation(input.getLocation());
        output.setNodeLabel(input.getNodeLabel());
        output.setParentForeignId(input.getParentForeignId());
        output.setParentForeignSource(input.getParentForeignSource());
        output.setParentNodeLabel(input.getParentNodeLabel());
        output.setCategories(input.getCategories().stream().map(c -> c.getName()).collect(Collectors.toSet()));
        output.setAssets(input.getAssets().stream().collect(Collectors.toMap(x -> x.getName(), x -> x.getValue())));

        input.getInterfaces().stream().map(i -> toPersistenceModel(i)).forEach(i -> output.addInterface(i));
        return output;
    }

    private static RequisitionInterfaceEntity toPersistenceModel(RequisitionInterface input) {
        if (input == null) {
            return null;
        }
        RequisitionInterfaceEntity output = new RequisitionInterfaceEntity();
        output.setDescription(input.getDescr());
        output.setIpAddress(input.getIpAddr());
        output.setManaged(input.isManaged());
        output.setSnmpPrimary(input.getSnmpPrimary());
        output.setStatus(input.getStatus());
        output.setCategories(input.getCategories().stream().map(c -> c.getName()).collect(Collectors.toSet()));

        input.getMonitoredServices().stream().map(s -> toPersistenceModel(s)).forEach(s -> output.addMonitoredService(s));
        return output;
    }

    private static RequisitionMonitoredServiceEntity toPersistenceModel(RequisitionMonitoredService input) {
        if (input == null) {
            return null;
        }
        RequisitionMonitoredServiceEntity output = new RequisitionMonitoredServiceEntity();
        output.setServiceName(input.getServiceName());
        output.setCategories(input.getCategories().stream().map(c -> c.getName()).collect(Collectors.toSet()));
        return output;
    }
}