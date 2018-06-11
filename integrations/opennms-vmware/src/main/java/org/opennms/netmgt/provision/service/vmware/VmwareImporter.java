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

package org.opennms.netmgt.provision.service.vmware;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.protocols.vmware.VmwareViJavaAccess;
import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.CustomFieldDef;
import com.vmware.vim25.CustomFieldStringValue;
import com.vmware.vim25.CustomFieldValue;
import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostSystemPowerState;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.DistributedVirtualPortgroup;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * <p>Generates a requisition of Vmware related entities using
 * the configuration details given in the {@link VmwareImportRequest}.</p>
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class VmwareImporter {

    private static final Logger logger = LoggerFactory.getLogger(VmwareImporter.class);

    private final VmwareImportRequest request;

    /**
     * Host system managedObjectId to name mapping
     */
    private Map<String, String> m_hostSystemMap = new HashMap<String, String>();

    /**
     * requisition object
     */
    private Requisition m_requisition = null;

    public VmwareImporter(VmwareImportRequest request) {
        this.request = Objects.requireNonNull(request);
    }

    public Requisition getRequisition() {
        logger.debug("Getting existing requisition (if any) for VMware management server {}", request.getHostname());
        Requisition curReq = request.getExistingRequisition();
        logger.debug("Building new requisition for VMware management server {}", request.getHostname());
        Requisition newReq = buildVMwareRequisition();
        logger.debug("Finished building new requisition for VMware management server {}", request.getHostname());
        if (curReq == null) {
            if (newReq == null) {
                // FIXME Is this correct ? This is the old behavior
                newReq = new Requisition(request.getForeignSource());
            }
        } else {
            if (newReq == null) {
                // If there is a requisition and the vCenter is not responding for some reason, it is better to use the old requisition,
                // instead of returning an empty one, which can cause the lost of all the nodes from the DB.
                newReq = curReq;
            } else {
                // If there is already a requisition, retrieve the custom assets and categories from the old one, and put them on the new one.
                // The VMWare related assets and categories will be preserved.
                for (RequisitionNode newNode : newReq.getNodes()) {
                    for (RequisitionNode curNode : curReq.getNodes()) {
                        if (newNode.getForeignId().equals(curNode.getForeignId())) {
                            // Add existing custom assets
                            for (RequisitionAsset asset : curNode.getAssets()) {
                                if (!asset.getName().startsWith("vmware")) {
                                    newNode.putAsset(asset);
                                }
                            }
                            // Add existing custom categories
                            for (RequisitionCategory cat : curNode.getCategories()) {
                                if (!cat.getName().startsWith("VMWare")) {
                                    newNode.putCategory(cat);
                                }
                            }
                            // Add existing custom services
                            /*
                             * For each interface on the new requisition,
                             * - Retrieve the list of custom services from the corresponding interface on the existing requisition,
                             *   matching the interface by the IP address
                             * - If the list of services is not empty, add them to the new interface
                             */
                            for (RequisitionInterface intf : curNode.getInterfaces()) {
                                List<RequisitionMonitoredService> services = getManualyConfiguredServices(intf);
                                if (!services.isEmpty()) {
                                    RequisitionInterface newIntf = getRequisitionInterface(newNode, intf.getIpAddr());
                                    if (newIntf != null) {
                                        newIntf.getMonitoredServices().addAll(services);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return newReq;
    }

    /**
     * Builds the complete requisition object.
     *
     * @return the requisition object
     */
    private Requisition buildVMwareRequisition() {
        VmwareViJavaAccess vmwareViJavaAccess = null;

        // for now, set the foreign source to the specified vcenter host
        m_requisition = new Requisition(request.getForeignSource());

        logger.debug("Creating new VIJava access object for host {} ...", request.getHostname());
        if ((request.getHostname() == null || "".equals(request.getHostname())) || (request.getPassword() == null || "".equals(request.getPassword()))) {
            logger.info("No credentials found for connecting to host {}, trying anonymously...", request.getHostname());
            try {
                vmwareViJavaAccess = new VmwareViJavaAccess(request.getHostname());
            } catch (IOException e) {
                logger.warn("Error initialising VMware connection to '{}': '{}'", request.getHostname(), e.getMessage());
                return null;
            }
        } else {
            vmwareViJavaAccess = new VmwareViJavaAccess(request.getHostname(), request.getUsername(), request.getPassword());
        }
        logger.debug("Successfully created new VIJava access object for host {}", request.getHostname());

        logger.debug("Connecting VIJava access for host {} ...", request.getHostname());
        try {
            vmwareViJavaAccess.connect();
        } catch (MalformedURLException e) {
            logger.warn("Error connecting VMware management server '{}': '{}'", request.getHostname(), e.getMessage());
            return null;
        } catch (RemoteException e) {
            logger.warn("Error connecting VMware management server '{}': '{}'", request.getHostname(), e.getMessage());
            return null;
        }
        logger.debug("Successfully connected VIJava access for host {}", request.getHostname());

        logger.debug("Starting to enumerate VMware managed objects from host {} ...", request.getHostname());
        try {
            int apiVersion = vmwareViJavaAccess.getMajorApiVersion();
            logger.debug("Starting to iterate host system managed objects from host {} ...", request.getHostname());
            iterateHostSystems(vmwareViJavaAccess, apiVersion);
            logger.debug("Done iterating host system managed objects from host {}", request.getHostname());
            logger.debug("Starting to iterate VM managed objects from host {} ...", request.getHostname());
            iterateVirtualMachines(vmwareViJavaAccess, apiVersion);
            logger.debug("Done iterating VM managed objects from host {}", request.getHostname());
        } finally {
            vmwareViJavaAccess.disconnect();
        }

        return m_requisition;
    }

    /**
     * Creates a requisition node for the given managed entity and type.
     *
     * @param ipAddresses   the set of Ip addresses
     * @param managedEntity the managed entity
     * @return the generated requisition node
     */
    private RequisitionNode createRequisitionNode(Set<String> ipAddresses, ManagedEntity managedEntity, int apiVersion, VmwareViJavaAccess vmwareViJavaAccess) {
        RequisitionNode requisitionNode = new RequisitionNode();

        // Setting the node label
        requisitionNode.setNodeLabel(managedEntity.getName());

        // Foreign Id consisting of managed entity Id
        requisitionNode.setForeignId(managedEntity.getMOR().getVal());

        /*
         * Original version:
         *
         * Foreign Id consisting of VMware management server's hostname and managed entity id
         *
         * requisitionNode.setForeignId(m_hostname + "/" + managedEntity.getMOR().getVal());
         */
        logger.debug("Start primary interface search for managed entity '{}' (ID: {})", managedEntity.getName(), managedEntity.getMOR().getVal());

        if (managedEntity instanceof VirtualMachine) {
            boolean firstInterface = true;

            // add all given interfaces
            for (String ipAddress : ipAddresses) {

                try {
                    if ((request.isPersistIPv4() && InetAddressUtils.isIPv4Address(ipAddress)) || (request.isPersistIPv6() && InetAddressUtils.isIPv6Address(ipAddress))) {
                        InetAddress inetAddress = InetAddress.getByName(ipAddress);

                        if (!inetAddress.isLoopbackAddress()) {
                            RequisitionInterface requisitionInterface = new RequisitionInterface();
                            requisitionInterface.setIpAddr(ipAddress);

                            //  the first one will be primary
                            if (firstInterface) {
                                requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
                                for (String service : request.getVirtualMachineServices()) {
                                    requisitionInterface.insertMonitoredService(new RequisitionMonitoredService(service.trim()));
                                }
                                firstInterface = false;
                            } else {
                                requisitionInterface.setSnmpPrimary(PrimaryType.SECONDARY);
                            }

                            requisitionInterface.setManaged(Boolean.TRUE);
                            requisitionInterface.setStatus(Integer.valueOf(1));
                            requisitionNode.putInterface(requisitionInterface);
                        }
                    }
                } catch (UnknownHostException unknownHostException) {
                    logger.warn("Invalid IP address '{}'", unknownHostException.getMessage());
                }
            }
        } else {
            if (managedEntity instanceof HostSystem) {
                boolean reachableInterfaceFound = false, firstInterface = true;
                List<RequisitionInterface> requisitionInterfaceList = new ArrayList<>();
                RequisitionInterface primaryInterfaceCandidate = null;

                // add all given interfaces
                for (String ipAddress : ipAddresses) {

                    try {
                        if ((request.isPersistIPv4() && InetAddressUtils.isIPv4Address(ipAddress)) || (request.isPersistIPv6() && InetAddressUtils.isIPv6Address(ipAddress))) {
                            InetAddress inetAddress = InetAddress.getByName(ipAddress);

                            if (!inetAddress.isLoopbackAddress()) {
                                RequisitionInterface requisitionInterface = new RequisitionInterface();
                                requisitionInterface.setIpAddr(ipAddress);

                                if (firstInterface) {
                                    primaryInterfaceCandidate = requisitionInterface;
                                    firstInterface = false;
                                }

                                if (!reachableInterfaceFound && reachableCimService(vmwareViJavaAccess, (HostSystem) managedEntity, ipAddress)) {
                                    primaryInterfaceCandidate = requisitionInterface;
                                    reachableInterfaceFound = true;
                                }

                                requisitionInterface.setManaged(Boolean.TRUE);
                                requisitionInterface.setStatus(Integer.valueOf(1));
                                requisitionInterface.setSnmpPrimary(PrimaryType.SECONDARY);
                                requisitionInterfaceList.add(requisitionInterface);
                            }
                        }

                    } catch (UnknownHostException unknownHostException) {
                        logger.warn("Invalid IP address '{}'", unknownHostException.getMessage());
                    }
                }

                if (primaryInterfaceCandidate != null) {
                    if (reachableInterfaceFound) {
                        logger.warn("Found reachable primary interface '{}'", primaryInterfaceCandidate.getIpAddr());
                    } else {
                        logger.warn("Only non-reachable interfaces found, using first one for primary interface '{}'", primaryInterfaceCandidate.getIpAddr());
                    }
                    primaryInterfaceCandidate.setSnmpPrimary(PrimaryType.PRIMARY);

                    for (String service : request.getHostSystemServices()) {
                        if (reachableInterfaceFound || !"VMwareCim-HostSystem".equals(service)) {
                            primaryInterfaceCandidate.insertMonitoredService(new RequisitionMonitoredService(service.trim()));
                        }
                    }
                } else {
                    logger.warn("No primary interface found");
                }

                for (RequisitionInterface requisitionInterface : requisitionInterfaceList) {
                    requisitionNode.putInterface(requisitionInterface);
                }

            } else {
                logger.error("Undefined type of managedEntity '{}'", managedEntity.getMOR().getType());
                return null;
            }
        }

        /*
         * For now we use displaycategory, notifycategory and pollercategory for storing
         * the vcenter Ip address, the username and the password
         */

        String powerState = "unknown";
        final StringBuilder vmwareTopologyInfo = new StringBuilder();

        // putting parents to topology information
        ManagedEntity parentEntity = managedEntity.getParent();

        do {
            if (vmwareTopologyInfo.length() > 0) {
                vmwareTopologyInfo.append(", ");
            }
            try {
                if (parentEntity != null && parentEntity.getMOR() != null) {
                    vmwareTopologyInfo.append(parentEntity.getMOR().getVal() + "/" + URLEncoder.encode(parentEntity.getName(), StandardCharsets.UTF_8.name()));
                } else {
                    logger.warn("Can't add topologyInformation because either the parentEntity or the MOR is null for " + managedEntity.getName());
                }
            } catch (UnsupportedEncodingException e) {
                logger.warn("Unsupported encoding '{}'", e.getMessage());
            }
            parentEntity = parentEntity == null ? null : parentEntity.getParent();
        } while (parentEntity != null);

        if (managedEntity instanceof HostSystem) {

            HostSystem hostSystem = (HostSystem) managedEntity;

            HostRuntimeInfo hostRuntimeInfo = hostSystem.getRuntime();

            if (hostRuntimeInfo == null) {
                logger.debug("hostRuntimeInfo=null");
            } else {
                HostSystemPowerState hostSystemPowerState = hostRuntimeInfo.getPowerState();
                if (hostSystemPowerState == null) {
                    logger.debug("hostSystemPowerState=null");
                } else {
                    powerState = hostSystemPowerState.toString();
                }
            }

            try {
                if (request.isTopologyDatastores()) {
                    for (Datastore datastore : hostSystem.getDatastores()) {
                        if (vmwareTopologyInfo.length() > 0) {
                            vmwareTopologyInfo.append(", ");
                        }
                        try {
                            vmwareTopologyInfo.append(datastore.getMOR().getVal() + "/" + URLEncoder.encode(datastore.getSummary().getName(), StandardCharsets.UTF_8.name()));
                        } catch (UnsupportedEncodingException e) {
                            logger.warn("Unsupported encoding '{}'", e.getMessage());
                        }
                    }
                }
            } catch (RemoteException e) {
                logger.warn("Cannot retrieve datastores for managedEntity '{}': '{}'", managedEntity.getMOR().getVal(), e.getMessage());
            }

            try {
                if (request.isTopologyNetworks()) {
                    for (Network network : hostSystem.getNetworks()) {
                        if (vmwareTopologyInfo.length() > 0) {
                            vmwareTopologyInfo.append(", ");
                        }
                        try {
                            if (network instanceof DistributedVirtualPortgroup ? request.isTopologyPortGroups() : true) {
                                vmwareTopologyInfo.append(network.getMOR().getVal() + "/" + URLEncoder.encode(network.getSummary().getName(), StandardCharsets.UTF_8.name()));
                            }
                        } catch (UnsupportedEncodingException e) {
                            logger.warn("Unsupported encoding '{}'", e.getMessage());
                        }
                    }
                }
            } catch (RemoteException e) {
                logger.warn("Cannot retrieve networks for managedEntity '{}': '{}'", managedEntity.getMOR().getVal(), e.getMessage());
            }
        } else {

            if (managedEntity instanceof VirtualMachine) {
                VirtualMachine virtualMachine = (VirtualMachine) managedEntity;

                VirtualMachineRuntimeInfo virtualMachineRuntimeInfo = virtualMachine.getRuntime();

                if (virtualMachineRuntimeInfo == null) {
                    logger.debug("virtualMachineRuntimeInfo=null");
                } else {
                    VirtualMachinePowerState virtualMachinePowerState = virtualMachineRuntimeInfo.getPowerState();
                    if (virtualMachinePowerState == null) {
                        logger.debug("virtualMachinePowerState=null");
                    } else {
                        powerState = virtualMachinePowerState.toString();
                    }
                }

                try {
                    if (request.isTopologyDatastores()) {
                        for (Datastore datastore : virtualMachine.getDatastores()) {
                            if (vmwareTopologyInfo.length() > 0) {
                                vmwareTopologyInfo.append(", ");
                            }
                            try {
                                vmwareTopologyInfo.append(datastore.getMOR().getVal() + "/" + URLEncoder.encode(datastore.getSummary().getName(), StandardCharsets.UTF_8.name()));
                            } catch (UnsupportedEncodingException e) {
                                logger.warn("Unsupported encoding '{}'", e.getMessage());
                            }
                        }
                    }
                } catch (RemoteException e) {
                    logger.warn("Cannot retrieve datastores for managedEntity '{}': '{}'", managedEntity.getMOR().getVal(), e.getMessage());
                }
                try {
                    if (request.isTopologyNetworks()) {
                        for (Network network : virtualMachine.getNetworks()) {
                            if (vmwareTopologyInfo.length() > 0) {
                                vmwareTopologyInfo.append(", ");
                            }
                            try {
                                if (network instanceof DistributedVirtualPortgroup ? request.isTopologyPortGroups() : true) {
                                    vmwareTopologyInfo.append(network.getMOR().getVal() + "/" + URLEncoder.encode(network.getSummary().getName(), StandardCharsets.UTF_8.name()));
                                }
                            } catch (UnsupportedEncodingException e) {
                                logger.warn("Unsupported encoding '{}'", e.getMessage());
                            }
                        }
                    }
                } catch (RemoteException e) {
                    logger.warn("Cannot retrieve networks for managedEntity '{}': '{}'", managedEntity.getMOR().getVal(), e.getMessage());
                }

                if (vmwareTopologyInfo.length() > 0) {
                    vmwareTopologyInfo.append(", ");
                }

                try {
                    if (m_hostSystemMap.get(virtualMachine.getRuntime().getHost().getVal()) != null) {
                        vmwareTopologyInfo.append(virtualMachine.getRuntime().getHost().getVal() + "/" + URLEncoder.encode(m_hostSystemMap.get(virtualMachine.getRuntime().getHost().getVal()), StandardCharsets.UTF_8.name()));
                    } else {
                        logger.warn("Problem building topology information for virtual machine '{}' with power state '{}' running on host system '{}'", virtualMachine.getMOR().getVal(), powerState, virtualMachine.getRuntime().getHost().getVal());
                    }
                } catch (UnsupportedEncodingException e) {
                    logger.warn("Unsupported encoding '{}'", e.getMessage());
                }
            } else {
                logger.error("Undefined type of managedEntity '{}'", managedEntity.getMOR().getType());

                return null;
            }
        }

        RequisitionAsset requisitionAssetHostname = new RequisitionAsset("vmwareManagementServer", request.getHostname());
        requisitionNode.putAsset(requisitionAssetHostname);

        RequisitionAsset requisitionAssetType = new RequisitionAsset("vmwareManagedEntityType", (managedEntity instanceof HostSystem ? "HostSystem" : "VirtualMachine"));
        requisitionNode.putAsset(requisitionAssetType);

        RequisitionAsset requisitionAssetId = new RequisitionAsset("vmwareManagedObjectId", managedEntity.getMOR().getVal());
        requisitionNode.putAsset(requisitionAssetId);

        RequisitionAsset requisitionAssetTopologyInfo = new RequisitionAsset("vmwareTopologyInfo", vmwareTopologyInfo.toString());
        requisitionNode.putAsset(requisitionAssetTopologyInfo);

        RequisitionAsset requisitionAssetState = new RequisitionAsset("vmwareState", powerState);
        requisitionNode.putAsset(requisitionAssetState);

        requisitionNode.putCategory(new RequisitionCategory("VMware" + apiVersion));

        return requisitionNode;
    }

    private boolean reachableCimService(VmwareViJavaAccess vmwareViJavaAccess, HostSystem hostSystem, String ipAddress) {
        if (!vmwareViJavaAccess.setTimeout(3000)) {
            logger.warn("Error setting connection timeout");
        }

        logger.debug("Probing address {} for Host System '{}'", ipAddress, hostSystem.getName());

        List<CIMObject> cimObjects = null;
        try {
            cimObjects = vmwareViJavaAccess.queryCimObjects(hostSystem, "CIM_NumericSensor", ipAddress);
        } catch (ConnectException | RemoteException | CIMException e) {
            logger.debug("Error while probing for address {} for Host System '{}'", ipAddress, hostSystem.getName());
            logger.debug("Exception thrown while probing IP address: {}", e);
            return false;
        }

        return cimObjects != null;
    }

    /**
     * Checks whether the host system should be imported into the requisition.
     *
     * @param hostSystem the system to check
     * @return true for import, false otherwise
     */
    private boolean checkHostPowerState(HostSystem hostSystem) {
        logger.debug("Checking power state for host system {} (ID {})", hostSystem.getName(), hostSystem.getMOR().getVal());
        String powerState = hostSystem.getRuntime().getPowerState().toString();

        if ("poweredOn".equals(powerState) && request.isImportHostPoweredOn()) {
            return true;
        }
        if ("poweredOff".equals(powerState) && request.isImportHostPoweredOff()) {
            return true;
        }
        if ("standBy".equals(powerState) && request.isImportHostStandBy()) {
            return true;
        }
        if ("unknown".equals(powerState) && request.isImportHostUnknown()) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether the virtual machine should be imported into the requisition.
     *
     * @param virtualMachine the system to check
     * @return true for import, false otherwise
     */
    private boolean checkVMPowerState(VirtualMachine virtualMachine) {
        logger.debug("Checking power state for VM {} (ID: {})", virtualMachine.getName(), virtualMachine.getMOR().getVal());
        String powerState = virtualMachine.getRuntime().getPowerState().toString();

        if ("poweredOn".equals(powerState) && request.isImportVMPoweredOn()) {
            return true;
        }
        if ("poweredOff".equals(powerState) && request.isImportVMPoweredOff()) {
            return true;
        }
        if ("suspended".equals(powerState) && request.isImportVMSuspended()) {
            return true;
        }

        return false;
    }


    /**
     * Iterates through the host systems and adds them to the requisition object.
     *
     * @param vmwareViJavaAccess the access/connection to use
     */
    private void iterateHostSystems(VmwareViJavaAccess vmwareViJavaAccess, int apiVersion) {
        ManagedEntity[] managedEntities = null;

        logger.debug("Starting to iterate Host Systems on VMware host {} ...", request.getHostname());

        try {
            managedEntities = vmwareViJavaAccess.searchManagedEntities("HostSystem");
        } catch (RemoteException e) {
            logger.error("Error iterating Host Systems on VMware host {}", request.getHostname());
            logger.error("Exception thrown while iterating for Host Systems: ", e);
        }

        if (managedEntities != null) {

            Arrays.stream(managedEntities)
                    .map(m -> (HostSystem) m)
                    .forEach(hostSystem -> {
                                synchronized (m_hostSystemMap) {
                                    m_hostSystemMap.put(hostSystem.getMOR().getVal(), hostSystem.getName());
                                }
                            }
                    );

            final ExecutorService executor = Executors.newFixedThreadPool(Math.min(managedEntities.length, 10));

            final List<CompletableFuture<Optional<RequisitionNode>>> completableFutures = Arrays.stream(managedEntities)
                    .map(managedEntity -> (HostSystem) managedEntity)
                    .filter(hostSystem -> checkHostPowerState(hostSystem))
                    .filter(hostSystem -> checkForAttribute(hostSystem))
                    .map(hostSystem -> CompletableFuture.supplyAsync(() -> {
                        logger.debug("Adding Host System '{}' (ID: {})", hostSystem.getName(), hostSystem.getMOR().getVal());

                        TreeSet<String> ipAddresses = vmwareViJavaAccess.getHostSystemIpAddresses(hostSystem);
                        logger.debug("Found {} IP addresses for Host System '{}' (ID: {}): {}", ipAddresses.size(), hostSystem.getName(), hostSystem.getMOR().getVal(), ipAddresses);

                        RequisitionNode node = createRequisitionNode(ipAddresses, hostSystem, apiVersion, vmwareViJavaAccess);

                        try {
                            node.putAsset(new RequisitionAsset("cpu", hostSystem.getHardware().getCpuInfo().getNumCpuCores() + " cores"));
                        } catch (Exception e) {
                            logger.debug("Can't find CPU information for '{}' (ID: {})", hostSystem.getName(), hostSystem.getMOR().getVal());
                        }

                        try {
                            node.putAsset(new RequisitionAsset("ram", Math.round(hostSystem.getHardware().getMemorySize() / 1000000f) + " MB"));
                        } catch (Exception e) {
                            logger.debug("Can't find Memory information for '{}' (ID: {})", hostSystem.getName(), hostSystem.getMOR().getVal());
                        }

                        return request.isPersistHosts() ? Optional.of(node) : Optional.<RequisitionNode>empty();

                    }, executor))
                    .collect(Collectors.toList());

            completableFutures.stream()
                    .map(CompletableFuture::join)
                    .filter(Optional::isPresent)
                    .forEach(m -> {
                        synchronized (m_requisition) {
                            m_requisition.insertNode(m.get());
                        }
                    });

            executor.shutdown();
        }
    }

    /**
     * Iterates through the virtual machines and adds them to the requisition object.
     *
     * @param vmwareViJavaAccess the access/connection to use
     */
    private void iterateVirtualMachines(VmwareViJavaAccess vmwareViJavaAccess, int apiVersion) {
        ManagedEntity[] managedEntities = null;

        logger.debug("Starting to iterate Virtual Machines on VMware host {} ...", request.getHostname());

        try {
            managedEntities = vmwareViJavaAccess.searchManagedEntities("VirtualMachine");
        } catch (RemoteException e) {
            logger.error("Error iterating Virtual Machines on VMware host {}", request.getHostname());
            logger.error("Exception thrown while iterating for Virtual Machines: ", e);
        }

        if (managedEntities != null) {
            final ExecutorService executor = Executors.newFixedThreadPool(Math.min(managedEntities.length, 10));

            final List<CompletableFuture<Optional<RequisitionNode>>> completableFutures = Arrays.stream(managedEntities)
                    .map(managedEntity -> (VirtualMachine) managedEntity)
                    .filter(virtualMachine -> checkVMPowerState(virtualMachine))
                    .filter(virtualMachine -> checkForAttribute(virtualMachine))
                    .map(virtualMachine -> CompletableFuture.supplyAsync(() -> {
                        logger.debug("Adding Virtual Machine '{}' (ID: {})", virtualMachine.getName(), virtualMachine.getMOR().getVal());

                        TreeSet<String> ipAddresses = vmwareViJavaAccess.getVirtualMachineIpAddresses(virtualMachine);
                        logger.debug("Found {} IP addresses for Virtual Machine '{}' (ID: {}): {}", ipAddresses.size(), virtualMachine.getName(), virtualMachine.getMOR().getVal(), ipAddresses);

                        RequisitionNode node = createRequisitionNode(ipAddresses, virtualMachine, apiVersion, vmwareViJavaAccess);

                        if (virtualMachine.getGuest().getGuestFullName() != null) {
                            node.putAsset(new RequisitionAsset("operatingSystem", virtualMachine.getGuest().getGuestFullName()));
                        }

                        try {
                            node.putAsset(new RequisitionAsset("cpu", virtualMachine.getConfig().getHardware().getNumCPU() + " vCPU"));
                        } catch (Exception e) {
                            logger.debug("Can't find CPU information for {} (ID: {})", virtualMachine.getName(), virtualMachine.getMOR().getVal());
                        }

                        try {
                            node.putAsset(new RequisitionAsset("ram", virtualMachine.getConfig().getHardware().getMemoryMB() + " MB"));
                        } catch (Exception e) {
                            logger.debug("Can't find Memory information for {} (ID: {})", virtualMachine.getName(), virtualMachine.getMOR().getVal());
                        }

                        return request.isPersistVMs() ? Optional.of(node) : Optional.<RequisitionNode>empty();

                    }, executor))
                    .collect(Collectors.toList());

            completableFutures.stream()
                    .map(CompletableFuture::join)
                    .filter(Optional::isPresent)
                    .forEach(m -> {
                        synchronized (m_requisition) {
                            m_requisition.insertNode(m.get());
                        }
                    });

            executor.shutdown();
        }
    }

    /**
     * Checks whether an attribute/value is defined by a managed entity.
     * 
     * <p>The old implementation allows the user to specify only one parameter.</p>
     * <p>The new implementation allows the user to use a regular expression for the value:</p>
     * <ul><li>key=location&value=~North.*</li></ul>
     * <p>As an alternative, now it is possible to specify several parameters on the query.
     * The rule is to add an underscore character ('_') before the parameter's name and use similar rules for the value:</p>
     * <ul><li>_location=~North.*</li></ul>
     * <p>With the new parameter specification, it is possible to pass several attributes. The managed entity must match
     * all of them to be accepted.</p>
     * <p>The new specification will take precedence over the old specification. If the new specification is not being used,
     * the old one will be processed. Otherwise, the new one will be processed, and the old one will be ignored. There is no
     * way to use both at the same time.</p>
     *
     * @param managedEntity the managed entity to check
     * @return true if present and value is equal, false otherwise
     */
    private boolean checkForAttribute(ManagedEntity managedEntity) {
        logger.debug("Getting Managed entity custom attributes from VMware management server {} : ManagedEntity {} (ID: {})", request.getHostname(), managedEntity.getName(), managedEntity.getMOR().getVal());
        Map<String,String> attribMap = getCustomAttributes(managedEntity);

        // Using new parameter specification
        if (!request.getCustomAttributes().isEmpty()) {
            logger.debug("_[customAttributeName] provisioning attributes specified. Making sure Managed Entity {} has the requested custom attributes", managedEntity.getName());
            boolean ok = true;
            final Map<String, String> customAttributes = request.getCustomAttributesMap();
            for (String keyName : customAttributes.keySet()) {
                logger.debug("Looking up for custom attribute {} with value {}", keyName, customAttributes.get(keyName));
                String attribValue = attribMap.get(StringUtils.removeStart(keyName, "_"));
                if (attribValue == null) {
                    logger.debug("No custom attribute named {} found for Managed Entity {}", keyName, managedEntity.getName());
                    ok = false;
                } else {
                    String keyValue = customAttributes.get(keyName);
                    if (keyValue.startsWith("~")) {
                        ok = ok && attribValue.matches(StringUtils.removeStart(keyValue, "~"));
                    } else {
                        ok = ok && attribValue.equals(keyValue);
                    }
                }
            }
            return ok;
        }

        // Using old parameter specification
        String key = request.getOldKey();
        String value = request.getOldValue();
        if (key == null && value == null) {
            logger.debug("No custom attributes required for provisioning Managed Entity {}.", managedEntity.getName());
            return true;
        }
        if (key == null || value == null) {
            logger.error("Not provisioning Manged Entiry {}: Using old key/value parameters, but either 'key' or 'value' parameter isn't set.", managedEntity.getName());
            return false;
        }
        String attribValue = attribMap.get(key);
        if (attribValue != null) {
            if (value.startsWith("~")) {
                return attribValue.matches(StringUtils.removeStart(value, "~"));
            } else {
                return attribValue.equals(value);
            }
        }

        logger.debug("No custom attributes named {} found for Managed Entity {}", key, managedEntity.getName());
        return false;
    }

    /**
     * Gets the custom attributes.
     *
     * @param entity the entity
     * @return the custom attributes
     */
    private Map<String,String> getCustomAttributes(ManagedEntity entity) {
        final Map<String,String> attributes = new TreeMap<String,String>();
        logger.debug("Getting custom attributes from VMware management server {} : ManagedEntity {} (ID: {})", request.getHostname(), entity.getName(), entity.getMOR().getVal());
        CustomFieldDef[] defs = new CustomFieldDef[0];
        try {
            defs = entity.getAvailableField();
        } catch (RemoteException e) {
            logger.warn("Cannot fetch attributes for entity '{}' (ID: {})", entity.getName(), entity.getMOR().getVal());
            logger.warn("Exception thrown while fetching attributes: {}", e);
            return attributes;
        }
        CustomFieldValue[] values = entity.getCustomValue();
        for (int i = 0; defs != null && i < defs.length; i++) {
            String key = defs[i].getName();
            int targetIndex = defs[i].getKey();
            for (int j = 0; values != null && j < values.length; j++) {
                if (targetIndex == values[j].getKey()) {
                    attributes.put(key, ((CustomFieldStringValue) values[j]).getValue());
                }
            }
        }
        return attributes;
    }

    private RequisitionInterface getRequisitionInterface(RequisitionNode node, String ipAddr) {
        for (RequisitionInterface intf : node.getInterfaces()) {
            if (ipAddr.equals(intf.getIpAddr())) {
                return intf;
            }
        }
        return null;
    }

    private List<RequisitionMonitoredService> getManualyConfiguredServices(RequisitionInterface intf) {
        List<RequisitionMonitoredService> services = new ArrayList<>();
        for (RequisitionMonitoredService svc : intf.getMonitoredServices()) {
            boolean found = false;
            for (String svcName : request.getHostSystemServices()) {
                if (svcName.trim().equals(svc.getServiceName())) {
                    found = true;
                    continue;
                }
            }
            for (String svcName : request.getVirtualMachineServices()) {
                if (svcName.trim().equals(svc.getServiceName())) {
                    found = true;
                    continue;
                }
            }
            if (!found) {
                services.add(svc);
            }
        }
        return services;
    }

}
