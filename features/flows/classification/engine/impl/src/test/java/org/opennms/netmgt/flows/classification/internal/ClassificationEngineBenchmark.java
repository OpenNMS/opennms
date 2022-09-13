/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2022 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.flows.classification.internal;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.BooleanUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.dto.RuleDTO;
import org.opennms.netmgt.flows.classification.persistence.api.GroupBuilder;
import org.opennms.netmgt.flows.classification.persistence.api.Groups;
import org.opennms.netmgt.flows.classification.persistence.api.Protocol;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Use the Java Microbenchmarking Harness (JMH) to measure classification performance.
 * <p>
 * Rule sets are loaded from csv files and classification is done with randomly generated flows based on the
 * protocols, ports, and addresses found in the loaded rule sets.
 */
public class ClassificationEngineBenchmark {

    // the number of classification request that are processed in a single benchmark method call
    // -> the reported number of operations per second must be multiplied by this number to get
    //    the number of classifications per second
    private static final int BATCH_SIZE = 1000;

    // the benchmark is run for different rule sets
    private static final String EXAMPLE_RULES_RESOURCE = "/example-rules.csv";
    private static final String PRE_DEFINED_RULES_RESOURCE = "/pre-defined-rules.csv";

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    public static List<RuleDTO> getRules(String resource) throws IOException {
        var group = new GroupBuilder().withName(Groups.USER_DEFINED).build();

        CSVFormat csvFormat = CSVFormat.RFC4180
                .withDelimiter(';')
                .withHeader();
        final CSVParser parser = csvFormat.parse(new InputStreamReader(ClassificationEngineBenchmark.class.getResourceAsStream(resource)));
        final var rules = parser.getRecords().stream()
                                .map(record -> {
                                    final Rule rule = new Rule();
                                    rule.setGroup(group);
                                    rule.setName(Strings.emptyToNull(record.get(0)));
                                    rule.setProtocol(record.get(1));
                                    rule.setSrcAddress(Strings.emptyToNull(record.get(2)));
                                    rule.setSrcPort(Strings.emptyToNull(record.get(3)));
                                    rule.setDstAddress(Strings.emptyToNull(record.get(4)));
                                    rule.setDstPort(Strings.emptyToNull(record.get(5)));
                                    rule.setExporterFilter(Strings.emptyToNull(record.get(6)));
                                    rule.setOmnidirectional(BooleanUtils.toBoolean(Strings.emptyToNull(record.get(7))));
                                    return rule;
                                })
                                .collect(Collectors.toList());

        final List<RuleDTO> result = Lists.newArrayListWithCapacity(rules.size());
        for (int i = 0; i < rules.size(); i++) {
            final var rule = rules.get(i);
            result.add(RuleDTO.builder()
                              .withName(rule.getName())
                              .withProtocols(Splitter.on(',').omitEmptyStrings().splitToList(rule.getProtocol()).stream()
                                                     .map(Protocols::getProtocol)
                                                     .map(Protocol::getDecimal)
                                                     .collect(Collectors.toList()))
                              .withDstAddress(rule.getDstAddress())
                              .withDstPort(rule.getDstPort())
                              .withSrcAddress(rule.getSrcAddress())
                              .withSrcPort(rule.getSrcPort())
                              .withPosition(i)
                              .build());
        }

        // TODO fooker: re-add omnidirectional rules

        return result;
    }

    @State(Scope.Benchmark)
    public static class BState {

        @Param({"0", "1"})
        public int index;

        @Param({EXAMPLE_RULES_RESOURCE, PRE_DEFINED_RULES_RESOURCE})
        public String ruleSet;

        private ReloadingClassificationEngine classificationEngine;
        private List<ClassificationRequest> classificationRequests;

        @Setup
        public void setup() throws Exception {
            var rules = getRules(ruleSet);

            classificationEngine = new DefaultClassificationEngine();
            classificationEngine.load(rules);

            classificationRequests = RandomClassificationEngineTest.streamOfclassificationRequests(rules, 123456l).skip(index * BATCH_SIZE).limit(BATCH_SIZE).collect(Collectors.toList());
        }

        public List<ClassificationRequest> requests() {
            return classificationRequests;
        }

        public ClassificationEngine classificationEngine() {
            return classificationEngine;
        }

    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 1)
    @Measurement(iterations = 2)
    public void classify(BState state, Blackhole blackhole) {
        var classificationEngine = state.classificationEngine();
        for (var cr: state.requests()) {
            var app = classificationEngine.classify(cr);
            blackhole.consume(app);
        }
    }

}
