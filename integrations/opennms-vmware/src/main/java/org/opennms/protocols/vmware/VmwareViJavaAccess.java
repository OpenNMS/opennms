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
 * Additional permission under GNU AGPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with SBLIM (or a modified version of that library),
 * containing parts covered by the terms of the Eclipse Public License,
 * the licensors of this Program grant you additional permission to
 * convey the resulting work. {Corresponding Source for a non-source
 * form of such a combination shall include the source code for the
 * parts of SBLIM used as well as that of the covered work.}
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.vmware;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.AnyServerX509TrustManager;
import org.opennms.netmgt.collectd.vmware.vijava.VmwarePerformanceValues;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.dao.VmwareConfigDao;
import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMNameSpace;
import org.sblim.wbem.cim.CIMObject;
import org.sblim.wbem.cim.CIMObjectPath;
import org.sblim.wbem.cim.CIMProperty;
import org.sblim.wbem.cim.CIMValue;
import org.sblim.wbem.client.CIMClient;
import org.sblim.wbem.client.PasswordCredential;
import org.sblim.wbem.client.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.GuestNicInfo;
import com.vmware.vim25.HostNetworkInfo;
import com.vmware.vim25.HostServiceTicket;
import com.vmware.vim25.HostVirtualNic;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.mo.HostNetworkSystem;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServerConnection;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.util.MorUtil;
import com.vmware.vim25.ws.Client;

/**
 * The Class VmwareViJavaAccess
 * <p/>
 * This class provides all the functionality to query Vmware infrastructure components.
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
public class VmwareViJavaAccess {

    /**
     * logging for VMware library VI Java
     */
    private final Logger logger = LoggerFactory.getLogger(VmwareViJavaAccess.class);

    /**
     * the config dao
     */
    private VmwareConfigDao m_vmwareConfigDao = null;

    private String m_hostname = null;
    private String m_username = null;
    private String m_password = null;

    private ServiceInstance m_serviceInstance = null;

    private PerformanceManager m_performanceManager = null;

    private Map<Integer, PerfCounterInfo> m_perfCounterInfoMap = null;

    private Map<HostSystem, HostServiceTicket> m_hostServiceTickets = new HashMap<HostSystem, HostServiceTicket>();

    private Map<HostSystem, String> m_hostSystemCimUrls = new HashMap<HostSystem, String>();

    /**
     * Constructor for creating a instance for a given server and credentials.
     *
     * @param hostname the vCenter's hostname
     * @param username the username
     * @param password the password
     */
    public VmwareViJavaAccess(String hostname, String username, String password) {
        this.m_hostname = hostname;
        this.m_username = username;
        this.m_password = password;
    }

    /**
     * Constructor for creating a instance for a given server. Checks whether credentials
     * are available in the Vmware config file.
     *
     * @param hostname the vCenter's hostname
     * @throws IOException
     */
    public VmwareViJavaAccess(String hostname) throws IOException {
        if (m_vmwareConfigDao == null) {
            m_vmwareConfigDao = BeanUtils.getBean("daoContext", "vmwareConfigDao", VmwareConfigDao.class);
        }

        this.m_hostname = hostname;

        if (m_vmwareConfigDao == null) {
            logger.error("vmwareConfigDao should be a non-null value.");
        } else {
            Map<String, VmwareServer> serverMap = m_vmwareConfigDao.getServerMap();
            if (serverMap == null) {
                logger.error("Error getting vmware-config.xml's server map.");
            } else {
                VmwareServer vmwareServer = serverMap.get(m_hostname);

                if (vmwareServer == null) {
                    logger.error("Error getting credentials for VMware management server '{}'.", m_hostname);
                } else {
                    this.m_username = vmwareServer.getUsername();
                    this.m_password = vmwareServer.getPassword();
                }
            }
        }

        if (this.m_username == null) {
            logger.error("Error getting username for VMware management server '{}'.", m_hostname);
            this.m_username = "";
        }

        if (this.m_password == null) {
            logger.error("Error getting password for VMware management server '{}'.", m_hostname);
            this.m_password = "";
        }
    }

    public VmwareViJavaAccess(VmwareServer vmwareServer) {
        m_hostname = Objects.requireNonNull(vmwareServer).getHostname();
        m_username = vmwareServer.getUsername();
        m_password = vmwareServer.getPassword();
    }

    /**
     * Connects to the server.
     *
     * @throws MalformedURLException
     * @throws RemoteException
     */
    public void connect() throws MalformedURLException, RemoteException {
        relax();

        m_serviceInstance = new ServiceInstance(new URL("https://" + m_hostname + "/sdk"), m_username, m_password);
    }

    /**
     * Sets the timeout for server connections.
     *
     * @param timeout the timeout to be used for connecting
     * @return true, if the operation was successful
     */
    public boolean setTimeout(int timeout) {
        if (m_serviceInstance != null) {
            ServerConnection serverConnection = m_serviceInstance.getServerConnection();
            if (serverConnection != null) {
                VimPortType vimService = serverConnection.getVimService();
                if (vimService != null) {
                    Client client = vimService.getWsc();
                    if (client != null) {
                        client.setConnectTimeout(timeout);
                        client.setReadTimeout(timeout);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Disconnects from the server.
     */
    public void disconnect() {
        if (m_serviceInstance == null) {
            // not connected
            return;
        } else {
            ServerConnection serverConnection = m_serviceInstance.getServerConnection();

            if (serverConnection == null) {
                // not connected
                return;
            } else {
                m_serviceInstance.getServerConnection().logout();
            }
        }
    }

    /**
     * This method is used to "relax" the policies concerning self-signed certificates.
     */
    protected void relax() {

        TrustManager[] trustAllCerts = new TrustManager[]{new AnyServerX509TrustManager()};

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Exception exception) {
            logger.warn("Error setting relaxed SSL policy", exception);
        }

        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
    }

    /**
     * Retrieves the performance manager for this instance.
     *
     * @return the performance manager
     */
    private PerformanceManager getPerformanceManager() {
        if (m_performanceManager == null) {
            m_performanceManager = m_serviceInstance.getPerformanceManager();
        }

        return m_performanceManager;
    }

    /**
     * This method retrieves the performance counters available.
     *
     * @return a map of performance counters
     */
    public Map<Integer, PerfCounterInfo> getPerfCounterInfoMap() {
        if (m_perfCounterInfoMap == null) {
            m_perfCounterInfoMap = new HashMap<Integer, PerfCounterInfo>();

            PerfCounterInfo[] perfCounterInfos = getPerformanceManager().getPerfCounter();

            for (PerfCounterInfo perfCounterInfo : perfCounterInfos) {
                m_perfCounterInfoMap.put(perfCounterInfo.getKey(), perfCounterInfo);
            }
        }
        return m_perfCounterInfoMap;
    }

    /**
     * Returns a managed entitiy for a given managed object Id.
     *
     * @param managedObjectId the managed object Id
     * @return the managed entity
     */
    public ManagedEntity getManagedEntityByManagedObjectId(String managedObjectId) {
        ManagedObjectReference managedObjectReference = new ManagedObjectReference();

        managedObjectReference.setType("ManagedEntity");
        managedObjectReference.setVal(managedObjectId);

        ManagedEntity managedEntity = MorUtil.createExactManagedEntity(m_serviceInstance.getServerConnection(), managedObjectReference);

        return managedEntity;
    }

    /**
     * Returns a virtual machine by a given managed object Id.
     *
     * @param managedObjectId the managed object Id
     * @return the virtual machine object
     */
    public VirtualMachine getVirtualMachineByManagedObjectId(String managedObjectId) {
        ManagedObjectReference managedObjectReference = new ManagedObjectReference();

        managedObjectReference.setType("VirtualMachine");
        managedObjectReference.setVal(managedObjectId);

        VirtualMachine virtualMachine = (VirtualMachine) MorUtil.createExactManagedEntity(m_serviceInstance.getServerConnection(), managedObjectReference);

        return virtualMachine;
    }

    /**
     * Returns a host system by a given managed object Id.
     *
     * @param managedObjectId the managed object Id
     * @return the host system object
     */
    public HostSystem getHostSystemByManagedObjectId(String managedObjectId) {
        ManagedObjectReference managedObjectReference = new ManagedObjectReference();

        managedObjectReference.setType("HostSystem");
        managedObjectReference.setVal(managedObjectId);

        HostSystem hostSystem = (HostSystem) MorUtil.createExactManagedEntity(m_serviceInstance.getServerConnection(), managedObjectReference);

        return hostSystem;
    }

    /**
     * Generates a human-readable name for a performance counter.
     *
     * @param perfCounterInfo the perfomance counter info object
     * @return a string-representation of the performance counter's name
     */
    private String getHumanReadableName(PerfCounterInfo perfCounterInfo) {
        return perfCounterInfo.getGroupInfo().getKey() + "." + perfCounterInfo.getNameInfo().getKey() + "." + perfCounterInfo.getRollupType().toString();
    }

    /**
     * This method queries performance values for a given managed entity.
     *
     * @param managedEntity the managed entity to query
     * @return the perfomance values
     * @throws RemoteException
     */
    public VmwarePerformanceValues queryPerformanceValues(ManagedEntity managedEntity) throws RemoteException {

        VmwarePerformanceValues vmwarePerformanceValues = new VmwarePerformanceValues();

        int refreshRate = getPerformanceManager().queryPerfProviderSummary(managedEntity).getRefreshRate();

        PerfQuerySpec perfQuerySpec = new PerfQuerySpec();
        perfQuerySpec.setEntity(managedEntity.getMOR());
        perfQuerySpec.setMaxSample(Integer.valueOf(1));

        perfQuerySpec.setIntervalId(refreshRate);

        PerfEntityMetricBase[] perfEntityMetricBases = getPerformanceManager().queryPerf(new PerfQuerySpec[]{perfQuerySpec});

        if (perfEntityMetricBases != null) {
            for (int i = 0; i < perfEntityMetricBases.length; i++) {
                PerfMetricSeries[] perfMetricSeries = ((PerfEntityMetric) perfEntityMetricBases[i]).getValue();

                for (int j = 0; perfMetricSeries != null && j < perfMetricSeries.length; j++) {

                    if (perfMetricSeries[j] instanceof PerfMetricIntSeries) {
                        long[] longs = ((PerfMetricIntSeries) perfMetricSeries[j]).getValue();

                        if (longs.length == 1) {

                            PerfCounterInfo perfCounterInfo = getPerfCounterInfoMap().get(perfMetricSeries[j].getId().getCounterId());
                            String instance = perfMetricSeries[j].getId().getInstance();
                            String name = getHumanReadableName(perfCounterInfo);

                            if (instance != null && !"".equals(instance)) {
                                vmwarePerformanceValues.addValue(name, instance, longs[0]);
                            } else {
                                vmwarePerformanceValues.addValue(name, longs[0]);
                            }
                        }
                    }
                }
            }
        }

        return vmwarePerformanceValues;
    }

    /**
     * Queries a host system for Cim data.
     *
     * @param hostSystem       the host system to query
     * @param cimClass         the class of Cim objects to retrieve
     * @param primaryIpAddress the Ip address to use
     * @return the list of Cim objects
     * @throws RemoteException
     * @throws CIMException
     */
    public List<CIMObject> queryCimObjects(HostSystem hostSystem, String cimClass, String primaryIpAddress) throws ConnectException, RemoteException, CIMException {
        List<CIMObject> cimObjects = new ArrayList<>();

        if (!m_hostServiceTickets.containsKey(hostSystem)) {
            m_hostServiceTickets.put(hostSystem, hostSystem.acquireCimServicesTicket());
        }

        HostServiceTicket hostServiceTicket = m_hostServiceTickets.get(hostSystem);

        if (!m_hostSystemCimUrls.containsKey(hostSystem)) {
            String ipAddress = primaryIpAddress;

            if (ipAddress == null) {
                ipAddress = getPrimaryHostSystemIpAddress(hostSystem);
            }


            if (ipAddress == null) {
                logger.warn("Cannot determine ip address for host system '{}'", hostSystem.getMOR().getVal());
                return cimObjects;
            }

            m_hostSystemCimUrls.put(hostSystem, "https://" + ipAddress + ":5989");
        }

        String cimAgentAddress = m_hostSystemCimUrls.get(hostSystem);

        String namespace = "root/cimv2";

        UserPrincipal userPr = new UserPrincipal(hostServiceTicket.getSessionId());
        PasswordCredential pwCred = new PasswordCredential(hostServiceTicket.getSessionId().toCharArray());

        CIMNameSpace ns = new CIMNameSpace(cimAgentAddress, namespace);
        CIMClient cimClient = new CIMClient(ns, userPr, pwCred);

        cimClient.getSessionProperties().setHttpTimeOut(3000);

        // very important to query esx5 hosts
        cimClient.useMPost(false);
        CIMObjectPath rpCOP = new CIMObjectPath(cimClass);
        Enumeration<?> rpEnm = cimClient.enumerateInstances(rpCOP);
        while (rpEnm.hasMoreElements()) {
            CIMObject rp = (CIMObject) rpEnm.nextElement();
            cimObjects.add(rp);
        }

        return cimObjects;
    }

    /**
     * Queries a host system for Cim data.
     *
     * @param hostSystem the host system to query
     * @param cimClass   the class of Cim objects to retrieve
     * @return the list of Cim objects
     * @throws RemoteException
     * @throws CIMException
     */
    public List<CIMObject> queryCimObjects(HostSystem hostSystem, String cimClass) throws ConnectException, RemoteException, CIMException {
        return queryCimObjects(hostSystem, cimClass, null);
    }

    /**
     * Searches for the primary ip address of a host system.
     * <p>The idea is to resolve the HostSystem's name and use the resulting IP if the IP is listed on the available addresses list,
     * otherwise, use the first ip listed on the available list.</p>
     *
     * @param hostSystem the host system to query
     * @return the primary ip address
     */
    // TODO We should use the IP of the "Management Network" (i.e. the port that has enabled "Management Traffic" on the available vSwitches).
    //      Resolving the name of the HostSystem as the FQDN is the most closest thing for that.
    public String getPrimaryHostSystemIpAddress(HostSystem hostSystem) {
        TreeSet<String> addresses = getHostSystemIpAddresses(hostSystem);
        String ipAddress = null;
        try {
            ipAddress = InetAddress.getByName(hostSystem.getName()).getHostAddress();
        } catch (Exception e) {
            logger.debug("Can't resolve the IP address from {}.", hostSystem.getName());
        }
        if (ipAddress == null) {
            return addresses.first();
        }
        return addresses.contains(ipAddress) ? ipAddress : addresses.first();
    }

    /**
     * Searches for all ip addresses of a host system
     *
     * @param hostSystem the host system to query
     * @return the ip addresses of the host system, the first one is the primary
     */
    public TreeSet<String> getHostSystemIpAddresses(HostSystem hostSystem) {
        TreeSet<String> ipAddresses = new TreeSet<String>();

        HostNetworkSystem hostNetworkSystem = null;
        try {
            hostNetworkSystem = hostSystem.getHostNetworkSystem();
        } catch (RemoteException e) {
            logger.warn("Error fetching network information for Host System '{}' (ID: {})", hostSystem.getName(), hostSystem.getMOR().getVal());
            logger.warn("Exception thrown while fetching network information: {}", e);
            return ipAddresses;
        }

        if (hostNetworkSystem != null) {
            HostNetworkInfo hostNetworkInfo = hostNetworkSystem.getNetworkInfo();
            if (hostNetworkInfo != null) {
                HostVirtualNic[] hostVirtualNics = hostNetworkInfo.getConsoleVnic();
                if (hostVirtualNics != null) {
                    for (HostVirtualNic hostVirtualNic : hostVirtualNics) {
                        ipAddresses.add(hostVirtualNic.getSpec().getIp().getIpAddress());
                    }
                }
                hostVirtualNics = hostNetworkInfo.getVnic();
                if (hostVirtualNics != null) {
                    for (HostVirtualNic hostVirtualNic : hostVirtualNics) {
                        ipAddresses.add(hostVirtualNic.getSpec().getIp().getIpAddress());
                    }
                }
            }
        }
        return ipAddresses;
    }

    /**
     * Searches for all ip addresses of a virtual machine
     *
     * @param virtualMachine the virtual machine to query
     * @return the ip addresses of the virtual machine, the first one is the primary
     */
    public TreeSet<String> getVirtualMachineIpAddresses(VirtualMachine virtualMachine) {
        TreeSet<String> ipAddresses = new TreeSet<String>();

        // add the Ip address reported by VMware tools, this should be primary
        if (virtualMachine.getGuest().getIpAddress() != null) {
            ipAddresses.add(virtualMachine.getGuest().getIpAddress());
        }

        // if possible, iterate over all virtual networks networks and add interface Ip addresses
        if (virtualMachine.getGuest().getNet() != null) {
            for (GuestNicInfo guestNicInfo : virtualMachine.getGuest().getNet()) {
                if (guestNicInfo.getIpAddress() != null) {
                    for (String ipAddress : guestNicInfo.getIpAddress()) {
                        ipAddresses.add(ipAddress);
                    }
                }
            }
        }

        return ipAddresses;
    }

    /**
     * Searches for a managed entity by a given type.
     *
     * @param type the type string to search for
     * @return the list of managed entities found
     * @throws RemoteException
     */
    public ManagedEntity[] searchManagedEntities(String type) throws RemoteException {
        return (new InventoryNavigator(m_serviceInstance.getRootFolder())).searchManagedEntities(type);
    }

    /**
     * Return the major API version for this management server
     *
     * @return the major API version
     */
    public int getMajorApiVersion() {
        if (m_serviceInstance != null) {
            String apiVersion = m_serviceInstance.getAboutInfo().getApiVersion();

            String[] arr = apiVersion.split("\\.");

            if (arr.length > 1) {
                int apiMajorVersion = Integer.valueOf(arr[0]);

                if (apiMajorVersion < 4) {
                    apiMajorVersion = 3;
                }

                return apiMajorVersion;
            } else {
                logger.error("Cannot parse vCenter API version '{}'", apiVersion);

                return 0;
            }
        } else {
            return 0;
        }
    }

    /**
     * Returns the value of a given cim object and property.
     *
     * @param cimObject    the Cim object
     * @param propertyName the property's name
     * @return the value
     */
    public String getPropertyOfCimObject(CIMObject cimObject, String propertyName) {
        if (cimObject == null) {
            return null;
        } else {
            CIMProperty cimProperty = cimObject.getProperty(propertyName);
            if (cimProperty == null) {
                return null;
            } else {
                CIMValue cimValue = cimProperty.getValue();
                if (cimValue == null) {
                    return null;
                } else {
                    Object object = cimValue.getValue();
                    if (object == null) {
                        return null;
                    } else {
                        return object.toString();
                    }
                }
            }
        }
    }
}
