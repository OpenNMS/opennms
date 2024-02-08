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
package org.opennms.netmgt.flows.classification.internal;

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

    public static List<Rule> getRules(String resource) {
        var group = new GroupBuilder().withName(Groups.USER_DEFINED).build();
        final CsvService csvService = new CsvServiceImpl(org.mockito.Mockito.mock(RuleValidator.class));
        final List<Rule> rules = csvService.parseCSV(group, CsvServiceTest.class.getResourceAsStream(resource), true).getRules();
        int cnt = 0;
        for (var r: rules) {
            r.setPosition(cnt++);
        }
        return rules;
    }

    @State(Scope.Benchmark)
    public static class BState {

        @Param({"0", "1"})
        public int index;

        @Param({EXAMPLE_RULES_RESOURCE, PRE_DEFINED_RULES_RESOURCE})
        public String ruleSet;

        private ClassificationEngine classificationEngine;
        private List<ClassificationRequest> classificationRequests;

        @Setup
        public void setup() throws InterruptedException {
            var rules = getRules(ruleSet);
            classificationEngine = new DefaultClassificationEngine(() -> rules, org.mockito.Mockito.mock(FilterService.class));
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
