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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Paths;
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
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.telemetry.adapters.jti.JtiGpbAdapter;
import org.opennms.netmgt.telemetry.config.dao.TelemetrydConfigDao;
import org.opennms.netmgt.telemetry.config.model.Adapter;
import org.opennms.netmgt.telemetry.config.model.Filter;
import org.opennms.netmgt.telemetry.config.model.Listener;
import org.opennms.netmgt.telemetry.config.model.Package;
import org.opennms.netmgt.telemetry.config.model.Parameter;
import org.opennms.netmgt.telemetry.config.model.Protocol;
import org.opennms.netmgt.telemetry.config.model.Rrd;
import org.opennms.netmgt.telemetry.config.model.TelemetrydConfiguration;
import org.opennms.netmgt.telemetry.daemon.Telemetryd;
import org.opennms.netmgt.telemetry.listeners.udp.UdpListener;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.io.Resources;

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
        "classpath:/META-INF/opennms/applicationContext-ipc-sink-server-camel.xml",
        "classpath:/META-INF/opennms/applicationContext-collectionAgentFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-telemetryDaemon.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class,reuseDatabase=false)
public class JtiIT {

    @Autowired
    private TelemetrydConfigDao telemetrydConfigDao;

    @Autowired
    private Telemetryd telemetryd;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private InterfaceToNodeCache interfaceToNodeCache;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File rrdBaseDir;

    @Before
    public void setUp() throws IOException {
        rrdBaseDir = tempFolder.newFolder("rrd");

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
    public void canReceivedAndPersistJtiMessages() throws Exception {
        final int port = 50001;

        // Use our custom configuration
        updateDaoWithConfig(getConfig(port));

        // Start the daemon
        telemetryd.start();

        // Send a JTI payload via a UDP socket
        final byte[] jtiMsgBytes = Resources.toByteArray(Resources.getResource("jti_15.1F4_ifd_ae_40000.raw"));
        InetAddress address = InetAddressUtils.getLocalHostAddress();
        DatagramPacket packet = new DatagramPacket(jtiMsgBytes, jtiMsgBytes.length, address, port);
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);

        // Wait until the JRB archive is created
        await().atMost(30, TimeUnit.SECONDS).until(() -> rrdBaseDir.toPath()
                .resolve(Paths.get("1", "ge_0_0_3", "ifOutOctets.jrb")).toFile().canRead(), equalTo(true));
    }

    private void updateDaoWithConfig(TelemetrydConfiguration config) throws IOException {
        final File tempFile = tempFolder.newFile();
        JaxbUtils.marshal(config, tempFile);
        telemetrydConfigDao.setConfigResource(new FileSystemResource(tempFile));
        telemetrydConfigDao.afterPropertiesSet();
    }

    private TelemetrydConfiguration getConfig(int port) {
        TelemetrydConfiguration telemetrydConfig = new TelemetrydConfiguration();

        Protocol jtiProtocol = new Protocol();
        jtiProtocol.setName("JTI");
        jtiProtocol.setDescription("Junos Telemetry Interface (JTI)");
        telemetrydConfig.getProtocols().add(jtiProtocol);

        Listener udpListener = new Listener();
        udpListener.setName("JTI-UDP-" + port);
        udpListener.setClassName(UdpListener.class.getCanonicalName());
        udpListener.getParameters().add(new Parameter("port", Integer.toString(port)));
        jtiProtocol.getListeners().add(udpListener);

        Adapter jtiGbpAdapter = new Adapter();
        jtiGbpAdapter.setName("JTI-GBP");
        jtiGbpAdapter.setClassName(JtiGpbAdapter.class.getCanonicalName());

        File script = Paths.get(System.getProperty("opennms.home"),
                "etc", "telemetryd-adapters", "junos-telemetry-interface.groovy").toFile();
        assertTrue("Can't read: " + script.getAbsolutePath(), script.canRead());
        jtiGbpAdapter.getParameters().add(new Parameter("script", script.getAbsolutePath()));
        jtiProtocol.getAdapters().add(jtiGbpAdapter);

        Package jtiDefaultPkg = new Package();
        jtiDefaultPkg.setName("JTI-Default");
        jtiDefaultPkg.setFilter(new Filter("IPADDR != '0.0.0.0'"));
        jtiProtocol.getPackages().add(jtiDefaultPkg);

        Rrd rrd = new Rrd();
        rrd.setStep(300);
        rrd.setBaseDir(rrdBaseDir.getAbsolutePath());
        rrd.getRras().add("RRA:AVERAGE:0.5:1:2016");
        jtiDefaultPkg.setRrd(rrd);

        return telemetrydConfig;
    }
}
