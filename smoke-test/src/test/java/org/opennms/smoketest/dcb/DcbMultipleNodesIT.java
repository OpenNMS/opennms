/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
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
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest.dcb;

import com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.features.deviceconfig.persistence.impl.DeviceConfigDaoImpl;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMetaData;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.smoketest.stacks.OpenNMSProfile;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.notNullValue;

public class DcbMultipleNodesIT {

    private static final String DCB_CONFIG_TYPE = "testcfg";
    private static final String DCB_USERNAME = "dcbuser";
    private static final String DCB_PASSWORD = "dcbpass";
    private static final String DCB_SCRIPT_NAME = "test";
    private static final String DCB_SVC_NAME = "DeviceConfig";

    private static final int SSH_PORT = 2222;

    private static final int NUMBER_OF_NODES = 100;

    private static final String FOREIGN_SOURCE = "SmokeTests";

    private static final OpenNMSProfile OPENNMS_PROFILE = OpenNMSProfile.newBuilder()
            .withFile("device-config/test.dcb", "etc/device-config/" + DCB_SCRIPT_NAME + ".dcb")
            .build();

    @ClassRule
    public static final OpenNMSStack STACK = OpenNMSStack.withModel(StackModel.newBuilder()
            .withOpenNMS(OPENNMS_PROFILE)
            .build());

    @ClassRule
    public static final GenericContainer<?> TARGET_CONTAINER = new GenericContainer<>(
            new ImageFromDockerfile().withDockerfileFromBuilder(builder -> builder.from("linuxserver/openssh-server")
                    .run("apk add --update tftp-hpa")
                    .build()))
            .withEnv("PASSWORD_ACCESS", "true")
            .withEnv("SUDO_ACCESS", "true")
            .withEnv("USER_NAME", DCB_USERNAME)
            .withEnv("USER_PASSWORD", DCB_PASSWORD)
            .withExposedPorts(SSH_PORT)
            .withNetwork(Network.SHARED)
            .withNetworkAliases("target")
            .withCreateContainerCmdModifier(TestContainerUtils::setGlobalMemAndCpuLimits);

    private static String targetAddress;

    private static RestClient restClient;

    @BeforeClass
    public static void setupClass() throws Exception {
        targetAddress = getContainerInternalIpAddress(TARGET_CONTAINER);

        restClient = new RestClient(STACK.opennms().getWebAddress());

        final Requisition requisition = new Requisition();
        requisition.setForeignSource(FOREIGN_SOURCE);

        // Time prep for the schedule
        LocalTime time = LocalTime.now().plusMinutes(2);
        String schedule = String.format("0 %d %d ? * *", time.getMinute(), time.getHour());

        final List<RequisitionMonitoredService> monitoredServiceList = new ArrayList<>();
        monitoredServiceList.add(new RequisitionMonitoredService(DCB_SVC_NAME));

        final RequisitionInterface requisitionInterface = new RequisitionInterface();
        requisitionInterface.setIpAddr(targetAddress);
        requisitionInterface.setManaged(true);
        requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
        requisitionInterface.setMonitoredServices(monitoredServiceList);

        final List<RequisitionMetaData> metaDataList = new ArrayList<>();
        metaDataList.add(new RequisitionMetaData("requisition", "dcb:config-type", DCB_CONFIG_TYPE));
        metaDataList.add(new RequisitionMetaData("requisition", "dcb:username", DCB_USERNAME));
        metaDataList.add(new RequisitionMetaData("requisition", "dcb:password", DCB_PASSWORD));
        metaDataList.add(new RequisitionMetaData("requisition", "dcb:script-file", DCB_SCRIPT_NAME));
        metaDataList.add(new RequisitionMetaData("requisition", "dcb:ssh-port", Integer.toString((SSH_PORT))));
        metaDataList.add(new RequisitionMetaData("requisition", "dcb:schedule", schedule));

        // Nodes in default location for triggering backup from core
        for (int i = 1; i <= NUMBER_OF_NODES; i++) {
            final RequisitionNode requisitionNode = new RequisitionNode();
            requisitionNode.setForeignId("local_" + i);
            requisitionNode.setNodeLabel("local_" + i);
            requisitionNode.setLocation(null);
            requisitionNode.setMetaData(metaDataList);
            requisitionNode.putInterface(requisitionInterface);

            requisition.insertNode(requisitionNode);
        }

        restClient.addOrReplaceRequisition(requisition);
        restClient.importRequisition(FOREIGN_SOURCE);

        final NodeDao nodeDao = STACK.postgres().dao(NodeDaoHibernate.class);
        // wait until all nodes are created
        await().atMost(5, MINUTES)
                .pollInterval(30, SECONDS)
                .until(() -> nodeDao.findByForeignSource(FOREIGN_SOURCE).size() == NUMBER_OF_NODES);
    }

    @Before
    public void setup() throws Exception {
        // Ensure there is no backup history for every test
        final var deviceConfigDao = STACK.postgres().dao(DeviceConfigDaoImpl.class);
        deviceConfigDao.deleteAll(deviceConfigDao.findAll());
        deviceConfigDao.flush();
    }

    private static String getContainerInternalIpAddress(final GenericContainer<?> container) {
        return Iterables.getOnlyElement(container.getContainerInfo()
                        .getNetworkSettings()
                        .getNetworks()
                        .entrySet())
                .getValue()
                .getIpAddress();
    }

    @Test
    public void testGetConfigFromMultipleNodes() throws Exception {
        // Waiting for the Backups to appear.
        await().atMost(5, MINUTES)
                .pollInterval(20, SECONDS)
                .until(restClient::getBackups, notNullValue());

        Assert.assertEquals(restClient.getBackups().size(), NUMBER_OF_NODES);
    }
}