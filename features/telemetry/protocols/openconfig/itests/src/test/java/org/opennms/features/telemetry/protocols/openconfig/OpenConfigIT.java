/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.features.telemetry.protocols.openconfig;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.telemetry.config.dao.TelemetrydConfigDao;
import org.opennms.netmgt.telemetry.config.model.AdapterConfig;
import org.opennms.netmgt.telemetry.config.model.ConnectorConfig;
import org.opennms.netmgt.telemetry.config.model.PackageConfig;
import org.opennms.netmgt.telemetry.config.model.Parameter;
import org.opennms.netmgt.telemetry.config.model.QueueConfig;
import org.opennms.netmgt.telemetry.config.model.TelemetrydConfig;
import org.opennms.netmgt.telemetry.daemon.Telemetryd;
import org.opennms.netmgt.telemetry.protocols.openconfig.adapter.OpenConfigAdapter;
import org.opennms.netmgt.telemetry.protocols.openconfig.connector.OpenConfigConnector;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice-mq-vm.xml",
        "classpath:/META-INF/opennms/applicationContext-ipc-sink-camel-server.xml",
        "classpath:/META-INF/opennms/applicationContext-ipc-sink-camel-client.xml",
        "classpath:/META-INF/opennms/applicationContext-collectionAgentFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-openconfig-components.xml",
        "classpath:/META-INF/opennms/applicationContext-daoEvents.xml",
        "classpath:/META-INF/opennms/applicationContext-telemetryDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-thresholding.xml",
        "classpath:/META-INF/opennms/applicationContext-noOpBlobStore.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-testThresholdingDaos.xml",
})
@JUnitConfigurationEnvironment(systemProperties={ // We don't need a real pinger here
        "org.opennms.netmgt.icmp.pingerClass=org.opennms.netmgt.icmp.NullPinger"})
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class,reuseDatabase=false)
public class OpenConfigIT {

    @Autowired
    private TelemetrydConfigDao telemetrydConfigDao;

    @Autowired
    private Telemetryd telemetryd;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private ServiceTypeDao serviceTypeDao;

    @Autowired
    private InterfaceToNodeCache interfaceToNodeCache;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private OpenConfigTestServer server;

    public File scriptFile;

    private File rrdBaseDir;

    private int port;

    @Before
    public void setUp() throws IOException {
        rrdBaseDir = tempFolder.newFolder("rrd");
        scriptFile  = tempFolder.newFile("script-file.groovy");

        NetworkBuilder nb = new NetworkBuilder();
        nb.addNode("R1")
                .setForeignSource("Juniper")
                .setForeignId("1")
                .setType(OnmsNode.NodeType.ACTIVE);
        nb.addInterface("127.0.0.1");
        OnmsServiceType onmsServiceType = new OnmsServiceType("OpenConfig");
        serviceTypeDao.save(onmsServiceType);
        nb.addService(onmsServiceType);
        nodeDao.save(nb.getCurrentNode());

        // Resync after adding nodes/interfaces
        interfaceToNodeCache.dataSourceSync();
        this.port = OpenConfigTestServer.getAvailablePort(new AtomicInteger(50054), 51000);
        server = new OpenConfigTestServer(this.port);
        server.start();
    }

    @Test
    public void testOpenConfigForJti() throws Exception {

        // Use custom configuration to enable openconfig.
        updateDaoWithConfig(getConfig(true));
        // Start the daemon
        telemetryd.start();
        // Wait until the JRB archive is created
        await().atMost(30, TimeUnit.SECONDS).until(() -> rrdBaseDir.toPath()
                .resolve(Paths.get("1", "eth0", "ifInOctets.jrb")).toFile().canRead(), equalTo(true));
    }

    @Test
    public void testOpenConfigForGnmi() throws Exception {

        // Use custom configuration to enable openconfig.
        updateDaoWithConfig(getConfig(false));
        // Start the daemon
        telemetryd.start();
        // Wait until the JRB archive is created
        await().atMost(30, TimeUnit.SECONDS).until(() -> rrdBaseDir.toPath()
                .resolve(Paths.get("1", "eth1", "ifInOctets.jrb")).toFile().canRead(), equalTo(true));
    }

    private void updateDaoWithConfig(TelemetrydConfig config) throws IOException {
        final File tempFile = tempFolder.newFile();
        JaxbUtils.marshal(config, tempFile);
        telemetrydConfigDao.setConfigResource(new FileSystemResource(tempFile));
        telemetrydConfigDao.afterPropertiesSet();
    }

    private TelemetrydConfig getConfig(boolean jti) throws IOException {
        TelemetrydConfig telemetrydConfig = new TelemetrydConfig();

        QueueConfig openConfigQueue = new QueueConfig();
        openConfigQueue.setName("OpenConfig");
        telemetrydConfig.getQueues().add(openConfigQueue);

        ConnectorConfig connectorConfig = new ConnectorConfig();
        connectorConfig.setName("OpenConfig-Connector");
        connectorConfig.setClassName(OpenConfigConnector.class.getCanonicalName());
        connectorConfig.setEnabled(true);
        connectorConfig.setServiceName("OpenConfig");
        connectorConfig.setQueue(openConfigQueue);
        telemetrydConfig.getConnectors().add(connectorConfig);

        PackageConfig connectorPackage = new PackageConfig();
        connectorPackage.setName("OpenConfig-Default");
        connectorPackage.setFilter(new PackageConfig.Filter("IPADDR != '0.0.0.0'"));
        if (jti) {
            connectorPackage.getParameters().add(new Parameter("mode", "JTI"));
        }
        String port = Integer.toString(this.port);
        connectorPackage.getParameters().add(new Parameter("port", port));
        connectorPackage.getParameters().add(new Parameter("group1","paths", "/interfaces"));
        connectorPackage.getParameters().add(new Parameter("group1", "frequency", "5000"));
        connectorConfig.getPackages().add(connectorPackage);

        if(jti) {
            Files.copy(
                    Paths.get(System.getProperty("opennms.home"),
                            "etc",
                            "telemetryd-adapters",
                            "openconfig-jti-telemetry.groovy"),
                    scriptFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } else {
            Files.copy(
                    Paths.get(System.getProperty("opennms.home"),
                            "etc",
                            "telemetryd-adapters",
                            "openconfig-gnmi-telemetry.groovy"),
                    scriptFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        assertTrue("Can't read: " + scriptFile.getAbsolutePath(), scriptFile.canRead());

        AdapterConfig openConfigAdapter = new AdapterConfig();
        openConfigAdapter.setEnabled(true);
        openConfigAdapter.setName("OpenConfig-Adapter");
        openConfigAdapter.setClassName(OpenConfigAdapter.class.getCanonicalName());
        openConfigAdapter.getParameters().add(new Parameter("script", scriptFile.getAbsolutePath()));
        if (jti) {
            openConfigAdapter.getParameters().add(new Parameter("mode", "JTI"));
        }
        openConfigQueue.getAdapters().add(openConfigAdapter);

        PackageConfig packageConfig = new PackageConfig();
        packageConfig.setName("OpenConfig-Default");
        packageConfig.setFilter(new PackageConfig.Filter("IPADDR != '0.0.0.0'"));
        openConfigAdapter.getPackages().add(packageConfig);

        PackageConfig.Rrd rrd = new PackageConfig.Rrd();
        rrd.setStep(300);
        rrd.setBaseDir(rrdBaseDir.getAbsolutePath());
        rrd.getRras().add("RRA:AVERAGE:0.5:1:2016");
        packageConfig.setRrd(rrd);

        return telemetrydConfig;
    }

    @After
    public void shutdown() {
        if(server != null) {
            server.stop();
        }
        telemetryd.destroy();
    }

}
