/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.vmware;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLSocketFactory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.collectd.vmware.vijava.VmwarePerformanceValues;
import org.opennms.protocols.vmware.ServiceInstancePool;
import org.opennms.protocols.vmware.VmwareViJavaAccess;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.sblim.wbem.cim.CIMDataType;
import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMNameSpace;
import org.sblim.wbem.cim.CIMObject;
import org.sblim.wbem.cim.CIMObjectPath;
import org.sblim.wbem.cim.CIMProperty;
import org.sblim.wbem.cim.CIMValue;
import org.sblim.wbem.client.CIMClient;
import org.sblim.wbem.util.SessionProperties;

import com.vmware.vim25.AboutInfo;
import com.vmware.vim25.ElementDescription;
import com.vmware.vim25.HostIpConfig;
import com.vmware.vim25.HostNetworkInfo;
import com.vmware.vim25.HostServiceTicket;
import com.vmware.vim25.HostVirtualNic;
import com.vmware.vim25.HostVirtualNicSpec;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PerfSummaryType;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.mo.HostNetworkSystem;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServerConnection;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.util.MorUtil;
import com.vmware.vim25.ws.WSClient;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceInstance.class, PerformanceManager.class, VmwareViJavaAccess.class, MorUtil.class, PerfProviderSummary.class, HostSystem.class, HostNetworkSystem.class, CIMClient.class})
public class VmwareViJavaAccessTest {

    private VmwareViJavaAccess vmwareViJavaAccess;
    private PerformanceManager mockPerformanceManager;
    private ServiceInstance mockServiceInstance;
    private ServerConnection mockServerConnection;
    private AboutInfo mockAboutInfo;
    private PerfProviderSummary mockPerfProviderSummary;
    private HostNetworkSystem mockHostNetworkSystem;
    private HostSystem mockHostSystem;
    private CIMClient mockCIMClient;

    private ManagedObjectReference managedObjectReferenceVirtualMachine;
    private ManagedObjectReference managedObjectReferenceHostSystem;
    private ManagedObjectReference managedObjectReferenceManagedEntity;

    private VirtualMachine virtualMachine;
    private HostSystem hostSystem;
    private ManagedEntity managedEntity;

    private PerfEntityMetricBase[] perfEntityMetricBases;
    private PerfCounterInfo[] perfCounterInfos;
    private PerfQuerySpec perfQuerySpec;
    private List<CIMObject> cimObjects;

    @Before
    public void setUp() throws Exception {
        // setup required objects

        managedObjectReferenceManagedEntity = new ManagedObjectReference();
        managedObjectReferenceManagedEntity.setType("ManagedEntity");
        managedObjectReferenceManagedEntity.setVal("moIdMe");

        managedObjectReferenceVirtualMachine = new ManagedObjectReference();
        managedObjectReferenceVirtualMachine.setType("VirtualMachine");
        managedObjectReferenceVirtualMachine.setVal("moIdVm");

        managedObjectReferenceHostSystem = new ManagedObjectReference();
        managedObjectReferenceHostSystem.setType("HostSystem");
        managedObjectReferenceHostSystem.setVal("moIdHs");

        // setup VmwareViJavaAccess
        vmwareViJavaAccess = new VmwareViJavaAccess("hostname", "username", "password") {
            @Override
            protected void relax() {
            }
        };

        // setup PerformanceManager
        mockPerformanceManager = mock(PerformanceManager.class);

        // setup ServiceInstance mock
        mockServiceInstance = mock(ServiceInstance.class);

        // setup ServerConnection
        mockServerConnection = new ServerConnection(new URL("https://hostname/sdk"), new VimPortType(new WSClient("https://hostname/sdk") {
            @Override
            protected SSLSocketFactory getTrustAllSocketFactory(boolean ignoreCert) throws RemoteException {
                return null;
            }
        }), mockServiceInstance);

        // setup AboutInfo
        mockAboutInfo = mock(AboutInfo.class);

        whenNew(ServiceInstance.class).withParameterTypes(URL.class, String.class, String.class).withArguments(new URL("https://hostname/sdk"), "username", "password").thenReturn(mockServiceInstance);
        when(mockServiceInstance.getServerConnection()).thenReturn(mockServerConnection);
        when(mockServiceInstance.getPerformanceManager()).thenReturn(mockPerformanceManager);
        when(mockServiceInstance.getAboutInfo()).thenReturn(mockAboutInfo);

        managedEntity = new ManagedEntity(null, managedObjectReferenceManagedEntity);
        virtualMachine = new VirtualMachine(null, managedObjectReferenceVirtualMachine);
        hostSystem = new HostSystem(null, managedObjectReferenceHostSystem);

        Whitebox.setInternalState(VmwareViJavaAccess.class, "m_serviceInstancePool", new ServiceInstancePool(){
            @Override
            public synchronized ServiceInstance retain(String host, String username, String password, int timeout) throws MalformedURLException, RemoteException {
                return mockServiceInstance;
            }
        });

        // setup MorUtil

        mockStatic(MorUtil.class);
        when(MorUtil.createExactManagedEntity(mockServerConnection, managedObjectReferenceManagedEntity)).thenReturn(managedEntity);
        when(MorUtil.createExactManagedEntity(mockServerConnection, managedObjectReferenceVirtualMachine)).thenReturn(virtualMachine);
        when(MorUtil.createExactManagedEntity(mockServerConnection, managedObjectReferenceHostSystem)).thenReturn(hostSystem);

        // setup about info

        when(mockAboutInfo.getApiVersion()).thenReturn("2.x", "3.x", "4.x", "5.x", "6.x", "x");

        // setup performance data

        int refreshRate = 100;
        int metricCount = 15;
        int instanceModifier = 5;

        perfQuerySpec = new PerfQuerySpec();
        perfQuerySpec.setEntity(managedEntity.getMOR());
        perfQuerySpec.setMaxSample(Integer.valueOf(1));
        perfQuerySpec.setIntervalId(refreshRate);

        perfEntityMetricBases = new PerfEntityMetricBase[metricCount];
        perfCounterInfos = new PerfCounterInfo[metricCount];

        for (int i = 0; i < metricCount; i++) {
            ElementDescription groupInfo = new ElementDescription();
            groupInfo.setKey("key" + i);

            ElementDescription nameInfo = new ElementDescription();
            nameInfo.setKey("name" + i);

            perfCounterInfos[i] = new PerfCounterInfo();
            perfCounterInfos[i].setKey(i);
            perfCounterInfos[i].setGroupInfo(groupInfo);
            perfCounterInfos[i].setNameInfo(nameInfo);
            perfCounterInfos[i].setRollupType(PerfSummaryType.average);

            perfEntityMetricBases[i] = new PerfEntityMetric();

            PerfMetricIntSeries[] perfMetricIntSeries;

            int instanceCount = (i % instanceModifier) + 1;

            perfMetricIntSeries = new PerfMetricIntSeries[instanceCount];

            for (int b = 0; b < instanceCount; b++) {
                PerfMetricId perfMetricId = new PerfMetricId();
                perfMetricId.setCounterId(i);

                if (instanceCount == 1) {
                    perfMetricId.setInstance(null);
                } else {
                    perfMetricId.setInstance("instance" + b);
                }

                perfMetricIntSeries[b] = new PerfMetricIntSeries();
                perfMetricIntSeries[b].setValue(new long[]{(long) 42});
                perfMetricIntSeries[b].setId(perfMetricId);
            }

            ((PerfEntityMetric) perfEntityMetricBases[i]).setValue(perfMetricIntSeries);
        }

        // setup PerfProviderSummary
        mockPerfProviderSummary = mock(PerfProviderSummary.class);

        when(mockPerformanceManager.queryPerfProviderSummary(managedEntity)).thenReturn(mockPerfProviderSummary);
        when(mockPerfProviderSummary.getRefreshRate()).thenReturn(refreshRate);
        when(mockPerformanceManager.getPerfCounter()).thenReturn(perfCounterInfos);
        when(mockPerformanceManager.queryPerf(any(PerfQuerySpec[].class))).thenReturn(perfEntityMetricBases);

        // setup network info

        HostNetworkInfo hostNetworkInfo = new HostNetworkInfo();

        int numberOfVnics = 3;

        HostVirtualNic[] hostVirtualNics = new HostVirtualNic[numberOfVnics];

        for (int i = 0; i < numberOfVnics; i++) {
            HostVirtualNicSpec hostVirtualNicSpec = new HostVirtualNicSpec();
            HostIpConfig hostIpConfig = new HostIpConfig();
            hostIpConfig.setIpAddress("192.168.1." + (i + 1));
            hostVirtualNicSpec.setIp(hostIpConfig);
            hostVirtualNics[i] = new HostVirtualNic();
            hostVirtualNics[i].setSpec(hostVirtualNicSpec);
        }

        hostNetworkInfo.setVnic(hostVirtualNics);

        HostVirtualNic[] hostVirtualConsoleNics = new HostVirtualNic[numberOfVnics];

        for (int i = 0; i < numberOfVnics; i++) {
            HostVirtualNicSpec hostVirtualNicSpec = new HostVirtualNicSpec();
            HostIpConfig hostIpConfig = new HostIpConfig();
            hostIpConfig.setIpAddress("192.168.2." + (i + 1));
            hostVirtualNicSpec.setIp(hostIpConfig);
            hostVirtualConsoleNics[i] = new HostVirtualNic();
            hostVirtualConsoleNics[i].setSpec(hostVirtualNicSpec);
        }

        hostNetworkInfo.setConsoleVnic(hostVirtualConsoleNics);

        HostServiceTicket hostServiceTicket = new HostServiceTicket();
        hostServiceTicket.setSessionId("sessionId");

        // setup HostSystem
        mockHostSystem = mock(HostSystem.class);

        // setup HostNetworkSystem
        mockHostNetworkSystem = mock(HostNetworkSystem.class);

        // setup CIMClient
        mockCIMClient = mock(CIMClient.class);

        // setup the cim objects

        cimObjects = new ArrayList<>();

        int cimObjectCount = 5;

        for (int i = 0; i < cimObjectCount; i++) {
            CIMInstance cimInstance = new CIMInstance();
            cimInstance.setName("cimInstance" + i);
            cimObjects.add(cimInstance);
        }

        when(mockHostSystem.getName()).thenReturn("mockesxi01.local");
        when(mockHostSystem.getHostNetworkSystem()).thenReturn(mockHostNetworkSystem);
        when(mockHostSystem.acquireCimServicesTicket()).thenReturn(hostServiceTicket);
        when(mockHostNetworkSystem.getNetworkInfo()).thenReturn(hostNetworkInfo);
        whenNew(CIMClient.class).withParameterTypes(CIMNameSpace.class, Principal.class, Object.class).withArguments(any(),  any(), any()).thenReturn(mockCIMClient);

        suppress(method(CIMClient.class, "useMPost"));

        when(mockCIMClient.enumerateInstances(new CIMObjectPath("cimClass"))).thenReturn(Collections.enumeration(cimObjects));

        SessionProperties sessionProperties = new SessionProperties();
        when(mockCIMClient.getSessionProperties()).thenReturn(sessionProperties);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockAboutInfo);
        verifyNoMoreInteractions(mockCIMClient);
        verifyNoMoreInteractions(mockHostNetworkSystem);
        verifyNoMoreInteractions(mockHostSystem);
        verifyNoMoreInteractions(mockPerformanceManager);
        verifyNoMoreInteractions(mockPerfProviderSummary);
        verifyNoMoreInteractions(mockServiceInstance);
    }

    @Test
    public void testGetPerfCounterInfoMap() {
        try {
            vmwareViJavaAccess.connect();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        Map<Integer, PerfCounterInfo> returnedPerfCounterInfoMap = vmwareViJavaAccess.getPerfCounterInfoMap();

        Assert.assertEquals(returnedPerfCounterInfoMap.size(), perfCounterInfos.length);

        for (int i : returnedPerfCounterInfoMap.keySet()) {
            Assert.assertEquals(returnedPerfCounterInfoMap.get(i), perfCounterInfos[i]);
        }

        verify(mockPerformanceManager, atLeastOnce()).getPerfCounter();
        verify(mockServiceInstance, atLeastOnce()).getPerformanceManager();
    }

    @Test
    public void testGetManagedEntityByManagedObjectId() {
        try {
            vmwareViJavaAccess.connect();
        } catch (MalformedURLException e) {
            Assert.fail(e.getMessage());
        } catch (RemoteException e) {
            Assert.fail(e.getMessage());
        }

        ManagedEntity returnedManagedEntity = vmwareViJavaAccess.getManagedEntityByManagedObjectId("moIdMe");

        Assert.assertNotNull(returnedManagedEntity);
        Assert.assertEquals(managedEntity.getMOR().getVal(), returnedManagedEntity.getMOR().getVal());

        verify(mockServiceInstance, atLeastOnce()).getServerConnection();
    }

    @Test
    public void testGetVirtualMachineByManagedObjectId() throws Exception {
        try {
            vmwareViJavaAccess.connect();
        } catch (MalformedURLException e) {
            Assert.fail(e.getMessage());
        } catch (RemoteException e) {
            Assert.fail(e.getMessage());
        }

        VirtualMachine returnedVirtualMachine = vmwareViJavaAccess.getVirtualMachineByManagedObjectId("moIdVm");

        Assert.assertNotNull(returnedVirtualMachine);
        Assert.assertEquals(virtualMachine.getMOR().getVal(), returnedVirtualMachine.getMOR().getVal());

        verify(mockServiceInstance, atLeastOnce()).getServerConnection();
    }

    @Test
    public void testGetHostSystemByManagedObjectId() throws Exception {
        try {
            vmwareViJavaAccess.connect();
        } catch (MalformedURLException e) {
            Assert.fail(e.getMessage());
        } catch (RemoteException e) {
            Assert.fail(e.getMessage());
        }

        HostSystem returnedHostSystem = vmwareViJavaAccess.getHostSystemByManagedObjectId("moIdHs");

        Assert.assertNotNull(returnedHostSystem);
        Assert.assertEquals(hostSystem.getMOR().getVal(), returnedHostSystem.getMOR().getVal());

        verify(mockServiceInstance, atLeastOnce()).getServerConnection();
    }

    @Test
    public void testQueryPerformanceValues() throws Exception {
        VmwarePerformanceValues vmwarePerformanceValues = null;

        try {
            vmwareViJavaAccess.connect();

            vmwarePerformanceValues = vmwareViJavaAccess.queryPerformanceValues(managedEntity);
        } catch (MalformedURLException e) {
            Assert.fail(e.getMessage());
        } catch (RemoteException e) {
            Assert.fail(e.getMessage());
        }

        Assert.assertNotNull(vmwarePerformanceValues);

        for (int i = 0; i < perfCounterInfos.length; i++) {
            PerfCounterInfo perfCounterInfo = perfCounterInfos[i];

            String expectedName = perfCounterInfo.getGroupInfo().getKey() + "." + perfCounterInfo.getNameInfo().getKey() + "." + perfCounterInfo.getRollupType().toString();

            if (vmwarePerformanceValues.hasInstances(expectedName)) {
                Set<String> instances = vmwarePerformanceValues.getInstances(expectedName);

                Assert.assertEquals(instances.size(), ((PerfEntityMetric) perfEntityMetricBases[i]).getValue().length);

                PerfMetricIntSeries[] perfMetricIntSeries = (PerfMetricIntSeries[]) ((PerfEntityMetric) perfEntityMetricBases[i]).getValue();

                for (int b = 0; b < perfMetricIntSeries.length; b++) {
                    Assert.assertTrue(instances.contains(perfMetricIntSeries[b].getId().getInstance()));
                }
            } else {
                Assert.assertEquals(1, ((PerfEntityMetric) perfEntityMetricBases[i]).getValue().length);
            }
        }

        verify(mockPerformanceManager, atLeastOnce()).getPerfCounter();
        verify(mockPerformanceManager, atLeastOnce()).queryPerfProviderSummary(any(ManagedEntity.class));
        verify(mockPerformanceManager, atLeastOnce()).queryPerf(any(PerfQuerySpec[].class));
        verify(mockPerfProviderSummary, atLeastOnce()).getRefreshRate();
        verify(mockServiceInstance, atLeastOnce()).getPerformanceManager();
    }

    @Test
    public void testQueryCimObjects() throws Exception {
        try {
            vmwareViJavaAccess.connect();
        } catch (MalformedURLException e) {
            Assert.fail(e.getMessage());
        } catch (RemoteException e) {
            Assert.fail(e.getMessage());
        }

        List<CIMObject> returnedCimObjects = null;

        try {
            returnedCimObjects = vmwareViJavaAccess.queryCimObjects(mockHostSystem, "cimClass");
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        Assert.assertNotNull(returnedCimObjects);
        Assert.assertEquals(returnedCimObjects.size(), cimObjects.size());

        for (CIMObject cimObject : cimObjects) {
            Assert.assertTrue(returnedCimObjects.contains(cimObject));
        }

        verify(mockCIMClient, atLeastOnce()).getSessionProperties();
        verify(mockCIMClient, atLeastOnce()).useMPost(false);
        verify(mockCIMClient, atLeastOnce()).useMPost(false);
        verify(mockCIMClient, atLeastOnce()).enumerateInstances(any(CIMObjectPath.class));
        verify(mockHostNetworkSystem, atLeastOnce()).getNetworkInfo();
        verify(mockHostSystem, atLeastOnce()).acquireCimServicesTicket();
        verify(mockHostSystem, atLeastOnce()).getHostNetworkSystem();
        verify(mockHostSystem, atLeastOnce()).getName();
    }

    @Test
    public void testSearchManagedEntities() throws Exception {
        // noop
    }

    @Test
    public void testGetPropertyOfCimObject() throws Exception {

        CIMObject obj1 = new CIMInstance();

        CIMProperty cimProperty1 = new CIMProperty();
        cimProperty1.setName("theKey");
        cimProperty1.setValue(new CIMValue("theValue", CIMDataType.getPredefinedType(CIMDataType.STRING)));
        obj1.addProperty(cimProperty1);

        CIMObject obj2 = new CIMInstance();

        CIMProperty cimProperty2 = new CIMProperty();
        cimProperty2.setName("theKey");
        cimProperty2.setValue(new CIMValue(null, CIMDataType.getPredefinedType(CIMDataType.STRING)));
        obj2.addProperty(cimProperty2);

        CIMObject obj3 = new CIMInstance();

        CIMProperty cimProperty3 = new CIMProperty();
        obj3.addProperty(cimProperty3);

        Assert.assertEquals(vmwareViJavaAccess.getPropertyOfCimObject(obj1, "theKey"), "theValue");
        Assert.assertNull(vmwareViJavaAccess.getPropertyOfCimObject(obj2, "theKey"));
        Assert.assertNull(vmwareViJavaAccess.getPropertyOfCimObject(obj3, "theKey"));
    }

    @Test
    public void testGetMajorApiVersion() throws Exception {
        try {
            vmwareViJavaAccess.connect();

            int majorApiVersion;

            majorApiVersion = vmwareViJavaAccess.getMajorApiVersion();
            Assert.assertEquals(majorApiVersion, 3);
            majorApiVersion = vmwareViJavaAccess.getMajorApiVersion();
            Assert.assertEquals(majorApiVersion, 3);
            majorApiVersion = vmwareViJavaAccess.getMajorApiVersion();
            Assert.assertEquals(majorApiVersion, 4);
            majorApiVersion = vmwareViJavaAccess.getMajorApiVersion();
            Assert.assertEquals(majorApiVersion, 5);
            majorApiVersion = vmwareViJavaAccess.getMajorApiVersion();
            Assert.assertEquals(majorApiVersion, 6);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        verify(mockAboutInfo, atLeastOnce()).getApiVersion();
        verify(mockServiceInstance, atLeastOnce()).getAboutInfo();
    }
}
