/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.minion;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThan;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.google.common.collect.Iterables;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;

public class DetectorsOnMinionIT {

    private static final Logger LOG = LoggerFactory.getLogger(DetectorsOnMinionIT.class);

    private static TestEnvironment m_testEnvironment;

    private static final String LOCALHOST = "127.0.0.1";

    @ClassRule
    public static final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().all();
            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            m_testEnvironment = builder.build();
            return m_testEnvironment;
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
    }

    @Test
    public void checkServicesDetectedOnMinion() throws ClientProtocolException, IOException, InterruptedException {

        final InetSocketAddress opennmsHttp = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 8980);
        RestClient client = new RestClient(opennmsHttp);
        addRequisition(client, "MINION", LOCALHOST);
        await().atMost(5, MINUTES).pollDelay(0, SECONDS).pollInterval(30, SECONDS)
                .until(getnumberOfServicesDetected(client), greaterThan(0));
    }
    
    public static void addRequisition(RestClient client, String location, String ipAddress) {

        Requisition requisition = new Requisition("foreignSource");
        List<RequisitionInterface> interfaces = new ArrayList<>();
        RequisitionInterface requisitionInterface = new RequisitionInterface();
        requisitionInterface.setIpAddr(ipAddress);
        requisitionInterface.setManaged(true);
        requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
        interfaces.add(requisitionInterface);
        RequisitionNode node = new RequisitionNode();
        node.setNodeLabel(ipAddress);
        node.setLocation(location);
        node.setInterfaces(interfaces);
        node.setForeignId("foreignId");
        requisition.insertNode(node);

        client.addOrReplaceRequisition(requisition);
        client.importRequisition("foreignSource");

    }

    public static Callable<Integer> getnumberOfServicesDetected(RestClient client) {
        return new Callable<Integer>() {
            public Integer call() throws Exception {
                List<OnmsNode> nodes = client.getNodes();
                Integer number = null;
                if (Iterables.any(nodes, (node) -> "foreignSource".equals(node.getForeignSource()))) {
                    List<OnmsMonitoredService> services = client.getServicesForANode("foreignSource:foreignId",
                            LOCALHOST);
                    if (!CollectionUtils.isEmpty(services)) {
                        number = services.size();
                        LOG.info("The services detected  are \n");
                        for (OnmsMonitoredService service : services) {
                            LOG.info("   {}  ", service.getServiceName());
                        }
                    }
                }
                return number;
            }
        };
    }
}
