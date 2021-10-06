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
        var classificationEngine = new DefaultClassificationEngine(() -> rules, createNiceMock(FilterService.class));

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
