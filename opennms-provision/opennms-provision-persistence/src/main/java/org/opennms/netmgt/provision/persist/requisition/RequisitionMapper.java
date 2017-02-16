package org.opennms.netmgt.provision.persist.requisition;

import java.util.stream.Collectors;

import org.opennms.netmgt.model.requisition.OnmsRequisition;
import org.opennms.netmgt.model.requisition.OnmsRequisitionInterface;
import org.opennms.netmgt.model.requisition.OnmsRequisitionMonitoredService;
import org.opennms.netmgt.model.requisition.OnmsRequisitionNode;

public class RequisitionMapper {

    public static RequisitionNode toRestModel(OnmsRequisitionNode input) {
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

    public static Requisition toRestModel(OnmsRequisition input) {
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

    public static RequisitionInterface toRestModel(OnmsRequisitionInterface input) {
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

    public static RequisitionMonitoredService toRestModel(OnmsRequisitionMonitoredService input) {
        if (input == null) {
            return null;
        }
        RequisitionMonitoredService output = new RequisitionMonitoredService();
        output.setServiceName(input.getServiceName());
        output.setCategories(input.getCategories().stream().map(category -> new RequisitionCategory(category)).collect(Collectors.toList()));
        return output;
    }

    public static OnmsRequisition toPersistenceModel(Requisition input) {
        if (input == null) {
            return null;
        }
        OnmsRequisition output = new OnmsRequisition();
        output.setForeignSource(input.getForeignSource());
        output.setLastUpdate(input.getDate());
        output.setLastImport(input.getLastImportAsDate());

        input.getNodes().stream().map(n -> toPersistenceModel(n)).forEach(n -> output.addNode(n));
        return output;
    }

    public static OnmsRequisitionNode toPersistenceModel(RequisitionNode input) {
        if (input == null) {
            return null;
        }
        OnmsRequisitionNode output = new OnmsRequisitionNode();
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

    public static OnmsRequisitionInterface toPersistenceModel(RequisitionInterface input) {
        if (input == null) {
            return null;
        }
        OnmsRequisitionInterface output = new OnmsRequisitionInterface();
        output.setDescription(input.getDescr());
        output.setIpAddress(input.getIpAddr());
        output.setManaged(input.isManaged());
        output.setSnmpPrimary(input.getSnmpPrimary());
        output.setStatus(input.getStatus()); // TODO MVR wofür wird dieser verwendet?
        output.setCategories(input.getCategories().stream().map(c -> c.getName()).collect(Collectors.toSet()));

        input.getMonitoredServices().stream().map(s -> toPersistenceModel(s)).forEach(s -> output.addMonitoredService(s));
        return output;
    }

    public static OnmsRequisitionMonitoredService toPersistenceModel(RequisitionMonitoredService input) {
        if (input == null) {
            return null;
        }
        OnmsRequisitionMonitoredService output = new OnmsRequisitionMonitoredService();
        output.setServiceName(input.getServiceName());
        output.setCategories(input.getCategories().stream().map(c -> c.getName()).collect(Collectors.toSet()));
        return output;
    }
}