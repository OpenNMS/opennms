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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.internal.decision.Tree;

public class ExampleRulesTest {

    @Test
    public void exampleRuleSet() throws InterruptedException {
        testRuleSet("/example-rules.csv");
    }

    public void testRuleSet(String resource) throws InterruptedException {
        var rules = ClassificationEngineBenchmark.getRules(resource);
        var classificationEngine = new DefaultClassificationEngine(() -> rules, org.mockito.Mockito.mock(FilterService.class));

        // create classifiers for brute force classification
        var classifiers = rules.stream()
                .flatMap(r -> r.isOmnidirectional() ? Stream.of(r, r.reversedRule()) : Stream.of(r))
                .map(r -> RandomClassificationEngineTest.classifier(r))
                .sorted()
                .collect(Collectors.toList());

        // a visitor that can be used to inspect the decision tree
        var visitor = new Tree.Visitor<Void>() {
            Stack<String> steps = new Stack<>();

            @Override
            public Void visit(Tree.Node.WithChoice node) {
                steps.push("< " + node.threshold);
                node.lt.accept(this);
                steps.pop();
                steps.push("= " + node.threshold);
                node.eq.accept(this);
                steps.pop();
                steps.push("> " + node.threshold);
                node.gt.accept(this);
                steps.pop();
                steps.push("? " + node.threshold);
                node.na.accept(this);
                steps.pop();
                return null;
            }

            @Override
            public Void visit(Tree.Node.WithoutChoice node) {
                steps.push("< " + node.threshold);
                node.lt.accept(this);
                steps.pop();
                steps.push("= " + node.threshold);
                node.eq.accept(this);
                steps.pop();
                steps.push("> " + node.threshold);
                node.gt.accept(this);
                steps.pop();
                return null;
            }

            @Override
            public Void visit(Tree.Leaf.Empty leaf) {
                return null;
            }

            @Override
            public Void visit(Tree.Leaf.WithClassifiers leaf) {
                if (leaf.classifiers().stream().anyMatch(c -> c.result.name.equals("Skype_Lync_Application_Sharing"))) {
//                if (leaf.classifiers.size() >= 3) {
                    System.out.println(steps.stream().map(s -> "(" + s + ")").collect(Collectors.joining(", ")));
                    leaf.classifiers().forEach(c -> System.out.println("  " + c));
                }
                return null;
            }
        };

        classificationEngine.getTree().accept(visitor);

        // check
        RandomClassificationEngineTest.streamOfclassificationRequests(rules, 123456l).limit(1000000).forEach(cr -> {
            var byEngine = Optional.ofNullable(classificationEngine.classify(cr));
            var byBruteForce = classifiers.stream().map(c -> c.classify(cr)).filter(s -> s != null).findFirst().map(r -> r.name);
            assertThat("classification request: " + cr, byEngine, is(byBruteForce));
        });
    }

}
