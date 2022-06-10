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
package org.opennms.netmgt.flows.classification.internal;

import static org.easymock.EasyMock.createNiceMock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.opennms.core.network.IPAddress;
import org.opennms.core.network.IPAddressRange;
import org.opennms.core.network.IPPortRange;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.internal.decision.Classifier;
import org.opennms.netmgt.flows.classification.internal.matcher.DstAddressMatcher;
import org.opennms.netmgt.flows.classification.internal.matcher.DstPortMatcher;
import org.opennms.netmgt.flows.classification.internal.matcher.FilterMatcher;
import org.opennms.netmgt.flows.classification.internal.matcher.Matcher;
import org.opennms.netmgt.flows.classification.internal.matcher.ProtocolMatcher;
import org.opennms.netmgt.flows.classification.internal.matcher.SrcAddressMatcher;
import org.opennms.netmgt.flows.classification.internal.matcher.SrcPortMatcher;
import org.opennms.netmgt.flows.classification.IpAddr;
import org.opennms.netmgt.flows.classification.internal.value.IpValue;
import org.opennms.netmgt.flows.classification.internal.value.PortValue;
import org.opennms.netmgt.flows.classification.internal.value.StringValue;
import org.opennms.netmgt.flows.classification.persistence.api.Protocol;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.opennms.netmgt.flows.classification.persistence.api.RuleDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.Shrinkable;
import net.jqwik.api.Tuple;

/**
 * Tests the classification engine using generated random rule sets.
 * <p>
 * Random rule sets are generated and used to instantiate classification engines. Then randomized classification
 * requests are generated based on the rule sets. Classification is checked by comparing the outcome when classifying
 * by the decision tree or by a simple brute force algorithm.
 */
public class RandomClassificationEngineTest {

    private static Logger LOG = LoggerFactory.getLogger(RandomClassificationEngineTest.class);

    private static int MAX_PROTOCOL = 9;
    private static int MAX_PORT = 65535;
    private static int MAX_ADDR = 65535;

    private static int MAX_RULES = 100;

    private static final FilterService FILTER_SERVICE = createNiceMock(FilterService.class);

    private static IPAddress ipAddress(int value) {
        var bytes = new byte[]{(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value};
        return new IPAddress(bytes);
    }

    private static String string(IPPortRange r) {
        if (r.getBegin() == r.getEnd()) {
            return String.valueOf(r.getBegin());
        } else {
            return String.valueOf(r.getBegin()) + '-' + String.valueOf(r.getEnd());
        }
    }

    private static String string(IPAddressRange r) {
        if (r.getBegin().equals(r.getEnd())) {
            return r.getBegin().toUserString();
        } else {
            return r.getBegin().toUserString() + '-' + r.getEnd().toUserString();
        }
    }

    @Property
    public boolean test(
            @ForAll("rulesAndRequests") Tuple.Tuple2<List<Rule>, List<ClassificationRequest>> rulesAndRequests
    ) throws InterruptedException {
        LOG.debug("construct decision tree");
        if (LOG.isDebugEnabled()) {
            rulesAndRequests.get1().forEach(r -> {
                var s = Stream.of(r.getName(), r.getProtocol(), r.getSrcAddress(), r.getSrcPort(), r.getDstAddress(), r.getDstPort(), "", String.valueOf(r.isOmnidirectional())).collect(Collectors.joining(";"));
                System.out.println(s);
            });
        }
        var ce = new DefaultClassificationEngine(() -> rulesAndRequests.get1(), FILTER_SERVICE);

        var classifiers = rulesAndRequests.get1().stream()
                .flatMap(r -> r.isOmnidirectional() ? Stream.of(r, r.reversedRule()) : Stream.of(r))
                .map(r -> RandomClassificationEngineTest.classifier(r))
                .sorted()
                .collect(Collectors.toList());

        var res = rulesAndRequests.get2().stream().allMatch(r -> {
            var appByTree = Optional.ofNullable(ce.classify(r));
            // brute force classification
            // -> take the result of the first matching classifier
            var appDirect = classifiers.stream().map(c -> c.classify(r)).filter(s -> s != null).findFirst().map(cr -> cr.name);
            return Objects.equals(appByTree, appDirect);
        });
        LOG.debug("checked classification");
        return res;
    }

    @Provide
    public Arbitrary<Tuple.Tuple2<List<Rule>, List<ClassificationRequest>>> rulesAndRequests() {
        return rules(0, MAX_RULES, MAX_PROTOCOL, MAX_PORT, MAX_ADDR).flatMap(rules -> classificationRequest(rules).list().map(requests -> Tuple.of(rules, requests)));
    }

    /**
     * Generates arbitrary lists of rules.
     * <p>
     * Protococls, ports, and addresses are in ranges of 0 to the corresponding specified maximum (inclusive).
     *
     * @param minRules    minimum number of rules
     * @param maxRules    maximum number of rules
     * @param maxProtocol maximum protocol number
     * @param maxPort     maximum port number for src/dst port
     * @param maxAddr     maximum address for src/dst addresses
     */
    public static Arbitrary<List<Rule>> rules(int minRules, int maxRules, int maxProtocol, int maxPort, int maxAddr) {
        var protocols = Arbitraries.integers().between(0, maxProtocol).map(Protocols::getProtocol).list().ofMinSize(0).ofMaxSize(5);

        var portRanges = Arbitraries.integers().between(0, maxPort)
                .flatMap(begin ->
                        Arbitraries.integers().between(begin, maxPort)
                                .map(end -> new IPPortRange(begin, end))
                ).list().ofMinSize(0).ofMaxSize(5);

        var addressRanges = Arbitraries.integers().between(0, maxAddr)
                .flatMap(begin ->
                        Arbitraries.integers().between(begin, maxAddr)
                                .map(end -> new IPAddressRange(ipAddress(begin), ipAddress(end)))
                ).list().ofMinSize(0).ofMaxSize(5);

        var omnidirectional = Arbitraries.of(true, false);

        return Combinators.combine(protocols, portRanges, portRanges, addressRanges, addressRanges, omnidirectional).as(
                (protos, srcPortRanges, dstPortRanges, srcAddressRanges, dstAddressRanges, omnidir) ->
                        new RuleBuilder()
                                .withName("x") // the name will be set again when the list is mapped below
                                .withProtocol(protos.stream().map(p -> p.getKeyword()).collect(Collectors.joining(",")))
                                .withSrcPort(srcPortRanges.stream().map(RandomClassificationEngineTest::string).collect(Collectors.joining(",")))
                                .withDstPort(dstPortRanges.stream().map(RandomClassificationEngineTest::string).collect(Collectors.joining(",")))
                                .withSrcAddress((srcAddressRanges.stream().map(RandomClassificationEngineTest::string).collect(Collectors.joining(","))))
                                .withDstAddress((dstAddressRanges.stream().map(RandomClassificationEngineTest::string).collect(Collectors.joining(","))))
                                .withOmnidirectional(omnidir)
                                .build()

        ).list().ofMinSize(minRules).ofMaxSize(maxRules).map(rules -> {
            var pos = 0;
            for (var r : rules) {
                r.setName(String.valueOf(pos));
                r.setPosition(pos++);
            }
            return rules;
        });
    }

    /**
     * Given a rule set generate arbitrary classification requests for that rule set.
     * <p>
     * All protocols, ports, and addresses are collected from these rules and randomly combined into classification
     * requests.
     */
    public static Arbitrary<ClassificationRequest> classificationRequest(Collection<Rule> rules) {
        var protocols = new HashSet<Integer>();
        var ports = new HashSet<Integer>();
        var addresses = new HashSet<IpAddr>();

        for (var r : rules) {
            new StringValue(r.getProtocol())
                    .splitBy(",")
                    .stream()
                    .map(p -> Protocols.getProtocol(p.getValue()))
                    .filter(p -> p != null)
                    .map(Protocol::getDecimal)
                    .forEach(protocols::add);
            PortValue.of(r.getSrcPort())
                    .getPortRanges()
                    .stream()
                    .flatMap(range -> Stream.of(range.getBegin(), range.getEnd()))
                    .forEach(ports::add);
            PortValue.of(r.getDstPort())
                    .getPortRanges()
                    .stream()
                    .flatMap(range -> Stream.of(range.getBegin(), range.getEnd()))
                    .forEach(ports::add);
            if (StringUtils.isNoneBlank(r.getSrcAddress())) {
                IpValue.of(r.getSrcAddress())
                        .getIpAddressRanges()
                        .stream()
                        .flatMap(range -> Stream.of(range.begin, range.end))
                        .forEach(addresses::add);
            }
            if (StringUtils.isNoneBlank(r.getDstAddress())) {
                IpValue.of(r.getDstAddress())
                        .getIpAddressRanges()
                        .stream()
                        .flatMap(range -> Stream.of(range.begin, range.end))
                        .forEach(addresses::add);
            }
        }
        return classificationRequest(protocols, ports, addresses);
    }

    private static Arbitrary<ClassificationRequest> classificationRequest(Set<Integer> protocols, Set<Integer> ports, Set<IpAddr> addresses) {
        final var protocolsArray = protocols.toArray(new Integer[0]);
        final var portsArray = ports.toArray(new Integer[0]);
        final var addressesArray = addresses.toArray(new IpAddr[0]);
        // in practice classification request will always have their protocol, src/dst port/addr being set
        // -> use zero as a default in case that a set is empty
        final var protocolsArb = protocolsArray.length == 0 ? Arbitraries.just(0) : Arbitraries.of(protocolsArray);
        final var portsArb = portsArray.length == 0 ? Arbitraries.just(0) : Arbitraries.of(portsArray);
        final var addressesArb = addressesArray.length == 0 ? Arbitraries.just(IpAddr.of("0.0.0.0")) : Arbitraries.of(addressesArray);
        return Combinators.combine(
                protocolsArb,
                portsArb,
                portsArb,
                addressesArb,
                addressesArb
        ).as((protocol, srcPort, dstPort, srcAddr, dstAddr) ->
                new ClassificationRequest("default", srcPort, srcAddr, dstPort, dstAddr, Protocols.getProtocol(protocol))
        );
    }

    public static Classifier classifier(RuleDefinition ruleDefinition) {
        final List<Matcher> matchers = new ArrayList<>();
        if (ruleDefinition.hasProtocolDefinition()) {
            matchers.add(new ProtocolMatcher(ruleDefinition.getProtocol()));
        }
        if (ruleDefinition.hasSrcPortDefinition()) {
            matchers.add(new SrcPortMatcher(ruleDefinition.getSrcPort()));
        }
        if (ruleDefinition.hasSrcAddressDefinition()) {
            matchers.add(new SrcAddressMatcher(ruleDefinition.getSrcAddress()));
        }
        if (ruleDefinition.hasDstAddressDefinition()) {
            matchers.add(new DstAddressMatcher(ruleDefinition.getDstAddress()));
        }
        if (ruleDefinition.hasDstPortDefinition()) {
            matchers.add(new DstPortMatcher(ruleDefinition.getDstPort()));
        }
        int matchedAspects = matchers.size();
        if (ruleDefinition.hasExportFilterDefinition()) {
            matchers.add(new FilterMatcher(ruleDefinition.getExporterFilter(), FILTER_SERVICE));
        }
        return new Classifier(
                matchers.toArray(new Matcher[matchers.size()]),
                new Classifier.Result(matchedAspects, ruleDefinition.getName()),
                ruleDefinition.getGroupPosition(),
                ruleDefinition.getPosition()
        );
    }

    public static Stream<ClassificationRequest> streamOfclassificationRequests(Collection<Rule> rules, long seed) {
        return classificationRequest(rules).generator(1000).stream(new Random(seed)).map(Shrinkable::value);
    }

}
