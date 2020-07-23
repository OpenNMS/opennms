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

package org.opennms.smoketest.flow;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.notNullValue;

import java.net.InetSocketAddress;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.hibernate.AlarmDaoHibernate;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.telemetry.FlowTestBuilder;
import org.opennms.smoketest.telemetry.FlowTester;
import org.opennms.smoketest.telemetry.Sender;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.RestClient;

import com.google.common.collect.ImmutableList;

public class ClockSkewIT {
    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withTelemetryProcessing()
            .withMinion()
            .build());

    @Test
    public void verifyClockSkewDetection() throws Exception {
        // setting up endpoint addresses
        final InetSocketAddress flowTelemetryAddress = stack.minion().getNetworkProtocolAddress(NetworkProtocol.FLOWS);
        final InetSocketAddress opennmsWebAddress = stack.opennms().getWebAddress();
        final InetSocketAddress elasticRestAddress = InetSocketAddress.createUnresolved(
                stack.elastic().getContainerIpAddress(), stack.elastic().getMappedPort(9200));

        // we need the host ip address in the docker network to be used as the exporter address
        final String localAddress = stack.minion()
                .getCurrentContainerInfo()
                .getNetworkSettings()
                .getNetworks()
                .entrySet()
                .stream()
                .findFirst()
                .get()
                .getValue()
                .getGateway();

        // create node with ip address
        final RestClient restClient = stack.opennms().getRestClient();
        final Requisition requisition = new Requisition("TestForeignSource");

        final RequisitionNode requisitionNode = new RequisitionNode();
        requisitionNode.setNodeLabel("ExporterNode");
        requisitionNode.setLocation(stack.minion().getLocation());

        final RequisitionInterface requisitionInterface = new RequisitionInterface();
        requisitionInterface.setIpAddr(localAddress);
        requisitionInterface.setManaged(true);
        requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
        requisitionNode.setInterfaces(ImmutableList.of(requisitionInterface));
        requisitionNode.setForeignId("ExporterNode");
        requisition.insertNode(requisitionNode);
        restClient.addOrReplaceRequisition(requisition);
        restClient.importRequisition("TestForeignSource");

        // wait till the node is created
        final NodeDao nodeDao = stack.postgres().dao(org.opennms.netmgt.dao.hibernate.NodeDaoHibernate.class);
        final OnmsNode onmsNode = await()
                .atMost(2, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.findMatchingCallable(nodeDao,
                        new CriteriaBuilder(OnmsNode.class)
                                .eq("label", "ExporterNode")
                                .toCriteria()),
                        notNullValue());

        // now send Netflow v5 packet
        final FlowTester flowTester = new FlowTestBuilder()
                .withNetflow5Packet(Sender.udp(flowTelemetryAddress))
                .verifyOpennmsRestEndpoint(opennmsWebAddress)
                .build(elasticRestAddress);

        // and verify
        flowTester.verifyFlows();

        // check for the event to appear
        final EventDao eventDao = stack.postgres().getDaoFactory().getDao(EventDaoHibernate.class);
        final OnmsEvent onmsEvent = await().atMost(2, MINUTES).pollInterval(10, SECONDS)
                .until(DaoUtils.findMatchingCallable(eventDao, new CriteriaBuilder(OnmsEvent.class)
                        .eq("eventUei", "uei.opennms.org/internal/telemetry/clockSkewDetected")
                        .toCriteria()), notNullValue());

        // check for the alarm to appear
        final AlarmDao alarmDao = stack.postgres().getDaoFactory().getDao(AlarmDaoHibernate.class);
        final OnmsAlarm onmsAlarm = await().atMost(2, MINUTES).pollInterval(10, SECONDS)
                .until(DaoUtils.findMatchingCallable(alarmDao, new CriteriaBuilder(OnmsAlarm.class)
                        .eq("uei", "uei.opennms.org/translator/telemetry/clockSkewDetected")
                        .toCriteria()), notNullValue());
    }
}
