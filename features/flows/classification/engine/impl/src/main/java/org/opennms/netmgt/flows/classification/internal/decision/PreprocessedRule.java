/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.flows.classification.internal.decision;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.IpAddr;
import org.opennms.netmgt.flows.classification.internal.value.IpValue;
import org.opennms.netmgt.flows.classification.internal.value.PortValue;
import org.opennms.netmgt.flows.classification.internal.value.ProtocolValue;
import org.opennms.netmgt.flows.classification.persistence.api.RuleDefinition;

/**
 * Bundles a rule with derived information. Improves tree construction performance.
 */
public class PreprocessedRule {

    public static PreprocessedRule of(RuleDefinition rule) {
        return new PreprocessedRule(rule,
                rule.hasProtocolDefinition() ? ProtocolValue.of(rule.getProtocol()) : null,
                rule.hasSrcPortDefinition() ? PortValue.of(rule.getSrcPort()) : null,
                rule.hasDstPortDefinition() ? PortValue.of(rule.getDstPort()) : null,
                rule.hasSrcAddressDefinition() ? IpValue.of(rule.getSrcAddress()) : null,
                rule.hasDstAddressDefinition() ? IpValue.of(rule.getDstAddress()) : null
        );
    }

    private static Stream<Threshold> protocolThresholds(ProtocolValue value) {
        return value == null ? Stream.empty() : value.getProtocols().stream().map(Threshold.Protocol::new);
    }

    private static Stream<Threshold> portThresholds(
            PortValue value,
            Function<Integer, Threshold> thresholdCreator
    ) {
        return value == null ? Stream.empty() : value
                .getPortRanges()
                .stream()
                .flatMap(range -> Stream.of(range.getBegin(), range.getEnd()))
                .map(thresholdCreator);
    }

    private static Stream<Threshold> addressThresholds(
            IpValue value,
            Function<IpAddr, Threshold> thresholdCreator
    ) {
        return value == null ? Stream.empty() : value
                .getIpAddressRanges()
                .stream()
                .flatMap(range -> Stream.of(range.begin, range.end))
                .map(thresholdCreator);
    }

    public final RuleDefinition ruleDefinition;

    // if a rule does not specify a criteria for some aspect then the corresponding ProtocolValue, PortValue, or IpValue is null
    public final ProtocolValue protocol;
    public final PortValue srcPort, dstPort;
    public final IpValue srcAddr, dstAddr;

    // candidate thresholds derived from the rules values
    public final Set<Threshold> thresholds;

    public PreprocessedRule(RuleDefinition ruleDefinition, ProtocolValue protocol, PortValue srcPort, PortValue dstPort, IpValue srcAddr, IpValue dstAddr) {
        this.ruleDefinition = ruleDefinition;
        this.protocol = protocol;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.srcAddr = srcAddr;
        this.dstAddr = dstAddr;
        this.thresholds = Stream.of(
                protocolThresholds(protocol),
                portThresholds(srcPort, Threshold.SrcPort::new),
                portThresholds(dstPort, Threshold.DstPort::new),
                addressThresholds(srcAddr, Threshold.SrcAddress::new),
                addressThresholds(dstAddr, Threshold.DstAddress::new)
        ).flatMap(Function.identity()).collect(Collectors.toSet());
    }

    public Classifier createClassifier(FilterService filterService, Bounds bounds) {
        return Classifier.of(this, filterService, bounds);
    }

    public PreprocessedRule reverse() {
        return new PreprocessedRule(
                ruleDefinition.reversedRule(),
                protocol,
                dstPort,
                srcPort,
                dstAddr,
                srcAddr
        );
    }

}
