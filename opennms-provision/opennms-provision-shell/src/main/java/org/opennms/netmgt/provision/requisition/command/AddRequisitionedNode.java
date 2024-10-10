package org.opennms.netmgt.provision.requisition.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.*;
import org.opennms.netmgt.model.PrimaryType;


@Command(scope = "opennms", name = "add-requisitioned-node", description = "Add a node to a named requisition. If the requisition doesn't exist, it will be created.")
@Service
public class AddRequisitionedNode implements Action {

    @Reference
    private ForeignSourceRepository deployedForeignSourceRepository;

    @Option(name = "-r", aliases = "--requisition", description = "The requisition name in which to create this node", required = true)
    private String requisitionName;
    @Option(name = "-l", aliases = "--location", description = "Minion Location")
    String location = null;
    @Option(name = "-p", aliases = "--primary-interface", description = "The SNMP primary interface IP address", required = true)
    String primaryInterface;
    @Option(name = "-i", aliases = "--interface", description = "Non-primary interface IP address, multiple '-i' ok, e.g. \"-i 127.1.1.1 -i 127.2.2.2", multiValued = true)
    List<String> otherInterface = null;
    @Option(name = "-c", aliases = "--category", description = "Node categories, multiple '-c' ok, e.g. \"-c foo -c bar -c baz\"", multiValued = true)
    List<String> categories = null;
    @Option(name = "-a", aliases = "--asset", description = "Node assets, multiple '-a' ok, 'key=value' pairs, e.g. \"-a operatingSystem=Windows -a vendor=Microsoft\"", multiValued = true)
    List<String> assets = null;
    @Option(name = "-m", aliases = "--meta-data", description = "Node metadata, multiple '-m' ok, 'key=value' pairs, e.g. \"-m foo=bar -m baz=bif\"", multiValued = true)
    List<String> nodeMetaData = null;
    @Option(name = "-b", aliases = "--building", description = "The 'building' node attribute")
    String building = null;
    @Option(name = "-t", aliases = "--city", description = "The 'city' node attribute")
    String city = null;
    @Option(name = "-f", aliases = "--foreign-id", description = "The node foreign id")
    String foreignID = String.valueOf(System.currentTimeMillis());
    @Option(name = "-n", aliases = "--node-label", description = "The node label", required = true)
    String nodeLabel;
    @Option(name = "-x", aliases = "--parent-foreign-id", description = "Parent foreign ID, for path outage. Optional.")
    String parentForeignId = null;
    @Option(name = "-y", aliases = "--parent-foreign-source", description = "Parent Foreign Source, for path outage. Optional.")
    String parentForeignSource = null;
    @Option(name = "-z", aliases = "--parent-node-label", description = "Parent node label, for path outage. Optional.")
    String parentNodeLabel = null;
    @Option(name = "-s", aliases = "--services", description = "Monitored Services assigned to all interfaces, multiple '-s' ok, e.g. \"-s ICMP -s SNMP\"", multiValued = true)
    List<String> monitoredServices = null;
    @Option(name = "-v", aliases = "--verbose", description = "Be verbose; show us the node XML")
    private boolean verbose = false;

    public Object execute() {
        // check if the requisition exists
        Requisition theRequisition;
        try {
            if (doesRequisitionExist()) {
                theRequisition = deployedForeignSourceRepository.getRequisition(requisitionName);
            } else {
                // if not create it
                theRequisition = createNewRequisition(requisitionName);
            }

            // create the node
            RequisitionNode theNode = createNode();

            //create the primary interface
            List<RequisitionInterface> theInterfaces = new ArrayList<>();
            RequisitionInterface thePrimaryInterface = createPrimaryInterface();
            theInterfaces.add(thePrimaryInterface);

            //create any other interfaces
            if (otherInterface != null) {
                List<RequisitionInterface> theOtherInterfaces = createOtherInterface();
                for (RequisitionInterface theseInterfaces : theOtherInterfaces) {
                    theInterfaces.add(theseInterfaces);
                }
            }
            theNode.setInterfaces(theInterfaces);

            // do metadata
            if (nodeMetaData != null) {
                List<RequisitionMetaData> theMetaData = createNodeMetadata();
                theNode.setMetaData(theMetaData);
            }

            // do categories
            if (categories != null) {
                List<RequisitionCategory> theCategories = createCategories();
                theNode.setCategories(theCategories);
            }

            // do assets
            if (assets != null) {
                List<RequisitionAsset> theAssets = createAssets();
                theNode.setAssets(theAssets);
            }
            if (verbose) {
                System.out.println("Verbose Node XML:");
                System.out.println(JaxbUtils.marshal(theNode));
                System.out.println();
            }
            theRequisition.insertNode(theNode);
            theRequisition.validate();
            deployedForeignSourceRepository.save(theRequisition);
        }
        catch(Exception e) {
            System.out.println("Failed to add node \""+ nodeLabel +"\" to requisition \"" + requisitionName + "\" !");
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
        System.out.println("Successfully added node \""+ nodeLabel +"\" to requisition \"" + requisitionName + "\".");
        return null;
    }

    private boolean doesRequisitionExist() {
        // first things first.  Does this node requisition exist?
        boolean reqExists = true;
        Requisition someReq = null;
        try {
            someReq = deployedForeignSourceRepository.getRequisition(requisitionName);
        } catch (ForeignSourceRepositoryException e) {
            reqExists = false;
        }
        if (someReq == null) {
            reqExists = false;
        }
        return reqExists;
    }

    private Requisition createNewRequisition(String name) {
        // I think this is enough?
        ForeignSource newForeignSource = new ForeignSource();
        newForeignSource.setName(requisitionName);
        Requisition thisReq = new Requisition();
        thisReq.setForeignSource(requisitionName);
        deployedForeignSourceRepository.save(thisReq);
        return thisReq;
    }

    private RequisitionNode createNode() {
        //instantiate a new reqNode object
        final RequisitionNode theNode = new RequisitionNode();
        theNode.setNodeLabel(nodeLabel); // required
        theNode.setForeignId(foreignID); // not required, but generated if not provided
        if (location != null) {
            theNode.setLocation(location);
        }
        if (building != null) {
            theNode.setBuilding(building);
        }
        if (city != null) {
            theNode.setCity(city);
        }
        if (parentForeignId != null) {
            theNode.setParentForeignId(parentForeignId);
        }
        if (parentForeignSource != null) {
            theNode.setParentForeignSource(parentForeignSource);
        }
        if (parentNodeLabel != null) {
            theNode.setParentNodeLabel(parentNodeLabel);
        }
        return theNode;
    }
    private RequisitionInterface createPrimaryInterface() {
            // set up primary interface and services
            final RequisitionInterface intf = new RequisitionInterface();
            intf.setIpAddr(primaryInterface);
            intf.setSnmpPrimary(PrimaryType.PRIMARY);
            intf.setManaged(Boolean.TRUE);
            intf.setStatus(1);

            if (monitoredServices != null && !monitoredServices.isEmpty()) {
                for (String service : monitoredServices) {
                    service = service.trim();
                    intf.insertMonitoredService(new RequisitionMonitoredService(service));
                }
            }
            return intf;
        }
    private List<RequisitionInterface> createOtherInterface() {
            // set up secondary interface and services
            List<RequisitionInterface> intfs = new ArrayList<>();
            for (String thisInterface : otherInterface) {
                final RequisitionInterface intf = new RequisitionInterface();
                intf.setIpAddr(thisInterface);
                intf.setSnmpPrimary(PrimaryType.SECONDARY);
                intf.setManaged(Boolean.TRUE);
                intf.setStatus(1);

                if (monitoredServices != null && !monitoredServices.isEmpty()) {
                    for (String service : monitoredServices) {
                        service = service.trim();
                        intf.insertMonitoredService(new RequisitionMonitoredService(service));
                    }
                }
                intfs.add(intf);
            }
            return intfs;
        }
    private List<RequisitionMetaData> createNodeMetadata() {
        List<RequisitionMetaData> allThisMetaData = new ArrayList<>();
        try {
            for (String thisKVpair : nodeMetaData) {
                String[] KV = thisKVpair.split("=");
                RequisitionMetaData metadataKeyValue = new RequisitionMetaData();
                metadataKeyValue.setContext("requisition");
                metadataKeyValue.setKey(KV[0]);
                metadataKeyValue.setValue(KV[1]);
                allThisMetaData.add(metadataKeyValue);
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
        return allThisMetaData;
    }
    private List<RequisitionCategory> createCategories() {
        List<RequisitionCategory> allTheseCategories = new ArrayList<>();
        try {
            for (String cat : categories) {
                RequisitionCategory thisCat = new RequisitionCategory();
                thisCat.setName(cat);
                allTheseCategories.add(thisCat);
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
        return allTheseCategories;
    }
    private List<RequisitionAsset> createAssets() {
        List<RequisitionAsset> allTheseAssets = new ArrayList<>();
        try {
            for (String asset : assets) {
                String[] KV = asset.split("=");
                RequisitionAsset thisAsset = new RequisitionAsset();
                thisAsset.setName(KV[0]);
                thisAsset.setValue(KV[1]);
                allTheseAssets.add(thisAsset);
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
        return allTheseAssets;
    }
}