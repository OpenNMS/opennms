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

import org.opennms.netmgt.flows.classification.IpAddr;
import org.opennms.netmgt.flows.classification.dto.RuleDTO;
import org.opennms.netmgt.flows.classification.internal.value.IpValue;
import org.opennms.netmgt.flows.classification.internal.value.PortValue;
import org.opennms.netmgt.flows.classification.internal.value.ProtocolValue;

import com.google.common.base.Strings;

/**
 * Bundles a rule with derived information. Improves tree construction performance.
 */
public class PreprocessedRule {

    public static PreprocessedRule of(RuleDTO rule) {
        return new PreprocessedRule(rule,
                                    rule.getProtocols() == null || rule.getProtocols().isEmpty() ? null : ProtocolValue.of(rule.getProtocols()),
                                    Strings.isNullOrEmpty(rule.getSrcPort()) ? null : PortValue.of(rule.getSrcPort()),
                                    Strings.isNullOrEmpty(rule.getDstPort()) ? null : PortValue.of(rule.getDstPort()),
                                    Strings.isNullOrEmpty(rule.getSrcAddress()) ? null : IpValue.of(rule.getSrcAddress()),
                                    Strings.isNullOrEmpty(rule.getDstAddress()) ? null : IpValue.of(rule.getDstAddress()),
                                    rule.getExporters() == null || rule.getExporters().isEmpty() ? null : IpValue.of(rule.getExporters()));
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

    public final RuleDTO rule;

    // if a rule does not specify a criteria for some aspect then the corresponding ProtocolValue, PortValue, or IpValue is null
    public final ProtocolValue protocol;
    public final PortValue srcPort, dstPort;
    public final IpValue srcAddr, dstAddr;
    public final IpValue exporterAddr;

    // candidate thresholds derived from the rules values
    public final Set<Threshold> thresholds;

    public PreprocessedRule(RuleDTO rule,
                            ProtocolValue protocol,
                            PortValue srcPort,
                            PortValue dstPort,
                            IpValue srcAddr,
                            IpValue dstAddr,
                            IpValue exporterAddr) {
        this.rule = rule;
        this.protocol = protocol;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.srcAddr = srcAddr;
        this.dstAddr = dstAddr;
        this.exporterAddr = exporterAddr;
        this.thresholds = Stream.of(
                protocolThresholds(protocol),
                portThresholds(srcPort, Threshold.SrcPort::new),
                portThresholds(dstPort, Threshold.DstPort::new),
                addressThresholds(srcAddr, Threshold.SrcAddress::new),
                addressThresholds(dstAddr, Threshold.DstAddress::new),
                addressThresholds(exporterAddr, Threshold.ExporterAddress::new)
        ).flatMap(Function.identity()).collect(Collectors.toSet());
    }

    public Classifier createClassifier(Bounds bounds) {
        return Classifier.of(this, bounds);
    }
}
