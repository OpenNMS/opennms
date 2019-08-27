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

package org.opennms.netmgt.telemetry.itests;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.collection.test.api.CollectorTestUtils;
import org.opennms.netmgt.config.dao.thresholding.api.OverrideableThreshdDao;
import org.opennms.netmgt.config.dao.thresholding.api.OverrideableThresholdingDao;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.telemetry.config.dao.TelemetrydConfigDao;
import org.opennms.netmgt.telemetry.config.model.AdapterConfig;
import org.opennms.netmgt.telemetry.config.model.ListenerConfig;
import org.opennms.netmgt.telemetry.config.model.PackageConfig;
import org.opennms.netmgt.telemetry.config.model.Parameter;
import org.opennms.netmgt.telemetry.config.model.ParserConfig;
import org.opennms.netmgt.telemetry.config.model.QueueConfig;
import org.opennms.netmgt.telemetry.config.model.TelemetrydConfig;
import org.opennms.netmgt.telemetry.daemon.Telemetryd;
import org.opennms.netmgt.telemetry.listeners.UdpListener;
import org.opennms.netmgt.telemetry.protocols.jti.adapter.JtiGpbAdapter;
import org.opennms.netmgt.telemetry.protocols.jti.adapter.proto.Port;
import org.opennms.netmgt.telemetry.protocols.jti.adapter.proto.TelemetryTop;
import org.opennms.netmgt.threshd.ThresholdingServiceImpl;
import org.opennms.netmgt.threshd.api.ThresholdingService;
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
        "classpath:/META-INF/opennms/applicationContext-thresholding.xml",
        "classpath:/META-INF/opennms/applicationContext-testPostgresBlobStore.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice-mq-vm.xml",
        "classpath:/META-INF/opennms/applicationContext-ipc-sink-camel-server.xml",
        "classpath:/META-INF/opennms/applicationContext-ipc-sink-camel-client.xml",
        "classpath:/META-INF/opennms/applicationContext-collectionAgentFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-jtiAdapterFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-telemetryDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-testThresholdingDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml"
})
@JUnitConfigurationEnvironment(systemProperties={ // We don't need a real pinger here
        "org.opennms.netmgt.icmp.pingerClass=org.opennms.netmgt.icmp.NullPinger"})
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class,reuseDatabase=false)
public class ThresholdingIT {

    private static int sequence_no = 49103;

    @Autowired
    private TelemetrydConfigDao telemetrydConfigDao;

    @Autowired
    private Telemetryd telemetryd;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private InterfaceToNodeCache interfaceToNodeCache;

    @Autowired
    private MockEventIpcManager mockEventIpcManager;

    @Autowired
    private ThresholdingService thresholdingService;

    @Autowired
    private RrdStrategy rrdStrategy;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    public File scriptFile;

    private File rrdBaseDir;

    private int port = 50001;

    @Autowired
    private OverrideableThreshdDao threshdDao;
    
    @Autowired
    private OverrideableThresholdingDao thresholdingDao;
    
    @Before
    public void setUp() throws IOException {
        rrdBaseDir = tempFolder.newFolder("rrd");
        scriptFile  = tempFolder.newFile("script-file.groovy");

        NetworkBuilder nb = new NetworkBuilder();
        nb.addNode("R1")
                .setForeignSource("Juniper")
                .setForeignId("1")
                .setSysObjectId(".1.3.6.1.4.1.9.1.222")
                .setType(OnmsNode.NodeType.ACTIVE);
        nb.addInterface("192.0.2.1").setIsSnmpPrimary("P").setIsManaged("P");
        nb.addInterface("172.23.2.12").setIsSnmpPrimary("P").setIsManaged("P");
        nodeDao.save(nb.getCurrentNode());

        // Resync after adding nodes/interfaces
        interfaceToNodeCache.dataSourceSync();
    }

    @Test
    public void canTriggerThresholds() throws Exception {
        // Use our custom configuration
        updateDaoWithConfig(getConfig(port));

        // Start the daemon
        telemetryd.start();

        long ifInOctets = 124827820;
        long ifOutOctets = 194503622;

        // Load custom threshd configuration
        initThreshdFactories("/threshd-configuration.xml", "/thresholds.xml");
        threshdDao.rebuildPackageIpListMap();
        mockEventIpcManager.addEventListener((EventListener) thresholdingService, ThresholdingServiceImpl.UEI_LIST);

        // Compute the path to the RRD file
        final File interfaceDir = rrdBaseDir.toPath().resolve("1" + File.separator + "ge_0_0_3").toFile();
        final File ifIn1SecPktsRrdFile = new File(interfaceDir, CollectorTestUtils.rrd(rrdStrategy, "ifIn1SecPkts"));
        // The file should not exist yet
        assertThat(ifIn1SecPktsRrdFile.exists(), equalTo(false));

        EventAnticipator eventAnticipator = mockEventIpcManager.getEventAnticipator();

        // Send an initial message
        sendTelemetryMessage("192.0.2.1", "ge_0_0_3", ifInOctets, ifOutOctets, 0);

        // Wait for the RRD file to be created
        await().atMost(60, TimeUnit.SECONDS).until(ifIn1SecPktsRrdFile::exists, equalTo(true));
        long lastModified = ifIn1SecPktsRrdFile.lastModified();

        // There should be no thresholding Events
        assertEquals(0, eventAnticipator.getUnanticipatedEvents().size());

        // Wait one second before sending the next message (RRDs require at least a one second step)
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        // Send another message
        ifInOctets += 10000000;
        ifOutOctets += 10000000;
        sendTelemetryMessage("192.0.2.1", "ge_0_0_3", ifInOctets, ifOutOctets, 5);

        // Wait for the RRD file to be updated
        await().atMost(60, TimeUnit.SECONDS).until(ifIn1SecPktsRrdFile::lastModified, greaterThan(lastModified));

        // There should still be no thresholding Events
        assertEquals(0, eventAnticipator.getUnanticipatedEvents().size());

        EventBuilder threshBldr = new EventBuilder(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "Test");
        threshBldr.setNodeid(1);
        threshBldr.setInterface(addr("192.0.2.1"));
        threshBldr.setService("JTI-GPB");
        eventAnticipator.anticipateEvent(threshBldr.getEvent());

        // Wait one second before sending the next message (RRDs require at least a one second step)
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        // Send another message - this time with ifIn1SecPkts > threshold
        ifInOctets += 10000000;
        ifOutOctets += 10000000;
        sendTelemetryMessage("192.0.2.1", "ge_0_0_3", ifInOctets, ifOutOctets, 20);

        // Wait until our threshold was triggered - the anticipator will remove the event from the list once received
        await().atMost(60, TimeUnit.SECONDS).until(eventAnticipator::getAnticipatedEvents, hasSize(0));

        // There should be no unexpected Thresholding Events
        assertEquals(0, eventAnticipator.getUnanticipatedEvents().size());
    }

    private void sendTelemetryMessage(String ipAddress, String ifName, long ifInOctets, long ifOutOctets, long ifIn1SecPkts) throws IOException {
        // Send a JTI payload via a UDP socket
        final TelemetryTop.TelemetryStream jtiMsg = buildJtiMessage(ipAddress, ifName, ifInOctets, ifOutOctets, ifIn1SecPkts);
        final byte[] jtiMsgBytes = jtiMsg.toByteArray();
        InetAddress address = InetAddressUtils.getLocalHostAddress();
        DatagramPacket packet = new DatagramPacket(jtiMsgBytes, jtiMsgBytes.length, address, port);
        try (DatagramSocket socket = new DatagramSocket();) {
            socket.send(packet);
        }
    }

    private static TelemetryTop.TelemetryStream buildJtiMessage(String ipAddress, String ifName, long ifInOctets, long ifOutOctets, long ifIn1SecPkts) {
        final Port.GPort port = Port.GPort.newBuilder()
                .addInterfaceStats(Port.InterfaceInfos.newBuilder()
                        .setIfName(ifName)
                        .setInitTime(1457647123)
                        .setSnmpIfIndex(517)
                        .setParentAeName("ae0")
                        .setIngressStats(Port.InterfaceStats.newBuilder()
                                .setIfOctets(ifInOctets)
                                .setIfPkts(1)
                                .setIf1SecPkts(ifIn1SecPkts)
                                .setIf1SecOctets(1)
                                .setIfUcPkts(1)
                                .setIfMcPkts(1)
                                .setIfBcPkts(1)
                                .build())
                        .setEgressStats(Port.InterfaceStats.newBuilder()
                                .setIfOctets(ifOutOctets)
                                .setIfPkts(1)
                                .setIf1SecPkts(1)
                                .setIf1SecOctets(1)
                                .setIfUcPkts(1)
                                .setIfMcPkts(1)
                                .setIfBcPkts(1)
                                .build())
                        .build())
                .build();

        final TelemetryTop.JuniperNetworksSensors juniperNetworksSensors = TelemetryTop.JuniperNetworksSensors.newBuilder()
                .setExtension(Port.jnprInterfaceExt, port)
                .build();

        final TelemetryTop.EnterpriseSensors sensors = TelemetryTop.EnterpriseSensors.newBuilder()
                .setExtension(TelemetryTop.juniperNetworks, juniperNetworksSensors)
                .build();

        final TelemetryTop.TelemetryStream jtiMsg = TelemetryTop.TelemetryStream.newBuilder()
                .setSystemId(ipAddress)
                .setComponentId(0)
                .setSensorName("ge_0_0_3")
                .setSequenceNumber(sequence_no++)
                .setTimestamp(new Date().getTime())
                .setEnterprise(sensors)
                .build();

        return jtiMsg;
    }

    private void updateDaoWithConfig(TelemetrydConfig config) throws IOException {
        final File tempFile = tempFolder.newFile();
        JaxbUtils.marshal(config, tempFile);
        telemetrydConfigDao.setConfigResource(new FileSystemResource(tempFile));
        telemetrydConfigDao.afterPropertiesSet();
    }

    private TelemetrydConfig getConfig(int port) throws IOException {
        TelemetrydConfig telemetrydConfig = new TelemetrydConfig();

        QueueConfig jtiQueue = new QueueConfig();
        jtiQueue.setName("JTI");
        telemetrydConfig.getQueues().add(jtiQueue);

        ListenerConfig jtiListener = new ListenerConfig();
        jtiListener.setEnabled(true);
        jtiListener.setName("JTI");
        jtiListener.setClassName(UdpListener.class.getCanonicalName());
        jtiListener.getParameters().add(new Parameter("port", Integer.toString(port)));
        telemetrydConfig.getListeners().add(jtiListener);

        ParserConfig jtiParser = new ParserConfig();
        jtiParser.setName("JTI-UDP-" + port);
        jtiParser.setClassName(org.opennms.netmgt.telemetry.protocols.common.parser.ForwardParser.class.getCanonicalName());
        jtiParser.setQueue(jtiQueue);
        jtiListener.getParsers().add(jtiParser);

        Files.copy(
                Paths.get(System.getProperty("opennms.home"),
                        "etc",
                        "telemetryd-adapters",
                        "junos-telemetry-interface.groovy"),
                scriptFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING
        );

        assertTrue("Can't read: " + scriptFile.getAbsolutePath(), scriptFile.canRead());

        AdapterConfig jtiGbpAdapter = new AdapterConfig();
        jtiGbpAdapter.setEnabled(true);
        jtiGbpAdapter.setName("JTI-GPB");
        jtiGbpAdapter.setClassName(JtiGpbAdapter.class.getCanonicalName());
        jtiGbpAdapter.getParameters().add(new Parameter("script", scriptFile.getAbsolutePath()));
        jtiQueue.getAdapters().add(jtiGbpAdapter);

        PackageConfig jtiDefaultPkg = new PackageConfig();
        jtiDefaultPkg.setName("JTI-Default");
        jtiDefaultPkg.setFilter(new PackageConfig.Filter("IPADDR != '0.0.0.0'"));
        jtiGbpAdapter.getPackages().add(jtiDefaultPkg);

        PackageConfig.Rrd rrd = new PackageConfig.Rrd();
        rrd.setStep(300);
        rrd.setBaseDir(rrdBaseDir.getAbsolutePath());
        rrd.getRras().add("RRA:AVERAGE:0.5:1:2016");
        jtiDefaultPkg.setRrd(rrd);

        return telemetrydConfig;
    }

    private void initThreshdFactories(String threshd, String thresholds) {
        thresholdingDao.overrideConfig(getClass().getResourceAsStream(thresholds));
        threshdDao.overrideConfig(getClass().getResourceAsStream(threshd));
    }

}
