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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.mock.snmp.MockSnmpAgent;
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

    private File tmpFile;
    private int port;

    private MockSnmpAgent mockSnmpAgent;

    @BeforeClass
    public static void setup() {
        System.setProperty("opennms.home", "../../../opennms-dao/src/test/opennms-home");
    }

    @Before
    public void setUp() throws IOException, InterruptedException {
        port = SocketUtils.findAvailableUdpPort();
        mockSnmpAgent = MockSnmpAgent
                .createAgentAndRun(new ClassPathResource("org/opennms/netmgt/snmp/snmpTestData1.properties").getURL(),
                        "127.0.0.1/"+port);
    }

    @After
    public void tearDown() throws InterruptedException {
        mockSnmpAgent.shutDownAndWait();
        tmpFile.delete();
    }

    /**
     * We want to make sure that the SnmpClient calls the configured proxy host and not the target destination. Therefor
     * we set up an UDP socket listener and check if 127.0.0.1 (the proxy host) is called.
     */
    @Test
    public void agentShouldUseConfiguredProxy() throws Exception {

        final List<SnmpObjId> snmpObjIds = Collections.singletonList(SnmpObjId.get(".1.3.6.1.2.1.1.2"));
        Resource configuration = createConfiguration(port);
        SnmpPeerFactory snmpAgentConfigFactory = new SnmpPeerFactory(configuration);

        String targetHost = "169.254.1.1";
        final SnmpAgentConfig agent = snmpAgentConfigFactory.getAgentConfig(InetAddress.getByName(targetHost));
        final CompletableFuture<List<SnmpResult>> future = locationAwareSnmpClient.walk(agent, snmpObjIds)
                .withDescription("snmp:walk")
                .execute();

        boolean answerReceived = false;


        while (true) {
            try {
                future.get(1, TimeUnit.SECONDS)
                        .forEach(res -> {
                            LOG.info("[{}].[{}] = {}", res.getBase(), res.getInstance(), res.getValue());
                        });
                answerReceived = true;
                break;
            } catch (TimeoutException e) {
                // pass
            }
            System.out.print(".");
        }

        assertTrue(answerReceived);
    }

    /** Create a temp file with the configuration and the given port. */
    private PathResource createConfiguration(int port) throws IOException {
        ClassPathResource resource = new ClassPathResource("org/opennms/netmgt/snmp/SnmpProxyIT.xml");
        String xml = new String ( Files.readAllBytes(resource.getFile().toPath()));
        xml = xml.replace("${port}", Integer.toString(port));
        File configFile = File.createTempFile(this.getClass().getSimpleName(), ".xml");
        tmpFile = configFile; // to be deleted later
        Files.write(configFile.toPath(), xml.getBytes());

        LOG.info("Configuration from org/netmgt/snmp/SnmpProxyIT.xml:\n" + xml);
        return new PathResource(configFile.toPath());
    }

}
