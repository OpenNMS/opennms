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

package org.opennms.smoketest.sentinel;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.opennms.netmgt.events.api.EventConstants.HIGH_THRESHOLD_EVENT_UEI;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsAlarmCollection;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMetaData;
import org.opennms.netmgt.telemetry.protocols.jti.adapter.proto.Port;
import org.opennms.netmgt.telemetry.protocols.jti.adapter.proto.TelemetryTop;
import org.opennms.smoketest.stacks.BlobStoreStrategy;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.JsonStoreStrategy;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.OpenNMSProfile;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.telemetry.Packet;
import org.opennms.smoketest.telemetry.Payload;
import org.opennms.smoketest.telemetry.Sender;
import org.opennms.smoketest.utils.KarafShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentinelThresholdingIT {
    private static final Logger LOG = LoggerFactory.getLogger(SentinelThresholdingIT.class);
    private static final String NODE_IP = "192.168.1.1";
    private static final String INTERFACE_ID = "eth0_system_test";

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withOpenNMS(OpenNMSProfile.newBuilder()
                    .withFile("sentinel-thresholding/threshd-configuration.xml", "etc/threshd-configuration.xml")
                    .withFile("sentinel-thresholding/thresholds.xml", "etc/thresholds.xml")
                    .build())
            .withMinion()
            .withSentinel()
            .withTelemetryProcessing()
            .withBlobStoreStrategy(BlobStoreStrategy.NEWTS_CASSANDRA)
            .withJsonStoreStrategy(JsonStoreStrategy.POSTGRES)
            .withIpcStrategy(IpcStrategy.JMS)
            .build());

    @Test
    public void canTriggerHighThresholdWithJti() throws InterruptedException, IOException {
        // The package send may contain a node, which must be created in order to have the adapter store it to newts
        // so we check if this is the case and afterwards create the requisition
        final InetSocketAddress opennmsHttpAddress = stack.opennms().getWebAddress();
        OnmsNode onmsNode = AbstractAdapterIT.createRequisition(getRequisitionToCreate(), opennmsHttpAddress,
                stack.postgres().getDaoFactory());
        // Verify that no existing highThresholdExceeded alarm exists for the node
        assertThat(getAlarmUeisForNode(onmsNode.getId()), not(hasItem(HIGH_THRESHOLD_EVENT_UEI)));

        // If a new requisition was created, also probably new nodes are available.
        // However, sentinel may not know about it yet, so we manually sync the InterfaceToNodeCache in order to
        // "see" the new nodes and interfaces.
        final InetSocketAddress sentinelSshAddress = stack.sentinel().getSshAddress();
        new KarafShell(sentinelSshAddress).runCommand("nodecache:sync");

        final InetSocketAddress minionListenerAddress = stack.minion().getNetworkProtocolAddress(NetworkProtocol.JTI);
        sendTriggerHighThresholdMessages(minionListenerAddress);

        LOG.info("Waiting for high threshold alarm...");
        await().atMost(1, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS)
                .until(() -> getAlarmUeisForNode(onmsNode.getId()), hasItem(HIGH_THRESHOLD_EVENT_UEI));

        sendClearHighThresholdMessages(minionListenerAddress);

        LOG.info("Waiting for alarm to clear...");
        await().atMost(1, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS)
                .until(() -> getAlarmsForNode(onmsNode.getId())
                        .contains(new AbstractMap.SimpleImmutableEntry<>(HIGH_THRESHOLD_EVENT_UEI,
                                OnmsSeverity.CLEARED)));
    }

    private Set<String> getAlarmUeisForNode(int nodeId) {
        return getAlarmsForNode(nodeId).stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Set<Map.Entry<String, OnmsSeverity>> getAlarmsForNode(int nodeId) {
        final OnmsAlarmCollection alarms = stack.opennms().getRestClient().getAlarmsForNode(nodeId);
        return alarms.getObjects()
                .stream()
                .map(a -> new AbstractMap.SimpleImmutableEntry<>(a.getUei(), a.getSeverity()))
                .collect(Collectors.toSet());
    }

    private AbstractAdapterIT.RequisitionCreateInfo getRequisitionToCreate() {
        final AbstractAdapterIT.RequisitionCreateInfo requisitionCreateInfo =
                new AbstractAdapterIT.RequisitionCreateInfo();
        requisitionCreateInfo.ipAddress = NODE_IP;
        requisitionCreateInfo.foreignSource = "telemetry-jti";
        requisitionCreateInfo.foreignId = "dummy-node";
        requisitionCreateInfo.nodeLabel = "Dummy Node";
        RequisitionMetaData metaData = new RequisitionMetaData();
        metaData.setKey("multiplier");
        metaData.setValue("1");
        metaData.setContext("requisition");
        requisitionCreateInfo.metaData = Collections.singletonList(metaData);
        return requisitionCreateInfo;
    }

    private void sendTriggerHighThresholdMessages(InetSocketAddress minionListenerAddress) throws InterruptedException, IOException {
        new Packet(buildJtiMessage(NODE_IP, INTERFACE_ID, 1, 1, 0))
                .send(Sender.udp(minionListenerAddress));
        sleepToEnsureSeparateDelivery();
        // We need the delta sum of in+out to exceed 100,000/s keeping in mind we slept for 30 seconds
        new Packet(buildJtiMessage(NODE_IP, INTERFACE_ID, 10000000, 10000000, 1))
                .send(Sender.udp(minionListenerAddress));
    }

    private void sendClearHighThresholdMessages(InetSocketAddress minionListenerAddress) throws InterruptedException,
            IOException {
        new Packet(buildJtiMessage(NODE_IP, INTERFACE_ID, 1, 1, 3))
                .send(Sender.udp(minionListenerAddress));
        sleepToEnsureSeparateDelivery();
        new Packet(buildJtiMessage(NODE_IP, INTERFACE_ID, 1, 1, 4))
                .send(Sender.udp(minionListenerAddress));
    }
    
    private void sleepToEnsureSeparateDelivery() throws InterruptedException {
        // We sleep here due to current limitations in how received packets are timestamped. The timestamp is derived
        // from the received time so we need to ensure they arrive separately at the Sentinel in spite of any collating
        // that may be attempted on the path from Minion to Sentinel which may require a significant time delay
        Thread.sleep(30000);
    }

    public static Payload buildJtiMessage(String ipAddress, String interfaceName,
                                          long ifInOctets, long ifOutOctets, int sequenceNumber) {
        Port.GPort.Builder builder = Port.GPort.newBuilder();
        sequenceNumber++;
        Random rnd = new Random();
        int random = rnd.nextInt(32);
        Port.InterfaceInfos interfaceInfos = Port.InterfaceInfos.newBuilder()
                .setIfName(interfaceName)
                .setInitTime(1457647123)
                .setSnmpIfIndex(512)
                .setParentAeName("ae0")
                .setIngressStats(Port.InterfaceStats.newBuilder()
                        .setIfOctets(ifInOctets)
                        .setIfPkts(56)
                        .setIf1SecPkts(random)
                        .setIf1SecOctets(random)
                        .setIfUcPkts(32)
                        .setIfMcPkts(96)
                        .setIfBcPkts(16)
                        .build())
                .setEgressStats(Port.InterfaceStats.newBuilder()
                        .setIfOctets(ifOutOctets)
                        .setIfPkts(36)
                        .setIf1SecPkts(random)
                        .setIf1SecOctets(random)
                        .setIfUcPkts(6)
                        .setIfMcPkts(18)
                        .setIfBcPkts(12)
                        .build())
                .build();

        builder.addInterfaceStats(interfaceInfos);

        final Port.GPort port = builder.build();

        final TelemetryTop.JuniperNetworksSensors juniperNetworksSensors =
                TelemetryTop.JuniperNetworksSensors.newBuilder()
                        .setExtension(Port.jnprInterfaceExt, port)
                        .build();

        final TelemetryTop.EnterpriseSensors sensors = TelemetryTop.EnterpriseSensors.newBuilder()
                .setExtension(TelemetryTop.juniperNetworks, juniperNetworksSensors)
                .build();

        return Payload.direct(TelemetryTop.TelemetryStream.newBuilder()
                .setSystemId(ipAddress)
                .setComponentId(0)
                .setSensorName("intf-stats")
                .setSequenceNumber(49103 + sequenceNumber)
                .setTimestamp(new Date().getTime())
                .setEnterprise(sensors)
                .build()
                .toByteArray());
    }
}
