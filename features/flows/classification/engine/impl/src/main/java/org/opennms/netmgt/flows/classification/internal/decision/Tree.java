/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2021 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.FilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a decision tree for classifying flows.
 * <p>
 * The tree consists of nested nodes that define thresholds for various aspects of flows (e.g. source ports or
 * destination addresses) and leaves that contain an ordered lists of classifiers. When a classification request
 * is processed it is compared to thresholds and traversal continues in subtrees corresponding to the comparison
 * result.
 * <p>
 * A special twist is that classification rules and classification requests may not contain a value for certain
 * aspects. For example a classification rule may have no ports being set or a classification request may be missing protocol
 * information. This is handled by having child nodes for 'non-applicable' rules and considering 'non-applicable'
 * orderings when comparing a classification request with thresholds (cf. {@link Threshold#compare(ClassificationRequest)}).
 */
public abstract class Tree {

    private static Logger LOG = LoggerFactory.getLogger(Tree.class);

    /**
     * Recursively constructs a decision tree consisting of nodes that split the given rules by thresholds
     * and leaves that contain the classifiers that were selected by the thresholds of their ancestor nodes.
     */
    public static Tree of(List<PreprocessedRule> rules, FilterService filterService) {
        return of(rules, Bounds.ANY, 0, filterService);
    }

    private static Tree of(List<PreprocessedRule> rules, Bounds bounds, int depth, FilterService filterService) {
        final var ruleSetSize = rules.size();
        if (ruleSetSize <= 1) {
            LOG.debug("Leaf - depth: " + depth + "; rules: " + ruleSetSize);
            return leaf(rules, filterService, bounds);
        }

        // Determine a threshold that results in the "optimum" split.
        var entry = thresholds(rules)
                .distinct()
                .parallel()
                .filter(t -> t.canRestrict(bounds))
                .map(t -> Map.entry(t, t.match(rules, bounds)))
                .filter(e -> maximumSize(e) < ruleSetSize)
                // different ordering criteria could be used here
                // (for example the summed sizes of the collections that result if the rule set is matched by a threshold was also tested)
                // -> the criteria that optimizes for the equal partitioning of rule sets gave the best results
                .min(Comparator.comparingLong(equallyPartitionedCriteria(ruleSetSize)))
                .orElse(null);

        if (entry != null) {
            LOG.debug("Node - depth: " + depth + "; rules: " + ruleSetSize + "; threshold: " + entry.getKey() + "; maximum child size: " + maximumSize(entry) +
                               "; lt: " + entry.getValue().lt.size() +
                               "; eq: " + entry.getValue().eq.size() + "; gt: " + entry.getValue().gt.size() + "; na: " + entry.getValue().na.size());
            var lt = of(entry.getValue().lt, entry.getKey().lt(bounds), depth + 1, filterService);
            var eq = of(entry.getValue().eq, entry.getKey().eq(bounds), depth + 1, filterService);
            var gt = of(entry.getValue().gt, entry.getKey().gt(bounds), depth + 1, filterService);
            var na = of(entry.getValue().na, bounds, depth + 1, filterService);

            return node(entry.getKey(), lt, eq, gt, na);
        } else {
            LOG.debug("Leaf - depth: " + depth + "; rules: " + ruleSetSize);
            return leaf(rules, filterService, bounds);
        }

    }

    private static ToLongFunction<Map.Entry<Threshold, Threshold.Matches>> equallyPartitionedCriteria(int ruleSetSize) {
        // calculates a criteria corresponding to the variance of collection sizes assuming an equal partitioning a rule set
        var mean = (long) ruleSetSize / 4;
        ToLongFunction<Integer> f = i -> (i - mean) * (i - mean);
        return entry -> {
            var m = entry.getValue();
            return f.applyAsLong(m.lt.size()) + f.applyAsLong(m.eq.size()) + f.applyAsLong(m.gt.size()) + f.applyAsLong(m.na.size());
        };
    }

    private static int maximumSize(Map.Entry<Threshold, Threshold.Matches> entry) {
        var m = entry.getValue();
        return Math.max(Math.max(Math.max(m.lt.size(), m.eq.size()), m.gt.size()), m.na.size());
    }

    private static Stream<Threshold> thresholds(Collection<PreprocessedRule> ruleSet) {
        return ruleSet.stream().flatMap(rule -> rule.thresholds.stream());
    }

    public static final Tree EMPTY = Leaf.EMPTY;

    /**
     * Collects statistical information about a decision tree.
     */
    public static class Info {

        public static Info FOR_LEAVE_WITH_0_RULES = new Info(0);
        public static Info FOR_LEAVE_WITH_1_RULE = new Info(1);
        public static Info FOR_LEAVE_WITH_2_RULES = new Info(2);
        public static Info FOR_LEAVE_WITH_3_RULES = new Info(3);

        public static Info forLeaf(int ruleSetSize) {
            switch (ruleSetSize) {
                case 0: return FOR_LEAVE_WITH_0_RULES;
                case 1: return FOR_LEAVE_WITH_1_RULE;
                case 2: return FOR_LEAVE_WITH_2_RULES;
                case 3: return FOR_LEAVE_WITH_3_RULES;
                default: return new Info(ruleSetSize);
            }
        }

        public final int minDepth, maxDepth, sumDepth;
        public final long minRuleSetSize, maxRuleSetSize, sumRuleSetSize;
        public final int nodes;
        public final int leaves;
        public final int choices;
        public final int minComp, maxComp, sumComp;

        public Info(int minDepth, int maxDepth, int sumDepth, long minRuleSetSize, long maxRuleSetSize, long sumRuleSetSize, int nodes, int leaves, int choices, int minComp, int maxComp, int sumComp) {
            this.minDepth = minDepth;
            this.maxDepth = maxDepth;
            this.sumDepth = sumDepth;
            this.minRuleSetSize = minRuleSetSize;
            this.maxRuleSetSize = maxRuleSetSize;
            this.sumRuleSetSize = sumRuleSetSize;
            this.nodes = nodes;
            this.leaves = leaves;
            this.choices = choices;
            this.minComp = minComp;
            this.maxComp = maxComp;
            this.sumComp = sumComp;
        }

        public Info(long ruleSetSize) {
            this(0, 0, 0, ruleSetSize, ruleSetSize, ruleSetSize, 0, 1, 0, 0, 0, 0);
        }

        @Override
        public String toString() {
            return "Info{" +
                   "depth(min/max/avg)=" + minDepth +
                   "/" + maxDepth +
                   "/" + String.format("%.2f", (double)sumDepth / leaves) +
                   ", comp(min/max/avg)=" + minComp +
                   "/" + maxComp +
                   "/" + String.format("%.2f", (double)sumComp / leaves) +
                   ", ruleSetSize(min/max/avg)=" + minRuleSetSize +
                   "/" + maxRuleSetSize +
                   "/" + String.format("%.2f", (double)sumRuleSetSize / leaves) +
                   ", nodes=" + nodes +
                   ", leaves=" + leaves +
                   ", choices=" + choices +
                   '}';
        }
    }

    // smart constructor for nodes
    private static Tree node(Threshold threshold, Tree lt, Tree eq, Tree gt, Tree na) {
        // optimize for cases with / without an "na" collection
        if (na.isEmpty()) {
            var leaves = lt.info.leaves + eq.info.leaves + gt.info.leaves;
            // the comparison taking place at the constructed node is done for all leaves
            // -> add the number of leaves to the sum of comparisons in addition to the sums of comparisons of subtrees
            var sumComp = leaves + lt.info.sumComp + eq.info.sumComp + gt.info.sumComp;
            var info = new Info(
                    1 + Math.min(Math.min(lt.info.minDepth, eq.info.minDepth), gt.info.minDepth),
                    1 + Math.max(Math.max(lt.info.maxDepth, eq.info.maxDepth), gt.info.maxDepth),
                    leaves + lt.info.sumDepth + eq.info.sumDepth + gt.info.sumDepth,
                    Math.min(Math.min(lt.info.minRuleSetSize, eq.info.minRuleSetSize), gt.info.minRuleSetSize),
                    Math.max(Math.max(lt.info.maxRuleSetSize, eq.info.maxRuleSetSize), gt.info.maxRuleSetSize),
                    lt.info.sumRuleSetSize + eq.info.sumRuleSetSize + gt.info.sumRuleSetSize,
                    1 + lt.info.nodes + eq.info.nodes + gt.info.nodes,
                    leaves,
                    lt.info.choices + eq.info.choices + gt.info.choices,
                    1 + Math.min(Math.min(lt.info.minComp, eq.info.minComp), gt.info.minComp),
                    1 + Math.max(Math.max(lt.info.maxComp, eq.info.maxComp), gt.info.maxComp),
                    sumComp
            );
            return new Node.WithoutChoice(info, threshold, lt, eq, gt);

        } else {
            var nonNaLeaves = lt.info.leaves + eq.info.leaves + gt.info.leaves;
            var leaves = nonNaLeaves + na.info.leaves;
            // the na subtree is also traversed for all requests that are lt, eq, or gt than the threshold
            // -> the fraction of these additionally traverses is "nonNaLeaves / leaves"
            // -> boost the sum of comparisons of the na subtree by that factor
            var naBoost = 1.0 + (double)nonNaLeaves / leaves;
            var sumComp = leaves + lt.info.sumComp + eq.info.sumComp + gt.info.sumComp + (int)(naBoost * na.info.sumComp);
            var info = new Info(
                    1 + Math.min(Math.min(Math.min(lt.info.minDepth, eq.info.minDepth), gt.info.minDepth), na.info.minDepth),
                    1 + Math.max(Math.max(Math.max(lt.info.maxDepth, eq.info.maxDepth), gt.info.maxDepth), na.info.maxDepth),
                    leaves + lt.info.sumDepth + eq.info.sumDepth + gt.info.sumDepth + na.info.sumDepth,
                    Math.min(Math.min(Math.min(lt.info.minRuleSetSize, eq.info.minRuleSetSize), gt.info.minRuleSetSize), na.info.minRuleSetSize),
                    Math.max(Math.max(Math.max(lt.info.maxRuleSetSize, eq.info.maxRuleSetSize), gt.info.maxRuleSetSize), na.info.maxRuleSetSize),
                    lt.info.sumRuleSetSize + eq.info.sumRuleSetSize + gt.info.sumRuleSetSize + na.info.sumRuleSetSize,
                    1 + lt.info.nodes + eq.info.nodes + gt.info.nodes + na.info.nodes,
                    leaves,
                    1 + lt.info.choices + eq.info.choices + gt.info.choices + na.info.choices,
                    1 + Math.min(Math.min(lt.info.minComp, eq.info.minComp), gt.info.minComp) + na.info.minComp,
                    1 + Math.max(Math.max(lt.info.maxComp, eq.info.maxComp), gt.info.maxComp) + na.info.maxComp,
                    sumComp
            );
            return new Node.WithChoice(info, threshold, lt, eq, gt, na);
        }
    }

    public final Info info;

    protected Tree(Info info) {
        this.info = info;
    }

    /**
     * Classifies a flow.
     * <p>
     * Recursively traverses the decision tree and collects applicable classifiers. The collected classifiers are
     * sorted by their priorities and the result of the first matching classifier is returned. In case that several
     * classifiers with the maximum priority match more specific classifiers (i.e. classifiers that consider more
     * aspects of flows) are prefered.
     *
     * @return Returns <code>null</code> if the request does not match a rule
     */
    public final String classify(ClassificationRequest request) {
        var classifiers = classifiers(request);
        final Collection<Classifier> cs;
        if (classifiers.ordered) {
            cs = classifiers.classifiers;
        } else {
            cs = classifiers.classifiers.stream().sorted().collect(Collectors.toList());
        }

        // classifiers are split according to their natural order (based on their groupPosition & position fields)
        // -> return the result with the maximum number of matched aspects in the first group of classifiers that has
        //    at least one classification result
        Classifier firstInSplit = null;
        Classifier.Result result = null;
        for (var c: cs) {
            // detect group change
            if (firstInSplit == null) {
                firstInSplit = c;
            } else {
                if (c.compareTo(firstInSplit) != 0) {
                    // return the current result
                    // -> all following classifiers have lower priority
                    if (result != null) {
                        return result.name;
                    }
                    firstInSplit = c;
                }
            }
            var r = c.classify(request);
            if (r == null) continue;
            // check if the new result is more specific
            if (result == null || r.matchedAspects > result.matchedAspects) {
                result = r;
            }
        }
        return result != null ? result.name : null;
    }

    public abstract <T> T accept(Visitor<T> visitor);

    protected abstract boolean isEmpty();

    protected abstract Classifiers classifiers(ClassificationRequest request);

    public static abstract class Node extends Tree {

        public final Threshold threshold;

        public Node(Info info, Threshold threshold) {
            super(info);
            this.threshold = threshold;
        }

        @Override
        protected boolean isEmpty() {
            return false;
        }

        public final static class WithoutChoice extends Node {
            public final Tree lt, eq, gt;

            public WithoutChoice(Info info, Threshold threshold, Tree lt, Tree eq, Tree gt) {
                super(info, threshold);
                this.lt = lt;
                this.eq = eq;
                this.gt = gt;
            }

            @Override
            public Classifiers classifiers(ClassificationRequest request) {
                switch (threshold.compare(request)) {
                    case LT:
                        return lt.classifiers(request);
                    case EQ:
                        return eq.classifiers(request);
                    case GT:
                        return gt.classifiers(request);
                }
                return Classifiers.EMPTY;
            }

            @Override
            public <T> T accept(Visitor<T> visitor) {
                return visitor.visit(this);
            }

            @Override
            public String toString() {
                return "WithoutChoice{" +
                       "threshold=" + threshold +
                       ", info=" + info +
                       '}';
            }
        }

        public final static class WithChoice extends Node {
            public final Tree lt, eq, gt, na;

            public WithChoice(Info info, Threshold threshold, Tree lt, Tree eq, Tree gt, Tree na) {
                super(info, threshold);
                this.lt = lt;
                this.eq = eq;
                this.gt = gt;
                this.na = na;
            }

            @Override
            public Classifiers classifiers(ClassificationRequest request) {
                // the "na" collection must also be considered
                // -> the number of "na" nodes along a "classification path" is small because it is limited
                //    by the number of different aspects that may be absent in some rules
                // -> there are 5 aspects (src/dst port/addr and protocol)
                switch (threshold.compare(request)) {
                    case LT:
                        return lt.classifiers(request).add(na.classifiers(request));
                    case EQ:
                        return eq.classifiers(request).add(na.classifiers(request));
                    case GT:
                        return gt.classifiers(request).add(na.classifiers(request));
                    case NA:
                        // if a request has no value corresponding to the threshold of this node then only the rules
                        // that did not include that value are considered
                        // -> alternatively we could consider all the rules for lt, eq, gt, and na. In that case
                        //    a request that has no corresponding value could match rules that constrain that value
                        return na.classifiers(request);
                    default:
                        return Classifiers.EMPTY; // unexpected
                }
            }

            @Override
            public <T> T accept(Visitor<T> visitor) {
                return visitor.visit(this);
            }

            @Override
            public String toString() {
                return "WithChoice{" +
                       "threshold=" + threshold +
                       ", info=" + info +
                       '}';
            }
        }
    }

    // smart constructor for leaves
    private static Leaf leaf(List<PreprocessedRule> ruleSet, FilterService filterService, Bounds bounds) {
        if (ruleSet.isEmpty()) {
            return Leaf.EMPTY;
        } else if (ruleSet.size() == 1) {
            return new Leaf.WithClassifiers(ruleSet.get(0).createClassifier(filterService, bounds));
        } else {
            var sorted = ruleSet
                    .stream()
                    .map(r -> r.createClassifier(filterService, bounds))
                    .sorted()
                    .collect(Collectors.toList());
            return new Leaf.WithClassifiers(sorted);
        }
    }

    public static abstract class Leaf extends Tree {

        public static final Leaf EMPTY = new Empty();

        public Leaf(Info info) {
            super(info);
        }

        public final static class Empty extends Leaf {
            public Empty() {
                super(Info.forLeaf(0));
            }

            @Override
            public Classifiers classifiers(ClassificationRequest request) {
                return Classifiers.EMPTY;
            }

            @Override
            protected boolean isEmpty() {
                return true;
            }

            @Override
            public <T> T accept(Visitor<T> visitor) {
                return visitor.visit(this);
            }

            @Override
            public String toString() {
                return "Empty{}";
            }
        }

        public final static class WithClassifiers extends Leaf {
            public final List<Classifier> classifiers;

            public WithClassifiers(List<Classifier> classifiers) {
                super(Info.forLeaf(classifiers.size()));
                this.classifiers = classifiers;
            }

            public WithClassifiers(Classifier classifier) {
                this(Collections.singletonList(classifier));
            }

            @Override
            public Classifiers classifiers(ClassificationRequest request) {
                return new Classifiers(true, classifiers);
            }

            @Override
            protected boolean isEmpty() {
                return false;
            }

            @Override
            public <T> T accept(Visitor<T> visitor) {
                return visitor.visit(this);
            }

            @Override
            public String toString() {
                return "WithClassifiers{" +
                       "classifiers=" + classifiers +
                       '}';
            }
        }

    }

    private static class Classifiers {

        private static Classifiers EMPTY = new Classifiers(true, Collections.emptyList());

        // indicates if the classifiers collection is ordered
        // -> allows to avoid unnecessary sorting
        private final boolean ordered;
        private final Collection<Classifier> classifiers;

        private Classifiers(boolean ordered, Collection<Classifier> classifiers) {
            this.ordered = ordered;
            this.classifiers = classifiers;
        }
        public Classifiers add(Classifiers other) {
            if (classifiers.isEmpty()) {
                return other;
            } else if (other.classifiers.isEmpty()) {
                return this;
            } else {
                var c = new ArrayList<Classifier>(classifiers.size() + other.classifiers.size());
                c.addAll(this.classifiers);
                c.addAll(other.classifiers);
                return new Classifiers(false, c);
            }
        }
    }

    public interface Visitor<T> {
        T visit(Node.WithChoice node);
        T visit(Node.WithoutChoice node);
        T visit(Leaf.Empty leaf);
        T visit(Leaf.WithClassifiers leaf);
    }
}
