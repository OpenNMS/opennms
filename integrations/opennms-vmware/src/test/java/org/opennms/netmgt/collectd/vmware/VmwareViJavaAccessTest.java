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

package org.opennms.netmgt.collectd.vmware;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createPartialMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.method;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.suppress;
import static org.powermock.api.easymock.PowerMock.verify;

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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.collectd.vmware.vijava.VmwarePerformanceValues;
import org.opennms.protocols.vmware.VmwareViJavaAccess;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
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
        mockPerformanceManager = createMock(PerformanceManager.class);

        // setup ServiceInstance mock
        mockServiceInstance = createMock(ServiceInstance.class);

        // setup ServerConnection
        mockServerConnection = new ServerConnection(new URL("https://hostname/sdk"), new VimPortType(new WSClient("https://hostname/sdk") {
            @Override
            protected SSLSocketFactory getTrustAllSocketFactory(boolean ignoreCert) throws RemoteException {
                return null;
            }
        }), mockServiceInstance);

        // setup AboutInfo
        mockAboutInfo = createMock(AboutInfo.class);

        expectNew(ServiceInstance.class, new Class<?>[]{URL.class, String.class, String.class}, new URL("https://hostname/sdk"), "username", "password").andReturn(mockServiceInstance).anyTimes();
        expect(mockServiceInstance.getServerConnection()).andReturn(mockServerConnection).anyTimes();
        expect(mockServiceInstance.getPerformanceManager()).andReturn(mockPerformanceManager).anyTimes();
        expect(mockServiceInstance.getAboutInfo()).andReturn(mockAboutInfo).anyTimes();

        managedEntity = new ManagedEntity(null, managedObjectReferenceManagedEntity);
        virtualMachine = new VirtualMachine(null, managedObjectReferenceVirtualMachine);
        hostSystem = new HostSystem(null, managedObjectReferenceHostSystem);

        // setup MorUtil

        mockStatic(MorUtil.class);
        expect(MorUtil.createExactManagedEntity(mockServerConnection, managedObjectReferenceManagedEntity)).andReturn(managedEntity).anyTimes();
        expect(MorUtil.createExactManagedEntity(mockServerConnection, managedObjectReferenceVirtualMachine)).andReturn(virtualMachine).anyTimes();
        expect(MorUtil.createExactManagedEntity(mockServerConnection, managedObjectReferenceHostSystem)).andReturn(hostSystem).anyTimes();

        // setup about info

        expect(mockAboutInfo.getApiVersion()).andReturn("2.x");
        expect(mockAboutInfo.getApiVersion()).andReturn("3.x");
        expect(mockAboutInfo.getApiVersion()).andReturn("4.x");
        expect(mockAboutInfo.getApiVersion()).andReturn("5.x");
        expect(mockAboutInfo.getApiVersion()).andReturn("6.x");
        expect(mockAboutInfo.getApiVersion()).andReturn("x");

        // setup performance data

        int refreshRate = 100;
        int metricCount = 15;
        int instanceModifier = 5;

        perfQuerySpec = new PerfQuerySpec();
        perfQuerySpec.setEntity(managedEntity.getMOR());
        perfQuerySpec.setMaxSample(new
                Integer(1)
        );
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
        mockPerfProviderSummary = createMock(PerfProviderSummary.class);

        expect(mockPerformanceManager.queryPerfProviderSummary(managedEntity)).andReturn(mockPerfProviderSummary).anyTimes();
        expect(mockPerfProviderSummary.getRefreshRate()).andReturn(refreshRate).anyTimes();
        expect(mockPerformanceManager.getPerfCounter()).andReturn(perfCounterInfos).anyTimes();
        expect(mockPerformanceManager.queryPerf(anyObject(PerfQuerySpec[].class))).andReturn(perfEntityMetricBases).anyTimes();

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
        mockHostSystem = createMock(HostSystem.class);

        // setup HostNetworkSystem
        mockHostNetworkSystem = createMock(HostNetworkSystem.class);

        // setup CIMClient
        mockCIMClient = createPartialMock(CIMClient.class, "enumerateInstances", "getSessionProperties");

        // setup the cim objects

        cimObjects = new ArrayList<>();

        int cimObjectCount = 5;

        for (int i = 0; i < cimObjectCount; i++) {
            CIMInstance cimInstance = new CIMInstance();
            cimInstance.setName("cimInstance" + i);
            cimObjects.add(cimInstance);
        }

        expect(mockHostSystem.getName()).andReturn("mockesxi01.local").anyTimes();
        expect(mockHostSystem.getHostNetworkSystem()).andReturn(mockHostNetworkSystem).anyTimes();
        expect(mockHostSystem.acquireCimServicesTicket()).andReturn(hostServiceTicket).anyTimes();
        expect(mockHostNetworkSystem.getNetworkInfo()).andReturn(hostNetworkInfo).anyTimes();
        expectNew(CIMClient.class, new Class<?>[]{CIMNameSpace.class, Principal.class, Object.class}, anyObject(), anyObject(), anyObject()).andReturn(mockCIMClient).anyTimes();

        suppress(method(CIMClient.class, "useMPost"));

        expect(mockCIMClient.enumerateInstances(new CIMObjectPath("cimClass"))).andReturn(Collections.enumeration(cimObjects)).anyTimes();

        SessionProperties sessionProperties = new SessionProperties();
        expect(mockCIMClient.getSessionProperties()).andReturn(sessionProperties).anyTimes();
    }

    @Test
    public void testGetPerfCounterInfoMap() {

        replay(mockPerformanceManager, mockServiceInstance, ServiceInstance.class);

        try {
            vmwareViJavaAccess.connect();
        } catch (MalformedURLException e) {
            Assert.fail(e.getMessage());
        } catch (RemoteException e) {
            Assert.fail(e.getMessage());
        }

        Map<Integer, PerfCounterInfo> returnedPerfCounterInfoMap = vmwareViJavaAccess.getPerfCounterInfoMap();

        Assert.assertEquals(returnedPerfCounterInfoMap.size(), perfCounterInfos.length);

        for (int i : returnedPerfCounterInfoMap.keySet()) {
            Assert.assertEquals(returnedPerfCounterInfoMap.get(i), perfCounterInfos[i]);
        }

        verify(mockPerformanceManager, mockServiceInstance, ServiceInstance.class);
    }

    @Test
    public void testGetManagedEntityByManagedObjectId() {
        replay(mockServiceInstance, MorUtil.class, ServiceInstance.class);

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

        verify(mockServiceInstance, MorUtil.class, ServiceInstance.class);
    }

    @Test
    public void testGetVirtualMachineByManagedObjectId() throws Exception {
        replay(mockServiceInstance, MorUtil.class, ServiceInstance.class);

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

        verify(mockServiceInstance, MorUtil.class, ServiceInstance.class);
    }

    @Test
    public void testGetHostSystemByManagedObjectId() throws Exception {
        replay(mockServiceInstance, MorUtil.class, ServiceInstance.class);

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

        verify(mockServiceInstance, MorUtil.class, ServiceInstance.class);
    }

    @Test
    public void testQueryPerformanceValues() {
        replay(mockPerformanceManager, mockPerfProviderSummary, mockServiceInstance, ServiceInstance.class);

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

        verify(mockPerformanceManager, mockPerfProviderSummary, mockServiceInstance, ServiceInstance.class);
    }

    @Test
    public void testQueryCimObjects() {
        replay(mockPerformanceManager, mockHostSystem, mockHostNetworkSystem, mockCIMClient, CIMClient.class, mockServiceInstance, ServiceInstance.class);

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

        verify(mockPerformanceManager, mockHostSystem, mockHostNetworkSystem, mockCIMClient, CIMClient.class, mockServiceInstance, ServiceInstance.class);
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
        replay(mockAboutInfo, mockServiceInstance, ServiceInstance.class);

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
        } catch (MalformedURLException e) {
            Assert.fail(e.getMessage());
        } catch (RemoteException e) {
            Assert.fail(e.getMessage());
        }
    }
}
