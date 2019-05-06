/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import static junit.framework.TestCase.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.SocketUtils;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
public class SnmpProxyIT {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpProxyIT.class);

    @Autowired
    private LocationAwareSnmpClient locationAwareSnmpClient;
    private ExecutorService executorService;
    private File tmpFile;

    @BeforeClass
    public static void setup() {
        System.setProperty("opennms.home", "../../../opennms-dao/src/test/opennms-home");
    }

    @Before
    public void setUp() {
        executorService = Executors.newFixedThreadPool(1);
    }

    @After
    public void tearDown() {
        executorService.shutdown();
        tmpFile.delete();
    }

    /**
     * We want to make sure that the SnmpClient calls the configured proxy host and not the target destination. Therefor
     * we set up an UDP socket listener and check if 127.0.0.1 (the proxy host) is called.
     */
    @Test
    public void agentShouldUseConfiguredProxy() throws Exception {
        int port = SocketUtils.findAvailableUdpPort();
        UDPListener udpListener = new UDPListener(port);
        executorService.submit(udpListener);

        final List<SnmpObjId> snmpObjIds = Collections.singletonList(SnmpObjId.get(new int[]{port}));
        Resource configuration = createConfiguration(port);
        SnmpPeerFactory snmpAgentConfigFactory = new SnmpPeerFactory(configuration);

        String targetHost = "169.254.1.1";
        final SnmpAgentConfig agent = snmpAgentConfigFactory.getAgentConfig(InetAddress.getByName(targetHost));
        locationAwareSnmpClient.walk(agent, snmpObjIds)
                .withDescription("snmp:walk")
                .execute();

        assertTrue(udpListener.wasCalled);
    }

    /** Create a temp file with the configuration and the given port. */
    private PathResource createConfiguration(int port) throws IOException {
        ClassPathResource resource = new ClassPathResource("org/netmgt/snmp/SnmpProxyIT.xml");
        String xml = new String ( Files.readAllBytes(resource.getFile().toPath()));
        xml = xml.replace("${port}", Integer.toString(port));
        File configFile = File.createTempFile(this.getClass().getSimpleName(), ".xml");
        tmpFile = configFile; // to be deleted later
        Files.write(configFile.toPath(), xml.getBytes());

        LOG.info("Configuration from org/netmgt/snmp/SnmpProxyIT.xml:\n" + xml);
        return new PathResource(configFile.toPath());
    }

    /**
     * Simple UDP Listener. Listens to the given port until it receives a package, afterwards it terminates.
     */
    private static class UDPListener implements Runnable {

        private static final Logger LOG = LoggerFactory.getLogger(SnmpProxyIT.class);

        private boolean wasCalled = false;
        private int port;

        UDPListener(int port) {
            this.port = port;
        }

        public void run() {
            try {
                LOG.info("Listening on UDP Port " + port);
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                DatagramSocket udpSocket = new DatagramSocket(port);
                udpSocket.receive(packet);
                String msg = new String(packet.getData()).trim();
                LOG.info("Message from " + packet.getAddress().getHostAddress() + ": " + msg);
                this.wasCalled = true;
            } catch (IOException e) {
                LOG.error("An exception occurred on socket for port " + port, e);
            }
        }
    }

}
