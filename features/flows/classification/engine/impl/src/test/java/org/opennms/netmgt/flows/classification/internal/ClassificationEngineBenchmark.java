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

import java.util.List;
import java.util.stream.Collectors;

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
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.csv.CsvService;
import org.opennms.netmgt.flows.classification.internal.csv.CsvServiceImpl;
import org.opennms.netmgt.flows.classification.internal.csv.CsvServiceTest;
import org.opennms.netmgt.flows.classification.internal.validation.RuleValidator;
import org.opennms.netmgt.flows.classification.persistence.api.GroupBuilder;
import org.opennms.netmgt.flows.classification.persistence.api.Groups;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

public class ClassificationEngineBenchmark {

    // the number of classification request that are processed in a single benchmark method call
    // -> the reported number of operations per second must be multiplied by this number to get
    //    the number of classifications per second
    private static final int BATCH_SIZE = 1000;

    // number of batches of different classification requests
    // -> the @Param annotation of the BState.index member must be set correspondingly
    private static final int BATCHES = 3;

    private static final String RULES_RESOURCE = "/example-rules.csv";

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    private static class Init {

        private static ClassificationEngine classificationEngine, oldCe;

        private static List<ClassificationRequest> requests;

        static {
            final List<Rule> rules = getRules(RULES_RESOURCE);
            classificationEngine = new DefaultClassificationEngine(() -> rules, createNiceMock(FilterService.class));
            requests = RandomClassificationEngineTest.streamOfclassificationRequests(rules, 123456l).limit(BATCHES * BATCH_SIZE).collect(Collectors.toList());
        }
    }

    public static List<Rule> getRules(String resource) {
        var group = new GroupBuilder().withName(Groups.USER_DEFINED).build();
        final CsvService csvService = new CsvServiceImpl(createNiceMock(RuleValidator.class));
        final List<Rule> rules = csvService.parseCSV(group, CsvServiceTest.class.getResourceAsStream(resource), true).getRules();
        int cnt = 0;
        for (var r: rules) {
            r.setPosition(cnt++);
        }
        return rules;
    }

    @State(Scope.Benchmark)
    public static class BState {

        /**
         * Corresponds to {@link #BATCHES}
         */
        @Param({"0", "1", "2", "3", "4"})
        public int index;

        @Setup
        public void setup() {
            // trigger initialization
            Init.requests.size();
        }

        public List<ClassificationRequest> requests() {
            return Init.requests.subList(index * BATCH_SIZE, (index + 1) * BATCH_SIZE);
        }

    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 1)
    @Measurement(iterations = 2)
    public void byNew(BState state, Blackhole blackhole) {
        for (var cr: state.requests()) {
            var app = Init.classificationEngine.classify(cr);
            blackhole.consume(app);
        }
    }

}
