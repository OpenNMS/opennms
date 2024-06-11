package org.opennms.netmgt.provision.service.vmware;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.utils.url.GenericURLFactory;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.dao.vmware.VmwareConfigDao;
import org.opennms.netmgt.provision.persist.RequisitionProviderRegistry;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.rpc.LocationAwareRequisitionClientImpl;
import org.opennms.netmgt.provision.persist.rpc.RequisitionRequestBuilderImpl;

public class NMS16320Test {
    public Map<String, String> parameters;
    public LocationAwareRequisitionClientImpl client;
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() {
        GenericURLFactory.initialize();
    }

    @Before
    public void before() throws Exception {
        tempFolder.create();
        final File keystoreFile = new File(tempFolder.getRoot(), "scv.jce");
        final SecureCredentialsVault secureCredentialsVault = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "notRealPassword");
        secureCredentialsVault.setCredentials("vmware", new Credentials("opennms-pittsboro", "p1tt5boro"));


        final VmwareServer vmwareServer1 = new VmwareServer();
        vmwareServer1.setHostname("my.first.vcenter.server.org");
        vmwareServer1.setUsername("${scv:vmware:username}");
        vmwareServer1.setPassword("${scv:vmware:password}");

        final VmwareServer vmwareServer2 = new VmwareServer();
        vmwareServer2.setHostname("my.second.vcenter.server.org");
        vmwareServer2.setUsername("opennms-fulda");
        vmwareServer2.setPassword("fu7da");

        final Map<String, VmwareServer> serverMap = new TreeMap<>();
        serverMap.put("my.first.vcenter.server.org", vmwareServer1);
        serverMap.put("my.second.vcenter.server.org", vmwareServer2);

        final VmwareConfigDao vmwareConfigDao = mock(VmwareConfigDao.class);
        when(vmwareConfigDao.getServerMap()).thenReturn(serverMap);

        client = mock(LocationAwareRequisitionClientImpl.class);

        final RequisitionProviderRegistry requisitionProviderRegistry = mock(RequisitionProviderRegistry.class);
        when(client.getRegistry()).thenReturn(requisitionProviderRegistry);
        when(requisitionProviderRegistry.getProviderByType("vmware")).thenReturn(new VmwareRequisitionProvider());
        when(client.requisition()).thenReturn(new RequisitionRequestBuilderImpl(client) {
            @Override
            public CompletableFuture<Requisition> execute() {
                NMS16320Test.this.parameters = parameters;
                return CompletableFuture.completedFuture(new Requisition());
            }
        });

        VmwareRequisitionUrlConnection.setVmwareConfigDao(vmwareConfigDao);
        VmwareRequisitionUrlConnection.setSecureCredentialsVault(secureCredentialsVault);
        VmwareRequisitionUrlConnection.setRequisitionProviderClient(client);
    }

    @Test
    public void testClient() throws Exception {
        new VmwareRequisitionUrlConnection(new URL("vmware://my.first.vcenter.server.org?location=Pittsboro")).getInputStream();
        Assert.assertEquals("my.first.vcenter.server.org", parameters.get("host"));
        Assert.assertEquals("Pittsboro", parameters.get("location"));
        Assert.assertEquals("opennms-pittsboro", parameters.get("username"));
        Assert.assertEquals("p1tt5boro", parameters.get("password"));
        parameters.clear();
        new VmwareRequisitionUrlConnection(new URL("vmware://my.second.vcenter.server.org?location=Fulda")).getInputStream();
        Assert.assertEquals("my.second.vcenter.server.org", parameters.get("host"));
        Assert.assertEquals("Fulda", parameters.get("location"));
        Assert.assertEquals("opennms-fulda", parameters.get("username"));
        Assert.assertEquals("fu7da", parameters.get("password"));
        parameters.clear();
        new VmwareRequisitionUrlConnection(new URL("vmware://my.third.vcenter.server.org?location=Apex&username=opennms-apex&password=ap3x")).getInputStream();
        Assert.assertEquals("my.third.vcenter.server.org", parameters.get("host"));
        Assert.assertEquals("Apex", parameters.get("location"));
        Assert.assertEquals("opennms-apex", parameters.get("username"));
        Assert.assertEquals("ap3x", parameters.get("password"));
        parameters.clear();
        new VmwareRequisitionUrlConnection(new URL("vmware://my.fourth.vcenter.server.org?username=opennms-default&password=d3fault")).getInputStream();
        Assert.assertEquals("my.fourth.vcenter.server.org", parameters.get("host"));
        Assert.assertNull(parameters.get("location"));
        Assert.assertEquals("opennms-default", parameters.get("username"));
        Assert.assertEquals("d3fault", parameters.get("password"));
    }
}
