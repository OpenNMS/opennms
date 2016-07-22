/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2016 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2016 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * <p>
 * For more information contact: OpenNMS(R) Licensing
 * <license@opennms.org> http://www.opennms.org/ http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest.minion;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;

public class ProvisionTest {

    private static TestEnvironment m_testEnvironment;

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
    public void checkICMPServiceDetection() throws ClientProtocolException, IOException, InterruptedException {

        final InetSocketAddress opennmsHttp = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 8980);

        RestClient client = new RestClient(opennmsHttp);

        Requisition requisition = new Requisition("foreignSource");
        List<RequisitionInterface> interfaces = new ArrayList<RequisitionInterface>();
        RequisitionInterface requisitionInterface = new RequisitionInterface();
        requisitionInterface.setIpAddr("127.0.0.1");
        requisitionInterface.setManaged(true);
        requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
        interfaces.add(requisitionInterface);
        RequisitionNode node = new RequisitionNode();
        node.setNodeLabel("label");
        node.setLocation("MINION");
        node.setInterfaces(interfaces);
        node.setForeignId("foreignId");
        requisition.insertNode(node);

        client.addOrReplaceRequisition(requisition);
        Thread.sleep(5000);
        client.importRequisition("foreignSource");
        Thread.sleep(90000);

        List<RequisitionNode> nodes = client.getNodesForRequisition("foreignSource");
        RequisitionNode requisitionNode = nodes.get(0);
        System.out.printf("node label = %s", requisitionNode.getNodeLabel());
        List<RequisitionInterface> interfacesList = client.getInterfacesForRequisition("foreignSource", "foreignId");
        System.out.printf("Interface = %s", interfacesList.get(0).toString());
        List<RequisitionMonitoredService> services = client.getServicesForRequisition("foreignSource", "foreignId",
                "127.0.0.1");
        RequisitionMonitoredService service = services.get(0);
        System.out.printf(" service Name = %s", service.getServiceName());

    }

}
