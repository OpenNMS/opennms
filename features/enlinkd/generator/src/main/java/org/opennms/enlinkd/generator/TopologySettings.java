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

package org.opennms.enlinkd.generator;

public class TopologySettings {
    private final int amountNodes;

    private final int amountElements;

    private final int amountLinks;

    private final int amountSnmpInterfaces;

    private final int amountIpInterfaces;

    private final TopologyGenerator.Topology topology;

    private final TopologyGenerator.Protocol protocol;

    private TopologySettings(
            Integer amountNodes,
            Integer amountElements,
            Integer amountLinks,
            Integer amountSnmpInterfaces,
            Integer amountIpInterfaces,
            TopologyGenerator.Topology topology,
            TopologyGenerator.Protocol protocol) {
        this.amountNodes = setToDefaultIfNotSet(amountNodes, 10);
        this.amountElements = setToDefaultIfNotSet(amountElements, this.amountNodes);
        this.amountLinks = setToDefaultIfNotSet(amountLinks, this.amountElements);
        this.amountSnmpInterfaces = setToDefaultIfNotSet(amountSnmpInterfaces, this.amountNodes * 18);
        this.amountIpInterfaces = setToDefaultIfNotSet(amountIpInterfaces, this.amountNodes * 2);
        this.topology = setToDefaultIfNotSet(topology, TopologyGenerator.Topology.random);
        this.protocol = setToDefaultIfNotSet(protocol, TopologyGenerator.Protocol.cdp);
    }

    private <T> T setToDefaultIfNotSet(T value, T defaultValue) {
        return (value == null) ? defaultValue : value;
    }

    public void verify() {
        // do basic checks to get configuration right:
        assertMoreOrEqualsThan("we need at least as many nodes as elements", this.amountElements, this.amountNodes);
        assertMoreOrEqualsThan("we need at least 2 nodes", 2, this.amountNodes);
        assertMoreOrEqualsThan("we need at least 2 elements", 2, this.amountElements);
        assertMoreOrEqualsThan("we need at least 1 link", 1, this.amountLinks);
        assertMoreOrEqualsThan("links must be less than or equal to number of snmp interfaces",
                this.amountLinks, this.amountSnmpInterfaces);
    }

    private static void assertMoreOrEqualsThan(String message, int expected, int actual) {
        if (actual < expected) {
            throw new IllegalArgumentException(message + String.format(" minimum expected=%s but found actual=%s", expected, actual));
        }
    }

    public int getAmountNodes() {
        return amountNodes;
    }

    public int getAmountElements() {
        return amountElements;
    }

    public int getAmountLinks() {
        return amountLinks;
    }

    public int getAmountSnmpInterfaces() {
        return amountSnmpInterfaces;
    }

    public int getAmountIpInterfaces() {
        return amountIpInterfaces;
    }

    public TopologyGenerator.Topology getTopology() {
        return topology;
    }

    public TopologyGenerator.Protocol getProtocol() {
        return protocol;
    }

    public static TopologySettingsBuilder builder() {
        return new TopologySettingsBuilder();
    }

    public static class TopologySettingsBuilder {
        private Integer amountNodes;
        private Integer amountElements;
        private Integer amountLinks;
        private Integer amountSnmpInterfaces;
        private Integer amountIpInterfaces;
        private TopologyGenerator.Topology topology;
        private TopologyGenerator.Protocol protocol;

        private TopologySettingsBuilder() {
        }

        public TopologySettingsBuilder amountNodes(Integer amountNodes) {
            this.amountNodes = amountNodes;
            return this;
        }

        public TopologySettingsBuilder amountElements(Integer amountElements) {
            this.amountElements = amountElements;
            return this;
        }

        public TopologySettingsBuilder amountLinks(Integer amountLinks) {
            this.amountLinks = amountLinks;
            return this;
        }

        public TopologySettingsBuilder amountSnmpInterfaces(Integer amountSnmpInterfaces) {
            this.amountSnmpInterfaces = amountSnmpInterfaces;
            return this;
        }

        public TopologySettingsBuilder amountIpInterfaces(Integer amountIpInterfaces) {
            this.amountIpInterfaces = amountIpInterfaces;
            return this;
        }

        public TopologySettingsBuilder topology(TopologyGenerator.Topology topology) {
            this.topology = topology;
            return this;
        }

        public TopologySettingsBuilder protocol(TopologyGenerator.Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public TopologySettings build() {
            return new TopologySettings(amountNodes, amountElements, amountLinks, amountSnmpInterfaces, amountIpInterfaces, topology, protocol);
        }

    }
}
