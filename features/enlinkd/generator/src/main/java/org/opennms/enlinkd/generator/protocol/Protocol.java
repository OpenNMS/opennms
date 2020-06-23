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

package org.opennms.enlinkd.generator.protocol;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.opennms.enlinkd.generator.TopologyContext;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologySettings;
import org.opennms.enlinkd.generator.topology.LinkedPairGenerator;
import org.opennms.enlinkd.generator.topology.PairGenerator;
import org.opennms.enlinkd.generator.topology.RandomConnectedPairGenerator;
import org.opennms.enlinkd.generator.topology.UndirectedPairGenerator;
import org.opennms.enlinkd.generator.util.InetAddressGenerator;
import org.opennms.enlinkd.generator.util.RandomUtil;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

public abstract class Protocol<Element> {

    final TopologySettings topologySettings;
    final TopologyContext context;
    private final RandomUtil random = new RandomUtil();
    // A map that holds a valid ifIndex for each node that has an snmp interface
    final Map<Integer, Integer> nodeIfIndexes = new HashMap<>();

    public Protocol(TopologySettings topologySettings, TopologyContext context) {
        this.topologySettings = adoptAndVerifySettings(topologySettings);
        this.context = context;
    }

    public void createAndPersistNetwork() {
        printProtocolSpecificMessage();
        OnmsCategory category = createCategory();
        context.getTopologyPersister().persist(category);
        List<OnmsNode> nodes = createNodes(topologySettings.getAmountNodes(), category);
        context.getTopologyPersister().persist(nodes);
        List<OnmsSnmpInterface> snmpInterfaces = createSnmpInterfaces(nodes);
        context.getTopologyPersister().persist(snmpInterfaces);
        List<OnmsIpInterface> ipInterfaces = createIpInterfaces(snmpInterfaces);
        context.getTopologyPersister().persist(ipInterfaces);

        createAndPersistProtocolSpecificEntities(nodes);
    }

    protected void printProtocolSpecificMessage() {
        this.context.currentProgress("%nCreating %s %s topology with %s Nodes, %s Elements, %s Links, %s SnmpInterfaces, %s IpInterfaces:",
                topologySettings.getTopology(),
                this.getProtocol(),
                topologySettings.getAmountNodes(),
                topologySettings.getAmountElements(),
                topologySettings.getAmountElements(),
                topologySettings.getAmountSnmpInterfaces(),
                topologySettings.getAmountIpInterfaces());
    }

    protected abstract void createAndPersistProtocolSpecificEntities(List<OnmsNode> nodes);

    protected abstract TopologyGenerator.Protocol getProtocol();

    protected TopologySettings adoptAndVerifySettings(TopologySettings topologySettings) {
        topologySettings.verify();
        return topologySettings;
    }

    protected OnmsCategory createCategory() {
        OnmsCategory category = new OnmsCategory();
        category.setName(TopologyGenerator.CATEGORY_NAME);
        return category;
    }

    private OnmsMonitoringLocation createMonitoringLocation() {
        OnmsMonitoringLocation location = new OnmsMonitoringLocation();
        location.setLocationName("Default");
        location.setMonitoringArea("localhost");
        return location;
    }

    protected List<OnmsNode> createNodes(int amountNodes, OnmsCategory category) {
        OnmsMonitoringLocation location = createMonitoringLocation();
        ArrayList<OnmsNode> nodes = new ArrayList<>();
        for (int i = 0; i < amountNodes; i++) {
            nodes.add(createNode(i, location, category));
        }
        return nodes;
    }

    protected OnmsNode createNode(int count, OnmsMonitoringLocation location, OnmsCategory category) {
        OnmsNode node = new OnmsNode();
        node.setId(count);
        node.setLabel("Node" + count);
        node.setLocation(location);
        node.addCategory(category);
        node.setType(OnmsNode.NodeType.ACTIVE);
        node.setForeignSource("fs" + count);
        node.setForeignId("fid" + count);
        return node;
    }

    protected List<OnmsSnmpInterface> createSnmpInterfaces(List<OnmsNode> nodes) {
        ArrayList<OnmsSnmpInterface> interfaces = new ArrayList<>();
        for (int i = 0; i < topologySettings.getAmountSnmpInterfaces(); i++) {
            interfaces.add(createSnmpInterface(i, random.getRandom(nodes)));
        }
        return interfaces;
    }

    protected OnmsSnmpInterface createSnmpInterface(int ifIndex, OnmsNode node) {
        OnmsSnmpInterface onmsSnmpInterface = new OnmsSnmpInterface();
        onmsSnmpInterface.setId((node.getId() * topologySettings.getAmountSnmpInterfaces()) + ifIndex);
        onmsSnmpInterface.setNode(node);
        onmsSnmpInterface.setIfIndex(ifIndex);
        onmsSnmpInterface.setIfType(4);
        onmsSnmpInterface.setIfSpeed(5L);
        onmsSnmpInterface.setIfAdminStatus(6);
        onmsSnmpInterface.setIfOperStatus(7);
        onmsSnmpInterface.setLastCapsdPoll(new Date());
        onmsSnmpInterface.setLastSnmpPoll(new Date());
        nodeIfIndexes.computeIfAbsent(node.getId(), key -> ifIndex);

        return onmsSnmpInterface;
    }

    protected List<OnmsIpInterface> createIpInterfaces(List<OnmsSnmpInterface> snmps) {
        ArrayList<OnmsIpInterface> interfaces = new ArrayList<>();
        InetAddressGenerator inetGenerator = new InetAddressGenerator();
        for (int i = 0; i < topologySettings.getAmountIpInterfaces(); i++) {
            interfaces.add(createIpInterface(random.getRandom(snmps), inetGenerator.next()));
        }
        return interfaces;
    }

    protected OnmsIpInterface createIpInterface(OnmsSnmpInterface snmp, InetAddress inetAddress) {
        OnmsIpInterface ip = new OnmsIpInterface();
        ip.setId(snmp.getId());
        ip.setSnmpInterface(snmp);
        ip.setIpLastCapsdPoll(new Date());
        ip.setNode(Optional.ofNullable(snmp).map(OnmsSnmpInterface::getNode).orElse(null));
        ip.setIpAddress(inetAddress);
        return ip;
    }

    protected <E> PairGenerator<E> createPairGenerator(List<E> elements) {
        if (TopologyGenerator.Topology.complete == topologySettings.getTopology()) {
            return new UndirectedPairGenerator<>(elements);
        } else if (TopologyGenerator.Topology.ring == topologySettings.getTopology()) {
            return new LinkedPairGenerator<>(elements);
        } else if (TopologyGenerator.Topology.random == topologySettings.getTopology()) {
            return new RandomConnectedPairGenerator<>(elements);
        } else {
            throw new IllegalArgumentException("unknown topology: " + topologySettings.getTopology());
        }
    }
}
