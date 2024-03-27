/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
