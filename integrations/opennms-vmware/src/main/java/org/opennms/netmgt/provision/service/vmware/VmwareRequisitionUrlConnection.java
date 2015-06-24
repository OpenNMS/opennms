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

package org.opennms.netmgt.provision.service.vmware;

import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;

import org.apache.commons.io.IOExceptionWithCause;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.url.GenericURLConnection;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.requisition.*;
import org.opennms.protocols.vmware.VmwareViJavaAccess;
import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.rmi.RemoteException;
import java.util.*;

/**
 * The Class VmwareRequisitionUrlConnection
 * 
 * <p>This class is used for the automtic requisition of Vmware related entities.</p>
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class VmwareRequisitionUrlConnection extends GenericURLConnection {
    /**
     * the logger
     */
    private Logger logger = LoggerFactory.getLogger(VmwareRequisitionUrlConnection.class);

    private static final String VMWARE_HOSTSYSTEM_SERVICES = "hostSystemServices";
    private static final String VMWARE_VIRTUALMACHINE_SERVICES = "virtualMachineServices";

    private String[] m_hostSystemServices;
    private String[] m_virtualMachineServices;

    private String m_hostname = null;
    private String m_username = null;
    private String m_password = null;
    protected String m_foreignSource = null;

    private boolean m_importVMPoweredOn = true;
    private boolean m_importVMPoweredOff = false;
    private boolean m_importVMSuspended = false;

    private boolean m_importHostPoweredOn = true;
    private boolean m_importHostPoweredOff = false;
    private boolean m_importHostStandBy = false;
    private boolean m_importHostUnknown = false;

    private boolean m_persistIPv4 = true;
    private boolean m_persistIPv6 = true;

    private boolean m_persistVMs = true;
    private boolean m_persistHosts = true;

    private boolean m_topologyPortGroups = false;
    private boolean m_topologyNetworks = true;
    private boolean m_topologyDatastores = true;

    /*
     * Host system managedObjectId to name mapping
     */

    private Map<String, String> m_hostSystemMap = new HashMap<String, String>();

    /**
     * the query arguments
     */
    private Map<String, String> m_args = null;

    /**
     * requisition object
     */
    private Requisition m_requisition = null;

    /**
     * Constructor for creating an instance of this class.
     *
     * @param url the URL to use
     * @throws MalformedURLException
     * @throws RemoteException
     */
    public VmwareRequisitionUrlConnection(URL url) throws MalformedURLException, RemoteException {
        super(url);

        m_hostname = url.getHost();

        m_username = getUsername();
        m_password = getPassword();

        m_args = getQueryArgs();

        boolean importVMOnly = queryParameter("importVMOnly", false);
        boolean importHostOnly = queryParameter("importHostOnly", false);

        if (importHostOnly && importVMOnly) {
            throw new MalformedURLException("importHostOnly and importVMOnly can't be true simultaneously");
        }
        if (importHostOnly) {
            m_persistVMs = false;
        }
        if (importVMOnly) {
            m_persistHosts = false;
        }

        boolean importIPv4Only = queryParameter("importIPv4Only", false);
        boolean importIPv6Only = queryParameter("importIPv6Only", false);

        if (importIPv4Only && importIPv6Only) {
            throw new MalformedURLException("importIPv4Only and importIPv6Only can't be true simultaneously");
        }
        if (importIPv4Only) {
            m_persistIPv6 = false;
        }
        if (importIPv6Only) {
            m_persistIPv4 = false;
        }

        m_topologyPortGroups = queryParameter("topologyPortGroups", false);
        m_topologyNetworks = queryParameter("topologyNetworks", true);
        m_topologyDatastores = queryParameter("topologyDatastores", true);

        m_importVMPoweredOn = queryParameter("importVMPoweredOn", true);
        m_importVMPoweredOff = queryParameter("importVMPoweredOff", false);
        m_importVMSuspended = queryParameter("importVMSuspended", false);

        m_importHostPoweredOn = queryParameter("importHostPoweredOn", true);
        m_importHostPoweredOff = queryParameter("importHostPoweredOff", false);
        m_importHostStandBy = queryParameter("importHostStandBy", false);
        m_importHostUnknown = queryParameter("importHostUnknown", false);

        if (queryParameter("importHostAll", false)) {
            m_importHostPoweredOn = true;
            m_importHostPoweredOff = true;
            m_importHostStandBy = true;
            m_importHostUnknown = true;
        }

        if (queryParameter("importVMAll", false)) {
            m_importVMPoweredOff = true;
            m_importVMPoweredOn = true;
            m_importVMSuspended = true;
        }

        String path = url.getPath();

        path = path.replaceAll("^/", "");
        path = path.replaceAll("/$", "");

        String[] pathElements = path.split("/");

        if (pathElements.length == 1) {
            if ("".equals(pathElements[0])) {
                m_foreignSource = "vmware-" + m_hostname;
            } else {
                m_foreignSource = pathElements[0];
            }
        } else {
            throw new MalformedURLException("Error processing path element of URL (vmware://username:password@host[/foreign-source]?keyA=valueA;keyB=valueB;...)");
        }
    }

    /**
     * Returns a boolean representation for a given on/off parameter.
     *
     * @param key          the parameter's name
     * @param defaultValue the default value to use
     * @return the boolean value
     */
    private boolean queryParameter(String key, boolean defaultValue) {
        if (m_args.get(key) == null) {
            return defaultValue;
        } else {
            String value = m_args.get(key).toLowerCase();

            return ("yes".equals(value) || "true".equals(value) || "on".equals(value) || "1".equals(value));
        }
    }

    @Override
    public void connect() throws IOException {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    private boolean reachableCimService(VmwareViJavaAccess vmwareViJavaAccess, HostSystem hostSystem, String ipAddress) {
        if (!vmwareViJavaAccess.setTimeout(3000)) {
            logger.warn("Error setting connection timeout");
        }

        List<CIMObject> cimObjects = null;
        try {
            cimObjects = vmwareViJavaAccess.queryCimObjects(hostSystem, "CIM_NumericSensor", ipAddress);
        } catch (ConnectException e) {
            return false;
        } catch (RemoteException e) {
            return false;
        } catch (CIMException e) {
            return false;
        }

        return cimObjects != null;
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

        if (managedEntity instanceof VirtualMachine) {
            boolean firstInterface = true;

            // add all given interfaces
            for (String ipAddress : ipAddresses) {

                try {
                    if ((m_persistIPv4 && InetAddressUtils.isIPv4Address(ipAddress)) || (m_persistIPv6 && InetAddressUtils.isIPv6Address(ipAddress))) {
                        InetAddress inetAddress = InetAddress.getByName(ipAddress);

                        if (!inetAddress.isLoopbackAddress()) {
                            RequisitionInterface requisitionInterface = new RequisitionInterface();
                            requisitionInterface.setIpAddr(ipAddress);

                            //  the first one will be primary
                            if (firstInterface) {
                                requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
                                for (String service : m_virtualMachineServices) {
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
                List<RequisitionInterface> requisitionInterfaceList = new ArrayList<RequisitionInterface>();
                RequisitionInterface primaryInterfaceCandidate = null;

                // add all given interfaces
                for (String ipAddress : ipAddresses) {

                    try {
                        if ((m_persistIPv4 && InetAddressUtils.isIPv4Address(ipAddress)) || (m_persistIPv6 && InetAddressUtils.isIPv6Address(ipAddress))) {
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

                    for (String service : m_hostSystemServices) {
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
        StringBuffer vmwareTopologyInfo = new StringBuffer();

        // putting parents to topology information
        ManagedEntity parentEntity = managedEntity.getParent();

        do {
            if (vmwareTopologyInfo.length() > 0) {
                vmwareTopologyInfo.append(", ");
            }
            try {
                if (parentEntity != null && parentEntity.getMOR() != null) {
                    vmwareTopologyInfo.append(parentEntity.getMOR().getVal() + "/" + URLEncoder.encode(parentEntity.getName(), "UTF-8"));
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
                if (m_topologyDatastores) {
                    for (Datastore datastore : hostSystem.getDatastores()) {
                        if (vmwareTopologyInfo.length() > 0) {
                            vmwareTopologyInfo.append(", ");
                        }
                        try {
                            vmwareTopologyInfo.append(datastore.getMOR().getVal() + "/" + URLEncoder.encode(datastore.getSummary().getName(), "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            logger.warn("Unsupported encoding '{}'", e.getMessage());
                        }
                    }
                }
            } catch (RemoteException e) {
                logger.warn("Cannot retrieve datastores for managedEntity '{}': '{}'", managedEntity.getMOR().getVal(), e.getMessage());
            }

            try {
                if (m_topologyNetworks) {
                    for (Network network : hostSystem.getNetworks()) {
                        if (vmwareTopologyInfo.length() > 0) {
                            vmwareTopologyInfo.append(", ");
                        }
                        try {
                            if (network instanceof DistributedVirtualPortgroup ? m_topologyPortGroups : true) {
                                vmwareTopologyInfo.append(network.getMOR().getVal() + "/" + URLEncoder.encode(network.getSummary().getName(), "UTF-8"));
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
                    if (m_topologyDatastores) {
                        for (Datastore datastore : virtualMachine.getDatastores()) {
                            if (vmwareTopologyInfo.length() > 0) {
                                vmwareTopologyInfo.append(", ");
                            }
                            try {
                                vmwareTopologyInfo.append(datastore.getMOR().getVal() + "/" + URLEncoder.encode(datastore.getSummary().getName(), "UTF-8"));
                            } catch (UnsupportedEncodingException e) {
                                logger.warn("Unsupported encoding '{}'", e.getMessage());
                            }
                        }
                    }
                } catch (RemoteException e) {
                    logger.warn("Cannot retrieve datastores for managedEntity '{}': '{}'", managedEntity.getMOR().getVal(), e.getMessage());
                }
                try {
                    if (m_topologyNetworks) {
                        for (Network network : virtualMachine.getNetworks()) {
                            if (vmwareTopologyInfo.length() > 0) {
                                vmwareTopologyInfo.append(", ");
                            }
                            try {
                                if (network instanceof DistributedVirtualPortgroup ? m_topologyPortGroups : true) {
                                    vmwareTopologyInfo.append(network.getMOR().getVal() + "/" + URLEncoder.encode(network.getSummary().getName(), "UTF-8"));
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
                    vmwareTopologyInfo.append(virtualMachine.getRuntime().getHost().getVal() + "/" + URLEncoder.encode(m_hostSystemMap.get(virtualMachine.getRuntime().getHost().getVal()), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    logger.warn("Unsupported encoding '{}'", e.getMessage());
                }
            } else {
                logger.error("Undefined type of managedEntity '{}'", managedEntity.getMOR().getType());

                return null;
            }
        }

        RequisitionAsset requisitionAssetHostname = new RequisitionAsset("vmwareManagementServer", m_hostname);
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

    /**
     * Builds the complete requisition object.
     *
     * @return the requisition object
     */
    private Requisition buildVMwareRequisition() {
        VmwareViJavaAccess vmwareViJavaAccess = null;

        // for now, set the foreign source to the specified vcenter host
        m_requisition = new Requisition(m_foreignSource);

        logger.debug("Creating new VIJava access object for host {} ...", m_hostname);
        if ((m_username == null || "".equals(m_username)) || (m_password == null || "".equals(m_password))) {
            try {
                vmwareViJavaAccess = new VmwareViJavaAccess(m_hostname);
            } catch (MarshalException e) {
                logger.warn("Error initialising VMware connection to '{}': '{}'", m_hostname, e.getMessage());
                return null;
            } catch (ValidationException e) {
                logger.warn("Error initialising VMware connection to '{}': '{}'", m_hostname, e.getMessage());
                return null;
            } catch (IOException e) {
                logger.warn("Error initialising VMware connection to '{}': '{}'", m_hostname, e.getMessage());
                return null;
            }
        } else {
            vmwareViJavaAccess = new VmwareViJavaAccess(m_hostname, m_username, m_password);
        }
        logger.debug("Successfully created new VIJava access object for host {}", m_hostname);

        logger.debug("Connecting VIJava access for host {} ...", m_hostname);
        try {
            vmwareViJavaAccess.connect();
        } catch (MalformedURLException e) {
            logger.warn("Error connecting VMware management server '{}': '{}'", m_hostname, e.getMessage());
            return null;
        } catch (RemoteException e) {
            logger.warn("Error connecting VMware management server '{}': '{}'", m_hostname, e.getMessage());
            return null;
        }
        logger.debug("Successfully connected VIJava access for host {}", m_hostname);

        logger.debug("Starting to enumerate VMware managed objects from host {} ...", m_hostname);
        try {
            int apiVersion = vmwareViJavaAccess.getMajorApiVersion();

            // get services to be added to host systems
            // m_hostSystemServices = getHostSystemServices(apiVersion);

            if (m_args != null && m_args.get(VMWARE_HOSTSYSTEM_SERVICES) != null) {
                m_hostSystemServices = m_args.get(VMWARE_HOSTSYSTEM_SERVICES).split(",");
            } else {
                m_hostSystemServices = new String[]{"VMware-ManagedEntity", "VMware-HostSystem", "VMwareCim-HostSystem"};
            }

            // get services to be added to virtual machines
            // m_virtualMachineServices = getVirtualMachineServices(apiVersion);

            if (m_args != null && m_args.get(VMWARE_VIRTUALMACHINE_SERVICES) != null) {
                m_virtualMachineServices = m_args.get(VMWARE_VIRTUALMACHINE_SERVICES).split(",");
            } else {
                m_virtualMachineServices = new String[]{"VMware-ManagedEntity", "VMware-VirtualMachine"};
            }

            logger.debug("Starting to iterate host system managed objects from host {} ...", m_hostname);
            iterateHostSystems(vmwareViJavaAccess, apiVersion);
            logger.debug("Done iterating host system managed objects from host {}", m_hostname);
            logger.debug("Starting to iterate VM managed objects from host {} ...", m_hostname);
            iterateVirtualMachines(vmwareViJavaAccess, apiVersion);
            logger.debug("Done iterating VM managed objects from host {}", m_hostname);
        } catch (RemoteException e) {
            logger.warn("Error retrieving managed objects from VMware management server '{}': '{}'", m_hostname, e.getMessage());
            return null;
        } finally {
            vmwareViJavaAccess.disconnect();
        }

        return m_requisition;
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

        if ("poweredOn".equals(powerState) && m_importHostPoweredOn) {
            return true;
        }
        if ("poweredOff".equals(powerState) && m_importHostPoweredOff) {
            return true;
        }
        if ("standBy".equals(powerState) && m_importHostStandBy) {
            return true;
        }
        if ("unknown".equals(powerState) && m_importHostUnknown) {
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

        if ("poweredOn".equals(powerState) && m_importVMPoweredOn) {
            return true;
        }
        if ("poweredOff".equals(powerState) && m_importVMPoweredOff) {
            return true;
        }
        if ("suspended".equals(powerState) && m_importVMSuspended) {
            return true;
        }

        return false;
    }

    /**
     * Iterates through the host systems and adds them to the requisition object.
     *
     * @param vmwareViJavaAccess the access/connection to use
     * @throws RemoteException
     */
    private void iterateHostSystems(VmwareViJavaAccess vmwareViJavaAccess, int apiVersion) throws RemoteException {
        ManagedEntity[] hostSystems;

        // search for host systems (esx hosts)
        logger.debug("Starting to iterate host systems on VMware host {} ...", m_hostname);
        hostSystems = vmwareViJavaAccess.searchManagedEntities("HostSystem");

        if (hostSystems != null) {

            for (ManagedEntity managedEntity : hostSystems) {
                HostSystem hostSystem = (HostSystem) managedEntity;
                logger.debug("Iterating host systems on VMware management server {} : {} (ID: {})", m_hostname, hostSystem.getName(), hostSystem.getMOR().getVal());

                m_hostSystemMap.put(hostSystem.getMOR().getVal(), hostSystem.getName());

                // check for correct key/value-pair
                if (checkHostPowerState(hostSystem) && checkForAttribute(hostSystem)) {
                    logger.debug("Adding Host System '{}' (ID: {})", hostSystem.getName(), hostSystem.getMOR().getVal());

                    // iterate over all service console networks and add interface Ip addresses
                    TreeSet<String> ipAddresses = vmwareViJavaAccess.getHostSystemIpAddresses(hostSystem);

                    // create the new node...
                    RequisitionNode node = createRequisitionNode(ipAddresses, hostSystem, apiVersion, vmwareViJavaAccess);

                    // add cpu
                    try {
                        node.putAsset(new RequisitionAsset("cpu", hostSystem.getHardware().getCpuInfo().getNumCpuCores() + " cores"));
                    } catch (Exception e) {
                        logger.debug("Can't find CPU information for {} (ID: {})", hostSystem.getName(), hostSystem.getMOR().getVal());
                    }

                    // add memory
                    try {
                        node.putAsset(new RequisitionAsset("ram", Math.round(hostSystem.getHardware().getMemorySize()/1000000f) + " MB"));
                    } catch (Exception e) {
                        logger.debug("Can't find Memory information for {} (ID: {})", hostSystem.getName(), hostSystem.getMOR().getVal());
                    }

                    // add vendor
                    /*
                    try {
                        node.putAsset(new RequisitionAsset("vendor", hostSystem.getHardware().getSystemInfo().getVendor()));
                    } catch (Exception e) {
                        logger.debug("Can't find vendor information for {}", hostSystem.getName());
                    }
                    */

                    // ...and add it to the requisition
                    if (node != null && m_persistHosts) {
                        m_requisition.insertNode(node);
                    }
                }
            }
        }
    }

    /**
     * Iterates through the virtual machines and adds them to the requisition object.
     *
     * @param vmwareViJavaAccess the access/connection to use
     * @throws RemoteException
     */
    private void iterateVirtualMachines(VmwareViJavaAccess vmwareViJavaAccess, int apiVersion) throws RemoteException {
        ManagedEntity[] virtualMachines;

        // search for all virtual machines
        virtualMachines = vmwareViJavaAccess.searchManagedEntities("VirtualMachine");

        if (virtualMachines != null) {

            // check for correct key/value-pair
            for (ManagedEntity managedEntity : virtualMachines) {
                VirtualMachine virtualMachine = (VirtualMachine) managedEntity;
                logger.debug("Iterating host systems on VMware management server {} : {} (ID: {})", m_hostname, virtualMachine.getName(), virtualMachine.getMOR().getVal());

                // import only when the specified attributes is set
                if (checkVMPowerState(virtualMachine) && checkForAttribute(virtualMachine)) {
                    logger.debug("Adding Virtual Machine '{}' (ID: {})", virtualMachine.getName(), virtualMachine.getMOR().getVal());

                    // iterate over all interfaces
                    TreeSet<String> ipAddresses = vmwareViJavaAccess.getVirtualMachineIpAddresses(virtualMachine);

                    // create the new node...
                    RequisitionNode node = createRequisitionNode(ipAddresses, virtualMachine, apiVersion, vmwareViJavaAccess);

                    // add the operating system
                    if (virtualMachine.getGuest().getGuestFullName() != null) {
                        node.putAsset(new RequisitionAsset("operatingSystem", virtualMachine.getGuest().getGuestFullName()));
                    }

                    // add cpu
                    try {
                        node.putAsset(new RequisitionAsset("cpu", virtualMachine.getConfig().getHardware().getNumCPU() + " vCPU"));
                    } catch (Exception e) {
                        logger.debug("Can't find CPU information for {} (ID: {})", virtualMachine.getName(), virtualMachine.getMOR().getVal());
                    }

                    // add memory
                    try {
                        node.putAsset(new RequisitionAsset("ram", virtualMachine.getConfig().getHardware().getMemoryMB() + " MB"));
                    } catch (Exception e) {
                        logger.debug("Can't find Memory information for {} (ID: {})", virtualMachine.getName(), virtualMachine.getMOR().getVal());
                    }

                    // ...and add it to the requisition
                    if (node != null && m_persistVMs) {
                        m_requisition.insertNode(node);
                    }
                }
            }
        }
    }

    /**
     * Checks whether an attribute/value is defined by a managed entity.
     * 
     * <p>The old implementation allows the user to specify only one parameter.</p>
     * <p>The new implementation allows the user to use a regular expression for the value:</p>
     * <ul><li>key=location&value=~North.*</li></ul>
     * <p>As an alternative, now it is possible to specify several parameters on the query.
     * The rule is to add an underscore character ('_') before the patameter's name and use similar rules for the value:</p>
     * <ul><li>_location=~North.*</li></ul>
     * <p>With the new parameter specification, it is possible to pass several attributes. The managed entity must match
     * all of them to be accepted.</p>
     * <p>The new specification will take precedence over the old specification. If the new specification is not being used,
     * the old one will be processed. Otherwise, the new one will be processed, and the old one will be ignored. There is no
     * way to use both at the same time.</p>
     *
     * @param managedEntity the managed entity to check
     * @return true if present and value is equal, false otherwise
     * @throws RemoteException
     */
    private boolean checkForAttribute(ManagedEntity managedEntity) throws RemoteException {
        logger.debug("Getting custom attributes from VMware management server {} : ManagedEntity {} (ID: {})", m_hostname, managedEntity.getName(), managedEntity.getMOR().getVal());
        Map<String,String> attribMap = getCustomAttributes(managedEntity);

        Set<String> keySet = new TreeSet<String>();
        for (String k : m_args.keySet()) {
            if (k.startsWith("_")) {
                keySet.add(k);
            }
        }

        if (!keySet.isEmpty()) {
            boolean ok = true;
            for (String keyName : keySet) {
                String attribValue = attribMap.get(StringUtils.removeStart(keyName, "_"));
                if (attribValue == null) {
                    ok = false;
                } else {
                    String keyValue = m_args.get(keyName);
                    if (keyValue.startsWith("~")) {
                        ok = ok && attribValue.matches(StringUtils.removeStart(keyValue, "~"));
                    } else {
                        ok = ok && attribValue.equals(keyValue);
                    }
                }
            }
            return ok;
        }

        String key = m_args.get("key");
        String value = m_args.get("value");

        // if key/value is not set, return true
        if (key == null && value == null) {
            return true;
        }

        // if only key or value is set, return false
        if (key == null || value == null) {
            return false;
        }

        // now search for the correct key/value pair
        String attribValue = attribMap.get(key);
        if (attribValue != null) {
            if (value.startsWith("~")) {
                return attribValue.matches(StringUtils.removeStart(value, "~"));
            } else {
                return attribValue.equals(value);
            }
        }

        return false;
    }

    /**
     * Gets the custom attributes.
     *
     * @param entity the entity
     * @return the custom attributes
     * @throws RemoteException the remote exception
     */
    private Map<String,String> getCustomAttributes(ManagedEntity entity) throws RemoteException {
        final Map<String,String> attributes = new TreeMap<String,String>();
        logger.debug("Getting custom attributes from VMware management server {} : ManagedEntity {} (ID: {})", m_hostname, entity.getName(), entity.getMOR().getVal());
        CustomFieldDef[] defs = entity.getAvailableField();
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

    /**
     * {@inheritDoc}
     * <p/>
     * Creates a ByteArrayInputStream implementation of InputStream of the XML
     * marshaled version of the Requisition class. Calling close on this stream
     * is safe.
     */
    @Override
    public InputStream getInputStream() throws IOException {

        InputStream stream = null;

        try {
            logger.debug("Getting existing requisition (if any) for VMware management server {}", m_hostname);
            Requisition curReq = getExistingRequisition();
            logger.debug("Building new requisition for VMware management server {}", m_hostname);
            Requisition newReq = buildVMwareRequisition();
            logger.debug("Finished building new requisition for VMware management server {}", m_hostname);
            if (curReq == null) {
                if (newReq == null) {
                    // FIXME Is this correct ? This is the old behavior
                    newReq = new Requisition(m_foreignSource);
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
            stream = new ByteArrayInputStream(jaxBMarshal(newReq).getBytes());
        } catch (Throwable e) {
            logger.warn("Problem getting input stream: '{}'", e);
            throw new IOExceptionWithCause("Problem getting input stream: " + e, e);
        }

        return stream;
    }

    protected Requisition getExistingRequisition() {
        Requisition curReq = null;
        try {
            ForeignSourceRepository repository = BeanUtils.getBean("daoContext", "deployedForeignSourceRepository", ForeignSourceRepository.class);
            if (repository != null) {
                curReq = repository.getRequisition(m_foreignSource);
            }
        } catch (Exception e) {
            logger.warn("Can't retrieve requisition {}", m_foreignSource);
        }
        return curReq;
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
        List<RequisitionMonitoredService> services = new ArrayList<RequisitionMonitoredService>();
        for (RequisitionMonitoredService svc : intf.getMonitoredServices()) {
            boolean found = false;
            for (String svcName : m_hostSystemServices) {
                if (svcName.trim().equals(svc.getServiceName())) {
                    found = true;
                    continue;
                }
            }
            for (String svcName : m_virtualMachineServices) {
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

    /**
     * Utility to marshal the Requisition class into XML.
     *
     * @param r the requisition object
     * @return a String of XML encoding the Requisition class
     * @throws javax.xml.bind.JAXBException
     */
    private String jaxBMarshal(Requisition r) throws JAXBException {
        return JaxbUtils.marshal(r);
    }
}
